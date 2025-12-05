// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <cstdint>
#include <algorithm>

#define LOG_TAG "YuvProcessor"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Clamp value to [0, 255]
static inline int clamp(int value) {
    return value < 0 ? 0 : (value > 255 ? 255 : value);
}

extern "C" {

/**
 * Native YUV420 to RGB conversion with cropping.
 * Directly processes YUV planes and writes to a pre-allocated int array.
 * Uses integer math with fixed-point arithmetic for maximum speed.
 *
 * BT.601 coefficients (scaled by 1024):
 * R = 1.164*(Y-16) + 1.596*(V-128)
 * G = 1.164*(Y-16) - 0.392*(U-128) - 0.813*(V-128)
 * B = 1.164*(Y-16) + 2.017*(U-128)
 */
JNIEXPORT void JNICALL
Java_com_zebra_ai_1multibarcodes_1capture_barcodedecoder_NativeYuvProcessor_cropYuvToRgbNative(
        JNIEnv *env,
        jclass clazz,
        jobject yBuffer,
        jobject uBuffer,
        jobject vBuffer,
        jint yRowStride,
        jint uvRowStride,
        jint uvPixelStride,
        jint cropLeft,
        jint cropTop,
        jint cropWidth,
        jint cropHeight,
        jintArray outputPixels) {

    // Get direct buffer pointers
    auto *yData = static_cast<uint8_t *>(env->GetDirectBufferAddress(yBuffer));
    auto *uData = static_cast<uint8_t *>(env->GetDirectBufferAddress(uBuffer));
    auto *vData = static_cast<uint8_t *>(env->GetDirectBufferAddress(vBuffer));

    if (yData == nullptr || uData == nullptr || vData == nullptr) {
        LOGE("Failed to get direct buffer addresses");
        return;
    }

    // Get output array
    jint *outPixels = env->GetIntArrayElements(outputPixels, nullptr);
    if (outPixels == nullptr) {
        LOGE("Failed to get output pixel array");
        return;
    }

    const int uvCropLeft = cropLeft / 2;
    int pixelIndex = 0;

    // Process each row
    for (int row = 0; row < cropHeight; row++) {
        const int srcY = cropTop + row;
        const int yRowOffset = srcY * yRowStride + cropLeft;
        const int uvRowOffset = (srcY >> 1) * uvRowStride;

        // Process each column
        for (int col = 0; col < cropWidth; col++) {
            // Get Y value
            const int y = static_cast<int>(yData[yRowOffset + col]) - 16;

            // Get U and V values (subsampled 2x2)
            const int uvIndex = uvRowOffset + ((uvCropLeft + (col >> 1)) * uvPixelStride);
            const int u = static_cast<int>(uData[uvIndex]) - 128;
            const int v = static_cast<int>(vData[uvIndex]) - 128;

            // YUV to RGB conversion using integer math (fixed-point, 10-bit precision)
            // Coefficients: Y*1192, V*1634, U*401, V*833, U*2066
            const int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v) >> 10;
            int g = (y1192 - 401 * u - 833 * v) >> 10;
            int b = (y1192 + 2066 * u) >> 10;

            // Clamp to [0, 255]
            r = clamp(r);
            g = clamp(g);
            b = clamp(b);

            // Pack as ARGB
            outPixels[pixelIndex++] = static_cast<jint>(0xFF000000 | (r << 16) | (g << 8) | b);
        }
    }

    // Release the array
    env->ReleaseIntArrayElements(outputPixels, outPixels, 0);
}

/**
 * Optimized version that writes directly to a Bitmap.
 * This avoids an extra copy from int[] to Bitmap.
 */
JNIEXPORT jboolean JNICALL
Java_com_zebra_ai_1multibarcodes_1capture_barcodedecoder_NativeYuvProcessor_cropYuvToBitmapNative(
        JNIEnv *env,
        jclass clazz,
        jobject yBuffer,
        jobject uBuffer,
        jobject vBuffer,
        jint yRowStride,
        jint uvRowStride,
        jint uvPixelStride,
        jint cropLeft,
        jint cropTop,
        jint cropWidth,
        jint cropHeight,
        jobject bitmap) {

    // Get direct buffer pointers
    auto *yData = static_cast<uint8_t *>(env->GetDirectBufferAddress(yBuffer));
    auto *uData = static_cast<uint8_t *>(env->GetDirectBufferAddress(uBuffer));
    auto *vData = static_cast<uint8_t *>(env->GetDirectBufferAddress(vBuffer));

    if (yData == nullptr || uData == nullptr || vData == nullptr) {
        LOGE("Failed to get direct buffer addresses");
        return JNI_FALSE;
    }

    // Lock the bitmap for writing
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("Failed to get bitmap info");
        return JNI_FALSE;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888");
        return JNI_FALSE;
    }

    void *bitmapPixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("Failed to lock bitmap pixels");
        return JNI_FALSE;
    }

    auto *outPixels = static_cast<uint32_t *>(bitmapPixels);
    const int uvCropLeft = cropLeft / 2;

    // Process each row
    for (int row = 0; row < cropHeight; row++) {
        const int srcY = cropTop + row;
        const int yRowOffset = srcY * yRowStride + cropLeft;
        const int uvRowOffset = (srcY >> 1) * uvRowStride;
        uint32_t *rowPtr = outPixels + row * (bitmapInfo.stride / 4);

        // Process each column
        for (int col = 0; col < cropWidth; col++) {
            // Get Y value
            const int y = static_cast<int>(yData[yRowOffset + col]) - 16;

            // Get U and V values (subsampled 2x2)
            const int uvIndex = uvRowOffset + ((uvCropLeft + (col >> 1)) * uvPixelStride);
            const int u = static_cast<int>(uData[uvIndex]) - 128;
            const int v = static_cast<int>(vData[uvIndex]) - 128;

            // YUV to RGB conversion using integer math
            const int y1192 = 1192 * y;
            int r = (y1192 + 1634 * v) >> 10;
            int g = (y1192 - 401 * u - 833 * v) >> 10;
            int b = (y1192 + 2066 * u) >> 10;

            // Clamp to [0, 255]
            r = clamp(r);
            g = clamp(g);
            b = clamp(b);

            // Write as ARGB (Android Bitmap is actually ABGR in memory for RGBA_8888)
            rowPtr[col] = 0xFF000000 | (b << 16) | (g << 8) | r;
        }
    }

    // Unlock the bitmap
    AndroidBitmap_unlockPixels(env, bitmap);

    return JNI_TRUE;
}

/**
 * Ultra-fast grayscale-only cropping.
 * Only reads the Y plane (luminance) and writes it as grayscale to the bitmap.
 * This is significantly faster than full YUV to RGB conversion since:
 * 1. No U/V plane processing
 * 2. No color conversion math
 * 3. Simple memory copy with grayscale expansion
 *
 * The Y plane IS the grayscale image - we just need to replicate it to R, G, B channels.
 */
JNIEXPORT jboolean JNICALL
Java_com_zebra_ai_1multibarcodes_1capture_barcodedecoder_NativeYuvProcessor_cropYToGrayscaleBitmapNative(
        JNIEnv *env,
        jclass clazz,
        jobject yBuffer,
        jint yRowStride,
        jint cropLeft,
        jint cropTop,
        jint cropWidth,
        jint cropHeight,
        jobject bitmap) {

    // Get direct buffer pointer for Y plane only
    auto *yData = static_cast<uint8_t *>(env->GetDirectBufferAddress(yBuffer));

    if (yData == nullptr) {
        LOGE("Failed to get Y buffer address");
        return JNI_FALSE;
    }

    // Lock the bitmap for writing
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("Failed to get bitmap info");
        return JNI_FALSE;
    }

    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888");
        return JNI_FALSE;
    }

    void *bitmapPixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
        LOGE("Failed to lock bitmap pixels");
        return JNI_FALSE;
    }

    auto *outPixels = static_cast<uint32_t *>(bitmapPixels);
    const int bitmapStride = bitmapInfo.stride / 4; // stride in pixels (4 bytes per pixel)

    // Process each row - just copy Y values as grayscale
    for (int row = 0; row < cropHeight; row++) {
        const int srcY = cropTop + row;
        const uint8_t *yRowPtr = yData + srcY * yRowStride + cropLeft;
        uint32_t *outRowPtr = outPixels + row * bitmapStride;

        // Process each column - Y value becomes R=G=B
        for (int col = 0; col < cropWidth; col++) {
            const uint8_t y = yRowPtr[col];
            // Pack as ARGB with R=G=B=Y (grayscale)
            // Android RGBA_8888 is stored as ABGR in memory
            outRowPtr[col] = 0xFF000000 | (y << 16) | (y << 8) | y;
        }
    }

    // Unlock the bitmap
    AndroidBitmap_unlockPixels(env, bitmap);

    return JNI_TRUE;
}

} // extern "C"
