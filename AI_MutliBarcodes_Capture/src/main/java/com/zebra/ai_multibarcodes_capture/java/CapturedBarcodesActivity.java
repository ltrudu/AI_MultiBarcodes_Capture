package com.zebra.ai_multibarcodes_capture.java;

import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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

    private static final int TRANSLATION_X = -180;

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
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_barcode_swipeable, parent, false);
                holder = new ViewHolder();
                holder.foregroundContainer = convertView.findViewById(R.id.foreground_container);
                holder.backgroundContainer = convertView.findViewById(R.id.background_container);
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
            holder.trashIcon.setOnClickListener(null);

            if (barcode != null) {
                String barcodeDateString = dateFormat.format(barcode.date) + " " + sdf.format(barcode.date);

                holder.valueTextView.setText("Value: " + barcode.value);
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcode.symbology);
                holder.symbologyTextView.setText("Symbology: " + symbology.getName());
                holder.quantityTextView.setText("Quantity: " + barcode.quantity);
                holder.dateTextView.setText("Date: " + barcodeDateString);

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

        saveButton.setText(R.string.update);
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

    private static class ViewHolder {
        View foregroundContainer;
        View backgroundContainer;
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
}