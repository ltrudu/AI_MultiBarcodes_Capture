package com.zebra.ai_multibarcodes_capture.eventinjection;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.emdkprofilemanagerhelper.IResultCallbacks;
import com.zebra.emdkprofilemanagerhelper.ProfileManagerCommand;
import com.zebra.eventinjectionservice.IEventInjectionService;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.TAG;


public class EventInjectionHelper {

    public interface IEventInjectionHelperConnectionCallback
    {
        void onServiceConnected();
        void onServiceDisconnected();
        void onAuthenticationSucceeded();
        void onAuthenticationFailed();
        void onException(Exception e);
    }

    private Context mContext = null;
    private IEventInjectionService iEventInjectionService = null;
    private IEventInjectionHelperConnectionCallback mCallback = null;
    private ClipboardManager mClipboardManager = null;

    public EventInjectionHelper(Context context)
    {
        this.mContext = context;
        mClipboardManager =  (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iEventInjectionService = IEventInjectionService.Stub.asInterface(service);
            Toast.makeText(mContext, "EventInjectionService Connected", Toast.LENGTH_SHORT).show();
            if(mCallback != null)
            {
                mCallback.onServiceConnected();
            }
            authenticateService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iEventInjectionService = null;
            Toast.makeText(mContext, "EventInjectionService Disonnected", Toast.LENGTH_SHORT).show();
            if(mCallback != null)
            {
                mCallback.onServiceDisconnected();
            }
        }
    };

    public void bindEventInjectionService(IEventInjectionHelperConnectionCallback callback) {
        mCallback = callback;
        Intent intent = new Intent("com.zebra.eventinjectionservice.IEventInjectionService");
        intent.setPackage("com.zebra.eventinjectionservice");
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void dispose()
    {
        try {
            mContext.unbindService(mServiceConnection);
            Toast.makeText(mContext, "KeyInjection service unbinded successfully.", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(mContext, "Error unbinding KeyInjection service.\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally {
            mServiceConnection = null;
            mContext = null;
            mCallback = null;
        }
    }

    private boolean authenticateService()
    {
        try {
            boolean result=iEventInjectionService.authenticate();
            if(result ) {
                Toast.makeText(mContext, "caller authentication successful", Toast.LENGTH_SHORT).show();
                if(mCallback != null)
                {
                    mCallback.onAuthenticationSucceeded();
                }
            } else {
                Toast.makeText(mContext, "caller authentication failed", Toast.LENGTH_SHORT).show();
                if(mCallback != null)
                {
                    mCallback.onAuthenticationFailed();
                }
            }
            return result;
        }catch(RemoteException re){
            Log.e(TAG, re.getMessage());
            if(mCallback != null)
            {
                mCallback.onException(re);
            }
        }
        return false;
    }

    public void sendStringAsClipboardPaste(String stringToSend) {
        if (iEventInjectionService != null) {

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                   if(mClipboardManager != null)
                   {
                       ClipData clipData = ClipData.newPlainText("text", stringToSend);
                       mClipboardManager.setPrimaryClip(clipData);
                       // Paste data
                       try {
                           long now = SystemClock.uptimeMillis();
                           iEventInjectionService.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PASTE, 0), 2);
                           iEventInjectionService.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PASTE, 0), 2);


                           mClipboardManager.clearPrimaryClip();
                       } catch (RemoteException re) {
                           Log.d(TAG, re.toString());
                       }
                   }
                }
            });
            t.start();
        }
    }

    public void sendStringAsKeyEvents(String stringToSend)
    {
        if(iEventInjectionService != null) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    List<KeyEvent> eventsToSend = KeyMappingHelper.stringToKeyEventList(stringToSend);
                    try {
                        for (KeyEvent event : eventsToSend) {
                            iEventInjectionService.injectInputEvent(event, 2);
                            Thread.sleep(Constants.KEY_INJECTION_SLEEP_BETWEEN_EVENTS);
                        }
                    } catch (RemoteException re) {
                        Log.e(TAG, re.toString());
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, e.toString());
                    }
                }
            });
            t.start();
        }
    }

    public void sendHomeKeyEvent(){
        if(iEventInjectionService != null) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        long now = SystemClock.uptimeMillis();
                        iEventInjectionService.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME, 0), 2);
                        iEventInjectionService.injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME, 0), 2);
                    } catch (RemoteException re) {
                        Log.d(TAG, re.toString());
                    }
                }

            };
            t.start();
        }
    }

    /******************************************************/
    /*** EMDK Helpers to access event injection service ***/
    /******************************************************/
    // Placeholder for custom certificate
    // Otherwise, the app will use the first certificate found with the method:
    // final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
    // TODO: Put your custom certificate in the apkCertificate member for MX AccessMgr registering (only if necessary and if you know what you are doing)
    public static Signature apkCertificate = null;

    public static void executeAccessMgrAthorizeServiceCommand(Context context, String serviceIdentifier, IResultCallbacks callbackInterface) {
        String profileName = "AccessMgr-1";
        String profileData = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            String path = context.getApplicationInfo().sourceDir;
            final String strAppName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            final String strPackageName = packageInfo.packageName;

            // Use custom signature if it has been set by the user
            Signature sig = EventInjectionHelper.apkCertificate;

            // Let's check if we have a custom certificate
            if (sig == null) {
                // Nope, we will get the first apk signing certificate that we find
                // You can copy/paste this snippet if you want to provide your own
                // certificate
                // TODO: use the following code snippet to extract your custom certificate if necessary
                final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
                if (arrSignatures == null || arrSignatures.length == 0) {
                    if (callbackInterface != null) {
                        callbackInterface.onError("Error : Package has no signing certificates... how's that possible ?","");
                        return;
                    }
                }
                sig = arrSignatures[0];
            }

            /*
             * Get the X.509 certificate.
             */
            final byte[] rawCert = sig.toByteArray();

            // Get the certificate as a base64 string
            String encoded = Base64.getEncoder().encodeToString(rawCert);

            profileData =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                            "<characteristic type=\"Profile\">" +
                            "<parm name=\"ProfileName\" value=\"" + profileName + "\"/>" +
                            "<characteristic type=\"AccessMgr\" version=\"11.3\">" +
                            "    <parm name=\"OperationMode\" value=\"1\" />\n" +
                            "    <parm name=\"ServiceAccessAction\" value=\"4\" />\n" +
                            "    <parm name=\"ServiceIdentifier\" value=\"" + serviceIdentifier + "\" />\n" +
                            "    <parm name=\"CallerPackageName\" value=\"" + strPackageName + "\" />\n" +
                            "    <parm name=\"CallerSignature\" value=\"" + encoded + "\" />\n" +
                            "</characteristic>" +
                            "</characteristic>";
            ProfileManagerCommand profileManagerCommand = new ProfileManagerCommand(context);
            profileManagerCommand.execute(profileData, profileName, callbackInterface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            if (callbackInterface != null) {
                callbackInterface.onError("Error on profile: " + profileName + "\nError:" + e.getLocalizedMessage() + "\nProfileData:" + profileData, "");
            }
        }
    }

    /************** Cached list *******************************/
    private final List<String> cacheList = new ArrayList<>();
    private final Object lock = new Object();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer = new Timer();

    public void addToClipBoardCache(String data) {
        synchronized (lock) {
            cacheList.add(data);
            resetFlushTimer();
        }
    }

    public List<String> extractCache() {
        synchronized (lock) {
            List<String> extractedData = new ArrayList<>(cacheList);
            cacheList.clear();
            return extractedData;
        }
    }

    private void resetFlushTimer() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new FlushTask(), Constants.CLIPBOARD_FLUSH_INTERVAL);
    }

    private class FlushTask extends TimerTask {
        @Override
        public void run() {
            handler.post(() -> {
                synchronized (lock) {
                    List<String> flushedData = extractCache();
                    // Process flushed data
                    String allData = "";
                    for(String data : flushedData)
                    {
                        allData += data;
                    }
                    sendStringAsClipboardPaste(allData);
                    System.out.println("Flushed Data: " + flushedData);
                }
            });
        }
    }

}
