package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.zebra.ai_multibarcodes_capture.autocapture.AutoCapturePreferencesHelper;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.filtering.FilteringPreferencesHelper;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;

/**
 * Helper class that loads all camera activity settings from SharedPreferences.
 * Consolidates the multiple loadXxxSettings() methods from CameraXLivePreviewActivity.
 */
public class CameraActivitySettingsLoader {

    private static final String TAG = Constants.TAG;

    /**
     * Container class holding all loaded settings.
     */
    public static class CameraSettings {
        // Filtering settings
        public boolean isFilteringEnabled = false;
        public FilteringConditionList filteringConditions = new FilteringConditionList();

        // Capture mode settings
        public ECaptureTriggerMode captureTriggerMode = ECaptureTriggerMode.CAPTURE_ON_PRESS;

        // Display settings
        public boolean displayAnalysisPerSecond = false;

        // Autofocus settings
        public boolean forceContinuousAutofocus = false;

        // Debounce settings
        public boolean isDebounceEnabled = false;
        public int debounceMaxFrames = 10;
        public int debounceThreshold = 50;
        public int debounceAlgorithm = 0; // 0 = Center Distance, 1 = IOU
        public float debounceIouThreshold = 0.3f;

        // Auto capture settings
        public boolean isAutoCaptureEnabled = false;
        public AutoCaptureConditionList autoCaptureConditions = null;

        // High-res stabilization settings
        public boolean isHighResStabilizationEnabled = false;
        public int highResStabilityThreshold = 3;

        // Camera settings
        public ECameraResolution cameraResolution = ECameraResolution.MP_2;
        public String selectedCameraId = null;
    }

    /**
     * Loads all camera activity settings from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences
     * @return A CameraSettings object containing all loaded settings
     */
    public static CameraSettings loadAllSettings(Context context) {
        CameraSettings settings = new CameraSettings();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        // Load filtering settings
        loadFilteringSettings(context, settings);

        // Load capture mode settings
        loadCaptureModeSettings(sharedPreferences, settings);

        // Load display analysis settings
        loadDisplayAnalysisSettings(sharedPreferences, settings);

        // Load force continuous autofocus settings
        loadForceContinuousAutofocusSettings(sharedPreferences, settings);

        // Load debounce settings
        loadDebounceSettings(sharedPreferences, settings);

        // Load auto capture settings
        loadAutoCaptureSettings(context, settings);

        // Load high-res stabilization settings
        loadHighResStabilizationSettings(sharedPreferences, settings);

        // Load camera settings
        loadCameraSettings(sharedPreferences, settings);

        return settings;
    }

    private static void loadFilteringSettings(Context context, CameraSettings settings) {
        settings.isFilteringEnabled = FilteringPreferencesHelper.isFilteringEnabled(context);
        settings.filteringConditions = FilteringPreferencesHelper.loadConditions(context);
        LogUtils.d(TAG, "Loaded filtering settings - enabled: " + settings.isFilteringEnabled +
                ", conditions count: " + settings.filteringConditions.size());
    }

    private static void loadCaptureModeSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        String captureTriggerModeKey = sharedPreferences.getString(
                Constants.SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE,
                Constants.SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE_DEFAULT);
        settings.captureTriggerMode = ECaptureTriggerMode.fromKey(captureTriggerModeKey);
        LogUtils.d(TAG, "Loaded capture trigger mode: " + settings.captureTriggerMode.toString());
    }

    private static void loadDisplayAnalysisSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        settings.displayAnalysisPerSecond = sharedPreferences.getBoolean(
                Constants.SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND,
                Constants.SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND_DEFAULT);
        LogUtils.d(TAG, "Display analysis per second: " + settings.displayAnalysisPerSecond);
    }

    private static void loadForceContinuousAutofocusSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        settings.forceContinuousAutofocus = sharedPreferences.getBoolean(
                Constants.SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS,
                Constants.SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS_DEFAULT);
        LogUtils.d(TAG, "Force continuous autofocus: " + settings.forceContinuousAutofocus);
    }

    private static void loadDebounceSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        settings.isDebounceEnabled = sharedPreferences.getBoolean(
                Constants.SHARED_PREFERENCES_DEBOUNCE_ENABLED,
                Constants.SHARED_PREFERENCES_DEBOUNCE_ENABLED_DEFAULT);
        settings.debounceMaxFrames = sharedPreferences.getInt(
                Constants.SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES,
                Constants.SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES_DEFAULT);
        settings.debounceThreshold = sharedPreferences.getInt(
                Constants.SHARED_PREFERENCES_DEBOUNCE_THRESHOLD,
                Constants.SHARED_PREFERENCES_DEBOUNCE_THRESHOLD_DEFAULT);
        settings.debounceAlgorithm = sharedPreferences.getInt(
                Constants.SHARED_PREFERENCES_DEBOUNCE_ALGORITHM,
                Constants.SHARED_PREFERENCES_DEBOUNCE_ALGORITHM_DEFAULT);
        int iouThresholdInt = sharedPreferences.getInt(
                Constants.SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD,
                Constants.SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD_DEFAULT);
        settings.debounceIouThreshold = iouThresholdInt / 100.0f;

        LogUtils.d(TAG, "Debounce enabled: " + settings.isDebounceEnabled +
                ", maxFrames: " + settings.debounceMaxFrames +
                ", threshold: " + settings.debounceThreshold +
                ", algorithm: " + settings.debounceAlgorithm +
                ", iouThreshold: " + settings.debounceIouThreshold);
    }

    private static void loadAutoCaptureSettings(Context context, CameraSettings settings) {
        settings.isAutoCaptureEnabled = AutoCapturePreferencesHelper.isAutoCaptureEnabled(context);
        settings.autoCaptureConditions = AutoCapturePreferencesHelper.loadConditions(context);
        LogUtils.d(TAG, "Auto capture enabled: " + settings.isAutoCaptureEnabled +
                ", conditions: " + (settings.autoCaptureConditions != null ? settings.autoCaptureConditions.size() : 0));
    }

    private static void loadHighResStabilizationSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        settings.isHighResStabilizationEnabled = sharedPreferences.getBoolean(
                Constants.SHARED_PREFERENCES_HIGH_RES_STABILIZATION_ENABLED,
                Constants.SHARED_PREFERENCES_HIGH_RES_STABILIZATION_ENABLED_DEFAULT);
        settings.highResStabilityThreshold = sharedPreferences.getInt(
                Constants.SHARED_PREFERENCES_HIGH_RES_STABILITY_THRESHOLD,
                Constants.SHARED_PREFERENCES_HIGH_RES_STABILITY_THRESHOLD_DEFAULT);

        LogUtils.d(TAG, "High-res stabilization enabled: " + settings.isHighResStabilizationEnabled +
                ", threshold: " + settings.highResStabilityThreshold);
    }

    private static void loadCameraSettings(SharedPreferences sharedPreferences, CameraSettings settings) {
        String resolutionKey = sharedPreferences.getString(
                Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION,
                Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION_DEFAULT);
        try {
            settings.cameraResolution = ECameraResolution.valueOf(resolutionKey);
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "Invalid camera resolution key: " + resolutionKey + ", using default");
            settings.cameraResolution = ECameraResolution.MP_2;
        }

        settings.selectedCameraId = sharedPreferences.getString(
                Constants.SHARED_PREFERENCES_SELECTED_CAMERA_ID, null);

        LogUtils.d(TAG, "Camera resolution: " + settings.cameraResolution +
                ", selectedCameraId: " + settings.selectedCameraId);
    }
}
