package com.pridetechnologies.businesscard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;

public class EditDepartmentContactsActivity extends AppCompatActivity {

    private String BusinessKey=null;
    private String ContactsKey=null;

    private TextInputEditText departmentName, departmentDesc, departmentEmail, departmentMobile, departmentWhatsApp;

    private CountryCodePicker mobileCode, whatsAppCode;

    String MobileCode = null;
    String WhatsAppCode = null;

    private String admin_id;
    private DatabaseReference CompanyContactsRef, CompanyRef;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    ConstraintLayout addedView;
    ScrollView scrollView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_department_contacts);

        progressBar = (ProgressBar) findViewById(R.id.editProgressBar);

        ImageButton closeBtn = (ImageButton) findViewById(R.id.imageButton991);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        BusinessKey = getIntent().getExtras().get("key").toString();
        ContactsKey = getIntent().getExtras().get("card_key").toString();
        addedView = (ConstraintLayout) findViewById(R.id.addedView);
        addedView.setVisibility(View.GONE);
        scrollView4 = (ScrollView) findViewById(R.id.scrollView4);
        scrollView4.setVisibility(View.VISIBLE);

        CompanyContactsRef = FirebaseDatabase.getInstance().getReference().child("Business Contacts");
        CompanyRef = FirebaseDatabase.getInstance().getReference().child("Businesses");

        departmentName = (TextInputEditText) findViewById(R.id.textfield1);
        departmentDesc = (TextInputEditText) findViewById(R.id.textfield23);
        departmentEmail = (TextInputEditText) findViewById(R.id.textfield2);
        departmentMobile = (TextInputEditText) findViewById(R.id.mobileTextfield);
        departmentWhatsApp = (TextInputEditText) findViewById(R.id.whatsAppTextfield);
        mobileCode = (CountryCodePicker) findViewById(R.id.ccp6);
        whatsAppCode = (CountryCodePicker) findViewById(R.id.ccp7);

        mobileCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {

                MobileCode = mobileCode.getSelectedCountryCodeWithPlus();
                departmentMobile.setText(MobileCode);
            }
        });
        whatsAppCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {

                WhatsAppCode = whatsAppCode.getSelectedCountryCodeWithPlus();
                departmentWhatsApp.setText(WhatsAppCode);

            }
        });

        MobileCode = mobileCode.getSelectedCountryCodeWithPlus();
        WhatsAppCode = whatsAppCode.getSelectedCountryCodeWithPlus();

        retrieveContactDetails();
    }

    private void retrieveContactDetails() {
        CompanyContactsRef.child(BusinessKey).child(ContactsKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {

                    if ((snapshot.hasChild("department_name")))
                    {
                        String DepartmentName = snapshot.child("department_name").getValue().toString();
                        departmentName.setText(DepartmentName);
                    }
                    if ((snapshot.hasChild("department_desc")))
                    {
                        String DepartmentDesc = snapshot.child("department_desc").getValue().toString();
                        departmentDesc.setText(DepartmentDesc);
                    }
                    if ((snapshot.hasChild("department_email")))
                    {
                        String DepartmentEmail= snapshot.child("department_email").getValue().toString();
                        departmentEmail.setText(DepartmentEmail);
                    }
                    if ((snapshot.hasChild("department_mobile")))
                    {
                        String DepartmentMobile = snapshot.child("department_mobile").getValue().toString();
                        departmentMobile.setText(DepartmentMobile);
                    }
                    if ((snapshot.hasChild("department_whatsapp")))
                    {
                        String DepartmentWhatsApp = snapshot.child("department_whatsapp").getValue().toString();
                        departmentWhatsApp.setText(DepartmentWhatsApp);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addDepartment(View view) {

        final String DepartmentName = departmentName.getText().toString().trim();
        final String DepartmentDesc = departmentDesc.getText().toString().trim();
        final String DepartmentEmail = departmentEmail.getText().toString().trim();

        String Mobile = "";
        String WhatsApp = "";
        if (departmentMobile==null)
        {
            Mobile = null;
        } else
        {
            Mobile = departmentMobile.getText().toString().trim();
        }
        if (departmentWhatsApp==null)
        {
            WhatsApp = null;
        } else
        {
            WhatsApp = departmentWhatsApp.getText().toString().trim();
        }

        if (TextUtils.isEmpty(DepartmentName))
        {
            Toast.makeText(EditDepartmentContactsActivity.this, "Please Enter Department Name", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(DepartmentEmail))
        {
            Toast.makeText(EditDepartmentContactsActivity.this, "Please Enter Department Email", Toast.LENGTH_LONG).show();
        }
        else if (!TextUtils.isEmpty(DepartmentName) && !TextUtils.isEmpty(DepartmentEmail)){

            progressBar.setVisibility(ProgressBar.VISIBLE);

            Map<String, Object> departmentMap = new HashMap<>();
            departmentMap.put("department_name", DepartmentName);
            departmentMap.put("department_desc", DepartmentDesc);
            departmentMap.put("department_email", DepartmentEmail);
            departmentMap.put("department_mobile", Mobile);
            departmentMap.put("department_whatsapp", WhatsApp);

            CompanyContactsRef.child(BusinessKey).child(ContactsKey).updateChildren(departmentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        addedView.setVisibility(View.VISIBLE);
                        scrollView4.setVisibility(View.GONE);
                    }else {
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(EditDepartmentContactsActivity.this, "Failed to Save Department Data. Try Again!!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void finishAdding(View view) {
        finish();
    }
}