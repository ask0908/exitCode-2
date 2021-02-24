package com.psj.welfare.API;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService
{
    private final String TAG = "MyFirebaseInstanceIDService";
    private static final String SUBSCRIBE_TO = "urbenefit";

    public MyFirebaseInstanceIDService()
    {
    }

    // 시스템이 토큰을 새로고침할 때마다 호출되게 한다. 이렇게 하는 이유는 보안 때문인가?
    // 일반적으로 사용자가 앱을 설치, 재설치, 앱 데이터 삭제 시 호출
    // 장치간에 알림을 보내므로 각 사용자는 고유한 user_id를 써서 주제를 구독해야 한다
    // 이를 통해 사용자는 user_id와 일치하는 주제로 전송된 알림을 받을 수 있다
     /*
          This method is invoked whenever the token refreshes
          OPTIONAL: If you want to send messages to this application instance or manage this apps subscriptions on the server side, you can send this token to your server.
          -> 이 함수는 토큰을 새로고침할 때마다 호출된다
          선택 사항 : 이 애플리케이션 인스턴스에 메시지를 보내거나 서버 측에서 이 앱 구독을 관리하려는 경우, 이 토큰을 서버로 보낼 수 있다
        */
    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        String token = FirebaseInstanceId.getInstance().getToken();
//        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO);
        Log.e("FCM 토큰 서비스", "onNewToken : " + token);
    }
}
