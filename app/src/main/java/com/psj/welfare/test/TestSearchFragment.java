package com.psj.welfare.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;

/* SearchFragment 바뀐 화면 테스트하는 곳
 * 구현 완료되면 SearchFragment로 옮긴다 */
public class TestSearchFragment extends Fragment
{
    private LinearLayout tag_total_layout; //태그 전체 레이아웃
    private ConstraintLayout tag_layout1,tag_layout2,tag_layout3; //태그 레이아웃 노인 임신/출산, 주거 청년 취업/창업, 코로나 한부모
    private EditText search_name_edittext; //검색 바
    private TextView search_fragment_top_textview; //"어떤 혜택을 찾으세요?" 텍스트
    private View recommend_firstview,recommend_second_firstview,recommend_second_secondview,recommend_thirdview; //태그(ex "#주거" "#청년") 사이에 간격을 임의로 주기 위한 뷰
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

        tag_total_layout = view.findViewById(R.id.tag_total_layout);
        tag_layout1 = view.findViewById(R.id.tag_layout1);
        tag_layout2 = view.findViewById(R.id.tag_layout2);
        tag_layout3 = view.findViewById(R.id.tag_layout3);

        recommend_firstview = view.findViewById(R.id.recommend_firstview);
        recommend_second_firstview = view.findViewById(R.id.recommend_second_firstview);
        recommend_second_secondview = view.findViewById(R.id.recommend_second_secondview);
        recommend_thirdview = view.findViewById(R.id.recommend_thirdview);

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

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());

//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getRealSize(size);

        //태그 전체 레이아웃
        tag_total_layout.setPadding((int)(size.x * 0.07),0,0,0);
        //"어떤 혜택을 찾으세요" 텍스트
        search_fragment_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.035));
        //검색
        search_name_edittext.setPadding((int)(size.x * 0.06),0,(int)(size.x * 0.06),0);
        //태그 레이아웃 첫번째 줄, 두번째 줄, 세번째 줄
        tag_layout1.setPadding(0,(int)(size.y * 0.015),0,(int)(size.y * 0.015));
        tag_layout2.setPadding(0,(int)(size.y * 0.015),0,(int)(size.y * 0.015));
        tag_layout3.setPadding(0,(int)(size.y * 0.015),0,(int)(size.y * 0.015));
        //"추천 태그" 텍스트
        recommend_tag_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.05));
        recommend_tag_textview.setPadding((int)(size.x * 0.08),0,(int)(size.x * 0.08),0);
        //태그 사이 간격
        recommend_firstview.getLayoutParams().width = (int)(size.x * 0.05);
        recommend_second_firstview.getLayoutParams().width = (int)(size.x * 0.05);
        recommend_second_secondview.getLayoutParams().width = (int)(size.x * 0.05);
        recommend_thirdview.getLayoutParams().width = (int)(size.x * 0.05);

        //태그 "노인"
        recommend_old.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_old.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "임신/출산"
        recommend_pregnancy.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_pregnancy.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "주거"
        recommend_living.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_living.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "청년"
        recommend_young_man.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_young_man.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "취업/창업"
        recommend_job.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_job.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "코로나"
        recommend_corona.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_corona.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));
        //태그 "한부모"
        recommend_single_parent.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.045));
        recommend_single_parent.setPadding((int)(size.x * 0.05),(int)(size.x * 0.025),(int)(size.x * 0.05),(int)(size.x * 0.025));

        // 검색창
        search_name_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (search_name_edittext.length() == 0 )
                {
                    Toast.makeText(getContext(), "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return true;
                } else if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " + search_name_edittext.getText().toString());
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    performSearch(search_name_edittext.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });


        //키보드 환경 설정(화면에 띄워져 있는지 판단하기 위함)
        InputMethodManager keyboard = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        //edittext 오른쪽 drawble 아이콘 클릭
        search_name_edittext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawable = search_name_edittext.getCompoundDrawables()[2]; //[0] -> left아이콘, [1] -> top아이콘, [2] -> right아이콘, [3] -> bottom아이콘
                if(event.getAction() == MotionEvent.ACTION_UP){ //edittext에서 터치를 땠을 때
                    if (drawable != null) { //edittext에서 오른쪽 아이콘이 있을 때
                        //edittext에서 오른쪽 아이콘 클릭 했을 때 (너비랑 아이콘 패딩, 아이콘 크기 값 으로 계산)
                        if(event.getX() >= (search_name_edittext.getWidth() - search_name_edittext.getPaddingRight() - drawable.getIntrinsicWidth())){

                            //키보드 열려 있을때만 검색 눌렀을 때 결과값 보여주기
                            if (search_name_edittext.length() == 0 )
                            {
                                Toast.makeText(getContext(), "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show();
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " + search_name_edittext.getText().toString());
                                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                                performSearch(search_name_edittext.getText().toString().trim());
                            }

//                            if(keyboard.isActive()){ //키보드 열려 있는지
//                                Log.e("TAG","키보드 열려 있음");
//
//                            } else {
//                                Log.e("TAG","키보드 닫혀 있음");
//                            }
                            return false;
                        }
                    }
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