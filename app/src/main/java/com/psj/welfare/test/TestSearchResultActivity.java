package com.psj.welfare.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.AndroidLogAdapter;
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

/* 매니페스트에서 android:windowSoftInputMode="adjustNothing" 속성 추가해 editText 때문에 UI가 뭉개지지 않게 함 */
public class TestSearchResultActivity extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener
{

    public final String TAG = this.getClass().getSimpleName();

    private ConstraintLayout result_tag_layout; //어떤 태그인지 알려주는 텍스트 레이아웃
    private TextView result_tag_textview; //태그 값
    private ImageView result_tag_backimage; //태그 뒤로가기 버튼

    private ImageView filter_layout_image; //필터 이미지
    SearchViewModel searchViewModel;
    EditText search_result_edittext;
    TextView total_search_result;
    RecyclerViewEmptySupport search_result_recyclerview;
    // 필터 검색 결과 없을 때 사용할 텍스트뷰
    TextView search_result_empty_textview;
    ImageView search_result_no_image;
    RenewalSearchResultAdapter adapter;
    ArrayList<SearchResultItem> list; //혜택 데이터를 담는 리스트
    RenewalSearchResultAdapter.onItemClickListener itemClickListener;

    // 검색 화면에서 가져온 검색어
    String keyword;
    // 태그 눌렀는지 검색했는지
    String type;
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

//    NavigationView search_result_drawer;
    //    DrawerLayout search_result_drawer;
    // 오른쪽에서 나오는 드로어블 레이아웃에 먹일 확장 리사이클러뷰
    private RecyclerView expanderRecyclerView;
    private ExpandableRecyclerViewAdapter expandableAdapter;


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
    // 나이대를 담을 ArrayList
    ArrayList<String> ageList = new ArrayList<>();
    // 지원 형태를 담을 ArrayList
    ArrayList<String> provideTypeList = new ArrayList<>();
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

    //기본 progressbar
    private ProgressDialog dialog;

    //필터값 담는 stringbuilder + //페이징 하기 위한 데이터
    StringBuilder thema = new StringBuilder();
    StringBuilder local = new StringBuilder();
    StringBuilder age = new StringBuilder();
    StringBuilder provideType = new StringBuilder();


    //필터 적용 했는지
    boolean isfilter = false;

    //네비게이션 드로우 사용
    private DrawerLayout drawerLayout;
    //include한 searchresult_navigation.xml 안에 뷰
    private View drawerView;
    
    //필터 데이터를 서버로 보낼 데이터를 담을 변수
    ArrayList<String> category_local = new ArrayList<>(); //지역
    ArrayList<String> category_age = new ArrayList<>(); //나이
    ArrayList<String> category_provideType = new ArrayList<>(); //지원방법

    //필터를 선택 했는지 판단하기 위한 병수
    ArrayList<Boolean> filter_local = new ArrayList<>(); //지역
    ArrayList<Boolean> filter_age = new ArrayList<>(); //나이
    ArrayList<Boolean> filter_provideType = new ArrayList<>(); //지원방법

    private ExpandableRecyclerViewAdapter expandablelistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(TestSearchResultActivity.this); //상태표시줄
        setContentView(R.layout.activity_test_search_result);

        //로거 쓰기 위한 환경 설정
        Logger.addLogAdapter(new AndroidLogAdapter());

        //초기화 작업
        init();

        //xml크기를 동적으로 변환
        setsize();

        //기본 progressbar 설정
        setprogressbar();
//        Log.e(TAG,"isfilter" + isfilter);


        // 검색어 담긴 변수 널 체크, 검색어 or 태그로 받은 키워드
        if (getIntent().hasExtra("keyword"))
        {
            Intent intent = getIntent();
            keyword = intent.getStringExtra("keyword");
            type = intent.getStringExtra("type");
            search_result_edittext.setText(keyword);
//            firstCalledSearchMethod(String.valueOf(integer_page));
            if(type.equals("tag")){ //태그로 액티비티를 들어왔다면
                result_tag_layout.setVisibility(View.VISIBLE);
                search_result_edittext.setVisibility(View.GONE);
                result_tag_textview.setText("#"+keyword);
            } else { //검색으로 액티비티를 들어왔다면
                result_tag_layout.setVisibility(View.GONE);
                search_result_edittext.setVisibility(View.VISIBLE);
            }
        }

        //필터 누르면 drawerview 열기
        search_result_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
//                search_result_drawer.setVisibility(View.VISIBLE);
            }
        });

        // 필터 적용
        filter_application();

        //필터 내용 초기화
        initiateExpander();

        // editText에서 검색되도록 처리
        search_welf();

        // 리사이클러뷰 페이징 처리
        recyclerView_paging();

//        Log.e(TAG,"type02 : " +  type);
        //검색 했을 때와 태그 눌렀을 때 값이 다름, 필터 적용 X
        if(type.equals("tag")){
            // 페이징을 tag값으로 가져오는게 아닌 search값으로 가져옴
            searchRecommendTag(keyword, "1", null, null, null, null);
        } else  if (type.equals("search")){
            // 페이징을 tag값으로 가져오는게 아닌 search값으로 가져옴
            renewalKeywordSearch(keyword,"1", null, null, null, null);
        }
        
        
        
        //뒤로가기 버튼
        result_tag_backimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //뒤로가기 버튼
            }
        });
    }


    //기본 progressbar 설정
    private void setprogressbar(){
        dialog = new ProgressDialog(TestSearchResultActivity.this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    // editText에서 검색되도록 처리
    private void search_welf(){
        search_result_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) //IME_ACTION_SEARCH -> 키보드가 올라왔다는 정보
                {
                    // 검색 후 네비게이션뷰 필터 초기화
                    initiateExpander();
//                    expandableAdapter =
//                            new ExpandableRecyclerViewAdapter(TestSearchResultActivity.this, parentList, categoryList, ageList, localList, provideTypeList, childListHolder, filter_local, filter_age, filter_provideType);
//                    expanderRecyclerView.setLayoutManager(new LinearLayoutManager(TestSearchResultActivity.this));
//                    expanderRecyclerView.setAdapter(expandableAdapter);


                    //검색 후 네비게이션뷰 안보이도록
                    drawerLayout.closeDrawer(drawerView);
//                    search_result_drawer.setVisibility(View.GONE);

                    //필터 적용했는지 여부 초기화(false)
                    isfilter = false;

                    //검색 타입 "search"
                    type = "search";

                    //페이지 1부터 다시 시작
                    integer_page = 1;

                    //검색하면 다시 리스트 초기화
                    list.clear();
                    String keyword = search_result_edittext.getText().toString().trim();

                    //검색어로 검색 했을 때
                    renewalKeywordSearch(keyword,"1", null, null, null, null);
                    // 검색어와 "1"을 인자로 넘긴다
//                    performSearch(search_result_edittext.getText().toString().trim(), String.valueOf(integer_page));

                    //데이터 리사이클러뷰를 혜택 개수 레이아웃에 붙인다
                    recyclerview_chainwelfCount();

                    //필터 리사이클러뷰 초기화
                    allList.clear();

                    //검색 후 키보드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search_result_edittext.getWindowToken(), 0);


                    //태그 레이아웃 숨기기
                    result_tag_layout.setVisibility(View.GONE);
                    // 검색바 보여주기
                    search_result_edittext.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }



    // 리사이클러뷰 페이징 처리
    private void recyclerView_paging(){
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
//                                Log.e(TAG,"integer_page : " + integer_page);

                                integer_page++;
                                progressbar.setVisibility(View.VISIBLE);
                                current_page = String.valueOf(integer_page);

                                // 태그 눌렀는지 검색 했는지
                                if(type.equals("tag")){
                                    if(isfilter){ //필터 적용 했다면
                                        searchRecommendTag(keyword, current_page, null, local.toString(), age.toString(), provideType.toString());
                                    } else {
                                        searchRecommendTag(keyword, current_page, null, null, null, null);
                                    }

                                } else if (type.equals("search")){
                                    if(isfilter){ //필터 적용 했다면
                                        renewalKeywordSearch(keyword, current_page, null, local.toString(), age.toString(), provideType.toString());
                                    } else {
                                        renewalKeywordSearch(keyword, current_page, null, null, null, null);
                                    }
                                }

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
    }


    // 필터 적용
    @SuppressLint("CheckResult")
    private void filter_application(){
        // 필터 적용 버튼
        filter_button.setOnClickListener(v ->
        {
            //페이지 1부터 다시 시작
            integer_page = 1;

            //검색 타입 "filter"
            isfilter = true;

            // 선택한 필터들이 보이는 리사이클러뷰을 VISIBLE로 바꾸고
            // 하단 리사이클러뷰의 머리를 이 리사이클러뷰의 밑으로 연결짓는다
            selected_filter_recyclerview.setVisibility(View.VISIBLE);

            // 필터 적용 버튼을 누르면 선택한 필터에 해당하는 조건들로 검색한다
            // 먼저 버튼을 누르면 선택한 값들을 가져와야 한다. 선택한 필터는 '혜택 총 n개' 밑에 예전 관심사 리스트처럼 보여줘야 한다
            String gainedValues = InnerRecyclerViewAdapter.getAllValues();
            String[] arr = gainedValues.split("zz");

            //필터 적용 할 때마다 필터값 초기화
            local.setLength(0);
            provideType.setLength(0);
            age.setLength(0);
            //InnerRecyclerViewAdapter.getAllValues() 값이 null을 "null"로 받아옴

            if(!arr[0].equals("null")){
                local.append(arr[0]);
            }

            if(!arr[1].equals("null")){
                age.append(arr[1]);
            }

            if(!arr[2].equals("null")){
                provideType.append(arr[2]);
            }

            //필터를 하나도 선택하지 않았다면
            if( arr[0].equals("null") && arr[1].equals("null") && arr[2].equals("null")){
                Toast.makeText(TestSearchResultActivity.this,"필터를 선택해 주세요",Toast.LENGTH_SHORT).show();
            } else {
                //필터 적용 누르면 새로 필터를 적용 시킨다(이전에 필터를 적용 했을 때 다시 적용하기 위함)
                allList.clear();

                //혜택 데이터 초기화
                list.clear();
                // 필터에서 선택한 값들을 토대로 서버에 다시 쿼리


                if(type.equals("tag")){
                    searchRecommendTag(keyword, "1", null, local.toString(), age.toString(), provideType.toString());
                } else if(type.equals("search")){
                    renewalKeywordSearch(keyword, "1", null, local.toString(), age.toString(), provideType.toString());
                }

                //데이터 리사이클러뷰를 필터리사이클러뷰에 붙인다
                recyclerview_chainfilter();

                //필터 적용 후 네비게이션뷰 안보이도록
                drawerLayout.closeDrawer(drawerView);
            }


            //새로운 필터값을 적용하기 위해 데이터 초기화
            //필터값을 초기화 하지 않으면 서버에 연결할 때 데이터를 이전의 값하고 같이 줌
            category_local.clear();
            category_provideType.clear();
            category_age.clear();

            //InnerRecyclerViewAdapter 에서 받은 배열 데이터 + '-' 구분자 split 안한 데이터를 split해서 하나하나씩 allList에 담음
            for (int i = 0; i < arr.length; i++){
                if(!arr[i].equals("null")){
                    //선택한 필터의 데이터를 각 카테고리의 맞게 담는다
                    String[] filter_category = arr[i].split("-");
                    if (i == 0) {
                        for (int j = 0; j < filter_category.length; j++) {
                            category_local.add(filter_category[j]);
                        }
                    } else if (i == 1) {
                        for (int j = 0; j < filter_category.length; j++) {
                            category_age.add(filter_category[j]);
                        }
                    } else if (i == 2) {
                        for (int j = 0; j < filter_category.length; j++) {
                            category_provideType.add(filter_category[j]);
                        }
                    }

                    String[] filter = arr[i].split("-");
                    for (int j = 0; j < filter.length; j++){
                        allList.add(filter[j]);
                    }
                }
            }


            //필터 선택했는지 확인하는 메소드
            select_filter();
            expandableAdapter.notifyDataSetChanged();

            // 선택한 필터들을 리사이클러뷰에 보여준다
            filter_adapter = new SearchResultHorizontalAdapter(this, allList, filter_clickListener);
            // 클릭 리스너 추가 -> 필터를 선택할 때마다 선택한 값들이 삭제되고 남아있는 값들로 다시 서버에 쿼리해야 한다
            // TODO : 필터 클릭 시 삭제 후 재쿼리하는 처리 진행 중
            filter_adapter.setOnItemClickListener(pos ->
            {
                //InnerRecyclerViewAdapter 안에 있는 선택한 필터를 담은 데이터 초기화
                InnerRecyclerViewAdapter.resetfilter();

                //페이지 1부터 다시 시작
                integer_page = 1;

                for (int i = 0; i < category_local.size(); i++){
                    if(category_local.get(i).equals(allList.get(pos))){
                        category_local.remove(i);
                    }
//                    Log.e(TAG,"category_local : " + category_local.get(i));
                }
                for (int i = 0; i < category_age.size(); i++){
                    if(category_age.get(i).equals(allList.get(pos))){
                        category_age.remove(i);
                    }
                }
                for (int i = 0; i < category_provideType.size(); i++){
                    if(category_provideType.get(i).equals(allList.get(pos))){
                        category_provideType.remove(i);
                    }
                }


                //필터 선택했는지 확인하는 메소드
                select_filter();
                expandableAdapter.notifyDataSetChanged();

                //초기화 하는 방법중 setLength(0)제일 빠르다고 함
                local.setLength(0);
                provideType.setLength(0);
                age.setLength(0);

                for (int i = 0; i < category_local.size(); i++){
                    if((category_local.size() -1) != i){
                        local.append(category_local.get(i) + "-");
                    } else {
                        local.append(category_local.get(i));
                    }
                }

                for (int i = 0; i < category_age.size(); i++){
                    if((category_age.size() -1) != i){
                        age.append(category_age.get(i) + "-");
                    } else {
                        age.append(category_age.get(i));
                    }
                }

                for (int i = 0; i < category_provideType.size(); i++){
                    if((category_provideType.size() -1) != i){
                        provideType.append(category_provideType.get(i) + "-");
                    } else {
                        provideType.append(category_provideType.get(i));
                    }
                }

                //선택한 필터값 삭제
                allList.remove(pos);
                filter_adapter.notifyDataSetChanged();

                //혜택 데이터 초기화
                list.clear();


                // 필터에서 선택한 값들을 토대로 서버에 다시 쿼리
                if(type.equals("tag")){
                    searchRecommendTag(keyword, "1", null, local.toString(), age.toString(), provideType.toString());
                } else if(type.equals("search")){
                    renewalKeywordSearch(keyword, "1", null, local.toString(), age.toString(), provideType.toString());
                }

                //필터 다 지우면 필터쪽 리사이클러뷰 안보이도록
                if(allList.size() == 0){
                    //데이터 리사이클러뷰를 혜택 개수 레이아웃에 붙인다
                    recyclerview_chainwelfCount();
                }

            });

            selected_filter_recyclerview.setAdapter(filter_adapter);
        });
    }


    //데이터 리사이클러뷰를 필터리사이클러뷰에 붙인다
    private void recyclerview_chainfilter(){
        /* 체인을 바꿀 리사이클러뷰 : search_result_recyclerview(topToBottom을 수정)
         * 원래 topToBottom에 묶여있던 뷰 : result_filter_layout
         * 필터 적용 시 체인을 연결할 뷰 : selected_filter_recyclerview(필터 적용 시 visible로 속성 변경)
         * 필터 적용 전 : search_result_recyclerview의 layout_constraintHeight_percent를 9로 변경 + layout_constraintVertical_bias를 0으로 변경
         * 필터 적용 후 : search_result_recyclerview의 layout_constraintHeight_percent를 8로 변경 + layout_constraintVertical_bias를 1로 변경 */

        //필터쪽 리사이클러뷰 보이기
        selected_filter_recyclerview.setVisibility(View.VISIBLE);

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
    }

    //데이터 리사이클러뷰를 혜택 개수 레이아웃에 붙인다
    private void recyclerview_chainwelfCount(){
        //필터쪽 리사이클러뷰 숨기기
        selected_filter_recyclerview.setVisibility(View.GONE);

        @SuppressLint("CutPasteId")
        ConstraintLayout result_constraint_layout = findViewById(R.id.search_result_constraint_layout);
        ConstraintSet result_constraint_Set = new ConstraintSet();
        result_constraint_Set.clone(result_constraint_layout);
        // 필터 적용 버튼을 누르면 search_result_recyclerview의 top을 -> selected_filter_recyclerview의 bottom에 연결한다
        result_constraint_Set.connect(R.id.search_result_recyclerview,    // startId : 어떤 뷰의 체인을 바꿀 것인가?            // 이거의
                ConstraintSet.TOP,                                // startSide : 그 뷰의 어디를 연결할 것인가?                  // 윗 부분을
                R.id.result_filter_layout,                        // endId : 어디에 체인을 걸 것인가?                           // 이거의
                ConstraintSet.BOTTOM,                             // endSide : 그 뷰의 어디에 1번 인자로 받은 뷰를 연결할 것인가? //밑에 붙인다
                0);                                        // margin : 제한할 여백(양수여야 함)
        result_constraint_Set.constrainPercentHeight(R.id.search_result_recyclerview, (float) 0.89);
        result_constraint_Set.setVerticalBias(R.id.search_result_recyclerview, (float) 0.1);
        result_constraint_Set.applyTo(result_constraint_layout);
    }

    //필터를 선택했는지 여부 확인하는 메소드
    private void select_filter(){
        //필터 선택한 값을 초기화 한다
        filter_local.clear();
        //필터를 선택했는지 판단하는 로직
        for (int i = 0; i < localList.size(); i++){
            filter_local.add(false);
            for (int j = 0; j < category_local.size(); j++){
                //필터를 선택했다면 해당 값을 true로 바꿈
                if(localList.get(i).equals(category_local.get(j))){
                    filter_local.set(i,true);
                }
            }
        }

        filter_age.clear();
        for (int i = 0; i < ageList.size(); i++){
            filter_age.add(false);
            for (int j = 0; j < category_age.size(); j++){
                //필터를 선택했다면 해당 값을 true로 바꿈
                if(ageList.get(i).equals(category_age.get(j))){
                    filter_age.set(i,true);
                }
            }
        }

        filter_provideType.clear();
        for (int i = 0; i < provideTypeList.size(); i++){
            filter_provideType.add(false);
            for (int j = 0; j < category_provideType.size(); j++){
                //필터를 선택했다면 해당 값을 true로 바꿈
                if(provideTypeList.get(i).equals(category_provideType.get(j))){
                    filter_provideType.set(i,true);
                }
            }
        }
    }

    /* 필터 내용 초기화 */
    private void initiateExpander()
    {
        //static 사용해서 다른 클래스에서도 사용
        parentList.add("지역");
        parentList.add("나이대");
        parentList.add("지원 형태");

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
//        childList.addAll(localList);

        childListHolder.add(localList);

        // 나이대
        ageList.add("10대");
        ageList.add("20대");
        ageList.add("30대");
        ageList.add("40대");
        ageList.add("50대");
        ageList.add("60대 이상");
//        childList.addAll(ageList);

        childListHolder.add(ageList);

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
//        childList.addAll(provideTypeList);

        childListHolder.add(provideTypeList);



        for (int i = 0; i < localList.size(); i++){
            filter_local.add(false);
        }

        for (int i = 0; i < ageList.size(); i++){
            filter_age.add(false);
        }

        filter_provideType.clear();
        for (int i = 0; i < provideTypeList.size(); i++){
            filter_provideType.add(false);
        }

        
        // 값이 담긴 리스트들을 어댑터 생성자에 넣어 초기화
        // 어댑터 안에서 값이 들어오는지 확인
       expandableAdapter =
                new ExpandableRecyclerViewAdapter(TestSearchResultActivity.this, parentList, categoryList, ageList, localList, provideTypeList, childListHolder, filter_local, filter_age, filter_provideType);
        expanderRecyclerView.setLayoutManager(new LinearLayoutManager(TestSearchResultActivity.this));
        expanderRecyclerView.setAdapter(expandableAdapter);
    }


    // 추천 태그 검색 메서드
    private void searchRecommendTag(String keyword, String page, String category, String local, String age, String provideType)
    {

        //1페이지 받을 때만 기본 프로그래스바 보여줌
        if(integer_page == 1){
            dialog.show();
        }

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        final Observer<String> recommendSearchObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    responseParsing(str);

                    //1페이지 받을 때만 기본 프로그래스바 보여줌
                    if(integer_page == 1){
                        dialog.dismiss();
                    }
//                    Log.e(TAG,"검색+태그 : test0000001" + str);
                }
                else
                {
                    Log.e(TAG, "검색어가 null입니다");
                }
            }
        };

        searchViewModel.searchRecommendTag(keyword, page, category, local, age, provideType)
                .observe(this, recommendSearchObserver);
    }


    // 검색 결과 화면에서 검색어 입력 후 검색 시 호출하는 메서드
    public void renewalKeywordSearch(String keyword, String page, String category, String local, String age, String provideType)
    {

        if (keyword != null && !keyword.equals(""))
        {
            //1페이지 받을 때만 기본 프로그래스바 보여줌
            if(integer_page == 1){
                dialog.show();
            }

//            Log.e(TAG, "검색할 키워드 : " + keyword);
            searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
            final Observer<String> searchObserver = new Observer<String>()
            {
                @Override
                public void onChanged(String str)
                {
                    if (str != null)
                    {
                        Logger.json(str);
                        responseParsing(str);

                        //1페이지 받을 때만 기본 프로그래스바 보여줌
                        if(integer_page == 1){
                            dialog.dismiss();
                        }
//                        Log.e(TAG,"필터 적용 + 페이징 : test0000004 : " + str);
                    }
                    else
                    {
                        Log.e(TAG, "str이 null입니다");
                    }
                }
            };

            // 키워드 검색만 했을 경우에 사용한다
            // 넣어야 할 인자 : keyword, page, category, local, age, provideType
//            Log.e(TAG,"keyword : " + keyword);
            searchViewModel.renewalSearchKeyword(keyword, page, category, local, age, provideType)
                    .observe(this, searchObserver);
        }
    }

    // 검색 결과 파싱 메서드
    @SuppressLint("CheckResult")
    private void responseParsing(String result)
    {
//        Log.e(TAG,"result" + result);
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
            total_search_result.setText("혜택 총 " + total_result_count + "개");
        }
        else if (adapter.getItemCount() == 0)
        {
            search_result_recyclerview.setVisibility(View.GONE);
            search_result_empty_textview.setVisibility(View.VISIBLE);
//            search_result_no_image.setVisibility(View.VISIBLE);
            search_result_recyclerview.setEmptyView(search_result_empty_textview);
            total_search_result.setText("혜택 총 " + total_result_count + "개");
        }


        if(integer_page != 1){
            // 데이터를 다 받아왔으면 프로그레스바를 다시 숨긴다
            progressbar.setVisibility(View.GONE);
        }


        // 선택한 아이템(혜택) 클릭,하단 리사이클러뷰 클릭 이벤트
        // 액티비티로 보내는 처리 대신 클릭 시 혜택 정보를 제대로 가져오는지 테스트
        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String tag = list.get(pos).getWelf_tag();
            String count = list.get(pos).getWelf_count();
            String local = list.get(pos).getWelf_local();
            String thema = list.get(pos).getWelf_thema();
            String id = list.get(pos).getWelf_id();
//            Log.e(TAG, "선택한 아이템의 이름 : " + name + ", 태그 : " + tag + ", 조회수 : " + count + ", 지역 : " + local + ", 테마 : " + thema + ", id : " + id);
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", id);
            intent.putExtra("welf_name", name);
//            Log.e(TAG, "id : " + id + ", name : " + name);
            startActivity(intent);
        });

        search_result_recyclerview.setAdapter(adapter);
        search_result_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }


    //초기화 작업
    private void init(){
        //네비게이션 드로우 사용
        drawerLayout = (DrawerLayout) findViewById(R.id.search_drawerlayout);
        //include한 searchresult_navigation.xml 안에 뷰와 연결
        drawerView = (View) findViewById(R.id.search_result_drawer);
        result_tag_textview = findViewById(R.id.result_tag_textview);
        result_tag_backimage = findViewById(R.id.result_tag_backimage);

        result_tag_layout = findViewById(R.id.result_tag_layout);
        filter_layout_image = findViewById(R.id.filter_layout_image);
        progressbar = findViewById(R.id.progressbar);
        total_search_result = findViewById(R.id.total_search_result);
        search_result_edittext = findViewById(R.id.search_result_edittext);
        search_result_recyclerview = findViewById(R.id.search_result_recyclerview);
        search_result_empty_textview = findViewById(R.id.search_result_empty_textview);
        search_result_no_image = findViewById(R.id.search_result_no_image);
        search_result_empty_textview.setVisibility(View.GONE);
        search_result_filter = findViewById(R.id.search_result_filter);
        selected_filter_recyclerview = findViewById(R.id.selected_filter_recyclerview);
        // 확장 / 축소 리사이클러뷰
        expanderRecyclerView = findViewById(R.id.expanderRecyclerView);

        filter_button = findViewById(R.id.filter_button);
        filter_textview = findViewById(R.id.filter_textview);
        filter_layout_text = findViewById(R.id.filter_layout_text);


        //리스트 초기화
        allList = new ArrayList<>();
        list = new ArrayList<>();
        mCategoryList = new ArrayList<>();
        mLocalList = new ArrayList<>();
        mProvideTypeList = new ArrayList<>();
        mAgeList = new ArrayList<>();
        item = new ArrayList<>();

        //리사이클러뷰 셋팅
        search_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        // 선택한 필터들을 보여주는 리사이클러뷰는 가로 모양이다
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        selected_filter_recyclerview.setLayoutManager(llm);
    }


    //xml크기를 동적으로 변환
    private void setsize() {
        // 뷰 크기, 글자 크기 조절
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        search_result_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) size.x / 24);  // 검색 결과 없을 때 보여주는 텍스트뷰
        filter_layout_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) size.x / 26);
        total_search_result.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) size.x / 23);
        filter_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) size.x / 21);
        filter_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) size.x / 17);

        search_result_recyclerview.setPadding((int)(size.x*0.05),(int)(size.x*0.02),(int)(size.x*0.05),(int)(size.x*0.02));

        //필터
        search_result_filter.getLayoutParams().height = (int) (size.x * 0.09);
        search_result_filter.setPadding((int)(size.x * 0.017),(int)(size.x * 0.017),(int)(size.x * 0.017),(int)(size.x * 0.017));
    }

    //상태표시줄 색상변경
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }


    @Override
    public void onBackPressed() {

        //드로우 레이아웃 열려 있으면 닫기 먼저 하기
        if(drawerLayout.isDrawerOpen(drawerView)){
            drawerLayout.closeDrawer(drawerView);
        } else {
            super.onBackPressed();
        }
    }
}