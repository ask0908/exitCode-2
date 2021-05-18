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
import com.psj.welfare.adapter.AreaWelfareAdapter;
import com.psj.welfare.data.AreaWelfare;
import com.psj.welfare.util.RecyclerViewHeightDecoration;
import com.psj.welfare.util.RecyclerViewWidthDecoration;

import java.util.ArrayList;

/* TestSearchFragment의 뷰페이저 안에서 지역별 혜택을 보여줄 프래그먼트 */
public class AreaWelfareFragment extends Fragment
{
    TextView area_title;
    RecyclerView area_recyclerview;
    AreaWelfareAdapter adapter;
    ArrayList<AreaWelfare> list;
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

        area_title = view.findViewById(R.id.area_title);
        area_recyclerview = view.findViewById(R.id.area_recyclerview);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        area_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);

        return view;
    }

//    private void SetSize() {
//        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
//        Display display = getWindowManager().getDefaultDisplay();  // in Activity
//        Point size = new Point();
//        display.getRealSize(size); // or getSize(size)
//
//        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
//        BenefitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
//
//        score_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //별점 타이틀
//        score_textview.setPadding(0, size.y / 100, 0, size.y / 130); //타이틀 패딩값 적용
//
//        please_tab_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //탭해서 별점주기
//
//        level_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //과정 평가
//        level_textview.setPadding(0, size.y / 75, 0, size.y / 130); //과정 평가 패딩값 적용
//        satisfaction_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //만족도 평가
//        satisfaction_textview.setPadding(0, size.y / 75, 0, 0); //만족도 패딩값 적용
//        your_opinion_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //의견 남겨주세요
//        your_opinion_textview.setPadding(0, size.y / 75, 0, size.y / 100); //의견란 패딩값 적용
//
////        ViewGroup.LayoutParams params_star = review_star.getLayoutParams();
////        params_star.width = size.x/7*6; params_star.height = size.y/2;
////        review_star.setLayoutParams(params_star);
//
//        ViewGroup.LayoutParams params_radiogroup = difficulty_radiogroup.getLayoutParams(); //난이도 라디오 버튼 그룹
//        params_radiogroup.height = size.y / 12;
//        difficulty_radiogroup.setLayoutParams(params_radiogroup);
//
//        ViewGroup.LayoutParams params_radiogroup2 = satisfaction_radiogroup.getLayoutParams(); //만족도 라디오 버튼 그룹
//        params_radiogroup2.height = size.y / 12;
//        satisfaction_radiogroup.setLayoutParams(params_radiogroup2);
//
//        easy_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //쉬워요 버튼 텍스트 크기
//        easy_radiobutton.getLayoutParams().height = size.y / 18; //쉬워요 버튼 크기 변경
//        easy_radiobutton.getLayoutParams().width = size.x / 6; //쉬워요 버튼 크기 변경
//
//        hard_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //어려워요 버튼 텍스트 크기
//        hard_radiobutton.getLayoutParams().height = size.y / 18; //어려워요 버튼 크기 변경
//        hard_radiobutton.getLayoutParams().width = size.x / 6; //어려워요 버튼 크기 변경
//
//        good_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 돼요 버튼 텍스트 크기
//        good_radiobutton.getLayoutParams().height = size.y / 18; //도움 돼요 버튼 크기 변경
//        good_radiobutton.getLayoutParams().width = size.x / 6; //도움 돼요 버튼 크기 변경
//
//        bad_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 안돼요 버튼 텍스트 크기
//        bad_radiobutton.getLayoutParams().height = size.y / 18; //도움 안돼요 버튼 크기 변경
//        bad_radiobutton.getLayoutParams().width = size.x / 6; //도움 안돼요 버튼 크기 변경
//
//        review_content_edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24);
//        review_content_edit.getLayoutParams().height = size.x / 3 * 2; //의견란
//        review_content_edit.getLayoutParams().width = size.x / 6 * 5; //의견란
//        review_content_edit.setPadding(size.y / 70, size.y / 70, size.y / 70, size.y / 70); //의견란 패딩값 적용
//
//        text_length_layout.getLayoutParams().height = size.y / 24; //의견란 글자수 레이아웃 크기 변경
//        text_length_layout.getLayoutParams().width = size.x / 6 * 5; //의견란 글자수 레이아웃 크기 변경
//        text_length_layout.setPadding(0, size.y / 150, 0, size.y / 90); //의견란 글자수 레이아웃 패딩값 적용
//
//        text_length.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 현재 글자수
//        text_length_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 최대 글자수
//
//        btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 20); //등록 버튼 텍스트 크기
//        btnRegister.getLayoutParams().height = size.y / 16; //등록 버튼 크기 변경
//        btnRegister.getLayoutParams().width = size.x / 6 * 5; //등록 버튼 크기 변경
//        btnRegister.setPadding(0, size.y / 90, 0, size.y / 80); //등록 버튼패딩값 적용
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

//        AreaWelfare item;
        list = new ArrayList<>();
        list.add(new AreaWelfare("# 서울"));
        list.add(new AreaWelfare("# 경기"));
        list.add(new AreaWelfare("# 인천"));
        list.add(new AreaWelfare("# 강원"));
        list.add(new AreaWelfare("# 충남"));
        list.add(new AreaWelfare("# 충북"));
        list.add(new AreaWelfare("# 경북"));
        list.add(new AreaWelfare("# 경남"));
        list.add(new AreaWelfare("# 전북"));
        list.add(new AreaWelfare("# 전남"));
        list.add(new AreaWelfare("# 제주"));
//        item = new AreaWelfare();
//        item.setCategory_name("# 교육");
//        item.setCategory_name("# 건강");
//        item.setCategory_name("# 근로");
//        item.setCategory_name("# 금융");
//        item.setCategory_name("# 기타");
//        item.setCategory_name("# 문화");
//        item.setCategory_name("# 사업");
//        item.setCategory_name("# 주거");
//        item.setCategory_name("# 환경");
//        list.add(item);
//        for (int i = 0; i < 20; i++)
//        {
//            item = new AreaWelfare();
//            item.setCategory_name("# 건강");
//            list.add(item);
//        }
        // 그리드 리사이클러뷰(세로, 2열) 레이아웃 매니저 적용 및 어댑터 초기화
        adapter = new AreaWelfareAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener((pos -> {
            String name = list.get(pos).getLocal_name();
            Toast.makeText(getActivity(), "선택한 버튼명 : " + name, Toast.LENGTH_SHORT).show();
        }));
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2);
        area_recyclerview.setLayoutManager(glm);
        area_recyclerview.setAdapter(adapter);
        // 리사이클러뷰 아이템 간 가로, 세로 간격 조절
        area_recyclerview.addItemDecoration(new RecyclerViewWidthDecoration(10));
        area_recyclerview.addItemDecoration(new RecyclerViewHeightDecoration(30));
        area_recyclerview.setHasFixedSize(true);
    }

}