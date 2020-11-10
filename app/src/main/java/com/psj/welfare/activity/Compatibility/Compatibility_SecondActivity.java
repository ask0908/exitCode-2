package com.psj.welfare.activity.Compatibility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.psj.welfare.R;

import java.util.ArrayList;

public class Compatibility_SecondActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    ImageView second_question_image;
    Button second_question_first_btn, second_question_second_btn;
    // FirstActivity에서 날아온 ArrayList의 값을 담을 ArrayList
    ArrayList<String> second_arraylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__second);

        init();

        // Compatibility_FirstActivity에서 날아온 인텐트 안에 들어있는 ArrayList의 데이터를 이 액티비티의 ArrayList로 옮겨담는다
        Intent first_intent = getIntent();
        second_arraylist.addAll((ArrayList<String>)first_intent.getSerializableExtra("list"));
        Log.e(TAG, "list 안의 값 = " + second_arraylist);

        // 버튼을 누르면 ArrayList에 나라 이름을 넣고 Compatibility_ThirdActivity 액티비티로 이동한다
        second_question_first_btn.setOnClickListener(v -> {
            second_arraylist.add("미국");
            Log.e(TAG, "2번째 list에 미국 삽입 : " + second_arraylist);
            Intent intent = new Intent(Compatibility_SecondActivity.this, Compatibility_ThirdActivity.class);
            intent.putExtra("second_list", second_arraylist);
            startActivityForResult(intent, 3);
        });

        second_question_second_btn.setOnClickListener(v -> {
            second_arraylist.add("한국");
            Log.e(TAG, "2번째 list에 한국 삽입 : " + second_arraylist);
            Intent intent = new Intent(Compatibility_SecondActivity.this, Compatibility_ThirdActivity.class);
            intent.putExtra("second_list", second_arraylist);
            startActivityForResult(intent, 4);
        });
    }

    /* findViewById() 모아놓은 메서드 */
    private void init()
    {
        second_question_image = findViewById(R.id.second_question_image);
        second_question_first_btn = findViewById(R.id.second_question_first_btn);
        second_question_second_btn = findViewById(R.id.second_question_second_btn);
    }

    /* Compatibility_ThirdActivity에서 리턴된 값과 boolean 값에 따라서, 가장 최근에 선택했던 버튼과 연결된 나라 이름을 ArrayList에서 삭제한다 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "3번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                second_arraylist.remove("미국");
                Log.e(TAG, "2번 액티비티에서 백버튼 눌려서 리스트에서 값 삭제된 후 ArrayList : " + second_arraylist);
            }
        }
        else if (requestCode == 4 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "4번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                second_arraylist.remove("한국");
                Log.e(TAG, "2번 액티비티에서 백버튼 눌려서 리스트에서 값 삭제된 후 ArrayList : " + second_arraylist);
            }
        }
        else
        {
            Log.e(TAG, "RESULT_OK가 아니거나 data가 null");
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent return_intent = new Intent();
        return_intent.putExtra("hasBackPressed", true);
        setResult(RESULT_OK, return_intent);
        finish();
    }
}