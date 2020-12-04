package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 지역별 혜택 지도 액티비티 */
public class MapActivity extends AppCompatActivity
{
    public final String TAG = "MapActivity";

    Button jeonnam_benefit_btn, seoul_benefit_btn, incheon_benefit_btn, gyongi_benefit_btn, gangwon_benefit_btn, choongnam_benefit_btn, jeonbuk_benefit_btn,
            choongbuk_benefit_btn, jeju_benefit_btn, gyongbuk_benefit_btn, gyongnam_benefit_btn, ulsan_benefit_btn, busan_benefit_btn, daejeon_benefit_btn,
            sejong_benefit_btn, gwangju_benefit_btn, daegu_benefit_btn;

    // 인텐트에서 가져온 지역명 담을 변수
    String user_area;
    // 유저가 선택한 지역에 있는 혜택의 개수들을 담을 변수
    String number_of_benefit;
    // OO시, OO구 정보를 담을 변수
    String map_city, map_district;
    TextView map_bottom_textview, area_textview, change_area;

    List<String> district_list, count_list;
    // 상세 보기로 이동할 때 혜택의 개수를 담아 보내기 위해 사용한 변수
    String count;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        user_area = intent.getStringExtra("user_area");
        map_city = intent.getStringExtra("city");
        map_district = intent.getStringExtra("district");
        Log.e(TAG, "user_area = " + user_area + ", city = " + map_city + ", district = " + map_district);

        getNumberOfBenefit();

        init();
        btnClickListener();

        district_list = new ArrayList<>();
        count_list = new ArrayList<>();

        // 반투명 텍스트뷰를 누르면 바로 내 지역의 혜택을 보러 이동한다
        map_bottom_textview.setOnClickListener(v -> {
            Intent textview_intent = new Intent(MapActivity.this, MapDetailActivity.class);
            textview_intent.putExtra("area", user_area);
            textview_intent.putExtra("welf_count", count);
            startActivity(textview_intent);
        });

        // 현재 지역명을 출력하는 텍스트뷰
        area_textview.setText(map_district);

        // 지역 변경
        change_area.setOnClickListener(OnSingleClickListener -> {
            Intent change_intent = new Intent(MapActivity.this, MapSearchActivity.class);
            startActivity(change_intent);
        });
    }

    /* 지역별 혜택 개수 받아오는 메서드 */
    void getNumberOfBenefit()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getNumberOfBenefit(user_area, "1");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    number_of_benefit = response.body();
                    Log.e(TAG, "number_of_benefit = " + number_of_benefit);
                    jsonParsing(number_of_benefit);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
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
                district_list.add(jsonObject.getString("local"));
                count_list.add(jsonObject.getString("welf_count"));
//                get_area = jsonObject.getString("local");
//                count_of_benefit = jsonObject.getString("welf_count");
//                Log.e(TAG, "local = " + get_area + ", welf_count = " + count_of_benefit);
                /* number_of_benefit = [{"local":"전국","welf_count":417},{"local":"강원","welf_count":9},{"local":"경기","welf_count":16},{"local":"경남","welf_count":54},
                {"local":"경북","welf_count":88},{"local":"광주","welf_count":9},{"local":"대구","welf_count":7},{"local":"대전","welf_count":9},{"local":"부산","welf_count":9}]
                 위와 같은 형태로 올 때는 위처럼 local, welf_count 안의 값을 담을 변수를 따로 만들어서 거기에 넣는 게 낫다 */
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "local 리스트 안의 값 = " + district_list.toString() + ", 숫자 리스트 안의 값 = " + count_list.toString());
        gangwon_benefit_btn.setText("강 원\n(" + count_list.get(1) + ")");
        gyongi_benefit_btn.setText("경 기\n(" + count_list.get(2) + ")");
        gyongnam_benefit_btn.setText("경 남\n(" + count_list.get(3) + ")");
        gyongbuk_benefit_btn.setText("경 북\n(" + count_list.get(4) + ")");
        gwangju_benefit_btn.setText("광 주\n(" + count_list.get(5) + ")");
        daegu_benefit_btn.setText("대 구\n(" + count_list.get(6) + ")");
        daejeon_benefit_btn.setText("대 전\n(" + count_list.get(7) + ")");
        busan_benefit_btn.setText("부 산\n(" + count_list.get(8) + ")");
        seoul_benefit_btn.setText("서 울\n(" + count_list.get(9) + ")");
        sejong_benefit_btn.setText("세 종\n(" + count_list.get(10) + ")");
        ulsan_benefit_btn.setText("울 산\n(" + count_list.get(11) + ")");
        incheon_benefit_btn.setText("인 천\n(" + count_list.get(12) + ")");
        jeonnam_benefit_btn.setText("전 남\n(" + count_list.get(13) + ")");
        jeonbuk_benefit_btn.setText("전 북\n(" + count_list.get(14) + ")");
        jeju_benefit_btn.setText("제 주\n(" + count_list.get(15) + ")");
        choongnam_benefit_btn.setText("충 남\n(" + count_list.get(16) + ")");
        choongbuk_benefit_btn.setText("충 북\n(" + count_list.get(17) + ")");

        // 내 지역에 따라서 하단 텍스트뷰에 set되는 내용도 변해야 한다
        // 추가로 하단의 반투명 텍스트뷰를 눌렀을 때 이동 시 혜택을 보여줘야 하므로 count에 담아서 인텐트로 보낸다?
        if (user_area.equals("강원"))
        {
            count = String.valueOf(count_list.get(1));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(1) + "개 >");
        }
        if (user_area.equals("경기"))
        {
            count = String.valueOf(count_list.get(2));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(2) + "개 >");
        }
        if (user_area.equals("경남"))
        {
            count = String.valueOf(count_list.get(3));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(3) + "개 >");
        }
        if (user_area.equals("경북"))
        {
            count = String.valueOf(count_list.get(4));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(4) + "개 >");
        }
        if (user_area.equals("광주"))
        {
            count = String.valueOf(count_list.get(5));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(5) + "개 >");
        }
        if (user_area.equals("대구"))
        {
            count = String.valueOf(count_list.get(6));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(6) + "개 >");
        }
        if (user_area.equals("대전"))
        {
            count = String.valueOf(count_list.get(7));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(7) + "개 >");
        }
        if (user_area.equals("부산"))
        {
            count = String.valueOf(count_list.get(8));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(8) + "개 >");
        }
        if (user_area.equals("서울"))
        {
            count = String.valueOf(count_list.get(9));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(9) + "개 >");
        }
        if (user_area.equals("세종"))
        {
            count = String.valueOf(count_list.get(10));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(10) + "개 >");
        }
        if (user_area.equals("울산"))
        {
            count = String.valueOf(count_list.get(11));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(11) + "개 >");
        }
        if (user_area.equals("인천"))
        {
            count = String.valueOf(count_list.get(12));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(12) + "개 >");
        }
        if (user_area.equals("전남"))
        {
            count = String.valueOf(count_list.get(13));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(13) + "개 >");
        }
        if (user_area.equals("전북"))
        {
            count = String.valueOf(count_list.get(14));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(14) + "개 >");
        }
        if (user_area.equals("제주"))
        {
            count = String.valueOf(count_list.get(15));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(15) + "개 >");
        }
        if (user_area.equals("충남"))
        {
            count = String.valueOf(count_list.get(16));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(16) + "개 >");
        }
        if (user_area.equals("충북"))
        {
            count = String.valueOf(count_list.get(17));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(17) + "개 >");
        }

    }

    /* 버튼 클릭 리스너들 모아놓은 메서드 */
    private void btnClickListener()
    {
        // 인천
        incheon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "인천");
            intent.putExtra("welf_count", String.valueOf(count_list.get(12)));
            startActivity(intent);
        });

        // 서울
        seoul_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "서울");
            intent.putExtra("welf_count", String.valueOf(count_list.get(9)));
            startActivity(intent);
        });

        // 경기
        gyongi_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경기");
            intent.putExtra("welf_count", String.valueOf(count_list.get(2)));
            startActivity(intent);
        });

        // 강원
        gangwon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "강원");
            intent.putExtra("welf_count", String.valueOf(count_list.get(1)));
            startActivity(intent);
        });

        // 충남
        choongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(16)));
            startActivity(intent);
        });

        // 충북
        choongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(17)));
            startActivity(intent);
        });

        // 세종
        sejong_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "세종");
            intent.putExtra("welf_count", String.valueOf(count_list.get(10)));
            startActivity(intent);
        });

        // 대전
        daejeon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "대전");
            intent.putExtra("welf_count", String.valueOf(count_list.get(7)));
            startActivity(intent);
        });

        // 경북
        gyongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(4)));
            startActivity(intent);
        });

        // 전북
        jeonbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(14)));
            startActivity(intent);
        });

        // 전남
        jeonnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(13)));
            startActivity(intent);
        });

        // 경남
        gyongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(3)));
            startActivity(intent);
        });

        // 제주
        jeju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "제주");
            intent.putExtra("welf_count", String.valueOf(count_list.get(15)));
            startActivity(intent);
        });

        // 울산
        ulsan_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "울산");
            intent.putExtra("welf_count", String.valueOf(count_list.get(11)));
            startActivity(intent);
        });

        // 대구
        daegu_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "대구");
            intent.putExtra("welf_count", String.valueOf(count_list.get(6)));
            startActivity(intent);
        });

        // 광주
        gwangju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "광주");
            intent.putExtra("welf_count", String.valueOf(count_list.get(5)));
            startActivity(intent);
        });

        // 부산
        busan_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "부산");
            intent.putExtra("welf_count", String.valueOf(count_list.get(8)));
            startActivity(intent);
        });
    }

    /* findViewById() 모아놓은 메서드 */
    private void init()
    {
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
        ulsan_benefit_btn = findViewById(R.id.ulsan_benefit_btn);
        busan_benefit_btn = findViewById(R.id.busan_benefit_btn);
        daejeon_benefit_btn = findViewById(R.id.daejeon_benefit_btn);
        sejong_benefit_btn = findViewById(R.id.sejong_benefit_btn);
        gwangju_benefit_btn = findViewById(R.id.gwangju_benefit_btn);
        daegu_benefit_btn = findViewById(R.id.daegu_benefit_btn);
    }
}