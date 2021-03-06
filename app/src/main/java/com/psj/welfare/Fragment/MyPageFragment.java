package com.psj.welfare.fragment;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.psj.welfare.R;
import com.psj.welfare.activity.GetUserInformationActivity;
import com.psj.welfare.activity.LoginActivity;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageFragment extends Fragment
{
    private final String TAG = "MyPageFragment";

    LinearLayout account_layout, benefit_type_layout, terms_location_layout, push_noti_layout, privacy_policy_layout, user_layout, keyword_layout;

    ImageView kakao_profile_image, move_update_personal_imageview, privacy_policy_imageview, account_imageview, keyword_imageview;
    TextView kakao_name, account_platform_text, push_setting_text;
    View mypage_divider;
    Switch push_noti_switch;
    Button account_btn, benefit_type_btn, terms_location_based_btn, privacy_policy_btn, mypage_login_btn, keyword_btn;
    Toolbar mypage_toolbar;

    SharedPreferences sharedPreferences;
    String profile_image, kakao_nick, server_token;
    String checked;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    public MyPageFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
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

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        init(view);

        push_noti_layout.setOnClickListener(v ->
        {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, "ch_push");
            startActivity(intent);
        });

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String written_nickname = sharedPreferences.getString("user_nickname", "");
        if (written_nickname != null)
        {
            if (!written_nickname.equals(""))
            {
                kakao_name.setText(sharedPreferences.getString("user_nickname", ""));
            }
        }
        server_token = sharedPreferences.getString("token", "");
        if (!server_token.equals(""))
        {
            mypage_login_btn.setText("로그아웃");
        }

        String user_keyword = sharedPreferences.getString("user_category", "");
        if (user_keyword != null)
        {
            // 키워드 정보가 없을 경우
            if (user_keyword.equals(""))
            {
                if (!sharedPreferences.getString("nickname", "").equals("") ||
                        !sharedPreferences.getString("user_area", "").equals("") ||
                        !sharedPreferences.getString("user_age", "").equals("") ||
                        !sharedPreferences.getString("user_gender", "").equals(""))
                {
                }
                keyword_layout.setVisibility(View.GONE);
            }
            else
            {
            }
        }

        kakao_profile_image.setImageResource(R.drawable.base_img);

        if (!sharedPreferences.getString("nickname", "").equals("") ||
                !sharedPreferences.getString("user_area", "").equals("") ||
                !sharedPreferences.getString("user_age", "").equals("") ||
                !sharedPreferences.getString("user_gender", "").equals(""))
        {
            kakao_nick = sharedPreferences.getString(getString(R.string.get_kakao_name), "");
            account_platform_text.setText(getString(R.string.set_kakao_account));
        }
        else
        {
            account_platform_text.setText("");
        }

        getUserInfo();

        account_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "개인정보 수정 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
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
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "개인정보 수정 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        keyword_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 정보 수정 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                Intent intent = new Intent(getActivity(), ChoiceKeywordActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            }
        });
        keyword_imageview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "키워드 정보 수정 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                Intent intent = new Intent(getActivity(), ChoiceKeywordActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            }
        });

        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 안드로이드 os 자체의 설정 화면에서 알림 설정값을 가져와 그 값대로 스위치를 바꾼다
        // 여기 뿐 아니라 onResume()에도 같은 처리를 해야 한다. 왜냐면 화면을 이동해서 설정값을 바꾸고 백버튼을 누르면 마이페이지로 돌아오는데, 이 때 호출되는 onResume()에서
        // 알림 설정값을 체크해야 스위치의 on/off가 바뀌고 유저는 실시간으로 설정값이 반영되는 걸 확인할 수 있다
        boolean isAllowed = areNotificationsEnabled();
        if (isAllowed == true)
        {
            push_noti_switch.setChecked(true);
            putPushSetting(true);
            editor.putBoolean("fcm_canceled", true);
            editor.apply();
        }
        else
        {
            push_noti_switch.setChecked(false);
            putPushSetting(false);
            editor.putBoolean("fcm_canceled", false);
            editor.apply();
        }

        benefit_type_layout.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

        terms_location_layout.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "이용약관 클릭");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        terms_location_based_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "이용약관 클릭");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                Intent intent = new Intent(getActivity(), TermsAndConditionsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        privacy_policy_btn.setOnClickListener(v ->
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "개인정보처리방침 클릭");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//            webview_layout.setVisibility(View.VISIBLE);
//            mypage_webview.loadUrl("https://www.urbene-fit.com/privacyPolicy.html");
            Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        });

        privacy_policy_layout.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "개인정보처리방침 클릭");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                webview_layout.setVisibility(View.VISIBLE);
//                mypage_webview.loadUrl("https://www.urbene-fit.com/privacyPolicy.html");
                Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });

        mypage_login_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                if (mypage_login_btn.getText().toString().equals("로그아웃"))
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
                }
                else if (mypage_login_btn.getText().toString().equals("로그인하러 가기"))
                {
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

        move_update_personal_imageview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
//                Intent intent = new Intent(getActivity(), MyInfoUpdateActivity.class);
//                startActivity(intent);
            }
        });

        if (sharedPreferences.getBoolean("logout", true))
        {
            kakao_name.setVisibility(View.GONE);
            account_platform_text.setVisibility(View.GONE);
            kakao_profile_image.setVisibility(View.GONE);
            push_noti_switch.setChecked(false);
            putPushSetting(false);
            mypage_login_btn.setText("로그인하러 가기");
            user_layout.setVisibility(View.GONE);
            account_layout.setVisibility(View.GONE);
            terms_location_layout.setVisibility(View.GONE);
            privacy_policy_layout.setVisibility(View.GONE);
            keyword_layout.setVisibility(View.GONE);
            mypage_divider.setVisibility(View.GONE);
        }
        else
        {
            kakao_name.setVisibility(View.VISIBLE);
            kakao_name.setText("nickname908");
            kakao_profile_image.setVisibility(View.VISIBLE);
            mypage_login_btn.setText("로그아웃");
            user_layout.setVisibility(View.VISIBLE);
            terms_location_layout.setVisibility(View.VISIBLE);
            privacy_policy_layout.setVisibility(View.VISIBLE);
            mypage_divider.setVisibility(View.VISIBLE);
        }

    }

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

    @Override
    public void onResume()
    {
        super.onResume();
        boolean isAllowed = areNotificationsEnabled();
        if (sharedPreferences.getBoolean("logout", false))
        {
            push_noti_switch.setChecked(false);
        }
        else
        {
            if (isAllowed)
            {
                push_noti_switch.setChecked(true);
                putPushSetting(true);
            }
            else
            {
                push_noti_switch.setChecked(false);
                putPushSetting(false);
            }
        }
    }

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
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");

        if (!token.equals("") && !sharedPreferences.getString("user_category", "").equals("") &&
                !sharedPreferences.getString("user_area", "").equals("") &&
                !sharedPreferences.getString("user_category", "").equals("") &&
                !sharedPreferences.getString("user_age", "").equals("") &&
                !sharedPreferences.getString("user_gender", "").equals(""))
        {
            if (checked != null)
            {
            }
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
        keyword_layout = view.findViewById(R.id.keyword_layout);

        kakao_profile_image = view.findViewById(R.id.kakao_profile_image);
        account_btn = view.findViewById(R.id.account_btn);
        push_noti_switch = view.findViewById(R.id.push_noti_switch);
        benefit_type_btn = view.findViewById(R.id.benefit_type_btn);
        terms_location_based_btn = view.findViewById(R.id.terms_location_based_btn);
        privacy_policy_btn = view.findViewById(R.id.privacy_policy_btn);
        mypage_login_btn = view.findViewById(R.id.mypage_login_btn);
        keyword_btn = view.findViewById(R.id.keyword_btn);

        kakao_name = view.findViewById(R.id.kakao_name);
        account_platform_text = view.findViewById(R.id.account_platform_text);
        move_update_personal_imageview = view.findViewById(R.id.move_update_personal_imageview);
        privacy_policy_imageview = view.findViewById(R.id.privacy_policy_imageview);
        account_imageview = view.findViewById(R.id.account_imageview);
        keyword_imageview = view.findViewById(R.id.keyword_imageview);

        push_setting_text = view.findViewById(R.id.push_setting_text);
        mypage_divider = view.findViewById(R.id.mypage_divider);
    }

}