package com.pridetechnologies.businesscard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import java.util.HashMap;
import java.util.Map;

public class AddBioActivity extends AppCompatActivity {

    private TextInputEditText userBioInput;

    private String admin_id;
    private DatabaseReference AdminRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_bio);

        progressBar = (ProgressBar) findViewById(R.id.addProgressBar4);
        userBioInput = (TextInputEditText) findViewById(R.id.user_bio1);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        admin_id = firebaseAuth.getCurrentUser().getUid();
        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void skipActivity(View view) {
        Intent intent = new Intent(AddBioActivity.this, HomeActivity.class);
        startActivity(intent);
        Animatoo.animateSlideLeft(AddBioActivity.this);
        finishAffinity();
    }

    public void saveBio(View view) {
        final String BIO = userBioInput.getText().toString();

        if (TextUtils.isEmpty(BIO))
        {
            Toast.makeText(AddBioActivity.this, "Please Enter Your Bio", Toast.LENGTH_LONG).show();
        }

        else if (!TextUtils.isEmpty(BIO))
        {
            progressBar.setVisibility(View.VISIBLE);

            Map<String, Object> usersMap = new HashMap<>();
            usersMap.put("user_bio", BIO);

            AdminRef.child(admin_id).updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(AddBioActivity.this, HomeActivity.class);
                        startActivity(intent);
                        Animatoo.animateFade(AddBioActivity.this);
                        finishAffinity();

                    }else
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(AddBioActivity.this, "Failed to Save Bio. Try Again!!", Toast.LENGTH_LONG).show();
                    }

                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    progressBar.setVisibility(View.INVISIBLE);;
                }
            });
        }
    }
}