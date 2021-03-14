package com.psj.welfare.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.psj.welfare.R;

public class TermsAndConditionsActivity extends AppCompatActivity
{
    Toolbar terms_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        terms_toolbar = findViewById(R.id.terms_toolbar);
        setSupportActionBar(terms_toolbar);
        getSupportActionBar().setTitle("이용약관");
    }
}