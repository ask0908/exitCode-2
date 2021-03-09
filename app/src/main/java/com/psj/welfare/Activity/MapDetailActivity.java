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

        /* 인터넷 연결 체크 */
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
        // 현금 지원, 현물 지원 등 welf_category 값들을 가로로 보여주는 상단 리사이클러뷰
        result_keyword_recyclerview = findViewById(R.id.result_keyword_recyclerview);
        result_keyword_recyclerview.setHasFixedSize(true);
        result_keyword_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 복지혜택 이름들을 세로로 보여주는 하단 리사이클러뷰
        map_result_recyclerview = findViewById(R.id.map_result_recyclerview);
        map_result_recyclerview.setHasFixedSize(true);
        map_result_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // end 값을 25로 하면 아래 문장에서 1자리 숫자의 색깔을 강조처리할 수 있다(띄어쓰기 포함)
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

    /* editText에 검색어 입력 후 검색 시 호출됨. 화면을 이동해서 검색 결과들을 보여준다 */
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

    /* 서버에서 넘어온 JSONArray 안의 값들을 파싱해서 하단 리사이클러뷰에 보여주는 메서드
    * 상단 리사이클러뷰(필터)를 눌렀을 때 하단 리사이클러뷰에 값이 바뀌도록 하는 처리도 여기서 수행한다 */
    private void jsonParse(String number_of_benefit)
    {
        item_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(number_of_benefit);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                parent_category = inner_json.getString("parent_category");
                welf_name = inner_json.getString("welf_name");
                welf_category = inner_json.getString("welf_category");
                tag = inner_json.getString("tag");

                // 하단 리사이클러뷰에 넣을 혜택 관련 값들을 객체에 대입해서 나중에 getter로 가져와 상/하단 리사이클러뷰에서 보여줄 수 있게 한다
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
                    // ';; ' 구분자가 포함된 welf_category 파싱 시작
                    String beforeWelfCategory = keywordItem.getWelf_category();
                    String[] category_array = beforeWelfCategory.split(";; ");
                    // 취업 지원이 보이지 않아서 작성한 로직. category_array 안의 값들을 keyword_list에 넣어서 취업 지원이 나오도록 시도
                    for (int j = 0; j < category_array.length; j++)
                    {
                        ResultKeywordItem keyword = new ResultKeywordItem();
                        Log.e(TAG, "category_array : " + category_array[j]);
                        keyword.setWelf_category(category_array[j]);
//                        keyword.setWelf_name(keywordItem.getWelf_name());   // 취업 지원 카테고리는 나오는데 취업 지원 관련 혜택은 나오지 않아서 추가해 본 코드
                        keyword_list.add(keyword);
                    }
                    // 상단 리사이클러뷰에 붙이기 위해 상단 리사이클러뷰 어댑터 초기화 시 사용하는 모델 클래스의 객체를 생성해서
                    // setter()로 welf_category, welf_name 값들을 집어넣는다
                    ResultKeywordItem items = new ResultKeywordItem();
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
                        ResultKeywordItem keyword = new ResultKeywordItem();
                        keyword.setWelf_name(name_array[j]);
                        keyword_list.add(keyword);
                    }
                    ResultKeywordItem items = new ResultKeywordItem();
                    for (int j = 0; j < name_array.length; j++) // category_array는 ';; '을 기준으로 split()한 결과물들이 담겨 있는 String[]이다
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
                    ResultKeywordItem item1 = new ResultKeywordItem();
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

        for (int i = 0; i < keyword_list.size(); i++)
        {
            Log.e(TAG, "keyword_list - getWelf_category : " + keyword_list.get(i).getWelf_category());
            Log.e(TAG, "keyword_list - getWelf_name : " + keyword_list.get(i).getWelf_name());
        }

        /* 상단 리사이클러뷰에 들어갈 "OO 지원"의 중복처리 로직 시작 */
        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
        StringBuilder stringBuilder = new StringBuilder();          // 상단 리사이클러뷰에 보일 'OO 지원'을 담을 StringBuilder
        StringBuilder welfareNameBuilder = new StringBuilder();     // 하단 리사이클러뷰에 보일 혜택명들을 담을 StringBuilder

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
        Log.e(TAG, "welf_category : " + stringBuilder.toString());
        Log.e(TAG, "welf_name : " + welfareNameBuilder.toString());

        // ";;"가 섞인 문자열 2개를 구분자로 각각 split()한다
        String[] arr = stringBuilder.toString().split(";;");
        String[] nameArr = welfareNameBuilder.toString().split(";;");

        // split() 처리 후 중복되는 것들을 없애기 위해 HashSet을 썼다
        // 저장 순서가 중요할 것 같아서 LinkedHashSet을 써 봤는데 HashSet을 썼을 때와 딱히 큰 차이를 느끼지 못해서 좀 더 손에 익은 HashSet을 썼다
        arr = new HashSet<>(Arrays.asList(arr)).toArray(new String[0]);
        nameArr = new HashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);

        // String[] arr 안에 들어있는 데이터 양만큼 반복문을 돌리며 setter로 welf_category를 넣기 위해 객체를 만들고, 아래 for문에서 setter로 값들을 박는다
        keyword_list.clear();
        /* ResultKeywordItem : 상단 리사이클러뷰에 쓰는 모델 클래스 / MapResultItem : 하단 리사이클러뷰에 쓰는 모델 클래스 */
        for (int i = 0; i < arr.length; i++)
        {
            // setter 사용을 위한 객체 생성
            ResultKeywordItem item = new ResultKeywordItem();
            item.setWelf_category(arr[i]);
            // keyword_list에 setWelf_name()을 해서 'OO 지원'에 해당하는 혜택들을 넣어야 하는데 setter()의 인자로 뭘 넣어야 할까?
            item.setWelf_name(nameArr[i]);
            keyword_list.add(item);
        }

        for (int i = 0; i < nameArr.length; i++)
        {
            MapResultItem item = new MapResultItem();
            item.setWelf_name(nameArr[i]);
        }

        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
        // keyword_list 크기가 0일 경우 아래에서 에러가 발생한다
        if (!keyword_list.get(0).getWelf_category().equals("전체") && !keyword_list.contains("전체"))
        {
            keyword_list.add(0, new ResultKeywordItem("전체"));
        }

        // 상단 리사이클러뷰에 붙일 어댑터 초기화
        adapter = new ResultKeywordAdapter(this, keyword_list, keyword_click);
        adapter.setOnResultKeywordClickListener((view, position) ->
        {
            /* 상단 리사이클러뷰에서 전체를 클릭한 경우 */
            if (keyword_list.get(position).getWelf_category().equals("전체"))
            {
                map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + welf_count + "개입니다");
                // welf_count가 String이라서 부등호가 안 먹히기 때문에 int로 캐스팅한다
                int changed_count = Integer.parseInt(welf_count);

                // int로 캐스팅된 값이 10 미만이라면 24~25 영역(숫자 1자리)만 색을 바꾼다
                if (changed_count < 10)
                {
                    SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    map_result_textview.setText(spannableString);
                }
                else
                {
                    // 10 이상이라면 24~26 영역(숫자 2자리)만 색을 바꾼다
                    SpannableString spannableString = new SpannableString(map_result_textview.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 24, 26, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    map_result_textview.setText(spannableString);
                }
                // 다른 카테고리를 선택해 결과를 조회한 후 다시 전체를 눌렀을 때 처음 이 화면에 들어왔을 때 봤던 검색결과를 다시 보여줘야 한다
                /* 서버에서 지역별 혜택 개수 받아오는 메서드 */
                getNumberOfBenefit();
                Log.e(TAG, "선택한 지역 : " + area);
                Log.e(TAG, "전체 클릭");
            }
            else
            {
                /* 상단 리사이클러뷰에서 전체 이외의 필터를 클릭한 경우 */
                // 여기서 선택한 'OO 지원'에 맞는 혜택들로 하단 리사이클러뷰 어댑터 초기화 시 사용되는 리스트를 채워야 한다

                // 찌꺼기 값이 없도록 clear()로 리스트를 한 차례 비우고 새로 데이터를 넣을 준비를 한다
                other_list.clear();

                // 상단 리사이클러뷰에서 선택한 'OO 지원' 이름을 가져온다
                String up_category = keyword_list.get(position).getWelf_category();
                Log.e(TAG, "4. 상단 리사이클러뷰에서 선택한 카테고리명 = " + up_category);   // 선택한 상단 카테고리 이름 가져오는 것 확인용 로그

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 선택한 하위 카테고리 : " + up_category);
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

                /* 선택한 OO 지원에 속하는 혜택들만을 리사이클러뷰에 보여주는 처리 시작 */
                // 현금 지원을 선택했으면 전체 데이터가 담긴 리스트에서 welf_category가 현금 지원인 혜택들을 리스트에 담아서 하단 리사이클러뷰 어댑터에 넣어야 한다
                /**
                 * 1. 전체 데이터가 담긴 리스트를 for문으로 반복한다
                 * 2. 반복하면서 상단 리사이클러뷰에서 선택한 아이템(현금 지원)과 같은 welf_category를 가진 혜택들을 꺼내서
                 *    하단 리사이클러뷰 어댑터에 사용할 리스트(list)에 넣는다
                 * 3. list에 값들이 다 들어가게 되면 로그로 확인한 후 하단 리사이클러뷰 어댑터 초기화를 진행한다
                 */

                // 전체 데이터가 들어있는 리스트(item_list)에서 선택한 welf_category(up_category)와 일치하는 welf_category를 갖는 데이터만을 따로 리스트에 넣어야 한다
                for (int i = 0; i < item_list.size(); i++)
                {
                    /* item_list -> 서버에서 받은 모든 값들이 다 들어있는 리스트 */
                    MapResultItem item = new MapResultItem();
                    // 상단 리사이클러뷰에서 선택한 'OO 지원'이 하단 리사이클러뷰에서 쓰이는 리스트에서 획득한 'OO 지원'과 같으면
                    // other_list(하단 리사이클러뷰에 새로 집어넣을 값들을 가질 리스트)에 넣는다
                    if (item_list.get(i).getWelf_category().equals(keyword_list.get(position).getWelf_category()))
                    {
                        item.setWelf_category(keyword_list.get(position).getWelf_category());
                        item.setWelf_name(item_list.get(i).getWelf_name());
                        other_list.add(item);
                    }
                }

                // other_list의 크기는 필터를 누른 결과 보여지는 혜택들의 개수이므로 이 개수만큼 텍스트뷰 숫자를 바꾼다
                map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 " + other_list.size() + "개입니다");
                // 결과수가 10 미만이라면 24~25 영역만 색을 바꾸도록 한다
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

                // 새 데이터가 들어있는 리스트(other_list)로 하단 리사이클러뷰의 내용물을 바꾼다
                map_adapter = new MapResultAdapter(MapDetailActivity.this, other_list, itemClickListener);

                // 클릭 리스너도 새로 정의해서 혜택 선택 시 상세보기 페이지로 이동하도록 한다
                /* 클릭 리스너가 안 먹힌다 */
                map_adapter.setOnItemClickListener(((v, pos) -> {
                    String name = other_list.get(pos).getWelf_name();
                    Log.e(TAG, "혜택 이름 = " + name);
                    Bundle second_bundle = new Bundle();
                    second_bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name + ")");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, second_bundle);
                    Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
                    see_detail_intent.putExtra("name", name);
                    see_detail_intent.putExtra("welf_local", area);
                    startActivity(see_detail_intent);
                }));

                // 클릭 리스너를 정의한 어댑터를 하단 리사이클러뷰에 붙여서 업데이트된 데이터들을 유저에게 보여준다
                map_result_recyclerview.setAdapter(map_adapter);
            }
        });

        // 상단 리사이클러뷰에 전체, 청년, 노인 등 문자열들을 넣는다
        result_keyword_recyclerview.setAdapter(adapter);

        // 하단 리사이클러뷰에 붙일 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
        // 이 부분의 하단 리사이클러뷰 어댑터 초기화는 처음 화면에 들어왔을 때 해당 지역의 전체 데이터를 보여주기 위해서 수행한다
        map_adapter = new MapResultAdapter(MapDetailActivity.this, item_list, itemClickListener);

        // 하단 리사이클러뷰 아이템 클릭 시 해당 혜택의 상세보기 화면으로 이동한다
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

    /* 네트워크 연결을 체크하는 메서드 */
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