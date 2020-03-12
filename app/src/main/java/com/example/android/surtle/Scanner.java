package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity {

    private ZXingScannerView mScannerView; // Programmatically initialize the scanner view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mScannerView = new ZXingScannerView(this);
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
        }
    }

}
