package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;

import com.zebra.ai_multibarcodes_capture.R;

import androidx.annotation.NonNull;

public enum ETheme {
    MODERN("modern",R.string.theme_modern),
    LEGACY("release", R.string.theme_legacy);

    private String key;
    private int nameResourceId;

    ETheme(String key, int nameResourceId)
    {
        this.key = key;
        this.nameResourceId = nameResourceId;
    }

    @NonNull
    @Override
    public String toString() {
        return key;
    }

    public String getDisplayName(Context context) {
        return context.getString(nameResourceId);
    }

    public static ETheme fromKey(String key)
    {
        switch(key)
        {
            case "modern":
                return MODERN;
            case "legacy":
                return LEGACY;
            default:
                return MODERN;
        }
    }

    public static ETheme fromDisplayName(Context context, String displayName)
    {
        for (ETheme mode : values()) {
            if (mode.getDisplayName(context).equals(displayName)) {
                return mode;
            }
        }
        return MODERN;
    }

}
