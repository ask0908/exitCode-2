package com.psj.welfare.activity;

import android.Manifest;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.test.TestActivity;
import com.psj.welfare.util.GpsTracker;

import java.util.ArrayList;
import java.util.List;

public class ThemeChooseActivity extends AppCompatActivity
{
    boolean isConnected = false;
    private Button[] mButton = new Button[14];
    // 각각 다르게 출력할 스트링을 넣어둘 리스트
    private ArrayList<Integer> mDataList;

    Button btnJob, btnYoung, btnLiving, btnChild, btnPregnancy, btnCultural, btnCompany, btnHomeless, btnOld, btnDisorder, btnMulticultural, btnLaw, btnMedical, btnEtc;

    public final String TAG = "SearchFragment";

    private EditText search_edittext;

    Toolbar search_toolbar;

    TextView recommend_search_textview, recent_search_history_textview;

    TextView main_job_title, main_student_title, main_living_title, main_pregnancy_title, main_child_title, main_cultural_title,
            main_company_title, main_homeless_title, main_old_title, main_disorder_title, main_multicultural_title, main_law_title;

    Button main_job, main_student, main_living, main_pregnancy, main_child, main_cultural, main_company, main_homeless, main_old, main_disorder,
            main_multicultural, main_law, main_medical, main_etc;

    // 유저에게 제공할 혜택들을 담을 ArrayList
    ArrayList<String> m_favorList;

    // 유저 로그 전송 시 세션값, 토큰값 가져오는 데 사용할 쉐어드
    SharedPreferences sharedPreferences;
    // 서버로 한글 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 해서, 인코딩한 문자열을 저장할 변수
    String encode_str;

    // 지도 버튼
    Button map_btn;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    /* 메인 화면에 있던 지도 기능을 쓰기 위해 필요한 변수들 */
    // 유저의 위치정보를 바꿀 때(서울특별시 -> 서울) 쓰는 변수
    String user_area;

    // 유저의 현 위치를 가져와 주소로 변환하는 처리를 하는 클래스
    private GpsTracker gpsTracker;

    // split() 후 문자열들을 담을 리스트
    List<String> split_list;

    // split() 후 결과를 담을 변수. OO시, OO구 정보를 담는다
    String city, district;

    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,   // 앱이 정확한 위치에 액세스하도록 허용하는 권한
            Manifest.permission.ACCESS_COARSE_LOCATION  // 앱이 대략적인 위치에 액세스하도록 허용하는 권한, ACCESS_FINE_LOCATION을 대체재로 쓸 수 있다
    };

    private final int LOCATION = 1001;

//    public SearchFragment()
//    {
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ThemeChooseActivity.this);
        setContentView(R.layout.activity_theme_choose);

        search_edittext = findViewById(R.id.search_edittext);

        mDataList = new ArrayList<>();

        mButton[0] = (Button) findViewById(R.id.btnJob);
        mButton[1] = (Button) findViewById(R.id.btnYoung);
        mButton[2] = (Button) findViewById(R.id.btnLiving);
        mButton[3] = (Button) findViewById(R.id.btnChild);
        mButton[4] = (Button) findViewById(R.id.btnPregnancy);
        mButton[5] = (Button) findViewById(R.id.btnCultural);
        mButton[6] = (Button) findViewById(R.id.btnCompany);
        mButton[7] = (Button) findViewById(R.id.btnHomeless);
        mButton[8] = (Button) findViewById(R.id.btnOld);
        mButton[9] = (Button) findViewById(R.id.btnDisorder);
        mButton[10] = (Button) findViewById(R.id.btnMulticultural);
        mButton[11] = (Button) findViewById(R.id.btnLaw);
        mButton[12] = (Button) findViewById(R.id.btnMedical);
        mButton[13] = (Button) findViewById(R.id.btnEtc);

        main_job = findViewById(R.id.btnJob);
        main_student = findViewById(R.id.btnYoung);
        main_living = findViewById(R.id.btnLiving);
        main_child = findViewById(R.id.btnChild);
        main_pregnancy = findViewById(R.id.btnPregnancy);
        main_cultural = findViewById(R.id.btnCultural);
        main_company = findViewById(R.id.btnCompany);
        main_homeless = findViewById(R.id.btnHomeless);
        main_old = findViewById(R.id.btnOld);
        main_disorder = findViewById(R.id.btnDisorder);
        main_multicultural = findViewById(R.id.btnMulticultural);
        main_law = findViewById(R.id.btnLaw);
        main_medical = findViewById(R.id.btnMedical);
        main_etc = findViewById(R.id.btnEtc);

        isConnected = isNetworkConnected(ThemeChooseActivity.this);
        if (!isConnected)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ThemeChooseActivity.this);
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

        /* 취업·창업 혜택 버튼 */
        main_job.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("취업·창업");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "취업·창업 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 청년 혜택 버튼 */
        main_student.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("청년");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "청년 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 주거 혜택 버튼 */
        main_living.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("주거");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "주거 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 아기·어린이 혜택 버튼 */
        main_child.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("아기·어린이");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "아기·어린이 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 육아·임신 혜택 버튼 */
        main_pregnancy.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("육아·임신");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "육아·임신 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 문화·생활 혜택 버튼 */
        main_cultural.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("문화·생활");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "문화·생활 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 기업·자영업자 혜택 버튼 */
        main_company.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("기업·자영업자");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "기업·자영업자 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 저소득층 혜택 버튼 */
        main_homeless.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("저소득층");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "저소득층 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 중장년·노인 혜택 버튼 */
        main_old.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("중장년·노인");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "중장년·노인 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 장애인 혜택 버튼 */
        main_disorder.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("장애인");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "장애인 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 다문화 혜택 버튼 */
        main_multicultural.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("다문화");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다문화 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 법률 혜택 버튼 */
        main_law.setOnClickListener(OnSingleClickListener ->
        {
            m_favorList.add("법률");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "법률 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 의료 혜택 버튼 */
        main_medical.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("의료");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "의료 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

        /* 기타 혜택 버튼 */
        main_etc.setOnClickListener(OnSingleClickListener -> {
            m_favorList.add("기타");
            if (!m_favorList.contains("전체"))
            {
                m_favorList.add(0, "전체");
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "기타 선택");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
            Intent m_intent = new Intent(ThemeChooseActivity.this, ResultBenefitActivity.class);
            m_intent.putStringArrayListExtra("favor_btn", m_favorList);
            startActivity(m_intent);
            // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
            m_favorList.clear();
        });

    }

    /* 인터넷 연결 상태 체크하는 메서드 */
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

    /* 액션바에 그라데이션 주는 메서드 */
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}