package com.psj.welfare.activity.Compatibility;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.psj.welfare.R;

import java.util.ArrayList;

public class Compatibility_ThirdActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    ImageView third_question_image;
    Button third_question_first_btn, third_question_second_btn;
    // SecondActivity에서 날아온 ArrayList의 값을 담을 ArrayList
    ArrayList<String> third_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__third);

        Intent third_intent = getIntent();
        third_list.addAll((ArrayList<String>)third_intent.getSerializableExtra("second_list"));
        Log.e(TAG, "2번째 액티비티에서 받은 list = " + third_list);

        third_question_image = findViewById(R.id.third_question_image);
        third_question_first_btn = findViewById(R.id.third_question_first_btn);
        third_question_second_btn = findViewById(R.id.third_question_second_btn);

        // 버튼을 누르면 ArrayList에 나라 이름을 넣고 다음 액티비티로 이동한다
        third_question_first_btn.setOnClickListener(v -> {
            third_list.add("미국");
            Log.e(TAG, "3번째 list = " + third_list);
            Intent intent = new Intent(Compatibility_ThirdActivity.this, Compatibility_ResultActivity.class);
            intent.putExtra("third_list", third_list);
            startActivity(intent);
        });

        third_question_second_btn.setOnClickListener(v -> {
            third_list.add("러시아");
            Log.e(TAG, "3번째 list = " + third_list);
            Intent intent = new Intent(Compatibility_ThirdActivity.this, Compatibility_ResultActivity.class);
            intent.putExtra("third_list", third_list);
            startActivity(intent);
        });
    }

    /* 이곳(3번째 액티비티)에서 백버튼을 누르면 Compatibility_SecondActivity로 이동한다. 이 때 3 or 4 중 하나의 값과 true 값을 Compatibility_SecondActivity로 같이 보낸다 */
    @Override
    public void onBackPressed()
    {
        Intent return_intent = new Intent();
        return_intent.putExtra("hasBackPressed", true);
        setResult(RESULT_OK, return_intent);
        finish();
    }

}