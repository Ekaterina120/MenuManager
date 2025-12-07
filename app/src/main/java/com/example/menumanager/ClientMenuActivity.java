package com.example.menumanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.*;

public class ClientMenuActivity extends AppCompatActivity {

    private static final String TAG = "ClientMenuActivity";
    private static final String PREFS_NAME = "MenuPrefs";
    private static final String KEY_FAVORITES = "favorites";

    private DatabaseHelper dbHelper;
    private List<MenuItem> allDishes = new ArrayList<>();
    private Set<String> favoriteIds = new HashSet<>(); // Храним ID избранных блюд
    private List<MenuItem> recommendedDishes = new ArrayList<>();
    private List<MenuItem> filteredDishes = new ArrayList<>();

    private TextView tvGreeting, tvTimeBasedGreeting;
    private EditText etSearch;
    private ImageButton btnClearSearch, btnLogout;
    private RecyclerView rvDishes, rvRecommended;
    private DishAdapter dishAdapter, recommendedAdapter;
    private LinearLayout llRecommendations, llAllDishes;
    private TextView tvEmptyMenu;
    private ImageButton btnQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_menu_modern);

        Log.d(TAG, "=== ClientMenuActivity Modern ===");

        initializeViews();
        setupDatabase();
        setupGreeting();
        setupSearch();
        setupRecyclerViews();
        loadFavoritesFromPrefs(); // Загружаем избранное из SharedPreferences
        loadMenuData();
        setupLogoutButton();
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTimeBasedGreeting = findViewById(R.id.tvTimeBasedGreeting);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnLogout = findViewById(R.id.btnLogout);
        rvDishes = findViewById(R.id.rvDishes);
        rvRecommended = findViewById(R.id.rvRecommended);
        llRecommendations = findViewById(R.id.llRecommendations);
        llAllDishes = findViewById(R.id.llAllDishes);
        tvEmptyMenu = findViewById(R.id.tvEmptyMenu);
        btnQRCode = findViewById(R.id.btnQRCode);
    }

    private void setupDatabase() {
        try {
            dbHelper = DatabaseHelper.getInstance(this);
            if (!dbHelper.testConnection()) {
                showDatabaseError();
            }
        } catch (Exception e) {
            Log.e(TAG, "Database error: " + e.getMessage());
            showDatabaseError();
        }
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) greeting = "Доброе утро!";
        else if (hour < 18) greeting = "Добрый день!";
        else greeting = "Добрый вечер!";

        tvTimeBasedGreeting.setText(greeting);
        tvGreeting.setText("Добро пожаловать в наш ресторан!");

        // Анимация приветствия
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        tvTimeBasedGreeting.startAnimation(fadeIn);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDishes(s.toString());
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear search button
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        // QR Code button
        btnQRCode.setOnClickListener(v -> {
            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            v.startAnimation(scaleUp);
            showQRCodeDialog();
        });
    }

    private void setupRecyclerViews() {
        // Main dishes RecyclerView
        rvDishes.setLayoutManager(new LinearLayoutManager(this));
        dishAdapter = new DishAdapter(allDishes, false);
        rvDishes.setAdapter(dishAdapter);

        // Recommended RecyclerView (horizontal)
        LinearLayoutManager recommendedLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
        );
        rvRecommended.setLayoutManager(recommendedLayoutManager);
        recommendedAdapter = new DishAdapter(recommendedDishes, true);
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            v.startAnimation(scaleUp);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выход")
                    .setMessage("Вы действительно хотите выйти?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        // Возвращаемся к окну выбора ролей
                        Intent intent = new Intent(ClientMenuActivity.this, RoleSelectionActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    private void loadFavoritesFromPrefs() {
        // Загружаем избранное из SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String favoritesJson = prefs.getString(KEY_FAVORITES, "[]");

        // Парсим JSON строку в Set<String>
        // Для простоты используем запятую как разделитель
        if (!favoritesJson.isEmpty() && !favoritesJson.equals("[]")) {
            String[] ids = favoritesJson.replace("[", "").replace("]", "").split(",");
            for (String id : ids) {
                if (!id.trim().isEmpty()) {
                    favoriteIds.add(id.trim());
                }
            }
        }

        Log.d(TAG, "Loaded favorites: " + favoriteIds.size());
    }

    private void saveFavoritesToPrefs() {
        // Сохраняем избранное в SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Преобразуем Set в JSON строку
        editor.putString(KEY_FAVORITES, favoriteIds.toString());
        editor.apply();

        Log.d(TAG, "Saved favorites: " + favoriteIds.size());
    }

    private void loadMenuData() {
        new Thread(() -> {
            try {
                List<MenuItem> dishes = dbHelper.getAvailableDishes();
                runOnUiThread(() -> {
                    allDishes.clear();
                    allDishes.addAll(dishes);
                    filteredDishes.clear();
                    filteredDishes.addAll(dishes);

                    // Generate recommendations based on time and price
                    generateRecommendations();

                    updateUI();

                    // Show success animation
                    Toast.makeText(ClientMenuActivity.this,
                            "Меню загружено!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ClientMenuActivity.this,
                            "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void generateRecommendations() {
        recommendedDishes.clear();

        if (allDishes.isEmpty()) return;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Different recommendations based on time of day
        List<MenuItem> timeBasedDishes = new ArrayList<>();

        if (hour < 12) { // Morning: breakfast items
            for (MenuItem dish : allDishes) {
                String name = dish.getName().toLowerCase();
                if (name.contains("омлет") || name.contains("каша") ||
                        name.contains("блин") || name.contains("йогурт")) {
                    timeBasedDishes.add(dish);
                }
            }
        } else if (hour < 18) { // Afternoon: lunch items
            for (MenuItem dish : allDishes) {
                String name = dish.getName().toLowerCase();
                if (name.contains("суп") || name.contains("салат") ||
                        name.contains("паста") || name.contains("гарнир")) {
                    timeBasedDishes.add(dish);
                }
            }
        } else { // Evening: dinner items
            for (MenuItem dish : allDishes) {
                String name = dish.getName().toLowerCase();
                if (name.contains("стейк") || name.contains("гриль") ||
                        name.contains("запеч") || name.contains("десерт")) {
                    timeBasedDishes.add(dish);
                }
            }
        }

        // If no time-based dishes, use random or popular
        if (timeBasedDishes.isEmpty()) {
            // Get 3 random dishes as recommendations
            List<MenuItem> tempList = new ArrayList<>(allDishes);
            Collections.shuffle(tempList);
            int count = Math.min(3, tempList.size());
            recommendedDishes.addAll(tempList.subList(0, count));
        } else {
            // Get up to 3 time-based dishes
            int count = Math.min(3, timeBasedDishes.size());
            recommendedDishes.addAll(timeBasedDishes.subList(0, count));
        }
    }

    private void updateUI() {
        // Update main menu
        dishAdapter.updateDishes(filteredDishes);
        tvEmptyMenu.setVisibility(filteredDishes.isEmpty() ? View.VISIBLE : View.GONE);

        // Update recommendations
        recommendedAdapter.notifyDataSetChanged();
        llRecommendations.setVisibility(recommendedDishes.isEmpty() ? View.GONE : View.VISIBLE);

        // Update empty states
        if (allDishes.isEmpty()) {
            findViewById(R.id.llContent).setVisibility(View.GONE);
            findViewById(R.id.llEmptyState).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.llContent).setVisibility(View.VISIBLE);
            findViewById(R.id.llEmptyState).setVisibility(View.GONE);
        }
    }

    private void filterDishes(String query) {
        filteredDishes.clear();

        if (query.isEmpty()) {
            filteredDishes.addAll(allDishes);
        } else {
            String lowerQuery = query.toLowerCase();
            for (MenuItem dish : allDishes) {
                if (dish.getName().toLowerCase().contains(lowerQuery) ||
                        dish.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredDishes.add(dish);
                }
            }
        }

        dishAdapter.updateDishes(filteredDishes);
        tvEmptyMenu.setVisibility(filteredDishes.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showQRCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null);

        ImageView ivQRCode = dialogView.findViewById(R.id.ivQRCode);
        TextView tvTableNumber = dialogView.findViewById(R.id.tvTableNumber);

        // Generate random table number for demo
        Random random = new Random();
        int tableNumber = random.nextInt(20) + 1;
        tvTableNumber.setText("Столик №" + tableNumber);

        builder.setView(dialogView)
                .setTitle("QR-меню")
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void toggleFavorite(MenuItem dish) {
        String dishId = String.valueOf(dish.getId());
        boolean isFavorite = favoriteIds.contains(dishId);

        if (isFavorite) {
            favoriteIds.remove(dishId);
            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
        } else {
            favoriteIds.add(dishId);
            Toast.makeText(this, "Добавлено в избранное ★", Toast.LENGTH_SHORT).show();
        }

        // Сохраняем изменения
        saveFavoritesToPrefs();

        // Update adapters
        dishAdapter.notifyDataSetChanged();
        recommendedAdapter.notifyDataSetChanged();
    }

    private boolean isFavorite(MenuItem dish) {
        return favoriteIds.contains(String.valueOf(dish.getId()));
    }

    private void showDishDetails(MenuItem dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_dish_details, null);

        TextView tvDishName = dialogView.findViewById(R.id.tvDishName);
        TextView tvDishPrice = dialogView.findViewById(R.id.tvDishPrice);
        TextView tvDishDescription = dialogView.findViewById(R.id.tvDishDescription);
        TextView tvIngredients = dialogView.findViewById(R.id.tvIngredients);
        ImageButton btnFavorite = dialogView.findViewById(R.id.btnFavorite);
        Button btnRecommendSimilar = dialogView.findViewById(R.id.btnRecommendSimilar);

        tvDishName.setText(dish.getName());
        tvDishPrice.setText(String.format("%.0f ₽", dish.getPrice()));
        tvDishDescription.setText(dish.getDescription().isEmpty() ?
                "Вкусное блюдо от нашего шеф-повара" : dish.getDescription());

        // Check if dish is favorite
        boolean isFavorite = isFavorite(dish);
        btnFavorite.setImageResource(isFavorite ?
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        // Load ingredients
        new Thread(() -> {
            try {
                List<Ingredient> ingredients = dbHelper.getIngredientsForDish(dish.getId());

                runOnUiThread(() -> {
                    StringBuilder ingredientsText = new StringBuilder("Состав:\n");
                    if (ingredients.isEmpty()) {
                        ingredientsText.append("Информация о составе временно недоступна");
                    } else {
                        for (Ingredient ingredient : ingredients) {
                            ingredientsText.append("• ").append(ingredient.getName())
                                    .append(" (").append(ingredient.getQuantity()).append(")\n");
                        }
                    }
                    tvIngredients.setText(ingredientsText.toString());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvIngredients.setText("Ошибка загрузки состава");
                });
            }
        }).start();

        btnFavorite.setOnClickListener(v -> {
            toggleFavorite(dish);
            boolean newFavoriteState = isFavorite(dish);
            btnFavorite.setImageResource(newFavoriteState ?
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        });

        btnRecommendSimilar.setOnClickListener(v -> {
            recommendSimilarDishes(dish);
        });

        builder.setView(dialogView)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private void recommendSimilarDishes(MenuItem dish) {
        List<MenuItem> similarDishes = new ArrayList<>();

        for (MenuItem otherDish : allDishes) {
            if (!otherDish.equals(dish)) {
                // Simple similarity based on price range and name
                double priceDiff = Math.abs(otherDish.getPrice() - dish.getPrice());
                if (priceDiff < 200) { // Similar price range
                    similarDishes.add(otherDish);
                }
            }
        }

        if (similarDishes.isEmpty()) {
            Toast.makeText(this, "Похожих блюд не найдено", Toast.LENGTH_SHORT).show();
        } else {
            // Show recommendations
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Похожие блюда на " + dish.getName());

            StringBuilder message = new StringBuilder();
            int count = Math.min(3, similarDishes.size());
            for (int i = 0; i < count; i++) {
                MenuItem similar = similarDishes.get(i);
                message.append("• ").append(similar.getName())
                        .append(" - ").append(String.format("%.0f ₽", similar.getPrice()))
                        .append("\n");
            }

            builder.setMessage(message.toString())
                    .setPositiveButton("Спасибо!", null)
                    .show();
        }
    }

    private void showDatabaseError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ошибка подключения")
                .setMessage("Не удалось загрузить меню. Пожалуйста, попробуйте позже.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Возвращаемся к выбору ролей при ошибке
                    Intent intent = new Intent(ClientMenuActivity.this, RoleSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    // Dish Adapter class
    private class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {
        private List<MenuItem> dishes;
        private boolean isCompact;

        public DishAdapter(List<MenuItem> dishes, boolean isCompact) {
            this.dishes = dishes;
            this.isCompact = isCompact;
        }

        public void updateDishes(List<MenuItem> newDishes) {
            this.dishes = newDishes;
            notifyDataSetChanged();
        }

        @Override
        public DishViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId = isCompact ? R.layout.item_dish_compact : R.layout.item_dish_card;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new DishViewHolder(view, isCompact);
        }

        @Override
        public void onBindViewHolder(DishViewHolder holder, int position) {
            MenuItem dish = dishes.get(position);
            holder.bind(dish);
        }

        @Override
        public int getItemCount() {
            return dishes.size();
        }

        class DishViewHolder extends RecyclerView.ViewHolder {
            private TextView tvName, tvPrice, tvDescription;
            private ImageButton btnFavorite;
            private MaterialCardView cardView;
            private ImageView ivDishImage;

            public DishViewHolder(View itemView, boolean isCompact) {
                super(itemView);

                if (isCompact) {
                    tvName = itemView.findViewById(R.id.tvDishNameCompact);
                    tvPrice = itemView.findViewById(R.id.tvDishPriceCompact);
                    btnFavorite = itemView.findViewById(R.id.btnFavoriteCompact);
                    cardView = (MaterialCardView) itemView;
                } else {
                    tvName = itemView.findViewById(R.id.tvDishName);
                    tvPrice = itemView.findViewById(R.id.tvDishPrice);
                    tvDescription = itemView.findViewById(R.id.tvDishDescription);
                    btnFavorite = itemView.findViewById(R.id.btnFavorite);
                    cardView = itemView.findViewById(R.id.cardDish);
                    ivDishImage = itemView.findViewById(R.id.ivDishImage);
                }
            }

            public void bind(MenuItem dish) {
                tvName.setText(dish.getName());
                tvPrice.setText(String.format("%.0f ₽", dish.getPrice()));

                // Устанавливаем черный цвет текста
                tvName.setTextColor(Color.BLACK);
                tvPrice.setTextColor(Color.BLACK);

                if (tvDescription != null) {
                    tvDescription.setText(dish.getDescription().isEmpty() ?
                            "Вкусное блюдо от нашего шеф-повара" : dish.getDescription());
                    tvDescription.setTextColor(Color.BLACK);
                }

                // Устанавливаем иконку избранного (сердечко)
                boolean isFavorite = isFavorite(dish);
                int favoriteIcon = isFavorite ?
                        R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border;
                btnFavorite.setImageResource(favoriteIcon);

                // Устанавливаем светло-серый фон для изображения блюда
                if (ivDishImage != null) {
                    // Используем одну иконку
                    ivDishImage.setImageResource(R.drawable.ic_qr_code);
                    // Светло-серый фон
                    ivDishImage.setBackgroundColor(Color.parseColor("#F5F5F5"));
                }

                // Устанавливаем светло-серый фон для карточки
                if (cardView != null) {
                    cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // светло-серый
                    // Черная граница для контраста
                    cardView.setStrokeColor(Color.parseColor("#E0E0E0"));
                    cardView.setStrokeWidth(1);
                }

                // Устанавливаем обработчики кликов
                btnFavorite.setOnClickListener(v -> {
                    toggleFavorite(dish);
                    notifyItemChanged(getAdapterPosition());
                });

                (isCompact ? cardView : itemView).setOnClickListener(v -> {
                    showDishDetails(dish);
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMenuData();
    }

    @Override
    public void onBackPressed() {
        // При нажатии назад тоже возвращаемся к выбору ролей
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход")
                .setMessage("Вы хотите вернуться к выбору роли?")
                .setPositiveButton("Да", (dialog, which) -> {
                    Intent intent = new Intent(ClientMenuActivity.this, RoleSelectionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}