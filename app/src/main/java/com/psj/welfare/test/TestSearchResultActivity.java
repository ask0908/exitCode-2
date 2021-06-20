package com.psj.welfare.test;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.orhanobut.logger.Logger;
import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.adapter.ExpandableRecyclerViewAdapter;
import com.psj.welfare.adapter.InnerRecyclerViewAdapter;
import com.psj.welfare.adapter.RenewalSearchResultAdapter;
import com.psj.welfare.adapter.SearchResultHorizontalAdapter;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.SearchResultItem;
import com.psj.welfare.viewmodel.SearchViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/* 매니페스트에서 android:windowSoftInputMode="adjustNothing" 속성 추가해 editText 때문에 UI가 뭉개지지 않게 함 */
public class TestSearchResultActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public final String TAG = this.getClass().getSimpleName();

    SearchViewModel searchViewModel;
    EditText search_result_edittext;
    TextView total_search_result;
    RecyclerViewEmptySupport search_result_recyclerview;
    // 필터 검색 결과 없을 때 사용할 텍스트뷰
    TextView search_result_empty_textview;
    ImageView search_result_no_image;
    RenewalSearchResultAdapter adapter;
    ArrayList<SearchResultItem> list;
    RenewalSearchResultAdapter.onItemClickListener itemClickListener;

    // 검색 화면에서 가져온 검색어
    String keyword;
    // 총 검색 결과 개수, 서버에 있는 전체 페이지 수
    String total_result_count, total_paging_count;
    // 검색 결과를 담을 변수
    String welf_id, welf_name, welf_tag, welf_count, welf_local, welf_thema, welf_category, welf_age;
    // 페이징 시 메서드 인자로 넘기는 받아야 할 페이지 숫자를 담을 변수
    // parseInt()로 int로 바꾼 다음 ++해서 다시 String으로 바꿔 이 변수에 담아야 한다
    String current_page;
    int integer_page = 1;

    // 다음 페이징 데이터를 가져오는 도중 보여줄 프로그레스바
    ProgressBar progressbar;

    NavigationView search_result_drawer;
    //    DrawerLayout search_result_drawer;
    // 오른쪽에서 나오는 드로어블 레이아웃에 먹일 확장 리사이클러뷰
    RecyclerView expanderRecyclerView;

    ConstraintLayout search_result_filter;
    private Button filter_button;
    private TextView filter_textview, filter_layout_text;
    // 필터 대분류들을 담을 ArrayList
    ArrayList<String> parentList = new ArrayList<>();
    // 카테고리, 지역, 지원 형태, 나이대를 담을 ArrayList
    // -> 이것들을 각각 다른 ArrayList에 담아서 어댑터 생성자로 넘겨야 한다
    ArrayList<ArrayList> childListHolder = new ArrayList<>();
    // 카테고리들을 담을 ArrayList
    ArrayList<String> categoryList = new ArrayList<>();
    // 지역을 담을 ArrayList
    ArrayList<String> localList = new ArrayList<>();
    // 지원 형태를 담을 ArrayList
    ArrayList<String> provideTypeList = new ArrayList<>();
    // 나이대를 담을 ArrayList
    ArrayList<String> ageList = new ArrayList<>();
    // 선택한 체크박스 값들을 모두 담을 리스트
    ArrayList<String> childList = new ArrayList<>();

    // 가로로 선택한 필터들을 보여줄 때 사용할 리사이클러뷰
    RecyclerView selected_filter_recyclerview;
    SearchResultHorizontalAdapter filter_adapter;
    SearchResultHorizontalAdapter.ItemClickListener filter_clickListener;
    ArrayList<String> mCategoryList;
    ArrayList<String> mLocalList;
    ArrayList<String> mProvideTypeList;
    ArrayList<String> mAgeList;
    ArrayList<String> allList;
    ArrayList<String> item;

    private Parcelable recyclerViewState;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_search_result);

        search_result_drawer = findViewById(R.id.search_result_drawer);
        progressbar = findViewById(R.id.progressbar);
        total_search_result = findViewById(R.id.total_search_result);
        search_result_edittext = findViewById(R.id.search_result_edittext);
        search_result_recyclerview = findViewById(R.id.search_result_recyclerview);
        search_result_empty_textview = findViewById(R.id.search_result_empty_textview);
        search_result_no_image = findViewById(R.id.search_result_no_image);
        search_result_empty_textview.setVisibility(View.GONE);
        search_result_filter = findViewById(R.id.search_result_filter);
        selected_filter_recyclerview = findViewById(R.id.selected_filter_recyclerview);

        search_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // 선택한 필터들을 보여주는 리사이클러뷰는 가로 모양이다
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        selected_filter_recyclerview.setLayoutManager(llm);

        allList = new ArrayList<>();
        list = new ArrayList<>();
        mCategoryList = new ArrayList<>();
        mLocalList = new ArrayList<>();
        mProvideTypeList = new ArrayList<>();
        mAgeList = new ArrayList<>();
        item = new ArrayList<>();

        // 검색어 담긴 변수 널 체크
        if (getIntent().hasExtra("keyword"))
        {
            Intent intent = getIntent();
            keyword = intent.getStringExtra("keyword");
            search_result_edittext.setText(keyword);
            firstCalledSearchMethod(String.valueOf(integer_page));
        }

        search_result_drawer.setVisibility(View.INVISIBLE);
        filter_button = findViewById(R.id.filter_button);
        filter_textview = findViewById(R.id.filter_textview);
        filter_layout_text = findViewById(R.id.filter_layout_text);

        // 뷰 크기, 글자 크기 조절
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        search_result_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);  // 검색 결과 없을 때 보여주는 텍스트뷰
        filter_layout_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 26);
        total_search_result.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        filter_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 21);
        filter_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);

        search_result_filter.setOnClickListener(v -> search_result_drawer.setVisibility(View.VISIBLE));

        // 필터 적용 버튼
        filter_button.setOnClickListener(v ->
        {
            // 선택한 필터들이 보이는 리사이클러뷰을 VISIBLE로 바꾸고
            // 하단 리사이클러뷰의 머리를 이 리사이클러뷰의 밑으로 연결짓는다
            selected_filter_recyclerview.setVisibility(View.VISIBLE);
            /* 체인을 바꿀 리사이클러뷰 : search_result_recyclerview(topToBottom을 수정)
            * 원래 topToBottom에 묶여있던 뷰 : result_filter_layout
            * 필터 적용 시 체인을 연결할 뷰 : selected_filter_recyclerview(필터 적용 시 visible로 속성 변경)
            * 필터 적용 전 : search_result_recyclerview의 layout_constraintHeight_percent를 9로 변경 + layout_constraintVertical_bias를 0으로 변경
            * 필터 적용 후 : search_result_recyclerview의 layout_constraintHeight_percent를 8로 변경 + layout_constraintVertical_bias를 1로 변경 */
            @SuppressLint("CutPasteId")
            ConstraintLayout constraintLayout = findViewById(R.id.search_result_constraint_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            // 필터 적용 버튼을 누르면 search_result_recyclerview의 top을 -> selected_filter_recyclerview의 bottom에 연결한다
            constraintSet.connect(R.id.search_result_recyclerview,    // startId : 어떤 뷰의 체인을 바꿀 것인가?
                    ConstraintSet.TOP,                                // startSide : 그 뷰의 어디를 연결할 것인가?
                    R.id.selected_filter_recyclerview,                // endId : 어디에 체인을 걸 것인가?
                    ConstraintSet.BOTTOM,                             // endSide : 그 뷰의 어디에 1번 인자로 받은 뷰를 연결할 것인가?
                    0);                                       // margin : 제한할 여백(양수여야 함)
            constraintSet.constrainPercentHeight(R.id.search_result_recyclerview, (float) 0.8);
            constraintSet.setVerticalBias(R.id.search_result_recyclerview, 1);
            constraintSet.applyTo(constraintLayout);

            // 필터 적용 버튼을 누르면 선택한 필터에 해당하는 조건들로 검색한다
            // 먼저 버튼을 누르면 선택한 값들을 가져와야 한다. 선택한 필터는 '혜택 총 n개' 밑에 예전 관심사 리스트처럼 보여줘야 한다
            String gainedValues = InnerRecyclerViewAdapter.getAllValues();
            String[] arr = gainedValues.split("zz");
            Log.e(TAG, "arr : " + Arrays.toString(arr));
            Log.e(TAG, "arr[0] : " + arr[0]);   // 카테고리
            Log.e(TAG, "arr[1] : " + arr[1]);   // 지역
            Log.e(TAG, "arr[2] : " + arr[2]);   // 나이대
            Log.e(TAG, "arr[3] : " + arr[3]);   // 지원 형태

            // 필터에서 선택한 값들을 토대로 서버에 다시 쿼리
            renewalKeywordSearch("1", arr[0], arr[1], arr[2], arr[3]);

            // null로 인한 에러를 막기 위한 null 처리
            // String[]의 각 요소가 null이 아니라면 넣는다
            if (!arr[0].equals("null"))
            {
                allList.add(arr[0]);
            }
            if (!arr[1].equals("null"))
            {
                allList.add(arr[1]);
            }
            if (!arr[2].equals("null"))
            {
                allList.add(arr[2]);
            }
            if (!arr[3].equals("null"))
            {
                allList.add(arr[3]);
            }

            // 선택한 필터들을 리사이클러뷰에 보여준다
            filter_adapter = new SearchResultHorizontalAdapter(this, allList, categoryList, localList, provideTypeList, ageList, filter_clickListener);
            // 클릭 리스너 추가 -> 필터를 선택할 때마다 선택한 값들이 삭제되고 남아있는 값들로 다시 서버에 쿼리해야 한다
            // TODO : 필터 클릭 시 삭제 후 재쿼리하는 처리 진행 중
            filter_adapter.setOnItemClickListener(pos ->
            {
                // 각 리스트를 반복하면서 리스트 안의 값이 선택한 값과 일치하는 경우에만 클릭 리스너를 호출해야 한다
                // 어댑터에서 처리가 끝난 리스트를 받아서 그 값들을 액티비티의 리스트에 옮겨 담는다
                item = filter_adapter.getList();
                String name = item.get(pos);
                Log.e(TAG, "가로 필터 리사이클러뷰에서 선택한 필터 : " + name);
                for (int i = 0; i < list.size(); i++)
                {
                    // 가로 리사이클러뷰에서 선택한 이름의 값이 카테고리인지 테마인지 나이대, 지역인지 알아야 한다
                    // 일치할 경우 없애서 해당 필터에 속하는 혜택은 하단 리사이클러뷰에 보이지 않도록 한다
                    if (list.get(i).getWelf_thema().equals(name))
                    {
                        Log.d(TAG, "245 - welf_thema 가져온 것 : " + list.get(i).getWelf_thema());
                        Log.d(TAG, "245 - list.get(i).getWelf_thema().equals(name) : " + list.get(i).getWelf_thema().equals(name));
                        list.remove(list.get(i));
                        adapter.notifyDataSetChanged();
                        /* 반응형 프로그래밍은 세 부분으로 구성돼 있다
                        * 1. input : 이벤트가 시작되는 부분, 문자열 / 배열 / ArrayList<T> / 사용자 이벤트 / 리스트뷰 같은 UI 컴포넌트 / 서버와의 통신도 가능
                        * 2. operators : 이벤트를 가공하고 조합(compose)해서 결과를 만드는 부분, 결과를 가공하는 부분이 조건문, 반복문 따위의 제어문이 아님
                        * 제어문은 명령형 프로그래밍의 요소인데, Rxjava에선 메서드 체이닝을 통해 operators를 연속적으로 붙일 수 있다. 이걸 조합(compose)한다고 한다
                        * 반응형 프로그래밍에는 기본 제공되는 operators의 개수가 많다. 자세한 건 https://rxmarbles.com/#from 참고(그림으로 설명하는 곳)
                        * 3. output : 가공한 결과를 출력하는 부분 */
                        Observable.just(total_search_result.getText().toString())
                                .map(s -> "혜택 총 " + list.size() + "개")
                                .subscribeOn(Schedulers.io())
                                .subscribe(s -> total_search_result.setText(s));
                    }
                    else if (list.get(i).getWelf_category().equals(name))
                    {
                        Log.d(TAG, "252 - welf_category 가져온 것 : " + list.get(i).getWelf_category());
                        Log.d(TAG, "252 - list.get(i).getWelf_category().equals(name) : " + list.get(i).getWelf_category().equals(name));
                        list.remove(list.get(i));
                        adapter.notifyDataSetChanged();
                        Observable.just(total_search_result.getText().toString())
                                .map(s -> "혜택 총 " + list.size() + "개")
                                .subscribeOn(Schedulers.io())
                                .subscribe(s -> total_search_result.setText(s));
                    }
                    else if (list.get(i).getWelf_local().equals(name))
                    {
                        Log.d(TAG, "257 - welf_local 가져온 것 : " + list.get(i).getWelf_local());
                        Log.d(TAG, "257 - list.get(i).getWelf_local().equals(name) : " + list.get(i).getWelf_local().equals(name));
                        list.remove(list.get(i));
                        adapter.notifyDataSetChanged();
                        Observable.just(total_search_result.getText().toString())
                                .map(s -> "혜택 총 " + list.size() + "개")
                                .subscribeOn(Schedulers.io())
                                .subscribe(s -> total_search_result.setText(s));
                    }
                    else if (list.get(i).getWelf_age().contains(name))
                    {
                        Log.d(TAG, "262 - welf_age 가져온 것 : " + list.get(i).getWelf_age());
                        Log.d(TAG, "262 - list.get(i).getWelf_age().contains(name) : " + list.get(i).getWelf_tag().contains(name));
                        list.remove(list.get(i));
                        adapter.notifyDataSetChanged();
                        Observable.just(total_search_result.getText().toString())
                                .map(s -> "혜택 총 " + list.size() + "개")
                                .subscribeOn(Schedulers.io())
                                .subscribe(s -> total_search_result.setText(s));
                    }
                }
            });

            selected_filter_recyclerview.setAdapter(filter_adapter);

            search_result_drawer.setVisibility(View.GONE);
        });

        // 확장 / 축소 리사이클러뷰
        expanderRecyclerView = findViewById(R.id.expanderRecyclerView);
        initiateExpander();

        // editText에서 검색되도록 처리
        search_result_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    // 검색어와 "1"을 인자로 넘긴다
                    performSearch(search_result_edittext.getText().toString().trim(), String.valueOf(integer_page));
                }
                return false;
            }
        });

        // 리사이클러뷰 페이징 처리
        search_result_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm != null)
                {
                    if (llm.getItemCount() == 0)
                    {
                        //
                    }
                    else
                    {
                        // 검색 결과가 있다면 마지막 스크롤 위치를 구해서 마지막 페이지에 도착했을 경우 페이징 처리된 다음 데이터를 가져오도록 한다
                        int totalItemCount = llm.getItemCount();
                        int lastVisible = llm.findLastCompletelyVisibleItemPosition();  // 마지막 아이템의 pos 값을 리턴하는 메서드

                        String log_str = String.valueOf(integer_page);
                        if (!log_str.equals(String.valueOf(total_paging_count)))
                        {
                            if (lastVisible >= totalItemCount - 1)
                            {
                                integer_page++;
                                progressbar.setVisibility(View.VISIBLE);
                                current_page = String.valueOf(integer_page);
                                renewalKeywordSearch(current_page, null, null, null, null);
                            }
                        }
                        else
                        {
                            Log.e(TAG, "서버에서 불러올 데이터가 없음");
                        }
                    }
                }
            }
        });

        // 인텐트로 받은 추천 태그 검색
        if (keyword.equals("노인") || keyword.equals("임신/출산") || keyword.equals("주거") || keyword.equals("청년") || keyword.equals("취업/창업") ||
                keyword.equals("코로나") || keyword.equals("한부모"))
        {
            searchRecommendTag(keyword, String.valueOf(integer_page));
        }

    }

    /* 필터 내용 초기화 */
    private void initiateExpander()
    {
        parentList.add("카테고리");
        parentList.add("지역");
        parentList.add("지원 형태");
        parentList.add("나이대");

        // 카테고리
        categoryList.add("교육");
        categoryList.add("건강");
        categoryList.add("근로");
        categoryList.add("금융");
        categoryList.add("기타");
        categoryList.add("문화");
        categoryList.add("사업");
        categoryList.add("주거");
        categoryList.add("환경");
        childList.addAll(categoryList);

        childListHolder.add(categoryList);

        // 지역
        localList.add("서울");
        localList.add("경기");
        localList.add("인천");
        localList.add("강원");
        localList.add("충남");
        localList.add("충북");
        localList.add("경북");
        localList.add("경남");
        localList.add("전북");
        localList.add("전남");
        localList.add("제주");
        childList.addAll(localList);

        childListHolder.add(localList);

        // 지원 형태
        provideTypeList.add("현금 지원");
        provideTypeList.add("물품 지원");
        provideTypeList.add("서비스 지원");
        provideTypeList.add("세금 면제");
        provideTypeList.add("감면");
        provideTypeList.add("정보 지원");
        provideTypeList.add("교육 지원");
        provideTypeList.add("인력 지원");
        provideTypeList.add("시설 지원");
        provideTypeList.add("카드 지원");
        provideTypeList.add("주거 지원");
        provideTypeList.add("일자리 지원");
        provideTypeList.add("융자 지원");
        provideTypeList.add("면제 지원");
        childList.addAll(provideTypeList);

        childListHolder.add(provideTypeList);

        // 나이대
        ageList.add("10대");
        ageList.add("20대");
        ageList.add("30대");
        ageList.add("40대");
        ageList.add("50대");
        ageList.add("60대 이상");
        childList.addAll(ageList);

        childListHolder.add(ageList);

        // 값이 담긴 리스트들을 어댑터 생성자에 넣어 초기화
        // 어댑터 안에서 값이 들어오는지 확인
        ExpandableRecyclerViewAdapter expandableCategoryRecyclerViewAdapter =
                new ExpandableRecyclerViewAdapter(TestSearchResultActivity.this, parentList, categoryList, ageList, localList, provideTypeList, childListHolder, childList);

        expanderRecyclerView.setLayoutManager(new LinearLayoutManager(TestSearchResultActivity.this));

        expanderRecyclerView.setAdapter(expandableCategoryRecyclerViewAdapter);
    }

    // 추천 태그 검색 메서드
    private void searchRecommendTag(String keyword, String page)
    {
        final ProgressDialog dialog = new ProgressDialog(TestSearchResultActivity.this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        final Observer<String> recommendSearchObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "액티비티에서 받은 추천 태그 검색 결과 : " + str);
                    responseParsing(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "검색어가 null입니다");
                }
            }
        };

        searchViewModel.searchRecommendTag(keyword, page, "tag")
                .observe(this, recommendSearchObserver);
    }

    // 검색 결과 화면에서 재검색했을 때 호출하는 메서드
    private void performSearch(String inner_keyword, String inner_page)
    {
        // 재검색 시 새로 받은 데이터로 리스트를 채워야 하기 때문에 기존에 데이터가 들어있던 리스트를 비운다
        list.clear();
        final ProgressDialog dialog = new ProgressDialog(TestSearchResultActivity.this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        if (keyword != null && !keyword.equals(""))
        {
            searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
            final Observer<String> searchObserver = new Observer<String>()
            {
                @Override
                public void onChanged(String str)
                {
                    if (str != null)
                    {
                        Log.e(TAG, "검색 결과 화면의 editText로 검색한 결과 : " + str);
                        responseParsing(str);
                        dialog.dismiss();
                    }
                    else
                    {
                        Log.e(TAG, "검색어가 null입니다");
                    }
                }
            };

            // 키워드 검색만 했을 경우에 사용한다
            // 넣어야 할 인자 : keyword, page, category, local, age, provideType
            searchViewModel.renewalSearchKeyword(inner_keyword, inner_page, null, null, null, null)
                    .observe(this, searchObserver);
        }
    }

    // 검색 화면에서 이 화면으로 넘어왔을 때(키워드만 사용한 검색) 호출하는 메서드
    public void firstCalledSearchMethod(String page)
    {
        final ProgressDialog dialog = new ProgressDialog(TestSearchResultActivity.this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();
        if (keyword != null && !keyword.equals(""))
        {
            searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
            final Observer<String> searchObserver = new Observer<String>()
            {
                @Override
                public void onChanged(String str)
                {
                    if (str != null)
                    {
                        Log.d(TAG, "↓ 검색 결과");
                        Logger.json(str);
                        responseParsing(str);
                        dialog.dismiss();
                    }
                    else
                    {
                        Log.e(TAG, "str이 null입니다");
                    }
                }
            };

            // 키워드 검색만 했을 경우에 사용한다
            // 넣어야 할 인자 : keyword, page, category, local, age, provideType
            searchViewModel.renewalSearchKeyword(keyword, page, null, null, null, null)
                    .observe(this, searchObserver);
        }
    }

    // 검색 결과 화면에서 검색어 입력 후 검색 시 호출하는 메서드
    public void renewalKeywordSearch(String page, String category, String local, String age, String provideType)
    {
        if (keyword != null && !keyword.equals(""))
        {
//            Log.e(TAG, "검색할 키워드 : " + keyword);
            searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
            final Observer<String> searchObserver = new Observer<String>()
            {
                @Override
                public void onChanged(String str)
                {
                    if (str != null)
                    {
                        Log.d(TAG, "↓ 검색 결과");
                        Logger.json(str);
                        responseParsing(str);
                    }
                    else
                    {
                        Log.e(TAG, "str이 null입니다");
                    }
                }
            };

            // 키워드 검색만 했을 경우에 사용한다
            // 넣어야 할 인자 : keyword, page, category, local, age, provideType
            searchViewModel.renewalSearchKeyword(keyword, page, category, local, age, provideType)
                    .observe(this, searchObserver);
        }
    }

    // 검색 결과 파싱 메서드
    @SuppressLint("CheckResult")
    private void responseParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total_result_count = jsonObject.getString("TotalCount");
            total_paging_count = jsonObject.getString("TotalPage");

            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                welf_id = inner_json.getString("welf_id");
                welf_name = inner_json.getString("welf_name");
                welf_tag = inner_json.getString("welf_tag");
                welf_count = inner_json.getString("welf_count");
                welf_local = inner_json.getString("welf_local");
                welf_thema = inner_json.getString("welf_thema");
                welf_category = inner_json.getString("welf_category");
                welf_age = inner_json.getString("age");

                // 검색 결과를 보여줄 리사이클러뷰에 혜택 데이터들을 넣기 위해 파싱할 때 객체 생성 후 setter 사용
                SearchResultItem searchResultItem = new SearchResultItem();
                searchResultItem.setWelf_id(welf_id);
                searchResultItem.setWelf_name(welf_name);
                searchResultItem.setWelf_tag(welf_tag);
                searchResultItem.setWelf_count(welf_count);
                searchResultItem.setWelf_local(welf_local);
                searchResultItem.setWelf_thema(welf_thema);
                searchResultItem.setWelf_category(welf_category);
                searchResultItem.setWelf_age(welf_age);
                list.add(searchResultItem);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 페이징해서 새 데이터를 가져올 때 스크롤이 맨 위로 자동으로 올라가지는 현상이 있어서 처음 리사이클러뷰의 상태가 저장된 변수를 리사이클러뷰에 set해서
        // 리사이클러뷰가 맨 위로 올라가지지 않고 맨 마지막 스크롤 위치에 머물러 있도록 한다
        recyclerViewState = search_result_recyclerview.getLayoutManager().onSaveInstanceState();

        // 어댑터 초기화를 먼저 진행해야 다음 페이징 데이터 가져올 때 스크롤이 자동으로 맨 위로 올라가는 걸 막는 코드를 추가할 수 있다
        adapter = new RenewalSearchResultAdapter(this, list, itemClickListener);
        // 다음 페이징 데이터 가져올 때 스크롤이 자동으로 맨 위로 올라가는 현상을 없애기 위해 어댑터에 보여주는 아이템의 범위가 바꼈음을 알린다
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());

        // 검색 결과가 있다면 개수를 보여주고
        if (adapter.getItemCount() > 0)
        {
            search_result_recyclerview.setVisibility(View.VISIBLE);
            search_result_empty_textview.setVisibility(View.GONE);
            search_result_no_image.setVisibility(View.GONE);
            total_search_result.setText("혜택 총 " + adapter.getItemCount() + "개");
        }
        else if (adapter.getItemCount() == 0)
        {
            search_result_recyclerview.setVisibility(View.GONE);
            search_result_empty_textview.setVisibility(View.VISIBLE);
//            search_result_no_image.setVisibility(View.VISIBLE);
            search_result_recyclerview.setEmptyView(search_result_empty_textview);
            total_search_result.setText("혜택 총 " + total_result_count + "개");
        }

        // 검색 결과가 없다면 공백으로 둬서 다이얼로그만 보여준다
        else
        {
            total_search_result.setText("");
        }

        // 데이터를 다 받아왔으면 프로그레스바를 다시 숨긴다
        progressbar.setVisibility(View.GONE);

        // 하단 리사이클러뷰 클릭 이벤트
        // 액티비티로 보내는 처리 대신 클릭 시 혜택 정보를 제대로 가져오는지 테스트
        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String tag = list.get(pos).getWelf_tag();
            String count = list.get(pos).getWelf_count();
            String local = list.get(pos).getWelf_local();
            String thema = list.get(pos).getWelf_thema();
            String id = list.get(pos).getWelf_id();
            Log.e(TAG, "선택한 아이템의 이름 : " + name + ", 태그 : " + tag + ", 조회수 : " + count + ", 지역 : " + local + ", 테마 : " + thema + ", id : " + id);
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", id);
            intent.putExtra("welf_name", name);
            Log.e(TAG, "id : " + id + ", name : " + name);
            startActivity(intent);
        });

        search_result_recyclerview.setAdapter(adapter);
        search_result_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            //
        }
        return false;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        list.clear();
        allList.clear();
        mCategoryList.clear();
        mLocalList.clear();
        mAgeList.clear();
        mProvideTypeList.clear();
        item.clear();
    }
}