// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.java;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.AspectRatioStrategy;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.zebra.ai.vision.analyzer.tracking.EntityTrackerAnalyzer;
import com.zebra.ai.vision.entity.BarcodeEntity;
import com.zebra.ai.vision.entity.Entity;
import com.zebra.ai.vision.viewfinder.EntityViewController;
import com.zebra.ai.vision.viewfinder.listners.EntityClickListener;
import com.zebra.ai.vision.viewfinder.listners.EntityViewResizeListener;
import com.zebra.ai.vision.viewfinder.listners.EntityViewResizeSpecs;
import com.zebra.ai_multibarcodes_capture.CameraXViewModel;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.databinding.ActivityCameraXlivePreviewBinding;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.ECameraResolution;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.helpers.PreferencesHelper;
import com.zebra.ai_multibarcodes_capture.java.analyzers.barcodetracker.BarcodeTracker;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;
import com.zebra.ai_multibarcodes_capture.java.analyzers.barcodetracker.BarcodeTrackerGraphic;



import com.zebra.ai_multibarcodes_capture.java.viewfinder.EntityBarcodeTracker;
import com.zebra.ai_multibarcodes_capture.java.viewfinder.EntityViewGraphic;
import com.zebra.ai_multibarcodes_capture.views.CaptureZoneOverlay;

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
public class CameraXLivePreviewActivity extends AppCompatActivity implements BarcodeTracker.DetectionCallback, EntityBarcodeTracker.DetectionCallback {

    private ActivityCameraXlivePreviewBinding binding;
    private final String TAG = "CameraXLivePreviewActivityJava";
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
    
    
    private static final String ENTITY_ANALYZER = "Tracker";
    @Nullable
    private Camera camera;
    @Nullable
    private Preview previewUseCase;
    private ImageAnalysis analysisUseCase;
    ProcessCameraProvider cameraProvider;
    private int imageWidth;
    private int imageHeight;
    private final int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;
    private ResolutionSelector resolutionSelector;
    private final ExecutorService executors = Executors.newFixedThreadPool(3);
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(3);

    private Button captureButton;
    private Button closeButton;
    private ImageView captureZoneToggleIcon;
    private ImageView flashlightToggleIcon;
    private CaptureZoneOverlay captureZoneOverlay;
    private boolean isFlashlightOn = false;

    // Filtering settings
    private boolean isFilteringEnabled = false;
    private String filteringRegex = "";
    
    private BarcodeTracker barcodeTracker;
    private EntityBarcodeTracker entityBarcodeTracker;
    private String selectedModel = ENTITY_ANALYZER;
    private EntityViewController entityViewController;
    private EntityViewGraphic entityViewGraphic;
    private boolean isIconStyleEnable = false;
    private Size selectedSize;

    // Store pending viewfinder resize data to apply once analyzer is ready
    private android.graphics.Matrix pendingTransformMatrix = null;
    private android.graphics.RectF pendingCropRegion = null;
    private int initialRotation = Surface.ROTATION_0;

    List<? extends Entity> entitiesHolder;

    private String captureFilePath;
    private String endpointUri;
    private boolean isHttpsPostMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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

        // Initialize selectedSize from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String cameraResolutionString = sharedPreferences.getString(Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION, Constants.SHARED_PREFERENCES_CAMERA_RESOLUTION_DEFAULT);
        ECameraResolution cameraResolution = ECameraResolution.valueOf(cameraResolutionString);
        selectedSize = new Size(cameraResolution.getWidth(), cameraResolution.getHeight());

        binding = ActivityCameraXlivePreviewBinding.inflate(getLayoutInflater());
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        setContentView(binding.getRoot());

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
                                if (initialRotation == 0 || initialRotation == 2) {
                                    imageWidth = selectedSize.getHeight();
                                    imageHeight = selectedSize.getWidth();
                                } else {
                                    imageWidth = selectedSize.getWidth();
                                    imageHeight = selectedSize.getHeight();
                                }
                                LogUtils.d(TAG, "Updated imageWidth=" + imageWidth + ", imageHeight=" + imageHeight);


                            bindAllCameraUseCases();
                        });

        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
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

        initEntityView();
        initCaptureZone();
    }

    private void initEntityView() {
        entityViewController = new EntityViewController(binding.entityView, this);
        entityViewController.registerEntityClickListener(new EntityClickListener() {
            @Override
            public void onEntityClicked(Entity entity) {
                isIconStyleEnable = !isIconStyleEnable;
                entityViewGraphic.enableIconPen(isIconStyleEnable);
            }
        });

        entityViewController.registerViewfinderResizeListener(new EntityViewResizeListener() {
            @Override
            public void onViewfinderResized(EntityViewResizeSpecs entityViewResizeSpecs) {
                if (entityBarcodeTracker != null && entityBarcodeTracker.getEntityTrackerAnalyzer() != null) {
                    // Analyzer is ready, apply the transform immediately
                    entityBarcodeTracker.getEntityTrackerAnalyzer().updateTransform(entityViewResizeSpecs.getSensorToViewMatrix());
                    entityBarcodeTracker.getEntityTrackerAnalyzer().setCropRect(entityViewResizeSpecs.getViewfinderFOVCropRegion());

                    // Clear any pending data
                    pendingTransformMatrix = null;
                    pendingCropRegion = null;
                    LogUtils.d(TAG, "Applied viewfinder resize specs immediately");
                } else {
                    // Analyzer not ready yet, extract and store the actual VALUES

                    try {
                        pendingTransformMatrix = new android.graphics.Matrix(entityViewResizeSpecs.getSensorToViewMatrix());
                        pendingCropRegion = new android.graphics.RectF(entityViewResizeSpecs.getViewfinderFOVCropRegion());
                        LogUtils.d(TAG, "Stored pending viewfinder resize data for later application");
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Failed to extract resize spec values", e);
                        pendingTransformMatrix = null;
                        pendingCropRegion = null;
                    }
                }
            }
        });

        entityViewGraphic = new EntityViewGraphic(entityViewController);
    }

    /**
     * Apply any pending viewfinder resize data to the EntityTrackerAnalyzer
     * This should be called after the analyzer is fully initialized
     */
    private void applyPendingResizeSpecs() {
        if (pendingTransformMatrix != null && pendingCropRegion != null &&
                entityBarcodeTracker != null && entityBarcodeTracker.getEntityTrackerAnalyzer() != null) {
            try {
                // Use our safely stored copies of the values
                entityBarcodeTracker.getEntityTrackerAnalyzer().updateTransform(pendingTransformMatrix);
                entityBarcodeTracker.getEntityTrackerAnalyzer().setCropRect(pendingCropRegion);
                LogUtils.d(TAG, "Applied pending viewfinder resize data from stored values");
            } catch (Exception e) {
                LogUtils.e(TAG, "Failed to apply pending resize data", e);
            } finally {
                // Clear the stored values
                pendingTransformMatrix = null;
                pendingCropRegion = null;
            }
        }
    }

    /**
     * Call this method when EntityBarcodeTracker is fully initialized
     * This ensures proper setup of the analyzer with any pending configurations
     */
    public void onEntityBarcodeTrackerReady() {
        LogUtils.d(TAG, "EntityBarcodeTracker is ready, applying pending configurations");
        applyPendingResizeSpecs();
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
            }
        });
        
        LogUtils.d(TAG, "Capture zone overlay initialized");
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

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load filtering enabled state
        isFilteringEnabled = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_FILTERING_ENABLED, Constants.SHARED_PREFERENCES_FILTERING_ENABLED_DEFAULT);

        // Load filtering regex pattern
        filteringRegex = sharedPreferences.getString(Constants.SHARED_PREFERENCES_FILTERING_REGEX, Constants.SHARED_PREFERENCES_FILTERING_REGEX_DEFAULT);

        LogUtils.d(TAG, "Loaded filtering settings - enabled: " + isFilteringEnabled + ", regex: '" + filteringRegex + "'");
        LogUtils.d(TAG, "=== loadFilteringSettings() END ===");
    }

    private boolean isValueMatchingFilteringRegex(String data)
    {
        if(isFilteringEnabled && filteringRegex.isEmpty() == false)
        {
            try {
                // Check if data matches the filteringRegex pattern
                boolean matches = data.matches(filteringRegex);
                LogUtils.v(TAG, "Regex filtering - Data: '" + data + "', Pattern: '" + filteringRegex + "', Matches: " + matches);
                return matches;
            } catch (Exception e) {
                // If regex is invalid, log error and allow the barcode through
                LogUtils.e(TAG, "Invalid regex pattern '" + filteringRegex + "': " + e.getMessage());
                return true;
            }
        }
        else
        {
            // If filtering is disabled, return always true
            return true;
        }
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
            barcodeTracker.stopAnalyzing();
        } catch (Exception e) {
            LogUtils.e(TAG, "Can not stop the analyzer: " + ENTITY_ANALYZER, e);
        }
    }

    public void disposeModels() {
        try {
            LogUtils.i(TAG, "Disposing the entity tracker analyzer");
            if (barcodeTracker != null) {
                barcodeTracker.stop();
            }
      } catch (Exception e) {
            LogUtils.e(TAG, "Can not dispose the analyzer: " + ENTITY_ANALYZER, e);
        }
    }

    private Rect mapBoundingBoxToOverlay(Rect bbox) {

        Display display = getWindowManager().getDefaultDisplay();
        int currentRotation = display.getRotation();

        int relativeRotation = ((currentRotation - initialRotation + 4) % 4);

        int overlayWidth = binding.graphicOverlay.getWidth();
        int overlayHeight = binding.graphicOverlay.getHeight();

        if (overlayWidth == 0 || overlayHeight == 0) {
            return bbox;
        }

        Rect transformedBbox = transformBoundingBoxForRotation(bbox, relativeRotation);

        int effectiveImageWidth = imageWidth;
        int effectiveImageHeight = imageHeight;

        if (relativeRotation == 1 || relativeRotation == 3) {
            effectiveImageWidth = imageHeight;
            effectiveImageHeight = imageWidth;
        }

        float scaleX = (float) overlayWidth / effectiveImageWidth;
        float scaleY = (float) overlayHeight / effectiveImageHeight;
        float scale = Math.max(scaleX, scaleY);

        float offsetX = (overlayWidth - effectiveImageWidth * scale) / 2f;
        float offsetY = (overlayHeight - effectiveImageHeight * scale) / 2f;

        return new Rect(
                (int) (transformedBbox.left * scale + offsetX),
                (int) (transformedBbox.top * scale + offsetY),
                (int) (transformedBbox.right * scale + offsetX),
                (int) (transformedBbox.bottom * scale + offsetY)
        );
    }

    private Rect transformBoundingBoxForRotation(Rect bbox, int relativeRotation) {
        // relativeRotation values:
        // 0: No rotation (0 degrees)
        // 1: 90 degrees clockwise
        // 2: 180 degrees
        // 3: 270 degrees clockwise
        // These values are calculated based on the difference between current and initial device rotation.
        // The transformation is needed to map the bounding box from the image coordinate system to the display coordinate system.
        switch (relativeRotation) {
            case 0: // No transformation needed, image is already aligned
                // No transformation
                return new Rect(bbox);
            case 1:
                // 90 degree clockwise rotation: swap x/y and adjust for width
                // left becomes top, top becomes (imageWidth - right), right becomes bottom, bottom becomes (imageWidth - left)
                return new Rect(
                        bbox.top,
                        imageWidth - bbox.right,
                        bbox.bottom,
                        imageWidth - bbox.left
                );
            case 2:
                // 180 degree rotation: flip both axes
                // left becomes (imageWidth - right), top becomes (imageHeight - bottom), right becomes (imageWidth - left), bottom becomes (imageHeight - top)
                return new Rect(
                        imageWidth - bbox.right,
                        imageHeight - bbox.bottom,
                        imageWidth - bbox.left,
                        imageHeight - bbox.top
                );
            case 3:
                // 270 degree clockwise rotation: swap x/y and adjust for height
                // left becomes (imageHeight - bottom), top becomes left, right becomes (imageHeight - top), bottom becomes right
                return new Rect(
                        imageHeight - bbox.bottom,
                        bbox.left,
                        imageHeight - bbox.top,
                        bbox.right
                );
            default:
                LogUtils.w(TAG, "Unknown relative rotation: " + relativeRotation + ", using original bbox");
                return new Rect(bbox);
        }
    }

    

    

    // Handles entity tracking results and updates the graphical overlay
    @Override
    public void handleEntities(EntityTrackerAnalyzer.Result result) {
        List<Rect> rects = new ArrayList<>();
        List<String> decodedStrings = new ArrayList<>();
        List<? extends Entity> entities;
        if(barcodeTracker.getBarcodeDecoder()!=null) {
            entities = result.getValue(barcodeTracker.getBarcodeDecoder());
            entitiesHolder = entities;
        } else {
            entities = null;
        }

        runOnUiThread(() -> {
            binding.graphicOverlay.clear();
            if (entities != null) {
                for (Entity entity : entities) {
                    if (entity instanceof BarcodeEntity) {
                        BarcodeEntity bEntity = (BarcodeEntity) entity;
                        Rect rect = bEntity.getBoundingBox();
                        if (rect != null) {
                            Rect overlayRect = mapBoundingBoxToOverlay(rect);
                            
                            // Only process barcode if it's inside the capture zone (when capture zone is enabled)
                            if (isBarcodeInCaptureZone(overlayRect)) {
                                // Check if the entity is matching the filtering regex
                                // If the filtering is not enabled, it returns always true
                                if(isValueMatchingFilteringRegex(bEntity.getValue())) {
                                    // Now if necessary, check if the barcode meets the
                                    rects.add(overlayRect);
                                    String hashCode = String.valueOf(bEntity.hashCode());
                                    // Ensure the string has at least 4 characters
                                    if (hashCode.length() >= 4) {
                                        // Get the last four digits
                                        hashCode = hashCode.substring(hashCode.length() - 4);

                                    }
                                    decodedStrings.add(bEntity.getValue());
                                    LogUtils.d(TAG, "Tracker UUID: " + hashCode + " Tracker Detected entity - Value: " + bEntity.getValue());
                                }
                                else
                                {
                                    LogUtils.v(TAG, "Barcode does not match regex, ignoring: " + bEntity.getValue());
                                }
                            } else {
                                LogUtils.v(TAG, "Barcode outside capture zone, ignoring: " + bEntity.getValue());
                            }
                        }
                    }

                    //currently using same BarcodeGraphic as there are no much difference in UI
                    binding.graphicOverlay.add(new BarcodeTrackerGraphic(binding.graphicOverlay, rects, decodedStrings));
                }
            }

        });
    }

    // Handles entities for the entity view tracker and updates the graphical overlay
    @Override
    public void handleEntitiesForEntityView(EntityTrackerAnalyzer.Result result) {
        LogUtils.d(TAG,"Handle View Entity - Result received");

        // Apply any pending resize specs now that the analyzer is ready
        applyPendingResizeSpecs();

        List<? extends Entity> entities = null;
        if(entityBarcodeTracker != null && entityBarcodeTracker.getBarcodeDecoder() != null) {
            entities = result.getValue(entityBarcodeTracker.getBarcodeDecoder());
            LogUtils.d(TAG, "EntityBarcodeTracker decoder available, entities count: " + (entities != null ? entities.size() : "null"));
        } else {
            LogUtils.w(TAG, "EntityBarcodeTracker or decoder is null - tracker: " + (entityBarcodeTracker != null) + ", decoder: " + (entityBarcodeTracker != null && entityBarcodeTracker.getBarcodeDecoder() != null));
        }

        if(entityViewGraphic != null) {
            entityViewGraphic.clear();
        } else {
            LogUtils.w(TAG, "EntityViewGraphic is null");
        }

        if (entities != null) {
            LogUtils.d(TAG, "Processing " + entities.size() + " entities for entity view");
            for (Entity entity : entities) {
                if (entity instanceof BarcodeEntity) {
                    BarcodeEntity bEntity = (BarcodeEntity) entity;
                    Rect rect = bEntity.getBoundingBox();
                    if (rect != null) {
                        LogUtils.d(TAG, "Adding entity to view - Value: " + bEntity.getValue() + ", BBox: " + rect);
                        entityViewGraphic.addEntity(bEntity);
                    } else {
                        LogUtils.w(TAG, "Entity has null bounding box - Value: " + bEntity.getValue());
                    }
                }
            }
            entityViewGraphic.render();
            LogUtils.d(TAG, "Rendered entities on entity view");
        } else {
            LogUtils.w(TAG, "No entities to process for entity view");
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        try {
            LogUtils.i(TAG, "Using Entity Analyzer");
            executors.execute(() -> {
                barcodeTracker = new BarcodeTracker(this, this, analysisUseCase);
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "Can not create model for : " + ENTITY_ANALYZER, e);
            return;
        }
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }


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

        analysisUseCase = new ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        previewUseCase = builder.build();
        binding.previewView.setVisibility(View.VISIBLE);
        binding.entityView.setVisibility(View.GONE);
        previewUseCase.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase, analysisUseCase);
        
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
            LogUtils.d(TAG, "Updated imageWidth=" + imageWidth + ", imageHeight=" + imageHeight);
        }
        bindAllCameraUseCases();

        // Register the BroadcastReceiver to listen for managed configuration changes
        IntentFilter filter = new IntentFilter(ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES);
        registerReceiver(reloadPreferencesReceiver, filter, RECEIVER_NOT_EXPORTED);
        LogUtils.d(TAG, "Registered BroadcastReceiver for managed configuration changes");
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
            List<BarcodeEntity> barcodeEntities = new ArrayList<>();
            for (Entity entity : entitiesHolder) {
                if (entity instanceof BarcodeEntity) {
                    BarcodeEntity bEntity = (BarcodeEntity) entity;
                    
                    // Check if barcode is in capture zone before adding to results
                    Rect boundingBox = bEntity.getBoundingBox();
                    if (boundingBox != null) {
                        Rect overlayRect = mapBoundingBoxToOverlay(boundingBox);
                        if (isBarcodeInCaptureZone(overlayRect)) {
                            if(isValueMatchingFilteringRegex(bEntity.getValue())) {
                                barcodeEntities.add(bEntity);
                                LogUtils.d(TAG, "Barcode captured.\nValue:" + bEntity.getValue() + "\nSymbology:" + bEntity.getSymbology() + "\nHashcode:" + bEntity.hashCode());
                            }
                            else
                            {
                                LogUtils.d(TAG,"Barcode does not match filtering regex:" + filteringRegex + " with value:" + bEntity.getValue());
                            }
                        } else {
                            LogUtils.d(TAG, "Barcode outside capture zone, not captured: " + bEntity.getValue());
                        }
                    } else {
                        LogUtils.w(TAG, "Barcode has no bounding box, skipping: " + bEntity.getValue());
                    }
                }
            }
            if(barcodeEntities.size() > 0)
            {
                ArrayList<Bundle> barcodeDataList = new ArrayList<>();
                for (BarcodeEntity bEntity : barcodeEntities) {
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
        if(keyCode == Constants.KEYCODE_BUTTON_R1 || keyCode == Constants.KEYCODE_SCAN)
        {
            captureData();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}