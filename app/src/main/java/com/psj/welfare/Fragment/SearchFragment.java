package com.psj.welfare.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.adapter.CategorySearchResultAdapter;
import com.psj.welfare.adapter.SelectedCategoryAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.CategorySearchBottomResultItem;
import com.psj.welfare.data.CategorySearchResultItem;
import com.psj.welfare.data.SearchItem;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/* 혜택 이름 검색하는 프래그먼트
 * 하단의 카테고리들을 선택하고 버튼을 눌러 이동한 경우, 혜택 하위 카테고리 검색을 적용해 결과를 보여준다
 * 카테고리 다중선택이 없어져서 이제 해시태그 키워드를 누르면, 그 카테고리에 관련된 혜택들만 보여준다 */
public class SearchFragment extends Fragment
{
    // 로그 찍을 때 사용하는 TAG
    public final String TAG = "SearchFragment";

    private EditText search_edittext;

    Toolbar search_toolbar;

    // 유저에게 제공할 혜택들을 담을 ArrayList
    ArrayList<String> m_favorList;

    // 유저 로그 전송 시 세션값, 토큰값 가져오는 데 사용할 쉐어드
    SharedPreferences sharedPreferences;
    // 서버로 한글 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 해서, 인코딩한 문자열을 저장할 변수
    String encode_str;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    public SearchFragment()
    {
    }

    //    Spinner s1;
    MaterialSpinner s1;

    //Create Spinner
    private Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    //SearchResultActivity에서 복붙한 변수들

    // 가로로 하위 카테고리들을 보여줄 리사이클러뷰, 세로로 검색 결과들을 보여줄 리사이클러뷰뷰
    private RecyclerView keyword_category_recycler, search_result_title_recycler;

    // 가로 리사이클러뷰에 붙일 어댑터
    private SelectedCategoryAdapter category_adapter;
    private SelectedCategoryAdapter.ItemClickListener category_clickListener;

    // 세로 리사이클러뷰에 붙일 어댑터
    private CategorySearchResultAdapter adapter;
    private CategorySearchResultAdapter.ItemClickListener itemClickListener;

    // parent_category를 모아둘 list
    List<SearchItem> parent_list;
    List<CategorySearchResultItem> top_list;
    // 세로 리사이클러뷰에 넣을 혜택 이름(welf_name)을 넣을 리스트
    List<CategorySearchBottomResultItem> name_list;

    // 서버에서 받은 JSONObject 안의 값들을 담을 변수, 검색 결과값이 없을 경우 판단 시 사용
    String status;

    // 서버에서 받은 JSONArray 안의 값들을 담을 변수
    String welf_name, welf_local, parent_category, welf_category, tag;

    // 2차적으로 파싱한 JSONArray 안의 값들을 담을 변수
    String second_welf_name, second_welf_local, second_parent_category, second_welf_category, second_tag;

    // 쿼리 결과 개수를 담을 변수
    String total_count, second_total_count;

    // 쿼리 결과 개수를 보여줄 텍스트뷰
    TextView search_result_benefit_title;

    // 중복 제거에 사용할 리스트
    List<SearchItem> other_list;

    // SearchFragment에서 editText에 입력한 검색 내용. 인텐트로 담아온다
    String keyword;
    //    String city;
    String region_name = "전국";


    // 구글 애널리틱스

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        init(view);

//        recommend_search_textview = view.findViewById(R.id.recomm);
//        recent_search_history_textview = view.findViewById(R.id.recent_search_history_textview);
//        map_btn = view.findViewById(R.id.map_btn);
        search_edittext = view.findViewById(R.id.search_edittext);

//        search_toolbar = view.findViewById(R.id.search_toolbar);



        //두번째 테스트
        final String [] city = {"전국", "서울", "경기", "인천", "강원", "세종", "충북", "충남", "대전",
                "대구", "경북", "경남", "전북", "전남", "광주", "부산", "울산", "제주" };


        //세번째 스피너 라이브러리 테스트

        com.psj.welfare.fragment.MaterialSpinner s1 = (com.psj.welfare.fragment.MaterialSpinner) view.findViewById(R.id.spinner);



        ArrayAdapter adapter = new ArrayAdapter(
                getContext(), // 현재화면의 제어권자
//                android.R.layout.simple_spinner_item, // 레이아웃
                R.layout.spin,
                city); // 데이터

        s1.setAdapter(adapter);

        s1.setOnItemSelectedListener(new com.psj.welfare.fragment.MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(com.psj.welfare.fragment.MaterialSpinner view, int position, long id, Object item) {
                Log.e(TAG, "선택된거 포지션:"+position);
                Log.e(TAG, "선택된 지역이름:"+item);
                Log.e(TAG, "선택된 id:"+id);
                region_name = item.toString();
            }
        });


        parent_list = new ArrayList<>();
        other_list = new ArrayList<>();
        name_list = new ArrayList<>();
        top_list = new ArrayList<>();

        search_result_benefit_title = view.findViewById(R.id.search_result_benefit_title);
        keyword_category_recycler = view.findViewById(R.id.keyword_category_recycler);
        search_result_title_recycler = view.findViewById(R.id.search_result_title_recycler);

        // 가로 리사이클러뷰 처리
        keyword_category_recycler.setHasFixedSize(true);
        keyword_category_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // 세로 리사이클러뷰 처리
        search_result_title_recycler.setHasFixedSize(true);
        search_result_title_recycler.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }


    /* 항상 onViewCreated()에서 findViewById()를 쓰고(뷰들이 완전히 생성됐기 때문) 뷰를 매개변수로 전달한다 */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated() 호출");

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }
        m_favorList = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(search_toolbar);
        }
//        search_toolbar.setTitle("검색");

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Log.e(TAG, "검색 키워드 : " + search_edittext.getText());
                    Log.e(TAG, "선택한 지역 : " + region_name);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " + search_edittext.getText());
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 검색 버튼 클릭 되었을 때 처리하는 기능
                    performSearch(search_edittext.getText().toString(), region_name);
                    return true;
                }

                return false;
            }

        });

        // 내 주변 혜택 찾기 버튼
        // 이거 누를 때에도 위치 권한을 허용했는지 확인해야 한다
//        map_btn.setOnClickListener(new OnSingleClickListener()
//        {
//            @Override
//            public void onSingleClick(View v)
//            {
//                // TODO : 위치정보 권한 설정
//                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//                {
//                    // 위치 권한 거부됐을 시
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setTitle("권한 허용")
//                            .setMessage("지역별 혜택을 확인하시려면 권한 허용이 필요합니다")
//                            .setPositiveButton("허용하러 가기", new DialogInterface.OnClickListener()
//                            {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
//                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//                                    dialog.dismiss();
//                                }
//                            }).setNegativeButton("종료", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            dialog.dismiss();
//                            Toast.makeText(getActivity(), "권한 설정 하지 않음", Toast.LENGTH_SHORT).show();
//                        }
//                    }).show();
//                }
//                else
//                {
//                    // 유저의 위치정보를 찾는 클래스의 객체 생성(Fragment기 때문에 인자로 getActivity()를 넣어야 함!!)
//                    gpsTracker = new GpsTracker(getActivity());
//
//                    // 유저의 위치에서 위도, 경도값을 가져와 변수에 저장
//                    double latitude = gpsTracker.getLatitude();
//                    double longitude = gpsTracker.getLongitude();
//
//                    // 위도, 경도값으로 주소를 만들어 String 변수에 저장
//                    String address = s1.getSelectedItem().toString();
//
//                    // String 변수 안의 값을 " "을 기준으로 split해서 'OO구' 글자를 빼낸다
//                    split_list = new ArrayList<>();
//                    String[] result = address.split(" ");
//                    split_list.addAll(Arrays.asList(result));
//
//                    // OO시, OO구에 대한 정보를 각각 변수에 집어넣는다
//                    city = split_list.get(1);
//                    district = split_list.get(2);
//
//                    // 들어있는 값에 따라 지역명을 바꿔서 변수에 저장한다
//                    if (address.contains("서울특별시"))
//                    {
//                        user_area = "서울";
//                    }
//                    if (address.contains("인천광역시"))
//                    {
//                        user_area = "인천";
//                    }
//                    if (address.contains("강원도"))
//                    {
//                        user_area = "강원";
//                    }
//                    if (address.contains("경기"))
//                    {
//                        user_area = "경기";
//                    }
//                    if (address.contains("충청북도"))
//                    {
//                        user_area = "충북";
//                    }
//                    if (address.contains("충청남도"))
//                    {
//                        user_area = "충남";
//                    }
//                    if (address.contains("세종시"))
//                    {
//                        user_area = "세종";
//                    }
//                    if (address.contains("대전시"))
//                    {
//                        user_area = "대전";
//                    }
//                    if (address.contains("경상북도"))
//                    {
//                        user_area = "경북";
//                    }
//                    if (address.contains("울산"))
//                    {
//                        user_area = "울산";
//                    }
//                    if (address.contains("대구"))
//                    {
//                        user_area = "대구";
//                    }
//                    if (address.contains("부산광역시"))
//                    {
//                        user_area = "부산";
//                    }
//                    if (address.contains("경상남도"))
//                    {
//                        user_area = "경남";
//                    }
//                    if (address.contains("전라북도"))
//                    {
//                        user_area = "전북";
//                    }
//                    if (address.contains("광주광역시"))
//                    {
//                        user_area = "광주";
//                    }
//                    if (address.contains("전라남도"))
//                    {
//                        user_area = "전남";
//                    }
//                    if (address.contains("제주도"))
//                    {
//                        user_area = "제주";
//                    }
//
//                    // 변환이 끝난 지역명은 인텐트에 담아서 지도 화면으로 보낸다
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지도 화면으로 이동");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                    Intent intent = new Intent(getActivity(), MapActivity.class);
//                    intent.putExtra("user_area", user_area);
//                    intent.putExtra("city", city);
//                    intent.putExtra("district", district);
//                    startActivity(intent);
//                }
//            }
//        });





    }

    // 모바일 키보드에서 검색 버튼 눌렀을 때
    public void performSearch(String search, String city)
    {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_edittext.getWindowToken(), 0);
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        Log.e(TAG, "performSearch() 안으로 들어온 선택 지역 : " + city);

        name_list.clear();
        top_list.clear();
//        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
//        intent.putExtra("search", search);
//        intent.putExtra("city", city);
//        startActivity(intent);
        searchWelfare(search,city);
    }

    @Override
    public void onStart()
    {
        super.onStart();


    }

    private void init(View view)
    {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    void searchWelfare(String keyword, String city)
    {
        sharedPreferences = getContext().getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchWelfare(token, session, "search", keyword, city, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String search_result = response.body();
                    Log.e(TAG, "search_result = " + search_result);
                    jsonParsing(search_result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    /* 서버에서 받은 키워드 검색 결과값들을 파싱하는 메서드 */
    private void jsonParsing(String search_result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                /* 이 중 welf_category를 가로 리사이클러뷰에 넣어야 한다 */
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                welf_local = inner_obj.getString("welf_local");
                parent_category = inner_obj.getString("parent_category");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");

                SearchItem item = new SearchItem();
                item.setWelf_name(welf_name);
                item.setWelf_local(welf_local);
                item.setParent_category(parent_category);
                item.setWelf_category(welf_category);
                item.setTag(tag);

                CategorySearchResultItem top_item = new CategorySearchResultItem();

                // 상단의 가로 리사이클러뷰(하위 카테고리 보이는곳)에 넣을 리스트에 넣을 객체
                top_item.setWelf_category(welf_category);
                Log.e("fff", "top_item = " + top_item.getWelf_category());
                boolean hasDuplicate = false;
                for (int j = 0; j < top_list.size(); j++)
                {
                    // 1번이라도 중복되는 게 있으면 break로 for문 탈출
                    if (top_list.get(j).getWelf_category().equals(top_item.getWelf_category()))
                    {
                        Log.e("fff", top_list.get(j).getWelf_category());
                        hasDuplicate = true;
                        break;
                    }
                }
                // 여기선 같은 게 있어서 나온건지 하나도 없어서 나온건지 모른다
                // 그래서 boolean 변수를 통해 같은 게 있었으면 true, 없었으면 false로 설정하고 false일 때 리스트에 아이템을 추가한다
                if (!hasDuplicate)
                {
                    /* top_list 안에 ;;이 들어간 아이템을 제외하고 값을 넣는다 */
                    if (!top_item.getWelf_category().contains(";; "))
                    {
                        top_list.add(top_item);
                    }
                }

                // 하단의 세로 리사이클러뷰에 넣을 리스트
                CategorySearchBottomResultItem name_item = new CategorySearchBottomResultItem();
                name_item.setWelf_name(welf_name);
                name_item.setWelf_category(welf_category);
                name_item.setWelf_local(welf_local);
//                name_list.clear();
                name_list.add(name_item);
            }
            total_count = jsonObject.getString("TotalCount");
            Log.e(TAG, "TotalCount = " + total_count);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status.equals("500") || status.equals("404"))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("요청하신 검색어와 일치하는 검색 결과가 없습니다")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
//                            finish();
                        }
                    }).show();
        }

        // 가로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
//        top_list.clear();
        top_list.add(0, new CategorySearchResultItem("전체"));
        category_adapter = new SelectedCategoryAdapter(getContext(), top_list, category_clickListener);
        category_adapter.setOnItemClickListener((view, pos) ->
        {
            String name = top_list.get(pos).getWelf_category();
            Log.e(TAG, "선택한 하위 카테고리명 = " + name);
            // 선택한 하위 카테고리에 속하는 정책들을 하단 리사이클러뷰에 표시한다
            // 이 메서드의 JSON 파싱 메서드가 호출되면, 이 파싱 메서드 안에서 해당 하위 카테고리를 상징하는 이미지를 아이템에 set한다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 'OO 지원' 클릭. 선택한 하위 카테고리 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            searchSubCategoryWelfare(name, search_edittext.getText().toString(),region_name); //현금지원, 주거, 충북
        });
        keyword_category_recycler.setAdapter(category_adapter);

        /* 쿼리 결과 개수로 몇 개가 검색됐는지 유저에게 알려준다 */
        if (total_count == null)
        {
            search_result_benefit_title.setText("검색 결과가 없습니다");
        }
        else
        {
            if (Integer.parseInt(total_count) != 0)
            {
                if (Integer.parseInt(total_count) < 10)
                {
                    // 숫자가 1자리수인 경우
                    SpannableString spannableString = new SpannableString("검색결과 " + total_count + "개입니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 9)
                {
                    // 숫자가 2자리수인 경우
                    SpannableString spannableString = new SpannableString("검색결과 " + total_count + "개입니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 99)
                {
                    // 숫자가 3자리수인 경우
                    SpannableString spannableString = new SpannableString("검색결과 " + total_count + "개입니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
            }
        }

        // 세로 리사이클러뷰(필터링 결과 출력)에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new CategorySearchResultAdapter(getContext(), name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            // 선택한 혜택의 이름, 실시지역을 따서 인텐트로 넘겨 상세정보를 볼 수 있게 한다
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 상세보기 화면으로 이동. 선택한 혜택 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

    /* 가로 리사이클러뷰 클릭 리스너에서도 이 메서드를 호출해야 세로 리사이클러뷰에 쓰이는 리스트 안의 값들을 바꿀 수 있다 */
    void searchSubCategoryWelfare(String sub_category, String keyword, String city)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        // 현물 지원, 현금 지원 등의 키워드를 받아야 하니까 상단 가로 리사이클러뷰 클릭 리스너에서 클릭 이벤트가 일어날 때마다, 아이템 안의 문자를 담아와야 한다
        Call<String> call = apiInterface.searchSubCategoryWelfare("child_category_search", sub_category, keyword, city);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "선택한 서브카테고리(현금지원,..):"+sub_category);
                    Log.e(TAG, "상단 리사이클러뷰 아이템 선택에 따른 연관 혜택 출력 = " + result);
                    secondJsonParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    /* 상단 리사이클러뷰에서 하위 카테고리를 선택하면, 그 카테고리에 매핑된 혜택들로 하단 리사이클러뷰 내용을 수정한다 */
    private void secondJsonParsing(String result)
    {
        name_list.clear();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            second_total_count = jsonObject.getString("TotalCount");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                second_welf_name = inner_obj.getString("welf_name");
                second_welf_local = inner_obj.getString("welf_local");
                second_parent_category = inner_obj.getString("parent_category");
                second_welf_category = inner_obj.getString("welf_category");
                second_tag = inner_obj.getString("tag");

                // 하단 리사이클러뷰에 박을 모델 클래스 객체 정의 후 값 대입

                CategorySearchBottomResultItem item = new
                        CategorySearchBottomResultItem();
                item.setWelf_name(second_welf_name);
                item.setWelf_local(second_welf_local);
                item.setParent_category(second_parent_category);
                item.setWelf_category(second_welf_category);
                item.setTag(second_tag);

                // 하단 리사이클러뷰에 쓰이는 리스트에 모델 클래스 객체 대입
                name_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        search_result_benefit_title.setText("검색결과 " + second_total_count + "개입니다");
        if (Integer.parseInt(second_total_count) < 10)
        {
            // 숫자가 1자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(second_total_count) > 9)
        {
            // 숫자가 2자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(second_total_count) > 99)
        {
            // 숫자가 3자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 4, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        // 세로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new CategorySearchResultAdapter(getContext(), name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 상세보기 화면으로 이동. 선택한 혜택 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 혜택 이름을 선택하면 이 이름을 갖고 액티비티를 이동해서 선택한 혜택의 상세 정보를 보여준다
            Intent intent = new Intent(getContext(), DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }



}