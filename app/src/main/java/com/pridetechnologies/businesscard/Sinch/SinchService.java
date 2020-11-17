package com.pridetechnologies.businesscard.Sinch;

import static com.pridetechnologies.businesscard.Sinch.IncomingCallScreenActivity.ACTION_ANSWER;
import static com.pridetechnologies.businesscard.Sinch.IncomingCallScreenActivity.ACTION_IGNORE;
import static com.pridetechnologies.businesscard.Sinch.IncomingCallScreenActivity.EXTRA_ID;
import static com.pridetechnologies.businesscard.Sinch.IncomingCallScreenActivity.MESSAGE_ID;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pridetechnologies.businesscard.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;

import java.util.List;
import java.util.Map;

public class SinchService  extends Service {

    public static final String APP_KEY = "4410a09d-1415-4387-be26-2ad9c6054618";  //add your keys
    public static final String APP_SECRET = "o6Veh2Pag0SXf71O5JRctg==";
    //public static final String ENVIRONMENT = "sandbox.sinch.com";
    public static final String ENVIRONMENT = "clientapi.sinch.com";

    public static final int MESSAGE_PERMISSIONS_NEEDED = 1;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PESMISSION";
    public static final String MESSENGER = "MESSENGER";
    private Messenger messenger;


    public static final String LOCATION = "LOCATION";
    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private SinchServiceInterface mSinchServiceInterface=new SinchServiceInterface();
    public SinchClient mSinchClient;
    public String mUserId;
    private StartFailedListener mListener;

    private DatabaseReference AdminRef;


    @Override
    public void onCreate() {
        super.onCreate();

        AdminRef = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    @Override
    public void onDestroy() {
        if(mSinchClient!=null && mSinchClient.isStarted())
        {
            mSinchClient.terminate();
        }
        super.onDestroy();
    }

    private void start(String userName){
        if(mSinchClient==null)
        {
            mUserId=userName;
            mSinchClient=Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(userName)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT).build();
            mSinchClient.setSupportCalling(true);
            mSinchClient.startListeningOnActiveConnection();
            mSinchClient.setSupportManagedPush(true);
            mSinchClient.addSinchClientListener(new MySinchClientListener());
            mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
            mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
            mSinchClient.start();
        }
        try {
            //mandatory checks
            mSinchClient.checkManifest();
            //auxiliary check
            if (getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw new MissingPermissionException(Manifest.permission.CAMERA);
            }
        } catch (MissingPermissionException e) {
            if (messenger != null) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(REQUIRED_PERMISSION, e.getRequiredPermission());
                message.setData(bundle);
                message.what = MESSAGE_PERMISSIONS_NEEDED;
                try {
                    messenger.send(message);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
    private void stop()
    {
        if(mSinchClient!=null)
        {
            mSinchClient.terminate();
            mSinchClient=null;
        }
    }
    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }
    @Override
    public IBinder onBind(Intent intent)     {
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder {

        public Call callVideo(String userId) {
            return mSinchClient.getCallClient().callUserVideo(userId);
        }

        /*public Call callUser(String userId) {
            return mSinchClient.getCallClient().callUser(userId);
        }*/

        public Call callUser(String userId, Map<String, String> headers) {
            return mSinchClient.getCallClient().callUser(userId, headers);
        }

        public String getUserName() {
            return mUserId;
        }


        public boolean isStarted() {
            return SinchService.this.isStarted();
        }

        public void startClient(String userName) {
            start(userName);
        }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient.getCallClient().getCall(callId);
        }

        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }

        /* public NotificationResult relayRemotePushNotificationPayload(final Map payload){
             if(mSinchClient!=null ){
                 startClient(getUserName());
             }else if(mSinchClient==null )
             {
                 System.out.println("can't start sinchclient no username");
             }
             return mSinchClient.relayRemotePushNotificationPayload(payload);
         }*/
        public NotificationResult relayRemotePushNotificationPayload(final Map payload) {

            if (mSinchClient == null && !mUserId.isEmpty()) {
                start(mUserId);
            } else if (mSinchClient == null && mUserId.isEmpty()) {
                Log.e(TAG, "Can't start a SinchClient as no username is available, unable to relay push.");
                return null;
            }
            return mSinchClient.relayRemotePushNotificationPayload(payload);
        }
    }
    public interface StartFailedListener{
        void onStartFailed(SinchError error);
        void onStarted();
    }
    private class MySinchClientListener implements SinchClientListener{

        @Override
        public void onClientStarted(SinchClient client) {
            Log.d(TAG,"Sinchclient started");
            if(mListener!=null)
            {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient sinchClient) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if(mListener!=null)
            {
                mListener.onStartFailed(error);
            }
            mSinchClient.terminate();
            mSinchClient=null;
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }
    }
    private class SinchCallClientListener implements CallClientListener {

        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            Log.d(TAG, "onIncomingCall: " + call.getCallId());


            Intent intent;
            if (call.getDetails().isVideoOffered()) {
                intent = new Intent(getApplicationContext(), IncomingVideoCallActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), IncomingCallScreenActivity.class);
            }

            intent.putExtra(EXTRA_ID, MESSAGE_ID);
            intent.putExtra(CALL_ID, call.getCallId());

            boolean inForeground = isAppOnForeground(getApplicationContext());

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!inForeground) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !inForeground) {
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(MESSAGE_ID, createIncomingCallNotification(call.getHeaders().get("name"), intent));
            } else {
                SinchService.this.startActivity(intent);
            }
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        private Bitmap getBitmap(Context context, int resId) {
            int largeIconWidth = (int) context.getResources()
                    .getDimension(R.dimen.notification_large_icon_width);
            int largeIconHeight = (int) context.getResources()
                    .getDimension(R.dimen.notification_large_icon_height);
            Drawable d = context.getResources().getDrawable(resId);
            Bitmap b = Bitmap.createBitmap(largeIconWidth, largeIconHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, largeIconWidth, largeIconHeight);
            d.draw(c);
            return b;
        }

        private PendingIntent getPendingIntent(Intent intent, String action) {
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 111, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return pendingIntent;
        }

        @TargetApi(29)
        private Notification createIncomingCallNotification(String userName, Intent fullScreenIntent) {

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 112, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext(), FcmListenerService.CHANNEL_ID)
                            .setContentTitle("Incoming Business Card Call")
                            .setContentText(userName+" is Calling")
                            .setSmallIcon(R.drawable.card_round)
                            .setAutoCancel(true)
                            .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                            .setColorized(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(Notification.CATEGORY_CALL)
                            .setContentIntent(pendingIntent)
                            .setFullScreenIntent(pendingIntent, true)
                            .addAction(R.drawable.button_decline, "Ignore", getPendingIntent(fullScreenIntent, ACTION_IGNORE))
                            .addAction(R.drawable.button_accept, "Answer",  getPendingIntent(fullScreenIntent, ACTION_ANSWER))
                            .setOngoing(true);

            return builder.build();

        }
    }
}
