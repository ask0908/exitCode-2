package com.psj.welfare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.activity.MainTabLayoutActivity;

public class TutorialWelcome extends AppCompatActivity {

    private FirebaseAnalytics analytics; //firebase 애널리틱스

    private Button BtnTutorial; //튜토리얼 하기 버튼
    private TextView BtnMain; //메인으로 가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_welcome);

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
            Toast.makeText(TutorialWelcome.this,"미리보기 건너뛰기",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TutorialWelcome.this, MainTabLayoutActivity.class);

            //미리보기 했는지
            SharedPreferences shared = getSharedPreferences("welf_preview",MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            editor.putBoolean("being_preview",true); //미리보기 건너뛰기를 했거나 미리보기 화면에 들어갔다면
            editor.apply();

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

}