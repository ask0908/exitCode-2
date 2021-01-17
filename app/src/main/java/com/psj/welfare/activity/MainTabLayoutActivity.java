package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MainViewPagerAdapter;

import java.util.ArrayList;

/* 메인 화면 등 프래그먼트 4개를 보여줄 액티비티 */
public class MainTabLayoutActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    // 프래그먼트 별 화면을 표시할 뷰페이저
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintablayout);

        sharedPreferences = getSharedPreferences("app_pref", 0);

        viewPager = findViewById(R.id.main_viewpager);
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            /* 탭 선택했을 때 / 선택하지 않았을 때 탭 아이템에서 어떤 이미지를 보여줄지 처리하는 부분 */
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();
                if (position == 0)
                {
                    tab.setIcon(R.drawable.home_red);
                }
                else if (position == 1)
                {
                    tab.setIcon(R.drawable.alarm_red);
                }
                else if (position == 2)
                {
                    tab.setIcon(R.drawable.search_red);
                }
                else if (position == 3)
                {
                    tab.setIcon(R.drawable.mypage_red);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();

                if (position == 0)
                {
                    tab.setIcon(R.drawable.home_icon_gray);
                }
                else if (position == 1)
                {
                    tab.setIcon(R.drawable.alarm_icon_gray);
                }
                else if (position == 2)
                {
                    tab.setIcon(R.drawable.search_icon_gray);
                }
                else if (position == 3)
                {
                    tab.setIcon(R.drawable.my_profile_icon_gray);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        LinearLayout tab_layout = (LinearLayout) tabLayout.getChildAt(0);
        tab_layout.getChildAt(3).setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_UP :
                        if (sharedPreferences.getString("user_category", "").equals(""))
                        {
                            // 키워드 쉐어드가 비어있으면 로그인 화면으로 이동시킨다
                            Intent intent = new Intent(MainTabLayoutActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        break;
                }
                return false;
            }
        });

        // 처음 들어왔을 때 하단 탭에서 보여줄 이미지를 ArrayList에 추가한다
        ArrayList<Integer> image = new ArrayList<>();
        image.add(R.drawable.home_red);
        image.add(R.drawable.alarm_icon_gray);
        image.add(R.drawable.search_icon_gray);
        image.add(R.drawable.my_profile_icon_gray);

        // for문으로 이미지가 든 ArrayList를 돌며 탭에 set
        for (int i = 0; i < image.size(); i++)
        {
            tabLayout.getTabAt(i).setIcon(image.get(i));
        }

    }

}