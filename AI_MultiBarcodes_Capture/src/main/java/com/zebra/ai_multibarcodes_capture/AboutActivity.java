// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AboutActivity displays information about the application and its dependencies.
 * Shows the app version and versions of key libraries used in the project.
 */
public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up the toolbar and back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set title
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText(R.string.activity_about);

        // Display app version
        TextView appVersionValue = findViewById(R.id.app_version_value);
        String versionInfo = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        appVersionValue.setText(versionInfo);

        // Display Zebra AI Vision SDK version
        TextView zebraAiVersionValue = findViewById(R.id.zebra_ai_vision_sdk_value);
        zebraAiVersionValue.setText(getVersionString("zebraAIVisionSdk"));

        // Display Barcode Localizer version
        TextView barcodeLocalizerValue = findViewById(R.id.barcode_localizer_value);
        barcodeLocalizerValue.setText(getVersionString("barcodeLocalizer"));

        // Display Critical Permission Helper version
        TextView criticalPermissionHelperValue = findViewById(R.id.critical_permission_helper_value);
        criticalPermissionHelperValue.setText(getVersionString("criticalpermissionhelper"));

        // Display DataWedge Intent Wrapper version
        TextView datawedgeIntentWrapperValue = findViewById(R.id.datawedge_intent_wrapper_value);
        datawedgeIntentWrapperValue.setText(getVersionString("datawedgeintentwrapper"));

        // Set up GitHub repository link
        TextView repositoryLink = findViewById(R.id.repository_link);
        repositoryLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubRepository();
            }
        });

        // Set up license view button
        Button viewLicenseButton = findViewById(R.id.view_license_button);
        viewLicenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLicensePdf();
            }
        });
    }

    /**
     * Opens the GitHub repository in a web browser
     */
    private void openGitHubRepository() {
        String url = getString(R.string.about_repository_url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Opens the license PDF file using an external PDF viewer
     */
    private void openLicensePdf() {
        try {
            LogUtils.d(TAG, "Opening license PDF");

            // Copy PDF from assets to cache directory if needed
            File pdfFile = new File(getCacheDir(), "Zebra_Development_Tool_License.pdf");
            LogUtils.d(TAG, "PDF file path: " + pdfFile.getAbsolutePath());

            if (!pdfFile.exists()) {
                LogUtils.d(TAG, "PDF file doesn't exist in cache, copying from assets");
                copyAssetToCache("Zebra_Development_Tool_License.pdf", pdfFile);
                LogUtils.d(TAG, "PDF copied successfully, file size: " + pdfFile.length());
            } else {
                LogUtils.d(TAG, "PDF already exists in cache, file size: " + pdfFile.length());
            }

            // Get URI using FileProvider
            Uri pdfUri = FileProvider.getUriForFile(
                this,
                "com.zebra.ai_multibarcodes_capture.dev.fileprovider",
                pdfFile
            );
            LogUtils.d(TAG, "FileProvider URI: " + pdfUri.toString());

            // Create intent to view PDF
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Try to start the activity with a chooser to ensure it works
            try {
                startActivity(Intent.createChooser(intent, "Open License PDF"));
                LogUtils.d(TAG, "PDF viewer intent started successfully");
            } catch (Exception e) {
                LogUtils.e(TAG, "Failed to open PDF with chooser", e);
                Toast.makeText(this, "No PDF viewer app found. Please install a PDF reader.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error opening license PDF", e);
            Toast.makeText(this, "Error opening license: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Copies a file from assets to the cache directory
     */
    private void copyAssetToCache(String assetName, File outputFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getAssets().open(assetName);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Gets version string for a dependency from gradle version catalog.
     * Versions are dynamically loaded from BuildConfig which reads from libs.versions.toml
     *
     * @param dependencyName Name of the dependency
     * @return Version string
     */
    private String getVersionString(String dependencyName) {
        // Version strings dynamically loaded from gradle/libs.versions.toml via BuildConfig
        switch (dependencyName) {
            case "zebraAIVisionSdk":
                return BuildConfig.ZEBRA_AI_VISION_SDK_VERSION;
            case "barcodeLocalizer":
                return BuildConfig.BARCODE_LOCALIZER_VERSION;
            case "criticalpermissionhelper":
                return BuildConfig.CRITICAL_PERMISSION_HELPER_VERSION;
            case "datawedgeintentwrapper":
                return BuildConfig.DATAWEDGE_INTENT_WRAPPER_VERSION;
            default:
                return "Unknown";
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}
