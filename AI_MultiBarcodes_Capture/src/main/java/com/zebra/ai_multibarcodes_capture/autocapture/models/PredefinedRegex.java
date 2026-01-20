package com.zebra.ai_multibarcodes_capture.autocapture.models;

/**
 * Data class for predefined regex patterns.
 */
public class PredefinedRegex {
    private String category;
    private String description;
    private String pattern;

    public PredefinedRegex(String category, String description, String pattern) {
        this.category = category;
        this.description = description;
        this.pattern = pattern;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns a searchable string combining category and description.
     *
     * @return Searchable string
     */
    public String getSearchableText() {
        return (category + " " + description).toLowerCase();
    }

    @Override
    public String toString() {
        return description + " (" + category + ")";
    }
}
