package com.pridetechnologies.businesscard;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

public class NewBusinessCardActivity extends AppCompatActivity {

    String BusinessName;
    private String UserID;
    private String cardKey=null;
    private TextView noteView;

    private ScrollView scrollView;
    private ConstraintLayout constraintLayout, constraintLayout2, constraintLayout3;

    private MaterialTextView user_profession, companyNameView;
    private CircleImageView  logoImageView;
    private MaterialButton sendBtn, refreshBtn, bioBtn;
    private ImageButton closeBtn;

    private ProgressBar pBar;
    private ProgressBar progressBar;
    private ConstraintLayout noInfo, yesInfo, sendingDone;

    private FirebaseAuth auth;

    private DatabaseReference CompanyRef, MyCardsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_business_card);

        cardKey = getIntent().getExtras().get("qr_string").toString();

        auth = FirebaseAuth.getInstance();
        UserID = auth.getCurrentUser().getUid();
        MyCardsRef = FirebaseDatabase.getInstance().getReference().child("My Business Cards");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

        pBar = (ProgressBar) findViewById(R.id.progressBar22);

        progressBar = (ProgressBar) findViewById(R.id.newBusinessCardProgressBar);

        //progressBar.setVisibility(ProgressBar.VISIBLE);

        scrollView = (ScrollView) findViewById(R.id.sv);
        constraintLayout = (ConstraintLayout) findViewById(R.id.cl);
        constraintLayout2 = (ConstraintLayout) findViewById(R.id.cl2);
        constraintLayout3 = (ConstraintLayout) findViewById(R.id.cl3);
        sendingDone = (ConstraintLayout) findViewById(R.id.cl8);
        bioBtn = (MaterialButton) findViewById(R.id.button34);
        refreshBtn = (MaterialButton) findViewById(R.id.button25);
        /*refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (haveNetwork()){
                    scrollView.setVisibility(ScrollView.VISIBLE);
                    constraintLayout.setVisibility(ConstraintLayout.GONE);
                    constraintLayout3.setVisibility(ConstraintLayout.GONE);
                } else if (!haveNetwork()) {
                    constraintLayout2.setVisibility(ConstraintLayout.VISIBLE);
                    scrollView.setVisibility(ScrollView.GONE);
                    constraintLayout3.setVisibility(ConstraintLayout.GONE);
                    Toast.makeText(NewBusinessCardActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                }

            }
        });*/

        noInfo = (ConstraintLayout) findViewById(R.id.noBusiness);
        yesInfo = (ConstraintLayout) findViewById(R.id.yesBusiness);


        closeBtn = (ImageButton) findViewById(R.id.imageButton2);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        logoImageView = (CircleImageView) findViewById(R.id.imageView2);
        user_profession = (MaterialTextView)findViewById(R.id.textView32);
        companyNameView = (MaterialTextView)findViewById(R.id.textView6);
        sendBtn = (MaterialButton) findViewById(R.id.button5);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
        noteView = (TextView)findViewById(R.id.textView63);

        scrollView.setVisibility(ScrollView.VISIBLE);
        constraintLayout.setVisibility(ConstraintLayout.GONE);
        constraintLayout3.setVisibility(ConstraintLayout.GONE);
        pBar.setVisibility(ProgressBar.GONE);

        /*if (haveNetwork()){
            scrollView.setVisibility(ScrollView.VISIBLE);
            constraintLayout.setVisibility(ConstraintLayout.GONE);
            constraintLayout3.setVisibility(ConstraintLayout.GONE);
            pBar.setVisibility(ProgressBar.INVISIBLE);

        } else if (!haveNetwork()) {
            pBar.setVisibility(ProgressBar.INVISIBLE);
            constraintLayout2.setVisibility(ConstraintLayout.VISIBLE);
            scrollView.setVisibility(ScrollView.GONE);
            constraintLayout3.setVisibility(ConstraintLayout.GONE);
        }*/

        getContents();

    }

    /*private boolean haveNetwork(){
        boolean have_WIFI= false;
        boolean have_MobileData = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo info:networkInfos){
            if (info.getTypeName().equalsIgnoreCase("WIFI"))if (info.isConnected())have_WIFI=true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE DATA"))if (info.isConnected())have_MobileData=true;
        }
        return have_WIFI||have_MobileData;
    }*/

    public void getContents() {

        CompanyRef.child(cardKey).addValueEventListener(new ValueEventListener() {
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
                        BusinessName = snapshot.child("business_name").getValue().toString();
                        companyNameView.setText(BusinessName);
                    }else {noInfo.setVisibility(View.VISIBLE);
                        yesInfo.setVisibility(View.GONE);
                    }
                    if ((snapshot.hasChild("business_location")))
                    {
                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                        String CompanDistrict = snapshot.child("business_district").getValue().toString();
                        String CompanyCountry = snapshot.child("business_country").getValue().toString();
                        user_profession.setText(CompanyArea+", "+CompanDistrict+", "+CompanyCountry);

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

        progressBar.setVisibility(ProgressBar.VISIBLE);

        final Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("type", "approved");

        MyCardsRef.child(UserID).child(cardKey).setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    MyCardsRef.child(cardKey).child(UserID).setValue(requestMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                scrollView.setVisibility(ScrollView.GONE);
                                sendingDone.setVisibility(ConstraintLayout.VISIBLE);
                            }
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                        }
                    });
                }else {progressBar.setVisibility(ProgressBar.INVISIBLE);}
            }
        });
    }

    public void closeBtn(View view) {
        finish();
    }
}