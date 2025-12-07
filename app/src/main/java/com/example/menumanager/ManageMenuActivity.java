package com.example.menumanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity {

    private static final String TAG = "ManageMenuActivity";
    private DatabaseHelper dbHelper;
    private List<MenuItem> dishes = new ArrayList<>();
    private DishAdapter dishAdapter;
    private TextView tvStats;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== onCreate началось ===");

        try {
            setContentView(R.layout.activity_manage_menu);
            Log.d(TAG, "setContentView выполнен успешно");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в setContentView: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка загрузки интерфейса", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            dbHelper = DatabaseHelper.getInstance(this);
            Log.d(TAG, "DatabaseHelper получен");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения DatabaseHelper: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации БД", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupHeader();
        setupStats();
        setupRecyclerView();
        setupFloatingButton();
        loadDishes();

        Log.d(TAG, "=== onCreate завершено ===");
    }

    private void setupHeader() {
        Log.d(TAG, "setupHeader началось");
        try {
            TextView tvHeader = findViewById(R.id.tvHeader);
            if (tvHeader != null) {
                tvHeader.setText("👑 Панель администратора");
                Log.d(TAG, "Заголовок установлен");
            }

            Button btnLogout = findViewById(R.id.btnLogout);
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    startActivity(new Intent(this, RoleSelectionActivity.class));
                    finish();
                });
                Log.d(TAG, "Кнопка выхода настроена");
            }

            // Кнопки навигации - только две вкладки
            Button btnDishes = findViewById(R.id.btnDishes);
            Button btnStats = findViewById(R.id.btnStats);

            if (btnDishes != null && btnStats != null) {
                btnDishes.setOnClickListener(v -> showDishesTab());
                btnStats.setOnClickListener(v -> showStatsTab());

                // Скрываем кнопку стоп-листа
                Button btnStopList = findViewById(R.id.btnStopList);
                if (btnStopList != null) {
                    btnStopList.setVisibility(View.GONE);
                }

                btnDishes.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                btnDishes.setTextColor(Color.WHITE);
                btnStats.setBackgroundColor(Color.parseColor("#F5F5F5"));
                btnStats.setTextColor(Color.BLACK);
                Log.d(TAG, "Кнопки навигации настроены");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в setupHeader: " + e.getMessage(), e);
        }
        Log.d(TAG, "setupHeader завершено");
    }

    private void setupStats() {
        Log.d(TAG, "setupStats началось");
        try {
            tvStats = findViewById(R.id.tvStats);
            if (tvStats != null) {
                tvStats.setText("📊 Загрузка статистики...");
                updateStats();
                Log.d(TAG, "Статистика настроена");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в setupStats: " + e.getMessage(), e);
        }
        Log.d(TAG, "setupStats завершено");
    }

    private void updateStats() {
        Log.d(TAG, "updateStats началось");
        new Thread(() -> {
            try {
                Log.d(TAG, "Запуск подсчета статистики в фоне...");
                final int totalDishes = dbHelper.getDishCount();
                final int stopListCount = dbHelper.getStopListCount();

                runOnUiThread(() -> {
                    try {
                        if (tvStats != null) {
                            tvStats.setText(String.format("📊 Статистика\n\n" +
                                            "Всего блюд: %d\n" +
                                            "В стоп-листе: %d\n" +
                                            "Доступно: %d",
                                    totalDishes, stopListCount, totalDishes - stopListCount));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка обновления UI статистики: " + e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в updateStats: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (tvStats != null) {
                        tvStats.setText("❌ Ошибка загрузки статистики\n" + e.getMessage());
                    }
                });
            }
        }).start();
        Log.d(TAG, "updateStats завершено");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView началось");
        try {
            RecyclerView rvDishes = findViewById(R.id.rvDishes);
            if (rvDishes != null) {
                rvDishes.setLayoutManager(new LinearLayoutManager(this));
                dishAdapter = new DishAdapter();
                rvDishes.setAdapter(dishAdapter);
                Log.d(TAG, "RecyclerView настроен");
            }

            tvEmpty = findViewById(R.id.tvEmpty);
            if (tvEmpty != null) {
                tvEmpty.setText("Загрузка блюд...");
                Log.d(TAG, "tvEmpty найден");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в setupRecyclerView: " + e.getMessage(), e);
        }
        Log.d(TAG, "setupRecyclerView завершено");
    }

    private void setupFloatingButton() {
        Log.d(TAG, "setupFloatingButton началось");
        try {
            FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
            if (fabAdd != null) {
                fabAdd.setOnClickListener(v -> showAddDishDialog());
                Log.d(TAG, "FloatingActionButton настроен");
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в setupFloatingButton: " + e.getMessage(), e);
        }
        Log.d(TAG, "setupFloatingButton завершено");
    }

    private void loadDishes() {
        Log.d(TAG, "loadDishes началось");
        if (tvEmpty != null) {
            tvEmpty.setText("Загрузка блюд...");
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "Загрузка блюд в фоновом потоке...");
                dishes = dbHelper.getAllDishes();
                Log.d(TAG, "Загружено блюд: " + dishes.size());

                runOnUiThread(() -> {
                    try {
                        if (dishAdapter != null) {
                            dishAdapter.notifyDataSetChanged();
                        }
                        updateEmptyState();
                        updateStats();
                        Log.d(TAG, "UI обновлен с " + dishes.size() + " блюдами");
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка обновления UI: " + e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в loadDishes: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (tvEmpty != null) {
                        tvEmpty.setText("❌ Ошибка загрузки блюд\n" + e.getMessage());
                    }
                    Toast.makeText(ManageMenuActivity.this,
                            "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
        Log.d(TAG, "loadDishes завершено (запущен поток)");
    }

    private void updateEmptyState() {
        Log.d(TAG, "updateEmptyState: блюд = " + dishes.size());
        if (tvEmpty != null) {
            if (dishes.isEmpty()) {
                tvEmpty.setText("Нет блюд для отображения\n\nНажмите + чтобы добавить");
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                tvEmpty.setVisibility(View.GONE);
            }
        }
    }

    private void showDishesTab() {
        Log.d(TAG, "Показана вкладка блюд");
        try {
            View llDishesTab = findViewById(R.id.llDishesTab);
            View llStatsTab = findViewById(R.id.llStatsTab);

            if (llDishesTab != null) llDishesTab.setVisibility(View.VISIBLE);
            if (llStatsTab != null) llStatsTab.setVisibility(View.GONE);

            loadDishes();

            // Обновляем кнопки
            Button btnDishes = findViewById(R.id.btnDishes);
            Button btnStats = findViewById(R.id.btnStats);

            if (btnDishes != null && btnStats != null) {
                btnDishes.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                btnDishes.setTextColor(Color.WHITE);
                btnStats.setBackgroundColor(Color.parseColor("#F5F5F5"));
                btnStats.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в showDishesTab: " + e.getMessage(), e);
        }
    }

    private void showStatsTab() {
        Log.d(TAG, "Показана вкладка статистики");
        try {
            View llDishesTab = findViewById(R.id.llDishesTab);
            View llStatsTab = findViewById(R.id.llStatsTab);

            if (llDishesTab != null) llDishesTab.setVisibility(View.GONE);
            if (llStatsTab != null) llStatsTab.setVisibility(View.VISIBLE);

            updateStats();

            // Обновляем кнопки
            Button btnDishes = findViewById(R.id.btnDishes);
            Button btnStats = findViewById(R.id.btnStats);

            if (btnDishes != null && btnStats != null) {
                btnDishes.setBackgroundColor(Color.parseColor("#F5F5F5"));
                btnDishes.setTextColor(Color.BLACK);
                btnStats.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                btnStats.setTextColor(Color.WHITE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в showStatsTab: " + e.getMessage(), e);
        }
    }

    // ==================== ДИАЛОГИ ДЛЯ РАБОТЫ С БЛЮДАМИ ====================

    private void showAddDishDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("➕ Добавить блюдо");

            // Создаем layout для диалога
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 30, 50, 30);

            final EditText etName = new EditText(this);
            etName.setHint("Название блюда");
            etName.setPadding(20, 20, 20, 20);
            etName.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

            final EditText etPrice = new EditText(this);
            etPrice.setHint("Цена (₽)");
            etPrice.setPadding(20, 20, 20, 20);
            etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etPrice.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

            layout.addView(etName);
            layout.addView(etPrice);

            builder.setView(layout)
                    .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = etName.getText().toString().trim();
                            String priceStr = etPrice.getText().toString().trim();

                            if (name.isEmpty() || priceStr.isEmpty()) {
                                Toast.makeText(ManageMenuActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                double price = Double.parseDouble(priceStr);
                                new Thread(() -> {
                                    try {
                                        boolean success = dbHelper.addDish(name, price, 1, 1);
                                        runOnUiThread(() -> {
                                            if (success) {
                                                Toast.makeText(ManageMenuActivity.this,
                                                        "✅ Блюдо добавлено", Toast.LENGTH_SHORT).show();
                                                loadDishes();
                                            } else {
                                                Toast.makeText(ManageMenuActivity.this,
                                                        "❌ Ошибка добавления", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Ошибка добавления блюда: " + e.getMessage(), e);
                                        runOnUiThread(() ->
                                                Toast.makeText(ManageMenuActivity.this,
                                                        "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                    }
                                }).start();
                            } catch (NumberFormatException e) {
                                Toast.makeText(ManageMenuActivity.this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в showAddDishDialog: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка открытия диалога", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDishDialog(final MenuItem dish) {
        try {
            Log.d(TAG, "Открытие диалога редактирования для блюда: " + dish.getName() + " (ID: " + dish.getId() + ")");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("✏️ Редактировать: " + dish.getName());

            // Создаем layout для диалога
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 30, 50, 30);

            final EditText etName = new EditText(this);
            etName.setText(dish.getName());
            etName.setPadding(20, 20, 20, 20);
            etName.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

            final EditText etPrice = new EditText(this);
            etPrice.setText(String.valueOf(dish.getPrice()));
            etPrice.setPadding(20, 20, 20, 20);
            etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etPrice.setBackground(getResources().getDrawable(android.R.drawable.edit_text));

            layout.addView(etName);
            layout.addView(etPrice);

            builder.setView(layout)
                    .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = etName.getText().toString().trim();
                            String priceStr = etPrice.getText().toString().trim();

                            if (name.isEmpty() || priceStr.isEmpty()) {
                                Toast.makeText(ManageMenuActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                // Проверяем корректность цены
                                double price;
                                try {
                                    price = Double.parseDouble(priceStr);
                                    if (price < 0) {
                                        Toast.makeText(ManageMenuActivity.this, "Цена не может быть отрицательной", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    if (price > 100000) {
                                        Toast.makeText(ManageMenuActivity.this, "Цена слишком большая", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(ManageMenuActivity.this, "Введите корректную цену (например: 150.50)", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Показываем прогресс
                                Toast.makeText(ManageMenuActivity.this, "Сохранение изменений...", Toast.LENGTH_SHORT).show();

                                new Thread(() -> {
                                    try {
                                        Log.d(TAG, "Попытка обновить блюдо ID: " + dish.getId() +
                                                ", новое имя: " + name + ", новая цена: " + price);

                                        // Используем простую версию обновления
                                        boolean success = dbHelper.updateDishSimple(dish.getId(), name, price);

                                        runOnUiThread(() -> {
                                            if (success) {
                                                Toast.makeText(ManageMenuActivity.this,
                                                        "✅ Изменения сохранены", Toast.LENGTH_SHORT).show();
                                                // Перезагружаем список
                                                loadDishes();
                                            } else {
                                                Toast.makeText(ManageMenuActivity.this,
                                                        "❌ Ошибка сохранения. Проверьте логи.", Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Обновление не удалось для блюда ID: " + dish.getId());
                                            }
                                        });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Ошибка обновления блюда в потоке: " + e.getMessage(), e);
                                        runOnUiThread(() -> {
                                            Toast.makeText(ManageMenuActivity.this,
                                                    "❌ Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }).start();

                            } catch (Exception e) {
                                Log.e(TAG, "Общая ошибка при сохранении: " + e.getMessage(), e);
                                Toast.makeText(ManageMenuActivity.this,
                                        "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .setNeutralButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDeleteConfirmation(dish);
                        }
                    })
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Ошибка в showEditDishDialog: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка открытия диалога: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void showDeleteConfirmation(final MenuItem dish) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("🗑️ Удаление блюда")
                    .setMessage("Удалить \"" + dish.getName() + "\"?")
                    .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(() -> {
                                try {
                                    boolean success = dbHelper.deleteDish(dish.getId());
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(ManageMenuActivity.this,
                                                    "✅ Блюдо удалено", Toast.LENGTH_SHORT).show();
                                            loadDishes();
                                        } else {
                                            Toast.makeText(ManageMenuActivity.this,
                                                    "❌ Ошибка удаления", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e(TAG, "Ошибка удаления блюда: " + e.getMessage(), e);
                                    runOnUiThread(() ->
                                            Toast.makeText(ManageMenuActivity.this,
                                                    "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                }
                            }).start();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Ошибка в showDeleteConfirmation: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка открытия диалога", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== АДАПТЕР ДЛЯ СПИСКА БЛЮД ====================

    private class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

        @Override
        public DishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            try {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_dish_admin, parent, false);
                return new DishViewHolder(view);
            } catch (Exception e) {
                Log.e(TAG, "Ошибка создания ViewHolder: " + e.getMessage(), e);
                // Создаем простой view программно
                LinearLayout layout = new LinearLayout(parent.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(20, 20, 20, 20);
                layout.setBackgroundColor(Color.WHITE);

                TextView tvName = new TextView(parent.getContext());
                tvName.setId(R.id.tvDishName);
                tvName.setTextSize(18);
                tvName.setTextColor(Color.BLACK);

                TextView tvPrice = new TextView(parent.getContext());
                tvPrice.setId(R.id.tvDishPrice);
                tvPrice.setTextSize(16);
                tvPrice.setTextColor(Color.GRAY);

                TextView tvStatus = new TextView(parent.getContext());
                tvStatus.setId(R.id.tvDishStatus);
                tvStatus.setTextSize(14);

                layout.addView(tvName);
                layout.addView(tvPrice);
                layout.addView(tvStatus);

                return new DishViewHolder(layout);
            }
        }

        @Override
        public void onBindViewHolder(DishViewHolder holder, int position) {
            try {
                if (position < dishes.size()) {
                    MenuItem dish = dishes.get(position);
                    holder.bind(dish);
                }
            } catch (Exception e) {
                Log.e(TAG, "Ошибка в onBindViewHolder: " + e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return dishes.size();
        }

        class DishViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvPrice, tvStatus;
            private View cardView;
            private Button btnToggleStopList;

            public DishViewHolder(View itemView) {
                super(itemView);
                try {
                    tvName = itemView.findViewById(R.id.tvDishName);
                    tvPrice = itemView.findViewById(R.id.tvDishPrice);
                    tvStatus = itemView.findViewById(R.id.tvDishStatus);
                    cardView = itemView.findViewById(R.id.cardDish);
                    btnToggleStopList = itemView.findViewById(R.id.btnToggleStopList);

                    // Если кнопки нет в layout, создаем ее программно
                    if (btnToggleStopList == null && itemView instanceof LinearLayout) {
                        btnToggleStopList = new Button(itemView.getContext());
                        btnToggleStopList.setText("Стоп-лист");
                        btnToggleStopList.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        ((LinearLayout) itemView).addView(btnToggleStopList);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка в конструкторе DishViewHolder: " + e.getMessage(), e);
                }
            }

            public void bind(final MenuItem dish) {
                try {
                    if (tvName != null) tvName.setText(dish.getName());
                    if (tvPrice != null) tvPrice.setText(String.format("%.0f ₽", dish.getPrice()));

                    // Сначала устанавливаем дефолтный статус
                    if (tvStatus != null) {
                        tvStatus.setText("⏳ Проверка...");
                        tvStatus.setTextColor(Color.GRAY);
                    }

                    if (cardView != null) {
                        cardView.setBackgroundColor(Color.parseColor("#F5F5F5"));
                    }

                    // Проверяем наличие в стоп-листе
                    new Thread(() -> {
                        try {
                            final boolean inStopList = dbHelper.isInStopList(dish.getId());
                            runOnUiThread(() -> {
                                try {
                                    if (tvStatus != null) {
                                        if (inStopList) {
                                            tvStatus.setText("⛔ В стоп-листе");
                                            tvStatus.setTextColor(Color.RED);
                                            if (cardView != null) {
                                                cardView.setBackgroundColor(Color.parseColor("#FFF5F5"));
                                            }
                                            if (btnToggleStopList != null) {
                                                btnToggleStopList.setText("✅ Восстановить");
                                                btnToggleStopList.setBackgroundColor(Color.parseColor("#4CAF50"));
                                            }
                                        } else {
                                            tvStatus.setText("✅ Доступно");
                                            tvStatus.setTextColor(Color.GREEN);
                                            if (cardView != null) {
                                                cardView.setBackgroundColor(Color.parseColor("#F5F5F5"));
                                            }
                                            if (btnToggleStopList != null) {
                                                btnToggleStopList.setText("⛔ В стоп-лист");
                                                btnToggleStopList.setBackgroundColor(Color.parseColor("#F44336"));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Ошибка обновления статуса UI: " + e.getMessage(), e);
                                }
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Ошибка проверки стоп-листа: " + e.getMessage(), e);
                        }
                    }).start();

                    // Обработчик клика на элемент (редактирование)
                    if (itemView != null) {
                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showEditDishDialog(dish);
                            }
                        });
                    }

                    // Обработчик кнопки стоп-листа
                    if (btnToggleStopList != null) {
                        btnToggleStopList.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new Thread(() -> {
                                    try {
                                        final boolean inStopList = dbHelper.isInStopList(dish.getId());
                                        runOnUiThread(() -> {
                                            if (inStopList) {
                                                // Удалить из стоп-листа
                                                new Thread(() -> {
                                                    try {
                                                        boolean success = dbHelper.removeFromStopList(dish.getId());
                                                        runOnUiThread(() -> {
                                                            if (success) {
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "✅ Блюдо восстановлено", Toast.LENGTH_SHORT).show();
                                                                loadDishes();
                                                            } else {
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "❌ Ошибка восстановления", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Ошибка удаления из стоп-листа: " + e.getMessage(), e);
                                                        runOnUiThread(() ->
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                    }
                                                }).start();
                                            } else {
                                                // Добавить в стоп-лист
                                                new Thread(() -> {
                                                    try {
                                                        boolean success = dbHelper.addToStopList(dish.getId(), dish.getName(), dish.getPrice());
                                                        runOnUiThread(() -> {
                                                            if (success) {
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "⛔ Блюдо добавлено в стоп-лист", Toast.LENGTH_SHORT).show();
                                                                loadDishes();
                                                            } else {
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "❌ Ошибка добавления в стоп-лист", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Ошибка добавления в стоп-лист: " + e.getMessage(), e);
                                                        runOnUiThread(() ->
                                                                Toast.makeText(ManageMenuActivity.this,
                                                                        "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                    }
                                                }).start();
                                            }
                                        });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Ошибка обработки кнопки стоп-листа: " + e.getMessage(), e);
                                    }
                                }).start();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка в bind: " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume вызван");
        loadDishes();
    }
}