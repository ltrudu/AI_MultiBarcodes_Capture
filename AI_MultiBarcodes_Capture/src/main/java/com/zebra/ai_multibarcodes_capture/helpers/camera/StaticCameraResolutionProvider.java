package com.zebra.ai_multibarcodes_capture.helpers.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Size;

import androidx.camera.core.CameraSelector;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.ECameraResolution;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

/**
 * Static camera resolution provider that uses predefined ECameraResolution presets.
 * Always uses the back camera (LENS_FACING_BACK).
 */
public class StaticCameraResolutionProvider implements ICameraResolutionProvider {

    private static final String TAG = "StaticCameraResProvider";

    private ECameraResolution selectedResolution;

    public StaticCameraResolutionProvider() {
        // Default to 2MP
        this.selectedResolution = ECameraResolution.MP_2;
    }

    @Override
    public CameraSelector getCameraSelector() {
        return new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
    }

    @Override
    public Size getResolution() {
        return new Size(selectedResolution.getWidth(), selectedResolution.getHeight());
    }

    @Override
    public String getCameraDisplayName(Context context) {
        return context.getString(R.string.camera_back);
    }

    @Override
    public String getResolutionDisplayName(Context context) {
        return selectedResolution.toString(context);
    }

    @Override
    public void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String resolutionString = prefs.getString(
                Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION,
                Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION_DEFAULT
        );

        try {
            selectedResolution = ECameraResolution.valueOf(resolutionString);
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "Invalid resolution string: " + resolutionString + ", using default");
            selectedResolution = ECameraResolution.MP_2;
        }

        LogUtils.d(TAG, "Loaded static resolution: " + selectedResolution.name() +
                " (" + selectedResolution.getWidth() + "x" + selectedResolution.getHeight() + ")");
    }

    @Override
    public void saveSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        prefs.edit()
                .putString(Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION, selectedResolution.name())
                .apply();
        LogUtils.d(TAG, "Saved static resolution: " + selectedResolution.name());
    }

    @Override
    public boolean hasFlash(Context context) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                String[] cameraIds = cameraManager.getCameraIdList();
                for (String cameraId : cameraIds) {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        return hasFlash != null && hasFlash;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error checking flash availability: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String getMode() {
        return CameraResolutionProviderFactory.MODE_STATIC;
    }

    @Override
    public boolean isValid(Context context) {
        // Static mode is always valid if we have a valid resolution
        return selectedResolution != null;
    }

    /**
     * Get the currently selected ECameraResolution.
     * @return Selected resolution enum
     */
    public ECameraResolution getSelectedResolution() {
        return selectedResolution;
    }

    /**
     * Set the selected resolution.
     * @param resolution Resolution enum value
     */
    public void setSelectedResolution(ECameraResolution resolution) {
        this.selectedResolution = resolution;
        LogUtils.d(TAG, "Set resolution to: " + resolution.name());
    }

    /**
     * Set the selected resolution by enum name.
     * @param resolutionName Resolution enum name (e.g., "MP_2")
     */
    public void setSelectedResolution(String resolutionName) {
        try {
            this.selectedResolution = ECameraResolution.valueOf(resolutionName);
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "Invalid resolution name: " + resolutionName);
        }
    }
}
