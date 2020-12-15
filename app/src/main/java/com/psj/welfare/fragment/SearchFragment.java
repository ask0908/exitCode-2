package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.adapter.SearchAdapter;
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

/* 혜택 이름 검색하는 프래그먼트 */
public class SearchFragment extends Fragment
{
    public static final String TAG = "SearchFragment"; // 로그 찍을 때 사용하는 TAG

    private RecyclerView search_frame_recycler;
    private SearchAdapter adapter;
    private SearchAdapter.ItemClickListener itemClickListener;

    private EditText searching;
    private LinearLayout search_main;
    FrameLayout search_result;
    TextView search_frame_title;

    private ArrayList<SearchItem> searchList;

    int position_Search = 0;

    Toolbar search_toolbar;

    // JSON 값을 파싱할 때 Value를 넣을 변수
    String welf_name, welf_local, parent_category, welf_category, tag;

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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        searching = view.findViewById(R.id.searching);
        search_main = view.findViewById(R.id.search_main);
        search_result = view.findViewById(R.id.search_result);
        search_frame_title = view.findViewById(R.id.search_frame_title);

        searchList = new ArrayList<>();
        search_frame_recycler = view.findViewById(R.id.search_frame_recycler);
        search_frame_recycler.setHasFixedSize(true);
        search_frame_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        searching.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {

                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Log.e(TAG, "검색 키워드 : " + searching.getText());
                    search_main.setVisibility(View.GONE);
                    // 검색 버튼 클릭 되었을 때 처리하는 기능
                    performSearch(searching.getText().toString());
                    return true;
                }

                return false;
            }

        });
    }

    // 모바일 키보드에서 검색 버튼 눌렀을 때
    public void performSearch(String search)
    {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searching.getWindowToken(), 0);
        search_result.setVisibility(View.VISIBLE);
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        if (search != null)
        {
            searchWelfare(search);
        }
    }

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
        // 리사이클러뷰 어댑터에서 쓰는 모델 클래스의 리스트를 선언
        List<SearchItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
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
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 어댑터 초기화, 클릭 리스너 설정
        adapter = new SearchAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener(((view, position) -> {
            String name = list.get(position).getWelf_name();
            Log.e(TAG, "선택한 혜택 이름 = " + name);
            Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
            intent.putExtra("RBF_title", name);
            startActivity(intent);
        }));
        search_frame_recycler.setAdapter(adapter);
    }

}