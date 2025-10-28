package com.example.menumanager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import java.util.List;

public class ClientMenuActivity extends AppCompatActivity {

    private final List<MenuItem> menuItems = Arrays.asList(
            new MenuItem(1, "Борщ", 350, "Супы"),
            new MenuItem(2, "Солянка", 380, "Супы")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_menu);

        setupMenuList();
        setupBottomNavigation();
        setupLogoutButton();
    }

    private void setupMenuList() {
        LinearLayout menuContainer = findViewById(R.id.menuContainer);
        menuContainer.removeAllViews();

        // Заголовок категории
        TextView categoryHeader = new TextView(this);
        categoryHeader.setText("Супы");
        categoryHeader.setTextSize(24);
        categoryHeader.setTextColor(Color.BLACK);
        categoryHeader.setPadding(0, 16, 0, 16);
        menuContainer.addView(categoryHeader);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MenuItem item : menuItems) {
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

        // Активная кнопка (Меню)
        btnMenu.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnMenu.setTextColor(ContextCompat.getColor(this, R.color.white));

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
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMenuActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}