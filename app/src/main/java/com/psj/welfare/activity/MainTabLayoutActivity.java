package com.psj.welfare.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

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

        // 처음 들어왔을 때 탭에서 보여줄 이미지를 추가한다
        ArrayList<Integer> image = new ArrayList<>();
        image.add(R.drawable.home_red);
        image.add(R.drawable.alarm_icon_gray);
        image.add(R.drawable.search_icon_gray);
        image.add(R.drawable.my_profile_icon_gray);

        for (int i = 0; i < image.size(); i++)
        {
            tabLayout.getTabAt(i).setIcon(image.get(i));
        }

    }

}