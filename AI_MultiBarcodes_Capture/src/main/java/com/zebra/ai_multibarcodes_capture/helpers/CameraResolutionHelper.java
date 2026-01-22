package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import com.zebra.ai_multibarcodes_capture.helpers.camera.AvailableCamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Get all available cameras on the device with their capabilities.
     *
     * @param context Application context
     * @return List of AvailableCamera objects, or empty list if unable to query
     */
    public static List<AvailableCamera> getAllAvailableCameras(Context context) {
        List<AvailableCamera> cameras = new ArrayList<>();

        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                LogUtils.e(TAG, "CameraManager is null");
                return cameras;
            }

            String[] cameraIds = cameraManager.getCameraIdList();
            LogUtils.d(TAG, "Found " + cameraIds.length + " cameras on device");

            for (String cameraId : cameraIds) {
                try {
                    CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                    AvailableCamera camera = createAvailableCamera(cameraId, characteristics);
                    if (camera != null) {
                        cameras.add(camera);
                        LogUtils.d(TAG, "Added camera: " + camera.toString());
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, "Error getting characteristics for camera " + cameraId + ": " + e.getMessage());
                }
            }

            // Sort cameras: back cameras first, then by focal length
            Collections.sort(cameras, new Comparator<AvailableCamera>() {
                @Override
                public int compare(AvailableCamera c1, AvailableCamera c2) {
                    // Back cameras first
                    if (c1.getLensFacing() != c2.getLensFacing()) {
                        if (c1.getLensFacing() == CameraCharacteristics.LENS_FACING_BACK) return -1;
                        if (c2.getLensFacing() == CameraCharacteristics.LENS_FACING_BACK) return 1;
                    }
                    // Then by camera ID (typically primary cameras have lower IDs)
                    return c1.getCameraId().compareTo(c2.getCameraId());
                }
            });

        } catch (Exception e) {
            LogUtils.e(TAG, "Error enumerating cameras: " + e.getMessage());
        }

        return cameras;
    }

    /**
     * Create an AvailableCamera object from CameraCharacteristics.
     *
     * @param cameraId Camera ID
     * @param characteristics Camera characteristics
     * @return AvailableCamera object or null if invalid
     */
    private static AvailableCamera createAvailableCamera(String cameraId, CameraCharacteristics characteristics) {
        AvailableCamera camera = new AvailableCamera();
        camera.setCameraId(cameraId);

        // Get lens facing
        Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (lensFacing == null) {
            LogUtils.w(TAG, "Camera " + cameraId + " has no lens facing info, skipping");
            return null;
        }
        camera.setLensFacing(lensFacing);

        // Get focal length
        float[] focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
        if (focalLengths != null && focalLengths.length > 0) {
            camera.setFocalLength(focalLengths[0]);
        }

        // Check flash availability
        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        camera.setHasFlash(hasFlash != null && hasFlash);

        // Check for logical multi-camera (API 28+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Set<String> physicalIds = characteristics.getPhysicalCameraIds();
            if (physicalIds != null && !physicalIds.isEmpty()) {
                camera.setPhysicalCameraIds(physicalIds);
                camera.setLogicalCamera(true);
            }
        }

        // Get supported resolutions
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
            if (sizes != null && sizes.length > 0) {
                List<Size> resolutions = new ArrayList<>(Arrays.asList(sizes));
                // Sort by resolution (largest first)
                Collections.sort(resolutions, new Comparator<Size>() {
                    @Override
                    public int compare(Size s1, Size s2) {
                        long pixels1 = (long) s1.getWidth() * s1.getHeight();
                        long pixels2 = (long) s2.getWidth() * s2.getHeight();
                        return Long.compare(pixels2, pixels1); // Descending
                    }
                });
                camera.setSupportedResolutions(resolutions);
            }

            // Also get high resolution sizes if available (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Size[] highResSizes = map.getHighResolutionOutputSizes(ImageFormat.YUV_420_888);
                if (highResSizes != null && highResSizes.length > 0) {
                    List<Size> existingResolutions = camera.getSupportedResolutions();
                    Set<String> existingSizeKeys = new HashSet<>();
                    for (Size size : existingResolutions) {
                        existingSizeKeys.add(size.getWidth() + "x" + size.getHeight());
                    }
                    for (Size highResSize : highResSizes) {
                        String key = highResSize.getWidth() + "x" + highResSize.getHeight();
                        if (!existingSizeKeys.contains(key)) {
                            existingResolutions.add(highResSize);
                        }
                    }
                    // Re-sort after adding high-res sizes
                    Collections.sort(existingResolutions, new Comparator<Size>() {
                        @Override
                        public int compare(Size s1, Size s2) {
                            long pixels1 = (long) s1.getWidth() * s1.getHeight();
                            long pixels2 = (long) s2.getWidth() * s2.getHeight();
                            return Long.compare(pixels2, pixels1);
                        }
                    });
                }
            }
        }

        return camera;
    }

    /**
     * Get supported resolutions for a specific camera.
     *
     * @param context Application context
     * @param cameraId Camera ID to query
     * @return List of supported Size objects, or empty list if unable to query
     */
    public static List<Size> getSupportedResolutions(Context context, String cameraId) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                return Collections.emptyList();
            }

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
                if (sizes != null) {
                    List<Size> resolutions = new ArrayList<>(Arrays.asList(sizes));
                    // Sort by resolution (largest first)
                    Collections.sort(resolutions, new Comparator<Size>() {
                        @Override
                        public int compare(Size s1, Size s2) {
                            long pixels1 = (long) s1.getWidth() * s1.getHeight();
                            long pixels2 = (long) s2.getWidth() * s2.getHeight();
                            return Long.compare(pixels2, pixels1);
                        }
                    });
                    return resolutions;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error getting resolutions for camera " + cameraId + ": " + e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Get a friendly display name for a camera based on its characteristics.
     *
     * @param context Application context
     * @param cameraId Camera ID
     * @return Display name string
     */
    public static String getCameraDisplayName(Context context, String cameraId) {
        List<AvailableCamera> cameras = getAllAvailableCameras(context);
        for (AvailableCamera camera : cameras) {
            if (camera.getCameraId().equals(cameraId)) {
                return camera.getLocalizedDisplayName(context);
            }
        }
        return "Camera " + cameraId;
    }

    /**
     * Check if flash is available for a specific camera.
     *
     * @param context Application context
     * @param cameraId Camera ID
     * @return true if flash is available
     */
    public static boolean hasFlash(Context context, String cameraId) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                return hasFlash != null && hasFlash;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error checking flash for camera " + cameraId + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Find the default back camera ID.
     *
     * @param context Application context
     * @return Camera ID of the primary back camera, or "0" as fallback
     */
    public static String getDefaultBackCameraId(Context context) {
        List<AvailableCamera> cameras = getAllAvailableCameras(context);
        for (AvailableCamera camera : cameras) {
            if (camera.getLensFacing() == CameraCharacteristics.LENS_FACING_BACK) {
                return camera.getCameraId();
            }
        }
        return "0"; // Fallback
    }

    /**
     * Check if a resolution is a standard video resolution.
     * Standard resolutions are multiples of 1920x1080 (Full HD) or 1280x720 (HD).
     *
     * Examples:
     * - 720p HD: 1280x720
     * - 1080p Full HD: 1920x1080
     * - 1440p QHD: 2560x1440
     * - 2160p 4K UHD: 3840x2160
     * - 4320p 8K UHD: 7680x4320
     *
     * @param width Resolution width
     * @param height Resolution height
     * @return true if it's a standard video resolution
     */
    private static boolean isStandardVideoResolution(int width, int height) {
        // Check for exact standard resolutions first
        // 720p
        if (width == 1280 && height == 720) return true;
        // 1080p Full HD
        if (width == 1920 && height == 1080) return true;
        // 1440p QHD (2K)
        if (width == 2560 && height == 1440) return true;
        // 2160p 4K UHD
        if (width == 3840 && height == 2160) return true;
        // 4320p 8K UHD
        if (width == 7680 && height == 4320) return true;

        // Check if dimensions are multiples of 1920 or 1080
        boolean widthIsMultipleOf1920 = (width % 1920 == 0);
        boolean widthIsMultipleOf1080 = (width % 1080 == 0);
        boolean heightIsMultipleOf1080 = (height % 1080 == 0);
        boolean heightIsMultipleOf1920 = (height % 1920 == 0);

        // Valid if width is multiple of 1920 and height is multiple of 1080 (landscape 16:9)
        if (widthIsMultipleOf1920 && heightIsMultipleOf1080) return true;

        // Valid if width is multiple of 1080 and height is multiple of 1920 (portrait 9:16)
        if (widthIsMultipleOf1080 && heightIsMultipleOf1920) return true;

        return false;
    }

    /**
     * Filter a list of resolutions to only include standard video resolutions.
     * Standard resolutions are 720p, 1080p, 1440p, 4K, 8K and multiples of 1920x1080.
     *
     * @param resolutions List of all available resolutions
     * @return Filtered list containing only standard video resolutions
     */
    public static List<Size> filterToStandardResolutions(List<Size> resolutions) {
        if (resolutions == null || resolutions.isEmpty()) {
            return resolutions;
        }

        List<Size> standardResolutions = new ArrayList<>();

        // Filter to only include standard video resolutions
        for (Size size : resolutions) {
            if (isStandardVideoResolution(size.getWidth(), size.getHeight())) {
                standardResolutions.add(size);
                LogUtils.d(TAG, "Standard resolution found: " + size.getWidth() + "x" + size.getHeight());
            }
        }

        // If no standard resolutions found, add 1080p as fallback if available, or top resolution
        if (standardResolutions.isEmpty()) {
            LogUtils.w(TAG, "No standard resolutions found, looking for closest match");
            // Try to find closest to 1080p
            Size closest = null;
            int minDiff = Integer.MAX_VALUE;
            for (Size size : resolutions) {
                int diff = Math.abs(size.getWidth() - 1920) + Math.abs(size.getHeight() - 1080);
                if (diff < minDiff) {
                    minDiff = diff;
                    closest = size;
                }
            }
            if (closest != null) {
                standardResolutions.add(closest);
                LogUtils.d(TAG, "Using closest resolution to 1080p: " + closest.getWidth() + "x" + closest.getHeight());
            }
        }

        // Sort by resolution (largest first)
        Collections.sort(standardResolutions, new Comparator<Size>() {
            @Override
            public int compare(Size s1, Size s2) {
                long pixels1 = (long) s1.getWidth() * s1.getHeight();
                long pixels2 = (long) s2.getWidth() * s2.getHeight();
                return Long.compare(pixels2, pixels1);
            }
        });

        LogUtils.d(TAG, "Filtered " + resolutions.size() + " resolutions to " + standardResolutions.size() + " standard resolutions");
        return standardResolutions;
    }

    /**
     * Get supported standard resolutions for a specific camera.
     * Only returns standard resolutions like 720p, 1080p, 4K, etc.
     *
     * @param context Application context
     * @param cameraId Camera ID to query
     * @return List of supported standard Size objects
     */
    public static List<Size> getStandardResolutions(Context context, String cameraId) {
        List<Size> allResolutions = getSupportedResolutions(context, cameraId);
        return filterToStandardResolutions(allResolutions);
    }
}
