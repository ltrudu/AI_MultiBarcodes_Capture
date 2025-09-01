package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PreferencesHelper {

    public static void saveLastSessionFile(Context context, String filePath)
    {
        if(filePath == null || filePath.isEmpty())
        {
            Toast.makeText(context, "Error saving last selected file", Toast.LENGTH_SHORT).show();
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

}


