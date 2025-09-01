package com.zebra.ai_multibarcodes_capture.java;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.filemanagement.ExportWriters;
import com.zebra.ai_multibarcodes_capture.filemanagement.FileUtil;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        public DisplayBarcodeData(String value, int symbology, int quantity) {
            this.value = value;
            this.symbology = symbology;
            this.quantity = quantity;
        }
    }

    private static final String TAG = "CapturedBarcodes";

    private List<BarcodeData> barcodesDataList;
    private ListView barcodesListView;

    private Button closeButton;
    private Button saveButton;


    private Map<String, Integer> barcodeQuantityMap;
    private Map<String, Integer> barcodeSymbologyMap;

    private String captureFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_captured_barcodes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        captureFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);

        // Retrieve data from bundle passed by CaptureData method of CameraXLivePreviewActivity and add them to the barcodesDataList
        barcodesDataList = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            ArrayList<Bundle> barcodeBundles = bundle.getParcelableArrayList("barcodeDataList");
            if (barcodeBundles != null) {
                for (Bundle barcodeBundle : barcodeBundles) {
                    BarcodeData barcodeData = new BarcodeData();
                    barcodeData.value = barcodeBundle.getString("value");
                    barcodeData.symbology = barcodeBundle.getInt("symbology");
                    barcodeData.hashcode = barcodeBundle.getInt("hashcode");
                    barcodesDataList.add(barcodeData);
                }
            }
        }

        // Process barcodesDataList to count quantities
        barcodeQuantityMap = new HashMap<>();
        barcodeSymbologyMap = new HashMap<>(); // To store symbology for each unique barcode

        for (BarcodeData data : barcodesDataList) {
            barcodeQuantityMap.put(data.value, barcodeQuantityMap.getOrDefault(data.value, 0) + 1);
            barcodeSymbologyMap.put(data.value, data.symbology); // Assuming symbology is consistent for a given value
        }

        List<DisplayBarcodeData> displayList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : barcodeQuantityMap.entrySet()) {
            String value = entry.getKey();
            int quantity = entry.getValue();
            int symbology = barcodeSymbologyMap.getOrDefault(value, 0); // Get symbology, default to 0 if not found (shouldn't happen)
            displayList.add(new DisplayBarcodeData(value, symbology, quantity));
        }

        barcodesListView = findViewById(R.id.barcodes_list_view);
        BarcodeAdapter adapter = new BarcodeAdapter(this, displayList);
        barcodesListView.setAdapter(adapter);

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
    }

    private static class BarcodeAdapter extends ArrayAdapter<DisplayBarcodeData> {

        public BarcodeAdapter(@NonNull Context context, List<DisplayBarcodeData> barcodes) {
            super(context, 0, barcodes);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barcode, parent, false);
            }

            DisplayBarcodeData barcode = getItem(position);

            TextView valueTextView = convertView.findViewById(R.id.barcode_value);
            TextView symbologyTextView = convertView.findViewById(R.id.barcode_symbology);
            TextView quantityTextView = convertView.findViewById(R.id.barcode_quantity);

            if (barcode != null) {
                valueTextView.setText("Value: " + barcode.value);
                symbologyTextView.setText("Symbology: " + barcode.symbology);
                quantityTextView.setText("Quantity: " + barcode.quantity);
            }

            return convertView;
        }
    }

    private void saveData()
    {
        Date currentDate = new Date();
        if(ExportWriters.saveData(this, captureFilePath, barcodeQuantityMap, barcodeSymbologyMap, currentDate))
        {
            finish();
        }
        else
        {
            // Do nothing as the user was informed with a toast of the problem.
        }
    }
}