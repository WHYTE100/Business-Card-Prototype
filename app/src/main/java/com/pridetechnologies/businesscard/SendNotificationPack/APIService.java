package com.pridetechnologies.businesscard.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAtBq1-pQ:APA91bHHF4Ho5h-TxzKK6U4MY89ImOb8KumG9X-TZL4SA110lmIvDsDKrdGa0-S__5oVnNNrsF9qhLVMnBHAPb23DtbW_wrm8pMTP5G_A2WwaUqu3SZgsvac99368-vl8UAERkKv-5Mr" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);
}

