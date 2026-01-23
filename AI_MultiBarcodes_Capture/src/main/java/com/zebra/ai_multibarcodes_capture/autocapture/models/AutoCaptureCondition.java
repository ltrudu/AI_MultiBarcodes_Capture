package com.zebra.ai_multibarcodes_capture.autocapture.models;

import com.zebra.ai_multibarcodes_capture.conditions.ICondition;

import java.util.UUID;

/**
 * Data class representing an auto capture condition.
 */
public class AutoCaptureCondition implements ICondition {
    private String id;
    private EAutoCaptureConditionType type;
    private int count;
    private String regex;
    private int symbology = -1;
    private String description;

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

    /**
     * Constructor for SYMBOLOGY condition type.
     *
     * @param count The minimum number of barcodes that must match the symbology
     * @param symbology The symbology value to match (from EBarcodesSymbologies.getIntValue())
     */
    public AutoCaptureCondition(int count, int symbology) {
        this.id = UUID.randomUUID().toString();
        this.type = EAutoCaptureConditionType.SYMBOLOGY;
        this.count = count;
        this.symbology = symbology;
    }

    /**
     * Constructor for COMPLEX condition type.
     * Matches barcodes that satisfy both symbology AND regex criteria.
     *
     * @param count The minimum number of barcodes that must match both criteria
     * @param symbology The symbology value to match (from EBarcodesSymbologies.getIntValue())
     * @param regex The regex pattern to match
     */
    public AutoCaptureCondition(int count, int symbology, String regex) {
        this.id = UUID.randomUUID().toString();
        this.type = EAutoCaptureConditionType.COMPLEX;
        this.count = count;
        this.symbology = symbology;
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

    public int getSymbology() {
        return symbology;
    }

    public void setSymbology(int symbology) {
        this.symbology = symbology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

        if (type == EAutoCaptureConditionType.SYMBOLOGY) {
            // Symbology must be a valid value (>= 0)
            if (symbology < 0) {
                return false;
            }
        }

        if (type == EAutoCaptureConditionType.COMPLEX) {
            // Symbology must be a valid value (>= 0)
            if (symbology < 0) {
                return false;
            }
            // Regex must be valid
            if (regex == null || regex.isEmpty()) {
                return false;
            }
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
