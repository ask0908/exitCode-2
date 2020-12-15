package com.psj.welfare.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.psj.welfare.R;

public class PushSettingActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private Switch push_noti_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_setting);

        push_noti_switch = findViewById(R.id.push_noti_switch);
        toolbar = findViewById(R.id.push_setting_toolbar);
        toolbar.setTitle("알림");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // 툴바 왼쪽의 뒤로가기 아이콘을 누르면 이전 화면으로 이동시키기 위한 처리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}