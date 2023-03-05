package com.example.offlinepasswordmanager;

public class Account {
    private String username;
    private String password;
    private String platform;

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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
