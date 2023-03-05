package com.example.offlinepasswordmanager;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
    }

    public void btnClearField(View view) {
        ((EditText) findViewById(R.id.add_record_et_username)).setText("");
        ((EditText) findViewById(R.id.add_record_et_platform)).setText("");
        ((EditText) findViewById(R.id.add_record_et_password)).setText("");
        ((EditText) findViewById(R.id.add_record_et_password_confirm)).setText("");
    }
}