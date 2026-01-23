package com.zebra.ai_multibarcodes_capture.conditions;

/**
 * Interface representing a generic condition (for AutoCapture or Filtering).
 * Both AutoCaptureCondition and FilteringCondition implement this interface.
 */
public interface ICondition {
    /**
     * @return The unique identifier for this condition
     */
    String getId();

    /**
     * @return The description for this condition, or null if not set
     */
    String getDescription();

    /**
     * Sets the description for this condition.
     * @param description The description to set
     */
    void setDescription(String description);

    /**
     * @return true if this condition is valid
     */
    boolean isValid();
}
