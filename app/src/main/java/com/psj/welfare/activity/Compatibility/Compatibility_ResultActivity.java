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
import com.psj.welfare.activity.MainTabLayoutActivity;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* MBTI 결과를 보여주는 화면, 유저가 선택한 것에 따른 나라 설명과 이미지, 해시태그, 설명에 관련된 텍스트를 가져와서 각각 알맞은 뷰에 set하는 액티비티
* 공유하기 버튼 - ShareSheet에 카톡, 인스타 등 SNS 어플들이 나오게 한다
* 프로필에 유형 등록하기 버튼 - 자신의 프로필에 내가 어떤 복지유형을 갖고 있는지를 표시할 수 있다
* 닫기 버튼 - 액티비티 스택의 모든 액티비티들을 없애고 MainTabLayoutActivity로 돌아간다 */
public class Compatibility_ResultActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    // 아이디를 표시할 텍스트뷰
    TextView user_id;

    // 결과로 나온 나라 이름을 표시할 텍스트뷰
    TextView compatibility_result_country;
    // 나라를 대표하는 이미지를 표시할 이미지븊
    ImageView compatibility_result_image;

    // 첫번째 해시태그, 두번째 해시태그 텍스트뷰
    TextView first_result_hashtag, second_result_hashtag;
    // 해시태그 바로 밑에 결과로 나온 나라의 설명이 들어갈 텍스트뷰
    TextView result_description_textview;

    // 환상의 케미국가, 환장의 케미국가 대표 이미지를 넣을 이미지뷰
    ImageView result_good_image, result_bad_image;

    // 공유하기, 프로필에 유형 등록하기, 닫기 버튼
    Button share_btn, regist_my_type_btn, compatibility_close_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__result);

        // 서버에서 테스트 결과를 가져오는 메서드
        getFirstResult();

        // findViewById()들을 모아놓은 메서드
        init();

        // 공유하기 버튼
        share_btn.setOnClickListener(v -> {
            Toast.makeText(this, "공유하기 버튼 클릭", Toast.LENGTH_SHORT).show();
        });

        // 프로필에 유형 등록하기 버튼
        regist_my_type_btn.setOnClickListener(v -> {
            Toast.makeText(this, "프로필에 유형 등록하기 버튼 클릭", Toast.LENGTH_SHORT).show();
        });

        // 닫기 버튼
        compatibility_close_btn.setOnClickListener(v -> {
            Toast.makeText(this, "닫기 버튼 클릭", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Compatibility_ResultActivity.this, MainTabLayoutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    /* 서버에서 테스트 결과 텍스트들을 가져오고 각 뷰에 텍스트들을 set하는 메서드 */
    void getFirstResult()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getFirstResult("미국");
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
                Log.e(TAG, "getFirstResult() 에러 : " + t.getMessage());
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
            String snack_country, snack_tag, snack_detail, good_country, bad_country;

            // 문자열에서 각 JSON 키값에 해당하는 value값을 빼 String 변수에 저장한 후
            snack_country = jsonObject_total.getString("snack_country");
            snack_tag = jsonObject_total.getString("snack_tag");
            snack_detail = jsonObject_total.getString("snack_detail");
            Log.e(TAG, "테스트 결과 화면 - snack_tag :  " + snack_tag + ", snack_detail : " + snack_detail);
            Log.e(TAG, "테스트 결과 화면 - snack_country : " + snack_country);

            // 텍스트뷰들에 set한다
            compatibility_result_country.setText(snack_country);
            first_result_hashtag.setText(snack_tag);
            result_description_textview.setText(snack_detail);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /* findViewById() 모아놓은 메서드 */
    private void init()
    {
        user_id = findViewById(R.id.compatibility_result_user_id);
        compatibility_result_country = findViewById(R.id.compatibility_result_country);
        compatibility_result_image = findViewById(R.id.compatibility_result_image);
        first_result_hashtag = findViewById(R.id.first_result_hashtag);
        second_result_hashtag = findViewById(R.id.second_result_hashtag);
        result_description_textview = findViewById(R.id.result_description_textview);
        result_good_image = findViewById(R.id.result_good_image);
        result_bad_image = findViewById(R.id.result_bad_image);
        share_btn = findViewById(R.id.share_btn);
        regist_my_type_btn = findViewById(R.id.regist_my_type_btn);
        compatibility_close_btn = findViewById(R.id.compatibility_close_btn);
    }
}