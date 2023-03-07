package com.example.offlinepasswordmanager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }

    public void btnCreate(View view) {
        String dbname = ((EditText) findViewById(R.id.create_et_db_name)).getText().toString();
        String password = ((EditText) findViewById(R.id.create_et_password)).getText().toString();
        String confirm = ((EditText) findViewById(R.id.create_et_confirm_password)).getText().toString();

        if (dbname.isEmpty()) {
            Toast.makeText(this, "Please fill out the database name", Toast.LENGTH_LONG).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please fill out the password", Toast.LENGTH_LONG).show();
        } else if (confirm.isEmpty()) {
            Toast.makeText(this, "Please re-type your password", Toast.LENGTH_LONG).show();
        } else if (!password.equals(confirm)) {
             Toast.makeText(this, "Password did not match", Toast.LENGTH_LONG).show();
        } else if (!dbname.matches("[a-zA-Z\\d]+")) {
             Toast.makeText(this, "Alphanumeric characters only for Database name", Toast.LENGTH_LONG).show();
        } else if (createDb(dbname, password)) {
            btnClearField(view);
        }
    }

    private boolean createDb(String DB_NAME, String DB_PASSWORD) {
        String SQL_CREATE_INFO =
            "CREATE TABLE info (" +
            "dbuser TEXT PRIMARY KEY, " +
            "dbhash TEXT, " +
            "dbsalt TEXT)";

        String SQL_CREATE_RECORDS =
            "CREATE TABLE records (" +
            "uid TEXT PRIMARY KEY, " +
            "username TEXT, " +
            "platform TEXT, " +
            "password TEXT, " +
            "iv TEXT, " +
            "salt TEXT)";

        try {
            AccountDbHelper dbHelper = new AccountDbHelper(getApplicationContext(), DB_NAME, null, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.execSQL(SQL_CREATE_INFO);
            db.execSQL(SQL_CREATE_RECORDS);

            // password hashing : start
            String hashedPassword, salt;
            try {
                salt = Cryptography.generateSalt(16);
                hashedPassword = Hash.SHA512Hash(DB_PASSWORD, salt, Hash.ITERATION_LEVEL);

                ContentValues values = new ContentValues();
                values.put("dbuser", DB_NAME);
                values.put("dbhash", hashedPassword);
                values.put("dbsalt", salt);

                db.insert("info", null, values);

            } catch (Exception err) {
                Toast.makeText(getApplicationContext(), "Internal Error Occured", Toast.LENGTH_SHORT).show();
                throw err;
            }
            // password hashing : end

            dbHelper.close();
            Toast.makeText(this, "Database successfully created", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception err) {
            Toast.makeText(this, "Database already existed", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void btnClearField(View view) {
        ((EditText) findViewById(R.id.create_et_db_name)).setText("");
        ((EditText) findViewById(R.id.create_et_password)).setText("");
        ((EditText) findViewById(R.id.create_et_confirm_password)).setText("");
    }
}