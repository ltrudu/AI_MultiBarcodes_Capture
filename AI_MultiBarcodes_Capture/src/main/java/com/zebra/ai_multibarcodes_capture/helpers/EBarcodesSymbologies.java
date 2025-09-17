package com.zebra.ai_multibarcodes_capture.helpers;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

import androidx.annotation.NonNull;

public enum  EBarcodesSymbologies {
    UNKNOWN("UNKNOWN", -1, false, "LABEL-TYPE-UNKNOWN"),
    EAN_8("EAN 8", 0, true, "LABEL-TYPE-EAN8"),
    EAN_13("EAN 13", 1, true, "LABEL-TYPE-EAN13"),
    UPC_A("UPC A", 2, true, "LABEL-TYPE-UPCA"),
    UPC_E("UPC E", 3, true, "LABEL-TYPE-UPCE"),
    AZTEC("AZTEC", 4, true, "LABEL-TYPE-AZTEC"),
    CODABAR("CODABAR", 5, true, "LABEL-TYPE-CODABAR"),
    CODE128("CODE128", 6, true, "LABEL-TYPE-CODE128"),
    CODE39("CODE39", 7, true, "LABEL-TYPE-CODE39"),
    I2OF5("I2OF5", 8, false, "LABEL-TYPE-I2OF5"),
    GS1_DATABAR("GS1 DATABAR", 9, true, "LABEL-TYPE-GS1DATABAR"),
    DATAMATRIX("DATAMATRIX", 10, true, "LABEL-TYPE-DATAMATRIX"),
    GS1_DATABAR_EXPANDED("GS1 DATABAR EXPANDED", 11, true, "LABEL-TYPE-GS1DATABAREXPANDED"),
    MAILMARK("MAILMARK", 12, true, "LABEL-TYPE-MAILMARK"),
    MAXICODE("MAXICODE", 13, true, "LABEL-TYPE-MAXICODE"),
    PDF417("PDF417", 14, true, "LABEL-TYPE-PDF417"),
    QRCODE("QRCODE", 15, true, "LABEL-TYPE-QRCODE"),
    DOTCODE("DOTCODE", 16, false, "LABEL-TYPE-DOTCODE"),
    GRID_MATRIX("GRID MATRIX", 17, false, "LABEL-TYPE-GRIDMATRIX"),
    GS1_DATAMATRIX("GS1 DATAMATRIX", 18, false, "LABEL-TYPE-GS1DATAMATRIX"),
    GS1_QRCODE("GS1 QRCODE", 19, false, "LABEL-TYPE-GS1QRCODE"),
    MICROQR("MICROQR", 20, false, "LABEL-TYPE-MICROQR"),
    MICROPDF("MICROPDF", 21, false, "LABEL-TYPE-MICROPDF"),
    USPOSTNET("USPOSTNET", 22, false, "LABEL-TYPE-USPOSTNET"),
    USPLANET("USPLANET", 23, false, "LABEL-TYPE-USPLANET"),
    UK_POSTAL("UK POSTAL", 24, false, "LABEL-TYPE-UKPOSTAL"),
    JAPANESE_POSTAL("JAPANESE POSTAL", 25, false, "LABEL-TYPE-JAPANESEPOSTAL"),
    AUSTRALIAN_POSTAL("AUSTRALIAN POSTAL", 26, false, "LABEL-TYPE-AUSPOSTAL"),
    CANADIAN_POSTAL("CANADIAN POSTAL", 27, false, "LABEL-TYPE-CANPOSTAL"),
    DUTCH_POSTAL("DUTCH POSTAL", 28, false, "LABEL-TYPE-DUTCHPOSTAL"),
    US4STATE("US4STATE", 29, false, "LABEL-TYPE-US4STATE"),
    US4STATE_FICS("US4STATE FICS", 30, false, "LABEL-TYPE-US4STATEFICS"),
    MSI("MSI", 31, false, "LABEL-TYPE-MSI"),
    CODE93("CODE93", 32, false, "LABEL-TYPE-CODE93"),
    TRIOPTIC39("TRIOPTIC39", 33, false, "LABEL-TYPE-TRIOPTIC39"),
    D2OF5("D2OF5", 34, false, "LABEL-TYPE-D2OF5"),
    CHINESE_2OF5("CHINESE 2OF5", 35, false, "LABEL-TYPE-CHINESE2OF5"),
    KOREAN_3OF5("KOREAN 3OF5", 36, false, "LABEL-TYPE-KOREAN3OF5"),
    CODE11("CODE11", 37, false, "LABEL-TYPE-CODE11"),
    TLC39("TLC39", 38, false, "LABEL-TYPE-TLC39"),
    HANXIN("HANXIN", 39, false, "LABEL-TYPE-HANXIN"),
    MATRIX_2OF5("MATRIX 2OF5", 40, false, "LABEL-TYPE-MATRIX2OF5"),
    UPCE1("UPCE1", 41, false, "LABEL-TYPE-UPCE1"),
    GS1_DATABAR_LIM("GS1 DATABAR LIM", 42, false, "LABEL-TYPE-GS1DATABARLIM"),
    FINNISH_POSTAL_4S("FINNISH POSTAL 4S", 43, false, "LABEL-TYPE-FINNISHPOSTAL4S"),
    COMPOSITE_AB("COMPOSITE AB", 44, false, "LABEL-TYPE-COMPOSITEAB"),
    COMPOSITE_C("COMPOSITE C", 45, false, "LABEL-TYPE-COMPOSITEC");

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
            case 0:
                return EBarcodesSymbologies.EAN_8;
            case 1:
                return EBarcodesSymbologies.EAN_13;
            case 2:
                return EBarcodesSymbologies.UPC_A;
            case 3:
                return EBarcodesSymbologies.UPC_E;
            case 4:
                return EBarcodesSymbologies.AZTEC;
            case 5:
                return EBarcodesSymbologies.CODABAR;
            case 6:
                return EBarcodesSymbologies.CODE128;
            case 7:
                return EBarcodesSymbologies.CODE39;
            case 8:
                return EBarcodesSymbologies.I2OF5;
            case 9:
                return EBarcodesSymbologies.GS1_DATABAR;
            case 10:
                return EBarcodesSymbologies.DATAMATRIX;
            case 11:
                return EBarcodesSymbologies.GS1_DATABAR_EXPANDED;
            case 12:
                return EBarcodesSymbologies.MAILMARK;
            case 13:
                return EBarcodesSymbologies.MAXICODE;
            case 14:
                return EBarcodesSymbologies.PDF417;
            case 15:
                return EBarcodesSymbologies.QRCODE;
            case 16:
                return EBarcodesSymbologies.DOTCODE;
            case 17:
                return EBarcodesSymbologies.GRID_MATRIX;
            case 18:
                return EBarcodesSymbologies.GS1_DATAMATRIX;
            case 19:
                return EBarcodesSymbologies.GS1_QRCODE;
            case 20:
                return EBarcodesSymbologies.MICROQR;
            case 21:
                return EBarcodesSymbologies.MICROPDF;
            case 22:
                return EBarcodesSymbologies.USPOSTNET;
            case 23:
                return EBarcodesSymbologies.USPLANET;
            case 24:
                return EBarcodesSymbologies.UK_POSTAL;
            case 25:
                return EBarcodesSymbologies.JAPANESE_POSTAL;
            case 26:
                return EBarcodesSymbologies.AUSTRALIAN_POSTAL;
            case 27:
                return EBarcodesSymbologies.CANADIAN_POSTAL;
            case 28:
                return EBarcodesSymbologies.DUTCH_POSTAL;
            case 29:
                return EBarcodesSymbologies.US4STATE;
            case 30:
                return EBarcodesSymbologies.US4STATE_FICS;
            case 31:
                return EBarcodesSymbologies.MSI;
            case 32:
                return EBarcodesSymbologies.CODE93;
            case 33:
                return EBarcodesSymbologies.TRIOPTIC39;
            case 34:
                return EBarcodesSymbologies.D2OF5;
            case 35:
                return EBarcodesSymbologies.CHINESE_2OF5;
            case 36:
                return EBarcodesSymbologies.KOREAN_3OF5;
            case 37:
                return EBarcodesSymbologies.CODE11;
            case 38:
                return EBarcodesSymbologies.TLC39;
            case 39:
                return EBarcodesSymbologies.HANXIN;
            case 40:
                return EBarcodesSymbologies.MATRIX_2OF5;
            case 41:
                return EBarcodesSymbologies.UPCE1;
            case 42:
                return EBarcodesSymbologies.GS1_DATABAR_LIM;
            case 43:
                return EBarcodesSymbologies.FINNISH_POSTAL_4S;
            case 44:
                return EBarcodesSymbologies.COMPOSITE_AB;
            case 45:
                return EBarcodesSymbologies.COMPOSITE_C;
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
