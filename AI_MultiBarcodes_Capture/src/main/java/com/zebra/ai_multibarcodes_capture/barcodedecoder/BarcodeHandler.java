// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.barcodedecoder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.zebra.ai.vision.detector.AIVisionSDKException;
import com.zebra.ai.vision.detector.ImageData;
import com.zebra.ai.vision.entity.BarcodeEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.zebra.ai.vision.detector.AIVisionSDKLicenseException;
import com.zebra.ai.vision.detector.BarcodeDecoder;
import com.zebra.ai.vision.detector.InferencerOptions;
import com.zebra.ai_multibarcodes_capture.helpers.EInferenceType;
import com.zebra.ai_multibarcodes_capture.helpers.EModelInputSize;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.camera.core.ImageAnalysis;
import androidx.core.content.ContextCompat;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_AUSTRALIAN_POSTAL;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_AUSTRALIAN_POSTAL_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_AZTEC;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_AZTEC_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CANADIAN_POSTAL;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CANADIAN_POSTAL_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CHINESE_2OF5;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CHINESE_2OF5_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODABAR;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODABAR_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE11;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE11_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE128;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE128_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE39;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE39_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE93;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_CODE93_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_COMPOSITE_AB;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_COMPOSITE_AB_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_COMPOSITE_C;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_COMPOSITE_C_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_D2OF5;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_D2OF5_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DATAMATRIX;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DATAMATRIX_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DOTCODE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DOTCODE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DUTCH_POSTAL;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_DUTCH_POSTAL_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_EAN_13;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_EAN_13_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_EAN_8;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_EAN_8_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_FINNISH_POSTAL_4S;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_FINNISH_POSTAL_4S_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GRID_MATRIX;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GRID_MATRIX_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR_EXPANDED;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR_EXPANDED_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR_LIM;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATABAR_LIM_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATAMATRIX;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_DATAMATRIX_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_QRCODE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_GS1_QRCODE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_HANXIN;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_HANXIN_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_I2OF5;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_I2OF5_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_INFERENCE_TYPE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_INFERENCE_TYPE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_JAPANESE_POSTAL;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_JAPANESE_POSTAL_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_KOREAN_3OF5;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_KOREAN_3OF5_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MAILMARK;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MAILMARK_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MATRIX_2OF5;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MATRIX_2OF5_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MAXICODE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MAXICODE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MICROPDF;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MICROPDF_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MICROQR;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MICROQR_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MODEL_INPUT_SIZE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MODEL_INPUT_SIZE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MSI;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_MSI_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_PDF417;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_PDF417_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_QRCODE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_QRCODE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_TLC39;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_TLC39_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_TRIOPTIC39;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_TRIOPTIC39_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UK_POSTAL;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UK_POSTAL_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPCE0;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPCE0_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPC_A;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPC_A_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPC_E;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_UPC_E_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_US4STATE;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_US4STATE_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_US4STATE_FICS;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_US4STATE_FICS_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_USPLANET;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_USPLANET_DEFAULT;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_USPOSTNET;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_USPOSTNET_DEFAULT;

/**
 * The BarcodeHandler class is responsible for setting up and managing the barcode
 * decoding process using a BarcodeDecoder and BarcodeAnalyzer. This class initializes
 * the necessary components for barcode detection and manages the lifecycle of the
 * decoding process within an Android application context.
 *
 * The BarcodeHandler sets up the decoder, assigns an analyzer for image analysis,
 * and provides methods to stop and dispose of resources when they are no longer needed.
 *
 * Usage:
 * - Instantiate the BarcodeHandler with the appropriate context, callback, and image analysis configuration.
 * - Call initializeBarcodeDecoder() to set up the barcode decoder and analyzer.
 * - Use getBarcodeAnalyzer() to retrieve the current instance of the BarcodeAnalyzer.
 * - Call stop() to terminate the executor service and dispose of the decoder when finished.
 *
 * Dependencies:
 * - Android Context: Required for managing resources and executing tasks on the main thread.
 * - ExecutorService: Used for asynchronous task execution.
 * - ImageAnalysis: Provides the framework for analyzing image data.
 * - BarcodeDecoder: Handles the decoding of barcode symbologies.
 * - BarcodeAnalyzer: Analyzes images to detect barcodes and report results via a callback.
 *
 * Exception Handling:
 * - Handles AIVisionSDKLicenseException and AIVisionSDKException during decoder initialization.
 * - Logs any other exceptions encountered during the setup process.
 *
 * Note: Ensure that the appropriate permissions and dependencies are configured
 * in the AndroidManifest and build files to utilize camera and image processing capabilities.
 */
public class BarcodeHandler {
    private static final String TAG = "BarcodeHandler";
    private BarcodeDecoder barcodeDecoder;
    private final ExecutorService executor;
    private final Context context;
    private BarcodeAnalyzer barcodeAnalyzer;
    private final BarcodeAnalyzer.DetectionCallback callback;
    private final ImageAnalysis imageAnalysis;
    private String mavenModelName = "barcode-localizer";
    private AnalyzerReadyCallback analyzerReadyCallback;

    /**
     * Callback interface to notify when the BarcodeAnalyzer is ready.
     */
    public interface AnalyzerReadyCallback {
        void onAnalyzerReady(BarcodeAnalyzer analyzer);
    }

    /**
     * Constructs a new BarcodeHandler with the specified context, callback, and image analysis configuration.
     *
     * @param context The Android context for resource management.
     * @param callback The callback for handling detection results.
     * @param imageAnalysis The image analysis configuration for processing image data.
     */
    public BarcodeHandler(Context context, BarcodeAnalyzer.DetectionCallback callback, ImageAnalysis imageAnalysis) {
        this.context = context;
        this.callback = callback;
        this.executor = Executors.newSingleThreadExecutor();
        this.imageAnalysis = imageAnalysis;
        initializeBarcodeDecoder();
    }

    /**
     * Initializes the BarcodeDecoder with predefined settings for barcode symbologies
     * and detection parameters. This method sets up the necessary components for analyzing
     * and decoding barcodes from image data.
     */
    public void initializeBarcodeDecoder() {
        try {
            BarcodeDecoder.Settings decoderSettings = new BarcodeDecoder.Settings(mavenModelName);
            Integer[] rpo = new Integer[3];
            rpo[0] = InferencerOptions.DSP;
            rpo[1] = InferencerOptions.CPU;
            rpo[2] = InferencerOptions.GPU;

            // Retrieve inference type from shared preferences
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            String inferenceTypeString = sharedPreferences.getString(SHARED_PREFERENCES_INFERENCE_TYPE, SHARED_PREFERENCES_INFERENCE_TYPE_DEFAULT);
            EInferenceType inferenceType = EInferenceType.valueOf(inferenceTypeString);
            rpo[0] = inferenceType.toInferencerOptions();

            // Retrieve model input size from shared preferences
            String modelInputSizeString = sharedPreferences.getString(SHARED_PREFERENCES_MODEL_INPUT_SIZE, SHARED_PREFERENCES_MODEL_INPUT_SIZE_DEFAULT);
            EModelInputSize modelInputSize = EModelInputSize.valueOf(modelInputSizeString);

            setAvailableSymbologiesFromPreferences(context, decoderSettings);

            decoderSettings.detectorSetting.inferencerOptions.runtimeProcessorOrder = rpo;
            decoderSettings.detectorSetting.inferencerOptions.defaultDims.height = modelInputSize.getHeight();
            decoderSettings.detectorSetting.inferencerOptions.defaultDims.width = modelInputSize.getWidth();

            long m_Start = System.currentTimeMillis();
            BarcodeDecoder.getBarcodeDecoder(decoderSettings, executor).thenAccept(decoderInstance -> {
                barcodeDecoder = decoderInstance;
                barcodeAnalyzer = new BarcodeAnalyzer(callback, barcodeDecoder);
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), barcodeAnalyzer);
                LogUtils.d(TAG, "BarcodeDecoder() obj creation time =" + (System.currentTimeMillis() - m_Start) + " milli sec");

                // Notify callback that analyzer is ready
                if (analyzerReadyCallback != null) {
                    analyzerReadyCallback.onAnalyzerReady(barcodeAnalyzer);
                }
            }).exceptionally(e -> {
                if (e instanceof AIVisionSDKLicenseException) {
                    LogUtils.e(TAG, "AIVisionSDKLicenseException: Barcode Decoder object creation failed, " + e.getMessage());
                } else {
                    LogUtils.e(TAG, "Fatal error: decoder creation failed - " + e.getMessage());
                }
                return null;
            });
        } catch (AIVisionSDKException ex) {
            LogUtils.e(TAG, "Model Loading: Barcode decoder returned with exception " + ex.getMessage());
        }
    }

    private static void setAvailableSymbologiesFromPreferences(Context context, BarcodeDecoder.Settings decoderSettings) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        decoderSettings.Symbology.AUSTRALIAN_POSTAL.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_AUSTRALIAN_POSTAL, SHARED_PREFERENCES_AUSTRALIAN_POSTAL_DEFAULT));
        decoderSettings.Symbology.AZTEC.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_AZTEC, SHARED_PREFERENCES_AZTEC_DEFAULT));
        decoderSettings.Symbology.CANADIAN_POSTAL.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CANADIAN_POSTAL, SHARED_PREFERENCES_CANADIAN_POSTAL_DEFAULT));
        decoderSettings.Symbology.CHINESE_2OF5.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CHINESE_2OF5, SHARED_PREFERENCES_CHINESE_2OF5_DEFAULT));
        decoderSettings.Symbology.CODABAR.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODABAR, SHARED_PREFERENCES_CODABAR_DEFAULT));
        decoderSettings.Symbology.CODE11.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE11, SHARED_PREFERENCES_CODE11_DEFAULT));
        decoderSettings.Symbology.CODE39.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE39, SHARED_PREFERENCES_CODE39_DEFAULT));
        decoderSettings.Symbology.CODE93.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE93, SHARED_PREFERENCES_CODE93_DEFAULT));
        decoderSettings.Symbology.CODE128.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE128, SHARED_PREFERENCES_CODE128_DEFAULT));
        decoderSettings.Symbology.COMPOSITE_AB.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_COMPOSITE_AB, SHARED_PREFERENCES_COMPOSITE_AB_DEFAULT));
        decoderSettings.Symbology.COMPOSITE_C.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_COMPOSITE_C, SHARED_PREFERENCES_COMPOSITE_C_DEFAULT));
        decoderSettings.Symbology.D2OF5.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_D2OF5, SHARED_PREFERENCES_D2OF5_DEFAULT));
        decoderSettings.Symbology.DATAMATRIX.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_DATAMATRIX, SHARED_PREFERENCES_DATAMATRIX_DEFAULT));
        decoderSettings.Symbology.DOTCODE.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_DOTCODE, SHARED_PREFERENCES_DOTCODE_DEFAULT));
        decoderSettings.Symbology.DUTCH_POSTAL.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_DUTCH_POSTAL, SHARED_PREFERENCES_DUTCH_POSTAL_DEFAULT));
        decoderSettings.Symbology.EAN8.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_EAN_8, SHARED_PREFERENCES_EAN_8_DEFAULT));
        decoderSettings.Symbology.EAN13.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_EAN_13, SHARED_PREFERENCES_EAN_13_DEFAULT));
        decoderSettings.Symbology.FINNISH_POSTAL_4S.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_FINNISH_POSTAL_4S, SHARED_PREFERENCES_FINNISH_POSTAL_4S_DEFAULT));
        decoderSettings.Symbology.GRID_MATRIX.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GRID_MATRIX, SHARED_PREFERENCES_GRID_MATRIX_DEFAULT));
        decoderSettings.Symbology.GS1_DATABAR.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR, SHARED_PREFERENCES_GS1_DATABAR_DEFAULT));
        decoderSettings.Symbology.GS1_DATABAR_EXPANDED.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR_EXPANDED, SHARED_PREFERENCES_GS1_DATABAR_EXPANDED_DEFAULT));
        decoderSettings.Symbology.GS1_DATABAR_LIM.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR_LIM, SHARED_PREFERENCES_GS1_DATABAR_LIM_DEFAULT));
        decoderSettings.Symbology.GS1_DATAMATRIX.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATAMATRIX, SHARED_PREFERENCES_GS1_DATAMATRIX_DEFAULT));
        decoderSettings.Symbology.GS1_QRCODE.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_QRCODE, SHARED_PREFERENCES_GS1_QRCODE_DEFAULT));
        decoderSettings.Symbology.HANXIN.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_HANXIN, SHARED_PREFERENCES_HANXIN_DEFAULT));
        decoderSettings.Symbology.I2OF5.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_I2OF5, SHARED_PREFERENCES_I2OF5_DEFAULT));
        decoderSettings.Symbology.JAPANESE_POSTAL.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_JAPANESE_POSTAL, SHARED_PREFERENCES_JAPANESE_POSTAL_DEFAULT));
        decoderSettings.Symbology.KOREAN_3OF5.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_KOREAN_3OF5, SHARED_PREFERENCES_KOREAN_3OF5_DEFAULT));
        decoderSettings.Symbology.MAILMARK.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MAILMARK, SHARED_PREFERENCES_MAILMARK_DEFAULT));
        decoderSettings.Symbology.MATRIX_2OF5.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MATRIX_2OF5, SHARED_PREFERENCES_MATRIX_2OF5_DEFAULT));
        decoderSettings.Symbology.MAXICODE.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MAXICODE, SHARED_PREFERENCES_MAXICODE_DEFAULT));
        decoderSettings.Symbology.MICROPDF.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MICROPDF, SHARED_PREFERENCES_MICROPDF_DEFAULT));
        decoderSettings.Symbology.MICROQR.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MICROQR, SHARED_PREFERENCES_MICROQR_DEFAULT));
        decoderSettings.Symbology.MSI.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_MSI, SHARED_PREFERENCES_MSI_DEFAULT));
        decoderSettings.Symbology.PDF417.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_PDF417, SHARED_PREFERENCES_PDF417_DEFAULT));
        decoderSettings.Symbology.QRCODE.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_QRCODE, SHARED_PREFERENCES_QRCODE_DEFAULT));
        decoderSettings.Symbology.TLC39.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_TLC39, SHARED_PREFERENCES_TLC39_DEFAULT));
        decoderSettings.Symbology.TRIOPTIC39.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_TRIOPTIC39, SHARED_PREFERENCES_TRIOPTIC39_DEFAULT));
        decoderSettings.Symbology.UK_POSTAL.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_UK_POSTAL, SHARED_PREFERENCES_UK_POSTAL_DEFAULT));
        decoderSettings.Symbology.UPCA.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPC_A, SHARED_PREFERENCES_UPC_A_DEFAULT));
        decoderSettings.Symbology.UPCEAN.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPC_E, SHARED_PREFERENCES_UPC_E_DEFAULT));
        decoderSettings.Symbology.UPCE0.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPCE0, SHARED_PREFERENCES_UPCE0_DEFAULT));
        decoderSettings.Symbology.USPLANET.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_USPLANET, SHARED_PREFERENCES_USPLANET_DEFAULT));
        decoderSettings.Symbology.USPOSTNET.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_USPOSTNET, SHARED_PREFERENCES_USPOSTNET_DEFAULT));
        decoderSettings.Symbology.US4STATE.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_US4STATE, SHARED_PREFERENCES_US4STATE_DEFAULT));
        decoderSettings.Symbology.US4STATE_FICS.enable(sharedPreferences.getBoolean(SHARED_PREFERENCES_US4STATE_FICS, SHARED_PREFERENCES_US4STATE_FICS_DEFAULT));
    }


    /**
     * Stops the executor service and disposes of the BarcodeDecoder, releasing any resources held.
     * This method should be called when barcode detection is no longer needed.
     */
    public void stop() {
        executor.shutdownNow();
        if (barcodeDecoder != null) {
            barcodeDecoder.dispose();
            LogUtils.d(TAG, "Barcode decoder is disposed");
            barcodeDecoder = null;
        }
    }

    /**
     * Retrieves the current instance of the BarcodeAnalyzer.
     *
     * @return The BarcodeAnalyzer instance, or null if not yet initialized.
     */
    public BarcodeAnalyzer getBarcodeAnalyzer() {
        return barcodeAnalyzer;
    }

    /**
     * Sets a callback to be notified when the BarcodeAnalyzer is ready.
     * This is useful for setting up crop regions or other configurations
     * that require the analyzer to be initialized first.
     *
     * @param callback The callback to invoke when the analyzer is ready.
     */
    public void setAnalyzerReadyCallback(AnalyzerReadyCallback callback) {
        this.analyzerReadyCallback = callback;
        // If analyzer is already ready, invoke callback immediately
        if (barcodeAnalyzer != null && callback != null) {
            callback.onAnalyzerReady(barcodeAnalyzer);
        }
    }

    /**
     * Decodes barcodes from a Bitmap image.
     * This method is useful for processing high-resolution captures for validation.
     *
     * @param bitmap The bitmap to decode barcodes from
     * @param rotationDegrees The rotation degrees of the image (0, 90, 180, 270)
     * @return A CompletableFuture containing the list of detected barcode entities,
     *         or an empty list if the decoder is not available
     */
    public CompletableFuture<List<BarcodeEntity>> decodeBitmap(Bitmap bitmap, int rotationDegrees) {
        if (barcodeDecoder == null || bitmap == null) {
            LogUtils.w(TAG, "BarcodeDecoder not available for bitmap decoding");
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        try {
            ImageData imageData = ImageData.fromBitmap(bitmap, rotationDegrees);
            return barcodeDecoder.process(imageData);
        } catch (AIVisionSDKException e) {
            LogUtils.e(TAG, "Error decoding bitmap: " + e.getMessage());
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
    }

    /**
     * Checks if the barcode decoder is ready for processing.
     *
     * @return true if the decoder is initialized and ready, false otherwise
     */
    public boolean isDecoderReady() {
        return barcodeDecoder != null;
    }
}
