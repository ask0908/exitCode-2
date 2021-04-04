package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.adapter.HorizontalSearchResultAdapter;
import com.psj.welfare.adapter.VerticalSearchResultAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.SearchItem;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getName();

    private RecyclerView keyword_category_recycler, search_result_title_recycler;

    private HorizontalSearchResultAdapter category_adapter;
    private HorizontalSearchResultAdapter.ItemClickListener category_clickListener;

    private VerticalSearchResultAdapter adapter;
    private VerticalSearchResultAdapter.VerticalItemClickListener itemClickListener;

    List<SearchItem> parent_list, top_list;
    List<SearchItem> name_list;

    String status;

    String welf_name, welf_local, parent_category, welf_category, tag;

    String second_welf_name, second_welf_local, second_parent_category, second_welf_category, second_tag;

    String total_count, second_total_count;

    TextView search_result_benefit_title;

    List<SearchItem> other_list;

    String keyword;

    SharedPreferences sharedPreferences;

    private FirebaseAnalytics analytics;

    boolean isConnected = false;

    String city;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(SearchResultActivity.this);
        setContentView(R.layout.activity_search_result);

        Intent intent = getIntent();
        city = intent.getStringExtra("city");

        isConnected = isNetworkConnected(this);
        if (!isConnected)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        keywordSearchLog("키워드 입력 후 검색 버튼 눌러 결과 화면으로 이동");
        Logger.addLogAdapter(new AndroidLogAdapter());

        if (getIntent().hasExtra("search"))
        {
            Intent intent1 = getIntent();
            keyword = intent1.getStringExtra("search");
            searchWelfare(keyword);
        }
        parent_list = new ArrayList<>();
        other_list = new ArrayList<>();
        name_list = new ArrayList<>();
        top_list = new ArrayList<>();

        search_result_benefit_title = findViewById(R.id.search_result_benefit_title);
        keyword_category_recycler = findViewById(R.id.keyword_category_recycler);
        search_result_title_recycler = findViewById(R.id.search_result_title_recycler);

        keyword_category_recycler.setHasFixedSize(true);
        keyword_category_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        search_result_title_recycler.setHasFixedSize(true);
        search_result_title_recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    void searchWelfare(String keyword)
    {
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchWelfare(token, session, "search", city, keyword, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String search_result = response.body();
                    jsonParsing(search_result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    private void jsonParsing(String search_result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                welf_local = inner_obj.getString("welf_local");
                parent_category = inner_obj.getString("parent_category");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");

                SearchItem item = new SearchItem();
                item.setWelf_name(welf_name);
                item.setWelf_local(welf_local);
                item.setParent_category(parent_category);
                item.setWelf_category(welf_category);
                item.setTag(tag);

                SearchItem top_item = new SearchItem();

                top_item.setWelf_category(welf_category);
                boolean hasDuplicate = false;
                for (int j = 0; j < top_list.size(); j++)
                {
                    if (top_list.get(j).getWelf_category().equals(top_item.getWelf_category()))
                    {
                        hasDuplicate = true;
                        break;
                    }
                }
                if (!hasDuplicate)
                {
                    if (!top_item.getWelf_category().contains(";; "))
                    {
                        top_list.add(top_item);
                    }
                }

                SearchItem name_item = new SearchItem();
                name_item.setWelf_name(welf_name);
                name_item.setWelf_category(welf_category);
                name_item.setWelf_local(welf_local);
                name_list.add(name_item);
            }
            total_count = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status.equals("500") || status.equals("404"))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("요청하신 검색어와 일치하는 검색 결과가 없습니다")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }

        top_list.add(0, new SearchItem("전체"));
        category_adapter = new HorizontalSearchResultAdapter(this, top_list, category_clickListener);
        category_adapter.setOnItemClickListener((view, pos) ->
        {
            String name = top_list.get(pos).getWelf_category();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 'OO 지원' 클릭. 선택한 하위 카테고리 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            searchSubCategoryWelfare(name, keyword);
        });
        keyword_category_recycler.setAdapter(category_adapter);

        if (total_count == null)
        {
            search_result_benefit_title.setText("검색 결과가 없습니다");
        }
        else
        {
            if (Integer.parseInt(total_count) != 0)
            {
                if (Integer.parseInt(total_count) < 10)
                {
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 9)
                {
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 99)
                {
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
            }
        }

        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 상세보기 화면으로 이동. 선택한 혜택 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(SearchResultActivity.this, DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

    void searchSubCategoryWelfare(String sub_category, String keyword)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        // 현물 지원, 현금 지원 등의 키워드를 받아야 하니까 상단 가로 리사이클러뷰 클릭 리스너에서 클릭 이벤트가 일어날 때마다, 아이템 안의 문자를 담아와야 한다
        Call<String> call = apiInterface.searchSubCategoryWelfare("child_category_search", sub_category, keyword, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    secondJsonParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    private void secondJsonParsing(String result)
    {
        name_list.clear();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            second_total_count = jsonObject.getString("TotalCount");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                second_welf_name = inner_obj.getString("welf_name");
                second_welf_local = inner_obj.getString("welf_local");
                second_parent_category = inner_obj.getString("parent_category");
                second_welf_category = inner_obj.getString("welf_category");
                second_tag = inner_obj.getString("tag");

                SearchItem item = new SearchItem();
                item.setWelf_name(second_welf_name);
                item.setWelf_local(second_welf_local);
                item.setParent_category(second_parent_category);
                item.setWelf_category(second_welf_category);
                item.setTag(second_tag);

                name_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        search_result_benefit_title.setText("복지혜택 결과가 총 " + second_total_count + "개\n검색되었습니다");
        if (Integer.parseInt(second_total_count) < 10)
        {
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 11, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(second_total_count) > 9)
        {
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 11, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        else if (Integer.parseInt(second_total_count) > 9)
        {
            SpannableString spannableString = new SpannableString(search_result_benefit_title.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 11, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            search_result_benefit_title.setText(spannableString);
        }
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 상세보기 화면으로 이동. 선택한 혜택 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(SearchResultActivity.this, DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", local);
            startActivity(intent);
        })));
        search_result_title_recycler.setAdapter(adapter);
    }

    void keywordSearchLog(String keyword_search_action)
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
        String action = userAction(keyword_search_action);
        Call<String> call = apiInterface.userLog(token, session, "search_result", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //
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
        keywordSearchLog("키워드 검색 결과 화면에서 뒤로가기 누름");
    }

    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected();
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
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