package com.zebra.ai_multibarcodes_capture.dataeditor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import android.content.SharedPreferences;
import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;

public class BarcodeDataEditorActivity extends AppCompatActivity {

    // Private members to hold values from intent
    private int position;
    private String barcodeValue;
    private int barcodeSymbology;
    private int barcodeQuantity;
    private Date barcodeDate;

    // UI components
    private TextView tvBarcodeValue;
    private TextView tvSymbologyValue;
    private EditText etQuantity;
    private Button btQuantityMinus;
    private Button btQuantityPlus;
    private TextView tvDateValue;
    private Button btSave;
    private Button btCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeHelpers.applyTheme(this);

        setContentView(R.layout.activity_barcode_data_editor);
        
        // Configure system bars
        ThemeHelpers.configureSystemBars(this, R.id.rl_barcode_data_editor);
        ThemeHelpers.applyCustomFont(this);

        // Setup toolbar
        setupToolbar();
        
        // Initialize UI components
        initializeViews();
        
        // Retrieve values from intent extras
        retrieveIntentExtras();
        
        // Populate UI fields
        populateFields();
        
        // Setup button listeners
        setupButtonListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    
    private void initializeViews() {
        tvBarcodeValue = findViewById(R.id.tvBarcodeValue);
        tvSymbologyValue = findViewById(R.id.tvSymbologyValue);
        etQuantity = findViewById(R.id.etQuantity);
        btQuantityMinus = findViewById(R.id.btQuantityMinus);
        btQuantityPlus = findViewById(R.id.btQuantityPlus);
        tvDateValue = findViewById(R.id.tvDateValue);
        btSave = findViewById(R.id.btSave);
        btCancel = findViewById(R.id.btCancel);
    }
    
    private void retrieveIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            position = intent.getIntExtra(Constants.EXTRA_POSITION, -1);
            barcodeValue = intent.getStringExtra(Constants.EXTRA_VALUE);
            barcodeSymbology = intent.getIntExtra(Constants.EXTRA_SYMBOLOGY, 0);
            barcodeQuantity = intent.getIntExtra(Constants.EXTRA_QUANTITY, 1);
            long dateMillis = intent.getLongExtra(Constants.EXTRA_DATE, System.currentTimeMillis());
            barcodeDate = new Date(dateMillis);
        }
    }
    
    private void populateFields() {
        // Set barcode value (read-only)
        if (barcodeValue != null) {
            tvBarcodeValue.setText(barcodeValue);
        }
        
        // Set symbology (read-only)
        EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcodeSymbology);
        tvSymbologyValue.setText(symbology.getName());
        
        // Set quantity (editable)
        etQuantity.setText(String.valueOf(barcodeQuantity));
        
        // Set date (read-only)
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        tvDateValue.setText(dateFormat.format(barcodeDate));
    }
    
    private void setupButtonListeners() {
        btQuantityMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });
        
        btQuantityPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });
        
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
        
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void decreaseQuantity() {
        int currentQuantity = getCurrentQuantityFromField();
        if (currentQuantity > 1) {
            currentQuantity--;
            etQuantity.setText(String.valueOf(currentQuantity));
        }
    }
    
    private void increaseQuantity() {
        int currentQuantity = getCurrentQuantityFromField();
        currentQuantity++;
        etQuantity.setText(String.valueOf(currentQuantity));
    }
    
    private int getCurrentQuantityFromField() {
        String quantityText = etQuantity.getText().toString().trim();
        try {
            int quantity = Integer.parseInt(quantityText);
            return Math.max(quantity, 1); // Ensure minimum of 1
        } catch (NumberFormatException e) {
            return 1; // Default to 1 if parsing fails
        }
    }
    
    private void saveChanges() {
        // Only update quantity from UI (other fields are read-only)
        String quantityText = etQuantity.getText().toString().trim();
        
        // Validate quantity
        try {
            barcodeQuantity = Integer.parseInt(quantityText);
            if (barcodeQuantity < 1) {
                barcodeQuantity = 1;
            }
        } catch (NumberFormatException e) {
            barcodeQuantity = 1;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.EXTRA_POSITION, position);
        resultIntent.putExtra(Constants.EXTRA_VALUE, barcodeValue);
        resultIntent.putExtra(Constants.EXTRA_SYMBOLOGY, barcodeSymbology);
        resultIntent.putExtra(Constants.EXTRA_QUANTITY, barcodeQuantity);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}