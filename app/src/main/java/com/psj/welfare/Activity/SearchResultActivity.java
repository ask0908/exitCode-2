package com.psj.welfare.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.Adapter.HorizontalSearchResultAdapter;
import com.psj.welfare.Adapter.VerticalSearchResultAdapter;
import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.Util.LogUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

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

/* SearchFragment에서 키워드를 입력하고 검색하면 이동되는 액티비티
* 가로 리사이클러뷰에 하위 카테고리들을 중복처리한 다음 넣는다 */
public class SearchResultActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getName();

    // 가로로 하위 카테고리들을 보여줄 리사이클러뷰, 세로로 검색 결과들을 보여줄 리사이클러뷰뷰
    private RecyclerView keyword_category_recycler, search_result_title_recycler;

    // 가로 리사이클러뷰에 붙일 어댑터
    private HorizontalSearchResultAdapter category_adapter;
    private HorizontalSearchResultAdapter.ItemClickListener category_clickListener;

    // 세로 리사이클러뷰에 붙일 어댑터
    private VerticalSearchResultAdapter adapter;
    private VerticalSearchResultAdapter.VerticalItemClickListener itemClickListener;

    // parent_category를 모아둘 list
    List<SearchItem> parent_list, top_list;
    // 세로 리사이클러뷰에 넣을 혜택 이름(welf_name)을 넣을 리스트
    List<SearchItem> name_list;

    // 서버에서 받은 JSONObject 안의 값들을 담을 변수, 검색 결과값이 없을 경우 판단 시 사용
    String status;

    // 서버에서 받은 JSONArray 안의 값들을 담을 변수
    String welf_name, welf_local, parent_category, welf_category, tag;

    // 2차적으로 파싱한 JSONArray 안의 값들을 담을 변수
    String second_welf_name, second_welf_local, second_parent_category, second_welf_category, second_tag;

    // 쿼리 결과 개수를 담을 변수
    String total_count, second_total_count;

    // 쿼리 결과 개수를 보여줄 텍스트뷰
    TextView search_result_benefit_title;

    // 중복 제거에 사용할 리스트
    List<SearchItem> other_list;

    // SearchFragment에서 editText에 입력한 검색 내용. 인텐트로 담아온다
    String keyword;

    SharedPreferences sharedPreferences;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
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
            Intent intent = getIntent();
            keyword = intent.getStringExtra("search");
            searchWelfare(keyword);
        }
        parent_list = new ArrayList<>();
        other_list = new ArrayList<>();
        name_list = new ArrayList<>();
        top_list = new ArrayList<>();

        search_result_benefit_title = findViewById(R.id.search_result_benefit_title);
        keyword_category_recycler = findViewById(R.id.keyword_category_recycler);
        search_result_title_recycler = findViewById(R.id.search_result_title_recycler);

        // 가로 리사이클러뷰 처리
        keyword_category_recycler.setHasFixedSize(true);
        keyword_category_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // 세로 리사이클러뷰 처리
        search_result_title_recycler.setHasFixedSize(true);
        search_result_title_recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    /* 키워드와 일치하는 복지혜택들의 데이터를 서버에서 가져오는 메서드 */
    void searchWelfare(String keyword)
    {
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.searchWelfare(token, session, "search", keyword, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String search_result = response.body();
                    Log.e(TAG, "search_result = " + search_result);
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

    /* 서버에서 받은 키워드 검색 결과값들을 파싱하는 메서드 */
    private void jsonParsing(String search_result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(search_result);
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                /* 이 중 welf_category를 가로 리사이클러뷰에 넣어야 한다 */
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

                // 상단의 가로 리사이클러뷰(하위 카테고리 보이는곳)에 넣을 리스트에 박을 객체
                top_item.setWelf_category(welf_category);
                Log.e("fff", "top_item = " + top_item.getWelf_category());
                boolean hasDuplicate = false;
                for (int j = 0; j < top_list.size(); j++)
                {
                    // 1번이라도 중복되는 게 있으면 break로 for문 탈출
                    if (top_list.get(j).getWelf_category().equals(top_item.getWelf_category()))
                    {
                        Log.e("fff", top_list.get(j).getWelf_category());
                        hasDuplicate = true;
                        break;
                    }
                }
                // 여기선 같은 게 있어서 나온건지 하나도 없어서 나온건지 모른다
                // 그래서 boolean 변수를 통해 같은 게 있었으면 true, 없었으면 false로 설정하고 false일 때 리스트에 아이템을 추가한다
                if (!hasDuplicate)
                {
                    /* top_list 안에 ;;이 들어간 아이템을 제외하고 값을 넣는다 */
                    if (!top_item.getWelf_category().contains(";; "))
                    {
                        top_list.add(top_item);
                    }
                }

                // 하단의 세로 리사이클러뷰에 넣을 리스트
                SearchItem name_item = new SearchItem();
                name_item.setWelf_name(welf_name);
                name_item.setWelf_category(welf_category);
                name_item.setWelf_local(welf_local);
                name_list.add(name_item);
            }
            total_count = jsonObject.getString("TotalCount");
            Log.e(TAG, "TotalCount = " + total_count);
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

        // 가로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        top_list.add(0, new SearchItem("전체"));
        category_adapter = new HorizontalSearchResultAdapter(this, top_list, category_clickListener);
        category_adapter.setOnItemClickListener((view, pos) ->
        {
            String name = top_list.get(pos).getWelf_category();
            Log.e(TAG, "선택한 하위 카테고리명 = " + name);
            // 선택한 하위 카테고리에 속하는 정책들을 하단 리사이클러뷰에 표시한다
            // 이 메서드의 JSON 파싱 메서드가 호출되면, 이 파싱 메서드 안에서 해당 하위 카테고리를 상징하는 이미지를 아이템에 set한다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 'OO 지원' 클릭. 선택한 하위 카테고리 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            searchSubCategoryWelfare(name, keyword);
        });
        keyword_category_recycler.setAdapter(category_adapter);

        /* 쿼리 결과 개수로 몇 개가 검색됐는지 유저에게 알려준다 */
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
                    // 숫자가 1자리수인 경우
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 9)
                {
                    // 숫자가 2자리수인 경우
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
                else if (Integer.parseInt(total_count) > 99)
                {
                    // 숫자가 3자리수인 경우
                    SpannableString spannableString = new SpannableString("복지 혜택 결과가 총 " + total_count + "개\n검색되었습니다");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE2F43")), 12, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    search_result_benefit_title.setText(spannableString);
                }
            }
        }

        // 세로 리사이클러뷰(필터링 결과 출력)에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            // 선택한 혜택의 이름, 실시지역을 따서 인텐트로 넘겨 상세정보를 볼 수 있게 한다
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
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

    /* 가로 리사이클러뷰 클릭 리스너에서도 이 메서드를 호출해야 세로 리사이클러뷰에 쓰이는 리스트 안의 값들을 바꿀 수 있다 */
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
                    Log.e(TAG, "상단 리사이클러뷰 아이템 선택에 따른 연관 혜택 출력 = " + result);
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

    /* 상단 리사이클러뷰에서 하위 카테고리를 선택하면, 그 카테고리에 매핑된 혜택들로 하단 리사이클러뷰 내용을 수정한다 */
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

                // 하단 리사이클러뷰에 박을 모델 클래스 객체 정의 후 값 대입
                SearchItem item = new SearchItem();
                item.setWelf_name(second_welf_name);
                item.setWelf_local(second_welf_local);
                item.setParent_category(second_parent_category);
                item.setWelf_category(second_welf_category);
                item.setTag(second_tag);

                // 하단 리사이클러뷰에 쓰이는 리스트에 모델 클래스 객체 대입
                name_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // 세로 리사이클러뷰에 쓸 어댑터의 리스트에 값들을 넣는다
        adapter = new VerticalSearchResultAdapter(this, name_list, itemClickListener);
        adapter.setOnItemClickListener((((view, pos) -> {
            String name = name_list.get(pos).getWelf_name();
            String local = name_list.get(pos).getWelf_local();
            Log.e(TAG, "선택한 혜택명 = " + name);
            Log.e(TAG, "카테고리 : " + name_list.get(pos).getWelf_category());
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 검색 결과 화면에서 상세보기 화면으로 이동. 선택한 혜택 : " + name);
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 혜택 이름을 선택하면 이 이름을 갖고 액티비티를 이동해서 선택한 혜택의 상세 정보를 보여준다
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

    /* 서버로 한글 보낼 때 인코딩해서 보내야 해서 만든 한글 인코딩 메서드
     * 로그 내용을 전송할 때 한글은 반드시 아래 메서드에 넣어서 인코딩한 후 변수에 저장하고 그 변수를 서버로 전송해야 한다 */
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

}