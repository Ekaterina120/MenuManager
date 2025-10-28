package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private CardView adminCard;
    private CardView chefCard;
    private CardView clientCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        adminCard = findViewById(R.id.admin_card);
        chefCard = findViewById(R.id.chef_card);
        clientCard = findViewById(R.id.client_card);
    }

    private void setupClickListeners() {
        // Администратор - переходим к авторизации
        adminCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                intent.putExtra("ROLE", "ADMIN");
                startActivity(intent);
            }
        });

        // Повар - переходим к авторизации
        chefCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                intent.putExtra("ROLE", "CHEF");
                startActivity(intent);
            }
        });

        // Клиент - сразу открываем меню
        clientCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, ClientMenuActivity.class);
                startActivity(intent);
            }
        });
    }
}
