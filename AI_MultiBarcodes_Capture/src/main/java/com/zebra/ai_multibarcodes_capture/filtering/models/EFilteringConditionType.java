package com.zebra.ai_multibarcodes_capture.filtering.models;

/**
 * Enum representing the types of filtering conditions.
 */
public enum EFilteringConditionType {
    /**
     * Condition based on whether barcodes match a regex pattern.
     */
    CONTAINS_REGEX("contains_regex"),

    /**
     * Condition based on whether barcodes match a specified symbology.
     */
    SYMBOLOGY("symbology"),

    /**
     * Complex condition combining symbology and regex matching.
     * Matches when a barcode matches BOTH the symbology AND regex.
     */
    COMPLEX("complex");

    private final String key;

    EFilteringConditionType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static EFilteringConditionType fromKey(String key) {
        for (EFilteringConditionType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        return CONTAINS_REGEX; // Default
    }
}
