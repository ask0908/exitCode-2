package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomWithdrawDialog;
import com.psj.welfare.custom.MyWithdrawListener;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 회원탈퇴 화면
 * 카카오 회원탈퇴 메서드도 같이 구현한다 */
public class WithdrawActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ImageView withdraw_back_image;
    TextView withdraw_top_textview, withdraw_question_textview, withdraw_alert_textview, select_reason_textview, withdraw_textview;
    EditText reason_to_leave_edittext;
    Button withdraw_button;

    DBOpenHelper helper;

    public String sqlite_token, reason;
    String status, message;
    SharedPreferences sharedPreferences;
    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    private int input;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));

        analytics = FirebaseAnalytics.getInstance(this);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        init();

        // Room DB에서 유저 토큰 획득
        getUserToken();

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        withdraw_question_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);    // 계정을 삭제하시나요?
        withdraw_alert_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);       // 계정을 삭제하면 관심사 선택~삭제됩니다
        select_reason_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);        // 계정을 삭제하는 이유를 말해주세요
        withdraw_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 19);             // "선택해 주세요" 텍스트뷰
        reason_to_leave_edittext.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 19);      // 탈퇴 사유 적는 editText
        withdraw_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 16);               // 탈퇴 버튼

        /* 회원탈퇴 버튼의 width, height 수정 */
        ViewGroup.LayoutParams button_params = withdraw_button.getLayoutParams();
        button_params.width = (size.x / 7) * 5;
        button_params.height = size.y / 15;
        withdraw_button.setLayoutParams(button_params);

        // 탈퇴 사유를 아직 선택하지 않은 경우
        if (withdraw_textview.getText().toString().equals(getString(R.string.withdraw_textview_default)))
        {
            reason_to_leave_edittext.setVisibility(View.GONE);
        }
        else
        {
            reason_to_leave_edittext.setVisibility(View.VISIBLE);
        }

        // 탈퇴 사유 선택하는 텍스트뷰
        withdraw_textview.setOnClickListener(v ->
        {
            CustomWithdrawDialog dialog = new CustomWithdrawDialog(this);
            // 선택해 주세요 텍스트뷰 클릭 시 커스텀 다이얼로그 출력
            dialog.showDialog();
            // 커스텀 다이얼로그에서 어떤 라디오 버튼을 눌렀냐에 따라 각각 다른 값을 변수에 담아 회원탈퇴 api 매개변수로 넘김
            dialog.setOnWithdrawListener(new MyWithdrawListener()
            {
                @Override
                public void sendFirstValue(String value)
                {
                    /* NPE 방지를 위해 매개변수에 null 체크(1~4번 모두 동일) */
                    if (value != null)
                    {
                        if (!value.equals(""))
                        {
                            // 매개변수로 받은 문자열을 액티비티의 멤버 변수에 대입
                            reason = value;
                            Log.e(TAG, "1번 클릭 후 다이얼로그 -> 액티비티로 값 전달 확인 : " + reason);
                            // 4번을 선택한 경우에만 editText가 보여야 하기 때문에 아직 GONE으로 visibility 설정
                            // 그게 아니라도 각 버튼마다 아래 코드를 써줘야 기타 -> 1~3번 클릭 시 editText가 안 보이게 된다
                            reason_to_leave_edittext.setVisibility(View.GONE);
                            withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                            withdraw_button.setEnabled(true);
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업을 끝내면 Rxjava로 텍스트뷰에 값 set
                            Observable.just(reason)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(data -> withdraw_textview.setText(reason));
                        }
                    }
                }

                @Override
                public void sendSecondValue(String second_value)
                {
                    if (second_value != null)
                    {
                        if (!second_value.equals(""))
                        {
                            reason = second_value;
                            Log.e(TAG, "2번 클릭 후 다이얼로그 -> 액티비티로 값 전달 확인 : " + reason);
                            reason_to_leave_edittext.setVisibility(View.GONE);
                            withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                            withdraw_button.setEnabled(true);
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업을 끝내면 Rxjava로 텍스트뷰에 값 set
                            Observable.just(reason)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(data -> withdraw_textview.setText(reason));
                        }
                    }
                }

                @Override
                public void sendThirdValue(String third_value)
                {
                    if (third_value != null)
                    {
                        if (!third_value.equals(""))
                        {
                            reason = third_value;
                            Log.e(TAG, "3번 클릭 후 다이얼로그 -> 액티비티로 값 전달 확인 : " + reason);
                            reason_to_leave_edittext.setVisibility(View.GONE);
                            withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                            withdraw_button.setEnabled(true);
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업을 끝내면 Rxjava로 텍스트뷰에 값 set
                            Observable.just(reason)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(data -> withdraw_textview.setText(reason));
                        }
                    }
                }

                @Override
                public void sendFourthValue(String fourth_value)
                {
                    if (fourth_value != null)
                    {
                        if (!fourth_value.equals(""))
                        {
                            reason = fourth_value;
                            Log.e(TAG, "4번 클릭 후 다이얼로그 -> 액티비티로 값 전달 확인 : " + reason);
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업을 끝내면 Rxjava로 텍스트뷰에 값 set
                            Observable.just(reason)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(data -> withdraw_textview.setText(reason));

                            // 기타를 눌렀을 경우 탈퇴 사유를 적을 수 있는 란을 visible로 바꾼다
                            if (reason.equals("기타"))
                            {
                                reason_to_leave_edittext.setVisibility(View.VISIBLE);
                                withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.withdraw_non_activated));
                                withdraw_button.setEnabled(false);
                            }
                            else
                            {
                                reason_to_leave_edittext.setVisibility(View.GONE);
                                withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                                withdraw_button.setEnabled(true);
                            }
                        }
                    }
                }
            });

        });

        // 탈퇴 사유 적는 editText
        reason_to_leave_edittext.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                //
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                input = reason_to_leave_edittext.getText().toString().length();
                Log.e(TAG, "input : " + input);
                if (input > 0)
                {
                    withdraw_button.setEnabled(true);
                    withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                }
                else
                {
                    withdraw_button.setEnabled(false);
                    withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.withdraw_non_activated));
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                //
            }
        });

        // 회원탈퇴 버튼
        withdraw_button.setOnClickListener(v ->
        {
            // 기타를 선택한 게 아니라면 라디오 버튼의 문자열들을 가져와서 변수에 대입
            Log.e(TAG, "회원탈퇴 api로 보낼 탈퇴 이유 : " + reason);

            // 라디오 버튼의 문자열(탈퇴 사유)을 api 인자로 넘겨 서버 통신
            leaveFromApp(reason);

            // editText가 보일 경우(=기타를 선택한 경우)
            // editText 안에 아무것도 쓰지 않았으면 토스트로 적으라고 안내
            if (reason_to_leave_edittext.getVisibility() == View.VISIBLE)
            {
                if (reason_to_leave_edittext.getText().toString().length() == 0)
                {
                    withdraw_button.setEnabled(false);
                    withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.withdraw_non_activated));
                    Toast.makeText(this, "탈퇴 사유를 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    withdraw_button.setEnabled(true);
                    withdraw_button.setBackground(ContextCompat.getDrawable(WithdrawActivity.this, R.drawable.radius_smallround_pink_button));
                    // 뭔가 입력했다면 회원탈퇴 api 호출
                    leaveFromApp(reason_to_leave_edittext.getText().toString());
                }
            }
        });

        withdraw_back_image.setOnClickListener(v -> finish());

    }

    // 회원탈퇴 메서드
    private void leaveFromApp(String reason)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.leaveFromApp(sqlite_token, "leave", reason);
        Log.e("회원탈퇴 메서드 안", "token : " + sqlite_token + ", 탈퇴 사유 : " + reason);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    parseResult(result);
                    Log.e(TAG, "회원탈퇴 성공 : " + result);
                }
                else
                {
                    Log.e(TAG, "회원탈퇴 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "회원탈퇴 에러 : " + t.getMessage());
            }
        });
    }

    // 회원탈퇴 api 결과값 파싱 메서드
    private void parseResult(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            status = jsonObject.getString("statusCode");
            message = jsonObject.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status.equals("200"))
        {
            // 회원탈퇴 성공한 경우 스플래시 화면부터 다시 앱을 시작하도록 한다
            kakaoUnlink();
        }
        else if (status.equals("400") || status.equals("404") || status.equals("500"))
        {
            Toast.makeText(this, "일시적인 오류가 발생했어요. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
        }
    }

    // 회원탈퇴 메서드
    private void kakaoUnlink()
    {
        new AlertDialog.Builder(WithdrawActivity.this)
                .setMessage(getString(R.string.kakao_unlink_message))
                .setPositiveButton("네", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback()
                        {
                            @Override
                            public void onFailure(ErrorResult errorResult)
                            {
                                int result = errorResult.getErrorCode();

                                if (result == ApiErrorCode.CLIENT_ERROR_CODE)
                                {
                                    Log.d(TAG, "카톡 로그아웃 - 네트워크 불안정, 에러 코드 : " + errorResult.getErrorCode() +
                                            ", 에러 메시지 : " + errorResult.getErrorMessage());
                                }
                                else
                                {
                                    Log.d(TAG, "카톡 로그아웃 - 실패");
                                }
                            }

                            @Override
                            public void onSessionClosed(ErrorResult errorResult)
                            {
                                Log.d(TAG, "세션 닫힘, 에러 코드 : " + errorResult.getErrorCode() + ", 에러 메시지 : " + errorResult.getErrorMessage());
                                Intent intent = new Intent(WithdrawActivity.this, SplashActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onNotSignedUp()
                            {
                                Intent intent = new Intent(WithdrawActivity.this, SplashActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onSuccess(Long result)
                            {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("logout", true);
                                editor.putBoolean("is_leaved", true);
                                editor.remove("user_nickname");
                                editor.apply();
                                /* 카카오 회원탈퇴가 완료되면 액티비티 스택을 비우고 스플래시 화면부터 시작한다 */
                                Intent intent = new Intent(WithdrawActivity.this, SplashActivity.class);
                                // 액티비티 스택 비우기
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                // 구글 애널리틱스로 사용자 로그 전송
                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "회원탈퇴 완료. 탈퇴 사유 : " + reason);
                                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                                Toast.makeText(WithdrawActivity.this, "회원탈퇴가 정상적으로 완료됐어요", Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                        });

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        Toast.makeText(WithdrawActivity.this, "회원탈퇴가 취소되었어요", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    // Room DB에서 유저 토큰 획득
    private void getUserToken()
    {
        helper = new DBOpenHelper(this);
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
    }

    // findViewById() 모아놓은 메서드
    private void init()
    {
        withdraw_back_image = findViewById(R.id.withdraw_back_image);
        withdraw_top_textview = findViewById(R.id.withdraw_top_textview);
        withdraw_question_textview = findViewById(R.id.withdraw_question_textview);
        withdraw_alert_textview = findViewById(R.id.withdraw_alert_textview);
        select_reason_textview = findViewById(R.id.select_reason_textview);
        withdraw_textview = findViewById(R.id.withdraw_textview);
        reason_to_leave_edittext = findViewById(R.id.reason_to_leave_edittext);
        withdraw_button = findViewById(R.id.withdraw_button);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("reason");
        editor.apply();
    }
}