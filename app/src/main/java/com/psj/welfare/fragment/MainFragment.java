package com.psj.welfare.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.psj.welfare.R;
import com.psj.welfare.activity.MapActivity;
import com.psj.welfare.activity.ResultBenefitActivity;
import com.psj.welfare.custom.CustomResultBenefitDialog;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.GpsTracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

/**
 * MainActivity를 대체하는 프래그먼트
 * 앱 실행 시 이 프래그먼트의 화면이 먼저 보이게 한다
 */
public class MainFragment extends Fragment
{
    public static final String TAG = "MainFragment"; // 로그 찍을 때 사용하는 TAG

    // 스크롤뷰
    private ScrollView main_ScrollView;

    // 좌상단 메인 로고. 클릭 시 더보기 버튼이 있는 페이지로 화면 전환 효과 없이 이동하게 한다 (main logo)
    ImageView main_logo, main_job_img, main_student_img, main_living_img, main_pregnancy_img, main_child_img, main_cultural_img, main_company_img, main_homeless_img,
            main_old_img, main_disorder_img, main_multicultural_img, main_law_img, main_etc_img;

    TextView main_move_text, main_job_title, main_student_title, main_living_title, main_pregnancy_title, main_child_title, main_cultural_title, main_company_title,
            main_homeless_title, main_old_title, main_disorder_title, main_multicultural_title, main_law_title, main_etc_title;

    LinearLayout main_job, main_student, main_living, main_pregnancy, main_child, main_cultural, main_company, main_homeless, main_old, main_disorder, main_multicultural,
            main_law, main_etc;

    // 맨 밑의 조회하기 버튼
    Button main_done;

    // 지도 버튼
    Button map_btn;

    // 더보기 버튼을 눌렀는지 확인할 때 사용할 boolean 변수. true일 경우에만 로고 클릭 이벤트를 발동시킨다
    boolean isClicked = false;

    // 더보기 버튼 있는 레이아웃
    LinearLayout more_layout;
    // 장애인, 다문화, 법률, 기타 버튼 있는 레이아웃
    LinearLayout multi_law_layout, etc_layout;

    ArrayList<String> m_favorList = new ArrayList<>();  // 유저에게 제공할 혜택들을 담을 ArrayList

    // 스크롤 수정
    LinearLayout main_content;

    String user_area;
    // 유저의 현 위치를 가져와 주소로 변환하는 처리를 하는 클래스
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_CODE = 200;

    // split() 후 문자열들을 담을 리스트
    List<String> split_list;

    // split() 후 결과를 담을 변수. OO시, OO구 정보를 담는다
    String city, district;

    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,   // 앱이 정확한 위치에 액세스하도록 허용하는 권한
            Manifest.permission.ACCESS_COARSE_LOCATION  // 앱이 대략적인 위치에 액세스하도록 허용하는 권한, ACCESS_FINE_LOCATION을 대체재로 쓸 수 있다
    };

    public MainFragment()
    {
        // 프래그먼트 사용 시 있어야 하는 디폴트 생성자
    }

    /* 프래그먼트와 연결된 뷰 계층 생성 위해 호출됨, onViewCreated()보다 먼저 호출된다
     * 최초에 onAttach()가 호출된 이후 onCreate() 다음에 3번째로 호출된다 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
    }

    /* onCreateView() 다음에 호출됨. 뷰 계층 구조가 완전히 생성됐음을 알려 하위 클래스가 스스로 초기화되게 한다 */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // 위치 권한 설정 처리
        PermissionListener permissionListener = new PermissionListener()
        {
            @Override
            public void onPermissionGranted()
            {
                //
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions)
            {
                //
            }
        };

        if (!checkLocationServicesStatus())
        {
            //
        }
        else
        {
            TedPermission.with(getActivity())
                    .setRationaleMessage("너의 혜택은 서비스를 이용하시려면 위치 정보 권한 설정이 필요합니다")
                    .setPermissionListener(permissionListener)
                    .setPermissions(REQUIRED_PERMISSIONS)
                    .check();
        }
        init(view);

        /* 더보기 버튼을 누르면 더보기 버튼이 사라지고 다른 4가지 버튼들이 나오게 한다 */
        more_layout.setOnClickListener(OnSingleClickListener ->
        {
            isClicked = true;
            Log.e(TAG, "isClicked 상태 : " + isClicked);
            Log.e(TAG, "더보기 버튼 클릭됨");

            // 더보기 버튼이 있는 레이아웃은 아예 안 보이게 한다
            more_layout.setVisibility(View.GONE);

            // 다른 4가지 레이아웃들은 보이도록 한다
            multi_law_layout.setVisibility(View.VISIBLE);
            etc_layout.setVisibility(View.VISIBLE);
            main_disorder.setVisibility(View.VISIBLE);
            main_law.setVisibility(View.VISIBLE);
        });

        // 메인 로고 클릭 리스너
        main_logo.setOnClickListener(OnSingleClickListener ->
        {
            if (isClicked)
            {
                isClicked = false;
                Log.e(TAG, "isClicked 상태 : " + isClicked);
                // 화면 전환 효과 없이 더보기 버튼이 있는 페이지로 이동하게 한다 = VISIBLE 상태가 되었던 버튼들을 다시 GONE 상태로 되돌린다
                more_layout.setVisibility(View.VISIBLE);
                multi_law_layout.setVisibility(View.GONE);
                etc_layout.setVisibility(View.GONE);
                main_disorder.setVisibility(View.GONE);
                main_law.setVisibility(View.GONE);
            }
        });

        // 조회하기 버튼
        main_done.setOnClickListener(OnSingleClickListener ->
        {
            Log.e("main_done 버튼 클릭", "m_favorList 크기 = " + m_favorList.size());
            // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
            if (m_favorList.size() == 0)
            {
                CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                dialog.callDialog();
            }
            else
            {
                m_favorList.add(0, "전체");
                for (int i = 0; i < m_favorList.size(); i++)
                {
                    Log.e(TAG, "m_favorList : " + m_favorList);
                }
                Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                startActivity(m_intent);
            }
//			m_favorList.add(0, "전체");
//			for (int i = 0; i < m_favorList.size(); i++) {
//				Log.e(TAG, "m_favorList : " + m_favorList);
//			}
//			Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
//			m_intent.putStringArrayListExtra("favor_btn", m_favorList);
//			startActivity(m_intent);
        });

        /* 아기·어린이 혜택 버튼 */
        main_child.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "아기·어린이 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("아기·어린이");
                main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child));
                main_child_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_child.setSelected(!main_child.isSelected());
            }
            else
            {
                m_favorList.add("아기·어린이");
                main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child_after));
                main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_child.setSelected(true);
            }
        });

        /* 학생·청년 혜택 버튼 */
        main_student.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "학생·청년 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("학생·청년");
                main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student));
                main_student_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_student.setSelected(!main_student.isSelected());
            }
            else
            {
                m_favorList.add("학생·청년");
                main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student_after));
                main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_student.setSelected(true);
            }
        });

        /* 중장년·노인 혜택 버튼 */
        main_old.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "학생·청년 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("중장년·노인");
                main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old));
                main_old_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_old.setSelected(!main_old.isSelected());
            }
            else
            {
                m_favorList.add("중장년·노인");
                main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old_after));
                main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_old.setSelected(true);
            }
        });

        /* 육아·임신 혜택 버튼 */
        main_pregnancy.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "학생·청년 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("육아·임신");
                main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy));
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_pregnancy.setSelected(!main_pregnancy.isSelected());
            }
            else
            {
                m_favorList.add("육아·임신");
                main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy_after));
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_pregnancy.setSelected(true);
            }
        });

        /* 장애인 혜택 버튼 */
        main_disorder.setOnClickListener(OnSingleClickListener ->
        {

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("장애인");
                main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder));
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_disorder.setSelected(!main_disorder.isSelected());
            }
            else
            {
                m_favorList.add("장애인");
                main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder_after));
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_disorder.setSelected(true);
            }
        });

        /* 문화·생활 혜택 버튼 */
        main_cultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("문화·생활");
                main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural));
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_cultural.setSelected(!main_cultural.isSelected());
            }
            else
            {
                m_favorList.add("문화·생활");
                main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural_after));
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_cultural.setSelected(true);
            }
        });

        /* 다문화 혜택 버튼 */
        main_multicultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("다문화");
                main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural));
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_multicultural.setSelected(!main_multicultural.isSelected());
            }
            else
            {
                m_favorList.add("다문화");
                main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural_after));
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_multicultural.setSelected(true);
            }
        });

        /* 기업·자영업자 혜택 버튼 */
        main_company.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("기업·자영업자");
                main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company));
                main_company_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_company.setSelected(!main_company.isSelected());
            }
            else
            {
                m_favorList.add("기업·자영업자");
                main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company_after));
                main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_company.setSelected(true);
            }
        });

        /* 법률 혜택 버튼 */
        main_law.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("법률");
                main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law));
                main_law_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_law.setSelected(!main_law.isSelected());
            }
            else
            {
                m_favorList.add("법률");
                main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law_after));
                main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_law.setSelected(true);
            }
        });

        /* 주거 혜택 버튼 */
        main_living.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("주거");
                main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living));
                main_living_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_living.setSelected(!main_living.isSelected());
            }
            else
            {
                m_favorList.add("주거");
                main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living_after));
                main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_living.setSelected(true);
            }
        });

        /* 취업·창업 혜택 버튼 */
        main_job.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("취업·창업");
                main_job_title.setTextColor(getResources().getColor(R.color.colorBlack));
                main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_job.setSelected(!main_job.isSelected());
            }
            else
            {
                m_favorList.add("취업·창업");
                main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job_after));
                main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_job.setSelected(true);
            }
        });

        /* 저소득층 혜택 버튼 */
        main_homeless.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("저소득층");
                main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless));
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_homeless.setSelected(!main_homeless.isSelected());
            }
            else
            {
                m_favorList.add("저소득층");
                main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless_after));
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_homeless.setSelected(true);
            }
        });

        /* 기타 혜택 버튼 */
        main_etc.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("기타");
                main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc));
                main_etc_title.setTextColor(getResources().getColor(R.color.colorBlack));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_etc.setSelected(!main_etc.isSelected());
            }
            else
            {
                m_favorList.add("기타");
                main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc_after));
                main_etc_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_etc.setSelected(true);
            }
        });

        // 혜택 조회하러 가기 클릭 리스너
        main_move_text.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Log.e(TAG, "혜택 조회하러 가기 클릭!");

                // 스크롤 0.5초만에 Y축 main_content 머리로 이동
                main_ScrollView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!isClicked)
                        {
                            // 더보기 버튼 클릭 전 스크롤 이동
                            ObjectAnimator.ofInt(main_ScrollView, "scrollY", main_content.getTop()).setDuration(500).start();
                        }
                        else
                        {
                            // 더보기 버튼 클릭 후 스크롤 이동
                            ObjectAnimator.ofInt(main_ScrollView, "scrollY", main_content.getTop()).setDuration(500).start();
                        }

                    }
                });
            }
        });

        // 지도 버튼 클릭 리스너
        map_btn.setOnClickListener(v -> {
            gpsTracker = new GpsTracker(getActivity());

            // 유저의 위치에서 위도, 경도값을 가져와 변수에 저장
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            // 위도, 경도값으로 주소를 만들어 String 변수에 저장
            String address = getCurrentAddress(latitude, longitude);
            Log.e(TAG, "address = " + address);

            // String 변수 안의 값을 " "을 기준으로 split해서 'OO구' 글자를 빼낸다
            split_list = new ArrayList<>();
            String[] result = address.split(" ");
            split_list.addAll(Arrays.asList(result));
            Log.e(TAG, "split_list = " + split_list);

            // OO시, OO구에 대한 정보를 각각 변수에 집어넣는다
            city = split_list.get(1);
            district = split_list.get(2);

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
                user_area = "세종시";
            }
            if (address.contains("대전시"))
            {
                user_area = "대전시";
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
            Intent intent = new Intent(getActivity(), MapActivity.class);
            intent.putExtra("user_area", user_area);
            intent.putExtra("city", city);
            intent.putExtra("district", district);
            startActivity(intent);
        });
    }

    /* 사용자가 위치 권한을 허용했는지 확인하는 메서드 */
//    void checkRunTimePermission()
//    {
//        // 런타임 퍼미션 처리
//        // 1. 위치 퍼미션을 가지고 있는지 체크
//        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        // 권한 2개를 다 허용했다면 아무 처리도 하지 않는다
//        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED)
//        {
//            // 2. 이미 퍼미션을 가지고 있다면
//            // (안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식함)
//            // 3. 위치 값을 가져올 수 있음
//        }
//        else
//        {  // 2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요하다. 2가지 경우(3-1, 4-1)가 있다
//
//            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0]))
//            {
//                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명한다
//                Toast.makeText(getActivity(), "내 주변 혜택 찾기 기능을 이용하시려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
//                // 3-3. 사용자게에 퍼미션 요청을 한다. 요청 결과는 onRequestPermissionResult에서 수신된다
//                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
//            }
//            else
//            {
//                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 한다
//                // 요청 결과는 onRequestPermissionResult에서 수신된다
//                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
//            }
//        }
//    }

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
            Toast.makeText(getActivity(), "잘못된 GPS 좌표입니다", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0)
        {
            Toast.makeText(getActivity(), "주소가 발견되지 않았습니다", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Log.e(TAG, "addresses 리스트 = " + addresses.toString());
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    public boolean checkLocationServicesStatus()
    {
        /* LocationManager : 시스템 위치 서비스에 대한 액세스 제공 */
        // Context의 위치(location) 정보를 LocationManager 객체에 저장한다
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        /* isProviderEnabled() : 지정된 provider의 현재 활성화 / 비활성화 상태를 리턴하는 메서드 */
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // 조회하기 버튼만 누르고 이동했다가 다시 돌아온 후, 다시 조회하기 버튼을 누르면 전체가 여러 개 찍히는 현상이 있다
        // 이 현상을 없애기 위해서 ArrayList.clear()로 리스트를 싹 비운다
        m_favorList.clear();

        // ResultBenefitActivity에서 뒤로가기 누를 시 버튼이 선택된 상태의 프래그먼트로 돌아오기 때문에, 버튼이 선택되지 않은 처음의 상태로 되돌리기 위해서 필요한 로직
        main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child));
        main_child_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_child.setSelected(false);

        main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student));
        main_student_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_student.setSelected(false);

        main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old));
        main_old_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_old.setSelected(false);

        main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy));
        main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_pregnancy.setSelected(false);

        main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder));
        main_disorder_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_disorder.setSelected(false);

        main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural));
        main_cultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_cultural.setSelected(false);

        main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural));
        main_multicultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_multicultural.setSelected(false);

        main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company));
        main_company_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_company.setSelected(false);

        main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law));
        main_law_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_law.setSelected(false);

        main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living));
        main_living_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_living.setSelected(false);

        main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job));
        main_job_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_job.setSelected(false);

        main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless));
        main_homeless_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_homeless.setSelected(false);

        main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc));
        main_etc_title.setTextColor(getResources().getColor(R.color.colorBlack));
        main_etc.setSelected(false);
    }

    private void init(View view)
    {
        main_content = view.findViewById(R.id.main_content);

        main_logo = view.findViewById(R.id.main_logo);
        more_layout = view.findViewById(R.id.more_layout);

        /* 더보기 버튼을 누르면 나타나야 하는 레이아웃 */
        multi_law_layout = view.findViewById(R.id.multi_law_layout);
        etc_layout = view.findViewById(R.id.etc_layout);

        main_ScrollView = view.findViewById(R.id.main_ScrollView);
        main_move_text = view.findViewById(R.id.main_move_text);

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
        main_etc = view.findViewById(R.id.main_etc);
        main_done = view.findViewById(R.id.main_done);

        main_child_img = view.findViewById(R.id.main_child_img);
        main_student_img = view.findViewById(R.id.main_student_img);
        main_old_img = view.findViewById(R.id.main_old_img);
        main_pregnancy_img = view.findViewById(R.id.main_pregnancy_img);
        main_disorder_img = view.findViewById(R.id.main_disorder_img);
        main_cultural_img = view.findViewById(R.id.main_cultural_img);
        main_multicultural_img = view.findViewById(R.id.main_multicultural_img);
        main_company_img = view.findViewById(R.id.main_company_img);
        main_law_img = view.findViewById(R.id.main_law_img);
        main_living_img = view.findViewById(R.id.main_living_img);
        main_job_img = view.findViewById(R.id.main_job_img);
        main_homeless_img = view.findViewById(R.id.main_homeless_img);
        main_etc_img = view.findViewById(R.id.main_etc_img);

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
        main_etc_title = view.findViewById(R.id.main_etc_title);

        map_btn = view.findViewById(R.id.map_btn);
    }

}