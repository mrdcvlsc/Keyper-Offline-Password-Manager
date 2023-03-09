package com.example.offlinepasswordmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AddRecordActivity extends AppCompatActivity {

    private ArrayList<String> usernames;
    private ArrayList<String> platforms;
    private ArrayList<String> passwords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        setTitle("Add New Account Record");

        usernames = new ArrayList<>();
        platforms = new ArrayList<>();
        passwords = new ArrayList<>();
    }

    public void btnClearField(View view) {
        ((EditText) findViewById(R.id.add_record_et_username)).setText("");
        ((EditText) findViewById(R.id.add_record_et_platform)).setText("");
        ((EditText) findViewById(R.id.add_record_et_password)).setText("");
        ((EditText) findViewById(R.id.add_record_et_password_confirm)).setText("");
    }

    public void btnAddRecord(View view) {
        String username = ((EditText) findViewById(R.id.add_record_et_username)).getText().toString();
        String platform = ((EditText) findViewById(R.id.add_record_et_platform)).getText().toString();
        String password = ((EditText) findViewById(R.id.add_record_et_password)).getText().toString();
        String confirm  = ((EditText) findViewById(R.id.add_record_et_password_confirm)).getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please fill out username", Toast.LENGTH_LONG).show();
        } else if (platform.isEmpty()) {
            Toast.makeText(this, "Please fill out the platform", Toast.LENGTH_LONG).show();
        }else if (password.isEmpty()) {
            Toast.makeText(this, "Please fill out the password", Toast.LENGTH_LONG).show();
        } else if (confirm.isEmpty()) {
            Toast.makeText(this, "Please re-type your password", Toast.LENGTH_LONG).show();
        } else if (!password.equals(confirm)) {
            Toast.makeText(this, "Password did not match", Toast.LENGTH_LONG).show();
        } else {
            if(addRecordToDb(username, platform, password)) {
                usernames.add(username);
                platforms.add(platform);
                passwords.add(password);

                btnClearField(view);
            }
        }
    }

    private boolean addRecordToDb(String username, String platform, String password) {
        String dbName = getIntent().getStringExtra("lata");
        String passwordRecorder = getIntent().getStringExtra("abre");

        try {
            String salt = Cryptography.generateSalt(16);
            SecretKey secretKey = Cryptography.generateKey(passwordRecorder, salt);

            byte[] raw_iv = new byte[16];
            new SecureRandom().nextBytes(raw_iv);
            IvParameterSpec randomIV = new IvParameterSpec(raw_iv);

            String algorithm = "AES/CBC/PKCS5Padding";

            String encryptedPassword = Cryptography.encrypt(algorithm, password, secretKey, randomIV);

            AccountDbHelper dbHelper = new AccountDbHelper(getApplicationContext(), dbName, null, 1);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("uid", username + platform);
            values.put("username", username);
            values.put("platform", platform);
            values.put("password", encryptedPassword);
            values.put("iv", Cryptography.ByteToString(raw_iv));
            values.put("salt", salt);

            db.insertOrThrow("records", null, values);
            db.close();

            Toast.makeText(this, "Record was added", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception err) {
            Toast.makeText(this, "The username in that platform already exist in the database", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        String[] usernamesByteArray = new String[usernames.size()];
        String[] platformsByteArray = new String[platforms.size()];
        String[] passwordsByteArray = new String[passwords.size()];

        for (int i = 0; i < usernames.size(); ++i) {
            usernamesByteArray[i] = usernames.get(i);
            platformsByteArray[i] = platforms.get(i);
            passwordsByteArray[i] = passwords.get(i);
        }

        Intent intent = new Intent();
        intent.putExtra("lata", getIntent().getStringExtra("lata"));
        intent.putExtra("abre", getIntent().getStringExtra("abre"));

        intent.putExtra("usernames", usernamesByteArray);
        intent.putExtra("platforms", platformsByteArray);
        intent.putExtra("passwords", passwordsByteArray);

        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}