/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rafao.codescanner.codescan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.rafao.codescanner.R;
import com.rafao.codescanner.camera.GraphicOverlay;


/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private final Context context;
    private final CameraResult cameraResult;
    private final GraphicOverlay graphicOverlay;

    BarcodeTrackerFactory(GraphicOverlay graphicOverlay, CameraResult cameraResult, Context context) {
        this.graphicOverlay = graphicOverlay;
        this.cameraResult = cameraResult;
        this.context = context;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(graphicOverlay, cameraResult, context);
        return new com.rafao.codescanner.codescan.GraphicTracker<>(graphicOverlay, graphic);
    }
}

/**
 * Graphic instance for rendering barcode position, size, and ID within an associated graphic
 * overlay view.
 */
class BarcodeGraphic extends TrackedGraphic<Barcode> {

    private static final int COLOR_CHOICES[] = new int[1];

    private static int currentColorIndex = 0;

    private final Paint rectPaint;
    private volatile Barcode barcode;

    private final CameraResult cameraResult;

    BarcodeGraphic(GraphicOverlay overlay, CameraResult cameraResult, Context context) {
        super(overlay);

        currentColorIndex = (currentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[currentColorIndex];

        rectPaint = new Paint();
        rectPaint.setColor(selectedColor);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(10.0f);

        this.cameraResult = cameraResult;
        COLOR_CHOICES[0] = ContextCompat.getColor(context, R.color.colorChoice);
    }

    /**
     * Updates the barcode instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateItem(Barcode barcode) {
        this.barcode = barcode;
        postInvalidate();
        cameraResult.onResult(barcode);
    }

    /**
     * Draws the barcode annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Barcode barcode = this.barcode;
        if (barcode == null) {
            return;
        }

        // Draws the bounding box around the barcode.
        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, rectPaint);
    }
}
