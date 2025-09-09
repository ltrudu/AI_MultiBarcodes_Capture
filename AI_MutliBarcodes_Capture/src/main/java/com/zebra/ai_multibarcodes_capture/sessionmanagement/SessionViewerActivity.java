package com.zebra.ai_multibarcodes_capture.sessionmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.zebra.ai_multibarcodes_capture.helpers.LocaleHelper;
import com.zebra.ai_multibarcodes_capture.helpers.SessionData;
import com.zebra.ai_multibarcodes_capture.java.CapturedBarcodesActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionViewerActivity extends AppCompatActivity {

    private static class DisplayBarcodeData {
        Integer key;
        String value;
        int symbology;
        int quantity;
        Date date;

        public DisplayBarcodeData(Integer key, String value, int symbology, int quantity, Date date) {
            this.key = key;
            this.value = value;
            this.symbology = symbology;
            this.quantity = quantity;
            this.date = date;
        }
    }

    private String sessionFilePath;
    private SessionData SessionData;
    private ListView barcodesListView;
    private Button closeButton;
    private Button saveButton;
    private Button mergeButton;

    private static final int TRANSLATION_X = -360; // Updated for two icons (60dp each)

    List<DisplayBarcodeData> displayList;
    BarcodeAdapter adapter;

    // ActivityResultLauncher for BarcodeDataEditorActivity
    private ActivityResultLauncher<Intent> barcodeEditorLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_viewer);
        
        // Configure system bars
        configureSystemBars();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
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
                        adapter.notifyDataSetChanged();
                        // Mark save button as update
                        saveButton.setText(R.string.update);
                    }
                }
                else
                {
                    resetAllSwipeStates();
                }
            }
        );
        
        Intent intent = getIntent();
        sessionFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);

        SessionData = SessionsFilesHelpers.loadData(this, sessionFilePath);
        if(SessionData == null)
        {
            Toast.makeText(this, getString(R.string.error_loading_data, sessionFilePath), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        barcodesListView = findViewById(R.id.session_barcodes_list_view);
        closeButton = findViewById(R.id.session_closeButton);
        saveButton = findViewById(R.id.session_saveData);
        mergeButton = findViewById(R.id.session_mergequantities);

        // Populate ListView with loaded data
        setupListView();

        // Setup button listeners
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSessionData();
            }
        });

        mergeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergeData();
            }
        });
    }

    private void setupListView() {
        // Convert SessionData to display list (same format as CapturedBarcodesActivity)
        displayList = new ArrayList<>();
        
        if (SessionData.barcodeQuantityMap != null) {
            for (Map.Entry<Integer, String> entry : SessionData.barcodeValuesMap.entrySet()) {
                String value = entry.getValue();
                int quantity = SessionData.barcodeQuantityMap.getOrDefault(entry.getKey(), 1);
                int symbology = SessionData.barcodeSymbologyMap.getOrDefault(entry.getKey(), EBarcodesSymbologies.UNKNOWN.getIntValue());
                Date date = SessionData.barcodeDateMap.getOrDefault(entry.getKey(), new Date());
                
                displayList.add(new DisplayBarcodeData(entry.getKey(), value, symbology, quantity, date));
            }
        }

        // Set up adapter (same as CapturedBarcodesActivity)
        adapter = new BarcodeAdapter(this, displayList);
        barcodesListView.setAdapter(adapter);
    }

    private SessionData sessionDataFromDisplayList(List<DisplayBarcodeData> dataList)
    {
        SessionData newBarcodes = new SessionData();
        Integer uniqueId = 0;
        for(DisplayBarcodeData entry : dataList)
        {
            newBarcodes.barcodeValuesMap.put(uniqueId, entry.value);
            newBarcodes.barcodeDateMap.put(uniqueId, entry.date);
            newBarcodes.barcodeQuantityMap.put(uniqueId, entry.quantity);
            newBarcodes.barcodeSymbologyMap.put(uniqueId, entry.symbology);
            uniqueId++;
        }
        return newBarcodes;
    }


    private void saveSessionData() {
        File sessionFile = new File(sessionFilePath);
        sessionFile.delete();
        SessionData = sessionDataFromDisplayList(displayList);
        SessionsFilesHelpers.saveData(this, sessionFilePath, SessionData);
    }

    private class BarcodeAdapter extends ArrayAdapter<DisplayBarcodeData> {

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        public BarcodeAdapter(@NonNull Context context, List<DisplayBarcodeData> barcodes) {
            super(context, 0, barcodes);
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

            if (barcode != null) {
                String barcodeDateString = dateFormat.format(barcode.date) + " " + sdf.format(barcode.date);

                holder.valueTextView.setText(getContext().getString(R.string.value_label, barcode.value));
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcode.symbology);
                holder.symbologyTextView.setText(getContext().getString(R.string.symbology_label, symbology.getName()));
                holder.quantityTextView.setText(getContext().getString(R.string.quantity_label, barcode.quantity));
                holder.dateTextView.setText(getContext().getString(R.string.date_label, barcodeDateString));
            }

            // Setup swipe gesture detector for this item
            setupSwipeGesture(holder, position, barcode);

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
                    openBarcodeEditor(position, barcode);
                }
            });

            // Setup trash can click listener
            holder.trashIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(position, barcode);
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

    private void openBarcodeEditor(int position, DisplayBarcodeData barcode) {
        if (barcode != null) {
            Intent intent = new Intent(SessionViewerActivity.this, BarcodeDataEditorActivity.class);
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

    private void removeItem(int position, DisplayBarcodeData barcode) {
        if (barcode != null) {
            // Remove from SessionData maps
            SessionData.barcodeValuesMap.remove(barcode.key);
            SessionData.barcodeQuantityMap.remove(barcode.key);
            SessionData.barcodeSymbologyMap.remove(barcode.key);
            SessionData.barcodeDateMap.remove(barcode.key);
            
            // Refresh the ListView
            setupListView();

            SessionViewerActivity.this.saveButton.setText(R.string.update);
        }
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
            targetList.add(new DisplayBarcodeData(barcodeUniqueIndex, value, symbology, quantity, currentDate));
        }

        displayList.clear();
        displayList.addAll(targetList);
        adapter.notifyDataSetChanged();

        SessionViewerActivity.this.saveButton.setText(R.string.update);
        SessionViewerActivity.this.mergeButton.setVisibility(View.GONE);

        Toast.makeText(this, getString(R.string.session_data_merged), Toast.LENGTH_SHORT).show();

    }

    public static Integer getKeyFromValue(HashMap<Integer, String> map, String value) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Return null if the value is not found
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
    protected void attachBaseContext(Context newBase) {
        String languageCode = LocaleHelper.getCurrentLanguageCode(newBase);
        Context context = LocaleHelper.setLocale(newBase, languageCode);
        super.attachBaseContext(context);
    }
}