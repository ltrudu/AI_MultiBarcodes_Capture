package com.zebra.ai_multibarcodes_capture.helpers;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

public class Constants {
    public static final String TAG = "AIMBCCapture";

    // Sharing provider
    public static final String PROVIDER_AUTHORITY = "com.zebra.ai_multibarcodes_capture.dev.fileprovider";
    public static final String PROVIDER_CACHE_FOLDER = "temp_files";

    // File constants
    public static final String FILE_TARGET_FOLDER = "AI_MultiBarcodes_Capture";
    public static final String FILE_DEFAULT_PREFIX = "MySession_";
    public static final String FILE_DEFAULT_EXTENSION = EExportMode.TEXT.getExtension();

    // File Browser Constants
    public static final String FILEBROWSER_EXTRA_FOLDER_PATH = "FOLDER_PATH";
    public static final String FILEBROWSER_EXTRA_PREFIX = "EXTRA_PREFIX";
    public static final String FILEBROWSER_EXTRA_EXTENSION = "EXTRA_EXTENSION";
    public static final String FILEBROWSER_EXTRA_MULTISELECT = "EXTRA_MULTISELECT";


    public static final String FILEBROWSER_RESULT_FILENAME = "RESULT_FILENAME";
    public static final String FILEBROWSER_RESULT_FILE_EXTENSION = "RESULT_FILE_EXTENSION";
    public static final String FILEBROWSER_RESULT_FILEPATH = "RESULT_FILEPATH";

    public static final String CAPTURE_FILE_PATH = "CAPTURE_FILE_PATH";
    public static final String ENDPOINT_URI = "ENDPOINT_URI";

    public static final String SHARED_PREFERENCES_LAST_SESSION_FILE = "lastsessionfile";
    public static final String SHARED_PREFERENCES_PREFIX = "prefix";
    public static final String SHARED_PREFERENCES_EXTENSION = "extension";
    public static final String SHARED_PREFERENCES_THEME = "theme";
    public static final String SHARED_PREFERENCES_THEME_DEFAULT = "legacy";

    // Barcode symbology preferences
    public static final String SHARED_PREFERENCES_AUSTRALIAN_POSTAL = "AUSTRALIAN_POSTAL";
    public static final boolean SHARED_PREFERENCES_AUSTRALIAN_POSTAL_DEFAULT = EBarcodesSymbologies.AUSTRALIAN_POSTAL.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_AZTEC = "AZTEC";
    public static final boolean SHARED_PREFERENCES_AZTEC_DEFAULT = EBarcodesSymbologies.AZTEC.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CANADIAN_POSTAL = "CANADIAN_POSTAL";
    public static final boolean SHARED_PREFERENCES_CANADIAN_POSTAL_DEFAULT = EBarcodesSymbologies.CANADIAN_POSTAL.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CHINESE_2OF5 = "CHINESE_2OF5";
    public static final boolean SHARED_PREFERENCES_CHINESE_2OF5_DEFAULT = EBarcodesSymbologies.CHINESE_2OF5.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CODABAR = "CODABAR";
    public static final boolean SHARED_PREFERENCES_CODABAR_DEFAULT = EBarcodesSymbologies.CODABAR.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CODE11 = "CODE11";
    public static final boolean SHARED_PREFERENCES_CODE11_DEFAULT = EBarcodesSymbologies.CODE11.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CODE39 = "CODE39";
    public static final boolean SHARED_PREFERENCES_CODE39_DEFAULT = EBarcodesSymbologies.CODE39.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CODE93 = "CODE93";
    public static final boolean SHARED_PREFERENCES_CODE93_DEFAULT = EBarcodesSymbologies.CODE93.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_CODE128 = "CODE128";
    public static final boolean SHARED_PREFERENCES_CODE128_DEFAULT = EBarcodesSymbologies.CODE128.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_COMPOSITE_AB = "COMPOSITE_AB";
    public static final boolean SHARED_PREFERENCES_COMPOSITE_AB_DEFAULT = EBarcodesSymbologies.COMPOSITE_AB.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_COMPOSITE_C = "COMPOSITE_C";
    public static final boolean SHARED_PREFERENCES_COMPOSITE_C_DEFAULT = EBarcodesSymbologies.COMPOSITE_C.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_D2OF5 = "D2OF5";
    public static final boolean SHARED_PREFERENCES_D2OF5_DEFAULT = EBarcodesSymbologies.D2OF5.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_DATAMATRIX = "DATAMATRIX";
    public static final boolean SHARED_PREFERENCES_DATAMATRIX_DEFAULT = EBarcodesSymbologies.DATAMATRIX.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_DOTCODE = "DOTCODE";
    public static final boolean SHARED_PREFERENCES_DOTCODE_DEFAULT = EBarcodesSymbologies.DOTCODE.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_DUTCH_POSTAL = "DUTCH_POSTAL";
    public static final boolean SHARED_PREFERENCES_DUTCH_POSTAL_DEFAULT = EBarcodesSymbologies.DUTCH_POSTAL.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_EAN_8 = "EAN_8";
    public static final boolean SHARED_PREFERENCES_EAN_8_DEFAULT = EBarcodesSymbologies.EAN_8.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_EAN_13 = "EAN_13";
    public static final boolean SHARED_PREFERENCES_EAN_13_DEFAULT = EBarcodesSymbologies.EAN_13.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_FINNISH_POSTAL_4S = "FINNISH_POSTAL_4S";
    public static final boolean SHARED_PREFERENCES_FINNISH_POSTAL_4S_DEFAULT = EBarcodesSymbologies.FINNISH_POSTAL_4S.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GRID_MATRIX = "GRID_MATRIX";
    public static final boolean SHARED_PREFERENCES_GRID_MATRIX_DEFAULT = EBarcodesSymbologies.GRID_MATRIX.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GS1_DATABAR = "GS1_DATABAR";
    public static final boolean SHARED_PREFERENCES_GS1_DATABAR_DEFAULT = EBarcodesSymbologies.GS1_DATABAR.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GS1_DATABAR_EXPANDED = "GS1_DATABAR_EXPANDED";
    public static final boolean SHARED_PREFERENCES_GS1_DATABAR_EXPANDED_DEFAULT = EBarcodesSymbologies.GS1_DATABAR_EXPANDED.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GS1_DATABAR_LIM = "GS1_DATABAR_LIM";
    public static final boolean SHARED_PREFERENCES_GS1_DATABAR_LIM_DEFAULT = EBarcodesSymbologies.GS1_DATABAR_LIM.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GS1_DATAMATRIX = "GS1_DATAMATRIX";
    public static final boolean SHARED_PREFERENCES_GS1_DATAMATRIX_DEFAULT = EBarcodesSymbologies.GS1_DATAMATRIX.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_GS1_QRCODE = "GS1_QRCODE";
    public static final boolean SHARED_PREFERENCES_GS1_QRCODE_DEFAULT = EBarcodesSymbologies.GS1_QRCODE.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_HANXIN = "HANXIN";
    public static final boolean SHARED_PREFERENCES_HANXIN_DEFAULT = EBarcodesSymbologies.HANXIN.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_I2OF5 = "I2OF5";
    public static final boolean SHARED_PREFERENCES_I2OF5_DEFAULT = EBarcodesSymbologies.I2OF5.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_JAPANESE_POSTAL = "JAPANESE_POSTAL";
    public static final boolean SHARED_PREFERENCES_JAPANESE_POSTAL_DEFAULT = EBarcodesSymbologies.JAPANESE_POSTAL.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_KOREAN_3OF5 = "KOREAN_3OF5";
    public static final boolean SHARED_PREFERENCES_KOREAN_3OF5_DEFAULT = EBarcodesSymbologies.KOREAN_3OF5.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MAILMARK = "MAILMARK";
    public static final boolean SHARED_PREFERENCES_MAILMARK_DEFAULT = EBarcodesSymbologies.MAILMARK.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MATRIX_2OF5 = "MATRIX_2OF5";
    public static final boolean SHARED_PREFERENCES_MATRIX_2OF5_DEFAULT = EBarcodesSymbologies.MATRIX_2OF5.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MAXICODE = "MAXICODE";
    public static final boolean SHARED_PREFERENCES_MAXICODE_DEFAULT = EBarcodesSymbologies.MAXICODE.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MICROPDF = "MICROPDF";
    public static final boolean SHARED_PREFERENCES_MICROPDF_DEFAULT = EBarcodesSymbologies.MICROPDF.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MICROQR = "MICROQR";
    public static final boolean SHARED_PREFERENCES_MICROQR_DEFAULT = EBarcodesSymbologies.MICROQR.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_MSI = "MSI";
    public static final boolean SHARED_PREFERENCES_MSI_DEFAULT = EBarcodesSymbologies.MSI.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_PDF417 = "PDF417";
    public static final boolean SHARED_PREFERENCES_PDF417_DEFAULT = EBarcodesSymbologies.PDF417.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_QRCODE = "QRCODE";
    public static final boolean SHARED_PREFERENCES_QRCODE_DEFAULT = EBarcodesSymbologies.QRCODE.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_TLC39 = "TLC39";
    public static final boolean SHARED_PREFERENCES_TLC39_DEFAULT = EBarcodesSymbologies.TLC39.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_TRIOPTIC39 = "TRIOPTIC39";
    public static final boolean SHARED_PREFERENCES_TRIOPTIC39_DEFAULT = EBarcodesSymbologies.TRIOPTIC39.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_UK_POSTAL = "UK_POSTAL";
    public static final boolean SHARED_PREFERENCES_UK_POSTAL_DEFAULT = EBarcodesSymbologies.UK_POSTAL.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_UPC_A = "UPC_A";
    public static final boolean SHARED_PREFERENCES_UPC_A_DEFAULT = EBarcodesSymbologies.UPC_A.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_UPC_E = "UPC_E";
    public static final boolean SHARED_PREFERENCES_UPC_E_DEFAULT = EBarcodesSymbologies.UPC_E.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_UPCE0 = "UPCE1";
    public static final boolean SHARED_PREFERENCES_UPCE0_DEFAULT = EBarcodesSymbologies.UPCE1.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_USPLANET = "USPLANET";
    public static final boolean SHARED_PREFERENCES_USPLANET_DEFAULT = EBarcodesSymbologies.USPLANET.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_USPOSTNET = "USPOSTNET";
    public static final boolean SHARED_PREFERENCES_USPOSTNET_DEFAULT = EBarcodesSymbologies.USPOSTNET.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_US4STATE = "US4STATE";
    public static final boolean SHARED_PREFERENCES_US4STATE_DEFAULT = EBarcodesSymbologies.US4STATE.getDefaultStatus();
    
    public static final String SHARED_PREFERENCES_US4STATE_FICS = "US4STATE_FICS";
    public static final boolean SHARED_PREFERENCES_US4STATE_FICS_DEFAULT = EBarcodesSymbologies.US4STATE_FICS.getDefaultStatus();

    // BarcodeDataEditor Intent Extras
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_SYMBOLOGY = "symbology";
    public static final String EXTRA_QUANTITY = "quantity";
    public static final String EXTRA_DATE = "date";

    // Capture Zone preferences
    public static final String SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED = "CAPTURE_ZONE_ENABLED";
    public static final boolean SHARED_PREFERENCES_CAPTURE_ZONE_ENABLED_DEFAULT = false;
    
    public static final String SHARED_PREFERENCES_CAPTURE_ZONE_X = "CAPTURE_ZONE_X";
    public static final int SHARED_PREFERENCES_CAPTURE_ZONE_X_DEFAULT = -1; // -1 means not set
    
    public static final String SHARED_PREFERENCES_CAPTURE_ZONE_Y = "CAPTURE_ZONE_Y";
    public static final int SHARED_PREFERENCES_CAPTURE_ZONE_Y_DEFAULT = -1; // -1 means not set
    
    public static final String SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH = "CAPTURE_ZONE_WIDTH";
    public static final int SHARED_PREFERENCES_CAPTURE_ZONE_WIDTH_DEFAULT = -1; // -1 means not set
    
    public static final String SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT = "CAPTURE_ZONE_HEIGHT";
    public static final int SHARED_PREFERENCES_CAPTURE_ZONE_HEIGHT_DEFAULT = -1; // -1 means not set
    
    // Flashlight preferences
    public static final String SHARED_PREFERENCES_FLASHLIGHT_ENABLED = "FLASHLIGHT_ENABLED";
    public static final boolean SHARED_PREFERENCES_FLASHLIGHT_ENABLED_DEFAULT = false;
    
    // Language preferences
    public static final String SHARED_PREFERENCES_LANGUAGE = "SELECTED_LANGUAGE";
    public static final String SHARED_PREFERENCES_LANGUAGE_DEFAULT = "system"; // "system" means use device language
    
    // Model Input Size preferences
    public static final String SHARED_PREFERENCES_MODEL_INPUT_SIZE = "SHARED_PREFERENCES_MODEL_INPUT_SIZE";
    public static final String SHARED_PREFERENCES_MODEL_INPUT_SIZE_DEFAULT = "SMALL";
    
    // Camera Resolution preferences
    public static final String SHARED_PREFERENCES_CAMERA_RESOLUTION = "SHARED_PREFERENCES_CAMERA_RESOLUTION";
    public static final String SHARED_PREFERENCES_CAMERA_RESOLUTION_DEFAULT = "MP_2";
    
    // Inference Type preferences
    public static final String SHARED_PREFERENCES_INFERENCE_TYPE = "SHARED_PREFERENCES_INFERENCE_TYPE";
    public static final String SHARED_PREFERENCES_INFERENCE_TYPE_DEFAULT = "DSP";

    // Processing Mode preferences
    public static final String SHARED_PREFERENCES_PROCESSING_MODE = "SHARED_PREFERENCES_PROCESSING_MODE";
    public static final String SHARED_PREFERENCES_PROCESSING_MODE_DEFAULT = "file";

    // HTTPS Post preferences
    public static final String SHARED_PREFERENCES_HTTPS_ENDPOINT = "SHARED_PREFERENCES_HTTPS_ENDPOINT";
    public static final String SHARED_PREFERENCES_HTTPS_ENDPOINT_DEFAULT = "";

    // Filtering preferences
    public static final String SHARED_PREFERENCES_FILTERING_ENABLED = "SHARED_PREFERENCES_FILTERING_ENABLED";
    public static final boolean SHARED_PREFERENCES_FILTERING_ENABLED_DEFAULT = false;

    public static final String SHARED_PREFERENCES_FILTERING_REGEX = "SHARED_PREFERENCES_FILTERING_REGEX";
    public static final String SHARED_PREFERENCES_FILTERING_REGEX_DEFAULT = "";

    // Capture Trigger Mode preferences
    public static final String SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE = "SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE";
    public static final String SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE_DEFAULT = "press";

    public static final int KEYCODE_BUTTON_R1 = 103;
    public static final int KEYCODE_SCAN = 10036;
}
