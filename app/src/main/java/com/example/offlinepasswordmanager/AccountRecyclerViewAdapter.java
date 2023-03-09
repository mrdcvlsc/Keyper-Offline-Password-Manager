package com.example.offlinepasswordmanager;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<Account> accountArray;

    public ArrayList<Account> getDataReference() {
        return this.accountArray;
    }

    public AccountRecyclerViewAdapter(ArrayList<Account> accountArray) {
        this.accountArray = accountArray;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<Account> newData) {
        accountArray = newData;
        notifyDataSetChanged();
    }

    public void addOnlyItem(Account account) {
        accountArray.add(account);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView username, password, platform;
        private final Toast copyNotice;

        private static final int SHOW = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        private static final int HIDE = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

        public MyViewHolder(final View view) {
            super(view);
            username = view.findViewById(R.id.account_record_tv_username);
            platform = view.findViewById(R.id.account_record_tv_platform);
            password = view.findViewById(R.id.account_record_tv_password);
            copyNotice = Toast.makeText(view.getRootView().getContext(), "password copied to clipboard", Toast.LENGTH_LONG);

            view.findViewById(R.id.account_record_btn_show).setOnClickListener(v -> {
                int inputType = password.getInputType();
                switch (inputType) {
                    case SHOW:
                        password.setInputType(HIDE);
                        break;
                    case HIDE:
                        password.setInputType(SHOW);
                        break;
                }
            });

            view.findViewById(R.id.account_record_btn_copy).setOnClickListener(v -> {
                ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", password.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                copyNotice.show();
            });
        }

        public TextView getUsername() {
            return username;
        }

        public TextView getPlatform() {
            return platform;
        }

        public TextView getPassword() {
            return password;
        }
    }

    @NonNull
    @Override
    public AccountRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View accountRecordLayout = layoutInflater.inflate(R.layout.account_record, parent, false);
        return new MyViewHolder(accountRecordLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.username.setText(accountArray.get(position).getUsername());
        holder.platform.setText(accountArray.get(position).getPlatform());
        holder.password.setText(accountArray.get(position).getPassword());
    }

    @Override
    public int getItemCount() {
        if (accountArray == null) {
            return 0;
        }
        return accountArray.size();
    }
}
