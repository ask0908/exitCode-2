package com.psj.welfare.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.activity.GetUserInformationActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.activity.MyInfoUpdateActivity;
import com.psj.welfare.activity.PersonalInformationActivity;
import com.psj.welfare.activity.SplashActivity;
import com.psj.welfare.activity.TermsAndConditionsActivity;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 마이페이지 프래그먼트
* 마이페이지에 카카오 계정으로 아직 로그인하지 않았는데도 카카오 계정으로 나오는 현상 있음 */
public class MyPageFragment extends Fragment
{
    private final String TAG = "MyPageFragment";

    LinearLayout account_layout, benefit_type_layout, terms_location_layout, push_noti_layout, privacy_policy_layout, user_layout;

    ImageView kakao_profile_image, move_update_personal_imageview, privacy_policy_imageview, account_imageview;
    TextView kakao_name, account_platform_text, push_setting_text;
    Switch push_noti_switch;
    Button account_btn, benefit_type_btn, terms_location_based_btn, privacy_policy_btn, mypage_login_btn;
    Toolbar mypage_toolbar;

    SharedPreferences sharedPreferences;
    String profile_image, kakao_nick, server_token;
    String checked;
    boolean fcm_canceled;

    public MyPageFragment()
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
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        Logger.addLogAdapter(new AndroidLogAdapter());

        push_setting_text.setOnClickListener(v -> {
            /* 아래 인자로 인텐트를 이동시키면 핸드폰에 설치된 앱의 알림 설정 화면으로 이동된다
            * 여기서 값이 T/F로 바뀌면 이 값을 받아와서 서버로 보내 알림 설정값을 바꾸는 흐름으로 스위치 로직을 바꾸면 원하는 기능이 만들어질 듯 */
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "ch_push");
            startActivity(intent);
        });

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);

        if (!sharedPreferences.getString("user_nickname", "").equals(""))
        {
            kakao_name.setText(sharedPreferences.getString("user_nickname", ""));
        }
        server_token = sharedPreferences.getString("token", "");
        // 서버에서 전송받은 토큰이 있다면 로그인한 것이므로 "로그인하러 가기" 버튼을 안 보이게 처리한다
        // 서버에서 전송받은 토큰이 있다면 로그인한 것이므로 "로그아웃" 글자로 보이게 한다
        if (!server_token.equals(""))
        {
            mypage_login_btn.setText("로그아웃");
        }

        mypage_toolbar.setTitle("마이페이지");
        // 프래그먼트에서 툴바를 사용하기 위한 처리
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(mypage_toolbar);
        }

        // 프로필 이미지는 카톡 로그인에서 받아온 걸 사용한다
        profile_image = sharedPreferences.getString(getString(R.string.get_kakao_image), "");
        if (profile_image != null)
        {
            Glide.with(this)
                    .load(profile_image)
                    .into(kakao_profile_image);
        }

        // 닉네임을 입력한 경우 마이페이지에서 유저 닉네임을 보여준다
        if (!sharedPreferences.getString("nickname", "").equals("") || !sharedPreferences.getString("user_area", "").equals("") ||
        !sharedPreferences.getString("user_age", "").equals("") || !sharedPreferences.getString("user_gender", "").equals(""))
        {
            kakao_nick = sharedPreferences.getString(getString(R.string.get_kakao_name), "");
            account_platform_text.setText(getString(R.string.set_kakao_account));
        }
        else
        {
            // 닉네임이 없으면 공백으로 둔다
            account_platform_text.setText("");
        }

        // 서버에 저장된 유저 정보를 가져와 뷰에 뿌린다
        getUserInfo();

        // 개인정보 수정
        account_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
                intent.putExtra("edit", 1);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        account_imageview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        boolean isAllowed = NotificationManagerCompat.from(getActivity()).areNotificationsEnabled();
        if (isAllowed)
        {
            push_noti_switch.setChecked(true);
            Toast.makeText(getActivity(), "푸시 알림 설정 수신 완료", Toast.LENGTH_SHORT).show();
        }
        else
        {
            push_noti_switch.setChecked(false);
            Toast.makeText(getActivity(), "푸시 알림 설정 거부 완료", Toast.LENGTH_SHORT).show();
        }

        // 푸시 알림 설정 스위치
        // 스위치에는 클릭 리스너를 달지 않고 왼쪽 텍스트뷰에만 추가해서, 텍스트뷰를 누르면 설정 화면으로 이동해 값을 수정할 수 있게 한다
        push_noti_switch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isChecked)
            {
                putPushSetting(true);
                fcm_canceled = true;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fcm_canceled", fcm_canceled);
                editor.apply();
//                CustomPushPermitDialog dialog = new CustomPushPermitDialog(getActivity());
//                dialog.showPushDialog();
            }
            else
            {
                putPushSetting(false);
                fcm_canceled = false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fcm_canceled", fcm_canceled);
                editor.apply();
//                CustomPushDenyDialog denyDialog = new CustomPushDenyDialog(getActivity());
//                denyDialog.showDenyDialog();
            }
        });

        // 혜택 유형
        benefit_type_layout.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

        // 이용약관
        terms_location_layout.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
                startActivity(intent);
            }
        });
        terms_location_based_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
                startActivity(intent);
            }
        });

        // 개인정보처리방침
        privacy_policy_btn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
            startActivity(intent);
        });
        privacy_policy_layout.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
                startActivity(intent);
            }
        });

        // 로그인하러 가기 or 로그아웃 버튼
        mypage_login_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                if (mypage_login_btn.getText().toString().equals("로그아웃"))
                {
                    // 로그아웃 누를 경우 한번 더 의사확인
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("로그아웃 하시겠어요?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    userLog("로그아웃 클릭");
                                    Log.e(TAG, "로그아웃 클릭");
                                    if (Session.getCurrentSession().getTokenInfo().getAccessToken() != null)
                                    {
                                        String aaa = Session.getCurrentSession().getTokenInfo().getAccessToken();
                                        Log.e("로그아웃 이후 카카오 토큰 상태", aaa);
                                        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        UserManagement.getInstance().requestLogout(new LogoutResponseCallback()
                                        {
                                            @Override
                                            public void onCompleteLogout()
                                            {
                                                Log.e(TAG, "로그아웃 성공");
                                                editor.putBoolean("logout", true);
                                                editor.remove("user_nickname");
                                                editor.apply();
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
                }
                else if (mypage_login_btn.getText().toString().equals("로그인하러 가기"))
                {
                    // 로그인하러 가기라면 로그인하러 이동한다
                    sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("logout", true);
                    editor.apply();
                    userLog("로그인 화면으로 이동");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });

        // 이름 우측의 >를 누르면 개인정보 수정 액티비티로 이동한다
        move_update_personal_imageview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), MyInfoUpdateActivity.class);
                startActivity(intent);
            }
        });

        // logout 값이 true면 로그아웃한 거니까 로그아웃 상태에 맞게 뷰 상태를 바꾼다
        if (sharedPreferences.getBoolean("logout", false))
        {
            kakao_name.setVisibility(View.GONE);
            account_platform_text.setVisibility(View.GONE);
            kakao_profile_image.setVisibility(View.GONE);
            push_noti_switch.setChecked(false);
            mypage_login_btn.setText("로그인하러 가기");
            user_layout.setVisibility(View.GONE);
            account_layout.setVisibility(View.GONE);
            terms_location_layout.setVisibility(View.GONE);
            privacy_policy_layout.setVisibility(View.GONE);
        }
    }

    /* 유저가 푸시알림설정 스위치를 on으로 두면 true, off로 두면 false를 서버로 보내 기존 값을 수정해 저장하는 메서드 */
    void putPushSetting(boolean isPushed)
    {
        String is_push = String.valueOf(isPushed);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String action = encode("푸시 설정값 수정");
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.putPushSetting(session, action, server_token, "push", is_push);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 : " + result);
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

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private String encode(String str)
    {
        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    /* 내 정보를 클릭했을 때 서버에서 사용자 정보를 조회해서 가져오는 메서드 */
    void getUserInfo()
    {
        String action = encode("서버의 사용자 정보 가져오기");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getUserInfo(session, action, server_token);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
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
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* onResume() : 사용자와 상호작용이 가능한 시점, 이벤트로 프래그먼트가 가려지기 전까지 이 메서드가 유지된다
    * 여기서도 setChecked()를 통해 T/F를 설정해야 설정 화면을 껐을 때 변동된 값이 스위치에 반영된다 */
    @Override
    public void onResume()
    {
        super.onResume();
        boolean isAllowed = NotificationManagerCompat.from(getActivity()).areNotificationsEnabled();
        if (isAllowed)
        {
            push_noti_switch.setChecked(true);
        }
        else
        {
            push_noti_switch.setChecked(false);
        }
    }

    /* 마이페이지에서 사용자들이 일으키는 이벤트 내용을 서버로 전송하는 메서드 */
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
                    String result = response.body();
                    Log.e(TAG, "로그인/로그아웃 로그 전송 결과 : " + result);
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

    /* getUserInfo()로 서버에서 받은 JSON 값을 파싱할 때 사용하는 메서드 */
    private void jsonParsing(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            checked = jsonObject.getString("is_push");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // 비로그인 상태에서 이 화면으로 들어가면 값이 없어서 죽기 때문에 이 예외처리 해야 함
        // 스위치 체크 상태를 바꾸기 전에 기본 정보, 키워드를 입력받았는지 확인하고 모든 정보를 입력받았다면 그 때 스위치 값을 바꾼다
        // 카톡 닉네임도 정보를 다 입력한 상태여야만 보이게 한다
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        if (!token.equals("") && !sharedPreferences.getString("user_category", "").equals("") &&
        !sharedPreferences.getString("user_area", "").equals("") &&
        !sharedPreferences.getString("user_category", "").equals("") &&
        !sharedPreferences.getString("user_age", "").equals("") &&
        !sharedPreferences.getString("user_gender", "").equals(""))
        {
            if (checked.equals(getString(R.string.is_true)))
            {
                push_noti_switch.setChecked(true);
            }
            else
            {
                push_noti_switch.setChecked(false);
            }
        }
        else
        {
            kakao_name.setText("");
        }
    }

    private void init(View view)
    {
        account_layout = view.findViewById(R.id.account_layout);
        benefit_type_layout = view.findViewById(R.id.benefit_type_layout);
        terms_location_layout = view.findViewById(R.id.terms_location_layout);
        push_noti_layout = view.findViewById(R.id.push_noti_layout);
        privacy_policy_layout = view.findViewById(R.id.privacy_policy_layout);
        user_layout = view.findViewById(R.id.user_layout);

        mypage_toolbar = view.findViewById(R.id.mypage_toolbar);
        kakao_profile_image = view.findViewById(R.id.kakao_profile_image);
        account_btn = view.findViewById(R.id.account_btn);
        push_noti_switch = view.findViewById(R.id.push_noti_switch);
        benefit_type_btn = view.findViewById(R.id.benefit_type_btn);
        terms_location_based_btn = view.findViewById(R.id.terms_location_based_btn);
        privacy_policy_btn = view.findViewById(R.id.privacy_policy_btn);
        mypage_login_btn = view.findViewById(R.id.mypage_login_btn);

        kakao_name = view.findViewById(R.id.kakao_name);
        account_platform_text = view.findViewById(R.id.account_platform_text);
        move_update_personal_imageview = view.findViewById(R.id.move_update_personal_imageview);
        privacy_policy_imageview = view.findViewById(R.id.privacy_policy_imageview);
        account_imageview = view.findViewById(R.id.account_imageview);

        push_setting_text = view.findViewById(R.id.push_setting_text);
    }

}