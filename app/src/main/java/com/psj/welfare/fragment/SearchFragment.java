package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.psj.welfare.custom.MaterialSpinner;
import com.psj.welfare.custom.SearchRecyclerViewEmpty;
import com.psj.welfare.data.CategorySearchBottomResultItem;
import com.psj.welfare.data.CategorySearchResultItem;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

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

    //    Spinner s1;
    MaterialSpinner s1;

    //Create Spinner
    private Spinner mSpinner;

    // 하단 리사이클러뷰에 검색 결과가 없을 때 보여줄 뷰
    private ImageView emptyImageView;
    private TextView emptyView;
    private TextView emptyView2;
    private View no_result_view;

    private RecyclerView up_recycler;
    // 상단 리사이클러뷰에 붙일 어댑터
    private SelectedCategoryAdapter up_adapter;
    private SelectedCategoryAdapter.ItemClickListener up_itemClickListener;
    // 상단 리사이클러뷰에 쓸 리스트
    List<CategorySearchResultItem> keyword_list;

    private SearchRecyclerViewEmpty bottom_recycler;
    // 하단 리사이클러뷰에 붙일 어댑터
    private CategorySearchResultAdapter bottom_adapter;
    private CategorySearchResultAdapter.ItemClickListener bottom_itemClickListener;
    // 하단 리사이클러뷰에 쓸 리스트
    List<CategorySearchBottomResultItem> item_list;

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
    List<CategorySearchBottomResultItem> other_list;

    //    String city;
    String region_name;


    // 구글 애널리틱스

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    String original_local, original_name;

    public SearchFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        search_edittext = view.findViewById(R.id.search_edittext);

        final String[] city = {"전국", "서울", "경기", "인천", "강원", "세종", "충북", "충남", "대전",
                "대구", "경북", "경남", "전북", "전남", "광주", "부산", "울산", "제주"};

        com.psj.welfare.custom.MaterialSpinner s1 = (com.psj.welfare.custom.MaterialSpinner) view.findViewById(R.id.spinner);

        ArrayAdapter adapter = new ArrayAdapter(
                getContext(), // 현재화면의 제어권자
//                android.R.layout.simple_spinner_item, // 레이아웃
                R.layout.spin,
                city); // 데이터

        s1.setAdapter(adapter);

        region_name = "전국";

        s1.setOnItemSelectedListener(new com.psj.welfare.custom.MaterialSpinner.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(com.psj.welfare.custom.MaterialSpinner view, int position, long id, Object item)
            {
                region_name = item.toString();
            }
        });

        other_list = new ArrayList<>();
        item_list = new ArrayList<>();
        keyword_list = new ArrayList<>();

        search_result_benefit_title = view.findViewById(R.id.search_result_benefit_title);
        up_recycler = view.findViewById(R.id.keyword_category_recycler);
        bottom_recycler = view.findViewById(R.id.search_result_recycler);

        emptyImageView = view.findViewById(R.id.emptyImageView);
        emptyView = view.findViewById(R.id.search_empty);
        emptyView2 = view.findViewById(R.id.search_empty2);
        no_result_view = view.findViewById(R.id.no_result_view);

        // 가로 리사이클러뷰 처리
        up_recycler.setHasFixedSize(true);
        up_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // 세로 리사이클러뷰 처리
        bottom_recycler.setHasFixedSize(true);
        bottom_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

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
        m_favorList = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        if ((AppCompatActivity) getActivity() != null)
        {
            ((AppCompatActivity) getActivity()).setSupportActionBar(search_toolbar);
        }

        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " + search_edittext.getText());
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    performSearch(search_edittext.getText().toString(), region_name);
                    return true;
                }

                return false;
            }

        });

    }

    public void performSearch(String search, String city)
    {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_edittext.getWindowToken(), 0);
        original_local = city;
        original_name = search;

        item_list.clear();
        keyword_list.clear();
        searchWelfare(search, city);
    }

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
                    jsonParse(search_result);
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

    private void jsonParse(String search_result)
    {
        keyword_list = new ArrayList<>();
        item_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
            total_count = jsonObject.getString("TotalCount");
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                parent_category = inner_json.getString("parent_category");
                welf_name = inner_json.getString("welf_name");
                welf_category = inner_json.getString("welf_category");
                welf_local = inner_json.getString("welf_local");
                tag = inner_json.getString("tag");

                /**
                 * CategorySearchResultItem : 상단 (이전 ResultKeywordItem)
                 * CategorySearchBottomResultItem : 하단 (이전 MapResultItem)
                 */

                // 상단 리사이클러뷰에 넣을 키워드(welf_category)를 저장할 객체 생성
                CategorySearchResultItem keywordItem = new CategorySearchResultItem();
                keywordItem.setWelf_name(welf_name);
                keywordItem.setWelf_category(welf_category);

                // 하단 리사이클러뷰에 넣을 혜택 관련 값들을 객체에 대입해서 나중에 getter로 가져와 상/하단 리사이클러뷰에서 보여줄 수 있게 한다
                CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
                item.setWelf_name(welf_name);
                item.setWelf_category(welf_category);
                item.setWelf_local(welf_local);

                // 상단 리사이클러뷰에 넣을 welf_category 값들에 붙어 있는 특수문자들을 파싱, 중복되지 않게 처리한다
                // 객체가 가진 값에 ';; '이 포함되어 있을 경우 ';; '을 기준으로 split()한 후, 상단 리사이클러뷰 어댑터에 넣을 리스트(keyword_list)에 값들을 넣는다
                if (keywordItem.getWelf_category().contains(";; "))
                {
                    // ';; ' 구분자가 포함된 welf_category 파싱 시작
                    String beforeWelfCategory = keywordItem.getWelf_category();
                    String[] category_array = beforeWelfCategory.split(";; ");
                    // 취업 지원이 보이지 않아서 작성한 로직. category_array 안의 값들을 keyword_list에 넣어서 취업 지원이 나오도록 시도
                    for (int j = 0; j < category_array.length; j++)
                    {
                        CategorySearchResultItem keyword = new CategorySearchResultItem();
                        keyword.setWelf_category(category_array[j]);
                        keyword_list.add(keyword);
                    }
                    // 상단 리사이클러뷰에 붙이기 위해 상단 리사이클러뷰 어댑터 초기화 시 사용하는 모델 클래스의 객체를 생성해서
                    // setter()로 welf_category, welf_name 값들을 집어넣는다
                    CategorySearchResultItem items = new CategorySearchResultItem();
                    for (int j = 0; j < category_array.length; j++) // category_array는 ';; '을 기준으로 split()한 결과물들이 담겨 있는 String[]이다
                    {
                        // split()한 결과가 들어있는 String[]의 크기만큼 반복해서 상단 리사이클러뷰 어댑터에 넣을 리스트에 값들을 setter()로 넣는다
                        items.setWelf_category(category_array[j]);
                        items.setWelf_name(keywordItem.getWelf_name());
                        // 상단 리사이클러뷰에 'OO 지원'을 보여줄 때 사용하는 리스트에 값을 넣는다
                        // keyword_list는 ResultKeywordItem 객체만 받는 ArrayList다
                        keyword_list.add(items);
                    }
                }
                else if (keywordItem.getWelf_name().contains(";; "))
                {
                    String beforeWelfName = keywordItem.getWelf_name();
                    String[] name_array = beforeWelfName.split(";; ");
                    for (int j = 0; j < name_array.length; j++)
                    {
                        CategorySearchResultItem keyword = new CategorySearchResultItem();
                        keyword.setWelf_name(name_array[j]);
                        keyword_list.add(keyword);
                    }
                    CategorySearchResultItem items = new CategorySearchResultItem();
                    for (int j = 0; j < name_array.length; j++) // name_array는 ';; '을 기준으로 split()한 결과물들이 담겨 있는 String[]이다
                    {
                        // split()한 결과가 들어있는 String[]의 크기만큼 반복해서 상단 리사이클러뷰 어댑터에 넣을 리스트에 값들을 setter()로 넣는다
                        items.setWelf_name(keywordItem.getWelf_name());
                        // 상단 리사이클러뷰에 'OO 지원'을 보여줄 때 사용하는 리스트에 값을 넣는다
                        // keyword_list는 ResultKeywordItem 객체만 받는 ArrayList다
                        keyword_list.add(items);
                    }
                }
                else
                {
                    // ';; ' 구분자가 붙어서 오지 않은 경우에는 중복되는 값이 없도록 처리한 다음 'OO 지원' 문자열들을 넣는다
                    CategorySearchResultItem item1 = new CategorySearchResultItem();
                    item1.setWelf_category(keywordItem.getWelf_category());
                    item1.setWelf_name(keywordItem.getWelf_name());

                    // 중복되는 값이 있는지 확인한 후 리스트에 add()한다
                    // 아래 코드를 없애면 기능이 제대로 작동하지 않는다
                    if (!keyword_list.contains(item1))
                    {
                        keyword_list.add(item1);
                    }
                }
                // 혜택 이름들을 보여주는 하단 리사이클러뷰의 어댑터에 넣을 List에
                // for문이 반복된 만큼 생성된 DTO 객체들을 넣는다. 이 부분이 for문의 마지막 부분이다
                item_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (status.equals("500") || status.equals("404"))
        {
            up_recycler.setVisibility(View.INVISIBLE);
            search_result_benefit_title.setVisibility(View.INVISIBLE);
            no_result_view.setVisibility(View.VISIBLE);
            bottom_recycler.setEmptyView(emptyImageView, emptyView, emptyView2);
        }
        else
        {
            search_result_benefit_title.setVisibility(View.VISIBLE);
            up_recycler.setVisibility(View.VISIBLE);
            no_result_view.setVisibility(View.GONE);
        }

        search_result_benefit_title.setText("검색결과 " + total_count + "개입니다");
        int first_changed_count = Integer.parseInt(total_count);
        if (first_changed_count < 10)
        {
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                    5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else
        {
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                    5, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }

        /* 상단 리사이클러뷰에 들어갈 "OO 지원"의 중복처리 로직 시작 */
        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
        StringBuilder stringBuilder = new StringBuilder();          // 상단 리사이클러뷰에 보일 'OO 지원'을 담을 StringBuilder
        StringBuilder welfareNameBuilder = new StringBuilder();     // 하단 리사이클러뷰에 보일 혜택명들을 담을 StringBuilder
        StringBuilder welfareLocalBuilder = new StringBuilder();

        /* 중복처리를 하기 위해 ';;' 구분자를 한번 더 붙인 다음 이를 파싱해서 HashSet에 넣어 중복되는 값들을 없애는 처리를 수행했다
         * 일을 2번 하는 느낌이라 이 부분은 나중에 확인한다 */
        // 상단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 'OO 지원' 사이사이에 ';;'을 붙인다
        for (int i = 0; i < keyword_list.size(); i++)
        {
            stringBuilder.append(keyword_list.get(i).getWelf_category()).append(";;");
        }

        // 하단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 혜택명 사이사이에 ';;'을 붙인다
        for (int i = 0; i < item_list.size(); i++)
        {
            welfareNameBuilder.append(item_list.get(i).getWelf_name()).append(";;");
        }

        for (int i = 0; i < item_list.size(); i++)
        {
            welfareLocalBuilder.append(item_list.get(i).getWelf_local()).append(";;");
        }

        // ";;"가 섞인 문자열 2개를 구분자로 각각 split()한다
        String[] arr = stringBuilder.toString().split(";;");
        String[] nameArr = welfareNameBuilder.toString().split(";;");
        String[] localArr = welfareLocalBuilder.toString().split(";;");

        // split() 처리 후 중복되는 것들을 없애기 위해 HashSet을 썼다
        arr = new LinkedHashSet<>(Arrays.asList(arr)).toArray(new String[0]);
        nameArr = new HashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);

        // String[] arr 안에 들어있는 데이터 양만큼 반복문을 돌리며 setter로 welf_category를 넣기 위해 객체를 만들고, 아래 for문에서 setter로 값들을 박는다
        keyword_list.clear();
        /* ResultKeywordItem : 상단 리사이클러뷰에 쓰는 모델 클래스 / MapResultItem : 하단 리사이클러뷰에 쓰는 모델 클래스 */
        for (int i = 0; i < arr.length; i++)
        {
            // setter 사용을 위한 객체 생성
            CategorySearchResultItem item = new CategorySearchResultItem();   // 상단 필터 리사이클러뷰에 사용할 모델 클래스
            item.setWelf_category(arr[i]);
            item.setWelf_name(nameArr[i]);
            item.setWelf_local(localArr[i]);
            keyword_list.add(item);
        }

        for (int i = 0; i < localArr.length; i++)
        {
            CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
            item.setWelf_local(localArr[i]);
            other_list.add(item);
        }

        if (!keyword_list.get(0).getWelf_category().equals("전체") && !keyword_list.contains("전체"))
        {
            keyword_list.add(0, new CategorySearchResultItem("전체"));
        }

        up_adapter = new SelectedCategoryAdapter(getActivity(), keyword_list, up_itemClickListener);
        up_adapter.setOnItemClickListener((view, position) ->
        {
            /* 상단 리사이클러뷰에서 전체를 클릭한 경우 */
            if (keyword_list.get(position).getWelf_category().equals("전체"))
            {
                searchWelfare(original_name, original_local);
                search_result_benefit_title.setText("검색결과 " + total_count + "개입니다");
                int changed_count = Integer.parseInt(total_count);
                if (changed_count < 10)
                {
                    SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                            5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else
                {
                    SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                            5, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
            }
            else
            {
                /* 상단 리사이클러뷰에서 전체 이외의 필터를 클릭한 경우 */
                other_list.clear();

                for (int i = 0; i < item_list.size(); i++)
                {
                    CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
                    if (item_list.get(i).getWelf_category().contains(keyword_list.get(position).getWelf_category()))
                    {
                        item.setWelf_category(keyword_list.get(position).getWelf_category());
                        item.setWelf_local(item_list.get(i).getWelf_local());
                        item.setWelf_name(item_list.get(i).getWelf_name());
                        other_list.add(item);
                    }
                }

                search_result_benefit_title.setText("검색결과 " + other_list.size() + "개입니다");
                if (other_list.size() < 10)
                {
                    SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                            5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else
                {
                    SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)),
                            5, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }

                bottom_adapter = new CategorySearchResultAdapter(getActivity(), other_list, bottom_itemClickListener);
                bottom_adapter.setOnItemClickListener((view1, pos) ->
                {
                    String name = other_list.get(pos).getWelf_name();
                    String local = other_list.get(pos).getWelf_local();

                    Intent see_detail_intent = new Intent(getActivity(), DetailBenefitActivity.class);
                    see_detail_intent.putExtra("name", name);
                    see_detail_intent.putExtra("welf_local", local);
                    startActivity(see_detail_intent);
                });
                bottom_recycler.setAdapter(bottom_adapter);
            }
        });

        up_recycler.setAdapter(up_adapter);

        bottom_adapter = new CategorySearchResultAdapter(getActivity(), item_list, bottom_itemClickListener);

        bottom_adapter.setOnItemClickListener((view, position) ->
        {
            String name = item_list.get(position).getWelf_name();
            String local = item_list.get(position).getWelf_local();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name + ")");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent see_detail_intent = new Intent(getActivity(), DetailBenefitActivity.class);
            see_detail_intent.putExtra("name", name);
            see_detail_intent.putExtra("welf_local", local);
            startActivity(see_detail_intent);
        });
        bottom_recycler.setAdapter(bottom_adapter);
    }

}