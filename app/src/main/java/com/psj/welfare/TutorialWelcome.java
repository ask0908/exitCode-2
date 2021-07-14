package com.psj.welfare;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;
import com.psj.welfare.activity.MainTabLayoutActivity;
import com.psj.welfare.util.UnCatchTaskService;

public class TutorialWelcome extends AppCompatActivity {

    private FirebaseAnalytics analytics; //firebase 애널리틱스

    private Button BtnTutorial; //튜토리얼 하기 버튼
    private TextView BtnMain; //메인으로 가기 버튼

    //로그인관련 쉐어드 singleton
    private SharedSingleton sharedSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_welcome);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // 상태바 글자색 검정색으로 바꾸기
        //쉐어드 싱글톤 사용
        sharedSingleton = SharedSingleton.getInstance(this);

        /* 강제종료 확인하는 서비스 실행
         * 보통 앱이 어느 지점에서 강제종료됐는지 확인하려면 먼저 앱의 시작점부터 서비스를 시작해야 한다
         * 앱의 시작점은 스플래시 화면이지만 스플래시보다 여기부터 서비스를 작동시켜서 확인하는 게 더 낫다고 생각했다 */
//        startService(new Intent(this, UnCatchTaskService.class));
        Logger.d("TutorialWelcome에서 UnCatchTaskService가 실행중인가? : " + isMyServiceRunning(UnCatchTaskService.class));

        analytics = FirebaseAnalytics.getInstance(this); //firebase 애널리틱스

        BtnTutorial = findViewById(R.id.BtnTutorial); //튜토리얼 하기 버튼
        BtnMain = findViewById(R.id.BtnMain); //메인으로 가기 버튼

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //튜토리얼 액티비티로 이동
        BtnTutorial.setOnClickListener(v -> {
            Bundle bundle = new Bundle(); //firebase 애널리틱스
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "미리보기 액티비티 이동"); //firebase 애널리틱스
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle); //firebase 애널리틱스

            Intent intent = new Intent(TutorialWelcome.this, TutorialCategory.class);
            startActivity(intent);
            finish();
        });

        //메인으로 이동
        BtnMain.setOnClickListener(v -> {
            Toast.makeText(TutorialWelcome.this,"메인으로 가기",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TutorialWelcome.this, MainTabLayoutActivity.class);

            //미리보기 했다는 정보 입력 (건너뛰기 포함)
            sharedSingleton.setBooleanPreview(true);
//            SharedPreferences sharedPreferences = getSharedPreferences("welf_preview", 0);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("being_preview", true); //미리보기 건너뛰기를 했거나 미리보기 화면에 들어갔다면
//            editor.apply();

            startActivity(intent);
            finish();
        });
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize(){
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)


        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        BtnTutorial.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/15);
    }

    /* 서비스가 실행중인지 확인해서 T/F를 리턴하는 메서드 */
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }
}