package com.zebra.ai_multibarcodes_capture.filtering.models;

import com.zebra.ai_multibarcodes_capture.conditions.ICondition;

import java.util.UUID;

/**
 * Data class representing a filtering condition.
 */
public class FilteringCondition implements ICondition {
    private String id;
    private EFilteringConditionType type;
    private String regex;
    private int symbology = -1;
    private String description;

    /**
     * Default constructor for Gson deserialization.
     */
    public FilteringCondition() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Constructor for CONTAINS_REGEX condition type.
     *
     * @param regex The regex pattern to match
     */
    public FilteringCondition(String regex) {
        this.id = UUID.randomUUID().toString();
        this.type = EFilteringConditionType.CONTAINS_REGEX;
        this.regex = regex;
    }

    /**
     * Constructor for SYMBOLOGY condition type.
     *
     * @param symbology The symbology value to match (from EBarcodesSymbologies.getIntValue())
     */
    public FilteringCondition(int symbology) {
        this.id = UUID.randomUUID().toString();
        this.type = EFilteringConditionType.SYMBOLOGY;
        this.symbology = symbology;
    }

    /**
     * Constructor for COMPLEX condition type.
     * Matches barcodes that satisfy both symbology AND regex criteria.
     *
     * @param symbology The symbology value to match (from EBarcodesSymbologies.getIntValue())
     * @param regex The regex pattern to match
     */
    public FilteringCondition(int symbology, String regex) {
        this.id = UUID.randomUUID().toString();
        this.type = EFilteringConditionType.COMPLEX;
        this.symbology = symbology;
        this.regex = regex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EFilteringConditionType getType() {
        return type;
    }

    public void setType(EFilteringConditionType type) {
        this.type = type;
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

        if (type == EFilteringConditionType.CONTAINS_REGEX) {
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

        if (type == EFilteringConditionType.SYMBOLOGY) {
            // Symbology must be a valid value (>= 0)
            if (symbology < 0) {
                return false;
            }
        }

        if (type == EFilteringConditionType.COMPLEX) {
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
        FilteringCondition that = (FilteringCondition) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
