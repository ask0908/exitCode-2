package com.psj.welfare.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;

/* 뷰페이저 안 쓰고 액티비티에서 탭으로 프래그먼트들을 제어하기 위해 만든 액티비티 */
public class AppMainTabActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main_tab);
    }
}