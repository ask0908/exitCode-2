package com.benefit.welfare.activity.Compatibility;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.benefit.welfare.R;
import com.benefit.welfare.api.ApiClient;
import com.benefit.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* mbti 테스트 3번 문제 화면 */
public class Compatibility_ThirdActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView third_question_text;
    ImageView third_question_image;
    Button third_question_first_btn, third_question_second_btn;
    // SecondActivity에서 날아온 ArrayList의 값을 담을 ArrayList
    ArrayList<String> third_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getThirdProblem();
        setContentView(R.layout.activity_compatibility__third);

        init();

        Intent third_intent = getIntent();
        third_list.addAll((ArrayList<String>)third_intent.getSerializableExtra("second_list"));
        Log.e(TAG, "2번째 액티비티에서 받은 list = " + third_list);

        // 버튼을 누르면 ArrayList에 나라 이름을 넣고 다음 액티비티로 이동한다
        third_question_first_btn.setOnClickListener(v -> {
            third_list.add("덴마크");
            Log.e(TAG, "3번째 list = " + third_list);
            Intent intent = new Intent(Compatibility_ThirdActivity.this, Compatibility_FourthActivity.class);
            intent.putExtra("third_list", third_list);
            startActivityForResult(intent, 5);
        });

        third_question_second_btn.setOnClickListener(v -> {
            third_list.add("미국");
            Log.e(TAG, "3번째 list = " + third_list);
            Intent intent = new Intent(Compatibility_ThirdActivity.this, Compatibility_FourthActivity.class);
            intent.putExtra("third_list", third_list);
            startActivityForResult(intent, 6);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "5번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                third_list.remove("덴마크");
                Log.e(TAG, "3번 액티비티에서 백버튼 눌려서 리스트에서 값 삭제된 후 ArrayList : " + third_list);
            }
        }
        else if (requestCode == 6 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "6번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                third_list.remove("덴마크");
                Log.e(TAG, "3번 액티비티에서 백버튼 눌려서 리스트에서 값 삭제된 후 ArrayList : " + third_list);
            }
        }
        else
        {
            Log.e(TAG, "RESULT_OK가 아니거나 data가 null");
        }
    }

    void getThirdProblem()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getThirdProblem("3");
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
                Log.e("getThirdProblem()", "에러 : " + t.getMessage());
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

            Log.e("2번 화면 결과", "question : " + question + ", choice_1 : " + choice_1 + ", choice_2 : " + choice_2 + ", choice_1_country : " + choice_1_country +
                    ", choice_2_country : " + choice_2_country + ", snack_image : " + snack_image);

            Uri uri = Uri.parse(snack_image);

            // 텍스트뷰와 버튼에 set한다
            Glide.with(this)
                    .load(uri)
                    .into(third_question_image);
            third_question_text.setText(question);
            third_question_first_btn.setText(choice_1);
            third_question_second_btn.setText(choice_2);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void init()
    {
        third_question_text = findViewById(R.id.third_question_text);
        third_question_image = findViewById(R.id.third_question_image);
        third_question_first_btn = findViewById(R.id.third_question_first_btn);
        third_question_second_btn = findViewById(R.id.third_question_second_btn);
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