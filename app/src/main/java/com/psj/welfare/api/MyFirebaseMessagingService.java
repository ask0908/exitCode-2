package com.psj.welfare.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.psj.welfare.R;
import com.psj.welfare.activity.MainTabLayoutActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

	public static final String TAG = "[FCM Service]";

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Log.d(TAG, "onMessageReceived 실행!");

		String msg, title, icon;

		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "getBody : " + remoteMessage.getNotification().getBody());
			Log.d(TAG, "getTitle : " + remoteMessage.getNotification().getTitle());

			msg = remoteMessage.getNotification().getBody();
			title = remoteMessage.getNotification().getTitle();
			icon = remoteMessage.getNotification().getIcon();


			showNotification(title, msg, icon);
		} // 외부에서 온 메시지 조건문 종료

	}

	public void showNotification(String title, String message, String icon) {
		Intent intent = new Intent(this, MainTabLayoutActivity.class);
		// 호출하는 Activity가 스택에 있을 경우, 해당 Activity를 최상위로 올리면서, 그 위에 있던 Activity들을 모두 삭제하는 Flag
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		// PendingIntent.FLAG_ONE_SHOT : pendingIntent 일회용
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		String channelID = "ch_push";
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelID)
				.setSmallIcon(R.drawable.etc)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(true)
				.setSound(defaultSoundUri)
				.setContentIntent(pendingIntent);

//		try {
//			URL url = new URL(icon);
//			//아이콘 처리
//			Bitmap bigIcon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//			notificationBuilder.setLargeIcon(bigIcon);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		// 푸시 알림을 보내기 위해 시스템에 권한을 요청하여 생성
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String channelName = "ch_pushName";
			NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(channel);
		} // 버전 체크 조건문 종료

		// 푸시 알림 보내기
		notificationManager.notify(0, notificationBuilder.build());
	}

	@Override
	public void onNewToken(@NonNull String token) {
		super.onNewToken(token);
		Log.d(TAG, "Refreshed token : " + token);

		// TODO : 여기에 새로 갱신된 토큰값을 서버에 보내 저장해야한다

	} // onNewToken end


} // MyFirebaseMessagingService class end
