package com.zebra.ai_multibarcodes_capture.sessionmanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.filemanagement.ExportWriters;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SessionViewerActivity extends AppCompatActivity {

    private static class DisplayBarcodeData {
        String value;
        int symbology;
        int quantity;
        Date date;

        public DisplayBarcodeData(String value, int symbology, int quantity, Date date) {
            this.value = value;
            this.symbology = symbology;
            this.quantity = quantity;
            this.date = date;
        }
    }

    private String sessionFilePath;
    private ExportWriters.loadedData loadedData;
    private ListView barcodesListView;
    private Button closeButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_session_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        Intent intent = getIntent();
        sessionFilePath = intent.getStringExtra(Constants.CAPTURE_FILE_PATH);

        loadedData = ExportWriters.loadData(this, sessionFilePath);
        if(loadedData == null)
        {
            Toast.makeText(this, getString(R.string.error_loading_data, sessionFilePath), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize views
        barcodesListView = findViewById(R.id.session_barcodes_list_view);
        closeButton = findViewById(R.id.session_closeButton);
        saveButton = findViewById(R.id.session_saveData);

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
    }

    private void setupListView() {
        // Convert loadedData to display list (same format as CapturedBarcodesActivity)
        List<DisplayBarcodeData> displayList = new ArrayList<>();
        
        if (loadedData.barcodeQuantityMap != null) {
            for (Map.Entry<String, Integer> entry : loadedData.barcodeQuantityMap.entrySet()) {
                String value = entry.getKey();
                int quantity = entry.getValue();
                int symbology = loadedData.barcodeSymbologyMap.getOrDefault(value, EBarcodesSymbologies.UNKNOWN.getIntValue());
                Date date = loadedData.barcodeDateMap.getOrDefault(value, new Date());
                
                displayList.add(new DisplayBarcodeData(value, symbology, quantity, date));
            }
        }

        // Set up adapter (same as CapturedBarcodesActivity)
        BarcodeAdapter adapter = new BarcodeAdapter(this, displayList);
        barcodesListView.setAdapter(adapter);
    }

    private void saveSessionData() {
        // TODO: Implement session data sharing functionality
        Toast.makeText(this, "Share functionality not yet implemented", Toast.LENGTH_SHORT).show();
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

                holder.valueTextView.setText("Value: " + barcode.value);
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(barcode.symbology);
                holder.symbologyTextView.setText("Symbology: " + symbology.getName());
                holder.quantityTextView.setText("Quantity: " + barcode.quantity);
                holder.dateTextView.setText("Date: " + barcodeDateString);
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
                                    .translationX(-120) // Show trash can area
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
                        .translationX(-120)
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
        ImageView trashIcon;
        TextView valueTextView;
        TextView symbologyTextView;
        TextView quantityTextView;
        TextView dateTextView;
    }

    private void removeItem(int position, DisplayBarcodeData barcode) {
        if (barcode != null) {
            // Remove from loadedData maps
            loadedData.barcodeQuantityMap.remove(barcode.value);
            loadedData.barcodeSymbologyMap.remove(barcode.value);
            loadedData.barcodeDateMap.remove(barcode.value);
            
            // Refresh the ListView
            setupListView();
            
            Toast.makeText(this, "Item removed: " + barcode.value, Toast.LENGTH_SHORT).show();
        }
    }
}