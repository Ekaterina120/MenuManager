package com.example.menumanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnBack;
    private TextView tvRoleTitle;

    private String selectedRole;

    private final List<User> users = Arrays.asList(
            new User(1, "admin", "ADMIN", "admin123"),
            new User(2, "chef", "CHEF", "chef123")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Получаем выбранную роль
        selectedRole = getIntent().getStringExtra("ROLE");
        if (selectedRole == null) {
            selectedRole = "ADMIN";
        }

        initViews();
        setupUI();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);
        tvRoleTitle = findViewById(R.id.tvRoleTitle);
    }

    private void setupUI() {
        // Устанавливаем заголовок в зависимости от роли
        if ("ADMIN".equals(selectedRole)) {
            tvRoleTitle.setText("Вход для администратора");
        } else if ("CHEF".equals(selectedRole)) {
            tvRoleTitle.setText("Вход для повара");
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    showError("Заполните все поля");
                    return;
                }

                User user = authenticateUser(username, password, selectedRole);
                if (user != null) {
                    openWorkScreen(user.getRole());
                } else {
                    showError("Неверный логин или пароль");
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private User authenticateUser(String username, String password, String role) {
        for (User user : users) {
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password) &&
                    user.getRole().equals(role)) {
                return user;
            }
        }
        return null;
    }

    private void openWorkScreen(String role) {
        Intent intent;
        switch (role) {
            case "ADMIN":
                intent = new Intent(this, ManageMenuActivity.class);
                break;
            case "CHEF":
                intent = new Intent(this, StopListActivity.class);
                break;
            default:
                intent = new Intent(this, ClientMenuActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}