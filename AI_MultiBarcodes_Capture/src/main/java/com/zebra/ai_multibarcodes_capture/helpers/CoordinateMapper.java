package com.zebra.ai_multibarcodes_capture.helpers;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Display;

/**
 * Helper class for coordinate transformations between different coordinate spaces:
 * - Raw sensor coordinates (the actual camera sensor)
 * - Effective image coordinates (after rotation adjustment)
 * - Overlay coordinates (screen display space)
 */
public class CoordinateMapper {

    private static final String TAG = Constants.TAG;

    // Image dimensions
    private int imageWidth;
    private int imageHeight;
    private int rawSensorWidth;
    private int rawSensorHeight;

    // Overlay dimensions
    private int overlayWidth;
    private int overlayHeight;

    // Rotation tracking
    private int initialRotation = 0;

    /**
     * Sets the image dimensions (effective dimensions after rotation).
     *
     * @param width The effective image width
     * @param height The effective image height
     */
    public void setImageDimensions(int width, int height) {
        this.imageWidth = width;
        this.imageHeight = height;
    }

    /**
     * Sets the raw sensor dimensions.
     *
     * @param width The raw sensor width
     * @param height The raw sensor height
     */
    public void setRawSensorDimensions(int width, int height) {
        this.rawSensorWidth = width;
        this.rawSensorHeight = height;
    }

    /**
     * Sets the overlay dimensions.
     *
     * @param width The overlay width
     * @param height The overlay height
     */
    public void setOverlayDimensions(int width, int height) {
        this.overlayWidth = width;
        this.overlayHeight = height;
    }

    /**
     * Sets the initial rotation.
     *
     * @param rotation The initial rotation value (Surface.ROTATION_*)
     */
    public void setInitialRotation(int rotation) {
        this.initialRotation = rotation;
    }

    /**
     * @return The effective image width
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * @return The effective image height
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * @return The raw sensor width
     */
    public int getRawSensorWidth() {
        return rawSensorWidth;
    }

    /**
     * @return The raw sensor height
     */
    public int getRawSensorHeight() {
        return rawSensorHeight;
    }

    /**
     * Maps a bounding box from effective image coordinates to overlay coordinates.
     *
     * @param bbox The bounding box in effective image coordinates
     * @param currentRotation The current display rotation
     * @return The bounding box in overlay coordinates
     */
    public Rect mapBoundingBoxToOverlay(Rect bbox, int currentRotation) {
        if (overlayWidth == 0 || overlayHeight == 0) {
            return bbox;
        }

        int relativeRotation = ((currentRotation - initialRotation + 4) % 4);
        Rect transformedBbox = transformBoundingBoxForRotation(bbox, relativeRotation);

        int effectiveImageWidth = imageWidth;
        int effectiveImageHeight = imageHeight;

        if (relativeRotation == 1 || relativeRotation == 3) {
            effectiveImageWidth = imageHeight;
            effectiveImageHeight = imageWidth;
        }

        float scaleX = (float) overlayWidth / effectiveImageWidth;
        float scaleY = (float) overlayHeight / effectiveImageHeight;
        float scale = Math.max(scaleX, scaleY);

        float offsetX = (overlayWidth - effectiveImageWidth * scale) / 2f;
        float offsetY = (overlayHeight - effectiveImageHeight * scale) / 2f;

        return new Rect(
                (int) (transformedBbox.left * scale + offsetX),
                (int) (transformedBbox.top * scale + offsetY),
                (int) (transformedBbox.right * scale + offsetX),
                (int) (transformedBbox.bottom * scale + offsetY)
        );
    }

    /**
     * Transforms a bounding box based on relative rotation.
     *
     * @param bbox The bounding box to transform
     * @param relativeRotation The relative rotation (0, 1, 2, or 3 representing 0°, 90°, 180°, 270°)
     * @return The transformed bounding box
     */
    public Rect transformBoundingBoxForRotation(Rect bbox, int relativeRotation) {
        switch (relativeRotation) {
            case 0:
                return new Rect(bbox);
            case 1:
                // 90 degree clockwise rotation
                return new Rect(
                        bbox.top,
                        imageWidth - bbox.right,
                        bbox.bottom,
                        imageWidth - bbox.left
                );
            case 2:
                // 180 degree rotation
                return new Rect(
                        imageWidth - bbox.right,
                        imageHeight - bbox.bottom,
                        imageWidth - bbox.left,
                        imageHeight - bbox.top
                );
            case 3:
                // 270 degree clockwise rotation
                return new Rect(
                        imageHeight - bbox.bottom,
                        bbox.left,
                        imageHeight - bbox.top,
                        bbox.right
                );
            default:
                LogUtils.w(TAG, "Unknown relative rotation: " + relativeRotation + ", using original bbox");
                return new Rect(bbox);
        }
    }

    /**
     * Converts overlay coordinates to raw sensor coordinates.
     * This is needed for cropping in raw sensor space.
     *
     * @param overlayRect The rectangle in overlay coordinates
     * @return The rectangle in raw sensor coordinates, or null if conversion fails
     */
    public Rect mapOverlayToRawSensorCoordinates(RectF overlayRect) {
        if (overlayRect == null) {
            return null;
        }

        if (overlayWidth == 0 || overlayHeight == 0 || rawSensorWidth == 0 || rawSensorHeight == 0) {
            LogUtils.w(TAG, "Cannot map overlay to raw sensor: invalid dimensions");
            return null;
        }

        // Determine the rotation needed to display the raw sensor image correctly
        int sensorToDisplayRotation = 0;
        if (imageWidth == rawSensorHeight && imageHeight == rawSensorWidth) {
            sensorToDisplayRotation = 90;
        } else if (imageWidth == rawSensorWidth && imageHeight == rawSensorHeight) {
            sensorToDisplayRotation = 0;
        }

        LogUtils.d(TAG, "Sensor to display rotation: " + sensorToDisplayRotation + "° (rawSensor=" +
                rawSensorWidth + "x" + rawSensorHeight + ", effective=" + imageWidth + "x" + imageHeight + ")");

        int effectiveImageWidth = imageWidth;
        int effectiveImageHeight = imageHeight;

        // Calculate scale and offset
        float scaleX = (float) overlayWidth / effectiveImageWidth;
        float scaleY = (float) overlayHeight / effectiveImageHeight;
        float scale = Math.max(scaleX, scaleY);

        float offsetX = (overlayWidth - effectiveImageWidth * scale) / 2f;
        float offsetY = (overlayHeight - effectiveImageHeight * scale) / 2f;

        // Step 1: Reverse scale and offset to get effective rotated image coordinates
        int effLeft = (int) ((overlayRect.left - offsetX) / scale);
        int effTop = (int) ((overlayRect.top - offsetY) / scale);
        int effRight = (int) ((overlayRect.right - offsetX) / scale);
        int effBottom = (int) ((overlayRect.bottom - offsetY) / scale);

        // Clamp to effective image bounds
        effLeft = Math.max(0, Math.min(effLeft, effectiveImageWidth));
        effTop = Math.max(0, Math.min(effTop, effectiveImageHeight));
        effRight = Math.max(effLeft, Math.min(effRight, effectiveImageWidth));
        effBottom = Math.max(effTop, Math.min(effBottom, effectiveImageHeight));

        LogUtils.d(TAG, "Effective image coords: (" + effLeft + "," + effTop + ") - (" + effRight + "," + effBottom + ")");

        // Step 2: Reverse rotation to get raw sensor coordinates
        Rect rawRect = reverseRotationDegreesToRawSensor(effLeft, effTop, effRight, effBottom, sensorToDisplayRotation);

        // Clamp to raw sensor bounds
        rawRect.left = Math.max(0, Math.min(rawRect.left, rawSensorWidth));
        rawRect.top = Math.max(0, Math.min(rawRect.top, rawSensorHeight));
        rawRect.right = Math.max(rawRect.left, Math.min(rawRect.right, rawSensorWidth));
        rawRect.bottom = Math.max(rawRect.top, Math.min(rawRect.bottom, rawSensorHeight));

        LogUtils.d(TAG, "Mapped overlay " + overlayRect + " to raw sensor " + rawRect + " (rotation=" + sensorToDisplayRotation + "°)");
        return rawRect;
    }

    /**
     * Reverses the rotation to convert from effective coordinates to raw sensor coordinates.
     *
     * @param left Left coordinate in effective image space
     * @param top Top coordinate in effective image space
     * @param right Right coordinate in effective image space
     * @param bottom Bottom coordinate in effective image space
     * @param rotationDegrees The rotation in degrees (0, 90, 180, 270)
     * @return Rectangle in raw sensor coordinates
     */
    public Rect reverseRotationDegreesToRawSensor(int left, int top, int right, int bottom, int rotationDegrees) {
        switch (rotationDegrees) {
            case 0:
                return new Rect(left, top, right, bottom);

            case 90:
                return new Rect(
                        top,
                        rawSensorHeight - right,
                        bottom,
                        rawSensorHeight - left
                );

            case 180:
                return new Rect(
                        rawSensorWidth - right,
                        rawSensorHeight - bottom,
                        rawSensorWidth - left,
                        rawSensorHeight - top
                );

            case 270:
                return new Rect(
                        rawSensorWidth - bottom,
                        left,
                        rawSensorWidth - top,
                        right
                );

            default:
                LogUtils.w(TAG, "Unknown rotation degrees: " + rotationDegrees);
                return new Rect(left, top, right, bottom);
        }
    }

    /**
     * Transforms a bounding box from raw sensor coordinates to effective image coordinates.
     * This is the forward rotation transformation.
     *
     * @param bbox The bounding box in raw sensor coordinates
     * @param rotationDegrees The rotation degrees from ImageProxy (0, 90, 180, 270)
     * @return The bounding box in effective image coordinates
     */
    public Rect transformRawSensorToEffective(Rect bbox, int rotationDegrees) {
        LogUtils.v(TAG, "Transforming raw bbox " + bbox + " with rotationDegrees=" + rotationDegrees);

        switch (rotationDegrees) {
            case 0:
                return new Rect(bbox);

            case 90:
                return new Rect(
                        rawSensorHeight - bbox.bottom,
                        bbox.left,
                        rawSensorHeight - bbox.top,
                        bbox.right
                );

            case 180:
                return new Rect(
                        rawSensorWidth - bbox.right,
                        rawSensorHeight - bbox.bottom,
                        rawSensorWidth - bbox.left,
                        rawSensorHeight - bbox.top
                );

            case 270:
                return new Rect(
                        bbox.top,
                        rawSensorWidth - bbox.right,
                        bbox.bottom,
                        rawSensorWidth - bbox.left
                );

            default:
                LogUtils.w(TAG, "Unknown rotation degrees for raw->effective: " + rotationDegrees);
                return new Rect(bbox);
        }
    }

    /**
     * Adjusts a bounding box from cropped space to full raw sensor space.
     *
     * @param bbox The bounding box relative to the cropped region
     * @param cropRegion The crop region in raw sensor coordinates
     * @return The bounding box in full raw sensor coordinates
     */
    public Rect adjustBboxForCropRegion(Rect bbox, Rect cropRegion) {
        return new Rect(
                bbox.left + cropRegion.left,
                bbox.top + cropRegion.top,
                bbox.right + cropRegion.left,
                bbox.bottom + cropRegion.top
        );
    }
}
