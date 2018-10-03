package com.rafao.codescanner.codescan;

import com.google.android.gms.vision.barcode.Barcode;

interface CameraResult {
    void onResult(Barcode barcode);
}
