package com.pridetechnologies.businesscard.SendNotificationPack;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_KEY;
import static com.pridetechnologies.businesscard.Sinch.SinchService.APP_SECRET;
import static com.pridetechnologies.businesscard.Sinch.SinchService.ENVIRONMENT;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pridetechnologies.businesscard.Common.Common;
import com.pridetechnologies.businesscard.HomeActivity;
import com.pridetechnologies.businesscard.R;
import com.pridetechnologies.businesscard.Sinch.IncomingCallScreenActivity;
import com.pridetechnologies.businesscard.Sinch.SinchService;
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

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    public static String CHANNEL_ID = "Sinch Push Notification Channel";

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
    String title,message;


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        title = remoteMessage.getData().get("Title");
        message = remoteMessage.getData().get("Message");


        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.card_round)
                .setContentText(message)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri);
        //.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        notificationManager.notify(j, builder.build());



        /*FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null && sent.equals(fUser.getUid()))
        {
            if (!savedCurrentUser.equals(user)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    sendOreoAndAboveNotification(remoteMessage);
                }
                else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }*/
        Map data = remoteMessage.getData();
        if(foregrounded())
        {
            return;
        }

        if (SinchHelpers.isSinchPushPayload(data)) {

            //NotificationResult result = sinchClient.relayRemotePushNotificationPayload(data);
            //initsinch();
            new ServiceConnection() {
                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (payload != null) {
                        SinchService.SinchServiceInterface sinchService = (SinchService.SinchServiceInterface) service;
                        if (sinchService != null) {

                            NotificationResult result = sinchService.relayRemotePushNotificationPayload(payload);
                            // handle result, e.g. show a notification or similar
                            // here is example for notifying user about missed/canceled call:
                            if (result.isValid() && result.isCall()) {
                                CallNotificationResult callResult = result.getCallResult();
                                if (callResult != null && result.getDisplayName() != null) {
                                    Common.saveUserData(MyFireBaseMessagingService.this,"user_name",callResult.getRemoteUserId());
                                }
                                if (callResult.isCallCanceled()) {
                                    String displayName = result.getDisplayName();
                                    if (displayName == null) {
                                        //displayName = sharedPreferences.getString(callResult.getRemoteUserId(),"n/a");
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        createMissedCallNotification(displayName != null && !displayName.isEmpty() ? displayName : callResult.getRemoteUserId());
                                    }
                                }
                            }
                        }
                    }
                    payload = null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    System.out.println("disconnestedd");
                }

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

    private void createMissedCallNotification(String userId) {

        //createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), HomeActivity.class), 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("Missed Business Card Call ")
                        .setContentText(userId)
                        .setContentIntent(contentIntent)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, builder.build());
    }

    /*private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, UserRequestActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("key", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0){
            j=i;
        }
        notificationManager.notify(j, builder.build());
    }

    private void sendOreoAndAboveNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, UserRequestActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("key", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getNotification(title, body, pendingIntent, defSoundUri, icon);

        int j = 0;
        if (i>0){
            j=i;
        }
        notification1.getManager().notify(j, builder.build());
    }*/

}
