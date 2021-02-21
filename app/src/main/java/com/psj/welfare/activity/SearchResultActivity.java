package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.HorizontalSearchResultAdapter;
import com.psj.welfare.adapter.VerticalSearchResultAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* SearchFragment에서 키워드를 입력하고 검색하면 이동되는 액티비티
* 가로 리사이클러뷰에 하위 카테고리들을 중복처리한 다음 넣는다 */
public class SearchResultActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getName();

    // 가로로 하위 카테고리들을 보여줄 리사이클러뷰, 세로로 검색 결과들을 보여줄 리사이클러뷰뷰
    private RecyclerView keyword_category_recycler, search_result_title_recycler;

    // 가로 리사이클러뷰에 붙일 어댑터
    private HorizontalSearchResultAdapter category_adapter;
    private HorizontalSearchResultAdapter.ItemClickListener category_clickListener;

    // 세로 리사이클러뷰에 붙일 어댑터
    private VerticalSearchResultAdapter adapter;
    private VerticalSearchResultAdapter.VerticalItemClickListener itemClickListener;

    // parent_category를 모아둘 list
    List<SearchItem> parent_list, top_list;
    // 세로 리사이클러뷰에 넣을 혜택 이름(welf_name)을 넣을 리스트
    List<SearchItem> name_list;

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

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Logger.addLogAdapter(new AndroidLogAdapter());

        if (getIntent().hasExtra("search"))
        {
            Intent intent = getIntent();
            keyword = intent.getStringExtra("search");
            searchWelfare(keyword);
        }
        parent_list = new ArrayList<>();
        other_list = new ArrayList<>();
        name_list = new ArrayList<>();
        top_list = new ArrayList<>();

        search_result_benefit_title = findViewById(R.id.search_result_benefit_title);
        keyword_category_recycler = findViewById(R.id.keyword_category_recycler);
        search_result_title_recycler = findViewById(R.id.search_result_title_recycler);

        // 가로 리사이클러뷰 처리
        keyword_category_recycler.setHasFixedSize(true);
        keyword_category_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // 세로 리사이클러뷰 처리
        search_result_title_recycler.setHasFixedSize(true);
        search_result_title_recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    /* 키워드와 일치하는 복지혜택들의 데이터를 서버에서 가져오는 메서드 */
    void searchWelfare(String keyword)
    {
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchWelfare(token, session, "search", keyword, LogUtil.getUserLog());
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

    /* 서버에서 받은 값들을 파싱하는 메서드 */
    private void jsonParsing(String search_result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
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

                SearchItem top_item = new SearchItem();

                // 상단의 가로 리사이클러뷰(하위 카테고리 보이는곳)에 넣을 리스트에 박을 객체
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
                SearchItem name_item = new SearchItem();
                name_item.setWelf_name(welf_name);
                name_item.setWelf_category(welf_category);
                name_item.setWelf_local(welf_local);
                name_list.add(name_item);
            }
            total_count = jsonObject.getString("TotalCount");
            Log.e(TAG, "TotalCount = " + total_count);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < top_list.size(); i++)
        {
            Log.e("ddd", "top_list = " + top_list.get(i).getWelf_category());
        }

        // 가로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        top_list.add(0, new SearchItem("전체"));
        category_adapter = new HorizontalSearchResultAdapter(this, top_list, category_clickListener);
        category_adapter.setOnItemClickListener((view, pos) ->
        {
            String name = top_list.get(pos).getWelf_category();
            Log.e(TAG, "선택한 하위 카테고리명 = " + name);
            // 선택한 하위 카테고리에 속하는 정책들을 하단 리사이클러뷰에 표시한다
            // 이 메서드의 JSON 파싱 메서드가 호출되면, 이 파싱 메서드 안에서 해당 하위 카테고리를 상징하는 이미지를 아이템에 set한다
            searchSubCategoryWelfare(name, keyword);
        });

        // 쿼리 결과 개수로 몇 개가 검색됐는지 유저에게 알려준다
        search_result_benefit_title.setText("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");

        // 검색 결과 중 숫자를 빨간색으로 강조
        if (Integer.parseInt(total_count) < 10)
        {
            // 숫자가 1자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(total_count) > 9)
        {
            // 숫자가 2자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(total_count) > 99)
        {
            // 숫자가 3자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        keyword_category_recycler.setAdapter(category_adapter);

        // 세로 리사이클러뷰(필터링 결과 출력)에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            // 선택한 혜택의 이름, 실시지역을 따서 인텐트로 넘겨 상세정보를 볼 수 있게 한다
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
            Intent intent = new Intent(SearchResultActivity.this, DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

    /* 가로 리사이클러뷰 클릭 리스너에서도 이 메서드를 호출해야 세로 리사이클러뷰에 쓰이는 리스트 안의 값들을 바꿀 수 있을 것이다 */
    void searchSubCategoryWelfare(String sub_category, String keyword)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        // 현물 지원, 현금 지원 등의 키워드를 받아야 하니까 상단 가로 리사이클러뷰 클릭 리스너에서 클릭 이벤트가 일어날 때마다, 아이템 안의 문자를 담아와야 한다
        Call<String> call = apiInterface.searchSubCategoryWelfare("child_category_search", sub_category, keyword, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
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
                SearchItem item = new SearchItem();
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
        // 세로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
            // 혜택 이름을 선택하면 이 이름을 갖고 액티비티를 이동해서 선택한 혜택의 상세 정보를 보여준다
            Intent intent = new Intent(SearchResultActivity.this, DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

}