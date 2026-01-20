// Copyright 2025 Zebra Technologies Corporation and/or its affiliates. All rights reserved.
package com.zebra.ai_multibarcodes_capture.barcodedecoder;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.zebra.ai_multibarcodes_capture.GraphicOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * The BarcodeGraphic class extends the GraphicOverlay.Graphic class and is responsible for
 * rendering visual elements on a canvas to represent detected barcodes. This includes drawing
 * bounding boxes around detected barcodes and displaying the decoded text associated with each
 * barcode.
 *
 * This class utilizes the Android Canvas and Paint classes to perform drawing operations, and
 * it manages the rendering of both bounding boxes and text content for barcodes detected in a
 * given overlay.
 *
 * Usage:
 * - Instantiate the BarcodeGraphic with a reference to the GraphicOverlay, and lists of bounding
 *   boxes and decoded strings.
 * - The draw(Canvas) method is called to render the graphics on the screen.
 *
 * Dependencies:
 * - GraphicOverlay: A custom view component that manages the drawing of multiple graphics
 *   on top of a camera preview or other content.
 * - Android Paint and Canvas classes: Used to perform drawing operations.
 *
 * Note: This class is typically used in conjunction with a barcode detection system to visually
 * display the results of the detection process in an Android application.
 */
public class BarcodeGraphic extends GraphicOverlay.Graphic {
    private final Paint boxPaint;
    private final Paint contentRectPaint;
    private final Paint contentTextPaint;
    private final List<Rect> boundingBoxes = new ArrayList<>();
    private final List<Rect> contentRectBoxes = new ArrayList<>();
    private final List<String> decodedValues = new ArrayList<>();
    private final List<Integer> boxColors = new ArrayList<>();
    private final int contentPadding = 25;

    /**
     * Constructs a new BarcodeGraphic object, initializing the Paint objects used for drawing
     * and preparing the bounding boxes and decoded text for rendering.
     *
     * @param overlay The GraphicOverlay on which this graphic will be drawn.
     * @param boxes A list of Rect objects representing the bounding boxes of detected barcodes.
     * @param decodedStrings A list of strings representing the decoded content of each barcode.
     */
    public BarcodeGraphic(GraphicOverlay overlay, List<Rect> boxes, List<String> decodedStrings) {
        this(overlay, boxes, decodedStrings, null);
    }

    /**
     * Constructs a new BarcodeGraphic object with per-box colors, initializing the Paint objects
     * used for drawing and preparing the bounding boxes and decoded text for rendering.
     *
     * @param overlay The GraphicOverlay on which this graphic will be drawn.
     * @param boxes A list of Rect objects representing the bounding boxes of detected barcodes.
     * @param decodedStrings A list of strings representing the decoded content of each barcode.
     * @param colors A list of colors for each bounding box. If null, defaults to GREEN.
     */
    public BarcodeGraphic(GraphicOverlay overlay, List<Rect> boxes, List<String> decodedStrings, List<Integer> colors) {
        super(overlay);
        overlay.clear();

        // Initialize the paint for drawing bounding boxes
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f);

        // Initialize the paint for drawing content rectangles
        contentRectPaint = new Paint();
        contentRectPaint.setColor(Color.WHITE);
        contentRectPaint.setStyle(Paint.Style.FILL);
        contentRectPaint.setStrokeWidth(6f);

        // Initialize the paint for drawing text
        contentTextPaint = new Paint();
        contentTextPaint.setColor(Color.DKGRAY);
        contentTextPaint.setAlpha(255);
        contentTextPaint.setTextSize(36F);
        boundingBoxes.clear();

        // Populate bounding boxes if provided
        if (boxes != null) {
            boundingBoxes.addAll(boxes);
        }

        // Populate decoded values if provided
        decodedValues.clear();
        if (decodedStrings != null) {
            decodedValues.addAll(decodedStrings);
        }

        // Store colors for per-box coloring
        boxColors.clear();
        if (colors != null) {
            boxColors.addAll(colors);
        }

        // Calculate rectangles for the content text background
        contentRectBoxes.clear();
        for (int i = 0; i < boundingBoxes.size(); i++) {
            int textWidth = (int) contentTextPaint.measureText(decodedValues.get(i));
            contentRectBoxes.add(new Rect(
                    boundingBoxes.get(i).left,
                    boundingBoxes.get(i).bottom + contentPadding / 2,
                    boundingBoxes.get(i).left + textWidth + contentPadding * 2,
                    boundingBoxes.get(i).bottom + (int) contentTextPaint.getTextSize() + contentPadding
            ));
        }

        // Trigger a redraw of the overlay
        postInvalidate();
    }

    /**
     * Draws the bounding boxes and decoded text on the given Canvas.
     *
     * @param canvas The canvas on which to draw the graphic.
     */
    @Override
    public void draw(Canvas canvas) {
        // Draw bounding boxes with individual colors
        for (int i = 0; i < boundingBoxes.size(); i++) {
            if (i < boxColors.size()) {
                boxPaint.setColor(boxColors.get(i));
            } else {
                boxPaint.setColor(Color.GREEN); // Default fallback
            }
            canvas.drawRect(boundingBoxes.get(i), boxPaint);
        }

        // Draw the text content of the barcode
        for (int i = 0; i < decodedValues.size(); i++) {
            if (!decodedValues.get(i).trim().isEmpty()) {
                // Draw the rectangle for barcode content
                canvas.drawRect(contentRectBoxes.get(i), contentRectPaint);

                // Draw the text
                canvas.drawText(
                        decodedValues.get(i),
                        boundingBoxes.get(i).left + contentPadding,
                        boundingBoxes.get(i).bottom + contentPadding * 2,
                        contentTextPaint
                );
            }
        }
    }
}
