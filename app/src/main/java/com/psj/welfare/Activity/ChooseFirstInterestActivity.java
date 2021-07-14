package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.orhanobut.logger.Logger;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomInterestDialog;
import com.psj.welfare.data.MyInterest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 첫 번째 관심사 선택 화면 */
public class ChooseFirstInterestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ImageView first_interest_back_image;
    TextView first_select_interest_textview, first_interest_top_textview, age_text, area_text;
    // 나이대
    ConstraintLayout age_button_layout, local_button_layout;
    Button under_teenage_button, teenage_button, twenty_button, thirty_button, forty_button, fifty_button, over_sixty_button;

    // 지역
    Button interest_seoul_btn, interest_gangwon_btn, interest_gyonggi_btn, interest_gyongnam_btn, interest_gyongbuk_btn, interest_gwangju_btn,
            interest_daegu_btn, interest_daejeon_btn, interest_busan_btn, interest_sejong_btn, interest_ulsan_btn, interest_incheon_btn,
            interest_jeonnam_btn, interest_jeonbuk_btn, interest_jeju_btn, interest_chungnam_btn, interest_chungbuk_btn;
    // 다음 버튼
    Button go_second_interest_btn;

    // 선택된 나이, 지역들을 담을 리스트
    ArrayList<String> age;
    ArrayList<String> area;

    // 나이대, 지역의 버튼 수만큼 int 배열을 만들어서 클릭 횟수를 저장한다
    // 이 클릭 횟수를 통해 버튼 테두리를 바꾸거나 리스트에 값을 넣고 뺀다
    int[] arr = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // 선택 완료 버튼을 눌렀을 경우 첫 번째 선택 화면을 종료해야 하기 때문에 선언한 변수
    public static Activity activity;

//    DBOpenHelper helper;
    ArrayList<MyInterest> list;

    String user_age, user_local;
    ArrayList<String> age_list, local_list;

    //로그인 토큰 값
    private String token;

    //쉐어드 싱글톤
    private SharedSingleton sharedSingleton;

    // 구글 애널리틱스
    private FirebaseAnalytics analytics;

    //앱 종료 시간 체크
    long backKeyPressedTime = 0;

    // API 호출 후 서버 응답코드
    private int status_code;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_choose_first_interest);

        //쉐어드 싱글톤 사용
        sharedSingleton = SharedSingleton.getInstance(this);

        //구글 analytics 사용 준비
        analytics = FirebaseAnalytics.getInstance(this);

        // findViewById() 모아놓은 메서드
        init();

        buttonsClickListener();

        activity = ChooseFirstInterestActivity.this;

        age = new ArrayList<>();
        area = new ArrayList<>();

        //xml크기를 동적으로 변환
        setsize();

        list = new ArrayList<>();

        token = sharedSingleton.getToken();

        //sqlite 사용 방법 참고용
//        helper = new DBOpenHelper(this);
//        helper.openDatabase();
//        helper.create();
//        Cursor cursor = helper.selectColumns();
//        if (cursor != null)
//        {
//            while (cursor.moveToNext())
//            {
//                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
//            }
//        }

        //관심사 선택을 한적이 있다면
        if(sharedSingleton.getBooleanInterst()){
            // 서버에 저장된 내 관심사 조회
            selectMyInterest();
        } else {
            // 관심사를 처음 선택 한다면 뒤로가기 버튼 안보이도록
            first_interest_back_image.setVisibility(View.GONE);
        }

        /* 버튼 클릭 시 테두리와 글자 색이 바뀌고 해당 값이 변수에 저장돼야 한다
         * 나이대, 지역에서 여러 값을 선택하면 값들 사이에 "-"를 붙인다 -> 다음 버튼 누르면 리스트에 저장되게 하자 */
        // 다음 버튼
        go_second_interest_btn.setOnClickListener(v ->
        {
            if (age.size() == 0 || area.size() == 0)
            {
                // 나이, 지역 중 아무것도 선택하지 않았으면 선택하도록 유도
                Toast.makeText(activity, "나이와 지역 모두 1개라도 선택해 주셔야 해요", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Intent intent = new Intent(this, ChooseSecondInterestActivity.class);
                intent.putStringArrayListExtra("age", age);
                intent.putStringArrayListExtra("area", area);
                Logger.d("나이 : " + age + "\n지역 : " + area);
                startActivity(intent);
            }
        });

        // 뒤로가기 이미지 클릭 시
        first_interest_back_image.setOnClickListener(v ->
        {
            // 강제종료하지 않았다면 기존 로직을 그대로 실행한다
            if (age.size() > 0 || area.size() > 0)
            {
                CustomInterestDialog dialog = new CustomInterestDialog(this);
                dialog.showDialog();
            }
            else
            {
                finish();
            }
        });
    }


    // 관심사 조회 메서드
    // "관심사 선택" 버튼을 눌러 이 화면으로 들어오면 show를 인자로 넘겨서 서버에 저장돼 있는 관심사 데이터들을 가져온다
    private void selectMyInterest()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.checkAndModifyInterest(token, null, null, null, null, "show");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
//                    Log.e(TAG, "서버에 저장된 내 관심사 : " + result);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        status_code = jsonObject.getInt("status_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //서버에서 정상적으로 값을 받았다면
                    if(status_code == 200){
                        parseInterest(result);
                    } else {
                        Toast.makeText(ChooseFirstInterestActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                    }


                }
                else
                {
                    Log.e(TAG, "관심사 조회 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "관심사 조회 에러 : " + t.getMessage());
            }
        });
    }

    private void parseInterest(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                user_age = inner_json.getString("age");
                user_local = inner_json.getString("local");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

//        Log.e(TAG, "user_age : " + user_age);
//        Log.e(TAG, "user_local : " + user_local);
        String[] age_arr = user_age.split("\\|");   // "|"을 split하려면 이스케이프 문자를 2번 넣어줘야 한다
        String[] local_arr = user_local.split("\\|");
//        Log.e(TAG, "age_arr : " + Arrays.toString(age_arr));
//        Log.e(TAG, "local_arr : " + Arrays.toString(local_arr));
        age_list = new ArrayList<>(Arrays.asList(age_arr));
        local_list = new ArrayList<>(Arrays.asList(local_arr));

        for (int i = 0; i < local_list.size(); i++)
        {
            Log.e(TAG, "local_list : " + local_list.get(i));
        }

        for (int i = 0; i < age_list.size(); i++)
        {
            Log.e(TAG, "age_list : " + age_list.get(i));
        }

        Button age_btn;
        Button local_btn;
        for (int i = 0; i < local_button_layout.getChildCount(); i++)
        {
            local_btn = (Button) local_button_layout.getChildAt(i);

            for (int j = 0; j < local_list.size(); j++)
            {
                if (local_btn.getText().toString().equals(local_list.get(j)))
                {
                    Log.e(TAG, "일치하는 것 : " + local_btn.getText().toString());
                    if (local_btn.getText().toString().equals(interest_seoul_btn.getText().toString()))
                    {
                        String value = "서울";
                        arr[7]++;
                        if (arr[7] % 2 == 0)
                        {
                            interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_gangwon_btn.getText().toString()))
                    {
                        String value = "강원";
                        arr[8]++;
                        if (arr[8] % 2 == 0)
                        {
                            interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_gyonggi_btn.getText().toString()))
                    {
                        String value = "경기";
                        arr[9]++;
                        if (arr[9] % 2 == 0)
                        {
                            interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_gyongnam_btn.getText().toString()))
                    {
                        String value = "경남";
                        arr[10]++;
                        if (arr[10] % 2 == 0)
                        {
                            interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_gyongbuk_btn.getText().toString()))
                    {
                        String value = "경북";
                        arr[11]++;
                        if (arr[11] % 2 == 0)
                        {
                            interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_gwangju_btn.getText().toString()))
                    {
                        String value = "광주";
                        arr[12]++;
                        if (arr[12] % 2 == 0)
                        {
                            interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_daegu_btn.getText().toString()))
                    {
                        String value = "대구";
                        arr[13]++;
                        if (arr[13] % 2 == 0)
                        {
                            interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_daejeon_btn.getText().toString()))
                    {
                        String value = "대전";
                        arr[14]++;
                        if (arr[14] % 2 == 0)
                        {
                            interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_busan_btn.getText().toString()))
                    {
                        String value = "부산";
                        arr[15]++;
                        if (arr[15] % 2 == 0)
                        {
                            interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_sejong_btn.getText().toString()))
                    {
                        String value = "세종";
                        arr[16]++;
                        if (arr[16] % 2 == 0)
                        {
                            interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_ulsan_btn.getText().toString()))
                    {
                        String value = "울산";
                        arr[17]++;
                        if (arr[17] % 2 == 0)
                        {
                            interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_incheon_btn.getText().toString()))
                    {
                        String value = "인천";
                        arr[18]++;
                        if (arr[18] % 2 == 0)
                        {
                            interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_jeonnam_btn.getText().toString()))
                    {
                        String value = "전남";
                        arr[19]++;
                        if (arr[19] % 2 == 0)
                        {
                            interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_jeonbuk_btn.getText().toString()))
                    {
                        String value = "전북";
                        arr[20]++;
                        if (arr[20] % 2 == 0)
                        {
                            interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_jeju_btn.getText().toString()))
                    {
                        String value = "제주";
                        arr[21]++;
                        if (arr[21] % 2 == 0)
                        {
                            interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_chungnam_btn.getText().toString()))
                    {
                        String value = "충남";
                        arr[22]++;
                        if (arr[22] % 2 == 0)
                        {
                            interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                    else if (local_btn.getText().toString().equals(interest_chungbuk_btn.getText().toString()))
                    {
                        String value = "충북";
                        arr[23]++;
                        if (arr[23] % 2 == 0)
                        {
                            interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            area.remove(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                        else
                        {
                            interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            area.add(value);
                            Log.e(TAG, "if 안 area list : " + area);
                        }
                    }
                }
            }
        }

        /* 나이대 */
        for (int i = 0; i < age_button_layout.getChildCount(); i++)
        {
            // 레이아웃의 자식 레이아웃 개수만큼 반복하는데 자식 레이아웃 중 버튼만 가져온다
            age_btn = (Button) age_button_layout.getChildAt(i);

            // age_list 안의 값과 버튼의 이름이 같다면, 그 같은 이름의 버튼 글자색과 테두리색을 바꾼다
            for (int j = 0; j < age_list.size(); j++)
            {
                if (age_btn.getText().toString().equals(age_list.get(j)))
                {
                    Log.e(TAG, "일치하는 것 : " + age_btn.getText().toString());
                    if (age_btn.getText().toString().equals(under_teenage_button.getText().toString()))
                    {
                        String value = "10대 미만";
                        arr[0]++;
                        if (arr[0] % 2 == 0)
                        {
                            under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(teenage_button.getText().toString()))
                    {
                        String value = "10대";
                        arr[1]++;
                        if (arr[1] % 2 == 0)
                        {
                            teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(twenty_button.getText().toString()))
                    {
                        String value = "20대";
                        arr[2]++;
                        if (arr[2] % 2 == 0)
                        {
                            twenty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            twenty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(thirty_button.getText().toString()))
                    {
                        String value = "30대";
                        arr[3]++;
                        if (arr[3] % 2 == 0)
                        {
                            thirty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            thirty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(forty_button.getText().toString()))
                    {
                        String value = "40대";
                        arr[4]++;
                        if (arr[4] % 2 == 0)
                        {
                            forty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            forty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(fifty_button.getText().toString()))
                    {
                        String value = "50대";
                        arr[5]++;
                        if (arr[5] % 2 == 0)
                        {
                            fifty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            fifty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                    else if (age_btn.getText().toString().equals(over_sixty_button.getText().toString()))
                    {
                        String value = "60대 이상";
                        arr[6]++;
                        if (arr[6] % 2 == 0)
                        {
                            over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            age.remove(value);
                            Log.e(TAG, "if 안 age list : " + age);
                        }
                        else
                        {
                            over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            age.add(value);
                            Log.e(TAG, "else 안 age list : " + age);
                        }
                    }
                }
            }
        }

    }

    // 나이, 지역 버튼 클릭 리스너들 모아놓은 메서드
    private void buttonsClickListener()
    {
        /* 나이대 */
        under_teenage_button.setOnClickListener(v ->
        {
            String value = "10대 미만";
            arr[0]++;
            if (arr[0] % 2 == 0)
            {
                // 짝수로 나눠서 나머지가 0이라면 유저가 선택하지 않았다는 뜻이다
                under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                // 나머지가 0이 아니라면 유저가 선택했다는 뜻이다
                under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        teenage_button.setOnClickListener(v ->
        {
            String value = "10대";
            arr[1]++;
            if (arr[1] % 2 == 0)
            {
                teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        twenty_button.setOnClickListener(v ->
        {
            String value = "20대";
            arr[2]++;
            if (arr[2] % 2 == 0)
            {
                twenty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                twenty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        thirty_button.setOnClickListener(v ->
        {
            String value = "30대";
            arr[3]++;
            if (arr[3] % 2 == 0)
            {
                thirty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                thirty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        forty_button.setOnClickListener(v ->
        {
            String value = "40대";
            arr[4]++;
            if (arr[4] % 2 == 0)
            {
                forty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                forty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        fifty_button.setOnClickListener(v ->
        {
            String value = "50대";
            arr[5]++;
            if (arr[5] % 2 == 0)
            {
                fifty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                fifty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        over_sixty_button.setOnClickListener(v ->
        {
            String value = "60대 이상";
            arr[6]++;
            if (arr[6] % 2 == 0)
            {
                over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        /* 지역 */
        interest_seoul_btn.setOnClickListener(v ->
        {
            String value = "서울";
            arr[7]++;
            if (arr[7] % 2 == 0)
            {
                interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gangwon_btn.setOnClickListener(v ->
        {
            String value = "강원";
            arr[8]++;
            if (arr[8] % 2 == 0)
            {
                interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyonggi_btn.setOnClickListener(v ->
        {
            String value = "경기";
            arr[9]++;
            if (arr[9] % 2 == 0)
            {
                interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyongnam_btn.setOnClickListener(v ->
        {
            String value = "경남";
            arr[10]++;
            if (arr[10] % 2 == 0)
            {
                interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyongbuk_btn.setOnClickListener(v ->
        {
            String value = "경북";
            arr[11]++;
            if (arr[11] % 2 == 0)
            {
                interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gwangju_btn.setOnClickListener(v ->
        {
            String value = "광주";
            arr[12]++;
            if (arr[12] % 2 == 0)
            {
                interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_daegu_btn.setOnClickListener(v ->
        {
            String value = "대구";
            arr[13]++;
            if (arr[13] % 2 == 0)
            {
                interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_daejeon_btn.setOnClickListener(v ->
        {
            String value = "대전";
            arr[14]++;
            if (arr[14] % 2 == 0)
            {
                interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_busan_btn.setOnClickListener(v ->
        {
            String value = "부산";
            arr[15]++;
            if (arr[15] % 2 == 0)
            {
                interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_sejong_btn.setOnClickListener(v ->
        {
            String value = "세종";
            arr[16]++;
            if (arr[16] % 2 == 0)
            {
                interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_ulsan_btn.setOnClickListener(v ->
        {
            String value = "울산";
            arr[17]++;
            if (arr[17] % 2 == 0)
            {
                interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_incheon_btn.setOnClickListener(v ->
        {
            String value = "인천";
            arr[18]++;
            if (arr[18] % 2 == 0)
            {
                interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeonnam_btn.setOnClickListener(v ->
        {
            String value = "전남";
            arr[19]++;
            if (arr[19] % 2 == 0)
            {
                interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeonbuk_btn.setOnClickListener(v ->
        {
            String value = "전북";
            arr[20]++;
            if (arr[20] % 2 == 0)
            {
                interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeju_btn.setOnClickListener(v ->
        {
            String value = "제주";
            arr[21]++;
            if (arr[21] % 2 == 0)
            {
                interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_chungnam_btn.setOnClickListener(v ->
        {
            String value = "충남";
            arr[22]++;
            if (arr[22] % 2 == 0)
            {
                interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_chungbuk_btn.setOnClickListener(v ->
        {
            String value = "충북";
            arr[23]++;
            if (arr[23] % 2 == 0)
            {
                interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });
    }

    // findViewById() 모아놓은 메서드
    private void init()
    {
        age_button_layout = findViewById(R.id.age_button_layout);
        local_button_layout = findViewById(R.id.local_button_layout);

        first_interest_back_image = findViewById(R.id.first_interest_back_image);
        first_select_interest_textview = findViewById(R.id.first_select_interest_textview);
        first_interest_top_textview = findViewById(R.id.first_interest_top_textview);
        age_text = findViewById(R.id.age_text);
        area_text = findViewById(R.id.area_text);

        under_teenage_button = findViewById(R.id.under_teenage_button);
        teenage_button = findViewById(R.id.teenage_button);
        twenty_button = findViewById(R.id.twenty_button);
        thirty_button = findViewById(R.id.thirty_button);
        forty_button = findViewById(R.id.forty_button);
        fifty_button = findViewById(R.id.fifty_button);
        over_sixty_button = findViewById(R.id.over_sixty_button);

        interest_seoul_btn = findViewById(R.id.interest_seoul_btn);
        interest_gangwon_btn = findViewById(R.id.interest_gangwon_btn);
        interest_gyonggi_btn = findViewById(R.id.interest_gyonggi_btn);
        interest_gyongnam_btn = findViewById(R.id.interest_gyongnam_btn);
        interest_gyongbuk_btn = findViewById(R.id.interest_gyongbuk_btn);
        interest_gwangju_btn = findViewById(R.id.interest_gwangju_btn);
        interest_daegu_btn = findViewById(R.id.interest_daegu_btn);
        interest_daejeon_btn = findViewById(R.id.interest_daejeon_btn);
        interest_busan_btn = findViewById(R.id.interest_busan_btn);
        interest_sejong_btn = findViewById(R.id.interest_sejong_btn);
        interest_ulsan_btn = findViewById(R.id.interest_ulsan_btn);
        interest_incheon_btn = findViewById(R.id.interest_incheon_btn);
        interest_jeonnam_btn = findViewById(R.id.interest_jeonnam_btn);
        interest_jeonbuk_btn = findViewById(R.id.interest_jeonbuk_btn);
        interest_jeju_btn = findViewById(R.id.interest_jeju_btn);
        interest_chungnam_btn = findViewById(R.id.interest_chungnam_btn);
        interest_chungbuk_btn = findViewById(R.id.interest_chungbuk_btn);

        go_second_interest_btn = findViewById(R.id.go_second_interest_btn);
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    private void setsize() {
        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        first_select_interest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        first_interest_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        age_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        area_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        go_second_interest_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (size.x * 0.055));
    }

    @Override
    public void onBackPressed()
    {
        //한번도 관심사 선택을 하지 않았다면
        if(!sharedSingleton.getBooleanInterst()){
            //1번째 백버튼 클릭
            if(System.currentTimeMillis()>backKeyPressedTime+2000){
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
            //2번째 백버튼 클릭 (종료)
            else{
                //앱종료
                finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                ActivityCompat.finishAffinity(this);
            }

        } else { //관심사 선택을 전에 한번이라도 선택 했다면
            if (age.size() > 0 || area.size() > 0)
            {
                CustomInterestDialog dialog = new CustomInterestDialog(this);
                dialog.showDialog();
            }
            else
            {
                finish();
            }
        }

    }
}