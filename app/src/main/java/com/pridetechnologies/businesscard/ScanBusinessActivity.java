package com.pridetechnologies.businesscard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanBusinessActivity extends AppCompatActivity {

    private IntentIntegrator qrScan, qrScan2;
    IntentResult result, result2;
    private String cardKey=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_business);

        qrScan = new IntentIntegrator(ScanBusinessActivity.this);
        qrScan.setBeepEnabled(true);
        qrScan.setOrientationLocked(false);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        qrScan.setPrompt("Scan QR Code");
        qrScan.setCameraId(0);  // Use a specific camera of the device
        qrScan.setBeepEnabled(true);
        qrScan.setBarcodeImageEnabled(true);
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null && resultCode == RESULT_OK && data != null) {

            if(result.getContents() == null) {

                finish();
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                cardKey = result.getContents().toString().trim();
                Intent intent= new Intent(ScanBusinessActivity.this, NewBusinessCardActivity.class);
                intent.putExtra("qr_string",cardKey);
                startActivity(intent);
                finish();
                Animatoo.animateFade(ScanBusinessActivity.this);

            }

        } else{
            super.onActivityResult(requestCode, resultCode, data);
            finish();

        }
    }
}