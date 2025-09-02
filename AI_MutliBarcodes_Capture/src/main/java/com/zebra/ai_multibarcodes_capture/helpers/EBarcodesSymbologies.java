package com.zebra.ai_multibarcodes_capture.helpers;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

import androidx.annotation.NonNull;

public enum  EBarcodesSymbologies {
    UNKNOWN("UNKNOWN", -1, false, "LABEL-TYPE-UNKNOWN"),
    AUSTRALIAN_POSTAL("AUSTRALIAN POSTAL", 26, false, "LABEL-TYPE-AUSPOSTAL"),
    AZTEC("AZTEC", 4, true, "LABEL-TYPE-AZTEC"),
    CANADIAN_POSTAL("CANADIAN POSTAL", 27, false, "LABEL-TYPE-CANPOSTAL"),
    CHINESE_2OF5("CHINESE 2OF5", 28, false, "LABEL-TYPE-CHINESE2OF5"),
    CODABAR("CODABAR", 1, true, "LABEL-TYPE-CODABAR"),
    CODE11("CODE11", 2, false, "LABEL-TYPE-CODE11"),
    CODE39("CODE39", 3, true, "LABEL-TYPE-CODE39"),
    CODE93("CODE93", 5, false, "LABEL-TYPE-CODE93"),
    CODE128("CODE128", 6, true, "LABEL-TYPE-CODE128"),
    COMPOSITE_AB("COMPOSITE AB", 7, false, "LABEL-TYPE-COMPOSITEAB"),
    COMPOSITE_C("COMPOSITE C", 8, false, "LABEL-TYPE-COMPOSITEC"),
    D2OF5("D2OF5", 9, false, "LABEL-TYPE-D2OF5"),
    DATAMATRIX("DATAMATRIX", 10, true, "LABEL-TYPE-DATAMATRIX"),
    DOTCODE("DOTCODE", 11, false, "LABEL-TYPE-DOTCODE"),
    DUTCH_POSTAL("DUTCH POSTAL", 29, false, "LABEL-TYPE-DUTCHPOSTAL"),
    EAN_8("EAN 8", 12, true, "LABEL-TYPE-EAN8"),
    EAN_13("EAN 13", 13, true, "LABEL-TYPE-EAN13"),
    FINNISH_POSTAL_4S("FINNISH POSTAL 4S", 30, false, "LABEL-TYPE-FINNISHPOSTAL4S"),
    GRID_MATRIX("GRID MATRIX", 14, false, "LABEL-TYPE-GRIDMATRIX"),
    GS1_DATABAR("GS1 DATABAR", 15, true, "LABEL-TYPE-GS1DATABAR"),
    GS1_DATABAR_EXPANDED("GS1 DATABAR EXPANDED", 16, true, "LABEL-TYPE-GS1DATABAREXPANDED"),
    GS1_DATABAR_LIM("GS1 DATABAR LIM", 17, false, "LABEL-TYPE-GS1DATABARLIM"),
    GS1_DATAMATRIX("GS1 DATAMATRIX", 18, false, "LABEL-TYPE-GS1DATAMATRIX"),
    GS1_QRCODE("GS1 QRCODE", 19, false, "LABEL-TYPE-GS1QRCODE"),
    HANXIN("HANXIN", 20, false, "LABEL-TYPE-HANXIN"),
    I2OF5("I2OF5", 21, false, "LABEL-TYPE-I2OF5"),
    JAPANESE_POSTAL("JAPANESE POSTAL", 31, false, "LABEL-TYPE-JAPANESEPOSTAL"),
    KOREAN_3OF5("KOREAN 3OF5", 32, false, "LABEL-TYPE-KOREAN3OF5"),
    MAILMARK("MAILMARK", 33, true, "LABEL-TYPE-MAILMARK"),
    MATRIX_2OF5("MATRIX 2OF5", 34, false, "LABEL-TYPE-MATRIX2OF5"),
    MAXICODE("MAXICODE", 22, true, "LABEL-TYPE-MAXICODE"),
    MICROPDF("MICROPDF", 23, false, "LABEL-TYPE-MICROPDF"),
    MICROQR("MICROQR", 24, false, "LABEL-TYPE-MICROQR"),
    MSI("MSI", 25, false, "LABEL-TYPE-MSI"),
    PDF417("PDF417", 35, true, "LABEL-TYPE-PDF417"),
    QRCODE("QRCODE", 36, true, "LABEL-TYPE-QRCODE"),
    TLC39("TLC39", 37, false, "LABEL-TYPE-TLC39"),
    TRIOPTIC39("TRIOPTIC39", 38, false, "LABEL-TYPE-TRIOPTIC39"),
    UK_POSTAL("UK POSTAL", 39, false, "LABEL-TYPE-UKPOSTAL"),
    UPC_A("UPC A", 40, true, "LABEL-TYPE-UPCA"),
    UPC_E("UPC E", 41, true, "LABEL-TYPE-UPCE"),
    UPCE1("UPCE1", 42, false, "LABEL-TYPE-UPCE1"),
    USPLANET("USPLANET", 43, false, "LABEL-TYPE-USPLANET"),
    USPOSTNET("USPOSTNET", 44, false, "LABEL-TYPE-USPOSTNET"),
    US4STATE("US4STATE", 45, false, "LABEL-TYPE-US4STATE"),
    US4STATE_FICS("US4STATE FICS", 46, false, "LABEL-TYPE-US4STATEFICS");

    private String name = "";
    private int value = -1;
    private boolean defaultStatus = false;
    private String labelType = "";

    EBarcodesSymbologies(String name, int value, boolean defaultStatus, String labelType)
    {
        this.name = name;
        this.value = value;
        this.defaultStatus = defaultStatus;
        this.labelType = labelType;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public int getIntValue() {
        return value;
    }

    @NonNull
    public boolean getDefaultStatus() {
        return defaultStatus;
    }

    @NonNull
    public String getLabelType() {
        return labelType;
    }

    public static EBarcodesSymbologies fromName(String name)
    {
        switch(name)
        {
            case "AUSTRALIAN POSTAL":
                return EBarcodesSymbologies.AUSTRALIAN_POSTAL;
            case "AZTEC":
                return EBarcodesSymbologies.AZTEC;
            case "CANADIAN POSTAL":
                return EBarcodesSymbologies.CANADIAN_POSTAL;
            case "CHINESE 2OF5":
                return EBarcodesSymbologies.CHINESE_2OF5;
            case "CODABAR":
                return EBarcodesSymbologies.CODABAR;
            case "CODE11":
                return EBarcodesSymbologies.CODE11;
            case "CODE39":
                return EBarcodesSymbologies.CODE39;
            case "CODE93":
                return EBarcodesSymbologies.CODE93;
            case "CODE128":
                return EBarcodesSymbologies.CODE128;
            case "COMPOSITE AB":
                return EBarcodesSymbologies.COMPOSITE_AB;
            case "COMPOSITE C":
                return EBarcodesSymbologies.COMPOSITE_C;
            case "D2OF5":
                return EBarcodesSymbologies.D2OF5;
            case "DATAMATRIX":
                return EBarcodesSymbologies.DATAMATRIX;
            case "DOTCODE":
                return EBarcodesSymbologies.DOTCODE;
            case "DUTCH POSTAL":
                return EBarcodesSymbologies.DUTCH_POSTAL;
            case "EAN 8":
                return EBarcodesSymbologies.EAN_8;
            case "EAN 13":
                return EBarcodesSymbologies.EAN_13;
            case "FINNISH POSTAL 4S":
                return EBarcodesSymbologies.FINNISH_POSTAL_4S;
            case "GRID MATRIX":
                return EBarcodesSymbologies.GRID_MATRIX;
            case "GS1 DATABAR":
                return EBarcodesSymbologies.GS1_DATABAR;
            case "GS1 DATABAR EXPANDED":
                return EBarcodesSymbologies.GS1_DATABAR_EXPANDED;
            case "GS1 DATABAR LIM":
                return EBarcodesSymbologies.GS1_DATABAR_LIM;
            case "GS1 DATAMATRIX":
                return EBarcodesSymbologies.GS1_DATAMATRIX;
            case "GS1 QRCODE":
                return EBarcodesSymbologies.GS1_QRCODE;
            case "HANXIN":
                return EBarcodesSymbologies.HANXIN;
            case "I2OF5":
                return EBarcodesSymbologies.I2OF5;
            case "JAPANESE POSTAL":
                return EBarcodesSymbologies.JAPANESE_POSTAL;
            case "KOREAN 3OF5":
                return EBarcodesSymbologies.KOREAN_3OF5;
            case "MAILMARK":
                return EBarcodesSymbologies.MAILMARK;
            case "MATRIX 2OF5":
                return EBarcodesSymbologies.MATRIX_2OF5;
            case "MAXICODE":
                return EBarcodesSymbologies.MAXICODE;
            case "MICROPDF":
                return EBarcodesSymbologies.MICROPDF;
            case "MICROQR":
                return EBarcodesSymbologies.MICROQR;
            case "MSI":
                return EBarcodesSymbologies.MSI;
            case "PDF417":
                return EBarcodesSymbologies.PDF417;
            case "QRCODE":
                return EBarcodesSymbologies.QRCODE;
            case "TLC39":
                return EBarcodesSymbologies.TLC39;
            case "TRIOPTIC39":
                return EBarcodesSymbologies.TRIOPTIC39;
            case "UK POSTAL":
                return EBarcodesSymbologies.UK_POSTAL;
            case "UPC A":
                return EBarcodesSymbologies.UPC_A;
            case "UPC E":
                return EBarcodesSymbologies.UPC_E;
            case "UPCE1":
                return EBarcodesSymbologies.UPCE1;
            case "USPLANET":
                return EBarcodesSymbologies.USPLANET;
            case "USPOSTNET":
                return EBarcodesSymbologies.USPOSTNET;
            case "US4STATE":
                return EBarcodesSymbologies.US4STATE;
            case "US4STATE FICS":
                return EBarcodesSymbologies.US4STATE_FICS;
            default:
                return EBarcodesSymbologies.UNKNOWN;
        }
    }

    public static EBarcodesSymbologies fromInt(int value)
    {
        switch(value)
        {
            case 1:
                return EBarcodesSymbologies.CODABAR;
            case 2:
                return EBarcodesSymbologies.CODE11;
            case 3:
                return EBarcodesSymbologies.CODE39;
            case 4:
                return EBarcodesSymbologies.AZTEC;
            case 5:
                return EBarcodesSymbologies.CODE93;
            case 6:
                return EBarcodesSymbologies.CODE128;
            case 7:
                return EBarcodesSymbologies.COMPOSITE_AB;
            case 8:
                return EBarcodesSymbologies.COMPOSITE_C;
            case 9:
                return EBarcodesSymbologies.D2OF5;
            case 10:
                return EBarcodesSymbologies.DATAMATRIX;
            case 11:
                return EBarcodesSymbologies.DOTCODE;
            case 12:
                return EBarcodesSymbologies.EAN_8;
            case 13:
                return EBarcodesSymbologies.EAN_13;
            case 14:
                return EBarcodesSymbologies.GRID_MATRIX;
            case 15:
                return EBarcodesSymbologies.GS1_DATABAR;
            case 16:
                return EBarcodesSymbologies.GS1_DATABAR_EXPANDED;
            case 17:
                return EBarcodesSymbologies.GS1_DATABAR_LIM;
            case 18:
                return EBarcodesSymbologies.GS1_DATAMATRIX;
            case 19:
                return EBarcodesSymbologies.GS1_QRCODE;
            case 20:
                return EBarcodesSymbologies.HANXIN;
            case 21:
                return EBarcodesSymbologies.I2OF5;
            case 22:
                return EBarcodesSymbologies.MAXICODE;
            case 23:
                return EBarcodesSymbologies.MICROPDF;
            case 24:
                return EBarcodesSymbologies.MICROQR;
            case 25:
                return EBarcodesSymbologies.MSI;
            case 26:
                return EBarcodesSymbologies.AUSTRALIAN_POSTAL;
            case 27:
                return EBarcodesSymbologies.CANADIAN_POSTAL;
            case 28:
                return EBarcodesSymbologies.CHINESE_2OF5;
            case 29:
                return EBarcodesSymbologies.DUTCH_POSTAL;
            case 30:
                return EBarcodesSymbologies.FINNISH_POSTAL_4S;
            case 31:
                return EBarcodesSymbologies.JAPANESE_POSTAL;
            case 32:
                return EBarcodesSymbologies.KOREAN_3OF5;
            case 33:
                return EBarcodesSymbologies.MAILMARK;
            case 34:
                return EBarcodesSymbologies.MATRIX_2OF5;
            case 35:
                return EBarcodesSymbologies.PDF417;
            case 36:
                return EBarcodesSymbologies.QRCODE;
            case 37:
                return EBarcodesSymbologies.TLC39;
            case 38:
                return EBarcodesSymbologies.TRIOPTIC39;
            case 39:
                return EBarcodesSymbologies.UK_POSTAL;
            case 40:
                return EBarcodesSymbologies.UPC_A;
            case 41:
                return EBarcodesSymbologies.UPC_E;
            case 42:
                return EBarcodesSymbologies.UPCE1;
            case 43:
                return EBarcodesSymbologies.USPLANET;
            case 44:
                return EBarcodesSymbologies.USPOSTNET;
            case 45:
                return EBarcodesSymbologies.US4STATE;
            case 46:
                return EBarcodesSymbologies.US4STATE_FICS;
            default:
                return EBarcodesSymbologies.UNKNOWN;
        }
    }


    public static EBarcodesSymbologies fromLabelType(String labelType)
    {
        switch(labelType)
        {
            case "LABEL-TYPE-AUSPOSTAL":
                return EBarcodesSymbologies.AUSTRALIAN_POSTAL;
            case "LABEL-TYPE-AZTEC":
                return EBarcodesSymbologies.AZTEC;
            case "LABEL-TYPE-CANPOSTAL":
                return EBarcodesSymbologies.CANADIAN_POSTAL;
            case "LABEL-TYPE-CHINESE2OF5":
                return EBarcodesSymbologies.CHINESE_2OF5;
            case "LABEL-TYPE-CODABAR":
                return EBarcodesSymbologies.CODABAR;
            case "LABEL-TYPE-CODE11":
                return EBarcodesSymbologies.CODE11;
            case "LABEL-TYPE-CODE39":
                return EBarcodesSymbologies.CODE39;
            case "LABEL-TYPE-CODE93":
                return EBarcodesSymbologies.CODE93;
            case "LABEL-TYPE-CODE128":
                return EBarcodesSymbologies.CODE128;
            case "LABEL-TYPE-COMPOSITEAB":
                return EBarcodesSymbologies.COMPOSITE_AB;
            case "LABEL-TYPE-COMPOSITEC":
                return EBarcodesSymbologies.COMPOSITE_C;
            case "LABEL-TYPE-D2OF5":
                return EBarcodesSymbologies.D2OF5;
            case "LABEL-TYPE-DATAMATRIX":
                return EBarcodesSymbologies.DATAMATRIX;
            case "LABEL-TYPE-DOTCODE":
                return EBarcodesSymbologies.DOTCODE;
            case "LABEL-TYPE-DUTCHPOSTAL":
                return EBarcodesSymbologies.DUTCH_POSTAL;
            case "LABEL-TYPE-EAN8":
                return EBarcodesSymbologies.EAN_8;
            case "LABEL-TYPE-EAN13":
                return EBarcodesSymbologies.EAN_13;
            case "LABEL-TYPE-FINNISHPOSTAL4S":
                return EBarcodesSymbologies.FINNISH_POSTAL_4S;
            case "LABEL-TYPE-GRIDMATRIX":
                return EBarcodesSymbologies.GRID_MATRIX;
            case "LABEL-TYPE-GS1DATABAR":
                return EBarcodesSymbologies.GS1_DATABAR;
            case "LABEL-TYPE-GS1DATABAREXPANDED":
                return EBarcodesSymbologies.GS1_DATABAR_EXPANDED;
            case "LABEL-TYPE-GS1DATABARLIM":
                return EBarcodesSymbologies.GS1_DATABAR_LIM;
            case "LABEL-TYPE-GS1DATAMATRIX":
                return EBarcodesSymbologies.GS1_DATAMATRIX;
            case "LABEL-TYPE-GS1QRCODE":
                return EBarcodesSymbologies.GS1_QRCODE;
            case "LABEL-TYPE-HANXIN":
                return EBarcodesSymbologies.HANXIN;
            case "LABEL-TYPE-I2OF5":
                return EBarcodesSymbologies.I2OF5;
            case "LABEL-TYPE-JAPANESEPOSTAL":
                return EBarcodesSymbologies.JAPANESE_POSTAL;
            case "LABEL-TYPE-KOREAN3OF5":
                return EBarcodesSymbologies.KOREAN_3OF5;
            case "LABEL-TYPE-MAILMARK":
                return EBarcodesSymbologies.MAILMARK;
            case "LABEL-TYPE-MATRIX2OF5":
                return EBarcodesSymbologies.MATRIX_2OF5;
            case "LABEL-TYPE-MAXICODE":
                return EBarcodesSymbologies.MAXICODE;
            case "LABEL-TYPE-MICROPDF":
                return EBarcodesSymbologies.MICROPDF;
            case "LABEL-TYPE-MICROQR":
                return EBarcodesSymbologies.MICROQR;
            case "LABEL-TYPE-MSI":
                return EBarcodesSymbologies.MSI;
            case "LABEL-TYPE-PDF417":
                return EBarcodesSymbologies.PDF417;
            case "LABEL-TYPE-QRCODE":
                return EBarcodesSymbologies.QRCODE;
            case "LABEL-TYPE-TLC39":
                return EBarcodesSymbologies.TLC39;
            case "LABEL-TYPE-TRIOPTIC39":
                return EBarcodesSymbologies.TRIOPTIC39;
            case "LABEL-TYPE-UKPOSTAL":
                return EBarcodesSymbologies.UK_POSTAL;
            case "LABEL-TYPE-UPCA":
                return EBarcodesSymbologies.UPC_A;
            case "LABEL-TYPE-UPCE":
                return EBarcodesSymbologies.UPC_E;
            case "LABEL-TYPE-UPCE1":
                return EBarcodesSymbologies.UPCE1;
            case "LABEL-TYPE-USPLANET":
                return EBarcodesSymbologies.USPLANET;
            case "LABEL-TYPE-USPOSTNET":
                return EBarcodesSymbologies.USPOSTNET;
            case "LABEL-TYPE-US4STATE":
                return EBarcodesSymbologies.US4STATE;
            case "LABEL-TYPE-US4STATEFICS":
                return EBarcodesSymbologies.US4STATE_FICS;
            default:
                return EBarcodesSymbologies.UNKNOWN;
        }
    }

}
