package com.psj.welfare.activity.Compatibility;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* SecondFragment에서 테스트 시작 버튼을 누르면 가장 먼저 나타나는 문제 화면 */
public class Compatibility_FirstActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView first_question_text;
    ImageView first_question_image;
    Button first_question_fist_btn, first_question_second_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__first);

        getFirstProblem();

        first_question_text = findViewById(R.id.first_question_text);
        first_question_image = findViewById(R.id.first_question_image);
        first_question_fist_btn = findViewById(R.id.first_question_first_btn);
        first_question_second_btn = findViewById(R.id.first_question_second_btn);

        // 지금은 이미지를 누르면 다음 액티비티로 이동하게 한다
        first_question_image.setOnClickListener(v -> {
            Intent intent = new Intent(Compatibility_FirstActivity.this, Compatibility_SecondActivity.class);
            startActivity(intent);
        });

        // 버튼을 누르면 어떤 처리를 할지는 아직 미정
        // 구색만 갖춰 놓는다
        first_question_fist_btn.setOnClickListener(v -> {
            Toast.makeText(this, "1번 버튼 클릭", Toast.LENGTH_SHORT).show();
        });

        first_question_second_btn.setOnClickListener(v -> {
            Toast.makeText(this, "2번 버튼 클릭", Toast.LENGTH_SHORT).show();
        });
    }

    // 서버에서 문제와 버튼에 넣을 텍스트를 가져오는 메서드
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