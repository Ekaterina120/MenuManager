package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class IngredientsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);

        setupHeader();
        setupBottomNavigation();
        setupLogoutButton();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Ингредиенты");

        // Здесь можно добавить функционал для работы с ингредиентами
    }

    private void setupBottomNavigation() {
        TextView btnStopList = findViewById(R.id.btnStopListNav);
        TextView btnIngredients = findViewById(R.id.btnIngredientsNav);
        TextView btnMenu = findViewById(R.id.btnMenuNav);

        // Активная кнопка (Ингредиенты)
        btnIngredients.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnIngredients.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnStopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IngredientsActivity.this, StopListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в ингредиентах
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IngredientsActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}