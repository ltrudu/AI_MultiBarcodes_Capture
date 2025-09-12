package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import com.zebra.ai_multibarcodes_capture.R;

public enum EModelInputSize {
    SMALL(R.string.resolution_small, R.string.resolution_small_description, 640, 640),
    MEDIUM(R.string.resolution_medium, R.string.resolution_medium_description,1280,1280),
    LARGE(R.string.resolution_large, R.string.resolution_large_description,1600,1600);

    private final int shortDescriptionResId;
    private final int longDescriptionResId;
    private final int width, height;

    EModelInputSize(int shortDescriptionResId, int longDescriptionResId, int width, int height) {
        this.shortDescriptionResId = shortDescriptionResId;
        this.longDescriptionResId = longDescriptionResId;
        this.width = width;
        this.height = height;
    }

    public static EModelInputSize fromString(String shortDescription, Context context) {
        if (shortDescription == null || context == null) {
            return null;
        }

        for (EModelInputSize choice : EModelInputSize.values()) {
            if (shortDescription.equals(context.getString(choice.shortDescriptionResId))) {
                return choice;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public String toString(Context context) {
        if (context == null) {
            return toString();
        }
        return context.getString(shortDescriptionResId);
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return height;
    }

    public String getDescription(Context context) {
        if (context == null) {
            return "";
        }
        return context.getString(longDescriptionResId);
    }

    public int getShortDescriptionResId() {
        return shortDescriptionResId;
    }

    public int getLongDescriptionResId() {
        return longDescriptionResId;
    }
}