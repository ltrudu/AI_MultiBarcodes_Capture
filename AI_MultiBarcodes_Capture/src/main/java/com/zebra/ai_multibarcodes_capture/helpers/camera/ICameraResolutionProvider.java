package com.zebra.ai_multibarcodes_capture.helpers.camera;

import android.content.Context;
import android.util.Size;

import androidx.camera.core.CameraSelector;

/**
 * Interface defining the contract for camera resolution providers.
 * Implementations can provide either static (preset) or dynamic (device-queried) resolutions.
 */
public interface ICameraResolutionProvider {

    /**
     * Get CameraSelector for CameraX binding.
     * @return CameraSelector configured for the selected camera
     */
    CameraSelector getCameraSelector();

    /**
     * Get resolution Size for ResolutionSelector.
     * @return The selected resolution Size
     */
    Size getResolution();

    /**
     * Get display name for current camera (for UI).
     * @param context Application context
     * @return Human-readable camera name
     */
    String getCameraDisplayName(Context context);

    /**
     * Get display name for current resolution (for UI).
     * @param context Application context
     * @return Human-readable resolution name (e.g., "1920 x 1080 (2MP)")
     */
    String getResolutionDisplayName(Context context);

    /**
     * Load settings from SharedPreferences.
     * @param context Application context
     */
    void loadSettings(Context context);

    /**
     * Save settings to SharedPreferences.
     * @param context Application context
     */
    void saveSettings(Context context);

    /**
     * Check if flash is available on current camera.
     * @param context Application context
     * @return true if flash is available
     */
    boolean hasFlash(Context context);

    /**
     * Get the resolution mode identifier.
     * @return "static" or "dynamic"
     */
    String getMode();

    /**
     * Check if the current settings are valid.
     * @param context Application context
     * @return true if the configuration is valid
     */
    boolean isValid(Context context);
}
