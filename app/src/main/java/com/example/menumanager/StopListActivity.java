package com.example.menumanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.ArrayList;

public class StopListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<StopListItem> stopListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_list);

        dbHelper = DatabaseHelper.getInstance(this);
        stopListItems = new ArrayList<>();

        setupHeader();
        loadStopList();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("🚫 Стоп-лист");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadStopList() {
        LinearLayout container = findViewById(R.id.stopListContainer);
        if (container == null) {
            Toast.makeText(this, "Контейнер не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        container.removeAllViews();

        stopListItems = dbHelper.getStopList();

        if (stopListItems.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Стоп-лист пуст 🎉");
            emptyText.setTextSize(18);
            emptyText.setTextColor(getResources().getColor(R.color.gray));
            emptyText.setGravity(android.view.Gravity.CENTER);
            container.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (final StopListItem item : stopListItems) {
            View itemView = inflater.inflate(R.layout.item_stop_list, container, false);

            TextView tvName = itemView.findViewById(R.id.tvStopDishName);
            TextView tvPrice = itemView.findViewById(R.id.tvStopDishPrice);
            TextView tvStatus = itemView.findViewById(R.id.tvStopStatus);
            Button btnRestore = itemView.findViewById(R.id.btnRestoreFromStopList);

            tvName.setText(item.getName());
            tvPrice.setText(String.format("%.0f ₽", item.getPrice()));
            tvStatus.setText(item.getStatus());

            btnRestore.setOnClickListener(v -> {
                showRestoreConfirmationDialog(item);
            });

            container.addView(itemView);
        }
    }

    private void showRestoreConfirmationDialog(final StopListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Восстановление блюда")
                .setMessage("Восстановить \"" + item.getName() + "\" в меню?")
                .setPositiveButton("Восстановить", (dialog, which) -> {
                    boolean success = dbHelper.removeFromStopList(item.getId());
                    if (success) {
                        Toast.makeText(this, "✅ Блюдо восстановлено", Toast.LENGTH_SHORT).show();
                        loadStopList();
                    } else {
                        Toast.makeText(this, "❌ Ошибка восстановления", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStopList();
    }
}