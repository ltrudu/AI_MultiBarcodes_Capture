package com.zebra.ai_multibarcodes_capture.filtering;

import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringCondition;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;
import com.zebra.ai_multibarcodes_capture.filtering.models.EFilteringConditionType;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai.vision.entity.BarcodeEntity;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.TAG;

/**
 * Evaluates filtering conditions against a barcode entity using OR logic.
 * An entity passes the filter if it matches ANY of the configured conditions.
 * If no conditions are defined, all entities pass (include all).
 */
public class FilteringEvaluator {

    /**
     * Determines if an entity should be included based on the filtering conditions.
     * Uses OR logic: entity matches if it satisfies ANY condition.
     * Returns true if no conditions are defined (include all entities).
     *
     * @param entity The barcode entity to evaluate
     * @param conditionList The list of filtering conditions
     * @return true if the entity should be included, false otherwise
     */
    public static boolean shouldIncludeEntity(BarcodeEntity entity, FilteringConditionList conditionList) {
        if (conditionList == null || conditionList.isEmpty()) {
            // No conditions defined = include all entities
            return true;
        }

        if (entity == null) {
            return false;
        }

        // OR logic: return true if entity matches ANY condition
        for (FilteringCondition condition : conditionList.getConditions()) {
            if (evaluateCondition(entity, condition)) {
                LogUtils.v(TAG, "FilteringEvaluator: Entity matches condition, including");
                return true;
            }
        }

        LogUtils.v(TAG, "FilteringEvaluator: Entity does not match any condition, excluding");
        return false;
    }

    /**
     * Evaluates a single condition against a barcode entity.
     *
     * @param entity The barcode entity to evaluate
     * @param condition The condition to check
     * @return true if the entity matches the condition, false otherwise
     */
    private static boolean evaluateCondition(BarcodeEntity entity, FilteringCondition condition) {
        if (condition == null || !condition.isValid()) {
            return false;
        }

        switch (condition.getType()) {
            case CONTAINS_REGEX:
                return evaluateRegexCondition(entity, condition.getRegex());
            case SYMBOLOGY:
                return evaluateSymbologyCondition(entity, condition.getSymbology());
            case COMPLEX:
                return evaluateComplexCondition(entity, condition.getSymbology(), condition.getRegex());
            default:
                return false;
        }
    }

    /**
     * Evaluates if the entity's value matches the regex pattern.
     *
     * @param entity The barcode entity
     * @param regex The regex pattern to match
     * @return true if the value matches the regex
     */
    private static boolean evaluateRegexCondition(BarcodeEntity entity, String regex) {
        if (regex == null || regex.isEmpty()) {
            return false;
        }

        String value = entity.getValue();
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            boolean matches = pattern.matcher(value).matches();
            LogUtils.v(TAG, "FilteringEvaluator: REGEX check - value: '" + value + "', pattern: '" + regex + "', matches: " + matches);
            return matches;
        } catch (PatternSyntaxException e) {
            LogUtils.e(TAG, "FilteringEvaluator: Invalid regex pattern: " + regex, e);
            return false;
        }
    }

    /**
     * Evaluates if the entity's symbology matches the specified symbology.
     *
     * @param entity The barcode entity
     * @param symbology The symbology value to match
     * @return true if the symbology matches
     */
    private static boolean evaluateSymbologyCondition(BarcodeEntity entity, int symbology) {
        boolean matches = entity.getSymbology() == symbology;
        LogUtils.v(TAG, "FilteringEvaluator: SYMBOLOGY check - entity symbology: " + entity.getSymbology() + ", required: " + symbology + ", matches: " + matches);
        return matches;
    }

    /**
     * Evaluates if the entity matches both symbology AND regex criteria.
     *
     * @param entity The barcode entity
     * @param symbology The symbology value to match
     * @param regex The regex pattern to match
     * @return true if both symbology and regex match
     */
    private static boolean evaluateComplexCondition(BarcodeEntity entity, int symbology, String regex) {
        // Both conditions must be true
        boolean symbologyMatch = evaluateSymbologyCondition(entity, symbology);
        if (!symbologyMatch) {
            return false;
        }

        boolean regexMatch = evaluateRegexCondition(entity, regex);
        LogUtils.v(TAG, "FilteringEvaluator: COMPLEX check - symbology matches: " + symbologyMatch + ", regex matches: " + regexMatch);
        return regexMatch;
    }
}
