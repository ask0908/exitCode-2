package com.benefit.welfare.Test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.benefit.welfare.R;
import com.benefit.welfare.activity.Compatibility.Compatibility_SecondActivity;
import com.benefit.welfare.api.ApiClient;
import com.benefit.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompatibilityTestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView first_question_text;
    ImageView first_question_image;
    // 버튼 클릭 시 버튼과 연결된 나라 이름을 ArrayList에 추가한다
    Button first_question_fist_btn, first_question_second_btn;

    // 나라 이름을 담고 결과 화면에서 나라 정보를 보여줄 때 활용할 ArrayList
    ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFirstProblem();
        setContentView(R.layout.activity_compatibility_test);

        first_question_text = findViewById(R.id.first_question_text);
        first_question_image = findViewById(R.id.first_question_image);
        first_question_fist_btn = findViewById(R.id.first_question_first_btn);
        first_question_second_btn = findViewById(R.id.first_question_second_btn);

        // 버튼을 누르면 ArrayList에 나라 이름을 넣고 다음 액티비티로 이동한다
        first_question_fist_btn.setOnClickListener(v -> {
            list.add("미국");
            Log.e(TAG, "arraylist 값 확인 = " + list.toString());
            Intent intent = new Intent(CompatibilityTestActivity.this, Compatibility_SecondActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 1);
        });

        first_question_second_btn.setOnClickListener(v -> {
            list.add("미국");
            Log.e(TAG, "arraylist 값 확인 = " + list.toString());
            Intent intent = new Intent(CompatibilityTestActivity.this, Compatibility_SecondActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 2);
        });
    }

    void getFirstProblem()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getFirstProblem("1");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String detail = response.body();
                    jsonParsing(detail);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 문자열로 날아온 JSON 값을 파싱해서 텍스트를 각 뷰에 set하는 메서드 */
    private void jsonParsing(String detail)
    {
        try
        {
            JSONObject jsonObject_total = new JSONObject(detail);
            // 테이블명을 그대로 변수명으로 쓰기로 했다
            String question, choice_1, choice_2;

            // 문자열에서 각 JSON 키값에 해당하는 value값을 빼 String 변수에 저장한 후
            question = jsonObject_total.getString("question");
            choice_1 = jsonObject_total.getString("choice_1");
            choice_2 = jsonObject_total.getString("choice_2");

            // 텍스트뷰와 버튼에 set한다
            first_question_text.setText(question);
            first_question_fist_btn.setText(choice_1);
            first_question_second_btn.setText(choice_2);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

}