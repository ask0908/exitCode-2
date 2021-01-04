package com.psj.welfare.Test.GetUserInformation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.psj.welfare.R;
import com.psj.welfare.custom.OnSingleClickListener;

public class TestGetInformationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private final String TAG = this.getClass().getSimpleName();

    Toolbar age_toolbar;
    // 닉네임, 나이 입력받는 editText
    EditText nickname_edittext, age_edittext;
    int age;

    // 성별 입력받는 스피너
    Spinner gender_spinner;
    ArrayAdapter<String> adapter;
    String gender;

    // 지역 입력받는 NumberPicker
    NumberPicker area_picker;
    final String[] first_area = {"지역 선택", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기도 가평군",
            "경기도 고양시", "경기도 과천시", "경기도 광명시", "경기도 광주시", "경기도 구리시", "경기도 군포시", "경기도 남양주시", "경기도 동두천시", "경기도 부천시", "경기도 성남시",
            "경기도 수원시", "경기도 시흥시", "경기도 안산시", "경기도 안성시", "경기도 안양시", "경기도 양주시", "경기도 양평군", "경기도 여주시", "경기도 연천군", "경기도 오산시",
            "경기도 용인시", "경기도 의왕시", "경기도 의정부시", "경기도 이천시", "경기도 파주시", "경기도 평택시", "경기도 포천시", "경기도 하남시", "경기도 화성시"};
    String user_area;

    Button next_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_get_information);

        age_toolbar = findViewById(R.id.get_information_toolbar);
        setSupportActionBar(age_toolbar);
        getSupportActionBar().setTitle("개인정보 입력");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nickname_edittext = findViewById(R.id.nickname_edittext);
        age_edittext = findViewById(R.id.age_edittext);
        gender_spinner = findViewById(R.id.gender_spinner);
        area_picker = findViewById(R.id.area_picker);
        next_btn = findViewById(R.id.get_ok_btn);

        // 성별 선택하는 스피너 안에 값 채우기
        String[] names = {"이곳을 클릭하여 선택하세요", "남자", "여자"};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        gender_spinner.setAdapter(adapter);
        gender_spinner.setOnItemSelectedListener(this);

        area_picker.setMinValue(0);
        area_picker.setMaxValue(first_area.length - 1);
        area_picker.setDisplayedValues(first_area);
        area_picker.setWrapSelectorWheel(false);

        getUserArea(area_picker);

        next_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                int nickname_length = nickname_edittext.getText().length();
                if (age_edittext.getText().toString().equals(""))
                {
                    Toast.makeText(TestGetInformationActivity.this, "나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    age = Integer.parseInt(age_edittext.getText().toString());
                }

                if (nickname_length < 2)
                {
                    // 아무것도 입력하지 않았으면 포커스 줘서 입력하라고 유도
                    Toast.makeText(TestGetInformationActivity.this, "닉네임은 2글자 이상 입력해 주세요", Toast.LENGTH_SHORT).show();
                    nickname_edittext.requestFocus();
                    return;
                }

                if (age > 115)   // 한국 최고령자 나이 이상 입력 시
                {
                    Toast.makeText(TestGetInformationActivity.this, "정확한 나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (gender.equals("선택 안함"))
                {
                    Toast.makeText(TestGetInformationActivity.this, "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user_area == null)
                {
                    Toast.makeText(TestGetInformationActivity.this, "지역을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                String nickname = nickname_edittext.getText().toString();
                Log.e(TAG, "닉넴 : " + nickname + ", 나이 : " + age + "살, 성별 : " + gender + ", 지역 : " + user_area);
            }
        });
    }

    void getUserArea(NumberPicker picker)
    {
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                switch (newVal)
                {
                    case 0 :    // 시 · 도 선택
                        Log.e(TAG, "0번 값");
                        user_area = "값 없음";
                        break;

                    case 1 :    // 서울특별시
                        Log.e(TAG, "1번 값");
                        user_area = "서울특별시";
                        break;

                    case 2 :    // 부산광역시
                        Log.e(TAG, "2번 값");
                        user_area = "부산광역시";
                        break;

                    case 3 :    // 대구광역시
                        Log.e(TAG, "3번 값");
                        user_area = "대구광역시";
                        break;

                    case 4 :    // 인천광역시
                        Log.e(TAG, "4번 값");
                        user_area = "인천광역시";
                        break;

                    case 5 :    // 광주광역시
                        Log.e(TAG, "5번 값");
                        user_area = "광주광역시";
                        break;

                    case 6 :    // 대전광역시
                        Log.e(TAG, "6번 값");
                        user_area = "대전광역시";
                        break;

                    case 7 :    // 울산광역시
                        Log.e(TAG, "7번 값");
                        user_area = "울산광역시";
                        break;

                    case 8 :    // 세종시
                        /* 고운동만 나온다 */
                        Log.e(TAG, "8번 값");
                        user_area = "세종시";
                        break;

                    case 9 :    // 경기도 가평군
                        Log.e(TAG, "9번 값");
                        user_area = "경기도 가평군";
                        break;

                    case 10 :    // 경기도 고양시
                        Log.e(TAG, "10번 값");
                        user_area = "경기도 고양시";
                        break;

                    case 11 :    // 경기도 과천시
                        Log.e(TAG, "11번 값");
                        user_area = "경기도 과천시";
                        break;

                    case 12 :    // 경기도 광명시
                        Log.e(TAG, "12번 값");
                        user_area = "경기도 광명시";
                        break;

                    case 13 :    // 경기도 광주시
                        Log.e(TAG, "13번 값");
                        user_area = "경기도 광주시";
                        break;

                    case 14 :    // 경기도 구리시
                        Log.e(TAG, "14번 값");
                        user_area = "경기도 구리시";
                        break;

                    case 15 :    // 경기도 군포시
                        Log.e(TAG, "15번 값");
                        user_area = "경기도 군포시";
                        break;

                    case 16 :    // 경기도 남양주시
                        Log.e(TAG, "16번 값");
                        user_area = "경기도 남양주시";
                        break;

                    case 17 :    // 경기도 동두천시
                        Log.e(TAG, "17번 값");
                        user_area = "경기도 동두천시";
                        break;

                    case 18 :    // 경기도 부천시
                        Log.e(TAG, "18번 값");
                        user_area = "경기도 부천시";
                        break;

                    case 19 :    // 경기도 성남시
                        Log.e(TAG, "19번 값");
                        user_area = "경기도 성남시";
                        break;

                    case 20 :    // 경기도 수원시
                        Log.e(TAG, "20번 값");
                        user_area = "경기도 수원시";
                        break;

                    case 21 :    // 경기도 시흥시
                        Log.e(TAG, "21번 값");
                        user_area = "경기도 시흥시";
                        break;

                    case 22 :    // 경기도 안산시
                        Log.e(TAG, "22번 값");
                        user_area = "경기도 안산시";
                        break;

                    case 23 :    // 경기도 안성시
                        Log.e(TAG, "23번 값");
                        user_area = "경기도 안성시";
                        break;

                    case 24 :    // 경기도 안양시
                        Log.e(TAG, "24번 값");
                        user_area = "경기도 안양시";
                        break;

                    case 25 :    // 경기도 양주시
                        Log.e(TAG, "25번 값");
                        user_area = "경기도 양주시";
                        break;

                    case 26 :    // 경기도 양평군
                        Log.e(TAG, "26번 값");
                        user_area = "경기도 양평군";
                        break;

                    case 27 :    // 경기도 여주시
                        Log.e(TAG, "27번 값");
                        user_area = "경기도 여주시";
                        break;

                    case 28 :    // 경기도 연천군
                        Log.e(TAG, "28번 값");
                        user_area = "경기도 연천군";
                        break;

                    case 29 :    // 경기도 오산시
                        Log.e(TAG, "29번 값");
                        user_area = "경기도 오산시";
                        break;

                    case 30 :    // 경기도 용인시
                        Log.e(TAG, "30번 값");
                        user_area = "경기도 용인시";
                        break;

                    case 31 :    // 경기도 의왕시
                        Log.e(TAG, "31번 값");
                        user_area = "경기도 의왕시";
                        break;

                    case 32 :    // 경기도 의정부시
                        Log.e(TAG, "32번 값");
                        user_area = "경기도 의정부시";
                        break;

                    case 33 :    // 경기도 이천시
                        Log.e(TAG, "33번 값");
                        user_area = "경기도 이천시";
                        break;

                    case 34 :    // 경기도 파주시
                        Log.e(TAG, "34번 값");
                        user_area = "경기도 파주시";
                        break;

                    case 35 :    // 경기도 평택시
                        Log.e(TAG, "35번 값");
                        user_area = "경기도 평택시";
                        break;

                    case 36 :    // 경기도 포천시
                        Log.e(TAG, "36번 값");
                        user_area = "경기도 포천시";
                        break;

                    case 37 :    // 경기도 하남시
                        Log.e(TAG, "37번 값");
                        user_area = "경기도 하남시";
                        break;

                    case 38 :    // 경기도 화성시
                        Log.e(TAG, "38번 값");
                        user_area = "경기도 화성시";
                        break;

                    default:
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("정보 입력을 그만두시겠어요?")
                        .setMessage("나가시면 현재 입력된 정보는 저장되지 않아요. 그래도 그만두시겠어요?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Toast.makeText(TestGetInformationActivity.this, "취소함", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(TestGetInformationActivity.this, "이어서 함", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (position >= 0)
        {
            gender = String.valueOf(position);
            if (gender.equals("0"))
            {
                gender = "선택 안함";
                Toast.makeText(this, "선택 안함", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "선택 안함");
            }
            else if (gender.equals("1"))
            {
                gender = "남자";
                Toast.makeText(this, "남자", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "남자");
            }
            else if (gender.equals("2"))
            {
                gender = "여자";
                Toast.makeText(this, "여자", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "여자");
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        //
    }
}