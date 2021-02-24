package com.benefit.welfare.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.benefit.welfare.R;

public class MyInfoUpdateActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    Toolbar info_update_toolbar;
    EditText info_update_email, info_update_name, info_update_birth, info_update_phone;
    TextView logout_textview, break_away_textview;
    private SharedPreferences app_pref;
    String server_token;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_info_update);

        Logger.addLogAdapter(new AndroidLogAdapter());

        app_pref = getSharedPreferences("app_pref", 0);
        server_token = app_pref.getString("token", "");

        init();

        setSupportActionBar(info_update_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        info_update_toolbar.setTitle("개인정보 수정");
    }

    private void init()
    {
        info_update_toolbar = findViewById(R.id.info_update_toolbar);
        info_update_email = findViewById(R.id.info_update_email);
        info_update_name = findViewById(R.id.info_update_name);
        info_update_birth = findViewById(R.id.info_update_birth);
        info_update_phone = findViewById(R.id.info_update_phone);
        logout_textview = findViewById(R.id.logout_textview);
        break_away_textview = findViewById(R.id.break_away_textview);
    }

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