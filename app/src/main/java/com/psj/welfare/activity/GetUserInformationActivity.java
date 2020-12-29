package com.psj.welfare.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetUserInformationActivity extends AppCompatActivity
{
    private ConstraintLayout third_fragment_layout;
    private final String TAG = this.getClass().getName();
    private NumberPicker area_picker;                   // 지역 선택하는 NumberPicker
    private EditText age_edittext, nickname_edittext;   // 나이, 닉네임 입력하는 editText
    private Button man_btn, woman_btn, next_btn;        // 남자/여자 버튼, 확인 버튼
    // 스피너에서 아이템을 선택했는지 여부를 확인하는 데 사용할 String 변수. 여기에 시 · 도 선택, 시 · 군 · 구 선택이 들어있다면 다음 액티비티로 넘어가지 못하게 한다
    String first_spinner_value;

    // 성별, 나이, 지역, 닉네임(사용자 정보)을 담을 변수. MainTabLayoutActivity로 이동 시 같이 가져간다
    String gender, age, area, user_nickname;

    // 성별, 나이, 지역, 닉네임을 저장할 쉐어드
    SharedPreferences sharedPreferences;

    // 서버에서 받은 결과값 파싱할 때 사용할 변수
    String age_group, user_gender, interest;
    String token, server_token;

    SharedPreferences app_pref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_information);

        init();
        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        sendUserTypeAndPlatform();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if (!task.isSuccessful())
                {
                    Log.w("FCM LOG", "getInstanceId failed", task.getException());
                    return;
                }
                token = task.getResult().getToken();
                Log.e(TAG, "FCM token = " + token);
            }
        });

        // 화면의 빈 공간을 누르면 키보드가 사라지게 한다
        third_fragment_layout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                hideKeyboard();
                return false;
            }
        });

        /* 스피너를 NumberPicker로 바꿔서 iOS 스피너처럼 보이도록 만든다 */
        final String[] first_area = {"지역 선택", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기도 가평군",
                "경기도 고양시", "경기도 과천시", "경기도 광명시", "경기도 광주시", "경기도 구리시", "경기도 군포시", "경기도 남양주시", "경기도 동두천시", "경기도 부천시", "경기도 성남시",
                "경기도 수원시", "경기도 시흥시", "경기도 안산시", "경기도 안성시", "경기도 안양시", "경기도 양주시", "경기도 양평군", "경기도 여주시", "경기도 연천군", "경기도 오산시",
                "경기도 용인시", "경기도 의왕시", "경기도 의정부시", "경기도 이천시", "경기도 파주시", "경기도 평택시", "경기도 포천시", "경기도 하남시", "경기도 화성시"};
//        final String[] seoul_area = {"강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구",
//                "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구", "중랑구"};
//        final String[] busan_area = {"강서구", "금정구", "기장군", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구",
//                "중구", "해운대구"};
//        final String[] daegu_area = {"남구", "달서구", "달성군", "동구", "북구", "서구", "수성구", "중구"};
//        final String[] incheon_area = {"강화군", "계양구", "남동구", "동구", "미추홀구", "부평구", "서구", "연수구", "옹진군", "중구"};
//        final String[] gwanju_area = {"광산구", "남구", "동구", "북구", "서구"};
//        final String[] daejeon_area = {"대덕구", "동구", "서구", "유성구", "중구"};
//        final String[] ulsan_area = {"남구", "동구", "북구", "서구", "울주군"};
//        final String[] sejong_area = {"고운동", "금남면", "대평동", "도담동", "보람동", "부강면", "새롬동", "소담동", "소정면", "아름동", "연기면", "연동면", "연서면", "장군면",
//                "전동면", "전의면", "조치원읍", "종촌동", "한솔동"};
//        final String[] gyongi_goyang_area = {"덕양구", "일산동구", "일산서구"};
//        final String[] gyongi_seongnam_area = {"분당구", "수정구", "중원구"};
//        final String[] gyongi_suwon_area = {"권선구", "영통구", "장안구", "팔달구"};
//        final String[] gyongi_ansan_area = {"단원구", "상록구"};
//        final String[] gyongi_anyang_area = {"동안구", "만안구"};
//        final String[] gyongi_yongin_area = {"기흥구", "수지구", "처인구"};
//        final String[] nothing = {"위에서 시 · 도를 선택하지 않으셨군요", "시 · 도를 먼저 선택해 주세요!"};
//        final String[] area_exception = {"선택이 완료되셨나요?", "완료되셨다면 확인 버튼을 눌러주세요"};

        // 스피너에 지역명 세팅
        area_picker.setMinValue(0);
        area_picker.setMaxValue(first_area.length - 1);
        area_picker.setDisplayedValues(first_area);
        area_picker.setWrapSelectorWheel(false);

        // To change format of number in NumberPicker
        area_picker.setFormatter(new NumberPicker.Formatter()
        {
            @Override
            public String format(int value)
            {
                switch (value)
                {
                    case 0 :
                        first_spinner_value = "시 · 도 선택";
                        return "시 · 도 선택";

                    case 1 :
                        first_spinner_value = "서울특별시";
                        return "서울특별시";

                    case 2 :
                        first_spinner_value = "부산광역시";
                        return "부산광역시";

                    default:
                        break;
                }
                Log.e("1번 setFormatter()", "value : " + value);
                return null;
            }
        });

        // 1번째 스피너에서 지역을 선택하면 그것을 전역변수에 담아 쉐어드에 저장할 준비를 한다
        area_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                switch (newVal)
                {
                    case 0 :    // 시 · 도 선택
                        Log.e(TAG, "0번 값");
                        first_spinner_value = "값 없음";
                        break;

                    case 1 :    // 서울특별시
                        Log.e(TAG, "1번 값");
                        first_spinner_value = "서울특별시";
                        break;

                    case 2 :    // 부산광역시
                        Log.e(TAG, "2번 값");
                        first_spinner_value = "부산광역시";
                        break;

                    case 3 :    // 대구광역시
                        Log.e(TAG, "3번 값");
                        first_spinner_value = "대구광역시";
                        break;

                    case 4 :    // 인천광역시
                        Log.e(TAG, "4번 값");
                        first_spinner_value = "인천광역시";
                        break;

                    case 5 :    // 광주광역시
                        Log.e(TAG, "5번 값");
                        first_spinner_value = "광주광역시";
                        break;

                    case 6 :    // 대전광역시
                        Log.e(TAG, "6번 값");
                        first_spinner_value = "대전광역시";
                        break;

                    case 7 :    // 울산광역시
                        Log.e(TAG, "7번 값");
                        first_spinner_value = "울산광역시";
                        break;

                    case 8 :    // 세종시
                        /* 고운동만 나온다 */
                        Log.e(TAG, "8번 값");
                        first_spinner_value = "세종시";
                        break;

                    case 9 :    // 경기도 가평군
                        Log.e(TAG, "9번 값");
                        first_spinner_value = "경기도 가평군";
                        break;

                    case 10 :    // 경기도 고양시
                        Log.e(TAG, "10번 값");
                        first_spinner_value = "경기도 고양시";
                        break;

                    case 11 :    // 경기도 과천시
                        Log.e(TAG, "11번 값");
                        first_spinner_value = "경기도 과천시";
                        break;

                    case 12 :    // 경기도 광명시
                        Log.e(TAG, "12번 값");
                        first_spinner_value = "경기도 광명시";
                        break;

                    case 13 :    // 경기도 광주시
                        Log.e(TAG, "13번 값");
                        first_spinner_value = "경기도 광주시";
                        break;

                    case 14 :    // 경기도 구리시
                        Log.e(TAG, "14번 값");
                        first_spinner_value = "경기도 구리시";
                        break;

                    case 15 :    // 경기도 군포시
                        Log.e(TAG, "15번 값");
                        first_spinner_value = "경기도 군포시";
                        break;

                    case 16 :    // 경기도 남양주시
                        Log.e(TAG, "16번 값");
                        first_spinner_value = "경기도 남양주시";
                        break;

                    case 17 :    // 경기도 동두천시
                        Log.e(TAG, "17번 값");
                        first_spinner_value = "경기도 동두천시";
                        break;

                    case 18 :    // 경기도 부천시
                        Log.e(TAG, "18번 값");
                        first_spinner_value = "경기도 부천시";
                        break;

                    case 19 :    // 경기도 성남시
                        Log.e(TAG, "19번 값");
                        first_spinner_value = "경기도 성남시";
                        break;

                    case 20 :    // 경기도 수원시
                        Log.e(TAG, "20번 값");
                        first_spinner_value = "경기도 수원시";
                        break;

                    case 21 :    // 경기도 시흥시
                        Log.e(TAG, "21번 값");
                        first_spinner_value = "경기도 시흥시";
                        break;

                    case 22 :    // 경기도 안산시
                        Log.e(TAG, "22번 값");
                        first_spinner_value = "경기도 안산시";
                        break;

                    case 23 :    // 경기도 안성시
                        Log.e(TAG, "23번 값");
                        first_spinner_value = "경기도 안성시";
                        break;

                    case 24 :    // 경기도 안양시
                        Log.e(TAG, "24번 값");
                        first_spinner_value = "경기도 안양시";
                        break;

                    case 25 :    // 경기도 양주시
                        Log.e(TAG, "25번 값");
                        first_spinner_value = "경기도 양주시";
                        break;

                    case 26 :    // 경기도 양평군
                        Log.e(TAG, "26번 값");
                        first_spinner_value = "경기도 양평군";
                        break;

                    case 27 :    // 경기도 여주시
                        Log.e(TAG, "27번 값");
                        first_spinner_value = "경기도 여주시";
                        break;

                    case 28 :    // 경기도 연천군
                        Log.e(TAG, "28번 값");
                        first_spinner_value = "경기도 연천군";
                        break;

                    case 29 :    // 경기도 오산시
                        Log.e(TAG, "29번 값");
                        first_spinner_value = "경기도 오산시";
                        break;

                    case 30 :    // 경기도 용인시
                        Log.e(TAG, "30번 값");
                        first_spinner_value = "경기도 용인시";
                        break;

                    case 31 :    // 경기도 의왕시
                        Log.e(TAG, "31번 값");
                        first_spinner_value = "경기도 의왕시";
                        break;

                    case 32 :    // 경기도 의정부시
                        Log.e(TAG, "32번 값");
                        first_spinner_value = "경기도 의정부시";
                        break;

                    case 33 :    // 경기도 이천시
                        Log.e(TAG, "33번 값");
                        first_spinner_value = "경기도 이천시";
                        break;

                    case 34 :    // 경기도 파주시
                        Log.e(TAG, "34번 값");
                        first_spinner_value = "경기도 파주시";
                        break;

                    case 35 :    // 경기도 평택시
                        Log.e(TAG, "35번 값");
                        first_spinner_value = "경기도 평택시";
                        break;

                    case 36 :    // 경기도 포천시
                        Log.e(TAG, "36번 값");
                        first_spinner_value = "경기도 포천시";
                        break;

                    case 37 :    // 경기도 하남시
                        Log.e(TAG, "37번 값");
                        first_spinner_value = "경기도 하남시";
                        break;

                    case 38 :    // 경기도 화성시
                        Log.e(TAG, "38번 값");
                        first_spinner_value = "경기도 화성시";
                        break;

                    default:
                        break;
                }
            }
        });

        // 남자 버튼 클릭 시
        // 여자 버튼을 누른 채로 남자 버튼을 누르면, 여자 버튼은 원상태로 돌아오고 남자 버튼이 선택된 상태가 돼야 한다
        man_btn.setOnClickListener(OnSingleClickListener ->
        {
            woman_btn.setSelected(false);
            woman_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "남자 버튼 클릭 해제됨");
                man_btn.setSelected(false);
                man_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            }
            else
            {
                Log.e(TAG, "남자 버튼 클릭됨");
                man_btn.setSelected(true);
                man_btn.setTextColor(getResources().getColor(R.color.colorMainWhite));
                gender = "남자";
            }
        });

        // 여자 버튼 클릭 이벤트
        // 남자 버튼을 누른 상태로 여자 버튼을 누르면, 남자 버튼은 원상태로 돌아오고 여자 버튼이 선택된 상태가 돼야 한다
        woman_btn.setOnClickListener(OnSingleClickListener ->
        {
            man_btn.setSelected(false);
            man_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "여자 버튼 클릭 해제됨");
                woman_btn.setSelected(false);
                woman_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            }
            else
            {
                Log.e(TAG, "여자 버튼 클릭됨");
                woman_btn.setSelected(true);
                woman_btn.setTextColor(getResources().getColor(R.color.colorMainWhite));
                gender = "여자";
            }
        });

        // 다음 버튼을 누르면 입력된 사용자 기본 정보를 갖고 액티비티 이동
        next_btn.setOnClickListener(v ->
        {
            // 값들 다 입력했는지 확인
            if (age_edittext.getText().toString().equals(""))
            {
                Toast.makeText(GetUserInformationActivity.this, "나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                age = age_edittext.getText().toString();
            }

            if (Integer.parseInt(age_edittext.getText().toString()) > 115)
            {
                // 현재 한국 최고령자의 나이인 115세 이상 입력했을 경우
                Toast.makeText(GetUserInformationActivity.this, "올바른 나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!gender.equals("남자") && !gender.equals("여자"))
            {
                Toast.makeText(GetUserInformationActivity.this, "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (first_spinner_value.equals("값 없음"))
            {
                Toast.makeText(GetUserInformationActivity.this, "지역을 선택해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nickname_edittext.getText().toString().equals(""))
            {
                Toast.makeText(GetUserInformationActivity.this, "닉네임을 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            // 인텐트 설정 및 보낼 데이터 (나이, 성별, 지역)
//           Intent intent = new Intent(GetUserInformationActivity.this, MainTabLayoutActivity.class);

            String request_value = age + gender + area;

            /* 키워드를 선택하기 위해 MainTabLayoutActivity가 아닌 ChoiceKeywordActivity로 이동되게 처리
            * MainTabLayoutActivity는 ChoiceKeywordActivity에서 필요한 작업을 다 마치면 이동하게 한다 */
            user_nickname = nickname_edittext.getText().toString();
            area = first_spinner_value;
            Log.e(TAG, "나이 = " + age + ", 성별 = " + gender + ", 지역 = " + area + ", 닉네임 = " + user_nickname);
            // 나이, 성별, 지역, 닉네임을 쉐어드, 서버에 저장한 뒤 액티비티 이동
            editor.putString("user_age", age);
            editor.putString("user_gender", gender);
            editor.putString("user_area", area);
            editor.putString("user_nickname", user_nickname);
            editor.apply();
            registerUserInfo();
            sendLog("사용자 기본 정보 입력 화면에서 확인 버튼 눌려짐", request_value);
        });

    }

    void sendLog(String content, String request_value)
    {
        String token = sharedPreferences.getString("token", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.sendLog("안드로이드", LogUtil.getVersion(), token, content, request_value);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 = " + result);
                }
                else
                {
                    Log.e(TAG, "실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 사용자 정보를 서버에 저장하는 메서드
    void registerUserInfo()
    {
        String token = sharedPreferences.getString("token", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.registerUserInfo(token, user_nickname, age, gender, area);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "response = " + response.body());
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

    private void jsonParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                age_group = inner_obj.getString("age_group");
                user_gender = inner_obj.getString("gender");
                interest = inner_obj.getString("interest");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, "age_group = " + age_group);
        Log.e(TAG, "gender = " + user_gender);
        Log.e(TAG, "interest = " + interest);
        Intent intent = new Intent(GetUserInformationActivity.this, ChoiceKeywordActivity.class);
        intent.putExtra("age_group", age_group);
        intent.putExtra("user_gender", user_gender);
        intent.putExtra("interest", interest);
        startActivity(intent);
    }

    void sendUserTypeAndPlatform()
    {
        String osType = getString(R.string.login_os);
        String platform = getString(R.string.main_platform);
        String fcm_token = token;
        String email = getString(R.string.email2);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.sendUserTypeAndPlatform(osType, platform, fcm_token, email);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    tokenParsing(response.body());
                }
                else
                {
                    Logger.e("onResponse() 실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.e("에러 = " + t.getMessage());
            }
        });
    }

    private void tokenParsing(String data)
    {
        app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        SharedPreferences.Editor editor = app_pref.edit();

        try
        {
            JSONObject token_object = new JSONObject(data);
            server_token = token_object.getString("Token");
            editor.putString("token", server_token);
            editor.apply();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Logger.e("server_token = " + server_token);
    }

    /* editText 바깥을 터치했을 때 키보드를 내리는 메서드 */
    private void hideKeyboard()
    {
        // 프래그먼트기 때문에 getActivity() 사용
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void init()
    {
        third_fragment_layout = findViewById(R.id.third_fragment_layout);
        area_picker = findViewById(R.id.first_choice_spinner);
        age_edittext = findViewById(R.id.age_edittext);
        nickname_edittext = findViewById(R.id.nickname_edittext);
        man_btn = findViewById(R.id.man_btn);
        woman_btn = findViewById(R.id.woman_btn);
        next_btn = findViewById(R.id.next_btn);
    }
}