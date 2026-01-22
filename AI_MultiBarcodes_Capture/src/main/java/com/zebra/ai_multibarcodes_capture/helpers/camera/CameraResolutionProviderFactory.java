package com.zebra.ai_multibarcodes_capture.helpers.camera;

import android.content.Context;
import android.content.SharedPreferences;

import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

/**
 * Factory class to create the appropriate ICameraResolutionProvider based on user settings.
 */
public class CameraResolutionProviderFactory {

    private static final String TAG = "CameraResProviderFactory";

    public static final String MODE_STATIC = "static";
    public static final String MODE_DYNAMIC = "dynamic";

    /**
     * Create and initialize the appropriate camera resolution provider based on saved preferences.
     *
     * @param context Application context
     * @return Configured ICameraResolutionProvider
     */
    public static ICameraResolutionProvider create(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String mode = prefs.getString(Constants.SHARED_PREFERENCES_RESOLUTION_MODE, Constants.SHARED_PREFERENCES_RESOLUTION_MODE_DEFAULT);

        LogUtils.d(TAG, "Creating camera resolution provider for mode: " + mode);

        ICameraResolutionProvider provider;
        if (MODE_DYNAMIC.equals(mode)) {
            provider = new DynamicCameraResolutionProvider();
        } else {
            provider = new StaticCameraResolutionProvider();
        }

        provider.loadSettings(context);
        return provider;
    }

    /**
     * Create a specific provider type without loading settings.
     *
     * @param mode "static" or "dynamic"
     * @return Uninitialized provider
     */
    public static ICameraResolutionProvider createForMode(String mode) {
        if (MODE_DYNAMIC.equals(mode)) {
            return new DynamicCameraResolutionProvider();
        }
        return new StaticCameraResolutionProvider();
    }

    /**
     * Get the current resolution mode from preferences.
     *
     * @param context Application context
     * @return Current mode string ("static" or "dynamic")
     */
    public static String getCurrentMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString(Constants.SHARED_PREFERENCES_RESOLUTION_MODE, Constants.SHARED_PREFERENCES_RESOLUTION_MODE_DEFAULT);
    }

    /**
     * Set the resolution mode in preferences.
     *
     * @param context Application context
     * @param mode "static" or "dynamic"
     */
    public static void setMode(Context context, String mode) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.SHARED_PREFERENCES_RESOLUTION_MODE, mode).apply();
        LogUtils.d(TAG, "Resolution mode set to: " + mode);
    }
}
