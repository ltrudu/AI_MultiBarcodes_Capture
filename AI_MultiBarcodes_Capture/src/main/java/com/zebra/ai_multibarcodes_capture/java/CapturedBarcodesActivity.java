package com.zebra.ai_multibarcodes_capture.java;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.dataeditor.BarcodeDataEditorActivity;
import com.zebra.ai_multibarcodes_capture.filemanagement.SessionsFilesHelpers;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;
import com.zebra.ai_multibarcodes_capture.helpers.KeystoreHelper;
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.SessionData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Button uploadButton;

    private SessionData sessionData;

    private String captureFilePath;
    private String endpointUri;
    private boolean isHttpsPostMode = false;

    SessionData SessionData;

    private Boolean hasDataBeenMerged = false;

    // ActivityResultLauncher for BarcodeDataEditorActivity
    private ActivityResultLauncher<Intent> barcodeEditorLauncher;

    // ExecutorService for background HTTP requests
    private ExecutorService executorService;

    private static final int TRANSLATION_X = -360; // Updated for two icons (60dp each)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Date currentDate = new Date();
        
        setContentView(R.layout.activity_captured_barcodes);
        
        // Configure system bars
        configureSystemBars();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        closeButton = findViewById(R.id.closeButton);
        saveButton = findViewById(R.id.saveData);
        mergeButton = findViewById(R.id.mergeData);
        uploadButton = findViewById(R.id.uploadData);

        // Set up Upload button click listener
        uploadButton.setOnClickListener(v -> uploadData());

        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Initialize ActivityResultLauncher for BarcodeDataEditorActivity
        barcodeEditorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Handle the result from BarcodeDataEditorActivity
                    Intent data = result.getData();
                    int position = data.getIntExtra(Constants.EXTRA_POSITION, -1);
                    if (position >= 0 && position < displayList.size()) {
                        // Update the item with new data
                        String newValue = data.getStringExtra(Constants.EXTRA_VALUE);
                        int newSymbology = data.getIntExtra(Constants.EXTRA_SYMBOLOGY, EBarcodesSymbologies.UNKNOWN.getIntValue());
                        int newQuantity = data.getIntExtra(Constants.EXTRA_QUANTITY, 1);
                        // Update the display list
                        DisplayBarcodeData item = displayList.get(position);
                        item.value = newValue;
                        item.symbology = newSymbology;
                        item.quantity = newQuantity;
                        // Reset all swipe states before refreshing
                        resetAllSwipeStates();
                        // Refresh the adapter
                        barcodeAdapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    resetAllSwipeStates();
                }
            }
        );

        Intent intent = getIntent();
        captureFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);
        endpointUri = intent.getStringExtra(Constants.ENDPOINT_URI);

        // Determine the mode based on which extra is provided
        isHttpsPostMode = (endpointUri != null && !endpointUri.isEmpty());

        // Only load existing session data if we're in file mode and have a valid file
        if (!isHttpsPostMode && captureFilePath != null) {
            File captureFile = new File(captureFilePath);
            if(captureFile.exists() && captureFile.length() > 0)
            {
                SessionData = SessionsFilesHelpers.loadData(this, captureFilePath);
                if(SessionData.barcodeValuesMap != null && SessionData.barcodeValuesMap.size() > 0)
                {
                    mergeButton.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                mergeButton.setVisibility(View.GONE);
            }
        }
        else
        {
            // HTTPS Post mode - no merge functionality needed
            mergeButton.setVisibility(View.GONE);
        }

        // Update button visibility and text based on processing mode
        if (isHttpsPostMode) {
            // HTTPS Post mode: Show Upload button, hide Close and Save buttons
            uploadButton.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
        } else {
            // File mode: Hide Upload button, show normal Save button, keep Close button hidden
            uploadButton.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            saveButton.setText(getString(R.string.addtofile));
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

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

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
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barcode_swipeable, parent, false);
                holder = new ViewHolder();
                holder.foregroundContainer = convertView.findViewById(R.id.foreground_container);
                holder.backgroundContainer = convertView.findViewById(R.id.background_container);
                holder.editIcon = convertView.findViewById(R.id.edit_icon);
                holder.trashIcon = convertView.findViewById(R.id.trash_icon);
                holder.valueTextView = convertView.findViewById(R.id.barcode_value);
                holder.symbologyTextView = convertView.findViewById(R.id.barcode_symbology);
                holder.quantityTextView = convertView.findViewById(R.id.barcode_quantity);
                holder.dateTextView = convertView.findViewById(R.id.barcode_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DisplayBarcodeData barcode = getItem(position);

            // Always reset the foreground position to prevent recycled views from showing swiped state
            holder.foregroundContainer.setTranslationX(0);
            // Clear any existing listeners to prevent conflicts
            holder.foregroundContainer.setOnTouchListener(null);
            holder.editIcon.setOnClickListener(null);
            holder.trashIcon.setOnClickListener(null);

            if (barcode != null) {
                String barcodeDateString = dateFormat.format(barcode.date) + " " + sdf.format(barcode.date);

                holder.valueTextView.setText(context.getString(R.string.value_label, barcode.value));
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcode.symbology);
                holder.symbologyTextView.setText(context.getString(R.string.symbology_label, symbology.getName()));
                holder.quantityTextView.setText(context.getString(R.string.quantity_label, barcode.quantity));
                holder.dateTextView.setText(context.getString(R.string.date_label, barcodeDateString));

                if(barcode.loaded == false)
                {
                    holder.foregroundContainer.setBackgroundColor(context.getColor(R.color.pastelbluelight));
                }
                else
                {
                    holder.foregroundContainer.setBackgroundColor(Color.WHITE);
                }
            }

            // Setup swipe gesture detector only for newly captured barcodes (loaded == false)
            if (barcode != null && barcode.loaded == false) {
                setupSwipeGesture(holder, position, barcode);
            }

            return convertView;
        }

        private void setupSwipeGesture(ViewHolder holder, int position, DisplayBarcodeData barcode) {
            GestureDetector gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener(holder, position, barcode));
            
            holder.foregroundContainer.setOnTouchListener(new View.OnTouchListener() {
                private float initialX = 0;
                private boolean isSwipeActive = false;
                
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = event.getX();
                            isSwipeActive = false;
                            break;
                            
                        case MotionEvent.ACTION_MOVE:
                            float deltaX = event.getX() - initialX;
                            if (deltaX < -50 && !isSwipeActive) { // Swipe left threshold
                                isSwipeActive = true;
                                // Show trash can by sliding foreground to the left
                                holder.foregroundContainer.animate()
                                    .translationX(TRANSLATION_X) // Show trash can area
                                    .setDuration(200)
                                    .start();
                            }
                            break;
                            
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (!isSwipeActive) {
                                // Reset position if not fully swiped
                                holder.foregroundContainer.animate()
                                    .translationX(0)
                                    .setDuration(200)
                                    .start();
                            }
                            break;
                    }
                    
                    return true; // Consume the touch event
                }
            });

            // Setup edit icon click listener
            holder.editIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CapturedBarcodesActivity) context).openBarcodeEditor(position, barcode);
                }
            });

            // Setup trash can click listener
            holder.trashIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CapturedBarcodesActivity) context).removeItem(position, barcode);
                }
            });
        }

        private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
            private ViewHolder holder;
            private int position;
            private DisplayBarcodeData barcode;
            
            public SwipeGestureListener(ViewHolder holder, int position, DisplayBarcodeData barcode) {
                this.holder = holder;
                this.position = position;
                this.barcode = barcode;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                // Check for left swipe
                if (Math.abs(diffX) > Math.abs(diffY) && diffX < -100 && Math.abs(velocityX) > 100) {
                    // Show trash can
                    holder.foregroundContainer.animate()
                        .translationX(TRANSLATION_X)
                        .setDuration(300)
                        .start();
                    return true;
                }
                
                return false;
            }
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
        mergeButton.setVisibility(View.GONE);

        // Only update button text in File mode
        if (!isHttpsPostMode) {
            saveButton.setText(R.string.update);
        }
    }

    private void saveData()
    {
        if (isHttpsPostMode) {
            // In HTTPS Post mode, we don't save to file, just finish the activity
            // TODO: Here you could implement the HTTP POST functionality
            // For now, we simply close the activity since data is already captured
            finish();
            return;
        }

        // File mode: save to session file
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

    private void uploadData() {
        android.util.Log.d("CapturedBarcodes", "uploadData() called");
        android.util.Log.d("CapturedBarcodes", "isHttpsPostMode: " + isHttpsPostMode);
        android.util.Log.d("CapturedBarcodes", "endpointUri: " + endpointUri);

        if (!isHttpsPostMode || endpointUri == null || endpointUri.isEmpty()) {
            android.util.Log.w("CapturedBarcodes", "Upload aborted - invalid mode or endpoint");
            return;
        }

        SessionData newBarcodes = sessionDataFromDisplayList(displayList);
        android.util.Log.d("CapturedBarcodes", "SessionData created with " +
            (newBarcodes.barcodeValuesMap != null ? newBarcodes.barcodeValuesMap.size() : 0) + " barcodes");

        // Show progress indication
        uploadButton.setEnabled(false);
        uploadButton.setText(getString(R.string.uploading));
        android.util.Log.d("CapturedBarcodes", "UI updated to show uploading state");

        // Perform upload in background thread
        executorService.execute(() -> {
            try {
                android.util.Log.d("CapturedBarcodes", "Starting background upload task");

                // Convert SessionData to JSON
                android.util.Log.d("CapturedBarcodes", "Converting SessionData to JSON");
                String jsonData = convertSessionDataToJson(newBarcodes);
                android.util.Log.d("CapturedBarcodes", "JSON conversion successful, length: " + jsonData.length());
                android.util.Log.v("CapturedBarcodes", "JSON data: " + jsonData);

                // Perform HTTP POST request
                android.util.Log.d("CapturedBarcodes", "Starting HTTP POST to: " + endpointUri);
                boolean success = performHttpPost(endpointUri, jsonData);
                android.util.Log.d("CapturedBarcodes", "HTTP POST completed, success: " + success);

                // Update UI on main thread
                runOnUiThread(() -> {
                    android.util.Log.d("CapturedBarcodes", "Updating UI on main thread");
                    uploadButton.setEnabled(true);
                    uploadButton.setText(getString(R.string.upload));

                    if (success) {
                        android.util.Log.i("CapturedBarcodes", "Upload successful, showing success toast");
                        android.widget.Toast.makeText(this,
                            getString(R.string.upload_successful),
                            android.widget.Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        android.util.Log.w("CapturedBarcodes", "Upload failed, showing failure toast");
                        android.widget.Toast.makeText(this,
                            getString(R.string.upload_failed),
                            android.widget.Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                android.util.Log.e("CapturedBarcodes", "Exception during upload", e);
                runOnUiThread(() -> {
                    android.util.Log.d("CapturedBarcodes", "Updating UI after exception");
                    uploadButton.setEnabled(true);
                    uploadButton.setText(getString(R.string.upload));
                    android.widget.Toast.makeText(this,
                        getString(R.string.upload_error) + ": " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String convertSessionDataToJson(SessionData sessionData) {
        android.util.Log.d("CapturedBarcodes", "convertSessionDataToJson() called");
        JsonArray barcodesArray = new JsonArray();
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        if (sessionData.barcodeValuesMap != null) {
            android.util.Log.d("CapturedBarcodes", "Processing " + sessionData.barcodeValuesMap.size() + " barcodes");
            int processedCount = 0;

            for (Map.Entry<Integer, String> entry : sessionData.barcodeValuesMap.entrySet()) {
                Integer barcodeId = entry.getKey();
                String value = entry.getValue();

                android.util.Log.v("CapturedBarcodes", "Processing barcode ID: " + barcodeId + ", value: " + value);

                if (value == null || value.isEmpty()) {
                    android.util.Log.d("CapturedBarcodes", "Skipping empty barcode with ID: " + barcodeId);
                    continue;
                }

                JsonObject barcodeObject = new JsonObject();
                barcodeObject.addProperty("value", value);

                // Get symbology
                int symbology = sessionData.barcodeSymbologyMap.getOrDefault(barcodeId,
                    EBarcodesSymbologies.UNKNOWN.getIntValue());
                barcodeObject.addProperty("symbology", symbology);
                android.util.Log.v("CapturedBarcodes", "Symbology for " + barcodeId + ": " + symbology);

                // Get quantity
                int quantity = sessionData.barcodeQuantityMap.getOrDefault(barcodeId, 1);
                barcodeObject.addProperty("quantity", quantity);
                android.util.Log.v("CapturedBarcodes", "Quantity for " + barcodeId + ": " + quantity);

                // Get timestamp
                Date timestamp = sessionData.barcodeDateMap.getOrDefault(barcodeId, new Date());
                String formattedTimestamp = iso8601Format.format(timestamp);
                barcodeObject.addProperty("timestamp", formattedTimestamp);
                android.util.Log.v("CapturedBarcodes", "Timestamp for " + barcodeId + ": " + formattedTimestamp);

                barcodesArray.add(barcodeObject);
                processedCount++;
            }

            android.util.Log.d("CapturedBarcodes", "Successfully processed " + processedCount + " barcodes for JSON");
        } else {
            android.util.Log.w("CapturedBarcodes", "barcodeValuesMap is null");
        }

        JsonObject rootObject = new JsonObject();
        rootObject.add("barcodes", barcodesArray);
        String sessionTimestamp = iso8601Format.format(new Date());
        rootObject.addProperty("session_timestamp", sessionTimestamp);
        android.util.Log.d("CapturedBarcodes", "Session timestamp: " + sessionTimestamp);

        // Add device hostname information
        String deviceHostname = getDeviceHostname();
        rootObject.addProperty("device_info", deviceHostname);
        android.util.Log.d("CapturedBarcodes", "Device hostname: " + deviceHostname);

        Gson gson = new Gson();
        String jsonResult = gson.toJson(rootObject);
        android.util.Log.d("CapturedBarcodes", "JSON conversion completed successfully");
        return jsonResult;
    }

    private String getDeviceHostname() {
        try {
            // Get device model and Android version info
            String deviceModel = Build.MODEL != null ? Build.MODEL : "Unknown";
            String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "Unknown";
            String androidVersion = Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "Unknown";

            // Create a descriptive hostname with device info
            String hostname = manufacturer + "_" + deviceModel + "_Android" + androidVersion;

            // Clean up hostname (remove spaces, special chars)
            hostname = hostname.replaceAll("[^a-zA-Z0-9_-]", "_");

            android.util.Log.d("CapturedBarcodes", "Generated device hostname: " + hostname);
            return hostname;

        } catch (Exception e) {
            android.util.Log.e("CapturedBarcodes", "Error getting device hostname", e);
            return "Unknown_Device";
        }
    }

    private boolean performHttpPost(String endpointUrl, String jsonData) throws IOException {
        android.util.Log.d("CapturedBarcodes", "performHttpPost() called");
        android.util.Log.d("CapturedBarcodes", "Endpoint URL: " + endpointUrl);
        android.util.Log.d("CapturedBarcodes", "JSON data length: " + jsonData.length() + " bytes");

        URL url = new URL(endpointUrl);
        android.util.Log.d("CapturedBarcodes", "URL created successfully");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        android.util.Log.d("CapturedBarcodes", "HTTP connection opened");

        try {
            // Configure HTTPS connection if needed
            if (connection instanceof HttpsURLConnection) {
                android.util.Log.d("CapturedBarcodes", "Detected HTTPS connection, configuring SSL");
                configureSslForHttps((HttpsURLConnection) connection);
                android.util.Log.d("CapturedBarcodes", "SSL configuration completed");
            } else {
                android.util.Log.d("CapturedBarcodes", "Using HTTP connection (no SSL needed)");
            }

            // Set request method and headers
            android.util.Log.d("CapturedBarcodes", "Setting request method and headers");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            android.util.Log.d("CapturedBarcodes", "Headers set: POST, JSON content-type, timeouts configured");

            // Add authentication if configured
            android.util.Log.d("CapturedBarcodes", "Adding authentication headers if configured");
            addAuthenticationHeaders(connection);

            // Send JSON data
            android.util.Log.d("CapturedBarcodes", "Sending JSON data to server");
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                android.util.Log.d("CapturedBarcodes", "Writing " + input.length + " bytes to output stream");
                os.write(input, 0, input.length);
                os.flush();
                android.util.Log.d("CapturedBarcodes", "JSON data sent successfully");
            }

            // Check response code
            android.util.Log.d("CapturedBarcodes", "Getting response code from server");
            int responseCode = connection.getResponseCode();
            android.util.Log.d("CapturedBarcodes", "Server response code: " + responseCode);

            String responseMessage = connection.getResponseMessage();
            android.util.Log.d("CapturedBarcodes", "Server response message: " + responseMessage);

            // Log response headers
            for (Map.Entry<String, java.util.List<String>> header : connection.getHeaderFields().entrySet()) {
                android.util.Log.v("CapturedBarcodes", "Response header: " + header.getKey() + " = " + header.getValue());
            }

            boolean success = responseCode >= 200 && responseCode < 300;
            android.util.Log.d("CapturedBarcodes", "Request success: " + success + " (code in 200-299 range)");

            return success;

        } catch (IOException e) {
            android.util.Log.e("CapturedBarcodes", "IOException during HTTP request", e);
            throw e;
        } finally {
            android.util.Log.d("CapturedBarcodes", "Disconnecting HTTP connection");
            connection.disconnect();
        }
    }

    private void configureSslForHttps(HttpsURLConnection httpsConnection) {
        android.util.Log.d("CapturedBarcodes", "configureSslForHttps() called");
        try {
            // Create a trust manager that accepts all certificates (for self-signed certificates)
            android.util.Log.d("CapturedBarcodes", "Creating trust-all SSL context for self-signed certificates");
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Trust all client certificates
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Trust all server certificates (including self-signed)
                    }
                }
            };

            // Install the all-trusting trust manager
            android.util.Log.d("CapturedBarcodes", "Installing SSL context and hostname verifier");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            // Create a hostname verifier that accepts all hostnames
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Accept all hostnames
                }
            };
            httpsConnection.setHostnameVerifier(allHostsValid);
            android.util.Log.d("CapturedBarcodes", "SSL configuration completed successfully");

        } catch (Exception e) {
            // Log the error but continue with default SSL configuration
            android.util.Log.e("CapturedBarcodes", "Failed to configure SSL for self-signed certificates", e);
        }
    }

    private void addAuthenticationHeaders(HttpURLConnection connection) {
        android.util.Log.d("CapturedBarcodes", "addAuthenticationHeaders() called");

        // Get authentication settings from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        boolean authEnabled = sharedPreferences.getBoolean(Constants.SHARED_PREFERENCES_HTTPS_AUTHENTICATION, false);
        android.util.Log.d("CapturedBarcodes", "Authentication enabled: " + authEnabled);

        if (authEnabled) {
            android.util.Log.d("CapturedBarcodes", "Loading credentials from keystore");
            KeystoreHelper keystoreHelper = new KeystoreHelper(this);
            String username = keystoreHelper.getUsername();
            String password = keystoreHelper.getPassword();

            android.util.Log.d("CapturedBarcodes", "Username loaded: " + (username.isEmpty() ? "empty" : "present"));
            android.util.Log.d("CapturedBarcodes", "Password loaded: " + (password.isEmpty() ? "empty" : "present"));

            if (!username.isEmpty() && !password.isEmpty()) {
                android.util.Log.d("CapturedBarcodes", "Adding Basic Authentication header");
                // Use Basic Authentication
                String credentials = username + ":" + password;
                String basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", basicAuth);
                android.util.Log.d("CapturedBarcodes", "Authorization header added successfully");
            } else {
                android.util.Log.w("CapturedBarcodes", "Authentication enabled but credentials are missing");
            }
        } else {
            android.util.Log.d("CapturedBarcodes", "Authentication disabled, no headers added");
        }
    }

    private static class ViewHolder {
        View foregroundContainer;
        View backgroundContainer;
        ImageView editIcon;
        ImageView trashIcon;
        TextView valueTextView;
        TextView symbologyTextView;
        TextView quantityTextView;
        TextView dateTextView;
    }

    public void removeItem(int position, DisplayBarcodeData barcode) {
        if (barcode != null && position < displayList.size()) {
            // Remove from display list
            displayList.remove(position);
            
            // Force a complete refresh of all views to reset their state
            barcodeAdapter.notifyDataSetChanged();
        }
    }
    
    private void openBarcodeEditor(int position, DisplayBarcodeData barcode) {
        if (barcode != null && barcode.loaded == false) {
            Intent intent = new Intent(CapturedBarcodesActivity.this, BarcodeDataEditorActivity.class);
            intent.putExtra(Constants.EXTRA_POSITION, position);
            intent.putExtra(Constants.EXTRA_VALUE, barcode.value);
            intent.putExtra(Constants.EXTRA_SYMBOLOGY, barcode.symbology);
            intent.putExtra(Constants.EXTRA_QUANTITY, barcode.quantity);
            intent.putExtra(Constants.EXTRA_DATE, barcode.date.getTime()); // Convert Date to long
            
            barcodeEditorLauncher.launch(intent);
        }
    }
    
    private void resetAllSwipeStates() {
        // Reset swipe state for all visible views
        for (int i = 0; i < barcodesListView.getChildCount(); i++) {
            View child = barcodesListView.getChildAt(i);
            if (child != null) {
                View foregroundContainer = child.findViewById(R.id.foreground_container);
                if (foregroundContainer != null) {
                    foregroundContainer.setTranslationX(0);
                }
            }
        }
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Set status bar color to zebra blue
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.zebra));
        
        // Set navigation bar color to black for consistent theming
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.black));
        }
        
        // Set status bar text to light (white) to contrast with blue background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_STATUS_BAR to use light (white) text
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        
        // Set navigation bar text to light (white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            View decorView = window.getDecorView();
            // Remove SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR to use light (white) icons
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}