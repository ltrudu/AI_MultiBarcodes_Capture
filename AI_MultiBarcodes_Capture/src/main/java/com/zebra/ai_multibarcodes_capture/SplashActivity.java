package com.zebra.ai_multibarcodes_capture;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;

public class SplashActivity extends AppCompatActivity {

    TextView tvStatus = null;
    TextView tvLoading = null;
    private Handler title_animation_handler;
    private Runnable title_animation_runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeHelpers.applyTheme(this);

        setContentView(R.layout.activity_splash);

        // Configure system bars to match the theme
        ThemeHelpers.configureSystemBars(this, R.id.cl_activity_splash);

        tvStatus = findViewById(R.id.tvStatus);
        tvLoading = findViewById(R.id.tvLoading);

        if (MainApplication.permissionGranted == false) {
            setTitle(R.string.app_name);
            startPointsAnimations(getString(R.string.app_name), getString(R.string.loading_status));
            MainApplication.iMainApplicationCallback = new MainApplication.iMainApplicationCallback() {
                @Override
                public void onPermissionSuccess(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopPointsAnimations();
                            tvStatus.setText(getString(R.string.success_granting_permissions));
                            // Start MainActivity
                            Intent intent = new Intent(SplashActivity.this, EntryChoiceActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onPermissionError(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopPointsAnimations();
                            tvStatus.setText(message);
                        }
                    });
                }

                @Override
                public void onPermissionDebug(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(message);
                        }
                    });

                }
            };

            // System bars are now handled by configureSystemBars() method
        }
        else
        {
            stopPointsAnimations();
            Intent intent = new Intent(SplashActivity.this, EntryChoiceActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void startPointsAnimations(String baseTitle, String baseLoadingStatus) {
        final int maxDots = 5;
        title_animation_handler = new Handler(Looper.getMainLooper());
        title_animation_runnable = new Runnable() {
            int dotCount = 0;

            @Override
            public void run() {
                StringBuilder title = new StringBuilder(baseTitle);
                StringBuilder loadingStatus = new StringBuilder(baseLoadingStatus);
                for (int i = 0; i < dotCount; i++) {
                    title.append(".");
                    loadingStatus.append(".");
                }
                setTitle(title.toString());
                tvLoading.setText(loadingStatus.toString());
                dotCount = (dotCount + 1) % (maxDots + 1);
                title_animation_handler.postDelayed(this, 500); // Update every 500 milliseconds
            }
        };
        title_animation_handler.post(title_animation_runnable);
    }

    private void stopPointsAnimations() {
        if (title_animation_handler != null && title_animation_runnable != null) {
            title_animation_handler.removeCallbacks(title_animation_runnable);
            title_animation_handler = null;
            title_animation_runnable = null;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(R.string.app_name);
                tvLoading.setText(R.string.loading_status);
            }
        });
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

        if ("modern".equals(theme)) {
            setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_Modern);
        } else {
            setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_Legacy);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}