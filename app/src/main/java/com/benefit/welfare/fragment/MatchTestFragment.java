package com.benefit.welfare.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.benefit.welfare.R;
import com.benefit.welfare.Activity.Compatibility.Compatibility_FirstActivity;

/* 복지국가 궁합 테스트 */
public class MatchTestFragment extends Fragment
{
    private final String TAG = "SecondFragment";
    private Button compatibility_btn;

    public MatchTestFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_match_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        // 테스트 시작 버튼
        compatibility_btn.setOnClickListener(OnSingleClickListener -> {
            Log.e(TAG, "클릭됨");
            Intent intent = new Intent(getActivity(), Compatibility_FirstActivity.class);
            startActivity(intent);
        });
    }

    private void init(View view)
    {
        compatibility_btn = view.findViewById(R.id.compatibility_btn);
    }
}