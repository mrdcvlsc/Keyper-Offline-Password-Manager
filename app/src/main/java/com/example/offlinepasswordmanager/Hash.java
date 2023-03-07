package com.example.offlinepasswordmanager;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static final int ITERATION_LEVEL = 1000;

    public static String SHA512Hash(String password, String salt, int level){
        String hash = sha512(password + salt);
        for(int i=0; i<level; ++i){
            hash = sha512(hash);
        }
        return hash;
    }
    private static String sha512(String password){
        MessageDigest sha;
        byte[] hash;

        try{
            sha = MessageDigest.getInstance("SHA-512");
            hash = sha.digest(password.getBytes(StandardCharsets.UTF_8));
            return convertToHex(hash);
        }
        catch(NoSuchAlgorithmException e){
            Log.d("Hash.sha512", e.getMessage());
        }

        return "";
    }

    private static String convertToHex(byte[] raw){
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}