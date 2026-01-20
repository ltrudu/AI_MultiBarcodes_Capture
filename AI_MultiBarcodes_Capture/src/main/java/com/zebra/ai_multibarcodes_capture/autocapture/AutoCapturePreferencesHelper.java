package com.zebra.ai_multibarcodes_capture.autocapture;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

/**
 * Helper class to save and load auto capture conditions as JSON via SharedPreferences.
 */
public class AutoCapturePreferencesHelper {
    private static final String TAG = Constants.TAG;
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Saves the auto capture enabled state.
     *
     * @param context The application context
     * @param enabled Whether auto capture is enabled
     */
    public static void saveAutoCaptureEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHARED_PREFERENCES_AUTO_CAPTURE_ENABLED, enabled);
        editor.apply();
        LogUtils.d(TAG, "Auto capture enabled saved: " + enabled);
    }

    /**
     * Loads the auto capture enabled state.
     *
     * @param context The application context
     * @return Whether auto capture is enabled
     */
    public static boolean isAutoCaptureEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_AUTO_CAPTURE_ENABLED, Constants.SHARED_PREFERENCES_AUTO_CAPTURE_ENABLED_DEFAULT);
    }

    /**
     * Saves the auto capture conditions list as JSON.
     *
     * @param context The application context
     * @param conditionList The conditions to save
     */
    public static void saveConditions(Context context, AutoCaptureConditionList conditionList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (conditionList == null || conditionList.isEmpty()) {
            editor.putString(Constants.SHARED_PREFERENCES_AUTO_CAPTURE_CONDITIONS, "");
        } else {
            String json = gson.toJson(conditionList);
            editor.putString(Constants.SHARED_PREFERENCES_AUTO_CAPTURE_CONDITIONS, json);
            LogUtils.d(TAG, "Auto capture conditions saved: " + json);
        }

        editor.apply();
    }

    /**
     * Loads the auto capture conditions list from JSON.
     *
     * @param context The application context
     * @return The conditions list, or an empty list if none saved
     */
    public static AutoCaptureConditionList loadConditions(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(Constants.SHARED_PREFERENCES_AUTO_CAPTURE_CONDITIONS, "");

        if (json == null || json.isEmpty()) {
            return new AutoCaptureConditionList();
        }

        try {
            AutoCaptureConditionList conditionList = gson.fromJson(json, AutoCaptureConditionList.class);
            if (conditionList == null) {
                return new AutoCaptureConditionList();
            }
            LogUtils.d(TAG, "Auto capture conditions loaded: " + conditionList.size() + " conditions");
            return conditionList;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error parsing auto capture conditions JSON", e);
            return new AutoCaptureConditionList();
        }
    }

    /**
     * Clears all auto capture conditions.
     *
     * @param context The application context
     */
    public static void clearConditions(Context context) {
        saveConditions(context, new AutoCaptureConditionList());
    }
}
