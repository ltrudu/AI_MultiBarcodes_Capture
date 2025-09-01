package com.zebra.ai_multibarcodes_capture.helpers;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

public class Constants {
    // Sharing provider
    public static final String PROVIDER_AUTHORITY = "com.zebra.ai_multibarcodes_capture.fileprovider";
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

    public static final String SHARED_PREFERENCES_LAST_SESSION_FILE = "lastsessionfile";
    public static final String SHARED_PREFERENCES_PREFIX = "prefix";
    public static final String SHARED_PREFERENCES_EXTENSION = "extension";


}
