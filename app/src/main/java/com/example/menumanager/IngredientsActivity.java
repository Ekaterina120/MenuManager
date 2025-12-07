package com.example.menumanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.ArrayList;

public class IngredientsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<Ingredient> ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);

        dbHelper = DatabaseHelper.getInstance(this);
        ingredients = new ArrayList<>();

        setupHeader();
        loadIngredients();

        Toast.makeText(this, "Управление ингредиентами", Toast.LENGTH_SHORT).show();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("🥕 Управление ингредиентами");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        Button btnAdd = findViewById(R.id.btnAddIngredient);
        btnAdd.setOnClickListener(v -> {
            showAddIngredientDialog();
        });
    }

    private void loadIngredients() {
        LinearLayout container = findViewById(R.id.ingredientsContainer);
        if (container == null) {
            Toast.makeText(this, "Контейнер не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        container.removeAllViews();

        ingredients = dbHelper.getAllIngredients();

        if (ingredients.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Нет ингредиентов");
            emptyText.setTextSize(18);
            emptyText.setTextColor(getResources().getColor(R.color.gray));
            emptyText.setGravity(android.view.Gravity.CENTER);
            container.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final Ingredient ingredient : ingredients) {
            View itemView = inflater.inflate(R.layout.item_ingredient, container, false);

            TextView tvName = itemView.findViewById(R.id.tvIngredientName);
            TextView tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
            TextView tvStatus = itemView.findViewById(R.id.tvIngredientStatus);
            Button btnUpdate = itemView.findViewById(R.id.btnUpdateIngredient);
            Button btnDelete = itemView.findViewById(R.id.btnDeleteIngredient);

            tvName.setText(ingredient.getName());
            tvQuantity.setText(String.format("Количество: %.1f", ingredient.getQuantity()));

            // Определяем статус
            if (ingredient.getQuantity() < 3) {
                tvStatus.setText("⚠️ МАЛО");
                tvStatus.setTextColor(getResources().getColor(R.color.red));
            } else if (ingredient.getQuantity() < 10) {
                tvStatus.setText("⚠️ СРЕДНЕ");
                tvStatus.setTextColor(getResources().getColor(R.color.orange));
            } else {
                tvStatus.setText("✓ НОРМА");
                tvStatus.setTextColor(getResources().getColor(R.color.green));
            }

            btnUpdate.setOnClickListener(v -> showUpdateIngredientDialog(ingredient));
            btnDelete.setOnClickListener(v -> showDeleteIngredientConfirmation(ingredient));

            container.addView(itemView);
        }
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Добавить ингредиент");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingredient, null);

        final EditText etName = dialogView.findViewById(R.id.etIngredientName);
        final EditText etQuantity = dialogView.findViewById(R.id.etIngredientQuantity);

        builder.setView(dialogView)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();

                    if (name.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double quantity = Double.parseDouble(quantityStr);
                        if (quantity < 0) {
                            Toast.makeText(this, "Количество не может быть отрицательным", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean success = dbHelper.addIngredient(name, quantity);

                        if (success) {
                            Toast.makeText(this, "✅ Ингредиент добавлен", Toast.LENGTH_SHORT).show();
                            loadIngredients();
                        } else {
                            Toast.makeText(this, "❌ Ошибка добавления", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите число в количестве", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showUpdateIngredientDialog(final Ingredient ingredient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📝 Изменить: " + ingredient.getName());

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_ingredient, null);

        final EditText etQuantity = dialogView.findViewById(R.id.etNewQuantity);
        etQuantity.setText(String.valueOf(ingredient.getQuantity()));

        builder.setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        double newQuantity = Double.parseDouble(etQuantity.getText().toString());
                        if (newQuantity < 0) {
                            Toast.makeText(this, "Количество не может быть отрицательным", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean success = dbHelper.updateIngredientQuantity(
                                ingredient.getId(), newQuantity);

                        if (success) {
                            Toast.makeText(this, "✅ Количество обновлено", Toast.LENGTH_SHORT).show();
                            loadIngredients();
                        } else {
                            Toast.makeText(this, "❌ Ошибка обновления", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите число", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteIngredientConfirmation(final Ingredient ingredient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🗑️ Удаление ингредиента")
                .setMessage("Удалить \"" + ingredient.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    boolean success = dbHelper.deleteIngredient(ingredient.getId());
                    if (success) {
                        Toast.makeText(this, "✅ Ингредиент удален", Toast.LENGTH_SHORT).show();
                        loadIngredients();
                    } else {
                        Toast.makeText(this, "❌ Ошибка удаления", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIngredients();
    }
}