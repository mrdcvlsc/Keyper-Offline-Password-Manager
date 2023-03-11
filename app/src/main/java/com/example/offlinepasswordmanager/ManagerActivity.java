package com.example.offlinepasswordmanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ManagerActivity extends AppCompatActivity {
    private RecyclerView accountRecyclerView;
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

        // load the database data into the accountArray

        accountArray = readDb(DATABASE_NAME);

        // setup RecyclerView and it's Adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new AccountRecyclerViewAdapter(accountArray);
        accountRecyclerView = findViewById(R.id.manager_rv_records);

        accountRecyclerView.setLayoutManager(layoutManager);
        accountRecyclerView.setItemAnimator(new DefaultItemAnimator());
        accountRecyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deleteAccountRecordCallBack);
        itemTouchHelper.attachToRecyclerView(accountRecyclerView);

        // setup SearchView
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

        // setup buttons
        FloatingActionButton viewBtnAddRecord = findViewById(R.id.manager_btn_add);
        ImageButton viewBtnExport = findViewById(R.id.manager_btn_export);
        ImageButton viewBtnDelete = findViewById(R.id.manager_btn_delete_db);
        ImageButton viewBtnHelp = findViewById(R.id.manager_btn_help);

        viewBtnAddRecord.setOnClickListener(this::btnAddRecord);
        viewBtnExport.setOnClickListener(this::btnExportDb);
        viewBtnDelete.setOnClickListener(this::btnDeleteDb);
        viewBtnHelp.setOnClickListener(this::btnHelp);
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
    
    private final ItemTouchHelper.SimpleCallback deleteAccountRecordCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
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
                    Toast.makeText(getApplicationContext(), "" + deletedRows + " record removed", Toast.LENGTH_SHORT).show();
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

    private void btnAddRecord(View view) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        intent.putExtra("lata", DATABASE_NAME);
        intent.putExtra("abre", RECORDING_PASSWORD);
        addRecordActivityResultLauncher.launch(intent);
    }

    private void btnHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("lata", DATABASE_NAME);
        intent.putExtra("abre", RECORDING_PASSWORD);
        helpActivityResultLauncher.launch(intent);
    }

    private void btnDeleteDb(View view) {
        String msg = "Are you sure you want to delete this database?";
        Snackbar.make(accountRecyclerView, msg, Snackbar.LENGTH_LONG).setAction("Yes", view1 -> {
            getApplicationContext().deleteDatabase(DATABASE_NAME + ".db");
            Toast.makeText(
                    ManagerActivity.this,
                    "The \"" + DATABASE_NAME + "\" database is deleted",
                    Toast.LENGTH_LONG
            ).show();
            onBackPressed();
        }).show();
    }

    private void btnExportDb(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerActivityLauncer.launch(intent);

    }

    private final ActivityResultLauncher<Intent> folderPickerActivityLauncer = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        Intent intent = result.getData();
                        Uri selectedFolderUri = intent.getData();

                        String dbFileName = DATABASE_NAME + ".db";

                        try {
                            // Create a new empty file in the specified folder
                            String outputMimeType = "application/octet-stream";
                            DocumentFile folder = DocumentFile.fromTreeUri(getApplicationContext(), selectedFolderUri);
                            DocumentFile outputFile = folder.createFile(outputMimeType, dbFileName);
                            OutputStream outputStream = getContentResolver().openOutputStream(outputFile.getUri());

                            // Read the database file
                            String dbOriginalPath = getDatabasePath(dbFileName).getAbsolutePath();
                            FileInputStream inputStream = new FileInputStream(dbOriginalPath);

                            // And copy it to the output stream
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }

                            // Close the streams
                            inputStream.close();
                            outputStream.flush();
                            outputStream.close();

                            Toast.makeText(getApplicationContext(), "Database backup created", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error creating database backup", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

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

                            boolean usernameMatch = usernamesByteArray[i].toLowerCase().contains(searchText.toLowerCase());
                            boolean platformMatch = platformsByteArray[i].toLowerCase().contains(searchText.toLowerCase());

                            if (accountArray != adapter.getDataReference() && (usernameMatch || platformMatch)) {
                                adapter.addOnlyItem(new Account(
                                        usernamesByteArray[i],
                                        passwordsByteArray[i],
                                        platformsByteArray[i]
                                ));
                            }

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

    private ArrayList<Account> readDb(String dbName) {
        String columnOrder = "username";
        String orderBy = "ASC"; // "DESC" - Ascending

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


            ArrayList<Account> accountDbData = new ArrayList<>();

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

                accountDbData.add(new Account(username, password, platform));
            }

            cursor.close();

            Toast.makeText(
                    this,
                    "Read a total of " + accountDbData.size() + " records",
                    Toast.LENGTH_SHORT
            ).show();

            dbHelper.close();
            return accountDbData;

        } catch (Exception err) {
            Toast.makeText(this, "Error reading the records", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }
    }

    // exit methods
    @Override
    public void onBackPressed() {
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