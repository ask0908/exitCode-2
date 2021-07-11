package com.psj.welfare.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.adapter.PushGatherAdapter;
import com.psj.welfare.data.PushGatherItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 추천혜택 프래그먼트 (API 만들어지면 여기에 보여줘야 하는 푸시 알림들을 보여준다)
 */
public class RecommendPushFragment extends Fragment
{
    ImageView recommend_push_bell_image;
    TextView recommend_push_bell_textview;
    Button recommend_push_login_button;
    RecyclerView recommend_push_recyclerview;
    PushGatherAdapter adapter;
    PushGatherAdapter.ItemClickListener itemClickListener;

    SharedPreferences sharedPreferences;

    public RecommendPushFragment()
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
        View view = inflater.inflate(R.layout.fragment_recommend_push, container, false);

        recommend_push_bell_image = view.findViewById(R.id.recommend_push_bell_image);
        recommend_push_bell_textview = view.findViewById(R.id.recommend_push_bell_textview);
        recommend_push_login_button = view.findViewById(R.id.recommend_push_login_button);
        recommend_push_recyclerview = view.findViewById(R.id.recommend_push_recyclerview);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        List<PushGatherItem> list = new ArrayList<>();
        adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        boolean isLogout = sharedPreferences.getBoolean("logout", false);

        if (isLogout)
        {
            // 로그아웃 변수가 true = 로그아웃한 것 = 버튼을 보여준다
            Logger.d("isLogout이 true일 거야. 그럼 비로그인이지 : " + isLogout);
            recommend_push_bell_textview.setText("최신 혜택과 추천 혜택을 받고 싶다면\n로그인을 해 주세요");
            recommend_push_bell_textview.setVisibility(View.VISIBLE);
            recommend_push_login_button.setVisibility(View.VISIBLE);
            recommend_push_recyclerview.setVisibility(View.GONE);
        }
        else
        {
            Logger.d("isLogout이 false일 거야. 그럼 로그인 한 거지 : " + isLogout);
            // 로그아웃 변수가 false = 로그인한 것 = 리사이클러뷰를 보여준다
            // 그런데 어댑터의 getItemCount()가 0이라면 아이템이 없는 것이니 텍스트뷰 문구를 다르게 해서 보여준다
            if (adapter.getItemCount() == 0)
            {
                // 어댑터에 값이 없으면 알림이 없는 것이니 리사이클러뷰를 숨기고 텍스트뷰를 보여준다
                recommend_push_bell_textview.setText("도착한 혜택 알림이 없어요");
                recommend_push_bell_textview.setVisibility(View.VISIBLE);
                recommend_push_bell_image.setVisibility(View.VISIBLE);
                recommend_push_recyclerview.setVisibility(View.GONE);
                recommend_push_login_button.setVisibility(View.GONE);
            }
            else
            {
                recommend_push_bell_image.setVisibility(View.GONE);
                recommend_push_bell_textview.setVisibility(View.GONE);
                recommend_push_login_button.setVisibility(View.GONE);
                recommend_push_recyclerview.setVisibility(View.VISIBLE);
            }
        }

        /* 로그인 버튼 누르면 로그인 화면으로 이동하도록 처리 */
        recommend_push_login_button.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }
}