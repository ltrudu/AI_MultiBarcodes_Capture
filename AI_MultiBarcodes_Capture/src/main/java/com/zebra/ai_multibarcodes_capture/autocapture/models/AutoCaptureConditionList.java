package com.zebra.ai_multibarcodes_capture.autocapture.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class for managing a list of auto capture conditions with CRUD operations and validation.
 */
public class AutoCaptureConditionList {
    private List<AutoCaptureCondition> conditions;

    public AutoCaptureConditionList() {
        this.conditions = new ArrayList<>();
    }

    public List<AutoCaptureCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<AutoCaptureCondition> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    /**
     * Adds a condition to the list.
     *
     * @param condition The condition to add
     * @return true if added successfully, false if validation failed
     */
    public boolean addCondition(AutoCaptureCondition condition) {
        if (condition == null || !condition.isValid()) {
            return false;
        }

        // Check if NUMBER_OF_BARCODES already exists (only one allowed)
        if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
            if (hasNumberOfBarcodesCondition()) {
                return false;
            }
        }

        conditions.add(condition);
        return true;
    }

    /**
     * Updates an existing condition.
     *
     * @param condition The condition to update (matched by ID)
     * @return true if updated successfully, false otherwise
     */
    public boolean updateCondition(AutoCaptureCondition condition) {
        if (condition == null || !condition.isValid()) {
            return false;
        }

        for (int i = 0; i < conditions.size(); i++) {
            if (conditions.get(i).getId().equals(condition.getId())) {
                conditions.set(i, condition);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a condition by ID.
     *
     * @param conditionId The ID of the condition to remove
     * @return true if removed, false if not found
     */
    public boolean removeCondition(String conditionId) {
        Iterator<AutoCaptureCondition> iterator = conditions.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId().equals(conditionId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a condition by ID.
     *
     * @param conditionId The ID of the condition to find
     * @return The condition or null if not found
     */
    public AutoCaptureCondition getConditionById(String conditionId) {
        for (AutoCaptureCondition condition : conditions) {
            if (condition.getId().equals(conditionId)) {
                return condition;
            }
        }
        return null;
    }

    /**
     * Checks if a NUMBER_OF_BARCODES condition already exists.
     *
     * @return true if such a condition exists
     */
    public boolean hasNumberOfBarcodesCondition() {
        for (AutoCaptureCondition condition : conditions) {
            if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all CONTAINS_REGEX conditions.
     *
     * @return List of regex conditions
     */
    public List<AutoCaptureCondition> getRegexConditions() {
        List<AutoCaptureCondition> regexConditions = new ArrayList<>();
        for (AutoCaptureCondition condition : conditions) {
            if (condition.getType() == EAutoCaptureConditionType.CONTAINS_REGEX) {
                regexConditions.add(condition);
            }
        }
        return regexConditions;
    }

    /**
     * Gets the NUMBER_OF_BARCODES condition if it exists.
     *
     * @return The condition or null if not found
     */
    public AutoCaptureCondition getNumberOfBarcodesCondition() {
        for (AutoCaptureCondition condition : conditions) {
            if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
                return condition;
            }
        }
        return null;
    }

    /**
     * Checks if all conditions are valid.
     *
     * @return true if all conditions are valid
     */
    public boolean isValid() {
        for (AutoCaptureCondition condition : conditions) {
            if (!condition.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the total number of conditions.
     *
     * @return The number of conditions
     */
    public int size() {
        return conditions.size();
    }

    /**
     * Checks if the list is empty.
     *
     * @return true if no conditions exist
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }

    /**
     * Clears all conditions.
     */
    public void clear() {
        conditions.clear();
    }
}
