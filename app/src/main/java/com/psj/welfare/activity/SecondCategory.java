package com.psj.welfare.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SecondCategoryItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.SecondCategoryAdapter;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 두번째 카테고리 선택 액티비티는 사용자의 맞춤 혜택을 보여주기 위한 두번째 단계이다
 * 첫번째 선택으로 연관된 두번째 선택지가 주어진다
 * 사용자에게 보여주는 혜택의 범위를 줄일 수 있어야 한다
 * */
public class SecondCategory extends AppCompatActivity
{
    public static final String TAG = "SecondCategory";

    private RecyclerView SecondCategory_recyclerview;
    private RecyclerView.Adapter SecondCategory_Adapter;

    int position_SecondCategory = 0;
    int position_select = 0;
    int init_color;

    private ArrayList<SecondCategoryItem> SecondCategory_List;
    private JSONArray jsonArray;

    private Button category_done;
    private ImageView secondCategory_back;

    ArrayList<String> Second_List = new ArrayList<>();

    String first_select;
    String second_select;
    JSONObject jsonObject;
    JSONArray select_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondcategory);
        Log.e(TAG, "onCreate 실행!");

        category_done = findViewById(R.id.category_done);

        SecondCategory_List = new ArrayList<SecondCategoryItem>();
        init_color = Color.parseColor("#e6e6e6");

        if (getIntent().hasExtra("retBody") && getIntent().hasExtra("first_select"))
        {
            Intent T_intent = getIntent();
            first_select = T_intent.getStringExtra("first_select");
            String retBody = T_intent.getStringExtra("retBody");
            Log.e(TAG, "리스트 형태 두번째 카테고리 정보 -> " + retBody.toString());
            try
            {
                jsonObject = new JSONObject(retBody);
                select_list = jsonObject.getJSONArray("second_layer");
                Log.e(TAG, "두번째 카테고리 첫번째 정보 : " + select_list.getString(0));
                Log.e(TAG, "리스트 형태 두번째 카테고리 길이 : " + select_list.length());

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            Log.e(TAG, "전달 받은 인텐트 값 없어요!");
        }

        SecondCategory_List = new ArrayList<>();
        // 사용자가 선택한 첫번째 카테고리 정보를 통해서 서버에서 가져온 두번째 카테고리 정보 세팅 하는 곳
        for (int i = 0; i < select_list.length(); i++)
        {
            try
            {
                SecondCategory_List.add(position_SecondCategory, new SecondCategoryItem(select_list.getString(i), init_color));
                position_SecondCategory++;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }


        // 카테고리 두번째 리사이클러뷰 시작
        SecondCategory_recyclerview = findViewById(R.id.recycler_categorySecond);
        SecondCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        SecondCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        SecondCategory_Adapter = new SecondCategoryAdapter(getApplicationContext(), SecondCategory_List, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object object = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) object;
                    Log.e(TAG, "클릭한 포지션 값 : " + position);
                    String c_title = ((SecondCategoryAdapter) SecondCategory_Adapter).getCategory(position).getCategoryTitle();
                    int c_bg = ((SecondCategoryAdapter) SecondCategory_Adapter).getCategory(position).getcategoryBg();

                    int select_color = Color.parseColor("#54A4FF");
                    int ready_color = Color.parseColor("#e6e6e6");

                    // 선택한 버튼 이외에 버튼은 기본 색상을 가지는 반복문
                    for (int i = 0; i < SecondCategory_List.size(); i++)
                    {
                        try
                        {
                            SecondCategory_List.set(i, new SecondCategoryItem(select_list.getString(i), ready_color));
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    // 선택한 버튼은 색상이 파랑으로 바뀌는 로직
                    SecondCategory_List.set(position, new SecondCategoryItem(c_title, select_color));
                    SecondCategory_Adapter.notifyDataSetChanged();

                    // 선택한 버튼과 선택하지 않은 버튼의 색상 수정이 끝난 후
                    // 사용자가 선택한 타이틀을 서버에 보내기 위해서 저장하는 변수
                    second_select = SecondCategory_List.get(position).getCategoryTitle();
                    Log.e(TAG, "서버에 보내기 위해서 저장하는 타이틀 : " + second_select);
                }
            }
        });
        SecondCategory_recyclerview.setAdapter(SecondCategory_Adapter);
        SecondCategory_recyclerview.setHasFixedSize(true);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // 다음단계 버튼 클릭
        category_done.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Log.e(TAG, "다음단계 버튼 클릭!");
                ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                Call<String> call = apiInterface.category2(first_select, second_select, "2");
                call.enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.e(TAG, "onResponse 성공 : " + response.body());
                            String category = response.body();
                            jsonParsing(category);

                        }
                        else
                        {
                            Log.e(TAG, "onResponse 실패" + response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                    {
                        Log.e(TAG, "onFailure : " + t.toString());
                    }
                });
            }
        });

    }

    private void jsonParsing(String category)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(category);

            Log.e(TAG, "JSON 길이 : " + jsonObject.length());
            String retBody = jsonObject.getString("retBody");

            JSONObject layer = new JSONObject(retBody);
            String layerNames = layer.names().toString();
            Log.e(TAG, "결과 카테고리 : " + layerNames);

            if (layerNames.equals("[\"welfare_list\"]"))
            {
                Log.e(TAG, "3차 카테고리 정보 없어요!");
                Intent second_intent = new Intent(SecondCategory.this, EndCategory.class);
                second_intent.putExtra("retBody", retBody);
                startActivity(second_intent);
				finish();
            }
            else if (layerNames.equals("[\"third_layer\"]"))
            {
                Log.e(TAG, "3차 카테고리 정보 있어요!");
                Intent second_intent = new Intent(SecondCategory.this, ThirdCategory.class);
                second_intent.putExtra("retBody", retBody);
                second_intent.putExtra("second_select", second_select);
                second_intent.putExtra("first_select", first_select);
                startActivity(second_intent);
				finish();
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

}
