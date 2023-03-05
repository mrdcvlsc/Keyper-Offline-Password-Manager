package com.example.offlinepasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class CreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }

    public void btnCreate(View view) {
        Intent intent = new Intent(this, ManagerActivity.class);
        startActivity(intent);
    }

    public void btnClearField(View view) {
        ((EditText) findViewById(R.id.create_et_db_name)).setText("");
        ((EditText) findViewById(R.id.create_et_password)).setText("");
        ((EditText) findViewById(R.id.create_et_confirm_password)).setText("");
    }
}