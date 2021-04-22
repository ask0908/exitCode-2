package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.adapter.CategorySearchResultAdapter;
import com.psj.welfare.adapter.SelectedCategoryAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.CategorySearchBottomResultItem;
import com.psj.welfare.data.CategorySearchResultItem;
import com.psj.welfare.data.ResultBenefitItem;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* RegionChooseActivity에서 지역 선택 후 나오는 결과 화면 */
public class RegionResultActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();

    /* 'OO 지원' 선택하는 상단 리사이클러뷰 */
    private RecyclerView up_recycler;
    private SelectedCategoryAdapter up_adapter;
    private SelectedCategoryAdapter.ItemClickListener up_itemClickListener;
    List<CategorySearchResultItem> keyword_list;

    /* 상단 리사이클러뷰에서 선택한 값에 따라 내용물이 바뀌는 하단 리사이클러뷰 */
    private RecyclerView bottom_recycler;
    private CategorySearchResultAdapter bottom_adapter;
    private CategorySearchResultAdapter.ItemClickListener bottom_itemClickListener;
    List<CategorySearchBottomResultItem> item_list;

    /* 글자 수 변경에 사용하는 리스트 */
    List<CategorySearchBottomResultItem> other_list;

    // 혜택 결과를 출력할 텍스트뷰
    TextView result_benefit_title;
    int position_RB = 1;

    private ArrayList<ResultBenefitItem> RBF_ListSet;
    ArrayList<String> favor_data;

    String welf_name, parent_category, welf_category, tag, welf_local, total_count;

    String category, last_category;
    StringBuffer sb;

    SharedPreferences sharedPreferences;

    private FirebaseAnalytics analytics;

    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_result);

        isConnected = isNetworkConnected(RegionResultActivity.this);
        if (!isConnected)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegionResultActivity.this);
            builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    }).show();
        }
        analytics = FirebaseAnalytics.getInstance(this);
        categoryChoiceLog("테마 선택 후 결과 화면으로 이동");

        result_benefit_title = findViewById(R.id.result_benefit_title);
        up_recycler = findViewById(R.id.category_recycler);
        bottom_recycler = findViewById(R.id.result_title_recycler);

        up_recycler.setHasFixedSize(true);
        up_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        bottom_recycler.setHasFixedSize(true);
        bottom_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        RBF_ListSet = new ArrayList<>();

        favor_data = new ArrayList<>();
        favor_data.clear();

        other_list = new ArrayList<>();

        Logger.addLogAdapter(new AndroidLogAdapter());

        if (getIntent().hasExtra("favor_btn"))
        {
            Intent RB_intent = getIntent();
            favor_data = RB_intent.getStringArrayListExtra("favor_btn");
            RBF_ListSet.add(0, new ResultBenefitItem(favor_data.get(0), R.drawable.rbf_btn_after));

            for (int i = 1; i < favor_data.size(); i++)
            {
                RBF_ListSet.add(position_RB, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                position_RB++;
            }

        }
        else if (getIntent().hasExtra("region_btn"))
        {
            Intent RB_intent = getIntent();
            favor_data = RB_intent.getStringArrayListExtra("region_btn");
            RBF_ListSet.add(0, new ResultBenefitItem(favor_data.get(0), R.drawable.rbf_btn_after));

            for (int i = 1; i < favor_data.size(); i++)
            {
                RBF_ListSet.add(position_RB, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                position_RB++;
            }
        }

        for (int i = 0; i < favor_data.size(); i++)
        {
            category += favor_data.get(i) + "|";
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
        }

        if (last_category.equals("서울") || last_category.equals("경기") || last_category.equals("인천") || last_category.equals("강원")
                || last_category.equals("세종") || last_category.equals("충북") || last_category.equals("충남") || last_category.equals("대전")
                || last_category.equals("대구") || last_category.equals("경북") || last_category.equals("경남") || last_category.equals("전북")
                || last_category.equals("전남") || last_category.equals("광주") || last_category.equals("부산") || last_category.equals("울산")
                || last_category.equals("제주"))
        {
            searchWelfareCategory_region(last_category);
        }
    }

    void searchWelfareCategory_region(String category)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        String token = sharedPreferences.getString("token", "");
        Call<String> call = apiInterface.getNumberOfBenefit(token, session, category, "2");
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

    private void jsonParse(String number_of_benefit)
    {
        keyword_list = new ArrayList<>();
        item_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(number_of_benefit);
            total_count = jsonObject.getString("TotalCount");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                parent_category = inner_json.getString("parent_category");
                welf_name = inner_json.getString("welf_name");
                welf_category = inner_json.getString("welf_category");
                welf_local = inner_json.getString("welf_local");
                tag = inner_json.getString("tag");

                /**
                 * CategorySearchResultItem : 상단 (이전 ResultKeywordItem)
                 * CategorySearchBottomResultItem : 하단 (이전 MapResultItem)
                 */

                // 상단 리사이클러뷰에 넣을 키워드(welf_category)를 저장할 객체 생성
                CategorySearchResultItem keywordItem = new CategorySearchResultItem();
                keywordItem.setWelf_name(welf_name);
                keywordItem.setWelf_category(welf_category);

                // 하단 리사이클러뷰에 넣을 혜택 관련 값들을 객체에 대입해서 나중에 getter로 가져와 상/하단 리사이클러뷰에서 보여줄 수 있게 한다
                CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
                item.setWelf_name(welf_name);
                item.setWelf_category(welf_category);
                item.setWelf_local(welf_local);

                // 상단 리사이클러뷰에 넣을 welf_category 값들에 붙어 있는 특수문자들을 파싱, 중복되지 않게 처리한다
                // 객체가 가진 값에 ';; '이 포함되어 있을 경우 ';; '을 기준으로 split()한 후, 상단 리사이클러뷰 어댑터에 넣을 리스트(keyword_list)에 값들을 넣는다
                if (keywordItem.getWelf_category().contains(";; "))
                {
                    // ';; ' 구분자가 포함된 welf_category 파싱 시작
                    String beforeWelfCategory = keywordItem.getWelf_category();
                    String[] category_array = beforeWelfCategory.split(";; ");
                    // 취업 지원이 보이지 않아서 작성한 로직. category_array 안의 값들을 keyword_list에 넣어서 취업 지원이 나오도록 시도
                    for (int j = 0; j < category_array.length; j++)
                    {
                        CategorySearchResultItem keyword = new CategorySearchResultItem();
                        Log.e(TAG, "category_array : " + category_array[j]);
                        keyword.setWelf_category(category_array[j]);
                        keyword_list.add(keyword);
                    }
                    // 상단 리사이클러뷰에 붙이기 위해 상단 리사이클러뷰 어댑터 초기화 시 사용하는 모델 클래스의 객체를 생성해서
                    // setter()로 welf_category, welf_name 값들을 집어넣는다
                    CategorySearchResultItem items = new CategorySearchResultItem();
                    for (int j = 0; j < category_array.length; j++) // category_array는 ';; '을 기준으로 split()한 결과물들이 담겨 있는 String[]이다
                    {
                        // split()한 결과가 들어있는 String[]의 크기만큼 반복해서 상단 리사이클러뷰 어댑터에 넣을 리스트에 값들을 setter()로 넣는다
                        items.setWelf_category(category_array[j]);
                        items.setWelf_name(keywordItem.getWelf_name());
                        // 상단 리사이클러뷰에 'OO 지원'을 보여줄 때 사용하는 리스트에 값을 넣는다
                        // keyword_list는 ResultKeywordItem 객체만 받는 ArrayList다
                        keyword_list.add(items);
                    }
                }
                else if (keywordItem.getWelf_name().contains(";; "))
                {
                    String beforeWelfName = keywordItem.getWelf_name();
                    String[] name_array = beforeWelfName.split(";; ");
                    for (int j = 0; j < name_array.length; j++)
                    {
                        CategorySearchResultItem keyword = new CategorySearchResultItem();
                        keyword.setWelf_name(name_array[j]);
                        keyword_list.add(keyword);
                    }
                    CategorySearchResultItem items = new CategorySearchResultItem();
                    for (int j = 0; j < name_array.length; j++) // name_array는 ';; '을 기준으로 split()한 결과물들이 담겨 있는 String[]이다
                    {
                        // split()한 결과가 들어있는 String[]의 크기만큼 반복해서 상단 리사이클러뷰 어댑터에 넣을 리스트에 값들을 setter()로 넣는다
                        items.setWelf_name(keywordItem.getWelf_name());
                        // 상단 리사이클러뷰에 'OO 지원'을 보여줄 때 사용하는 리스트에 값을 넣는다
                        // keyword_list는 ResultKeywordItem 객체만 받는 ArrayList다
                        keyword_list.add(items);
                    }
                }
                else
                {
                    // ';; ' 구분자가 붙어서 오지 않은 경우에는 중복되는 값이 없도록 처리한 다음 'OO 지원' 문자열들을 넣는다
                    CategorySearchResultItem item1 = new CategorySearchResultItem();
                    item1.setWelf_category(keywordItem.getWelf_category());
                    item1.setWelf_name(keywordItem.getWelf_name());

                    // 중복되는 값이 있는지 확인한 후 리스트에 add()한다
                    // 아래 코드를 없애면 기능이 제대로 작동하지 않는다
                    if (!keyword_list.contains(item1))
                    {
                        keyword_list.add(item1);
                    }
                }
                // 혜택 이름들을 보여주는 하단 리사이클러뷰의 어댑터에 넣을 List에
                // for문이 반복된 만큼 생성된 DTO 객체들을 넣는다. 이 부분이 for문의 마지막 부분이다
                item_list.add(item);
            }
        }   // try end
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        result_benefit_title.setText("혜택, 총 " + total_count + "개");
        int first_changed_count = Integer.parseInt(total_count);
        if (first_changed_count < 10)
        {
            SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimaryDark)),
                    6, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            result_benefit_title.setText(spannableString);
        }
        else
        {
            SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimaryDark)),
                    6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            result_benefit_title.setText(spannableString);
        }

        /* 상단 리사이클러뷰에 들어갈 "OO 지원"의 중복처리 로직 시작 */
        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
        StringBuilder stringBuilder = new StringBuilder();          // 상단 리사이클러뷰에 보일 'OO 지원'을 담을 StringBuilder
        StringBuilder welfareNameBuilder = new StringBuilder();     // 하단 리사이클러뷰에 보일 혜택명들을 담을 StringBuilder
        StringBuilder welfareLocalBuilder = new StringBuilder();

        /* 중복처리를 하기 위해 ';;' 구분자를 한번 더 붙인 다음 이를 파싱해서 HashSet에 넣어 중복되는 값들을 없애는 처리를 수행했다
         * 일을 2번 하는 느낌이라 이 부분은 나중에 확인한다 */
        // 상단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 'OO 지원' 사이사이에 ';;'을 붙인다
        for (int i = 0; i < keyword_list.size(); i++)
        {
            stringBuilder.append(keyword_list.get(i).getWelf_category()).append(";;");
        }

        // 하단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 혜택명 사이사이에 ';;'을 붙인다
        for (int i = 0; i < item_list.size(); i++)
        {
            welfareNameBuilder.append(item_list.get(i).getWelf_name()).append(";;");
        }

        for (int i = 0; i < item_list.size(); i++)
        {
            welfareLocalBuilder.append(item_list.get(i).getWelf_local()).append(";;");
        }

        // ";;"가 섞인 문자열 2개를 구분자로 각각 split()한다
        String[] arr = stringBuilder.toString().split(";;");
        String[] nameArr = welfareNameBuilder.toString().split(";;");
        String[] localArr = welfareLocalBuilder.toString().split(";;");

        // split() 처리 후 중복되는 것들을 없애기 위해 HashSet을 썼다
        arr = new LinkedHashSet<>(Arrays.asList(arr)).toArray(new String[0]);
        nameArr = new HashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);

        // String[] arr 안에 들어있는 데이터 양만큼 반복문을 돌리며 setter로 welf_category를 넣기 위해 객체를 만들고, 아래 for문에서 setter로 값들을 박는다
        keyword_list.clear();
        /* ResultKeywordItem : 상단 리사이클러뷰에 쓰는 모델 클래스 / MapResultItem : 하단 리사이클러뷰에 쓰는 모델 클래스 */
        for (int i = 0; i < arr.length; i++)
        {
            // setter 사용을 위한 객체 생성
            CategorySearchResultItem item = new CategorySearchResultItem();   // 상단 필터 리사이클러뷰에 사용할 모델 클래스
            item.setWelf_category(arr[i]);
            item.setWelf_name(nameArr[i]);
            item.setWelf_local(localArr[i]);
            keyword_list.add(item);
        }

        for (int i = 0; i < localArr.length; i++)
        {
            CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
            item.setWelf_local(localArr[i]);
            other_list.add(item);
        }

        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
        // keyword_list 크기가 0일 경우 아래에서 에러가 발생한다
        if (!keyword_list.get(0).getWelf_category().equals("전체") && !keyword_list.contains("전체"))
        {
            keyword_list.add(0, new CategorySearchResultItem("전체"));
        }

        // 상단 리사이클러뷰에 붙일 어댑터 초기화
        up_adapter = new SelectedCategoryAdapter(this, keyword_list, up_itemClickListener);
        up_adapter.setOnItemClickListener((view, position) ->
        {
            /* 상단 리사이클러뷰에서 전체를 클릭한 경우 */
            if (keyword_list.get(position).getWelf_category().equals("전체"))
            {
                searchWelfareCategory_region(last_category);
                result_benefit_title.setText("혜택, 총 " + total_count + "개");
                int changed_count = Integer.parseInt(total_count);
                if (changed_count < 10)
                {
                    SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 6, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    result_benefit_title.setText(spannableString);
                }
                else
                {
                    SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    result_benefit_title.setText(spannableString);
                }
            }
            else
            {
                /* 상단 리사이클러뷰에서 전체 이외의 필터를 클릭한 경우 */
                // 여기서 선택한 'OO 지원'에 맞는 혜택들로 하단 리사이클러뷰 어댑터 초기화 시 사용되는 리스트를 채워야 한다

                other_list.clear();

                String up_category = keyword_list.get(position).getWelf_category();
                Log.e(TAG, "519 - 상단 필터에서 선택한 카테고리명 : " + up_category);

                /* 선택한 OO 지원에 속하는 혜택들만을 리사이클러뷰에 보여주는 처리 시작 */
                // 현금 지원을 선택했으면 전체 데이터가 담긴 리스트에서 welf_category가 현금 지원인 혜택들을 리스트에 담아서 하단 리사이클러뷰 어댑터에 넣어야 한다
                for (int i = 0; i < item_list.size(); i++)
                {
                    CategorySearchBottomResultItem item = new CategorySearchBottomResultItem();
                    if (item_list.get(i).getWelf_category().contains(keyword_list.get(position).getWelf_category()))
                    {
                        item.setWelf_category(keyword_list.get(position).getWelf_category());
//                        item.setWelf_local(keyword_list.get(position).getWelf_local());
                        item.setWelf_local(item_list.get(i).getWelf_local());
                        item.setWelf_name(item_list.get(i).getWelf_name());
                        other_list.add(item);
                    }
                }

                result_benefit_title.setText("혜택, 총 " + other_list.size() + "개");
                if (other_list.size() < 10)
                {
                    SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 6, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    result_benefit_title.setText(spannableString);
                }
                else
                {
                    SpannableString spannableString = new SpannableString(result_benefit_title.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    result_benefit_title.setText(spannableString);
                }

                // 새 데이터가 들어있는 리스트(other_list)로 하단 리사이클러뷰의 내용물을 바꾼다
                bottom_adapter = new CategorySearchResultAdapter(this, other_list, bottom_itemClickListener);
                bottom_adapter.setOnItemClickListener((view1, pos) ->
                {
                    String name = other_list.get(pos).getWelf_name();
                    String local = other_list.get(pos).getWelf_local();

                    Log.e(TAG, "555 - 필터 바꾼 후 선택한 혜택 이름 : " + name);
                    Log.e(TAG, "556 - 필터 바꾼 후 선택한 혜택 이름의 실시 지역 : " + local);

                    Intent see_detail_intent = new Intent(RegionResultActivity.this, DetailBenefitActivity.class);
                    see_detail_intent.putExtra("name", name);
                    see_detail_intent.putExtra("welf_local", local);
                    startActivity(see_detail_intent);
                });
                bottom_recycler.setAdapter(bottom_adapter);
            }
        });

        // 상단 리사이클러뷰에 전체, 청년, 노인 등 문자열들을 넣는다
        up_recycler.setAdapter(up_adapter);

        // 하단 리사이클러뷰에 붙일 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
        // 이 부분의 하단 리사이클러뷰 어댑터 초기화는 처음 화면에 들어왔을 때 해당 지역의 전체 데이터를 보여주기 위해서 수행한다
        bottom_adapter = new CategorySearchResultAdapter(this, item_list, bottom_itemClickListener);

        /* 화면에 처음 들어온 상태에서 아이템을 누르면 이 곳이 호출된다 */
        // 하단 리사이클러뷰 아이템 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
        bottom_adapter.setOnItemClickListener((view, position) ->
        {
            String name = item_list.get(position).getWelf_name();
            String local = item_list.get(position).getWelf_local();
            Log.e(TAG, "혜택 이름 = " + name);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name + ")");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent see_detail_intent = new Intent(RegionResultActivity.this, DetailBenefitActivity.class);
            see_detail_intent.putExtra("name", name);
            see_detail_intent.putExtra("welf_local", local);
            startActivity(see_detail_intent);
        });
        bottom_recycler.setAdapter(bottom_adapter);
    }

    void categoryChoiceLog(String category_action)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token;
        if (sharedPreferences.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = sharedPreferences.getString("token", "");
        }
        String session = sharedPreferences.getString("sessionId", "");
        String action = userAction(category_action);
        Call<String> call = apiInterface.userLog(token, session, "search_result", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "검색 화면 진입 로그 전송 결과 : " + result);
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

    private String userAction(String user_action)
    {
        try
        {
            user_action = URLEncoder.encode(user_action, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return user_action;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        categoryChoiceLog("테마 키워드 화면에서 뒤로가기 누름");
    }

    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   // 핸드폰
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);       // 와이파이
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);     // 태블릿
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected(); // wimax 상태 체크
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            // 모바일 네트워크 체크
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            // wifi 네트워크 체크
            {
                return true;
            }
        }
        return false;
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }
}