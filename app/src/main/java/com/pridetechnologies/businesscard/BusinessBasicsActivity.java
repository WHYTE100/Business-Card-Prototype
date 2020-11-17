package com.pridetechnologies.businesscard;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessBasicsActivity extends AppCompatActivity {

    private String BusinessKey, CompanyName, CompanyBio, CompanyBuildingName, CompanyStreetName, CompanyAreaLocated, CompanyDistrict, CompanyCountry, CompanyEmail, CompanyWebsite;
    private TextInputEditText companyName, companyBio, companyBuildingName, companyStreetName, companyAreaLocated, companyDistrict, companyCountry, companyEmail, companyWebsite;
    private MaterialButton saveBtn;

    private DatabaseReference CompanyRef;
    private FirebaseAuth firebaseAuth;
    private CircleImageView logoImageView;
    private Button editButton;
    private FirebaseFirestore db;
    private CollectionReference businessRef;

    private Index index;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_basics);

        progressBar = (ProgressBar) findViewById(R.id.progressBar5);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton99);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Client client = new Client("T5LG6WB4JZ", "e799a43983df842a91ead0dad768e64d");
        index = client.getIndex("businesses");

        firebaseAuth = FirebaseAuth.getInstance();
        BusinessKey = getIntent().getExtras().get("key").toString();

        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");
        db = FirebaseFirestore.getInstance();
        businessRef = db.collection("Businesses");

        editButton = (Button) findViewById(R.id.button27);

        companyName = (TextInputEditText) findViewById(R.id.textfield1);
        companyBio = (TextInputEditText) findViewById(R.id.textField100);
        companyBuildingName = (TextInputEditText) findViewById(R.id.textfield3);
        companyAreaLocated = (TextInputEditText) findViewById(R.id.textfield4);
        companyDistrict = (TextInputEditText) findViewById(R.id.textfield5);
        companyCountry = (TextInputEditText) findViewById(R.id.textfield6);
        companyStreetName = (TextInputEditText) findViewById(R.id.textfield22);
        companyEmail = (TextInputEditText) findViewById(R.id.emailTextField);
        companyWebsite = (TextInputEditText) findViewById(R.id.websiteTextField);
        saveBtn = (MaterialButton) findViewById(R.id.button12);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo();
            }
        });

        logoImageView = (CircleImageView) findViewById(R.id.imageView19);

        retrieveInfo();
    }

    private void retrieveInfo() {

        CompanyRef.child(BusinessKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if ((snapshot.hasChild("business_logo")))
                    {
                        editButton.setText("Change Logo");
                        String Image = snapshot.child("business_logo").getValue().toString();

                        Picasso.get().load(Image).fit().centerCrop().placeholder(R.mipmap.background_icon).into(logoImageView);
                    }
                    if ((snapshot.hasChild("business_name")))
                    {
                        final String CompanyName = snapshot.child("business_name").getValue().toString();
                        companyName.setText(CompanyName);
                    }
                    if ((snapshot.hasChild("business_bio")))
                    {
                        final String CompanyBio = snapshot.child("business_bio").getValue().toString();
                        companyBio.setText(CompanyBio);
                    }
                    if ((snapshot.hasChild("business_building")))
                    {
                        String CompanyBuilding = snapshot.child("business_building").getValue().toString();
                        companyBuildingName.setText(CompanyBuilding);
                    }
                    if ((snapshot.hasChild("business_street")))
                    {
                        String CompanyStreet = snapshot.child("business_street").getValue().toString();
                        companyStreetName.setText(CompanyStreet);
                    }
                    if ((snapshot.hasChild("business_location")))
                    {
                        String CompanyArea = snapshot.child("business_location").getValue().toString();
                        companyAreaLocated.setText(CompanyArea);
                    }
                    if ((snapshot.hasChild("business_district")))
                    {
                        String CompanyDistrict = snapshot.child("business_district").getValue().toString();
                        companyDistrict.setText(CompanyDistrict);
                    }
                    if ((snapshot.hasChild("business_email")))
                    {
                        String CompanyEmail = snapshot.child("business_email").getValue().toString();
                        companyEmail.setText(CompanyEmail);
                    }
                    if ((snapshot.hasChild("business_website")))
                    {
                        String CompanyWebsite = snapshot.child("business_website").getValue().toString();
                        companyWebsite.setText(CompanyWebsite);
                    }
                    if ((snapshot.hasChild("business_country")))
                    {
                        String CompanyCountry = snapshot.child("business_country").getValue().toString();
                        companyCountry.setText(CompanyCountry);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveInfo() {

        CompanyName = companyName.getText().toString().trim();
        CompanyBio = companyBio.getText().toString().trim();
        CompanyBuildingName = companyBuildingName.getText().toString().trim();
        CompanyAreaLocated = companyAreaLocated.getText().toString().trim();
        CompanyDistrict = companyDistrict.getText().toString().trim();
        CompanyCountry = companyCountry.getText().toString().trim();
        CompanyStreetName = companyStreetName.getText().toString().trim();
        CompanyEmail = companyEmail.getText().toString().trim();
        CompanyWebsite = companyWebsite.getText().toString().trim();

        if (TextUtils.isEmpty(CompanyName))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter Business Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyStreetName))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter Street Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyBuildingName))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter Building Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyAreaLocated))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter Location", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyCountry))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter Country", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(CompanyDistrict))
        {
            Toast.makeText(BusinessBasicsActivity.this, "Please Enter District", Toast.LENGTH_LONG).show();
        }
        if (!TextUtils.isEmpty(CompanyBuildingName) && !TextUtils.isEmpty(CompanyAreaLocated))
        {

            final Dialog dialog = new Dialog(BusinessBasicsActivity.this);
            dialog.setContentView(R.layout.custom_dialog__message_layout);
            TextView titleView = dialog.findViewById(R.id.dialogTitle);
            TextView messageView = dialog.findViewById(R.id.dialogMessage);
            TextView yesBtn = dialog.findViewById(R.id.dialogYesBtn);
            TextView noBtn = dialog.findViewById(R.id.dialogNoBtn);
            titleView.setText("Save");
            messageView.setText("Are you sure you want to save this info?");
            noBtn.setText("Cancel");
            yesBtn.setText("Save");
            yesBtn.setOnClickListener(v -> {
                dialog.dismiss();
                progressBar.setVisibility(ProgressBar.VISIBLE);

                final Map<String, Object> companyMap = new HashMap<>();
                companyMap.put("business_name", CompanyName);
                companyMap.put("business_bio", CompanyBio);
                companyMap.put("business_location", CompanyAreaLocated);
                companyMap.put("business_district", CompanyDistrict);
                companyMap.put("business_street", CompanyStreetName);
                companyMap.put("business_building", CompanyBuildingName);
                companyMap.put("business_country", CompanyCountry);
                companyMap.put("business_email", CompanyEmail);
                companyMap.put("business_website", CompanyWebsite);

                JSONObject object = null;
                try {
                    object = new JSONObject()
                            .put("business_name", CompanyName)
                            .put("business_bio", CompanyBio)
                            .put("business_location", CompanyAreaLocated)
                            .put("business_district", CompanyDistrict)
                            .put("business_street", CompanyStreetName)
                            .put("business_building", CompanyBuildingName)
                            .put("business_country", CompanyCountry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                index.addObjectAsync(object, BusinessKey, null);
                CompanyRef.child(BusinessKey).updateChildren(companyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            finish();
                            Toast.makeText(BusinessBasicsActivity.this, "Business Updated Successfully", Toast.LENGTH_LONG).show();

                        }
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });
                businessRef.document(BusinessKey)
                        .set(companyMap, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    //Toast.makeText(BusinessBasicsActivity.this, "Firestore Updated Successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                                    //Toast.makeText(BusinessBasicsActivity.this, "Firestore Updated Failed", Toast.LENGTH_LONG).show();

                                }
                            }
                        });
            });
            noBtn.setOnClickListener(v -> dialog.dismiss());
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    }
    public void goToLogoPage(View view) {
        Intent intent = new Intent(BusinessBasicsActivity.this, BusinessLogoActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateFade(BusinessBasicsActivity.this);
    }
}