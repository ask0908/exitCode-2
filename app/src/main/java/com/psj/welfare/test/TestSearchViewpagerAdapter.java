package com.psj.welfare.test;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class TestSearchViewpagerAdapter extends FragmentStatePagerAdapter
{
    ArrayList<Fragment> fragments = new ArrayList<>();

    public TestSearchViewpagerAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
    }

    public void addItem(Fragment fragment)
    {
        fragments.add(fragment);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }
}
