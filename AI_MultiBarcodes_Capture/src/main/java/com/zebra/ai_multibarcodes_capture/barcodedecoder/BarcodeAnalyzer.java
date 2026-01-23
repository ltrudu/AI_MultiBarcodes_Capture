// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.barcodedecoder;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.util.Log;

import com.zebra.ai.vision.detector.AIVisionSDKException;
import com.zebra.ai.vision.detector.BarcodeDecoder;
import com.zebra.ai.vision.detector.ImageData;
import com.zebra.ai.vision.entity.BarcodeEntity;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

/**
 * The BarcodeAnalyzer class implements the ImageAnalysis.Analyzer interface and is
 * responsible for analyzing image frames to detect barcodes. It utilizes a BarcodeDecoder
 * to process the image data and execute the detection logic asynchronously.
 *
 * This class is designed to be used within the context of an Android application, typically
 * as part of a camera-based barcode scanning solution. It provides a callback interface to
 * return detection results to the caller.
 *
 * Usage:
 * - Instantiate the BarcodeAnalyzer with a DetectionCallback and a BarcodeDecoder.
 * - Implement the DetectionCallback interface to handle detection results.
 * - The analyze(ImageProxy) method is called by the camera framework to process image frames.
 * - Call stopAnalyzing() to stop the analysis process and release resources.
 *
 * Dependencies:
 * - Android ImageProxy: Provides access to image data from the camera.
 * - ExecutorService: Used for asynchronous task execution.
 * - BarcodeDecoder: Handles the decoding of barcode data from images.
 *
 * Concurrency:
 * - Uses a single-threaded executor to ensure that image analysis tasks are processed sequentially.
 * - Manages concurrency with volatile flags to control analysis state and termination.
 *
 * Note: Ensure that the appropriate permissions and dependencies are configured
 * in the AndroidManifest and build files to utilize camera and image processing capabilities.
 */
public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    /**
     * Interface for handling the results of the barcode detection process.
     * Implement this interface to define how detection results are processed.
     */
    public interface DetectionCallback {
        void onDetectionResult(List<BarcodeEntity> list);
    }

    /**
     * Interface for receiving analysis timing data.
     * Implement this interface to display performance metrics (analysis count, analysis time).
     */
    public interface AnalysisTimingCallback {
        /**
         * Called with timing information after each analysis completes.
         * @param analysisTimeMs The time in milliseconds taken to analyze the frame
         * @param analysisPerSecond The number of analyses completed in the last second
         */
        void onAnalysisTiming(long analysisTimeMs, int analysisPerSecond);
    }

    private static final String TAG = "BarcodeAnalyzer";
    private final DetectionCallback callback;
    private final BarcodeDecoder barcodeDecoder;
    private final ExecutorService executorService;
    private volatile boolean isAnalyzing = true;
    private volatile boolean isStopped = false;

    // Crop region support for capture zone optimization
    @Nullable
    private volatile Rect cropRegion = null;
    private volatile int cropOffsetX = 0;
    private volatile int cropOffsetY = 0;
    // Store the image dimensions for proper offset transformation
    private volatile int sourceImageWidth = 0;
    private volatile int sourceImageHeight = 0;
    // Store the last image rotation degrees for coordinate transformation
    private volatile int lastImageRotationDegrees = 0;

    // Timing tracking for performance monitoring
    @Nullable
    private volatile AnalysisTimingCallback timingCallback = null;
    private volatile boolean timingEnabled = false;
    private volatile long apsWindowStartTime = 0;
    private volatile int frameCountInWindow = 0;
    private volatile int currentAnalysisPerSecond = 0;
    private static final long APS_WINDOW_MS = 1000; // Calculate APS over 1 second window

    // Average analysis time calculation
    private static final int AVG_WINDOW_SIZE = 20; // Calculate average over 20 analyses
    private final long[] analysisTimesBuffer = new long[AVG_WINDOW_SIZE];
    private volatile int analysisTimesIndex = 0;
    private volatile int analysisTimesCount = 0;
    private volatile long averageAnalysisTimeMs = 0;

    /**
     * Constructs a new BarcodeAnalyzer with the specified callback and barcode decoder.
     *
     * @param callback The callback for handling detection results.
     * @param barcodeDecoder The barcode decoder used to process image data.
     */
    public BarcodeAnalyzer(DetectionCallback callback, BarcodeDecoder barcodeDecoder) {
        this.callback = callback;
        this.barcodeDecoder = barcodeDecoder;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Analyzes the given image to detect barcodes. This method is called by the camera
     * framework to process image frames asynchronously.
     *
     * When a crop region is set, only the cropped portion of the image is sent to the decoder,
     * reducing computational load. The returned bounding boxes are then adjusted to account
     * for the crop offset.
     *
     * @param image The image frame to analyze.
     */
    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (!isAnalyzing || isStopped) {
            image.close();
            return;
        }

        isAnalyzing = false; // Prevent re-entry

        // Capture current crop region (volatile read)
        final Rect currentCropRegion = cropRegion;
        final int currentOffsetX = cropOffsetX;
        final int currentOffsetY = cropOffsetY;

        // Capture timing state for this analysis
        final boolean trackTiming = timingEnabled && timingCallback != null;
        //long startTimeNanos = 0;

        Future<?> future = executorService.submit(() -> {
            try {
                LogUtils.d(TAG, "Starting image analysis" + (currentCropRegion != null ? " with crop region" : ""));

                // Get the rotation from the ImageProxy
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                LogUtils.d(TAG, "Image rotation degrees: " + rotationDegrees);

                // Store the rotation for the activity to use when transforming bounding boxes
                lastImageRotationDegrees = rotationDegrees;

                // Determine which image data to process
                ImageData imageData;
                if (currentCropRegion != null) {
                    // Crop the image before processing - returns grayscale bitmap directly
                    Bitmap croppedBitmap = cropImageProxy(image, currentCropRegion);
                    if (croppedBitmap != null) {
                        // When cropping, we pass rotation=0 because:
                        // 1. We crop in raw image space (before rotation)
                        // 2. We want bounding boxes in raw image space (so we can add raw crop offset)
                        // 3. The activity uses lastImageRotationDegrees to transform to effective space
                        imageData = ImageData.fromBitmap(croppedBitmap, 0);
                        LogUtils.d(TAG, "Processing grayscale cropped image: " + croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight() + " (rotation=" + rotationDegrees + " stored for activity)");
                    } else {
                        // Fallback to full image if cropping fails
                        LogUtils.w(TAG, "Cropping failed, falling back to full image");
                        imageData = ImageData.fromImageProxy(image);
                    }
                } else {
                    // Process full image - SDK handles rotation internally
                    imageData = ImageData.fromImageProxy(image);
                }

                final long startTimeNanos = trackTiming ? System.nanoTime() : 0;
                barcodeDecoder.process(imageData)
                        .thenAccept(result -> {
                            // Calculate timing when entering thenAccept (before isStopped check)
                            if (trackTiming) {
                                long endTimeNanos = System.nanoTime();
                                long analysisTimeMs = (endTimeNanos - startTimeNanos) / 1_000_000;

                                // Update rolling average for analysis time (over 20 samples)
                                analysisTimesBuffer[analysisTimesIndex] = analysisTimeMs;
                                analysisTimesIndex = (analysisTimesIndex + 1) % AVG_WINDOW_SIZE;
                                if (analysisTimesCount < AVG_WINDOW_SIZE) {
                                    analysisTimesCount++;
                                }
                                // Calculate average
                                long sum = 0;
                                for (int i = 0; i < analysisTimesCount; i++) {
                                    sum += analysisTimesBuffer[i];
                                }
                                averageAnalysisTimeMs = sum / analysisTimesCount;

                                // Update analysis per second calculation
                                long currentTimeMs = System.currentTimeMillis();
                                frameCountInWindow++;

                                if (apsWindowStartTime == 0) {
                                    apsWindowStartTime = currentTimeMs;
                                } else {
                                    long elapsedMs = currentTimeMs - apsWindowStartTime;
                                    if (elapsedMs >= APS_WINDOW_MS) {
                                        // Store count and reset window
                                        currentAnalysisPerSecond = frameCountInWindow;
                                        apsWindowStartTime = currentTimeMs;
                                        frameCountInWindow = 0;
                                    }
                                }

                                // Notify callback with average timing data
                                if (timingCallback != null) {
                                    timingCallback.onAnalysisTiming(averageAnalysisTimeMs, currentAnalysisPerSecond);
                                }
                            }

                            if (!isStopped) {
                                // Adjust bounding boxes if we used a crop region
                                List<BarcodeEntity> adjustedResult = result;
                                if (currentCropRegion != null && result != null) {
                                    adjustedResult = adjustBoundingBoxesForCrop(result, currentOffsetX, currentOffsetY);
                                }
                                callback.onDetectionResult(adjustedResult);
                            }
                            image.close();
                            isAnalyzing = true;
                        })
                        .exceptionally(ex -> {
                            LogUtils.e(TAG, "Error in completable future result " + ex.getMessage());
                            image.close();
                            isAnalyzing = true;
                            return null;
                        });
            } catch (AIVisionSDKException e) {
                LogUtils.e(TAG, Objects.requireNonNull(e.getMessage()));
                image.close();
                isAnalyzing = true;
            } catch (Exception e) {
                LogUtils.e(TAG, "Unexpected error during analysis: " + e.getMessage());
                image.close();
                isAnalyzing = true;
            }
        });

        // Cancel the task if the analyzer is stopped
        if (isStopped) {
            future.cancel(true);
        }
    }

    /**
     * Crops the ImageProxy to the specified region and returns a Bitmap.
     * Uses native NDK implementation for maximum performance when available,
     * falls back to Java implementation otherwise.
     *
     * @param image The source ImageProxy from CameraX
     * @param cropRect The region to crop in image coordinates
     * @return A cropped Bitmap, or null if cropping fails
     */
    @Nullable
    private Bitmap cropImageProxy(@NonNull ImageProxy image, @NonNull Rect cropRect) {
        try {
            if (image.getFormat() != ImageFormat.YUV_420_888) {
                LogUtils.w(TAG, "Unsupported image format for cropping: " + image.getFormat());
                return null;
            }

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            // Validate and constrain crop rect to image bounds
            // Align to even boundaries for YUV chroma subsampling
            int left = Math.max(0, Math.min(cropRect.left, imageWidth - 1)) & ~1;
            int top = Math.max(0, Math.min(cropRect.top, imageHeight - 1)) & ~1;
            int right = Math.min(((Math.max(left + 2, Math.min(cropRect.right, imageWidth)) + 1) & ~1), imageWidth);
            int bottom = Math.min(((Math.max(top + 2, Math.min(cropRect.bottom, imageHeight)) + 1) & ~1), imageHeight);

            int cropWidth = right - left;
            int cropHeight = bottom - top;

            if (cropWidth <= 0 || cropHeight <= 0) {
                LogUtils.w(TAG, "Invalid crop dimensions: " + cropWidth + "x" + cropHeight);
                return null;
            }

            // Use grayscale conversion - much faster than full YUV to RGB
            // The Y plane is already grayscale, so we just copy it directly
            if (NativeYuvProcessor.isAvailable()) {
                return cropYuvToGrayscaleNative(image, left, top, cropWidth, cropHeight);
            } else {
                // Fall back to Java implementation
                return cropYuvToGrayscaleJava(image, left, top, cropWidth, cropHeight);
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "Error cropping image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ultra-fast native grayscale cropping.
     * Only reads the Y plane (which is already grayscale) and writes directly to bitmap.
     * This is significantly faster than full YUV to RGB conversion.
     */
    @Nullable
    private Bitmap cropYuvToGrayscaleNative(@NonNull ImageProxy image, int cropLeft, int cropTop, int cropWidth, int cropHeight) {
        try {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();

            ByteBuffer yBuffer = planes[0].getBuffer();
            int yRowStride = planes[0].getRowStride();

            // Create output bitmap
            Bitmap bitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888);

            // Call native method - only needs Y plane!
            boolean success = NativeYuvProcessor.cropYToGrayscaleBitmapNative(
                    yBuffer,
                    yRowStride,
                    cropLeft, cropTop, cropWidth, cropHeight,
                    bitmap
            );

            if (success) {
                return bitmap;
            } else {
                bitmap.recycle();
                LogUtils.w(TAG, "Native grayscale conversion failed, falling back to Java");
                return cropYuvToGrayscaleJava(image, cropLeft, cropTop, cropWidth, cropHeight);
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "Error in cropYuvToGrayscaleNative: " + e.getMessage());
            return cropYuvToGrayscaleJava(image, cropLeft, cropTop, cropWidth, cropHeight);
        }
    }

    /**
     * Java fallback implementation for cropped Y plane to grayscale conversion.
     * Much simpler and faster than full YUV to RGB - just copies Y values.
     */
    @Nullable
    private Bitmap cropYuvToGrayscaleJava(@NonNull ImageProxy image, int cropLeft, int cropTop, int cropWidth, int cropHeight) {
        try {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            int yRowStride = planes[0].getRowStride();

            // Create output pixel array
            int[] grayPixels = new int[cropWidth * cropHeight];
            int pixelIndex = 0;

            // Just copy Y values as grayscale (R=G=B=Y)
            for (int row = 0; row < cropHeight; row++) {
                int srcY = cropTop + row;
                int yRowOffset = srcY * yRowStride + cropLeft;

                for (int col = 0; col < cropWidth; col++) {
                    int y = yBuffer.get(yRowOffset + col) & 0xFF;
                    // Pack as ARGB with R=G=B=Y (grayscale)
                    grayPixels[pixelIndex++] = 0xFF000000 | (y << 16) | (y << 8) | y;
                }
            }

            // Create bitmap from pixel array
            Bitmap bitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(grayPixels, 0, cropWidth, 0, 0, cropWidth, cropHeight);
            return bitmap;

        } catch (Exception e) {
            LogUtils.e(TAG, "Error in cropYuvToGrayscaleJava: " + e.getMessage());
            return null;
        }
    }

    /**
     * Adjusts bounding boxes of detected barcodes to account for crop offset.
     * When we crop the image before decoding, the bounding boxes returned by the decoder
     * are relative to the cropped image. We need to offset them back to the original
     * image coordinate system.
     *
     * @param entities The list of detected barcode entities
     * @param offsetX The X offset of the crop region in the original image
     * @param offsetY The Y offset of the crop region in the original image
     * @return A new list with adjusted bounding boxes
     */
    private List<BarcodeEntity> adjustBoundingBoxesForCrop(List<BarcodeEntity> entities, int offsetX, int offsetY) {
        // BarcodeEntity objects from the SDK are likely immutable, and their bounding boxes
        // are in image coordinates. Since we're cropping and then mapping to overlay,
        // we need to adjust the coordinates at the mapping stage in CameraXLivePreviewActivity.
        //
        // However, the BarcodeEntity.getBoundingBox() returns a Rect that we can't modify
        // on the entity itself. The adjustment will need to happen when we map to overlay.
        //
        // For now, we'll store the offset and let the activity handle the adjustment
        // by providing methods to get the current crop offset.
        //
        // Actually, since the activity already maps bounding boxes through mapBoundingBoxToOverlay(),
        // we can return the entities as-is and have the activity add the offset before mapping.

        return entities;
    }

    /**
     * Stops the analysis process and terminates any ongoing tasks. This method should be
     * called to release resources and halt image analysis when it is no longer required.
     */
    public void stopAnalyzing() {
        isStopped = true;
        executorService.shutdownNow(); // Attempt to cancel ongoing tasks
    }

    /**
     * Resumes the analysis process after it was stopped.
     */
    public void resumeAnalyzing() {
        isStopped = false;
        isAnalyzing = true;
    }

    /**
     * Sets the crop region for image analysis.
     * When set, only the specified region of the image will be sent to the decoder,
     * significantly reducing computational load when using a capture zone.
     *
     * @param region The crop region in image coordinates (not overlay coordinates).
     *               Pass null to disable cropping and process the full frame.
     * @param imageWidth The width of the source image (for offset transformation)
     * @param imageHeight The height of the source image (for offset transformation)
     */
    public void setCropRegion(@Nullable Rect region, int imageWidth, int imageHeight) {
        if (region != null) {
            this.cropRegion = new Rect(region);
            this.cropOffsetX = region.left;
            this.cropOffsetY = region.top;
            this.sourceImageWidth = imageWidth;
            this.sourceImageHeight = imageHeight;
            LogUtils.d(TAG, "Crop region set: " + region.toString() + " for image " + imageWidth + "x" + imageHeight);
        } else {
            this.cropRegion = null;
            this.cropOffsetX = 0;
            this.cropOffsetY = 0;
            this.sourceImageWidth = 0;
            this.sourceImageHeight = 0;
            LogUtils.d(TAG, "Crop region cleared");
        }
    }

    /**
     * Sets the crop region for image analysis (without image dimensions).
     * @deprecated Use {@link #setCropRegion(Rect, int, int)} instead for proper coordinate handling.
     */
    @Deprecated
    public void setCropRegion(@Nullable Rect region) {
        setCropRegion(region, 0, 0);
    }

    /**
     * Gets the current crop region.
     *
     * @return The current crop region, or null if no cropping is active.
     */
    @Nullable
    public Rect getCropRegion() {
        Rect region = cropRegion;
        return region != null ? new Rect(region) : null;
    }

    /**
     * Gets the X offset of the current crop region.
     * This should be added to bounding box coordinates returned by the decoder
     * when a crop region is active.
     *
     * @return The X offset of the crop region, or 0 if no cropping is active.
     */
    public int getCropOffsetX() {
        return cropOffsetX;
    }

    /**
     * Gets the Y offset of the current crop region.
     * This should be added to bounding box coordinates returned by the decoder
     * when a crop region is active.
     *
     * @return The Y offset of the crop region, or 0 if no cropping is active.
     */
    public int getCropOffsetY() {
        return cropOffsetY;
    }

    /**
     * Checks if cropping is currently enabled.
     *
     * @return true if a crop region is set, false otherwise.
     */
    public boolean isCroppingEnabled() {
        return cropRegion != null;
    }

    /**
     * Gets the rotation degrees from the last processed image.
     * This is needed to transform bounding boxes from raw sensor space to effective image space
     * when cropping is enabled (since we decode with rotation=0).
     *
     * @return The rotation degrees (0, 90, 180, or 270) needed to display the image correctly.
     */
    public int getLastImageRotationDegrees() {
        return lastImageRotationDegrees;
    }

    /**
     * Sets the timing callback for receiving analysis performance metrics.
     *
     * @param callback The callback to receive timing data, or null to disable
     */
    public void setTimingCallback(@Nullable AnalysisTimingCallback callback) {
        this.timingCallback = callback;
    }

    /**
     * Enables or disables timing tracking for performance monitoring.
     * When enabled, the analyzer will calculate analysis per second and average analysis time.
     *
     * @param enabled true to enable timing tracking, false to disable
     */
    public void setTimingEnabled(boolean enabled) {
        this.timingEnabled = enabled;
        if (enabled) {
            // Reset timing state when enabling
            apsWindowStartTime = 0;
            frameCountInWindow = 0;
            currentAnalysisPerSecond = 0;
            // Reset average calculation
            analysisTimesIndex = 0;
            analysisTimesCount = 0;
            averageAnalysisTimeMs = 0;
        }
    }

    /**
     * Checks if timing tracking is enabled.
     *
     * @return true if timing tracking is enabled, false otherwise
     */
    public boolean isTimingEnabled() {
        return timingEnabled;
    }

    /**
     * Gets the average analysis time over the last 20 analyses.
     *
     * @return The average time in milliseconds taken to analyze frames
     */
    public long getAverageAnalysisTimeMs() {
        return averageAnalysisTimeMs;
    }

    /**
     * Gets the current number of analyses per second.
     *
     * @return The number of analyses completed in the last second
     */
    public int getCurrentAnalysisPerSecond() {
        return currentAnalysisPerSecond;
    }
}
