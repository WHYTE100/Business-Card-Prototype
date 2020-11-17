package com.pridetechnologies.businesscard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pridetechnologies.businesscard.Common.Common;
import com.pridetechnologies.businesscard.Sinch.BaseActivity;

public class SplashActivity extends BaseActivity{

    private static int SPLASH_TIME = 5000;

    private int REQUEST_CODE = 30;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(SplashActivity.this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {

                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
                {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result,AppUpdateType.FLEXIBLE, SplashActivity.this,REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });



        MotionLayout motionLayout = findViewById(R.id.motion2);
        motionLayout.transitionToEnd();
        motionLayout.setTransitionDuration(4000);
        //Code to start timer and take action after the timer ends
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()

            {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null)
                {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Animatoo.animateFade(SplashActivity.this);
                    finish();
                }
                else
                {
                    if (!getSinchServiceInterface().isStarted()) {
                        getSinchServiceInterface().startClient(currentUser.getUid());
                        Common.saveUserData(SplashActivity.this,"user_name",currentUser.getUid());
                    }
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                    Animatoo.animateFade(SplashActivity.this);
                    finish();
                    //biometricPrompt.authenticate(promptInfo);
                }

            }
        }, SPLASH_TIME);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            Toast.makeText(this, "Downloading Update.", Toast.LENGTH_SHORT).show();
            if (resultCode !=RESULT_OK)
            {

            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}