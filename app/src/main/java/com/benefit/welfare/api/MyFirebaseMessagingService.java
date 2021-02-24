package com.benefit.welfare.API;

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
import com.benefit.welfare.R;
import com.benefit.welfare.Activity.MainTabLayoutActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private SharedPreferences sharedPreferences;

    public static final String TAG = "[FCM Service]";
    String channelID = "ch_push";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Log.e(TAG, "onMessageReceived 실행!");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        sharedPreferences = getSharedPreferences("app_pref", 0);
        boolean isPushDisabled = sharedPreferences.getBoolean("fcm_canceled", false);
        Log.e(TAG, "isPushDisabled = " + isPushDisabled);
        String msg, title, icon;

        if (remoteMessage.getNotification() != null)
        {
            Log.e(TAG, "getBody : " + remoteMessage.getNotification().getBody());
            Log.e(TAG, "getTitle : " + remoteMessage.getNotification().getTitle());

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

    public void showNotification(String title, String message, String icon)
    {
        Intent intent = new Intent(this, MainTabLayoutActivity.class);
        // 호출하는 Activity가 스택에 있을 경우, 해당 Activity를 최상위로 올리면서 그 위에 있던 Activity들을 모두 삭제하는 Flag
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent.FLAG_ONE_SHOT : pendingIntent 일회용
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.etc)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // 푸시 알림을 보내기 위해 시스템에 권한을 요청하여 채널 생성
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelName = "ch_pushName";
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 푸시 알림 보내기
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token)
    {
        super.onNewToken(token);
        Log.e(TAG, "Refreshed token : " + token);
    }

}
