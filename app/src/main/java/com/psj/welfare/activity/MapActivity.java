package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    String user_area = "";
    // 유저가 선택한 지역에 있는 혜택의 개수들을 담을 변수
    String number_of_benefit;
    // OO시, OO구 정보를 담을 변수
    String map_city, map_district;
    TextView map_bottom_textview, area_textview, change_area;
    String encode_str;

    List<String> district_list, count_list;
    // 상세 보기로 이동할 때 혜택의 개수를 담아 보내기 위해 사용한 변수
    String count;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        user_area = intent.getStringExtra("user_area");
        map_city = intent.getStringExtra("city");
        map_district = intent.getStringExtra("district");
        Log.e(TAG, "user_area = " + user_area + ", city = " + map_city);

        // 지역별 혜택 개수 받아와서 원 안에 넣는 메서드
        getNumberOfBenefit();

        // findViewById() 모아놓은 메서드
        init();
        // 버튼 클릭 리스너 모아놓은 메서드
        btnClickListener();

        district_list = new ArrayList<>();
        count_list = new ArrayList<>();

        // 반투명 텍스트뷰를 누르면 바로 내 지역의 혜택을 보러 이동한다
        map_bottom_textview.setOnClickListener(v -> {
            Intent textview_intent = new Intent(MapActivity.this, MapDetailActivity.class);
            sharedPreferences = getSharedPreferences("app_pref", 0);
            String shared_area = sharedPreferences.getString("user_area", "");
            textview_intent.putExtra("area", user_area);
//            textview_intent.putExtra("area", shared_area);
            textview_intent.putExtra("welf_count", count);
            Log.e(TAG, "user_area = " + user_area);
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
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String sessionId = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.getNumberOfBenefit(token, sessionId, user_area, "1");
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

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    /* PHP 파일 보완으로 서버에서 전송되는 값이 바뀌어 JSON 파싱 함수 재구성 */
    private void jsonParsing(String number_of_benefit)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(number_of_benefit);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                district_list.add(inner_json.getString("local"));
                count_list.add(inner_json.getString("welf_count"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "local 리스트 안의 값 = " + district_list.toString() + "\n숫자 리스트 안의 값 = " + count_list.toString());
        choongbuk_benefit_btn.setText("충 북\n(" + count_list.get(1) + ")");
        choongnam_benefit_btn.setText("충 남\n(" + count_list.get(2) + ")");
        jeju_benefit_btn.setText("제 주\n(" + count_list.get(3) + ")");
        jeonbuk_benefit_btn.setText("전 북\n(" + count_list.get(4) + ")");
        jeonnam_benefit_btn.setText("전 남\n(" + count_list.get(5) + ")");
        incheon_benefit_btn.setText("인 천\n(" + count_list.get(6) + ")");
        ulsan_benefit_btn.setText("울 산\n(" + count_list.get(7) + ")");
        sejong_benefit_btn.setText("세 종\n(" + count_list.get(8) + ")");
        seoul_benefit_btn.setText("서 울\n(" + count_list.get(9) + ")");
        busan_benefit_btn.setText("부 산\n(" + count_list.get(10) + ")");
        daejeon_benefit_btn.setText("대 전\n(" + count_list.get(11) + ")");
        gyongnam_benefit_btn.setText("경 남\n(" + count_list.get(12) + ")");
        gyongi_benefit_btn.setText("경 기\n(" + count_list.get(13) + ")");
        gangwon_benefit_btn.setText("강 원\n(" + count_list.get(14) + ")");
        daegu_benefit_btn.setText("대 구\n(" + count_list.get(15) + ")");
        gwangju_benefit_btn.setText("광 주\n(" + count_list.get(16) + ")");
        gyongbuk_benefit_btn.setText("경 북\n(" + count_list.get(17) + ")");

        // 내 지역에 따라서 하단 텍스트뷰에 set되는 내용도 변해야 한다
        // 추가로 하단의 반투명 텍스트뷰를 눌렀을 때 이동 시 혜택을 보여줘야 하므로 count에 담아서 인텐트로 보낸다?
        if (user_area.equals("충북"))
        {
            count = String.valueOf(count_list.get(1));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(1) + "개 >");
        }
        if (user_area.equals("충남"))
        {
            count = String.valueOf(count_list.get(2));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(2) + "개 >");
        }
        if (user_area.equals("제주"))
        {
            count = String.valueOf(count_list.get(3));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(3) + "개 >");
        }
        if (user_area.equals("전북"))
        {
            count = String.valueOf(count_list.get(4));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(4) + "개 >");
        }
        if (user_area.equals("전남"))
        {
            count = String.valueOf(count_list.get(5));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(5) + "개 >");
        }
        if (user_area.equals("인천"))
        {
            count = String.valueOf(count_list.get(6));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(6) + "개 >");
        }
        if (user_area.equals("울산"))
        {
            count = String.valueOf(count_list.get(7));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(7) + "개 >");
        }
        if (user_area.equals("세종"))
        {
            count = String.valueOf(count_list.get(8));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(8) + "개 >");
        }
        if (user_area.equals("서울"))
        {
            count = String.valueOf(count_list.get(9));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(9) + "개 >");
        }
        if (user_area.equals("부산"))
        {
            count = String.valueOf(count_list.get(10));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(10) + "개 >");
        }
        if (user_area.equals("대전"))
        {
            count = String.valueOf(count_list.get(11));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(11) + "개 >");
        }
        if (user_area.equals("경남"))
        {
            count = String.valueOf(count_list.get(12));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(12) + "개 >");
        }
        if (user_area.equals("경기"))
        {
            count = String.valueOf(count_list.get(13));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(13) + "개 >");
        }
        if (user_area.equals("강원"))
        {
            count = String.valueOf(count_list.get(14));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(14) + "개 >");
        }
        if (user_area.equals("대구"))
        {
            count = String.valueOf(count_list.get(15));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(15) + "개 >");
        }
        if (user_area.equals("광주"))
        {
            count = String.valueOf(count_list.get(16));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(16) + "개 >");
        }
        if (user_area.equals("경북"))
        {
            count = String.valueOf(count_list.get(17));
            map_bottom_textview.setText("내 주변 혜택 보기 " + count_list.get(17) + "개 >");
        }
    }

    /* 버튼 클릭 리스너들 모아놓은 메서드 */
    /* 서울, 경기, 강원, 경북, 세종, 충남, 대전, 광주, 대구, 경남, 부산 */
    private void btnClickListener()
    {
        // 인천
        incheon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "인천");
            intent.putExtra("welf_count", String.valueOf(count_list.get(6)));
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
            intent.putExtra("welf_count", String.valueOf(count_list.get(13)));
            startActivity(intent);
        });

        // 강원
        gangwon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "강원");
            intent.putExtra("welf_count", String.valueOf(count_list.get(14)));
            startActivity(intent);
        });

        // 충남
        choongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(2)));
            startActivity(intent);
        });

        // 충북
        choongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "충북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(1)));
            startActivity(intent);
        });

        // 세종
        sejong_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "세종");
            intent.putExtra("welf_count", String.valueOf(count_list.get(8)));
            startActivity(intent);
        });

        // 대전
        daejeon_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "대전");
            intent.putExtra("welf_count", String.valueOf(count_list.get(11)));
            startActivity(intent);
        });

        // 경북
        gyongbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(17)));
            startActivity(intent);
        });

        // 전북
        jeonbuk_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전북");
            intent.putExtra("welf_count", String.valueOf(count_list.get(4)));
            startActivity(intent);
        });

        // 전남
        jeonnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "전남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(5)));
            startActivity(intent);
        });

        // 경남
        gyongnam_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "경남");
            intent.putExtra("welf_count", String.valueOf(count_list.get(12)));
            startActivity(intent);
        });

        // 제주
        jeju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "제주");
            intent.putExtra("welf_count", String.valueOf(count_list.get(3)));
            startActivity(intent);
        });

        // 울산
        ulsan_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "울산");
            intent.putExtra("welf_count", String.valueOf(count_list.get(7)));
            startActivity(intent);
        });

        // 대구
        daegu_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "대구");
            intent.putExtra("welf_count", String.valueOf(count_list.get(15)));
            startActivity(intent);
        });

        // 광주
        gwangju_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "광주");
            intent.putExtra("welf_count", String.valueOf(count_list.get(16)));
            startActivity(intent);
        });

        // 부산
        busan_benefit_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(MapActivity.this, MapDetailActivity.class);
            intent.putExtra("area", "부산");
            intent.putExtra("welf_count", String.valueOf(count_list.get(10)));
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