package com.zebra.ai_multibarcodes_capture.autocapture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.models.PredefinedRegex;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

/**
 * Activity for picking a predefined regex pattern.
 */
public class PredefinedRegexPickerActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvPredefinedRegex;
    private PredefinedRegexAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelpers.applyTheme(this);
        setContentView(R.layout.activity_predefined_regex_picker);
        ThemeHelpers.applyCustomFont(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        rvPredefinedRegex = findViewById(R.id.rvPredefinedRegex);

        adapter = new PredefinedRegexAdapter();
        adapter.setOnRegexSelectedListener(regex -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AutoCaptureConditionsActivity.EXTRA_SELECTED_REGEX, regex.getPattern());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        rvPredefinedRegex.setLayoutManager(new LinearLayoutManager(this));
        rvPredefinedRegex.setAdapter(adapter);

        // Load all patterns initially
        adapter.setRegexList(PredefinedRegexProvider.getPredefinedRegexList());

        // Setup search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                adapter.setRegexList(PredefinedRegexProvider.filterPredefinedRegex(query));
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}
