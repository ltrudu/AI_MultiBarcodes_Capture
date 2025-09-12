package com.zebra.ai_multibarcodes_capture.filemanagement;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {

    private static final int SWIPE_DISTANCE_X = -250;

    public interface OnFileDeleteListener {
        void onFileDelete(File file, int position);
    }
    
    public interface OnFolderClickListener {
        void onFolderClick(File folder);
    }

    private Context mContext;
    private ArrayList<File> fileList;
    private OnFileDeleteListener deleteListener;
    private OnFolderClickListener folderClickListener;
    private ListView parentListView;
    private int selectedPosition = -1;

    public FileAdapter(Context context, ArrayList<File> files) {
        super(context, 0, files);
        this.mContext = context;
        this.fileList = files;
    }
    
    public void setParentListView(ListView listView) {
        this.parentListView = listView;
    }

    public void setOnFileDeleteListener(OnFileDeleteListener listener) {
        this.deleteListener = listener;
    }
    
    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.folderClickListener = listener;
    }
    
    public void resetSwipeState() {
        // Find all visible views and reset their swipe state
        if (parentListView != null) {
            for (int i = 0; i < parentListView.getChildCount(); i++) {
                View childView = parentListView.getChildAt(i);
                View foregroundContainer = childView.findViewById(R.id.foreground_container);
                if (foregroundContainer != null && foregroundContainer.getTranslationX() != 0) {
                    foregroundContainer.animate()
                        .translationX(0)
                        .setDuration(300)
                        .start();
                }
            }
        }
    }
    
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }
    
    public int getSelectedPosition() {
        return selectedPosition;
    }
    
    public File getSelectedFile() {
        if (selectedPosition >= 0 && selectedPosition < fileList.size()) {
            return fileList.get(selectedPosition);
        }
        return null;
    }
    
    public void clearSelection() {
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        File file = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_file_swipeable, parent, false);
            holder = new ViewHolder();
            holder.foregroundContainer = convertView.findViewById(R.id.foreground_container);
            holder.backgroundContainer = convertView.findViewById(R.id.background_container);
            holder.trashIcon = convertView.findViewById(R.id.trash_icon);
            holder.textView = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set display name
        String displayName;
        if (file.getName().equals("..")) {
            displayName = ".. (Parent Directory)";
        } else if (file.isDirectory()) {
            displayName = "📁 " + file.getName();
        } else {
            displayName = "📄 " + file.getName();
        }
        holder.textView.setText(displayName);

        // Set selection state (but never for parent directory or folders)
        boolean shouldBeSelected = position == selectedPosition && !file.getName().equals("..") && file.isFile();
        holder.foregroundContainer.setSelected(shouldBeSelected);
        
        // Force parent directory to never show any selection state
        if (file.getName().equals("..")) {
            holder.foregroundContainer.setPressed(false);
            holder.foregroundContainer.setActivated(false);
        }

        // Remove long press listener - selection is now handled in touch events
        holder.foregroundContainer.setOnLongClickListener(null);

        // Always clear existing listeners and reset view state (due to ViewHolder reuse)
        holder.foregroundContainer.setOnTouchListener(null);
        if (holder.trashIcon != null) {
            holder.trashIcon.setOnClickListener(null);
        }
        // Aggressively reset any leftover swipe animation state
        float currentTranslationX = holder.foregroundContainer.getTranslationX();
        LogUtils.d("FileAdapter", "RESETTING view state for " + file.getName() + " - current translationX: " + currentTranslationX);
        
        // Cancel any running animations
        holder.foregroundContainer.clearAnimation();
        holder.foregroundContainer.animate().cancel();
        
        // Reset all transform properties
        holder.foregroundContainer.setTranslationX(0);
        holder.foregroundContainer.setTranslationY(0);
        holder.foregroundContainer.setScaleX(1.0f);
        holder.foregroundContainer.setScaleY(1.0f);
        holder.foregroundContainer.setAlpha(1.0f);
        
        // ALWAYS hide the red background by default - only show during actual swipe
        if (holder.backgroundContainer != null) {
            LogUtils.d("FileAdapter", "HIDING background by default for: " + file.getName());
            holder.backgroundContainer.setVisibility(View.GONE);
        }
        
        // Force the view to update immediately
        holder.foregroundContainer.requestLayout();
        holder.foregroundContainer.invalidate();
        if (holder.backgroundContainer != null) {
            holder.backgroundContainer.requestLayout();
            holder.backgroundContainer.invalidate();
        }
        
        // Verify the reset worked
        float afterResetTranslationX = holder.foregroundContainer.getTranslationX();
        int backgroundVisibility = holder.backgroundContainer != null ? holder.backgroundContainer.getVisibility() : View.GONE;
        float backgroundAlpha = holder.backgroundContainer != null ? holder.backgroundContainer.getAlpha() : 0.0f;
        LogUtils.d("FileAdapter", "AFTER RESET for " + file.getName() + " - translationX: " + afterResetTranslationX + 
            ", background visibility: " + backgroundVisibility + ", background alpha: " + backgroundAlpha);
        
        // Setup touch handling - background is hidden by default for ALL items
        if (!file.getName().equals("..")) {
            LogUtils.d("FileAdapter", "Setting up SWIPE gesture for: " + file.getName());
            // Regular files and folders get full swipe gesture support
            setupSwipeGesture(holder, position, file);
        } else {
            LogUtils.d("FileAdapter", "Setting up PARENT TAP for: " + file.getName());
            // Parent directory only needs tap handling, no swipe
            setupParentDirectoryTap(holder, file);
        }

        return convertView;
    }

    private void setupSwipeGesture(ViewHolder holder, int position, File file) {
        GestureDetector gestureDetector = new GestureDetector(mContext, new SwipeGestureListener(holder, position, file));
        
        holder.foregroundContainer.setOnTouchListener(new View.OnTouchListener() {
            private float initialX = 0;
            private float initialY = 0;
            private boolean isSwipeActive = false;
            private boolean touchHandled = false;
            private MotionEvent downEvent = null;
            private boolean gestureDetectorUsed = false;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogUtils.d("FileAdapter", "=== SWIPE TOUCH EVENT for " + file.getName() + " ===");
                LogUtils.d("FileAdapter", "Event action: " + event.getAction() + " at (" + event.getX() + ", " + event.getY() + ")");
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        LogUtils.d("FileAdapter", "ACTION_DOWN for " + file.getName());
                        initialX = event.getX();
                        initialY = event.getY();
                        isSwipeActive = false;
                        touchHandled = false;
                        gestureDetectorUsed = false;
                        // Store the down event for potential gesture detection later
                        downEvent = MotionEvent.obtain(event);
                        LogUtils.d("FileAdapter", "ACTION_DOWN returning false for " + file.getName());
                        // Never consume DOWN events - let ListView handle them
                        return false;
                        
                    case MotionEvent.ACTION_MOVE:
                        LogUtils.d("FileAdapter", "ACTION_MOVE for " + file.getName());
                        if (touchHandled) {
                            LogUtils.d("FileAdapter", "ACTION_MOVE already handled, returning true for " + file.getName());
                            return true; // Already handling this touch sequence
                        }
                        
                        float moveX = event.getX() - initialX;
                        float moveY = event.getY() - initialY;
                        LogUtils.d("FileAdapter", "ACTION_MOVE delta: (" + moveX + ", " + moveY + ") for " + file.getName());
                        
                        // Only intercept if this is clearly a horizontal left swipe
                        // Need significant horizontal movement with minimal vertical movement
                        if (Math.abs(moveX) > 120 && Math.abs(moveY) < 40 && moveX < -100) {
                            if (!isSwipeActive) {
                                LogUtils.d("FileAdapter", "Triggering swipe animation in ACTION_MOVE for: " + file.getName() + " - setting translationX to " + SWIPE_DISTANCE_X);
                                isSwipeActive = true;
                                touchHandled = true;
                                // Show the red background first, then slide foreground
                                if (holder.backgroundContainer != null) {
                                    LogUtils.d("FileAdapter", "SHOWING background for animation trigger for: " + file.getName());
                                    holder.backgroundContainer.setVisibility(View.VISIBLE);
                                }
                                // Show trash can by sliding foreground to the left
                                holder.foregroundContainer.animate()
                                    .translationX(SWIPE_DISTANCE_X)
                                    .setDuration(200)
                                    .start();
                                // Prevent ListView from scrolling
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                            LogUtils.d("FileAdapter", "ACTION_MOVE swipe active, returning true for " + file.getName());
                            return true;
                        }
                        
                        // Only check gesture detector for potential swipes when there's significant movement
                        if (Math.abs(moveX) > 30 || Math.abs(moveY) > 30) {
                            LogUtils.d("FileAdapter", "ACTION_MOVE activating gesture detector for " + file.getName());
                            if (downEvent != null) {
                                gestureDetector.onTouchEvent(downEvent);
                                downEvent = null; // Only send once
                                gestureDetectorUsed = true;
                            }
                            if (gestureDetectorUsed) {
                                gestureDetector.onTouchEvent(event);
                            }
                        }
                        
                        LogUtils.d("FileAdapter", "ACTION_MOVE returning false for " + file.getName());
                        return false;
                        
                    case MotionEvent.ACTION_UP:
                        LogUtils.d("FileAdapter", "ACTION_UP for " + file.getName());
                        // Clean up stored event
                        if (downEvent != null) {
                            downEvent.recycle();
                            downEvent = null;
                        }
                        
                        if (isSwipeActive || touchHandled) {
                            LogUtils.d("FileAdapter", "ACTION_UP swipe was active, returning true for " + file.getName());
                            // We handled this as a swipe
                            return true;
                        }
                        
                        // If gesture detector was used, send the UP event too
                        if (gestureDetectorUsed) {
                            LogUtils.d("FileAdapter", "ACTION_UP sending to gesture detector for " + file.getName());
                            gestureDetector.onTouchEvent(event);
                        }
                        
                        // Check if view moved during touch
                        if (holder.foregroundContainer.getTranslationX() != 0) {
                            LogUtils.d("FileAdapter", "ACTION_UP resetting partial movement for " + file.getName());
                            // Reset any partial movement and hide background
                            holder.foregroundContainer.animate()
                                .translationX(0)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    // Hide background after animation completes
                                    if (holder.backgroundContainer != null) {
                                        LogUtils.d("FileAdapter", "HIDING background after reset animation for: " + file.getName());
                                        holder.backgroundContainer.setVisibility(View.GONE);
                                    }
                                })
                                .start();
                            return true;
                        }
                        
                        // Check if this was a tap (minimal movement)
                        float deltaX = event.getX() - initialX;
                        float deltaY = event.getY() - initialY;
                        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        LogUtils.d("FileAdapter", "ACTION_UP tap check: distance=" + distance + " for " + file.getName());
                        
                        if (distance < 20) {  // This is a tap
                            LogUtils.d("FileAdapter", "ACTION_UP detected tap for " + file.getName());
                            // Handle folder navigation or file selection
                            if (file.isDirectory() || file.getName().equals("..")) {
                                LogUtils.d("FileAdapter", "ACTION_UP calling folder click for " + file.getName());
                                if (folderClickListener != null) {
                                    folderClickListener.onFolderClick(file);
                                    return true;
                                }
                            } else if (file.isFile()) {
                                LogUtils.d("FileAdapter", "ACTION_UP toggling file selection for " + file.getName());
                                // For files, toggle selection on tap
                                if (selectedPosition == position) {
                                    clearSelection();
                                } else {
                                    setSelectedPosition(position);
                                }
                                return true;
                            }
                        }
                        
                        LogUtils.d("FileAdapter", "ACTION_UP returning false for " + file.getName());
                        return false;
                        
                    case MotionEvent.ACTION_CANCEL:
                        if (holder.foregroundContainer.getTranslationX() != 0) {
                            holder.foregroundContainer.animate()
                                .translationX(0)
                                .setDuration(200)
                                .start();
                        }
                        return false;
                }
                
                return false;
            }
        });

        // Setup trash can click listener
        holder.trashIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("FileAdapter", "Trash icon clicked for: " + file.getName());
                if (deleteListener != null) {
                    deleteListener.onFileDelete(file, position);
                } else {
                    LogUtils.d("FileAdapter", "Delete listener is null!");
                }
            }
        });
    }

    private void setupParentDirectoryTap(ViewHolder holder, File file) {
        LogUtils.d("FileAdapter", "Setting up parent directory tap for: " + file.getName());
        holder.foregroundContainer.setOnTouchListener(new View.OnTouchListener() {
            private float initialX = 0;
            private float initialY = 0;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogUtils.d("FileAdapter", "=== PARENT TOUCH EVENT for " + file.getName() + " ===");
                LogUtils.d("FileAdapter", "Parent event action: " + event.getAction() + " at (" + event.getX() + ", " + event.getY() + ")");
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        LogUtils.d("FileAdapter", "Parent ACTION_DOWN for " + file.getName());
                        initialX = event.getX();
                        initialY = event.getY();
                        LogUtils.d("FileAdapter", "Parent ACTION_DOWN returning false for " + file.getName());
                        return false;
                        
                    case MotionEvent.ACTION_UP:
                        LogUtils.d("FileAdapter", "Parent ACTION_UP for " + file.getName());
                        // Check if this was a tap (minimal movement)
                        float deltaX = event.getX() - initialX;
                        float deltaY = event.getY() - initialY;
                        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                        LogUtils.d("FileAdapter", "Parent ACTION_UP distance: " + distance + " for " + file.getName());
                        
                        if (distance < 20) {  // This is a tap
                            LogUtils.d("FileAdapter", "Parent ACTION_UP detected tap, calling folder click for " + file.getName());
                            if (folderClickListener != null) {
                                folderClickListener.onFolderClick(file);
                                return true;
                            }
                        }
                        LogUtils.d("FileAdapter", "Parent ACTION_UP returning false for " + file.getName());
                        return false;
                }
                LogUtils.d("FileAdapter", "Parent default returning false for " + file.getName());
                return false;
            }
        });
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private ViewHolder holder;
        private int position;
        private File file;
        
        public SwipeGestureListener(ViewHolder holder, int position, File file) {
            this.holder = holder;
            this.position = position;
            this.file = file;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LogUtils.d("FileAdapter", "=== GESTURE onFling for " + file.getName() + " ===");
            if (e1 == null || e2 == null) {
                LogUtils.d("FileAdapter", "onFling: e1 or e2 is null, returning false for " + file.getName());
                return false;
            }
            
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            LogUtils.d("FileAdapter", "onFling: diffX=" + diffX + " diffY=" + diffY + " velocityX=" + velocityX + " velocityY=" + velocityY + " for " + file.getName());
            
            // Check for left swipe - be very strict to avoid accidental triggers
            if (Math.abs(diffX) > Math.abs(diffY) && diffX < -120 && Math.abs(velocityX) > 200 && Math.abs(diffY) < 50) {
                LogUtils.d("FileAdapter", "Triggering swipe animation in onFling for: " + file.getName() + " - setting translationX to " + SWIPE_DISTANCE_X);
                // Show the red background first, then slide foreground
                if (holder.backgroundContainer != null) {
                    LogUtils.d("FileAdapter", "SHOWING background for onFling trigger for: " + file.getName());
                    holder.backgroundContainer.setVisibility(View.VISIBLE);
                }
                // Show trash can
                holder.foregroundContainer.animate()
                    .translationX(SWIPE_DISTANCE_X)
                    .setDuration(300)
                    .start();
                return true;
            }
            
            LogUtils.d("FileAdapter", "onFling: conditions not met, returning false for " + file.getName());
            return false;
        }
    }

    private static class ViewHolder {
        View foregroundContainer;
        View backgroundContainer;
        View trashIcon;
        TextView textView;
    }
}
