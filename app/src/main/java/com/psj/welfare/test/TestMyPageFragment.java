package com.psj.welfare.test;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.activity.BookmarkCheckActivity;
import com.psj.welfare.activity.ChooseFirstInterestActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.activity.PersonalInformationActivity;
import com.psj.welfare.activity.SplashActivity;
import com.psj.welfare.activity.TermsAndConditionsActivity;
import com.psj.welfare.activity.WithdrawActivity;
import com.psj.welfare.activity.WrittenReviewCheckActivity;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomEditNicknameDialog;
import com.psj.welfare.custom.MyDialogListener;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestMyPageFragment extends Fragment
{
    private final String TAG = "TestMyPageFragment";
    // 데이터 바인딩 적용
    private com.psj.welfare.databinding.TestMyPageFragment binding;


    SharedPreferences sharedPreferences;
    boolean isLogout;

    DBOpenHelper helper;
    String sqlite_token, message;
    String receivedNickname = "";

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    public TestMyPageFragment()
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
        // 프래그먼트에서 데이터 바인딩을 쓰려면 onCreateView()에서 바인딩 처리를 해줘야 한다
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_my_page, container, false);

        // 마지막으로 getRoot()로 바인딩과 관련된 레이아웃 파일의 가장 바깥쪽 뷰(=부모 레이아웃)를 리턴한다
        // 이후 onCreateView() 다음 호출되는 onViewCreated()에서 binding 변수를 통해 findViewById()를 하지 않고도 뷰를 참조할 수 있다
        return binding.getRoot();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        helper = new DBOpenHelper(getActivity());
        helper.openDatabase();
        helper.create();

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());

        binding.searchFragmentTopTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.035)); //"마이페이지" 텍스트
        binding.mypageMyId.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.03)); //닉네임 텍스트
        binding.nicknameEditImage.getLayoutParams().width = (int) (size.y * 0.03); //닉네임 변경 아이콘
        binding.nicknameEditImage.getLayoutParams().height = (int) (size.y * 0.03); //닉네임 변경 아이콘

        binding.bookmarkTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.02)); //"북마크 혜택" 텍스트
        binding.recentWelfareTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.02)); //"최근본 혜택" 텍스트
        binding.writtenReviewTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.02)); //"작성한 리뷰" 텍스트

        binding.editInterestTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"관심사 선택"
        binding.editInterestTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.pushSettingText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"푸시 알림 선택"
        binding.mypagePushLayout.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.checkNoticeTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"공지사항 확인"
        binding.checkNoticeTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.checkTermTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"이용약관 확인"
        binding.checkTermTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.checkPrivacyTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"개인정보 처리방침 확인"
        binding.checkPrivacyTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.appVersionTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"버전 2.0.0"
        binding.appVersionTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.mypageLogoutTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"로그아웃"
        binding.mypageLogoutTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));

        binding.mypageWithdrawTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22); //"탈퇴하기"
        binding.mypageWithdrawTextview.setPadding((int) (size.x * 0.04),(int) (size.y * 0.02),(int) (size.x * 0.04),(int) (size.y * 0.02));


        binding.mypageLoginButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 21);


        checkMyNickname("show_name");

        /* 쉐어드에서 로그인 상태값을 가져와 로그인, 비로그인일 경우 보이는 UI를 각각 다르게 해야 한다 */
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 비로그인, 로그인 상태에 따라 다른 뷰를 보여주는 처리부
        isLogout = sharedPreferences.getBoolean("logout", false);
        if (isLogout)
        {
            Log.e(TAG, "로그인하지 않음");
            notLoginView();
        }
        else
        {
            Log.e(TAG, "로그인함");
            loginView();
        }

        // 기기 설정값에 따라 스위치의 T/F 값을 바꿔 보여주는 처리부
        boolean isAllowed = areNotificationsEnabled();
        if (isAllowed)
        {
            binding.mypageNotiSwitch.setChecked(true);
            putPushSetting(true);
            editor.putBoolean("fcm_canceled", true);
            editor.apply();
        }
        else
        {
            binding.mypageNotiSwitch.setChecked(false);
            putPushSetting(false);
            editor.putBoolean("fcm_canceled", false);
            editor.apply();
        }

        /* 데이터 바인딩을 적용했기 때문에 앞에 "binding."을 붙이고 뷰, 위젯의 이름들을 파스칼 표기법으로 입력해야 한다 */
        // 닉네임 수정 이미지
        binding.nicknameEditImage.setOnClickListener(v ->
        {
            /* 커스텀 다이얼로그를 호출해서 닉네임 변경 */
            CustomEditNicknameDialog dialog = new CustomEditNicknameDialog(getActivity());
            dialog.setDialogListener(new MyDialogListener()
            {
                @Override
                public void onDuplicatedCheck(boolean isDuplicated)
                {
                    Log.e(TAG, "닉네임 중복 여부 확인 : " + isDuplicated);
                }

                @SuppressLint("CheckResult")
                @Override
                public void onPositiveClicked(String edited_str)
                {
                    Log.e(TAG, "다이얼로그에서 가져온 문자열 : " + edited_str);
                    receivedNickname = edited_str;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("changed_nickname", receivedNickname);
                    editor.apply();
                    changeNickname(receivedNickname, "save");
                }
            });
            dialog.showDialog();
        });

        // 로그인
        binding.mypageLoginButton.setOnClickListener(v ->
        {
            sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
            SharedPreferences.Editor editor2 = sharedPreferences.edit();
            editor2.putBoolean("logout", true);
            editor2.apply();
            userLog("로그인 화면으로 이동");
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // 로그아웃
        binding.mypageLogoutTextview.setOnClickListener(v ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("로그아웃 하시겠어요?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            userLog("로그아웃 클릭");
                            if (Session.getCurrentSession().getTokenInfo().getAccessToken() != null)
                            {
                                sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                UserManagement.getInstance().requestLogout(new LogoutResponseCallback()
                                {
                                    @Override
                                    public void onCompleteLogout()
                                    {
                                        editor.putBoolean("logout", true);
                                        editor.remove("user_nickname");
                                        editor.apply();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "로그아웃 클릭");
                                        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                                        Intent intent = new Intent(getActivity(), SplashActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                });
                            }
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).show();
        });

        // 푸시 알림 설정하러 기기의 설정 화면으로 이동
        binding.mypagePushLayout.setOnClickListener(v ->
        {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "ch_push");
            startActivity(intent);
        });

        // 이용약관 확인
        binding.checkTermTextview.setOnClickListener(v ->
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "이용약관 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        // 개인정보 처리방침
        binding.checkPrivacyTextview.setOnClickListener(v ->
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "개인정보 처리방침 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
            startActivity(intent);
        });

        // 북마크 혜택
        binding.bookmarkWelfareLayout.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "북마크 혜택 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), BookmarkCheckActivity.class);
            startActivity(intent);
        });

        // 작성한 리뷰
        binding.writtenReviewLayout.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "작성한 리뷰 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), WrittenReviewCheckActivity.class);
            startActivity(intent);
        });

        // 관심사 선택
        binding.editInterestTextview.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "관심사 선택 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), ChooseFirstInterestActivity.class);
            startActivity(intent);
        });

        // 탈퇴하기
        binding.mypageWithdrawTextview.setOnClickListener(v ->
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "탈퇴하기 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            Intent intent = new Intent(getActivity(), WithdrawActivity.class);
            startActivity(intent);
        });

    }

    // 비로그인 상태일 때 보여주는 화면
    void notLoginView()
    {
        binding.mypageMyId.setVisibility(View.GONE);                // 닉네임 보여주는 곳 가림
        binding.nicknameEditImage.setVisibility(View.GONE);         // 닉네임 수정하는 펜 이미지 가림
        binding.threeMenuLayout.setVisibility(View.GONE);           // 북마크 혜택, 작성한 리뷰 버튼 있는 레이아웃 가림
        binding.mypageDividerView.setVisibility(View.GONE);         // 위 레이아웃 밑의 회색 구분선 가림
        binding.editInterestTextview.setVisibility(View.GONE);      // 관심사 선택 가림
        binding.mypageLogoutTextview.setVisibility(View.GONE);      // 로그아웃 가림
        binding.mypageWithdrawTextview.setVisibility(View.GONE);    // 탈퇴하기 가림
        binding.mypageLoginLayout.setVisibility(View.VISIBLE);      // 로그인하기 버튼과 그 위의 텍스트뷰 있는 레이아웃 보임

        // 관심사 수정, 푸시 알림 설정 등이 있는 레이아웃의 top 체인을 곡선 있는 흰색 레이아웃의 bottom에 연결한다
        ConstraintLayout constraintLayout = binding.mypageBottomLayout;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.mypage_bottom_layout,    // startId : 어떤 뷰의 체인을 바꿀 것인가?
                ConstraintSet.TOP,                          // startSide : 그 뷰의 어디를 연결할 것인가?
                R.id.mypage_white_layout,                   // endId : 어디에 체인을 걸 것인가?
                ConstraintSet.BOTTOM,                       // endSide : 그 뷰의 어디에 1번 인자로 받은 뷰를 연결할 것인가?
                0);                                  // margin : 제한할 여백(양수여야 함)
        constraintSet.applyTo(constraintLayout);
        // verticalBias를 0으로 만들어서 맨 위로 올라가도록 한다
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.mypageBottomLayout.getLayoutParams();
        params.verticalBias = 0f;
        binding.mypageBottomLayout.setLayoutParams(params);
    }

    // 로그인 상태일 때 보여주는 화면
    void loginView()
    {
        // 로그인 상태
        binding.mypageMyId.setVisibility(View.VISIBLE);
        binding.nicknameEditImage.setVisibility(View.VISIBLE);
        binding.threeMenuContainer.setVisibility(View.VISIBLE);
        binding.mypageDividerView.setVisibility(View.VISIBLE);
        binding.mypageLoginLayout.setVisibility(View.GONE);
        // 밑에 있는 관심사 수정, 푸시 알림 설정이 있는 레이아웃의 top 체인을 곡선 있는 흰색 레이아웃의 bottom에 연결한다
        ConstraintLayout constraintLayout = binding.mypageBottomLayout;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(R.id.mypage_bottom_layout,    // startId : 어떤 뷰의 체인을 바꿀 것인가?
                ConstraintSet.TOP,                          // startSide : 그 뷰의 어디를 연결할 것인가?
                R.id.three_menu_container,                  // endId : 어디에 체인을 걸 것인가?
                ConstraintSet.TOP,                       // endSide : 그 뷰의 어디에 1번 인자로 받은 뷰를 연결할 것인가?
                0);                                  // margin : 두 레이아웃의 위아래 간격 마진(양수여야 함)
        constraintSet.applyTo(constraintLayout);
    }

    // 내 닉네임 가져와서 보여주는 메서드
    void checkMyNickname(String type)
    {
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.editNickname(sqlite_token, "", type);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    parseResult(result);
                }
                else
                {
                    Log.e(TAG, "서버에서 닉네임 가져오기 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "서버에서 닉네임 가져오기 에러 : " + t.getMessage());
            }
        });
    }

    // 닉네임 변경 메서드
    void changeNickname(@Nullable String nickname, String type)
    {
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.editNickname(sqlite_token, nickname, type);
//        Log.e(TAG, "token : " + sqlite_token + ", 변경할 닉네임 : " + nickname);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "닉네임 변경 결과 : " + result);
                    parseResult(result);
                }
                else
                {
                    Log.e(TAG, "닉네임 변경 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "닉네임 변경 에러 : " + t.getMessage());
            }
        });
    }

    // 가져온 내 닉네임을 텍스트뷰에 set하는 메서드
    @SuppressLint("CheckResult")
    private void parseResult(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

//        Log.e(TAG, "닉네임 변경 요청 후 서버에서 받은 message : " + message);
        if (message.equals("계정 정보가 존재하지 않습니다.") || message.equals("data is empty"))
        {
            //
        }
        else
        {
            Observable.just(message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data ->
                    {
                        if (message.equals(getString(R.string.change_complete)))
                        {
                            binding.mypageMyId.setText(receivedNickname);
                        }
                        else
                        {
                            binding.mypageMyId.setText(message);
                        }
                    });
        }
    }

    // 사용자 로그
    void userLog(String user_action)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
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
                    //
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

    // 서버로 한글 로그 보내면 깨지기 때문에 인코딩하는 메서드
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

    // 기기의 푸시 알림 설정값을 가져오는 메서드
    public boolean areNotificationsEnabled()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (!manager.areNotificationsEnabled())
            {
                return false;
            }
            List<NotificationChannel> channels = manager.getNotificationChannels();
            for (NotificationChannel channel : channels)
            {
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE)
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return NotificationManagerCompat.from(getActivity()).areNotificationsEnabled();
        }
    }

    // 푸시 알림 받을지를 설정하는 스위치의 값 변경 메서드
    void putPushSetting(boolean isPushed)
    {
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        String is_push = String.valueOf(isPushed);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String action = userAction("푸시 설정값 수정");
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.putPushSetting(session, action, sqlite_token, "push", is_push);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e("putPushSetting()", "에러 = " + t.getMessage());
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        boolean isAllowed = areNotificationsEnabled();
        if (isAllowed)
        {
            binding.mypageNotiSwitch.setChecked(true);
            putPushSetting(true);
        }
        else
        {
            binding.mypageNotiSwitch.setChecked(false);
            putPushSetting(false);
        }
    }
}