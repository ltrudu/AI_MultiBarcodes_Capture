package com.zebra.ai_multibarcodes_capture.java;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.filemanagement.SessionsFilesHelpers;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;
import com.zebra.ai_multibarcodes_capture.helpers.SessionData;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CapturedBarcodesActivity extends AppCompatActivity {

    private static class BarcodeData
    {
        String value;
        int hashcode;
        int symbology;
    }

    private static class DisplayBarcodeData {
        String value;
        int symbology;
        int quantity;
        Date date;
        boolean loaded = false;

        public DisplayBarcodeData(Boolean loaded, String value, int symbology, int quantity, Date date) {
            this.loaded = loaded;
            this.value = value;
            this.symbology = symbology;
            this.quantity = quantity;
            this.date = date;
        }
    }

    private static final String TAG = "CapturedBarcodes";

    private ListView barcodesListView;
    List<DisplayBarcodeData> displayList;
    BarcodeAdapter barcodeAdapter;

    private Button closeButton;
    private Button saveButton;
    private Button mergeButton;

    private SessionData sessionData;

    private String captureFilePath;

    SessionData SessionData;

    private Boolean hasDataBeenMerged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Date currentDate = new Date();
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_captured_barcodes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        captureFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);

        File captureFile = new File(captureFilePath);
        if(captureFile.length() > 0)
        {
            SessionData = SessionsFilesHelpers.loadData(this, captureFilePath);
        }

        // Initialize the list we'll use to display barcodes
       displayList = new ArrayList<>();
        Integer barcodeUniqueIndex = -1;
        if(SessionData != null && SessionData.barcodeValuesMap != null && SessionData.barcodeValuesMap.size() > 0)
        {
            for (Map.Entry<Integer, String> entry : SessionData.barcodeValuesMap.entrySet()) {
                Integer barcodeUniqueID = entry.getKey();
                if(barcodeUniqueID > barcodeUniqueIndex)
                    barcodeUniqueIndex = barcodeUniqueID;
                String value = entry.getValue();
                if(value.isEmpty())
                    continue;
                int quantity = SessionData.barcodeQuantityMap.getOrDefault(barcodeUniqueID, 1);
                int symbology = SessionData.barcodeSymbologyMap.getOrDefault(barcodeUniqueID, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)
                Date date = SessionData.barcodeDateMap.getOrDefault(barcodeUniqueID, currentDate);
                displayList.add(new DisplayBarcodeData(true, value, symbology, quantity, date));
            }
            // Increment barcodeUnique index to prevent merging
            barcodeUniqueIndex++;
        }


        // Retrieve data from bundle passed by CaptureData method of CameraXLivePreviewActivity and add them to the barcodesDataList
        List<BarcodeData> barcodesDataList = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ArrayList<Bundle> barcodeBundles = bundle.getParcelableArrayList("barcodeDataList");
            if (barcodeBundles != null) {
                for (Bundle barcodeBundle : barcodeBundles) {
                    String value = barcodeBundle.getString("value");
                    if(value.isEmpty())
                        continue;
                    BarcodeData barcodeData = new BarcodeData();
                    barcodeData.value = value;
                    barcodeData.symbology = barcodeBundle.getInt("symbology");
                    barcodeData.hashcode = barcodeBundle.getInt("hashcode");
                    barcodesDataList.add(barcodeData);
                }
            }
        }

        // Process barcodesDataList to count quantities
        if(sessionData == null)
            sessionData = new SessionData();

        HashMap<Integer, String> barcodeValuesMap;
        HashMap<Integer, Integer> barcodeQuantityMap;
        HashMap<Integer, Integer> barcodeSymbologyMap;
        HashMap<Integer, Date> barcodesDateMap;

        barcodeValuesMap = new HashMap<>();
        barcodeQuantityMap = new HashMap<>();
        barcodeSymbologyMap = new HashMap<>(); // To store symbology for each unique barcode
        barcodesDateMap = new HashMap<>();

        for (BarcodeData data : barcodesDataList) {
            Integer existingData = getKeyFromValue(barcodeValuesMap, data.value);
            if(existingData != null)
            {
                // We suppose that the same value has the same symbology
                // We just update the quantity
                barcodeQuantityMap.replace(existingData, barcodeQuantityMap.getOrDefault(existingData, 0) + 1);
            }
            else {
                // We have a new barcode
                barcodeValuesMap.put(barcodeUniqueIndex, data.value);
                barcodeQuantityMap.put(barcodeUniqueIndex, 1);
                barcodeSymbologyMap.put(barcodeUniqueIndex, data.symbology); // Assuming symbology is consistent for a given value
                barcodesDateMap.put(barcodeUniqueIndex, currentDate);
                barcodeUniqueIndex++;
            }
        }

        for (Map.Entry<Integer, String> entry : barcodeValuesMap.entrySet()) {
            barcodeUniqueIndex = entry.getKey();
            String value = entry.getValue();
            int quantity = barcodeQuantityMap.getOrDefault(barcodeUniqueIndex, 1);
            int symbology = barcodeSymbologyMap.getOrDefault(barcodeUniqueIndex, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)
            displayList.add(new DisplayBarcodeData(false, value, symbology, quantity, currentDate));
        }

        barcodesListView = findViewById(R.id.barcodes_list_view);
        barcodeAdapter = new BarcodeAdapter(this, displayList);
        barcodesListView.setAdapter(barcodeAdapter);
        barcodesListView.setSelection(barcodeAdapter.getCount() - 1);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveButton = findViewById(R.id.saveData);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        mergeButton = findViewById(R.id.mergeData);
        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergeData();
            }
        });
    }

    public static Integer getKeyFromValue(HashMap<Integer, String> map, String value) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Return null if the value is not found
    }

    private static class BarcodeAdapter extends ArrayAdapter<DisplayBarcodeData> {

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Context context;

        public BarcodeAdapter(@NonNull Context context, List<DisplayBarcodeData> barcodes) {
            super(context, 0, barcodes);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barcode, parent, false);
            }

            DisplayBarcodeData barcode = getItem(position);

            LinearLayout llRowLayout = convertView.findViewById(R.id.llRowLayout);
            TextView valueTextView = convertView.findViewById(R.id.barcode_value);
            TextView symbologyTextView = convertView.findViewById(R.id.barcode_symbology);
            TextView quantityTextView = convertView.findViewById(R.id.barcode_quantity);
            TextView dateTextView = convertView.findViewById(R.id.barcode_date);

            if (barcode != null) {
                String barcodeDateString = dateFormat.format(barcode.date) + " " + sdf.format(barcode.date);

                valueTextView.setText("Value: " + barcode.value);
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcode.symbology);
                symbologyTextView.setText("Symbology: " + symbology.getName());
                quantityTextView.setText("Quantity: " + barcode.quantity);
                dateTextView.setText("Date: " + barcodeDateString);

                if(barcode.loaded == false)
                {
                    llRowLayout.setBackgroundColor(context.getColor(R.color.pastelbluelight));
                }
                else
                {
                    llRowLayout.setBackgroundColor(Color.WHITE);
                }
            }

            return convertView;
        }
    }

    private SessionData sessionDataFromDisplayList(List<DisplayBarcodeData> dataList)
    {
        SessionData newBarcodes = new SessionData();
        Integer uniqueId = 0;
        for(DisplayBarcodeData entry : dataList)
        {
            if(entry.loaded == false)
            {
                newBarcodes.barcodeValuesMap.put(uniqueId, entry.value);
                newBarcodes.barcodeDateMap.put(uniqueId, entry.date);
                newBarcodes.barcodeQuantityMap.put(uniqueId, entry.quantity);
                newBarcodes.barcodeSymbologyMap.put(uniqueId, entry.symbology);
                uniqueId++;
            }
        }
        return newBarcodes;
    }

    private void mergeData()
    {
        HashMap<Integer, String> barcodeValuesMap;
        HashMap<Integer, Integer> barcodeQuantityMap;
        HashMap<Integer, Integer> barcodeSymbologyMap;
        HashMap<Integer, Date> barcodesDateMap;

        barcodeValuesMap = new HashMap<>();
        barcodeQuantityMap = new HashMap<>();
        barcodeSymbologyMap = new HashMap<>(); // To store symbology for each unique barcode
        barcodesDateMap = new HashMap<>();

        Integer barcodeUniqueIndex = 0;
        for (DisplayBarcodeData displayBarcodeData : displayList) {
            Integer existingData = getKeyFromValue(barcodeValuesMap, displayBarcodeData.value);
            if(existingData != null)
            {
                // We suppose that the same value has the same symbology
                // We just update the quantity
                Integer currentQuantity = barcodeQuantityMap.getOrDefault(existingData, 0);
                barcodeQuantityMap.replace(existingData, currentQuantity + displayBarcodeData.quantity);
            }
            else {
                // We have a new barcode
                barcodeValuesMap.put(barcodeUniqueIndex, displayBarcodeData.value);
                barcodeQuantityMap.put(barcodeUniqueIndex, displayBarcodeData.quantity);
                barcodeSymbologyMap.put(barcodeUniqueIndex, displayBarcodeData.symbology); // Assuming symbology is consistent for a given value
                barcodesDateMap.put(barcodeUniqueIndex, displayBarcodeData.date);
                barcodeUniqueIndex++;
            }
        }

        Date currentDate = new Date();

        List<DisplayBarcodeData> targetList = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : barcodeValuesMap.entrySet()) {
            barcodeUniqueIndex = entry.getKey();
            String value = entry.getValue();
            int quantity = barcodeQuantityMap.getOrDefault(barcodeUniqueIndex, 1);
            int symbology = barcodeSymbologyMap.getOrDefault(barcodeUniqueIndex, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)
            targetList.add(new DisplayBarcodeData(false, value, symbology, quantity, currentDate));
        }

        displayList.clear();
        displayList.addAll(targetList);
        barcodeAdapter.notifyDataSetChanged();

        hasDataBeenMerged = true;
    }

    private void saveData()
    {
        SessionData newBarcodes = sessionDataFromDisplayList(displayList);
        if(hasDataBeenMerged)
        {
            // We need to erase old session file and create a new one because the data have been merged
            File sessionFile = new File(captureFilePath);
            if(sessionFile.exists())
                sessionFile.delete();
        }
        if(SessionsFilesHelpers.saveData(this, captureFilePath, newBarcodes))
        {
            finish();
        }
        else
        {
            // Do nothing as the user was informed with a toast of the problem.
        }
    }
}