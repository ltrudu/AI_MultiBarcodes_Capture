package com.zebra.ai_multibarcodes_capture;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    TextView tvStatus = null;
    TextView tvLoading = null;
    private Handler title_animation_handler;
    private Runnable title_animation_runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure system bars to match the theme
        configureSystemBars();
        
        setContentView(R.layout.activity_splash);
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
                            tvStatus.setText("Success Granting Permissions.");
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

    private void configureSystemBars() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Set status bar color to zebra blue
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.zebra));
        
        // Set navigation bar color to black for consistent theming
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.black));
        }
        
        // Set status bar text to light (white) to contrast with blue background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_STATUS_BAR to use light (white) text
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        
        // Set navigation bar text to light (white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR to use light (white) icons
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
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
}