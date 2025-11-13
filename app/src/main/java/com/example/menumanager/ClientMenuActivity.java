package com.example.menumanager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.ArrayList;
import java.util.List;

public class ClientMenuActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<MenuItem> menuItems;
    private BroadcastReceiver stopListUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_menu);

        dbHelper = new DatabaseHelper(this);
        menuItems = new ArrayList<>();

        setupMenuList();
        setupBottomNavigation();
        setupLogoutButton();
        setupBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMenuList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stopListUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(stopListUpdateReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stopListUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(stopListUpdateReceiver);
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
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

    private void setupMenuList() {
        LinearLayout menuContainer = findViewById(R.id.menuContainer);
        menuContainer.removeAllViews();

        // Получаем только доступные блюда
        menuItems = dbHelper.getAvailableDishes();

        if (menuItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Меню пустое");
            emptyText.setTextSize(18);
            emptyText.setTextColor(Color.BLACK);
            emptyText.setPadding(0, 50, 0, 0);
            emptyText.setGravity(android.view.Gravity.CENTER);
            menuContainer.addView(emptyText);
            return;
        }

        TextView categoryHeader = new TextView(this);
        categoryHeader.setText("Основное меню");
        categoryHeader.setTextSize(24);
        categoryHeader.setTextColor(Color.BLACK);
        categoryHeader.setPadding(0, 16, 0, 16);
        menuContainer.addView(categoryHeader);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final MenuItem item : menuItems) {
            View itemView = inflater.inflate(R.layout.item_menu_client, menuContainer, false);

            TextView tvName = itemView.findViewById(R.id.tvDishName);
            TextView tvPrice = itemView.findViewById(R.id.tvDishPrice);
            Button btnAdd = itemView.findViewById(R.id.btnAddToCart);

            tvName.setText(item.getName());
            tvPrice.setText(item.getPrice() + " ₽");

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ClientMenuActivity.this, "Добавлено в корзину: " + item.getName(), Toast.LENGTH_SHORT).show();
                }
            });

            menuContainer.addView(itemView);
        }
    }

    private void setupBottomNavigation() {
        TextView btnMenu = findViewById(R.id.btnMenu);
        TextView btnFavorites = findViewById(R.id.btnFavorites);
        TextView btnCart = findViewById(R.id.btnCart);

        btnMenu.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnMenu.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnFavorites.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        btnFavorites.setTextColor(ContextCompat.getColor(this, R.color.purple_500));

        btnCart.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        btnCart.setTextColor(ContextCompat.getColor(this, R.color.purple_500));

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в меню
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMenuActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMenuActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutConfirmation();
                }
            });
        }
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти?")
                .setPositiveButton("Выйти", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        Intent intent = new Intent(ClientMenuActivity.this, RoleSelectionActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    public void refreshMenu() {
        setupMenuList();
    }
}