package com.example.offlinepasswordmanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenActivity extends AppCompatActivity {

    private EditText editTextDbName, editTextDbPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        setTitle("Login");

        editTextDbName = findViewById(R.id.open_et_db_name);
        editTextDbPass = findViewById(R.id.open_et_password);

        Button buttonOpen = findViewById(R.id.open_btn_open_db);
        Button buttonImport = findViewById(R.id.open_btn_import_db);

        // shorten versions of creating an on click listener callback function.
        // see also 'inline lambda' and 'lambda' functions.
        buttonOpen.setOnClickListener(this::btnOpen);
        buttonImport.setOnClickListener(this::btnImport);
    }

    private void btnOpen(View view) {
        String dbName = editTextDbName.getText().toString();
        String dbPass = editTextDbPass.getText().toString();

        if (dbName.isEmpty()) {
            Toast.makeText(this, "Please fill out the database name", Toast.LENGTH_SHORT).show();
            return;
        } else if (dbPass.isEmpty()) {
            Toast.makeText(this, "Please fill out the password", Toast.LENGTH_SHORT).show();
            return;
        } else if (!dbName.matches("[a-zA-Z\\d]+")) {
            Toast.makeText(this, "Alphanumeric characters only for Database name", Toast.LENGTH_SHORT).show();
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

                    editTextDbPass.setText("");
                    editTextDbName.setText("");

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception err) {
                Toast.makeText(this, "Error opening the database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "database not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void btnImport(View view) {
        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        data.setType("*/*");
        data = Intent.createChooser(data, "Choose a file");
        filePickerActivityLauncher.launch(data);
    }

    ActivityResultLauncher<Intent> filePickerActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri uri = data.getData();

                    String[] pathSegments = uri.getPath().split(File.separator);
                    String dbFileName = pathSegments[pathSegments.length - 1];

                    File checkDb = getApplicationContext().getDatabasePath(dbFileName);

                    if (checkDb.exists()) {
                        Toast.makeText(getApplicationContext(), "Import Failed: Already Existing Database", Toast.LENGTH_LONG).show();
                    } else if (dbFileName.endsWith(".db")) {
                        try { // copy the database file to the database path of the application
                            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
                            OutputStream outputStream = new FileOutputStream(getDatabasePath(dbFileName));

                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }

                            outputStream.flush();
                            outputStream.close();
                            inputStream.close();

                            Toast.makeText(getApplicationContext(), "Import Success: Database Added", Toast.LENGTH_LONG).show();

                            editTextDbName.setText(dbFileName.replace(".db", ""));
                            editTextDbPass.setText("");

                        } catch (Exception err) {
                            Toast.makeText(getApplicationContext(), "Import Failed: Read or Write Error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Import Failed: Incorrect File Extension", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );
}