package com.psj.welfare.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapSearchItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MapSearchAdapter;

import java.util.ArrayList;
import java.util.List;

public class MapSearchActivity extends AppCompatActivity
{
    EditText search_area_edittext;
    RecyclerView map_search_recycler;
    MapSearchAdapter adapter;
    MapSearchAdapter.ItemClickListener itemClickListener;
    List<MapSearchItem> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

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
                        break;

                    case EditorInfo.IME_ACTION_DONE :
                        // 엔터 버튼을 눌렀을 때
                        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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
}