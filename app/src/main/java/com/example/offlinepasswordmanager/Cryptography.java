package com.example.offlinepasswordmanager;

import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class Cryptography {

    public static void main(String[] Args)
    {
        try{
            String plainText = "this is a sensitive message";

            SecretKey secretKey = generateKey("RandomPasswordString", "RandomSaltString");
            IvParameterSpec randomIV = generateIV(16);
            String algorithm = "AES/CBC/PKCS5Padding";

            String cipherText = encrypt(algorithm, plainText, secretKey, randomIV);
            String recoverText = decrypt(algorithm, cipherText, secretKey, randomIV);

            System.out.println("plainText   : "+plainText+"\nlength = "+plainText.length()+"\n");
            System.out.println("cipherText  : "+cipherText+"\nlength = "+cipherText.length()+"\n");
            System.out.println("recoverText : "+recoverText+"\nlength = "+recoverText.length()+"\n");

            if(!recoverText.equals(plainText))
            {
                throw new AssertionError("AES ENCRYPTION-DECRYPTION : FAILED");
            }
            else
            {
                System.out.println("AES ENCRYPTION-DECRYPTION : PASSED");
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static SecretKey generateKey(String password, String salt)
            throws
            InvalidKeySpecException,
            NoSuchAlgorithmException
    {
        SecretKeyFactory KEYGENSCHEME = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), Hash.ITERATION_LEVEL, 128);
        return KEYGENSCHEME.generateSecret(keySpec);
    }

    public static IvParameterSpec generateIV(int bytes)
    {
        byte[] IV = new byte[bytes];
        new SecureRandom().nextBytes(IV);
        return new IvParameterSpec(IV);
    }

    public static String generateSalt(int allocatedBytes) {
        byte[] salt = new byte[allocatedBytes];
        new SecureRandom().nextBytes(salt);
        return ByteToString(salt);
    }

    public static IvParameterSpec getIV(byte[] IV)
    {
        return new IvParameterSpec(IV);
    }

    public static String ByteToString(byte[] array)
    {
        return new String(android.util.Base64.encode(array, Base64.DEFAULT));
    }

    public static byte[] StringToByte(String str)
    {
        return android.util.Base64.decode(str, Base64.DEFAULT);
    }

    public static String encrypt(String encryptionScheme, String plainText, SecretKey key, IvParameterSpec IV)
            throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            BadPaddingException,
            IllegalBlockSizeException
    {

        Cipher AESENCRYPT = Cipher.getInstance(encryptionScheme);
        AESENCRYPT.init(Cipher.ENCRYPT_MODE, key, IV);
        byte[] cipherText = AESENCRYPT.doFinal(plainText.getBytes());
        return ByteToString(cipherText);
    }

    public static String decrypt(String decryptionScheme, String cipherText, SecretKey key, IvParameterSpec IV)
            throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            BadPaddingException,
            IllegalBlockSizeException
    {

        Cipher AESDECRYPT = Cipher.getInstance(decryptionScheme);
        AESDECRYPT.init(Cipher.DECRYPT_MODE, key, IV);
        byte[] recoveredText = AESDECRYPT.doFinal(StringToByte(cipherText));
        return new String(recoveredText);
    }
}
