package com.benefit.welfare.Activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.benefit.welfare.R;

public class TestActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getName();

    private String[] items = {"금액을 선택해 주세요"};
    Spinner spinner, spinner2, spinner3, spinner4, spinner5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_welf_calculator);

        spinner = findViewById(R.id.spinner_test);
        spinner2 = findViewById(R.id.spinner_test2);
        spinner3 = findViewById(R.id.spinner_test3);
        spinner4 = findViewById(R.id.spinner_test4);
        spinner5 = findViewById(R.id.spinner_test5);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner3.setAdapter(adapter);
        spinner4.setAdapter(adapter);
        spinner5.setAdapter(adapter);
    }
}