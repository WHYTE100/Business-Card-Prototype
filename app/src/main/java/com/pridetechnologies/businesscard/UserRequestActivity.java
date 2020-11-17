package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRequestActivity extends AppCompatActivity{

    private ImageView playBtn = null;
    private ImageView stopBtn = null;
    private TextView playView, stopView;

    private MediaPlayer mp;



    String FirstName;
    private String UserID;
    private String cardKey=null;
    private TextView SummaryView;

    private ScrollView scrollView;
    private ConstraintLayout  requestSentLayout;

    private MaterialTextView nameView, user_profession, positionView, countryView, areaView, districtView, companyNameView, buildingNameView, companyStreetName;
    private CircleImageView imageView, logoImageView;
    private MaterialButton approveBtn, declineBtn, bioBtn;
    private ImageButton closeBtn;
    private ProgressBar progressBar;

    private MaterialCardView noteCard;

    private ConstraintLayout noInfo, yesInfo, sendingDone;

    private FirebaseAuth auth;
    private Dialog dialog;
    String file = null;

    private DatabaseReference userRef, CompanyRequestRef, CompanyRef, MyCardsRef, CardRequestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        progressBar = (ProgressBar) findViewById(R.id.userRequestProgressBar);


        scrollView = (ScrollView) findViewById(R.id.sv);
        noteCard = (MaterialCardView) findViewById(R.id.note_card);
        requestSentLayout = (ConstraintLayout) findViewById(R.id.cl88);
        bioBtn = (MaterialButton) findViewById(R.id.button34);
        declineBtn = (MaterialButton) findViewById(R.id.button35);
        approveBtn = (MaterialButton) findViewById(R.id.button36);

        noInfo = (ConstraintLayout) findViewById(R.id.noBusiness);
        yesInfo = (ConstraintLayout) findViewById(R.id.yesBusiness);

        playBtn = (ImageView) findViewById(R.id.imageButton10);
        stopBtn = (ImageView) findViewById(R.id.imageButton11);


        closeBtn = (ImageButton) findViewById(R.id.imageButton22);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        cardKey = getIntent().getExtras().get("key").toString();

        nameView = (MaterialTextView)findViewById(R.id.textView6);
        imageView = (CircleImageView) findViewById(R.id.imageView2);
        logoImageView = (CircleImageView) findViewById(R.id.circleImageView6);
        user_profession = (MaterialTextView)findViewById(R.id.textView32);
        companyNameView = (MaterialTextView)findViewById(R.id.textView14);
        companyStreetName = (MaterialTextView) findViewById(R.id.textView60);
        positionView = (MaterialTextView)findViewById(R.id.textView15);
        buildingNameView = (MaterialTextView)findViewById(R.id.textView21);
        areaView = (MaterialTextView)findViewById(R.id.textView23);
        SummaryView = (TextView)findViewById(R.id.textView78);
        districtView = (MaterialTextView)findViewById(R.id.textView24);
        countryView = (MaterialTextView)findViewById(R.id.textView25);

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Cards");
        CompanyRequestRef = FirebaseDatabase.getInstance().getReference().child("Businesses Teams");
        CardRequestRef = FirebaseDatabase.getInstance().getReference().child("Card Requests");

        userRef.child(UserID).child(cardKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("user_audio_note")))
                    {
                        noteCard.setVisibility(View.VISIBLE);
                        file = snapshot.child("user_audio_note").getValue().toString();

                    }
                }else {
                    noteCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAudioPlay();
            }
        });

        getContents();

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
                                    dialog = new Dialog(UserRequestActivity.this);
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
                                    Toast.makeText(UserRequestActivity.this.getApplicationContext(), "No Bio!", Toast.LENGTH_SHORT)
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
                    }else {
                        noInfo.setVisibility(View.VISIBLE);
                        yesInfo.setVisibility(View.GONE);
                    }

                    approveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final Dialog dialog = new Dialog(UserRequestActivity.this);
                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                            titleView.setText("Approve");
                            messageView.setText("Approve Card Request");
                            noBtn.setText("Cancel");
                            yesBtn.setText("Approve");
                            yesBtn.setOnClickListener(v ->  {
                                dialog.dismiss();
                                progressBar.setVisibility(ProgressBar.VISIBLE);

                                final Map<String, Object> requestMap = new HashMap<>();
                                requestMap.put("type", "approved");

                                MyCardsRef.child(cardKey).child(UserID).setValue(requestMap).addOnCompleteListener(task -> {
                                    if (task.isSuccessful())
                                    {
                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        final Dialog dialog2 = new Dialog(UserRequestActivity.this);
                                        dialog2.setContentView(R.layout.custom_dialog__message_layout);
                                        TextView titleView2 = dialog2.findViewById(R.id.dialogTitle);
                                        TextView messageView2 = dialog2.findViewById(R.id.dialogMessage);
                                        TextView yesBtn2 = dialog2.findViewById(R.id.dialogYesBtn);
                                        TextView noBtn2 = dialog2.findViewById(R.id.dialogNoBtn);
                                        titleView2.setText("Save "+FirstName+"'s Card");
                                        messageView2.setText("Do you want to save "+FirstName+"'s card to your personal cards");
                                        noBtn2.setText("Cancel");
                                        yesBtn2.setText("Save");
                                        yesBtn2.setOnClickListener(v2 -> {
                                            dialog2.dismiss();
                                            progressBar.setVisibility(ProgressBar.VISIBLE);
                                            MyCardsRef.child(UserID).child(cardKey).setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        CardRequestRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    userRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                                scrollView.setVisibility(View.GONE);
                                                                                requestSentLayout.setVisibility(View.VISIBLE);
                                                                                SummaryView.setText("Card has been approved");
                                                                            }else {progressBar.setVisibility(ProgressBar.INVISIBLE);;}
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                        });
                                        noBtn2.setOnClickListener(v2 -> {
                                            dialog2.dismiss();
                                            progressBar.setVisibility(ProgressBar.VISIBLE);
                                            CardRequestRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        userRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                                    scrollView.setVisibility(View.GONE);
                                                                    requestSentLayout.setVisibility(View.VISIBLE);
                                                                    SummaryView.setText("Card has been approved");
                                                                }else {progressBar.setVisibility(ProgressBar.INVISIBLE);;}
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                                        });
                                        dialog2.setCancelable(true);
                                        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog2.show();
                                    }else {progressBar.setVisibility(ProgressBar.INVISIBLE);}
                                });

                            });
                            noBtn.setOnClickListener(v -> dialog.dismiss());
                            dialog.setCancelable(true);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });

                    declineBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final Dialog dialog = new Dialog(UserRequestActivity.this);
                            dialog.setContentView(R.layout.custom_dialog__message_layout);
                            TextView titleView = dialog.findViewById(R.id.dialogTitle);
                            TextView messageView = dialog.findViewById(R.id.dialogMessage);
                            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
                            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
                            titleView.setText("Decline Card");
                            messageView.setText("Are you sure you want to decline this request?");
                            noBtn.setText("Cancel");
                            yesBtn.setText("Decline");
                            yesBtn.setOnClickListener(v -> {
                                progressBar.setVisibility(ProgressBar.VISIBLE);
                                CardRequestRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {

                                            userRef.child(UserID).child(cardKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                                                        scrollView.setVisibility(View.GONE);
                                                        requestSentLayout.setVisibility(View.VISIBLE);
                                                        SummaryView.setText("Card has been declined");
                                                    }else {progressBar.setVisibility(ProgressBar.INVISIBLE);}
                                                }
                                            });

                                        }
                                    }
                                });

                                dialog.dismiss();
                            });
                            noBtn.setOnClickListener(v -> dialog.dismiss());
                            dialog.setCancelable(true);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });
                }else
                {
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void stopAudioPlay()
    {
        if(null != mp){
            mp.stop();
            mp.reset();
            mp.release();
            playBtn.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);

        }
    }
    private void playAudio()
    {
        mp=new MediaPlayer();
        try{
            //you can change the path, here path is external directory(e.g. sdcard) /Music/maine.mp3
            mp.setDataSource(file);
            mp.prepare();
            mp.start();
        }catch(Exception e){e.printStackTrace();}
        playBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.VISIBLE);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        });

    }

    public void closeReqBtn(View view) {
        finish();
    }
}