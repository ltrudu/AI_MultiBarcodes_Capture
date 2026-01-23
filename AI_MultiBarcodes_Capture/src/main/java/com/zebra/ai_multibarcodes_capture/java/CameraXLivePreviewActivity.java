// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.java;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.google.common.util.concurrent.ListenableFuture;
import com.zebra.ai.vision.entity.BarcodeEntity;

import com.zebra.ai_multibarcodes_capture.CameraXViewModel;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.barcodedecoder.BarcodeAnalyzer;
import com.zebra.ai_multibarcodes_capture.barcodedecoder.BarcodeGraphic;
import com.zebra.ai_multibarcodes_capture.barcodedecoder.BarcodeHandler;
import com.zebra.ai_multibarcodes_capture.databinding.ActivityCameraXlivePreviewBinding;
import com.zebra.ai_multibarcodes_capture.helpers.CachedBarcode;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.CoordinateMapper;
import com.zebra.ai_multibarcodes_capture.helpers.DebounceManager;
import com.zebra.ai_multibarcodes_capture.helpers.ECameraResolution;
import com.zebra.ai_multibarcodes_capture.helpers.BaseActivity;
import com.zebra.ai_multibarcodes_capture.helpers.ECaptureTriggerMode;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.helpers.PreferencesHelper;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;
import com.zebra.ai_multibarcodes_capture.helpers.camera.CameraResolutionProviderFactory;
import com.zebra.ai_multibarcodes_capture.helpers.camera.DynamicCameraResolutionProvider;
import com.zebra.ai_multibarcodes_capture.helpers.camera.ICameraResolutionProvider;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;

import com.zebra.ai_multibarcodes_capture.settings.SettingsActivity;
import com.zebra.ai_multibarcodes_capture.views.CaptureZoneOverlay;
import com.zebra.ai_multibarcodes_capture.autocapture.AutoCaptureEvaluator;
import com.zebra.ai_multibarcodes_capture.autocapture.AutoCapturePreferencesHelper;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.filtering.FilteringEvaluator;
import com.zebra.ai_multibarcodes_capture.filtering.FilteringPreferencesHelper;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;
import com.zebra.datawedgeprofileintents.DWProfileBaseSettings;
import com.zebra.datawedgeprofileintents.DWProfileCommandBase;
import com.zebra.datawedgeprofileintents.DWScannerPluginDisable;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The CameraXLivePreviewActivity class is an Android activity that demonstrates the use of CameraX
 * for live camera preview and integrates multiple detection and recognition capabilities, including
 * barcode detection, text OCR, product recognition, and entity tracking.

 * This class provides functionality for switching between different models and use cases, handling
 * camera lifecycle, managing image analysis, and updating the UI based on detection results.

 * Usage:
 * - This activity is started as part of the application to demonstrate CameraX functionalities.
 * - It binds and unbinds camera use cases based on user selection and manages their lifecycle.
 * - It provides a spinner UI to select between different detection models and dynamically adapts
 *   the camera preview and analysis based on the selected model.

 * Dependencies:
 * - CameraX: Provides camera lifecycle and use case management.
 * - BarcodeHandler, OCRHandler, ProductRecognitionHandler, BarcodeTracker, EntityBarcodeTracker:
 *   Classes that handle specific detection and recognition tasks.
 * - ActivityCameraXlivePreviewBinding: Used for view binding to access UI components.
 * - GraphicOverlay: Custom view for rendering graphical overlays on camera preview.
 * - ExecutorService: Used for asynchronous task execution.

 * Exception Handling:
 * - Handles exceptions during analyzer setup and model disposal.

 * Note: Ensure that the appropriate permissions are configured in the AndroidManifest to utilize camera capabilities.
 */
public class CameraXLivePreviewActivity extends BaseActivity implements BarcodeAnalyzer.DetectionCallback, BarcodeAnalyzer.AnalysisTimingCallback {

    private ActivityCameraXlivePreviewBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // BroadcastReceiver to listen for managed configuration reload requests
    private BroadcastReceiver reloadPreferencesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES.equals(intent.getAction())) {
                LogUtils.d(TAG, "Received reload preferences request from ManagedConfigurationReceiver");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraXLivePreviewActivity.this, 
                            getString(R.string.managed_configuration_updated), 
                            Toast.LENGTH_LONG).show();
                        
                        // Restart the activity to apply new settings
                        Intent restartIntent = getIntent();
                        finish();
                        startActivity(restartIntent);
                    }
                });
            }
        }
    };
    
    
    private static final String BARCODE_DETECTION = "Barcode";
    @Nullable
    private Camera camera;
    @Nullable
    private Preview previewUseCase;
    private ImageAnalysis analysisUseCase;
    ProcessCameraProvider cameraProvider;
    private int imageWidth;
    private int imageHeight;
    // Raw sensor dimensions (before rotation adjustment)
    private int rawSensorWidth;
    private int rawSensorHeight;
    private ICameraResolutionProvider cameraResolutionProvider;
    private CameraSelector cameraSelector;
    private ResolutionSelector resolutionSelector;
    private final ExecutorService executors = Executors.newFixedThreadPool(3);

    private Button captureButton;
    private Button closeButton;
    private ImageView captureZoneToggleIcon;
    private ImageView flashlightToggleIcon;
    private CaptureZoneOverlay captureZoneOverlay;
    private boolean isFlashlightOn = false;
    private ECaptureTriggerMode captureTriggerMode = ECaptureTriggerMode.CAPTURE_ON_PRESS;

    // Filtering settings
    private boolean isFilteringEnabled = false;
    private FilteringConditionList filteringConditions = new FilteringConditionList();

    // Analysis timing overlay
    private TextView analysisOverlay;
    private boolean displayAnalysisPerSecond = false;

    // Force continuous autofocus setting
    private boolean forceContinuousAutofocus = false;

    // Debounce manager
    private final DebounceManager debounceManager = new DebounceManager();

    // Coordinate mapper
    private final CoordinateMapper coordinateMapper = new CoordinateMapper();

    // Auto capture settings
    private boolean isAutoCaptureEnabled = false;
    private AutoCaptureConditionList autoCaptureConditions = null;

    // High-res stabilization settings
    private ImageCapture imageCaptureUseCase;
    private boolean isHighResCapturing = false;
    private long lastHighResCaptureTime = 0;
    private static final long HIGH_RES_CAPTURE_COOLDOWN_MS = 500;
    private boolean isHighResStabilizationEnabled = false;
    private int highResStabilityThreshold = 3;  // Trigger after N unstable frames
    private int consecutiveUnstableFrames = 0;

    private BarcodeHandler barcodeHandler;

    private String selectedModel = BARCODE_DETECTION;

    private Size selectedSize;

    private int initialRotation = Surface.ROTATION_0;

    List<BarcodeEntity> entitiesHolder;

    private String captureFilePath;
    private String endpointUri;
    private boolean isHttpsPostMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeHelpers.applyTheme(this);

        // Enable immersive full-screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        Intent intent = getIntent();
        captureFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);
        endpointUri = intent.getStringExtra(Constants.ENDPOINT_URI);

        // Determine the mode based on which extra is provided
        isHttpsPostMode = (endpointUri != null && !endpointUri.isEmpty());

        LogUtils.d(TAG, "Camera mode: " + (isHttpsPostMode ? "HTTPS Post" : "File") +
                   ", FilePath: " + captureFilePath + ", Endpoint: " + endpointUri);

        // Initialize camera resolution provider and selected size
        loadCameraSettings();

        binding = ActivityCameraXlivePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ThemeHelpers.applyCustomFont(this);

        resolutionSelector = new ResolutionSelector.Builder()
                .setAspectRatioStrategy(
                        new AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO)
                )
                .setResolutionStrategy(
                        new ResolutionStrategy(selectedSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                ).build();

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            LogUtils.v(TAG, "Binding all camera");

                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            LogUtils.d(TAG, "Orientation locked to portrait for " + selectedModel + " mode");

                                Display display = getWindowManager().getDefaultDisplay();
                                initialRotation = display != null ? display.getRotation() : Surface.ROTATION_0;
                                // Store raw sensor dimensions (always the same regardless of rotation)
                                rawSensorWidth = selectedSize.getWidth();
                                rawSensorHeight = selectedSize.getHeight();
                                if (initialRotation == 0 || initialRotation == 2) {
                                    imageWidth = selectedSize.getHeight();
                                    imageHeight = selectedSize.getWidth();
                                } else {
                                    imageWidth = selectedSize.getWidth();
                                    imageHeight = selectedSize.getHeight();
                                }
                                // Update coordinate mapper with dimensions
                                coordinateMapper.setRawSensorDimensions(rawSensorWidth, rawSensorHeight);
                                coordinateMapper.setImageDimensions(imageWidth, imageHeight);
                                coordinateMapper.setInitialRotation(initialRotation);
                                LogUtils.d(TAG, "Updated imageWidth=" + imageWidth + ", imageHeight=" + imageHeight + ", rawSensor=" + rawSensorWidth + "x" + rawSensorHeight);


                            bindAllCameraUseCases();
                        });

        binding.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureData();
            }
        });

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        captureZoneToggleIcon = findViewById(R.id.capture_zone_toggle_icon);
        captureZoneToggleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCaptureZone();
            }
        });

        flashlightToggleIcon = findViewById(R.id.flashlight_toggle_icon);
        flashlightToggleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlashlight();
            }
        });

        analysisOverlay = findViewById(R.id.analysis_overlay);

        initCaptureZone();
    }

    private void initCaptureZone() {
        captureZoneOverlay = findViewById(R.id.capture_zone_overlay);
        captureZoneOverlay.setCaptureZoneListener(new CaptureZoneOverlay.CaptureZoneListener() {
            @Override
            public void onCaptureZoneChanged(RectF captureZone) {
                LogUtils.d(TAG, "Capture zone changed: " + captureZone.toString());

                int x = (int) captureZone.left;
                int y = (int) captureZone.top;
                int width = (int) captureZone.width();
                int height = (int) captureZone.height();

                // Save capture zone dimensions to SharedPreferences
                PreferencesHelper.saveCaptureZonePosition(CameraXLivePreviewActivity.this, x, y, width, height);

                LogUtils.d(TAG, "Capture zone dimensions saved to preferences: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);

                // Update analyzer crop region when capture zone changes
                updateAnalyzerCropRegion();
            }
        });

        LogUtils.d(TAG, "Capture zone overlay initialized");
    }

    /**
     * Converts the capture zone from overlay coordinates to RAW SENSOR image coordinates.
     * This is needed because we crop the raw ImageProxy from CameraX.
     *
     * The transformation chain is:
     * 1. Overlay coords -> remove scale/offset -> "effective" rotated image coords
     * 2. Effective coords -> reverse rotation -> raw sensor coords
     *
     * @param overlayRect The capture zone rectangle in overlay coordinates
     * @return The capture zone rectangle in raw sensor coordinates, or null if conversion fails
     */
    private Rect mapOverlayToRawSensorCoordinates(RectF overlayRect) {
        if (overlayRect == null || binding == null) {
            return null;
        }

        // Update overlay dimensions before mapping
        int overlayWidth = binding.graphicOverlay.getWidth();
        int overlayHeight = binding.graphicOverlay.getHeight();
        coordinateMapper.setOverlayDimensions(overlayWidth, overlayHeight);

        return coordinateMapper.mapOverlayToRawSensorCoordinates(overlayRect);
    }

    /**
     * Updates the analyzer's crop region based on the current capture zone state.
     * Should be called when:
     * - Capture zone visibility changes
     * - Capture zone position/size changes
     * - Camera is bound/rebound
     */
    private void updateAnalyzerCropRegion() {
        if (barcodeHandler == null || barcodeHandler.getBarcodeAnalyzer() == null) {
            LogUtils.d(TAG, "Cannot update crop region: analyzer not ready");
            return;
        }

        BarcodeAnalyzer analyzer = barcodeHandler.getBarcodeAnalyzer();

        if (captureZoneOverlay != null && captureZoneOverlay.isVisible()) {
            RectF captureZone = captureZoneOverlay.getCaptureZone();
            if (captureZone != null && !captureZone.isEmpty()) {
                // Convert overlay coordinates to raw sensor coordinates for cropping
                Rect rawSensorCropRegion = mapOverlayToRawSensorCoordinates(captureZone);
                if (rawSensorCropRegion != null && rawSensorCropRegion.width() > 0 && rawSensorCropRegion.height() > 0) {
                    analyzer.setCropRegion(rawSensorCropRegion, rawSensorWidth, rawSensorHeight);
                    LogUtils.d(TAG, "Analyzer crop region updated to raw sensor coords: " + rawSensorCropRegion);
                    return;
                }
            }
        }

        // Capture zone is disabled or invalid - clear the crop region
        analyzer.setCropRegion(null);
        LogUtils.d(TAG, "Analyzer crop region cleared");
    }

    private void loadCaptureZoneSettings() {
        // Load enabled state
        boolean isEnabled = PreferencesHelper.isCaptureZoneEnabled(this);

        // Load dimensions if they exist
        int x = PreferencesHelper.getCaptureZoneX(this);
        int y = PreferencesHelper.getCaptureZoneY(this);
        int width = PreferencesHelper.getCaptureZoneWidth(this);
        int height = PreferencesHelper.getCaptureZoneHeight(this);

        LogUtils.d(TAG, "Loading capture zone settings - enabled: " + isEnabled + ", x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);

        // Set visibility and icon state immediately
        captureZoneOverlay.setVisible(isEnabled);
        if (captureZoneToggleIcon != null) {
            if (isEnabled) {
                captureZoneToggleIcon.setImageResource(R.drawable.capture_zone_icon);
            } else {
                captureZoneToggleIcon.setImageResource(R.drawable.capture_zone_icon_disabled);
            }
        }

        // Store saved dimensions for later restoration
        if (x != -1 && y != -1 && width != -1 && height != -1) {
            captureZoneOverlay.setPendingDimensions(x, y, width, height);
            LogUtils.d(TAG, "Stored pending dimensions: " + x + "," + y + "," + width + "x" + height);
        }

        // Schedule crop region update after analyzer is ready
        // This will be called from bindAnalysisUseCase after initialization completes
        LogUtils.d(TAG, "Capture zone settings loaded");
    }
    
    private void loadFlashlightSettings() {
        LogUtils.d(TAG, "=== loadFlashlightSettings() START ===");

        // Load flashlight enabled state
        boolean wasFlashlightEnabled = PreferencesHelper.isFlashlightEnabled(this);

        LogUtils.d(TAG, "Loaded flashlight state from preferences: " + wasFlashlightEnabled);
        LogUtils.d(TAG, "Current camera state: " + (camera != null ? "available" : "null"));
        LogUtils.d(TAG, "Camera has flash unit: " + (camera != null && camera.getCameraInfo().hasFlashUnit()));
        LogUtils.d(TAG, "Current isFlashlightOn field: " + isFlashlightOn);
        LogUtils.d(TAG, "flashlightToggleIcon: " + (flashlightToggleIcon != null ? "available" : "null"));

        if (wasFlashlightEnabled && camera != null && camera.getCameraInfo().hasFlashUnit()) {
            LogUtils.d(TAG, "Conditions met - restoring flashlight to ON state");
            isFlashlightOn = true;

            try {
                camera.getCameraControl().enableTorch(true);
                LogUtils.d(TAG, "Camera torch enabled successfully");
            } catch (Exception e) {
                LogUtils.e(TAG, "Failed to enable camera torch", e);
            }

            // Update icon to reflect restored state
            if (flashlightToggleIcon != null) {
                flashlightToggleIcon.setImageResource(R.drawable.flashlight_on_icon);
                LogUtils.d(TAG, "Updated icon to flashlight_on_icon");
            } else {
                LogUtils.w(TAG, "Cannot update icon - flashlightToggleIcon is null");
            }

            LogUtils.d(TAG, "Flashlight restored to ON state");
        } else {
            LogUtils.d(TAG, "Conditions not met - setting flashlight to OFF state");
            LogUtils.d(TAG, "Reason: wasEnabled=" + wasFlashlightEnabled +
                      ", camera=" + (camera != null) +
                      ", hasFlash=" + (camera != null && camera.getCameraInfo().hasFlashUnit()));

            isFlashlightOn = false;

            if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
                try {
                    camera.getCameraControl().enableTorch(false);
                    LogUtils.d(TAG, "Camera torch disabled successfully");
                } catch (Exception e) {
                    LogUtils.e(TAG, "Failed to disable camera torch", e);
                }
            }

            // Update icon to reflect off state
            if (flashlightToggleIcon != null) {
                flashlightToggleIcon.setImageResource(R.drawable.flashlight_off_icon);
                LogUtils.d(TAG, "Updated icon to flashlight_off_icon");
            } else {
                LogUtils.w(TAG, "Cannot update icon - flashlightToggleIcon is null");
            }

            LogUtils.d(TAG, "Flashlight set to OFF state");
        }

        LogUtils.d(TAG, "Final isFlashlightOn field value: " + isFlashlightOn);
        LogUtils.d(TAG, "=== loadFlashlightSettings() END ===");
    }

    private void loadFilteringSettings() {
        LogUtils.d(TAG, "=== loadFilteringSettings() START ===");

        // Load filtering enabled state using FilteringPreferencesHelper
        isFilteringEnabled = FilteringPreferencesHelper.isFilteringEnabled(this);

        // Load filtering conditions using FilteringPreferencesHelper
        filteringConditions = FilteringPreferencesHelper.loadConditions(this);

        LogUtils.d(TAG, "Loaded filtering settings - enabled: " + isFilteringEnabled + ", conditions count: " + filteringConditions.size());
        LogUtils.d(TAG, "=== loadFilteringSettings() END ===");
    }

    private void loadCaptureModeSettings() {
        LogUtils.d(TAG, "=== loadCaptureModeSettings() START ===");

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load capture trigger mode
        String captureTriggerModeKey = sharedPreferences.getString(Constants.SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE, Constants.SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE_DEFAULT);
        captureTriggerMode = ECaptureTriggerMode.fromKey(captureTriggerModeKey);

        LogUtils.d(TAG, "Loaded capture trigger mode: " + captureTriggerMode.toString() + " (" + captureTriggerMode.getDisplayName(this) + ")");
        LogUtils.d(TAG, "=== loadCaptureModeSettings() END ===");
    }

    private void loadDisplayAnalysisSettings() {
        LogUtils.d(TAG, "=== loadDisplayAnalysisSettings() START ===");

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load display analysis per second setting
        displayAnalysisPerSecond = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND, Constants.SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND_DEFAULT);

        // Update overlay visibility
        if (analysisOverlay != null) {
            analysisOverlay.setVisibility(displayAnalysisPerSecond ? View.VISIBLE : View.GONE);
        }

        // Update analyzer timing callback
        updateAnalyzerTimingCallback();

        LogUtils.d(TAG, "Display analysis per second: " + displayAnalysisPerSecond);
        LogUtils.d(TAG, "=== loadDisplayAnalysisSettings() END ===");
    }

    private void loadForceContinuousAutofocusSettings() {
        LogUtils.d(TAG, "=== loadForceContinuousAutofocusSettings() START ===");

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load force continuous autofocus setting
        forceContinuousAutofocus = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS, Constants.SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS_DEFAULT);

        LogUtils.d(TAG, "Force continuous autofocus: " + forceContinuousAutofocus);
        LogUtils.d(TAG, "=== loadForceContinuousAutofocusSettings() END ===");
    }

    private void loadDebounceSettings() {
        LogUtils.d(TAG, "=== loadDebounceSettings() START ===");

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load debounce settings
        boolean isDebounceEnabled = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_DEBOUNCE_ENABLED, Constants.SHARED_PREFERENCES_DEBOUNCE_ENABLED_DEFAULT);
        int debounceMaxFrames = sharedPreferences.getInt(Constants.SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES, Constants.SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES_DEFAULT);
        int debounceThreshold = sharedPreferences.getInt(Constants.SHARED_PREFERENCES_DEBOUNCE_THRESHOLD, Constants.SHARED_PREFERENCES_DEBOUNCE_THRESHOLD_DEFAULT);
        int debounceAlgorithm = sharedPreferences.getInt(Constants.SHARED_PREFERENCES_DEBOUNCE_ALGORITHM, Constants.SHARED_PREFERENCES_DEBOUNCE_ALGORITHM_DEFAULT);
        int iouThresholdInt = sharedPreferences.getInt(Constants.SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD, Constants.SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD_DEFAULT);
        float debounceIouThreshold = iouThresholdInt / 100.0f;

        // Update debounce manager with new settings
        debounceManager.updateSettings(isDebounceEnabled, debounceMaxFrames, debounceThreshold, debounceAlgorithm, debounceIouThreshold);

        LogUtils.d(TAG, "=== loadDebounceSettings() END ===");
    }

    private void loadAutoCaptureSettings() {
        isAutoCaptureEnabled = AutoCapturePreferencesHelper.isAutoCaptureEnabled(this);
        autoCaptureConditions = AutoCapturePreferencesHelper.loadConditions(this);
        LogUtils.d(TAG, "Auto capture enabled: " + isAutoCaptureEnabled + ", conditions: " + (autoCaptureConditions != null ? autoCaptureConditions.size() : 0));
    }

    private void loadHighResStabilizationSettings() {
        LogUtils.d(TAG, "=== loadHighResStabilizationSettings() START ===");

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        isHighResStabilizationEnabled = sharedPreferences.getBoolean(
            Constants.SHARED_PREFERENCES_HIGH_RES_STABILIZATION_ENABLED,
            Constants.SHARED_PREFERENCES_HIGH_RES_STABILIZATION_ENABLED_DEFAULT);

        highResStabilityThreshold = sharedPreferences.getInt(
            Constants.SHARED_PREFERENCES_HIGH_RES_STABILITY_THRESHOLD,
            Constants.SHARED_PREFERENCES_HIGH_RES_STABILITY_THRESHOLD_DEFAULT);

        // Reset counters when settings are reloaded
        consecutiveUnstableFrames = 0;
        isHighResCapturing = false;

        LogUtils.d(TAG, "High-res stabilization enabled: " + isHighResStabilizationEnabled +
            ", threshold: " + highResStabilityThreshold);
        LogUtils.d(TAG, "=== loadHighResStabilizationSettings() END ===");
    }

    /**
     * Shows a brief white border flash to indicate high-res capture is being taken.
     */
    private void showHighResFlash() {
        runOnUiThread(() -> {
            View flashOverlay = binding.highResFlashOverlay;
            if (flashOverlay == null) {
                LogUtils.w(TAG, "HighRes: Flash overlay view not found");
                return;
            }

            // Create a flash animation: fade in quickly, then fade out
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(100); // 100ms fade in

            AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(200); // 200ms fade out
            fadeOut.setStartOffset(100); // Start after fade in

            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    flashOverlay.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    flashOverlay.startAnimation(fadeOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    flashOverlay.setAlpha(0f);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            flashOverlay.startAnimation(fadeIn);
            LogUtils.d(TAG, "HighRes: Flash animation started");
        });
    }

    /**
     * Sets up the ImageCapture use case for high-res stabilization.
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void setupImageCapture() {
        if (!isHighResStabilizationEnabled) {
            imageCaptureUseCase = null;
            return;
        }

        ImageCapture.Builder builder = new ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation());

        // Use highest available resolution for ImageCapture
        ResolutionSelector highResSelector = new ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .build();
        builder.setResolutionSelector(highResSelector);

        imageCaptureUseCase = builder.build();
        LogUtils.d(TAG, "ImageCapture use case configured for high-res stabilization");
    }

    /**
     * Checks for instability in the debounce cache and triggers high-res capture if needed.
     */
    private void checkAndTriggerHighResCapture() {
        if (isHighResCapturing) {
            LogUtils.v(TAG, "HighRes: Skipping check - capture already in progress");
            return;
        }

        // Check cooldown
        long now = System.currentTimeMillis();
        long timeSinceLastCapture = now - lastHighResCaptureTime;
        if (timeSinceLastCapture < HIGH_RES_CAPTURE_COOLDOWN_MS) {
            LogUtils.v(TAG, "HighRes: Skipping check - cooldown active (" + timeSinceLastCapture + "ms < " + HIGH_RES_CAPTURE_COOLDOWN_MS + "ms)");
            return;
        }

        List<CachedBarcode> unstableBarcodes = new ArrayList<>();

        for (CachedBarcode cached : debounceManager.getCache()) {
            // No decoded value OR low stability
            boolean hasValue = cached.hasDecodedValue();
            float stability = cached.getStabilityScore();
            if (!hasValue || stability < 0.5f) {
                unstableBarcodes.add(cached);
                LogUtils.d(TAG, "HighRes: Unstable barcode detected - hasValue=" + hasValue +
                    ", stability=" + String.format("%.2f", stability) +
                    ", value='" + cached.getLastValue() + "'" +
                    ", consistentFrames=" + cached.getConsistentValueCount() +
                    ", valueChanges=" + cached.getValueChangeCount());
            }
        }

        if (!unstableBarcodes.isEmpty()) {
            consecutiveUnstableFrames++;
            LogUtils.d(TAG, "HighRes: " + unstableBarcodes.size() + " unstable barcodes, consecutiveFrames=" +
                consecutiveUnstableFrames + "/" + highResStabilityThreshold);
            if (consecutiveUnstableFrames >= highResStabilityThreshold) {
                LogUtils.i(TAG, "HighRes: *** TRIGGERING HIGH-RES CAPTURE *** (threshold reached)");
                triggerHighResCapture(unstableBarcodes);
            }
        } else {
            if (consecutiveUnstableFrames > 0) {
                LogUtils.d(TAG, "HighRes: All barcodes stable, resetting counter (was " + consecutiveUnstableFrames + ")");
            }
            consecutiveUnstableFrames = 0;
        }
    }

    /**
     * Triggers a high-resolution capture to validate unstable barcodes.
     *
     * @param targetBarcodes The list of unstable barcodes to validate
     */
    private void triggerHighResCapture(List<CachedBarcode> targetBarcodes) {
        if (imageCaptureUseCase == null) {
            LogUtils.w(TAG, "HighRes: Cannot capture - ImageCapture use case is null");
            return;
        }
        if (isHighResCapturing) {
            LogUtils.w(TAG, "HighRes: Cannot capture - already capturing");
            return;
        }

        isHighResCapturing = true;
        lastHighResCaptureTime = System.currentTimeMillis();
        consecutiveUnstableFrames = 0;

        LogUtils.i(TAG, "HighRes: ========================================");
        LogUtils.i(TAG, "HighRes: STARTING HIGH-RES CAPTURE");
        LogUtils.i(TAG, "HighRes: Target barcodes: " + targetBarcodes.size());
        for (int i = 0; i < targetBarcodes.size(); i++) {
            CachedBarcode cb = targetBarcodes.get(i);
            LogUtils.i(TAG, "HighRes:   [" + i + "] value='" + cb.getLastValue() +
                "', stability=" + String.format("%.2f", cb.getStabilityScore()) +
                ", rect=" + cb.getOverlayRect());
        }
        LogUtils.i(TAG, "HighRes: ========================================");

        // Show visual feedback
        showHighResFlash();

        final long captureStartTime = System.currentTimeMillis();

        imageCaptureUseCase.takePicture(executors,
            new ImageCapture.OnImageCapturedCallback() {
                @Override
                public void onCaptureSuccess(@NonNull ImageProxy image) {
                    long captureTime = System.currentTimeMillis() - captureStartTime;
                    int rotationDegrees = image.getImageInfo().getRotationDegrees();
                    LogUtils.i(TAG, "HighRes: *** CAPTURE SUCCESS ***");
                    LogUtils.i(TAG, "HighRes: Resolution: " + image.getWidth() + "x" + image.getHeight());
                    LogUtils.i(TAG, "HighRes: Format: " + image.getFormat());
                    LogUtils.i(TAG, "HighRes: Rotation degrees: " + rotationDegrees);
                    LogUtils.i(TAG, "HighRes: Capture time: " + captureTime + "ms");
                    LogUtils.i(TAG, "HighRes: Compare with Analysis image: " + imageWidth + "x" + imageHeight);
                    LogUtils.i(TAG, "HighRes: rawSensorWidth=" + rawSensorWidth + ", rawSensorHeight=" + rawSensorHeight);

                    processHighResForStabilization(image, targetBarcodes, rotationDegrees);
                    image.close();
                    runOnUiThread(() -> isHighResCapturing = false);
                }

                @Override
                public void onError(@NonNull ImageCaptureException e) {
                    long captureTime = System.currentTimeMillis() - captureStartTime;
                    LogUtils.e(TAG, "HighRes: *** CAPTURE FAILED ***");
                    LogUtils.e(TAG, "HighRes: Error: " + e.getMessage());
                    LogUtils.e(TAG, "HighRes: Error code: " + e.getImageCaptureError());
                    LogUtils.e(TAG, "HighRes: Time until failure: " + captureTime + "ms");
                    runOnUiThread(() -> isHighResCapturing = false);
                }
            });
    }

    /**
     * Processes the high-res captured image to validate unstable barcodes.
     *
     * @param image The captured high-res image
     * @param targetBarcodes The list of unstable barcodes to validate
     * @param rotationDegrees The rotation degrees from the captured image
     */
    private void processHighResForStabilization(ImageProxy image,
            List<CachedBarcode> targetBarcodes, int rotationDegrees) {
        long processingStartTime = System.currentTimeMillis();
        LogUtils.i(TAG, "HighRes: Processing captured image with rotation=" + rotationDegrees);

        try {
            Bitmap bitmap = imageProxyToBitmap(image);
            if (bitmap == null) {
                LogUtils.e(TAG, "HighRes: Failed to convert ImageProxy to Bitmap");
                return;
            }
            LogUtils.d(TAG, "HighRes: Bitmap created: " + bitmap.getWidth() + "x" + bitmap.getHeight() +
                ", config=" + bitmap.getConfig());

            // Run AI Vision SDK detection on high-res bitmap
            // Pass the rotation so SDK returns bboxes in effective (rotated) space
            long decodeStartTime = System.currentTimeMillis();
            List<BarcodeEntity> highResResults = detectBarcodesInBitmap(bitmap, rotationDegrees);
            long decodeTime = System.currentTimeMillis() - decodeStartTime;

            LogUtils.i(TAG, "HighRes: ----------------------------------------");
            LogUtils.i(TAG, "HighRes: DECODE RESULTS");
            LogUtils.i(TAG, "HighRes: Decode time: " + decodeTime + "ms");
            LogUtils.i(TAG, "HighRes: Barcodes found: " + highResResults.size());

            if (highResResults.isEmpty()) {
                LogUtils.w(TAG, "HighRes: No barcodes decoded from high-res image!");
            } else {
                for (int i = 0; i < highResResults.size(); i++) {
                    BarcodeEntity be = highResResults.get(i);
                    LogUtils.i(TAG, "HighRes:   [" + i + "] value='" + be.getValue() +
                        "', symbology=" + be.getSymbology() +
                        ", bbox=" + be.getBoundingBox());
                }
            }
            LogUtils.i(TAG, "HighRes: ----------------------------------------");

            // Match results to cache entries by position
            int validatedCount = 0;
            int notMatchedCount = 0;
            for (CachedBarcode target : targetBarcodes) {
                LogUtils.d(TAG, "HighRes: Matching target: rect=" + target.getOverlayRect() +
                    ", currentValue='" + target.getLastValue() + "'");

                BarcodeEntity match = findMatchingBarcode(
                    target.getOverlayRect(), highResResults,
                    bitmap.getWidth(), bitmap.getHeight());

                if (match != null && match.getValue() != null && !match.getValue().isEmpty()) {
                    String oldValue = target.getLastValue();
                    target.setValidatedValue(match.getValue());
                    validatedCount++;
                    LogUtils.i(TAG, "HighRes: *** VALIDATED *** '" + oldValue + "' -> '" + match.getValue() + "'");
                } else {
                    notMatchedCount++;
                    LogUtils.d(TAG, "HighRes: No match found for target rect=" + target.getOverlayRect());
                }
            }

            long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
            LogUtils.i(TAG, "HighRes: ========================================");
            LogUtils.i(TAG, "HighRes: PROCESSING COMPLETE");
            LogUtils.i(TAG, "HighRes: Total processing time: " + totalProcessingTime + "ms");
            LogUtils.i(TAG, "HighRes: Validated: " + validatedCount + "/" + targetBarcodes.size());
            LogUtils.i(TAG, "HighRes: Not matched: " + notMatchedCount);
            LogUtils.i(TAG, "HighRes: ========================================");

            bitmap.recycle();
        } catch (Exception e) {
            LogUtils.e(TAG, "HighRes: Error in processing: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an ImageProxy to a Bitmap.
     *
     * @param image The ImageProxy to convert
     * @return The converted Bitmap, or null if conversion fails
     */
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        try {
            if (image.getFormat() == ImageFormat.JPEG) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } else if (image.getFormat() == ImageFormat.YUV_420_888) {
                // Convert YUV to JPEG first
                ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
                ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
                ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

                int ySize = yBuffer.remaining();
                int uSize = uBuffer.remaining();
                int vSize = vBuffer.remaining();

                byte[] nv21 = new byte[ySize + uSize + vSize];
                yBuffer.get(nv21, 0, ySize);
                vBuffer.get(nv21, ySize, vSize);
                uBuffer.get(nv21, ySize + vSize, uSize);

                YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21,
                    image.getWidth(), image.getHeight(), null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                yuvImage.compressToJpeg(new Rect(0, 0,
                    image.getWidth(), image.getHeight()), 90, out);

                byte[] imageBytes = out.toByteArray();
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
            LogUtils.w(TAG, "Unsupported image format: " + image.getFormat());
            return null;
        } catch (Exception e) {
            LogUtils.e(TAG, "Error converting ImageProxy to Bitmap: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Detects barcodes in a bitmap using the existing barcode handler.
     *
     * @param bitmap The bitmap to scan
     * @param rotationDegrees The rotation degrees to apply (so SDK returns bboxes in effective space)
     * @return List of detected barcode entities
     */
    private List<BarcodeEntity> detectBarcodesInBitmap(Bitmap bitmap, int rotationDegrees) {
        // Use the existing barcode handler to decode the bitmap
        if (barcodeHandler != null && barcodeHandler.isDecoderReady()) {
            try {
                // Pass the rotation so SDK transforms bboxes to effective (rotated) image space
                LogUtils.d(TAG, "HighRes: Decoding bitmap with rotation=" + rotationDegrees);
                return barcodeHandler.decodeBitmap(bitmap, rotationDegrees).get();
            } catch (Exception e) {
                LogUtils.e(TAG, "Error decoding bitmap: " + e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }

    /**
     * Finds a matching barcode from high-res results based on position.
     * Since we pass rotation to the SDK, bboxes are in effective (rotated) image space.
     * We use the same mapping logic as mapBoundingBoxToOverlay for consistency.
     *
     * @param targetRect The target bounding box in overlay coordinates
     * @param results The list of barcode entities from high-res capture
     * @param highResWidth The width of the high-res image (raw, before rotation)
     * @param highResHeight The height of the high-res image (raw, before rotation)
     * @return The matching barcode entity, or null if no match found
     */
    private BarcodeEntity findMatchingBarcode(Rect targetRect, List<BarcodeEntity> results,
            int highResWidth, int highResHeight) {
        // Get overlay dimensions
        int overlayWidth = binding.graphicOverlay.getWidth();
        int overlayHeight = binding.graphicOverlay.getHeight();

        if (overlayWidth == 0 || overlayHeight == 0) {
            LogUtils.w(TAG, "HighRes: Cannot match - overlay dimensions are 0");
            return null;
        }

        // After rotation, the effective high-res dimensions may be swapped
        // If original is landscape (w > h) and we rotate 90°, effective is portrait (h > w)
        // The SDK with rotation returns bboxes in effective space
        // For 90° or 270° rotation, swap width/height
        int effectiveHighResWidth = highResWidth;
        int effectiveHighResHeight = highResHeight;
        // Note: We don't know the exact rotation here, but we can infer from aspect ratios
        // If analysis is portrait and high-res raw is landscape, they rotated it
        if ((imageWidth < imageHeight) != (highResWidth < highResHeight)) {
            effectiveHighResWidth = highResHeight;
            effectiveHighResHeight = highResWidth;
        }

        LogUtils.i(TAG, "HighRes: ============ COORDINATE MAPPING DEBUG ============");
        LogUtils.i(TAG, "HighRes: Target (from preview): " + targetRect);
        LogUtils.i(TAG, "HighRes: Dimensions:");
        LogUtils.i(TAG, "HighRes:   - HighRes raw: " + highResWidth + "x" + highResHeight);
        LogUtils.i(TAG, "HighRes:   - HighRes effective: " + effectiveHighResWidth + "x" + effectiveHighResHeight);
        LogUtils.i(TAG, "HighRes:   - Analysis image: " + imageWidth + "x" + imageHeight);
        LogUtils.i(TAG, "HighRes:   - Overlay: " + overlayWidth + "x" + overlayHeight);

        // For each result, map its bounding box to overlay coordinates and compare
        BarcodeEntity best = null;
        double bestIOU = 0.3;  // Minimum IOU threshold

        for (BarcodeEntity result : results) {
            Rect bbox = result.getBoundingBox();
            if (bbox == null) continue;

            LogUtils.i(TAG, "HighRes: --- Barcode '" + result.getValue() + "' ---");
            LogUtils.i(TAG, "HighRes:   Bbox from SDK (effective space): " + bbox);

            // Map high-res effective bbox to overlay coordinates
            // Use the same scaling logic as mapBoundingBoxToOverlay
            float scaleX = (float) overlayWidth / effectiveHighResWidth;
            float scaleY = (float) overlayHeight / effectiveHighResHeight;
            float scale = Math.max(scaleX, scaleY);

            float offsetX = (overlayWidth - effectiveHighResWidth * scale) / 2f;
            float offsetY = (overlayHeight - effectiveHighResHeight * scale) / 2f;

            LogUtils.i(TAG, "HighRes:   Scale: " + String.format("%.4f", scale) +
                " (scaleX=" + String.format("%.4f", scaleX) +
                ", scaleY=" + String.format("%.4f", scaleY) + ")");
            LogUtils.i(TAG, "HighRes:   Offset: (" + String.format("%.1f", offsetX) +
                ", " + String.format("%.1f", offsetY) + ")");

            Rect scaledBbox = new Rect(
                (int) (bbox.left * scale + offsetX),
                (int) (bbox.top * scale + offsetY),
                (int) (bbox.right * scale + offsetX),
                (int) (bbox.bottom * scale + offsetY)
            );

            LogUtils.i(TAG, "HighRes:   Mapped to overlay: " + scaledBbox);
            LogUtils.i(TAG, "HighRes:   Target for comparison: " + targetRect);

            double iou = calculateIOU(targetRect, scaledBbox);
            LogUtils.i(TAG, "HighRes:   IOU = " + String.format("%.3f", iou) +
                (iou > bestIOU ? " [ABOVE THRESHOLD]" : " [BELOW THRESHOLD 0.3]"));

            if (iou > bestIOU) {
                bestIOU = iou;
                best = result;
            }
        }

        LogUtils.i(TAG, "HighRes: ============ END COORDINATE MAPPING ============");

        if (best != null) {
            LogUtils.i(TAG, "HighRes: Best match: '" + best.getValue() + "' with IOU=" + String.format("%.3f", bestIOU));
        } else {
            LogUtils.w(TAG, "HighRes: No match found (all IOUs below threshold 0.3)");
        }

        return best;
    }

    /**
     * Calculates Intersection over Union (IOU) between two rectangles.
     *
     * @param rect1 First rectangle
     * @param rect2 Second rectangle
     * @return IOU value between 0.0 and 1.0
     */
    private double calculateIOU(Rect rect1, Rect rect2) {
        int intersectLeft = Math.max(rect1.left, rect2.left);
        int intersectTop = Math.max(rect1.top, rect2.top);
        int intersectRight = Math.min(rect1.right, rect2.right);
        int intersectBottom = Math.min(rect1.bottom, rect2.bottom);

        int intersectWidth = Math.max(0, intersectRight - intersectLeft);
        int intersectHeight = Math.max(0, intersectBottom - intersectTop);
        int intersectionArea = intersectWidth * intersectHeight;

        int area1 = rect1.width() * rect1.height();
        int area2 = rect2.width() * rect2.height();
        int unionArea = area1 + area2 - intersectionArea;

        if (unionArea == 0) return 0.0;
        return (double) intersectionArea / unionArea;
    }

    /**
     * Load camera settings using the ICameraResolutionProvider strategy pattern.
     * Creates the appropriate provider (Static or Dynamic) based on user preferences.
     */
    private void loadCameraSettings() {
        LogUtils.d(TAG, "=== loadCameraSettings() START ===");

        // Create the appropriate provider based on saved preferences
        cameraResolutionProvider = CameraResolutionProviderFactory.create(this);

        LogUtils.d(TAG, "Camera resolution provider mode: " + cameraResolutionProvider.getMode());
        LogUtils.d(TAG, "Camera: " + cameraResolutionProvider.getCameraDisplayName(this));
        LogUtils.d(TAG, "Resolution: " + cameraResolutionProvider.getResolutionDisplayName(this));

        // Get the resolution from the provider
        selectedSize = cameraResolutionProvider.getResolution();
        LogUtils.d(TAG, "Selected size: " + selectedSize.getWidth() + "x" + selectedSize.getHeight());

        // Get the camera selector from the provider
        // For dynamic mode, we need to use the getCameraSelector(context) method
        if (cameraResolutionProvider instanceof DynamicCameraResolutionProvider) {
            cameraSelector = ((DynamicCameraResolutionProvider) cameraResolutionProvider).getCameraSelector(this);
        } else {
            cameraSelector = cameraResolutionProvider.getCameraSelector();
        }

        LogUtils.d(TAG, "=== loadCameraSettings() END ===");
    }

    /**
     * Queries the actual resolution used by ImageAnalysis after binding and updates
     * the imageWidth/imageHeight/rawSensor dimensions accordingly.
     *
     * This is critical because when binding multiple use cases (especially with ImageCapture),
     * CameraX may select a different resolution than requested. The actual resolution can be
     * limited to PREVIEW size when ImageCapture is bound.
     */
    private void updateActualImageDimensions() {
        if (analysisUseCase == null) {
            LogUtils.w(TAG, "Cannot update dimensions: analysisUseCase is null");
            return;
        }

        androidx.camera.core.ResolutionInfo resolutionInfo = analysisUseCase.getResolutionInfo();
        if (resolutionInfo == null) {
            LogUtils.w(TAG, "Cannot update dimensions: resolutionInfo is null");
            return;
        }

        android.util.Size actualResolution = resolutionInfo.getResolution();
        int rotationDegrees = resolutionInfo.getRotationDegrees();

        LogUtils.d(TAG, "=== updateActualImageDimensions() ===");
        LogUtils.d(TAG, "Requested resolution: " + selectedSize.getWidth() + "x" + selectedSize.getHeight());
        LogUtils.d(TAG, "Actual ImageAnalysis resolution: " + actualResolution.getWidth() + "x" + actualResolution.getHeight());
        LogUtils.d(TAG, "Rotation degrees from ResolutionInfo: " + rotationDegrees);

        // Update raw sensor dimensions with actual resolution
        rawSensorWidth = actualResolution.getWidth();
        rawSensorHeight = actualResolution.getHeight();

        // Update effective dimensions based on rotation
        // rotationDegrees tells us how much to rotate the image for correct display
        if (rotationDegrees == 90 || rotationDegrees == 270) {
            // Image needs 90° or 270° rotation - swap width/height for effective dimensions
            imageWidth = actualResolution.getHeight();
            imageHeight = actualResolution.getWidth();
        } else {
            // No rotation or 180° - keep dimensions as-is
            imageWidth = actualResolution.getWidth();
            imageHeight = actualResolution.getHeight();
        }

        // Update coordinate mapper with new dimensions
        coordinateMapper.setRawSensorDimensions(rawSensorWidth, rawSensorHeight);
        coordinateMapper.setImageDimensions(imageWidth, imageHeight);

        LogUtils.d(TAG, "Updated dimensions - rawSensor: " + rawSensorWidth + "x" + rawSensorHeight +
            ", effective: " + imageWidth + "x" + imageHeight);

        // Log if resolution differs from requested (this can happen due to device limitations)
        if (actualResolution.getWidth() != selectedSize.getWidth() ||
            actualResolution.getHeight() != selectedSize.getHeight()) {
            LogUtils.i(TAG, "Note: ImageAnalysis resolution differs from requested (" +
                selectedSize.getWidth() + "x" + selectedSize.getHeight() + " -> " +
                actualResolution.getWidth() + "x" + actualResolution.getHeight() + ")");
        }
    }

    private void updateAnalyzerTimingCallback() {
        if (barcodeHandler != null && barcodeHandler.getBarcodeAnalyzer() != null) {
            BarcodeAnalyzer analyzer = barcodeHandler.getBarcodeAnalyzer();
            if (displayAnalysisPerSecond) {
                analyzer.setTimingCallback(this);
                analyzer.setTimingEnabled(true);
                LogUtils.d(TAG, "Timing callback enabled on analyzer");
            } else {
                analyzer.setTimingEnabled(false);
                analyzer.setTimingCallback(null);
                LogUtils.d(TAG, "Timing callback disabled on analyzer");
            }
        }
    }

    @Override
    public void onAnalysisTiming(long analysisTimeMs, int analysisPerSecond) {
        // Update the overlay on UI thread
        runOnUiThread(() -> {
            if (analysisOverlay != null && displayAnalysisPerSecond) {
                String text = String.format(getString(R.string.analysis_overlay_format), analysisPerSecond, analysisTimeMs);
                analysisOverlay.setText(text);
            }
        });
    }

    /**
     * Determines if an entity should be included based on the filtering conditions.
     * Uses OR logic: entity matches if it satisfies ANY condition.
     * Returns true if filtering is disabled or no conditions are defined.
     *
     * @param entity The barcode entity to evaluate
     * @return true if the entity should be included, false otherwise
     */
    private boolean shouldIncludeEntity(BarcodeEntity entity)
    {
        if (!isFilteringEnabled) {
            // If filtering is disabled, include all entities
            return true;
        }

        // Use FilteringEvaluator with OR logic
        boolean shouldInclude = FilteringEvaluator.shouldIncludeEntity(entity, filteringConditions);
        LogUtils.v(TAG, "Filtering - Entity value: '" + entity.getValue() + "', symbology: " + entity.getSymbology() + ", shouldInclude: " + shouldInclude);
        return shouldInclude;
    }

    private boolean isBarcodeInCaptureZone(Rect overlayRect) {
        // If capture zone is not enabled or overlay is not available, allow all barcodes
        if (captureZoneOverlay == null || !captureZoneOverlay.isVisible()) {
            return true;
        }
        
        // Get the capture zone bounds in overlay coordinates
        RectF captureZone = captureZoneOverlay.getCaptureZone();
        if (captureZone == null) {
            return true; // Allow all barcodes if no capture zone is set
        }
        
        // Convert RectF to Rect for intersection check
        Rect captureZoneRect = new Rect(
            (int) captureZone.left,
            (int) captureZone.top,
            (int) captureZone.right,
            (int) captureZone.bottom
        );
        
        // Check if the barcode rectangle intersects with the capture zone
        boolean intersects = Rect.intersects(overlayRect, captureZoneRect);
        
        LogUtils.v(TAG, "Barcode rect: " + overlayRect + ", Capture zone: " + captureZoneRect + ", Intersects: " + intersects);
        return intersects;
    }

    private void toggleCaptureZone() {
        if (captureZoneOverlay != null && captureZoneToggleIcon != null) {
            boolean isCurrentlyVisible = captureZoneOverlay.isVisible();
            boolean newVisibility = !isCurrentlyVisible;
            captureZoneOverlay.setVisible(newVisibility);

            // Save the enabled state to SharedPreferences
            PreferencesHelper.saveCaptureZoneEnabled(this, newVisibility);

            // Update icon drawable to reflect capture zone visibility
            if (newVisibility) {
                // Capture zone is now enabled - show normal icon
                captureZoneToggleIcon.setImageResource(R.drawable.capture_zone_icon);
            } else {
                // Capture zone is now disabled - show disabled icon with forbidden sign
                captureZoneToggleIcon.setImageResource(R.drawable.capture_zone_icon_disabled);
            }

            // Update analyzer crop region based on new visibility state
            updateAnalyzerCropRegion();

            LogUtils.d(TAG, "Capture zone toggled to: " + newVisibility + ", saved to preferences");
        }
    }

    private void toggleFlashlight() {
        LogUtils.d(TAG, "=== toggleFlashlight() START ===");
        LogUtils.d(TAG, "Current isFlashlightOn: " + isFlashlightOn);
        LogUtils.d(TAG, "Camera available: " + (camera != null));
        LogUtils.d(TAG, "Has flash unit: " + (camera != null && camera.getCameraInfo().hasFlashUnit()));
        
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            boolean oldState = isFlashlightOn;
            isFlashlightOn = !isFlashlightOn;
            
            LogUtils.d(TAG, "Toggling flashlight from " + oldState + " to " + isFlashlightOn);
            
            try {
                camera.getCameraControl().enableTorch(isFlashlightOn);
                LogUtils.d(TAG, "Camera torch set to: " + isFlashlightOn);
            } catch (Exception e) {
                LogUtils.e(TAG, "Failed to set camera torch to " + isFlashlightOn, e);
            }
            
            // Update icon to reflect flashlight state
            if (flashlightToggleIcon != null) {
                if (isFlashlightOn) {
                    flashlightToggleIcon.setImageResource(R.drawable.flashlight_on_icon);
                    LogUtils.d(TAG, "Updated icon to flashlight_on_icon");
                } else {
                    flashlightToggleIcon.setImageResource(R.drawable.flashlight_off_icon);
                    LogUtils.d(TAG, "Updated icon to flashlight_off_icon");
                }
            } else {
                LogUtils.w(TAG, "Cannot update icon - flashlightToggleIcon is null");
            }
            
            // Save flashlight state to preferences
            LogUtils.d(TAG, "About to save flashlight state: " + isFlashlightOn);
            PreferencesHelper.saveFlashlightEnabled(this, isFlashlightOn);
            LogUtils.d(TAG, "Flashlight state saved to preferences");
            
            LogUtils.d(TAG, "Flashlight toggled to: " + (isFlashlightOn ? "ON" : "OFF"));
        } else {
            LogUtils.w(TAG, "Camera not available or does not have flashlight");
            if (flashlightToggleIcon != null) {
                // Disable the icon if no flashlight is available
                flashlightToggleIcon.setAlpha(0.3f);
                LogUtils.d(TAG, "Set flashlight icon alpha to 0.3 (disabled)");
            }
        }
        
        LogUtils.d(TAG, "=== toggleFlashlight() END ===");
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void stopAnalyzing() {
        try {
            if (barcodeHandler != null && barcodeHandler.getBarcodeAnalyzer() != null) {
                barcodeHandler.getBarcodeAnalyzer().stopAnalyzing();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Can not stop the analyzer: " + BARCODE_DETECTION, e);
        }
    }

    public void disposeModels() {
        try {
            LogUtils.i(TAG, "Disposing the barcode analyzer");
            if (barcodeHandler != null) {
                barcodeHandler.stop();
            }
      } catch (Exception e) {
            LogUtils.e(TAG, "Can not dispose the analyzer: " + BARCODE_DETECTION, e);
        }
    }

    // Handles barcode detection results and updates the graphical overlay
    @Override
    public void onDetectionResult(List<BarcodeEntity> result) {
        List<Rect> rects = new ArrayList<>();
        List<String> decodedStrings = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();  // Track color for each barcode
        List<BarcodeEntity> filtered_entities = new ArrayList<>();

        // Track which cache entries were used this frame (for debounce)
        List<CachedBarcode> usedCacheEntries = new ArrayList<>();

        // Get crop region and rotation info if cropping is enabled
        Rect cropRegion = null;
        int imageRotationDegrees = 0;
        if (barcodeHandler != null && barcodeHandler.getBarcodeAnalyzer() != null) {
            BarcodeAnalyzer analyzer = barcodeHandler.getBarcodeAnalyzer();
            if (analyzer.isCroppingEnabled()) {
                cropRegion = analyzer.getCropRegion();
                imageRotationDegrees = analyzer.getLastImageRotationDegrees();
                LogUtils.v(TAG, "Crop region active: " + cropRegion + ", rotationDegrees: " + imageRotationDegrees);
            }
        }

        if (result != null) {
            for (BarcodeEntity bEntity : result) {
                Rect rect = bEntity.getBoundingBox();
                if (rect != null) {
                    Rect adjustedRect;
                    if (cropRegion != null) {
                        // The bounding box from the decoder is relative to the cropped image.
                        // The crop was done in raw sensor space, so we add the crop offset
                        // in raw sensor space to get full raw sensor coordinates.
                        Rect rawSensorRect = coordinateMapper.adjustBboxForCropRegion(rect, cropRegion);
                        LogUtils.v(TAG, "Raw sensor rect: " + rawSensorRect + " (bbox " + rect + " + crop offset " + cropRegion.left + "," + cropRegion.top + ")");

                        // Transform from raw sensor space to effective (rotated) image space.
                        // This is needed because we decoded with rotation=0, so bboxes are in raw sensor coords,
                        // but mapBoundingBoxToOverlay expects effective image coords.
                        adjustedRect = coordinateMapper.transformRawSensorToEffective(rawSensorRect, imageRotationDegrees);
                        LogUtils.v(TAG, "Effective rect after rotation transform: " + adjustedRect);
                    } else {
                        adjustedRect = rect;
                    }

                    // Update overlay dimensions and get the overlay rect
                    coordinateMapper.setOverlayDimensions(
                            binding.graphicOverlay.getWidth(),
                            binding.graphicOverlay.getHeight());
                    Display display = getWindowManager().getDefaultDisplay();
                    int currentRotation = display.getRotation();
                    Rect overlayRect = coordinateMapper.mapBoundingBoxToOverlay(adjustedRect, currentRotation);

                    // Get the barcode value and symbology
                    String barcodeValue = bEntity.getValue();
                    int symbology = bEntity.getSymbology();
                    boolean usedCache = false;  // Track if cache was used for color coding
                    BarcodeEntity entityToCapture = bEntity;  // Entity to use for capture

                    // Apply debounce logic if enabled
                    if (debounceManager.isEnabled()) {
                        if (barcodeValue != null && !barcodeValue.isEmpty()) {
                            // Barcode has a value - update the cache
                            if (isHighResStabilizationEnabled) {
                                LogUtils.d(TAG, "HighRes-Preview: Caching barcode '" + barcodeValue + "'");
                                LogUtils.d(TAG, "HighRes-Preview:   Raw bbox from SDK: " + rect);
                                LogUtils.d(TAG, "HighRes-Preview:   After adjustments: " + adjustedRect);
                                LogUtils.d(TAG, "HighRes-Preview:   Final overlayRect: " + overlayRect);
                                LogUtils.d(TAG, "HighRes-Preview:   imageWidth=" + coordinateMapper.getImageWidth() + ", imageHeight=" + coordinateMapper.getImageHeight());
                            }
                            debounceManager.updateOrAddToCache(bEntity, overlayRect);
                            // usedCache stays false - this is a fresh decode
                        } else {
                            // Barcode has no value - try to find a cached match
                            CachedBarcode cachedMatch = debounceManager.findCachedMatch(overlayRect, usedCacheEntries);
                            if (cachedMatch != null) {
                                // Use cached value and entity
                                barcodeValue = cachedMatch.getValue();
                                symbology = cachedMatch.getSymbology();
                                entityToCapture = cachedMatch.getEntity();  // Use cached entity for capture
                                usedCacheEntries.add(cachedMatch);
                                cachedMatch.updatePosition(overlayRect);
                                cachedMatch.resetFrameAge();
                                // Update stability tracking - empty value indicates instability
                                cachedMatch.updateValue(bEntity.getValue());
                                usedCache = true;  // Mark as cache hit
                                LogUtils.v(TAG, "Debounce: Using cached value '" + barcodeValue + "' for empty barcode");
                            }
                        }
                    }


                    // Check if the entity passes the filtering conditions
                    // If filtering is not enabled, it returns always true
                    if (barcodeValue != null && !barcodeValue.isEmpty() && shouldIncludeEntity(entityToCapture)) {
                        // Now if necessary, check if the barcode meets the
                        rects.add(overlayRect);
                        String hashCode = String.valueOf(entityToCapture.hashCode());
                        // Ensure the string has at least 4 characters
                        if (hashCode.length() >= 4) {
                            // Get the last four digits
                            hashCode = hashCode.substring(hashCode.length() - 4);
                        }
                        decodedStrings.add(barcodeValue);
                        colors.add(usedCache ? Color.BLUE : Color.GREEN);  // BLUE for cached, GREEN for decoded
                        LogUtils.d(TAG, "Tracker UUID: " + hashCode + " Tracker Detected entity - Value: " + barcodeValue);
                        filtered_entities.add(entityToCapture);
                    } else if (barcodeValue == null || barcodeValue.isEmpty()) {
                        // Show RED box for empty barcodes (no value, no cache match)
                        rects.add(overlayRect);
                        decodedStrings.add("");  // Empty string for display
                        colors.add(Color.RED);
                        LogUtils.v(TAG, "Barcode has no value (and no cache match), showing RED box");
                    } else {
                        LogUtils.v(TAG, "Barcode does not match filtering conditions, ignoring: " + barcodeValue);
                    }
                }
            }
        }
        else
        {
            LogUtils.v(TAG, "Results empty.");

            // If debounce is enabled, use cached barcodes when detection returns no results
            List<CachedBarcode> debounceCache = debounceManager.getCache();
            if (debounceManager.isEnabled() && !debounceCache.isEmpty()) {
                LogUtils.v(TAG, "Debounce: Using " + debounceCache.size() + " cached barcodes for empty detection result");
                for (CachedBarcode cached : debounceCache) {
                    rects.add(cached.getOverlayRect());
                    decodedStrings.add(cached.getValue());
                    colors.add(Color.BLUE);  // All cached entries are BLUE
                    filtered_entities.add(cached.getEntity());
                    LogUtils.v(TAG, "Debounce: Added cached barcode '" + cached.getValue() + "' at (" + cached.getCenterX() + ", " + cached.getCenterY() + ")");
                }
            }
        }

        // Age and prune cache after processing all barcodes
        if (debounceManager.isEnabled()) {
            debounceManager.incrementAndPruneCacheAge();
        }

        // Check for high-res stabilization trigger (after debounce processing)
        if (isHighResStabilizationEnabled && debounceManager.isEnabled() && !debounceManager.getCache().isEmpty()) {
            checkAndTriggerHighResCapture();
        }

        entitiesHolder = filtered_entities;

        // Auto capture evaluation
        if (isAutoCaptureEnabled && autoCaptureConditions != null && !autoCaptureConditions.isEmpty()) {
            if (AutoCaptureEvaluator.evaluateConditions(autoCaptureConditions, filtered_entities)) {
                runOnUiThread(() -> captureData());
                return; // Skip overlay update since we're capturing
            }
        }

        runOnUiThread(() -> {
            binding.graphicOverlay.clear();
            if (rects.size() > 0) {
                binding.graphicOverlay.clear();
                binding.graphicOverlay.add(new BarcodeGraphic(binding.graphicOverlay, rects, decodedStrings, colors));
            }
        });
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        try {
            LogUtils.i(TAG, "Using Entity Analyzer");
            executors.execute(() -> {
                barcodeHandler = new BarcodeHandler(this, this, analysisUseCase);
                // Set callback to initialize crop region once analyzer is ready
                barcodeHandler.setAnalyzerReadyCallback(new BarcodeHandler.AnalyzerReadyCallback() {
                    @Override
                    public void onAnalyzerReady(BarcodeAnalyzer analyzer) {
                        LogUtils.d(TAG, "Analyzer ready, updating crop region and timing callback");
                        // Run on UI thread since we access UI components
                        runOnUiThread(() -> {
                            updateAnalyzerCropRegion();
                            updateAnalyzerTimingCallback();
                        });
                    }
                });
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Can not create model for : " + BARCODE_DETECTION, e);
            return;
        }
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }


    @SuppressLint("UnsafeOptInUsageError")
    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            binding.graphicOverlay.clear();
            cameraProvider.unbind(previewUseCase);
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        builder.setResolutionSelector(resolutionSelector);

        ImageAnalysis.Builder analysisBuilder = new ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);

        // Apply continuous autofocus using Camera2Interop if enabled
        if (forceContinuousAutofocus) {
            LogUtils.d(TAG, "Applying forced continuous autofocus via Camera2Interop");
            Camera2Interop.Extender<Preview> previewExtender = new Camera2Interop.Extender<>(builder);
            previewExtender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            );

            Camera2Interop.Extender<ImageAnalysis> analysisExtender = new Camera2Interop.Extender<>(analysisBuilder);
            analysisExtender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            );
        }

        analysisUseCase = analysisBuilder.build();
        previewUseCase = builder.build();
        binding.previewView.setVisibility(View.VISIBLE);
        binding.entityView.setVisibility(View.GONE);
        previewUseCase.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        // First bind Preview + ImageAnalysis together (without ImageCapture)
        // This allows ImageAnalysis to get the user's requested resolution
        camera = cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase, analysisUseCase);

        // Query the actual resolution used by ImageAnalysis after binding
        // This is critical because CameraX may select a different resolution than requested
        updateActualImageDimensions();

        // Setup and bind ImageCapture separately if high-res stabilization is enabled
        // Binding separately allows ImageCapture to get the true maximum sensor resolution
        // instead of being limited by the Preview + ImageAnalysis stream combination
        if (isHighResStabilizationEnabled) {
            setupImageCapture();
            if (imageCaptureUseCase != null) {
                LogUtils.d(TAG, "Binding ImageCapture separately for maximum resolution");
                cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, imageCaptureUseCase);

                // Log the ImageCapture resolution after separate binding
                androidx.camera.core.ResolutionInfo captureResInfo = imageCaptureUseCase.getResolutionInfo();
                if (captureResInfo != null) {
                    android.util.Size captureRes = captureResInfo.getResolution();
                    LogUtils.d(TAG, "ImageCapture resolution after separate binding: " +
                        captureRes.getWidth() + "x" + captureRes.getHeight());
                }
            }
        }

        // Load flashlight settings after camera is bound and available
        LogUtils.d(TAG, "Camera bound successfully, loading flashlight settings");
        loadFlashlightSettings();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onResume() {
        super.onResume();
        LogUtils.v(TAG, "OnResume called");
        
        // Load and apply capture zone settings
        if (captureZoneOverlay != null) {
            LogUtils.d(TAG, "onResume - CaptureZoneOverlay dimensions: " + captureZoneOverlay.getWidth() + "x" + captureZoneOverlay.getHeight());
            loadCaptureZoneSettings();
        }

        // Load filtering settings
        loadFilteringSettings();

        // Load capture mode settings
        loadCaptureModeSettings();

        // Load display analysis per second settings
        loadDisplayAnalysisSettings();

        // Load force continuous autofocus settings
        loadForceContinuousAutofocusSettings();

        // Load debounce settings
        loadDebounceSettings();

        // Load auto capture settings
        loadAutoCaptureSettings();

        // Load high-res stabilization settings
        loadHighResStabilizationSettings();

        // Flashlight settings are now loaded after camera is bound in bindPreviewUseCase()

        int currentRotation = getWindowManager().getDefaultDisplay().getRotation();
        if (currentRotation != initialRotation) {
            LogUtils.d(TAG, "Rotation changed during pause, updating initialRotation from " + initialRotation + " to " + currentRotation);
            initialRotation = currentRotation;

            // check if the device rotation is changes when suspended (0-> 0°, 2 -> 270°)
            if(initialRotation == 0 || initialRotation == 2 ) {
                imageWidth = selectedSize.getHeight();
                imageHeight = selectedSize.getWidth();
            }
            else{
                imageWidth = selectedSize.getWidth();
                imageHeight = selectedSize.getHeight();
            }
            // Update coordinate mapper with new dimensions and rotation
            coordinateMapper.setImageDimensions(imageWidth, imageHeight);
            coordinateMapper.setInitialRotation(initialRotation);
            LogUtils.d(TAG, "Updated imageWidth=" + imageWidth + ", imageHeight=" + imageHeight);
        }
        bindAllCameraUseCases();

        // Register the BroadcastReceiver to listen for managed configuration changes
        IntentFilter filter = new IntentFilter(ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES);
        registerReceiver(reloadPreferencesReceiver, filter, RECEIVER_NOT_EXPORTED);
        LogUtils.d(TAG, "Registered BroadcastReceiver for managed configuration changes");

        disableDatawedgePlugin();
    }

    public void onPause() {
        super.onPause();
        LogUtils.v(TAG, "onPause called");

        // Unregister the BroadcastReceiver
        try {
            unregisterReceiver(reloadPreferencesReceiver);
            LogUtils.d(TAG, "Unregistered BroadcastReceiver for managed configuration changes");
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
            LogUtils.d(TAG, "BroadcastReceiver was not registered, ignoring unregister attempt");
        }

        stopAnalyzing();
        unBindCameraX();
        disposeModels();
    }

    private void disableDatawedgePlugin()
    {
        DWScannerPluginDisable dwplugindisable = new DWScannerPluginDisable(this);
        DWProfileBaseSettings settings = new DWProfileBaseSettings()
        {{
            mProfileName = CameraXLivePreviewActivity.this.getPackageName();
        }};

        dwplugindisable.execute(settings, new DWProfileCommandBase.onProfileCommandResult() {
            @Override
            public void result(String profileName, String action, String command, String result, String resultInfo, String commandidentifier) {
            }
            @Override
            public void timeout(String profileName) {

            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }

    private void unBindCameraX() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            LogUtils.v(TAG, "Camera Unbounded");
        }
    }

    private void captureData() {
        if(entitiesHolder != null) {
             if(entitiesHolder.size() > 0)
            {
                ArrayList<Bundle> barcodeDataList = new ArrayList<>();
                for (BarcodeEntity bEntity : entitiesHolder) {
                    Bundle barcodeBundle = new Bundle();
                    barcodeBundle.putString("value", bEntity.getValue());
                    barcodeBundle.putInt("symbology", bEntity.getSymbology());
                    barcodeBundle.putInt("hashcode", bEntity.hashCode());
                    barcodeDataList.add(barcodeBundle);
                }

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("barcodeDataList", barcodeDataList);

                Intent intent = new Intent(this, CapturedBarcodesActivity.class);
                if (isHttpsPostMode) {
                    intent.putExtra(Constants.ENDPOINT_URI, endpointUri);
                } else {
                    intent.putExtra(Constants.CAPTURE_FILE_PATH, captureFilePath);
                }
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if(captureTriggerMode == ECaptureTriggerMode.CAPTURE_ON_PRESS) {
            if (keyCode == Constants.KEYCODE_BUTTON_R1 || keyCode == Constants.KEYCODE_SCAN) {
                captureData();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(captureTriggerMode == ECaptureTriggerMode.CAPTURE_ON_RELEASE) {
            if (keyCode == Constants.KEYCODE_BUTTON_R1 || keyCode == Constants.KEYCODE_SCAN) {
                captureData();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}