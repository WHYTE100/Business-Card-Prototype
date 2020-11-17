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

import java.util.HashMap;
import java.util.Map;

public class ManageBusinessActivity extends AppCompatActivity {

    private MaterialButton saveBtn;

    private TextInputEditText companyWeb, companyEmail, companyMobile, companyWhatsApp;

    private String BusinessKey;
    private DatabaseReference AdminRef, CompanyRef;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_business);

        progressBar = (ProgressBar) findViewById(R.id.manageProgressBar);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton100);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        BusinessKey = getIntent().getExtras().get("key").toString();
        
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

        companyWeb = (TextInputEditText) findViewById(R.id.textField21);
        companyEmail = (TextInputEditText) findViewById(R.id.textField20);
        companyMobile = (TextInputEditText) findViewById(R.id.mobileTextfield2);
        companyWhatsApp = (TextInputEditText) findViewById(R.id.whatsAppTextfield2);

        saveBtn = (MaterialButton) findViewById(R.id.button122);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInfo();
            }
        });

        retrieveInfo();
    }

    private void retrieveInfo() {

        CompanyRef.child(BusinessKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    if (snapshot.hasChild("business_mobile"))
                    {
                        String BM = snapshot.child("business_mobile").getValue().toString();
                        companyMobile.setText(BM);
                    }
                    if (snapshot.hasChild("business_whatsapp"))
                    {
                        String BW = snapshot.child("business_whatsapp").getValue().toString();
                        companyWhatsApp.setText(BW);
                    }
                    if (snapshot.hasChild("business_website"))
                    {
                        String BS = snapshot.child("business_website").getValue().toString();
                        companyWeb.setText(BS);
                    }
                    if (snapshot.hasChild("business_email"))
                    {
                        String BE = snapshot.child("business_email").getValue().toString();
                        companyEmail.setText(BE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveInfo() {

        final Dialog dialog = new Dialog(ManageBusinessActivity.this);
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
            progressBar.setVisibility(ProgressBar.VISIBLE);

            String CompanyWeb = companyWeb.getText().toString().trim();
            String CompanyEmail = companyEmail.getText().toString().trim();
            String CMobile = companyMobile.getText().toString().trim();
            String CWhatsApp = companyWhatsApp.getText().toString().trim();

            final Map<String, Object> companyMap = new HashMap<>();
            companyMap.put("business_mobile", CMobile);
            companyMap.put("business_whatsapp", CWhatsApp);
            companyMap.put("business_website", CompanyWeb);
            companyMap.put("business_email", CompanyEmail);


            CompanyRef.child(BusinessKey).updateChildren(companyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        progressBar.setVisibility(ProgressBar.GONE);
                        finish();
                    }
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            });
        });
        noBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }
}