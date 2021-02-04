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
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
    TextView kakao_name, account_platform_text;
    Switch push_noti_switch;
    Button account_btn, benefit_type_btn, terms_location_based_btn, privacy_policy_btn, mypage_login_btn;
    Toolbar mypage_toolbar;

    SharedPreferences sharedPreferences;
    String profile_image, kakao_nick, server_token;
    String checked, encode_str;
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

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
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

        profile_image = sharedPreferences.getString(getString(R.string.get_kakao_image), "");
        if (!sharedPreferences.getString("nickname", "").equals("") || !sharedPreferences.getString("user_area", "").equals("") ||
        !sharedPreferences.getString("user_age", "").equals("") || !sharedPreferences.getString("user_gender", "").equals(""))
        {
            kakao_nick = sharedPreferences.getString(getString(R.string.get_kakao_name), "");
            kakao_name.setText(kakao_nick);
            account_platform_text.setText(getString(R.string.set_kakao_account));
        }
        else
        {
            account_platform_text.setText("");
        }
        if (profile_image != null)
        {
            Glide.with(this)
                    .load(profile_image)
                    .into(kakao_profile_image);
        }

        getUserInfo();

        // 개인정보 수정
        account_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
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

//        account_layout.setOnClickListener(new OnSingleClickListener()
//        {
//            @Override
//            public void onSingleClick(View v)
//            {
//                Toast.makeText(getActivity(), "클릭", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
////                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
////                sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
////                if (!sharedPreferences.getString("user_category", "").equals("") && !sharedPreferences.getBoolean("logout", false))
////                {
////                    // value 값이 있고 logout이 false면 로그인을 했다는 거니까 이 때만 개인정보 수정 화면으로 이동시킨다
////                    Intent intent = new Intent(getActivity(), GetUserInformationActivity.class);
////                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                    startActivity(intent);
////                }
//            }
//        });

        // 푸시 알림 설정 스위치
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
//                sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
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
                Intent intent = new Intent(getActivity(), PersonalInformationActivity.class);
                startActivity(intent);
            }
        });

        // 개인정보처리방침
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

    public boolean areNotiEnabled()
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
                    Log.e(TAG, "channel = " + channel);
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

    /* 유저가 푸시알림설정 스위치를 on으로 두면 true, off로 두면 false를 서버로 보내 기존 값을 수정해 저장하는 메서드 */
    void putPushSetting(boolean isPushed)
    {
        String is_push = String.valueOf(isPushed);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        encode("푸시 설정값 수정");
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.putPushSetting(session, encode_str, server_token, "push", is_push);
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

    /* 내 정보를 클릭했을 때 서버에서 사용자 정보를 조회해서 가져오는 메서드 */
    void getUserInfo()
    {
        encode("서버의 사용자 정보 가져오기");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getUserInfo(session, encode_str, server_token);
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
    }

}