package com.benefit.welfare.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.benefit.welfare.R;

public class CalculatorResultActivity extends AppCompatActivity
{
    TextView textView, result_body;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator_result);

        textView = findViewById(R.id.result_title);
        result_body = findViewById(R.id.result_body);

        SpannableString spannableString = new SpannableString(textView.getText().toString());
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#6f52e8")), 9, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);

        SpannableString string = new SpannableString(result_body.getText().toString());
        string.setSpan(new ForegroundColorSpan(Color.parseColor("#6f52e8")), 23, 79, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result_body.setText(string);
    }
}