package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnAdmin = findViewById(R.id.btnAdmin);
        Button btnChef = findViewById(R.id.btnCook);
        Button btnClient = findViewById(R.id.btnClient);

        btnAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", "admin");
            startActivity(intent);
        });

        btnChef.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", "chef");
            startActivity(intent);
        });

        btnClient.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            intent.putExtra("ROLE", "client");
            startActivity(intent);
        });

        Toast.makeText(this, "Выберите роль", Toast.LENGTH_SHORT).show();
    }
}