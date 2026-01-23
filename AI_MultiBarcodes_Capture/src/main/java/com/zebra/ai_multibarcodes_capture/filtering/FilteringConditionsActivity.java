package com.zebra.ai_multibarcodes_capture.filtering;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.PredefinedRegexPickerActivity;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringCondition;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;
import com.zebra.ai_multibarcodes_capture.filtering.models.EFilteringConditionType;
import com.zebra.ai_multibarcodes_capture.helpers.BaseActivity;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Activity for managing filtering conditions.
 * Filtering uses OR logic - entities matching at least ONE condition will be included.
 */
public class FilteringConditionsActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_REGEX = "selected_regex";

    private RecyclerView rvConditions;
    private View tvEmptyState;
    private FloatingActionButton fabAddCondition;
    private FilteringConditionsAdapter adapter;
    private FilteringConditionList conditionList;

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

    // Launcher for exporting JSON to Documents folder
    private ActivityResultLauncher<Intent> exportFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        exportConditionsToUri(uri);
                    }
                }
            }
    );

    // Launcher for importing JSON from Documents folder
    private ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importConditionsFromUri(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelpers.applyTheme(this);
        setContentView(R.layout.activity_filtering_conditions);
        ThemeHelpers.applyCustomFont(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvConditions = findViewById(R.id.rvConditions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddCondition = findViewById(R.id.fabAddCondition);

        adapter = new FilteringConditionsAdapter();
        adapter.setOnConditionClickListener(new FilteringConditionsAdapter.OnConditionClickListener() {
            @Override
            public void onEditClick(FilteringCondition condition) {
                showEditConditionDialog(condition);
            }

            @Override
            public void onDeleteClick(FilteringCondition condition) {
                showDeleteConfirmationDialog(condition);
            }
        });

        rvConditions.setLayoutManager(new LinearLayoutManager(this));
        rvConditions.setAdapter(adapter);

        fabAddCondition.setOnClickListener(v -> showAddConditionMenu());

        loadConditions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtering_conditions_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_import_json) {
            showImportFilePicker();
            return true;
        } else if (id == R.id.action_export_json) {
            showExportFilePicker();
            return true;
        } else if (id == R.id.action_delete_all) {
            showDeleteAllConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExportFilePicker() {
        if (conditionList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_conditions_to_export), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.filtering_conditions_filename));
        exportFileLauncher.launch(intent);
    }

    private void showImportFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importFileLauncher.launch(intent);
    }

    private void exportConditionsToUri(Uri uri) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(conditionList);

            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(json.getBytes());
                outputStream.close();
                Toast.makeText(this, getString(R.string.export_successful), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.export_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void importConditionsFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                String json = stringBuilder.toString();
                Gson gson = new Gson();
                FilteringConditionList importedList = gson.fromJson(json, FilteringConditionList.class);

                if (importedList != null && importedList.getConditions() != null) {
                    conditionList = importedList;
                    saveConditions();
                    Toast.makeText(this, getString(R.string.import_successful), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.import_failed, "Invalid JSON format"), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.import_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteAllConfirmationDialog() {
        if (conditionList.isEmpty()) {
            Toast.makeText(this, getString(R.string.filtering_no_conditions_configured), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_all_conditions))
                .setMessage(getString(R.string.delete_all_conditions_warning))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    conditionList.clear();
                    saveConditions();
                    Toast.makeText(this, getString(R.string.all_conditions_deleted), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void loadConditions() {
        conditionList = FilteringPreferencesHelper.loadConditions(this);
        updateUI();
    }

    private void saveConditions() {
        FilteringPreferencesHelper.saveConditions(this, conditionList);
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
        String[] options = new String[]{
                getString(R.string.filtering_condition_type_regex),
                getString(R.string.filtering_condition_type_symbology),
                getString(R.string.filtering_condition_type_complex)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_condition))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showRegexConditionDialog(null);
                            break;
                        case 1:
                            showSymbologyConditionDialog(null);
                            break;
                        case 2:
                            showComplexConditionDialog(null);
                            break;
                    }
                })
                .show();
    }

    private void showEditConditionDialog(FilteringCondition condition) {
        if (condition.getType() == EFilteringConditionType.CONTAINS_REGEX) {
            showRegexConditionDialog(condition);
        } else if (condition.getType() == EFilteringConditionType.SYMBOLOGY) {
            showSymbologyConditionDialog(condition);
        } else if (condition.getType() == EFilteringConditionType.COMPLEX) {
            showComplexConditionDialog(condition);
        }
    }

    private void showRegexConditionDialog(FilteringCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filtering_regex, null);
        EditText etRegexPattern = dialogView.findViewById(R.id.etRegexPattern);
        Button btPickPredefined = dialogView.findViewById(R.id.btPickPredefined);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        currentRegexEditText = etRegexPattern;

        if (existingCondition != null) {
            etRegexPattern.setText(existingCondition.getRegex());
            if (existingCondition.getDescription() != null) {
                etDescription.setText(existingCondition.getDescription());
            }
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
                String regex = etRegexPattern.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

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
                    existingCondition.setRegex(regex);
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    FilteringCondition newCondition = new FilteringCondition(regex);
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showSymbologyConditionDialog(FilteringCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filtering_symbology, null);
        Spinner spinnerSymbology = dialogView.findViewById(R.id.spinnerSymbology);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Build list of symbologies (excluding UNKNOWN)
        List<EBarcodesSymbologies> symbologyList = new ArrayList<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            if (symbology != EBarcodesSymbologies.UNKNOWN) {
                symbologyList.add(symbology);
            }
        }

        // Create adapter for spinner
        ArrayAdapter<EBarcodesSymbologies> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                symbologyList
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymbology.setAdapter(spinnerAdapter);

        // Pre-fill values if editing existing condition
        if (existingCondition != null) {
            // Find and select the existing symbology
            EBarcodesSymbologies existingSymbology = EBarcodesSymbologies.fromInt(existingCondition.getSymbology());
            int position = symbologyList.indexOf(existingSymbology);
            if (position >= 0) {
                spinnerSymbology.setSelection(position);
            }
            if (existingCondition.getDescription() != null) {
                etDescription.setText(existingCondition.getDescription());
            }
        }

        String title = existingCondition != null ? getString(R.string.edit_condition) : getString(R.string.add_condition);
        String positiveButton = existingCondition != null ? getString(R.string.ok) : getString(R.string.add);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String description = etDescription.getText().toString().trim();

                EBarcodesSymbologies selectedSymbology = (EBarcodesSymbologies) spinnerSymbology.getSelectedItem();
                if (selectedSymbology == null) {
                    Toast.makeText(this, getString(R.string.select_symbology), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (existingCondition != null) {
                    existingCondition.setSymbology(selectedSymbology.getIntValue());
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    FilteringCondition newCondition = new FilteringCondition(selectedSymbology.getIntValue());
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showComplexConditionDialog(FilteringCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filtering_complex, null);
        Spinner spinnerSymbology = dialogView.findViewById(R.id.spinnerSymbology);
        EditText etRegexPattern = dialogView.findViewById(R.id.etRegexPattern);
        Button btPickPredefined = dialogView.findViewById(R.id.btPickPredefined);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        currentRegexEditText = etRegexPattern;

        // Build list of symbologies (excluding UNKNOWN)
        List<EBarcodesSymbologies> symbologyList = new ArrayList<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            if (symbology != EBarcodesSymbologies.UNKNOWN) {
                symbologyList.add(symbology);
            }
        }

        // Create adapter for spinner
        ArrayAdapter<EBarcodesSymbologies> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                symbologyList
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymbology.setAdapter(spinnerAdapter);

        // Pre-fill values if editing existing condition
        if (existingCondition != null) {
            etRegexPattern.setText(existingCondition.getRegex());
            // Find and select the existing symbology
            EBarcodesSymbologies existingSymbology = EBarcodesSymbologies.fromInt(existingCondition.getSymbology());
            int position = symbologyList.indexOf(existingSymbology);
            if (position >= 0) {
                spinnerSymbology.setSelection(position);
            }
            if (existingCondition.getDescription() != null) {
                etDescription.setText(existingCondition.getDescription());
            }
        }

        String title = existingCondition != null ? getString(R.string.edit_condition) : getString(R.string.add_condition);
        String positiveButton = existingCondition != null ? getString(R.string.ok) : getString(R.string.add);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, null)
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
                String regex = etRegexPattern.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                EBarcodesSymbologies selectedSymbology = (EBarcodesSymbologies) spinnerSymbology.getSelectedItem();
                if (selectedSymbology == null) {
                    Toast.makeText(this, getString(R.string.select_symbology), Toast.LENGTH_SHORT).show();
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
                    existingCondition.setSymbology(selectedSymbology.getIntValue());
                    existingCondition.setRegex(regex);
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    FilteringCondition newCondition = new FilteringCondition(selectedSymbology.getIntValue(), regex);
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(FilteringCondition condition) {
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
