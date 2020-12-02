package com.psj.welfare.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.PushGatherItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.PushGatherAdapter;

import java.util.ArrayList;
import java.util.List;

/* 받았던 푸시 알림들을 모아서 볼 수 있는 화면 */
public class PushGatherFragment extends Fragment
{
    private final String TAG = "PushGatherFragment";

    RecyclerView push_layout_recycler;
    PushGatherAdapter adapter;
    PushGatherAdapter.ItemClickListener itemClickListener;

    List<PushGatherItem> list;

    public PushGatherFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_push_gather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        push_layout_recycler = view.findViewById(R.id.push_layout_recycler);
        push_layout_recycler.setHasFixedSize(true);
        push_layout_recycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        push_layout_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 아래 하드코딩한 내용들은 서버에서 데이터를 받을 수 있게 되면 파싱해서 그 내용으로 나오도록 수정한다
        list = new ArrayList<>();
        list.add(new PushGatherItem("버팀목 전세자금 대출을 신청하셨군요!", "버팀목 전세자금 대출을 신청하셨군요!", "1일 전"));
        list.add(new PushGatherItem("학교 우유급식을 신청하셨군요!", "학교 우유급식을 신청하셨군요!", "1일 전"));
        list.add(new PushGatherItem("한부모가족 아동 양육비 지원을 신청하셨군요!", "한부모가족 아동 양육비 지원을 신청하셨군요!", "3일 전"));
        list.add(new PushGatherItem("어린이 국가예방접종 지원사업을 신청하셨군요!", "어린이 국가예방접종 지원사업을 신청하셨군요!", "4일 전"));
        list.add(new PushGatherItem("장애친화 건강검진을 신청하셨군요!", "장애친화 건강검진을 신청하셨군요!", "5일 전"));
        list.add(new PushGatherItem("장애인 직업재활시설 이용을 신청하셨군요!", "장애인 직업재활시설 이용을 신청하셨군요!", "1주일 전"));
        list.add(new PushGatherItem("장애대학생 교육활동 지원 사업을 신청하셨군요!", "장애대학생 교육활동 지원 사업을 신청하셨군요!", "5주일 전"));
        list.add(new PushGatherItem("장애아 방과후 보육료 지원을 신청하셨군요!", "장애아 방과후 보육료 지원을 신청하셨군요!", "6주일 전"));
        list.add(new PushGatherItem("아동수당을 신청하셨군요!", "아동수당을 신청하셨군요!", "1달 전"));
        list.add(new PushGatherItem("아이돌봄서비스를 신청하셨군요!", "아이돌봄서비스를 신청하셨군요!", "1달 전"));
        list.add(new PushGatherItem("어린이 불소 도포를 신청하셨군요!", "어린이 불소 도포를 신청하셨군요!", "2달 전"));
        list.add(new PushGatherItem("소아 암환자 의료비 지원을 신청하셨군요!", "소아 암환자 의료비 지원을 신청하셨군요!", "2달 전"));
        list.add(new PushGatherItem("선천성 난청검사 및 보청기 지원을 신청하셨군요!", "선천성 난청검사 및 보청기 지원을 신청하셨군요!", "3달 전"));
        list.add(new PushGatherItem("다함께 돌봄 사업을 신청하셨군요!", "다함께 돌봄 사업을 신청하셨군요!", "4달 전"));
        list.add(new PushGatherItem("희망복지지원단 통합사례관리사업을 신청하셨군요!", "희망복지지원단 통합사례관리사업을 신청하셨군요!", "4달 전"));
        adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener(new PushGatherAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                String push_name = list.get(position).getPush_gather_title();
                Log.e(TAG, "선택한 푸시 제목 = " + push_name + ", position = " + position);
            }
        });
        push_layout_recycler.setAdapter(adapter);
    }

}