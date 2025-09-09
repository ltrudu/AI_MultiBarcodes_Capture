package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

public class PreferencesHelper {

    public static void saveLastSessionFile(Context context, String filePath)
    {
        if(filePath == null || filePath.isEmpty())
        {
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_saving_last_selected_file), Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        // Get the SharedPreferences.Editor object to make changes
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Constants.SHARED_PREFERENCES_LAST_SESSION_FILE, filePath);

        // Commit the changes
        editor.commit();
    }

    public static String getLastSelectedSession(Context context)
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        // Retrieve the stored integer value, with a default value of 0 if not found
        String filePath = sharedPreferences.getString(Constants.SHARED_PREFERENCES_LAST_SESSION_FILE, null);
        return filePath;
    }

    public static void saveCurrentExtension(Context context, String extension)
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        // Get the SharedPreferences.Editor object to make changes
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Constants.SHARED_PREFERENCES_EXTENSION, extension);

        // Commit the changes
        editor.commit();

    }

    public static String getCurrentExtension(Context context)
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String extension = sharedPreferences.getString(Constants.SHARED_PREFERENCES_EXTENSION, Constants.FILE_DEFAULT_EXTENSION);
        return extension;
    }

    // Capture Zone preferences methods
    
    public static void saveCaptureZoneEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED, enabled);
        editor.apply();
    }
    
    public static boolean isCaptureZoneEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED, Constants.SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED_DEFAULT);
    }
    
    public static void saveCaptureZonePosition(Context context, int x, int y, int width, int height) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_X, x);
        editor.putInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_Y, y);
        editor.putInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH, width);
        editor.putInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT, height);
        editor.apply();
    }
    
    public static int getCaptureZoneX(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_X, Constants.SHARED_PREFERENCES_CAPTURE_ZONE_X_DEFAULT);
    }
    
    public static int getCaptureZoneY(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_Y, Constants.SHARED_PREFERENCES_CAPTURE_ZONE_Y_DEFAULT);
    }
    
    public static int getCaptureZoneWidth(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH, Constants.SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH_DEFAULT);
    }
    
    public static int getCaptureZoneHeight(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT, Constants.SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT_DEFAULT);
    }
    
    // Flashlight preferences methods
    
    public static void saveFlashlightEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHARED_PREFERENCES_FLASHLIGHT_ENABLED, enabled);
        boolean success = editor.commit();
        LogUtils.d("PreferencesHelper_FLASHLIGHT", "Saving flashlight state: " + enabled + ", success: " + success);
    }
    
    public static boolean isFlashlightEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        boolean enabled = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_FLASHLIGHT_ENABLED, Constants.SHARED_PREFERENCES_FLASHLIGHT_ENABLED_DEFAULT);
        LogUtils.d("PreferencesHelper_FLASHLIGHT", "Loading flashlight state: " + enabled + " (default: " + Constants.SHARED_PREFERENCES_FLASHLIGHT_ENABLED_DEFAULT + ")");
        return enabled;
    }
    
    // Language preferences methods
    
    public static void saveSelectedLanguage(Context context, String languageCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_LANGUAGE, languageCode);
        boolean success = editor.commit();
        LogUtils.d("PreferencesHelper_LANGUAGE", "Saving language: " + languageCode + ", success: " + success);
    }
    
    public static String getSelectedLanguage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String languageCode = sharedPreferences.getString(Constants.SHARED_PREFERENCES_LANGUAGE, Constants.SHARED_PREFERENCES_LANGUAGE_DEFAULT);
        LogUtils.d("PreferencesHelper_LANGUAGE", "Loading language: " + languageCode + " (default: " + Constants.SHARED_PREFERENCES_LANGUAGE_DEFAULT + ")");
        return languageCode;
    }

}


