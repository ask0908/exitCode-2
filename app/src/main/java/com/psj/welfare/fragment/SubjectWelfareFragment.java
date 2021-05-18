package com.psj.welfare.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.adapter.SubjectWelfareAdapter;
import com.psj.welfare.data.SubjectWelfareItem;
import com.psj.welfare.util.RecyclerViewHeightDecoration;
import com.psj.welfare.util.RecyclerViewWidthDecoration;

import java.util.ArrayList;

/* TestSearchFragment의 뷰페이저 안에서 주제별 혜택을 보여줄 프래그먼트 */
public class SubjectWelfareFragment extends Fragment
{
    TextView subject_title;
    RecyclerView subject_recyclerview;
    ArrayList<SubjectWelfareItem> list;
    SubjectWelfareAdapter adapter;
    SubjectWelfareAdapter.onItemClickListener itemClickListener;

    public SubjectWelfareFragment()
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
        View view = inflater.inflate(R.layout.fragment_subject_welfare, container, false);

        subject_title = view.findViewById(R.id.subject_title);
        subject_recyclerview = view.findViewById(R.id.subject_recyclerview);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        subject_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        list = new ArrayList<>();
        list.add(new SubjectWelfareItem("# 교육"));
        list.add(new SubjectWelfareItem("# 건강"));
        list.add(new SubjectWelfareItem("# 근로"));
        list.add(new SubjectWelfareItem("# 금융"));
        list.add(new SubjectWelfareItem("# 기타"));
        list.add(new SubjectWelfareItem("# 문화"));
        list.add(new SubjectWelfareItem("# 사업"));
        list.add(new SubjectWelfareItem("# 주거"));
        list.add(new SubjectWelfareItem("# 환경"));

        adapter = new SubjectWelfareAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener((pos -> {
            String name = list.get(pos).getSubject();
            Toast.makeText(getActivity(), "선택한 버튼명 : " + name, Toast.LENGTH_SHORT).show();
        }));
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
        subject_recyclerview.setLayoutManager(glm);
        subject_recyclerview.setAdapter(adapter);
        // 리사이클러뷰 아이템 간 가로, 세로 간격 조절
        subject_recyclerview.addItemDecoration(new RecyclerViewWidthDecoration(10));
        subject_recyclerview.addItemDecoration(new RecyclerViewHeightDecoration(30));
        subject_recyclerview.setHasFixedSize(true);
    }
}