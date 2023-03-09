package com.example.offlinepasswordmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class OpenActivity extends AppCompatActivity {

    private EditText editTextDbName;

    private EditText editTextDbPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        editTextDbName = findViewById(R.id.open_et_db_name);
        editTextDbPass = findViewById(R.id.open_et_password);
    }

    public void btnOpen(View view) {
        String dbName = editTextDbName.getText().toString();
        String dbPass = editTextDbPass.getText().toString();

        if (dbName.isEmpty()) {
            Toast.makeText(this, "Please fill out the database name", Toast.LENGTH_LONG).show();
            return;
        } else if (dbPass.isEmpty()) {
            Toast.makeText(this, "Please fill out the password", Toast.LENGTH_LONG).show();
            return;
        } else if (!dbName.matches("[a-zA-Z\\d]+")) {
            Toast.makeText(this, "Alphanumeric characters only for Database name", Toast.LENGTH_LONG).show();
            return;
        }

        File dbFile = getApplicationContext().getDatabasePath(dbName + ".db");

        if (dbFile.exists()) {
            try {
                AccountDbHelper dbHelper = new AccountDbHelper(getApplicationContext(), dbName, null, 1);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                String[] columns = { "dbhash", "dbsalt" };
                String[] selectionArg = { dbName };

                Cursor cursor = db.query(
                        "info",
                        columns,
                        "dbuser = ?",
                        selectionArg,
                        null,
                        null,
                        "dbuser DESC"
                );

                String hash = "", salt = "";
                while (cursor.moveToNext()) {
                    hash = cursor.getString(cursor.getColumnIndexOrThrow("dbhash"));
                    salt = cursor.getString(cursor.getColumnIndexOrThrow("dbsalt"));
                }

                cursor.close();

                String passwordHash = Hash.SHA512Hash(dbPass, salt, Hash.ITERATION_LEVEL);

                dbHelper.close();

                if (passwordHash.equals(hash)) {
                    String recordingPassword = Hash.SHA512Hash(hash, salt, Hash.ITERATION_LEVEL);

                    Intent intent = new Intent(this, ManagerActivity.class);
                    intent.putExtra("lata", dbName);
                    intent.putExtra("abre", recordingPassword);

                    editTextDbName.setText("");
                    editTextDbPass.setText("");

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_LONG).show();
                }

            } catch (Exception err) {
                Toast.makeText(this, "Error opening the database", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "database not found", Toast.LENGTH_LONG).show();
        }
    }
}