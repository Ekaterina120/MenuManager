package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== onCreate началось ===");

        try {
            setContentView(R.layout.activity_login);
            Log.d(TAG, "setContentView выполнен");
        } catch (Exception e) {
            Log.e(TAG, "ОШИБКА в setContentView: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRoleLabel = findViewById(R.id.tvRole);

        String role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "admin";

        Log.d(TAG, "Роль: " + role);
        tvRoleLabel.setText("Роль: " + role);

        // Скрываем поле пароля для клиента
        if (role.equals("client")) {
            Log.d(TAG, "Скрываем поле пароля для клиента");
            etPassword.setVisibility(View.GONE);
            View passwordLabel = findViewById(R.id.tvPasswordLabel);
            if (passwordLabel != null) {
                passwordLabel.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "Поле пароля видимо для " + role);
            etPassword.setVisibility(View.VISIBLE);
            View passwordLabel = findViewById(R.id.tvPasswordLabel);
            if (passwordLabel != null) {
                passwordLabel.setVisibility(View.VISIBLE);
            }
        }

        final String finalRole = role;

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "=== КНОПКА НАЖАТА! ===");
                Log.d(TAG, "Роль: " + finalRole);

                try {
                    // Для клиента пароль не требуется
                    if (finalRole.equals("client")) {
                        Log.d(TAG, "Открываем ClientMenuActivity для клиента");
                        Toast.makeText(LoginActivity.this, "Вход как Клиент...", Toast.LENGTH_SHORT).show();

                        // ИСПРАВЛЕНО: ClientMenuActivity вместо ClientActivity
                        Intent intent = new Intent(LoginActivity.this, ClientMenuActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Переход на ClientMenuActivity выполнен");
                        finish();
                        return;
                    }

                    // Проверка пароля для админа и повара
                    String password = etPassword.getText().toString().trim();
                    Log.d(TAG, "Введен пароль: '" + password + "'");

                    if (password.isEmpty()) {
                        etPassword.setError("Введите пароль");
                        Log.d(TAG, "Пароль пустой, показываем ошибку");
                        Toast.makeText(LoginActivity.this, "Введите пароль!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (finalRole.equals("admin") && password.equals("admin123")) {
                        Log.d(TAG, "Пароль админа верный, открываем ManageMenuActivity");
                        Toast.makeText(LoginActivity.this, "Вход как Администратор...", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, ManageMenuActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Переход на ManageMenuActivity выполнен");
                        finish();
                    }
                    else if (finalRole.equals("chef") && password.equals("chef123")) {
                        Log.d(TAG, "Пароль повара верный, открываем ChefActivity");
                        Toast.makeText(LoginActivity.this, "Вход как Повар...", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, ChefActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Переход на ChefActivity выполнен");
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Неверный пароль для роли " + finalRole + ": " + password);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "ОШИБКА перехода: " + e.getMessage(), e);
                    Toast.makeText(LoginActivity.this,
                            "Ошибка: " + e.getClass().getSimpleName() + "\n" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Log.d(TAG, "=== onCreate завершено ===");
    }
}