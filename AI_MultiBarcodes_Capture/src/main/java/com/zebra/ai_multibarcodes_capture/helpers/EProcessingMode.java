package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import androidx.annotation.NonNull;
import com.zebra.ai_multibarcodes_capture.R;

public enum EProcessingMode {
    FILE("file", R.string.processing_mode_file),
    HTTPSPOST("https_post", R.string.processing_mode_https_post);

    private String key;
    private int nameResourceId;

    EProcessingMode(String key, int nameResourceId)
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

    public static EProcessingMode fromKey(String key)
    {
        switch(key)
        {
            case "file":
                return FILE;
            case "https_post":
                return HTTPSPOST;
            default:
                return FILE;
        }
    }

    public static EProcessingMode fromDisplayName(Context context, String displayName)
    {
        for (EProcessingMode mode : values()) {
            if (mode.getDisplayName(context).equals(displayName)) {
                return mode;
            }
        }
        return FILE;
    }

}
