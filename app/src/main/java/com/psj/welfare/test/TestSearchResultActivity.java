package com.psj.welfare.test;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.psj.welfare.R;
import com.psj.welfare.adapter.ExpandableRecyclerViewAdapter;
import com.psj.welfare.adapter.InnerRecyclerViewAdapter;
import com.psj.welfare.adapter.RenewalSearchResultAdapter;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.SearchResultItem;
import com.psj.welfare.viewmodel.SearchViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    List<SearchResultItem> list;
    RenewalSearchResultAdapter.onItemClickListener itemClickListener;

    // 검색 화면에서 가져온 검색어
    String keyword;
    // 총 검색 결과 개수, 서버에 있는 전체 페이지 수
    String total_result_count, total_paging_count;
    // 검색 결과를 담을 변수
    String welf_id, welf_name, welf_tag, welf_count, welf_local, welf_thema;
    // 페이징 시 메서드 인자로 넘기는 받아야 할 페이지 숫자를 담을 변수
    // parseInt()로 int로 바꾼 다음 ++해서 다시 String으로 바꿔 이 변수에 담아야 한다
    String current_page;
    int integer_page = 1;

    // 다음 페이징 데이터를 가져오는 도중 보여줄 프로그레스바
    ProgressBar progressbar;

    NavigationView search_result_drawer;
    // 오른쪽에서 나오는 드로어블 레이아웃에 먹일 확장 리사이클러뷰
    RecyclerView expanderRecyclerView;

    LinearLayout search_result_filter;
    // 필터 클릭 횟수를 저장할 변수
    int count = 0;
    private Button filter_button;
    private TextView filter_textview, filter_layout_text;
    private ImageView filter_close;
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
    List<String> mCategoryList;
    List<String> mLocalList;
    List<String> mProvideTypeList;
    List<String> mAgeList;
    List<String> allList;
    List<String> item;

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
        filter_close = findViewById(R.id.filter_close);
        // 뷰 크기, 글자 크기 조절
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        search_result_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);  // 검색 결과 없을 때 보여주는 텍스트뷰
        filter_layout_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        total_search_result.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        filter_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 21);
        filter_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 21);

        filter_close.setOnClickListener(v -> search_result_drawer.setVisibility(View.GONE));

        search_result_filter.setOnClickListener(v -> search_result_drawer.setVisibility(View.VISIBLE));

        // 필터 적용 버튼
        filter_button.setOnClickListener(v -> {
            list.clear();
            allList.clear();
            mCategoryList.clear();
            mLocalList.clear();
            mAgeList.clear();
            mProvideTypeList.clear();
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
            if (arr[0] != null)
            {
                allList.add(arr[0]);
            }
            if (arr[1] != null)
            {
                allList.add(arr[1]);
            }
            if (arr[2] != null)
            {
                allList.add(arr[2]);
            }
            if (arr[3] != null)
            {
                allList.add(arr[3]);
            }

            // 선택한 필터들을 리사이클러뷰에 보여준다
            filter_adapter = new SearchResultHorizontalAdapter(this, allList, filter_clickListener);
            // 어댑터에서 처리가 끝난 리스트를 받아서 그 값들을 액티비티의 리스트에 옮겨 담는다
            item = filter_adapter.getList();
            // 클릭 리스너 추가 -> 필터를 선택할 때마다 선택한 값들이 삭제되고 남아있는 값들로 다시 서버에 쿼리해야 한다
            filter_adapter.setOnItemClickListener(pos ->
            {
                // 각 리스트를 반복하면서 리스트 안의 값이 선택한 값과 일치하는 경우에만 클릭 리스너를 호출해야 한다
                String name;
                name = item.get(pos);
                Log.e(TAG, "가로 필터 리사이클러뷰에서 선택한 필터 : " + name);
                Log.e(TAG, "어댑터에서 remove 처리 이후일 것으로 생각되는 부분 : " + item);
                // 리스트 크기가 0일 경우를 대비한 예외처리
//                if (item.size() != 0)
//                {
//                    // 선택한 필터의 이름 가져오기 성공
//                    name = item.get(pos);
//                    Log.e(TAG, "가로 필터 리사이클러뷰에서 선택한 필터 : " + name);
//                    // 필터 선택 시 리스트에서 해당 필터명을 지우고 이 리스트로 다시 서버에 요청해야 한다
////                    item.remove(name);
////                    item = filter_adapter.getList();
//                    Log.e(TAG, "어댑터에서 remove 처리 이후일 것으로 생각되는 부분 : " + item);
//                    // item 리스트를 반복하면서 안의 값을 체크한다
//                    // 안의 값이 각 리스트 안에 들어있는 값과 일치할 경우 그 리스트를 clear()하고 일치하는 값을 넣어 리스트를 새로고침한다
//                    if (item.size() > 0)
//                    {
//                        /**/
////                            allList.clear();
////                            allList = item;
////                            filter_adapter = new SearchResultHorizontalAdapter(TestSearchResultActivity.this, allList, filter_clickListener);
//                    }
//                }
            });

            selected_filter_recyclerview.setAdapter(filter_adapter);

            search_result_drawer.setVisibility(View.GONE);

            allList.clear();
            mCategoryList.clear();
            mLocalList.clear();
            mAgeList.clear();
            mProvideTypeList.clear();
        });

        // 페이징 처리 위해 어댑터 초기화, 적용하는 코드 위치 변경
        adapter = new RenewalSearchResultAdapter(this, list, itemClickListener);
        search_result_recyclerview.setAdapter(adapter);

        // 확장 / 축소 리사이클러뷰
        expanderRecyclerView = findViewById(R.id.expanderRecyclerView);
        initiateExpander();

        // 리사이클러뷰 페이징 처리
        search_result_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm.getItemCount() == 0)
                {
                    // 검색 결과가 0이면 다이얼로그로 검색 결과가 없다고 알려준 후, 확인을 눌러 이전 화면으로 돌아가도록 유도한다
                    AlertDialog.Builder builder = new AlertDialog.Builder(TestSearchResultActivity.this);
                    builder.setMessage("요청하신 검색어에 대한 결과가 없습니다\n다른 검색어를 입력해 주세요")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).setCancelable(false).show();
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
        });

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

    // 검색 결과 화면에서 재검색했을 때 호출하는 메서드
    private void performSearch(String inner_keyword, String inner_page)
    {
        // 재검색 시 새로 받은 데이터로 리스트를 채워야 하기 때문에 기존에 데이터가 들어있던 리스트를 비운다
        list.clear();
        final ProgressDialog dialog = new ProgressDialog(TestSearchResultActivity.this);
        dialog.setMessage("잠시만 기다려 주세요...");
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
//                        Log.e(TAG, "검색 결과 : " + str);
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
                        Log.e(TAG, "검색 결과 : " + str);
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

                // 검색 결과를 보여줄 리사이클러뷰에 넣기 위해 객체 생성 후 setter 사용
                SearchResultItem item = new SearchResultItem();
                item.setWelf_id(welf_id);
                item.setWelf_name(welf_name);
                item.setWelf_tag(welf_tag);
                item.setWelf_count(welf_count);
                item.setWelf_local(welf_local);
                item.setWelf_thema(welf_thema);
                list.add(item);
                adapter.notifyDataSetChanged();
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

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
            search_result_no_image.setVisibility(View.VISIBLE);
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

        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String tag = list.get(pos).getWelf_tag();
            String count = list.get(pos).getWelf_count();
            String local = list.get(pos).getWelf_local();
            String thema = list.get(pos).getWelf_thema();
//            Log.e(TAG, "선택한 아이템의 이름 : " + name + ", 태그 : " + tag + ", 조회수 : " + count + ", 지역 : " + local + ", 테마 : " + thema);
        });

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
}