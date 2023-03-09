package com.example.offlinepasswordmanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
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
    private AccountRecyclerViewAdapter adapter;
    private SearchView searchView;

    private ArrayList<Account> accountArray;
    private static String DATABASE_NAME;
    private static String RECORDING_PASSWORD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        DATABASE_NAME = getIntent().getStringExtra("lata");
        RECORDING_PASSWORD = getIntent().getStringExtra("abre");

        setTitle("Database \"" + DATABASE_NAME + "\"");

        readDb(DATABASE_NAME);

        setRecyclerView();

        searchView = findViewById(R.id.manager_sv_search);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchMatchingRecord(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            adapter.setData(accountArray);
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.onActionViewCollapsed();
            return true;
        });
    }

    private void searchMatchingRecord(String newText) {
        ArrayList<Account> matchingRecords = new ArrayList<>();

        for (int i = 0; i < accountArray.size(); ++i) {
            boolean usernameMatch = accountArray.get(i).getUsername().toLowerCase().contains(newText.toLowerCase());
            boolean platformMatch = accountArray.get(i).getPlatform().toLowerCase().contains(newText.toLowerCase());

            if (usernameMatch || platformMatch) {
                matchingRecords.add(accountArray.get(i));
            }
        }

        if (!matchingRecords.isEmpty()) {
            adapter.setData(matchingRecords);
        } else {
            adapter.setData(new ArrayList<>());
        }
    }

    private void setRecyclerView() {
        // set adapter
        RecyclerView accountRecyclerView = findViewById(R.id.manager_rv_records);
        adapter = new AccountRecyclerViewAdapter(accountArray);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        accountRecyclerView.setLayoutManager(layoutManager);
        accountRecyclerView.setItemAnimator(new DefaultItemAnimator());
        accountRecyclerView.setAdapter(adapter);

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

            AccountRecyclerViewAdapter.MyViewHolder myViewHolder = (AccountRecyclerViewAdapter.MyViewHolder) viewHolder;

            String username = myViewHolder.getUsername().getText().toString();
            String platform = myViewHolder.getPlatform().getText().toString();
            String uid = username + platform;

            if (direction == ItemTouchHelper.LEFT) {
                try {
                    // delete record in the Sqlite database
                    AccountDbHelper dbHelper = new AccountDbHelper(getApplicationContext(), DATABASE_NAME, null, 1);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    String selection = "uid = ?";
                    String[] selectionArgs = { uid };

                    int deletedRows = db.delete("records", selection, selectionArgs);

                    dbHelper.close();

                    // remove the account record in the adapter array list
                    ArrayList<Account> adapterData = adapter.getDataReference();
                    adapterData.remove(position);
                    adapter.notifyItemRemoved(position);

                    // remove the account record in the original array list if adapter array list has different reference
                    if (accountArray != adapterData) {
                        int index = -1;

                        while (++index < accountArray.size()) {
                            String indexUsername = accountArray.get(index).getUsername();
                            String indexPlatform = accountArray.get(index).getPlatform();

                            if (indexUsername.equals(username) && indexPlatform.equals(platform)) {
                                accountArray.remove(index);
                                break;
                            }
                        }
                    }

                    // end delete, alert success
                    Toast.makeText(getApplicationContext(), "" + deletedRows + " record removed", Toast.LENGTH_LONG).show();
                } catch (Exception err) {

                    // alert failed delete
                    Toast.makeText(getApplicationContext(), "Failed to delete the record", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            // Add the implementation below in "app > Gradle Script > build.gradle (Module :app) > dependencies"
            // implementation 'it.xabaras.android:recyclerview-swipedecorator:1.4'
            // then click "Sync"
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(ManagerActivity.this, R.color.black))
                    .addActionIcon(R.drawable.ic_btn_trash)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    public void btnAddRecord(View view) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra("lata", DATABASE_NAME);
        intent.putExtra("abre", RECORDING_PASSWORD);
        addRecordActivityResultLauncher.launch(intent);
    }

    public void btnHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("lata", DATABASE_NAME);
        intent.putExtra("abre", RECORDING_PASSWORD);
        helpActivityResultLauncher.launch(intent);
    }

    public void btnDeleteDb(View view) {

    }

    public void btnExportDb(View view) {

    }

    private final ActivityResultLauncher<Intent> addRecordActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String searchText = searchView.getQuery().toString();

                        Intent intent = result.getData();

                        DATABASE_NAME = intent.getStringExtra("lata");
                        RECORDING_PASSWORD = intent.getStringExtra("abre");

                        String[] usernamesByteArray = intent.getStringArrayExtra("usernames");
                        String[] platformsByteArray = intent.getStringArrayExtra("platforms");
                        String[] passwordsByteArray = intent.getStringArrayExtra("passwords");

                        boolean matchInAdded = false;

                        for (int i = 0; i < usernamesByteArray.length; ++i) {
                            accountArray.add(new Account(
                                usernamesByteArray[i],
                                passwordsByteArray[i],
                                platformsByteArray[i]
                            ));

                            if (accountArray != adapter.getDataReference()) {
                                adapter.addOnlyItem(new Account(
                                        usernamesByteArray[i],
                                        passwordsByteArray[i],
                                        platformsByteArray[i]
                                ));
                            }

                            boolean usernameMatch = usernamesByteArray[i].toLowerCase().contains(searchText.toLowerCase());
                            boolean platformMatch = platformsByteArray[i].toLowerCase().contains(searchText.toLowerCase());

                            if (usernameMatch || platformMatch) {
                                matchInAdded = true;
                            }
                        }

                        if (matchInAdded) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> helpActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Intent intent = result.getData();

                        DATABASE_NAME = intent.getStringExtra("lata");
                        RECORDING_PASSWORD = intent.getStringExtra("abre");
                    }
                }
            }
    );

    private void readDb(String dbName) {
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
        }
    }
}