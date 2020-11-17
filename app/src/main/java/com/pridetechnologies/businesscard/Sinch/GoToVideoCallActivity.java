package com.pridetechnologies.businesscard.Sinch;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GoToVideoCallActivity extends BaseActivity implements SinchService.StartFailedListener{

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_to_video_call);
        getApplicationContext().bindService(new Intent(GoToVideoCallActivity.this, SinchService.class), GoToVideoCallActivity.this,
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
        userRef.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild("user_image")){

                        String receiverUserImage=snapshot.child("user_image").getValue().toString();

                        Picasso.get().load(receiverUserImage).fit().centerCrop().into(profileImageCalling);
                    }
                    if (snapshot.hasChild("user_first_name")){

                        String receiverUserName=snapshot.child("user_first_name").getValue().toString();

                        //mCallerName.setText(receiverUserName);
                    }
                    if (snapshot.hasChild("user_mobile")){

                        receiverUserMobile=snapshot.child("user_mobile").getValue().toString();

                        //mCallerName.setText(receiverUserName);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Dexter.withContext(GoToVideoCallActivity.this)
                .withPermissions(Manifest.permission.CAMERA,Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO)
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
                                Toast.makeText(GoToVideoCallActivity.this, "Error occurred! ", Toast.LENGTH_SHORT)
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
            call=getSinchServiceInterface().callVideo(userid);
            callId=call.getCallId();

            Intent voiceCallIntent;
            voiceCallIntent=new Intent(this, VideoCallScreenActivity.class);
            voiceCallIntent.putExtra(SinchService.CALL_ID,callId);
            voiceCallIntent.putExtra("userid", userid);
            startActivity(voiceCallIntent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Can't make a call now"+e, Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}