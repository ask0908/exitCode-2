package com.psj.welfare.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.Data.ResultBenefitItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.CategorySearchResultAdapter;
import com.psj.welfare.adapter.SelectedCategoryAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* SearchFragment에서 관심사 선택 후 보여주는 조회 결과 화면 */
public class ResultBenefitActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();

    // 상단 리사이클러뷰, 하단 리사이클러뷰
    private RecyclerView up_recycler, bottom_recycler;
    // 상단 리사이클러뷰에 붙일 어댑터
    private SelectedCategoryAdapter up_adapter;
    private SelectedCategoryAdapter.ItemClickListener up_itemClickListener;
    // 하단 리사이클러뷰에 붙일 어댑터
    private CategorySearchResultAdapter bottom_adapter;
    private CategorySearchResultAdapter.ItemClickListener bottom_itemClickListener;
    List<CategorySearchResultItem> list;

    // 하위 카테고리를 담을 리스트
    List<CategorySearchResultItem> top_list;

    // 세로 리사이클러뷰에 넣을 혜택 이름(welf_name)을 넣을 리스트
    List<CategorySearchResultItem> name_list;

//    private RecyclerView.Adapter RbfBtn_Adapter, RbfTitle_Adapter;

    TextView result_benefit_title; // 혜택 결과 개수 타이틀
    int position_RB = 1; // 관심사 버튼 넘버

    private ArrayList<ResultBenefitItem> RBF_ListSet;       // 관심사 버튼 리스트
    private ArrayList<ResultBenefitItem> RBFTitle_ListSet;  // 복지혜택 이름을 리사이클러뷰에 보여줄 때 해당 리사이클러뷰의 어댑터에 사용할 리스트
    ArrayList<String> favor_data; // 관심사 버튼 문자열

    // 검색 api 리뉴얼로 추가한 변수
    String welf_name, parent_category, welf_category, tag, welf_local, total_count;

    String category, last_category;
    StringBuffer sb;

    // 기존 변수와 구분하기 위해 SearchResultActivity에서 사용된 변수 가져옴
    String second_welf_name, second_parent_category, second_welf_category, second_tag, second_welf_local, second_total_count;

    List<CategorySearchResultItem> test_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultbenefit);

        result_benefit_title = findViewById(R.id.result_benefit_title);
        up_recycler = findViewById(R.id.category_recycler);           // 위의 가로 리사이클러뷰(카테고리 이름들 출력)
        bottom_recycler = findViewById(R.id.result_title_recycler);   // 아래의 세로 리사이클러뷰(혜택 이름들 출력)

        /* 상단 리사이클러뷰(하위 카테고리들 나오는 리사이클러뷰) 처리 */
        up_recycler.setHasFixedSize(true);
        up_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        /* 하단 리사이클러뷰(혜택들 제목 나오는 리사이클러뷰) 처리 */
        bottom_recycler.setHasFixedSize(true);
        bottom_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        RBF_ListSet = new ArrayList<>();
        RBFTitle_ListSet = new ArrayList<>();

        favor_data = new ArrayList<>();
        favor_data.clear();

        list = new ArrayList<>();
        top_list = new ArrayList<>();
        name_list = new ArrayList<>();

        Logger.addLogAdapter(new AndroidLogAdapter());

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
    }

    /* 선택한 카테고리에 속하는 정책들의 정보들을 가져와 뷰에 set하는 메서드 (혜택 상위 카테고리 검색) */
    void searchWelfareCategory(String category)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "검색 키워드 = " + category);
        Call<String> call = apiInterface.searchWelfareCategory("category_search", category, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String category_result = response.body();
                    Log.e(TAG, "category_result = " + category_result);
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

    private void jsonParse(String category_result)
    {
        test_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(category_result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                parent_category = inner_obj.getString("parent_category");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");
                welf_local = inner_obj.getString("welf_local");

                // 하단 리사이클러뷰에 박을 데이터들
                CategorySearchResultItem item = new CategorySearchResultItem();
                item.setWelf_name(welf_name);
                item.setParent_category(parent_category);
                item.setWelf_category(welf_category);
                item.setTag(tag);
                item.setWelf_local(welf_local);
                list.add(item);

                // 하위 카테고리만 따로 담아서 중복 체크한 뒤 리사이클러뷰에 박는다
                CategorySearchResultItem top_item = new CategorySearchResultItem();
                top_item.setWelf_category(welf_category);

                Log.e("jsonParse()", top_item.getWelf_category());
                boolean hasDuplicate = false;   // for-loop 안에서 하위 카테고리 이름 중복 여부 확인 시 사용
                for (int j = 0; j < top_list.size(); j++)
                {
                    if (top_list.get(j).getWelf_category().equals(top_item.getWelf_category()))
                    {
                        // 추가하지 않음
                        // 1개라도 중복되는 게 있으면 브레이크로 빠져나옴
                        hasDuplicate = true;
                        break;
                    }
                }
                // if문 조건이 false인 경우는 중복되는 게 없다는 뜻이니까 리스트에 add로 값을 추가한다
                if (!hasDuplicate)
                {
                    /* top_list 안에 ;;이 들어간 아이템을 제외하고 값을 넣는다 */
                    if (!top_item.getWelf_category().contains(";; "))
                    {
                        top_list.add(top_item);
                    }
                }
            }
            total_count = jsonObject.getString("TotalCount");
            Log.e(TAG, "숫자 = " + total_count);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        up_adapter = new SelectedCategoryAdapter(ResultBenefitActivity.this, top_list, up_itemClickListener);
        up_adapter.setOnItemClickListener((view, position) ->
        {
            String name = top_list.get(position).getWelf_category();
            Log.e(TAG, "상단 리사이클러뷰에서 선택한 아이템 이름 = " + name);
            searchUpLevelCategory(name);
        });

        // 쿼리 결과 개수로 몇 개가 검색됐는지 유저에게 알려준다
        result_benefit_title.setText("복지혜택 결과가 총 " + total_count + "개\n검색되었습니다");
        if (Integer.parseInt(total_count) > 9)
        {
            SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 11, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(total_count) < 10)
        {
            SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 11, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            result_benefit_title.setText(spannableString);
        }
        bottom_adapter = new CategorySearchResultAdapter(this, list, bottom_itemClickListener);
        bottom_recycler.setAdapter(bottom_adapter);
        up_recycler.setAdapter(up_adapter);
    }

    /* 하위 카테고리 눌러 검색할 때 사용하는 메서드 */
    void searchUpLevelCategory(String sub_category)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchUpLevelCategory("child_category_search", last_category, sub_category, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 : " + result);
                    categoryParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 상단 리사이클러뷰에서 하위 카테고리를 선택하면, 그 카테고리에 매핑된 혜택들로 하단 리사이클러뷰 내용을 수정한다 */
    private void categoryParsing(String result)
    {
        list.clear();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                second_welf_name = inner_obj.getString("welf_name");
                second_welf_local = inner_obj.getString("welf_local");
                second_parent_category = inner_obj.getString("parent_category");
                second_welf_category = inner_obj.getString("welf_category");
                second_tag = inner_obj.getString("tag");

                // 하단 리사이클러뷰에 박을 모델 클래스 객체 정의 후 값 대입
                CategorySearchResultItem item = new CategorySearchResultItem();
                item.setWelf_name(second_welf_name);
                item.setWelf_local(second_welf_local);
                item.setParent_category(second_parent_category);
                item.setWelf_category(second_welf_category);
                item.setTag(second_tag);

                // 하단 리사이클러뷰에 쓰이는 리스트에 모델 클래스 객체 대입
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        bottom_adapter = new CategorySearchResultAdapter(this, list, bottom_itemClickListener);
        bottom_adapter.setOnItemClickListener(new CategorySearchResultAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int pos)
            {
                String name = list.get(pos).getWelf_name();
                Log.e(TAG, "선택한 혜택명 = " + name);
            }
        });
        bottom_recycler.setAdapter(bottom_adapter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        favor_data.clear();
        list.clear();
        top_list.clear();
    }
}
