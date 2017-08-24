package com.rafao.codescanner.codescan;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.rafao.codescanner.R;
import com.rafao.codescanner.camera.CameraSourcePreview;
import com.rafao.codescanner.camera.GraphicOverlay;
import com.rafao.codescanner.dialog.DialogUtils;


public class CodeScanActivity extends AppCompatActivity implements com.rafao.codescanner.codescan.CameraResult, Runnable {

    private final int PERMISSIONS_REQUEST_CAMERA = 1;

    private CameraSourcePreview cameraView;
    private GraphicOverlay cameraOverlay, cameraOverlayTop, cameraOverlayBottom;

    private CameraSource cameraSource;

    private AlertDialog dialog;

    private Button buttonType;

    private final Handler handler = new Handler();
    private final int timer = 12000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_code_scan);

        loadComponents();
        loadActions();
    }

    private void loadComponents() {
        cameraView = (CameraSourcePreview) findViewById(R.id.camera_view);

        cameraOverlay = (GraphicOverlay) findViewById(R.id.camera_overlay);

        cameraOverlayTop = (GraphicOverlay) findViewById(R.id.camera_overlay_top);
        cameraOverlayTop.getBackground().setAlpha(150);

        cameraOverlayBottom = (GraphicOverlay) findViewById(R.id.camera_overlay_bottom);
        cameraOverlayBottom.getBackground().setAlpha(150);

        buttonType = (Button) findViewById(R.id.button_type);

        final Runnable runnable = this;
        dialog = DialogUtils.createInputDialog(
                this,
                getResources().getString(R.string.code_scan_dialog_prompt_title),
                getResources().getString(R.string.code_scan_dialog_prompt_hint),
                getResources().getString(R.string.action_submit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResultIntent(((EditText) dialog.findViewById(R.id.edittext_content)).getText().toString());
                        dialogInterface.dismiss();
                    }
                },
                getResources().getString(R.string.action_try_again),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        handler.postDelayed(runnable, timer);
                        startCameraSource();
                        dialogInterface.cancel();
                    }
                });
    }

    private void loadActions() {
        if (isVersionGreaterThanLollipop()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
                handler.postDelayed(this, timer);
            } else {
                requestCameraPermission();
            }
        } else {
            createCameraSource();
            handler.postDelayed(this, timer);
        }

        buttonType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }

    private void createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(cameraOverlay, this, this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.error_low_storage, Toast.LENGTH_LONG).show();
            }
        }

        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();
    }

    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            final int HANDLE_GMS = 9001;
            Dialog dialog = GoogleApiAvailability
                    .getInstance().getErrorDialog(this, code, HANDLE_GMS);
            dialog.show();
        }

        if (cameraSource != null) {
            try {
                cameraView.start(cameraSource, cameraOverlay);
            } catch (Exception e) {
                e.printStackTrace();
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.CAMERA) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    loadActions();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

        cameraOverlayTop.getBackground().setAlpha(150);
        cameraOverlayBottom.getBackground().setAlpha(150);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
        handler.removeCallbacks(this);

        cameraOverlayTop.getBackground().setAlpha(255);
        cameraOverlayBottom.getBackground().setAlpha(255);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }

        handler.removeCallbacks(this);
        cameraOverlayTop.getBackground().setAlpha(255);
        cameraOverlayBottom.getBackground().setAlpha(255);
    }

    @Override
    public void onResult(Barcode barcode) {
        if (!barcode.rawValue.isEmpty()) {
            if (barcode.rawValue.length() > 40)
                sendResultIntent(barcode.rawValue);
        } else {
            back();
        }

        //Toast.makeText(this, barcode.displayValue, Toast.LENGTH_LONG).show();
    }

    private void sendResultIntent(String value) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("item", value);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                back();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void back() {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    public void run() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public boolean isVersionGreaterThanLollipop() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
