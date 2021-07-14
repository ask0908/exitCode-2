package com.psj.welfare.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.psj.welfare.R;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.activity.SplashActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private SharedPreferences sharedPreferences;

    public static final String TAG = "[FCM Service]";
    String channelID = "ch_push";
    String token;

    private SharedSingleton sharedSingleton;

    /* 푸시 알림을 받았을 때 */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        sharedSingleton = SharedSingleton.getInstance(getApplicationContext());
        token = sharedSingleton.getToken();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        boolean isPushDisabled = sharedPreferences.getBoolean("fcm_canceled", false);
        String msg, title, icon;
        if (remoteMessage.getNotification() != null)
        {
            msg = remoteMessage.getNotification().getBody();
            title = remoteMessage.getNotification().getTitle();
            icon = remoteMessage.getNotification().getIcon();
            if (isPushDisabled)
            {
                // true면 fcm 푸시 받도록 설정
                showNotification(title, msg, icon);
            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    manager.deleteNotificationChannel(channelID);
                }
            }
        }
    }

    // 푸시 알람이 오면 화면에 띄우고 클릭 시 해당 혜택의 상세보기 화면으로 이동시키는 메서드
    public void showNotification(String title, String message, String icon)
    {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("push_clicked", "noti_intent");
        intent.setAction("com.psj.welfare.push");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        changePushStatus();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.etc)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelName = "ch_pushName";
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token)
    {
        super.onNewToken(token);
    }

    void changePushStatus()
    {
        if (sharedPreferences.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = sharedSingleton.getToken();
        }
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.changePushStatus(token, "customizedRecv");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                }
                else
                {
                    Log.e(TAG, "수신 상태값 변경 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

}
