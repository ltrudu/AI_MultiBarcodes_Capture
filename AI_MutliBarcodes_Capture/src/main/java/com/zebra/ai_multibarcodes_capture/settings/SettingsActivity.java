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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;
import com.zebra.ai_multibarcodes_capture.managedconfig.ManagedConfigurationReceiver;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private EditText etPrefix;
    private RadioButton rbCSV, rbTXT, rbXSLX;
    private CheckBox cbOpenSymbologies;
    private ScrollView svSymbologies;
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
                Log.d(TAG, "Received reload preferences request from ManagedConfigurationReceiver");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadPreferences();
                        Toast.makeText(SettingsActivity.this, 
                            "Settings updated by administrator", Toast.LENGTH_SHORT).show();
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
        
        etPrefix = findViewById(R.id.etPrefix);
        rbCSV = findViewById(R.id.rbCSV);
        rbTXT = findViewById(R.id.rbTxt);
        rbXSLX = findViewById(R.id.rbXSLX);
        cbOpenSymbologies = findViewById(R.id.cbOpenSymbologies);
        svSymbologies = findViewById(R.id.svSymbologies);
        
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

        // Initially hide the ScrollView
        svSymbologies.setVisibility(View.GONE);

        // Set up checkbox listener to show/hide symbologies ScrollView
        cbOpenSymbologies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    svSymbologies.setVisibility(View.VISIBLE);
                } else {
                    svSymbologies.setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        super.onResume();
        
        // Register the BroadcastReceiver to listen for managed configuration changes
        IntentFilter filter = new IntentFilter(ManagedConfigurationReceiver.ACTION_RELOAD_PREFERENCES);
        registerReceiver(reloadPreferencesReceiver, filter);
        Log.d(TAG, "Registered BroadcastReceiver for managed configuration changes");
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Unregister the BroadcastReceiver
        try {
            unregisterReceiver(reloadPreferencesReceiver);
            Log.d(TAG, "Unregistered BroadcastReceiver for managed configuration changes");
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
            Log.d(TAG, "BroadcastReceiver was not registered, ignoring unregister attempt");
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

        editor.putString(SHARED_PREFERENCES_EXTENSION, getSelectedExtension());

        // Commit the changes
        editor.commit();

        Toast.makeText(this, getString(R.string.settings_saved_successfully), Toast.LENGTH_LONG).show();

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
