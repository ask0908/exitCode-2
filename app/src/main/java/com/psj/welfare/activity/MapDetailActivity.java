package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MapResultAdapter;
import com.psj.welfare.adapter.ResultKeywordAdapter;

import java.util.ArrayList;
import java.util.List;

/* 지도 화면에서 지역 선택 시 이동해 지역별 혜택 목록을 보여주는 액티비티 */
public class MapDetailActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    TextView map_result_textview;
    RecyclerView result_keyword_recyclerview, map_result_recyclerview;
    ResultKeywordAdapter adapter;
    MapResultAdapter map_adapter;

    List<ResultKeywordItem> list;
    List<MapResultItem> item_list;

    // 지도 화면에서 가져온 지역 정보를 담을 변수
    String area;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        init();

        result_keyword_recyclerview = findViewById(R.id.result_keyword_recyclerview);
        result_keyword_recyclerview.setHasFixedSize(true);
        result_keyword_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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
        adapter = new ResultKeywordAdapter(MapDetailActivity.this, list);
        result_keyword_recyclerview.setAdapter(adapter);

        item_list = new ArrayList<>();
        item_list.add(new MapResultItem("인플루엔자 국가예방접종 지원사업", "더보기"));
        item_list.add(new MapResultItem("주거안정 월세대출 (순수 월세 대출 시)", "더보기"));
        item_list.add(new MapResultItem("지역아동센터 지원", "더보기"));
        item_list.add(new MapResultItem("학교 우유급식", "더보기"));
        item_list.add(new MapResultItem("직업훈련 생계비 대부사업", "더보기"));
        item_list.add(new MapResultItem("청년내일채움공제", "더보기"));
        item_list.add(new MapResultItem("신중년 사회공헌", "더보기"));
        item_list.add(new MapResultItem("산재근로자 직업훈련", "더보기"));
        item_list.add(new MapResultItem("국민연금 실업크레딧", "더보기"));
        item_list.add(new MapResultItem("청소년활동 지원", "더보기"));
        map_adapter = new MapResultAdapter(MapDetailActivity.this, item_list);
        map_result_recyclerview.setAdapter(map_adapter);

        Intent intent = getIntent();
        area = intent.getStringExtra("area");

        if (area.equals("서울"))
        {
            map_result_textview.setText("서울 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("인천"))
        {
            map_result_textview.setText("인천 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("강원"))
        {
            map_result_textview.setText("강원 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("경기"))
        {
            map_result_textview.setText("경기 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("충남"))
        {
            map_result_textview.setText("충남 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("충북"))
        {
            map_result_textview.setText("충북 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("경북"))
        {
            map_result_textview.setText("경북 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("세종, 대전"))
        {
            map_result_textview.setText("세종, 대전 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("전북"))
        {
            map_result_textview.setText("전북 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("전남"))
        {
            map_result_textview.setText("전남 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("경남"))
        {
            map_result_textview.setText("경남 지역에서 검색된 혜택,\n총 300개입니다");
        }
        else if (area.equals("제주"))
        {
            map_result_textview.setText("제주 지역에서 검색된 혜택,\n총 300개입니다");
        }
    }

    private void init()
    {
        map_result_textview = findViewById(R.id.map_result_textview);
    }
}