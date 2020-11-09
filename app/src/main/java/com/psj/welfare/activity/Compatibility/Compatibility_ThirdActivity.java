package com.psj.welfare.activity.Compatibility;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.psj.welfare.R;

public class Compatibility_ThirdActivity extends AppCompatActivity
{
    ImageView third_question_image;
    Button third_question_first_btn, third_question_second_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__third);

        third_question_image = findViewById(R.id.third_question_image);
        third_question_first_btn = findViewById(R.id.third_question_first_btn);
        third_question_second_btn = findViewById(R.id.third_question_second_btn);

        // 이미지뷰를 누르면 다음 화면으로 이동한다
        third_question_image.setOnClickListener(v -> {
            Intent intent = new Intent(Compatibility_ThirdActivity.this, Compatibility_ResultActivity.class);
            startActivity(intent);
        });

        // 버튼 처리는 아직 미구현
        third_question_first_btn.setOnClickListener(v -> {
            Toast.makeText(this, "위 버튼 클릭", Toast.LENGTH_SHORT).show();
        });

        third_question_second_btn.setOnClickListener(v -> {
            Toast.makeText(this, "아래 버튼 클릭", Toast.LENGTH_SHORT).show();
        });
    }
}