package com.zebra.aisuite_quickstart;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.zebra.criticalpermissionshelper.CriticalPermissionsHelper;
import com.zebra.criticalpermissionshelper.EPermissionType;
import com.zebra.criticalpermissionshelper.IResultCallbacks;

public class MainApplication extends Application {

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
}
