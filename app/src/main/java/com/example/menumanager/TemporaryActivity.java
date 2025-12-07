package com.example.menumanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TemporaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("ТЕСТОВАЯ АКТИВНОСТЬ\nУспешный переход!");
        textView.setTextSize(24);
        setContentView(textView);
    }
}