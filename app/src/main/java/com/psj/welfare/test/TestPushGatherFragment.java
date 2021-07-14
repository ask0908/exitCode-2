package com.psj.welfare.test;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.fragment.AllPushFragment;
import com.psj.welfare.fragment.LatestPushFragment;
import com.psj.welfare.fragment.RecommendPushFragment;
import com.psj.welfare.viewmodel.PushViewModel;

import java.util.Objects;

/**
 * 알림 프래그먼트
 */
public class TestPushGatherFragment extends Fragment
{
    private final String TAG = "TestPushGatherFragment";

    TextView push_top_textview, push_all_textview, push_latest_textview, push_recommend_textview;
    View push_all_bottom_view, push_latest_bottom_view, push_recommend_bottom_view;

    // 로그인 여부 확인 위한 쉐어드
    SharedPreferences sharedPreferences;

    LinearLayout push_all_layout, push_latest_layout, push_recommend_layout;
    Fragment AllPushFragment, LatestPushFragment, RecommendPushFragment;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;

    PushViewModel viewModel;

    public TestPushGatherFragment()
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
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        View view = inflater.inflate(R.layout.fragment_test_push_gather, container, false);

        // findViewById() 모아놓은 메서드
        init(view);

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(getActivity());

        push_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.035));
        push_all_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));
        push_latest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));
        push_recommend_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // 처음 알림 화면에 들어오면 보여주는 화면은 AllPushFragment이다
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.push_container, AllPushFragment);
        transaction.commit();

        // 전체 눌렸을 때
        push_all_layout.setOnClickListener(v ->
        {
            push_all_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_all_bottom_view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_all_bottom_view.setVisibility(View.VISIBLE);
            push_latest_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_latest_bottom_view.setVisibility(View.INVISIBLE);
            push_recommend_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_recommend_bottom_view.setVisibility(View.INVISIBLE);

            // 전체를 눌렀을 경우 AllPushFragment를 화면에 보여준다
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.push_container, AllPushFragment);
            transaction.commit();
        });

        // 최신혜택 눌렸을 때
        push_latest_layout.setOnClickListener(v ->
        {
            push_latest_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_latest_bottom_view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_latest_bottom_view.setVisibility(View.VISIBLE);
            push_all_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_all_bottom_view.setVisibility(View.INVISIBLE);
            push_recommend_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_recommend_bottom_view.setVisibility(View.INVISIBLE);

            // 최신혜택을 눌렀을 경우 LatestPushFragment를 화면에 보여준다
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.push_container, LatestPushFragment);
            transaction.commit();
        });

        // 추천혜택 눌렸을 때
        push_recommend_layout.setOnClickListener(v ->
        {
            push_recommend_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_recommend_bottom_view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.layout_background_start_gradation));
            push_recommend_bottom_view.setVisibility(View.VISIBLE);
            push_latest_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_latest_bottom_view.setVisibility(View.INVISIBLE);
            push_all_textview.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorGray_B));
            push_all_bottom_view.setVisibility(View.INVISIBLE);

            // 추천혜택을 눌렀을 경우 RecommendPushFragment를 화면에 보여준다
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.push_container, RecommendPushFragment);
            transaction.commit();
        });

    }


    // findViewById() 모아놓은 메서드
    private void init(View view)
    {
        push_top_textview = view.findViewById(R.id.push_top_textview);

        push_all_textview = view.findViewById(R.id.push_all_textview);
        push_latest_textview = view.findViewById(R.id.push_latest_textview);
        push_recommend_textview = view.findViewById(R.id.push_recommend_textview);

        push_all_bottom_view = view.findViewById(R.id.push_all_bottom_view);
        push_latest_bottom_view = view.findViewById(R.id.push_latest_bottom_view);
        push_recommend_bottom_view = view.findViewById(R.id.push_recommend_bottom_view);

        push_all_layout = view.findViewById(R.id.push_all_layout);
        push_latest_layout = view.findViewById(R.id.push_latest_layout);
        push_recommend_layout = view.findViewById(R.id.push_recommend_layout);

        AllPushFragment = new AllPushFragment();
        LatestPushFragment = new LatestPushFragment();
        RecommendPushFragment = new RecommendPushFragment();
    }

}