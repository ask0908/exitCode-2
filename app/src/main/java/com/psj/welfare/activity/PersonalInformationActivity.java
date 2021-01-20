package com.psj.welfare.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.psj.welfare.R;

/* 개인정보 처리방침 화면 */
public class PersonalInformationActivity extends AppCompatActivity
{
    Toolbar personal_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        personal_toolbar = findViewById(R.id.personal_information_toolbar);
        setSupportActionBar(personal_toolbar);
        getSupportActionBar().setTitle("개인정보 처리방침");
    }
}