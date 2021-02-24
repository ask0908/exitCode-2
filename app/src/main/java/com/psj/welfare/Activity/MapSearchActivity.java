package com.psj.welfare.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapSearchItem;
import com.psj.welfare.R;
import com.psj.welfare.Adapter.MapSearchAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* 지도 화면에서 우상단의 지역변경 텍스트를 누르면 이동하는 검색 화면
* 서초구, 성동구, 강남구 등을 입력했을 때 이 지역이 서울인지 경기인지 등을 판단해야 하는데, 이 작업은 구현하기엔 무리가 있을 것 같아 다르게 접근하는 건? */
public class MapSearchActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();
    LinearLayout search_linear;
    EditText search_area_edittext;
    RecyclerView map_search_recycler;
    MapSearchAdapter adapter;
    MapSearchAdapter.ItemClickListener itemClickListener;
    List<MapSearchItem> lists;
    // split()한 문자열을 담을 리스트
    List<String> keyword_list;
    String keyword;

    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        keyword_list = new ArrayList<>();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        search_linear = findViewById(R.id.search_linear);
        search_linear.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                hideKeyboard();
                return false;
            }
        });

        search_area_edittext = findViewById(R.id.search_area_edittext);
        search_area_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                switch (actionId)
                {
                    case EditorInfo.IME_ACTION_SEARCH :
                        // 검색 했을 때
                        Toast.makeText(MapSearchActivity.this, "검색한 내용 = " + search_area_edittext.getText().toString(), Toast.LENGTH_SHORT).show();
                        String keyword = search_area_edittext.getText().toString();
                        String[] result = keyword.split(" ");
                        keyword_list.addAll(Arrays.asList(result));
                        Log.e(TAG, "검색한 내용 split = " + keyword_list.toString());
                        // 앞의 공백이 0번 인덱스라 1번 인덱스의 값이 서울 등 지역명이다
                        keyword = keyword_list.get(0);
                        // 지역명을 따면 인텐트로 보내서 해당 지역의 혜택들을 보여주게 한다
                        Intent intent = new Intent(MapSearchActivity.this, MapDetailActivity.class);
                        intent.putExtra("area", keyword);
                        // 기존에 있던 액티비티를 불러오기 때문에 플래그 관리를 해야 한다
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });

        map_search_recycler = findViewById(R.id.map_search_recycler);
        map_search_recycler.setHasFixedSize(true);
        map_search_recycler.setLayoutManager(new LinearLayoutManager(this));

        // 예시용 하드코딩
        lists = new ArrayList<>();
        lists.add(new MapSearchItem("서울 서초구 효령도 164"));
        lists.add(new MapSearchItem("서울 성동구 독서당로 377"));
        lists.add(new MapSearchItem("서울 강남구 삼성로 11"));
        adapter = new MapSearchAdapter(MapSearchActivity.this, lists, itemClickListener);
        map_search_recycler.setAdapter(adapter);
        adapter.setOnItemClickListener(new MapSearchAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                String area_name = lists.get(position).getMap_search_result_name();
                search_area_edittext.setText(area_name);
            }
        });

    }

    /* editText 바깥을 터치했을 때 키보드를 내리는 메서드 */
    void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}