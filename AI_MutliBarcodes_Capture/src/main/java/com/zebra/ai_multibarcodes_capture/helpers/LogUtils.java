package com.zebra.ai_multibarcodes_capture.helpers;

import android.util.Log;

/**
 * Utility class that provides a wrapper around Android's Log class.
 * This allows for centralized logging control and potential future enhancements
 * like log filtering, formatting, or remote logging capabilities.
 */
public class LogUtils {

    // Verbose logging
    public static void v(String TAG, String message) {
        Log.v(TAG, message);
    }

    public static void v(String TAG, String message, Throwable throwable) {
        Log.v(TAG, message, throwable);
    }

    // Debug logging
    public static void d(String TAG, String message) {
        Log.d(TAG, message);
    }

    public static void d(String TAG, String message, Throwable throwable) {
        Log.d(TAG, message, throwable);
    }

    // Info logging
    public static void i(String TAG, String message) {
        Log.i(TAG, message);
    }

    public static void i(String TAG, String message, Throwable throwable) {
        Log.i(TAG, message, throwable);
    }

    // Warning logging
    public static void w(String TAG, String message) {
        Log.w(TAG, message);
    }

    public static void w(String TAG, String message, Throwable throwable) {
        Log.w(TAG, message, throwable);
    }

    public static void w(String TAG, Throwable throwable) {
        Log.w(TAG, throwable);
    }

    // Error logging
    public static void e(String TAG, String message) {
        Log.e(TAG, message);
    }

    public static void e(String TAG, String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }

    // What a Terrible Failure logging
    public static void wtf(String TAG, String message) {
        Log.wtf(TAG, message);
    }

    public static void wtf(String TAG, String message, Throwable throwable) {
        Log.wtf(TAG, message, throwable);
    }

    public static void wtf(String TAG, Throwable throwable) {
        Log.wtf(TAG, throwable);
    }

    // Utility methods for checking if log levels are enabled
    public static boolean isLoggable(String TAG, int level) {
        return Log.isLoggable(TAG, level);
    }

    // Convenience methods for common log level checks
    public static boolean isVerboseLoggable(String TAG) {
        return Log.isLoggable(TAG, Log.VERBOSE);
    }

    public static boolean isDebugLoggable(String TAG) {
        return Log.isLoggable(TAG, Log.DEBUG);
    }

    public static boolean isInfoLoggable(String TAG) {
        return Log.isLoggable(TAG, Log.INFO);
    }

    public static boolean isWarnLoggable(String TAG) {
        return Log.isLoggable(TAG, Log.WARN);
    }

    public static boolean isErrorLoggable(String TAG) {
        return Log.isLoggable(TAG, Log.ERROR);
    }
}
