package com.example.menumanager;

import android.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ChefActivity extends AppCompatActivity {

    private static final String TAG = "ChefActivity";
    private DatabaseHelper dbHelper;
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<StopListItem> stopList = new ArrayList<>();
    private List<MenuItem> menuDishes = new ArrayList<>();
    private IngredientAdapter ingredientAdapter;
    private StopListAdapter stopListAdapter;
    private MenuAdapter menuAdapter;

    private TextView tvStats;
    private LinearLayout llIngredientsTab, llStopListTab, llStatsTab, llMenuTab;
    private Button btnAddIngredient, btnAddDish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef);

        dbHelper = DatabaseHelper.getInstance(this);

        setupHeader();
        setupTabs();
        setupRecyclerViews();
        setupButtons();
        loadData();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("👨‍🍳 Панель повара");

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
            finish();
        });
    }

    private void setupTabs() {
        Button btnIngredients = findViewById(R.id.btnIngredients);
        Button btnStopList = findViewById(R.id.btnStopList);
        Button btnStats = findViewById(R.id.btnStats);
        Button btnMenu = findViewById(R.id.btnMenu);

        llIngredientsTab = findViewById(R.id.llIngredientsTab);
        llStopListTab = findViewById(R.id.llStopListTab);
        llStatsTab = findViewById(R.id.llStatsTab);
        llMenuTab = findViewById(R.id.llMenuTab);
        tvStats = findViewById(R.id.tvStats);

        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddDish = findViewById(R.id.btnAddDish);

        // Назначаем обработчики кликов
        btnIngredients.setOnClickListener(v -> {
            animateButtonClick(btnIngredients);
            showIngredientsTab();
        });

        btnStopList.setOnClickListener(v -> {
            animateButtonClick(btnStopList);
            showStopListTab();
        });

        btnStats.setOnClickListener(v -> {
            animateButtonClick(btnStats);
            showStatsTab();
        });

        btnMenu.setOnClickListener(v -> {
            animateButtonClick(btnMenu);
            showMenuTab();
        });

        // Начальная вкладка
        showIngredientsTab();
        btnIngredients.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        btnIngredients.setTextColor(Color.WHITE);
    }

    private void setupButtons() {
        // Кнопка добавления ингредиента
        btnAddIngredient.setOnClickListener(v -> {
            showAddIngredientDialog();
        });

        // Кнопка добавления блюда
        btnAddDish.setOnClickListener(v -> {
            showAddDishDialog();
        });
    }

    private void animateButtonClick(Button button) {
        // Сброс всех кнопок
        Button btnIngredients = findViewById(R.id.btnIngredients);
        Button btnStopList = findViewById(R.id.btnStopList);
        Button btnStats = findViewById(R.id.btnStats);
        Button btnMenu = findViewById(R.id.btnMenu);

        int defaultColor = Color.parseColor("#F5F5F5");
        int textColor = Color.BLACK;

        btnIngredients.setBackgroundColor(defaultColor);
        btnIngredients.setTextColor(textColor);

        btnStopList.setBackgroundColor(defaultColor);
        btnStopList.setTextColor(textColor);

        btnStats.setBackgroundColor(defaultColor);
        btnStats.setTextColor(textColor);

        btnMenu.setBackgroundColor(defaultColor);
        btnMenu.setTextColor(textColor);

        // Активация выбранной кнопки
        button.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        button.setTextColor(Color.WHITE);
    }

    private void setupRecyclerViews() {
        // RecyclerView для ингредиентов
        RecyclerView rvIngredients = findViewById(R.id.rvIngredients);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingredientAdapter = new IngredientAdapter();
        rvIngredients.setAdapter(ingredientAdapter);

        // RecyclerView для стоп-листа
        RecyclerView rvStopList = findViewById(R.id.rvStopList);
        rvStopList.setLayoutManager(new LinearLayoutManager(this));
        stopListAdapter = new StopListAdapter();
        rvStopList.setAdapter(stopListAdapter);

        // RecyclerView для меню
        RecyclerView rvMenu = findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter();
        rvMenu.setAdapter(menuAdapter);
    }

    private void showIngredientsTab() {
        llIngredientsTab.setVisibility(View.VISIBLE);
        llStopListTab.setVisibility(View.GONE);
        llStatsTab.setVisibility(View.GONE);
        llMenuTab.setVisibility(View.GONE);
        loadIngredients();
    }

    private void showStopListTab() {
        llIngredientsTab.setVisibility(View.GONE);
        llStopListTab.setVisibility(View.VISIBLE);
        llStatsTab.setVisibility(View.GONE);
        llMenuTab.setVisibility(View.GONE);
        loadStopList();
    }

    private void showStatsTab() {
        llIngredientsTab.setVisibility(View.GONE);
        llStopListTab.setVisibility(View.GONE);
        llStatsTab.setVisibility(View.VISIBLE);
        llMenuTab.setVisibility(View.GONE);
        updateStats();
    }

    private void showMenuTab() {
        llIngredientsTab.setVisibility(View.GONE);
        llStopListTab.setVisibility(View.GONE);
        llStatsTab.setVisibility(View.GONE);
        llMenuTab.setVisibility(View.VISIBLE);
        loadMenuDishes();
    }

    private void loadData() {
        loadIngredients();
        loadMenuDishes();
        updateStats();
    }

    private void loadIngredients() {
        new Thread(() -> {
            ingredients = dbHelper.getAllIngredients();
            runOnUiThread(() -> {
                ingredientAdapter.notifyDataSetChanged();
                updateEmptyState(R.id.tvEmptyIngredients, ingredients.isEmpty());
                updateStats();
            });
        }).start();
    }

    private void loadStopList() {
        new Thread(() -> {
            stopList = dbHelper.getStopList();
            runOnUiThread(() -> {
                stopListAdapter.notifyDataSetChanged();
                updateEmptyState(R.id.tvEmptyStopList, stopList.isEmpty());
            });
        }).start();
    }

    private void loadMenuDishes() {
        new Thread(() -> {
            menuDishes = dbHelper.getAvailableDishes();
            runOnUiThread(() -> {
                menuAdapter.notifyDataSetChanged();
                updateEmptyState(R.id.tvEmptyMenu, menuDishes.isEmpty());
                updateStats();
            });
        }).start();
    }

    private void updateStats() {
        int totalIngredients = ingredients.size();
        int lowIngredients = 0;
        int stopListCount = stopList.size();
        int availableDishes = menuDishes.size();

        // Считаем ингредиенты с низким запасом (менее 5 единиц)
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getQuantity() < 5) {
                lowIngredients++;
            }
        }

        String statsText = String.format("📊 Статистика кухни\n\n" +
                        "Ингредиентов: %d\n" +
                        "Мало осталось (<5): %d\n" +
                        "В стоп-листе: %d\n" +
                        "Доступно блюд: %d\n\n" +
                        "🍽️ Готов к работе!",
                totalIngredients, lowIngredients, stopListCount, availableDishes);

        tvStats.setText(statsText);
    }

    private void updateEmptyState(int textViewId, boolean isEmpty) {
        TextView tvEmpty = findViewById(textViewId);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("➕ Добавить ингредиент");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingredient, null);

        EditText etName = dialogView.findViewById(R.id.etIngredientName);
        EditText etQuantity = dialogView.findViewById(R.id.etIngredientQuantity);

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
                        new Thread(() -> {
                            boolean success = dbHelper.addIngredient(name, quantity);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(ChefActivity.this, "✅ Ингредиент добавлен", Toast.LENGTH_SHORT).show();
                                    loadIngredients();
                                } else {
                                    Toast.makeText(ChefActivity.this, "❌ Ошибка добавления", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите число в количестве", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showAddDishDialog() {
        // Загружаем ингредиенты перед показом диалога
        new Thread(() -> {
            final List<Ingredient> allIngredients = dbHelper.getAllIngredients();

            runOnUiThread(() -> {
                if (allIngredients.isEmpty()) {
                    Toast.makeText(ChefActivity.this, "❌ Сначала добавьте ингредиенты", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("🍽️ Добавить блюдо в меню");

                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_dish, null);

                EditText etDishName = dialogView.findViewById(R.id.etDishName);
                EditText etDishPrice = dialogView.findViewById(R.id.etDishPrice);
                EditText etDishDescription = dialogView.findViewById(R.id.etDishDescription);
                LinearLayout llIngredientsList = dialogView.findViewById(R.id.llIngredientsList);

                // Очищаем список перед заполнением
                llIngredientsList.removeAllViews();

                // Создаем чекбоксы для каждого ингредиента
                for (Ingredient ingredient : allIngredients) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(ingredient.getName() + " (" + ingredient.getQuantity() + " ед.)");
                    checkBox.setTag(ingredient.getId());
                    checkBox.setTextColor(Color.BLACK);
                    checkBox.setPadding(8, 8, 8, 8);
                    checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_orange_dark)));
                    llIngredientsList.addView(checkBox);
                }

                builder.setView(dialogView)
                        .setPositiveButton("Добавить", (dialog, which) -> {
                            String name = etDishName.getText().toString().trim();
                            String priceStr = etDishPrice.getText().toString().trim();
                            String description = etDishDescription.getText().toString().trim();

                            if (name.isEmpty() || priceStr.isEmpty()) {
                                Toast.makeText(this, "Заполните название и цену", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                double price = Double.parseDouble(priceStr);

                                // Собираем выбранные ингредиенты
                                List<Integer> selectedIngredientIds = new ArrayList<>();
                                for (int i = 0; i < llIngredientsList.getChildCount(); i++) {
                                    CheckBox checkBox = (CheckBox) llIngredientsList.getChildAt(i);
                                    if (checkBox.isChecked()) {
                                        selectedIngredientIds.add((Integer) checkBox.getTag());
                                    }
                                }

                                if (selectedIngredientIds.isEmpty()) {
                                    Toast.makeText(this, "❌ Выберите хотя бы один ингредиент", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                new Thread(() -> {
                                    boolean success = dbHelper.addDishWithIngredients(name, price, description, selectedIngredientIds);
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(ChefActivity.this, "✅ Блюдо добавлено в меню", Toast.LENGTH_SHORT).show();
                                            loadMenuDishes();
                                        } else {
                                            Toast.makeText(ChefActivity.this, "❌ Ошибка добавления блюда", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }).start();

                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "❌ Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            });
        }).start();
    }

    private void showUpdateIngredientDialog(Ingredient ingredient) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📝 Изменить: " + ingredient.getName());

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_ingredient, null);

        EditText etQuantity = dialogView.findViewById(R.id.etNewQuantity);
        etQuantity.setText(String.valueOf(ingredient.getQuantity()));

        builder.setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    try {
                        double newQuantity = Double.parseDouble(etQuantity.getText().toString());
                        new Thread(() -> {
                            boolean success = dbHelper.updateIngredientQuantity(ingredient.getId(), newQuantity);
                            runOnUiThread(() -> {
                                if (success) {
                                    Toast.makeText(ChefActivity.this, "✅ Количество обновлено", Toast.LENGTH_SHORT).show();
                                    loadIngredients();
                                } else {
                                    Toast.makeText(ChefActivity.this, "❌ Ошибка обновления", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите число", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showDishDetails(MenuItem dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(dish.getName());

        StringBuilder details = new StringBuilder();
        details.append("💰 Цена: ").append(String.format("%.0f ₽", dish.getPrice())).append("\n\n");

        if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
            details.append("📝 Описание:\n").append(dish.getDescription()).append("\n\n");
        }

        details.append("🥦 Ингредиенты:\nЗагрузка...");

        builder.setMessage(details.toString());
        builder.setPositiveButton("Закрыть", null);

        // Добавляем кнопки управления блюдом
        builder.setNegativeButton("❌ В стоп-лист", (dialog, which) -> {
            moveToStopList(dish);
        });

        builder.setNeutralButton("✏️ Редактировать", (dialog, which) -> {
            showEditDishDialog(dish);
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Загружаем ингредиенты асинхронно
        new Thread(() -> {
            try {
                List<Ingredient> ingredients = dbHelper.getIngredientsForDish(dish.getId());
                StringBuilder updatedDetails = new StringBuilder();
                updatedDetails.append("💰 Цена: ").append(String.format("%.0f ₽", dish.getPrice())).append("\n\n");

                if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                    updatedDetails.append("📝 Описание:\n").append(dish.getDescription()).append("\n\n");
                }

                updatedDetails.append("🥦 Ингредиенты:\n");
                if (ingredients.isEmpty()) {
                    updatedDetails.append("Ингредиенты не указаны");
                } else {
                    for (Ingredient ingredient : ingredients) {
                        updatedDetails.append("• ").append(ingredient.getName())
                                .append(": ").append(ingredient.getQuantity()).append(" ед.\n");
                    }
                }

                runOnUiThread(() -> {
                    if (dialog.isShowing()) {
                        TextView messageView = dialog.findViewById(android.R.id.message);
                        if (messageView != null) {
                            messageView.setText(updatedDetails.toString());
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки ингредиентов: " + e.getMessage());
                runOnUiThread(() -> {
                    if (dialog.isShowing()) {
                        TextView messageView = dialog.findViewById(android.R.id.message);
                        if (messageView != null) {
                            messageView.setText(details.toString().replace("Загрузка...", "❌ Ошибка загрузки ингредиентов"));
                        }
                    }
                });
            }
        }).start();
    }

    private void moveToStopList(MenuItem dish) {
        new AlertDialog.Builder(this)
                .setTitle("⛔ Перемещение в стоп-лист")
                .setMessage("Переместить блюдо \"" + dish.getName() + "\" в стоп-лист?")
                .setPositiveButton("Да", (dialog, which) -> {
                    new Thread(() -> {
                        boolean success = dbHelper.addToStopList(
                                dish.getId(),
                                dish.getName(),
                                dish.getPrice()
                        );
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(ChefActivity.this, "✅ Блюдо перемещено в стоп-лист", Toast.LENGTH_SHORT).show();
                                loadMenuDishes();
                                loadStopList();
                            } else {
                                Toast.makeText(ChefActivity.this, "❌ Ошибка перемещения", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showEditDishDialog(MenuItem dish) {
        // Загружаем ингредиенты перед показом диалога
        new Thread(() -> {
            final List<Ingredient> allIngredients = dbHelper.getAllIngredients();
            final List<Ingredient> dishIngredients = dbHelper.getIngredientsForDish(dish.getId());

            // Собираем ID текущих ингредиентов блюда
            Set<Integer> currentIngredientIds = new HashSet<>();
            for (Ingredient ingredient : dishIngredients) {
                currentIngredientIds.add(ingredient.getId());
            }

            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("✏️ Редактировать: " + dish.getName());

                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_dish, null);

                EditText etDishName = dialogView.findViewById(R.id.etDishName);
                EditText etDishPrice = dialogView.findViewById(R.id.etDishPrice);
                EditText etDishDescription = dialogView.findViewById(R.id.etDishDescription);
                LinearLayout llIngredientsList = dialogView.findViewById(R.id.llIngredientsList);

                // Очищаем список перед заполнением
                llIngredientsList.removeAllViews();

                // Заполняем текущие данные
                etDishName.setText(dish.getName());
                etDishPrice.setText(String.valueOf(dish.getPrice()));
                etDishDescription.setText(dish.getDescription());

                // Создаем чекбоксы для каждого ингредиента
                for (Ingredient ingredient : allIngredients) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(ingredient.getName() + " (" + ingredient.getQuantity() + " ед.)");
                    checkBox.setTag(ingredient.getId());
                    checkBox.setTextColor(Color.BLACK);
                    checkBox.setPadding(8, 8, 8, 8);
                    checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.holo_orange_dark)));

                    // Отмечаем текущие ингредиенты
                    if (currentIngredientIds.contains(ingredient.getId())) {
                        checkBox.setChecked(true);
                    }

                    llIngredientsList.addView(checkBox);
                }

                builder.setView(dialogView)
                        .setPositiveButton("Сохранить", (dialog, which) -> {
                            String name = etDishName.getText().toString().trim();
                            String priceStr = etDishPrice.getText().toString().trim();
                            String description = etDishDescription.getText().toString().trim();

                            if (name.isEmpty() || priceStr.isEmpty()) {
                                Toast.makeText(this, "Заполните название и цену", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                double price = Double.parseDouble(priceStr);

                                // Собираем выбранные ингредиенты
                                List<Integer> selectedIngredientIds = new ArrayList<>();
                                for (int i = 0; i < llIngredientsList.getChildCount(); i++) {
                                    CheckBox checkBox = (CheckBox) llIngredientsList.getChildAt(i);
                                    if (checkBox.isChecked()) {
                                        selectedIngredientIds.add((Integer) checkBox.getTag());
                                    }
                                }

                                if (selectedIngredientIds.isEmpty()) {
                                    Toast.makeText(this, "Выберите хотя бы один ингредиент", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                new Thread(() -> {
                                    boolean success = dbHelper.updateDishWithIngredients(dish.getId(), name, price, description, selectedIngredientIds);
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(ChefActivity.this, "✅ Блюдо обновлено", Toast.LENGTH_SHORT).show();
                                            loadMenuDishes();
                                        } else {
                                            Toast.makeText(ChefActivity.this, "❌ Ошибка обновления", Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Ошибка обновления блюда ID: " + dish.getId());
                                        }
                                    });
                                }).start();

                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "Введите корректную цену", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Удалить", (dialog, which) -> {
                            showDeleteDishDialog(dish);
                        })
                        .setNeutralButton("Отмена", null)
                        .show();
            });
        }).start();
    }

    private void showDeleteDishDialog(MenuItem dish) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Удаление блюда")
                .setMessage("Удалить блюдо \"" + dish.getName() + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    new Thread(() -> {
                        boolean success = dbHelper.deleteDish(dish.getId());
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(ChefActivity.this, "✅ Блюдо удалено", Toast.LENGTH_SHORT).show();
                                loadMenuDishes();
                            } else {
                                Toast.makeText(ChefActivity.this, "❌ Ошибка удаления", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Адаптер для ингредиентов
    private class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

        @Override
        public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ingredient_chef, parent, false);
            return new IngredientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IngredientViewHolder holder, int position) {
            Ingredient ingredient = ingredients.get(position);
            holder.bind(ingredient);
        }

        @Override
        public int getItemCount() {
            return ingredients.size();
        }

        class IngredientViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvQuantity, tvStatus;
            private MaterialCardView cardView;
            private ProgressBar progressBar;

            public IngredientViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvIngredientName);
                tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
                tvStatus = itemView.findViewById(R.id.tvIngredientStatus);
                cardView = itemView.findViewById(R.id.cardIngredient);
                progressBar = itemView.findViewById(R.id.progressBar);
            }

            public void bind(Ingredient ingredient) {
                tvName.setText(ingredient.getName());
                tvQuantity.setText(String.format("%.1f ед.", ingredient.getQuantity()));

                // Определяем статус
                double quantity = ingredient.getQuantity();
                if (quantity < 3) {
                    tvStatus.setText("⚠️ КРИТИЧЕСКИ МАЛО");
                    tvStatus.setTextColor(Color.RED);
                    cardView.setCardBackgroundColor(Color.parseColor("#FFF5F5"));
                    progressBar.setProgress((int) (quantity / 10 * 100));
                    progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                } else if (quantity < 10) {
                    tvStatus.setText("⚠️ МАЛО");
                    tvStatus.setTextColor(Color.parseColor("#FF9800"));
                    cardView.setCardBackgroundColor(Color.parseColor("#FFF8E1"));
                    progressBar.setProgress((int) (quantity / 10 * 100));
                    progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800")));
                } else {
                    tvStatus.setText("✓ НОРМА");
                    tvStatus.setTextColor(Color.GREEN);
                    cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
                    progressBar.setProgress(100);
                    progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.GREEN));
                }

                // Обработчики кликов
                itemView.setOnClickListener(v -> showUpdateIngredientDialog(ingredient));

                ImageButton btnDelete = itemView.findViewById(R.id.btnDelete);
                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(ChefActivity.this)
                            .setTitle("🗑️ Удаление ингредиента")
                            .setMessage("Удалить \"" + ingredient.getName() + "\"?")
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                new Thread(() -> {
                                    boolean success = dbHelper.deleteIngredient(ingredient.getId());
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(ChefActivity.this,
                                                    "✅ Ингредиент удален", Toast.LENGTH_SHORT).show();
                                            loadIngredients();
                                        } else {
                                            Toast.makeText(ChefActivity.this,
                                                    "❌ Ошибка удаления", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }).start();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
            }
        }
    }

    // Адаптер для стоп-листа
    private class StopListAdapter extends RecyclerView.Adapter<StopListAdapter.StopListViewHolder> {

        @Override
        public StopListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stop_list_chef, parent, false);
            return new StopListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StopListViewHolder holder, int position) {
            StopListItem item = stopList.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return stopList.size();
        }

        class StopListViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvPrice, tvStatus;
            private MaterialCardView cardView;

            public StopListViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvStopDishName);
                tvPrice = itemView.findViewById(R.id.tvStopDishPrice);
                tvStatus = itemView.findViewById(R.id.tvStopStatus);
                cardView = itemView.findViewById(R.id.cardStopList);
            }

            public void bind(StopListItem item) {
                tvName.setText(item.getName());
                tvPrice.setText(String.format("%.0f ₽", item.getPrice()));
                tvStatus.setText("⛔ НЕТ В ПРОДАЖЕ");
                tvStatus.setTextColor(Color.RED);
                cardView.setCardBackgroundColor(Color.parseColor("#FFF5F5"));

                // Кнопка восстановления
                Button btnRestore = itemView.findViewById(R.id.btnRestore);
                btnRestore.setOnClickListener(v -> {
                    new AlertDialog.Builder(ChefActivity.this)
                            .setTitle("✅ Восстановление блюда")
                            .setMessage("Восстановить \"" + item.getName() + "\" в меню?")
                            .setPositiveButton("Восстановить", (dialog, which) -> {
                                new Thread(() -> {
                                    boolean success = dbHelper.removeFromStopList(item.getId());
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(ChefActivity.this,
                                                    "✅ Блюдо восстановлено", Toast.LENGTH_SHORT).show();
                                            loadStopList();
                                            loadMenuDishes();
                                        } else {
                                            Toast.makeText(ChefActivity.this,
                                                    "❌ Ошибка восстановления", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }).start();
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });
            }
        }
    }

    // Адаптер для меню
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        @Override
        public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_chef, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MenuViewHolder holder, int position) {
            MenuItem dish = menuDishes.get(position);
            holder.bind(dish);
        }

        @Override
        public int getItemCount() {
            return menuDishes.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvPrice, tvIngredients;
            private MaterialCardView cardView;

            public MenuViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvDishName);
                tvPrice = itemView.findViewById(R.id.tvDishPrice);
                tvIngredients = itemView.findViewById(R.id.tvDishIngredients);
                cardView = itemView.findViewById(R.id.cardDish);
            }

            public void bind(MenuItem dish) {
                tvName.setText(dish.getName());
                tvPrice.setText(String.format("%.0f ₽", dish.getPrice()));

                // Цветовая индикация по цене
                if (dish.getPrice() > 500) {
                    cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // оранжевый
                } else if (dish.getPrice() > 200) {
                    cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // зеленый
                } else {
                    cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // синий
                }

                // Загружаем ингредиенты
                tvIngredients.setText("Загрузка ингредиентов...");

                new Thread(() -> {
                    try {
                        List<Ingredient> ingredients = dbHelper.getIngredientsForDish(dish.getId());
                        StringBuilder ingredientsText = new StringBuilder();

                        if (ingredients.isEmpty()) {
                            ingredientsText.append("Ингредиенты не указаны");
                        } else {
                            ingredientsText.append("Ингредиенты: ");
                            for (int i = 0; i < Math.min(3, ingredients.size()); i++) {
                                if (i > 0) ingredientsText.append(", ");
                                ingredientsText.append(ingredients.get(i).getName());
                            }
                            if (ingredients.size() > 3) {
                                ingredientsText.append(" и еще ").append(ingredients.size() - 3);
                            }
                        }

                        final String finalText = ingredientsText.toString();
                        runOnUiThread(() -> tvIngredients.setText(finalText));

                    } catch (Exception e) {
                        runOnUiThread(() -> tvIngredients.setText("❌ Ошибка загрузки"));
                    }
                }).start();

                // Клик для просмотра деталей
                itemView.setOnClickListener(v -> showDishDetails(dish));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}