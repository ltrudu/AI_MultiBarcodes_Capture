package com.zebra.ai_multibarcodes_capture.helpers;

import android.graphics.Rect;

import com.zebra.ai.vision.entity.BarcodeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages barcode debouncing logic to stabilize detection results across frames.
 * Extracts debounce-related methods from CameraXLivePreviewActivity.
 */
public class DebounceManager {

    private static final String TAG = Constants.TAG;

    // Algorithm constants
    public static final int ALGORITHM_CENTER_DISTANCE = 0;
    public static final int ALGORITHM_IOU = 1;

    private final List<CachedBarcode> debounceCache = new ArrayList<>();

    // Settings
    private boolean enabled = false;
    private int maxFrames = 10;
    private int distanceThreshold = 50;
    private int algorithm = ALGORITHM_CENTER_DISTANCE;
    private float iouThreshold = 0.3f;

    /**
     * Updates the debounce settings.
     *
     * @param enabled Whether debouncing is enabled
     * @param maxFrames Maximum frames before a cache entry expires
     * @param distanceThreshold Distance threshold for center distance algorithm
     * @param algorithm Algorithm to use (0 = Center Distance, 1 = IOU)
     * @param iouThreshold IOU threshold for IOU algorithm
     */
    public void updateSettings(boolean enabled, int maxFrames, int distanceThreshold,
                               int algorithm, float iouThreshold) {
        this.enabled = enabled;
        this.maxFrames = maxFrames;
        this.distanceThreshold = distanceThreshold;
        this.algorithm = algorithm;
        this.iouThreshold = iouThreshold;
        // Clear cache when settings change
        debounceCache.clear();
        LogUtils.d(TAG, "DebounceManager settings updated - enabled: " + enabled +
                ", maxFrames: " + maxFrames + ", threshold: " + distanceThreshold +
                ", algorithm: " + algorithm + ", iouThreshold: " + iouThreshold);
    }

    /**
     * Updates settings from a CameraSettings object.
     *
     * @param settings The camera settings containing debounce configuration
     */
    public void updateSettings(CameraActivitySettingsLoader.CameraSettings settings) {
        updateSettings(
                settings.isDebounceEnabled,
                settings.debounceMaxFrames,
                settings.debounceThreshold,
                settings.debounceAlgorithm,
                settings.debounceIouThreshold
        );
    }

    /**
     * @return Whether debouncing is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Clears all cached entries.
     */
    public void clearCache() {
        debounceCache.clear();
    }

    /**
     * @return The current cache for iteration
     */
    public List<CachedBarcode> getCache() {
        return debounceCache;
    }

    /**
     * Updates an existing cache entry or adds a new one for the given barcode.
     *
     * @param entity The barcode entity
     * @param overlayRect The bounding box mapped to overlay coordinates
     */
    public void updateOrAddToCache(BarcodeEntity entity, Rect overlayRect) {
        // Look for an existing cache entry using the selected algorithm
        for (CachedBarcode cached : debounceCache) {
            boolean isMatch = isMatch(cached, overlayRect);

            if (isMatch) {
                // Update existing entry
                cached.updatePosition(overlayRect);
                cached.resetFrameAge();
                // Update stability tracking with current value
                cached.updateValue(entity.getValue());
                return;
            }
        }
        // No existing entry found, add new one
        debounceCache.add(new CachedBarcode(entity, overlayRect));
    }

    /**
     * Finds a cached barcode that matches the given bounding box using the selected algorithm.
     * Excludes barcodes that have already been used this frame.
     *
     * @param boundingBox The bounding box to match
     * @param usedCacheEntries List of cache entries already used this frame
     * @return The matching CachedBarcode or null if none found
     */
    public CachedBarcode findCachedMatch(Rect boundingBox, List<CachedBarcode> usedCacheEntries) {
        CachedBarcode bestMatch = null;
        double bestScore = 0;

        for (CachedBarcode cached : debounceCache) {
            // Skip if already used this frame
            if (usedCacheEntries.contains(cached)) {
                continue;
            }

            if (algorithm == ALGORITHM_CENTER_DISTANCE) {
                // Center Distance algorithm
                double distance = cached.distanceTo(boundingBox);
                if (distance <= distanceThreshold) {
                    double score = 1.0 / (1.0 + distance); // Higher score for closer
                    if (score > bestScore) {
                        bestMatch = cached;
                        bestScore = score;
                    }
                }
            } else {
                // IOU algorithm
                double iou = cached.calculateIOU(boundingBox);
                if (iou >= iouThreshold && iou > bestScore) {
                    bestMatch = cached;
                    bestScore = iou;
                }
            }
        }

        return bestMatch;
    }

    /**
     * Increments the age of all cache entries and removes expired ones.
     */
    public void incrementAndPruneCacheAge() {
        List<CachedBarcode> toRemove = new ArrayList<>();

        for (CachedBarcode cached : debounceCache) {
            cached.incrementFrameAge();
            if (cached.getFrameAge() > maxFrames) {
                toRemove.add(cached);
            }
        }

        debounceCache.removeAll(toRemove);
        if (toRemove.size() > 0) {
            LogUtils.v(TAG, "Removed " + toRemove.size() + " expired cache entries");
        }
    }

    /**
     * Checks if a cached barcode matches a given bounding box using the current algorithm.
     *
     * @param cached The cached barcode
     * @param boundingBox The bounding box to check
     * @return true if they match according to the current algorithm
     */
    private boolean isMatch(CachedBarcode cached, Rect boundingBox) {
        if (algorithm == ALGORITHM_CENTER_DISTANCE) {
            return cached.distanceTo(boundingBox) <= distanceThreshold;
        } else {
            return cached.calculateIOU(boundingBox) >= iouThreshold;
        }
    }
}
