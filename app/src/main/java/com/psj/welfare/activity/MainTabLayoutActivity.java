package com.psj.welfare.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MainViewPagerAdapter;

import java.util.ArrayList;

/*
 * 메인 테스트 액티비티는 구현해 놓은 메인 액티비티를 수정이 필요할 때
 * 수정된 화면을 미리보기 할 때 사용한다
 * 2번째 탭(스낵컨텐츠)을 삭제한다
 * */
public class MainTabLayoutActivity extends AppCompatActivity
{

    public final String TAG = this.getClass().getName();

    // 프래그먼트 별 화면을 표시할 뷰페이저
    ViewPager viewPager;

    LogoutResponseCallback logoutCallbak;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintablayout);

        viewPager = findViewById(R.id.main_viewpager);
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.main_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();

                if (position == 0)
                {
                    tab.setIcon(R.drawable.home_icon_focus);
                }
                else if (position == 1)
                {
                    tab.setIcon(R.drawable.alarm_icon_focus);
                }
                else if (position == 2)
                {
                    tab.setIcon(R.drawable.search_icon_focus);
                }
                else if (position == 3)
                {
                    tab.setIcon(R.drawable.my_profile_icon_focus);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                Log.e(TAG, "onTabUnselected() 호출");

                int position = tab.getPosition();
                Log.e(TAG, "onTabUnselected() position -> " + position); // 탭 포지션 확인 로그

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
            public void onTabReselected(TabLayout.Tab tab)
            {
                Log.e(TAG, "onTabReselected() 호출");
            }
        });

        // 탭에 이미지 추가
        ArrayList<Integer> image = new ArrayList<>();
        image.add(R.drawable.home_icon_focus);
//        image.add(R.drawable.second_icon_gray);
        image.add(R.drawable.alarm_icon_gray);
        image.add(R.drawable.search_icon_gray);
        image.add(R.drawable.my_profile_icon_gray);

        for (int i = 0; i < image.size(); i++)
        {
            tabLayout.getTabAt(i).setIcon(image.get(i));
        }

    }

}