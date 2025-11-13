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

public class StopListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<StopListItem> stopListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_list);

        // Простая инициализация базы данных
        dbHelper = new DatabaseHelper(this);

        setupHeader();
        setupStopList();
        setupBottomNavigation();
        setupLogoutButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список при возвращении на экран
        setupStopList();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Стоп-лист");

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStopListItemDialog();
            }
        });
    }

    private void showAddStopListItemDialog() {
        final EditText inputDishName = new EditText(this);
        final EditText inputPrice = new EditText(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        inputDishName.setHint("Название блюда");
        inputPrice.setHint("Цена");
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        layout.addView(inputDishName);
        layout.addView(inputPrice);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить в стоп-лист")
                .setView(layout)
                .setPositiveButton("Добавить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String dishName = inputDishName.getText().toString().trim();
                        String priceStr = inputPrice.getText().toString().trim();

                        if (!dishName.isEmpty() && !priceStr.isEmpty()) {
                            try {
                                double price = Double.parseDouble(priceStr);
                                // Получаем максимальный dish_id
                                List<StopListItem> currentList = dbHelper.getStopList();
                                int newDishId = 1;
                                if (!currentList.isEmpty()) {
                                    newDishId = currentList.get(currentList.size() - 1).getId() + 1;
                                }

                                boolean success = dbHelper.addToStopList(newDishId, dishName, price);
                                if (success) {
                                    setupStopList();
                                    Toast.makeText(StopListActivity.this, "Блюдо добавлено в стоп-лист", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(StopListActivity.this, "Ошибка добавления", Toast.LENGTH_SHORT).show();
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(StopListActivity.this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StopListActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupStopList() {
        LinearLayout stopListContainer = findViewById(R.id.stopListContainer);
        stopListContainer.removeAllViews();

        // Получаем стоп-лист из БД
        stopListItems = dbHelper.getStopList();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final StopListItem item : stopListItems) {
            View itemView = inflater.inflate(R.layout.item_stop_list, stopListContainer, false);

            TextView tvDishName = itemView.findViewById(R.id.tvStopListDishName);
            TextView tvReason = itemView.findViewById(R.id.tvStopListReason);
            Button btnRemove = itemView.findViewById(R.id.btnRemove);

            tvDishName.setText(item.getDishName());
            tvReason.setText(item.getReason());

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRemoveFromStopListConfirmation(item);
                }
            });

            stopListContainer.addView(itemView);
        }
    }

    private void showRemoveFromStopListConfirmation(final StopListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Восстановление блюда")
                .setMessage("Вы уверены, что хотите вернуть блюдо \"" + item.getDishName() + "\" в основное меню?")
                .setPositiveButton("Вернуть в меню", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // Получаем цену из стоп-листа
                        double price = dbHelper.getPriceFromStopList(item.getId());

                        // Добавляем блюдо в основное меню
                        boolean addSuccess = dbHelper.addDishToMenu(item.getId(), item.getDishName(), price);

                        // Удаляем из стоп-листа
                        boolean removeSuccess = dbHelper.removeFromStopList(item.getId());

                        if (addSuccess && removeSuccess) {
                            setupStopList();
                            Toast.makeText(StopListActivity.this, "Блюдо \"" + item.getDishName() + "\" возвращено в меню", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StopListActivity.this, "Ошибка при восстановлении блюда", Toast.LENGTH_SHORT).show();
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

        // Активная кнопка (Стоп-лист)
        btnStopList.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnStopList.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnStopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в стоп-листе
            }
        });

        btnIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopListActivity.this, IngredientsActivity.class);
                startActivity(intent);
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopListActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}