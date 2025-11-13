package com.example.menumanager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.List;
import java.util.ArrayList;

public class ManageMenuActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<MenuItem> menuItems;
    private BroadcastReceiver stopListUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        dbHelper = new DatabaseHelper(this);
        menuItems = new ArrayList<>();

        setupBroadcastReceiver();
        setupHeader();
        setupBottomNavigation();
        setupMenuList();
    }

    private void setupBroadcastReceiver() {
        stopListUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("STOP_LIST_UPDATED".equals(intent.getAction())) {
                    setupMenuList();
                }
            }
        };

        IntentFilter filter = new IntentFilter("STOP_LIST_UPDATED");
        LocalBroadcastManager.getInstance(this).registerReceiver(stopListUpdateReceiver, filter);
    }

    private void sendStopListUpdateBroadcast() {
        Intent intent = new Intent("STOP_LIST_UPDATED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMenuList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stopListUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(stopListUpdateReceiver);
        }
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Управление меню");


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

    private void setupBottomNavigation() {
        TextView btnStopList = findViewById(R.id.btnStopListNav);
        TextView btnMenu = findViewById(R.id.btnMenuNav);

        // УБИРАЕМ кнопку ингредиентов для администратора
        TextView btnIngredients = findViewById(R.id.btnIngredientsNav);
        if (btnIngredients != null) {
            btnIngredients.setVisibility(View.GONE);
        }

        if (btnStopList != null && btnMenu != null) {
            // Активная кнопка (Меню)
            btnMenu.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
            btnMenu.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnStopList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ManageMenuActivity.this, AdminStopListActivity.class);
                    startActivity(intent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Уже в меню
                }
            });
        }
    }

    private void setupMenuList() {
        LinearLayout menuContainer = findViewById(R.id.menuManageContainer);
        if (menuContainer == null) {
            Toast.makeText(this, "Ошибка: контейнер меню не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        menuContainer.removeAllViews();

        // Получаем только доступные блюда (не в стоп-листе)
        menuItems = dbHelper.getAvailableDishes();

        if (menuItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Меню пустое");
            emptyText.setTextSize(18);
            emptyText.setTextColor(android.graphics.Color.BLACK);
            emptyText.setPadding(0, 50, 0, 0);
            emptyText.setGravity(android.view.Gravity.CENTER);
            menuContainer.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final MenuItem item : menuItems) {
            View itemView = inflater.inflate(R.layout.item_manage_menu, menuContainer, false);

            TextView tvName = itemView.findViewById(R.id.tvManageDishName);
            TextView tvPrice = itemView.findViewById(R.id.tvManageDishPrice);
            Button btnEdit = itemView.findViewById(R.id.btnEdit);
            Button btnDelete = itemView.findViewById(R.id.btnDelete);
            Button btnAddToStopList = itemView.findViewById(R.id.btnAddToStopList);

            tvName.setText(item.getName());
            tvPrice.setText(item.getPrice() + " ₽");

            // Все блюда здесь доступны для добавления в стоп-лист
            btnAddToStopList.setText("В СТОП-ЛИСТ");
            btnAddToStopList.setBackgroundColor(android.graphics.Color.GREEN);

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditDishDialog(item);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(item);
                }
            });

            btnAddToStopList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddToStopListConfirmation(item);
                }
            });

            menuContainer.addView(itemView);
        }
    }

    private void showAddToStopListConfirmation(final MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавление в стоп-лист")
                .setMessage("Вы уверены, что хотите добавить блюдо \"" + item.getName() + "\" в стоп-лист?")
                .setPositiveButton("Добавить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // Добавляем в стоп-лист и скрываем из меню
                        boolean success = dbHelper.addToStopListAndHide(item.getId(), item.getName(), item.getPrice());

                        if (success) {
                            // Мгновенно обновляем список
                            setupMenuList();
                            Toast.makeText(ManageMenuActivity.this, "Блюдо \"" + item.getName() + "\" добавлено в стоп-лист", Toast.LENGTH_SHORT).show();

                            // Отправляем broadcast для обновления стоп-листа
                            sendStopListUpdateBroadcast();
                        } else {
                            Toast.makeText(ManageMenuActivity.this, "Ошибка добавления в стоп-лист", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDeleteConfirmationDialog(final MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить блюдо \"" + item.getName() + "\"?")
                .setPositiveButton("Удалить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        boolean success = dbHelper.deleteDish(item.getId());
                        if (success) {
                            // Также удаляем из стоп-листа если есть
                            dbHelper.removeFromStopList(item.getId());
                            setupMenuList();
                            Toast.makeText(ManageMenuActivity.this, "Блюдо удалено: " + item.getName(), Toast.LENGTH_SHORT).show();
                            sendStopListUpdateBroadcast();
                        } else {
                            Toast.makeText(ManageMenuActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditDishDialog(final MenuItem item) {
        final EditText inputName = new EditText(this);
        final EditText inputPrice = new EditText(this);

        inputName.setText(item.getName());
        inputPrice.setText(String.valueOf(item.getPrice()));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        inputName.setHint("Название блюда");
        inputPrice.setHint("Цена");
        inputPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        layout.addView(inputName);
        layout.addView(inputPrice);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать блюдо")
                .setView(layout)
                .setPositiveButton("Сохранить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String name = inputName.getText().toString().trim();
                        String priceStr = inputPrice.getText().toString().trim();

                        if (!name.isEmpty() && !priceStr.isEmpty()) {
                            try {
                                double price = Double.parseDouble(priceStr);
                                boolean success = dbHelper.updateDish(item.getId(), name, price, "Основное меню");
                                if (success) {
                                    setupMenuList();
                                    Toast.makeText(ManageMenuActivity.this, "Блюдо обновлено: " + name, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ManageMenuActivity.this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
                                }
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
}