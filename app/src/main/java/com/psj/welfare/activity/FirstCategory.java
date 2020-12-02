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

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.FirstCategoryItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.FirstCategoryAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/*
 * 첫번째 카테고리 선택 액티비티는 사용자의 맞춤 혜택을 보여주기 위한 첫 단계이다
 * 메인 액티비티에서 보여주는 관심사를 1차로 보여주고 하나만 선택하게 하여 사용자에게 보여주는 혜택의 범위를
 * 줄일 수 있어야 한다
 * */
public class FirstCategory extends AppCompatActivity
{
    public static final String TAG = "FirstCategory";

    private RecyclerView FirstCategory_recyclerview;
    private RecyclerView.Adapter FirstCategory_Adapter;

    int position_FirstCategory = 0;
    int position_select = 0;
    int init_color;

    private ArrayList<FirstCategoryItem> FirstCategory_List;

    private ImageView firstCategory_back;
    private Button category_done;

    ArrayList<String> First_List;
    String select;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstcategory);
        Logger.addLogAdapter(new AndroidLogAdapter());

        FirstCategory_List = new ArrayList<FirstCategoryItem>();

        firstCategory_back = findViewById(R.id.firstCategory_back);
        category_done = findViewById(R.id.category_done);

        init_color = Color.parseColor("#e6e6e6");


        FirstCategory_List = new ArrayList<>();
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("아기·어린이", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("학생·청년", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("중장년·노인", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("육아·임신", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("장애인", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("문화·생활", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("다문화", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("기업·자영업자", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("법률", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("주거", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("취업·창업", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("저소득층", init_color));
        position_FirstCategory++;
        FirstCategory_List.add(position_FirstCategory, new FirstCategoryItem("기타", init_color));


        // 복지 카테고리 첫번째 리사이클러뷰 시작
        FirstCategory_recyclerview = findViewById(R.id.recycler_categoryFirst);
        FirstCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        FirstCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        FirstCategory_Adapter = new FirstCategoryAdapter(getApplicationContext(), FirstCategory_List, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object object = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) object;
                    Log.e(TAG, "클릭한 포지션 값 : " + position);
                    String c_title = ((FirstCategoryAdapter) FirstCategory_Adapter).getCategory(position).getCategoryTitle();
                    int c_bg = ((FirstCategoryAdapter) FirstCategory_Adapter).getCategory(position).getCategoryBg();

                    int select_color = Color.parseColor("#54A4FF");
                    int ready_color = Color.parseColor("#e6e6e6");
                    Log.e(TAG, "첫번째 페이지 리스트 사이즈 : " + FirstCategory_List.size());
                    // 선택한 버튼 이외에 버튼은 기본 색상을 가지는 반복문
                    for (int i = 0; i < FirstCategory_List.size(); i++)
                    {
                        if (i == 0)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("아기·어린이", ready_color));
                        }
                        else if (i == 1)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("학생·청년", ready_color));
                        }
                        else if (i == 2)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("중장년·노인", ready_color));
                        }
                        else if (i == 3)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("육아·임신", ready_color));
                        }
                        else if (i == 4)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("장애인", ready_color));
                        }
                        else if (i == 5)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("문화·생활", ready_color));
                        }
                        else if (i == 6)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("다문화", ready_color));
                        }
                        else if (i == 7)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("기업·자영업자", ready_color));
                        }
                        else if (i == 8)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("법률", ready_color));
                        }
                        else if (i == 9)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("주거", ready_color));
                        }
                        else if (i == 10)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("취업·창업", ready_color));
                        }
                        else if (i == 11)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("저소득층", ready_color));
                        }
                        else if (i == 12)
                        {
                            FirstCategory_List.set(i, new FirstCategoryItem("기타", ready_color));
                        }
                    }

                    // 선택한 버튼은 색상이 파랑으로 바뀌는 로직
                    FirstCategory_List.set(position, new FirstCategoryItem(c_title, select_color));
                    FirstCategory_Adapter.notifyDataSetChanged();

                    // 선택한 버튼과 선택하지 않은 버튼의 색상 수정이 끝난 후
                    // 사용자가 선택한 타이틀을 서버에 보내기 위해서 저장하는 변수
                    select = FirstCategory_List.get(position).getCategoryTitle();
                    Log.e(TAG, "서버에 보내기 위해서 저장하는 타이틀 : " + select);
                }
            }
        });
        FirstCategory_recyclerview.setAdapter(FirstCategory_Adapter);
        FirstCategory_recyclerview.setHasFixedSize(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // < 이미지 클릭
        firstCategory_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "< 이미지 클릭!");
                Intent f_CategoryIntent = new Intent(FirstCategory.this, MainTabLayoutActivity.class);
                startActivity(f_CategoryIntent);
                finish();
            }
        });

        // 다음단계 버튼 클릭
        category_done.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                Call<String> call = apiInterface.category1(select, "1");
                call.enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.i(TAG, "onResponse 성공 : " + response.body());
                            String category = response.body();
                            jsonParsing(category);

                        }
                        else
                        {
                            Log.i(TAG, "onResponse 실패" + response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                    {
                        Log.i(TAG, "onFailure : " + t.toString());

                    }
                });
            }
        });

    }

    private void jsonParsing(String category)
    {

        try
        {
            JSONObject jsonObject_retBody = new JSONObject(category);

            Log.i(TAG, "JSON 길이 : " + jsonObject_retBody.length());

            String retBody = jsonObject_retBody.getString("retBody");
            Log.i(TAG, "첫번째 카테고리 retBody : " + retBody);

            Intent first_intent = new Intent(FirstCategory.this, SecondCategory.class);
            first_intent.putExtra("retBody", retBody);
            first_intent.putExtra("first_select", select);
            startActivity(first_intent);
            finish();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

}
