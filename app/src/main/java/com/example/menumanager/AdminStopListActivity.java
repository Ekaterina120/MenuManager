package com.example.menumanager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminStopListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<StopListItem> stopList = new ArrayList<>();
    private StopListAdapter stopListAdapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stop_list);

        dbHelper = DatabaseHelper.getInstance(this);

        setupHeader();
        setupRecyclerView();
        loadStopList();
    }

    private void setupHeader() {
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("⛔ Стоп-лист (Админ)");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView rvStopList = findViewById(R.id.rvStopList);
        rvStopList.setLayoutManager(new LinearLayoutManager(this));
        stopListAdapter = new StopListAdapter();
        rvStopList.setAdapter(stopListAdapter);

        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void loadStopList() {
        new Thread(() -> {
            stopList = dbHelper.getStopList();
            runOnUiThread(() -> {
                stopListAdapter.notifyDataSetChanged();
                updateEmptyState();
            });
        }).start();
    }

    private void updateEmptyState() {
        tvEmpty.setVisibility(stopList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Адаптер
    private class StopListAdapter extends RecyclerView.Adapter<StopListAdapter.StopListViewHolder> {

        @Override
        public StopListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stop_list_admin, parent, false);
            return new StopListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StopListViewHolder holder, int position) {
            StopListItem item = stopList.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return stopList.size();
        }

        class StopListViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPrice;
            Button btnRestore;

            public StopListViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvStopDishName);
                tvPrice = itemView.findViewById(R.id.tvStopDishPrice);
                btnRestore = itemView.findViewById(R.id.btnRestore);
            }

            public void bind(StopListItem item) {
                tvName.setText(item.getName());
                tvPrice.setText(String.format("%.0f ₽", item.getPrice()));

                btnRestore.setOnClickListener(v -> {
                    new Thread(() -> {
                        boolean success = dbHelper.removeFromStopList(item.getId());
                        runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(AdminStopListActivity.this,
                                        "✅ Блюдо восстановлено", Toast.LENGTH_SHORT).show();
                                loadStopList();
                            }
                        });
                    }).start();
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStopList();
    }
}