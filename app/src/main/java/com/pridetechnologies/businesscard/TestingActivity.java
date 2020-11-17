package com.pridetechnologies.businesscard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;
import com.pridetechnologies.businesscard.Sinch.CheckNetwork;
import com.pridetechnologies.businesscard.Sinch.SinchService;
import com.pridetechnologies.businesscard.Sinch.VoiceCallScreenActivity;
import com.pubnub.api.PubNub;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestingActivity extends BaseActivity implements SinchService.StartFailedListener {

    static final String JSON_EXCEPTION = "JSON Exception";
    static final String PUBNUB_EXCEPTION = "Pubnub Exception";
    private ImageButton mCallButton;
    private EditText mCallName;
    CheckNetwork checkNetwork;
    private  String strContactNum;
    TextView infoText;
    private ArrayList<String> users;
    static TextView tv_check_connection;
    private JSONArray hereNowUuids;
    boolean blnGotSearchData=false;
    public static Typeface fontFields,fontButton;
    private BroadcastReceiver mNetworkReceiver;
    int m=0;
    private PubNub pubnub;

    private FirebaseAuth mAuth;
    private DatabaseReference AdminRef;

    String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();

        //FirebaseApp.initializeApp(this);
         getApplicationContext().bindService(new Intent(this, SinchService.class), this,
          BIND_AUTO_CREATE);

        checkNetwork=new CheckNetwork(getApplicationContext());
        //fontFields = Typeface.createFromAsset(getAssets(), getResources()
                //.getString(R.string.font_RobotoLightItalic));
        //fontButton = Typeface.createFromAsset(getAssets(), getResources()
                //.getString(R.string.font_RobotoRegular));
        mCallName = (EditText) findViewById(R.id.callName);
        tv_check_connection=(TextView) findViewById(R.id.tv_check_connection);


        // ArrayAdapter<String> adapter = new ArrayAdapter<String>(PlaceActivity.this,
        //       android.R.layout.simple_list_item_1, LoginActivityForLoc.strArrData);
        //mCallName.setAdapter(adapter);
        mCallName.setTypeface(fontFields);
        infoText=(TextView)findViewById(R.id.infoText);
        infoText.setTypeface(fontFields);
        mCallButton = (ImageButton) findViewById(R.id.callButton);
        mCallButton.setEnabled(false);
        mCallButton.setOnClickListener(buttonClickListener);

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(buttonClickListener);

        // pubnub = new Pubnub("pub-c-c7e1a8f8-4f83-442c-9675-0842c205c7e6", "sub-c-83cf381a-3436-11e9-b681-be2e977db94e");
        //pubnub.setUUID(getSinchServiceInterface().getUserName());

    }



    @Override
    protected void onServiceConnected() {
        TextView username = (TextView) findViewById(R.id.loggedInName);
        username.setTypeface(fontFields);
        //pubnub.setUUID(getSinchServiceInterface().getUserName());
        //System.out.println("pubnubuserwhenservice_"+getSinchServiceInterface().getUserName());
        username.setText(getSinchServiceInterface().getUserName());
        mCallButton.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
       /* if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }*/
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //  pubnub.unsubscribe("calling_channel");
    }

    private void stopButtonClicked() {
        /*if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        finish();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void callButtonClicked() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastloc = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        Double longitude= lastloc.getLongitude();
        Double latitude= lastloc.getLatitude();
        System.out.println("lat lng"+latitude);

        Geocoder geocoder=new Geocoder(this, Locale.getDefault());
        List<Address> addresses=null;
        try{
            addresses=geocoder.getFromLocation(latitude,longitude,1);
        }catch (IOException e){
            e.printStackTrace();
        }

        Map<String,String> headers=new HashMap<String,String>();
        headers.put("location",addresses.get(0).getAddressLine(0));

        //String username="fPEDeXCjr2TfoCpJ8Z6aPJSXMNM2";
        String username="Dx6FH0qTxKdXZSMRkXdXDcyvCrw2";

        if(username.isEmpty()){
            Toast.makeText(this,"Please enter a user to call",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Call call = getSinchServiceInterface().callUser(username, headers);
            String callId = call.getCallId();
            //System.out.println("CallerIDD"+callId);
            Intent callScreen=new Intent(this, VoiceCallScreenActivity.class);
            callScreen.putExtra(SinchService.CALL_ID,callId);
            callScreen.putExtra("userid", username);
            startActivity(callScreen);
        }catch (Exception error)
        {
            Toast.makeText(this, "Can't make a call now"+error, Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener buttonClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.callButton:
                    callButtonClicked();
                    break;
                case R.id.stopButton:
                    stopButtonClicked();
                    break;
            }
        }
    };

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}