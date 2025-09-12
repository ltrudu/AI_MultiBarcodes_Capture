// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

/**
 * Custom view that provides an interactive capture zone overlay.
 * Features:
 * - Displays a rectangular capture zone that can be moved and resized
 * - Areas outside the capture zone are dimmed with semi-transparent overlay
 * - Supports zoom gestures to resize the capture zone
 * - Supports pan gestures to move the capture zone
 * - Enforces boundary constraints to keep capture zone within view bounds
 */
public class CaptureZoneOverlay extends View {
    
    private static final String TAG = "CaptureZoneOverlay";
    
    // Default capture zone properties
    private static final float DEFAULT_ZONE_WIDTH_RATIO = 0.7f;
    private static final float DEFAULT_ZONE_HEIGHT_RATIO = 0.5f;
    private static final float MIN_ZONE_SIZE_RATIO = 0.2f;
    private static final float MAX_ZONE_SIZE_RATIO = 0.9f;
    private static final float ANCHOR_SIZE = 30f;
    private static final float ANCHOR_TOUCH_RADIUS = 50f;
    
    // Paint objects for rendering
    private Paint dimPaint;
    private Paint zoneBorderPaint;
    private Paint clearPaint;
    private Paint anchorPaint;
    private Paint anchorBorderPaint;
    
    // Capture zone rectangle
    private RectF captureZone;
    private boolean isVisible = false;
    
    // Pending dimensions to restore
    private int pendingX = -1, pendingY = -1, pendingWidth = -1, pendingHeight = -1;
    
    // Gesture detectors
    private ScaleGestureDetector scaleDetector;
    
    // Gesture handling state
    private boolean isScaling = false;
    private boolean isPanning = false;
    private boolean isResizingWithAnchor = false;
    private float lastTouchX, lastTouchY;
    private float dragStartX, dragStartY;
    
    // Anchor tracking
    private enum AnchorPosition {
        NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    private AnchorPosition activeAnchor = AnchorPosition.NONE;
    private RectF originalZoneBeforeResize;
    
    public interface CaptureZoneListener {
        void onCaptureZoneChanged(RectF captureZone);
    }
    
    private CaptureZoneListener listener;

    public CaptureZoneOverlay(Context context) {
        super(context);
        init();
    }

    public CaptureZoneOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureZoneOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paint objects
        dimPaint = new Paint();
        dimPaint.setColor(0xC0808080); // Less transparent gray (75% opacity)
        dimPaint.setAntiAlias(true);
        
        zoneBorderPaint = new Paint();
        zoneBorderPaint.setColor(0xFFFFFFFF); // White border
        zoneBorderPaint.setStyle(Paint.Style.STROKE);
        zoneBorderPaint.setStrokeWidth(4f);
        zoneBorderPaint.setAntiAlias(true);
        
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        // Initialize anchor paints
        anchorPaint = new Paint();
        anchorPaint.setColor(0xFFFFFFFF); // White fill
        anchorPaint.setStyle(Paint.Style.FILL);
        anchorPaint.setAntiAlias(true);
        
        anchorBorderPaint = new Paint();
        anchorBorderPaint.setColor(0xFF000000); // Black border
        anchorBorderPaint.setStyle(Paint.Style.STROKE);
        anchorBorderPaint.setStrokeWidth(2f);
        anchorBorderPaint.setAntiAlias(true);
        
        // Initialize capture zone
        captureZone = new RectF();
        
        // Initialize gesture detectors
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        
        LogUtils.d(TAG, "CaptureZoneOverlay initialized - initial visibility: " + isVisible + ", view visibility: " + getVisibility());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initializeCaptureZone(w, h);
        LogUtils.d(TAG, "View size changed: " + w + "x" + h + ", isVisible=" + isVisible + ", viewVisibility=" + getVisibility());
    }

    private void initializeCaptureZone(int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) return;
        
        // Check if we have pending dimensions to restore
        if (pendingX != -1 && pendingY != -1 && pendingWidth != -1 && pendingHeight != -1) {
            // Validate pending dimensions are within view bounds
            if (pendingX + pendingWidth <= viewWidth && pendingY + pendingHeight <= viewHeight) {
                captureZone.set(pendingX, pendingY, pendingX + pendingWidth, pendingY + pendingHeight);
                LogUtils.d(TAG, "Restored capture zone from pending dimensions: " + captureZone.toString());
            } else {
                LogUtils.w(TAG, "Pending dimensions out of bounds, using defaults. View: " + viewWidth + "x" + viewHeight + ", Pending: " + pendingX + "," + pendingY + "," + pendingWidth + "x" + pendingHeight);
                // Fall back to defaults
                setDefaultCaptureZone(viewWidth, viewHeight);
            }
            // Clear pending dimensions after use
            pendingX = pendingY = pendingWidth = pendingHeight = -1;
        } else {
            // No pending dimensions, use defaults
            setDefaultCaptureZone(viewWidth, viewHeight);
        }
        notifyListener();
    }
    
    private void setDefaultCaptureZone(int viewWidth, int viewHeight) {
        float zoneWidth = viewWidth * DEFAULT_ZONE_WIDTH_RATIO;
        float zoneHeight = viewHeight * DEFAULT_ZONE_HEIGHT_RATIO;
        
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;
        
        captureZone.set(
            centerX - zoneWidth / 2f,
            centerY - zoneHeight / 2f,
            centerX + zoneWidth / 2f,
            centerY + zoneHeight / 2f
        );
        
        LogUtils.d(TAG, "Initialized default capture zone: " + captureZone.toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isVisible || captureZone.isEmpty()) {
            return;
        }
        
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        
        // Save canvas state
        int saveCount = canvas.saveLayer(0, 0, viewWidth, viewHeight, null);
        
        // Fill entire view with dim overlay
        canvas.drawRect(0, 0, viewWidth, viewHeight, dimPaint);
        
        // Clear the capture zone area to make it transparent
        canvas.drawRect(captureZone, clearPaint);
        
        // Restore canvas state
        canvas.restoreToCount(saveCount);
        
        // Draw border around capture zone
        canvas.drawRect(captureZone, zoneBorderPaint);
        
        // Draw corner anchors
        drawAnchors(canvas);
    }
    
    private void drawAnchors(Canvas canvas) {
        if (captureZone.isEmpty()) return;
        
        float halfAnchor = ANCHOR_SIZE / 2f;
        
        // Top-left anchor
        canvas.drawRect(
            captureZone.left - halfAnchor,
            captureZone.top - halfAnchor,
            captureZone.left + halfAnchor,
            captureZone.top + halfAnchor,
            anchorPaint
        );
        canvas.drawRect(
            captureZone.left - halfAnchor,
            captureZone.top - halfAnchor,
            captureZone.left + halfAnchor,
            captureZone.top + halfAnchor,
            anchorBorderPaint
        );
        
        // Top-right anchor
        canvas.drawRect(
            captureZone.right - halfAnchor,
            captureZone.top - halfAnchor,
            captureZone.right + halfAnchor,
            captureZone.top + halfAnchor,
            anchorPaint
        );
        canvas.drawRect(
            captureZone.right - halfAnchor,
            captureZone.top - halfAnchor,
            captureZone.right + halfAnchor,
            captureZone.top + halfAnchor,
            anchorBorderPaint
        );
        
        // Bottom-left anchor
        canvas.drawRect(
            captureZone.left - halfAnchor,
            captureZone.bottom - halfAnchor,
            captureZone.left + halfAnchor,
            captureZone.bottom + halfAnchor,
            anchorPaint
        );
        canvas.drawRect(
            captureZone.left - halfAnchor,
            captureZone.bottom - halfAnchor,
            captureZone.left + halfAnchor,
            captureZone.bottom + halfAnchor,
            anchorBorderPaint
        );
        
        // Bottom-right anchor
        canvas.drawRect(
            captureZone.right - halfAnchor,
            captureZone.bottom - halfAnchor,
            captureZone.right + halfAnchor,
            captureZone.bottom + halfAnchor,
            anchorPaint
        );
        canvas.drawRect(
            captureZone.right - halfAnchor,
            captureZone.bottom - halfAnchor,
            captureZone.right + halfAnchor,
            captureZone.bottom + halfAnchor,
            anchorBorderPaint
        );
    }
    
    private AnchorPosition getAnchorAtPosition(float x, float y) {
        float touchRadius = ANCHOR_TOUCH_RADIUS;
        
        // Check top-left anchor
        if (Math.abs(x - captureZone.left) < touchRadius && Math.abs(y - captureZone.top) < touchRadius) {
            return AnchorPosition.TOP_LEFT;
        }
        
        // Check top-right anchor
        if (Math.abs(x - captureZone.right) < touchRadius && Math.abs(y - captureZone.top) < touchRadius) {
            return AnchorPosition.TOP_RIGHT;
        }
        
        // Check bottom-left anchor
        if (Math.abs(x - captureZone.left) < touchRadius && Math.abs(y - captureZone.bottom) < touchRadius) {
            return AnchorPosition.BOTTOM_LEFT;
        }
        
        // Check bottom-right anchor
        if (Math.abs(x - captureZone.right) < touchRadius && Math.abs(y - captureZone.bottom) < touchRadius) {
            return AnchorPosition.BOTTOM_RIGHT;
        }
        
        return AnchorPosition.NONE;
    }
    
    private void resizeWithAnchor(float x, float y) {
        if (activeAnchor == AnchorPosition.NONE || originalZoneBeforeResize == null) {
            return;
        }
        
        RectF newZone = new RectF(originalZoneBeforeResize);
        
        switch (activeAnchor) {
            case TOP_LEFT:
                newZone.left = Math.min(x, newZone.right - getMinZoneSize());
                newZone.top = Math.min(y, newZone.bottom - getMinZoneSize());
                break;
            case TOP_RIGHT:
                newZone.right = Math.max(x, newZone.left + getMinZoneSize());
                newZone.top = Math.min(y, newZone.bottom - getMinZoneSize());
                break;
            case BOTTOM_LEFT:
                newZone.left = Math.min(x, newZone.right - getMinZoneSize());
                newZone.bottom = Math.max(y, newZone.top + getMinZoneSize());
                break;
            case BOTTOM_RIGHT:
                newZone.right = Math.max(x, newZone.left + getMinZoneSize());
                newZone.bottom = Math.max(y, newZone.top + getMinZoneSize());
                break;
        }
        
        // Apply maximum size constraints
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        float maxWidth = viewWidth * MAX_ZONE_SIZE_RATIO;
        float maxHeight = viewHeight * MAX_ZONE_SIZE_RATIO;
        
        if (newZone.width() > maxWidth) {
            if (activeAnchor == AnchorPosition.TOP_LEFT || activeAnchor == AnchorPosition.BOTTOM_LEFT) {
                newZone.left = newZone.right - maxWidth;
            } else {
                newZone.right = newZone.left + maxWidth;
            }
        }
        
        if (newZone.height() > maxHeight) {
            if (activeAnchor == AnchorPosition.TOP_LEFT || activeAnchor == AnchorPosition.TOP_RIGHT) {
                newZone.top = newZone.bottom - maxHeight;
            } else {
                newZone.bottom = newZone.top + maxHeight;
            }
        }
        
        // Apply boundary constraints
        constrainToBounds(newZone);
        
        // Update capture zone
        captureZone.set(newZone);
        invalidate();
        notifyListener();
    }
    
    private float getMinZoneSize() {
        return Math.min(getWidth(), getHeight()) * MIN_ZONE_SIZE_RATIO;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isVisible) {
            return super.onTouchEvent(event);
        }
        
        boolean handled = false;
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        
        // Always let scale detector process the event first
        boolean scaleHandled = scaleDetector.onTouchEvent(event);
        handled |= scaleHandled;
        
        // Debug logging for multi-touch events
        if (event.getPointerCount() > 1) {
            LogUtils.d(TAG, "Multi-touch event: action=" + action + ", pointers=" + event.getPointerCount() + ", scaleHandled=" + scaleHandled + ", isScaling=" + isScaling);
        }
        
        // Process single-touch events when we're not actively scaling
        if (!isScaling && event.getPointerCount() == 1) {
            LogUtils.d(TAG, "Processing single-touch: action=" + action + ", isScaling=" + isScaling);
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Check if touch is on an anchor first
                activeAnchor = getAnchorAtPosition(x, y);
                if (activeAnchor != AnchorPosition.NONE) {
                    isResizingWithAnchor = true;
                    originalZoneBeforeResize = new RectF(captureZone);
                    LogUtils.d(TAG, "Started anchor resize with: " + activeAnchor);
                    return true;
                }
                
                // Check if touch is within capture zone for dragging
                if (captureZone.contains(x, y)) {
                    isPanning = true;
                    lastTouchX = x;
                    lastTouchY = y;
                    dragStartX = x;
                    dragStartY = y;
                    LogUtils.d(TAG, "Started drag at: " + x + "," + y);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (isResizingWithAnchor && activeAnchor != AnchorPosition.NONE) {
                    resizeWithAnchor(x, y);
                    return true;
                } else if (isPanning) {
                    // Handle direct drag movement
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;
                    
                    // Move capture zone
                    RectF newZone = new RectF(captureZone);
                    newZone.offset(deltaX, deltaY);
                    
                    // Apply boundary constraints
                    constrainToBounds(newZone);
                    
                    // Update capture zone
                    captureZone.set(newZone);
                    
                    // Update last touch position
                    lastTouchX = x;
                    lastTouchY = y;
                    
                    invalidate();
                    notifyListener();
                    
                    LogUtils.d(TAG, "Dragged zone by: " + deltaX + "," + deltaY);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isResizingWithAnchor) {
                    isResizingWithAnchor = false;
                    activeAnchor = AnchorPosition.NONE;
                    originalZoneBeforeResize = null;
                    LogUtils.d(TAG, "Ended anchor resize");
                    return true;
                } else if (isPanning) {
                    isPanning = false;
                    LogUtils.d(TAG, "Ended drag");
                    return true;
                }
                // Reset other gesture states when ending single touch
                break;
            }
        }
        
        // Handle final touch events
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // Only reset non-scaling states for single-touch end
            if (event.getPointerCount() == 1) {
                isPanning = false;
                isResizingWithAnchor = false;
                activeAnchor = AnchorPosition.NONE;
                originalZoneBeforeResize = null;
            }
        }
        
        // Reset scaling state on all pointers up (handled by ScaleGestureDetector)
        
        // Always return true to consume all touch events when visible
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // Allow scaling unless explicitly resizing with anchor
            if (isResizingWithAnchor) {
                LogUtils.d(TAG, "Scale gesture blocked by anchor resize");
                return false;
            }
            
            // Cancel any ongoing pan gesture
            isPanning = false;
            
            isScaling = true;
            LogUtils.d(TAG, "Scale gesture began");
            return true;
        }
        
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!isVisible || isResizingWithAnchor) return false;
            
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();
            
            // Calculate new size
            float currentWidth = captureZone.width();
            float currentHeight = captureZone.height();
            float newWidth = currentWidth * scaleFactor;
            float newHeight = currentHeight * scaleFactor;
            
            // Apply size constraints
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            float minSize = Math.min(viewWidth, viewHeight) * MIN_ZONE_SIZE_RATIO;
            float maxWidth = viewWidth * MAX_ZONE_SIZE_RATIO;
            float maxHeight = viewHeight * MAX_ZONE_SIZE_RATIO;
            
            newWidth = Math.max(minSize, Math.min(newWidth, maxWidth));
            newHeight = Math.max(minSize, Math.min(newHeight, maxHeight));
            
            // Scale around focus point
            float centerX = captureZone.centerX();
            float centerY = captureZone.centerY();
            
            // Calculate new bounds
            RectF newZone = new RectF(
                centerX - newWidth / 2f,
                centerY - newHeight / 2f,
                centerX + newWidth / 2f,
                centerY + newHeight / 2f
            );
            
            // Apply boundary constraints
            constrainToBounds(newZone);
            
            // Update capture zone
            captureZone.set(newZone);
            
            invalidate();
            notifyListener();
            
            LogUtils.d(TAG, "Scaled capture zone to: " + captureZone.toString());
            return true;
        }
        
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            LogUtils.d(TAG, "Scale gesture ended");
        }
    }


    private void constrainToBounds(RectF zone) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        
        // Ensure zone stays within view bounds
        if (zone.left < 0) {
            zone.offset(-zone.left, 0);
        }
        if (zone.top < 0) {
            zone.offset(0, -zone.top);
        }
        if (zone.right > viewWidth) {
            zone.offset(viewWidth - zone.right, 0);
        }
        if (zone.bottom > viewHeight) {
            zone.offset(0, viewHeight - zone.bottom);
        }
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onCaptureZoneChanged(new RectF(captureZone));
        }
    }

    // Public API methods
    
    public void setVisible(boolean visible) {
        int currentViewVisibility = getVisibility();
        LogUtils.d(TAG, "setVisible called with: " + visible + ", current isVisible: " + this.isVisible + ", view visibility: " + currentViewVisibility);
        
        // Always sync the states to ensure consistency
        this.isVisible = visible;
        int targetVisibility = visible ? VISIBLE : GONE;
        if (currentViewVisibility != targetVisibility) {
            setVisibility(targetVisibility);
            LogUtils.d(TAG, "View visibility changed from " + currentViewVisibility + " to " + targetVisibility);
        }
        invalidate();
        
        LogUtils.d(TAG, "Final state - isVisible: " + this.isVisible + ", view visibility: " + getVisibility());
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public RectF getCaptureZone() {
        return new RectF(captureZone);
    }
    
    public void setCaptureZone(RectF zone) {
        if (zone != null) {
            constrainToBounds(zone);
            this.captureZone.set(zone);
            invalidate();
            notifyListener();
            LogUtils.d(TAG, "Capture zone set to: " + captureZone.toString());
        }
    }
    
    public void setPendingDimensions(int x, int y, int width, int height) {
        this.pendingX = x;
        this.pendingY = y;
        this.pendingWidth = width;
        this.pendingHeight = height;
        LogUtils.d(TAG, "Set pending dimensions: " + x + "," + y + "," + width + "x" + height);
    }
    
    public void setCaptureZoneListener(CaptureZoneListener listener) {
        this.listener = listener;
    }
    
    public void resetCaptureZone() {
        initializeCaptureZone(getWidth(), getHeight());
        invalidate();
        LogUtils.d(TAG, "Capture zone reset");
    }
}