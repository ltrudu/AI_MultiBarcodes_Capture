package com.zebra.ai_multibarcodes_capture.autocapture;

import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureCondition;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.autocapture.models.EAutoCaptureConditionType;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai.vision.entity.BarcodeEntity;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.TAG;

/**
 * Evaluates auto capture conditions against a list of detected barcodes.
 */
public class AutoCaptureEvaluator {

    /**
     * Evaluates all conditions against the detected entities.
     * All conditions must be TRUE for auto capture to trigger.
     *
     * @param conditionList The list of conditions to evaluate
     * @param entities The list of detected entities
     * @return true if all conditions are met, false otherwise
     */
    public static boolean evaluateConditions(AutoCaptureConditionList conditionList, List<BarcodeEntity> entities) {
        if (conditionList == null || conditionList.isEmpty()) {
            return false;
        }

        if (entities == null || entities.isEmpty()) {
            return false;
        }

        // All conditions must be met
        for (AutoCaptureCondition condition : conditionList.getConditions()) {
            if (!evaluateCondition(condition, entities)) {
                return false;
            }
        }

        LogUtils.d(TAG, "AutoCaptureEvaluator: All conditions met, triggering auto capture");
        return true;
    }

    /**
     * Evaluates a single condition against the detected entities.
     *
     * @param condition The condition to evaluate
     * @param entities The list of detected entities
     * @return true if the condition is met, false otherwise
     */
    private static boolean evaluateCondition(AutoCaptureCondition condition, List<BarcodeEntity> entities) {
        if (condition == null || !condition.isValid()) {
            return false;
        }

        if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
            // Check exact number of barcodes
            boolean result = entities.size() == condition.getCount();
            LogUtils.d(TAG, "AutoCaptureEvaluator: NUMBER_OF_BARCODES condition - required: " +
                    condition.getCount() + ", actual: " + entities.size() + ", result: " + result);
            return result;
        } else if (condition.getType() == EAutoCaptureConditionType.CONTAINS_REGEX) {
            // Count matching barcodes
            int matchingCount = countMatchingBarcodes(condition.getRegex(), entities);
            boolean result = matchingCount >= condition.getCount();
            LogUtils.d(TAG, "AutoCaptureEvaluator: CONTAINS_REGEX condition - required: " +
                    condition.getCount() + "+, matching: " + matchingCount + ", result: " + result);
            return result;
        } else if (condition.getType() == EAutoCaptureConditionType.SYMBOLOGY) {
            // Count barcodes matching the symbology
            int matchingCount = countMatchingSymbology(condition.getSymbology(), entities);
            boolean result = matchingCount >= condition.getCount();
            LogUtils.d(TAG, "AutoCaptureEvaluator: SYMBOLOGY condition - required: " +
                    condition.getCount() + "+, symbology: " + condition.getSymbology() +
                    ", matching: " + matchingCount + ", result: " + result);
            return result;
        } else if (condition.getType() == EAutoCaptureConditionType.COMPLEX) {
            // Count barcodes matching BOTH symbology AND regex
            int matchingCount = countMatchingComplex(condition.getSymbology(), condition.getRegex(), entities);
            boolean result = matchingCount >= condition.getCount();
            LogUtils.d(TAG, "AutoCaptureEvaluator: COMPLEX condition - required: " +
                    condition.getCount() + "+, symbology: " + condition.getSymbology() +
                    ", regex: " + condition.getRegex() +
                    ", matching: " + matchingCount + ", result: " + result);
            return result;
        }

        return false;
    }

    /**
     * Counts how many barcodes match the given regex pattern.
     *
     * @param regex The regex pattern to match
     * @param entities The list of detected entities
     * @return The count of matching barcodes
     */
    private static int countMatchingBarcodes(String regex, List<BarcodeEntity> entities) {
        if (regex == null || regex.isEmpty()) {
            return 0;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            LogUtils.e(TAG, "AutoCaptureEvaluator: Invalid regex pattern: " + regex, e);
            return 0;
        }

        int count = 0;
        for (BarcodeEntity entity : entities) {
            String barcodeData = entity.getValue();
            if (barcodeData != null && pattern.matcher(barcodeData).matches()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts how many barcodes match the given symbology.
     *
     * @param symbology The symbology value to match
     * @param entities The list of detected entities
     * @return The count of matching barcodes
     */
    private static int countMatchingSymbology(int symbology, List<BarcodeEntity> entities) {
        int count = 0;
        for (BarcodeEntity entity : entities) {
            if (entity.getSymbology() == symbology) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts how many barcodes match both the given symbology AND regex pattern.
     *
     * @param symbology The symbology value to match
     * @param regex The regex pattern to match
     * @param entities The list of detected entities
     * @return The count of matching barcodes
     */
    private static int countMatchingComplex(int symbology, String regex, List<BarcodeEntity> entities) {
        if (regex == null || regex.isEmpty()) {
            return 0;
        }

        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            LogUtils.e(TAG, "AutoCaptureEvaluator: Invalid regex pattern: " + regex, e);
            return 0;
        }

        int count = 0;
        for (BarcodeEntity entity : entities) {
            // Check both symbology AND regex match
            if (entity.getSymbology() == symbology) {
                String barcodeData = entity.getValue();
                if (barcodeData != null && pattern.matcher(barcodeData).matches()) {
                    count++;
                }
            }
        }

        return count;
    }
}
