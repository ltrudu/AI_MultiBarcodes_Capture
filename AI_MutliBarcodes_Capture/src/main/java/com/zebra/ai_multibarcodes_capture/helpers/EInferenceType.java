package com.zebra.ai_multibarcodes_capture.helpers;

import android.content.Context;
import com.zebra.ai_multibarcodes_capture.R;

public enum EInferenceType {
    DSP(R.string.inference_type_dsp, R.string.inference_type_dsp_description),
    GPU(R.string.inference_type_gpu, R.string.inference_type_gpu_description),
    CPU(R.string.inference_type_cpu, R.string.inference_type_cpu_description);

    private final int shortDescriptionResId;
    private final int longDescriptionResId;

    EInferenceType(int shortDescriptionResId, int longDescriptionResId) {
        this.shortDescriptionResId = shortDescriptionResId;
        this.longDescriptionResId = longDescriptionResId;
    }

    public static EInferenceType fromString(String shortDescription, Context context) {
        if (shortDescription == null || context == null) {
            return null;
        }

        for (EInferenceType inferenceType : EInferenceType.values()) {
            if (shortDescription.equals(context.getString(inferenceType.shortDescriptionResId))) {
                return inferenceType;
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