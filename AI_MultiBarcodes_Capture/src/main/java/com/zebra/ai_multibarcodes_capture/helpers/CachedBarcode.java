package com.zebra.ai_multibarcodes_capture.helpers;

import android.graphics.Rect;

import com.zebra.ai.vision.entity.BarcodeEntity;

/**
 * Represents a cached barcode entry for debouncing purposes.
 * Stores the barcode entity, overlay rect, and frame age.
 */
public class CachedBarcode {
    private BarcodeEntity entity;
    private Rect overlayRect;
    private int frameAge;

    /**
     * Creates a new cached barcode entry.
     *
     * @param entity The barcode entity
     * @param overlayRect The bounding box mapped to overlay coordinates
     */
    public CachedBarcode(BarcodeEntity entity, Rect overlayRect) {
        this.entity = entity;
        this.overlayRect = new Rect(overlayRect);
        this.frameAge = 0;
    }

    public BarcodeEntity getEntity() {
        return entity;
    }

    public Rect getOverlayRect() {
        return overlayRect;
    }

    public int getCenterX() {
        return overlayRect.centerX();
    }

    public int getCenterY() {
        return overlayRect.centerY();
    }

    public String getValue() {
        return entity.getValue();
    }

    public int getSymbology() {
        return entity.getSymbology();
    }

    public int getFrameAge() {
        return frameAge;
    }

    /**
     * Increments the frame age counter.
     * Called once per frame to track how long since this barcode was last updated.
     */
    public void incrementFrameAge() {
        frameAge++;
    }

    /**
     * Resets the frame age to 0.
     * Called when the cached barcode is matched/used again.
     */
    public void resetFrameAge() {
        frameAge = 0;
    }

    /**
     * Updates the cached overlay rect.
     *
     * @param newOverlayRect The new overlay rect
     */
    public void updatePosition(Rect newOverlayRect) {
        this.overlayRect = new Rect(newOverlayRect);
    }

    /**
     * Calculates the Euclidean distance from this cached barcode's center to a rect's center.
     *
     * @param rect The rect to calculate distance to
     * @return The distance in pixels
     */
    public double distanceTo(Rect rect) {
        return Math.sqrt(Math.pow(this.overlayRect.centerX() - rect.centerX(), 2) +
                         Math.pow(this.overlayRect.centerY() - rect.centerY(), 2));
    }

    /**
     * Calculates Intersection over Union (IOU) with another rect.
     *
     * @param other The rect to calculate IOU with
     * @return IOU value between 0.0 and 1.0
     */
    public double calculateIOU(Rect other) {
        int intersectLeft = Math.max(overlayRect.left, other.left);
        int intersectTop = Math.max(overlayRect.top, other.top);
        int intersectRight = Math.min(overlayRect.right, other.right);
        int intersectBottom = Math.min(overlayRect.bottom, other.bottom);

        int intersectWidth = Math.max(0, intersectRight - intersectLeft);
        int intersectHeight = Math.max(0, intersectBottom - intersectTop);
        int intersectionArea = intersectWidth * intersectHeight;

        int area1 = overlayRect.width() * overlayRect.height();
        int area2 = other.width() * other.height();
        int unionArea = area1 + area2 - intersectionArea;

        if (unionArea == 0) return 0.0;
        return (double) intersectionArea / unionArea;
    }
}
