package com.zebra.ai_multibarcodes_capture.conditions;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.PredefinedRegexPickerActivity;
import com.zebra.ai_multibarcodes_capture.helpers.BaseActivity;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Base activity for managing conditions (AutoCapture or Filtering).
 * Provides common UI patterns and functionality for both condition types.
 *
 * @param <C> The condition type (e.g., AutoCaptureCondition or FilteringCondition)
 * @param <L> The condition list type (e.g., AutoCaptureConditionList or FilteringConditionList)
 */
public abstract class BaseConditionsActivity<C extends ICondition, L extends IConditionList<C>> extends BaseActivity {

    public static final String EXTRA_SELECTED_REGEX = "selected_regex";

    protected RecyclerView rvConditions;
    protected View tvEmptyState;
    protected FloatingActionButton fabAddCondition;
    protected L conditionList;

    // For regex dialog - store the EditText to update when picker returns
    protected EditText currentRegexEditText;
    protected AlertDialog currentRegexDialog;

    protected ActivityResultLauncher<Intent> regexPickerLauncher = registerForActivityResult(
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
    protected ActivityResultLauncher<Intent> exportFileLauncher = registerForActivityResult(
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
    protected ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(
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
        setContentView(getLayoutResourceId());
        ThemeHelpers.applyCustomFont(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvConditions = findViewById(R.id.rvConditions);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddCondition = findViewById(R.id.fabAddCondition);

        setupAdapter();

        rvConditions.setLayoutManager(new LinearLayoutManager(this));
        fabAddCondition.setOnClickListener(v -> showAddConditionMenu());

        loadConditions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuResourceId(), menu);
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

    // ==================== Abstract methods to be implemented by subclasses ====================

    /**
     * @return The layout resource ID for this activity
     */
    protected abstract int getLayoutResourceId();

    /**
     * @return The menu resource ID for this activity
     */
    protected abstract int getMenuResourceId();

    /**
     * @return The default export filename for this condition type
     */
    protected abstract String getExportFilename();

    /**
     * @return The "no conditions configured" message string resource ID
     */
    protected abstract int getNoConditionsMessageResId();

    /**
     * Sets up the RecyclerView adapter for the specific condition type.
     */
    protected abstract void setupAdapter();

    /**
     * Updates the adapter with the current condition list.
     */
    protected abstract void updateAdapter();

    /**
     * Loads conditions from preferences.
     */
    protected abstract void doLoadConditions();

    /**
     * Saves conditions to preferences.
     */
    protected abstract void doSaveConditions();

    /**
     * Creates an empty condition list.
     * @return A new empty condition list
     */
    protected abstract L createEmptyConditionList();

    /**
     * Parses JSON into a condition list.
     * @param json The JSON string to parse
     * @return The parsed condition list, or null if parsing failed
     */
    protected abstract L parseConditionListFromJson(String json);

    /**
     * Shows the menu for adding a new condition.
     */
    protected abstract void showAddConditionMenu();

    /**
     * Shows the edit dialog for a condition.
     * @param condition The condition to edit
     */
    protected abstract void showEditConditionDialog(C condition);

    // ==================== Common implementation ====================

    protected void loadConditions() {
        doLoadConditions();
        updateUI();
    }

    protected void saveConditions() {
        doSaveConditions();
        updateUI();
    }

    protected void updateUI() {
        if (conditionList == null || conditionList.isEmpty()) {
            rvConditions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvConditions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
        updateAdapter();
    }

    protected void showExportFilePicker() {
        if (conditionList == null || conditionList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_conditions_to_export), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, getExportFilename());
        exportFileLauncher.launch(intent);
    }

    protected void showImportFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importFileLauncher.launch(intent);
    }

    protected void exportConditionsToUri(Uri uri) {
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

    protected void importConditionsFromUri(Uri uri) {
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
                L importedList = parseConditionListFromJson(json);

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

    protected void showDeleteAllConfirmationDialog() {
        if (conditionList == null || conditionList.isEmpty()) {
            Toast.makeText(this, getString(getNoConditionsMessageResId()), Toast.LENGTH_SHORT).show();
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

    protected void showDeleteConfirmationDialog(C condition) {
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

    /**
     * Launches the predefined regex picker activity.
     */
    protected void launchRegexPicker() {
        Intent intent = new Intent(this, PredefinedRegexPickerActivity.class);
        regexPickerLauncher.launch(intent);
    }
}
