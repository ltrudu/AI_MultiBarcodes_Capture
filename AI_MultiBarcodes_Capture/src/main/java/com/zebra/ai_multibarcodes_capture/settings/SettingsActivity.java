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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.adapters.LanguageAdapter;
import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;
import com.zebra.ai_multibarcodes_capture.helpers.ECaptureTriggerMode;
import com.zebra.ai_multibarcodes_capture.helpers.EProcessingMode;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
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

    private EditText etPrefix, etHttpsEndpoint, etFilteringRegex;
    private RadioButton rbCSV, rbTXT, rbXSLX;
    private ImageView ivToggleSymbologies, ivToggleFileTypes, ivToggleAdvanced, ivToggleHttpsPost, ivToggleFiltering;
    private LinearLayout llSymbologies, llAdvancedContent, llFileProcessingContent, llHttpsPost, llFileProcessing, llHttpsPostContent, llFilteringContent;
    private CheckBox cbEnableFiltering;
    private RadioGroup rgFileTypes, rgModelInputSize, rgCameraResolution, rgInferenceType;
    private RadioButton rbSmallInputSize, rbMediumInputSize, rbLargeInputSize;
    private RadioButton rb1MPResolution, rb2MPResolution, rb4MPResolution, rb8MPResolution;
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
        applyTheme();

        setContentView(R.layout.activity_setup);
        
        // Configure system bars
        configureSystemBars();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etPrefix = findViewById(R.id.etPrefix);
        etHttpsEndpoint = findViewById(R.id.etHttpsEndpoint);
        etFilteringRegex = findViewById(R.id.etFilteringRegex);
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


        // Setup symbology checkboxes change listeners
        setupSymbologyListeners();

        // Setup filtering checkbox listener
        setupFilteringListeners();

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
        
        // Set the radio button based on saved preference
        if ("MP_1".equals(cameraResolution)) {
            rb1MPResolution.setChecked(true);
        } else if ("MP_4".equals(cameraResolution)) {
            rb4MPResolution.setChecked(true);
        } else if ("MP_8".equals(cameraResolution)) {
            rb8MPResolution.setChecked(true);
        } else {
            // Default to MP_2 (2MP)
            rb2MPResolution.setChecked(true);
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
        String cameraResolution = "MP_2"; // Default
        
        if (rb1MPResolution.isChecked()) {
            cameraResolution = "MP_1";
        } else if (rb4MPResolution.isChecked()) {
            cameraResolution = "MP_4";
        } else if (rb8MPResolution.isChecked()) {
            cameraResolution = "MP_8";
        }
        
        editor.putString(SHARED_PREFERENCES_CAMERA_RESOLUTION, cameraResolution);
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

    private void configureSystemBars() {
        Window window = getWindow();

        // 1. Set the Navigation Bar Background Color to Black
        window.setNavigationBarColor(Color.BLACK);

        // 2. Control the Navigation Bar Icon Color (Light/White)
        // Ensure the system bars are drawn over the app's content
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Use the compatibility controller for managing bar appearance
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        // Request light navigation bar icons (white)
        // Setting this to 'false' tells the system to use light icons on a dark background.
        controller.setAppearanceLightNavigationBars(false);

        // Force status bar color
        View rootLayout = findViewById(R.id.rl_setup_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+

            // 1. Set Navigation Bar background color using the WindowInsetsListener on decorView
            window.getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    // Set the background color to the view (decorView) - BLACK for navigation bar
                    view.setBackgroundColor(Color.BLACK);
                    return insets;
                }
            });

            // 2. Handle Status Bar color and Root Layout padding using ViewCompat
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
                // Get the system bar insets (status bar and navigation bar area)
                // Use getInsets(WindowInsetsCompat.Type.systemBars())
                // equivalent to the Kotlin line
                androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                int statusBarHeight = insets.top;

                // Below code is for adding padding top and bottom (setting margins on the rootLayout)
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;

                    // The Kotlin updateLayoutParams<MarginLayoutParams> block is equivalent to this:
                    marginLp.topMargin = insets.top;
                    marginLp.bottomMargin = insets.bottom;
                    v.setLayoutParams(marginLp); // Apply the updated layout params
                }


                // 3. Create and add a separate Status Bar View
                View statusBarView = new View(getApplicationContext());

                // Below code is for setting color and height to notification bar
                // Height is the status bar height
                statusBarView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight
                ));

                // Set the status bar color using ContextCompat
                statusBarView.setBackgroundColor(getColor(R.color.zebra));

                // Add the view to the activity's content view group
                addContentView(statusBarView, statusBarView.getLayoutParams());

                // Consume the insets so they aren't passed down further
                return WindowInsetsCompat.CONSUMED;
            });

        } else {
            // For Android 14 and below
            window.setStatusBarColor(getColor(R.color.zebra));
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
        cbEnableFiltering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etFilteringRegex.setEnabled(isChecked);
            }
        });
    }

    private void loadFilteringSettings(SharedPreferences sharedPreferences) {
        boolean filteringEnabled = sharedPreferences.getBoolean(SHARED_PREFERENCES_FILTERING_ENABLED, SHARED_PREFERENCES_FILTERING_ENABLED_DEFAULT);
        String filteringRegex = sharedPreferences.getString(SHARED_PREFERENCES_FILTERING_REGEX, SHARED_PREFERENCES_FILTERING_REGEX_DEFAULT);

        cbEnableFiltering.setChecked(filteringEnabled);
        etFilteringRegex.setText(filteringRegex);
        etFilteringRegex.setEnabled(filteringEnabled);
    }

    private void saveFilteringSettings(SharedPreferences.Editor editor) {
        editor.putBoolean(SHARED_PREFERENCES_FILTERING_ENABLED, cbEnableFiltering.isChecked());
        editor.putString(SHARED_PREFERENCES_FILTERING_REGEX, etFilteringRegex.getText().toString());
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

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String theme = sharedPreferences.getString(SHARED_PREFERENCES_THEME, SHARED_PREFERENCES_THEME_DEFAULT);

        if ("modern".equals(theme)) {
            setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_ActionBar_Modern);
        } else {
            setTheme(R.style.Base_Theme_AIMultiBarcodes_Capture_ActionBar_Legacy);
        }
    }
}
