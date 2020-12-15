package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.Data.ResultBenefitItem;
import com.psj.welfare.Data.SelectedButtonItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.CategorySearchResultAdapter;
import com.psj.welfare.adapter.RBFAdapter;
import com.psj.welfare.adapter.RBFTitleAdapter;
import com.psj.welfare.adapter.SelectedCategoryAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 메인 페이지에서 관심사 선택 후 보여주는 혜택 결과 페이지는 양이 많은 결과 데이터를 사용자에게 직관성있게 보여주어야 한다
 * 그리고 사용자가 맞춤 혜택을 찾을 수 있는 흐름도 보여주어야 한다
 * */
public class ResultBenefitActivity extends AppCompatActivity
{
    public static final String TAG = "ResultBenefitActivity";

    // 리사이클러뷰 객체 선언
    private RecyclerView category_recycler, result_title_recycler;
    private CategorySearchResultAdapter adapter;
    private CategorySearchResultAdapter.ItemClickListener itemClickListener;
    private SelectedCategoryAdapter selectedCategoryAdapter;
    private SelectedCategoryAdapter.ItemClickListener selected_itemClickListener;
    List<SelectedButtonItem> selected_list;
    List<CategorySearchResultItem> list;

    private RecyclerView.Adapter RbfBtn_Adapter, RbfTitle_Adapter;

    // 유저가 선택한 카테고리에 속하는 모든 정책 개수를 담을 변수
    String countOfWelf;


    TextView result_benefit_title; // 혜택 결과 개수 타이틀
    ImageView RBF_back; // 뒤로가기 버튼 이미지
    int position_RB = 1; // 관심사 버튼 넘버
    int position_RBT = 0; // 관심사 타이틀 넘버

    private ArrayList<ResultBenefitItem> RBF_ListSet;       // 관심사 버튼 리스트
    private ArrayList<ResultBenefitItem> RBFTitle_ListSet;  // 복지혜택 이름을 리사이클러뷰에 보여줄 때 해당 리사이클러뷰의 어댑터에 사용할 리스트
    ArrayList<String> favor_data; // 관심사 버튼 문자열

    // 서버에서 응답 받은 JSON 구조를 파싱하기 위한 JSONArray 변수들
    JSONArray child, student, law, old, pregnancy, disorder, cultural, multicultural, company, living, job, homeless, etc;

    // 검색 api 리뉴얼로 추가한 변수
    String welf_name, welf_category, tag, welf_local;

    String category, last_category;
    StringBuffer sb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultbenefit);

        list = new ArrayList<>();

        Logger.addLogAdapter(new AndroidLogAdapter());

        result_benefit_title = findViewById(R.id.result_benefit_title);
        RBF_back = findViewById(R.id.RBF_back);
        category_recycler = findViewById(R.id.category_recycler);           // 위의 가로 리사이클러뷰(카테고리 이름들 출력)
        result_title_recycler = findViewById(R.id.result_title_recycler);   // 아래의 세로 리사이클러뷰(혜택 이름들 출력)

        RBF_ListSet = new ArrayList<>();
        RBFTitle_ListSet = new ArrayList<>();

        // 메인에서 전달받은 인텐트를 검사하는 곳
        // 선택한 관심사를 현재 페이지에서 버튼으로 표현하기 위한 데이터
        if (getIntent().hasExtra("favor_btn"))
        {
            Intent RB_intent = getIntent();
            favor_data = RB_intent.getStringArrayListExtra("favor_btn");
            RBF_ListSet.add(0, new ResultBenefitItem(favor_data.get(0), R.drawable.rbf_btn_after));
            Log.e(TAG, "리스트 형태 관심사 정보 -> " + favor_data.toString());

            // 메인에서 전달받은 리스트의 1번 요소를 제외한 나머지 정보는 포커싱 상태를 해제
            for (int i = 1; i < favor_data.size(); i++)
            {
                Log.e(TAG, "리스트 형태 관심사 버튼 반복문 -> " + favor_data.get(i));
                RBF_ListSet.add(position_RB, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                Log.e(TAG, "for문 안 position_RB = " + position_RB);
                position_RB++;
            }

        }
        else
        {
            Log.e(TAG, "전달 받은 인텐트 값 없어요!");
        }

        /* MainFragment에서 유저가 선택한 카테고리들에 구분자를 붙여서 변수에 저장한다 */
        for (int i = 0; i < favor_data.size(); i++)
        {
            category += favor_data.get(i) + "|";   // api 내용 수정으로 ,에서 |로 구분자 변경
            Log.e(TAG, "for문 안 favor_data = " + category);
            if (category.contains("null전체|"))
            {
                category = category.replace("null전체|", "");
            }
        }
        if (category.length() > 0)
        {
            sb = new StringBuffer(category);
            sb.deleteCharAt(category.length() - 1);
            last_category = sb.toString();
            Log.e(TAG, "sb = " + sb);
        }
        Log.e(TAG, "리스트값 스트링으로 변환 -> " + last_category);
        // "null전체|" 문자열을 삭제한 문자열로 DB에서 데이터를 검색
        searchWelfareCategory(last_category);

        category_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selected_list = new ArrayList<>();
        selectedCategoryAdapter = new SelectedCategoryAdapter(ResultBenefitActivity.this, selected_list, selected_itemClickListener);
        selectedCategoryAdapter.setOnItemClickListener(((view, position) -> {
            String name = selected_list.get(position).getButton_title();
            Log.e(TAG, "이름 출력 테스트 = " + name);
        }));
        category_recycler.setAdapter(selectedCategoryAdapter);
        /* 상단 리사이클러뷰(유저가 선택한 카테고리들 나오는 리사이클러뷰) */
        RbfBtn_Adapter = new RBFAdapter(ResultBenefitActivity.this, RBF_ListSet, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object obj = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) obj;
//                    Log.e(TAG, "관심사 버튼 클릭 " + position);
                    int btnColor = ((RBFAdapter) RbfBtn_Adapter).getRBF(position).getRBF_btnColor();
//                    Log.e(TAG, "내가 선택한 버튼 색상 : " + btnColor);
//                    Log.e(TAG, "비교할 버튼 새상 : " + R.drawable.btn_done);

                    // 선택하지 않았던 버튼을 선택할 경우 색을 바꾸고
                    if (btnColor != R.drawable.rbf_btn_after)
                    {
                        for (int i = 0; i < favor_data.size(); i++)
                        {
                            Log.e(TAG, "favor_data = " + favor_data.get(i));
                        }
//                        Log.e(TAG, "선택하지 않은 버튼 입니다!");
                        for (int i = 0; i < favor_data.size(); i++)
                        {
                            RBF_ListSet.set(i, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                        }
                        RBF_ListSet.set(position, new ResultBenefitItem(favor_data.get(position), R.drawable.rbf_btn_after));
                        RbfBtn_Adapter.notifyItemRangeChanged(0, favor_data.size());

                        if (favor_data.get(position).equals("전체"))
                        {
                            Log.e(TAG, "데이터 확인 = " + favor_data.get(position));
                            Log.e(TAG, "전체 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            Log.e(TAG, "전체 눌렀을 때 검색할 키워드 = " + sb);
                            searchWelfareCategory(last_category);
                        }
                        else if (favor_data.get(position).equals("취업·창업"))
                        {
                            Log.e(TAG, "취업·창업 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("취업·창업");
                        }
                        else if (favor_data.get(position).equals("학생·청년"))
                        {
                            Log.e(TAG, "학생·청년 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("청년");
                        }
                        else if (favor_data.get(position).equals("주거"))
                        {
                            Log.e(TAG, "주거 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("주거");
                        }
                        else if (favor_data.get(position).equals("육아·임신"))
                        {
                            Log.e(TAG, "육아·임신 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("육아·임신");
                        }
                        else if (favor_data.get(position).equals("아기·어린이"))
                        {
                            Log.e(TAG, "아기·어린이 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("아기·어린이");
                        }
                        else if (favor_data.get(position).equals("문화·생활"))
                        {
                            Log.e(TAG, "문화·생활 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("문화·생활");
                        }
                        else if (favor_data.get(position).equals("기업·자영업자"))
                        {
                            Log.e(TAG, "기업·자영업자 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("기업·자영업자");
                        }
                        else if (favor_data.get(position).equals("저소득층"))
                        {
                            Log.e(TAG, "저소득층 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("저소득층");
                        }
                        else if (favor_data.get(position).equals("중장년·노인"))
                        {
                            Log.e(TAG, "중장년·노인 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("중장년·노인");
                        }
                        else if (favor_data.get(position).equals("장애인"))
                        {
                            Log.e(TAG, "장애인 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("장애인");
                        }
                        else if (favor_data.get(position).equals("다문화"))
                        {
                            Log.e(TAG, "다문화 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("다문화");
                        }
                        else if (favor_data.get(position).equals("법률"))
                        {
                            Log.e(TAG, "법률 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("법률");
                        }
                        else if (favor_data.get(position).equals("기타"))
                        {
                            Log.e(TAG, "기타 클릭");
                            RbfBtn_Adapter.notifyDataSetChanged();
                            list.clear();
                            searchWelfareCategory("기타");
                        }

                    }
                    else
                    {
                        Log.e(TAG, "선택한 버튼입니다!");
                    }
                }
            }
        });
        category_recycler.setAdapter(RbfBtn_Adapter);
        category_recycler.setHasFixedSize(true);

        /* 하단 리사이클러뷰(혜택들 제목 나오는 리사이클러뷰) */
        result_title_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RbfTitle_Adapter = new RBFTitleAdapter(ResultBenefitActivity.this, RBFTitle_ListSet, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object obj = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) obj;
                    Log.e(TAG, "관심사 버튼을 클릭 했어요 -> " + position);
                    String title = ((RBFTitleAdapter) RbfTitle_Adapter).getRBFTitle(position).getRBF_Title();

                    Intent RBF_intent = new Intent(ResultBenefitActivity.this, DetailBenefitActivity.class);
                    RBF_intent.putExtra("RBF_title", title);
                    startActivity(RBF_intent);
                    finish();
                }
            }
        });
        result_title_recycler.setAdapter(RbfTitle_Adapter);
        result_title_recycler.setHasFixedSize(true);
    }

    /* 선택한 정책의 정보들을 가져와 뷰에 set하는 메서드 */
    void searchWelfareCategory(String category)
    {
        Log.e(TAG, "searchWelf() 호출됨");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "검색 키워드 = " + category);
        Call<String> call = apiInterface.searchWelfareCategory("category_search", category);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String category_result = response.body();
                    jsonParse(category_result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 받은 JSON 값들을 파싱해 리스트에 담고, 이걸 리사이클러뷰 어댑터에 추가하는 메서드 */
    private void jsonParse(String category_result)
    {
        Log.e(TAG, "jsonParse() 호출됨");
        try
        {
            JSONObject jsonObject = new JSONObject(category_result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");
                welf_local = inner_obj.getString("welf_local");
                CategorySearchResultItem item = new CategorySearchResultItem();
                item.setWelf_name(welf_name);
                item.setWelf_category(welf_category);
                item.setTag(tag);
                item.setWelf_local(welf_local);
                list.add(item);
            }
            countOfWelf = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        adapter = new CategorySearchResultAdapter(ResultBenefitActivity.this, list, itemClickListener);
        adapter.setOnItemClickListener(((view, position) -> {
            String name = list.get(position).getWelf_name();
            Log.e(TAG, "선택한 이름 : " + name);
            Intent intent = new Intent(ResultBenefitActivity.this, DetailBenefitActivity.class);
            intent.putExtra("RBF_title", name);
            startActivity(intent);
        }));
        Log.e(TAG, "Totalcount 값 확인 = " + countOfWelf);
        result_title_recycler.setAdapter(adapter);
        result_benefit_title.setText("당신이 놓치고 있는 혜택,\n총 " + countOfWelf + "개 입니다");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        RBF_back.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent RBF_intent = new Intent(ResultBenefitActivity.this, MainTabLayoutActivity.class);
                startActivity(RBF_intent);
                finish();
            }
        });
    }

}
