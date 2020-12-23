package com.psj.welfare.activity;

import android.content.Intent;
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
    List<SearchItem> parent_list;
    // 세로 리사이클러뷰에 넣을 혜택 이름(welf_name)을 넣을 리스트
    List<SearchItem> name_list;

    // 서버에서 받은 JSONArray 안의 값들을 담을 변수
    String welf_name, welf_local, parent_category, welf_category, tag;
    // 쿼리 결과 개수를 담을 변수
    String total_count;

    // 쿼리 결과 개수를 보여줄 텍스트뷰
    TextView search_result_benefit_title;

    // 중복 제거에 사용할 리스트
    List<SearchItem> other_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        Logger.addLogAdapter(new AndroidLogAdapter());

        if (getIntent().hasExtra("search"))
        {
            Intent intent = getIntent();
            String keyword = intent.getStringExtra("search");
            searchWelfare(keyword);
        }
        parent_list = new ArrayList<>();
        other_list = new ArrayList<>();
        name_list = new ArrayList<>();

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
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchWelfare("search", keyword);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String search_result = response.body();
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
                SearchItem parent_item = new SearchItem();
                // 상단의 가로 리사이클러뷰에 넣을 리스트
                parent_item.setParent_category(welf_category);
                /* 가로 리사이클러뷰에 들어갈 내용 중복 제거 */
                other_list.add(parent_item);
                parent_list.add(parent_item);
//                for (int j = 0; j < parent_list.size(); j++)
//                {
//                    if (!other_list.contains(parent_list.get(j)))
//                    {
//                        other_list.add(parent_list.get(j));
//                    }
//                    Log.e(TAG, "" + parent_list.get(j));
//                }
                // 하단의 세로 리사이클러뷰에 넣을 리스트
                SearchItem name_item = new SearchItem();
                name_item.setWelf_name(welf_name);
                name_list.add(name_item);
            }
            total_count = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < other_list.size(); i++)
        {
            Log.e(TAG, "중복 제거 확인 = " + other_list.get(i));
        }
        /* ;; 구분자를 없앤 다음, 분리된 문자열들을 중복 처리해서 리스트에 담는다 */
        // 가로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        category_adapter = new HorizontalSearchResultAdapter(this, other_list, category_clickListener);
        category_adapter.setOnItemClickListener(((view, pos) -> {
            String name = other_list.get(pos).getParent_category();
        }));
        // 쿼리 결과 개수로 몇 개가 검색됐는지 유저에게 알려준다
        search_result_benefit_title.setText("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
        /* total_count의 숫자가 1자리수/2자리수인 경우 각각 색깔 강조 처리 */
        if (Integer.parseInt(total_count) > 9)
        {
            // 숫자가 2자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(total_count) < 10)
        {
            // 숫자가 1자리수인 경우
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        keyword_category_recycler.setAdapter(category_adapter);

        // 세로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            Log.e(TAG, "선택한 혜택명 = " + name);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

}