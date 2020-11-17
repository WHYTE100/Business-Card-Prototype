package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class BusinessSocialMediaActivity extends AppCompatActivity {

    private MaterialButton fbBtn, instaBtn, twiBtn, linkBtn;

    private Dialog dialog;
    private String BusinessKey, FaceBook, Twitter, LinkedIn, Instagram;
    private MaterialTextView facebookView, linkedInView, twitterView, instagramiew;

    private FirebaseAuth auth;

    private DatabaseReference userRef;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_social_media);

        progressBar = (ProgressBar) findViewById(R.id.socialMediaProgressBar);


        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton7);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        facebookView = (MaterialTextView)findViewById(R.id.textView20);
        linkedInView = (MaterialTextView)findViewById(R.id.textView22);
        twitterView = (MaterialTextView)findViewById(R.id.textView21);
        instagramiew = (MaterialTextView)findViewById(R.id.textView23);

        fbBtn = (MaterialButton) findViewById(R.id.button18);
        instaBtn = (MaterialButton) findViewById(R.id.button21);
        twiBtn = (MaterialButton) findViewById(R.id.button19);
        linkBtn = (MaterialButton) findViewById(R.id.button20);

        BusinessKey = getIntent().getExtras().get("key").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

        userRef.child(BusinessKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("business_facebook")))
                    {

                        FaceBook = snapshot.child("business_facebook").getValue().toString();
                        facebookView.setText(FaceBook);
                        fbBtn.setText("Edit");

                    }
                    if ((snapshot.hasChild("business_twitter")))
                    {
                        Twitter = snapshot.child("business_twitter").getValue().toString();
                        twitterView.setText(Twitter);
                        twiBtn.setText("Edit");
                    }
                    if ((snapshot.hasChild("business_linked_in")))
                    {
                        LinkedIn = snapshot.child("business_linked_in").getValue().toString();
                        linkedInView.setText(LinkedIn);
                        linkBtn.setText("Edit");
                    }
                    if ((snapshot.hasChild("business_instagram")))
                    {

                        Instagram = snapshot.child("business_instagram").getValue().toString();
                        instagramiew.setText(Instagram);
                        instaBtn.setText("Edit");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void editFacebook(View view) {

        dialog = new Dialog(BusinessSocialMediaActivity.this);
        dialog.setContentView(R.layout.edit_social_media);
        TextView nameView = (TextView) dialog.findViewById(R.id.textView80);
        nameView.setText("Facebook Page Link");
        TextView descView = (TextView) dialog.findViewById(R.id.textView81);
        descView.setText("Please type or copy and paste your PAGE URL from Facebook into the  TextEditor. The PAGE URL will look like this https://www.facebook.com/PAGE_ACCOUNT_ID from your facebook page.");
        final TextInputEditText facebookInput = (TextInputEditText) dialog.findViewById(R.id.textField14);
        facebookInput.setText(FaceBook);
        MaterialButton sendDialog  = (MaterialButton) dialog.findViewById(R.id.button82);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String FacebookInput = facebookInput.getText().toString();

                progressBar.setVisibility(ProgressBar.VISIBLE);

                final Map<String, Object > socialMap = new HashMap<>();
                socialMap.put("business_facebook", FacebookInput);


                userRef.child(BusinessKey).updateChildren(socialMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    dialog.dismiss();
                                }
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                        });

            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }

    public void editInsta(View view) {

        dialog = new Dialog(BusinessSocialMediaActivity.this);
        dialog.setContentView(R.layout.edit_social_media);
        TextView nameView = (TextView) dialog.findViewById(R.id.textView80);
        nameView.setText("Instagram Page Link");
        TextView descView = (TextView) dialog.findViewById(R.id.textView81);
        descView.setText("Please type or copy and paste PAGE URL from your Instagram profile page into the  TextEditor. The PAGE URL will look like this https://www.instagram.com/PAGE_ACCOUNT_ID/.");
        final TextInputEditText instagramInput = (TextInputEditText) dialog.findViewById(R.id.textField14);
        instagramInput.setText(Instagram);
        MaterialButton sendDialog  = (MaterialButton) dialog.findViewById(R.id.button82);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String InstagramInput = instagramInput.getText().toString();

                progressBar.setVisibility(ProgressBar.VISIBLE);

                final Map<String, Object > socialMap = new HashMap<>();
                socialMap.put("business_instagram", InstagramInput);


                userRef.child(BusinessKey).updateChildren(socialMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    dialog.dismiss();
                                }
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                        });

            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void editLinkedIn(View view) {

        dialog = new Dialog(BusinessSocialMediaActivity.this);
        dialog.setContentView(R.layout.edit_social_media);
        TextView nameView = (TextView) dialog.findViewById(R.id.textView80);
        nameView.setText("LinkedIn Link");
        TextView descView = (TextView) dialog.findViewById(R.id.textView81);
        descView.setText("Please type or copy and paste your PROFILE URL from LinkedIn into the  TextEditor. The PROFILE URL will look like this https://www.linkedin.com/in/PAGE_ACCOUNT_ID/.");
        final TextInputEditText linkedInInput = (TextInputEditText) dialog.findViewById(R.id.textField14);
        linkedInInput.setText(LinkedIn);
        MaterialButton sendDialog  = (MaterialButton) dialog.findViewById(R.id.button82);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String LinkedInInput = linkedInInput.getText().toString();

                progressBar.setVisibility(ProgressBar.VISIBLE);

                final Map<String, Object > socialMap = new HashMap<>();
                socialMap.put("business_linked_in", LinkedInInput);


                userRef.child(BusinessKey).updateChildren(socialMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    dialog.dismiss();
                                }
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                        });

            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void editTwitter(View view) {

        dialog = new Dialog(BusinessSocialMediaActivity.this);
        dialog.setContentView(R.layout.edit_social_media);
        TextView nameView = (TextView) dialog.findViewById(R.id.textView80);
        nameView.setText("Twitter Page Link");
        TextView descView = (TextView) dialog.findViewById(R.id.textView81);
        descView.setText("Please type or copy and paste your PROFILE URL from Twitter into the  TextEditor. The PROFILE URL will be in this link https://twitter.com/PAGE_ACCOUNT_ID from your profile page.");
        final TextInputEditText twitterInput = (TextInputEditText) dialog.findViewById(R.id.textField14);
        twitterInput.setText(Twitter);
        MaterialButton sendDialog  = (MaterialButton) dialog.findViewById(R.id.button82);
        sendDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String TwitterInput = twitterInput.getText().toString();

                progressBar.setVisibility(ProgressBar.VISIBLE);
                final Map<String, Object > socialMap = new HashMap<>();
                socialMap.put("business_twitter", TwitterInput);


                userRef.child(BusinessKey).updateChildren(socialMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    dialog.dismiss();
                                }
                                progressBar.setVisibility(ProgressBar.GONE);
                            }
                        });

            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}