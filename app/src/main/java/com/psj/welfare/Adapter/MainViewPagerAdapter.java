package com.psj.welfare.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.psj.welfare.test.TestFragment;
import com.psj.welfare.test.TestMyPageFragment;
import com.psj.welfare.test.TestPushGatherFragment;
import com.psj.welfare.test.TestSearchFragment;

import java.util.ArrayList;
import java.util.List;

public class MainViewPagerAdapter extends FragmentPagerAdapter
{
    private final List<Fragment> list = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    private static Context mContext;

    public MainViewPagerAdapter(Context context, @NonNull FragmentManager fm)
    {
        super(fm);
        mContext = context;
//        list.add(new MainFragment());
        list.add(new TestFragment());
//        list.add(new SearchFragment());
        list.add(new TestSearchFragment());
        list.add(new TestPushGatherFragment());
//        list.add(new PushGatherFragment());
//        list.add(new MyPageFragment());
        list.add(new TestMyPageFragment());
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

    public void addFragment(Fragment fragment, String title)
    {
        list.add(fragment);
        mFragmentTitleList.add(title);
    }

}
