package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

/* 회원탈퇴 화면 */
public class WithdrawActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ImageView withdraw_back_image;
    TextView withdraw_top_textview, withdraw_question_textview, withdraw_alert_textview, select_reason_textview;
    TextView withdraw_textview;
    EditText reason_to_leave_edittext;
    Button withdraw_button;

    DBOpenHelper helper;

    String sqlite_token, reason;
    String status, message;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        init();

        // 토큰 꺼내기 위한 Room DB 초기화 및 생성
        helper = new DBOpenHelper(this);
        helper.openDatabase();
        helper.create();

        // Cursor로 통해 DB에서 토큰을 가져온다
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        withdraw_question_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        withdraw_alert_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        select_reason_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);

        withdraw_textview.setOnClickListener(v ->
        {
            CustomWithdrawDialog dialog = new CustomWithdrawDialog(this);
            // 선택해 주세요 텍스트뷰 클릭 시 커스텀 다이얼로그 출력
            dialog.showDialog();
            // 커스텀 다이얼로그에서 어떤 라디오 버튼을 눌렀냐에 따라 각각 다른 값을 변수에 담아 회원탈퇴 api 호출 준비
            dialog.setOnWithdrawListener(new MyWithdrawListener()
            {
                @Override
                public void sendFirstValue(String value)
                {
                    if (value != null)
                    {
                        if (!value.equals(""))
                        {
                            reason = value;
                            Log.e(TAG, "1번 클릭 후 다이얼로그 -> 액티비티로 값 전달 확인 : " + reason);
                            reason_to_leave_edittext.setVisibility(View.GONE);
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업 끝내면 Rxjava로 텍스트뷰에 값 set
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
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업 끝내면 Rxjava로 텍스트뷰에 값 set
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
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업 끝내면 Rxjava로 텍스트뷰에 값 set
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
                            // 콜백 메서드가 다이얼로그 -> 액티비티로 값 가져오는 작업 끝내면 Rxjava로 텍스트뷰에 값 set
                            Observable.just(reason)
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(data -> withdraw_textview.setText(reason));

                            // 기타를 눌렀을 경우 탈퇴 사유를 적을 수 있는 란을 visible로 바꾼다
                            if (reason.equals("기타"))
                            {
                                reason_to_leave_edittext.setVisibility(View.VISIBLE);
                                withdraw_button.setEnabled(false);
                            }
                            else
                            {
                                reason_to_leave_edittext.setVisibility(View.GONE);
                                withdraw_button.setEnabled(true);
                            }
                        }
                    }
                }
            });

        });

        // 버튼을 클릭했을 때 사유 적는 란이 보이는 상태면 / 아무것도 안 적혀 있다면 버튼을 활성화하지 않는다
        if (reason_to_leave_edittext.getVisibility() == View.VISIBLE || reason_to_leave_edittext.getText().toString().length() == 0)
        {
            withdraw_button.setEnabled(false);
        }
        else
        {
            withdraw_button.setEnabled(true);
        }

        withdraw_button.setOnClickListener(v ->
        {
            if (reason_to_leave_edittext.getVisibility() == View.VISIBLE)
            {
                if (reason_to_leave_edittext.getText().toString().length() == 0)
                {
                    Toast.makeText(this, "탈퇴 사유를 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    leaveFromApp(reason_to_leave_edittext.getText().toString());
                }
            }
            else
            {
                leaveFromApp(reason);
            }
        });

    }

    private void leaveFromApp(String reason)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.leaveFromApp(sqlite_token, "leave", reason);
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
    }

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
}