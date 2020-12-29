package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;

/* 스플래시 액티비티, 쉐어드를 초기화한 후 안에 저장된 사용자 기본 정보가 없으면 GetUserInformationActivity로 이동한다
* 기본 정보가 있고 이미 로그인 돼 있으면 MainTabLayoutActivity로 이동하며, 재로그인 시 뷰페이저는 gone으로 돌린다
* 폰트가 iOS와 달라서 이 부분 수정 필요 */
public class SplashActivity extends AppCompatActivity
{
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("app_pref", 0);

        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(), 3000);
    }

    private class SplashHandler implements Runnable
    {
        @Override
        public void run()
        {
            // 사용자 기본 정보(나이, 성별, 지역)가 없으면 설정하도록 기본 정보를 받는 화면으로 이동한다
            if (sharedPreferences.getString("user_area", "").equals("") || sharedPreferences.getString("user_nickname", "").equals("")
            || sharedPreferences.getString("user_gender", "").equals(""))
            {
                startActivity(new Intent(getApplication(), GetUserInformationActivity.class));
                SplashActivity.this.finish();
            }
            else
            {
                // 사용자 기본 정보가 있으면 MainTabLayoutActivity로 이동해 로그인할 수 있도록 한다
                startActivity(new Intent(getApplication(), MainTabLayoutActivity.class));
                SplashActivity.this.finish();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        // 스플래시 화면에서 넘어가기 전까지 백버튼을 눌러도 아무것도 일어나지 않게 한다
    }
}