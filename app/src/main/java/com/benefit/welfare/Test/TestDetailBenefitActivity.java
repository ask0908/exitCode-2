package com.benefit.welfare.Test;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.benefit.welfare.R;
import com.benefit.welfare.Adapter.DetailViewpagerAdapter;

public class TestDetailBenefitActivity extends AppCompatActivity
{
    ViewPager detail_viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_detail_benefit);

        detail_viewpager = findViewById(R.id.detail_viewpager);
        DetailViewpagerAdapter detailViewpagerAdapter = new DetailViewpagerAdapter(getSupportFragmentManager());
        detail_viewpager.setAdapter(detailViewpagerAdapter);

        TabLayout tabLayout = findViewById(R.id.detail_tab_layout);
        tabLayout.setupWithViewPager(detail_viewpager);
    }
}