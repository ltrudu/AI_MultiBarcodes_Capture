// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallerLauncher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zebra.ai.vision.detector.AIVisionSDK;
import com.zebra.ai_multibarcodes_capture.databinding.ActivityEntryChoiceBinding;
import com.zebra.ai_multibarcodes_capture.filemanagement.BrowserActivity;
import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;
import com.zebra.ai_multibarcodes_capture.filemanagement.FileUtil;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.helpers.PreferencesHelper;
import com.zebra.ai_multibarcodes_capture.java.CameraXLivePreviewActivity;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;
import com.zebra.ai_multibarcodes_capture.sessionmanagement.SessionViewerActivity;
import com.zebra.ai_multibarcodes_capture.settings.SettingsActivity;

import java.io.File;
import java.io.IOException;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.FILE_DEFAULT_EXTENSION;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.FILE_DEFAULT_PREFIX;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_EXTENSION;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_PREFIX;

/**
 * The EntryChoiceActivity class is an Android activity that serves as an entry point for selecting
 * between different implementations of camera-based functionalities. It checks for camera permissions,
 * initializes the AI Vision SDK, and provides options to navigate to Java or Kotlin versions of CameraX
 * Live Preview activity.

 * Usage:
 * - This activity is started as part of the application launch sequence.
 * - It displays buttons that allow the user to choose between Java and Kotlin implementations of CameraXLivePreviewActivity.
 * - It ensures that camera permissions are granted and initializes the AI Vision SDK.

 * Dependencies:
 * - Android Activity: Provides the lifecycle management and user interface capabilities.
 * - ActivityEntryChoiceBinding: Used for view binding to access UI components.
 * - AIVisionSDK: Provides functionality for initializing AI Vision capabilities.
 * - CameraXLivePreviewActivity: The target activity for the Java implementation.
 * - com.zebra.ai_multibarcodes_capture.kotlin.CameraXLivePreviewActivity: The target activity for the Kotlin implementation.

 * Permissions:
 * - Camera permission is required for the app to function. This activity handles permission checks and requests.

 * Exception Handling:
 * - Handles UnsupportedOperationException during AI Vision SDK initialization.
 * - Displays error dialogs to inform the user of issues.

 * Note: Ensure that the appropriate permissions are configured in the AndroidManifest to utilize camera capabilities.
 */
public class EntryChoiceActivity extends AppCompatActivity {

    private ActivityEntryChoiceBinding binding;
    private final String TAG = "EntryChoiceActivity";
    // Define a request code for camera permission
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private String sessionFilePathString;

    private ActivityResultLauncher<Intent> resultBrowseFile;
    private ActivityResultLauncher<Intent> resultSettings;
    private ActivityResultLauncher<Intent> resultCapture;

    String filePrefix = FILE_DEFAULT_PREFIX;
    EExportMode eExportMode = EExportMode.TEXT;

    // BroadcastReceiver to listen for managed configuration reload requests
    private BroadcastReceiver reloadPreferencesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES.equals(intent.getAction())) {
                LogUtils.d(TAG, "Received reload preferences request from ManagedConfigurationReceiver");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadPreferences();
                        Toast.makeText(EntryChoiceActivity.this, 
                            "Settings updated by administrator", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEntryChoiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initializing the AI Vision SDK
        try {
            boolean isInitDone = AIVisionSDK.getInstance(this.getApplicationContext()).init();
            LogUtils.i(TAG, "AI Vision SDK Init ret = " + isInitDone);
        } catch (UnsupportedOperationException ex) {
            LogUtils.e(TAG, getString(R.string.ai_vision_sdk_init_failed));
        }

        // load preferences
        loadPreferences();

        setSupportActionBar(binding.tbEntry);
        binding.tbEntry.setNavigationIcon(R.drawable.ic_menu_white);
        binding.tbEntry.setNavigationOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 PopupMenu popup = new PopupMenu(EntryChoiceActivity.this, view);
                 MenuInflater inflater = popup.getMenuInflater();
                 inflater.inflate(R.menu.entry_menu, popup.getMenu());
                 
                 // Force PopupMenu to show icons
                 try {
                     java.lang.reflect.Field mFieldPopup = popup.getClass().getDeclaredField("mPopup");
                     mFieldPopup.setAccessible(true);
                     Object menuPopupHelper = mFieldPopup.get(popup);
                     java.lang.reflect.Method setForceIcons = menuPopupHelper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
                     setForceIcons.invoke(menuPopupHelper, true);
                 } catch (Exception e) {
                     LogUtils.e(TAG, "Failed to force show icons in popup menu", e);
                 }
                 
                 popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                     @Override
                     public boolean onMenuItemClick(MenuItem menuItem) {
                         int id = menuItem.getItemId();
                         if (id == R.id.action_manage_sessions) {
                             Intent intent = new Intent(EntryChoiceActivity.this, BrowserActivity.class);
                             File todayFolderPath = FileUtil.getBaseFolder();
                             intent.putExtra(Constants.FILEBROWSER_EXTRA_FOLDER_PATH, todayFolderPath.getPath());
                             intent.putExtra(Constants.FILEBROWSER_EXTRA_EXTENSION, eExportMode.getExtension());

                             resultBrowseFile.launch(intent);

                         } else if (id == R.id.action_settings) {
                             Intent intent = new Intent(EntryChoiceActivity.this, SettingsActivity.class);
                             resultSettings.launch(intent);
                         }
                         return false;
                     }
                 });
                 popup.show();
             }
         });

        resultCapture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    updateCards();
        });

        setCardEnabled(binding.cardStartCapture, false);
        binding.cardStartCapture.setOnClickListener(v -> {
            Intent mainIntent = new Intent(this, CameraXLivePreviewActivity.class);
            mainIntent.putExtra(Constants.CAPTURE_FILE_PATH, sessionFilePathString);
            resultCapture.launch(mainIntent);
        });

        setCardEnabled(binding.cardViewSessionData, false);
        binding.cardViewSessionData.setOnClickListener(v ->{
            File sessionFile = new File(sessionFilePathString);
            if(sessionFile.exists() == false)
            {
                Toast.makeText(this, getString(R.string.error_file_does_not_exist, sessionFilePathString), Toast.LENGTH_LONG).show();
                return;
            }
            if(sessionFile.length() <= 0)
            {
                Toast.makeText(this, getString(R.string.error_file_is_empty, sessionFilePathString), Toast.LENGTH_LONG).show();
                return;
            }
            Intent mainIntent = new Intent(this, SessionViewerActivity.class);
            mainIntent.putExtra(Constants.CAPTURE_FILE_PATH, sessionFilePathString);
            startActivity(mainIntent);
        });

        binding.cardLoadLastSession.setOnClickListener(v -> {
            sessionFilePathString = PreferencesHelper.getLastSelectedSession(this);
            if(sessionFilePathString != null)
            {
                File sessionFile = new File(sessionFilePathString);
                if(sessionFile.exists() == false){
                    Toast.makeText(this, getString(R.string.last_session_file_not_found), Toast.LENGTH_LONG).show();
                    updateCards();
                }
                else {
                    updateCards();
                    String fileExtension = FileUtil.getFileExtension(sessionFile);
                    eExportMode = EExportMode.fromExtension(fileExtension);
                    PreferencesHelper.saveCurrentExtension(this, fileExtension);
                }
            }
            else
            {
                Toast.makeText(this, getString(R.string.no_session_file_saved), Toast.LENGTH_LONG).show();
                updateCards();
            }
        });

        binding.cardCreateNewSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File todayFolder = FileUtil.getTodayFolder(true);
                try {
                    sessionFilePathString = FileUtil.createNewFileAndReturnFullPath(todayFolder, filePrefix, eExportMode);
                    if(sessionFilePathString != null)
                    {
                        updateCards();
                        PreferencesHelper.saveLastSessionFile(EntryChoiceActivity.this, sessionFilePathString);

                    }
                } catch (IOException e) {
                    Toast.makeText(EntryChoiceActivity.this, getString(R.string.error_creating_new_session, e.getMessage()), Toast.LENGTH_LONG).show();
                    updateCards();
                }
            }
        });

        resultSettings = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result ->{
                    if(result.getResultCode() == RESULT_OK) {
                        loadPreferences();
                        if (sessionFilePathString != null && sessionFilePathString.isEmpty() == false) {
                            updateCards();
                            String sessionExtension = FileUtil.getFileExtension(new File(sessionFilePathString));
                            EExportMode sessionMode = EExportMode.fromExtension(sessionExtension);
                            if (sessionMode.equals(eExportMode) == false) {
                                setCardEnabled(binding.cardStartCapture, false);
                                setCardEnabled(binding.cardViewSessionData, false);
                                binding.txtSession.setText(R.string.select_session_file);
                            }
                            String lastSessionFilePathString = PreferencesHelper.getLastSelectedSession(this);
                            String lastSessionExtension = FileUtil.getFileExtension(new File(lastSessionFilePathString));
                            if(lastSessionExtension.equals(eExportMode) == false)
                            {
                                setCardEnabled(binding.cardLoadLastSession, false);
                            }
                            else
                            {
                                setCardEnabled(binding.cardLoadLastSession, true);
                            }
                        }
                    }
                });

        resultBrowseFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if(result.getData() == null)
                        {
                            Toast.makeText(EntryChoiceActivity.this, getString(R.string.missing_filename), Toast.LENGTH_LONG).show();
                            return;
                        }
                        String resultData = result.getData().getStringExtra(Constants.FILEBROWSER_RESULT_FILEPATH);
                        sessionFilePathString = resultData;
                        updateCards();
                        PreferencesHelper.saveLastSessionFile(this, sessionFilePathString);
                    }
                    else
                    {
                        updateCards();
                    }
                }
        );
    }

    private void updateCards()
    {
        setCardEnabled(binding.cardCreateNewSession, true);
        setCardEnabled(binding.cardLoadLastSession, true);
        if(sessionFilePathString == null || sessionFilePathString.isEmpty())
        {
            setCardEnabled(binding.cardViewSessionData, false);
            setCardEnabled(binding.cardStartCapture, false);
            binding.txtSession.setText(R.string.select_session_file);
            return;
        }
        File sessionFile = new File(sessionFilePathString);
        if(sessionFile.exists()) {
            binding.txtSession.setText(sessionFilePathString);
            setCardEnabled(binding.cardStartCapture, true);
            setCardEnabled(binding.cardViewSessionData, sessionFile.length() > 0);
        }
        else {
            binding.txtSession.setText(R.string.select_session_file);
            setCardEnabled(binding.cardStartCapture, false);
            setCardEnabled(binding.cardViewSessionData, false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkCameraPermission();

        // Initializing the AI Vision SDK
        try {
            boolean isInitDone = AIVisionSDK.getInstance(this.getApplicationContext()).init();
            LogUtils.i(TAG, "AI Vision SDK Init = " + isInitDone);
            LogUtils.i(TAG, "AI Vision SDK version = " + AIVisionSDK.getInstance(this.getApplicationContext()).getSDKVersion());
        } catch (UnsupportedOperationException ex) {
            runOnUiThread(() -> showErrorDialog(ex.getMessage()));
        }

        // Register the BroadcastReceiver to listen for managed configuration changes
        IntentFilter filter = new IntentFilter(ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES);
        registerReceiver(reloadPreferencesReceiver, filter, RECEIVER_NOT_EXPORTED);
        LogUtils.d(TAG, "Registered BroadcastReceiver for managed configuration changes");
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Unregister the BroadcastReceiver
        try {
            unregisterReceiver(reloadPreferencesReceiver);
            LogUtils.d(TAG, "Unregistered BroadcastReceiver for managed configuration changes");
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
            LogUtils.d(TAG, "BroadcastReceiver was not registered, ignoring unregister attempt");
        }
    }

    private void loadPreferences()
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Retrieve the stored integer value, with a default value of 0 if not found
        filePrefix = sharedPreferences.getString(SHARED_PREFERENCES_PREFIX, FILE_DEFAULT_PREFIX);
        String extension = sharedPreferences.getString(SHARED_PREFERENCES_EXTENSION, FILE_DEFAULT_EXTENSION);
        eExportMode = EExportMode.fromExtension(extension);
    }

    /**
     * Displays an error dialog with a specified message.
     *
     * @param message The message to display in the dialog.
     */
    private void showErrorDialog(String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(
                "Ok",
                (dialog, id) -> {
                    dialog.cancel();
                    EntryChoiceActivity.this.finish();
                });
        builder.create().show();
    }

    /**
     * Checks for camera permission and requests it if not granted.
     */
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show an explanation to the user why the permission is needed
                showNonCancellablePermissionRationaleDialog();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            LogUtils.v(TAG, "Camera permission granted");
        }
    }

    /**
     * Displays a dialog explaining why camera permission is needed and provides options to open settings or exit.
     */
    private void showNonCancellablePermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Camera Permission Needed")
                .setMessage("This app needs the Camera permission to function. Please grant the permission in the app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Exit", (dialog, which) -> {
                    // Exit the app or disable the functionality
                    finish();
                })
                .setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Opens the app settings in the device settings menu.
     */
    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Helper method to enable/disable card views with visual feedback
     * @param cardView The card layout to enable/disable
     * @param enabled Whether the card should be enabled
     */
    private void setCardEnabled(View cardView, boolean enabled) {
        cardView.setEnabled(enabled);
        cardView.setClickable(enabled);
        cardView.setFocusable(enabled);
        cardView.setAlpha(enabled ? 1.0f : 0.5f);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtils.v(TAG, "Camera permission granted");
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show();
                showNonCancellablePermissionRationaleDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
