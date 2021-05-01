package com.psj.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.adapter.AreaWelfareAdapter;
import com.psj.welfare.data.AreaWelfare;

import java.util.ArrayList;
import java.util.List;

/* TestSearchFragment의 뷰페이저 안에서 지역별 혜택을 보여줄 프래그먼트 */
public class AreaWelfareFragment extends Fragment
{
    RecyclerView subject_recyclerview;
    AreaWelfareAdapter adapter;
    List<AreaWelfare> list;
    AreaWelfareAdapter.onItemClickListener itemClickListener;

    public AreaWelfareFragment()
    {
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_area_welfare, container, false);

        subject_recyclerview = view.findViewById(R.id.subject_recyclerview);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        AreaWelfare item;
        list = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            item = new AreaWelfare();
            item.setCategory_name("# 건강");
            list.add(item);
        }
        // 그리드 리사이클러뷰(세로, 2열) 레이아웃 매니저 적용 및 어댑터 초기화
        adapter = new AreaWelfareAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener((pos -> {
            String name = list.get(pos).getCategory_name();
            Toast.makeText(getActivity(), "선택한 버튼명 : " + name, Toast.LENGTH_SHORT).show();
        }));
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
        subject_recyclerview.setLayoutManager(glm);
        subject_recyclerview.setAdapter(adapter);
        subject_recyclerview.setHasFixedSize(true);
    }

}