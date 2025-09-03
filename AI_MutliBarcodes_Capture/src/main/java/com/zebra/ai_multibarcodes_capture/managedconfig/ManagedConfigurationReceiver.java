package com.zebra.ai_multibarcodes_capture.managedconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.zebra.ai_multibarcodes_capture.helpers.Constants;

/**
 * BroadcastReceiver that handles managed configuration changes.
 * When the app receives a new managed configuration from an EMM/MDM system,
 * this receiver automatically writes the content to SharedPreferences.
 */
public class ManagedConfigurationReceiver extends BroadcastReceiver {

    private static final String TAG = "ManagedConfigReceiver";
    
    // Action for notifying SettingsActivity about configuration changes
    public static final String ACTION_RELOAD_PREFERENCES = "com.zebra.ai_multibarcodes_capture.RELOAD_PREFERENCES";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED.equals(action)) {
            Log.d(TAG, "Managed configuration changed, updating SharedPreferences");
            
            RestrictionsManager restrictionsManager = 
                (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
            
            if (restrictionsManager != null) {
                Bundle restrictions = restrictionsManager.getApplicationRestrictions();
                updateSharedPreferences(context, restrictions);
            } else {
                Log.w(TAG, "RestrictionsManager is null");
            }
        }
    }

    /**
     * Updates SharedPreferences with values from managed configuration
     * @param context Application context
     * @param restrictions Bundle containing managed configuration values
     */
    private void updateSharedPreferences(Context context, Bundle restrictions) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            // Update file prefix if provided
            if (restrictions.containsKey("prefix")) {
                String prefix = restrictions.getString("prefix");
                if (prefix != null && !prefix.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_PREFIX, prefix);
                    Log.d(TAG, "Updated prefix: " + prefix);
                }
            }

            // Update file extension if provided
            if (restrictions.containsKey("extension")) {
                String extension = restrictions.getString("extension");
                if (extension != null && !extension.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_EXTENSION, extension);
                    Log.d(TAG, "Updated extension: " + extension);
                }
            }

            // Update barcode symbologies from nested bundle
            if (restrictions.containsKey("barcode_symbologies")) {
                Bundle barcodeSymbologies = restrictions.getBundle("barcode_symbologies");
                if (barcodeSymbologies != null) {
                    updateBarcodeSymbologies(editor, barcodeSymbologies);
                }
            }

            // Apply all changes atomically
            editor.apply();
            Log.d(TAG, "Successfully updated SharedPreferences with managed configuration");

            // Notify SettingsActivity if it's currently open (will only be received if registered)
            Log.d(TAG, "Sending reload preferences intent to SettingsActivity");
            Intent reloadIntent = new Intent(ACTION_RELOAD_PREFERENCES);
            context.sendBroadcast(reloadIntent);

        } catch (Exception e) {
            Log.e(TAG, "Error updating SharedPreferences with managed configuration", e);
        }
    }

    /**
     * Updates barcode symbology preferences from the nested bundle
     * @param editor SharedPreferences editor
     * @param barcodeSymbologies Bundle containing barcode symbology settings
     */
    private void updateBarcodeSymbologies(SharedPreferences.Editor editor, Bundle barcodeSymbologies) {
        Log.d(TAG, "Updating barcode symbologies from managed configuration");

        // Postal barcodes
        updateBooleanSetting(editor, barcodeSymbologies, "AUSTRALIAN_POSTAL", Constants.SHARED_PREFERENCES_AUSTRALIAN_POSTAL);
        updateBooleanSetting(editor, barcodeSymbologies, "CANADIAN_POSTAL", Constants.SHARED_PREFERENCES_CANADIAN_POSTAL);
        updateBooleanSetting(editor, barcodeSymbologies, "DUTCH_POSTAL", Constants.SHARED_PREFERENCES_DUTCH_POSTAL);
        updateBooleanSetting(editor, barcodeSymbologies, "FINNISH_POSTAL_4S", Constants.SHARED_PREFERENCES_FINNISH_POSTAL_4S);
        updateBooleanSetting(editor, barcodeSymbologies, "JAPANESE_POSTAL", Constants.SHARED_PREFERENCES_JAPANESE_POSTAL);
        updateBooleanSetting(editor, barcodeSymbologies, "UK_POSTAL", Constants.SHARED_PREFERENCES_UK_POSTAL);
        updateBooleanSetting(editor, barcodeSymbologies, "USPLANET", Constants.SHARED_PREFERENCES_USPLANET);
        updateBooleanSetting(editor, barcodeSymbologies, "USPOSTNET", Constants.SHARED_PREFERENCES_USPOSTNET);
        updateBooleanSetting(editor, barcodeSymbologies, "US4STATE", Constants.SHARED_PREFERENCES_US4STATE);
        updateBooleanSetting(editor, barcodeSymbologies, "US4STATE_FICS", Constants.SHARED_PREFERENCES_US4STATE_FICS);
        updateBooleanSetting(editor, barcodeSymbologies, "MAILMARK", Constants.SHARED_PREFERENCES_MAILMARK);

        // 2D Matrix codes
        updateBooleanSetting(editor, barcodeSymbologies, "AZTEC", Constants.SHARED_PREFERENCES_AZTEC);
        updateBooleanSetting(editor, barcodeSymbologies, "DATAMATRIX", Constants.SHARED_PREFERENCES_DATAMATRIX);
        updateBooleanSetting(editor, barcodeSymbologies, "QRCODE", Constants.SHARED_PREFERENCES_QRCODE);
        updateBooleanSetting(editor, barcodeSymbologies, "MICROQR", Constants.SHARED_PREFERENCES_MICROQR);
        updateBooleanSetting(editor, barcodeSymbologies, "MAXICODE", Constants.SHARED_PREFERENCES_MAXICODE);
        updateBooleanSetting(editor, barcodeSymbologies, "PDF417", Constants.SHARED_PREFERENCES_PDF417);
        updateBooleanSetting(editor, barcodeSymbologies, "MICROPDF", Constants.SHARED_PREFERENCES_MICROPDF);
        updateBooleanSetting(editor, barcodeSymbologies, "DOTCODE", Constants.SHARED_PREFERENCES_DOTCODE);
        updateBooleanSetting(editor, barcodeSymbologies, "GRID_MATRIX", Constants.SHARED_PREFERENCES_GRID_MATRIX);
        updateBooleanSetting(editor, barcodeSymbologies, "HANXIN", Constants.SHARED_PREFERENCES_HANXIN);

        // Linear 1D barcodes
        updateBooleanSetting(editor, barcodeSymbologies, "CODE39", Constants.SHARED_PREFERENCES_CODE39);
        updateBooleanSetting(editor, barcodeSymbologies, "CODE128", Constants.SHARED_PREFERENCES_CODE128);
        updateBooleanSetting(editor, barcodeSymbologies, "CODE93", Constants.SHARED_PREFERENCES_CODE93);
        updateBooleanSetting(editor, barcodeSymbologies, "CODE11", Constants.SHARED_PREFERENCES_CODE11);
        updateBooleanSetting(editor, barcodeSymbologies, "CODABAR", Constants.SHARED_PREFERENCES_CODABAR);
        updateBooleanSetting(editor, barcodeSymbologies, "MSI", Constants.SHARED_PREFERENCES_MSI);

        // EAN/UPC codes
        updateBooleanSetting(editor, barcodeSymbologies, "EAN_8", Constants.SHARED_PREFERENCES_EAN_8);
        updateBooleanSetting(editor, barcodeSymbologies, "EAN_13", Constants.SHARED_PREFERENCES_EAN_13);
        updateBooleanSetting(editor, barcodeSymbologies, "UPC_A", Constants.SHARED_PREFERENCES_UPC_A);
        updateBooleanSetting(editor, barcodeSymbologies, "UPC_E", Constants.SHARED_PREFERENCES_UPC_E);
        updateBooleanSetting(editor, barcodeSymbologies, "UPCE1", Constants.SHARED_PREFERENCES_UPCE0);

        // GS1 DataBar
        updateBooleanSetting(editor, barcodeSymbologies, "GS1_DATABAR", Constants.SHARED_PREFERENCES_GS1_DATABAR);
        updateBooleanSetting(editor, barcodeSymbologies, "GS1_DATABAR_EXPANDED", Constants.SHARED_PREFERENCES_GS1_DATABAR_EXPANDED);
        updateBooleanSetting(editor, barcodeSymbologies, "GS1_DATABAR_LIM", Constants.SHARED_PREFERENCES_GS1_DATABAR_LIM);
        updateBooleanSetting(editor, barcodeSymbologies, "GS1_DATAMATRIX", Constants.SHARED_PREFERENCES_GS1_DATAMATRIX);
        updateBooleanSetting(editor, barcodeSymbologies, "GS1_QRCODE", Constants.SHARED_PREFERENCES_GS1_QRCODE);

        // 2 of 5 variants
        updateBooleanSetting(editor, barcodeSymbologies, "CHINESE_2OF5", Constants.SHARED_PREFERENCES_CHINESE_2OF5);
        updateBooleanSetting(editor, barcodeSymbologies, "D2OF5", Constants.SHARED_PREFERENCES_D2OF5);
        updateBooleanSetting(editor, barcodeSymbologies, "I2OF5", Constants.SHARED_PREFERENCES_I2OF5);
        updateBooleanSetting(editor, barcodeSymbologies, "MATRIX_2OF5", Constants.SHARED_PREFERENCES_MATRIX_2OF5);
        updateBooleanSetting(editor, barcodeSymbologies, "KOREAN_3OF5", Constants.SHARED_PREFERENCES_KOREAN_3OF5);

        // Composite codes
        updateBooleanSetting(editor, barcodeSymbologies, "COMPOSITE_AB", Constants.SHARED_PREFERENCES_COMPOSITE_AB);
        updateBooleanSetting(editor, barcodeSymbologies, "COMPOSITE_C", Constants.SHARED_PREFERENCES_COMPOSITE_C);

        // Other codes
        updateBooleanSetting(editor, barcodeSymbologies, "TLC39", Constants.SHARED_PREFERENCES_TLC39);
        updateBooleanSetting(editor, barcodeSymbologies, "TRIOPTIC39", Constants.SHARED_PREFERENCES_TRIOPTIC39);
    }

    /**
     * Helper method to update a boolean setting from managed configuration
     * @param editor SharedPreferences editor
     * @param bundle Bundle containing the managed configuration values
     * @param configKey Key in the managed configuration bundle
     * @param prefKey Key in SharedPreferences
     */
    private void updateBooleanSetting(SharedPreferences.Editor editor, Bundle bundle, 
                                    String configKey, String prefKey) {
        if (bundle.containsKey(configKey)) {
            boolean value = bundle.getBoolean(configKey);
            editor.putBoolean(prefKey, value);
            Log.d(TAG, "Updated " + configKey + ": " + value);
        }
    }

    /**
     * Public method to manually apply managed configuration
     * This can be called during app startup to ensure current restrictions are applied
     * @param context Application context
     */
    public static void applyManagedConfiguration(Context context) {
        Log.d(TAG, "Manually applying managed configuration");
        
        RestrictionsManager restrictionsManager = 
            (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
        
        if (restrictionsManager != null) {
            Bundle restrictions = restrictionsManager.getApplicationRestrictions();
            if (restrictions != null && !restrictions.isEmpty()) {
                ManagedConfigurationReceiver receiver = new ManagedConfigurationReceiver();
                receiver.updateSharedPreferences(context, restrictions);
            } else {
                Log.d(TAG, "No managed configuration restrictions found");
            }
        } else {
            Log.w(TAG, "RestrictionsManager is null");
        }
    }

}