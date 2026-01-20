package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import com.zebra.ai_multibarcodes_capture.R;

public enum ECameraResolution {
    MP_1(R.string.camera_resolution_1mp, R.string.camera_resolution_1mp_description,1280,720),
    MP_2(R.string.camera_resolution_2mp, R.string.camera_resolution_2mp_description,1920,1080),
    MP_4(R.string.camera_resolution_4mp, R.string.camera_resolution_4mp_description,2688,1512),
    MP_8(R.string.camera_resolution_8mp, R.string.camera_resolution_8mp_description,3840,2160),
    MP_12(R.string.camera_resolution_12mp, R.string.camera_resolution_12mp_description,4000,3000),
    MP_12_5(R.string.camera_resolution_12_5mp, R.string.camera_resolution_12_5mp_description,4080,3060),
    MP_12_6(R.string.camera_resolution_12_6mp, R.string.camera_resolution_12_6mp_description,4096,3072);

    private final int shortDescriptionResId;
    private final int longDescriptionResId;
    private final int width, height;

    ECameraResolution(int shortDescriptionResId, int longDescriptionResId, int width, int height) {
        this.shortDescriptionResId = shortDescriptionResId;
        this.longDescriptionResId = longDescriptionResId;
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return height;
    }

    public static ECameraResolution fromString(String shortDescription, Context context) {
        if (shortDescription == null || context == null) {
            return null;
        }

        for (ECameraResolution resolution : ECameraResolution.values()) {
            if (shortDescription.equals(context.getString(resolution.shortDescriptionResId))) {
                return resolution;
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