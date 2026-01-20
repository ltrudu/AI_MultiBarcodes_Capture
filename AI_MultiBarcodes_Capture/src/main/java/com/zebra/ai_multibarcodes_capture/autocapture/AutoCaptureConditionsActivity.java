package com.zebra.ai_multibarcodes_capture.autocapture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureCondition;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.autocapture.models.EAutoCaptureConditionType;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Activity for managing auto capture conditions.
 */
public class AutoCaptureConditionsActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_REGEX = "selected_regex";

    private RecyclerView rvConditions;
    private View tvEmptyState;
    private FloatingActionButton fabAddCondition;
    private AutoCaptureConditionsAdapter adapter;
    private AutoCaptureConditionList conditionList;

    // For regex dialog - store the EditText to update when picker returns
    private EditText currentRegexEditText;
    private AlertDialog currentRegexDialog;

    private ActivityResultLauncher<Intent> regexPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedRegex = result.getData().getStringExtra(EXTRA_SELECTED_REGEX);
                    if (selectedRegex != null && currentRegexEditText != null) {
                        currentRegexEditText.setText(selectedRegex);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelpers.applyTheme(this);
        setContentView(R.layout.activity_auto_capture_conditions);
        ThemeHelpers.applyCustomFont(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvConditions = findViewById(R.id.rvConditions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddCondition = findViewById(R.id.fabAddCondition);

        adapter = new AutoCaptureConditionsAdapter();
        adapter.setOnConditionClickListener(new AutoCaptureConditionsAdapter.OnConditionClickListener() {
            @Override
            public void onEditClick(AutoCaptureCondition condition) {
                showEditConditionDialog(condition);
            }

            @Override
            public void onDeleteClick(AutoCaptureCondition condition) {
                showDeleteConfirmationDialog(condition);
            }
        });

        rvConditions.setLayoutManager(new LinearLayoutManager(this));
        rvConditions.setAdapter(adapter);

        fabAddCondition.setOnClickListener(v -> showAddConditionMenu());

        loadConditions();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }

    private void loadConditions() {
        conditionList = AutoCapturePreferencesHelper.loadConditions(this);
        updateUI();
    }

    private void saveConditions() {
        AutoCapturePreferencesHelper.saveConditions(this, conditionList);
        updateUI();
    }

    private void updateUI() {
        if (conditionList.isEmpty()) {
            rvConditions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvConditions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
        adapter.setConditions(conditionList.getConditions());
    }

    private void showAddConditionMenu() {
        String[] options;
        boolean hasNumberCondition = conditionList.hasNumberOfBarcodesCondition();

        if (hasNumberCondition) {
            options = new String[]{
                    getString(R.string.condition_type_number_of_barcodes) + " " + getString(R.string.condition_already_exists),
                    getString(R.string.condition_type_contains_regex)
            };
        } else {
            options = new String[]{
                    getString(R.string.condition_type_number_of_barcodes),
                    getString(R.string.condition_type_contains_regex)
            };
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_condition))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (hasNumberCondition) {
                            Toast.makeText(this, getString(R.string.condition_already_exists), Toast.LENGTH_SHORT).show();
                        } else {
                            showNumberOfBarcodesDialog(null);
                        }
                    } else {
                        showRegexConditionDialog(null);
                    }
                })
                .show();
    }

    private void showEditConditionDialog(AutoCaptureCondition condition) {
        if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
            showNumberOfBarcodesDialog(condition);
        } else {
            showRegexConditionDialog(condition);
        }
    }

    private void showNumberOfBarcodesDialog(AutoCaptureCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_number_input, null);
        EditText etNumber = dialogView.findViewById(R.id.etNumber);

        if (existingCondition != null) {
            etNumber.setText(String.valueOf(existingCondition.getCount()));
        }

        String title = existingCondition != null ? getString(R.string.edit_condition) : getString(R.string.add_condition);
        String positiveButton = existingCondition != null ? getString(R.string.ok) : getString(R.string.add);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, (dialog, which) -> {
                    String numberStr = etNumber.getText().toString().trim();
                    if (numberStr.isEmpty()) {
                        Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int count;
                    try {
                        count = Integer.parseInt(numberStr);
                        if (count < 1) {
                            Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existingCondition != null) {
                        existingCondition.setCount(count);
                        conditionList.updateCondition(existingCondition);
                    } else {
                        AutoCaptureCondition newCondition = new AutoCaptureCondition(count);
                        conditionList.addCondition(newCondition);
                    }
                    saveConditions();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showRegexConditionDialog(AutoCaptureCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_regex_condition, null);
        EditText etMinimumMatches = dialogView.findViewById(R.id.etMinimumMatches);
        EditText etRegexPattern = dialogView.findViewById(R.id.etRegexPattern);
        Button btPickPredefined = dialogView.findViewById(R.id.btPickPredefined);

        currentRegexEditText = etRegexPattern;

        if (existingCondition != null) {
            etMinimumMatches.setText(String.valueOf(existingCondition.getCount()));
            etRegexPattern.setText(existingCondition.getRegex());
        }

        String title = existingCondition != null ? getString(R.string.edit_condition) : getString(R.string.add_condition);
        String positiveButton = existingCondition != null ? getString(R.string.ok) : getString(R.string.add);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, null) // Set to null, we'll override
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        currentRegexDialog = dialog;

        btPickPredefined.setOnClickListener(v -> {
            Intent intent = new Intent(this, PredefinedRegexPickerActivity.class);
            regexPickerLauncher.launch(intent);
        });

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String matchesStr = etMinimumMatches.getText().toString().trim();
                String regex = etRegexPattern.getText().toString().trim();

                if (matchesStr.isEmpty()) {
                    Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                    return;
                }

                int count;
                try {
                    count = Integer.parseInt(matchesStr);
                    if (count < 1) {
                        Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.invalid_count), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (regex.isEmpty()) {
                    Toast.makeText(this, getString(R.string.invalid_regex), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate regex
                try {
                    Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    Toast.makeText(this, getString(R.string.invalid_regex) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (existingCondition != null) {
                    existingCondition.setCount(count);
                    existingCondition.setRegex(regex);
                    conditionList.updateCondition(existingCondition);
                } else {
                    AutoCaptureCondition newCondition = new AutoCaptureCondition(count, regex);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(AutoCaptureCondition condition) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_condition))
                .setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    conditionList.removeCondition(condition.getId());
                    saveConditions();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }
}
