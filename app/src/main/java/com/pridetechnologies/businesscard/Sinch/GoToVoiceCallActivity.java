package com.pridetechnologies.businesscard.Sinch;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.R;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GoToVoiceCallActivity extends BaseActivity implements SinchService.StartFailedListener {

   // private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private TextView mCallState;
    private long mSigningSequence = 1;
    //ProgressDialog loadingBar;
    String currentUserID;
    String userid="";
    FirebaseUser fuser;
    DatabaseReference userRef;
    String receiverUserMobile;
    CheckNetwork checkNetwork;

    String receiverFirstName, receiverSurName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_voice_call);

        getApplicationContext().bindService(new Intent(GoToVoiceCallActivity.this, SinchService.class), GoToVoiceCallActivity.this,
                BIND_AUTO_CREATE);

        checkNetwork=new CheckNetwork(getApplicationContext());

        mCallState = (TextView) findViewById(R.id.cs);
        CircleImageView profileImageCalling = (CircleImageView) findViewById(R.id.voice_image_calling);

        Intent intent=getIntent();

        userid=intent.getStringExtra("userid");
        //Toast.makeText(this, userid, Toast.LENGTH_LONG).show();

        //loadingBar = new ProgressDialog(GoToVoiceCallActivity.this);

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID=fuser.getUid();
        //Toast.makeText(this, currentUserID, Toast.LENGTH_LONG).show();
        userRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("user_first_name")){

                        receiverFirstName=snapshot.child("user_first_name").getValue().toString();

                    }
                    if (snapshot.hasChild("user_surname")){

                        receiverSurName=snapshot.child("user_surname").getValue().toString();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        userRef.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("user_image")){

                        String receiverUserImage=snapshot.child("user_image").getValue().toString();

                        Picasso.get().load(receiverUserImage).fit().centerCrop().into(profileImageCalling);
                    }
                    /*if (snapshot.hasChild("user_first_name")){

                        receiverFirstName=snapshot.child("user_first_name").getValue().toString();

                        //mCallerName.setText(receiverUserName);
                    }
                    if (snapshot.hasChild("user_surname")){

                        receiverSurName=snapshot.child("user_surname").getValue().toString();

                    }
                    if (snapshot.hasChild("user_mobile")){

                        receiverUserMobile=snapshot.child("user_mobile").getValue().toString();

                        //mCallerName.setText(receiverUserName);
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Dexter.withContext(GoToVoiceCallActivity.this)
                .withPermissions(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                            new CountDownTimer(2000,1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {

                                    //loadingBar.setTitle("Voice Call");
                                    //loadingBar.setMessage("Please wait, while we are configuring voice call...");
                                    //loadingBar.setCanceledOnTouchOutside(false);
                                    //loadingBar.show();

                                    //FirebaseApp.initializeApp(this);
                                    makeCall();

                                }
                            }.start();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(
                        new PermissionRequestErrorListener() {
                            @Override
                            public void onError(DexterError error) {
                                Toast.makeText(GoToVoiceCallActivity.this, "Error occurred! ", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .onSameThread()
                .check();

    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    public void onStartFailed(SinchError error) {
        appCallingFailed();
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        //loadingBar.dismiss();
    }

    @Override
    public void onStarted() {
        //makeCall();
    }

    private void startClientAndMakeCall() {
        // start Sinch Client, it'll result onStarted() callback from where the place call activity will be started
        if (!getSinchServiceInterface().isStarted()) {
           // getSinchServiceInterface().startClient();
            //Toast.makeText(this, "CLIENT STARTED", Toast.LENGTH_LONG).show();
        }

    }

    private void makeCall() {

        Call call;
        String callId;
        try {
            Map<String,String> voiceHeaders=new HashMap<String,String>();
            voiceHeaders.put("name", receiverFirstName+" "+receiverSurName);

            //Toast.makeText(this, "Caller : "+voiceHeaders, Toast.LENGTH_LONG).show();
            call=getSinchServiceInterface().callUser(userid, voiceHeaders);
            callId=call.getCallId();

            Intent voiceCallIntent;
            voiceCallIntent=new Intent(this, VoiceCallScreenActivity.class);
            voiceCallIntent.putExtra(SinchService.CALL_ID,callId);
            voiceCallIntent.putExtra("userid", userid);
            startActivity(voiceCallIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Can't make a call now"+e, Toast.LENGTH_LONG).show();
            appCallingFailed();
        }


    }

    private void appCallingFailed() {

        Dialog dialog2 = new Dialog(GoToVoiceCallActivity.this);
        dialog2.setContentView(R.layout.custom_call_failed_dialog);
        Button callButton= dialog2.findViewById(R.id.button56);
        Button cancelButton= dialog2.findViewById(R.id.button57);

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!receiverUserMobile.isEmpty())
                {
                    String xulNumber = String.format("tel: %s", receiverUserMobile);
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse(xulNumber));
                    Dexter.withContext(GoToVoiceCallActivity.this)
                            .withPermissions(Manifest.permission.CALL_PHONE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    // check if all permissions are granted
                                    if (report.areAllPermissionsGranted()) {
                                        dialog2.dismiss();
                                        startActivity(callIntent);
                                        finish();
                                    }
                                }
                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                               PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            })
                            .withErrorListener(
                                    new PermissionRequestErrorListener() {
                                        @Override
                                        public void onError(DexterError error) {
                                            Toast.makeText(GoToVoiceCallActivity.this, "Error occurred! ", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    })
                            .onSameThread()
                            .check();
                }else
                {
                    Toast.makeText(GoToVoiceCallActivity.this, "No Mobile Number", Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                finish();
            }
        });
        dialog2.setCancelable(true);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.show();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}