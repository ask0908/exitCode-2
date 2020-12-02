package com.psj.welfare.activity.Compatibility;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* mbti 테스트 마지막 문제 화면 */
public class Compatibility_FourthActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView fourth_question_text;
    ImageView fourth_question_image;
    Button fourth_question_first_btn, fourth_question_second_btn;
    // ThirdActivity에서 날아온 ArrayList의 값을 담을 ArrayList
    ArrayList<String> fourth_arraylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFourthProblem();
        setContentView(R.layout.activity_compatibility__fourth);

        init();

        Intent third_intent = getIntent();
        fourth_arraylist.addAll((ArrayList<String>)third_intent.getSerializableExtra("third_list"));
        Log.e(TAG, "list 안의 값 = " + fourth_arraylist);

        fourth_question_first_btn.setOnClickListener(v -> {
            fourth_arraylist.add("덴마크");
            Log.e(TAG, "4번째 list에 덴마크 삽입 : " + fourth_arraylist);
            Intent intent = new Intent(Compatibility_FourthActivity.this, Compatibility_ResultActivity.class);
            intent.putExtra("fourth_list", fourth_arraylist);
            startActivity(intent);
        });

        fourth_question_second_btn.setOnClickListener(v -> {
            fourth_arraylist.add("덴마크");
            Log.e(TAG, "4번째 list에 덴마크 삽입 : " + fourth_arraylist);
            Intent intent = new Intent(Compatibility_FourthActivity.this, Compatibility_ResultActivity.class);
            intent.putExtra("fourth_list", fourth_arraylist);
            startActivity(intent);
        });
    }

    private void init()
    {
        fourth_question_text = findViewById(R.id.fourth_question_text);
        fourth_question_image = findViewById(R.id.fourth_question_image);
        fourth_question_first_btn = findViewById(R.id.fourth_question_first_btn);
        fourth_question_second_btn = findViewById(R.id.fourth_question_second_btn);
    }

    void getFourthProblem()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getFourthProblem("4");
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
            String question, choice_1, choice_2, snack_image, choice_1_country, choice_2_country;

            // 문자열에서 각 JSON 키값에 해당하는 value값을 빼 String 변수에 저장한 후
            question = jsonObject_total.getString("question");
            choice_1 = jsonObject_total.getString("choice_1");
            choice_2 = jsonObject_total.getString("choice_2");
            choice_1_country = jsonObject_total.getString("choice_1_country");
            choice_2_country = jsonObject_total.getString("choice_2_country");
            snack_image = jsonObject_total.getString("snack_image");

            Log.e("4번 화면 결과", "question : " + question + ", choice_1 : " + choice_1 + ", choice_2 : " + choice_2 + ", choice_1_country : " + choice_1_country +
                    ", choice_2_country : " + choice_2_country + ", snack_image : " + snack_image);

            Uri uri = Uri.parse(snack_image);

            // 텍스트뷰와 버튼에 set한다
            Glide.with(this)
                    .load(uri)
                    .into(fourth_question_image);
            fourth_question_text.setText(question);
            fourth_question_first_btn.setText(choice_1);
            fourth_question_second_btn.setText(choice_2);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /* 이곳(4번째 액티비티)에서 백버튼을 누르면 Compatibility_SecondActivity로 이동한다. 이 때 5 or 6 중 하나의 값과 true 값을 Compatibility_SecondActivity로 같이 보낸다 */
    @Override
    public void onBackPressed()
    {
        Intent return_intent = new Intent();
        return_intent.putExtra("hasBackPressed", true);
        setResult(RESULT_OK, return_intent);
        finish();
    }

}