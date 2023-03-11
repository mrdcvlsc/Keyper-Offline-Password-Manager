package com.example.offlinepasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("");

        Button buttonOpen = findViewById(R.id.main_btn_open);
        Button buttonCreate = findViewById(R.id.main_btn_create);

        buttonOpen.setOnClickListener(v -> btnOpen());
        buttonCreate.setOnClickListener(v -> btnCreate());
    }

    private void btnCreate() {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    private void btnOpen() {
        Intent intent = new Intent(this, OpenActivity.class);
        startActivity(intent);
    }
}