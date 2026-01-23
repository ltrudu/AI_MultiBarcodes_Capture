package com.zebra.ai_multibarcodes_capture.autocapture;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureCondition;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureConditionList;
import com.zebra.ai_multibarcodes_capture.autocapture.models.EAutoCaptureConditionType;
import com.zebra.ai_multibarcodes_capture.conditions.BaseConditionsActivity;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Activity for managing auto capture conditions.
 */
public class AutoCaptureConditionsActivity extends BaseConditionsActivity<AutoCaptureCondition, AutoCaptureConditionList> {

    private AutoCaptureConditionsAdapter adapter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_auto_capture_conditions;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.auto_capture_conditions_menu;
    }

    @Override
    protected String getExportFilename() {
        return getString(R.string.auto_capture_conditions_filename);
    }

    @Override
    protected int getNoConditionsMessageResId() {
        return R.string.no_conditions_configured;
    }

    @Override
    protected void setupAdapter() {
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
        rvConditions.setAdapter(adapter);
    }

    @Override
    protected void updateAdapter() {
        if (conditionList != null) {
            adapter.setConditions(conditionList.getConditions());
        }
    }

    @Override
    protected void doLoadConditions() {
        conditionList = AutoCapturePreferencesHelper.loadConditions(this);
    }

    @Override
    protected void doSaveConditions() {
        AutoCapturePreferencesHelper.saveConditions(this, conditionList);
    }

    @Override
    protected AutoCaptureConditionList createEmptyConditionList() {
        return new AutoCaptureConditionList();
    }

    @Override
    protected AutoCaptureConditionList parseConditionListFromJson(String json) {
        return new Gson().fromJson(json, AutoCaptureConditionList.class);
    }

    @Override
    protected void showAddConditionMenu() {
        boolean hasNumberCondition = conditionList.hasNumberOfBarcodesCondition();

        List<String> optionsList = new ArrayList<>();
        List<Runnable> actionsList = new ArrayList<>();

        if (!hasNumberCondition) {
            optionsList.add(getString(R.string.condition_type_number_of_barcodes));
            actionsList.add(() -> showNumberOfBarcodesDialog(null));
        }

        optionsList.add(getString(R.string.condition_type_contains_regex));
        actionsList.add(() -> showRegexConditionDialog(null));

        optionsList.add(getString(R.string.condition_type_symbology));
        actionsList.add(() -> showSymbologyConditionDialog(null));

        optionsList.add(getString(R.string.condition_type_complex));
        actionsList.add(() -> showComplexConditionDialog(null));

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_condition))
                .setItems(options, (dialog, which) -> actionsList.get(which).run())
                .show();
    }

    @Override
    protected void showEditConditionDialog(AutoCaptureCondition condition) {
        if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
            showNumberOfBarcodesDialog(condition);
        } else if (condition.getType() == EAutoCaptureConditionType.CONTAINS_REGEX) {
            showRegexConditionDialog(condition);
        } else if (condition.getType() == EAutoCaptureConditionType.SYMBOLOGY) {
            showSymbologyConditionDialog(condition);
        } else if (condition.getType() == EAutoCaptureConditionType.COMPLEX) {
            showComplexConditionDialog(condition);
        }
    }

    private void showNumberOfBarcodesDialog(AutoCaptureCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_number_input, null);
        EditText etNumber = dialogView.findViewById(R.id.etNumber);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        if (existingCondition != null) {
            etNumber.setText(String.valueOf(existingCondition.getCount()));
            if (existingCondition.getDescription() != null) {
                etDescription.setText(existingCondition.getDescription());
            }
        }

        String title = existingCondition != null ? getString(R.string.edit_condition) : getString(R.string.add_condition);
        String positiveButton = existingCondition != null ? getString(R.string.ok) : getString(R.string.add);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(positiveButton, (dialog, which) -> {
                    String numberStr = etNumber.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
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
                        existingCondition.setDescription(description.isEmpty() ? null : description);
                        conditionList.updateCondition(existingCondition);
                    } else {
                        AutoCaptureCondition newCondition = new AutoCaptureCondition(count);
                        newCondition.setDescription(description.isEmpty() ? null : description);
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
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        currentRegexEditText = etRegexPattern;

        if (existingCondition != null) {
            etMinimumMatches.setText(String.valueOf(existingCondition.getCount()));
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
                .setPositiveButton(positiveButton, null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        currentRegexDialog = dialog;

        btPickPredefined.setOnClickListener(v -> launchRegexPicker());

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String matchesStr = etMinimumMatches.getText().toString().trim();
                String regex = etRegexPattern.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

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

                try {
                    Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    Toast.makeText(this, getString(R.string.invalid_regex) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (existingCondition != null) {
                    existingCondition.setCount(count);
                    existingCondition.setRegex(regex);
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    AutoCaptureCondition newCondition = new AutoCaptureCondition(count, regex);
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showSymbologyConditionDialog(AutoCaptureCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_symbology_condition, null);
        EditText etMinimumMatches = dialogView.findViewById(R.id.etMinimumMatches);
        Spinner spinnerSymbology = dialogView.findViewById(R.id.spinnerSymbology);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        List<EBarcodesSymbologies> symbologyList = new ArrayList<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            if (symbology != EBarcodesSymbologies.UNKNOWN) {
                symbologyList.add(symbology);
            }
        }

        ArrayAdapter<EBarcodesSymbologies> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, symbologyList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymbology.setAdapter(spinnerAdapter);

        if (existingCondition != null) {
            etMinimumMatches.setText(String.valueOf(existingCondition.getCount()));
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
                String matchesStr = etMinimumMatches.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

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

                EBarcodesSymbologies selectedSymbology = (EBarcodesSymbologies) spinnerSymbology.getSelectedItem();
                if (selectedSymbology == null) {
                    Toast.makeText(this, getString(R.string.select_symbology), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (existingCondition != null) {
                    existingCondition.setCount(count);
                    existingCondition.setSymbology(selectedSymbology.getIntValue());
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    AutoCaptureCondition newCondition = new AutoCaptureCondition(count, selectedSymbology.getIntValue());
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void showComplexConditionDialog(AutoCaptureCondition existingCondition) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_complex_condition, null);
        EditText etMinimumMatches = dialogView.findViewById(R.id.etMinimumMatches);
        Spinner spinnerSymbology = dialogView.findViewById(R.id.spinnerSymbology);
        EditText etRegexPattern = dialogView.findViewById(R.id.etRegexPattern);
        Button btPickPredefined = dialogView.findViewById(R.id.btPickPredefined);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        currentRegexEditText = etRegexPattern;

        List<EBarcodesSymbologies> symbologyList = new ArrayList<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            if (symbology != EBarcodesSymbologies.UNKNOWN) {
                symbologyList.add(symbology);
            }
        }

        ArrayAdapter<EBarcodesSymbologies> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, symbologyList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSymbology.setAdapter(spinnerAdapter);

        if (existingCondition != null) {
            etMinimumMatches.setText(String.valueOf(existingCondition.getCount()));
            etRegexPattern.setText(existingCondition.getRegex());
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

        btPickPredefined.setOnClickListener(v -> launchRegexPicker());

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String matchesStr = etMinimumMatches.getText().toString().trim();
                String regex = etRegexPattern.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

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

                EBarcodesSymbologies selectedSymbology = (EBarcodesSymbologies) spinnerSymbology.getSelectedItem();
                if (selectedSymbology == null) {
                    Toast.makeText(this, getString(R.string.select_symbology), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (regex.isEmpty()) {
                    Toast.makeText(this, getString(R.string.invalid_regex), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Pattern.compile(regex);
                } catch (PatternSyntaxException e) {
                    Toast.makeText(this, getString(R.string.invalid_regex) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (existingCondition != null) {
                    existingCondition.setCount(count);
                    existingCondition.setSymbology(selectedSymbology.getIntValue());
                    existingCondition.setRegex(regex);
                    existingCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.updateCondition(existingCondition);
                } else {
                    AutoCaptureCondition newCondition = new AutoCaptureCondition(count, selectedSymbology.getIntValue(), regex);
                    newCondition.setDescription(description.isEmpty() ? null : description);
                    conditionList.addCondition(newCondition);
                }
                saveConditions();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
