package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.psj.welfare.R;

/* 스플래시 액티비티, 쉐어드를 초기화한 후 안에 저장된 사용자 기본 정보가 없으면 GetUserInformationActivity로 이동한다
* 기본 정보가 있고 이미 로그인 돼 있으면 MainTabLayoutActivity로 이동하며, 재로그인 시 뷰페이저는 gone으로 돌린다
* 로그인과 기본정보 검사를 여기서 실행 */
@Keep
public class SplashActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    SharedPreferences sharedPreferences;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        FirebaseApp.initializeApp(this);

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

//        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>()
//        {
//            @Override
//            public void onSuccess(InstanceIdResult instanceIdResult)
//            {
//                String tokens = instanceIdResult.getToken();
//                Log.e("addOnSuccessListener", "새로 만든 토큰 = " + tokens);
//                editor.putString("fcm_token", token);
//                editor.apply();
//            }
//        });
        /**/
        // 스플래시 화면에서 FCM 토큰을 발급받는다. 여기서 한번 토큰을 받았으니 다른 액티비티에서 추가로 토큰을 생성하지 못하게 한다
        // 생성된 토큰값은 쉐어드에 저장한 뒤 메인 화면을 타고 로그인 화면으로 넘어갔을 때 거기서 꺼내 사용한다
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if (!task.isSuccessful())
                {
                    Log.e("FCM LOG", "getInstanceId failed", task.getException());
                    return;
                }
                token = task.getResult().getToken();
                Log.e(TAG, "FCM token = " + token);
                editor.putString("fcm_token", token);
                editor.apply();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(), 3000);
    }

    private class SplashHandler implements Runnable
    {
        @Override
        public void run()
        {
            startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
            SplashActivity.this.finish();

//            // 사용자 기본 정보(나이, 성별, 지역)가 없으면 로그인 후 설정하도록 로그인 화면으로 이동한다
//            if (sharedPreferences.getString("user_area", "").equals("") || sharedPreferences.getString("user_nickname", "").equals("")
//            || sharedPreferences.getString("user_gender", "").equals(""))
//            {
//                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                SplashActivity.this.finish();
//            }
//            else
//            {
//                // 사용자 기본 정보가 있으면 MainTabLayoutActivity로 이동한다
//                startActivity(new Intent(getApplication(), MainTabLayoutActivity.class));
//                SplashActivity.this.finish();
//            }
        }
    }

    @Override
    public void onBackPressed()
    {
        // 스플래시 화면에서 넘어가기 전까지 백버튼을 눌러도 아무것도 일어나지 않게 한다
    }
}