package com.example.offlinepasswordmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ManagerActivity extends AppCompatActivity {

    private ArrayList<Account> accountArray;
    private AccountRecyclerViewAdapter adapter;

    private static String DATABASE_NAME;
    private static String RECORDING_PASSWORD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        Log.d("manager-calls", "initial setup loaded");

        DATABASE_NAME = getIntent().getStringExtra("lata");
        RECORDING_PASSWORD = getIntent().getStringExtra("abre");

        readDb(DATABASE_NAME);

        Log.d("manager-calls", "ArrayList data loaded");

        setRecyclerView();
    }

    private void setRecyclerView() {
        // set adapter
        RecyclerView accountRecyclerView = findViewById(R.id.manager_rv_records);
        adapter = new AccountRecyclerViewAdapter(accountArray);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        accountRecyclerView.setLayoutManager(layoutManager);
        accountRecyclerView.setItemAnimator(new DefaultItemAnimator());
        accountRecyclerView.setAdapter(adapter);

        Log.d("manager-calls", "setRecyclerView call");

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deleteAccountRecordCallBack);
        itemTouchHelper.attachToRecyclerView(accountRecyclerView);
    }
    
    ItemTouchHelper.SimpleCallback deleteAccountRecordCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();

            if (direction == ItemTouchHelper.LEFT) {
                accountArray.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            // Add the implementation below in "app > Gradle Script > build.gradle (Module :app) > dependencies"
            // implementation 'it.xabaras.android:recyclerview-swipedecorator:1.4'
            // then click "Sync"
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(ManagerActivity.this, R.color.black))
                    .addActionIcon(R.drawable.delete_icon)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

//            Log.d("Slide Action", "Detected Slide to left");
        }
    };

    public void btnAddRecord(View view) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra("lata", DATABASE_NAME);
        intent.putExtra("abre", RECORDING_PASSWORD);
        addRecordActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> addRecordActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Intent intent = result.getData();

                        DATABASE_NAME = intent.getStringExtra("lata");
                        RECORDING_PASSWORD = intent.getStringExtra("abre");

                        String[] usernamesByteArray = intent.getStringArrayExtra("usernames");
                        String[] platformsByteArray = intent.getStringArrayExtra("platforms");
                        String[] passwordsByteArray = intent.getStringArrayExtra("passwords");

                        for (int i = 0; i < usernamesByteArray.length; ++i) {
                            accountArray.add(new Account(
                                usernamesByteArray[i],
                                platformsByteArray[i],
                                passwordsByteArray[i]
                            ));

                            adapter.notifyItemInserted(accountArray.size() - 1);
                        }
                    }
                }
            }
    );

    private void readDb(String dbName) {
        Log.d("manager-calls", "readDb invokation");

        String columnOrder = "username";
        String orderBy = "DESC"; // "ASC" - Ascending

        try {
            AccountDbHelper dbHelper = new AccountDbHelper(getApplicationContext(), dbName, null, 1);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    "records",
                    null,
                    null,
                    null,
                    null,
                    null,
                    columnOrder + " " + orderBy
            );

            if (accountArray == null) {
                accountArray = new ArrayList<>();
            } else {
                accountArray.clear();
            }

            String algorithm = "AES/CBC/PKCS5Padding";

            String username, platform, cipherPW, iv, salt, password;

            while (cursor.moveToNext()) {
                username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                platform = cursor.getString(cursor.getColumnIndexOrThrow("platform"));
                cipherPW = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                iv = cursor.getString(cursor.getColumnIndexOrThrow("iv"));
                salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));

                SecretKey secretKey = Cryptography.generateKey(RECORDING_PASSWORD, salt);
                IvParameterSpec randomIV = Cryptography.getIV(Cryptography.StringToByte(iv));

                password = Cryptography.decrypt(algorithm, cipherPW, secretKey, randomIV);

//                Log.d("password", password);

                accountArray.add(new Account(username, password, platform));
            }

            cursor.close();

            Toast.makeText(
                    this,
                    "Read a total of " + accountArray.size() + " records",
                    Toast.LENGTH_LONG
            ).show();

            dbHelper.close();
        } catch (Exception err) {
            Toast.makeText(this, "Error reading the records", Toast.LENGTH_LONG).show();
            Log.d("Manager Read Error", err.getMessage());
        }
    }
}