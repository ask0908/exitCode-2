package com.psj.welfare.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;

/* 처음 로그인할 때 작성했던 유저 정보를 수정 혹은 입력하지 않고 넘겼을 때 여기서 등록할 수 있게 하는 액티비티 */
public class ChangeUserInformationActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_information);
    }
}