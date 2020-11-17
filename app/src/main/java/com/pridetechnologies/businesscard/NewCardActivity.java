package com.pridetechnologies.businesscard;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dewarder.holdinglibrary.HoldingButtonLayout;
import com.dewarder.holdinglibrary.HoldingButtonLayoutListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.pridetechnologies.businesscard.SendNotificationPack.APIService;
import com.pridetechnologies.businesscard.SendNotificationPack.Client;
import com.pridetechnologies.businesscard.SendNotificationPack.Data;
import com.pridetechnologies.businesscard.SendNotificationPack.MyResponse;
import com.pridetechnologies.businesscard.SendNotificationPack.NotificationSender;
import com.pridetechnologies.businesscard.SendNotificationPack.Token;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewCardActivity extends AppCompatActivity implements HoldingButtonLayoutListener {

    private static final DateFormat mFormatter = new SimpleDateFormat("ss:SS");
    private static final float SLIDE_TO_CANCEL_ALPHA_MULTIPLIER = 2.5f;
    private static final long TIME_INVALIDATION_FREQUENCY = 50L;

    private HoldingButtonLayout mHoldingButtonLayout;
    //private EditText mInput;
    //private View mSlideToCancel;

    private ImageView playBtn, stopBtn;
    private TextView mTime, statusView, skipBtn, recordView, playView, stopView;
   // private Chronometer myChronometer;

    private int mAnimationDuration;
    private ViewPropertyAnimator mTimeAnimator;
    private ViewPropertyAnimator mSlideToCancelAnimator;
    private ViewPropertyAnimator mInputAnimator;

    private long mStartTime;
    private Runnable mTimerRunnable;

    private MediaRecorder mediaRecorder;

    public static String fileName = "recorded.3gp";

    String file = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName;

    String FirstName;
    private String UserID;
    private String cardKey=null;
    private TextView noteView;
    private Button saveDialog;

    private ScrollView scrollView;
    private ConstraintLayout constraintLayout, constraintLayout2, constraintLayout3;

    private MaterialTextView nameView, user_profession, positionView, countryView, areaView, districtView, companyNameView, buildingNameView, companyStreetName;
    private CircleImageView imageView, logoImageView;
    private MaterialButton sendBtn, refreshBtn, bioBtn;
    private ImageButton closeBtn;

    private ProgressBar pBar;
    private ProgressBar progressBar;
    private ConstraintLayout noInfo, yesInfo, sendingDone;

    private FirebaseAuth auth;
    private Dialog dialog;
    private Dialog dialog2;
    private MediaPlayer mediaPlayer;

    private APIService apiService;
    boolean notify = false;

    private StorageReference storage;
    private DatabaseReference userRef, requestRef, CompanyRef, MyCardsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        mAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        scrollView = (ScrollView) findViewById(R.id.sv);
        constraintLayout = (ConstraintLayout) findViewById(R.id.cl);
        constraintLayout2 = (ConstraintLayout) findViewById(R.id.cl2);
        constraintLayout3 = (ConstraintLayout) findViewById(R.id.cl3);
        sendingDone = (ConstraintLayout) findViewById(R.id.cl8);
        bioBtn = (MaterialButton) findViewById(R.id.button34);
        refreshBtn = (MaterialButton) findViewById(R.id.button25);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        noInfo = (ConstraintLayout) findViewById(R.id.noBusiness);
        yesInfo = (ConstraintLayout) findViewById(R.id.yesBusiness);

        pBar = (ProgressBar) findViewById(R.id.progressBar2);
        //pBar.setVisibility(ProgressBar.VISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.newCardProgressBar);


        closeBtn = (ImageButton) findViewById(R.id.imageButton2);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cardKey = getIntent().getExtras().get("qr_string").toString();

        nameView = (MaterialTextView)findViewById(R.id.textView6);
        imageView = (CircleImageView) findViewById(R.id.imageView2);
        logoImageView = (CircleImageView) findViewById(R.id.circleImageView6);
        user_profession = (MaterialTextView)findViewById(R.id.textView32);
        companyNameView = (MaterialTextView)findViewById(R.id.textView14);
        companyStreetName = (MaterialTextView) findViewById(R.id.textView60);
        positionView = (MaterialTextView)findViewById(R.id.textView15);
        buildingNameView = (MaterialTextView)findViewById(R.id.textView21);
        areaView = (MaterialTextView)findViewById(R.id.textView23);
        districtView = (MaterialTextView)findViewById(R.id.textView24);
        countryView = (MaterialTextView)findViewById(R.id.textView25);
        sendBtn = (MaterialButton) findViewById(R.id.button5);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
        noteView = (TextView)findViewById(R.id.textView63);


        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        requestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Cards");

        scrollView.setVisibility(ScrollView.VISIBLE);
        constraintLayout.setVisibility(ConstraintLayout.GONE);
        constraintLayout3.setVisibility(ConstraintLayout.GONE);
        pBar.setVisibility(ProgressBar.INVISIBLE);

        /*if (haveNetwork()){
            scrollView.setVisibility(ScrollView.VISIBLE);
            constraintLayout.setVisibility(ConstraintLayout.GONE);
            constraintLayout3.setVisibility(ConstraintLayout.GONE);
            pBar.setVisibility(ProgressBar.INVISIBLE);
            //Toast.makeText(ProfileActivity.this, "Network connection is available", Toast.LENGTH_LONG).show();
        } else if (!haveNetwork()) {
            pBar.setVisibility(ProgressBar.INVISIBLE);
            constraintLayout2.setVisibility(ConstraintLayout.VISIBLE);
            scrollView.setVisibility(ScrollView.GONE);
            constraintLayout3.setVisibility(ConstraintLayout.GONE);
            //Toast.makeText(ProfileActivity.this, "Network connection is not available", Toast.LENGTH_LONG).show();
        }*/

        getContents();

        if (cardKey.equals(UserID))
        {
            sendBtn.setVisibility(View.GONE);
            noteView.setText("This is your Card.");
        }
        UpdateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void UpdateToken( String refreshToken) {

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1= new Token(refreshToken);
        ref.child(firebaseUser.getUid()).setValue(token1);
    }

    public void getContents() {

        userRef.child(cardKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("user_image"))
                    {
                        String Image = snapshot.child("user_image").getValue().toString();
                        Picasso.get().load(Image).placeholder(R.mipmap.user_gold).into(imageView);
                    }
                    if ((snapshot.hasChild("user_first_name")))
                    {
                        FirstName = snapshot.child("user_first_name").getValue().toString();
                        String OtherNames= snapshot.child("user_other_names").getValue().toString();
                        String Surname= snapshot.child("user_surname").getValue().toString();
                        nameView.setText(FirstName+" "+OtherNames+" "+Surname);
                        bioBtn.setText(FirstName+"'s Bio");
                        bioBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (snapshot.hasChild("user_bio"))
                                {

                                    String BIO= snapshot.child("user_bio").getValue().toString();
                                    dialog = new Dialog(NewCardActivity.this);
                                    dialog.setContentView(R.layout.custom_user_bio);
                                    TextView nameView = (TextView) dialog.findViewById(R.id.textView65);
                                    nameView.setText(FirstName+"'s Bio");
                                    final TextView bioView = (TextView) dialog.findViewById(R.id.textView66);
                                    bioView.setText(BIO);
                                    Button cancelDialog  = (Button) dialog.findViewById(R.id.button29);
                                    cancelDialog.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.setCancelable(true);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.show();

                                }else {
                                    Toast.makeText(NewCardActivity.this.getApplicationContext(), "No Bio!", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });


                    }
                    if ((snapshot.hasChild("user_profession")))
                    {
                        String Profession = snapshot.child("user_profession").getValue().toString();
                        user_profession.setText(Profession);

                    }
                    if ((snapshot.hasChild("user_position")))
                    {
                        String Position = snapshot.child("user_position").getValue().toString();
                        positionView.setText(Position);

                    }
                    if (snapshot.hasChild("company_key"))
                    {

                        noInfo.setVisibility(View.GONE);
                        yesInfo.setVisibility(View.VISIBLE);

                        String CompanyId = snapshot.child("company_key").getValue().toString();
                        CompanyRef.child(CompanyId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {

                                    if ((snapshot.hasChild("business_logo")))
                                    {
                                        String Image = snapshot.child("business_logo").getValue().toString();

                                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(logoImageView);
                                    }
                                    if ((snapshot.hasChild("business_name")))
                                    {
                                        String CompanyName = snapshot.child("business_name").getValue().toString();
                                        companyNameView.setText(CompanyName);
                                    }else {noInfo.setVisibility(View.VISIBLE);
                                        yesInfo.setVisibility(View.GONE);}
                                    if ((snapshot.hasChild("business_building")))
                                    {
                                        String CompanDistrict = snapshot.child("business_building").getValue().toString();
                                        buildingNameView.setText(CompanDistrict);

                                    }
                                    if ((snapshot.hasChild("business_street")))
                                    {
                                        String CompanyStreet = snapshot.child("business_street").getValue().toString();

                                        companyStreetName.setText(CompanyStreet);

                                    }
                                    if ((snapshot.hasChild("business_location")))
                                    {
                                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                                        areaView.setText(CompanyArea);

                                    }
                                    if ((snapshot.hasChild("business_district")))
                                    {
                                        String CompanDistrict = snapshot.child("business_district").getValue().toString();
                                        districtView.setText(CompanDistrict);

                                    }
                                    if ((snapshot.hasChild("business_mobile")))
                                    {
                                        String CompanyName = snapshot.child("business_mobile").getValue().toString();
                                        //user_profession.setText(Profession);

                                    }
                                    if ((snapshot.hasChild("business_whatsapp")))
                                    {
                                        String CompanyName = snapshot.child("business_whatsapp").getValue().toString();
                                        //user_profession.setText(Profession);

                                    }
                                    if ((snapshot.hasChild("business_country")))
                                    {
                                        String CompanyCountry = snapshot.child("business_country").getValue().toString();
                                        countryView.setText(CompanyCountry);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        MyCardsRef.child(UserID).child(cardKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    sendBtn.setVisibility(View.GONE);
                                    noteView.setText("You already have this Card.");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        requestRef.child(UserID).child(cardKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    sendBtn.setVisibility(View.GONE);
                                    noteView.setText("You have already requested for this Card.");
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else {
                        noInfo.setVisibility(View.VISIBLE);
                        yesInfo.setVisibility(View.GONE);
                    }
                }else
                    {
                        constraintLayout3.setVisibility(ConstraintLayout.VISIBLE);
                        constraintLayout2.setVisibility(ConstraintLayout.GONE);
                        scrollView.setVisibility(ScrollView.GONE);
                        constraintLayout.setVisibility(ConstraintLayout.VISIBLE);
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendRequest() {

        dialog2 = new Dialog(NewCardActivity.this);
        dialog2.setContentView(R.layout.custom_user_request);
        mHoldingButtonLayout = dialog2.findViewById(R.id.input_holder);
        mHoldingButtonLayout.addListener(NewCardActivity.this);


        TextView nameView = (TextView) dialog2.findViewById(R.id.textView83);
        nameView.setText("Before sending the request ,let "+FirstName+" know why you want the card. PRESS and HOLD the button to record your message or skip if not necessary.");
        playBtn  = (ImageView) dialog2.findViewById(R.id.imageView20);
        mTime  = (TextView) dialog2.findViewById(R.id.textView79);
        //myChronometer = (Chronometer)dialog2.findViewById(R.id.chronometer);
        //myChronometer.setTextColor(Color.BLUE);
        statusView  = (TextView) dialog2.findViewById(R.id.status_view);
        skipBtn  = (TextView) dialog2.findViewById(R.id.skipBtn);
        recordView  = (TextView) dialog2.findViewById(R.id.textView84);
        playView  = (TextView) dialog2.findViewById(R.id.textView85);
        stopView  = (TextView) dialog2.findViewById(R.id.textView86);
        stopBtn  = (ImageView) dialog2.findViewById(R.id.imageView21);
        saveDialog  = (Button) dialog2.findViewById(R.id.saveBtn);
        saveDialog.setVisibility(Button.GONE);
        skipBtn.setVisibility(Button.VISIBLE);
        playBtn.setVisibility(ImageView.GONE);
        stopView.setVisibility(ImageView.GONE);
        recordView.setVisibility(TextView.VISIBLE);
        playView.setVisibility(TextView.GONE);
        stopBtn.setVisibility(TextView.GONE);

        mTime.setTextColor(Color.BLUE);
        saveDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog2.dismiss();
                progressBar.setVisibility(ProgressBar.VISIBLE);

                Uri uploadUri = Uri.fromFile(new File(file));

                StorageReference filepath = storage.child("Request Note Audios/" + "note"
                        + "."+ getFileExtension(uploadUri));

                filepath.putFile(uploadUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> downloadUrl = taskSnapshot.getStorage().getDownloadUrl();
                        while (!downloadUrl.isComplete());
                        Uri profilePhotoUri = downloadUrl.getResult();

                        final Map<String, Object > typeMap = new HashMap<>();
                        typeMap.put("type", "sent");

                        final Map<String, Object > typeMap2 = new HashMap<>();
                        typeMap2.put("type", "received");

                        Map<String, Object> usersMap = new HashMap<>();
                        usersMap.put("user_audio_note", profilePhotoUri.toString().trim());

                        userRef.child(cardKey).child(UserID).setValue(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    notify=true;
                                    requestRef.child(cardKey).child(UserID).setValue(typeMap2)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        FirebaseDatabase.getInstance().getReference().child("Tokens").child(cardKey).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists())
                                                                {
                                                                    if (notify)
                                                                    {
                                                                        String usertoken = dataSnapshot.getValue(String.class);
                                                                        sendNotifications(usertoken, "Card Request",FirstName+" has sent you a card request");
                                                                    }
                                                                }
                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                scrollView.setVisibility(ScrollView.GONE);
                                                                sendingDone.setVisibility(ConstraintLayout.VISIBLE);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                                /*userRef.child(UserID).addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                        if (notify)
                                                                        {
                                                                            sendNotifications(cardKey, "Card Request",FirstName+" has sent you a card request");

                                                                        }
                                                                        waitingDialog.dismiss();
                                                                        scrollView.setVisibility(ScrollView.GONE);
                                                                        sendingDone.setVisibility(ConstraintLayout.VISIBLE);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });*/
                                                    }else {
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(NewCardActivity.this,"Failed to send request. Please send again and make sure you have an internet connection!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        }
                                    });
                                }
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewCardActivity.this,"Failed : "+e, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });
            }
        });
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog2.dismiss();
                progressBar.setVisibility(ProgressBar.VISIBLE);
                final Map<String, Object > typeMap = new HashMap<>();
                typeMap.put("type", "sent");

                final Map<String, Object > typeMap2 = new HashMap<>();
                typeMap2.put("type", "received");


                requestRef.child(cardKey).child(UserID).setValue(typeMap2)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    notify=true;
                                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(cardKey).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists())
                                            {
                                                if (notify)
                                                {
                                                    String usertoken = dataSnapshot.getValue(String.class);
                                                    sendNotifications(usertoken, "Card Request", FirstName+" has sent you a card request");
                                                }
                                            }
                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                            scrollView.setVisibility(ScrollView.GONE);
                                            sendingDone.setVisibility(ConstraintLayout.VISIBLE);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        }
                                    });
                                            /*userRef.child(UserID).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (notify)
                                                    {
                                                        sendNotifications(cardKey, "Card Request",FirstName+" has sent you a card request");
                                                    }
                                                    waitingDialog.dismiss();
                                                    scrollView.setVisibility(ScrollView.GONE);
                                                    sendingDone.setVisibility(ConstraintLayout.VISIBLE);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });*/

                                }else {
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        dialog2.setCancelable(true);
                        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog2.show();
                    }
                });
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
                //myChronometer.setVisibility(View.VISIBLE);
                //myChronometer.start();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudioPlay();
                //myChronometer.stop();
            }
        });
        dialog2.setCancelable(true);
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog2.show();

    }

    private void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data,usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200)
                {
                    if (response.body().success != 1)
                    {
                        //Toast.makeText(NewCardActivity.this,"Notification Failed", Toast.LENGTH_SHORT).show();

                    }else
                        {
                            //Toast.makeText(NewCardActivity.this,"Notification Sent", Toast.LENGTH_SHORT).show();

                        }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    public void closeBtn(View view) {
        finish();
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
        }
    };

    private void record()
    {
       // myChronometer.setVisibility(View.GONE);
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(file);
        mediaRecorder.setOnErrorListener(errorListener);
        mediaRecorder.setOnInfoListener(infoListener);
        recordView.setVisibility(TextView.GONE);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusView.setText("Recording");
        statusView.setTextColor(Color.BLUE);
    }
    private void stopAudio()
    {
        if(null != mediaRecorder){

            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder.reset();

                mediaRecorder = null;
            } catch(RuntimeException stopException) {
                // handle cleanup here
            }

        }
        statusView.setText("Recording Completed");
        statusView.setTextColor(Color.BLUE);
    }

    private void stopAudioPlay()
    {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            statusView.setText("Play Stopped");
        }
        statusView.setText("Play Stopped");
    }

    private void stopPlay(){
        mediaPlayer.stop();
        playView.setVisibility(TextView.VISIBLE);
        playBtn.setVisibility(TextView.VISIBLE);
        stopBtn.setVisibility(TextView.GONE);
        stopView.setVisibility(ImageView.GONE);
    }
    private void play()
    {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(file);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusView.setText("Playing Audio");
        statusView.setTextColor(Color.BLUE);
        stopView.setVisibility(ImageView.VISIBLE);
        recordView.setVisibility(TextView.VISIBLE);
        playView.setVisibility(TextView.VISIBLE);
        playBtn.setVisibility(TextView.VISIBLE);
        stopBtn.setVisibility(TextView.VISIBLE);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                statusView.setText("Play Again");
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
    }

    private void showSettingsDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage(
                "This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                NewCardActivity.this.openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onBeforeExpand() {

        cancelAllAnimations();

        mTime.setTranslationY(mTime.getHeight());
        mTime.setAlpha(0f);
        mTime.setVisibility(View.VISIBLE);
        stopView.setVisibility(ImageView.GONE);
        recordView.setVisibility(TextView.GONE);
        playView.setVisibility(TextView.GONE);
        stopView.setVisibility(TextView.GONE);
        mTimeAnimator = mTime.animate().translationY(0f).alpha(1f).setDuration(mAnimationDuration);
        mTimeAnimator.start();


    }

    @Override
    public void onExpand() {

        Dexter.withActivity(NewCardActivity.this)
                .withPermissions(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            stopView.setVisibility(ImageView.GONE);
                            recordView.setVisibility(TextView.GONE);
                            playView.setVisibility(TextView.GONE);
                            stopBtn.setVisibility(TextView.GONE);
                            mStartTime = System.currentTimeMillis();
                            invalidateTimer();
                            record();

                        }else {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
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
                                Toast.makeText(NewCardActivity.this.getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .onSameThread()
                .check();

    }


    @Override
    public void onBeforeCollapse() {

        stopView.setVisibility(ImageView.GONE);
        recordView.setVisibility(TextView.GONE);
        playView.setVisibility(TextView.GONE);
        stopBtn.setVisibility(TextView.GONE);
        cancelAllAnimations();

        mTimeAnimator = mTime.animate().translationY(mTime.getHeight()).alpha(0f).setDuration(mAnimationDuration);
        mTimeAnimator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTime.setVisibility(View.INVISIBLE);
                mTimeAnimator.setListener(null);
            }
        });
        mTimeAnimator.start();
    }

    @Override
    public void onCollapse(boolean isCancel) {
        stopTimer();
        stopAudio();
        if (isCancel) {
            Toast.makeText(this, "Action canceled! Time " + getFormattedTime(), Toast.LENGTH_SHORT).show();
        } else {

            mTime.setText(getFormattedTime().toString().trim());
            saveDialog.setVisibility(Button.VISIBLE);
            skipBtn.setVisibility(Button.VISIBLE);
            playBtn.setVisibility(ImageView.VISIBLE);
            stopView.setVisibility(ImageView.GONE);
            recordView.setVisibility(TextView.VISIBLE);
            playView.setVisibility(TextView.VISIBLE);
            stopBtn.setVisibility(TextView.GONE);
            statusView.setText("Recording Completed");

        }
    }

    @Override
    public void onOffsetChanged(float offset, boolean b) {

    }

    private void invalidateTimer() {
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                mTime.setText(getFormattedTime());
                invalidateTimer();
            }
        };

        mTime.postDelayed(mTimerRunnable, TIME_INVALIDATION_FREQUENCY);
    }

    private void stopTimer() {
        if (mTimerRunnable != null) {
            mTime.getHandler().removeCallbacks(mTimerRunnable);
        }
    }

    private void cancelAllAnimations() {
        if (mInputAnimator != null) {
            mInputAnimator.cancel();
        }

        if (mSlideToCancelAnimator != null) {
            mSlideToCancelAnimator.cancel();
        }

        if (mTimeAnimator != null) {
            mTimeAnimator.cancel();
        }
    }

    private String getFormattedTime() {
        return mFormatter.format(new Date(System.currentTimeMillis() - mStartTime));
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /*public void sendNotifications(String usertoken, String title, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(usertoken);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(UserID, message, title, usertoken, R.mipmap.card_round);

                    NotificationSender sender = new NotificationSender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }*/
}