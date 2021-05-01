package com.psj.welfare;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.activity.MainTabLayoutActivity;

public class TutorialWelcome extends AppCompatActivity {

    private Button BtnTutorial; //튜토리얼 하기 버튼
    private TextView BtnMain; //메인으로 가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_welcome);

        BtnTutorial = findViewById(R.id.BtnTutorial); //튜토리얼 하기 버튼
        BtnMain = findViewById(R.id.BtnMain); //메인으로 가기 버튼

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //튜토리얼 액티비티로 이동
        BtnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialWelcome.this, TutorialCategory.class);
            startActivity(intent);
            finish();
        });

        //메인으로 이동
        BtnMain.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialWelcome.this, MainTabLayoutActivity.class);
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