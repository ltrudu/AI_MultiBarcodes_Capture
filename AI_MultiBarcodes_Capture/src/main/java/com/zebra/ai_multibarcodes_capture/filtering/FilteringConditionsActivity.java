package com.zebra.ai_multibarcodes_capture.filtering;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.conditions.BaseConditionsActivity;
import com.zebra.ai_multibarcodes_capture.filtering.models.EFilteringConditionType;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringCondition;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringConditionList;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Activity for managing filtering conditions.
 * Filtering uses OR logic - entities matching at least ONE condition will be included.
 */
public class FilteringConditionsActivity extends BaseConditionsActivity<FilteringCondition, FilteringConditionList> {

    private FilteringConditionsAdapter adapter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_filtering_conditions;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.filtering_conditions_menu;
    }

    @Override
    protected String getExportFilename() {
        return getString(R.string.filtering_conditions_filename);
    }

    @Override
    protected int getNoConditionsMessageResId() {
        return R.string.filtering_no_conditions_configured;
    }

    @Override
    protected void setupAdapter() {
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
        conditionList = FilteringPreferencesHelper.loadConditions(this);
    }

    @Override
    protected void doSaveConditions() {
        FilteringPreferencesHelper.saveConditions(this, conditionList);
    }

    @Override
    protected FilteringConditionList createEmptyConditionList() {
        return new FilteringConditionList();
    }

    @Override
    protected FilteringConditionList parseConditionListFromJson(String json) {
        return new Gson().fromJson(json, FilteringConditionList.class);
    }

    @Override
    protected void showAddConditionMenu() {
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

    @Override
    protected void showEditConditionDialog(FilteringCondition condition) {
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
                .setPositiveButton(positiveButton, null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        currentRegexDialog = dialog;

        btPickPredefined.setOnClickListener(v -> launchRegexPicker());

        dialog.setOnShowListener(d -> {
            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(v -> {
                String regex = etRegexPattern.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

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
}
