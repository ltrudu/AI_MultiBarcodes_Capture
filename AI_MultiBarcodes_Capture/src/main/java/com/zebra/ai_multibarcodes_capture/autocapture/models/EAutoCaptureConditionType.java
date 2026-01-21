package com.zebra.ai_multibarcodes_capture.autocapture.models;

/**
 * Enum representing the types of auto capture conditions.
 */
public enum EAutoCaptureConditionType {
    /**
     * Condition based on the number of barcodes detected.
     * Only one condition of this type is allowed per configuration.
     */
    NUMBER_OF_BARCODES("number_of_barcodes"),

    /**
     * Condition based on whether barcodes match a regex pattern.
     * Multiple conditions of this type are allowed.
     */
    CONTAINS_REGEX("contains_regex"),

    /**
     * Condition based on whether barcodes match a specified symbology.
     * Multiple conditions of this type are allowed.
     */
    SYMBOLOGY("symbology"),

    /**
     * Complex condition combining number of barcodes, symbology, and regex matching.
     * Triggers when the specified number of barcodes match both the symbology AND regex.
     * Multiple conditions of this type are allowed.
     */
    COMPLEX("complex");

    private final String key;

    EAutoCaptureConditionType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static EAutoCaptureConditionType fromKey(String key) {
        for (EAutoCaptureConditionType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return NUMBER_OF_BARCODES; // Default
    }
}
