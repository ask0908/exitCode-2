package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MapResultAdapter;
import com.psj.welfare.adapter.ResultKeywordAdapter;
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

/* 지도 화면에서 지역 선택 시 이동해 지역별 혜택 목록을 보여주는 액티비티 */
public class MapDetailActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView map_result_textview;
    RecyclerView result_keyword_recyclerview, map_result_recyclerview;
    ResultKeywordAdapter adapter;
    ResultKeywordAdapter.ItemClickListener keyword_click;
    MapResultAdapter map_adapter;
    MapResultAdapter.ItemClickListener itemClickListener;

    List<ResultKeywordItem> list;
    List<MapResultItem> item_list;

    // 지도 화면에서 가져온 지역 정보를 담을 변수
    String area, welf_count;

    // 서버 통신 시 지역명을 보낼 때, 혜택 개수를 담을 때 사용하는 변수
    String user_area, number_of_benefit;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        Intent intent = getIntent();
        area = intent.getStringExtra("area");
        welf_count = intent.getStringExtra("welf_count");
        Log.e(TAG, "받아온 지역명 = " + area + ", 혜택 개수 = " + welf_count);

        /* 서버에서 지역별 혜택 개수 받아오는 메서드 */
        getNumberOfBenefit();

        map_result_textview = findViewById(R.id.map_result_textview);
        // 전체, 학생 등 카테고리를 가로로 보여주는 리사이클러뷰
        result_keyword_recyclerview = findViewById(R.id.result_keyword_recyclerview);
        result_keyword_recyclerview.setHasFixedSize(true);
        result_keyword_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 복지혜택 이름들을 세로로 보여주는 리사이클러뷰
        map_result_recyclerview = findViewById(R.id.map_result_recyclerview);
        map_result_recyclerview.setHasFixedSize(true);
        map_result_recyclerview.addItemDecoration(new DividerItemDecoration(MapDetailActivity.this, DividerItemDecoration.VERTICAL));
        map_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        list.add(new ResultKeywordItem("전체"));
        list.add(new ResultKeywordItem("학생·청년"));
        list.add(new ResultKeywordItem("주거"));
        list.add(new ResultKeywordItem("육아·임신"));
        list.add(new ResultKeywordItem("아기·어린이"));
        list.add(new ResultKeywordItem("문화·생활"));
        list.add(new ResultKeywordItem("기업·자영업자"));
        list.add(new ResultKeywordItem("저소득층"));
        list.add(new ResultKeywordItem("중장년·노인"));
        list.add(new ResultKeywordItem("장애인"));
        list.add(new ResultKeywordItem("다문화"));
        list.add(new ResultKeywordItem("법률"));
        list.add(new ResultKeywordItem("기타"));
        adapter = new ResultKeywordAdapter(MapDetailActivity.this, list, keyword_click);
        adapter.setOnResultKeywordClickListener((view, position) ->
        {
            String name = list.get(position).getKeyword_category();
            Log.e(TAG, "ResultKeywordAdapter - name = " + name);
        });
        result_keyword_recyclerview.setAdapter(adapter);

        if (area.equals("서울"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("인천"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("강원"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("경기"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("충남"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("충북"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("경북"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("세종"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("전북"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("전남"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("경남"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("제주"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("부산"))
        {
            map_result_textview.setText(area + " 지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("대구"))
        {
            map_result_textview.setText(area + "지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("울산"))
        {
            map_result_textview.setText(area + "지역에서 검색된 혜택,\n총 "+ welf_count + "개입니다");
        }
        if (area.equals("대전"))
        {
            map_result_textview.setText(area + "지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("전국"))
        {
            map_result_textview.setText(area + "에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
        if (area.equals("광주"))
        {
            map_result_textview.setText(area + "지역에서 검색된 혜택,\n총 " + welf_count + "개입니다");
        }
    }

    /* 서버에서 지역별 혜택 개수 받아오는 메서드 */
    void getNumberOfBenefit()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getNumberOfBenefit(area, "2");
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
                Log.e(TAG, "getUNumberOfBenefit() 에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 넘어온 JSONArray 안의 값들을 파싱해서 리사이클러뷰에 보여주는 메서드 */
    private void jsonParsing(String number_of_benefit)
    {
        item_list = new ArrayList<>();
        try
        {
            // 서버에서 JSONArray 형태로 오기 때문에 클라에서도 서버 응답을 먼저 JSONArray로 받는다
            JSONArray jsonArray = new JSONArray(number_of_benefit);
            // JSONArray의 크기만큼 for문을 돌면서 JSONArray 안의 값들을 꺼낸다
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // 리사이클러뷰에 복지혜택 이름들을 담아야 하니까 getString()으로 빼내서 전역변수에 담는다
                name = jsonObject.getString("welf_name");
                // 리사이클러뷰에 붙이기 위한 처리
                MapResultItem item = new MapResultItem();
                item.setBenefit_name(name);
                // 혜택 이름들을 보여주는 리사이클러뷰의 어댑터에 쓰일 List에 for문이 반복된 만큼 생성된 DTO 객체들을 넣는다
                item_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
        map_adapter = new MapResultAdapter(MapDetailActivity.this, item_list, itemClickListener);
        // 더보기 버튼 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
        map_adapter.setOnItemClickListener((view, position) ->
        {
            String name = item_list.get(position).getBenefit_name();
            Log.e(TAG, "혜택 이름 = " + name);
            Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
            see_detail_intent.putExtra("name", name);
            startActivity(see_detail_intent);
        });
        map_result_recyclerview.setAdapter(map_adapter);
    }

}