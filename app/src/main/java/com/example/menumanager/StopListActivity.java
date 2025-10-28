package com.example.menumanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StopListActivity extends AppCompatActivity {

    private List<StopListItem> stopListItems = new ArrayList<>(Arrays.asList(
            new StopListItem(1, "Тирамису", "Закончился сыр маскарпоне"),
            new StopListItem(2, "Стейк", "Закончилась говядина")
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_list);

        setupHeader();
        setupStopList();
        setupBottomNavigation();
        setupLogoutButton();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Стоп-лист");

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddStopListItemDialog();
            }
        });
    }

    private void showAddStopListItemDialog() {
        // Создаем диалоговое окно
        final EditText inputDishName = new EditText(this);
        final EditText inputReason = new EditText(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        inputDishName.setHint("Название блюда");
        inputReason.setHint("Причина");

        layout.addView(inputDishName);
        layout.addView(inputReason);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить в стоп-лист")
                .setView(layout)
                .setPositiveButton("Добавить", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String dishName = inputDishName.getText().toString().trim();
                        String reason = inputReason.getText().toString().trim();

                        if (!dishName.isEmpty() && !reason.isEmpty()) {
                            StopListItem newItem = new StopListItem(
                                    stopListItems.size() + 1,
                                    dishName,
                                    reason
                            );
                            stopListItems.add(newItem);
                            setupStopList();
                            Toast.makeText(StopListActivity.this, "Блюдо добавлено в стоп-лист", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(StopListActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setupStopList() {
        LinearLayout stopListContainer = findViewById(R.id.stopListContainer);
        stopListContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final StopListItem item : stopListItems) {
            View itemView = inflater.inflate(R.layout.item_stop_list, stopListContainer, false);

            TextView tvDishName = itemView.findViewById(R.id.tvStopListDishName);
            TextView tvReason = itemView.findViewById(R.id.tvStopListReason);
            Button btnRemove = itemView.findViewById(R.id.btnRemove);

            tvDishName.setText(item.getDishName());
            tvReason.setText(item.getReason());

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopListItems.remove(item);
                    setupStopList();
                    Toast.makeText(StopListActivity.this, "Блюдо удалено из стоп-листа", Toast.LENGTH_SHORT).show();
                }
            });

            stopListContainer.addView(itemView);
        }
    }

    private void setupBottomNavigation() {
        TextView btnStopList = findViewById(R.id.btnStopListNav);
        TextView btnIngredients = findViewById(R.id.btnIngredientsNav);
        TextView btnMenu = findViewById(R.id.btnMenuNav);

        // Активная кнопка (Стоп-лист)
        btnStopList.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
        btnStopList.setTextColor(ContextCompat.getColor(this, R.color.white));

        btnStopList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Уже в стоп-листе
            }
        });

        btnIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StopListActivity.this, IngredientsActivity.class);
                startActivity(intent);
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
                Intent intent = new Intent(StopListActivity.this, RoleSelectionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}