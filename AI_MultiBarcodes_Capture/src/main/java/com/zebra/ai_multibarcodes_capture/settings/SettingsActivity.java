package com.zebra.ai_multibarcodes_capture.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.adapters.LanguageAdapter;
import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;
import com.zebra.ai_multibarcodes_capture.helpers.EProcessingMode;
import com.zebra.ai_multibarcodes_capture.helpers.KeystoreHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;
import com.zebra.ai_multibarcodes_capture.models.LanguageItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private EditText etPrefix, etHttpsEndpoint, etUsername, etPassword;
    private RadioButton rbCSV, rbTXT, rbXSLX;
    private CheckBox cbAuthentication;
    private ImageView ivToggleSymbologies, ivToggleFileTypes, ivToggleAdvanced, ivToggleHttpsPost;
    private LinearLayout llSymbologies, llAdvancedContent, llFileProcessingContent, llHttpsPost, llAuthenticationGroup, llFileProcessing, llHttpsPostContent;
    private RadioGroup rgFileTypes, rgModelInputSize, rgCameraResolution, rgInferenceType;
    private RadioButton rbSmallInputSize, rbMediumInputSize, rbLargeInputSize;
    private RadioButton rb1MPResolution, rb2MPResolution, rb4MPResolution, rb8MPResolution;
    private RadioButton rbDSPInference, rbGPUInference, rbCPUInference;
    private TextView tvSymbologiesBadge;
    private boolean isSymbologiesExpanded = false;
    private boolean isFileTypesExpanded = false;
    private boolean isAdvancedExpanded = false;
    private boolean isHttpsPostExpanded = false;
    private Spinner spinnerLanguage, spinnerProcessingMode;
    private LanguageAdapter languageAdapter;
    private List<LanguageItem> languageList;
    private String pendingLanguageCode = null; // Track pending language changes
    private KeystoreHelper keystoreHelper;
    private String originalDummyPassword = null; // Track original dummy password to detect changes
    private CheckBox cbAUSTRALIAN_POSTAL, cbAZTEC, cbCANADIAN_POSTAL, cbCHINESE_2OF5, cbCODABAR;
    private CheckBox cbCODE11, cbCODE39, cbCODE93, cbCODE128, cbCOMPOSITE_AB, cbCOMPOSITE_C;
    private CheckBox cbD2OF5, cbDATAMATRIX, cbDOTCODE, cbDUTCH_POSTAL, cbEAN_8, cbEAN_13;
    private CheckBox cbFINNISH_POSTAL_4S, cbGRID_MATRIX, cbGS1_DATABAR, cbGS1_DATABAR_EXPANDED;
    private CheckBox cbGS1_DATABAR_LIM, cbGS1_DATAMATRIX, cbGS1_QRCODE, cbHANXIN, cbI2OF5;
    private CheckBox cbJAPANESE_POSTAL, cbKOREAN_3OF5, cbMAILMARK, cbMATRIX_2OF5, cbMAXICODE;
    private CheckBox cbMICROPDF, cbMICROQR, cbMSI, cbPDF417, cbQRCODE, cbTLC39, cbTRIOPTIC39;
    private CheckBox cbUK_POSTAL, cbUPC_A, cbUPC_E, cbUPCE1, cbUSPLANET, cbUSPOSTNET;
    private CheckBox cbUS4STATE, cbUS4STATE_FICS;

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
        setContentView(R.layout.activity_setup);
        
        // Configure system bars
        configureSystemBars();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize KeystoreHelper
        keystoreHelper = new KeystoreHelper(this);
        
        etPrefix = findViewById(R.id.etPrefix);
        etHttpsEndpoint = findViewById(R.id.etHttpsEndpoint);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbAuthentication = findViewById(R.id.cbAuthentication);
        rbCSV = findViewById(R.id.rbCSV);
        rbTXT = findViewById(R.id.rbTxt);
        rbXSLX = findViewById(R.id.rbXSLX);
        ivToggleSymbologies = findViewById(R.id.ivToggleSymbologies);
        ivToggleFileTypes = findViewById(R.id.ivToggleFileTypes);
        ivToggleAdvanced = findViewById(R.id.ivToggleAdvanced);
        ivToggleHttpsPost = findViewById(R.id.ivToggleHttpsPost);
        llSymbologies = findViewById(R.id.llSymbologies);
        llAdvancedContent = findViewById(R.id.llAdvancedContent);
        llFileProcessingContent = findViewById(R.id.llFileProcessingContent);
        llFileProcessing = findViewById(R.id.llFileProcessing);
        llHttpsPost = findViewById(R.id.llHttpsPost);
        llHttpsPostContent = findViewById(R.id.llHttpsPostContent);
        llAuthenticationGroup = findViewById(R.id.llAuthenticationGroup);
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
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerProcessingMode = findViewById(R.id.spinnerProcessingMode);

        // Setup language spinner
        setupLanguageSpinner();

        // Setup processing mode spinner
        setupProcessingModeSpinner();

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

        // Set up authentication checkbox listener
        cbAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    llAuthenticationGroup.setVisibility(View.VISIBLE);
                } else {
                    llAuthenticationGroup.setVisibility(View.GONE);
                    // Clear credentials when authentication is disabled for security
                    etUsername.setText("");
                    etPassword.setText("");
                    originalDummyPassword = "";
                    try {
                        keystoreHelper.clearAllCredentials();
                        LogUtils.d(TAG, "Cleared credentials when authentication disabled");
                    } catch (Exception e) {
                        LogUtils.e(TAG, "Error clearing credentials: " + e.getMessage());
                    }
                }
            }
        });

        // Setup symbology checkboxes change listeners
        setupSymbologyListeners();

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

    private void setupProcessingModeSpinner() {
        // Create array of processing mode display names
        String[] processingModeDisplayNames = new String[EProcessingMode.values().length];
        for (int i = 0; i < EProcessingMode.values().length; i++) {
            processingModeDisplayNames[i] = EProcessingMode.values()[i].getDisplayName(this);
        }

        // Create adapter and set to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, processingModeDisplayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    private void loadHttpsPostSettings(SharedPreferences sharedPreferences) {
        String endpoint = sharedPreferences.getString(SHARED_PREFERENCES_HTTPS_ENDPOINT, SHARED_PREFERENCES_HTTPS_ENDPOINT_DEFAULT);
        boolean authentication = sharedPreferences.getBoolean(SHARED_PREFERENCES_HTTPS_AUTHENTICATION, SHARED_PREFERENCES_HTTPS_AUTHENTICATION_DEFAULT);

        // Load endpoint as-is, supporting both HTTP and HTTPS

        etHttpsEndpoint.setText(endpoint);
        cbAuthentication.setChecked(authentication);

        // Load username from secure keystore and show dummy password
        try {
            String username = keystoreHelper.getUsername();
            etUsername.setText(username);

            // Show dummy characters matching the stored password length for security
            int passwordLength = keystoreHelper.getPasswordLength();
            if (passwordLength > 0) {
                // Create dummy password with bullet characters (•)
                StringBuilder dummyPassword = new StringBuilder();
                for (int i = 0; i < passwordLength; i++) {
                    dummyPassword.append("•");
                }
                originalDummyPassword = dummyPassword.toString();
                etPassword.setText(originalDummyPassword);
                LogUtils.d(TAG, "Loaded username and displayed dummy password with length: " + passwordLength);
            } else {
                originalDummyPassword = "";
                etPassword.setText("");
                LogUtils.d(TAG, "No stored password found");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error loading credentials from keystore: " + e.getMessage());
            etUsername.setText("");
            etPassword.setText("");
            originalDummyPassword = "";
        }

        // Set initial visibility of authentication group
        if (authentication) {
            llAuthenticationGroup.setVisibility(View.VISIBLE);
        } else {
            llAuthenticationGroup.setVisibility(View.GONE);
        }
    }

    private void saveHttpsPostSettings(SharedPreferences.Editor editor) {
        String endpoint = etHttpsEndpoint.getText().toString();
        boolean authentication = cbAuthentication.isChecked();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        editor.putString(SHARED_PREFERENCES_HTTPS_ENDPOINT, endpoint);
        editor.putBoolean(SHARED_PREFERENCES_HTTPS_AUTHENTICATION, authentication);

        // Save username and password securely in keystore
        try {
            boolean usernameStored = keystoreHelper.storeUsername(username);
            boolean passwordStored = true;

            // Only save password if it has been modified by the user (not the dummy password)
            if (!password.equals(originalDummyPassword)) {
                passwordStored = keystoreHelper.storePassword(password);
                LogUtils.d(TAG, "Password was modified, storing new password");
            } else {
                LogUtils.d(TAG, "Password unchanged (dummy), keeping existing stored password");
            }

            if (usernameStored && passwordStored) {
                LogUtils.d(TAG, "Successfully stored credentials in keystore");
            } else {
                LogUtils.w(TAG, "Warning: Some credentials may not have been stored properly");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "Error storing credentials in keystore: " + e.getMessage());
            // Show user-friendly error message
            Toast.makeText(this, "Warning: Could not securely store credentials", Toast.LENGTH_LONG).show();
        }
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
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Set status bar color to zebra blue
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.zebra));
        
        // Set navigation bar color to black for consistent theming
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.black));
        }
        
        // Set status bar text to light (white) to contrast with blue background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_STATUS_BAR to use light (white) text
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        
        // Set navigation bar text to light (white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR to use light (white) icons
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }
}
