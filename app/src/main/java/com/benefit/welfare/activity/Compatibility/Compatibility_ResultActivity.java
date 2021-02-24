package com.benefit.welfare.activity.Compatibility;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.benefit.welfare.R;
import com.benefit.welfare.activity.MainTabLayoutActivity;
import com.benefit.welfare.api.ApiClient;
import com.benefit.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    TextView compatibility_description_text;
    // 결과로 나온 나라 이름을 표시할 텍스트뷰
    TextView compatibility_result_country;
    // 나라를 대표하는 이미지를 표시할 이미지븊
    ImageView compatibility_result_image;

    // 첫번째 해시태그, 두번째 해시태그 텍스트뷰
    TextView first_result_hashtag;
    // 해시태그 바로 밑에 결과로 나온 나라의 설명이 들어갈 텍스트뷰
    TextView result_description_textview;

    // 환상의 케미국가, 환장의 케미국가 대표 이미지를 넣을 이미지뷰
    ImageView result_good_image, result_bad_image;

    // 공유하기, 프로필에 유형 등록하기, 닫기 버튼
    Button share_btn, regist_my_type_btn, compatibility_close_btn;

    // 유저가 선택한 나라 이름들을 종합해서 담을 ArrayList. 이 안에 들어있는 나라 이름 문자열의 수를 세서 가장 많은 수의 나라에 대한 정보를 출력한다
    ArrayList<String> final_list = new ArrayList<>();

    // 나라 이름 문자열의 숫자를 셀 변수. 테스트기 때문에 미국, 러시아, 한국의 3가지 경우에 대해서만 변수를 만들었다
    int denmark_num, america_num;

    // 가장 많이 선택된 나라 이름을 담아서 서버로 데이터를 요청할 때 사용할 변수
    String country_name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compatibility__result);

        Logger.addLogAdapter(new AndroidLogAdapter());

        Intent final_intent = getIntent();
        final_list.addAll((ArrayList<String>) final_intent.getSerializableExtra("fourth_list"));
        Logger.e("결과 화면까지 쌓인 ArrayList 내용물 = " + final_list);

        // 마지막 액티비티까지 가져온 ArrayList의 내용물을 세서 int 변수에 집어넣는다
        denmark_num = Collections.frequency(final_list, "덴마크");
        america_num = Collections.frequency(final_list, "미국");

        Logger.e("덴마크 문자열 개수 : " + denmark_num + "개");
        Logger.e("미국 문자열 개수 : " + america_num + "개");

        // 각 변수의 크기를 구하고 가장 숫자가 많은 변수가 의미하는 나라 이름을 서버로 보낸다
        if (denmark_num > america_num)
        {
            country_name = "덴마크";
//            Log.e(TAG, "나라 이름 : " + country_name);
            Logger.e("나라 이름 = " + country_name);
        }
        else if (america_num > denmark_num)
        {
            country_name = "미국";
//            Log.e(TAG, "나라 이름 : " + country_name);
            Logger.e("나라 이름 = " + country_name);
        }
        Logger.e("서버에 결과 요청할 나라 이름 = " + country_name);

        // 서버에서 테스트 결과를 가져오는 메서드
        getFirstResult(country_name);

        // findViewById()들을 모아놓은 메서드
        init();

        // 공유하기 버튼
        share_btn.setOnClickListener(v ->
        {
            // 카카오 링크 API를 써서 카톡을 통해 스낵 컨텐츠 결과를 공유한다
            /* 앱에서 보기, 웹에서 보기 버튼의 워딩 및 평문 워딩도 수정해야 한다. 이미지가 나오도록 고쳐야 한다 */
            FeedTemplate params = FeedTemplate
                    .newBuilder(ContentObject.newBuilder("너의 혜택은", "http://3.34.64.143/images/reviews/test.png",
                            LinkObject.newBuilder().setMobileWebUrl("'https://developers.kakao.com").build())
                            .setDescrption("나에게 맞는 혜택을 찾아주는 \"너의 혜택은\" 서비스를 이용해 보세요!")
                            .build())
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                            .setWebUrl("'https://developers.kakao.com")
                            .setMobileWebUrl("'https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder()
                            .setWebUrl("'https://developers.kakao.com")
                            .setMobileWebUrl("'https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .build();

            Map<String, String> serverCallbackArgs = new HashMap<>();
            serverCallbackArgs.put("user_id", "${current_user_id}");
            serverCallbackArgs.put("product_id", "${shared_product_id}");

            KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    Logger.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {
                    // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                    Logger.e(result.toString());
                }
            });

        });

        // 프로필에 유형 등록하기 버튼
        regist_my_type_btn.setOnClickListener(v -> Toast.makeText(this, "프로필에 유형 등록하기 버튼 클릭", Toast.LENGTH_SHORT).show());

        // 닫기 버튼
        compatibility_close_btn.setOnClickListener(v ->
        {
            Toast.makeText(this, "닫기 버튼 클릭", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Compatibility_ResultActivity.this, MainTabLayoutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    /* 서버에서 테스트 결과 텍스트들을 가져오고 각 뷰에 텍스트들을 set하는 메서드 */
    void getFirstResult(String country_name)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getFirstResult(country_name);
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
                Logger.e("getFirstResult() 에러 : " + t.getMessage());
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
            String snack_country, snack_image, snack_tag, snack_detail, good_country, bad_country, snack_country_detail;

            // 문자열에서 각 JSON 키값에 해당하는 value값을 빼 String 변수에 저장한 후
            snack_country = jsonObject_total.getString("snack_country");
            snack_image = jsonObject_total.getString("snack_image");
            snack_tag = jsonObject_total.getString("snack_tag");
            snack_detail = jsonObject_total.getString("snack_detail");
            good_country = jsonObject_total.getString("good_country");
            bad_country = jsonObject_total.getString("bad_country");
            snack_country_detail = jsonObject_total.getString("snack_country_detail");
            Log.e(TAG, "테스트 결과 화면 - snack_tag :  " + snack_tag + ", snack_detail : " + snack_detail + ", snack_country : " + snack_country + ", snack_image : "
                    + snack_image + ", good_country : " + good_country + ", bad_country : " + bad_country + ", snack_country_detail : " + snack_country_detail);

            // String -> Uri 변환
            Uri uri = Uri.parse(snack_image);

            // 각 뷰들에 set한다
            Glide.with(this)
                    .load(uri)
                    .into(compatibility_result_image);
            compatibility_description_text.setText(snack_country_detail);
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
        compatibility_description_text = findViewById(R.id.compatibility_description_text);
        compatibility_result_image = findViewById(R.id.compatibility_result_image);
        first_result_hashtag = findViewById(R.id.first_result_hashtag);
        result_description_textview = findViewById(R.id.result_description_textview);
        result_good_image = findViewById(R.id.result_good_image);
        result_bad_image = findViewById(R.id.result_bad_image);
        share_btn = findViewById(R.id.share_btn);
        regist_my_type_btn = findViewById(R.id.regist_my_type_btn);
        compatibility_close_btn = findViewById(R.id.compatibility_close_btn);
    }
}