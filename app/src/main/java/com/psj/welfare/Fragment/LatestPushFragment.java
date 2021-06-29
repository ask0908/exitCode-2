package com.psj.welfare.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.psj.welfare.R;

/**
 * 최신혜택 프래그먼트 (API 만들어지면 여기에 보여줘야 하는 푸시 알림들을 보여준다)
 */
public class LatestPushFragment extends Fragment
{

    public LatestPushFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_latest_push, container, false);
        return view;
    }
}