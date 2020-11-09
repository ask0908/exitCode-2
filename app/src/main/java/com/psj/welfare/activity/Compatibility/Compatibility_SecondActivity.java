package com.psj.welfare.activity.Compatibility;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.psj.welfare.R;

public class Compatibility_SecondActivity extends AppCompatActivity
{
    ImageView second_question_image;
    Button second_question_first_btn, second_question_second_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__second);

        second_question_image = findViewById(R.id.second_question_image);
        second_question_first_btn = findViewById(R.id.second_question_first_btn);
        second_question_second_btn = findViewById(R.id.second_question_second_btn);

        // 지금은 이미지를 누르면 액티비티 이동
        second_question_image.setOnClickListener(v -> {
            Intent intent = new Intent(Compatibility_SecondActivity.this, Compatibility_ThirdActivity.class);
            startActivity(intent);
        });

        // 버튼을 누르면 어떤 처리를 할지는 아직 미정
        // 구색만 갖춰 놓는다
        second_question_first_btn.setOnClickListener(v -> {
            Toast.makeText(this, "위 버튼 클릭", Toast.LENGTH_SHORT).show();
        });

        second_question_second_btn.setOnClickListener(v -> {
            Toast.makeText(this, "아래 버튼 클릭", Toast.LENGTH_SHORT).show();
        });
    }
}