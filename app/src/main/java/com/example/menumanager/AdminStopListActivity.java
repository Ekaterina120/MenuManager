package com.example.menumanager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.List;
import java.util.ArrayList;

public class AdminStopListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<StopListItem> stopListItems;
    private BroadcastReceiver stopListUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stop_list);

        dbHelper = new DatabaseHelper(this);
        stopListItems = new ArrayList<>();

        setupBroadcastReceiver();
        setupHeader();
        setupStopList();
        setupBottomNavigation();
        setupBackButton();
    }

    private void setupBroadcastReceiver() {
        stopListUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("STOP_LIST_UPDATED".equals(intent.getAction())) {
                    setupStopList();
                }
            }
        };

        IntentFilter filter = new IntentFilter("STOP_LIST_UPDATED");
        LocalBroadcastManager.getInstance(this).registerReceiver(stopListUpdateReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupStopList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stopListUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(stopListUpdateReceiver);
        }
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Стоп-лист");
    }

    private void setupStopList() {
        LinearLayout stopListContainer = findViewById(R.id.stopListContainer);
        stopListContainer.removeAllViews();

        stopListItems = dbHelper.getStopList();

        if (stopListItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Стоп-лист пустой");
            emptyText.setTextSize(18);
            emptyText.setTextColor(android.graphics.Color.BLACK);
            emptyText.setPadding(0, 50, 0, 0);
            emptyText.setGravity(android.view.Gravity.CENTER);
            stopListContainer.addView(emptyText);
        } else {
            LayoutInflater inflater = LayoutInflater.from(this);

            for (final StopListItem item : stopListItems) {
                View itemView = inflater.inflate(R.layout.item_admin_stop_list, stopListContainer, false);

                TextView tvDishName = itemView.findViewById(R.id.tvStopListDishName);
                TextView tvReason = itemView.findViewById(R.id.tvStopListReason);
                Button btnRemoveFromStopList = itemView.findViewById(R.id.btnRemoveFromStopList);

                tvDishName.setText(item.getDishName());
                tvReason.setText(item.getReason());

                btnRemoveFromStopList.setText("УБРАТЬ ИЗ СТОП-ЛИСТА");
                btnRemoveFromStopList.setBackgroundColor(android.graphics.Color.RED);

                btnRemoveFromStopList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRemoveFromStopListConfirmation(item);
                    }
                });

                stopListContainer.addView(itemView);
            }
        }
    }

    private void setupBottomNavigation() {
        TextView btnStopList = findViewById(R.id.btnStopListNav);
        TextView btnMenu = findViewById(R.id.btnMenuNav);

        // УБИРАЕМ кнопку ингредиентов для администратора
        TextView btnIngredients = findViewById(R.id.btnIngredientsNav);
        if (btnIngredients != null) {
            btnIngredients.setVisibility(View.GONE);
        }

        if (btnStopList != null && btnMenu != null) {
            // Активная кнопка (Стоп-лист)
            btnStopList.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500));
            btnStopList.setTextColor(ContextCompat.getColor(this, R.color.white));

            btnStopList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Уже в стоп-листе
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Переход к управлению меню
                    Intent intent = new Intent(AdminStopListActivity.this, ManageMenuActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void showRemoveFromStopListConfirmation(final StopListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Восстановление блюда")
                .setMessage("Вы уверены, что хотите вернуть блюдо \"" + item.getDishName() + "\" в меню?")
                .setPositiveButton("Вернуть в меню", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        boolean success = dbHelper.removeFromStopListAndShow(item.getId());

                        if (success) {
                            setupStopList();
                            Toast.makeText(AdminStopListActivity.this, "Блюдо \"" + item.getDishName() + "\" возвращено в меню", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent("STOP_LIST_UPDATED");
                            LocalBroadcastManager.getInstance(AdminStopListActivity.this).sendBroadcast(intent);
                        } else {
                            Toast.makeText(AdminStopListActivity.this, "Ошибка при восстановлении блюда", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
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