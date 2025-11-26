package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.TextView;

import com.zebra.ai_multibarcodes_capture.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Locale;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_THEME;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.SHARED_PREFERENCES_THEME_DEFAULT;

public class ThemeHelpers {
    public static void configureSystemBars(AppCompatActivity activity, int resourceID) {
        Window window = activity.getWindow();

        // 1. Set the Navigation Bar Background Color to Black
        window.setNavigationBarColor(Color.BLACK);

        // 2. Control the Navigation Bar Icon Color (Light/White)
        // Ensure the system bars are drawn over the app's content
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Use the compatibility controller for managing bar appearance
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        // Request light navigation bar icons (white)
        // Setting this to 'false' tells the system to use light icons on a dark background.
        controller.setAppearanceLightNavigationBars(false);

        // Force status bar color
        View rootLayout = activity.findViewById(resourceID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+

            // 1. Set Navigation Bar background color using the WindowInsetsListener on decorView
            window.getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    // Set the background color to the view (decorView) - BLACK for navigation bar
                    view.setBackgroundColor(Color.BLACK);
                    return insets;
                }
            });

            // 2. Handle Status Bar color and Root Layout padding using ViewCompat
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
                // Get the system bar insets (status bar and navigation bar area)
                // Use getInsets(WindowInsetsCompat.Type.systemBars())
                // equivalent to the Kotlin line
                androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                int statusBarHeight = insets.top;

                // Below code is for adding padding top and bottom (setting margins on the rootLayout)
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;

                    // The Kotlin updateLayoutParams<MarginLayoutParams> block is equivalent to this:
                    marginLp.topMargin = insets.top;
                    marginLp.bottomMargin = insets.bottom;
                    v.setLayoutParams(marginLp); // Apply the updated layout params
                }


                // 3. Create and add a separate Status Bar View
                View statusBarView = new View(activity.getApplicationContext());

                // Below code is for setting color and height to notification bar
                // Height is the status bar height
                statusBarView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight
                ));

                // Set the status bar color using ContextCompat
                statusBarView.setBackgroundColor(androidx.appcompat.R.attr.colorPrimary);

                // Add the view to the activity's content view group
                activity.addContentView(statusBarView, statusBarView.getLayoutParams());

                // Consume the insets so they aren't passed down further
                return WindowInsetsCompat.CONSUMED;
            });

        } else {
            // For Android 14 and below
            window.setStatusBarColor(androidx.appcompat.R.attr.colorPrimary);
        }
    }

    public static void applyTheme(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

        if ("modern".equals(theme)) {
            activity.setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_Modern);
        } else {
            activity.setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_Legacy);
        }
    }

    /**
     * Checks if the current device locale uses Latin script (ABCD letters).
     * Returns true for languages like English, German, French, Spanish, Italian, etc.
     * Returns false for languages using other scripts like Arabic, Cyrillic, CJK, etc.
     *
     * @return true if the current locale uses Latin script, false otherwise
     */
    public static boolean isLatinScript() {
        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();

        // List of language codes that use Latin script
        // ISO 639-1 language codes for Latin-script languages
        String[] latinScriptLanguages = {
            "en", // English
            "de", // German
            "fr", // French
            "es", // Spanish
            "it", // Italian
            "pt", // Portuguese
            "nl", // Dutch
            "pl", // Polish
            "cs", // Czech
            "sk", // Slovak
            "hu", // Hungarian
            "ro", // Romanian
            "fi", // Finnish
            "sv", // Swedish
            "no", // Norwegian
            "da", // Danish
            "is", // Icelandic
            "et", // Estonian
            "lv", // Latvian
            "lt", // Lithuanian
            "sl", // Slovenian
            "hr", // Croatian
            "bs", // Bosnian
            "sq", // Albanian
            "af", // Afrikaans
            "eu", // Basque
            "ca", // Catalan
            "gl", // Galician
            "ga", // Irish
            "cy", // Welsh
            "mt", // Maltese
            "tr", // Turkish
            "az", // Azerbaijani
            "uz", // Uzbek
            "tk", // Turkmen
            "id", // Indonesian
            "ms", // Malay
            "tl", // Tagalog
            "vi", // Vietnamese
            "sw", // Swahili
            "zu", // Zulu
            "xh"  // Xhosa
        };

        // Check if the current language is in the Latin script list
        for (String latinLang : latinScriptLanguages) {
            if (latinLang.equals(language)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Applies the ZebraSans custom font to the activity if using modern theme
     * and the device locale uses Latin script.
     *
     * This method should be called after setContentView() in the activity.
     *
     * @param activity The activity to apply the font to
     */
    public static void applyCustomFont(AppCompatActivity activity) {
        // Check if we should apply custom font
        SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

        // Only apply custom font for modern theme with Latin script
        if (!"modern".equals(theme) || !isLatinScript()) {
            return;
        }

        try {
            // Load the custom font from assets
            Typeface zebraFont = Typeface.createFromAsset(activity.getAssets(), "ZebraMono-Medium.otf");

            // Get the root view
            ViewGroup rootView = activity.findViewById(android.R.id.content);
            if (rootView != null) {
                applyFontToViewGroup(rootView, zebraFont);
            }
        } catch (Exception e) {
            // If font loading fails, silently continue with default font
            e.printStackTrace();
        }
    }

    /**
     * Recursively applies a typeface to all TextViews and Buttons in a ViewGroup
     *
     * @param viewGroup The ViewGroup to apply the font to
     * @param typeface The Typeface to apply
     */
    private static void applyFontToViewGroup(ViewGroup viewGroup, Typeface typeface) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof TextView) {
                ((TextView) child).setTypeface(typeface);
            } else if (child instanceof Button) {
                ((Button) child).setTypeface(typeface);
            }

            if (child instanceof ViewGroup) {
                applyFontToViewGroup((ViewGroup) child, typeface);
            }
        }
    }
}
