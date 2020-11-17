package com.pridetechnologies.businesscard;

import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_KEY;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_SECRET;
import static com.pridetechnologies.businesscard.Sinch.SinchService.ENVIRONMENT;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pridetechnologies.businesscard.Common.Common;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;
import com.pridetechnologies.businesscard.Sinch.SinchService;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.LogInCallback;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushTokenRegistrationCallback;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.UserController;
import com.sinch.android.rtc.UserRegistrationCallback;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements SinchService.StartFailedListener, PushTokenRegistrationCallback, UserRegistrationCallback {

    private TextInputEditText regEMail;
    private TextInputEditText regPassword;
    private TextInputEditText regPassword2;
    private MaterialButton login, regBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private FirebaseFirestore db;
    private ProgressBar regPrb;

    private long mSigningSequence = 1;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        regEMail = (TextInputEditText) findViewById(R.id.editTextTextEmailAddress2);
        regPassword = (TextInputEditText) findViewById(R.id.editTextTextPassword);
        regPassword2 = (TextInputEditText) findViewById(R.id.editTextTextPassword2);
        login = (MaterialButton) findViewById(R.id.button24);
        regBtn = (MaterialButton) findViewById(R.id.button23);

        regPrb = (ProgressBar) findViewById(R.id.regProgressBar);


        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        db = FirebaseFirestore.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                Animatoo.animateFade(RegisterActivity.this);
                finish();
            }
        });
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAccount();
            }
        });
    }

    public void registerAccount() {

        final String email = regEMail.getText().toString().trim();
        final String pass = regPassword.getText().toString().trim();
        String pass2 = regPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(RegisterActivity.this, "Please Enter Email", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(pass))
        {
            Toast.makeText(RegisterActivity.this, "Please Enter Password", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(pass2))
        {
            Toast.makeText(RegisterActivity.this, "Please Enter Confirmation Password", Toast.LENGTH_LONG).show();
        }
        else if (!pass.equals(pass2))
        {
            Toast.makeText(RegisterActivity.this, " Passwords do not match", Toast.LENGTH_LONG).show();
        }
        else if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) & !TextUtils.isEmpty(pass2))
        {
            regPrb.setVisibility(ProgressBar.VISIBLE);
            regBtn.setEnabled(false);


            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        final String currentUserID = mAuth.getCurrentUser().getUid();
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        Map<String, Object> usersMap = new HashMap<>();
                        usersMap.put("device_token", deviceToken);
                        usersMap.put("user_email", email);
                        usersMap.put("user_uid",currentUserID);

                        Purchases.getSharedInstance().logIn(currentUserID, new LogInCallback() {
                            @Override
                            public void onReceived(@NotNull CustomerInfo customerInfo, boolean created) {
                                // customerInfo updated for my_app_user_id
                            }
                            @Override
                            public void onError(@NotNull PurchasesError error) {

                            }
                        });

                        if (!getSinchServiceInterface().isStarted()) {
                            getSinchServiceInterface().startClient(currentUserID);
                            Common.saveUserData(RegisterActivity.this,"user_name",currentUserID);
                        }
                        UserController uc = Sinch.getUserControllerBuilder()
                                .context(getApplicationContext())
                                .applicationKey(APP_KEY)
                                .userId(currentUserID)
                                .environmentHost(ENVIRONMENT)
                                .build();
                        uc.registerUser(RegisterActivity.this, RegisterActivity.this);


                        UsersRef.child(currentUserID).setValue(usersMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            regPrb.setVisibility(ProgressBar.INVISIBLE);
                                            regBtn.setEnabled(true);
                                            Intent intent = new Intent(RegisterActivity.this, AddPersonalDetailsActivity.class);
                                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            Animatoo.animateFade(RegisterActivity.this);
                                            finish();
                                        }else
                                        {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                                            regPrb.setVisibility(ProgressBar.INVISIBLE);
                                            regBtn.setEnabled(true);
                                        }
                                    }
                                });
                    }
                    else
                    {
                        String error = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error:" + error, Toast.LENGTH_LONG).show();
                        regPrb.setVisibility(ProgressBar.INVISIBLE);
                        regBtn.setEnabled(true);
                    }
                }
            });

        }
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        getSinchServiceInterface().setStartListener(this);

    }

    @Override
    public void onStartFailed(SinchError error) {
        //Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        //if (loginPrb != null) {
        //    loginPrb.setVisibility(ProgressBar.INVISIBLE);
        //}

    }

    @Override
    public void onStarted() {
        //sendToMain("");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void tokenRegistered() {

    }

    @Override
    public void tokenRegistrationFailed(SinchError sinchError) {
        //Toast.makeText(this, "Push token registration failed - incoming calls can't be received!", Toast.LENGTH_LONG).show();
    }

    // The most secure way is to obtain the signature from the backend,
    // since storing APP_SECRET in the app is not secure.
    // Following code demonstrates how the signature is obtained provided
    // the UserId and the APP_KEY and APP_SECRET.
    @Override
    public void onCredentialsRequired(ClientRegistration clientRegistration) {
        String toSign = mUserId + APP_KEY + mSigningSequence + APP_SECRET;
        String signature;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] hash = messageDigest.digest(toSign.getBytes("UTF-8"));
            signature = Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        clientRegistration.register(signature, mSigningSequence++);
    }

    @Override
    public void onUserRegistered() {
        // Instance is registered, but we'll wait for another callback, assuring that the push token is
        // registered as well, meaning we can receive incoming calls.
    }

    @Override
    public void onUserRegistrationFailed(SinchError sinchError) {
        //Toast.makeText(this, "Registration failed!", Toast.LENGTH_LONG).show();
    }
}