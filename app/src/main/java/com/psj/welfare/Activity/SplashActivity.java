package com.psj.welfare.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.R;
import com.psj.welfare.Util.LogUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 스플래시 액티비티, 쉐어드를 초기화한 후 안에 저장된 사용자 기본 정보가 없으면 GetUserInformationActivity로 이동한다
 * 기본 정보가 있고 이미 로그인 돼 있으면 MainTabLayoutActivity로 이동하며, 재로그인 시 뷰페이저는 gone으로 돌린다
 * 로그인과 기본정보 검사를 여기서 실행 */
@Keep
public class SplashActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    String token, sessionId, encode_str;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 스플래시 화면에서 FCM 토큰을 발급받는다. 여기서 한번 토큰을 받았으니 다른 액티비티에서 추가로 토큰을 생성하지 않는다
        // 생성된 토큰값은 쉐어드에 저장한 뒤 메인 화면을 타고 로그인 화면으로 넘어갔을 때 등 필요한 때 꺼내 사용한다
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

        userLog();

        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(), 3000);
    }

    private class SplashHandler implements Runnable
    {
        @Override
        public void run()
        {
            /* 핸드폰 비행기 모드(인터넷 연결 상태) 체크 */
            boolean isConnected = isNetworkConnected(SplashActivity.this);
            if (!isConnected)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("인터넷 연결이 원활하지 않습니다")
                        .setCancelable(false)
                        .setPositiveButton("종료", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finishAffinity();
                            }
                        }).show();
            }
            else
            {
                startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
                SplashActivity.this.finish();
            }
        }
    }

    /* 사용자 행동 로그 메서드, 처음 앱에 접속할 경우에만 호출되야 하니까 유저가 처음 접속하는지 or 다시 접속하는지 구분해야 한다 */
    void userLog()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        Call<String> call = apiInterface.userLog(token, null, "main", "앱 접속", null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "세션 id값 확인 : " + result);
                    sessionParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    /* 서버에서 받은 세션 id값 파싱 */
    private void sessionParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            sessionId = jsonObject.getString("SessionId");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // 세션 id 가져오는 것 확인
        Log.e("sessionParsing()", "스플래시에서 받은 세션 id = " + sessionId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sessionId", sessionId);
        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        // 이 메서드를 재정의하고 비워두면 스플래시 화면에서 백버튼을 눌러도 뒤로가기가 되지 않는다
    }

    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   // 핸드폰
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);       // 와이파이
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);     // 태블릿
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected(); // wimax 상태 체크
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            // 모바일 네트워크 체크
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            // wifi 네트워크 체크
            {
                return true;
            }
        }
        return false;
    }
}