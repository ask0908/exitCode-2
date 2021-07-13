package com.psj.welfare.kakao;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.BuildConfig;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class App extends Application
{
    /* volatile : 자바 변수를 메인 메모리에 저장하겠다는 걸 명시하는 키워드, 변수 값을 읽어올 때마다 CPU 캐시에서 찾는 게 아닌 메인 메모리에서 읽어온다
    * 변수 값을 write할 때도 메인 메모리에 작성한다
    * 왜 쓰는가? - volatile 변수를 안 쓰는 멀티 쓰레드 어플리케이션에선 Task를 수행하는 동안 성능 향상을 위해 메인 메모리에서 읽은 변수 값을 CPU 메모리에 저장하게 된다
    * 만약 멀티 쓰레드 환경에서 쓰레드가 변수값을 가져올 때 각각의 CPU 캐시에 저장된 값이 다르기 때문에 변수값 불일치 문제가 생길 수 있다
    * 언제 쓰는가? - 멀티 쓰레드 환경에서 하나의 쓰레드만 read/write 하고, 나머지 쓰레드가 read 하는 상황에서 사용한다
    * 여러 쓰레드가 write하는 상황에서 사용한다면 synchronized로 변수 값 읽기, 쓰기의 원자성(atomic)을 보장해야 함 */
    private static volatile App instance = null;

    private static class KakaoSDKAdapter extends KakaoAdapter
    {
        /**
         * Session Config에 대해서는 default값들이 존재한다.
         * 필요한 상황에서만 override해서 사용하면 됨.
         *
         * @return Session의 설정값.
         */
        // 카카오 로그인 세션을 불러올 때의 설정값을 설정하는 부분.
        public ISessionConfig getSessionConfig()
        {
            return new ISessionConfig()
            {
                @Override
                public AuthType[] getAuthTypes()
                {
                    return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
                    /* 로그인을 하는 방식을 지정하는 부분. AuthType은 다음 네 가지 방식이 있다.
                    KAKAO_TALK : 카카오톡으로 로그인, KAKAO_STORY: 카카오스토리로 로그인, KAKAO_ACCOUNT: 웹뷰를 통한 로그인,
                    KAKAO_TALK_EXCLUDE_NATIVE_LOGIN : 카카오톡으로만 로그인+계정 없으면 계정생성 버튼 제공
                    KAKAO_LOGIN_ALL : 모든 로그인방식 사용 가능. 정확히는, 카카오톡이나 카카오스토리가 있으면 그 쪽으로 로그인 기능을 제공하고
                    둘 다 없으면 웹뷰를 통한 로그인을 제공한다 */
                }

                @Override
                public boolean isUsingWebviewTimer()
                {
                    return false;
                    // SDK 로그인시 사용되는 WebView에서 pause와 resume시에 Timer를 설정하여 CPU소모를 절약한다.
                    // true 를 리턴할경우 webview로그인을 사용하는 화면서 모든 webview에 onPause와 onResume 시에 Timer를 설정해 주어야 한다.
                    // 지정하지 않을 시 false로 설정된다.
                }

                @Override
                public boolean isSecureMode()
                {
                    return false;
                    // 로그인시 access token과 refresh token을 저장할 때의 암호화 여부를 결정한다.
                }

                @Override
                public ApprovalType getApprovalType()
                {
                    return ApprovalType.INDIVIDUAL;
                    // 일반 사용자가 아닌 Kakao와 제휴된 앱에서만 사용되는 값으로, 값을 채워주지 않을경우 ApprovalType.INDIVIDUAL 값을 사용하게 된다.
                }

                @Override
                public boolean isSaveFormData()
                {
                    return true;
                    // Kakao SDK 에서 사용되는 WebView에서 email 입력폼의 데이터를 저장할지 여부를 결정한다.
                    // true일 경우, 다음번에 다시 로그인 시 email 폼을 누르면 이전에 입력했던 이메일이 나타난다.
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig()
        {
            return new IApplicationConfig()
            {
                @Override
                public Context getApplicationContext()
                {
                    return App.getGlobalApplicationContext();
                }
            };
        }
    }

    public static App getGlobalApplicationContext()
    {
        if (instance == null)
        {
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        }
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // ========================================================================================================================
        /* 여기에 로그가 어떻게 찍혀 나올지 커스텀한 다음 addLogAdapter()를 호출한 다음 액티비티 등에서 Logger.d()를 사용하면 로그가 찍혀나온다
        * 매니페스트의 <application> 에서 name 속성에 이미 이 클래스가 지정돼 있기 때문에 이런 처리가 가능하다
        * application - name : 앱의 어떤 컴포넌트(액티비티, 서비스 등)보다 먼저 객체화(실행)되는 서브 클래스 */

        // Logger 공식 깃허브 주소 : https://github.com/orhanobut/logger
        // FormatStrategy : 메시지를 출력, 저장하는 방법을 결정할 때 사용되는 클래스, Logger 라이브러리에 내장된 클래스
        // PrettyFormatStrategy : 추가 정보와 함께 로그 메시지 주변에 테두리를 그려주는 클래스
        FormatStrategy strategy = PrettyFormatStrategy.newBuilder()
                .tag("혜택모아 :: ")    // 로그 좌측에 찍혀지는 문장 (기본값 : PRETTY__LOGGER)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(strategy));
        Logger.addLogAdapter(new AndroidLogAdapter()
        {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag)
            {
                return BuildConfig.DEBUG;
            }
        });
        instance = this;
        // ========================================================================================================================

        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        instance = null;
    }
}
