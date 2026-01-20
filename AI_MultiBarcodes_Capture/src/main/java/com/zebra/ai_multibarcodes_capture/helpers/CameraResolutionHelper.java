package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to query camera capabilities and supported resolutions.
 * Uses Camera2 API to get the list of supported output sizes.
 */
public class CameraResolutionHelper {

    private static final String TAG = "CameraResolutionHelper";

    /**
     * Get the list of supported camera resolutions for the back camera.
     *
     * @param context Application context
     * @return List of supported Size objects, or empty list if unable to query
     */
    public static List<Size> getSupportedResolutions(Context context) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                LogUtils.e(TAG, "CameraManager is null");
                return Collections.emptyList();
            }

            String[] cameraIds = cameraManager.getCameraIdList();

            // Find back camera
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    if (map != null) {
                        // Get supported sizes for YUV_420_888 (what CameraX uses for ImageAnalysis)
                        Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
                        if (sizes != null) {
                            LogUtils.d(TAG, "Found " + sizes.length + " supported resolutions for back camera");
                            return Arrays.asList(sizes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error getting camera resolutions: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Check if a specific resolution is supported by the camera.
     *
     * @param width Target width
     * @param height Target height
     * @param supportedSizes List of supported sizes from getSupportedResolutions()
     * @return true if the exact resolution is supported, false otherwise
     */
    public static boolean isResolutionSupported(int width, int height, List<Size> supportedSizes) {
        for (Size size : supportedSizes) {
            if (size.getWidth() == width && size.getHeight() == height) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if an ECameraResolution is supported by the camera.
     *
     * @param resolution The ECameraResolution to check
     * @param supportedSizes List of supported sizes from getSupportedResolutions()
     * @return true if the resolution is supported, false otherwise
     */
    public static boolean isResolutionSupported(ECameraResolution resolution, List<Size> supportedSizes) {
        return isResolutionSupported(resolution.getWidth(), resolution.getHeight(), supportedSizes);
    }

    /**
     * Get a list of supported ECameraResolution values.
     *
     * @param context Application context
     * @return List of ECameraResolution values that are supported by the camera
     */
    public static List<ECameraResolution> getSupportedECameraResolutions(Context context) {
        List<Size> supportedSizes = getSupportedResolutions(context);
        List<ECameraResolution> supportedResolutions = new ArrayList<>();

        for (ECameraResolution resolution : ECameraResolution.values()) {
            if (isResolutionSupported(resolution, supportedSizes)) {
                supportedResolutions.add(resolution);
                LogUtils.d(TAG, "Resolution " + resolution.name() + " (" +
                        resolution.getWidth() + "x" + resolution.getHeight() + ") is supported");
            } else {
                LogUtils.d(TAG, "Resolution " + resolution.name() + " (" +
                        resolution.getWidth() + "x" + resolution.getHeight() + ") is NOT supported");
            }
        }

        return supportedResolutions;
    }

    /**
     * Log all supported camera resolutions for debugging purposes.
     *
     * @param context Application context
     */
    public static void logAllSupportedResolutions(Context context) {
        List<Size> supportedSizes = getSupportedResolutions(context);
        LogUtils.d(TAG, "=== All Supported Camera Resolutions ===");
        for (Size size : supportedSizes) {
            double megapixels = (size.getWidth() * size.getHeight()) / 1_000_000.0;
            LogUtils.d(TAG, String.format("  %dx%d (%.1f MP)",
                    size.getWidth(), size.getHeight(), megapixels));
        }
        LogUtils.d(TAG, "========================================");
    }
}
