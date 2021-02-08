package com.psj.welfare.fragment;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.psj.welfare.Data.HorizontalYoutubeItem;
import com.psj.welfare.Data.RecommendItem;
import com.psj.welfare.R;
import com.psj.welfare.Test.TestViewPagerAdapter;
import com.psj.welfare.Test.TestViewPagerData;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.activity.MapActivity;
import com.psj.welfare.activity.YoutubeActivity;
import com.psj.welfare.adapter.HorizontalYoutubeAdapter;
import com.psj.welfare.adapter.RecommendAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.GpsTracker;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;

/* 앱 메인 페이지. 유튜브 혜택 소개, 맞춤 혜택 목록을 볼 수 있다 */
public class MainFragment extends Fragment
{
    public static final String TAG = "MainFragment"; // 로그 찍을 때 사용하는 TAG

    CardView main_fragment_cardview;

    // 구글 로그인 테스트 위한 카톡 탈퇴 버튼
    Button kakao_unlink_btn;
    Button kakao_logout_btn;

    // 지도 버튼
    Button map_btn;

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

    // 가로로 유튜브 영상들을 보여줄 리사이클러뷰
    RecyclerView youtube_video_recyclerview;
    HorizontalYoutubeAdapter adapter;
    HorizontalYoutubeAdapter.ItemClickListener itemClickListener;
    List<HorizontalYoutubeItem> lists;
    // 유튜브 영상 데이터를 파싱할 때 사용할 변수
    String thumbnail, title, videoId;

    // 서버에서 받아 저장한 토큰값을 저장할 때 사용하는 쉐어드
    SharedPreferences sharedPreferences;

    // 서버에서 받은 값을 저장할 때 사용하는 변수
    String welf_name, welf_local, welf_category, tag, count;

    // 가로로 맞춤 혜택들을 보여줄 리사이클러뷰, 어댑터, 아이템 클릭 리스너, 리스트
    RecyclerView recom_recycler;
    RecommendAdapter recommendAdapter;
    RecommendAdapter.ItemClickListener recom_itemClickListener;
    List<RecommendItem> list;

    // 테스트용 뷰페이저
    ViewPager2 viewPager2;

    // 화면 우상단의 혜택찾기 버튼
    Button find_welfare_btn;

    // 카카오 이메일 담을 변수
    String kakao_email;

    // 리사이클러뷰에 어댑터가 없으면(비로그인 시) 맞춤 혜택을 보여주지 않기 때문에, 맞춤 혜택란을 설명하는 텍스트뷰를 가려야 한다
    public String status = "";
    TextView recommend_welfare_textview, recommend_welfare_count;

    // YoutubeActivity로 이동할 때 선택한 영상을 재생하는 데 사용할 HashMap
    HashMap<String, String> youtube_hashmap;

    int count_int;

    // 로그아웃 상태 체크에 사용할 변수
    boolean loggedOut = false;

    String encode_str;

    public MainFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

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

        recom_recycler = view.findViewById(R.id.recom_recycler);
        recommend_welfare_textview = view.findViewById(R.id.recommend_welfare_textview);
        recommend_welfare_count = view.findViewById(R.id.recommend_welfare_count);
        // 로그인해서 카톡 정보가 있다면 유저이름을 따와서 텍스트뷰에 넣는다
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);

        if (!sharedPreferences.getString("nickname", "").equals(""))
        {
            String kakao_name = sharedPreferences.getString("nickname", "");
            recommend_welfare_textview.setText(kakao_name + "님에게");
            // 아이디, 개수 색깔 강조
            SpannableString spannableString = new SpannableString(recommend_welfare_textview.getText().toString());
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            recommend_welfare_textview.setText(spannableString);
        }
        // if문으로 맞춤 혜택 받아온 개수를 뽑아 10 미만인 경우, 10 이상인 경우 별로 각각 다른 위치를 강조해야 한다
//        if (Integer.parseInt(count) < 10)
//        {
//            SpannableString count_span = new SpannableString(recommend_welfare_count.getText().toString());
//            count_span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            recommend_welfare_count.setText(count_span);
//        }
//        else if (Integer.parseInt(count) > 10)
//        {
//            SpannableString count_span = new SpannableString(recommend_welfare_count.getText().toString());
//            count_span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 5, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            recommend_welfare_count.setText(count_span);
//        }

        youtube_hashmap = new HashMap<>();
        main_fragment_cardview = view.findViewById(R.id.main_fragment_cardview);
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        if (!sharedPreferences.getString("user_category", "").equals(""))
        {
            main_fragment_cardview.setVisibility(View.GONE);
        }
        // 로그아웃한 상태고 닉네임이 없으면 혜택찾기 버튼 있는 카드뷰를 보이게 한다
        if (sharedPreferences.getBoolean("logout", false) || sharedPreferences.getString("user_nickname", "").equals(""))
        {
            main_fragment_cardview.setVisibility(View.VISIBLE);
            recommend_welfare_textview.setVisibility(View.GONE);
            recommend_welfare_count.setVisibility(View.GONE);
            recom_recycler.setVisibility(View.GONE);
        }
        // logout이 false면 로그인한 상태기 때문에 맞춤혜택들을 보여주고 혜택찾기 버튼을 없앤다
        else if (!sharedPreferences.getBoolean("logout", false))
        {
            main_fragment_cardview.setVisibility(View.GONE);
            recommend_welfare_textview.setVisibility(View.VISIBLE);
            recommend_welfare_count.setVisibility(View.VISIBLE);
            recom_recycler.setVisibility(View.VISIBLE);
        }
        map_btn = view.findViewById(R.id.map_btn);
        find_welfare_btn = view.findViewById(R.id.find_welfare_btn);

        // 비로그인 시에만 보이는 레이아웃, 누르면 맞춤 혜택 찾으러 가겠냐는 다이얼로그를 띄우고 예를 누르면 이동시킨다
        find_welfare_btn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("맞춤 혜택을 확인하시려면 먼저 로그인이 필요해요\n로그인하시겠어요?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            if (sharedPreferences.getBoolean("logout", false))
                            {
                                // logout이 true면 로그아웃한 상태니까 true
                                loggedOut = true;
                                intent.putExtra("loggedOut", loggedOut);
                                startActivity(intent);
                            }
                            else
                            {
                                loggedOut = false;
                                intent.putExtra("loggedOut", loggedOut);
                                startActivity(intent);
                            }
                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            }).show();
        });

        /* 비로그인 때만 보이는 뷰페이저 */
        viewPager2 = view.findViewById(R.id.test_view_pager2);
        ArrayList<TestViewPagerData> list = new ArrayList<>();
        list.add(new TestViewPagerData("당신이 놓치고 있는\n700개의 여성 혜택"));
        list.add(new TestViewPagerData("당신을 위한 900개의 혜택\n바로 확인해 보세요"));
        viewPager2.setAdapter(new TestViewPagerAdapter(getActivity(), list));

        // 사용자 기본 정보(나이, 성별, 지역)가 있는 경우 & 로그인한 경우 뷰페이저를 gone으로 돌린다
        if (!sharedPreferences.getString("user_area", "").equals("") || !sharedPreferences.getString("user_nickname", "").equals("")
                || !sharedPreferences.getString("user_gender", "").equals(""))
        {
            viewPager2.setVisibility(View.GONE);
        }

        /* 유튜브 영상을 보여주는 가로 리사이클러뷰 선언, 처리 */
//        getYoutubeInformation(); // onResume()으로 이동
        youtube_video_recyclerview = view.findViewById(R.id.youtube_video_recyclerview);
        youtube_video_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));

        /* 맞춤 혜택 보여주는 가로 리사이클러뷰 선언, 처리 */
        recom_recycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        /* 맞춤 혜택을 위한 개인정보, 관심사 데이터가 없다면 맞춤 혜택을 보여주지 말아야 한다 */
        if (sharedPreferences.getString("user_category", "").equals("") || sharedPreferences.getString("user_area", "").equals("")
                || sharedPreferences.getString("user_age", "").equals("") || sharedPreferences.getString("user_gender", "").equals(""))
        {
            recommend_welfare_textview.setVisibility(View.GONE);
            recommend_welfare_count.setVisibility(View.GONE);
            recom_recycler.setVisibility(View.GONE);
        }

        /* 카톡 로그아웃 버튼, 로그아웃하면 로그인 페이지로 리다이렉트하도록 한다? */
        kakao_logout_btn = view.findViewById(R.id.kakao_logout_btn);
        kakao_logout_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Toast.makeText(getActivity(), "로그아웃 클릭", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "로그아웃 클릭");
                if (Session.getCurrentSession().getTokenInfo().getAccessToken() != null)
                {
                    String aaa = Session.getCurrentSession().getTokenInfo().getAccessToken();
                    Log.e("로그아웃 이후 카카오 토큰 상태", aaa);
                    UserManagement.getInstance().requestLogout(new LogoutResponseCallback()
                    {
                        @Override
                        public void onCompleteLogout()
                        {
                            Log.e(TAG, "로그아웃 성공");
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                }
                else
                {
                    Log.e(TAG, "카카오 토큰 없음");
                }
            }
        });

        /* 카톡 회원탈퇴 버튼. 구글 로그인 테스트 위해 집어넣었음 */
        kakao_unlink_btn = view.findViewById(R.id.kakao_unlink_btn);
        kakao_unlink_btn.setOnClickListener(v ->
        {
            final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
            new AlertDialog.Builder(getActivity()).setMessage(appendMessage).setPositiveButton(getString(R.string.com_kakao_ok_button), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback()
                    {
                        @Override
                        public void onFailure(ErrorResult errorResult)
                        {
                            Log.e("다이얼로그 안", "회원탈퇴 실패");
                        }

                        @Override
                        public void onSessionClosed(ErrorResult errorResult)
                        {
                            Log.e("다이얼로그 안", "onSessionClosed");
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }

                        @Override
                        public void onNotSignedUp()
                        {
                            Log.e("다이얼로그 안", "onNotSignedUp");
                        }

                        @Override
                        public void onSuccess(Long userId)
                        {
                            Log.e(TAG, "카톡 탈퇴 성공");
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                    });
                    dialog.dismiss();
                    getActivity().finish();
                }
            }).setNegativeButton(getString(R.string.com_kakao_cancel_button), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            }).show();
        });

        // 내 주변 혜택 찾기 버튼
        map_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
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
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra("user_area", user_area);
                intent.putExtra("city", city);
                intent.putExtra("district", district);
                startActivity(intent);
            }
        });

        Log.e(TAG, "메인에서 count_int = " + count_int);
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

    /* 유저 관심사에 따라 관련 혜택들을 보여주는 메서드, 처음에 액티비티가 켜질 때 혜택들을 제대로 받아오지 못하는 경우가 있다
    * 로그인 해야 맞춤 혜택이 보이도록 처리해야 함 */
    void userOrderedWelfare()
    {
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        encode("메인 화면 진입");
        Log.e(TAG, "URLEncoder : " + encode_str);
        Call<String> call = apiInterface.userOrderedWelfare(token, "customized", LogUtil.getUserLog());   // 2번 인자는 customized로 고정이다
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "관심사 따른 혜택 가져오기 : " + result);
                    recommendParsing(result);
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

    /* 맞춤 혜택 파싱 */
    private void recommendParsing(String result)
    {
        list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            count = jsonObject.getString("TotalCount");
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                welf_local = inner_obj.getString("welf_local");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");

                RecommendItem item = new RecommendItem();
                item.setWelf_name(welf_name);
                item.setWelf_local(welf_local);
                item.setWelf_category(welf_category);
                item.setTag(tag);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (count != null)
        {
            count_int = Integer.parseInt(count);
        }
        if (status.equals(""))
        {
            recommend_welfare_textview.setVisibility(View.GONE);
            recommend_welfare_count.setVisibility(View.GONE);
            recom_recycler.setVisibility(View.GONE);
        }
        // 맞춤 혜택 보여주는 가로 리사이클러뷰의 어댑터 설정
        recommendAdapter = new RecommendAdapter(getActivity(), list, recom_itemClickListener);
        recommendAdapter.setOnItemClickListener((view, pos) ->
        {
            String name = list.get(pos).getWelf_name();
            // 맞춤 혜택 클릭 시 혜택 상세보기 페이지로 혜택명을 갖고 이동한다
            // 혜택명은 상세보기 페이지에서 서버로 넘겨 혜택의 내용들을 가져올 때 사용한다
            Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", list.get(pos).getWelf_local());
            startActivity(intent);
        });
        recom_recycler.setAdapter(recommendAdapter);

        if (count_int > 9)
        {
            SpannableString count_span = new SpannableString(recommend_welfare_count.getText().toString());
            count_span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 5, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            recommend_welfare_count.setText(count_span);
        }
        else
        {
            SpannableString count_span = new SpannableString(recommend_welfare_count.getText().toString());
            count_span.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            recommend_welfare_count.setText(count_span);
        }
    }

    /* 유튜브 영상 정보 가져오는 메서드 */
    void getYoutubeInformation()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String session = sharedPreferences.getString("sessionId", "");
        encode("유튜브 영상 가져오기");
        Call<String> call = apiInterface.getYoutubeInformation();
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    jsonParsing(result);
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

    /* JSON 형태의 String을 파싱해 리사이클러뷰에 각각 유튜브 영상 데이터를 뿌리는 메서드 */
    private void jsonParsing(String result)
    {
        lists = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                thumbnail = inner_obj.getString("thumbnail");
                title = inner_obj.getString("title");
                videoId = inner_obj.getString("videoId");
                // 리사이클러뷰에 JSONObject의 개수만큼 데이터를 뿌리기 위해 객체, 리스트, setter 활용
                HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                item.setYoutube_name(title);
                item.setYoutube_thumbnail(thumbnail);
                item.setYoutube_id(videoId);
                lists.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < lists.size(); i++)
        {
            // for문을 돌면서 hashmap의 키값으로 video_id, value로 영상 제목을 넣는다
            youtube_hashmap.put(lists.get(i).getYoutube_name(), lists.get(i).getYoutube_id());
        }
        adapter = new HorizontalYoutubeAdapter(getActivity(), lists, itemClickListener);
        // MainFragment에선 썸넬과 영상 제목만 알려주기 때문에, 썸넬을 클릭하면 액티비티를 이동해 그곳에서 유튜브 영상을 틀어준다
        adapter.setOnItemClickListener(new HorizontalYoutubeAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                Intent intent = new Intent(getActivity(), YoutubeActivity.class);
                String youtube_name = lists.get(position).getYoutube_name();
                intent.putExtra("youtube_name", youtube_name);
                intent.putExtra("youtube_hashmap", youtube_hashmap);
//                Log.e(TAG, "getYoutube_name() : " + videoId);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        youtube_video_recyclerview.setAdapter(adapter);

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

    /* API 사용 후 결과를 확인할 때 사용하는 메서드 */
    private HttpLoggingInterceptor httpLoggingInterceptor()
    {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger()
        {
            @Override
            public void log(String message)
            {
                Log.e("인터셉터 내용 : ", message + "");
            }
        });

        return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getYoutubeInformation();
        userOrderedWelfare();
    }
}