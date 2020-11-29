package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;

/* 지역별 혜택 지도 액티비티 */
public class MapActivity extends AppCompatActivity
{
    Button sejong_and_daejeon_benefit_btn, jeonnam_benefit_btn, seoul_benefit_btn, incheon_benefit_btn, gyongi_benefit_btn, gangwon_benefit_btn,
            choongnam_benefit_btn, jeonbuk_benefit_btn, choongbuk_benefit_btn, jeju_benefit_btn, gyongbuk_benefit_btn, gyongnam_benefit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init();
        btnClickListener();

    }

    /* 버튼 클릭 리스너들 모아놓은 메서드 */
    private void btnClickListener()
    {
        // 인천
        incheon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "인천");
            startActivity(intent);
        });

        // 서울
        seoul_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "서울");
            startActivity(intent);
        });

        // 경기
        gyongi_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경기");
            startActivity(intent);
        });

        // 강원
        gangwon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "강원");
            startActivity(intent);
        });

        // 충남
        choongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충남");
            startActivity(intent);
        });

        // 충북
        choongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충북");
            startActivity(intent);
        });

        // 세종, 대전
//        sejong_and_daejeon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
//            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
//            intent.putExtra("area", "세종, 대전");
//            startActivity(intent);
//        });

        // 경북
        gyongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경북");
            startActivity(intent);
        });

        // 전북
        jeonbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전북");
            startActivity(intent);
        });

        // 전남
        jeonnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전남");
            startActivity(intent);
        });

        // 경남
        gyongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경남");
            startActivity(intent);
        });

        // 제주
        jeju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "제주");
            startActivity(intent);
        });
    }

    /* findViewById() 모아놓은 메서드 */
    private void init()
    {
//        sejong_and_daejeon_benefit_btn = findViewById(R.id.sejong_and_daejeon_benefit_btn);
        jeonnam_benefit_btn = findViewById(R.id.jeonnam_benefit_btn);
        seoul_benefit_btn = findViewById(R.id.seoul_benefit_btn);
        incheon_benefit_btn = findViewById(R.id.incheon_benefit_btn);
        gyongi_benefit_btn = findViewById(R.id.gyongi_benefit_btn);
        gangwon_benefit_btn = findViewById(R.id.gangwon_benefit_btn);
        choongnam_benefit_btn = findViewById(R.id.choongnam_benefit_btn);
        jeonbuk_benefit_btn = findViewById(R.id.jeonbuk_benefit_btn);
        choongbuk_benefit_btn = findViewById(R.id.choongbuk_benefit_btn);
        jeju_benefit_btn = findViewById(R.id.jeju_benefit_btn);
        gyongbuk_benefit_btn = findViewById(R.id.gyongbuk_benefit_btn);
        gyongnam_benefit_btn = findViewById(R.id.gyongnam_benefit_btn);
    }
}