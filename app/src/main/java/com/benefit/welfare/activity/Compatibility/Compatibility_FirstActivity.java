package com.benefit.welfare.Activity.Compatibility;

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
import com.benefit.welfare.API.ApiClient;
import com.benefit.welfare.API.ApiInterface;
import com.benefit.welfare.Custom.CustomAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* mbti 테스트 1번째 문제 화면, MatchTestFragment에서 시작하기 버튼을 누르면 이곳으로 이동한다 */
public class Compatibility_FirstActivity extends AppCompatActivity
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
        Log.e(TAG, "onCreate()");
        getFirstProblem();
        setContentView(R.layout.activity_compatibility__first);

        first_question_text = findViewById(R.id.first_question_text);
        first_question_image = findViewById(R.id.first_question_image);
        first_question_fist_btn = findViewById(R.id.first_question_first_btn);
        first_question_second_btn = findViewById(R.id.first_question_second_btn);

        // 버튼을 누르면 ArrayList에 나라 이름을 넣고 다음 액티비티로 이동한다
        first_question_fist_btn.setOnClickListener(v -> {
            list.add("덴마크");
            Log.e(TAG, "arraylist 값 확인 = " + list.toString());
            Intent intent = new Intent(Compatibility_FirstActivity.this, Compatibility_SecondActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 1);
        });

        first_question_second_btn.setOnClickListener(v -> {
            list.add("미국");
            Log.e(TAG, "arraylist 값 확인 = " + list.toString());
            Intent intent = new Intent(Compatibility_FirstActivity.this, Compatibility_SecondActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 2);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "1번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                list.remove("덴마크");
                Log.e(TAG, "백버튼 눌려서 리스트에서 값 삭제 후 ArrayList : " + list);
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK && data != null)
        {
            Log.e(TAG, "2번 값 리턴됨");
            boolean hasBackPressed = data.getBooleanExtra("hasBackPressed", false);
            Log.e(TAG, "hasBackPressed = " + hasBackPressed);
            if (hasBackPressed)
            {
                list.remove("미국");
                Log.e(TAG, "백버튼 눌려서 리스트에서 값 삭제 후 ArrayList : " + list);
            }
        }
        else
        {
            Log.e(TAG, "RESULT_OK가 아니거나 data가 null");
        }
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
                    .into(first_question_image);
            first_question_text.setText(question);
            first_question_fist_btn.setText(choice_1);
            first_question_second_btn.setText(choice_2);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /* 백버튼 클릭 시 지금 테스트를 종료하면 결과가 저장되지 않는다는 경고 커스텀 다이얼로그를 띄운다 (메인 기능이 아니라 우선순위는 나중으로 둔다) */
    @Override
    public void onBackPressed()
    {
        // 지금 종료하면 지금까지 진행한 상황은 저장되지 않는다는 커스텀 경고 다이얼로그를 띄운다
        CustomAlertDialog dialog = new CustomAlertDialog(Compatibility_FirstActivity.this);
        dialog.showAlertDialog();
    }
}