package com.example.offlinepasswordmanager;

public class Account {
    private final String username;
    private final String password;
    private final String platform;

    public Account(String username, String password, String platform) {
        this.username = username;
        this.password = password;
        this.platform = platform;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPlatform() {
        return platform;
    }
}
