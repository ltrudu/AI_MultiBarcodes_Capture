package com.zebra.ai_multibarcodes_capture.helpers;

import android.graphics.Rect;

import com.zebra.ai.vision.entity.BarcodeEntity;

import java.util.Objects;

/**
 * Represents a cached barcode entry for debouncing purposes.
 * Stores the barcode entity, overlay rect, frame age, and stability tracking data.
 */
public class CachedBarcode {
    private BarcodeEntity entity;
    private Rect overlayRect;
    private int frameAge;

    // Stability tracking fields for high-res stabilization
    private String lastValue;           // Previous frame's value
    private int consistentValueCount;   // How many frames with same value
    private int valueChangeCount;       // How many times value changed
    private boolean needsHighResValidation;

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

        // Initialize stability tracking
        this.lastValue = entity.getValue();
        this.consistentValueCount = (lastValue != null && !lastValue.isEmpty()) ? 1 : 0;
        this.valueChangeCount = 0;
        this.needsHighResValidation = (lastValue == null || lastValue.isEmpty());
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

    // ==================== Stability Tracking Methods ====================

    /**
     * Updates the value tracking and returns whether the value changed.
     *
     * @param newValue The new barcode value detected this frame
     * @return true if the value changed from the previous frame
     */
    public boolean updateValue(String newValue) {
        boolean valueChanged = !Objects.equals(lastValue, newValue);
        if (valueChanged) {
            valueChangeCount++;
            consistentValueCount = 1;
        } else {
            consistentValueCount++;
        }
        lastValue = newValue;

        // Mark as needing validation if value is empty or unstable
        if (newValue == null || newValue.isEmpty()) {
            needsHighResValidation = true;
        }

        return valueChanged;
    }

    /**
     * Gets the stability score for this cached barcode.
     * Score of 1.0 means perfectly stable, 0.0 means very unstable.
     *
     * @return Stability score between 0.0 and 1.0
     */
    public float getStabilityScore() {
        int total = consistentValueCount + valueChangeCount;
        return total == 0 ? 0f : (float) consistentValueCount / total;
    }

    /**
     * Checks if this cached barcode has a decoded value.
     *
     * @return true if the entity has a non-empty value
     */
    public boolean hasDecodedValue() {
        return entity.getValue() != null && !entity.getValue().isEmpty();
    }

    /**
     * Sets a validated value from high-res capture and boosts stability.
     *
     * @param validatedValue The validated barcode value from high-res capture
     */
    public void setValidatedValue(String validatedValue) {
        if (validatedValue != null && !validatedValue.isEmpty()) {
            this.lastValue = validatedValue;
            this.consistentValueCount = 5;  // Boost stability
            this.valueChangeCount = 0;
            this.needsHighResValidation = false;
        }
    }

    /**
     * Checks if this barcode needs high-res validation.
     *
     * @return true if validation is needed
     */
    public boolean needsHighResValidation() {
        return needsHighResValidation;
    }

    /**
     * Sets whether this barcode needs high-res validation.
     *
     * @param needsValidation true if validation is needed
     */
    public void setNeedsHighResValidation(boolean needsValidation) {
        this.needsHighResValidation = needsValidation;
    }

    /**
     * Gets the last tracked value.
     *
     * @return The last value seen for this barcode
     */
    public String getLastValue() {
        return lastValue;
    }

    /**
     * Gets the count of consecutive frames with the same value.
     *
     * @return The consistent value count
     */
    public int getConsistentValueCount() {
        return consistentValueCount;
    }

    /**
     * Gets the count of value changes.
     *
     * @return The value change count
     */
    public int getValueChangeCount() {
        return valueChangeCount;
    }
}
