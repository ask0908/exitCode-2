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
import com.psj.welfare.adapter.AgeGroupAdapter;
import com.psj.welfare.data.AgeGroupItem;
import com.psj.welfare.util.RecyclerViewHeightDecoration;
import com.psj.welfare.util.RecyclerViewWidthDecoration;

import java.util.ArrayList;

/* TestSearchFragment의 뷰페이저 안에서 나이대별 혜택을 보여줄 프래그먼트 */
public class AgeGroupWelfareFragment extends Fragment
{
    TextView age_title;
    RecyclerView age_recyclerview;
    ArrayList<AgeGroupItem> list;
    AgeGroupAdapter adapter;
    AgeGroupAdapter.onItemClickListener itemClickListener;

    public AgeGroupWelfareFragment()
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
        View view = inflater.inflate(R.layout.fragment_age_group_welfare, container, false);

        age_title = view.findViewById(R.id.age_title);
        age_recyclerview = view.findViewById(R.id.age_recyclerview);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        age_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        list = new ArrayList<>();
        list.add(new AgeGroupItem("10대"));
        list.add(new AgeGroupItem("20대"));
        list.add(new AgeGroupItem("30대"));
        list.add(new AgeGroupItem("40대"));
        list.add(new AgeGroupItem("50대"));
        list.add(new AgeGroupItem("60대 이상"));

        adapter = new AgeGroupAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener(pos -> {
            String name = list.get(pos).getAge();
            Toast.makeText(getActivity(), "선택한 버튼명 : " + name, Toast.LENGTH_SHORT).show();
        });
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
        age_recyclerview.setLayoutManager(glm);
        age_recyclerview.setAdapter(adapter);
        age_recyclerview.addItemDecoration(new RecyclerViewWidthDecoration(10));
        age_recyclerview.addItemDecoration(new RecyclerViewHeightDecoration(30));
        age_recyclerview.setHasFixedSize(true);
    }
}