package com.psj.welfare.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.adapter.RenewalSearchResultAdapter;
import com.psj.welfare.data.SearchResultItem;
import com.psj.welfare.viewmodel.SearchViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TestSearchResultActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();
    SearchViewModel searchViewModel;
    EditText search_result_edittext;
    Toolbar toolbar;
    RecyclerView search_result_recyclerview;
    RenewalSearchResultAdapter adapter;
    List<SearchResultItem> list;
    RenewalSearchResultAdapter.onItemClickListener itemClickListener;

    // 검색 화면에서 가져온 검색어
    String keyword;
    // 총 검색 결과 개수
    String total_result_count;
    // 검색 결과를 담을 변수
    String welf_id, welf_name, welf_tag, welf_count, welf_local, welf_thema;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_search_result);

        toolbar = findViewById(R.id.search_result_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        search_result_edittext = findViewById(R.id.search_result_edittext);
        search_result_recyclerview = findViewById(R.id.search_result_recyclerview);

        // 검색어 담긴 변수 널 체크
        if (getIntent().hasExtra("keyword"))
        {
            Intent intent = getIntent();
            keyword = intent.getStringExtra("keyword");
            search_result_edittext.setText(keyword);
            renewalKeywordSearch();
        }

    }

    public void renewalKeywordSearch()
    {
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
            searchViewModel.renewalSearchKeyword(keyword, "1", null, null, null, null)
                    .observe(this, searchObserver);
        }
    }

    private void responseParsing(String result)
    {
        list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total_result_count = jsonObject.getString("TotalCount");

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
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* 값 들어오는 것 확인 */
//        for (int i = 0; i < list.size(); i++)
//        {
//            Log.e(TAG, "list 안의 id값 : " + list.get(i).getWelf_id());
//            Log.e(TAG, "list 안의 name값 : " + list.get(i).getWelf_name());
//            Log.e(TAG, "list 안의 tag값 : " + list.get(i).getWelf_tag());
//            Log.e(TAG, "list 안의 count값 : " + list.get(i).getWelf_count());
//            Log.e(TAG, "list 안의 local값 : " + list.get(i).getWelf_local());
//            Log.e(TAG, "list 안의 thema값 : " + list.get(i).getWelf_thema());
//        }

        adapter = new RenewalSearchResultAdapter(this, list, itemClickListener);
        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String tag = list.get(pos).getWelf_tag();
            String count = list.get(pos).getWelf_count();
            String local = list.get(pos).getWelf_local();
            String thema = list.get(pos).getWelf_thema();
            Log.e(TAG, "선택한 아이템의 이름 : " + name + ", 태그 : " + tag + ", 조회수 : " + count + ", 지역 : " + local + ", 테마 : " + thema);
        });
        search_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        search_result_recyclerview.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}