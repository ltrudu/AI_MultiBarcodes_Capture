package com.zebra.ai_multibarcodes_capture.eventinjection;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.EntryChoiceActivity;
import com.zebra.ai_multibarcodes_capture.R;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.TAG;

public class AIMultiBarcodeCaptureInjectionService extends Service {
    private static final int SERVICE_ID = 1;

    private static boolean isRunning = false;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    public AIMultiBarcodeCaptureInjectionService() {
    }

    public IBinder onBind(Intent paramIntent)
    {
        return null;
    }

    public void onCreate()
    {
        logD("onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logD("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startService();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isRunning = true;
        }
        return Service.START_STICKY;
    }

    public void onDestroy()
    {
        logD("onDestroy");
        stopService();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isRunning = false;
        }
    }

    @SuppressLint({"Wakelock"})
    private void startService()
    {
        logD("startService");
        try
        {
            if(mNotificationManager == null)
                mNotificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));

            Intent mainActivityIntent = new Intent(this, EntryChoiceActivity.class);
            PendingIntent pendingIntent = null;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        mainActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
            else {
                pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        mainActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Create the Foreground Service
            String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(mNotificationManager) : "";


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
            mNotification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.foreground_service_notification_title))
                    .setContentText(getString(R.string.foreground_service_notification_text))
                    .setTicker(getString(R.string.foreground_service_notification_tickle))
                    .setPriority(PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();

            TaskStackBuilder localTaskStackBuilder = TaskStackBuilder.create(this);
            localTaskStackBuilder.addParentStack(EntryChoiceActivity.class);
            localTaskStackBuilder.addNextIntent(mainActivityIntent);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
            }
            else {
                notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT));
            }

            // Start foreground service
            startForeground(SERVICE_ID, mNotification);

            bindEventInjectionService();

            logD("startService:Service started without error.");
        }
        catch(Exception e)
        {
            logD("startService:Error while starting service.");
            e.printStackTrace();
        }


    }

    /***************************/
    /*** Custom code members ***/
    /***************************/
    EventInjectionHelper mEventInjectionHelper = null;

    public EventInjectionHelper getmEventInjectionHelper()
    {
        return mEventInjectionHelper;
    }

    private void bindEventInjectionService()
    {
        // TODO: Add your service initialization and running code here
        mEventInjectionHelper = new EventInjectionHelper(this);
        mEventInjectionHelper.bindEventInjectionService(new EventInjectionHelper.IEventInjectionHelperConnectionCallback() {
            @Override
            public void onServiceConnected() {
                Log.v(TAG, "EventInjectionService connected.");
            }

            @Override
            public void onServiceDisconnected() {
                Log.e(TAG, "EventInjectionService disconnected.");
            }

            @Override
            public void onAuthenticationSucceeded() {
                Log.v(TAG, "EventInjectionService authenticated.");
                // TODO: add your custom behaviour and code here
                initializeCustomCode();
            }

            @Override
            public void onAuthenticationFailed() {
                Log.e(TAG, "EventInjectionService authentication failed.");
            }

            @Override
            public void onException(Exception e) {
                Log.e(TAG, "EventInjectionService EXCEPTION");
                e.printStackTrace();
            }
        });

    }



    private void stopEventInjectionService()
    {
        // TODO: Stop your actions and release your stuffs here
        mEventInjectionHelper.dispose();
        mEventInjectionHelper = null;

        disposeCustomCode();
    }

    private void stopService()
    {
        try
        {
            logD("stopService.");

            stopEventInjectionService();
            if(mNotificationManager != null)
            {
                mNotificationManager.cancelAll();
                mNotificationManager = null;
            }


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(STOP_FOREGROUND_REMOVE);
            }
            else {
                stopForeground(true);
            }

            logD("stopService:Service stopped without error.");
        }
        catch(Exception e)
        {
            logD("Error while stopping service.");
            e.printStackTrace();

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel channel = new NotificationChannel(getString(R.string.foregroundservice_channel_id), getString(R.string.foregroundservice_channel_name), NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return getString(R.string.foregroundservice_channel_id);
    }

    private void logD(String message)
    {
        Log.d(TAG, message);
    }

    public static void startService(Context context)
    {
        Intent myIntent = new Intent(context.getApplicationContext(), AIMultiBarcodeCaptureInjectionService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Use start foreground service to prevent the runtime error:
            // "not allowed to start service intent app is in background"
            // to happen when running on OS >= Oreo
            context.startForegroundService(myIntent);
        }
        else
        {
            context.startService(myIntent);
        }
    }

    public static void stopService(Context context)
    {
        Intent myIntent = new Intent(context, AIMultiBarcodeCaptureInjectionService.class);
        context.stopService(myIntent);
    }

    public static boolean isRunning(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return isRunning;
        }
        else {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AIMultiBarcodeCaptureInjectionService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*****************************************************************************
     * Insert your custom code here
     *****************************************************************************/

    /***
     * TODO: Initialize your custom code here
     */
    private void initializeCustomCode()
    {
        // Ex: here you can setup a broadcast receiver or a custom device implementation and
        // use the EventInjectionHelper to inject key events to the focused control
        // For example you can send clipboard events with the method (recommended):
        // mEventInjectionHelper.sendStringAsClipboardPaste("myStringToSend");
        // or you can add many strings to the cliboard cache that is flushed every Constants.CLIPBOARD_FLUSH_INTERVAL ms
        // mEventInjectionHelper.addToClipBoardCache("myEPCString" or "myBarcodeString")
        // mEventInjectionHelper.addToClipBoardCache("myEPCString2" or "myBarcodeString2")
        // mEventInjectionHelper.addToClipBoardCache("myEPCString3" or "myBarcodeString3")
        // or you can send using KeyboardInjection (not recommended except for webapp that catch key events)
        // mEventInjectionHelper.sendStringAsKeyEvents("myBarcodeDataString");
        // a sleep will occur between each keystroke its value is Constants.KEY_INJECTION_SLEEP_BETWEEN_EVENTS
        Toast.makeText(this, "Service started : initializeCustomCode executed.", Toast.LENGTH_SHORT).show();
    }

    /**
     * TODO: Stop and dispose your custom code here
     */
    private void disposeCustomCode()
    {
        Toast.makeText(this, "Service stoped : disposeCustomCode executed.", Toast.LENGTH_SHORT).show();
    }
}
