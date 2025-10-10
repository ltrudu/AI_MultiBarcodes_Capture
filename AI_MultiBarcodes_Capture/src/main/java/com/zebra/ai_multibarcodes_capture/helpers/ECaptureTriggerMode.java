package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;

import com.zebra.ai_multibarcodes_capture.R;

import androidx.annotation.NonNull;

public enum ECaptureTriggerMode {
    CAPTURE_ON_PRESS("press", R.string.capture_mode_press),
    CAPTURE_ON_RELEASE("release", R.string.capture_mode_release);

    private String key;
    private int nameResourceId;

    ECaptureTriggerMode(String key, int nameResourceId)
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

    public static ECaptureTriggerMode fromKey(String key)
    {
        switch(key)
        {
            case "press":
                return CAPTURE_ON_PRESS;
            case "release":
                return CAPTURE_ON_RELEASE;
            default:
                return CAPTURE_ON_PRESS;
        }
    }

    public static ECaptureTriggerMode fromDisplayName(Context context, String displayName)
    {
        for (ECaptureTriggerMode mode : values()) {
            if (mode.getDisplayName(context).equals(displayName)) {
                return mode;
            }
        }
        return CAPTURE_ON_PRESS;
    }

}
