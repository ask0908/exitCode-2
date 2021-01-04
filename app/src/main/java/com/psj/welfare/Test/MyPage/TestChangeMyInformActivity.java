package com.psj.welfare.Test.MyPage;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.psj.welfare.R;

/* 키워드 선택 화면(ChoiceKeywordActivity) 이전에 두는 화면 */
public class TestChangeMyInformActivity extends AppCompatActivity
{
    Toolbar toolbar;

    NumberPicker area_picker;
    final String[] first_area = {"지역 선택", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기도 가평군",
            "경기도 고양시", "경기도 과천시", "경기도 광명시", "경기도 광주시", "경기도 구리시", "경기도 군포시", "경기도 남양주시", "경기도 동두천시", "경기도 부천시", "경기도 성남시",
            "경기도 수원시", "경기도 시흥시", "경기도 안산시", "경기도 안성시", "경기도 안양시", "경기도 양주시", "경기도 양평군", "경기도 여주시", "경기도 연천군", "경기도 오산시",
            "경기도 용인시", "경기도 의왕시", "경기도 의정부시", "경기도 이천시", "경기도 파주시", "경기도 평택시", "경기도 포천시", "경기도 하남시", "경기도 화성시"};

    private Menu mOptionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_change_my_inform);

        toolbar = findViewById(R.id.change_myinform_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        area_picker = findViewById(R.id.change_area_picker);
        area_picker.setMinValue(0);
        area_picker.setMaxValue(first_area.length - 1);
        area_picker.setDisplayedValues(first_area);
        area_picker.setWrapSelectorWheel(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mOptionMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.keyword_menu, menu);

        // 메뉴 버튼 색 바꾸기
        MenuItem liveitem = mOptionMenu.findItem(R.id.keyword_ok);
        SpannableString spannableString = new SpannableString(liveitem.getTitle().toString());
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryDark)), 0, spannableString.length(), 0);
        liveitem.setTitle(spannableString);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                // 좌상단 백버튼 눌렀을 시
        }
        return super.onOptionsItemSelected(item);
    }
}