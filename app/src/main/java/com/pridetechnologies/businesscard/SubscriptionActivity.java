package com.pridetechnologies.businesscard;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;

public class SubscriptionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 20;
    AsyncHttpClient client;
    Button btnMakePayment;
    ProgressBar progressBar;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);


    }


}