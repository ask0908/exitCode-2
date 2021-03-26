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
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;

import java.util.ArrayList;

public class RegionChooseActivity extends AppCompatActivity
{
    boolean isConnected = false;
    private Button[] mButton = new Button[17];
    // 각각 다르게 출력할 스트링을 넣어둘 리스트
    private ArrayList<Integer> mDataList;

    public final String TAG = "SearchFragment";

    Button btnSeoul, btnGyeonggi, btnIncheon, btnGangwon, btnSejong, btnChungbuk, btnChungnam, btnDaejeon, btnDaegu, btnGyeongbuk,
            btnGyeongnam, btnJeonbuk, btnJeonnam, btnGwangju, btnBusan, btnUlsan, btnJeju;

    // 유저에게 제공할 혜택들을 담을 ArrayList
    ArrayList<String> m_favorList;

    // 유저 로그 전송 시 세션값, 토큰값 가져오는 데 사용할 쉐어드
    SharedPreferences sharedPreferences;
    // 서버로 한글 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 해서, 인코딩한 문자열을 저장할 변수
    String encode_str;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

//    public SearchFragment()
//    {
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(RegionChooseActivity.this);
        setContentView(R.layout.activity_region_choose);

        mDataList = new ArrayList<>();

        mButton[0] = (Button) findViewById(R.id.btnSeoul);
        mButton[1] = (Button) findViewById(R.id.btnGyeonggi);
        mButton[2] = (Button) findViewById(R.id.btnIncheon);
        mButton[3] = (Button) findViewById(R.id.btnGangwon);
        mButton[4] = (Button) findViewById(R.id.btnSejong);
        mButton[5] = (Button) findViewById(R.id.btnChungbuk);
        mButton[6] = (Button) findViewById(R.id.btnChungnam);
        mButton[7] = (Button) findViewById(R.id.btnDaejeon);
        mButton[8] = (Button) findViewById(R.id.btnDaegu);
        mButton[9] = (Button) findViewById(R.id.btnGyeongbuk);
        mButton[10] = (Button) findViewById(R.id.btnGyeongnam);
        mButton[11] = (Button) findViewById(R.id.btnJeonbuk);
        mButton[12] = (Button) findViewById(R.id.btnJeonnam);
        mButton[13] = (Button) findViewById(R.id.btnGwangju);
        mButton[14] = (Button) findViewById(R.id.btnBusan);
        mButton[15] = (Button) findViewById(R.id.btnUlsan);
        mButton[16] = (Button) findViewById(R.id.btnJeju);

        btnSeoul = findViewById(R.id.btnSeoul);
        btnGyeonggi = findViewById(R.id.btnGyeonggi);
        btnIncheon = findViewById(R.id.btnIncheon);
        btnSejong = findViewById(R.id.btnSejong);
        btnGangwon = findViewById(R.id.btnGangwon);
        btnChungbuk = findViewById(R.id.btnChungbuk);
        btnChungnam = findViewById(R.id.btnChungnam);
        btnDaejeon = findViewById(R.id.btnDaejeon);
        btnDaegu = findViewById(R.id.btnDaegu);
        btnGyeongbuk = findViewById(R.id.btnGyeongbuk);
        btnGyeongnam = findViewById(R.id.btnGyeongnam);
        btnJeonbuk = findViewById(R.id.btnJeonbuk);
        btnJeonnam = findViewById(R.id.btnJeonnam);
        btnGwangju = findViewById(R.id.btnGwangju);
        btnBusan = findViewById(R.id.btnBusan);
        btnUlsan = findViewById(R.id.btnUlsan);
        btnJeju = findViewById(R.id.btnJeju);

//        for (int i = 0; i < 14; i++)
//        {
//            // 버튼의 포지션(배열에서의 index)를 태그로 저장
//            mButton[i].setTag(i);
//
//            // 클릭 리스너 등록
//            mButton[i].setOnClickListener(this);
//
//            // 출력할 데이터 생성
//            mDataList.add(i);
//        }

        isConnected = isNetworkConnected(RegionChooseActivity.this);
        if (!isConnected)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(RegionChooseActivity.this);
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
        m_favorList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);

        /* 서울 혜택 버튼 */
        btnSeoul.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "서울 버튼 클릭!");
            m_favorList.add("서울");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "서울 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 경기 혜택 버튼 */
        btnGyeonggi.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("경기");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "경기 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 인천 혜택 버튼 */
        btnIncheon.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("인천");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "인천 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 강원 혜택 버튼 */
        btnGangwon.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("강원");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "강원 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            m_favorList.clear();
        });

        /* 세종 혜택 버튼 */
        btnSejong.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("세종");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "세종 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            m_favorList.clear();
        });

        /* 충북 혜택 버튼 */
        btnChungbuk.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("충북");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "충북 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            m_favorList.clear();
        });

        /* 충남 혜택 버튼 */
        btnChungnam.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("충남");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "충남 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            m_favorList.clear();
        });

        /* 대전 혜택 버튼 */
        btnDaejeon.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("대전");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "대전 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            m_favorList.clear();
        });

        /* 대구 혜택 버튼 */
        btnDaegu.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("대구");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "대구 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 경북 혜택 버튼 */
        btnGyeongbuk.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("경북");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "경북 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 경남 혜택 버튼 */
        btnGyeongnam.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("경남");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "경남 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 전북 혜택 버튼 */
        btnJeonbuk.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("전북");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "전북 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 전남 혜택 버튼 */
        btnJeonnam.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("전남");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "전남 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 광주 혜택 버튼 */
        btnGwangju.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("광주");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "광주 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 부산 혜택 버튼 */
        btnBusan.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("부산");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "부산 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 울산 혜택 버튼 */
        btnUlsan.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("울산");
            /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "울산 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 제주 혜택 버튼 */
        btnJeju.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("제주");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "제주 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(RegionChooseActivity.this, RegionResultActivity.class);
            m_intent.putStringArrayListExtra("region_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });
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