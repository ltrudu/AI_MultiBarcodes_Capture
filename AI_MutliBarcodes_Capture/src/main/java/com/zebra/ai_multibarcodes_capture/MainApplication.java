package com.zebra.ai_multibarcodes_capture;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zebra.criticalpermissionshelper.CriticalPermissionsHelper;
import com.zebra.criticalpermissionshelper.EPermissionType;
import com.zebra.criticalpermissionshelper.IResultCallbacks;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;

public class MainApplication extends Application {

    private static final String TAG = "MainApplication";
    private ManagedConfigurationReceiver managedConfigReceiver;

    public interface iMainApplicationCallback
    {
        void onPermissionSuccess(String message);
        void onPermissionError(String message);
        void onPermissionDebug(String message);
    }

    public static boolean permissionGranted = false;
    public static String sErrorMessage = null;

    public static iMainApplicationCallback iMainApplicationCallback = null;

    // Let's Add a fake delay of 2000 milliseconds just for the show ;)
    // Otherwise Splash Screen is too fast
    private final static int S_FAKE_DELAY = 2000;

    @Override
    public void onCreate() {
        super.onCreate();

        // Register managed configuration receiver dynamically (required for Android 8.0+)
        registerManagedConfigurationReceiver();

        // Apply managed configuration on startup
        ManagedConfigurationReceiver.applyManagedConfiguration(this);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                CriticalPermissionsHelper.grantPermission(MainApplication.this, EPermissionType.ALL_DANGEROUS_PERMISSIONS, new IResultCallbacks() {
                    @Override
                    public void onSuccess(String message, String resultXML) {
                        permissionGranted = true;
                        sErrorMessage = null;
                        if(MainApplication.iMainApplicationCallback != null)
                        {
                            MainApplication.iMainApplicationCallback.onPermissionSuccess(message);
                        }
                    }

                    @Override
                    public void onError(String message, String resultXML) {
                        Toast.makeText(MainApplication.this, message, Toast.LENGTH_LONG).show();
                        permissionGranted = true;
                        sErrorMessage = message;
                        if(MainApplication.iMainApplicationCallback != null)
                        {
                            MainApplication.iMainApplicationCallback.onPermissionError(message);
                        }
                    }

                    @Override
                    public void onDebugStatus(String message) {
                        if(MainApplication.iMainApplicationCallback != null)
                        {
                            MainApplication.iMainApplicationCallback.onPermissionDebug(message);
                        }
                    }
                });
            }
        }, S_FAKE_DELAY); // Let's add some S_FAKE_DELAY like in music production
    }

    /**
     * Register the ManagedConfigurationReceiver dynamically
     * This is required for Android 8.0+ due to broadcast limitations
     */
    private void registerManagedConfigurationReceiver() {
        try {
            managedConfigReceiver = new ManagedConfigurationReceiver();
            IntentFilter filter = new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
            registerReceiver(managedConfigReceiver, filter);
            LogUtils.d(TAG, "ManagedConfigurationReceiver registered dynamically");
        } catch (Exception e) {
            LogUtils.e(TAG, "Failed to register ManagedConfigurationReceiver", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Unregister the managed configuration receiver
        if (managedConfigReceiver != null) {
            try {
                unregisterReceiver(managedConfigReceiver);
                LogUtils.d(TAG, "ManagedConfigurationReceiver unregistered");
            } catch (IllegalArgumentException e) {
                LogUtils.d(TAG, "ManagedConfigurationReceiver was not registered, ignoring unregister attempt");
            }
        }
    }
}
