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

import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.Adapter.MapResultAdapter;
import com.psj.welfare.Adapter.ResultKeywordAdapter;
import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;
import com.psj.welfare.Util.LogUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
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
            map_result_textview.setText("당신이 놓치고 있는 " + area + " 지역의 혜택은\n총 "+ welf_count + "개입니다");
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
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                parent_category = inner_json.getString("parent_category");
                welf_name = inner_json.getString("welf_name");
                welf_category = inner_json.getString("welf_category");
                tag = inner_json.getString("tag");

                // 하단 리사이클러뷰에 넣을 혜택명들을 저장할 객체에 값 대입
                MapResultItem item = new MapResultItem();
                item.setParent_category(parent_category);
                item.setWelf_name(welf_name);
                item.setWelf_category(welf_category);
                item.setKeyword_tag(tag);

                // 상단 리사이클러뷰에 넣을 키워드를 저장할 객체 생성
                ResultKeywordItem keywordItem = new ResultKeywordItem();
                keywordItem.setParent_category(parent_category);

                /* 키워드들을 보여주는 상단 리사이클러뷰의 어댑터에 쓰일 리스트에 객체들을 넣는다
                * 넣기 전 중복되는 값들을 빼고 넣는다(SearchResultActivity 163번 줄부터 같은 로직 있음) */
                // 중복 여부를 if문으로 확인할 때 사용할 변수
                boolean isDuplicate = false;
                for (int j = 0; j < keyword_list.size(); j++)
                {
                    if (keyword_list.get(j).getParent_category().equals(keywordItem.getParent_category()))
                    {
                        isDuplicate = true;
                        break;
                    }
                }
                // 여기선 같은 게 있어서 for문을 나온건지, 하나도 없어서 나온건지 알 수 없다
                // 그래서 boolean 변수를 통해 같은 게 있었으면 true, 없었으면 false로 설정하고 false일 때 리스트에 아이템을 추가한다
                if (!isDuplicate)
                {
                    keyword_list.add(keywordItem);
                    /* 아래 조건은 무조건 필요한 게 아니다. for문이 끝난 후 boolean 변수가 true일 경우에만 리스트에 추가하도록 해서도
                    * 중복되지 않는 값을 넣을 수 있다 */
//                    if (!keywordItem.getParent_category().equals(keywordItem.getParent_category()))
//                    {
//                        keyword_list.add(keywordItem);
//                    }
                }

                // 혜택 이름들을 보여주는 하단 리사이클러뷰의 어댑터에 쓰일 List에 for문이 반복된 만큼 생성된 DTO 객체들을 넣는다
                item_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        /* 여기서 keyword_list 안의 값들을 쉐어드에 저장한 다음, 전체를 누르면 쉐어드의 값을 가져와서 그걸 토대로 검색할 수 있도록 해보자 */
        // ArrayList 안의 데이터를 String으로 바꾼다
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < keyword_list.size(); i++)
        {
            stringBuilder.append(keyword_list.get(i).getParent_category()).append("|");
        }

        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
        if (!keyword_list.get(0).getParent_category().equals("전체"))
        {
            keyword_list.add(0, new ResultKeywordItem("전체"));
        }

        // 상단 리사이클러뷰 어댑터 처리
        adapter = new ResultKeywordAdapter(this, keyword_list, keyword_click);
        adapter.setOnResultKeywordClickListener((view, position) ->
        {
            if (keyword_list.get(position).getParent_category().equals("전체"))
            {
                // 다른 카테고리를 선택해 결과를 조회한 후 다시 전체를 눌렀을 때 처음 이 화면에 들어왔을 때 봤던 검색결과를 다시 보여줘야 함
                reGetNumberOfBenefit();
                Log.e(TAG, "선택한 지역 : " + area);
//                for (int i = 0; i < item_list.size(); i++)
//                {
//                    Log.e(TAG, "지역에서 파싱 후 item_list getWelf_name() : " + item_list.get(i).getWelf_name());
//                    Log.e(TAG, "지역에서 파싱 후 item_list getParent_category() : " + item_list.get(i).getParent_category());
//                    Log.e(TAG, "지역에서 파싱 후 item_list getWelf_category() : " + item_list.get(i).getWelf_category());
//                    // 여기서 어떻게 해야?
//                    // 1. 모든 데이터를 배열에 담는다. 그리고 필터링에 사용할 배열을 초기화한다
//                    // 2. 청년을 누르면 parent_category가 청년인 데이터들만을 해서 필터링 배열에 담는다
//                    // 3. 데이터가 담긴 필터링 배열을 ArrayList로 바꿔서 리사이클러뷰 어댑터에 넣어 유저에게 보여준다
//                    // 4. 다른 필터를 누르면 필터링 배열 안의 데이터들을 전부 지우고 새 데이터로 채워넣어 3번을 반복한다
//                    final MapResultItem[] test = new MapResultItem[item_list.size()];
//                    Log.e(TAG, "필터링 배열 크기 : " + test.length);
//                }
            }
            else
            {
                String name = keyword_list.get(position).getParent_category();
                Log.e(TAG, "4. 상단 리사이클러뷰에서 선택한 카테고리명 = " + name);

                for (int i = 0; i < item_list.size(); i++)
                {
                    Log.e(TAG, "지역에서 파싱 후 item_list getWelf_name() : " + item_list.get(i).getWelf_name());
                    Log.e(TAG, "지역에서 파싱 후 item_list getParent_category() : " + item_list.get(i).getParent_category());
                    Log.e(TAG, "지역에서 파싱 후 item_list getWelf_category() : " + item_list.get(i).getWelf_category());
                    // 여기서 어떻게 해야?
                    // 1. 모든 데이터를 배열에 담는다. 그리고 필터링에 사용할 배열을 초기화한다
                    // 2. 청년을 누르면 parent_category가 청년인 데이터들만을 해서 필터링 배열에 담는다
                    // 3. 데이터가 담긴 필터링 배열을 ArrayList로 바꿔서 리사이클러뷰 어댑터에 넣어 유저에게 보여준다
                    // 4. 다른 필터를 누르면 필터링 배열 안의 데이터들을 전부 지우고 새 데이터로 채워넣어 3번을 반복한다
                }

                // 청년, 저소득층, 육아·임신 등 parent_category가 들어 있는 필터를 누르면 그 필터에 해당하는 데이터들만 리사이클러뷰에 보여야 한다
                boolean isDuplicated = false;
                for (int i = 0; i < item_list.size(); i++)
                {
                    if (item_list.get(i).getParent_category().equals(name))
                    {
                        // item_list를 반복하며 유저가 선택한 필터명과 같은 parent_category인 정책은 따로 리스트에 넣는다
                        isDuplicated = true;
                        break;
                    }
                }
                if (isDuplicated)
                {
                    other_list.addAll(item_list);
                    map_adapter = new MapResultAdapter(MapDetailActivity.this, other_list, itemClickListener);
                    map_adapter.setOnItemClickListener(((view1, position1) -> {
                        String name2 = item_list.get(position).getWelf_name();
                        Log.e(TAG, "혜택 이름 = " + name2);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 상세보기 화면으로 이동 (선택한 혜택 : " + name2 + ")");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        Intent see_detail_intent = new Intent(MapDetailActivity.this, DetailBenefitActivity.class);
                        see_detail_intent.putExtra("name", name2);
                        see_detail_intent.putExtra("welf_local", area);
                        startActivity(see_detail_intent);
                    }));
                    map_result_recyclerview.setAdapter(map_adapter);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 선택한 하위 카테고리 : " + name);
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                searchUpLevelCategory(name);
            }
        });
        result_keyword_recyclerview.setAdapter(adapter);

        // 어댑터 초기화, 이 때 for문 안에서 값이 들어간 List를 인자로 넣는다
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

    // 상단 리사이클러뷰에서 선택한 카테고리명에 따라 리사이클러뷰에 뿌리는 혜택명들을 바꾼다
    void searchUpLevelCategory(String select_category)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.searchWelfareCategory(token, session, "category_search", select_category, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 : " + result);
                    second_parsing(result);
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
            if (keyword_list.get(position).getParent_category().equals("전체"))
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
                String name = keyword_list.get(position).getParent_category();
                Log.e(TAG, "2. 상단 리사이클러뷰에서 선택한 카테고리명 = " + name);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지역 검색 화면에서 선택한 하위 카테고리 : " + name);
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                searchUpLevelCategory(name);
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