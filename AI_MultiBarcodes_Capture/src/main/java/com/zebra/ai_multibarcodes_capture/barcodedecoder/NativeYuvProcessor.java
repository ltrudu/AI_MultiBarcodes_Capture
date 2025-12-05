// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.barcodedecoder;

import android.graphics.Bitmap;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Native YUV processor using NDK for high-performance YUV to RGB conversion.
 * This provides significantly faster cropped YUV conversion compared to Java implementation.
 */
public class NativeYuvProcessor {

    private static final String TAG = "NativeYuvProcessor";
    private static boolean isNativeLibraryLoaded = false;

    static {
        try {
            System.loadLibrary("yuvprocessor");
            isNativeLibraryLoaded = true;
            Log.i(TAG, "Native YUV processor library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            isNativeLibraryLoaded = false;
            Log.e(TAG, "Failed to load native YUV processor library: " + e.getMessage());
        }
    }

    /**
     * Check if the native library is available.
     *
     * @return true if native processing is available, false otherwise
     */
    public static boolean isAvailable() {
        return isNativeLibraryLoaded;
    }

    /**
     * Native method to convert cropped YUV420 to RGB pixel array.
     *
     * @param yBuffer      Y plane direct ByteBuffer
     * @param uBuffer      U plane direct ByteBuffer
     * @param vBuffer      V plane direct ByteBuffer
     * @param yRowStride   Row stride for Y plane
     * @param uvRowStride  Row stride for UV planes
     * @param uvPixelStride Pixel stride for UV planes
     * @param cropLeft     Left coordinate of crop region (must be even)
     * @param cropTop      Top coordinate of crop region (must be even)
     * @param cropWidth    Width of crop region
     * @param cropHeight   Height of crop region
     * @param outputPixels Pre-allocated int array of size cropWidth * cropHeight
     */
    public static native void cropYuvToRgbNative(
            ByteBuffer yBuffer,
            ByteBuffer uBuffer,
            ByteBuffer vBuffer,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int cropLeft,
            int cropTop,
            int cropWidth,
            int cropHeight,
            int[] outputPixels
    );

    /**
     * Native method to convert cropped YUV420 directly to a Bitmap.
     * This is more efficient as it avoids an extra copy.
     *
     * @param yBuffer      Y plane direct ByteBuffer
     * @param uBuffer      U plane direct ByteBuffer
     * @param vBuffer      V plane direct ByteBuffer
     * @param yRowStride   Row stride for Y plane
     * @param uvRowStride  Row stride for UV planes
     * @param uvPixelStride Pixel stride for UV planes
     * @param cropLeft     Left coordinate of crop region (must be even)
     * @param cropTop      Top coordinate of crop region (must be even)
     * @param cropWidth    Width of crop region
     * @param cropHeight   Height of crop region
     * @param bitmap       Pre-allocated ARGB_8888 bitmap of size cropWidth x cropHeight
     * @return true if successful, false otherwise
     */
    public static native boolean cropYuvToBitmapNative(
            ByteBuffer yBuffer,
            ByteBuffer uBuffer,
            ByteBuffer vBuffer,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int cropLeft,
            int cropTop,
            int cropWidth,
            int cropHeight,
            Bitmap bitmap
    );

    /**
     * Ultra-fast native method to crop Y plane only and create a grayscale Bitmap.
     * This is significantly faster than full YUV to RGB conversion because:
     * - Only reads the Y plane (no U/V processing)
     * - No color conversion math required
     * - Simple memory copy with grayscale expansion
     *
     * @param yBuffer      Y plane direct ByteBuffer
     * @param yRowStride   Row stride for Y plane
     * @param cropLeft     Left coordinate of crop region
     * @param cropTop      Top coordinate of crop region
     * @param cropWidth    Width of crop region
     * @param cropHeight   Height of crop region
     * @param bitmap       Pre-allocated ARGB_8888 bitmap of size cropWidth x cropHeight
     * @return true if successful, false otherwise
     */
    public static native boolean cropYToGrayscaleBitmapNative(
            ByteBuffer yBuffer,
            int yRowStride,
            int cropLeft,
            int cropTop,
            int cropWidth,
            int cropHeight,
            Bitmap bitmap
    );
}
