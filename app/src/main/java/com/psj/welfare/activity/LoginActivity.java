package com.psj.welfare.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.psj.welfare.R;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.custom.CustomLoginButton;

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
    // 쉐어드를 전역에서 사용할 수 있게 하는 변수
    SharedPreferences preferences;

    public LoginActivity loginContext;

    // 카카오 로그인하는 커스텀 버튼
    private CustomLoginButton fake_kakao;
    private com.kakao.usermgmt.LoginButton real_kakao;
    // 카카오 로그인 시 필요한 세션 콜백
    private SessionCallback sessionCallback;

    private ImageView loginClose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                String token = task.getResult().getToken();
                Log.d(TAG, "FCM 토큰 : " + token);

                // 레트로핏 서버 URL 설정해놓은 객체 생성 후 GET, POST 같은 서버에 데이터를 보내기 위해서 생성합니다
                ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

                // 인터페이스 ApiService에 선언한 fcmToken()를 호출합니다
                Call<String> call = apiInterface.fcmToken("tkdwns3340@naver.com", token);
                call.enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful())
                        {
                            Log.i(TAG, "onResponse FCM 토큰 전달 성공 : " + response.body().toString());

                        }
                        else
                        {
                            Log.i(TAG, "onResponse 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.i(TAG, "onFailure : " + t.toString());

                    }
                }); // call enqueue end
            } // onComplete end
        }); // addOnCompleteListener end

        // LoginActivity context 저장
        loginContext = LoginActivity.this;

        String keyhash = com.kakao.util.helper.Utility.getKeyHash(this);
        Log.e(TAG, "keyhash = " + keyhash);

        // 카카오 로그인 시작. onClick(View v) 참고
        fake_kakao.setOnClickListener(this);

    } // onCreate end

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

                @Override
                public void onSuccess(MeV2Response result)
                {
                    Log.e("onSuccess() 내부", "result : " + result);
                    // 카카오 로그인 성공 시 실행할 처리
                    Intent intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
					intent.putExtra("name", result.getNickname());
					intent.putExtra("profile", result.getProfileImagePath());
					intent.putExtra("email", result.getKakaoAccount().getEmail());
                    Log.e("onSuccess() 내부", "닉네임 : " + result.getNickname());
                    Log.e("onSuccess() 내부", "프로필 이미지 : " + result.getProfileImagePath());
                    Log.e("onSuccess() 내부", "이메일 : " + result.getKakaoAccount().getEmail());
					startActivity(intent);
					finish();
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

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart 실행!");
    } // onStart end

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume start");

        // X 버튼 클릭
        loginClose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "loginClose click");
                Intent l_Intent = new Intent(LoginActivity.this, MainTabLayoutActivity.class);
                startActivity(l_Intent);
                finish();
            }
        });
    } // onResume end

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // 카카오 로그인 후 액티비티 꺼질 때 필요한 처리
        // 현재 액티비티 제거 시 콜백도 같이 제거
        Session.getCurrentSession().removeCallback(sessionCallback);
    } // onDestroy end

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
    } // onActivityResult end

} // LoginActivity class end