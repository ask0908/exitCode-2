package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 지역별 혜택 지도 액티비티 */
public class MapActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getName();

    Button sejong_and_daejeon_benefit_btn, jeonnam_benefit_btn, seoul_benefit_btn, incheon_benefit_btn, gyongi_benefit_btn, gangwon_benefit_btn,
            choongnam_benefit_btn, jeonbuk_benefit_btn, choongbuk_benefit_btn, jeju_benefit_btn, gyongbuk_benefit_btn, gyongnam_benefit_btn;
//    String user_area, number_of_benefit, all_result, ganwon_result, gyongi_result, gyongnam_result, gyongbuk_result, gwanju_result, daegu_result, daejeon_result, busan_result;

    // 인텐트에서 가져온 지역명 담을 변수
    String user_area;
    // 유저가 선택한 지역명을 담을 변수와 그 지역에 있는 혜택의 개수들을 담을 변수
    String get_area, number_of_benefit, count_of_benefit;
    TextView map_bottom_textview, area_textview, change_area;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        user_area = intent.getStringExtra("user_area");

        getUNumberOfBenefit();

        init();
        btnClickListener();

        map_bottom_textview.setOnClickListener(v -> {
            Intent textview_intent = new Intent(MapActivity.this, MapDetailActivity.class);
            textview_intent.putExtra("area", "부산");
            textview_intent.putExtra("welf_count", count_of_benefit);
            startActivity(textview_intent);
        });

        // 현재 지역명을 출력하는 텍스트뷰
        area_textview.setText("현재 지역 : " + user_area);

        // 지역 변경
        change_area.setOnClickListener(OnSingleClickListener -> {
            Intent change_intent = new Intent(MapActivity.this, MapSearchActivity.class);
            startActivity(change_intent);
        });
    }

    /* 지역별 혜택 개수 받아오는 메서드 */
    void getUNumberOfBenefit()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getNumberOfBenefit(user_area, "1");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    number_of_benefit = response.body();
                    Log.e(TAG, "number_of_benefit = " + number_of_benefit);
                    jsonParsing(number_of_benefit);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e("getUNumberOfBenefit()", "에러 : " + t.getMessage());
            }
        });
    }

    private void jsonParsing(String number_of_benefit)
    {
        try
        {
            JSONArray jsonArray = new JSONArray(number_of_benefit);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                get_area = jsonObject.getString("local");
                count_of_benefit = jsonObject.getString("welf_count");
                /* number_of_benefit = [{"local":"전국","welf_count":417},{"local":"강원","welf_count":9},{"local":"경기","welf_count":16},{"local":"경남","welf_count":54},
                {"local":"경북","welf_count":88},{"local":"광주","welf_count":9},{"local":"대구","welf_count":7},{"local":"대전","welf_count":9},{"local":"부산","welf_count":9}]
                 위와 같은 형태로 올 때는 local, welf_count 안의 값을 담을 변수를 따로 만들어서 거기에 넣는 게 낫다*/
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        map_bottom_textview.setText(get_area + " 지역 혜택 보기 " + count_of_benefit + "개 >");
        Log.e(TAG, "지역명 = " + get_area + ", 혜택 수 = " + count_of_benefit);
    }

    /* 버튼 클릭 리스너들 모아놓은 메서드 */
    private void btnClickListener()
    {
        // 인천
        incheon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "인천");
            startActivity(intent);
        });

        /* TODO : 서울이라 써 있지만 지금은 서울 쪽 복지 데이터가 없어서 부산을 선택했다고 가정한다 */
        // 서울
        seoul_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
//            intent.putExtra("area", "서울");
            intent.putExtra("area", "부산");
            intent.putExtra("welf_count", count_of_benefit);
            startActivity(intent);
        });

        // 경기
        gyongi_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경기");
            startActivity(intent);
        });

        // 강원
        gangwon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "강원");
            startActivity(intent);
        });

        // 충남
        choongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충남");
            startActivity(intent);
        });

        // 충북
        choongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충북");
            startActivity(intent);
        });

        // 세종, 대전
//        sejong_and_daejeon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
//            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
//            intent.putExtra("area", "세종, 대전");
//            startActivity(intent);
//        });

        // 경북
        gyongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경북");
            startActivity(intent);
        });

        // 전북
        jeonbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전북");
            startActivity(intent);
        });

        // 전남
        jeonnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전남");
            startActivity(intent);
        });

        // 경남
        gyongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경남");
            startActivity(intent);
        });

        // 제주
        jeju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "제주");
            startActivity(intent);
        });
    }

    /* findViewById() 모아놓은 메서드 */
    private void init()
    {
//        sejong_and_daejeon_benefit_btn = findViewById(R.id.sejong_and_daejeon_benefit_btn);
        jeonnam_benefit_btn = findViewById(R.id.jeonnam_benefit_btn);
        seoul_benefit_btn = findViewById(R.id.seoul_benefit_btn);
        incheon_benefit_btn = findViewById(R.id.incheon_benefit_btn);
        gyongi_benefit_btn = findViewById(R.id.gyongi_benefit_btn);
        gangwon_benefit_btn = findViewById(R.id.gangwon_benefit_btn);
        choongnam_benefit_btn = findViewById(R.id.choongnam_benefit_btn);
        jeonbuk_benefit_btn = findViewById(R.id.jeonbuk_benefit_btn);
        choongbuk_benefit_btn = findViewById(R.id.choongbuk_benefit_btn);
        jeju_benefit_btn = findViewById(R.id.jeju_benefit_btn);
        gyongbuk_benefit_btn = findViewById(R.id.gyongbuk_benefit_btn);
        gyongnam_benefit_btn = findViewById(R.id.gyongnam_benefit_btn);
        map_bottom_textview = findViewById(R.id.map_bottom_textview);
        area_textview = findViewById(R.id.area_textview);
        change_area = findViewById(R.id.change_area);
    }
}