package com.pridetechnologies.businesscard;

import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_KEY;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_SECRET;
import static com.pridetechnologies.businesscard.Sinch.SinchService.ENVIRONMENT;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pridetechnologies.businesscard.Common.Common;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;
import com.pridetechnologies.businesscard.Sinch.SinchService;
import com.pridetechnologies.businesscard.databinding.ActivityLoginBinding;
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

public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener, PushTokenRegistrationCallback, UserRegistrationCallback {

    public static final String EXTRAS_ENDLESS_MODE = "EXTRAS_ENDLESS_MODE";
    private static final int RC_SIGN_IN = 3333;

    private TextInputEditText edt1;
    private TextInputEditText edt2;
    private TextView textView2;
    private ProgressBar loginPrb;
    private MaterialButton lgnBtn, button;
    private TextInputLayout textInputLayout;
    private ImageView facebookLogin;
    private FirebaseAuth mAuth;
    DatabaseReference UsersRef;
    private long mSigningSequence = 1;
    String mUserId;
    GoogleSignInClient mGoogleSignInClient;

    CallbackManager callbackManager;

    private ActivityLoginBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        edt1 = (TextInputEditText) findViewById(R.id.loginEmail);
        edt2 = (TextInputEditText) findViewById(R.id.loginPassword);
        lgnBtn = (MaterialButton)findViewById(R.id.loginButton);
        button = (MaterialButton) findViewById(R.id.button3);
        loginPrb = (ProgressBar) findViewById(R.id.progressBar);
        textView2 = (TextView) findViewById(R.id.textView33);

        edt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                calculatePasswordStrength(s.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textView2.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            Animatoo.animateFade(LoginActivity.this);
        });
        button.setOnClickListener(v -> {

            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            Animatoo.animateFade(LoginActivity.this);
            finish();
        });
        lgnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = edt1.getText().toString().trim();
                String loginPassword = edt2.getText().toString().trim();

                if (TextUtils.isEmpty(loginEmail))
                {
                    Toast.makeText(LoginActivity.this, "Please Enter Email", Toast.LENGTH_LONG).show();

                }
                else if (TextUtils.isEmpty(loginPassword))
                {
                    Toast.makeText(LoginActivity.this, "Please Enter Password", Toast.LENGTH_LONG).show();

                }
                else if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword))
                {

                    lgnBtn.setEnabled(false);
                    loginPrb.setVisibility(ProgressBar.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                mUserId=currentUserID;

                                sendToMain(currentUserID);
                            } else {

                                loginPrb.setVisibility(ProgressBar.INVISIBLE);
                                lgnBtn.setEnabled(true);
                                String error = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        


    }

    private void calculatePasswordStrength(String str) {
        // Now, we need to define a PasswordStrength enum
        // with a calculate static method returning the password strength
        PasswordStrength passwordStrength = PasswordStrength.calculate(str);
        String message = String.valueOf(passwordStrength.msg);
        //textView2.setText(passwordStrength.msg);
        // textView2.setBackgroundColor(passwordStrength.color);
        //Toast.makeText(LoginActivity.this, "Password Strength: "+message, Toast.LENGTH_LONG).show();
    }

    private void sendToMain(String currentUserID) {

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(currentUserID);
            Common.saveUserData(LoginActivity.this,"user_name",currentUserID);
        }

        UserController uc = Sinch.getUserControllerBuilder()
                .context(getApplicationContext())
                .applicationKey(APP_KEY)
                .userId(currentUserID)
                .environmentHost(ENVIRONMENT)
                .build();
        uc.registerUser(LoginActivity.this, LoginActivity.this);

        loginPrb.setVisibility(ProgressBar.INVISIBLE);
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Animatoo.animateFade(LoginActivity.this);
        finish();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        getSinchServiceInterface().setStartListener(this);

    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (loginPrb != null) {
            loginPrb.setVisibility(ProgressBar.INVISIBLE);
        }

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

    public void googleSignIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        loginPrb.setVisibility(ProgressBar.VISIBLE);
        //Toast.makeText(LoginActivity.this, "Not working right now", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        loginPrb.setVisibility(ProgressBar.GONE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("LoginActivity", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Failed to login: "+e, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            sendToMain(user.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Log.w("LoginActivity", "signInWithCredential:failure");
                            //updateUI(null);
                            Toast.makeText(LoginActivity.this, "Failed to login: "+task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
