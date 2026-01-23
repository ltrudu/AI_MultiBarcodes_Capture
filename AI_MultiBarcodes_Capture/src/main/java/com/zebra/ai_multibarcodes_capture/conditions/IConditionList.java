package com.zebra.ai_multibarcodes_capture.conditions;

import java.util.List;

/**
 * Interface representing a generic condition list (for AutoCapture or Filtering).
 * Both AutoCaptureConditionList and FilteringConditionList implement this interface.
 *
 * @param <T> The condition type
 */
public interface IConditionList<T extends ICondition> {
    /**
     * @return The list of conditions
     */
    List<T> getConditions();

    /**
     * Adds a condition to the list.
     * @param condition The condition to add
     * @return true if added successfully
     */
    boolean addCondition(T condition);

    /**
     * Updates an existing condition.
     * @param condition The condition to update (matched by ID)
     * @return true if updated successfully
     */
    boolean updateCondition(T condition);

    /**
     * Removes a condition by ID.
     * @param conditionId The ID of the condition to remove
     * @return true if removed
     */
    boolean removeCondition(String conditionId);

    /**
     * @return The number of conditions
     */
    int size();

    /**
     * @return true if no conditions exist
     */
    boolean isEmpty();

    /**
     * Clears all conditions.
     */
    void clear();
}
