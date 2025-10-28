package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupBottomNavigation();
        setupBackButton();
    }

    private void setupBottomNavigation() {
        TextView btnMenu = findViewById(R.id.btnMenu);
        TextView btnFavorites = findViewById(R.id.btnFavorites);
        TextView btnCart = findViewById(R.id.btnCart);

        // Активная кнопка (Корзина)
        btnCart.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnCart.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в корзине
            }
        });
    }

    private void setupBackButton() {
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}