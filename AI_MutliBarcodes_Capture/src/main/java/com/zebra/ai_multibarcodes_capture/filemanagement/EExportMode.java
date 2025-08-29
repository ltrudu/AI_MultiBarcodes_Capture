package com.zebra.ai_multibarcodes_capture.filemanagement;

import androidx.annotation.NonNull;

public enum EExportMode {
    TEXT("TEXT"),
    CSV("CSV"),
    EXCEL("EXCEL");

    private String name = "";
    EExportMode(String name)
    {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public String getExtension()
    {
        switch(this) {
            case TEXT:
                return ".txt";
            case CSV:
                return ".csv";
            case EXCEL:
                return ".xlsx";
        }
        return ".txt";
    }

    public static EExportMode fromExtension(String value)
    {
        switch(value)
        {
            case ".csv":
                return EExportMode.CSV;
            case ".xlsx":
                return EExportMode.EXCEL;
            default:
                return EExportMode.TEXT;
        }
    }
}
