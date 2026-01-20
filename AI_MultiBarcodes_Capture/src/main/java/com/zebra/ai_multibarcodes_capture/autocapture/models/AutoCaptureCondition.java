package com.zebra.ai_multibarcodes_capture.autocapture.models;

import java.util.UUID;

/**
 * Data class representing an auto capture condition.
 */
public class AutoCaptureCondition {
    private String id;
    private EAutoCaptureConditionType type;
    private int count;
    private String regex;

    /**
     * Default constructor for Gson deserialization.
     */
    public AutoCaptureCondition() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Constructor for NUMBER_OF_BARCODES condition type.
     *
     * @param count The exact number of barcodes required
     */
    public AutoCaptureCondition(int count) {
        this.id = UUID.randomUUID().toString();
        this.type = EAutoCaptureConditionType.NUMBER_OF_BARCODES;
        this.count = count;
        this.regex = null;
    }

    /**
     * Constructor for CONTAINS_REGEX condition type.
     *
     * @param count The minimum number of barcodes that must match the regex
     * @param regex The regex pattern to match
     */
    public AutoCaptureCondition(int count, String regex) {
        this.id = UUID.randomUUID().toString();
        this.type = EAutoCaptureConditionType.CONTAINS_REGEX;
        this.count = count;
        this.regex = regex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EAutoCaptureConditionType getType() {
        return type;
    }

    public void setType(EAutoCaptureConditionType type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * Validates this condition.
     *
     * @return true if the condition is valid, false otherwise
     */
    public boolean isValid() {
        if (type == null) {
            return false;
        }

        if (count < 1) {
            return false;
        }

        if (type == EAutoCaptureConditionType.CONTAINS_REGEX) {
            if (regex == null || regex.isEmpty()) {
                return false;
            }
            // Validate regex pattern
            try {
                java.util.regex.Pattern.compile(regex);
            } catch (java.util.regex.PatternSyntaxException e) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoCaptureCondition that = (AutoCaptureCondition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
