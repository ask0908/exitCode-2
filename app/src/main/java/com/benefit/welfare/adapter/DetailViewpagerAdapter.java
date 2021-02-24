package com.benefit.welfare.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.benefit.welfare.Fragment.ContentFragment;
import com.benefit.welfare.Fragment.FacilitiesFragment;
import com.benefit.welfare.Fragment.ReviewFragment;

import java.util.ArrayList;

public class DetailViewpagerAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> list = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();

    public DetailViewpagerAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
        list.add(new ContentFragment());
        list.add(new ReviewFragment());
        list.add(new FacilitiesFragment());

        name.add("내용");
        name.add("리뷰");
        name.add("주변시설");
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return name.get(position);
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
