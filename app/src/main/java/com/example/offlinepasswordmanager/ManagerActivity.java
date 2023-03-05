package com.example.offlinepasswordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ManagerActivity extends AppCompatActivity {

    private ArrayList<Account> accountArray;
    private AccountRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        Log.d("onCreate", "initial setup loaded");

        accountArray = new ArrayList<>();
        accountArray.add(new Account("Alpha", "alpha_password", "facebook"));
        accountArray.add(new Account("Beta", "beta_password", "discord"));
        accountArray.add(new Account("Charlie", "charlie_password", "reddit"));
        accountArray.add(new Account("Delta", "delta_password", "stackoverflow"));
        accountArray.add(new Account("Epsilon", "epsilon_password", "twitter"));
        accountArray.add(new Account("Foxtrot", "foxtrot_password", "gmail"));
        accountArray.add(new Account("Micha", "micha_password", "JJ"));
        accountArray.add(new Account("Mikka", "Mikka_password", "KK"));
        accountArray.add(new Account("Lyka", "lyka_password", "NN"));
        accountArray.add(new Account("Multi", "random", "exchange"));
        accountArray.add(new Account("John", "Doe", "normal"));
        accountArray.add(new Account("Karren", "annoying", "shit"));
        accountArray.add(new Account("Kevin", "boy", "karen"));
        accountArray.add(new Account("Alaine", "Odle", "Ulbert"));
        accountArray.add(new Account("Bukubuku", "Chagama", "supreame_begin"));
        accountArray.add(new Account("Death", "star", "space"));
        accountArray.add(new Account("Vadar", "sith", "lord"));
        accountArray.add(new Account("Walker", "jedi", "master"));
        accountArray.add(new Account("Cloud", "greatsword", "finalfantasy"));
        accountArray.add(new Account("Sora", "keyblade", "kingdomhearts"));
        accountArray.add(new Account("Riku", "dragon", "breathoffire"));

        Log.d("onCreate", "ArrayList data loaded");
//
//        // set adapter
        RecyclerView accountRecyclerView = findViewById(R.id.manager_rv_records);
        adapter = new AccountRecyclerViewAdapter(accountArray);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        accountRecyclerView.setLayoutManager(layoutManager);
        accountRecyclerView.setItemAnimator(new DefaultItemAnimator());
        accountRecyclerView.setAdapter(adapter);

        Log.d("onCreate", "Adapter loaded");

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
//            int position = viewHolder.getAdapterPosition();
//
//            switch (direction) {
//                case ItemTouchHelper.LEFT:
//                    accountArray.remove(position);
//                    adapter.notifyItemRemoved(position);
//                    break;
//            }

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

            Log.d("Slide Action", "Detected Slide to left");
        }
    };

    public void btnAddRecord(View view) {
        Intent intent = new Intent(this, AddRecordActivity.class);
        startActivity(intent);
    }
}