package com.psj.welfare.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;

/* SearchFragment 바뀐 화면 테스트하는 곳
 * 구현 완료되면 SearchFragment로 옮긴다 */
public class TestSearchFragment extends Fragment
{
    EditText search_name_edittext;
    TextView search_fragment_top_textview;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    TextView recommend_tag_textview;
    // 추천 태그
    TextView recommend_old, recommend_pregnancy, recommend_living, recommend_young_man, recommend_job, recommend_corona, recommend_single_parent;

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
        search_fragment_top_textview = view.findViewById(R.id.search_fragment_top_textview);

        recommend_tag_textview = view.findViewById(R.id.recommend_tag_textview);
        recommend_old = view.findViewById(R.id.recommend_old);
        recommend_pregnancy = view.findViewById(R.id.recommend_pregnancy);
        recommend_living = view.findViewById(R.id.recommend_living);
        recommend_young_man = view.findViewById(R.id.recommend_young_man);
        recommend_job = view.findViewById(R.id.recommend_job);
        recommend_corona = view.findViewById(R.id.recommend_corona);
        recommend_single_parent = view.findViewById(R.id.recommend_single_parent);

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

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        search_fragment_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 14);
        recommend_tag_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        recommend_old.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_pregnancy.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_living.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_young_man.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_job.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_corona.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        recommend_single_parent.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);

        // 검색창
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

        /* 추천 태그별 클릭 이벤트 적용, performSearch()의 인자로 넘긴다 */
        recommend_old.setOnClickListener(v ->
        {
            performSearch("노인");
        });

        recommend_pregnancy.setOnClickListener(v ->
        {
            performSearch("임신/출산");
        });

        recommend_living.setOnClickListener(v ->
        {
            performSearch("주거");
        });

        recommend_young_man.setOnClickListener(v ->
        {
            performSearch("청년");
        });

        recommend_job.setOnClickListener(v ->
        {
            performSearch("취업/창업");
        });

        recommend_corona.setOnClickListener(v ->
        {
            performSearch("코로나");
        });

        recommend_single_parent.setOnClickListener(v ->
        {
            performSearch("한부모");
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