package com.zebra.ai_multibarcodes_capture.helpers.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;

import com.zebra.ai_multibarcodes_capture.helpers.CameraResolutionHelper;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.util.List;

/**
 * Dynamic camera resolution provider that queries Camera2 API for all available cameras
 * and their supported resolutions.
 */
public class DynamicCameraResolutionProvider implements ICameraResolutionProvider {

    private static final String TAG = "DynamicCameraResProvider";

    private String selectedCameraId;
    private Size selectedResolution;
    private List<AvailableCamera> availableCameras;

    public DynamicCameraResolutionProvider() {
        // Default values
        this.selectedCameraId = "0";
        this.selectedResolution = new Size(1920, 1080);
    }

    @Override
    public CameraSelector getCameraSelector() {
        // Create a CameraSelector that filters by camera ID
        return new CameraSelector.Builder()
                .addCameraFilter(cameraInfos -> {
                    for (CameraInfo cameraInfo : cameraInfos) {
                        // CameraInfo doesn't directly expose camera ID in CameraX 1.x
                        // We use a workaround by checking lens facing and trying to match
                        // For a more robust solution, we'd need to use Camera2 interop
                    }
                    // Return all cameras and let CameraX pick the right one
                    // We'll use requireLensFacing based on the selected camera's lens facing
                    return cameraInfos;
                })
                .build();
    }

    /**
     * Get a CameraSelector that requires the lens facing of the selected camera.
     * This is the preferred method for CameraX binding.
     *
     * @param context Application context
     * @return CameraSelector configured for the selected camera's lens facing
     */
    public CameraSelector getCameraSelector(Context context) {
        AvailableCamera selectedCamera = getSelectedCamera(context);
        if (selectedCamera != null) {
            int lensFacing = selectedCamera.getLensFacing();
            int cameraXLensFacing = (lensFacing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT)
                    ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;

            LogUtils.d(TAG, "Creating CameraSelector for camera " + selectedCameraId +
                    " with lens facing: " + (cameraXLensFacing == CameraSelector.LENS_FACING_FRONT ? "FRONT" : "BACK"));

            return new CameraSelector.Builder()
                    .requireLensFacing(cameraXLensFacing)
                    .build();
        }

        // Fallback to back camera
        LogUtils.w(TAG, "Selected camera not found, falling back to back camera");
        return new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
    }

    @Override
    public Size getResolution() {
        return selectedResolution;
    }

    @Override
    public String getCameraDisplayName(Context context) {
        AvailableCamera camera = getSelectedCamera(context);
        if (camera != null) {
            return camera.getLocalizedDisplayName(context);
        }
        return "Camera " + selectedCameraId;
    }

    @Override
    public String getResolutionDisplayName(Context context) {
        return AvailableCamera.formatResolution(selectedResolution);
    }

    @Override
    public void loadSettings(Context context) {
        // First, refresh the available cameras list
        availableCameras = CameraResolutionHelper.getAllAvailableCameras(context);
        LogUtils.d(TAG, "Loaded " + availableCameras.size() + " available cameras");

        // Load saved settings
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        selectedCameraId = prefs.getString(
                Constants.SHARED_PREFERENCES_SELECTED_CAMERA_ID,
                Constants.SHARED_PREFERENCES_SELECTED_CAMERA_ID_DEFAULT
        );

        int width = prefs.getInt(
                Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_WIDTH,
                Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_WIDTH_DEFAULT
        );
        int height = prefs.getInt(
                Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_HEIGHT,
                Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_HEIGHT_DEFAULT
        );
        selectedResolution = new Size(width, height);

        // Validate that the selected camera still exists
        if (!isCameraIdValid(selectedCameraId)) {
            LogUtils.w(TAG, "Selected camera ID " + selectedCameraId + " is no longer valid, resetting to default");
            selectedCameraId = CameraResolutionHelper.getDefaultBackCameraId(context);
            saveSettings(context);
        }

        // Validate that the selected resolution is supported by the camera
        AvailableCamera camera = getSelectedCamera(context);
        if (camera != null && !isResolutionSupported(selectedResolution, camera.getSupportedResolutions())) {
            LogUtils.w(TAG, "Selected resolution " + selectedResolution + " is not supported, finding best match");
            Size bestMatch = camera.findBestMatchingResolution(width, height);
            if (bestMatch != null) {
                selectedResolution = bestMatch;
                saveSettings(context);
            }
        }

        LogUtils.d(TAG, "Loaded dynamic settings: camera=" + selectedCameraId +
                ", resolution=" + selectedResolution.getWidth() + "x" + selectedResolution.getHeight());
    }

    @Override
    public void saveSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        prefs.edit()
                .putString(Constants.SHARED_PREFERENCES_SELECTED_CAMERA_ID, selectedCameraId)
                .putInt(Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_WIDTH, selectedResolution.getWidth())
                .putInt(Constants.SHARED_PREFERENCES_DYNAMIC_RESOLUTION_HEIGHT, selectedResolution.getHeight())
                .apply();
        LogUtils.d(TAG, "Saved dynamic settings: camera=" + selectedCameraId +
                ", resolution=" + selectedResolution.getWidth() + "x" + selectedResolution.getHeight());
    }

    @Override
    public boolean hasFlash(Context context) {
        AvailableCamera camera = getSelectedCamera(context);
        return camera != null && camera.hasFlash();
    }

    @Override
    public String getMode() {
        return CameraResolutionProviderFactory.MODE_DYNAMIC;
    }

    @Override
    public boolean isValid(Context context) {
        return selectedCameraId != null &&
               selectedResolution != null &&
               isCameraIdValid(selectedCameraId);
    }

    // ===== Dynamic Mode Specific Methods =====

    /**
     * Get all available cameras.
     * Call loadSettings() first to populate this list.
     *
     * @return List of available cameras
     */
    public List<AvailableCamera> getAvailableCameras() {
        return availableCameras;
    }

    /**
     * Get all available cameras, refreshing from the system if needed.
     *
     * @param context Application context
     * @return List of available cameras
     */
    public List<AvailableCamera> getAvailableCameras(Context context) {
        if (availableCameras == null || availableCameras.isEmpty()) {
            availableCameras = CameraResolutionHelper.getAllAvailableCameras(context);
        }
        return availableCameras;
    }

    /**
     * Get resolutions for a specific camera.
     *
     * @param context Application context
     * @param cameraId Camera ID
     * @return List of supported resolutions
     */
    public List<Size> getResolutionsForCamera(Context context, String cameraId) {
        return CameraResolutionHelper.getSupportedResolutions(context, cameraId);
    }

    /**
     * Get resolutions for the currently selected camera.
     *
     * @param context Application context
     * @return List of supported resolutions
     */
    public List<Size> getResolutionsForSelectedCamera(Context context) {
        AvailableCamera camera = getSelectedCamera(context);
        if (camera != null) {
            return camera.getSupportedResolutions();
        }
        return CameraResolutionHelper.getSupportedResolutions(context, selectedCameraId);
    }

    /**
     * Get the selected camera ID.
     *
     * @return Selected camera ID
     */
    public String getSelectedCameraId() {
        return selectedCameraId;
    }

    /**
     * Set the selected camera ID.
     *
     * @param cameraId Camera ID to select
     */
    public void setSelectedCameraId(String cameraId) {
        this.selectedCameraId = cameraId;
        LogUtils.d(TAG, "Set camera ID to: " + cameraId);
    }

    /**
     * Set the selected resolution.
     *
     * @param resolution Resolution to select
     */
    public void setSelectedResolution(Size resolution) {
        this.selectedResolution = resolution;
        LogUtils.d(TAG, "Set resolution to: " + resolution.getWidth() + "x" + resolution.getHeight());
    }

    /**
     * Set the selected resolution by dimensions.
     *
     * @param width Resolution width
     * @param height Resolution height
     */
    public void setSelectedResolution(int width, int height) {
        this.selectedResolution = new Size(width, height);
        LogUtils.d(TAG, "Set resolution to: " + width + "x" + height);
    }

    /**
     * Get the AvailableCamera object for the selected camera.
     *
     * @param context Application context
     * @return Selected AvailableCamera or null if not found
     */
    public AvailableCamera getSelectedCamera(Context context) {
        List<AvailableCamera> cameras = getAvailableCameras(context);
        for (AvailableCamera camera : cameras) {
            if (camera.getCameraId().equals(selectedCameraId)) {
                return camera;
            }
        }
        return null;
    }

    /**
     * Check if a camera ID is valid (exists in available cameras).
     *
     * @param cameraId Camera ID to check
     * @return true if the camera exists
     */
    private boolean isCameraIdValid(String cameraId) {
        if (availableCameras == null) {
            return true; // Can't validate without camera list
        }
        for (AvailableCamera camera : availableCameras) {
            if (camera.getCameraId().equals(cameraId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a resolution is in the list of supported resolutions.
     *
     * @param resolution Resolution to check
     * @param supportedResolutions List of supported resolutions
     * @return true if the resolution is supported
     */
    private boolean isResolutionSupported(Size resolution, List<Size> supportedResolutions) {
        if (supportedResolutions == null) {
            return true; // Can't validate without resolution list
        }
        for (Size size : supportedResolutions) {
            if (size.getWidth() == resolution.getWidth() && size.getHeight() == resolution.getHeight()) {
                return true;
            }
        }
        return false;
    }
}
