package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Base activity class that provides common functionality for all activities in the app.
 *
 * Features:
 * - Locale handling via attachBaseContext() - automatically applies the user's language preference
 *
 * All activities should extend this class instead of AppCompatActivity to ensure
 * consistent behavior across the app.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Handles locale configuration for the activity.
     * This ensures the user's language preference is applied before the activity is created.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}
