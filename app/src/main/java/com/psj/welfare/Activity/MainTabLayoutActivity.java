package com.psj.welfare.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MainViewPagerAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.fragment.MainFragment;
import com.psj.welfare.fragment.MyPageFragment;
import com.psj.welfare.fragment.PushGatherFragment;
import com.psj.welfare.fragment.SearchFragment;
import com.psj.welfare.util.LogUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 메인 화면 등 프래그먼트 4개를 보여줄 액티비티 */
public class MainTabLayoutActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    // 프래그먼트 별 화면을 표시할 뷰페이저
    ViewPager viewPager;

    Fragment mainFragment, mypageFragment, searchFragment, pushGatherFragment;

    // 백버튼을 누른 시간을 저장하기 위한 변수
    private long backKeyPressedTime = 0;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    private BroadcastReceiver mReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintablayout);

        // 애널리틱스 초기화
        analytics = FirebaseAnalytics.getInstance(this);

        sharedPreferences = getSharedPreferences("app_pref", 0);

        mainFragment = new MainFragment();
        searchFragment = new SearchFragment();
        mypageFragment = new MyPageFragment();
        pushGatherFragment = new PushGatherFragment();

        viewPager = findViewById(R.id.main_viewpager);
        TabLayout tabLayout = findViewById(R.id.main_tab_layout);

        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        Intent intent = getIntent();
        int ddd = intent.getIntExtra("push", -1);
        if (ddd == 100)
        {
            adapter = new MainViewPagerAdapter(getSupportFragmentManager());
            adapter.addFragment(new MainFragment(), "main");
            adapter.addFragment(new SearchFragment(), "search");
            adapter.addFragment(new PushGatherFragment(), "push");
            adapter.addFragment(new MyPageFragment(), "mypage");

            tabLayout.getTabAt(0).setIcon(R.drawable.home_icon_gray);
            tabLayout.getTabAt(1).setIcon(R.drawable.search_icon_gray);
            tabLayout.getTabAt(2).setIcon(R.drawable.alarm_red);
            tabLayout.getTabAt(3).setIcon(R.drawable.my_profile_icon_gray);
            viewPager.setCurrentItem(2);

            /* 알림 화면을 처음 보여주고 난 다음, 다른 탭을 누르면 이동할 수 있도록 한다 */
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
            {
                @Override
                public void onTabSelected(TabLayout.Tab tab)
                {
                    final int position = tab.getPosition();
                    if (position == 0)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.home_red);
                        reHomeLog("다른 화면에서 홈 화면으로 진입");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    }
                    else if (position == 1)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        tab.setIcon(R.drawable.search_red);
                        searchEnterLog("검색 화면 진입");
                    }
                    else if (position == 2)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.alarm_red);
                        alarmLog("알람 화면 진입");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
                        if (sharedPreferences.getString("push_status", "").equals("400") ||
                                sharedPreferences.getString("push_status", "").equals("500"))
                        {
                            // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
//                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (position == 3)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.mypage_red);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        mypageEnterLog("마이페이지 진입");
                    }
                }

                // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
                // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
                @Override
                public void onTabUnselected(TabLayout.Tab tab)
                {
                    int position = tab.getPosition();

                    if (position == 0)
                    {
                        tab.setIcon(R.drawable.home_icon_gray);
                    }
                    else if (position == 1)
                    {
                        tab.setIcon(R.drawable.search_icon_gray);
                    }
                    else if (position == 2)
                    {
                        tab.setIcon(R.drawable.alarm_icon_gray);
                    }
                    else if (position == 3)
                    {
                        tab.setIcon(R.drawable.my_profile_icon_gray);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab)
                {
                }
            });
        }
        else
        {
            /* 탭 선택했을 때 / 선택하지 않았을 때 탭 아이템에서 어떤 이미지를 보여줄지 처리하는 부분 */
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
            {
                @Override
                public void onTabSelected(TabLayout.Tab tab)
                {
                    final int position = tab.getPosition();
                    if (position == 0)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.home_red);
                        reHomeLog("다른 화면에서 홈 화면으로 진입");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    }
                    else if (position == 1)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        tab.setIcon(R.drawable.search_red);
                        searchEnterLog("검색 화면 진입");
                    }
                    else if (position == 2)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.alarm_red);
                        alarmLog("알람 화면 진입");
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
                        if (sharedPreferences.getString("push_status", "").equals("400") ||
                                sharedPreferences.getString("push_status", "").equals("500"))
                        {
                            // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
//                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (position == 3)
                    {
                        /* 인터넷 연결 체크 */
                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
                        if (!isConnected)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
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
                        tab.setIcon(R.drawable.mypage_red);
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                        mypageEnterLog("마이페이지 진입");
                    }
                }

                // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
                // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
                @Override
                public void onTabUnselected(TabLayout.Tab tab)
                {
                    int position = tab.getPosition();

                    if (position == 0)
                    {
                        tab.setIcon(R.drawable.home_icon_gray);
                    }
                    else if (position == 1)
                    {
                        tab.setIcon(R.drawable.search_icon_gray);
                    }
                    else if (position == 2)
                    {
                        tab.setIcon(R.drawable.alarm_icon_gray);
                    }
                    else if (position == 3)
                    {
                        tab.setIcon(R.drawable.my_profile_icon_gray);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab)
                {
                }
            });

            // 처음 들어왔을 때 하단 탭에서 보여줄 이미지를 ArrayList에 추가한다
            ArrayList<Integer> image = new ArrayList<>();
            image.add(R.drawable.home_red);
            image.add(R.drawable.search_icon_gray);
            image.add(R.drawable.alarm_icon_gray);
            image.add(R.drawable.my_profile_icon_gray);

            // for문으로 이미지가 든 ArrayList를 돌며 탭에 set
            for (int i = 0; i < image.size(); i++)
            {
                tabLayout.getTabAt(i).setIcon(image.get(i));
            }
        }

//        if (getIntent().getAction() != null)
//        {
//            Log.e(TAG, "받은 액션 : " + getIntent().getAction());
//            if (getIntent().getAction().equals("com.psj.welfare.push"))
//            {
//                Intent intents = getIntent();
//                String intentAction = intents.getAction();
//                Log.e(TAG, "intentAction : " + intentAction);
//                Log.e(TAG, "android.intent.action.MAIN 액션값 들어옴!!");
//                adapter = new MainViewPagerAdapter(getSupportFragmentManager());
//                adapter.addFragment(new MainFragment(), "main");
//                adapter.addFragment(new SearchFragment(), "search");
//                adapter.addFragment(new PushGatherFragment(), "push");
//                adapter.addFragment(new MyPageFragment(), "mypage");
//
//                tabLayout.getTabAt(0).setIcon(R.drawable.home_icon_gray);
//                tabLayout.getTabAt(1).setIcon(R.drawable.search_icon_gray);
//                tabLayout.getTabAt(2).setIcon(R.drawable.alarm_red);
//                tabLayout.getTabAt(3).setIcon(R.drawable.my_profile_icon_gray);
//                viewPager.setCurrentItem(2);
//
//                /* 알림 화면을 처음 보여주고 난 다음, 다른 탭을 누르면 이동할 수 있도록 한다 */
//                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//                {
//                    @Override
//                    public void onTabSelected(TabLayout.Tab tab)
//                    {
//                        final int position = tab.getPosition();
//                        if (position == 0)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.home_red);
//                            reHomeLog("다른 화면에서 홈 화면으로 진입");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        }
//                        else if (position == 1)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            tab.setIcon(R.drawable.search_red);
//                            searchEnterLog("검색 화면 진입");
//                        }
//                        else if (position == 2)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.alarm_red);
//                            alarmLog("알람 화면 진입");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
//                            if (sharedPreferences.getString("push_status", "").equals("400") ||
//                                    sharedPreferences.getString("push_status", "").equals("500"))
//                            {
//                                // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
////                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        else if (position == 3)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.mypage_red);
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            mypageEnterLog("마이페이지 진입");
//                        }
//                    }
//
//                    // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
//                    // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
//                    @Override
//                    public void onTabUnselected(TabLayout.Tab tab)
//                    {
//                        int position = tab.getPosition();
//
//                        if (position == 0)
//                        {
//                            tab.setIcon(R.drawable.home_icon_gray);
//                        }
//                        else if (position == 1)
//                        {
//                            tab.setIcon(R.drawable.search_icon_gray);
//                        }
//                        else if (position == 2)
//                        {
//                            tab.setIcon(R.drawable.alarm_icon_gray);
//                        }
//                        else if (position == 3)
//                        {
//                            tab.setIcon(R.drawable.my_profile_icon_gray);
//                        }
//                    }
//
//                    @Override
//                    public void onTabReselected(TabLayout.Tab tab)
//                    {
//                    }
//                });
//            }
//            else
//            {
//                /* 탭 선택했을 때 / 선택하지 않았을 때 탭 아이템에서 어떤 이미지를 보여줄지 처리하는 부분 */
//                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//                {
//                    @Override
//                    public void onTabSelected(TabLayout.Tab tab)
//                    {
//                        final int position = tab.getPosition();
//                        if (position == 0)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.home_red);
//                            reHomeLog("다른 화면에서 홈 화면으로 진입");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        }
//                        else if (position == 1)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            tab.setIcon(R.drawable.search_red);
//                            searchEnterLog("검색 화면 진입");
//                        }
//                        else if (position == 2)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.alarm_red);
//                            alarmLog("알람 화면 진입");
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
//                            if (sharedPreferences.getString("push_status", "").equals("400") ||
//                                    sharedPreferences.getString("push_status", "").equals("500"))
//                            {
//                                // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
////                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        else if (position == 3)
//                        {
//                            /* 인터넷 연결 체크 */
//                            isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                            if (!isConnected)
//                            {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                                builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                        .setCancelable(false)
//                                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                        {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which)
//                                            {
//                                                dialog.dismiss();
//                                                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                                startActivity(intent);
//                                            }
//                                        }).show();
//                            }
//                            tab.setIcon(R.drawable.mypage_red);
//                            Bundle bundle = new Bundle();
//                            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
//                            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                            mypageEnterLog("마이페이지 진입");
//                        }
//                    }
//
//                    // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
//                    // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
//                    @Override
//                    public void onTabUnselected(TabLayout.Tab tab)
//                    {
//                        int position = tab.getPosition();
//
//                        if (position == 0)
//                        {
//                            tab.setIcon(R.drawable.home_icon_gray);
//                        }
//                        else if (position == 1)
//                        {
//                            tab.setIcon(R.drawable.search_icon_gray);
//                        }
//                        else if (position == 2)
//                        {
//                            tab.setIcon(R.drawable.alarm_icon_gray);
//                        }
//                        else if (position == 3)
//                        {
//                            tab.setIcon(R.drawable.my_profile_icon_gray);
//                        }
//                    }
//
//                    @Override
//                    public void onTabReselected(TabLayout.Tab tab)
//                    {
//                    }
//                });
//
//                // 처음 들어왔을 때 하단 탭에서 보여줄 이미지를 ArrayList에 추가한다
//                ArrayList<Integer> image = new ArrayList<>();
//                image.add(R.drawable.home_red);
//                image.add(R.drawable.search_icon_gray);
//                image.add(R.drawable.alarm_icon_gray);
//                image.add(R.drawable.my_profile_icon_gray);
//
//                // for문으로 이미지가 든 ArrayList를 돌며 탭에 set
//                for (int i = 0; i < image.size(); i++)
//                {
//                    tabLayout.getTabAt(i).setIcon(image.get(i));
//                }
//            }
//        }
//        else
//        {
//            /* 탭 선택했을 때 / 선택하지 않았을 때 탭 아이템에서 어떤 이미지를 보여줄지 처리하는 부분 */
//            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//            {
//                @Override
//                public void onTabSelected(TabLayout.Tab tab)
//                {
//                    final int position = tab.getPosition();
//                    if (position == 0)
//                    {
//                        /* 인터넷 연결 체크 */
//                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                        if (!isConnected)
//                        {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                            builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                    .setCancelable(false)
//                                    .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                    {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            dialog.dismiss();
//                                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                            startActivity(intent);
//                                        }
//                                    }).show();
//                        }
//                        tab.setIcon(R.drawable.home_red);
//                        reHomeLog("다른 화면에서 홈 화면으로 진입");
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
//                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                    }
//                    else if (position == 1)
//                    {
//                        /* 인터넷 연결 체크 */
//                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                        if (!isConnected)
//                        {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                            builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                    .setCancelable(false)
//                                    .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                    {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            dialog.dismiss();
//                                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                            startActivity(intent);
//                                        }
//                                    }).show();
//                        }
//                        Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
//                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        tab.setIcon(R.drawable.search_red);
//                        searchEnterLog("검색 화면 진입");
//                    }
//                    else if (position == 2)
//                    {
//                        /* 인터넷 연결 체크 */
//                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                        if (!isConnected)
//                        {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                            builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                    .setCancelable(false)
//                                    .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                    {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            dialog.dismiss();
//                                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                            startActivity(intent);
//                                        }
//                                    }).show();
//                        }
//                        tab.setIcon(R.drawable.alarm_red);
//                        alarmLog("알람 화면 진입");
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
//                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
//                        if (sharedPreferences.getString("push_status", "").equals("400") ||
//                                sharedPreferences.getString("push_status", "").equals("500"))
//                        {
//                            // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
////                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    else if (position == 3)
//                    {
//                        /* 인터넷 연결 체크 */
//                        isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                        if (!isConnected)
//                        {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                            builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                    .setCancelable(false)
//                                    .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                    {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which)
//                                        {
//                                            dialog.dismiss();
//                                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                            startActivity(intent);
//                                        }
//                                    }).show();
//                        }
//                        tab.setIcon(R.drawable.mypage_red);
//                        Bundle bundle = new Bundle();
//                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
//                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                        mypageEnterLog("마이페이지 진입");
//                    }
//                }
//
//                // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
//                // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
//                @Override
//                public void onTabUnselected(TabLayout.Tab tab)
//                {
//                    int position = tab.getPosition();
//
//                    if (position == 0)
//                    {
//                        tab.setIcon(R.drawable.home_icon_gray);
//                    }
//                    else if (position == 1)
//                    {
//                        tab.setIcon(R.drawable.search_icon_gray);
//                    }
//                    else if (position == 2)
//                    {
//                        tab.setIcon(R.drawable.alarm_icon_gray);
//                    }
//                    else if (position == 3)
//                    {
//                        tab.setIcon(R.drawable.my_profile_icon_gray);
//                    }
//                }
//
//                @Override
//                public void onTabReselected(TabLayout.Tab tab)
//                {
//                }
//            });
//
//            // 처음 들어왔을 때 하단 탭에서 보여줄 이미지를 ArrayList에 추가한다
//            ArrayList<Integer> image = new ArrayList<>();
//            image.add(R.drawable.home_red);
//            image.add(R.drawable.search_icon_gray);
//            image.add(R.drawable.alarm_icon_gray);
//            image.add(R.drawable.my_profile_icon_gray);
//
//            // for문으로 이미지가 든 ArrayList를 돌며 탭에 set
//            for (int i = 0; i < image.size(); i++)
//            {
//                tabLayout.getTabAt(i).setIcon(image.get(i));
//            }
//        }

//        /* 탭 선택했을 때 / 선택하지 않았을 때 탭 아이템에서 어떤 이미지를 보여줄지 처리하는 부분 */
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
//        {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab)
//            {
//                final int position = tab.getPosition();
//                if (position == 0)
//                {
//                    /* 인터넷 연결 체크 */
//                    isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                    if (!isConnected)
//                    {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                .setCancelable(false)
//                                .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        dialog.dismiss();
//                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                        startActivity(intent);
//                                    }
//                                }).show();
//                    }
//                    tab.setIcon(R.drawable.home_red);
//                    reHomeLog("다른 화면에서 홈 화면으로 진입");
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 홈 프래그먼트로 진입");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                }
//                else if (position == 1)
//                {
//                    /* 인터넷 연결 체크 */
//                    isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                    if (!isConnected)
//                    {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                .setCancelable(false)
//                                .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        dialog.dismiss();
//                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                        startActivity(intent);
//                                    }
//                                }).show();
//                    }
//                    Log.e("SearchFragment", "MainTabLayoutActivity - 검색 아이콘 클릭");
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 검색 프래그먼트로 진입");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                    tab.setIcon(R.drawable.search_red);
//                    searchEnterLog("검색 화면 진입");
//                }
//                else if (position == 2)
//                {
//                    /* 인터넷 연결 체크 */
//                    isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                    if (!isConnected)
//                    {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                .setCancelable(false)
//                                .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        dialog.dismiss();
//                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                        startActivity(intent);
//                                    }
//                                }).show();
//                    }
//                    tab.setIcon(R.drawable.alarm_red);
//                    alarmLog("알람 화면 진입");
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 알람 모아보는 프래그먼트로 진입");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                    // 서버에서 전송받은 상태 메시지가 400, 500인 경우 받은 알림이 없다는 메시지를 띄운다
//                    if (sharedPreferences.getString("push_status", "").equals("400") ||
//                            sharedPreferences.getString("push_status", "").equals("500"))
//                    {
//                        // TODO : 받은 알림이 없으면 알림이 없다는 화면을 띄워야 한다 (ios와 맞추기)
////                        Toast.makeText(MainTabLayoutActivity.this, "현재 받은 알림이 없습니다", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                else if (position == 3)
//                {
//                    /* 인터넷 연결 체크 */
//                    isConnected = isNetworkConnected(MainTabLayoutActivity.this);
//                    if (!isConnected)
//                    {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(MainTabLayoutActivity.this);
//                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                .setCancelable(false)
//                                .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        dialog.dismiss();
//                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                        startActivity(intent);
//                                    }
//                                }).show();
//                    }
//                    tab.setIcon(R.drawable.mypage_red);
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "다른 프래그먼트에서 마이페이지 프래그먼트로 진입");
//                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                    mypageEnterLog("마이페이지 진입");
//                }
//            }
//
//            // 하단 탭 선택 후 다른 탭을 선택하면 호출되는 메서드
//            // 검색 누른 후 알림 누르면 124번 줄의 else if문이 작동한다
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab)
//            {
//                int position = tab.getPosition();
//
//                if (position == 0)
//                {
//                    tab.setIcon(R.drawable.home_icon_gray);
//                }
//                else if (position == 1)
//                {
//                    tab.setIcon(R.drawable.search_icon_gray);
//                }
//                else if (position == 2)
//                {
//                    tab.setIcon(R.drawable.alarm_icon_gray);
//                }
//                else if (position == 3)
//                {
//                    tab.setIcon(R.drawable.my_profile_icon_gray);
//                }
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab)
//            {
//            }
//        });
//
//        // 처음 들어왔을 때 하단 탭에서 보여줄 이미지를 ArrayList에 추가한다
//        ArrayList<Integer> image = new ArrayList<>();
//        image.add(R.drawable.home_red);
//        image.add(R.drawable.search_icon_gray);
//        image.add(R.drawable.alarm_icon_gray);
//        image.add(R.drawable.my_profile_icon_gray);
//
//        // for문으로 이미지가 든 ArrayList를 돌며 탭에 set
//        for (int i = 0; i < image.size(); i++)
//        {
//            tabLayout.getTabAt(i).setIcon(image.get(i));
//        }

    }

    /* 마이페이지에 들어갔을 때 이벤트 내용을 서버로 전송하는 메서드 */
    void mypageEnterLog(String user_action)
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
        String action = userAction(user_action);
        Call<String> call = apiInterface.userLog(token, session, "myPage", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "마이페이지 진입 로그 전송 결과 : " + result);
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

    /* 검색 화면에 들어갔을 때 해당 이벤트 내용을 서버로 전송하는 메서드 */
    void searchEnterLog(String search_action)
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
        String action = userAction(search_action);
        Call<String> call = apiInterface.userLog(token, session, "search", action, null, LogUtil.getUserLog());
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

    /* 알림 모아보는 화면에 들어갔을 때 해당 이벤트 내용을 서버로 전송하는 메서드 */
    void alarmLog(String alarm_action)
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
        String action = userAction(alarm_action);
        Call<String> call = apiInterface.userLog(token, session, "push_list", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "알림 모아보는 화면으로 이동하는 로그 전송 결과 : " + result);
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

    /* 다시 홈 화면으로 이동했을 경우 해당 이벤트 내용을 서버로 전송하는 메서드 */
    void reHomeLog(String home_action)
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
        String action = userAction(home_action);
        Call<String> call = apiInterface.userLog(token, session, "home", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "다시 홈 화면으로 이동 로그 전송 결과 : " + result);
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

    /* 백버튼 눌렀을 때 일정 시간이 지난 후 백버튼을 눌러야 앱이 종료되도록 한다 */
    @Override
    public void onBackPressed()
    {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000)
        {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2초 이내에 뒤로가기 버튼을 한번 더 클릭 시 앱 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000)
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "앱 종료");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            finish();
        }
    }

    /* 인터넷 연결 체크하는 메서드 */
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

    /* https://stackoverflow.com/questions/26608627/how-to-open-fragment-page-when-pressed-a-notification-in-android
    * 위 링크 참고해 만든 메서드 */
    public void openFragmentWithoutAnimation(Context context, int replaceLayout, Bundle bundle, Class<?> fragment, String fragmentTag)
    {
        Fragment dynamicFragment = null;
        try
        {
            Class<?> clazz = Class.forName(fragment.getName());
            dynamicFragment = (Fragment) clazz.newInstance();
            System.out.println(clazz.getSuperclass());
            System.out.println(clazz.getName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        AppCompatActivity activity = null;
        if (context instanceof AppCompatActivity)
        {
            activity = ((AppCompatActivity) context);
        }
        if (activity.getSupportFragmentManager().findFragmentByTag(fragmentTag) == null)
        {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            if (bundle != null)
            {
                dynamicFragment.setArguments(bundle);
            }
            transaction.add(replaceLayout, dynamicFragment, fragmentTag);
            transaction.addToBackStack(fragmentTag);
            transaction.commit();
        }

    }

}