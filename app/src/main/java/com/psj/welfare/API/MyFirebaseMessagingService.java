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
import com.psj.welfare.activity.MainTabLayoutActivity;
import com.psj.welfare.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private SharedPreferences sharedPreferences;

    public static final String TAG = "[FCM Service]";
    String channelID = "ch_push";
    String token;

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
        // 푸시 알림을 누르면 메인 화면으로 이동한다. MainTabLayoutActivity에서 처음 프래그먼트가 메인 화면이기 때문에 이 액티비티로 이동하면 바로 메인화면이 보인다
        Intent intent = new Intent(this, MainTabLayoutActivity.class);
        // 호출하는 Activity가 스택에 있을 경우, 해당 Activity를 최상위로 올리면서 그 위에 있던 Activity들을 모두 삭제하는 Flag
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("push_clicked", "noti_intent");
        /* putExtra()로 해서 안되면 아래 코드로 시도해보기 */
//        Intent broadcast = new Intent("broadcaster");
//        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);

        // PendingIntent.FLAG_ONE_SHOT : pendingIntent 일회용, 원래 이걸 사용했으나 특정 프래그먼트를 띄우기 위해 FLAG_UPDATE_CURRENT를 사용함
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 푸시 아이템을 눌러 화면을 이동할 때 수신 알림값을 변경한다
        changePushStatus();

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

    /* 사용자가 알림 받으면 상태값을 바꾸는 메서드 */
    void changePushStatus()
    {
        sharedPreferences = getSharedPreferences("app_pref", 0);
        if (sharedPreferences.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = sharedPreferences.getString("token", "");
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
                    Log.e(TAG, "수신 상태값 변경 성공 : " + result);
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
