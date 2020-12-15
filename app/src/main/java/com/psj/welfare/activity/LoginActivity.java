package com.psj.welfare.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 로그인 액티비티는 SNS 로그인(구글, 네이버)과 너의 혜택은 자체 로그인이 있다
 * 로그인 컨셉 구상중....
 * */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "LoginActivity";
    SharedPreferences preferences;
    SharedPreferences app_pref;

    public LoginActivity loginContext;

    // 카카오 로그인하는 커스텀 버튼
    private CustomLoginButton fake_kakao;
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
    String server_token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Logger.addLogAdapter(new AndroidLogAdapter());

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
        // 커스텀된 카카오 로그인 버튼
        fake_kakao = findViewById(R.id.fake_kakao);

        // 파이어베이스 FCM 디바이스 토큰값을 확인하는 로직
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if (!task.isSuccessful())
                {
                    Log.w("FCM LOG", "getInstanceId failed", task.getException());
                    return;
                }
                token = task.getResult().getToken();
                Log.e(TAG, "FCM token = " + token);
            }
        });

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

    /* 구글 로그인 처리하는 메서드 */
    private void resultLogin(final GoogleSignInAccount account)
    {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                // 로그인이 성공했을 때
                if (task.isSuccessful())
                {
                    Log.e(TAG, "구글 로그인 성공");
                    String FamilyName = account.getFamilyName();
                    String DisplayName = account.getDisplayName();
                    String GivenName = account.getGivenName();
                    final String email = account.getEmail();
                    Log.e(TAG, "FamilyName : " + FamilyName + "\tDisplayName : " + DisplayName + "\tGivenName : " + GivenName + "\t이메일 : " + email);
                    idToken = account.getIdToken();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                    Log.e(TAG, "account.getIdToken() 결과 " + idToken);
                    Call<String> call = apiInterface.googleUser("GOOGLE", email, DisplayName, idToken);
                    call.enqueue(new Callback<String>()
                    {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                        {
                            if (response.isSuccessful() && response.body() != null)
                            {
                                Log.e(TAG, "onResponse 구글 성공 : " + response.body());
                                String googleInfo = response.body();
                                // 구글 로그인을 이미 시도했는지 아니면 처음으로 하는지 확인
                                SharedPreferences preferences = getSharedPreferences(getString(R.string.login_shared), MODE_PRIVATE);

                                String result_server = preferences.getString("serverToken", "NoServerToken");
                                String result_state = preferences.getString("state", "NoState");
                                String result_sns = preferences.getString("snsToken", "NoSNSToken");
                                String result_email = preferences.getString("email", "NoEmail");

                                if (result_server.equals("NoServerToken") && result_state.equals("1"))
                                {
                                    Log.e(TAG, "and_login.php 전송");
                                    // 이미 구글 sns 로그인을 시도해서 and_login.php 경로로 전송하는 로직
                                    reLogin(email, idToken);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("snsToken", idToken);
                                    editor.putString("email", email);
                                    editor.commit();
                                }
                                else
                                {
                                    // 처음으로 구글 sns 로그인을 시도해서 and_register.php 경로로 전송하는 로직
                                    jsonParsing(googleInfo, email);
                                    /* 사용자 정보(나이, 성별, 닉네임)를 입력받기 위해 액티비티 이동. 기존에 입력했던 정보가 있다면 MainTablayoutActivity로 이동한다 */
//                                    Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                                    Intent intent = new Intent(LoginActivity.this, GetUserInformationActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
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
                else
                { // 로그인이 실패했을 때
                    Log.e(TAG, "로그인 실패");
                }
            }
        });
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
                    app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
                    SharedPreferences.Editor editor = app_pref.edit();
                    Log.e(TAG, "result = " + result);
                    String age = app_pref.getString("age", "");
                    String area = app_pref.getString("area", "");
                    String gender = app_pref.getString("gender", "");
                    String user_nickname = app_pref.getString("user_nickname", "");
                    Log.e(TAG, "쉐어드의 age = " + age + ", 지역 = " + area + ", 성별 = " + gender + ", 닉네임 = " + user_nickname);
                    // 카카오 로그인 성공 시 실행할 처리
//                    Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                    /* 사용자 정보(나이, 성별, 지역, 닉네임)를 입력받기 위해 이동한다. 기존에 입력된 정보가 있으면 MainTabLayoutActivity로 이동한다 */
                    if (app_pref.getString("user_age", "").equals("") && app_pref.getString("user_area", "").equals("") &&
                    app_pref.getString("user_gender", "").equals("") && app_pref.getString("user_nickname", "").equals(""))
                    {
                        // 기존 정보가 없을 경우
                        Intent intent = new Intent(LoginActivity.this, GetUserInformationActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                        editor.putString(getString(R.string.get_kakao_image), result.getProfileImagePath());
                        editor.putString(getString(R.string.get_kakao_name), result.getNickname());
                        editor.apply();
                        intent.putExtra("name", result.getNickname());
                        intent.putExtra("profile", result.getProfileImagePath());
                        intent.putExtra("email", result.getKakaoAccount().getEmail());
                        if (result.getKakaoAccount().needsScopeAccountEmail())
                        {
                            // 현재 이메일에 체크하지 않아서(이메일 정보 제공에 동의하지 않아서) true가 나온다
                            String email_result = result.getKakaoAccount().getEmail();
                            Logger.e("email_result = " + email_result);
                        }
                        /* 서버로 osType, platform, FCM 토큰값 정보를 보내는 메서드 */
                        sendUserTypeAndPlatform();
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

    /* 서버로 osType, platform, FCM 토큰값 정보를 보내는 메서드 */
    void sendUserTypeAndPlatform()
    {
        String osType = getString(R.string.login_os);
        String platform = getString(R.string.main_platform);
        String fcm_token = token;
        String email = getString(R.string.email2);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.sendUserTypeAndPlatform(osType, platform, fcm_token, email);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    tokenParsing(response.body());
                }
                else
                {
                    Logger.e("onResponse() 실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.e("에러 = " + t.getMessage());
            }
        });
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
                // 로그인 결과 값 출력
                resultLogin(googleSignInAccount);
            }
        }

    }

    /* 첫 로그인, 다시 로그인한 사람을 구분하는 로직. 필요없어서 주석 처리 */
    // 로그인 후 서버에서 난독화된 토큰값을 쉐어드에 저장하는 기능
    // 난독화된 토큰값을 쉐어드에 저장하는 이유
    // 유저 정보를 알아내어 서비스를 이용하는 것을 막기 위함
    // 난독화된 토큰값이 존재하면 로그인 상태인지 비로그인 상태인지 체크할 수 있다
    private void loginSharedSave(String serverToken, String email)
    {
        preferences = getSharedPreferences(getString(R.string.login_shared), MODE_PRIVATE);

        // 데이터를 저장 or 편집하기 위해 Editor 변수를 선언한다
        SharedPreferences.Editor editor = preferences.edit();
        Log.e(TAG, "구글 서버 토큰 값 : " + idToken);
        Log.e(TAG, "네이버 서버 토큰 값 : " + n_oAuthLoginInstance.getAccessToken(this));
        if (idToken != null)
        {
            editor.putString("snsToken", idToken);
        }
        else
        {
            editor.putString("snsToken", n_oAuthLoginInstance.getAccessToken(this));
        }
        // key 값과 연결되는 value 값을 저장한다

        editor.putString("serverToken", serverToken);
        editor.putString("email", email);
        editor.putString("state", "1");

        // 메모리에 있는 데이터를 저장장치에 저장한다
        editor.commit();

    }

    // 로그인 상태를 확인하기 위한 쉐어드에 저장된 토큰값 불러오기 기능
    private void loginSharedGet()
    {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.login_shared), MODE_PRIVATE);

        String result_sns = preferences.getString("snsToken", "NoSNSToken");
        String result_email = preferences.getString("email", "NoEmail");
        String result_server = preferences.getString("serverToken", "NoServerToken");
        String result_state = preferences.getString("state", "NoState");
        Log.e(TAG, "loginSharedGet : " + result_server);

        if (result_server.equals("NoServerToken") && result_sns.equals("NoSNSToken") && result_email.equals("NoEmail") && result_state.equals("NoState"))
        {
            Log.e(TAG, "첫 로그인을 진행해주세요!");
        }
        else if (!result_server.equals("NoServerToken") && !result_email.equals("NoEmail"))
        {
            Log.e(TAG, "로그인 되어있는 사용자 입니다!");
            Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
            startActivity(intent);
            finish();
        }
        else if (result_server.equals("NoServerToken") && result_state.equals("1"))
        {
            Log.e(TAG, "로그아웃 한 사용자 입니다!\n서버 토큰 값을 다시 요청해야 합니다!");
        }
    }

    // 로그인 응답 내용이 json 구조라서 파싱을 하기 위한 기능
    private void jsonParsing(String login, String email)
    {
        try
        {
            JSONObject login_total = new JSONObject(login);
            String retCode_data, token_data;

            retCode_data = login_total.getString("retCode");
            Log.e(TAG, "retCode 내용 : " + retCode_data);

            if (retCode_data.equals("0"))
            {
                token_data = login_total.getString("token");
                Log.e(TAG, "token 내용 : " + token_data);

                loginSharedSave(token_data, email);
            }
            else if (retCode_data.equals("406"))
            {
                Log.e(TAG, "이미 로그인한 상태입니다!");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void tokenParsing(String data)
    {
        app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        SharedPreferences.Editor editor = app_pref.edit();

        try
        {
            JSONObject token_object = new JSONObject(data);
            server_token = token_object.getString("Token");
            editor.putString("token", server_token);
            editor.commit();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Logger.e("token = " + server_token);
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