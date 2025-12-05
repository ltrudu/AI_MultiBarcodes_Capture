package com.zebra.ai_multibarcodes_capture.managedconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.TAG;

/**
 * BroadcastReceiver that handles managed configuration changes.
 * When the app receives a new managed configuration from an EMM/MDM system,
 * this receiver automatically writes the content to SharedPreferences.
 */
public class ManagedConfigurationReceiver extends BroadcastReceiver {

    // Action for notifying SettingsActivity about configuration changes
    public static final String ACTION_RELOAD_PREFERENCES = "com.zebra.ai_multibarcodes_capture.RELOAD_PREFERENCES";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED.equals(action)) {
            LogUtils.d(TAG, "Managed configuration changed, updating SharedPreferences");
            
            RestrictionsManager restrictionsManager = 
                (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
            
            if (restrictionsManager != null) {
                Bundle restrictions = restrictionsManager.getApplicationRestrictions();
                updateSharedPreferences(context, restrictions);
            } else {
                LogUtils.w(TAG, "RestrictionsManager is null");
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
                    LogUtils.d(TAG, "Updated prefix: " + prefix);
                }
            }

            // Update file extension if provided
            if (restrictions.containsKey("extension")) {
                String extension = restrictions.getString("extension");
                if (extension != null && !extension.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_EXTENSION, extension);
                    LogUtils.d(TAG, "Updated extension: " + extension);
                }
            }

            // Update language if provided
            if (restrictions.containsKey("language")) {
                String language = restrictions.getString("language");
                if (language != null && !language.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_LANGUAGE, language);
                    LogUtils.d(TAG, "Updated language: " + language);
                }
            }

            // Update capture trigger mode if provided
            if (restrictions.containsKey("capture_trigger_mode")) {
                String captureTriggerMode = restrictions.getString("capture_trigger_mode");
                if (captureTriggerMode != null && !captureTriggerMode.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE, captureTriggerMode);
                    LogUtils.d(TAG, "Updated capture_trigger_mode: " + captureTriggerMode);
                }
            }

            // Update barcode symbologies from nested bundle
            if (restrictions.containsKey("barcode_symbologies")) {
                Bundle barcodeSymbologies = restrictions.getBundle("barcode_symbologies");
                if (barcodeSymbologies != null) {
                    updateBarcodeSymbologies(editor, barcodeSymbologies);
                }
            }

            // Update processing mode if provided
            if (restrictions.containsKey("processing_mode")) {
                String processingMode = restrictions.getString("processing_mode");
                if (processingMode != null && !processingMode.trim().isEmpty()) {
                    editor.putString(Constants.SHARED_PREFERENCES_PROCESSING_MODE, processingMode);
                    LogUtils.d(TAG, "Updated processing_mode: " + processingMode);
                }
            }

            // Update HTTPS configuration from nested bundle
            if (restrictions.containsKey("https_configuration")) {
                Bundle httpsConfig = restrictions.getBundle("https_configuration");
                if (httpsConfig != null) {
                    updateHttpsConfiguration(editor, httpsConfig);
                }
            }

            // Update advanced settings from nested bundle
            if (restrictions.containsKey("advanced_settings")) {
                Bundle advancedSettings = restrictions.getBundle("advanced_settings");
                if (advancedSettings != null) {
                    updateAdvancedSettings(editor, advancedSettings);
                }
            }

            // Update filtering settings from nested bundle
            if (restrictions.containsKey("filtering_settings")) {
                Bundle filteringSettings = restrictions.getBundle("filtering_settings");
                if (filteringSettings != null) {
                    updateFilteringSettings(editor, filteringSettings);
                }
            }

            // Apply all changes atomically
            editor.apply();
            LogUtils.d(TAG, "Successfully updated SharedPreferences with managed configuration");

            // Notify SettingsActivity and CameraXLivePreviewActivity if they're currently open (will only be received if registered)
            LogUtils.d(TAG, "Sending reload preferences intent to registered activities");
            Intent reloadIntent = new Intent(ACTION_RELOAD_PREFERENCES);
            context.sendBroadcast(reloadIntent);

        } catch (Exception e) {
            LogUtils.e(TAG, "Error updating SharedPreferences with managed configuration", e);
        }
    }

    /**
     * Updates barcode symbology preferences from the nested bundle
     * @param editor SharedPreferences editor
     * @param barcodeSymbologies Bundle containing barcode symbology settings
     */
    private void updateBarcodeSymbologies(SharedPreferences.Editor editor, Bundle barcodeSymbologies) {
        LogUtils.d(TAG, "Updating barcode symbologies from managed configuration");

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
     * Updates advanced settings preferences from the nested bundle
     * @param editor SharedPreferences editor
     * @param advancedSettings Bundle containing advanced settings
     */
    private void updateAdvancedSettings(SharedPreferences.Editor editor, Bundle advancedSettings) {
        LogUtils.d(TAG, "Updating advanced settings from managed configuration");

        // Update model input size
        updateStringSetting(editor, advancedSettings, "model_input_size", Constants.SHARED_PREFERENCES_MODEL_INPUT_SIZE);

        // Update camera resolution
        updateStringSetting(editor, advancedSettings, "camera_resolution", Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION);

        // Update inference type
        updateStringSetting(editor, advancedSettings, "inference_type", Constants.SHARED_PREFERENCES_INFERENCE_TYPE);

        // Update display analysis per second
        updateBooleanSetting(editor, advancedSettings, "display_analysis_per_second", Constants.SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND);

        // Update logging enabled - also apply immediately to LogUtils
        if (advancedSettings.containsKey("logging_enabled")) {
            boolean loggingEnabled = advancedSettings.getBoolean("logging_enabled");
            editor.putBoolean(Constants.SHARED_PREFERENCES_LOGGING_ENABLED, loggingEnabled);
            LogUtils.setLoggingEnabled(loggingEnabled);
            LogUtils.d(TAG, "Updated logging_enabled: " + loggingEnabled);
        }
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
            LogUtils.d(TAG, "Updated " + configKey + ": " + value);
        }
    }

    /**
     * Helper method to update a string setting from managed configuration
     * @param editor SharedPreferences editor
     * @param bundle Bundle containing the managed configuration values
     * @param configKey Key in the managed configuration bundle
     * @param prefKey Key in SharedPreferences
     */
    private void updateStringSetting(SharedPreferences.Editor editor, Bundle bundle, 
                                   String configKey, String prefKey) {
        if (bundle.containsKey(configKey)) {
            String value = bundle.getString(configKey);
            if (value != null && !value.trim().isEmpty()) {
                editor.putString(prefKey, value);
                LogUtils.d(TAG, "Updated " + configKey + ": " + value);
            }
        }
    }

    /**
     * Updates HTTPS configuration preferences from the nested bundle
     * @param editor SharedPreferences editor
     * @param httpsConfig Bundle containing HTTPS configuration settings
     */
    private void updateHttpsConfiguration(SharedPreferences.Editor editor, Bundle httpsConfig) {
        LogUtils.d(TAG, "Updating HTTPS configuration from managed configuration");

        // Update HTTPS endpoint
        updateStringSetting(editor, httpsConfig, "https_endpoint", Constants.SHARED_PREFERENCES_HTTPS_ENDPOINT);
    }

    /**
     * Updates filtering settings preferences from the nested bundle
     * @param editor SharedPreferences editor
     * @param filteringSettings Bundle containing filtering settings
     */
    private void updateFilteringSettings(SharedPreferences.Editor editor, Bundle filteringSettings) {
        LogUtils.d(TAG, "Updating filtering settings from managed configuration");

        // Update filtering enabled setting
        updateBooleanSetting(editor, filteringSettings, "filtering_enabled", Constants.SHARED_PREFERENCES_FILTERING_ENABLED);

        // Update filtering regex
        updateStringSetting(editor, filteringSettings, "filtering_regex", Constants.SHARED_PREFERENCES_FILTERING_REGEX);
    }

    /**
     * Public method to manually apply managed configuration
     * This can be called during app startup to ensure current restrictions are applied
     * @param context Application context
     */
    public static void applyManagedConfiguration(Context context) {
        LogUtils.d(TAG, "Manually applying managed configuration");

        RestrictionsManager restrictionsManager =
            (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);

        if (restrictionsManager != null) {
            Bundle restrictions = restrictionsManager.getApplicationRestrictions();
            if (restrictions != null && !restrictions.isEmpty()) {
                ManagedConfigurationReceiver receiver = new ManagedConfigurationReceiver();
                receiver.updateSharedPreferences(context, restrictions);
            } else {
                LogUtils.d(TAG, "No managed configuration restrictions found");
            }
        } else {
            LogUtils.w(TAG, "RestrictionsManager is null");
        }
    }

}