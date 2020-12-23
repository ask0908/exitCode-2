package com.psj.welfare.Test;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.SearchResultActivity;

import java.util.ArrayList;

/* SearchFragment에 적용하기 전에 확인하는 테스트 프래그먼트 */
public class TestSearchFragment extends Fragment
{
    public static final String TAG = "SearchFragment"; // 로그 찍을 때 사용하는 TAG

    private EditText searching;

    private ArrayList<SearchItem> searchList;

    Toolbar search_toolbar;

    // JSON 값을 파싱할 때 Value를 넣을 변수
    String welf_name, welf_local, parent_category, welf_category, tag;

    TextView recommend_search_textview, recent_search_history_textview;

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
        return inflater.inflate(R.layout.fragment_test_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        recommend_search_textview = view.findViewById(R.id.recommend_search_textview);
        recent_search_history_textview = view.findViewById(R.id.recent_search_history_textview);
        searching = view.findViewById(R.id.searching);

        searchList = new ArrayList<>();

        search_toolbar = view.findViewById(R.id.search_toolbar);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(search_toolbar);
        }
        search_toolbar.setTitle("검색");

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        searching.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {

                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Log.e(TAG, "검색 키워드 : " + searching.getText());
//                    search_main.setVisibility(View.GONE);
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
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra("search", search);
        startActivity(intent);
        // 아래가 원래 작동하던 코드
//        if (search != null)
//        {
//            searchWelfare(search);
//        }
    }

}