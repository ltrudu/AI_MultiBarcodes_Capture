package com.zebra.ai_multibarcodes_capture.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.adapters.LanguageAdapter;
import com.zebra.ai_multibarcodes_capture.autocapture.AutoCaptureConditionsActivity;
import com.zebra.ai_multibarcodes_capture.autocapture.AutoCapturePreferencesHelper;
import com.zebra.ai_multibarcodes_capture.filtering.FilteringConditionsActivity;
import com.zebra.ai_multibarcodes_capture.filtering.FilteringPreferencesHelper;
import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;
import com.zebra.ai_multibarcodes_capture.helpers.CameraResolutionHelper;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.ECaptureTriggerMode;
import com.zebra.ai_multibarcodes_capture.helpers.ECameraResolution;
import com.zebra.ai_multibarcodes_capture.helpers.EProcessingMode;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;
import com.zebra.ai_multibarcodes_capture.helpers.camera.AvailableCamera;
import com.zebra.ai_multibarcodes_capture.helpers.camera.CameraResolutionProviderFactory;
import com.zebra.ai_multibarcodes_capture.helpers.camera.DynamicCameraResolutionProvider;

import android.util.Size;
import java.util.List;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;
import com.zebra.ai_multibarcodes_capture.models.LanguageItem;
import com.zebra.datawedgeprofileenums.INT_E_DELIVERY;
import com.zebra.datawedgeprofileenums.MB_E_CONFIG_MODE;
import com.zebra.datawedgeprofileenums.SC_E_SCANNER_IDENTIFIER;
import com.zebra.datawedgeprofileintents.DWProfileBaseSettings;
import com.zebra.datawedgeprofileintents.DWProfileCommandBase;
import com.zebra.datawedgeprofileintents.DWProfileSetConfigSettings;
import com.zebra.datawedgeprofileintents.DWScanReceiver;
import com.zebra.datawedgeprofileintents.DWScannerPluginDisable;
import com.zebra.datawedgeprofileintents.DWScannerPluginEnable;
import com.zebra.datawedgeprofileintentshelpers.CreateProfileHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.HashMap;
import java.util.List;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;


public class SettingsActivity extends AppCompatActivity {

    private EditText etPrefix, etHttpsEndpoint;
    private Button btEditFilteringConditions;
    private RadioButton rbCSV, rbTXT, rbXSLX;
    private ImageView ivToggleSymbologies, ivToggleFileTypes, ivToggleAdvanced, ivToggleHttpsPost, ivToggleFiltering;
    private LinearLayout llSymbologies, llAdvancedContent, llFileProcessingContent, llHttpsPost, llFileProcessing, llHttpsPostContent, llFilteringContent;
    private CheckBox cbEnableFiltering;
    private RadioGroup rgFileTypes, rgModelInputSize, rgCameraResolution, rgInferenceType;
    private RadioButton rbSmallInputSize, rbMediumInputSize, rbLargeInputSize;
    private RadioButton rb1MPResolution, rb2MPResolution, rb4MPResolution, rb8MPResolution, rb12MPResolution, rb12_5MPResolution, rb12_6MPResolution;
    private RadioButton rbDSPInference, rbGPUInference, rbCPUInference;
    private TextView tvSymbologiesBadge;
    private boolean isSymbologiesExpanded = false;
    private boolean isFileTypesExpanded = false;
    private boolean isAdvancedExpanded = false;
    private boolean isHttpsPostExpanded = false;
    private boolean isFilteringExpanded = false;
    private Spinner spinnerTheme, spinnerLanguage, spinnerProcessingMode, spinnerCaptureTriggerMode;
    private LanguageAdapter languageAdapter;
    private List<LanguageItem> languageList;
    private String pendingLanguageCode = null; // Track pending language changes
    private CheckBox cbAUSTRALIAN_POSTAL, cbAZTEC, cbCANADIAN_POSTAL, cbCHINESE_2OF5, cbCODABAR;
    private CheckBox cbCODE11, cbCODE39, cbCODE93, cbCODE128, cbCOMPOSITE_AB, cbCOMPOSITE_C;
    private CheckBox cbD2OF5, cbDATAMATRIX, cbDOTCODE, cbDUTCH_POSTAL, cbEAN_8, cbEAN_13;
    private CheckBox cbFINNISH_POSTAL_4S, cbGRID_MATRIX, cbGS1_DATABAR, cbGS1_DATABAR_EXPANDED;
    private CheckBox cbGS1_DATABAR_LIM, cbGS1_DATAMATRIX, cbGS1_QRCODE, cbHANXIN, cbI2OF5;
    private CheckBox cbJAPANESE_POSTAL, cbKOREAN_3OF5, cbMAILMARK, cbMATRIX_2OF5, cbMAXICODE;
    private CheckBox cbMICROPDF, cbMICROQR, cbMSI, cbPDF417, cbQRCODE, cbTLC39, cbTRIOPTIC39;
    private CheckBox cbUK_POSTAL, cbUPC_A, cbUPC_E, cbUPCE1, cbUSPLANET, cbUSPOSTNET;
    private CheckBox cbUS4STATE, cbUS4STATE_FICS;
    private CheckBox cbDisplayAnalysisPerSecond;
    private CheckBox cbLoggingEnabled;
    private CheckBox cbForceContinuousAutofocus;
    private CheckBox cbDebounceEnabled;
    private SeekBar sbDebounceMaxFrames;
    private SeekBar sbDebounceThreshold;
    private TextView tvDebounceMaxFramesValue;
    private TextView tvDebounceThresholdValue;
    private LinearLayout llDebounceMaxFrames;
    private LinearLayout llDebounceThreshold;
    private LinearLayout llDebounceAlgorithm;
    private Spinner spinnerDebounceAlgorithm;
    private LinearLayout llDebounceIouThreshold;
    private SeekBar sbDebounceIouThreshold;
    private TextView tvDebounceIouThresholdValue;

    // Auto Capture views
    private ImageView ivToggleAutoCapture;
    private LinearLayout llAutoCaptureContent;
    private CheckBox cbEnableAutoCapture;
    private Button btEditAutoCaptureConditions;
    private boolean isAutoCaptureExpanded = false;

    // Dynamic Camera Selection views
    private Spinner spinnerResolutionMode;
    private Spinner spinnerCameraSelection;
    private Spinner spinnerResolutionSelection;
    private Spinner spinnerResolutionFilter;
    private LinearLayout llStaticResolutionSection;
    private LinearLayout llDynamicResolutionSection;
    private TextView tvCameraInfo;
    private List<AvailableCamera> availableCameras;
    private DynamicCameraResolutionProvider dynamicProvider;
    private boolean useStandardResolutions = true;

    private DWScanReceiver mScanReceiver;


    // BroadcastReceiver to listen for managed configuration reload requests
    private BroadcastReceiver reloadPreferencesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES.equals(intent.getAction())) {
                LogUtils.d(TAG, "Received reload preferences request from ManagedConfigurationReceiver");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadPreferences();
                        reloadLanguageSpinner();
                        Toast.makeText(SettingsActivity.this, 
                            getString(R.string.managed_configuration_updated), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeHelpers.applyTheme(this);

        setContentView(R.layout.activity_setup);
        
        // Configure system bars
        ThemeHelpers.configureSystemBars(this, R.id.rl_setup_activity);
        ThemeHelpers.applyCustomFont(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etPrefix = findViewById(R.id.etPrefix);
        etHttpsEndpoint = findViewById(R.id.etHttpsEndpoint);
        btEditFilteringConditions = findViewById(R.id.btEditFilteringConditions);
        rbCSV = findViewById(R.id.rbCSV);
        rbTXT = findViewById(R.id.rbTxt);
        rbXSLX = findViewById(R.id.rbXSLX);
        ivToggleSymbologies = findViewById(R.id.ivToggleSymbologies);
        ivToggleFileTypes = findViewById(R.id.ivToggleFileTypes);
        ivToggleAdvanced = findViewById(R.id.ivToggleAdvanced);
        ivToggleHttpsPost = findViewById(R.id.ivToggleHttpsPost);
        ivToggleFiltering = findViewById(R.id.ivToggleFiltering);
        llSymbologies = findViewById(R.id.llSymbologies);
        llAdvancedContent = findViewById(R.id.llAdvancedContent);
        llFileProcessingContent = findViewById(R.id.llFileProcessingContent);
        llFileProcessing = findViewById(R.id.llFileProcessing);
        llHttpsPost = findViewById(R.id.llHttpsPost);
        llHttpsPostContent = findViewById(R.id.llHttpsPostContent);
        llFilteringContent = findViewById(R.id.llFilteringContent);
        cbEnableFiltering = findViewById(R.id.cbEnableFiltering);
        rgFileTypes = findViewById(R.id.rgFileTypes);
        rgModelInputSize = findViewById(R.id.rgModelInputSize);
        rgCameraResolution = findViewById(R.id.rgCameraResolution);
        rgInferenceType = findViewById(R.id.rgInferenceType);
        rbSmallInputSize = findViewById(R.id.rbSmallInputSize);
        rbMediumInputSize = findViewById(R.id.rbMediumInputSize);
        rbLargeInputSize = findViewById(R.id.rbLargeInputSize);
        rb1MPResolution = findViewById(R.id.rb1MPResolution);
        rb2MPResolution = findViewById(R.id.rb2MPResolution);
        rb4MPResolution = findViewById(R.id.rb4MPResolution);
        rb8MPResolution = findViewById(R.id.rb8MPResolution);
        rb12MPResolution = findViewById(R.id.rb12MPResolution);
        rb12_5MPResolution = findViewById(R.id.rb12_5MPResolution);
        rb12_6MPResolution = findViewById(R.id.rb12_6MPResolution);
        rbDSPInference = findViewById(R.id.rbDSPInference);
        rbGPUInference = findViewById(R.id.rbGPUInference);
        rbCPUInference = findViewById(R.id.rbCPUInference);
        tvSymbologiesBadge = findViewById(R.id.tvSymbologiesBadge);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerProcessingMode = findViewById(R.id.spinnerProcessingMode);
        spinnerCaptureTriggerMode = findViewById(R.id.spinnerCaptureTriggerMode);

        // Setup theme spinner
        setupThemeSpinner();

        // Setup language spinner
        setupLanguageSpinner();

        // Setup processing mode spinner
        setupProcessingModeSpinner();

        // Setup capture trigger mode spinner
        setupCaptureTriggerModeSpinner();

        // Setup HTTPS endpoint validation
        setupHttpsEndpointValidation();
        
        // Initialize all barcode symbology checkboxes
        cbAUSTRALIAN_POSTAL = findViewById(R.id.cbAUSTRALIAN_POSTAL);
        cbAZTEC = findViewById(R.id.cbAZTEC);
        cbCANADIAN_POSTAL = findViewById(R.id.cbCANADIAN_POSTAL);
        cbCHINESE_2OF5 = findViewById(R.id.cbCHINESE_2OF5);
        cbCODABAR = findViewById(R.id.cbCODABAR);
        cbCODE11 = findViewById(R.id.cbCODE11);
        cbCODE39 = findViewById(R.id.cbCODE39);
        cbCODE93 = findViewById(R.id.cbCODE93);
        cbCODE128 = findViewById(R.id.cbCODE128);
        cbCOMPOSITE_AB = findViewById(R.id.cbCOMPOSITE_AB);
        cbCOMPOSITE_C = findViewById(R.id.cbCOMPOSITE_C);
        cbD2OF5 = findViewById(R.id.cbD2OF5);
        cbDATAMATRIX = findViewById(R.id.cbDATAMATRIX);
        cbDOTCODE = findViewById(R.id.cbDOTCODE);
        cbDUTCH_POSTAL = findViewById(R.id.cbDUTCH_POSTAL);
        cbEAN_8 = findViewById(R.id.cbEAN_8);
        cbEAN_13 = findViewById(R.id.cbEAN_13);
        cbFINNISH_POSTAL_4S = findViewById(R.id.cbFINNISH_POSTAL_4S);
        cbGRID_MATRIX = findViewById(R.id.cbGRID_MATRIX);
        cbGS1_DATABAR = findViewById(R.id.cbGS1_DATABAR);
        cbGS1_DATABAR_EXPANDED = findViewById(R.id.cbGS1_DATABAR_EXPANDED);
        cbGS1_DATABAR_LIM = findViewById(R.id.cbGS1_DATABAR_LIM);
        cbGS1_DATAMATRIX = findViewById(R.id.cbGS1_DATAMATRIX);
        cbGS1_QRCODE = findViewById(R.id.cbGS1_QRCODE);
        cbHANXIN = findViewById(R.id.cbHANXIN);
        cbI2OF5 = findViewById(R.id.cbI2OF5);
        cbJAPANESE_POSTAL = findViewById(R.id.cbJAPANESE_POSTAL);
        cbKOREAN_3OF5 = findViewById(R.id.cbKOREAN_3OF5);
        cbMAILMARK = findViewById(R.id.cbMAILMARK);
        cbMATRIX_2OF5 = findViewById(R.id.cbMATRIX_2OF5);
        cbMAXICODE = findViewById(R.id.cbMAXICODE);
        cbMICROPDF = findViewById(R.id.cbMICROPDF);
        cbMICROQR = findViewById(R.id.cbMICROQR);
        cbMSI = findViewById(R.id.cbMSI);
        cbPDF417 = findViewById(R.id.cbPDF417);
        cbQRCODE = findViewById(R.id.cbQRCODE);
        cbTLC39 = findViewById(R.id.cbTLC39);
        cbTRIOPTIC39 = findViewById(R.id.cbTRIOPTIC39);
        cbUK_POSTAL = findViewById(R.id.cbUK_POSTAL);
        cbUPC_A = findViewById(R.id.cbUPC_A);
        cbUPC_E = findViewById(R.id.cbUPC_E);
        cbUPCE1 = findViewById(R.id.cbUPCE1);
        cbUSPLANET = findViewById(R.id.cbUSPLANET);
        cbUSPOSTNET = findViewById(R.id.cbUSPOSTNET);
        cbUS4STATE = findViewById(R.id.cbUS4STATE);
        cbUS4STATE_FICS = findViewById(R.id.cbUS4STATE_FICS);
        cbDisplayAnalysisPerSecond = findViewById(R.id.cbDisplayAnalysisPerSecond);
        cbLoggingEnabled = findViewById(R.id.cbLoggingEnabled);
        cbForceContinuousAutofocus = findViewById(R.id.cbForceContinuousAutofocus);
        cbDebounceEnabled = findViewById(R.id.cbDebounceEnabled);
        sbDebounceMaxFrames = findViewById(R.id.sbDebounceMaxFrames);
        sbDebounceThreshold = findViewById(R.id.sbDebounceThreshold);
        tvDebounceMaxFramesValue = findViewById(R.id.tvDebounceMaxFramesValue);
        tvDebounceThresholdValue = findViewById(R.id.tvDebounceThresholdValue);
        llDebounceMaxFrames = findViewById(R.id.llDebounceMaxFrames);
        llDebounceThreshold = findViewById(R.id.llDebounceThreshold);
        llDebounceAlgorithm = findViewById(R.id.llDebounceAlgorithm);
        spinnerDebounceAlgorithm = findViewById(R.id.spinnerDebounceAlgorithm);
        llDebounceIouThreshold = findViewById(R.id.llDebounceIouThreshold);
        sbDebounceIouThreshold = findViewById(R.id.sbDebounceIouThreshold);
        tvDebounceIouThresholdValue = findViewById(R.id.tvDebounceIouThresholdValue);

        // Auto Capture views
        ivToggleAutoCapture = findViewById(R.id.ivToggleAutoCapture);
        llAutoCaptureContent = findViewById(R.id.llAutoCaptureContent);
        cbEnableAutoCapture = findViewById(R.id.cbEnableAutoCapture);
        btEditAutoCaptureConditions = findViewById(R.id.btEditAutoCaptureConditions);

        // Dynamic Camera Selection views
        spinnerResolutionMode = findViewById(R.id.spinnerResolutionMode);
        spinnerCameraSelection = findViewById(R.id.spinnerCameraSelection);
        spinnerResolutionSelection = findViewById(R.id.spinnerResolutionSelection);
        spinnerResolutionFilter = findViewById(R.id.spinnerResolutionFilter);
        llStaticResolutionSection = findViewById(R.id.llStaticResolutionSection);
        llDynamicResolutionSection = findViewById(R.id.llDynamicResolutionSection);
        tvCameraInfo = findViewById(R.id.tvCameraInfo);

        // Setup resolution mode spinner
        setupResolutionModeSpinner();

        // Initially hide the LinearLayout and set collapsed state
        llSymbologies.setVisibility(View.GONE);
        isSymbologiesExpanded = false;
        ivToggleSymbologies.setImageResource(R.drawable.ic_expand_less_24);

        // Initially hide the File Processing content and set collapsed state
        llFileProcessingContent.setVisibility(View.GONE);
        isFileTypesExpanded = false;
        ivToggleFileTypes.setImageResource(R.drawable.ic_expand_less_24);

        // Initially hide the Advanced content and set collapsed state
        llAdvancedContent.setVisibility(View.GONE);
        isAdvancedExpanded = false;
        ivToggleAdvanced.setImageResource(R.drawable.ic_expand_less_24);

        // Initially hide the HTTPS Post content and set collapsed state
        llHttpsPostContent.setVisibility(View.GONE);
        isHttpsPostExpanded = false;
        ivToggleHttpsPost.setImageResource(R.drawable.ic_expand_less_24);

        // Initially hide the Filtering content and set collapsed state
        llFilteringContent.setVisibility(View.GONE);
        isFilteringExpanded = false;
        ivToggleFiltering.setImageResource(R.drawable.ic_expand_less_24);

        // Initially hide the Auto Capture content and set collapsed state
        llAutoCaptureContent.setVisibility(View.GONE);
        isAutoCaptureExpanded = false;
        ivToggleAutoCapture.setImageResource(R.drawable.ic_expand_less_24);

        // Set up click listener for symbologies toggle
        ivToggleSymbologies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSymbologies();
            }
        });

        // Set up click listener for file types toggle
        ivToggleFileTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFileTypes();
            }
        });

        // Set up click listener for advanced toggle
        ivToggleAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAdvanced();
            }
        });

        // Set up click listener for HTTPS Post toggle
        ivToggleHttpsPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleHttpsPost();
            }
        });

        // Set up click listener for filtering toggle
        ivToggleFiltering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFiltering();
            }
        });

        // Set up click listener for auto capture toggle
        ivToggleAutoCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAutoCapture();
            }
        });

        // Set up click listener for edit auto capture conditions button
        btEditAutoCaptureConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AutoCaptureConditionsActivity.class);
                startActivity(intent);
            }
        });

        // Setup symbology checkboxes change listeners
        setupSymbologyListeners();

        // Setup filtering checkbox listener
        setupFilteringListeners();

        // Setup debounce listeners
        setupDebounceListeners();

        findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset any pending language changes
                pendingLanguageCode = null;
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.btOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences();
                setResult(RESULT_OK);
                finish();
            }
        });

        // Create Datawedge profile
        createDataWedgeProfile();

        // Initialize receiver
        initializeScanReceiver();
    }

    @Override
    protected void onResume() {
        loadPreferences();
        updateSymbologiesBadge();
        super.onResume();
        
        // Register the BroadcastReceiver to listen for managed configuration changes
        IntentFilter filter = new IntentFilter(ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES);
        registerReceiver(reloadPreferencesReceiver, filter, RECEIVER_NOT_EXPORTED);
        LogUtils.d(TAG, "Registered BroadcastReceiver for managed configuration changes");

        enableDataWedgePlugin();
        mScanReceiver.startReceive();

    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Unregister the BroadcastReceiver
        try {
            unregisterReceiver(reloadPreferencesReceiver);
            LogUtils.d(TAG, "Unregistered BroadcastReceiver for managed configuration changes");
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
            LogUtils.d(TAG, "BroadcastReceiver was not registered, ignoring unregister attempt");
        }
        mScanReceiver.stopReceive();
        disableDatawedgePlugin();
    }

    private void loadPreferences()
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Retrieve the stored integer value, with a default value of 0 if not found
        String prefix = sharedPreferences.getString(SHARED_PREFERENCES_PREFIX, FILE_DEFAULT_PREFIX);
        String extension = sharedPreferences.getString(SHARED_PREFERENCES_EXTENSION, FILE_DEFAULT_EXTENSION);

        loadBarcodesSymbologies(sharedPreferences);
        loadModelInputSize(sharedPreferences);
        loadCameraResolution(sharedPreferences);
        loadInferenceType(sharedPreferences);
        loadProcessingMode(sharedPreferences);
        loadHttpsPostSettings(sharedPreferences);
        loadFilteringSettings(sharedPreferences);
        loadCaptureTriggerMode(sharedPreferences);
        loadDisplayAnalysisPerSecond(sharedPreferences);
        loadLoggingEnabled(sharedPreferences);
        loadForceContinuousAutofocus(sharedPreferences);
        loadDebounceSettings(sharedPreferences);
        loadAutoCaptureSettings();

        etPrefix.setText(prefix);
        selectExtensionRadioButton(extension);
    }

    private void loadBarcodesSymbologies(SharedPreferences sharedPreferences) {
        cbAUSTRALIAN_POSTAL.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_AUSTRALIAN_POSTAL, SHARED_PREFERENCES_AUSTRALIAN_POSTAL_DEFAULT));
        cbAZTEC.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_AZTEC, SHARED_PREFERENCES_AZTEC_DEFAULT));
        cbCANADIAN_POSTAL.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CANADIAN_POSTAL, SHARED_PREFERENCES_CANADIAN_POSTAL_DEFAULT));
        cbCHINESE_2OF5.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CHINESE_2OF5, SHARED_PREFERENCES_CHINESE_2OF5_DEFAULT));
        cbCODABAR.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODABAR, SHARED_PREFERENCES_CODABAR_DEFAULT));
        cbCODE11.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE11, SHARED_PREFERENCES_CODE11_DEFAULT));
        cbCODE39.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE39, SHARED_PREFERENCES_CODE39_DEFAULT));
        cbCODE93.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE93, SHARED_PREFERENCES_CODE93_DEFAULT));
        cbCODE128.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_CODE128, SHARED_PREFERENCES_CODE128_DEFAULT));
        cbCOMPOSITE_AB.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_COMPOSITE_AB, SHARED_PREFERENCES_COMPOSITE_AB_DEFAULT));
        cbCOMPOSITE_C.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_COMPOSITE_C, SHARED_PREFERENCES_COMPOSITE_C_DEFAULT));
        cbD2OF5.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_D2OF5, SHARED_PREFERENCES_D2OF5_DEFAULT));
        cbDATAMATRIX.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_DATAMATRIX, SHARED_PREFERENCES_DATAMATRIX_DEFAULT));
        cbDOTCODE.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_DOTCODE, SHARED_PREFERENCES_DOTCODE_DEFAULT));
        cbDUTCH_POSTAL.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_DUTCH_POSTAL, SHARED_PREFERENCES_DUTCH_POSTAL_DEFAULT));
        cbEAN_8.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_EAN_8, SHARED_PREFERENCES_EAN_8_DEFAULT));
        cbEAN_13.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_EAN_13, SHARED_PREFERENCES_EAN_13_DEFAULT));
        cbFINNISH_POSTAL_4S.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_FINNISH_POSTAL_4S, SHARED_PREFERENCES_FINNISH_POSTAL_4S_DEFAULT));
        cbGRID_MATRIX.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GRID_MATRIX, SHARED_PREFERENCES_GRID_MATRIX_DEFAULT));
        cbGS1_DATABAR.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR, SHARED_PREFERENCES_GS1_DATABAR_DEFAULT));
        cbGS1_DATABAR_EXPANDED.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR_EXPANDED, SHARED_PREFERENCES_GS1_DATABAR_EXPANDED_DEFAULT));
        cbGS1_DATABAR_LIM.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATABAR_LIM, SHARED_PREFERENCES_GS1_DATABAR_LIM_DEFAULT));
        cbGS1_DATAMATRIX.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_DATAMATRIX, SHARED_PREFERENCES_GS1_DATAMATRIX_DEFAULT));
        cbGS1_QRCODE.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_GS1_QRCODE, SHARED_PREFERENCES_GS1_QRCODE_DEFAULT));
        cbHANXIN.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_HANXIN, SHARED_PREFERENCES_HANXIN_DEFAULT));
        cbI2OF5.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_I2OF5, SHARED_PREFERENCES_I2OF5_DEFAULT));
        cbJAPANESE_POSTAL.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_JAPANESE_POSTAL, SHARED_PREFERENCES_JAPANESE_POSTAL_DEFAULT));
        cbKOREAN_3OF5.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_KOREAN_3OF5, SHARED_PREFERENCES_KOREAN_3OF5_DEFAULT));
        cbMAILMARK.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MAILMARK, SHARED_PREFERENCES_MAILMARK_DEFAULT));
        cbMATRIX_2OF5.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MATRIX_2OF5, SHARED_PREFERENCES_MATRIX_2OF5_DEFAULT));
        cbMAXICODE.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MAXICODE, SHARED_PREFERENCES_MAXICODE_DEFAULT));
        cbMICROPDF.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MICROPDF, SHARED_PREFERENCES_MICROPDF_DEFAULT));
        cbMICROQR.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MICROQR, SHARED_PREFERENCES_MICROQR_DEFAULT));
        cbMSI.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_MSI, SHARED_PREFERENCES_MSI_DEFAULT));
        cbPDF417.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_PDF417, SHARED_PREFERENCES_PDF417_DEFAULT));
        cbQRCODE.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_QRCODE, SHARED_PREFERENCES_QRCODE_DEFAULT));
        cbTLC39.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_TLC39, SHARED_PREFERENCES_TLC39_DEFAULT));
        cbTRIOPTIC39.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_TRIOPTIC39, SHARED_PREFERENCES_TRIOPTIC39_DEFAULT));
        cbUK_POSTAL.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_UK_POSTAL, SHARED_PREFERENCES_UK_POSTAL_DEFAULT));
        cbUPC_A.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPC_A, SHARED_PREFERENCES_UPC_A_DEFAULT));
        cbUPC_E.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPC_E, SHARED_PREFERENCES_UPC_E_DEFAULT));
        cbUPCE1.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_UPCE0, SHARED_PREFERENCES_UPCE0_DEFAULT));
        cbUSPLANET.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_USPLANET, SHARED_PREFERENCES_USPLANET_DEFAULT));
        cbUSPOSTNET.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_USPOSTNET, SHARED_PREFERENCES_USPOSTNET_DEFAULT));
        cbUS4STATE.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_US4STATE, SHARED_PREFERENCES_US4STATE_DEFAULT));
        cbUS4STATE_FICS.setChecked(sharedPreferences.getBoolean(SHARED_PREFERENCES_US4STATE_FICS, SHARED_PREFERENCES_US4STATE_FICS_DEFAULT));
    }

    private void loadModelInputSize(SharedPreferences sharedPreferences) {
        String modelInputSize = sharedPreferences.getString(SHARED_PREFERENCES_MODEL_INPUT_SIZE, SHARED_PREFERENCES_MODEL_INPUT_SIZE_DEFAULT);
        
        // Set the radio button based on saved preference
        if ("SMALL".equals(modelInputSize)) {
            rbSmallInputSize.setChecked(true);
        } else if ("LARGE".equals(modelInputSize)) {
            rbLargeInputSize.setChecked(true);
        } else {
            // Default to MEDIUM
            rbMediumInputSize.setChecked(true);
        }
    }

    private void loadCameraResolution(SharedPreferences sharedPreferences) {
        String cameraResolution = sharedPreferences.getString(SHARED_PREFERENCES_CAMERA_RESOLUTION, SHARED_PREFERENCES_CAMERA_RESOLUTION_DEFAULT);

        // Get supported resolutions and hide unsupported ones
        List<Size> supportedSizes = CameraResolutionHelper.getSupportedResolutions(this);
        CameraResolutionHelper.logAllSupportedResolutions(this);

        // Check which resolutions are supported
        boolean is1MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_1, supportedSizes);
        boolean is2MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_2, supportedSizes);
        boolean is4MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_4, supportedSizes);
        boolean is8MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_8, supportedSizes);
        boolean is12MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_12, supportedSizes);
        boolean is12_5MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_12_5, supportedSizes);
        boolean is12_6MPSupported = CameraResolutionHelper.isResolutionSupported(ECameraResolution.MP_12_6, supportedSizes);

        // Hide unsupported resolutions
        rb1MPResolution.setVisibility(is1MPSupported ? View.VISIBLE : View.GONE);
        rb2MPResolution.setVisibility(is2MPSupported ? View.VISIBLE : View.GONE);
        rb4MPResolution.setVisibility(is4MPSupported ? View.VISIBLE : View.GONE);
        rb8MPResolution.setVisibility(is8MPSupported ? View.VISIBLE : View.GONE);
        rb12MPResolution.setVisibility(is12MPSupported ? View.VISIBLE : View.GONE);
        // Hide exotic resolutions (12.5MP and 12.6MP) - keep code but don't show in UI
        rb12_5MPResolution.setVisibility(View.GONE);
        rb12_6MPResolution.setVisibility(View.GONE);

        // Set the radio button based on saved preference, falling back if not supported
        boolean selectionMade = false;

        if ("MP_1".equals(cameraResolution) && is1MPSupported) {
            rb1MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_2".equals(cameraResolution) && is2MPSupported) {
            rb2MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_4".equals(cameraResolution) && is4MPSupported) {
            rb4MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_8".equals(cameraResolution) && is8MPSupported) {
            rb8MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_12".equals(cameraResolution) && is12MPSupported) {
            rb12MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_12_5".equals(cameraResolution) && is12_5MPSupported) {
            rb12_5MPResolution.setChecked(true);
            selectionMade = true;
        } else if ("MP_12_6".equals(cameraResolution) && is12_6MPSupported) {
            rb12_6MPResolution.setChecked(true);
            selectionMade = true;
        }

        // If saved preference is not supported, select first available supported resolution
        if (!selectionMade) {
            if (is2MPSupported) {
                rb2MPResolution.setChecked(true);
            } else if (is1MPSupported) {
                rb1MPResolution.setChecked(true);
            } else if (is4MPSupported) {
                rb4MPResolution.setChecked(true);
            } else if (is8MPSupported) {
                rb8MPResolution.setChecked(true);
            } else if (is12MPSupported) {
                rb12MPResolution.setChecked(true);
            } else if (is12_5MPSupported) {
                rb12_5MPResolution.setChecked(true);
            } else if (is12_6MPSupported) {
                rb12_6MPResolution.setChecked(true);
            }
        }
    }

    private void loadInferenceType(SharedPreferences sharedPreferences) {
        String inferenceType = sharedPreferences.getString(SHARED_PREFERENCES_INFERENCE_TYPE, SHARED_PREFERENCES_INFERENCE_TYPE_DEFAULT);
        
        // Set the radio button based on saved preference
        if ("GPU".equals(inferenceType)) {
            rbGPUInference.setChecked(true);
        } else if ("CPU".equals(inferenceType)) {
            rbCPUInference.setChecked(true);
        } else {
            // Default to DSP (Best Choice)
            rbDSPInference.setChecked(true);
        }
    }

    private void loadProcessingMode(SharedPreferences sharedPreferences) {
        String processingModeKey = sharedPreferences.getString(SHARED_PREFERENCES_PROCESSING_MODE, SHARED_PREFERENCES_PROCESSING_MODE_DEFAULT);
        EProcessingMode processingMode = EProcessingMode.fromKey(processingModeKey);

        // Set the spinner selection based on the loaded processing mode
        for (int i = 0; i < EProcessingMode.values().length; i++) {
            if (EProcessingMode.values()[i] == processingMode) {
                spinnerProcessingMode.setSelection(i);
                break;
            }
        }
    }

    private void loadCaptureTriggerMode(SharedPreferences sharedPreferences) {
        String captureTriggerModeKey = sharedPreferences.getString(SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE, SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE_DEFAULT);
        ECaptureTriggerMode captureTriggerMode = ECaptureTriggerMode.fromKey(captureTriggerModeKey);

        // Set the spinner selection based on the loaded capture trigger mode
        for (int i = 0; i < ECaptureTriggerMode.values().length; i++) {
            if (ECaptureTriggerMode.values()[i] == captureTriggerMode) {
                spinnerCaptureTriggerMode.setSelection(i);
                break;
            }
        }
    }

    private void savePreferences()
    {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Get the SharedPreferences.Editor object to make changes
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String prefix = etPrefix.getText().toString();
        if(prefix.isEmpty() == false)
        {
            editor.putString(SHARED_PREFERENCES_PREFIX, prefix);
        }

        saveBarcodeSymbologies(editor);
        saveModelInputSize(editor);
        saveCameraResolution(editor);
        saveInferenceType(editor);
        saveProcessingMode(editor);
        saveHttpsPostSettings(editor);
        saveFilteringSettings(editor);
        saveCaptureTriggerMode(editor);
        saveDisplayAnalysisPerSecond(editor);
        saveLoggingEnabled(editor);
        saveForceContinuousAutofocus(editor);
        saveDebounceSettings(editor);
        saveAutoCaptureSettings();

        editor.putString(SHARED_PREFERENCES_EXTENSION, getSelectedExtension());

        // Check if language has changed and needs to be applied
        boolean languageChanged = false;
        if (pendingLanguageCode != null) {
            LogUtils.d(TAG, "Saving language change to: " + pendingLanguageCode);
            LocaleHelper.saveLanguageChoice(this, pendingLanguageCode);
            languageChanged = true;
        }

        // Commit the changes
        editor.commit();

        // Show success message and handle language change if needed
        if (languageChanged) {
            Toast.makeText(this, getString(R.string.settings_saved_successfully), Toast.LENGTH_SHORT).show();
            applyLocaleChange(pendingLanguageCode);
        } else {
            Toast.makeText(this, getString(R.string.settings_saved_successfully), Toast.LENGTH_LONG).show();
        }

    }
    
    private void setupLanguageSpinner() {
        languageList = LocaleHelper.getLanguageList(this);
        languageAdapter = new LanguageAdapter(this, languageList);
        spinnerLanguage.setAdapter(languageAdapter);
        
        // Set current selection
        String currentLang = LocaleHelper.getCurrentLanguageCode(this);
        LogUtils.d(TAG, "Current language code: " + currentLang);
        
        for (int i = 0; i < languageList.size(); i++) {
            if (languageList.get(i).getLanguageCode().equals(currentLang)) {
                spinnerLanguage.setSelection(i);
                LogUtils.d(TAG, "Set spinner selection to position: " + i + " (" + languageList.get(i).getLanguageName() + ")");
                break;
            }
        }
        
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LanguageItem selectedLang = languageList.get(position);
                String newLangCode = selectedLang.getLanguageCode();
                String currentLangCode = LocaleHelper.getCurrentLanguageCode(SettingsActivity.this);
                
                // Only track as pending change, don't apply immediately
                if (!newLangCode.equals(currentLangCode)) {
                    LogUtils.d(TAG, "Language selection changed from " + currentLangCode + " to " + newLangCode + " (pending validation)");
                    pendingLanguageCode = newLangCode;
                } else {
                    // Same as current, no pending change
                    pendingLanguageCode = null;
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void reloadLanguageSpinner() {
        // Get the current language from preferences (which may have been updated by managed config)
        String currentLang = LocaleHelper.getCurrentLanguageCode(this);
        LogUtils.d(TAG, "Reloading language spinner with current language: " + currentLang);
        
        // Find the position of the current language in the list
        for (int i = 0; i < languageList.size(); i++) {
            if (languageList.get(i).getLanguageCode().equals(currentLang)) {
                spinnerLanguage.setSelection(i);
                LogUtils.d(TAG, "Updated spinner selection to position: " + i + " (" + languageList.get(i).getLanguageName() + ")");
                break;
            }
        }
        
        // Reset any pending changes since we're reflecting managed configuration
        pendingLanguageCode = null;
    }

    private void setupThemeSpinner() {
        // Create array of theme names
        String[] themeNames = new String[]{
            getString(R.string.theme_legacy),
            getString(R.string.theme_modern)
        };

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, themeNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);

        // Set current selection based on saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String currentTheme = sharedPreferences.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

        // Set spinner selection: 0 for legacy, 1 for modern
        int themePosition = currentTheme.equals("modern") ? 1 : 0;
        spinnerTheme.setSelection(themePosition);

        // Set up selection listener
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip the first automatic selection when spinner is initialized
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                // Determine which theme was selected
                String selectedTheme = (position == 1) ? "modern" : "legacy";

                // Get current theme from preferences
                SharedPreferences sp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                String currentTheme = sp.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

                // Only apply if theme changed
                if (!selectedTheme.equals(currentTheme)) {
                    LogUtils.d(TAG, "Theme changed from " + currentTheme + " to " + selectedTheme);

                    // Save the new theme preference
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(SHARED_PREFERENCES_THEME, selectedTheme);
                    editor.apply();

                    // Recreate the activity to apply the new theme
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupProcessingModeSpinner() {
        // Create array of processing mode display names
        String[] processingModeDisplayNames = new String[EProcessingMode.values().length];
        for (int i = 0; i < EProcessingMode.values().length; i++) {
            processingModeDisplayNames[i] = EProcessingMode.values()[i].getDisplayName(this);
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, processingModeDisplayNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerProcessingMode.setAdapter(adapter);

        // Set current selection based on saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String currentProcessingMode = sharedPreferences.getString(SHARED_PREFERENCES_PROCESSING_MODE, SHARED_PREFERENCES_PROCESSING_MODE_DEFAULT);

        EProcessingMode currentMode = EProcessingMode.fromKey(currentProcessingMode);
        for (int i = 0; i < EProcessingMode.values().length; i++) {
            if (EProcessingMode.values()[i] == currentMode) {
                spinnerProcessingMode.setSelection(i);
                break;
            }
        }

        // Set up selection listener for dynamic visibility
        spinnerProcessingMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSectionVisibility(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Update initial visibility
        updateSectionVisibility(spinnerProcessingMode.getSelectedItemPosition());
    }

    private void setupCaptureTriggerModeSpinner() {
        // Create array of capture trigger mode display names
        String[] captureTriggerModeDisplayNames = new String[ECaptureTriggerMode.values().length];
        for (int i = 0; i < ECaptureTriggerMode.values().length; i++) {
            captureTriggerModeDisplayNames[i] = ECaptureTriggerMode.values()[i].getDisplayName(this);
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, captureTriggerModeDisplayNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCaptureTriggerMode.setAdapter(adapter);

        // Set current selection based on saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String currentCaptureTriggerMode = sharedPreferences.getString(SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE, SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE_DEFAULT);

        ECaptureTriggerMode currentMode = ECaptureTriggerMode.fromKey(currentCaptureTriggerMode);
        for (int i = 0; i < ECaptureTriggerMode.values().length; i++) {
            if (ECaptureTriggerMode.values()[i] == currentMode) {
                spinnerCaptureTriggerMode.setSelection(i);
                break;
            }
        }
    }

    private void setupHttpsEndpointValidation() {
        // No automatic prefix enforcement - allow both HTTP and HTTPS
    }

    private void setupResolutionModeSpinner() {
        // Create array of resolution mode display names
        String[] resolutionModeNames = new String[]{
            getString(R.string.resolution_mode_static),
            getString(R.string.resolution_mode_dynamic)
        };

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, resolutionModeNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerResolutionMode.setAdapter(adapter);

        // Set current selection based on saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String currentMode = sharedPreferences.getString(SHARED_PREFERENCES_RESOLUTION_MODE, SHARED_PREFERENCES_RESOLUTION_MODE_DEFAULT);

        // Set spinner selection: 0 for static, 1 for dynamic
        int modePosition = currentMode.equals(CameraResolutionProviderFactory.MODE_DYNAMIC) ? 1 : 0;
        spinnerResolutionMode.setSelection(modePosition);

        // Update UI visibility based on current mode
        updateResolutionModeVisibility(modePosition);

        // Set up selection listener
        spinnerResolutionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip the first automatic selection when spinner is initialized
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                updateResolutionModeVisibility(position);

                // If switching to dynamic mode, initialize the dynamic camera selection
                if (position == 1) {
                    initializeDynamicCameraSelection();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initialize dynamic camera selection if already in dynamic mode
        if (modePosition == 1) {
            initializeDynamicCameraSelection();
        }
    }

    private void updateResolutionModeVisibility(int modePosition) {
        if (modePosition == 0) {
            // Static mode
            llStaticResolutionSection.setVisibility(View.VISIBLE);
            llDynamicResolutionSection.setVisibility(View.GONE);
        } else {
            // Dynamic mode
            llStaticResolutionSection.setVisibility(View.GONE);
            llDynamicResolutionSection.setVisibility(View.VISIBLE);
        }
    }

    private void initializeDynamicCameraSelection() {
        // Get available cameras
        availableCameras = CameraResolutionHelper.getAllAvailableCameras(this);

        if (availableCameras.isEmpty()) {
            tvCameraInfo.setText(getString(R.string.no_cameras_found));
            return;
        }

        // Load saved resolution filter setting
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String filterMode = prefs.getString(SHARED_PREFERENCES_RESOLUTION_FILTER, SHARED_PREFERENCES_RESOLUTION_FILTER_DEFAULT);
        useStandardResolutions = filterMode.equals(Constants.RESOLUTION_FILTER_STANDARD);

        // Create dynamic provider to load saved settings
        dynamicProvider = new DynamicCameraResolutionProvider();
        dynamicProvider.loadSettings(this);

        // Setup camera selection spinner
        setupCameraSelectionSpinner();

        // Setup resolution filter spinner
        setupResolutionFilterSpinner();

        // Setup resolution selection spinner for the selected camera
        setupResolutionSelectionSpinner();
    }

    private void setupResolutionFilterSpinner() {
        // Create array of filter mode display names
        String[] filterModeNames = new String[]{
            getString(R.string.resolution_filter_standard),
            getString(R.string.resolution_filter_all)
        };

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, filterModeNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerResolutionFilter.setAdapter(adapter);

        // Set current selection: 0 for standard, 1 for all
        int filterPosition = useStandardResolutions ? 0 : 1;
        spinnerResolutionFilter.setSelection(filterPosition);

        // Set up selection listener
        spinnerResolutionFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip the first automatic selection when spinner is initialized
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                useStandardResolutions = (position == 0);
                // Refresh resolution spinner with new filter
                setupResolutionSelectionSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupCameraSelectionSpinner() {
        if (availableCameras == null || availableCameras.isEmpty()) {
            return;
        }

        // Create array of camera display names
        String[] cameraNames = new String[availableCameras.size()];
        for (int i = 0; i < availableCameras.size(); i++) {
            cameraNames[i] = availableCameras.get(i).getLocalizedDisplayName(this);
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, cameraNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCameraSelection.setAdapter(adapter);

        // Find and set current selection based on saved camera ID
        String savedCameraId = dynamicProvider.getSelectedCameraId();
        int selectedPosition = 0;
        for (int i = 0; i < availableCameras.size(); i++) {
            if (availableCameras.get(i).getCameraId().equals(savedCameraId)) {
                selectedPosition = i;
                break;
            }
        }
        spinnerCameraSelection.setSelection(selectedPosition);

        // Update camera info display
        updateCameraInfoDisplay(availableCameras.get(selectedPosition));

        // Set up selection listener
        spinnerCameraSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AvailableCamera selectedCamera = availableCameras.get(position);
                dynamicProvider.setSelectedCameraId(selectedCamera.getCameraId());

                // Update camera info display
                updateCameraInfoDisplay(selectedCamera);

                // Update resolution spinner for the new camera
                setupResolutionSelectionSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateCameraInfoDisplay(AvailableCamera camera) {
        if (camera != null && tvCameraInfo != null) {
            String flashStatus = camera.hasFlash() ? getString(R.string.flash_yes) : getString(R.string.flash_no);
            String info = String.format(getString(R.string.camera_info_format), camera.getFocalLength(), flashStatus);
            tvCameraInfo.setText(info);
        }
    }

    private void setupResolutionSelectionSpinner() {
        if (dynamicProvider == null) {
            return;
        }

        // Get resolutions for the selected camera
        List<Size> resolutions = dynamicProvider.getResolutionsForSelectedCamera(this);

        if (resolutions == null || resolutions.isEmpty()) {
            tvCameraInfo.setText(getString(R.string.no_resolutions_found));
            return;
        }

        // Apply filter if using standard resolutions only
        if (useStandardResolutions) {
            resolutions = CameraResolutionHelper.filterToStandardResolutions(resolutions);
        }

        // Create array of resolution display names
        String[] resolutionNames = new String[resolutions.size()];
        for (int i = 0; i < resolutions.size(); i++) {
            resolutionNames[i] = AvailableCamera.formatResolution(resolutions.get(i));
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, resolutionNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerResolutionSelection.setAdapter(adapter);

        // Find and set current selection based on saved resolution
        Size savedResolution = dynamicProvider.getResolution();
        int selectedPosition = 0;
        for (int i = 0; i < resolutions.size(); i++) {
            Size res = resolutions.get(i);
            if (res.getWidth() == savedResolution.getWidth() && res.getHeight() == savedResolution.getHeight()) {
                selectedPosition = i;
                break;
            }
        }
        spinnerResolutionSelection.setSelection(selectedPosition);

        // Set up selection listener
        final List<Size> finalResolutions = resolutions;
        spinnerResolutionSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < finalResolutions.size()) {
                    Size selectedResolution = finalResolutions.get(position);
                    dynamicProvider.setSelectedResolution(selectedResolution);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateSectionVisibility(int selectedPosition) {
        if (selectedPosition >= 0 && selectedPosition < EProcessingMode.values().length) {
            EProcessingMode selectedMode = EProcessingMode.values()[selectedPosition];

            if (selectedMode == EProcessingMode.FILE) {
                // Show File Processing, hide HTTPS Post
                llFileProcessing.setVisibility(View.VISIBLE);
                llHttpsPost.setVisibility(View.GONE);
            } else if (selectedMode == EProcessingMode.HTTPSPOST) {
                // Show HTTPS Post, hide File Processing
                llFileProcessing.setVisibility(View.GONE);
                llHttpsPost.setVisibility(View.VISIBLE);
            }
        }
    }

    private void applyLocaleChange(String languageCode) {
        LogUtils.d(TAG, "Applying locale change to: " + languageCode);
        
        // Show toast to indicate language change
        Toast.makeText(this, "Language changed. Restarting app...", Toast.LENGTH_SHORT).show();
        
        // Restart the entire app to apply the new locale to all activities
        // Use a delayed restart to ensure the toast is shown
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                restartApplication();
            }
        }, 1000); // 1 second delay
    }
    
    private void restartApplication() {
        // Clear all activities and restart from EntryChoiceActivity
        Intent intent = new Intent(this, com.zebra.ai_multibarcodes_capture.EntryChoiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Kill the current process to ensure clean restart
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }

    private void saveBarcodeSymbologies(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_AUSTRALIAN_POSTAL, cbAUSTRALIAN_POSTAL.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_AZTEC, cbAZTEC.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CANADIAN_POSTAL, cbCANADIAN_POSTAL.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CHINESE_2OF5, cbCHINESE_2OF5.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CODABAR, cbCODABAR.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CODE11, cbCODE11.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CODE39, cbCODE39.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CODE93, cbCODE93.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_CODE128, cbCODE128.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_COMPOSITE_AB, cbCOMPOSITE_AB.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_COMPOSITE_C, cbCOMPOSITE_C.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_D2OF5, cbD2OF5.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_DATAMATRIX, cbDATAMATRIX.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_DOTCODE, cbDOTCODE.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_DUTCH_POSTAL, cbDUTCH_POSTAL.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_EAN_8, cbEAN_8.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_EAN_13, cbEAN_13.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_FINNISH_POSTAL_4S, cbFINNISH_POSTAL_4S.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GRID_MATRIX, cbGRID_MATRIX.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GS1_DATABAR, cbGS1_DATABAR.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GS1_DATABAR_EXPANDED, cbGS1_DATABAR_EXPANDED.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GS1_DATABAR_LIM, cbGS1_DATABAR_LIM.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GS1_DATAMATRIX, cbGS1_DATAMATRIX.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_GS1_QRCODE, cbGS1_QRCODE.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_HANXIN, cbHANXIN.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_I2OF5, cbI2OF5.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_JAPANESE_POSTAL, cbJAPANESE_POSTAL.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_KOREAN_3OF5, cbKOREAN_3OF5.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MAILMARK, cbMAILMARK.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MATRIX_2OF5, cbMATRIX_2OF5.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MAXICODE, cbMAXICODE.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MICROPDF, cbMICROPDF.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MICROQR, cbMICROQR.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_MSI, cbMSI.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_PDF417, cbPDF417.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_QRCODE, cbQRCODE.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_TLC39, cbTLC39.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_TRIOPTIC39, cbTRIOPTIC39.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_UK_POSTAL, cbUK_POSTAL.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_UPC_A, cbUPC_A.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_UPC_E, cbUPC_E.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_UPCE0, cbUPCE1.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_USPLANET, cbUSPLANET.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_USPOSTNET, cbUSPOSTNET.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_US4STATE, cbUS4STATE.isChecked());
        editor.putBoolean(SHARED_PREFERENCES_US4STATE_FICS, cbUS4STATE_FICS.isChecked());
    }

    private void saveModelInputSize(SharedPreferences.Editor editor) {
        String modelInputSize = "MEDIUM"; // Default
        
        if (rbSmallInputSize.isChecked()) {
            modelInputSize = "SMALL";
        } else if (rbLargeInputSize.isChecked()) {
            modelInputSize = "LARGE";
        }
        
        editor.putString(SHARED_PREFERENCES_MODEL_INPUT_SIZE, modelInputSize);
    }

    private void saveCameraResolution(SharedPreferences.Editor editor) {
        // Save resolution mode
        int modePosition = spinnerResolutionMode.getSelectedItemPosition();
        String mode = (modePosition == 1) ? CameraResolutionProviderFactory.MODE_DYNAMIC : CameraResolutionProviderFactory.MODE_STATIC;
        editor.putString(SHARED_PREFERENCES_RESOLUTION_MODE, mode);

        if (mode.equals(CameraResolutionProviderFactory.MODE_STATIC)) {
            // Save static resolution
            String cameraResolution = "MP_2"; // Default

            if (rb1MPResolution.isChecked()) {
                cameraResolution = "MP_1";
            } else if (rb4MPResolution.isChecked()) {
                cameraResolution = "MP_4";
            } else if (rb8MPResolution.isChecked()) {
                cameraResolution = "MP_8";
            } else if (rb12MPResolution.isChecked()) {
                cameraResolution = "MP_12";
            } else if (rb12_5MPResolution.isChecked()) {
                cameraResolution = "MP_12_5";
            } else if (rb12_6MPResolution.isChecked()) {
                cameraResolution = "MP_12_6";
            }

            editor.putString(SHARED_PREFERENCES_CAMERA_RESOLUTION, cameraResolution);
        } else {
            // Save dynamic resolution settings
            if (dynamicProvider != null) {
                dynamicProvider.saveSettings(this);
            }

            // Save resolution filter setting
            String filterMode = useStandardResolutions ? Constants.RESOLUTION_FILTER_STANDARD : Constants.RESOLUTION_FILTER_ALL;
            editor.putString(SHARED_PREFERENCES_RESOLUTION_FILTER, filterMode);
        }
    }

    private void saveInferenceType(SharedPreferences.Editor editor) {
        String inferenceType = "DSP"; // Default
        
        if (rbGPUInference.isChecked()) {
            inferenceType = "GPU";
        } else if (rbCPUInference.isChecked()) {
            inferenceType = "CPU";
        }
        
        editor.putString(SHARED_PREFERENCES_INFERENCE_TYPE, inferenceType);
    }

    private void saveProcessingMode(SharedPreferences.Editor editor) {
        int selectedPosition = spinnerProcessingMode.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < EProcessingMode.values().length) {
            EProcessingMode selectedMode = EProcessingMode.values()[selectedPosition];
            editor.putString(SHARED_PREFERENCES_PROCESSING_MODE, selectedMode.toString());
        }
    }

    private void saveCaptureTriggerMode(SharedPreferences.Editor editor) {
        int selectedPosition = spinnerCaptureTriggerMode.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < ECaptureTriggerMode.values().length) {
            ECaptureTriggerMode selectedMode = ECaptureTriggerMode.values()[selectedPosition];
            editor.putString(SHARED_PREFERENCES_CAPTURE_TRIGGER_MODE, selectedMode.toString());
        }
    }

    private void loadHttpsPostSettings(SharedPreferences sharedPreferences) {
        String endpoint = sharedPreferences.getString(SHARED_PREFERENCES_HTTPS_ENDPOINT, SHARED_PREFERENCES_HTTPS_ENDPOINT_DEFAULT);

        // Load endpoint as-is, supporting both HTTP and HTTPS
        etHttpsEndpoint.setText(endpoint);
    }

    private void saveHttpsPostSettings(SharedPreferences.Editor editor) {
        String endpoint = etHttpsEndpoint.getText().toString();
        editor.putString(SHARED_PREFERENCES_HTTPS_ENDPOINT, endpoint);
    }

    private String getSelectedExtension()
    {
        if(rbCSV.isChecked())
            return EExportMode.CSV.getExtension();
        else if(rbXSLX.isChecked())
            return EExportMode.EXCEL.getExtension();
        return EExportMode.TEXT.getExtension();
    }

    private void selectExtensionRadioButton(String extension)
    {
        switch (extension)
        {
            case ".csv":
                rbXSLX.setChecked(false);
                rbTXT.setChecked(false);
                rbCSV.setChecked(true);
                break;
            case ".txt":
                rbXSLX.setChecked(false);
                rbTXT.setChecked(true);
                rbCSV.setChecked(false);
                break;
            case ".xlsx":
                rbXSLX.setChecked(true);
                rbTXT.setChecked(false);
                rbCSV.setChecked(false);
                break;

        }
    }
    private void setupSymbologyListeners() {
        CompoundButton.OnCheckedChangeListener badgeUpdateListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSymbologiesBadge();
            }
        };

        // Set listeners for all symbology checkboxes
        cbAUSTRALIAN_POSTAL.setOnCheckedChangeListener(badgeUpdateListener);
        cbAZTEC.setOnCheckedChangeListener(badgeUpdateListener);
        cbCANADIAN_POSTAL.setOnCheckedChangeListener(badgeUpdateListener);
        cbCHINESE_2OF5.setOnCheckedChangeListener(badgeUpdateListener);
        cbCODABAR.setOnCheckedChangeListener(badgeUpdateListener);
        cbCODE11.setOnCheckedChangeListener(badgeUpdateListener);
        cbCODE39.setOnCheckedChangeListener(badgeUpdateListener);
        cbCODE93.setOnCheckedChangeListener(badgeUpdateListener);
        cbCODE128.setOnCheckedChangeListener(badgeUpdateListener);
        cbCOMPOSITE_AB.setOnCheckedChangeListener(badgeUpdateListener);
        cbCOMPOSITE_C.setOnCheckedChangeListener(badgeUpdateListener);
        cbD2OF5.setOnCheckedChangeListener(badgeUpdateListener);
        cbDATAMATRIX.setOnCheckedChangeListener(badgeUpdateListener);
        cbDOTCODE.setOnCheckedChangeListener(badgeUpdateListener);
        cbDUTCH_POSTAL.setOnCheckedChangeListener(badgeUpdateListener);
        cbEAN_8.setOnCheckedChangeListener(badgeUpdateListener);
        cbEAN_13.setOnCheckedChangeListener(badgeUpdateListener);
        cbFINNISH_POSTAL_4S.setOnCheckedChangeListener(badgeUpdateListener);
        cbGRID_MATRIX.setOnCheckedChangeListener(badgeUpdateListener);
        cbGS1_DATABAR.setOnCheckedChangeListener(badgeUpdateListener);
        cbGS1_DATABAR_EXPANDED.setOnCheckedChangeListener(badgeUpdateListener);
        cbGS1_DATABAR_LIM.setOnCheckedChangeListener(badgeUpdateListener);
        cbGS1_DATAMATRIX.setOnCheckedChangeListener(badgeUpdateListener);
        cbGS1_QRCODE.setOnCheckedChangeListener(badgeUpdateListener);
        cbHANXIN.setOnCheckedChangeListener(badgeUpdateListener);
        cbI2OF5.setOnCheckedChangeListener(badgeUpdateListener);
        cbJAPANESE_POSTAL.setOnCheckedChangeListener(badgeUpdateListener);
        cbKOREAN_3OF5.setOnCheckedChangeListener(badgeUpdateListener);
        cbMAILMARK.setOnCheckedChangeListener(badgeUpdateListener);
        cbMATRIX_2OF5.setOnCheckedChangeListener(badgeUpdateListener);
        cbMAXICODE.setOnCheckedChangeListener(badgeUpdateListener);
        cbMICROPDF.setOnCheckedChangeListener(badgeUpdateListener);
        cbMICROQR.setOnCheckedChangeListener(badgeUpdateListener);
        cbMSI.setOnCheckedChangeListener(badgeUpdateListener);
        cbPDF417.setOnCheckedChangeListener(badgeUpdateListener);
        cbQRCODE.setOnCheckedChangeListener(badgeUpdateListener);
        cbTLC39.setOnCheckedChangeListener(badgeUpdateListener);
        cbTRIOPTIC39.setOnCheckedChangeListener(badgeUpdateListener);
        cbUK_POSTAL.setOnCheckedChangeListener(badgeUpdateListener);
        cbUPC_A.setOnCheckedChangeListener(badgeUpdateListener);
        cbUPC_E.setOnCheckedChangeListener(badgeUpdateListener);
        cbUPCE1.setOnCheckedChangeListener(badgeUpdateListener);
        cbUSPLANET.setOnCheckedChangeListener(badgeUpdateListener);
        cbUSPOSTNET.setOnCheckedChangeListener(badgeUpdateListener);
        cbUS4STATE.setOnCheckedChangeListener(badgeUpdateListener);
        cbUS4STATE_FICS.setOnCheckedChangeListener(badgeUpdateListener);
    }

    private void updateSymbologiesBadge() {
        int count = 0;
        
        if (cbAUSTRALIAN_POSTAL.isChecked()) count++;
        if (cbAZTEC.isChecked()) count++;
        if (cbCANADIAN_POSTAL.isChecked()) count++;
        if (cbCHINESE_2OF5.isChecked()) count++;
        if (cbCODABAR.isChecked()) count++;
        if (cbCODE11.isChecked()) count++;
        if (cbCODE39.isChecked()) count++;
        if (cbCODE93.isChecked()) count++;
        if (cbCODE128.isChecked()) count++;
        if (cbCOMPOSITE_AB.isChecked()) count++;
        if (cbCOMPOSITE_C.isChecked()) count++;
        if (cbD2OF5.isChecked()) count++;
        if (cbDATAMATRIX.isChecked()) count++;
        if (cbDOTCODE.isChecked()) count++;
        if (cbDUTCH_POSTAL.isChecked()) count++;
        if (cbEAN_8.isChecked()) count++;
        if (cbEAN_13.isChecked()) count++;
        if (cbFINNISH_POSTAL_4S.isChecked()) count++;
        if (cbGRID_MATRIX.isChecked()) count++;
        if (cbGS1_DATABAR.isChecked()) count++;
        if (cbGS1_DATABAR_EXPANDED.isChecked()) count++;
        if (cbGS1_DATABAR_LIM.isChecked()) count++;
        if (cbGS1_DATAMATRIX.isChecked()) count++;
        if (cbGS1_QRCODE.isChecked()) count++;
        if (cbHANXIN.isChecked()) count++;
        if (cbI2OF5.isChecked()) count++;
        if (cbJAPANESE_POSTAL.isChecked()) count++;
        if (cbKOREAN_3OF5.isChecked()) count++;
        if (cbMAILMARK.isChecked()) count++;
        if (cbMATRIX_2OF5.isChecked()) count++;
        if (cbMAXICODE.isChecked()) count++;
        if (cbMICROPDF.isChecked()) count++;
        if (cbMICROQR.isChecked()) count++;
        if (cbMSI.isChecked()) count++;
        if (cbPDF417.isChecked()) count++;
        if (cbQRCODE.isChecked()) count++;
        if (cbTLC39.isChecked()) count++;
        if (cbTRIOPTIC39.isChecked()) count++;
        if (cbUK_POSTAL.isChecked()) count++;
        if (cbUPC_A.isChecked()) count++;
        if (cbUPC_E.isChecked()) count++;
        if (cbUPCE1.isChecked()) count++;
        if (cbUSPLANET.isChecked()) count++;
        if (cbUSPOSTNET.isChecked()) count++;
        if (cbUS4STATE.isChecked()) count++;
        if (cbUS4STATE_FICS.isChecked()) count++;
        
        tvSymbologiesBadge.setText(String.valueOf(count));
    }

    private void toggleSymbologies() {
        isSymbologiesExpanded = !isSymbologiesExpanded;
        
        if (isSymbologiesExpanded) {
            llSymbologies.setVisibility(View.VISIBLE);
            ivToggleSymbologies.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llSymbologies.setVisibility(View.GONE);
            ivToggleSymbologies.setImageResource(R.drawable.ic_expand_less_24);
        }
    }

    private void toggleFileTypes() {
        isFileTypesExpanded = !isFileTypesExpanded;

        if (isFileTypesExpanded) {
            llFileProcessingContent.setVisibility(View.VISIBLE);
            ivToggleFileTypes.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llFileProcessingContent.setVisibility(View.GONE);
            ivToggleFileTypes.setImageResource(R.drawable.ic_expand_less_24);
        }
    }

    private void toggleAdvanced() {
        isAdvancedExpanded = !isAdvancedExpanded;

        if (isAdvancedExpanded) {
            llAdvancedContent.setVisibility(View.VISIBLE);
            ivToggleAdvanced.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llAdvancedContent.setVisibility(View.GONE);
            ivToggleAdvanced.setImageResource(R.drawable.ic_expand_less_24);
        }
    }

    private void toggleHttpsPost() {
        isHttpsPostExpanded = !isHttpsPostExpanded;

        if (isHttpsPostExpanded) {
            llHttpsPostContent.setVisibility(View.VISIBLE);
            ivToggleHttpsPost.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llHttpsPostContent.setVisibility(View.GONE);
            ivToggleHttpsPost.setImageResource(R.drawable.ic_expand_less_24);
        }
    }


    private void createDataWedgeProfile()
    {
        DWProfileSetConfigSettings settings = new DWProfileSetConfigSettings()
        {{
            mProfileName = SettingsActivity.this.getPackageName();
            mTimeOutMS = 5000;
            MainBundle.APP_LIST = new HashMap<>();
            MainBundle.APP_LIST.put(SettingsActivity.this.getPackageName(), null);
            MainBundle.CONFIG_MODE = MB_E_CONFIG_MODE.CREATE_IF_NOT_EXIST;
            IntentPlugin.intent_action = SettingsActivity.this.getPackageName() + ".RECVR";
            IntentPlugin.intent_category = "android.intent.category.DEFAULT";;
            IntentPlugin.intent_output_enabled = true;
            IntentPlugin.intent_delivery = INT_E_DELIVERY.BROADCAST;
            KeystrokePlugin.keystroke_output_enabled = false;
            ScannerPlugin.scanner_selection_by_identifier = SC_E_SCANNER_IDENTIFIER.AUTO;
            ScannerPlugin.scanner_input_enabled = true;
            ScannerPlugin.Decoders.decoder_qrcode = true;
            ScannerPlugin.Decoders.decoder_ean8 = false;
            ScannerPlugin.Decoders.decoder_ean13 = false;
            ScannerPlugin.Decoders.decoder_aztec = false;
            ScannerPlugin.Decoders.decoder_micropdf = false;
        }};

        CreateProfileHelper.createProfile(SettingsActivity.this, settings, new CreateProfileHelper.CreateProfileHelperCallback() {
            @Override
            public void onSuccess(String profileName) {

            }

            @Override
            public void onError(String profileName, String error, String errorMessage) {

            }

            @Override
            public void ondebugMessage(String profileName, String message) {
            }
        });

    }

    private void initializeScanReceiver()
    {
        mScanReceiver = new DWScanReceiver(this,
                SettingsActivity.this.getPackageName() + ".RECVR",
                "android.intent.category.DEFAULT",
                false,
                new DWScanReceiver.onScannedData() {
                    @Override
                    public void scannedData(String source, String data, String symbology) {
                        if(symbology.equalsIgnoreCase("QRCODE"))
                        {
                            if(data != null && data.startsWith("AIMultiBarcodeEndpoint:"))
                            {
                                String endpoint = data.substring("AIMultiBarcodeEndpoint:".length());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        etHttpsEndpoint.setText(endpoint);
                                        Toast.makeText(SettingsActivity.this,
                                            getString(R.string.endpoint_updated_from_qr), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void enableDataWedgePlugin()
    {
        DWScannerPluginEnable dwpluginenable = new DWScannerPluginEnable(this);
        DWProfileBaseSettings settings = new DWProfileBaseSettings()
        {{
            mProfileName = SettingsActivity.this.getPackageName();
        }};

        dwpluginenable.execute(settings, new DWProfileCommandBase.onProfileCommandResult() {
            @Override
            public void result(String profileName, String action, String command, String result, String resultInfo, String commandidentifier) {
            }

            @Override
            public void timeout(String profileName) {

            }
        });
    }

    private void disableDatawedgePlugin()
    {
        DWScannerPluginDisable dwplugindisable = new DWScannerPluginDisable(this);
        DWProfileBaseSettings settings = new DWProfileBaseSettings()
        {{
            mProfileName = SettingsActivity.this.getPackageName();
        }};

        dwplugindisable.execute(settings, new DWProfileCommandBase.onProfileCommandResult() {
            @Override
            public void result(String profileName, String action, String command, String result, String resultInfo, String commandidentifier) {
            }
            @Override
            public void timeout(String profileName) {

            }
        });
    }

    private void setupFilteringListeners() {
        // Set up click listener for edit filtering conditions button
        btEditFilteringConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, FilteringConditionsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadFilteringSettings(SharedPreferences sharedPreferences) {
        boolean filteringEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_FILTERING_ENABLED, SHARED_PREFERENCES_FILTERING_ENABLED_DEFAULT);
        cbEnableFiltering.setChecked(filteringEnabled);
    }

    private void saveFilteringSettings(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_FILTERING_ENABLED, cbEnableFiltering.isChecked());
    }

    private void toggleFiltering() {
        isFilteringExpanded = !isFilteringExpanded;

        if (isFilteringExpanded) {
            llFilteringContent.setVisibility(View.VISIBLE);
            ivToggleFiltering.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llFilteringContent.setVisibility(View.GONE);
            ivToggleFiltering.setImageResource(R.drawable.ic_expand_less_24);
        }
    }

    private void loadDisplayAnalysisPerSecond(SharedPreferences sharedPreferences) {
        boolean displayAnalysisPerSecond = sharedPreferences.getBoolean(SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND, SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND_DEFAULT);
        cbDisplayAnalysisPerSecond.setChecked(displayAnalysisPerSecond);
    }

    private void saveDisplayAnalysisPerSecond(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_DISPLAY_ANALYSIS_PER_SECOND, cbDisplayAnalysisPerSecond.isChecked());
    }

    private void loadLoggingEnabled(SharedPreferences sharedPreferences) {
        boolean loggingEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_LOGGING_ENABLED, SHARED_PREFERENCES_LOGGING_ENABLED_DEFAULT);
        cbLoggingEnabled.setChecked(loggingEnabled);
    }

    private void saveLoggingEnabled(SharedPreferences.Editor editor) {
        boolean loggingEnabled = cbLoggingEnabled.isChecked();
        editor.putBoolean(SHARED_PREFERENCES_LOGGING_ENABLED, loggingEnabled);
        // Apply the setting immediately to LogUtils
        LogUtils.setLoggingEnabled(loggingEnabled);
    }

    private void loadForceContinuousAutofocus(SharedPreferences sharedPreferences) {
        boolean forceContinuousAutofocus = sharedPreferences.getBoolean(SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS, SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS_DEFAULT);
        cbForceContinuousAutofocus.setChecked(forceContinuousAutofocus);
    }

    private void saveForceContinuousAutofocus(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_FORCE_CONTINUOUS_AUTOFOCUS, cbForceContinuousAutofocus.isChecked());
    }

    private void setupDebounceListeners() {
        // Setup algorithm spinner
        setupDebounceAlgorithmSpinner();

        // Checkbox listener to enable/disable sliders
        cbDebounceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateDebounceUIState(isChecked);
            }
        });

        // Max Frames SeekBar listener
        sbDebounceMaxFrames.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDebounceMaxFramesValue.setText(getString(R.string.debounce_max_frames_value, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Threshold SeekBar listener (Center Distance)
        sbDebounceThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvDebounceThresholdValue.setText(getString(R.string.debounce_threshold_value, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // IOU Threshold SeekBar listener
        sbDebounceIouThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float iouValue = progress / 100.0f;
                tvDebounceIouThresholdValue.setText(String.format(getString(R.string.debounce_iou_threshold_value), iouValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupDebounceAlgorithmSpinner() {
        // Create array of algorithm display names
        String[] algorithmNames = getResources().getStringArray(R.array.debounce_algorithm_names);

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, algorithmNames);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDebounceAlgorithm.setAdapter(adapter);

        // Set up selection listener
        spinnerDebounceAlgorithm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDebounceAlgorithmVisibility(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateDebounceAlgorithmVisibility(int algorithmIndex) {
        // 0 = Center Distance, 1 = IOU
        if (algorithmIndex == 0) {
            llDebounceThreshold.setVisibility(View.VISIBLE);
            llDebounceIouThreshold.setVisibility(View.GONE);
        } else {
            llDebounceThreshold.setVisibility(View.GONE);
            llDebounceIouThreshold.setVisibility(View.VISIBLE);
        }
    }

    private void updateDebounceUIState(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.5f;
        llDebounceAlgorithm.setAlpha(alpha);
        llDebounceMaxFrames.setAlpha(alpha);
        llDebounceThreshold.setAlpha(alpha);
        llDebounceIouThreshold.setAlpha(alpha);
        spinnerDebounceAlgorithm.setEnabled(enabled);
        sbDebounceMaxFrames.setEnabled(enabled);
        sbDebounceThreshold.setEnabled(enabled);
        sbDebounceIouThreshold.setEnabled(enabled);
    }

    private void loadDebounceSettings(SharedPreferences sharedPreferences) {
        boolean debounceEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_DEBOUNCE_ENABLED, SHARED_PREFERENCES_DEBOUNCE_ENABLED_DEFAULT);
        int maxFrames = sharedPreferences.getInt(SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES, SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES_DEFAULT);
        int threshold = sharedPreferences.getInt(SHARED_PREFERENCES_DEBOUNCE_THRESHOLD, SHARED_PREFERENCES_DEBOUNCE_THRESHOLD_DEFAULT);
        int algorithm = sharedPreferences.getInt(SHARED_PREFERENCES_DEBOUNCE_ALGORITHM, SHARED_PREFERENCES_DEBOUNCE_ALGORITHM_DEFAULT);
        int iouThreshold = sharedPreferences.getInt(SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD, SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD_DEFAULT);

        cbDebounceEnabled.setChecked(debounceEnabled);
        sbDebounceMaxFrames.setProgress(maxFrames);
        sbDebounceThreshold.setProgress(threshold);
        spinnerDebounceAlgorithm.setSelection(algorithm);
        sbDebounceIouThreshold.setProgress(iouThreshold);

        // Update value labels
        tvDebounceMaxFramesValue.setText(getString(R.string.debounce_max_frames_value, maxFrames));
        tvDebounceThresholdValue.setText(getString(R.string.debounce_threshold_value, threshold));
        float iouValue = iouThreshold / 100.0f;
        tvDebounceIouThresholdValue.setText(String.format(getString(R.string.debounce_iou_threshold_value), iouValue));

        // Update UI state based on checkbox
        updateDebounceUIState(debounceEnabled);

        // Update visibility based on algorithm selection
        updateDebounceAlgorithmVisibility(algorithm);
    }

    private void saveDebounceSettings(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_DEBOUNCE_ENABLED, cbDebounceEnabled.isChecked());
        editor.putInt(SHARED_PREFERENCES_DEBOUNCE_MAX_FRAMES, sbDebounceMaxFrames.getProgress());
        editor.putInt(SHARED_PREFERENCES_DEBOUNCE_THRESHOLD, sbDebounceThreshold.getProgress());
        editor.putInt(SHARED_PREFERENCES_DEBOUNCE_ALGORITHM, spinnerDebounceAlgorithm.getSelectedItemPosition());
        editor.putInt(SHARED_PREFERENCES_DEBOUNCE_IOU_THRESHOLD, sbDebounceIouThreshold.getProgress());
    }

    private void toggleAutoCapture() {
        isAutoCaptureExpanded = !isAutoCaptureExpanded;

        if (isAutoCaptureExpanded) {
            llAutoCaptureContent.setVisibility(View.VISIBLE);
            ivToggleAutoCapture.setImageResource(R.drawable.ic_expand_more_24);
        } else {
            llAutoCaptureContent.setVisibility(View.GONE);
            ivToggleAutoCapture.setImageResource(R.drawable.ic_expand_less_24);
        }
    }

    private void loadAutoCaptureSettings() {
        boolean autoCaptureEnabled = AutoCapturePreferencesHelper.isAutoCaptureEnabled(this);
        cbEnableAutoCapture.setChecked(autoCaptureEnabled);
    }

    private void saveAutoCaptureSettings() {
        AutoCapturePreferencesHelper.saveAutoCaptureEnabled(this, cbEnableAutoCapture.isChecked());
    }
}
