package com.zebra.ai_multibarcodes_capture.helpers;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class that provides a wrapper around Android's Log class.
 * This allows for centralized logging control and potential future enhancements
 * like log filtering, formatting, or remote logging capabilities.
 * 
 * Features:
 * - Automatic feedback channel reporting for error logs
 * - Centralized error tracking and reporting
 * - Device and app information collection for debugging
 */
public class LogUtils {

    private static Context appContext;
    private static final String FEEDBACK_EMAIL = "support@zebra.com";
    private static boolean feedbackReportingEnabled = true;

    /**
     * Initialize LogUtils with application context for feedback reporting
     * Call this in your Application class onCreate method
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * Enable or disable automatic feedback reporting for errors
     */
    public static void setFeedbackReportingEnabled(boolean enabled) {
        feedbackReportingEnabled = enabled;
    }

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

    // Error logging with automatic feedback reporting
    public static void e(String TAG, String message) {
        Log.e(TAG, message);
        if (feedbackReportingEnabled && appContext != null) {
            reportErrorToFeedbackChannel(TAG, message, null);
        }
    }

    public static void e(String TAG, String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        if (feedbackReportingEnabled && appContext != null) {
            reportErrorToFeedbackChannel(TAG, message, throwable);
        }
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

    /**
     * Reports error to feedback channels (EMM and email)
     * This method creates a detailed error report and sends it via multiple channels
     */
    private static void reportErrorToFeedbackChannel(String tag, String message, Throwable throwable) {
        try {
            String errorReport = generateErrorReport(tag, message, throwable);
            
            // Try EMM feedback channel first (enterprise priority)
            boolean emmSent = sendErrorReportViaEMM(tag, message, throwable, errorReport);
            
            // Send via email as backup or if EMM is not available
            if (!emmSent) {
                sendErrorReportViaEmail(errorReport);
            }
        } catch (Exception e) {
            // Failsafe: don't crash if feedback reporting fails
            Log.e("LogUtils", "Failed to report error to feedback channel", e);
        }
    }

    /**
     * Generates a comprehensive error report with device and app information
     */
    private static String generateErrorReport(String tag, String message, Throwable throwable) {
        StringBuilder report = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        report.append("=== AI MultiBarcode Capture - Error Report ===\n\n");
        
        // Timestamp
        report.append("Timestamp: ").append(dateFormat.format(new Date())).append("\n\n");
        
        // Error Details
        report.append("=== ERROR DETAILS ===\n");
        report.append("Tag: ").append(tag).append("\n");
        report.append("Message: ").append(message).append("\n\n");
        
        // Exception Details
        if (throwable != null) {
            report.append("Exception: ").append(throwable.getClass().getSimpleName()).append("\n");
            report.append("Exception Message: ").append(throwable.getMessage()).append("\n");
            
            // Stack trace
            report.append("\n=== STACK TRACE ===\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            report.append(sw.toString()).append("\n");
        }
        
        // Device Information
        report.append("=== DEVICE INFORMATION ===\n");
        report.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        report.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("Build ID: ").append(Build.ID).append("\n");
        report.append("Build Type: ").append(Build.TYPE).append("\n\n");
        
        // App Information
        try {
            String packageName = appContext.getPackageName();
            String versionName = appContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
            int versionCode = appContext.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            
            report.append("=== APP INFORMATION ===\n");
            report.append("Package: ").append(packageName).append("\n");
            report.append("Version: ").append(versionName).append(" (").append(versionCode).append(")\n\n");
        } catch (PackageManager.NameNotFoundException e) {
            report.append("=== APP INFORMATION ===\n");
            report.append("Unable to retrieve app information\n\n");
        }
        
        report.append("=== END REPORT ===");
        
        return report.toString();
    }

    /**
     * Sends error report via EMM feedback channel
     * Uses Android Enterprise feedback mechanisms for managed devices
     * 
     * @param tag Error tag
     * @param message Error message
     * @param throwable Exception (if any)
     * @param errorReport Full error report
     * @return true if EMM feedback was sent successfully, false otherwise
     */
    private static boolean sendErrorReportViaEMM(String tag, String message, Throwable throwable, String errorReport) {
        try {
            if (appContext == null) {
                return false;
            }

            // Try multiple EMM feedback mechanisms
            boolean sent = false;

            // Method 1: Device Policy Manager feedback
            sent |= sendViaDPMFeedback(errorReport);

            // Method 2: Enterprise feedback intent
            sent |= sendViaEnterpriseFeedbackIntent(tag, message, throwable, errorReport);

            // Method 3: Managed configuration feedback broadcast
            sent |= sendViaManagedConfigFeedback(tag, message, throwable, errorReport);

            if (sent) {
                Log.d("LogUtils", "Error report sent via EMM feedback channel");
            }

            return sent;
        } catch (Exception e) {
            Log.e("LogUtils", "Failed to send error report via EMM", e);
            return false;
        }
    }

    /**
     * Send feedback via Device Policy Manager (Android Enterprise)
     */
    private static boolean sendViaDPMFeedback(String errorReport) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                DevicePolicyManager dpm = (DevicePolicyManager) appContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
                
                if (dpm != null && dpm.isDeviceOwnerApp(appContext.getPackageName())) {
                    // Device owner app can use DPM feedback mechanisms
                    Bundle feedbackBundle = new Bundle();
                    feedbackBundle.putString("error_report", errorReport);
                    feedbackBundle.putString("app_package", appContext.getPackageName());
                    feedbackBundle.putLong("timestamp", System.currentTimeMillis());
                    
                    // Send feedback via device policy manager
                    Intent feedbackIntent = new Intent("android.app.action.DEVICE_ADMIN_FEEDBACK");
                    feedbackIntent.putExtras(feedbackBundle);
                    feedbackIntent.setPackage("com.google.android.gms"); // Google Play Services
                    
                    if (feedbackIntent.resolveActivity(appContext.getPackageManager()) != null) {
                        appContext.sendBroadcast(feedbackIntent);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.d("LogUtils", "DPM feedback not available: " + e.getMessage());
        }
        return false;
    }

    /**
     * Send feedback via Enterprise feedback intent
     */
    private static boolean sendViaEnterpriseFeedbackIntent(String tag, String message, Throwable throwable, String errorReport) {
        try {
            // Try enterprise feedback intent
            Intent enterpriseIntent = new Intent("com.android.enterprise.FEEDBACK");
            enterpriseIntent.putExtra("error_tag", tag);
            enterpriseIntent.putExtra("error_message", message);
            enterpriseIntent.putExtra("error_report", errorReport);
            enterpriseIntent.putExtra("app_package", appContext.getPackageName());
            enterpriseIntent.putExtra("timestamp", System.currentTimeMillis());
            
            if (throwable != null) {
                enterpriseIntent.putExtra("exception_class", throwable.getClass().getSimpleName());
                enterpriseIntent.putExtra("exception_message", throwable.getMessage());
            }
            
            // Check if any EMM can handle this intent
            if (enterpriseIntent.resolveActivity(appContext.getPackageManager()) != null) {
                appContext.sendBroadcast(enterpriseIntent);
                return true;
            }
        } catch (Exception e) {
            Log.d("LogUtils", "Enterprise feedback intent not available: " + e.getMessage());
        }
        return false;
    }

    /**
     * Send feedback via managed configuration system
     * Leverages our existing managed configuration receiver
     */
    private static boolean sendViaManagedConfigFeedback(String tag, String message, Throwable throwable, String errorReport) {
        try {
            // Send feedback via our managed configuration system
            Intent managedConfigIntent = new Intent("com.zebra.ai_multibarcodes_capture.EMM_FEEDBACK");
            managedConfigIntent.putExtra("feedback_type", "ERROR_REPORT");
            managedConfigIntent.putExtra("error_tag", tag);
            managedConfigIntent.putExtra("error_message", message);
            managedConfigIntent.putExtra("error_report", errorReport);
            managedConfigIntent.putExtra("app_package", appContext.getPackageName());
            managedConfigIntent.putExtra("app_version", getAppVersion());
            managedConfigIntent.putExtra("device_model", Build.MANUFACTURER + " " + Build.MODEL);
            managedConfigIntent.putExtra("android_version", Build.VERSION.RELEASE);
            managedConfigIntent.putExtra("timestamp", System.currentTimeMillis());
            
            if (throwable != null) {
                managedConfigIntent.putExtra("exception_class", throwable.getClass().getSimpleName());
                managedConfigIntent.putExtra("stack_trace", getStackTraceString(throwable));
            }
            
            // Send as broadcast - EMM systems can listen for this
            appContext.sendBroadcast(managedConfigIntent);
            
            // Also try to send to specific EMM packages
            String[] emmPackages = {
                "com.microsoft.intune.mam",           // Microsoft Intune
                "com.google.android.apps.work.clouddpc", // Google Cloud DPC
                "com.vmware.workspace",               // VMware Workspace ONE
                "com.mobileiron.android.client",     // MobileIron
                "com.citrix.xenmobile",              // Citrix XenMobile
                "com.samsung.android.knox.mpos",     // Samsung Knox
                "com.blackberry.blackberrybridge"    // BlackBerry Bridge
            };
            
            boolean sentToEmm = false;
            for (String emmPackage : emmPackages) {
                try {
                    Intent emmIntent = new Intent(managedConfigIntent);
                    emmIntent.setPackage(emmPackage);
                    if (emmIntent.resolveActivity(appContext.getPackageManager()) != null) {
                        appContext.sendBroadcast(emmIntent);
                        sentToEmm = true;
                        Log.d("LogUtils", "Sent feedback to EMM: " + emmPackage);
                    }
                } catch (Exception e) {
                    // Continue to next EMM package
                }
            }
            
            return sentToEmm;
        } catch (Exception e) {
            Log.d("LogUtils", "Managed config feedback not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to get app version
     */
    private static String getAppVersion() {
        try {
            return appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    /**
     * Helper method to get stack trace as string
     */
    private static String getStackTraceString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Opens email client with pre-filled error report
     */
    private static void sendErrorReportViaEmail(String errorReport) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + FEEDBACK_EMAIL));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "AI MultiBarcode Capture - Error Report");
            emailIntent.putExtra(Intent.EXTRA_TEXT, errorReport);
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // Check if there's an email app available
            if (emailIntent.resolveActivity(appContext.getPackageManager()) != null) {
                appContext.startActivity(emailIntent);
            } else {
                // Fallback: show toast with error info
                Toast.makeText(appContext, appContext.getString(R.string.error_reported, errorReport.substring(0, Math.min(100, errorReport.length()))), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("LogUtils", "Failed to send error report via email", e);
        }
    }

    /**
     * Manually report an error to the feedback channel
     * Use this for critical errors that need immediate attention
     */
    public static void reportCriticalError(String tag, String message, Throwable throwable) {
        // Log the error normally
        Log.e(tag, message, throwable);
        
        // Force feedback reporting even if disabled
        if (appContext != null) {
            reportErrorToFeedbackChannel(tag, "CRITICAL: " + message, throwable);
        }
    }
}
