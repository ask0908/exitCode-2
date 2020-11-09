package com.psj.welfare.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.psj.welfare.activity.Compatibility.Compatibility_FirstActivity;
import com.psj.welfare.R;

public class SecondFragment extends Fragment
{
    private Button compatibility_btn;

    public SecondFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        // 테스트 시작 버튼
        compatibility_btn.setOnClickListener(OnSingleClickListener -> {
            Intent intent = new Intent(getActivity(), Compatibility_FirstActivity.class);
            startActivity(intent);
        });
    }

    private void init(View view)
    {
        compatibility_btn = view.findViewById(R.id.compatibility_btn);
    }
}