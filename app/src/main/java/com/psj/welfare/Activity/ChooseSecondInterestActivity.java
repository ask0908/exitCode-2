package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import androidx.core.content.ContextCompat;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 가구 형태, 카테고리 선택해 관심사 선택 완료하는 화면 */
public class ChooseSecondInterestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ArrayList<String> age;
    ArrayList<String> area;
    ArrayList<String> household_type;
    ArrayList<String> category;

    ImageView second_interest_back_image;
    TextView second_interest_top_textview, second_select_interest_textview, household_text, category_text;

    // 가구 형태
    Button interest_multiple_culture_button, interest_multi_child_button, interest_child_household_button, interest_adoptive_family_button,
            interest_one_or_old_family_button, other_form_button;

    // 카테고리
    Button interest_soldier_button, interest_farmer_button, interest_pregnancy_button, interest_foreigner_button, interest_low_income_button,
            interest_disabled_people_button, interest_patient_button, interest_student_button, other_category_button;

    // 선택 완료 버튼
    Button choose_complete_button;

    // 가구 형태, 카테고리 버튼 수만큼 int 배열 만들어서 클릭 횟수를 저장한다
    // 이 클릭 횟수를 통해 버튼 테두리를 바꾸거나 리스트에 값을 넣고 뺀다
    int[] arr = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    DBOpenHelper helper;
    String sqlite_token;
    // 서버 응답 코드, 메시지
    String message, status;
    String send_age, send_local, send_household, send_category;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_choose_second_interest);

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

        // findViewById() 모아놓은 메서드
        init();

        household_type = new ArrayList<>();
        category = new ArrayList<>();
        Intent intent = getIntent();
        age = (ArrayList<String>) intent.getSerializableExtra("age");
        area = (ArrayList<String>) intent.getSerializableExtra("area");

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        second_interest_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        second_select_interest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        household_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        category_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        choose_complete_button.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (size.x*0.055));

        // 가구 형태, 카테고리 버튼 클릭 리스너 모음
        buttonsClickListener();

        // 선택 완료 버튼
        choose_complete_button.setOnClickListener(v ->
        {
            // 4개 리스트 안의 값들 사이에 "-"를 붙여서 String으로 만든다
            // 그 후 api 인자로 넘겨서 관심사 선택 마무리
            StringBuilder age_builder = new StringBuilder();
            StringBuilder local_builder = new StringBuilder();
            StringBuilder household_builder = new StringBuilder();
            StringBuilder category_builder = new StringBuilder();

            if (age.size() == 0 || area.size() == 0 || household_type.size() == 0 || category.size() == 0)
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
                Log.e(TAG, "send_age : " + send_age);

                // 지역
                for (String str : area)
                {
                    local_builder.append(str);
                    local_builder.append("-");
                }
                send_local = local_builder.toString();
                send_local = send_local.substring(0, send_local.length() - 1);
                Log.e(TAG, "send_local : " + send_local);

                // 가구 형태
                for (String str : household_type)
                {
                    household_builder.append(str);
                    household_builder.append("-");
                }
                send_household = household_builder.toString();
                send_household = send_household.substring(0, send_household.length() - 1);
                Log.e(TAG, "send_household : " + send_household);

                // 카테고리
                for (String str : category)
                {
                    category_builder.append(str);
                    category_builder.append("-");
                }
                send_category = category_builder.toString();
                send_category = send_category.substring(0, send_category.length() - 1);
                Log.e(TAG, "send_category : " + send_category);

                saveMyInterest(send_age, send_local, send_household, send_category);
            }

        });

        second_interest_back_image.setOnClickListener(v ->
        {
            finish();
        });

    }

    // 관심사 추가
    void saveMyInterest(String age, String local, String household, String category)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.saveMyInterest(sqlite_token, age, local, household, category);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "관심사 추가 성공 : " + result);
                    parseResult(result);
                }
                else
                {
                    Log.e(TAG, "관심사 추가 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "관심사 추가 에러 : " + t.getMessage());
            }
        });
    }

    private void parseResult(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("message");
            status = jsonObject.getString("statusCode");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // 선택 완료를 눌러 관심사를 저장한 후 두 번째 화면을 종료하면 첫 번째 선택 화면도 같이 종료한다
        ChooseFirstInterestActivity first = (ChooseFirstInterestActivity) ChooseFirstInterestActivity.activity;
        first.finish();
        finish();
    }

    private void buttonsClickListener()
    {
        /* 가구 형태 */
        interest_multiple_culture_button.setOnClickListener(v ->
        {
            String value = "다문화";
            arr[0]++;
            if (arr[0] % 2 == 0)
            {
                interest_multiple_culture_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_multiple_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                interest_multiple_culture_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_multiple_culture_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                interest_multi_child_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_multi_child_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                interest_child_household_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_child_household_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                interest_adoptive_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_adoptive_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                interest_one_or_old_family_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_one_or_old_family_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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
                household_type.remove(value);
                Log.e(TAG, "if 안 household_type list : " + household_type);
            }
            else
            {
                other_form_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                other_form_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                household_type.add(value);
                Log.e(TAG, "else 안 household_type list : " + household_type);
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

    private void init()
    {
        second_interest_back_image = findViewById(R.id.second_interest_back_image);
        second_interest_top_textview = findViewById(R.id.second_interest_top_textview);

        second_select_interest_textview = findViewById(R.id.second_select_interest_textview);
        household_text = findViewById(R.id.household_text);
        category_text = findViewById(R.id.category_text);

        interest_multiple_culture_button = findViewById(R.id.interest_multiple_culture_button);
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

}