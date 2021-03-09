package com.psj.welfare.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;
import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.Adapter.MapResultAdapter;
import com.psj.welfare.Adapter.ResultKeywordAdapter;
import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;
import com.psj.welfare.Util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 지도 화면에서 지역 선택 시 이동해 지역별 혜택 목록을 보여주는 액티비티 */
public class MapDetailActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    TextView map_result_textview;
    EditText map_detail_search_edittext;
    // 상단 카테고리 리사이클러뷰, 하단 혜택명 나오는 리사이클러뷰
    RecyclerView result_keyword_recyclerview, map_result_recyclerview;

    /* ============================================================================== */
    // 상단 리사이클러뷰에 붙일 어댑터
    ResultKeywordAdapter adapter;
    ResultKeywordAdapter.ItemClickListener keyword_click;
    // 상단 리사이클러뷰에 쓸 리스트
    List<ResultKeywordItem> keyword_list;
    /* ============================================================================== */

    /* ============================================================================== */
    // 하단 리사이클러뷰에 붙일 어댑터
    MapResultAdapter map_adapter;
    MapResultAdapter.ItemClickListener itemClickListener;
    // 하단 리사이클러뷰에 쓸 리스트
    List<MapResultItem> item_list;
    List<MapResultItem> nameParsingList;
    /* ============================================================================== */

    // 지도 화면에서 필터를 누르면 그 필터에 해당하는 데이터만 담을 리스트
    List<MapResultItem> other_list;

    // 지도 화면에서 가져온 지역 정보를 담을 변수
    String area, welf_count;

    // 서버 통신 시 지역명을 보낼 때, 혜택 개수를 담을 때 사용하는 변수
    String number_of_benefit;

    // 서버에서 받는 JSON 값을 파싱할 때 쓸 변수
    String parent_category, welf_name, welf_category, tag;
    /* 아래는 잠시 보류 */
//    String parent_category, welf_name, tag;
//    String[] welf_category;

    // 상단 리사이클러뷰에 나오는 카테고리를 눌렀을 때 서버에서 가져오는 데이터를 파싱하기 위해 사용하는 변수
    String second_welf_name, second_parent_category, second_welf_category, second_tag, second_welf_local, second_count;
    // 위 변수들을 담을 리스트
    List<MapResultItem> second_item_list;

    // 토큰값, 유저의 지역 정보 등을 가져올 때 사용할 쉐어드
    SharedPreferences sharedPreferences;

    // 서버로 한글 로그 보내기 전 한글을 인코딩해서 저장할 때 쓰는 변수
    String encode_str;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    // 인터넷 상태 확인 후 상태 따라 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);
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
        mapSearchLog("지도 화면에서 지역 검색 화면으로 이동");
        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        keyword_list = new ArrayList<>();
        second_item_list = new ArrayList<>();
        other_list = new ArrayList<>();
        nameParsingList = new ArrayList<>();

        if (getIntent().hasExtra("area") || getIntent().hasExtra("welf_count"))
        {
            Intent intent = getIntent();
            String map_area = intent.getStringExtra("area");
            String map_welf_count = intent.getStringExtra("welf_count");
            editor.putString("map_detail_area", map_area);
            editor.putString("map_detail_welf_count", map_welf_count);
            editor.apply();
        }
        if (!sharedPreferences.getString("map_detail_area", "").equals("")
                && !sharedPreferences.getString("map_detail_welf_count", "").equals(""))
        {
            area = sharedPreferences.getString("map_detail_area", "");
            welf_count = sharedPreferences.getString("map_detail_welf_count", "");
        }
        Log.e(TAG, "받아온 지역명 = " + area + ", 혜택 개수 = " + welf_count);

        /* 서버에서 지역별 혜택 개수 받아오는 메서드 */
        getNumberOfBenefit();

        map_detail_search_edittext = findViewById(R.id.map_detail_search_edittext);
        map_detail_search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    performSearch(map_detail_search_edittext.getText().toString());
                    return true;
                }
                return false;
            }
        });

        map_result_textview = findViewById(R.id.map_result_textview);
        // 전체, 학생 등 카테고리를 가로로 보여주는 리사이클러뷰
        result_keyword_recyclerview = findViewById(R.id.result_keyword_recyclerview);
        result_keyword_recyclerview.setHasFixedSize(true);
        result_keyword_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 복지혜택 이름들을 세로로 보여주는 하단 리사이클러뷰
        map_result_recyclerview = findViewById(R.id.map_result_recyclerview);
        map_result_recyclerview.setHasFixedSize(true);
        map_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // end 값이 25인 것은 1자리 숫자기 때문에 그 숫자만 색깔을 바꾸게 하기 위한 처리다.
        if (area.equals("서울"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("인천"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("강원"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");

            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);
            Logger.e("changed_count = " + changed_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("경기"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");

            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);
            Logger.e("changed_count = " + changed_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("충남"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("충북"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("경북"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("세종"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("전북"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("전남"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("경남"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("제주"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("부산"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("대구"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("울산"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("대전"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
        if (area.equals("광주"))
        {
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
            // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
            int changed_count = Integer.parseInt(welf_count);

            // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
            if (changed_count < 10)
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
            // 10 미만이 아니라면 두자리 수 이상의 숫자기 때문에 24~26 영역만 색을 바꾸도록 한다
            else
            {
                SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                map_result_textview.setText(spannableString);
            }
        }
    }

    private void performSearch(String search)
    {
        InputMethodManager imm = (InputMethodManager) MapDetailActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(map_detail_search_edittext.getWindowToken(), 0);
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        Intent intent = new Intent(MapDetailActivity.this, SearchResultActivity.class);
        intent.putExtra("search", search);
        startActivity(intent);
    }

    /* 다른 상위 카테고리 눌렀다가 "전체" 눌렀을 때 처음 들어왔을 때와 같은 결과를 보여줘야 한다 */
    void reGetNumberOfBenefit()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String sessionId = sharedPreferences.getString("sessionId", "");
        String token = sharedPreferences.getString("token", "");
        encode("지역 검색 결과 리스트 진입");
        area = sharedPreferences.getString("map_detail_area", "");
        Call<String> call = apiInterface.getNumberOfBenefit(token, sessionId, area, "2");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "다시 전체 눌렀을 때 결과 : " + result);
                    jsonParse(result);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 지역별 혜택 개수 받아오는 메서드 */
    void getNumberOfBenefit()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String sessionId = sharedPreferences.getString("sessionId", "");
        String token = sharedPreferences.getString("token", "");
        encode("지역 검색 결과 리스트 진입");
        Call<String> call = apiInterface.getNumberOfBenefit(token, sessionId, area, "2");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    number_of_benefit = response.body();
                    Log.e(TAG, "성공 : " + number_of_benefit);
                    jsonParse(number_of_benefit);
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

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    /* 서버에서 넘어온 JSONArray 안의 값들을 파싱해서 하단 리사이클러뷰에 보여주는 메서드 */
    private void jsonParse(String number_of_benefit)
    {
        item_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(number_of_benefit);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            // "Message" 배열을 가진 JSONArray의 크기만큼 반복하며 JSONArray 안의 값들을 파싱한다
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                parent_category = inner_json.getString("parent_category");
                welf_name = inner_json.getString("welf_name");
                welf_category = inner_json.getString("welf_category");  // <- 이 값들이 중복 없이 상단 리사이클러뷰에 나오도록 해야 한다
                tag = inner_json.getString("tag");

                // 하단 리사이클러뷰에 넣을 혜택 관련 값들을 객체에 대입해서 getter로 가져와 보여줄 수 있게 한다
                MapResultItem item = new MapResultItem();
                item.setParent_category(parent_category);
                item.setWelf_name(welf_name);
                item.setWelf_category(welf_category);
                item.setKeyword_tag(tag);

                // 상단 리사이클러뷰에 넣을 키워드(welf_category)를 저장할 객체 생성
                ResultKeywordItem keywordItem = new ResultKeywordItem();
                keywordItem.setWelf_category(welf_category);
                keywordItem.setWelf_name(welf_name);

                // 상단 리사이클러뷰에 넣을 welf_category 값들에 붙어 있는 특수문자들을 파싱, 중복되지 않게 처리한다
                // 객체가 가진 값에 ';; '이 포함되어 있을 경우 ';; '을 기준으로 split()한 후, 상단 리사이클러뷰 어댑터에 넣을 리스트(keyword_list)에 값들을 넣는다
                if (keywordItem.getWelf_category().contains(";; "))
                {
                    // "현금 지원;; 현물 지원" 형태로 왔을 경우 어떻게 파싱해야 둘 다 살릴 수 있는가?
                    // 1. welf_category를 받아 변수에 저장한다
                    // 2. ';; '을 기준으로 split()해서 배열에 저장한다
                    // 3. for문으로 2번의 배열 크기만큼 반복하여 상단 리사이클러뷰 어댑터에 넣을 리스트에 값을 넣는다
                    String beforeWelfCategory = keywordItem.getWelf_category();
                    String[] category_array = beforeWelfCategory.split(";; ");
                    ResultKeywordItem items = new ResultKeywordItem();
                    for (int j = 0; j < category_array.length; j++)
                    {
                        // split()한 결과가 들어있는 배열의 크기만큼 반복하며 상단 리사이클러뷰 어댑터에 넣을 리스트에 값들을 넣는다
                        /* 현물 지원, 교육 지원만 나온다 */
//                        items.setWelf_category(category_array[j]);
//                        keyword_list.add(items);
                        items.setWelf_category(category_array[j]);
                        items.setWelf_name(keywordItem.getWelf_name());
                        keyword_list.add(items);
//                        if (!items.getWelf_category().equals(keywordItem))
//                        {
//                            keyword_list.add(items);
//                        }
                    }
                    /* 0309 11:31) 취업 지원이 나오지 않아서 원래 쓰던 if문 주석 처리함 */
//                    if (!items.getWelf_category().contains(keywordItem.getWelf_category()))
//                    {
//                        keyword_list.add(items);
//                    }
//                    Log.e(TAG, "items에 split()한 후 : " + items.getWelf_category());
                    /**/
//                    for (int j = 0; j < category_array.length; j++)
//                    {
//                        keyword_list.add(category_array[i]);
//                    }
                }
                else
                {
                    ResultKeywordItem item1 = new ResultKeywordItem();
                    item1.setWelf_category(keywordItem.getWelf_category());
                    item1.setWelf_name(keywordItem.getWelf_name());
                    if (!keyword_list.contains(item1))
                    {
                        keyword_list.add(item1);
                    }

                    boolean hasDuplicate = false;
                    for (int j = 0; j < keyword_list.size(); j++)
                    {
                        if (keyword_list.get(j).getWelf_category().equals(item1.getWelf_category()))
                        {
                            hasDuplicate = true;
                            break;
                        }
                    }
                    if (hasDuplicate)
                    {
                        if (!keyword_list.get(i).getWelf_category().equals(item1.getWelf_category()))
                        {
                            keyword_list.add(item1);
                        }
                    }
                }
                // 혜택 이름들을 보여주는 하단 리사이클러뷰의 어댑터에 넣을 List에
                // for문이 반복된 만큼 생성된 DTO 객체들을 넣는다
                item_list.add(item);
            }
        }   // try end
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* item_list는 서버에서 한 번에 받아온 OO 지역의 모든 혜택 데이터가 JSON 형태로 담겨져 있는 걸로 아는데, 아래는 그게 맞는지 확인하기 위한 로그다
        * -> item_list는 서버에서 받은 데이터 전체를 갖고 있는 리스트다. 제네릭 안에 들어간 모델 클래스는 MapResultItem이다 */

        /* 밑에서 OO 지원에 해당하는 혜택들만을 가져오게 하려는데 getWelf_name() 값을 확인한 결과 null이 나온다
        * 그래서 밑에서 split()으로 welf_category만 처리하는 게 아니라 welf_name도 StringBuilder로 구분자 섞어서 이어붙인 다음, 파싱해서 배열에 넣고
        * 배열에 들어간 welf_name들을 객체에 박아야 할 것 같다 */

        /* 상단 리사이클러뷰에 들어갈 "OO 지원"의 중복처리 로직 시작 */
        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder welfareNameBuilder = new StringBuilder();
        for (int i = 0; i < keyword_list.size(); i++)
        {
            stringBuilder.append(keyword_list.get(i).getWelf_category()).append(";;");
//            if (keyword_list.get(i).getWelf_name() != null)
//            {
//                welfareNameBuilder.append(keyword_list.get(i).getWelf_name()).append(";;");
//            }
        }
        for (int i = 0; i < item_list.size(); i++)
        {
            welfareNameBuilder.append(item_list.get(i).getWelf_name()).append(";;");
        }
//        Log.e(TAG, "welfareNameBuilder : " + welfareNameBuilder.toString());
        // ";;"가 섞여서 오는데 이걸 구분자로 split()하고, 중복되는 것 없이 keyword_list에 들어가도록 해본다
        String[] arr = stringBuilder.toString().split(";;");
        String[] nameArr = welfareNameBuilder.toString().split(";;");
//        for (int i = 0; i < nameArr.length; i++)
//        {
//            Log.e(TAG, "';;'을 기준으로 welf_name을 split()한 결과 : " + nameArr[i]);
//        }
        // split() 후 중복되는 것들을 없애는 처리를 한다
        arr = new HashSet<>(Arrays.asList(arr)).toArray(new String[0]);
        nameArr = new HashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);
//        for (int i = 0; i < nameArr.length; i++)
//        {
//            Log.e(TAG, "Arrays.asList() 한 결과 : " + nameArr[i]);
//        }
        // String[] arr 안에 들어있는 데이터 양만큼 반복문을 돌리며 setter로 welf_category를 넣기 위해 객체를 만들고, 아래 for문에서 setter로 값들을 박는다
        keyword_list.clear();
        for (int i = 0; i < arr.length; i++)
        {
            ResultKeywordItem item = new ResultKeywordItem();
            item.setWelf_category(arr[i]);
            // keyword_list에 setWelf_name()을 해서 'OO 지원'에 해당하는 혜택들을 넣어야 하는데 setter()의 인자로 뭘 넣어야 할까?
            item.setWelf_name(nameArr[i]);
            keyword_list.add(item);
            Log.e(TAG, "keyword_list getWelf_name : " + keyword_list.get(i).getWelf_name());
        }

//        map_adapter = new MapResultAdapter(MapDetailActivity.this, item_list, itemClickListener);
        for (int i = 0; i < nameArr.length; i++)
        {
            MapResultItem item = new MapResultItem();   // ResultKeywordItem : 상단 리사이클러뷰에 쓰는 모델 클래스 / MapResultItem : 하단 리사이클러뷰에 쓰는 모델 클래스
            item.setWelf_name(nameArr[i]);
            nameParsingList.add(item);  // 여기서 구분자를 뺀 모든 혜택 이름이 담기게 된다
        }

        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
        // keyword_list 크기가 0일 경우 아래에서 에러가 발생한다
        // 0309 15:06) && 연산자부터 뒤의 contains() 추가
        if (!keyword_list.get(0).getWelf_category().equals("전체") && !keyword_list.contains("전체"))
        {
            keyword_list.add(0, new ResultKeywordItem("전체"));
        }

        // 상단 리사이클러뷰 어댑터 처리
        adapter = new ResultKeywordAdapter(this, keyword_list, keyword_click);
        adapter.setOnResultKeywordClickListener((view, position) ->
        {
            if (keyword_list.get(position).getWelf_category().equals("전체"))
            {
                // 다른 카테고리를 선택해 결과를 조회한 후 다시 전체를 눌렀을 때 처음 이 화면에 들어왔을 때 봤던 검색결과를 다시 보여줘야 함
                reGetNumberOfBenefit();
                Log.e(TAG, "선택한 지역 : " + area);
            }
            else
            {
                /* 전체가 아닌 다른 필터를 선택한 경우, 여기서 선택한 'OO 지원'에 맞는 혜택들로 하단 리사이클러뷰 어댑터 초기화 시 사용되는 리스트를
                * 채워야 한다 */
                other_list.clear();
                // 상단 리사이클러뷰에서 선택한 'OO 지원' 이름을 가져온다
                String up_category = keyword_list.get(position).getWelf_category();
                Log.e(TAG, "4. 상단 리사이클러뷰에서 선택한 카테고리명 = " + up_category);   // 선택한 상단 카테고리 이름 가져오는 것 확인

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 선택한 하위 카테고리 : " + up_category);
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

                /* 선택한 OO 지원에 속하는 혜택들만을 리사이클러뷰에 보여주는 작업 처리부 */
                // 현금 지원을 선택했으면 전체 데이터가 담긴 리스트에서 welf_category가 현금 지원인 혜택들을 리스트에 담아서 하단 리사이클러뷰 어댑터에 넣어야 한다
                /**
                 * 1. 전체 데이터가 담긴 리스트를 for문으로 반복한다
                 * 2. 반복하면서 상단 리사이클러뷰에서 선택한 아이템(현금 지원)과 같은 welf_category를 가진 혜택들을 꺼내서
                 *    하단 리사이클러뷰 어댑터에 사용할 리스트(list)에 넣는다
                 * 3. list에 값들이 다 들어가게 되면 로그로 확인한 후 하단 리사이클러뷰 어댑터 초기화를 진행한다
                 */
//                List<MapResultItem> before_list = new ArrayList<>();    // 하단 리사이클러뷰 어댑터를 초기화할 때 인자로 박을 리스트
//                MapResultItem item = new MapResultItem();               // 일치하는 데이터만 가져와서 before_list에 넣기 전에 객체에 담아야 하는데 그 때 사용할 객체
//                for (int i = 0; i < keyword_list.size(); i++)
//                {
//                    // item_list에서 유저가 선택한 welf_category와 같은 welf_category인 데이터를 꺼낸다
//                    // item_list에서 getWelf_category()로 데이터를 꺼내는데, keyword_list에서 getWelf_category()로 가져온 OO 지원과 일치하는 데이터만을 가져와야 한다
//                    if (item_list.get(i).getWelf_category().equals(name))
//                    {
//                        item.setWelf_name(keyword_list.get(i).getWelf_name());
//                        item.setWelf_category(keyword_list.get(i).getWelf_category());
//                        before_list.add(item);
//                    }
//                }

                // 유저가 선택한 OO 지원에 속하는 혜택들만을 리스트에 모두 담았는지 확인하기 위한 로그
                // 지금은 필터 누른 것과 상관없이 서울 지역의 모든 혜택들을 다 가져오는데 이러면 안되고 서비스 지원을 누르면 welf_category가 서비스 지원인 혜택들만
                // 가져와서 보여줄 수 있도록 해야 한다


                // TODO : 이 안은 상단 리사이클러뷰 어댑터의 클릭 리스너 안이다
                // 전체 데이터가 들어있는 리스트(item_list)에서 선택한 welf_category(up_category)와 일치하는 welf_category를 갖는 데이터만을 따로 리스트에 넣어야 한다
                for (int i = 0; i < item_list.size(); i++)
                {
                    /* item_list에 들어있는 값들 확인 -> 모든 값들이 다 들어있는 리스트인 건 맞다 */
                    MapResultItem item = new MapResultItem();
                    if (item_list.get(i).getWelf_category().equals(keyword_list.get(position).getWelf_category()))
                    {
                        item.setWelf_category(keyword_list.get(position).getWelf_category());
                        item.setWelf_name(item_list.get(i).getWelf_name());
//                        item.setWelf_name(nameParsingList.get(position).getWelf_name());  // 이상한 거 나옴
                        other_list.add(item);
                    }
//                    if (item_list.get(i).getWelf_category().equals(up_category))
//                    {
//                        item.setWelf_category(up_category);
//                        item.setWelf_name(item_list.get(i).getWelf_name());
//                        nameParsingList.add(item);
//                    }
                }
                for (int i = 0; i < other_list.size(); i++)
                {
                    Log.e(TAG, "other_list size : " + other_list.size());
                    Log.e(TAG, "other_list getWelf_category() : " + other_list.get(i).getWelf_category());
                    Log.e(TAG, "other_list getWelf_name() : " + other_list.get(i).getWelf_name());  // <- 여기가 null이라서 이름이 보이지 않는다
                }
                map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + other_list.size() + "개입니다");
                // int로 캐스팅된 값이 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
                if (other_list.size() < 10)
                {
                    SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 25, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    map_result_textview.setText(spannableString);
                }
                else
                {
                    SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    map_result_textview.setText(spannableString);
                }

                map_adapter = new MapResultAdapter(MapDetailActivity.this, other_list, itemClickListener);
                map_result_recyclerview.setAdapter(map_adapter);


//                // 하단 리사이클러뷰에 붙일 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
//                map_adapter = new MapResultAdapter(MapDetailActivity.this, filter_list, itemClickListener);
//
//                // 아이템 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
//                map_adapter.setOnItemClickListener(((view1, position1) -> {
//                    String clicked_name = filter_list.get(position).getWelf_name();
//                    Log.e(TAG, "선택한 혜택 이름 : " + clicked_name);
//                    Bundle bundles = new Bundle();
//                    bundles.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + clicked_name + ")");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundles);
//                    Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
//                    see_detail_intent.putExtra("name", clicked_name);
//                    see_detail_intent.putExtra("welf_local", area);
//                    startActivity(see_detail_intent);
//                }));
////                map_adapter.notifyDataSetChanged();
//                map_result_recyclerview.setAdapter(map_adapter);














                // 선택한 parent_category를 보여줄 리스트를 따로 만든다(other_list)
                // other_list에 item_list 안의 데이터들을 이전한다

//                other_list.addAll(item_list);   // other_list에 모든 데이터 담겨진 것 확인
//                Log.e(TAG, "other_list 크기 : " + other_list.size());
//                // 상단 리사이클러뷰에서 선택한 parent_category가 other_list 안에서의 parent_category와 일치하는 경우에만 other_list를 채운다
//                // addAll()만 했기 때문에 모든 데이터가 다 들어가 있는데 이 중에서 선택한 parent_category로 필터링 과정을 거쳐야 한다
//                boolean isEqual = false;
//                for (int i = 0; i < other_list.size(); i++)
//                {
//                    if (!other_list.get(i).getWelf_category().equals(name))
//                    {
////                        other_list.remove(other_list.get(i).getParent_category());
//                        isEqual = true;
//                        break;
//                    }
//                }
//                if (isEqual)
//                {
//                    //
//                }

                /* 필터링 테스트2 */
//                if (isEqual)
//                {
//                    for (int i = 0; i < keyword_list.size(); i++)
//                    {
//                        if (keyword_list.get(i).getParent_category().equals(name))
//                        {
//                            ResultKeywordItem inner_item = new ResultKeywordItem();
//                            inner_item.setParent_category(parent_category);
//                            inner_item.setWelf_name(welf_name);
//                            inner_item.setWelf_category(welf_category);
//                            inner_item.setKeyword_tag(tag);
//                            other_list.add(inner_item);
//                        }
//                    }
//                }
//                for (int i = 0; i < other_list.size(); i++)
//                {
//                    Log.e(TAG, "2other_list getParent_category() = " + other_list.get(i).getParent_category());
//                    Log.e(TAG, "2other_list getWelf_name() = " + other_list.get(i).getWelf_name());
//                    Log.e(TAG, "2other_list getWelf_category() = " + other_list.get(i).getWelf_category());
//                }
//                adapter = new ResultKeywordAdapter(this, other_list, keyword_click);

                /* 필터링 테스트 */
                // other_list = 선택한 카테고리(parent_category)와 같은 카테고리에 속하는 혜택들만을 필터링해 담을 리스트
//                for (int i = 0; i < other_list.size(); i++)
//                {
//                    // 710번 줄의 name과 같은 parent_category인 데이터만을 리스트에 담는다
//                    if (other_list.get(i).getParent_category().equals(name))
//                    {
//                        // name과 일치하는 parent_category 발견 시 true로 돌리고 for문 탈출
//                        isEqual = true;
//                        break;
//                    }
//                }
                // isEqual이 true라서 for문을 탈출한 경우, break가 걸린 데이터를 리스트에 넣는다

                /* 필요없는 코드라 주석처리 */
                // 청년, 저소득층, 육아·임신 등 parent_category가 들어 있는 필터를 누르면 그 필터에 해당하는 데이터들만 리사이클러뷰에 보여야 한다
//                boolean isDuplicated = false;
//                for (int i = 0; i < item_list.size(); i++)
//                {
//                    if (item_list.get(i).getParent_category().equals(name))
//                    {
//                        // item_list를 반복하며 유저가 선택한 필터명과 같은 parent_category인 정책은 따로 리스트에 넣는다
//                        isDuplicated = true;
//                        break;
//                    }
//                }
//                if (isDuplicated)
//                {
//                    other_list.addAll(item_list);
//                    map_adapter = new MapResultAdapter(MapDetailActivity.this, other_list, itemClickListener);
//                    map_adapter.setOnItemClickListener(((view1, position1) -> {
//                        String name2 = item_list.get(position).getWelf_name();
//                        Log.e(TAG, "혜택 이름 = " + name2);
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name2 + ")");
//                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
//                        see_detail_intent.putExtra("name", name2);
//                        see_detail_intent.putExtra("welf_local", area);
//                        startActivity(see_detail_intent);
//                    }));
//                    map_result_recyclerview.setAdapter(map_adapter);
//                }
//                searchUpLevelCategory(name);  // 이 메서드를 주석 해제하면 필터별로 다른 데이터들이 보이게 되긴 하지만 내가 원하는 건 아니다
            }
        });

        // 상단 리사이클러뷰에 전체, 청년, 노인 등 문자열들을 넣는다
        result_keyword_recyclerview.setAdapter(adapter);

        /* 여기에서 로그를 찍으면 화면에 처음 들어왔을 때부터 로그가 찍힌다. 그래서 여기가 아니라 윗부분에서 처리해야 할 듯하다 */
        // item_list에 담긴 데이터들 중 유저가 선택한 parent_category에 속하는 혜택들만을 other_list에 넣어야 한다
        // item_list는 원본 데이터기 때문에 "전체"를 눌렀을 경우 보여줘야 한다. 그래서 건드리면 ㄴㄴ

        // 하단 리사이클러뷰에 붙일 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
        map_adapter = new MapResultAdapter(MapDetailActivity.this, item_list, itemClickListener);

        // 더보기 버튼 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
        map_adapter.setOnItemClickListener((view, position) ->
        {
            String name = item_list.get(position).getWelf_name();
            Log.e(TAG, "혜택 이름 = " + name);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name + ")");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
            see_detail_intent.putExtra("name", name);
            see_detail_intent.putExtra("welf_local", area);
            startActivity(see_detail_intent);
        });
        map_result_recyclerview.setAdapter(map_adapter);
    }

//    // 상단 리사이클러뷰에서 선택한 카테고리명에 따라 리사이클러뷰에 뿌리는 혜택명들을 바꾼다
//    void searchUpLevelCategory(String select_category)
//    {
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
//        String token = sharedPreferences.getString("token", "");
//        String session = sharedPreferences.getString("sessionId", "");
//        Call<String> call = apiInterface.searchWelfareCategory(token, session, "category_search", select_category, LogUtil.getUserLog());
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    String result = response.body();
//                    Log.e(TAG, "searchUpLevelCategory() 성공 : " + result);
//                    second_parsing(result);
//                }
//                else
//                {
//                    Log.e(TAG, "searchUpLevelCategory() 실패 : " + response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e(TAG, "에러 : " + t.getMessage());
//            }
//        });
//    }

    /* 카테고리를 눌러 가져온 데이터들을 파싱하는 2차 파싱 메서드 */
    private void second_parsing(String result)
    {
        item_list.clear();
        second_item_list.clear();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            second_count = jsonObject.getString("TotalCount");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner = jsonArray.getJSONObject(i);
                second_welf_name = inner.getString("welf_name");
                second_parent_category = inner.getString("parent_category");
                second_welf_category = inner.getString("welf_category");
                second_tag = inner.getString("tag");
                second_welf_local = inner.getString("welf_local");

                MapResultItem item = new MapResultItem();
                item.setWelf_category(second_welf_category);
                item.setKeyword_tag(second_tag);
                item.setParent_category(second_parent_category);
                item.setWelf_name(second_welf_name);
                item.setWelf_local(second_welf_local);
                second_item_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        adapter = new ResultKeywordAdapter(this, keyword_list, keyword_click);
        adapter.setOnResultKeywordClickListener((view, position) ->
        {
            if (keyword_list.get(position).getWelf_category().equals("전체"))
            {
                // 다른 상위 카테고리를 누른 후 다시 전체를 눌렀을 때 처음과 같은 검색 결과를 보여줘야 한다
                // 그럼 이 액티비티에 처음 들어왔을 때 검색어를 저장했다가 전체를 누르면 그 검색어를 통해 다시 서버에 요청하면 되지 않을까?
                String name = sharedPreferences.getString("map_detail_area", "");
                Log.e(TAG, "1. 상단 리사이클러뷰에서 선택한 카테고리명 = " + name);
                reGetNumberOfBenefit();
            }
            else
            {
                // 전체 말고 다른 필터를 선택했을 때
//                String name = keyword_list.get(position).getParent_category();
                String name = keyword_list.get(position).getWelf_category();
                Log.e(TAG, "2. 상단 리사이클러뷰에서 선택한 카테고리명 = " + name);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 선택한 하위 카테고리 : " + name);
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                searchUpLevelCategory(name);
            }
        });
        /* 여기서 한번 더 setAdapter()를 호출할 경우 선택된 필터의 색깔 유지가 되지 않고 첫 위치로 돌아간다. 호출하지 말자 */
//        result_keyword_recyclerview.setAdapter(adapter);

        // 하단 리사이클러뷰의 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
        map_adapter = new MapResultAdapter(MapDetailActivity.this, second_item_list, itemClickListener);
        // 더보기 버튼 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
        map_adapter.setOnItemClickListener((view, position) ->
        {
            String name = second_item_list.get(position).getWelf_name();
            String second_local = second_item_list.get(position).getWelf_local();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name + ")");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
            see_detail_intent.putExtra("name", name);
            see_detail_intent.putExtra("welf_local", second_local);
            startActivity(see_detail_intent);
        });
        map_result_recyclerview.setAdapter(map_adapter);
    }

    /* 지도 화면에서 지역 선택 후 결과 리스트 화면으로 이동한 유저 로그를 서버로 보내는 메서드 */
    void mapSearchLog(String map_action)
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
        String action = userAction(map_action);
        Call<String> call = apiInterface.userLog(token, session, "map_search", action, null, LogUtil.getUserLog());
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
        mapSearchLog("지역 검색 화면에서 지도 화면으로 뒤로가기 눌러 이동");
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