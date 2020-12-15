package com.psj.welfare.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.activity.MyInfoUpdateActivity;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomPushDenyDialog;
import com.psj.welfare.custom.CustomPushPermitDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 마이페이지 프래그먼트 */
public class MyPageFragment extends Fragment
{
    private final String TAG = "MyPageFragment";

    ImageView kakao_profile_image, move_update_personal_imageview;
    TextView kakao_name, account_platform_text;
    Switch push_noti_switch;
    Button account_btn, benefit_type_btn, terms_location_based_btn, privacy_policy_btn;
    Toolbar mypage_toolbar;

    private SharedPreferences sharedPreferences;
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

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        server_token = sharedPreferences.getString("token", "");

        mypage_toolbar.setTitle("마이페이지");
        // 프래그먼트에서 툴바를 사용하기 위한 처리
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(mypage_toolbar);
        }

        profile_image = sharedPreferences.getString(getString(R.string.get_kakao_image), "");
        if (sharedPreferences.getString(getString(R.string.get_kakao_name), "") != null)
        {
            kakao_nick = sharedPreferences.getString(getString(R.string.get_kakao_name), "");
            kakao_name.setText(kakao_nick);
            account_platform_text.setText(getString(R.string.set_kakao_account));
        }
        if (profile_image != null)
        {
            Glide.with(this)
                    .load(profile_image)
                    .into(kakao_profile_image);
        }

        getUserInfo();

        // 계정 설정
        account_btn.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

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
                CustomPushPermitDialog dialog = new CustomPushPermitDialog(getActivity());
                dialog.showPushDialog();
            }
            else
            {
                putPushSetting(false);
                fcm_canceled = false;
//                sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fcm_canceled", fcm_canceled);
                editor.apply();
                CustomPushDenyDialog denyDialog = new CustomPushDenyDialog(getActivity());
                denyDialog.showDenyDialog();
            }
        });

        // 혜택 유형
        benefit_type_btn.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

        // 위치기반 서비스 이용약관
        terms_location_based_btn.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

        // 개인정보처리방침
        privacy_policy_btn.setOnClickListener(v -> Toast.makeText(getActivity(), getString(R.string.not_yet), Toast.LENGTH_SHORT).show());

        // 이름 우측의 >를 누르면 개인정보 수정 액티비티로 이동한다
        move_update_personal_imageview.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyInfoUpdateActivity.class);
            startActivity(intent);
        });

        /* 앱 설정 화면에서 볼 수 있는 알림 설정값을 가져오는 메서드. 값 확인 위해 사용 */
        boolean aaa = NotificationManagerCompat.from(getActivity()).areNotificationsEnabled();
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
        Call<String> call = apiInterface.putPushSetting(server_token, is_push);
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
                    //
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e("putPushSetting()", "에러 = " + t.getMessage());
            }
        });
    }

    /* 내 정보를 클릭했을 때 서버에서 사용자 정보를 조회해서 가져오는 메서드 */
    void getUserInfo()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getUserInfo(server_token);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    jsonParsing(response.body());
                }
                else
                {
//                    Logger.e("onResponse() 실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
//                Log.e("getUserInfo()", "에러 = " + t.getMessage());
            }
        });
    }

    private void jsonParsing(String response)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            checked = jsonObject.getString(getString(R.string.push_value));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (checked.equals(getString(R.string.is_true)))
        {
            push_noti_switch.setChecked(true);
        }
        else
        {
            push_noti_switch.setChecked(false);
        }
    }

    private void init(View view)
    {
        mypage_toolbar = view.findViewById(R.id.mypage_toolbar);
        kakao_profile_image = view.findViewById(R.id.kakao_profile_image);
        account_btn = view.findViewById(R.id.account_btn);
        push_noti_switch = view.findViewById(R.id.push_noti_switch);
        benefit_type_btn = view.findViewById(R.id.benefit_type_btn);
        terms_location_based_btn = view.findViewById(R.id.terms_location_based_btn);
        privacy_policy_btn = view.findViewById(R.id.privacy_policy_btn);
        kakao_name = view.findViewById(R.id.kakao_name);
        account_platform_text = view.findViewById(R.id.account_platform_text);
        move_update_personal_imageview = view.findViewById(R.id.move_update_personal_imageview);
    }

}