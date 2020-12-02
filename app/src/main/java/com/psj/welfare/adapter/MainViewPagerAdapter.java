package com.psj.welfare.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import com.psj.welfare.fragment.PushGatherFragment;
import com.psj.welfare.fragment.SearchFragment;
import com.psj.welfare.fragment.MainFragment;
import com.psj.welfare.fragment.MatchTestFragment;
import com.psj.welfare.fragment.InterestBenefitFragment;


import java.util.ArrayList;

public class MainViewPagerAdapter extends FragmentPagerAdapter
{
    // 아래에 있는 생성자에 선언한 프래그먼트들을 담을 리스트. 이 리스트에 담긴 사이즈만큼 탭 레이아웃을 만든다
    // activity_maintest.xml에서도 프래그먼트 개수만큼 TabItem을 만들어야 함
    private ArrayList<Fragment> list = new ArrayList<>();

    public MainViewPagerAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
        list.add(new MainFragment());
        list.add(new MatchTestFragment());
        list.add(new InterestBenefitFragment());
        list.add(new SearchFragment());
        list.add(new PushGatherFragment());
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public int getCount()
    {
        return list.size();
    }
}
