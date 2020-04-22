package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity {

    private ZXingScannerView mScannerView; // Programmatically initialize the scanner view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        verifyPermission();
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scanner);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }

    public void scanButtonClicked(View view){
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(new ZXingScannerResultHandler()); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    class ZXingScannerResultHandler implements ZXingScannerView.ResultHandler {
        @Override
        public void handleResult(Result rawResult) {

            // Do something with the result here
            Log.d("ScanResult", rawResult.getText()); // Prints scan results
            Log.d("ScanResult", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
            onPause();//Pause Camera
            setContentView(R.layout.activity_scanner);//Go back to activity scanner

            // If you would like to resume scanning, call this method below:
    //        mScannerView.resumeCameraPreview(this);*/

            //start Profile Activity with scanResult = [scanResult]
            Intent i = new Intent(getApplicationContext(), rate.class);
            i.putExtra("scanResult", rawResult.getText());
            startActivity(i);
            finish();

            //access result on next activity with
            //String result = getIntent().getStringExtra("scanResult");
        }
    }

    //verify camera permission
    private void verifyPermission() {
        Log.d("TAG", "verifyPermission: asking user for permissions");
        String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }
}
