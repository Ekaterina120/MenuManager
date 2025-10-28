package com.example.menumanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity {

    private List<MenuItem> menuItems = new ArrayList<>(Arrays.asList(
            new MenuItem(1, "Борщ", 350, "Супы"),
            new MenuItem(2, "Солянка", 380, "Супы")
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        setupHeader();
        setupMenuList();
        setupLogoutButton();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Управление меню");
    }

    private void setupMenuList() {
        LinearLayout menuContainer = findViewById(R.id.menuManageContainer);
        menuContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MenuItem item : menuItems) {
            View itemView = inflater.inflate(R.layout.item_manage_menu, menuContainer, false);

            TextView tvName = itemView.findViewById(R.id.tvManageDishName);
            TextView tvPrice = itemView.findViewById(R.id.tvManageDishPrice);
            Button btnEdit = itemView.findViewById(R.id.btnEdit);
            Button btnDelete = itemView.findViewById(R.id.btnDelete);

            tvName.setText(item.getName());
            tvPrice.setText(item.getPrice() + " ₽");

            final MenuItem currentItem = item; // final переменная для использования в listener

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDishDialog(currentItem);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuItems.remove(currentItem);
                    setupMenuList();
                    Toast.makeText(ManageMenuActivity.this, "Блюдо удалено: " + currentItem.getName(), Toast.LENGTH_SHORT).show();
                }
            });

            menuContainer.addView(itemView);
        }

        Button btnAddDish = findViewById(R.id.btnAddDish);
        btnAddDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDishDialog();
            }
        });
    }

    private void showAddDishDialog() {
        final EditText inputName = new EditText(this);
        final EditText inputPrice = new EditText(this);
        final EditText inputCategory = new EditText(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        inputName.setHint("Название блюда");
        inputPrice.setHint("Цена");
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputCategory.setHint("Категория");

        layout.addView(inputName);
        layout.addView(inputPrice);
        layout.addView(inputCategory);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить блюдо")
                .setView(layout)
                .setPositiveButton("Добавить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String name = inputName.getText().toString().trim();
                        String priceStr = inputPrice.getText().toString().trim();
                        String category = inputCategory.getText().toString().trim();

                        if (!name.isEmpty() && !priceStr.isEmpty() && !category.isEmpty()) {
                            try {
                                int price = Integer.parseInt(priceStr);
                                MenuItem newItem = new MenuItem(
                                        menuItems.size() + 1,
                                        name,
                                        price,
                                        category
                                );
                                menuItems.add(newItem);
                                setupMenuList();
                                Toast.makeText(ManageMenuActivity.this, "Блюдо добавлено: " + name, Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Toast.makeText(ManageMenuActivity.this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManageMenuActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditDishDialog(final MenuItem item) {
        final EditText inputName = new EditText(this);
        final EditText inputPrice = new EditText(this);
        final EditText inputCategory = new EditText(this);

        inputName.setText(item.getName());
        inputPrice.setText(String.valueOf(item.getPrice()));
        inputCategory.setText(item.getCategory());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        inputName.setHint("Название блюда");
        inputPrice.setHint("Цена");
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputCategory.setHint("Категория");

        layout.addView(inputName);
        layout.addView(inputPrice);
        layout.addView(inputCategory);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать блюдо")
                .setView(layout)
                .setPositiveButton("Сохранить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String name = inputName.getText().toString().trim();
                        String priceStr = inputPrice.getText().toString().trim();
                        String category = inputCategory.getText().toString().trim();

                        if (!name.isEmpty() && !priceStr.isEmpty() && !category.isEmpty()) {
                            try {
                                int price = Integer.parseInt(priceStr);
                                // Обновляем элемент в списке
                                int index = menuItems.indexOf(item);
                                if (index != -1) {
                                    menuItems.set(index, new MenuItem(item.getId(), name, price, category));
                                }
                                setupMenuList();
                                Toast.makeText(ManageMenuActivity.this, "Блюдо обновлено: " + name, Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Toast.makeText(ManageMenuActivity.this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManageMenuActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMenuActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}