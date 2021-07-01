package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 카카오 로그인 진행하는 액티비티 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    public final String TAG = this.getClass().getSimpleName();
    SharedPreferences app_pref;

    // 카카오 로그인하는 커스텀 버튼
    private Button fake_kakao;
    private com.kakao.usermgmt.LoginButton real_kakao;
    // 카카오 로그인 시 필요한 세션 콜백
    private SessionCallback sessionCallback;

    // FCM 토큰값 저장 변수
    String token;

    // 로그인 시 서버에서 주는 토큰
    String server_token, nickName;
    // 카카오 로그인 시 확인할 수 있는 카카오 이메일을 담기 위한 변수
    String kakao_email;
    // 로그아웃 상태 체크용 변수
    boolean loggedOut;

    // 혜택모아 텍스트
    TextView app_logo_text;
    TextView login_activity_explanation_textview;

    String encode_str;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    DBOpenHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바 배경 흰색으로 설정

        helper = new DBOpenHelper(this);
        helper.openDatabase();
        helper.create();

        analytics = FirebaseAnalytics.getInstance(this);

        app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        // 스플래시 화면에서 받은 FCM 토큰
        token = app_pref.getString("fcm_token", "");

        // 서버로 사용자 로그 전송
        userLog(getString(R.string.login_entry));

        if (getIntent().hasExtra("loggedOut"))
        {
            Intent intent = getIntent();
            loggedOut = intent.getBooleanExtra("loggedOut", false);
        }

        /* 앱 실행 시 카카오 서버의 로그인 토큰이 있으면 자동으로 로그인 수행  */
        // 카카오 SDK를 쓰기 위해 SessionCallback 객체화
        sessionCallback = new SessionCallback();

        // 현재 Session 객체를 가져와서 상태 체크 후, 세션 상태 변화 콜백을 받기 위해 SessionCallback 콜백을 등록한다
        // 인자로 들어가는 sessionCallback은 추가할 세션 콜백이다
        Session.getCurrentSession().addCallback(sessionCallback);

        // 세션이 isOpenable 상태라면 SessionCallback 객체를 통해 로그인 시도
        // 요청 결과는 KakaoAdapter의 ISessionCallback으로 전달된다
        Session.getCurrentSession().checkAndImplicitOpen();

        real_kakao = findViewById(R.id.real_kakao);
        // 카카오 로그인 버튼
        fake_kakao = findViewById(R.id.fake_kakao);
        // 혜택모아 텍스트
        app_logo_text = findViewById(R.id.app_logo_text);
        login_activity_explanation_textview = findViewById(R.id.login_activity_explanation_textview);

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        app_logo_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 7);

        // 카카오 로그인 시작. 195번 줄의 onClick() 참고
        fake_kakao.setOnClickListener(this);

    }

    /* 카카오 커스텀 로그인 버튼 클릭 리스너. 여기에서 카카오 로그인 로직이 실행된다 */
    @Override
    public void onClick(View v)
    {
        // 카카오로 로그인 버튼 클릭했을 때, 이미 카카오 로그인을 진행했다면
        switch (v.getId())
        {
            case R.id.fake_kakao:
                userLog("카카오 로그인 버튼 클릭");
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "카카오 로그인 버튼 클릭");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                // performClick() : 정의되어 있던 뷰의 클릭 리스너를 호출하는 메서드. 클릭과 관련된 모든 액션을 수행한다
                boolean what = real_kakao.performClick();
                break;

            default:
                break;
        }
    }

    // 카카오 로그인 버튼 클릭 시 로직
    private class SessionCallback implements ISessionCallback
    {
        @Override
        public void onSessionOpened()
        {
            UserManagement.getInstance().me(new MeV2ResponseCallback()
            {
                // 로그인 실패 시
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    int result = errorResult.getErrorCode();

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE)
                    {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다 : " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult)
                {
                    Toast.makeText(getApplicationContext(), "세션이 닫혔습니다. 다시 시도해 주세요 : " + errorResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }

                // 로그인 성공 시
                @Override
                public void onSuccess(MeV2Response result)
                {
                    // 카카오 계정에 저장된 이메일을 가져온다
                    if (result.getKakaoAccount().hasEmail() == OptionalBoolean.TRUE)
                    {
                        kakao_email = result.getKakaoAccount().getEmail();
                    }
                    app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
                    SharedPreferences.Editor editor = app_pref.edit();
                    /* 사용자 정보(관심사)를 입력받기 위해 이동한다. 기존에 입력된 정보가 있으면 MainTabLayoutActivity로 이동한다 */
                    if (!app_pref.getString("interest", "").equals(""))
                    {
                        Intent login_intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                        editor.putString(getString(R.string.get_kakao_name), result.getNickname());
                        editor.putString("kakao_email", result.getKakaoAccount().getEmail());
                        editor.putBoolean("is_leaved", false);
                        editor.apply();
                        login_intent.putExtra("name", result.getNickname());
                        login_intent.putExtra("email", result.getKakaoAccount().getEmail());
                        login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        sendUserTypeAndPlatform();
                        login_intent.putExtra("user_token", server_token);
                        startActivity(login_intent);
                        finish();
                    }
                    // 사용자 정보가 없으면 정보 받는 화면으로 이동한다
                    else if (app_pref.getString("interest", "").equals(""))
                    {
                        Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                        editor.putString(getString(R.string.get_kakao_image), result.getProfileImagePath());
                        editor.putString(getString(R.string.get_kakao_name), result.getNickname());
                        editor.putString("kakao_email", result.getKakaoAccount().getEmail());
                        editor.apply();
                        intent.putExtra("name", result.getNickname());
                        intent.putExtra("profile", result.getProfileImagePath());
                        intent.putExtra("email", result.getKakaoAccount().getEmail());
                        sendUserTypeAndPlatform();
                        intent.putExtra("user_token", server_token);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }

        // 로그인 세션이 정상적으로 열리지 않았을 때
        @Override
        public void onSessionOpenFailed(KakaoException e)
        {
            Toast.makeText(getApplicationContext(), "로그인 도중 세션 오류가 발생했습니다. 인터넷 연결을 확인해주세요: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /* 카카오 로그인 시 서버로 osType, platform, FCM 토큰값 정보를 보내는 메서드 */
    void sendUserTypeAndPlatform()
    {
        String email = kakao_email;
        String os_type = getString(R.string.login_os);
        String platform = getString(R.string.main_platform);

        encode("카카오 로그인 시도");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.sendUserTypeAndPlatform(email, token, os_type, platform);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    tokenParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    /* 로그인 화면으로 들어오면 로그인 화면에 들어왔다는 로그를 만들어 보낸다 */
    void userLog(String user_action)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        app_pref = getSharedPreferences("app_pref", 0);
        String token;
        if (app_pref.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = app_pref.getString("token", "");
        }
        String session = app_pref.getString("sessionId", "");
        encode(user_action);
        Call<String> call = apiInterface.userLog(token, session, "login", encode_str, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //
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

    /* 화면이 완전히 종료되면 카카오 로그인 세션을 종료한다 */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    // 카카오 로그인 인증을 요청 했을 때 결과 값을 되돌려 받는 곳
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // 카카오 로그인 시 필요한 처리
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    /* 로그인 시 서버에서 넘어오는 토큰값을 저장하는 메서드 */
    private void tokenParsing(String data)
    {
        // 서버에서 발급받은 토큰값을 저장할 쉐어드 준비
        app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        SharedPreferences.Editor editor = app_pref.edit();
        try
        {
            JSONObject token_object = new JSONObject(data);
            server_token = token_object.getString("Token");
            nickName = token_object.getString("nickName");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* 다른 액티비티/프래그먼트에서도 활용해야 하기 때문에 서버에서 받은 토큰값을 쉐어드에 저장해 사용한다 */
        editor.putString("token", server_token);

        /* SQLite에 저장 */
        helper.insertColumn(server_token);

        // logout이 true라면 로그아웃한 상태에서 로그인하는 거니까 서버에서 받은 닉네임 값을 쉐어드에 저장하고 메인 화면으로 이동시킨다
        // 이렇게 메인으로 이동한 경우 로그인했을 때처럼 맞춤 혜택을 보여줘야 한다
        if (app_pref.getBoolean("logout", false))
        {
            editor.putString("user_nickname", nickName);
            editor.putBoolean("logout", false);
        }
        editor.apply();
    }
}