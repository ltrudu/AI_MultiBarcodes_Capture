package com.zebra.ai_multibarcodes_capture.helpers.camera;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;

import com.zebra.ai_multibarcodes_capture.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Model class representing an available camera and its capabilities.
 * Used for dynamic camera enumeration via Camera2 API.
 */
public class AvailableCamera {

    private String cameraId;
    private int lensFacing;
    private String displayName;
    private float focalLength;
    private boolean hasFlash;
    private boolean isLogicalCamera;
    private Set<String> physicalCameraIds;
    private List<Size> supportedResolutions;

    public AvailableCamera() {
        this.physicalCameraIds = new HashSet<>();
        this.supportedResolutions = new ArrayList<>();
    }

    public AvailableCamera(String cameraId, int lensFacing) {
        this();
        this.cameraId = cameraId;
        this.lensFacing = lensFacing;
    }

    // Getters and Setters

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public int getLensFacing() {
        return lensFacing;
    }

    public void setLensFacing(int lensFacing) {
        this.lensFacing = lensFacing;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public boolean hasFlash() {
        return hasFlash;
    }

    public void setHasFlash(boolean hasFlash) {
        this.hasFlash = hasFlash;
    }

    public boolean isLogicalCamera() {
        return isLogicalCamera;
    }

    public void setLogicalCamera(boolean logicalCamera) {
        isLogicalCamera = logicalCamera;
    }

    public Set<String> getPhysicalCameraIds() {
        return physicalCameraIds;
    }

    public void setPhysicalCameraIds(Set<String> physicalCameraIds) {
        this.physicalCameraIds = physicalCameraIds;
    }

    public List<Size> getSupportedResolutions() {
        return supportedResolutions;
    }

    public void setSupportedResolutions(List<Size> supportedResolutions) {
        this.supportedResolutions = supportedResolutions;
    }

    /**
     * Get a localized display name for the camera based on lens facing and focal length.
     * @param context Application context
     * @return Localized camera name
     */
    public String getLocalizedDisplayName(Context context) {
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }

        String baseName;
        switch (lensFacing) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                baseName = context.getString(R.string.camera_front);
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                baseName = context.getString(R.string.camera_back);
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                baseName = context.getString(R.string.camera_external);
                break;
            default:
                baseName = "Camera " + cameraId;
        }

        // Add camera type based on focal length for back cameras
        if (lensFacing == CameraCharacteristics.LENS_FACING_BACK && focalLength > 0) {
            String cameraType = getCameraTypeFromFocalLength(context, focalLength);
            if (cameraType != null) {
                return baseName + " (" + cameraType + ")";
            }
        }

        return baseName + " [" + cameraId + "]";
    }

    /**
     * Determines camera type (Wide, Telephoto, Ultra-Wide) based on focal length.
     * This is a heuristic based on common smartphone camera configurations.
     *
     * @param context Application context
     * @param focalLength The camera's focal length in mm
     * @return Camera type string or null if standard
     */
    private String getCameraTypeFromFocalLength(Context context, float focalLength) {
        // Typical smartphone focal lengths (35mm equivalent approximations):
        // Ultra-wide: < 2.5mm (roughly 10-16mm equivalent)
        // Wide (standard): 2.5mm - 6mm (roughly 24-28mm equivalent)
        // Telephoto: > 6mm (roughly 50mm+ equivalent)

        if (focalLength < 2.5f) {
            return context.getString(R.string.camera_ultrawide);
        } else if (focalLength > 6f) {
            return context.getString(R.string.camera_telephoto);
        } else {
            return context.getString(R.string.camera_wide);
        }
    }

    /**
     * Check if this camera is the primary back camera.
     * The primary back camera is typically the first back-facing camera with ID "0".
     * @return true if this is likely the primary back camera
     */
    public boolean isPrimaryBackCamera() {
        return lensFacing == CameraCharacteristics.LENS_FACING_BACK && "0".equals(cameraId);
    }

    /**
     * Get a formatted resolution string for display.
     * @param size The resolution size
     * @return Formatted string like "1920 x 1080 (2.1 MP)"
     */
    public static String formatResolution(Size size) {
        double megapixels = (size.getWidth() * size.getHeight()) / 1_000_000.0;
        return String.format("%d x %d (%.1f MP)", size.getWidth(), size.getHeight(), megapixels);
    }

    /**
     * Find the best matching resolution from supported resolutions.
     * @param targetWidth Desired width
     * @param targetHeight Desired height
     * @return Best matching Size or null if no resolutions available
     */
    public Size findBestMatchingResolution(int targetWidth, int targetHeight) {
        if (supportedResolutions == null || supportedResolutions.isEmpty()) {
            return null;
        }

        Size bestMatch = null;
        int minDiff = Integer.MAX_VALUE;

        for (Size size : supportedResolutions) {
            int diff = Math.abs(size.getWidth() - targetWidth) + Math.abs(size.getHeight() - targetHeight);
            if (diff < minDiff) {
                minDiff = diff;
                bestMatch = size;
            }
            // Exact match
            if (diff == 0) {
                return size;
            }
        }

        return bestMatch;
    }

    @Override
    public String toString() {
        return "AvailableCamera{" +
                "cameraId='" + cameraId + '\'' +
                ", lensFacing=" + lensFacing +
                ", displayName='" + displayName + '\'' +
                ", focalLength=" + focalLength +
                ", hasFlash=" + hasFlash +
                ", isLogicalCamera=" + isLogicalCamera +
                ", resolutions=" + supportedResolutions.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailableCamera that = (AvailableCamera) o;
        return cameraId != null ? cameraId.equals(that.cameraId) : that.cameraId == null;
    }

    @Override
    public int hashCode() {
        return cameraId != null ? cameraId.hashCode() : 0;
    }
}
