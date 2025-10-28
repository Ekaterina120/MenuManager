package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setupBottomNavigation();
        setupBackButton();
    }

    private void setupBottomNavigation() {
        TextView btnMenu = findViewById(R.id.btnMenu);
        TextView btnFavorites = findViewById(R.id.btnFavorites);
        TextView btnCart = findViewById(R.id.btnCart);

        // Активная кнопка (Избранное)
        btnFavorites.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnFavorites.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в избранном
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoritesActivity.this, CartActivity.class);
                startActivity(intent);
                finish();
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