package com.zebra.ai_multibarcodes_capture.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.zebra.ai_multibarcodes_capture.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;


public class SettingsActivity extends AppCompatActivity {

    private EditText etPrefix;
    private RadioButton rbCSV, rbTXT, rbXSLX;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.tbSetup);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Settings");
        
        etPrefix = findViewById(R.id.etPrefix);
        rbCSV = findViewById(R.id.rbCSV);
        rbTXT = findViewById(R.id.rbTxt);
        rbXSLX = findViewById(R.id.rbXSLX);

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
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Retrieve the stored integer value, with a default value of 0 if not found
        String prefix = sharedPreferences.getString(SHARED_PREFERENCES_PREFIX, FILE_DEFAULT_PREFIX);
        String extension = sharedPreferences.getString(SHARED_PREFERENCES_EXTENSION, FILE_DEFAULT_EXTENSION);

        etPrefix.setText(prefix);
        selectExtensionRadioButton(extension);
        super.onResume();
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

        editor.putString(SHARED_PREFERENCES_EXTENSION, getSelectedExtension());

        // Commit the changes
        editor.commit();

    }

    private String getSelectedExtension()
    {
        if(rbCSV.isChecked())
            return FILE_EXTENSION_CSV;
        else if(rbXSLX.isChecked())
            return FILE_EXTENSION_XLSX;
        return FILE_EXTENSION_TXT;
    }

    private void selectExtensionRadioButton(String extension)
    {
        switch (extension)
        {
            case FILE_EXTENSION_CSV:
                rbXSLX.setChecked(false);
                rbTXT.setChecked(false);
                rbCSV.setChecked(true);
                break;
            case FILE_EXTENSION_TXT:
                rbXSLX.setChecked(false);
                rbTXT.setChecked(true);
                rbCSV.setChecked(false);
                break;
            case FILE_EXTENSION_XLSX:
                rbXSLX.setChecked(true);
                rbTXT.setChecked(false);
                rbCSV.setChecked(false);
                break;

        }
    }
}