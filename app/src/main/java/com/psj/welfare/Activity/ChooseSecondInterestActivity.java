package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 가구 형태, 카테고리 선택해 관심사 선택 완료하고 지금까지 선택한 관심사 데이터들을 서버에 저장하는 화면 */
public class ChooseSecondInterestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // 선택된 관심사 값들을 담을 리스트
    ArrayList<String> age;
    ArrayList<String> area;
    ArrayList<String> family;
    ArrayList<String> category;

    ImageView second_interest_back_image;
    TextView second_interest_top_textview, second_select_interest_textview, household_text, category_text;

    // 가구 형태
    ConstraintLayout household_button_layout;
    Button interest_culture_button, interest_multi_child_button, interest_child_household_button, interest_adoptive_family_button,
            interest_one_or_old_family_button, other_form_button;

    // 카테고리
    ConstraintLayout category_button_layout;
    Button interest_soldier_button, interest_farmer_button, interest_pregnancy_button, interest_foreigner_button, interest_low_income_button,
            interest_disabled_people_button, interest_patient_button, interest_student_button, other_category_button;

    // 선택 완료 버튼
    Button choose_complete_button;

    // 가구 형태, 카테고리 버튼 수만큼 int 배열 만들어서 클릭 횟수를 저장한다
    // 이 클릭 횟수를 통해 버튼 테두리를 바꾸거나 리스트에 값을 넣고 뺀다
    int[] arr = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

//    DBOpenHelper helper;
//    String sqlite_token;
    // 서버로 보낼 관심사(나이, 지역, 가구 형태, 카테고리)
    String send_age, send_local, send_family, send_category;

    // 가구 형태, 카테고리
    String user_category, user_family;
    // 가구 형태, 카테고리 값 담을 ArrayList
    ArrayList<String> category_list, family_list;

    // 강제종료해서 관심사 선택으로 온 건지 구별할 때 사용할 인텐트, 변수
    Intent force_stopped_intent;
//    int force_stopped_value = 0;
    String force_stopped;

    SharedPreferences sharedPreferences;

    //로그인 토큰 값
    private String token;

    //로그인 관련 쉐어드 singleton
    private SharedSingleton sharedSingleton;

    // API 호출 후 서버 응답코드
    private int status_code;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_choose_second_interest);

        //로그인 관련 쉐어드 singleton 사용
        sharedSingleton = SharedSingleton.getInstance(this);
        token = sharedSingleton.getToken();

        family = new ArrayList<>();
        category = new ArrayList<>();

//        helper = new DBOpenHelper(this);
//        helper.openDatabase();
//        helper.create();
//
//        Cursor cursor = helper.selectColumns();
//        if (cursor != null)
//        {
//            while (cursor.moveToNext())
//            {
//                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
//            }
//        }

        // findViewById() 모아놓은 메서드
        init();

        //xml크기를 동적으로 변환
        setsize();

        //ChooseFirstInterestActivity에서 인텐트로 받은 값 변수에 넣기
        getintentData();

        // 가구 형태, 카테고리 버튼 클릭 리스너 모음
        buttonsClickListener();

        //이전에 관심사 선택했었다면(관심사 수정이라면)
        if(sharedSingleton.getBooleanPreview()){
            //관심사 조회 하기
            selectMyInterest();
        }


        // 선택 완료 버튼
        choose_complete_button.setOnClickListener(v ->
        {
            // 4개 리스트 안의 값들 사이에 "-"를 붙여서 String으로 만든다
            // 그 후 api 인자로 넘겨서 관심사 선택 마무리
            StringBuilder age_builder = new StringBuilder();
            StringBuilder local_builder = new StringBuilder();
            StringBuilder household_builder = new StringBuilder();
            StringBuilder category_builder = new StringBuilder();

//            Intent intent = getIntent();
//            age = (ArrayList<String>) intent.getSerializableExtra("age");
//            area = (ArrayList<String>) intent.getSerializableExtra("area");
//
//            // findViewById() 모아놓은 메서드
//            init();
//
//            // 가구 형태, 카테고리 버튼 클릭 리스너 모음
//            buttonsClickListener();

            if (age.size() == 0 || area.size() == 0 || family.size() == 0 || category.size() == 0)
            {
                Toast.makeText(this, "가구 형태와 카테고리 모두 1개라도 선택해 주셔야 해요", Toast.LENGTH_SHORT).show();
            }
            else
            {
                /* 서버로 넘기기 위해 각 4개 리스트 요소 사이에 "-" 추가 */
                // 나이
                for (String str : age)
                {
                    age_builder.append(str);
                    age_builder.append("-");
                }
                send_age = age_builder.toString();
                send_age = send_age.substring(0, send_age.length() - 1);

                // 지역
                for (String str : area)
                {
                    local_builder.append(str);
                    local_builder.append("-");
                }
                send_local = local_builder.toString();
                send_local = send_local.substring(0, send_local.length() - 1);

                // 가구 형태
                for (String str : family)
                {
                    household_builder.append(str);
                    household_builder.append("-");
                }
                send_family = household_builder.toString();
                send_family = send_family.substring(0, send_family.length() - 1);

                // 카테고리
                for (String str : category)
                {
                    category_builder.append(str);
                    category_builder.append("-");
                }
                send_category = category_builder.toString();
                send_category = send_category.substring(0, send_category.length() - 1);


//                sharedPreferences = getSharedPreferences("app_pref", 0);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
////                editor.putString("force_stopped", "0");
//                editor.putString("interest_age", send_age);
//                editor.putString("interest_local", send_local);
//                editor.putString("interest_family", send_family);
//                editor.putString("interest_category", send_category);
//                editor.apply();


                //이전에 관심사 선택을 했었다면 관심사 수정
                if(sharedSingleton.getBooleanPreview()){
                    modifyMyInterest();
                } else { //이전에 관심사 선택을 안했었다면 관심사 추가
                    AddMyInterest();
                }

            }
        });

        // 뒤로 가기 이미지
        second_interest_back_image.setOnClickListener(v -> {
            finish();
        });

    }



    /* 관심사 추가 메서드 */
    private void AddMyInterest()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.AddMyInterest(token, send_age, send_local, send_family, send_category);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "관심사 수정 결과 : " + result);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        status_code = jsonObject.getInt("status_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //서버에서 정상적으로 값을 받았다면
                    if(status_code == 200){
                        parseModifyResult(result);
                    } else {
                        Toast.makeText(ChooseSecondInterestActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                    }


                }
                else
                {
                    Log.e(TAG, "관심사 수정 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "관심사 수정 에러 : " + t.getMessage());
            }
        });
    }


    /* 관심사 수정 메서드 */
    private void modifyMyInterest()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.checkAndModifyInterest(token, send_age, send_local, send_family, send_category, "modify");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "관심사 수정 결과 : " + result);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        status_code = jsonObject.getInt("status_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //서버에서 정상적으로 값을 받았다면
                    if(status_code == 200){
                        parseModifyResult(result);
                    } else {
                        Toast.makeText(ChooseSecondInterestActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                    }


                }
                else
                {
                    Log.e(TAG, "관심사 수정 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "관심사 수정 에러 : " + t.getMessage());
            }
        });
    }

    /* 관심사 수정 결과값 파싱 메서드 */
    private void parseModifyResult(String result)
    {
        String msg = null;
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            msg = jsonObject.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }




        if (msg != null && !msg.equals(""))
        {
            //첫 로그인시 관심사는 무조건 받아야 한다
            //관심사 선택을 한적이 없다면
            if(!sharedSingleton.getBooleanInterst()){

                Toast.makeText(this, "관심사 선택이 완료됐어요", Toast.LENGTH_SHORT).show();

                //관심사 선택 첫번째 클래스 액티비티 종료
                ChooseFirstInterestActivity firstInterestActivity = (ChooseFirstInterestActivity) ChooseFirstInterestActivity.activity;
                firstInterestActivity.finish();

                //로그인 액티비티 종료
                LoginActivity loginActivity = (LoginActivity) LoginActivity.activity;
                //로그인 누르고 바로 관심사 선택으로 넘어올 수도 있지만 첫 로그인시 앱 종료후 다시 키면 관심사 선택으로 넘어온다
                //그 때 로그인 화면 액티비티가 살아있으면 종료 시킨다
                if(loginActivity != null){
                    loginActivity.finish();
                }
                
                //관심사 선택을 했다고 설정 바꿈
                sharedSingleton.setBooleanInterst(true);

                //첫 로그인시 관심사를 선택 했으면 메인으로 보내야 한다
                //서버 연결해서 관심사 있는지 없는지
                Intent intent = new Intent(ChooseSecondInterestActivity.this, MainTabLayoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
            else { //관심사 선택을 했었다면
                Toast.makeText(this, "관심사 수정이 완료됐어요", Toast.LENGTH_SHORT).show();

                //관심사 선택 첫번째 클래스 액티비티 종료
                ChooseFirstInterestActivity firstInterestActivity = (ChooseFirstInterestActivity) ChooseFirstInterestActivity.activity;
                firstInterestActivity.finish();
                finish();
            }

        }
        else
        {
            Toast.makeText(this, "일시적인 오류가 발생했어요. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
        }

    }

    // 관심사 조회 메서드
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
                    Log.e(TAG, "서버에 저장된 내 관심사 : " + result);

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
                        Toast.makeText(ChooseSecondInterestActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
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

    /* 서버에서 받아온 관심사 값 파싱하는 메서드
    * 후에 첫 번째 화면에서 받아온 값을 인텐트로 넘겨받고 그 값으로 버튼 색깔 처리로직 수정해야 함 */
    private void parseInterest(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                user_category = inner_json.getString("category");
                user_family = inner_json.getString("family");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        String[] category_arr = user_category.split("\\|");
        String[] family_arr = user_family.split("\\|");
        category_list = new ArrayList<>(Arrays.asList(category_arr));
        family_list = new ArrayList<>(Arrays.asList(family_arr));

        Button category_btn;
        Button family_btn;

        // 레이아웃마다의 자식 레이아웃(버튼들)들을 가져온다
        // 가구 형태
        for(int i = 0; i < household_button_layout.getChildCount(); i++)
        {
            family_btn = (Button) household_button_layout.getChildAt(i);

            for (int j = 0; j < family_list.size(); j++)
            {
                if (family_btn.getText().toString().equals(family_list.get(j)))
                {
                    Log.e(TAG, "일치하는 것 : " + family_btn.getText().toString());
                    if (family_btn.getText().toString().equals(interest_culture_button.getText().toString()))
                    {
                        String value = "다문화";
                        arr[0]++;
                        if (arr[0] % 2 == 0)
                        {
                            interest_culture_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 family list : " + family);
                        }
                        else
                        {
                            interest_culture_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 family list : " + family);
                        }
                    }
                    else if (family_btn.getText().toString().equals(interest_multi_child_button.getText().toString()))
                    {
                        String value = "다자녀";
                        arr[1]++;
                        if (arr[1] % 2 == 0)
                        {
                            interest_multi_child_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_multi_child_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 household_type list : " + family);
                        }
                        else
                        {
                            interest_multi_child_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_multi_child_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 household_type list : " + family);
                        }
                    }
                    else if (family_btn.getText().toString().equals(interest_child_household_button.getText().toString()))
                    {
                        String value = "소년소녀 가장";
                        arr[2]++;
                        if (arr[2] % 2 == 0)
                        {
                            interest_child_household_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_child_household_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 household_type list : " + family);
                        }
                        else
                        {
                            interest_child_household_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_child_household_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 household_type list : " + family);
                        }
                    }
                    else if (family_btn.getText().toString().equals(interest_adoptive_family_button.getText().toString()))
                    {
                        String value = "입양가정";
                        arr[3]++;
                        if (arr[3] % 2 == 0)
                        {
                            interest_adoptive_family_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_adoptive_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 household_type list : " + family);
                        }
                        else
                        {
                            interest_adoptive_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_adoptive_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 household_type list : " + family);
                        }
                    }
                    else if (family_btn.getText().toString().equals(interest_one_or_old_family_button.getText().toString()))
                    {
                        String value = "한부모/조손가정";
                        arr[4]++;
                        if (arr[4] % 2 == 0)
                        {
                            interest_one_or_old_family_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_one_or_old_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 household_type list : " + family);
                        }
                        else
                        {
                            interest_one_or_old_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_one_or_old_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 household_type list : " + family);
                        }
                    }
                    else if (family_btn.getText().toString().equals(other_form_button.getText().toString()))
                    {
                        String value = "기타";
                        arr[5]++;
                        if (arr[5] % 2 == 0)
                        {
                            other_form_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            other_form_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            family.remove(value);
                            Log.e(TAG, "if 안 household_type list : " + family);
                        }
                        else
                        {
                            other_form_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            other_form_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            family.add(value);
                            Log.e(TAG, "else 안 household_type list : " + family);
                        }
                    }
                }
            }
        }

        // 카테고리
        for (int i = 0; i < category_button_layout.getChildCount(); i++)
        {
            category_btn = (Button) category_button_layout.getChildAt(i);
            for (int j = 0; j < category_list.size(); j++)
            {
                if (category_btn.getText().toString().equals(category_list.get(j)))
                {
                    Log.e(TAG, "일치하는 것 : " + category_btn.getText().toString());
                    if (category_btn.getText().toString().equals(interest_soldier_button.getText().toString()))
                    {
                        String value = "군인/보훈대상자";
                        arr[6]++;
                        if (arr[6] % 2 == 0)
                        {
                            interest_soldier_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_soldier_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_soldier_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_soldier_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_farmer_button.getText().toString()))
                    {
                        String value = "농축수산인";
                        arr[7]++;
                        if (arr[7] % 2 == 0)
                        {
                            interest_farmer_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_farmer_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_farmer_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_farmer_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_pregnancy_button.getText().toString()))
                    {
                        String value = "임신/출산";
                        arr[8]++;
                        if (arr[8] % 2 == 0)
                        {
                            interest_pregnancy_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_pregnancy_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_pregnancy_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_pregnancy_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_foreigner_button.getText().toString()))
                    {
                        String value = "외국인/재외국인";
                        arr[9]++;
                        if (arr[9] % 2 == 0)
                        {
                            interest_foreigner_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_foreigner_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_foreigner_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_foreigner_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_low_income_button.getText().toString()))
                    {
                        String value = "저소득층";
                        arr[10]++;
                        if (arr[10] % 2 == 0)
                        {
                            interest_low_income_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_low_income_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_low_income_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_low_income_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_disabled_people_button.getText().toString()))
                    {
                        String value = "장애인";
                        arr[11]++;
                        if (arr[11] % 2 == 0)
                        {
                            interest_disabled_people_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_disabled_people_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_disabled_people_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_disabled_people_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_patient_button.getText().toString()))
                    {
                        String value = "환자";
                        arr[12]++;
                        if (arr[12] % 2 == 0)
                        {
                            interest_patient_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_patient_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_patient_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_patient_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(interest_student_button.getText().toString()))
                    {
                        String value = "학생";
                        arr[13]++;
                        if (arr[13] % 2 == 0)
                        {
                            interest_student_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            interest_student_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            interest_student_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            interest_student_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                    else if (category_btn.getText().toString().equals(other_category_button.getText().toString()))
                    {
                        String value = "기타";
                        arr[14]++;
                        if (arr[14] % 2 == 0)
                        {
                            other_category_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                            other_category_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                            category.remove(value);
                            Log.e(TAG, "if 안 category list : " + category);
                        }
                        else
                        {
                            other_category_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                            other_category_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                            category.add(value);
                            Log.e(TAG, "else 안 category list : " + category);
                        }
                    }
                }
            }
        }

    }


    // 버튼 클릭 리스너 모아놓은 메서드
    private void buttonsClickListener()
    {
        /* 가구 형태 */
        interest_culture_button.setOnClickListener(v ->
        {
            String value = "다문화";
            arr[0]++;
            if (arr[0] % 2 == 0)
            {
                interest_culture_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                interest_culture_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        interest_multi_child_button.setOnClickListener(v ->
        {
            String value = "다자녀";
            arr[1]++;
            if (arr[1] % 2 == 0)
            {
                interest_multi_child_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_multi_child_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                interest_multi_child_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_multi_child_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        interest_child_household_button.setOnClickListener(v ->
        {
            String value = "소년소녀 가장";
            arr[2]++;
            if (arr[2] % 2 == 0)
            {
                interest_child_household_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_child_household_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                interest_child_household_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_child_household_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        interest_adoptive_family_button.setOnClickListener(v ->
        {
            String value = "입양가정";
            arr[3]++;
            if (arr[3] % 2 == 0)
            {
                interest_adoptive_family_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_adoptive_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                interest_adoptive_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_adoptive_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        interest_one_or_old_family_button.setOnClickListener(v ->
        {
            String value = "한부모/조손가정";
            arr[4]++;
            if (arr[4] % 2 == 0)
            {
                interest_one_or_old_family_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_one_or_old_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                interest_one_or_old_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_one_or_old_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        other_form_button.setOnClickListener(v ->
        {
            String value = "기타";
            arr[5]++;
            if (arr[5] % 2 == 0)
            {
                other_form_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                other_form_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                family.remove(value);
                Log.e(TAG, "if 안 household_type list : " + family);
            }
            else
            {
                other_form_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                other_form_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                family.add(value);
                Log.e(TAG, "else 안 household_type list : " + family);
            }
        });

        /* 카테고리 */
        interest_soldier_button.setOnClickListener(v ->
        {
            String value = "군인/보훈대상자";
            arr[6]++;
            if (arr[6] % 2 == 0)
            {
                interest_soldier_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_soldier_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_soldier_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_soldier_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_farmer_button.setOnClickListener(v ->
        {
            String value = "농축수산인";
            arr[7]++;
            if (arr[7] % 2 == 0)
            {
                interest_farmer_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_farmer_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_farmer_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_farmer_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_pregnancy_button.setOnClickListener(v ->
        {
            String value = "임신/출산";
            arr[8]++;
            if (arr[8] % 2 == 0)
            {
                interest_pregnancy_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_pregnancy_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_pregnancy_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_pregnancy_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_foreigner_button.setOnClickListener(v ->
        {
            String value = "외국인/재외국인";
            arr[9]++;
            if (arr[9] % 2 == 0)
            {
                interest_foreigner_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_foreigner_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_foreigner_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_foreigner_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_low_income_button.setOnClickListener(v ->
        {
            String value = "저소득층";
            arr[10]++;
            if (arr[10] % 2 == 0)
            {
                interest_low_income_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_low_income_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_low_income_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_low_income_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_disabled_people_button.setOnClickListener(v ->
        {
            String value = "장애인";
            arr[11]++;
            if (arr[11] % 2 == 0)
            {
                interest_disabled_people_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_disabled_people_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_disabled_people_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_disabled_people_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_patient_button.setOnClickListener(v ->
        {
            String value = "환자";
            arr[12]++;
            if (arr[12] % 2 == 0)
            {
                interest_patient_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_patient_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_patient_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_patient_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        interest_student_button.setOnClickListener(v ->
        {
            String value = "학생";
            arr[13]++;
            if (arr[13] % 2 == 0)
            {
                interest_student_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_student_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                interest_student_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_student_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });

        other_category_button.setOnClickListener(v ->
        {
            String value = "기타";
            arr[14]++;
            if (arr[14] % 2 == 0)
            {
                other_category_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                other_category_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                category.remove(value);
                Log.e(TAG, "if 안 category list : " + category);
            }
            else
            {
                other_category_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                other_category_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                category.add(value);
                Log.e(TAG, "else 안 category list : " + category);
            }
        });
    }

    //ChooseFirstInterestActivity에서 인텐트로 받은 값 변수에 넣기
    private void getintentData() {
        Intent intent = getIntent();
        age = (ArrayList<String>) intent.getSerializableExtra("age");
        area = (ArrayList<String>) intent.getSerializableExtra("area");

        for (int i = 0; i < age.size(); i++){
            Log.e(TAG,"age : " + age.get(i));
        }
    }

    private void init()
    {
        household_button_layout = findViewById(R.id.household_button_layout);
        category_button_layout = findViewById(R.id.category_button_layout);

        second_interest_back_image = findViewById(R.id.second_interest_back_image);
        second_interest_top_textview = findViewById(R.id.second_interest_top_textview);

        second_select_interest_textview = findViewById(R.id.second_select_interest_textview);
        household_text = findViewById(R.id.household_text);
        category_text = findViewById(R.id.category_text);

        interest_culture_button = findViewById(R.id.interest_culture_button);
        interest_multi_child_button = findViewById(R.id.interest_multi_child_button);
        interest_child_household_button = findViewById(R.id.interest_child_household_button);
        interest_adoptive_family_button = findViewById(R.id.interest_adoptive_family_button);
        interest_one_or_old_family_button = findViewById(R.id.interest_one_or_old_family_button);
        other_form_button = findViewById(R.id.other_form_button);

        interest_soldier_button = findViewById(R.id.interest_soldier_button);
        interest_farmer_button = findViewById(R.id.interest_farmer_button);
        interest_pregnancy_button = findViewById(R.id.interest_pregnancy_button);
        interest_foreigner_button = findViewById(R.id.interest_foreigner_button);
        interest_low_income_button = findViewById(R.id.interest_low_income_button);
        interest_disabled_people_button = findViewById(R.id.interest_disabled_people_button);
        interest_patient_button = findViewById(R.id.interest_patient_button);
        interest_student_button = findViewById(R.id.interest_student_button);
        other_category_button = findViewById(R.id.other_category_button);

        choose_complete_button = findViewById(R.id.choose_complete_button);
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    //xml크기를 동적으로 변환
    private void setsize() {
        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        second_interest_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        second_select_interest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        household_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        category_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        choose_complete_button.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (size.x*0.055));
    }

}