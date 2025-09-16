package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Helper class for secure credential storage using Android Keystore
 */
public class KeystoreHelper {
    private static final String TAG = "KeystoreHelper";
    private static final String KEYSTORE_ALIAS_USERNAME = "https_username_key";
    private static final String KEYSTORE_ALIAS_PASSWORD = "https_password_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PREF_USERNAME_ENCRYPTED = "encrypted_username";
    private static final String PREF_PASSWORD_ENCRYPTED = "encrypted_password";
    private static final String PREF_USERNAME_IV = "username_iv";
    private static final String PREF_PASSWORD_IV = "password_iv";
    private static final String PREF_PASSWORD_LENGTH = "password_length";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final Context context;

    public KeystoreHelper(Context context) {
        this.context = context;
    }

    /**
     * Stores username securely using Android Keystore
     */
    public boolean storeUsername(String username) {
        return storeCredential(username, KEYSTORE_ALIAS_USERNAME, PREF_USERNAME_ENCRYPTED, PREF_USERNAME_IV);
    }

    /**
     * Stores password securely using Android Keystore
     */
    public boolean storePassword(String password) {
        // Store the password length separately for display purposes
        if (password != null && !password.isEmpty()) {
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit()
                    .putInt(PREF_PASSWORD_LENGTH, password.length())
                    .apply();
        } else {
            // Remove the length if password is empty
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit()
                    .remove(PREF_PASSWORD_LENGTH)
                    .apply();
        }

        return storeCredential(password, KEYSTORE_ALIAS_PASSWORD, PREF_PASSWORD_ENCRYPTED, PREF_PASSWORD_IV);
    }

    /**
     * Retrieves username from secure storage
     */
    public String getUsername() {
        return getCredential(KEYSTORE_ALIAS_USERNAME, PREF_USERNAME_ENCRYPTED, PREF_USERNAME_IV);
    }

    /**
     * Retrieves password from secure storage
     */
    public String getPassword() {
        return getCredential(KEYSTORE_ALIAS_PASSWORD, PREF_PASSWORD_ENCRYPTED, PREF_PASSWORD_IV);
    }

    /**
     * Gets the length of stored password without decrypting it
     * This is more secure as it doesn't expose the actual password
     */
    public int getPasswordLength() {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getInt(PREF_PASSWORD_LENGTH, 0);
    }

    /**
     * Deletes stored username
     */
    public boolean deleteUsername() {
        return deleteCredential(KEYSTORE_ALIAS_USERNAME, PREF_USERNAME_ENCRYPTED, PREF_USERNAME_IV);
    }

    /**
     * Deletes stored password
     */
    public boolean deletePassword() {
        // Also remove the password length
        context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .edit()
                .remove(PREF_PASSWORD_LENGTH)
                .apply();

        return deleteCredential(KEYSTORE_ALIAS_PASSWORD, PREF_PASSWORD_ENCRYPTED, PREF_PASSWORD_IV);
    }

    private boolean storeCredential(String credential, String keyAlias, String encryptedPrefKey, String ivPrefKey) {
        if (credential == null || credential.isEmpty()) {
            // Delete the credential if empty
            return deleteCredential(keyAlias, encryptedPrefKey, ivPrefKey);
        }

        try {
            // Generate or get the secret key
            SecretKey secretKey = getOrCreateSecretKey(keyAlias);

            // Encrypt the credential
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryptedData = cipher.doFinal(credential.getBytes(StandardCharsets.UTF_8));

            // Store encrypted data and IV in SharedPreferences
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit()
                    .putString(encryptedPrefKey, Base64.encodeToString(encryptedData, Base64.DEFAULT))
                    .putString(ivPrefKey, Base64.encodeToString(iv, Base64.DEFAULT))
                    .apply();

            LogUtils.d(TAG, "Credential stored securely with alias: " + keyAlias);
            return true;

        } catch (Exception e) {
            LogUtils.e(TAG, "Error storing credential with alias " + keyAlias + ": " + e.getMessage());
            return false;
        }
    }

    private String getCredential(String keyAlias, String encryptedPrefKey, String ivPrefKey) {
        try {
            // Get encrypted data and IV from SharedPreferences
            String encryptedDataString = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .getString(encryptedPrefKey, null);
            String ivString = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .getString(ivPrefKey, null);

            if (encryptedDataString == null || ivString == null) {
                LogUtils.d(TAG, "No encrypted credential found for alias: " + keyAlias);
                return "";
            }

            // Get the secret key
            SecretKey secretKey = getOrCreateSecretKey(keyAlias);

            // Decrypt the credential
            byte[] encryptedData = Base64.decode(encryptedDataString, Base64.DEFAULT);
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedData = cipher.doFinal(encryptedData);
            String credential = new String(decryptedData, StandardCharsets.UTF_8);

            LogUtils.d(TAG, "Credential retrieved securely with alias: " + keyAlias);
            return credential;

        } catch (Exception e) {
            LogUtils.e(TAG, "Error retrieving credential with alias " + keyAlias + ": " + e.getMessage());
            return "";
        }
    }

    private boolean deleteCredential(String keyAlias, String encryptedPrefKey, String ivPrefKey) {
        try {
            // Delete from SharedPreferences
            context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit()
                    .remove(encryptedPrefKey)
                    .remove(ivPrefKey)
                    .apply();

            // Delete key from keystore
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            keyStore.deleteEntry(keyAlias);

            LogUtils.d(TAG, "Credential deleted for alias: " + keyAlias);
            return true;

        } catch (Exception e) {
            LogUtils.e(TAG, "Error deleting credential with alias " + keyAlias + ": " + e.getMessage());
            return false;
        }
    }

    private SecretKey getOrCreateSecretKey(String keyAlias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (keyStore.containsAlias(keyAlias)) {
            // Key exists, retrieve it
            return (SecretKey) keyStore.getKey(keyAlias, null);
        } else {
            // Key doesn't exist, create it
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false) // We handle IV generation
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            return keyGenerator.generateKey();
        }
    }

    /**
     * Checks if credentials are available
     */
    public boolean hasStoredCredentials() {
        String encryptedUsername = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getString(PREF_USERNAME_ENCRYPTED, null);
        String encryptedPassword = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                .getString(PREF_PASSWORD_ENCRYPTED, null);

        return encryptedUsername != null && encryptedPassword != null;
    }

    /**
     * Clears all stored credentials
     */
    public boolean clearAllCredentials() {
        boolean usernameDeleted = deleteUsername();
        boolean passwordDeleted = deletePassword();
        return usernameDeleted && passwordDeleted;
    }
}