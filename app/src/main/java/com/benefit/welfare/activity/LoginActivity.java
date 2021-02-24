package com.benefit.welfare.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.benefit.welfare.R;
import com.benefit.welfare.API.ApiClient;
import com.benefit.welfare.API.ApiInterface;
import com.benefit.welfare.Util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 카카오, 구글 로그인 진행하는 액티비티 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    public final String TAG = this.getClass().getSimpleName();
    SharedPreferences app_pref;

    public LoginActivity loginContext;

    // 카카오 로그인하는 커스텀 버튼
    private Button fake_kakao;
    private com.kakao.usermgmt.LoginButton real_kakao;
    // 카카오 로그인 시 필요한 세션 콜백
    private SessionCallback sessionCallback;

    private ImageView loginClose;

    // 네이버 로그인 API 객체
    public static OAuthLoginButton n_oAuthLoginButton;
    public static OAuthLogin n_oAuthLoginInstance;

    // 파이어베이스 인증 객체
    private FirebaseAuth firebaseAuth;
    // 구글 API 클라이언트 객체 (deprecated 돼서 다른 것으로 바꿈)
    private GoogleSignInClient googleSignInClient;
    // 구글 로그인 했을 때 결과 코드
    private static final int REQ_SIGN_GOOGLE = 100;
    // 구글 로그인한 유저 토큰값
    FirebaseUser currentUser;
    // 구글 아이디 토큰값, FCM 토큰값 저장 변수
    String idToken, token;
    // 구글 로그인 버튼
    SignInButton google_login_btn;

    // 로그인 시 서버에서 주는 토큰
    String server_token, nickName;
    // 카카오 로그인 시 확인할 수 있는 카카오 이메일을 담기 위한 변수
    String kakao_email;
    // 로그아웃 상태 체크용 변수
    boolean loggedOut;

    String encode_str, action_str;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Logger.addLogAdapter(new AndroidLogAdapter());

        // GetUserInformationActivity, ChoiceKeywordActivity 체크 위한 쉐어드 처리
        app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        // 스플래시 화면에서 받은 FCM 토큰
        token = app_pref.getString("fcm_token", "");

        // 서버로 사용자 로그 전송
        userLog("로그인 화면 진입");

        if (getIntent().hasExtra("loggedOut"))
        {
            Intent intent = getIntent();
            loggedOut = intent.getBooleanExtra("loggedOut", false);
            Log.e(TAG, "로그아웃 상태 : " + loggedOut);
        }

        google_login_btn = findViewById(R.id.google_login_btn);

        // 구글 로그인 버튼에 써진 텍스트를 변경한다
        setGooglePlusButtonText(google_login_btn, getString(R.string.login_google));

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

        // 화면 우상단의 X 버튼
        loginClose = findViewById(R.id.loginClose);
        // 카카오 로그인 버튼
        fake_kakao = findViewById(R.id.fake_kakao);

        // LoginActivity context 저장
        loginContext = LoginActivity.this;

        // 카카오 키 해시 생성
//        String keyhash = com.kakao.util.helper.Utility.getKeyHash(this);
//        Log.e(TAG, "keyhash = " + keyhash);

        // 카카오 로그인 시작. onClick(View v) 참고
        fake_kakao.setOnClickListener(this);

        /* 구글 로그인 */
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, options);
        firebaseAuth = FirebaseAuth.getInstance();

        // 구글 로그인 버튼 클릭
        google_login_btn.setOnClickListener(v ->
        {
            Intent google_intent = googleSignInClient.getSignInIntent();
            startActivityForResult(google_intent, REQ_SIGN_GOOGLE);
        });

    }

    void setGooglePlusButtonText(SignInButton signInButton, String buttonText)
    {
        for (int i = 0; i < signInButton.getChildCount(); i++)
        {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView)
            {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTextSize(20);
                return;
            }
        }
    }

    /* 카카오 커스텀 로그인 버튼 클릭 리스너. 여기에서 카카오 로그인 로직이 실행된다 */
    @Override
    public void onClick(View v)
    {
        // 카카오로 로그인 버튼 클릭했을 때, 이미 카카오 로그인을 진행했다면
        switch (v.getId())
        {
            case R.id.fake_kakao:
                Log.e(TAG, "fake_kakao 클릭");
                userLog("카카오 로그인 버튼 클릭");
                // performClick() : 정의되어 있던 뷰의 클릭 리스너를 호출하는 메서드. 클릭과 관련된 모든 액션을 수행한다
                boolean what = real_kakao.performClick();
                // 이곳에 오는 boolean 값은 항상 true다
                if (what)
                {
                    /* 카카오 로그인을 처음 진행할 때 간편 로그인, 다른 계정으로 로그인 중에서 선택할 수 있고 사용자 동의를 요청한다 */
                    // 카카오 SDK를 쓰기 위해 SessionCallback 객체화
                    sessionCallback = new SessionCallback();

                    // 현재 Session 객체를 가져와서 상태 체크 후, 세션 상태 변화 콜백을 받기 위해 SessionCallback 콜백을 등록한다
                    // 인자로 들어가는 sessionCallback은 추가할 세션 콜백이다
                    Session.getCurrentSession().addCallback(sessionCallback);

                    // 세션이 isOpenable 상태라면 SessionCallback 객체를 통해 로그인 시도
                    // 요청 결과는 KakaoAdapter의 ISessionCallback으로 전달된다
                    Session.getCurrentSession().checkAndImplicitOpen();
                }
                else
                {
                    Log.e(TAG, "카카오 버튼 클릭안됨");
                }
                break;

            default:
                break;
        }
    }

    /* 카카오 로그아웃 처리하는 메서드 */
    private void kakaoLogout()
    {
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback()
        {
            @Override
            public void onCompleteLogout()
            {
                Log.e(TAG, "로그아웃 성공");
            }
        });
    }

    /* 카카오 회원탈퇴 메서드. AlertDialog를 통해서 회원탈퇴를 진행한다 */
    private void kakaoUnlink()
    {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this).setMessage(appendMessage).setPositiveButton(getString(R.string.com_kakao_ok_button), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback()
                {
                    @Override
                    public void onFailure(ErrorResult errorResult)
                    {
                        Log.e("다이얼로그 안", "회원탈퇴 실패");
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult)
                    {
                        Log.e("다이얼로그 안", "onSessionClosed");
                    }

                    @Override
                    public void onNotSignedUp()
                    {
                        Log.e("다이얼로그 안", "onNotSignedUp");
                    }

                    @Override
                    public void onSuccess(Long userId)
                    {
                        Log.e("다이얼로그 안", "onSuccess");
                    }
                });
                dialog.dismiss();
            }
        }).setNegativeButton(getString(R.string.com_kakao_cancel_button), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        }).show();
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
                    Log.e(TAG, "result = " + result);
                    String age = app_pref.getString("user_age", "");
                    String area = app_pref.getString("user_area", "");
                    String gender = app_pref.getString("user_gender", "");
                    String user_nickname = app_pref.getString("user_nickname", "");
                    Log.e(TAG, "쉐어드의 age = " + age + ", 지역 = " + area + ", 성별 = " + gender + ", 닉네임 = " + user_nickname);
                    /* 사용자 정보(나이, 성별, 지역, 닉네임)를 입력받기 위해 이동한다. 기존에 입력된 정보가 있으면 MainTabLayoutActivity로 이동한다 */
                    if (app_pref.getBoolean("logout", false))
                    {
                        Intent login_intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                        login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        sendUserTypeAndPlatform();
                        startActivity(login_intent);
                        finish();
                    }
                    else
                    {
//                        Intent intent = new Intent(LoginActivity.this, GetUserInformationActivity.class);
                        Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                        editor.putString(getString(R.string.get_kakao_image), result.getProfileImagePath());
                        editor.putString(getString(R.string.get_kakao_name), result.getNickname());
                        editor.putString("kakao_email", result.getKakaoAccount().getEmail());
                        editor.apply();
                        // logout이 true라면 로그아웃한 상태에서 로그인하는 거니까 서버에서 받은 닉네임 값을 쉐어드에 저장하고 메인 화면으로 이동시킨다
                        // 이렇게 메인으로 이동한 경우 로그인했을 때처럼 맞춤 혜택을 보여줘야 한다
                        intent.putExtra("name", result.getNickname());
                        intent.putExtra("profile", result.getProfileImagePath());
                        intent.putExtra("email", result.getKakaoAccount().getEmail());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        /* 서버로 osType, platform, FCM 토큰값 정보를 보내는 메서드 */
                        sendUserTypeAndPlatform();
                        // sendUserTypeAndPlatform() 처리 후 기본 정보 입력받는 화면으로 이동
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
        Log.e("sendUserTypeAndPlatform()", "쉐어드의 FCM 토큰값 = " + token);
        String os_type = getString(R.string.login_os);
        String platform = getString(R.string.main_platform);

        encode("카카오 로그인 시도");
        String session = app_pref.getString("sessionId", "");
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
                    tokenParsing(response.body());
                    Log.e(TAG, "카카오 로그인 성공 = " + result);
                }
                else
                {
                    Log.e(TAG, "실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Logger.e("에러 = " + t.getMessage());
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
        String action = encodeAction(user_action);
        Call<String> call = apiInterface.userLog(token, session, "login", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "로그인 화면 로그 전송 결과 = "  + result);
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

    /* 서버로 한글을 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 한다. 이 때 한글을 인코딩하는 메서드 */
    private String encodeAction(String str)
    {
        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    class GoogleLogOut extends Thread
    {
        @Override
        public void run()
        {
            super.run();
            Log.e(TAG, "구글 로그아웃 스레드 실행!");
            firebaseAuth.getInstance().signOut();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // 구글 서버 토큰 존재하는지 확인
        currentUser = firebaseAuth.getCurrentUser();
        Log.e(TAG, "G_currentUser(구글 서버 토큰) : " + currentUser);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // X 버튼 클릭
        loginClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "loginClose click");
                Intent l_Intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                startActivity(l_Intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // 카카오 로그인 후 액티비티 꺼질 때 필요한 처리
        // 현재 액티비티 제거 시 콜백도 같이 제거
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    // 구글 로그인 인증을 요청 했을 때 결과 값을 되돌려 받는 곳
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

        /* 구글 로그인할 때 인텐트로 데이터 받아와 처리하는 부분. 필요없어서 주석 처리 */
        // 구글 로그인 액티비티에서 넘어온 경우일 때 실행
        if (requestCode == REQ_SIGN_GOOGLE)
        {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // 인증결과가 성공적일 때
            if (googleSignInResult.isSuccess())
            {
                // googleSignInAccount는 구글 로그인 정보를 담고 있다 (닉네임, 프로필사진Url, 이메일주소 등)
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
            }
        }

    }

    /* 로그인 시 서버에서 넘어오는 토큰값이 있는데 이걸 저장해야 한다 */
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
        Log.e(TAG, "tokenParsing() - 쉐어드에 저장");
        /* 다른 액티비티/프래그먼트에서도 활용해야 하기 때문에 서버에서 받은 토큰값을 쉐어드에 저장해 사용한다 */
        editor.putString("token", server_token);
        Log.e(TAG, "nickName = " + nickName);
        // logout이 true라면 로그아웃한 상태에서 로그인하는 거니까 서버에서 받은 닉네임 값을 쉐어드에 저장하고 메인 화면으로 이동시킨다
        // 이렇게 메인으로 이동한 경우 로그인했을 때처럼 맞춤 혜택을 보여줘야 한다
        if (app_pref.getBoolean("logout", false))
        {
            editor.putString("user_nickname", nickName);
            editor.putBoolean("logout", false);
        }
        editor.apply();
    }

    /* 다시 구글 로그인할 때 사용하는 메서드 */
    public void reLogin(String email, String token)
    {
        Log.e(TAG, "reLogin() 진입");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "이메일 : " + email + "\n토큰 : " + token);
        Call<String> call = apiInterface.ReUser(email, token);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "onResponse 재로그인 성공 : " + response.body());
                    String relogin = response.body();
                    try
                    {
                        JSONObject login_total = new JSONObject(relogin);
                        String token_data;
                        token_data = login_total.getString("token");

                        SharedPreferences preferences = getSharedPreferences(getString(R.string.login_shared), MODE_PRIVATE);

                        // 데이터를 저장 or 편집하기 위해 Editor 변수를 선언한다
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("serverToken", token_data);
                        editor.apply();

                        editor.commit();
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                    startActivity(intent);
                    finish();

                }
                else
                {
                    Log.e(TAG, "onResponse 실패");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "onFailure : " + t.toString());

            }
        });
    }

}