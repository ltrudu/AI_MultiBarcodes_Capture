package com.zebra.ai_multibarcodes_capture.filtering;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

/**
 * Helper class to save and load filtering conditions as JSON via SharedPreferences.
 */
public class FilteringPreferencesHelper {
    private static final String TAG = Constants.TAG;
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Saves the filtering enabled state.
     *
     * @param context The application context
     * @param enabled Whether filtering is enabled
     */
    public static void saveFilteringEnabled(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.SHARED_PREFERENCES_FILTERING_ENABLED, enabled);
        editor.apply();
        LogUtils.d(TAG, "Filtering enabled saved: " + enabled);
    }

    /**
     * Loads the filtering enabled state.
     *
     * @param context The application context
     * @return Whether filtering is enabled
     */
    public static boolean isFilteringEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_FILTERING_ENABLED, Constants.SHARED_PREFERENCES_FILTERING_ENABLED_DEFAULT);
    }

    /**
     * Saves the filtering conditions list as JSON.
     *
     * @param context The application context
     * @param conditionList The conditions to save
     */
    public static void saveConditions(Context context, FilteringConditionList conditionList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (conditionList == null || conditionList.isEmpty()) {
            editor.putString(Constants.SHARED_PREFERENCES_FILTERING_CONDITIONS, "");
        } else {
            String json = gson.toJson(conditionList);
            editor.putString(Constants.SHARED_PREFERENCES_FILTERING_CONDITIONS, json);
            LogUtils.d(TAG, "Filtering conditions saved: " + json);
        }

        editor.apply();
    }

    /**
     * Loads the filtering conditions list from JSON.
     *
     * @param context The application context
     * @return The conditions list, or an empty list if none saved
     */
    public static FilteringConditionList loadConditions(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(Constants.SHARED_PREFERENCES_FILTERING_CONDITIONS, "");

        if (json == null || json.isEmpty()) {
            return new FilteringConditionList();
        }

        try {
            FilteringConditionList conditionList = gson.fromJson(json, FilteringConditionList.class);
            if (conditionList == null) {
                return new FilteringConditionList();
            }
            LogUtils.d(TAG, "Filtering conditions loaded: " + conditionList.size() + " conditions");
            return conditionList;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error parsing filtering conditions JSON", e);
            return new FilteringConditionList();
        }
    }

    /**
     * Clears all filtering conditions.
     *
     * @param context The application context
     */
    public static void clearConditions(Context context) {
        saveConditions(context, new FilteringConditionList());
    }
}
