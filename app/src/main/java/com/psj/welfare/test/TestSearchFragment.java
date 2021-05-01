package com.psj.welfare.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.fragment.AgeGroupWelfareFragment;
import com.psj.welfare.fragment.AreaWelfareFragment;
import com.psj.welfare.fragment.SubjectWelfareFragment;

import me.relex.circleindicator.CircleIndicator;

/* SearchFragment 바뀐 화면 테스트하는 곳
 * 구현 완료되면 SearchFragment로 옮긴다 */
public class TestSearchFragment extends Fragment
{
    CircleIndicator indicator;
    ViewPager search_default_viewpager;
    TestSearchViewpagerAdapter adapter;
    EditText search_name_edittext;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    public TestSearchFragment()
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
        View view = inflater.inflate(R.layout.fragment_test_search, container, false);

        search_name_edittext = view.findViewById(R.id.search_name_edittext);
        indicator = view.findViewById(R.id.top_indicator);
        search_default_viewpager = view.findViewById(R.id.search_default_viewpager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        // 뷰페이저 어댑터 초기화 후 뷰페이저 안에 프래그먼트들 추가
        adapter = new TestSearchViewpagerAdapter(getActivity().getSupportFragmentManager());
        AreaWelfareFragment areaWelfareFragment = new AreaWelfareFragment();
        adapter.addItem(areaWelfareFragment);

        SubjectWelfareFragment subjectWelfareFragment = new SubjectWelfareFragment();
        adapter.addItem(subjectWelfareFragment);

        AgeGroupWelfareFragment ageGroupWelfareFragment = new AgeGroupWelfareFragment();
        adapter.addItem(ageGroupWelfareFragment);

        // 뷰페이저에서 보여줄 프래그먼트 개수는 3개다
        search_default_viewpager.setOffscreenPageLimit(3);
        // 뷰페이저에 프래그먼트들이 들어간 어댑터 set
        search_default_viewpager.setAdapter(adapter);

        // 인디케이터 점의 개수 = 프래그먼트 개수
        indicator.createIndicators(3, 0);
        // 뷰페이저에 인디케이터 set
        indicator.setViewPager(search_default_viewpager);

        search_name_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " +
                            search_name_edittext.getText().toString());
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    performSearch(search_name_edittext.getText().toString().trim());

                    return true;
                }
                return false;
            }
        });

    }

    public void performSearch(String search)
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_name_edittext.getWindowToken(), 0);

        Intent intent = new Intent(getActivity(), TestSearchResultActivity.class);
        intent.putExtra("keyword", search);
        startActivity(intent);
    }

}