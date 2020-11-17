package com.pridetechnologies.businesscard.Sinch;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_KEY;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_SECRET;
import static com.pridetechnologies.businesscard.Sinch.SinchService.ENVIRONMENT;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pridetechnologies.businesscard.Common.Common;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallNotificationResult;

import java.util.Map;

public class FcmListenerService extends FirebaseMessagingService {

    public static String CHANNEL_ID = "Sinch Push Notification Channel";
    private static final String TAG = FcmListenerService.class.getSimpleName();
    public static SinchClient sinchClient=null;
    public static CallClient callClient=null;

    String username;

    @Override
    public void onCreate() {
        username= Common.getSavedUserData(this,"user_name");
        if(username!=null && username.length()!=0){
            initsinch();
        }


        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        if(foregrounded())
        {
            return;
        }
        Map data = remoteMessage.getData();

        // Optional: inspect the payload w/o starting Sinch Client and thus avoiding onIncomingCall()
        // e.g. useful to fetch user related polices (blacklist), resources (to show a picture, etc).
        NotificationResult result = SinchHelpers.queryPushNotificationPayload(getApplicationContext(), data);
        if (result.isValid() && result.isCall()) {
            CallNotificationResult callResult = result.getCallResult();
            Log.d(TAG, "queryPushNotificationPayload() -> display name: " + result.getDisplayName());
            if (callResult != null) {
                Log.d(TAG, "queryPushNotificationPayload() -> headers: " + result.getCallResult().getHeaders());
                Log.d(TAG, "queryPushNotificationPayload() -> remote user ID: " + result.getCallResult().getRemoteUserId());
            }
        }

        // Mandatory: forward payload to the SinchClient.
        if (SinchHelpers.isSinchPushPayload(data)) {
            new ServiceConnection() {
                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (payload != null) {
                        SinchService.SinchServiceInterface sinchService = (SinchService.SinchServiceInterface) service;
                        if (sinchService != null) {
                            NotificationResult result = sinchService.relayRemotePushNotificationPayload(payload);
                            if (result.isValid() && result.isCall()) {
                                // Optional: handle result, e.g. show a notification or similar.
                            }
                        }
                    }
                    payload = null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {}

                public void relayMessageData(Map<String, String> data) {
                    payload = data;
                    createNotificationChannel(NotificationManager.IMPORTANCE_MAX);
                    getApplicationContext().bindService(new Intent(getApplicationContext(), SinchService.class), this, BIND_AUTO_CREATE);
                }
            }.relayMessageData(data);
        }
    }
    ///To check if the app is in foreground ///
    public static boolean foregrounded() {
        ActivityManager.RunningAppProcessInfo appProcessInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }
    private void initsinch() {
        if (sinchClient == null) {

            android.content.Context context = this.getApplicationContext();
            sinchClient = Sinch.getSinchClientBuilder().context(context)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT)
                    .userId(username).build();

            sinchClient.setSupportCalling(true);
            sinchClient.setSupportActiveConnectionInBackground(true);
            sinchClient.startListeningOnActiveConnection();
            sinchClient.setSupportManagedPush(true);

            sinchClient.setPushNotificationDisplayName("my display name");
            sinchClient.addSinchClientListener(new SinchClientListener() {

                public void onClientStarted(SinchClient client) {


                }

                public void onClientStopped(SinchClient client) {

                }

                public void onClientFailed(SinchClient client, SinchError error) {

                }

                public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) {


                }

                public void onLogMessage(int level, String area, String message) {


                }

            });
            callClient = sinchClient.getCallClient();
            callClient.setRespectNativeCalls(true);
            callClient.addCallClientListener(new CallClientListener() {
                @Override
                public void onIncomingCall(CallClient callClient, Call INCOMMINGCALL) {


                    Intent it = new Intent(getApplicationContext(), IncomingCallScreenActivity.class);
                    it.putExtra("mCall", INCOMMINGCALL.getCallId());
                    it.putExtra("mCall_caller", INCOMMINGCALL.getRemoteUserId());
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                }


            });

        }
        if (sinchClient != null && !sinchClient.isStarted()) {
            sinchClient.start();
        }
    }

    private void createNotificationChannel(int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sinch";
            String description = "Incoming Sinch Push Notifications.";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

