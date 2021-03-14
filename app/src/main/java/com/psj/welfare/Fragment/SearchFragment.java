package com.psj.welfare.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.activity.MapActivity;
import com.psj.welfare.activity.ResultBenefitActivity;
import com.psj.welfare.activity.SearchResultActivity;
import com.psj.welfare.custom.CustomResultBenefitDialog;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.R;
import com.psj.welfare.util.GpsTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

/* 혜택 이름 검색하는 프래그먼트
* 하단의 카테고리들을 선택하고 버튼을 눌러 이동한 경우, 혜택 하위 카테고리 검색을 적용해 결과를 보여준다
* 카테고리 다중선택이 없어져서 이제 해시태그 키워드를 누르면, 그 카테고리에 관련된 혜택들만 보여준다 */
public class SearchFragment extends Fragment
{
    // 로그 찍을 때 사용하는 TAG
    public final String TAG = "SearchFragment";

    private EditText search_edittext;

    Toolbar search_toolbar;

    TextView recommend_search_textview, recent_search_history_textview;

    TextView main_job_title, main_student_title, main_living_title, main_pregnancy_title, main_child_title, main_cultural_title,
            main_company_title, main_homeless_title, main_old_title, main_disorder_title, main_multicultural_title, main_law_title;

    LinearLayout main_job, main_student, main_living, main_pregnancy, main_child, main_cultural, main_company, main_homeless, main_old, main_disorder,
            main_multicultural, main_law;

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

    public SearchFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        init(view);

        recommend_search_textview = view.findViewById(R.id.recommend_search_textview);
        recent_search_history_textview = view.findViewById(R.id.recent_search_history_textview);
        map_btn = view.findViewById(R.id.map_btn);
        search_edittext = view.findViewById(R.id.search_edittext);

        search_toolbar = view.findViewById(R.id.search_toolbar);
        return view;
    }

    /* 항상 onViewCreated()에서 findViewById()를 쓰고(뷰들이 완전히 생성됐기 때문) 뷰를 매개변수로 전달한다 */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Log.e(TAG, "onViewCreated() 호출");

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }
        m_favorList = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(search_toolbar);
        }
        search_toolbar.setTitle("검색");

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Log.e(TAG, "검색 키워드 : " + search_edittext.getText());
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "검색 화면에서 키워드 검색 결과 화면으로 이동. 검색한 키워드 : " + search_edittext.getText());
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 검색 버튼 클릭 되었을 때 처리하는 기능
                    performSearch(search_edittext.getText().toString());
                    return true;
                }

                return false;
            }

        });

        // 내 주변 혜택 찾기 버튼
        // 이거 누를 때에도 위치 권한을 허용했는지 확인해야 한다
        map_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                // TODO : 위치정보 권한 설정
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // 위치 권한 거부됐을 시
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("권한 허용")
                            .setMessage("지역별 혜택을 확인하시려면 권한 허용이 필요합니다")
                            .setPositiveButton("허용하러 가기", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            }).setNegativeButton("종료", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "권한 설정 하지 않음", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
                }
                else
                {
                    // 유저의 위치정보를 찾는 클래스의 객체 생성(Fragment기 때문에 인자로 getActivity()를 넣어야 함!!)
                    gpsTracker = new GpsTracker(getActivity());

                    // 유저의 위치에서 위도, 경도값을 가져와 변수에 저장
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    // 위도, 경도값으로 주소를 만들어 String 변수에 저장
                    String address = getCurrentAddress(latitude, longitude);

                    // String 변수 안의 값을 " "을 기준으로 split해서 'OO구' 글자를 빼낸다
                    split_list = new ArrayList<>();
                    String[] result = address.split(" ");
                    split_list.addAll(Arrays.asList(result));

                    // OO시, OO구에 대한 정보를 각각 변수에 집어넣는다
                    city = split_list.get(1);
                    district = split_list.get(2);

                    // 들어있는 값에 따라 지역명을 바꿔서 변수에 저장한다
                    if (address.contains("서울특별시"))
                    {
                        user_area = "서울";
                    }
                    if (address.contains("인천광역시"))
                    {
                        user_area = "인천";
                    }
                    if (address.contains("강원도"))
                    {
                        user_area = "강원";
                    }
                    if (address.contains("경기"))
                    {
                        user_area = "경기";
                    }
                    if (address.contains("충청북도"))
                    {
                        user_area = "충북";
                    }
                    if (address.contains("충청남도"))
                    {
                        user_area = "충남";
                    }
                    if (address.contains("세종시"))
                    {
                        user_area = "세종";
                    }
                    if (address.contains("대전시"))
                    {
                        user_area = "대전";
                    }
                    if (address.contains("경상북도"))
                    {
                        user_area = "경북";
                    }
                    if (address.contains("울산"))
                    {
                        user_area = "울산";
                    }
                    if (address.contains("대구"))
                    {
                        user_area = "대구";
                    }
                    if (address.contains("부산광역시"))
                    {
                        user_area = "부산";
                    }
                    if (address.contains("경상남도"))
                    {
                        user_area = "경남";
                    }
                    if (address.contains("전라북도"))
                    {
                        user_area = "전북";
                    }
                    if (address.contains("광주광역시"))
                    {
                        user_area = "광주";
                    }
                    if (address.contains("전라남도"))
                    {
                        user_area = "전남";
                    }
                    if (address.contains("제주도"))
                    {
                        user_area = "제주";
                    }

                    // 변환이 끝난 지역명은 인텐트에 담아서 지도 화면으로 보낸다
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "지도 화면으로 이동");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra("user_area", user_area);
                    intent.putExtra("city", city);
                    intent.putExtra("district", district);
                    startActivity(intent);
                }
            }
        });

        /* 아기·어린이 혜택 버튼 */
        main_child.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "아기·어린이 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "OnSingleClickListener.isSelected() = " + OnSingleClickListener.isSelected());
                m_favorList.remove("아기·어린이");
                main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                main_child.setSelected(!main_child.isSelected());
                main_child.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                Log.e(TAG, "OnSingleClickListener.isSelected() = " + OnSingleClickListener.isSelected());
                m_favorList.add("아기·어린이");
                main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                main_child.setSelected(true);
                main_child.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "아기·어린이 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 학생·청년 혜택 버튼 */
        main_student.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("청년");
                main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_student.setSelected(!main_student.isSelected());
                main_student.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                // 이 때 아이콘이 하얗게 변한다
                m_favorList.add("청년");
                main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_student.setSelected(true);
                main_student.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "청년 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 중장년·노인 혜택 버튼 */
        main_old.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("중장년·노인");
                main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_old.setSelected(!main_old.isSelected());
                main_old.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("중장년·노인");
                main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_old.setSelected(true);
                main_old.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "중장년·노인 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 육아·임신 혜택 버튼 */
        main_pregnancy.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("육아·임신");
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_pregnancy.setSelected(!main_pregnancy.isSelected());
                main_pregnancy.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("육아·임신");
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_pregnancy.setSelected(true);
                main_pregnancy.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "육아·임신 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 장애인 혜택 버튼 */
        main_disorder.setOnClickListener(OnSingleClickListener ->
        {

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("장애인");
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_disorder.setSelected(!main_disorder.isSelected());
                main_disorder.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("장애인");
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_disorder.setSelected(true);
                main_disorder.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "장애인 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 문화·생활 혜택 버튼 */
        main_cultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("문화·생활");
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_cultural.setSelected(!main_cultural.isSelected());
                main_cultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("문화·생활");
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_cultural.setSelected(true);
                main_cultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "문화·생활 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 다문화 혜택 버튼 */
        main_multicultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("다문화");
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_multicultural.setSelected(!main_multicultural.isSelected());
                main_multicultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("다문화");
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_multicultural.setSelected(true);
                main_multicultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다문화 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 기업·자영업자 혜택 버튼 */
        main_company.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("기업·자영업자");
                main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_company.setSelected(!main_company.isSelected());
                main_company.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("기업·자영업자");
                main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_company.setSelected(true);
                main_company.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "기업·자영업자 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 법률 혜택 버튼 */
        main_law.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("법률");
                main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_law.setSelected(!main_law.isSelected());
                main_law.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("법률");
                main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_law.setSelected(true);
                main_law.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "법률 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 주거 혜택 버튼 */
        main_living.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("주거");
                main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_living.setSelected(!main_living.isSelected());
                main_living.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("주거");
                main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_living.setSelected(true);
                main_living.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "주거 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 취업·창업 혜택 버튼 */
        main_job.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("취업·창업");
                main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_job.setSelected(!main_job.isSelected());
                main_job.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("취업·창업");
                main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_job.setSelected(true);
                main_job.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "취업·창업 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 저소득층 혜택 버튼 */
        main_homeless.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("저소득층");
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_homeless.setSelected(!main_homeless.isSelected());
                main_homeless.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("저소득층");
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_homeless.setSelected(true);
                main_homeless.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "저소득층 선택");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));

    }

    // 모바일 키보드에서 검색 버튼 눌렀을 때
    public void performSearch(String search)
    {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_edittext.getWindowToken(), 0);
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra("search", search);
        startActivity(intent);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_child.setSelected(false);

        main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_student.setSelected(false);

        main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_old.setSelected(false);

        main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_pregnancy.setSelected(false);

        main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_disorder.setSelected(false);

        main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_cultural.setSelected(false);

        main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_multicultural.setSelected(false);

        main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_company.setSelected(false);

        main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_law.setSelected(false);

        main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_living.setSelected(false);

        main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_job.setSelected(false);

        main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_homeless.setSelected(false);
    }

    private void init(View view)
    {
        main_child = view.findViewById(R.id.main_child);
        main_student = view.findViewById(R.id.main_student);
        main_old = view.findViewById(R.id.main_old);
        main_pregnancy = view.findViewById(R.id.main_pregnancy);
        main_disorder = view.findViewById(R.id.main_disorder);
        main_cultural = view.findViewById(R.id.main_cultural);
        main_multicultural = view.findViewById(R.id.main_multicultural);
        main_company = view.findViewById(R.id.main_company);
        main_law = view.findViewById(R.id.main_law);
        main_living = view.findViewById(R.id.main_living);
        main_job = view.findViewById(R.id.main_job);
        main_homeless = view.findViewById(R.id.main_homeless);

        main_child_title = view.findViewById(R.id.main_child_title);
        main_student_title = view.findViewById(R.id.main_student_title);
        main_old_title = view.findViewById(R.id.main_old_title);
        main_pregnancy_title = view.findViewById(R.id.main_pregnancy_title);
        main_disorder_title = view.findViewById(R.id.main_disorder_title);
        main_cultural_title = view.findViewById(R.id.main_cultural_title);
        main_multicultural_title = view.findViewById(R.id.main_multicultural_title);
        main_company_title = view.findViewById(R.id.main_company_title);
        main_law_title = view.findViewById(R.id.main_law_title);
        main_living_title = view.findViewById(R.id.main_living_title);
        main_job_title = view.findViewById(R.id.main_job_title);
        main_homeless_title = view.findViewById(R.id.main_homeless_title);
    }

    // 위도, 경도를 받아서 주소를 만들어내는 메서드
    public String getCurrentAddress(double latitude, double longitude)
    {
        // 지오코더를 통해 GPS 값을 주소로 변환한다
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses;

        try
        {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        }
        catch (IOException ioException)
        {
            // 네트워크 문제
            Toast.makeText(getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }
        catch (IllegalArgumentException illegalArgumentException)
        {
            // 좌표 문제
            Toast.makeText(getActivity(), "잘못된 GPS 좌표입니다", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0)
        {
            // 주소 미발견
            Toast.makeText(getActivity(), "주소가 발견되지 않았습니다", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0) + "\n";
    }

    /* 단말 위치를 가져올 수 있는지 상태를 체크하는 메서드 */
    public boolean checkLocationServicesStatus()
    {
        /* LocationManager : 시스템 위치 서비스에 대한 액세스 제공 */
        // Context의 위치(location) 정보를 LocationManager 객체에 저장한다
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        /* isProviderEnabled() : 지정된 provider의 현재 활성화 / 비활성화 상태를 리턴하는 메서드 */
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // TODO : 권한 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case LOCATION :
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(getActivity(), "위치정보 권한 허용돼 있음", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getActivity(), "위치정보 권한 거부돼 있음", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}