package com.pridetechnologies.businesscard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class BusinessBioActivity extends AppCompatActivity {

    private TextInputEditText businessBioInput;

    private String admin_id, BusinessKey;
    private DatabaseReference AdminRef;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_bio);

        progressBar = (ProgressBar) findViewById(R.id.progressBar7);

        BusinessKey = getIntent().getExtras().get("key").toString();

        businessBioInput = (TextInputEditText) findViewById(R.id.business_bio1);

        firebaseAuth = FirebaseAuth.getInstance();
        admin_id = firebaseAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

    }

    public void skipActivity(View view) {
        Intent intent = new Intent(BusinessBioActivity.this, AddBusinessLogoActivity.class);
        intent.putExtra("key",BusinessKey);
        startActivity(intent);
        Animatoo.animateSlideLeft(BusinessBioActivity.this);
        finish();
    }

    public void saveBio(View view) {
        final String BIO = businessBioInput.getText().toString();

        if (TextUtils.isEmpty(BIO))
        {
            Toast.makeText(BusinessBioActivity.this, "Please Enter Bio", Toast.LENGTH_LONG).show();
        }

        else if (!TextUtils.isEmpty(BIO))
        {
            progressBar.setVisibility(ProgressBar.VISIBLE);

            Map<String, Object> usersMap = new HashMap<>();
            usersMap.put("business_bio", BIO);

            db.collection("Businesses").document(BusinessKey)
                    .set(usersMap, SetOptions.merge());

            AdminRef.child(BusinessKey).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        Intent intent = new Intent(BusinessBioActivity.this, AddBusinessLogoActivity.class);
                        intent.putExtra("key",BusinessKey);
                        startActivity(intent);
                        finish();
                        Animatoo.animateSlideLeft(BusinessBioActivity.this);
                    }else {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(BusinessBioActivity.this, "Failed to Save Business Bio. Try Again!!", Toast.LENGTH_LONG).show();
                    }

                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            });
        }
    }
}