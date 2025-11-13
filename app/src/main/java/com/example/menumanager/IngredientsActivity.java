package com.example.menumanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.List;

public class IngredientsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<Ingredient> ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);

        dbHelper = new DatabaseHelper(this);

        setupHeader();
        setupIngredientsList();
        setupBottomNavigation();
        setupLogoutButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupIngredientsList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Управление ингредиентами");
    }

    private void setupIngredientsList() {
        LinearLayout ingredientsContainer = findViewById(R.id.ingredientsContainer);
        ingredientsContainer.removeAllViews();

        ingredients = dbHelper.getAllIngredients();

        if (ingredients.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Ингредиенты не найдены");
            emptyText.setTextSize(18);
            emptyText.setTextColor(android.graphics.Color.BLACK);
            emptyText.setPadding(0, 50, 0, 0);
            emptyText.setGravity(android.view.Gravity.CENTER);
            ingredientsContainer.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final Ingredient ingredient : ingredients) {
            View itemView = inflater.inflate(R.layout.item_ingredient, ingredientsContainer, false);

            TextView tvName = itemView.findViewById(R.id.tvIngredientName);
            TextView tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
            Button btnUpdateQuantity = itemView.findViewById(R.id.btnUpdateQuantity);

            tvName.setText(ingredient.getName());
            tvQuantity.setText("Количество: " + ingredient.getQuantity() + " г");

            // Подсвечиваем ингредиенты с низким количеством
            if (ingredient.getQuantity() < 1000) {
                itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFF9C4"));
            }

            btnUpdateQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUpdateQuantityDialog(ingredient);
                }
            });

            // Добавляем долгое нажатие для удаления
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showDeleteIngredientConfirmation(ingredient);
                    return true;
                }
            });

            ingredientsContainer.addView(itemView);
        }
    }

    private void showUpdateQuantityDialog(final Ingredient ingredient) {
        final EditText inputQuantity = new EditText(this);
        inputQuantity.setHint("Новое количество (г)");
        inputQuantity.setText(String.valueOf(ingredient.getQuantity()));
        inputQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputQuantity);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение количества: " + ingredient.getName())
                .setView(layout)
                .setPositiveButton("Сохранить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String quantityStr = inputQuantity.getText().toString().trim();
                        if (!quantityStr.isEmpty()) {
                            try {
                                double newQuantity = Double.parseDouble(quantityStr);
                                boolean success = dbHelper.updateIngredientQuantity(ingredient.getId(), newQuantity);
                                if (success) {
                                    setupIngredientsList();
                                    Toast.makeText(IngredientsActivity.this,
                                            "Количество обновлено: " + newQuantity + " г",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(IngredientsActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(IngredientsActivity.this, "Введите корректное число", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteIngredientConfirmation(final Ingredient ingredient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить ингредиент \"" + ingredient.getName() + "\"?")
                .setPositiveButton("Удалить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        boolean success = dbHelper.deleteIngredient(ingredient.getId());
                        if (success) {
                            setupIngredientsList();
                            Toast.makeText(IngredientsActivity.this,
                                    "Ингредиент удален: " + ingredient.getName(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(IngredientsActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupBottomNavigation() {
        TextView btnStopList = findViewById(R.id.btnStopListNav);
        TextView btnIngredients = findViewById(R.id.btnIngredientsNav);
        TextView btnMenu = findViewById(R.id.btnMenuNav);

        // Активная кнопка (Ингредиенты)
        btnIngredients.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnIngredients.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnStopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход к стоп-листу
                Intent intent = new Intent(IngredientsActivity.this, AdminStopListActivity.class);
                startActivity(intent);
                // НЕ завершаем текущую активность, чтобы можно было вернуться
            }
        });

        btnIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в ингредиентах - ничего не делаем
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход к управлению меню
                Intent intent = new Intent(IngredientsActivity.this, ManageMenuActivity.class);
                startActivity(intent);
                // НЕ завершаем текущую активность, чтобы можно было вернуться
            }
        });
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IngredientsActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish(); // Завершаем только при выходе из приложения
            }
        });
    }
}