package com.benefit.welfare.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.benefit.welfare.R;
import com.benefit.welfare.API.ApiClient;
import com.benefit.welfare.API.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 유저에게 기본 정보(나이, 성별, 지역)를 입력받는 화면 */
public class GetUserInformationActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    public static GetUserInformationActivity activity;

    Toolbar age_toolbar;
    // 닉네임, 나이 입력받는 editText
    EditText nickname_edittext, age_edittext;
    int age;
    String nickname;

    // 성별 입력받는 라디오 버튼
    RadioGroup radioGroup;
    RadioButton male_radiobutton, female_radiobutton;
    String gender;

    // 지역 입력받는 NumberPicker
    NumberPicker area_picker;
    final String[] first_area = {"지역 선택", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기도 가평군",
            "경기도 고양시", "경기도 과천시", "경기도 광명시", "경기도 광주시", "경기도 구리시", "경기도 군포시", "경기도 남양주시", "경기도 동두천시", "경기도 부천시", "경기도 성남시",
            "경기도 수원시", "경기도 시흥시", "경기도 안산시", "경기도 안성시", "경기도 안양시", "경기도 양주시", "경기도 양평군", "경기도 여주시", "경기도 연천군", "경기도 오산시",
            "경기도 용인시", "경기도 의왕시", "경기도 의정부시", "경기도 이천시", "경기도 파주시", "경기도 평택시", "경기도 포천시", "경기도 하남시", "경기도 화성시"};
    // NumberPicker에서 선택한 도시명을 담을 변수
    String user_area;

    // 성별, 나이, 지역, 닉네임을 저장할 쉐어드
    SharedPreferences app_pref;

    // 서버에서 받은 결과값 파싱할 때 사용할 변수
    String age_group, user_gender, interest;

    // SplashActivity에서 생성된 FCM 토큰을 담을 변수
    String token, server_token;

    // 메뉴 버튼의 색을 바꾸는 데 사용할 메뉴 객체
    private Menu mOptionMenu;

    String encode_str;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_information);

        activity = GetUserInformationActivity.this;
        age_toolbar = findViewById(R.id.get_information_toolbar);
        setSupportActionBar(age_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        app_pref = getSharedPreferences("app_pref", 0);
        /* 서버에서 받은 유저 별 토큰값을 저장해 관심사를 보낼 때 사용한다 */
        token = app_pref.getString("token", "");

        // 인텐트로 넘어왔을 때 값을 확인해서 MypageFragment에서 보낸 값과 일치할 경우 쉐어드에 저장된 유저의 정보를 가져와 set
        if (getIntent().hasExtra("edit"))
        {
            Intent intent = getIntent();
            int editable = intent.getIntExtra("edit", -1);
            if (editable == 1)
            {
                String nickname = app_pref.getString("user_nickname", "");
                String age = app_pref.getString("user_age", "");
                String gender = app_pref.getString("user_gender", "");
                String area = app_pref.getString("user_area", "");
                // editText에 set
                nickname_edittext.setText(nickname);
                age_edittext.setText(age);

                // radiobutton에 set
                if (!gender.equals(""))
                {
                    if (gender.equals("남자"))
                    {
                        male_radiobutton.setChecked(true);
                        female_radiobutton.setChecked(false);
                    }
                    else if (gender.equals("여자"))
                    {
                        female_radiobutton.setChecked(true);
                        male_radiobutton.setChecked(false);
                    }
                }
                // 지역에 따라 NumberPicker 선택값 변경
                if (!area.equals(""))
                {
                    area_picker.setMinValue(0);
                    area_picker.setMaxValue(first_area.length - 1);
                    area_picker.setDisplayedValues(first_area);
                    area_picker.setWrapSelectorWheel(false);
                }
                else
                {
                    area_picker.setMinValue(0);
                    area_picker.setMaxValue(first_area.length - 1);
                    area_picker.setDisplayedValues(first_area);
                    area_picker.setWrapSelectorWheel(false);
                }
            }
        }
        else
        {
            /* 성별 고르는 라디오 버튼 선언, 처리 */
            gender = "선택 안함";   // 아직 어떤 라디오 버튼도 눌러지지 않아서 초기값은 선택 안함으로 한다
            Log.e(TAG, "라디오 버튼 선택 안했을 때 gender 값 = " + gender);

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId)
                {
                    if (checkedId == R.id.male_radiobutton)
                    {
                        gender = "남자";
                        Log.e(TAG, "gender = " + gender);
                    }
                    else if (checkedId == R.id.female_radiobutton)
                    {
                        gender = "여자";
                        Log.e(TAG, "gender = " + gender);
                    }
                }
            });

            // 지역 선택하는 NumberPicker 처리
            area_picker.setMinValue(0);
            area_picker.setMaxValue(first_area.length - 1);
            area_picker.setDisplayedValues(first_area);
            area_picker.setWrapSelectorWheel(false);
        }

        /* 화면의 빈 공간을 누르면 키보드가 사라지게 한다
         * 이걸 키보드가 올라왔을 때만 작동하도록 조건을 줘야 함. 들어오자마자 화면 터치하면 앱이 죽음 */
//        get_user_inform_activity.setOnTouchListener(new View.OnTouchListener()
//        {
//            @Override
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                hideKeyboard();
//                return false;
//            }
//        });

//        /* 성별 고르는 라디오 버튼 선언, 처리 */
//        gender = "선택 안함";   // 아직 어떤 라디오 버튼도 눌러지지 않아서 초기값은 선택 안함으로 한다
//        Log.e(TAG, "라디오 버튼 선택 안했을 때 gender 값 = " + gender);
//
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
//        {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId)
//            {
//                if (checkedId == R.id.male_radiobutton)
//                {
//                    gender = "남자";
//                    Log.e(TAG, "gender = " + gender);
//                }
//                else if (checkedId == R.id.female_radiobutton)
//                {
//                    gender = "여자";
//                    Log.e(TAG, "gender = " + gender);
//                }
//            }
//        });
//
//        setSupportActionBar(age_toolbar);
//        getSupportActionBar().setTitle("");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        // 지역 선택하는 NumberPicker 처리
//        area_picker.setMinValue(0);
//        area_picker.setMaxValue(first_area.length - 1);
//        area_picker.setDisplayedValues(first_area);
//        area_picker.setWrapSelectorWheel(false);

        getUserArea(area_picker);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == R.id.male_radiobutton)
                {
                    gender = "남자";
                    Log.e(TAG, "gender = " + gender);
                }
                else if (checkedId == R.id.female_radiobutton)
                {
                    gender = "여자";
                    Log.e(TAG, "gender = " + gender);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mOptionMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.keyword_menu, menu);

        // 메뉴 버튼 색 바꾸기
        MenuItem liveitem = mOptionMenu.findItem(R.id.keyword_ok);
        SpannableString spannableString = new SpannableString(liveitem.getTitle().toString());
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryDark)), 0, spannableString.length(), 0);
        liveitem.setTitle(spannableString);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                // 좌상단 백버튼 눌렀을 시(뭔가를 입력했을 때만 나타나야 한다)

                // 각 EditText 별 입력된 문자열 개수를 가져와서 이 개수가 일정 개수 이하면 다이얼로그가 나오도록 한다?
                // 그럼 일정 개수 이상이면 안 나온다는 건가? 일정 개수 이상이어야 나오는 게 맞을 것 같다
                int nicknames = nickname_edittext.getText().toString().length();
                int ages = age_edittext.getText().toString().length();
                if (nicknames > 0 || ages > 0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GetUserInformationActivity.this);
                    builder.setMessage("지금 나가시면 입력했던 정보들은 저장되지 않아요\n그래도 나가시겠어요?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    finish();
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
                else
                {
                    finish();
                }
                break;

            case R.id.keyword_ok:
                // 우상단 등록 버튼
                SharedPreferences.Editor editor = app_pref.edit();
                int nickname_length = nickname_edittext.getText().length();
                if (age_edittext.getText().toString().equals(""))
                {
                    Toast.makeText(GetUserInformationActivity.this, "나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    age = Integer.parseInt(age_edittext.getText().toString());
                }

                if (nickname_length < 2)
                {
                    // 아무것도 입력하지 않았으면 포커스 줘서 입력하라고 유도
                    Toast.makeText(GetUserInformationActivity.this, "닉네임은 2글자 이상 입력해 주세요", Toast.LENGTH_SHORT).show();
                    nickname_edittext.requestFocus();
                    break;
                }

                if (age > 115)   // 한국 최고령자 나이 이상 입력 시
                {
                    Toast.makeText(GetUserInformationActivity.this, "정확한 나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (gender.equals("선택 안함"))
                {
                    Toast.makeText(GetUserInformationActivity.this, "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (user_area == null)
                {
                    Toast.makeText(GetUserInformationActivity.this, "지역을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }

                nickname = nickname_edittext.getText().toString();
                Log.e(TAG, "닉넴 : " + nickname + ", 나이 : " + age + "살, 성별 : " + gender + ", 지역 : " + user_area);

                /* 키워드를 선택하기 위해 MainTabLayoutActivity가 아닌 ChoiceKeywordActivity로 이동되게 처리
                 * MainTabLayoutActivity는 ChoiceKeywordActivity에서 필요한 작업을 다 마치면 이동하게 한다
                 * 액티비티 이동은 registerUserInfo()에서 수행한다 */
                // 나이, 성별, 지역, 닉네임을 쉐어드, 서버에 저장한 뒤 액티비티 이동
                editor.putString("user_age", String.valueOf(age));
                editor.putString("user_gender", gender);
                editor.putString("user_area", user_area);
                editor.putString("user_nickname", nickname);
                editor.apply();
                // 기본 정보가 다 입력되고 쉐어드에도 값이 저장되면 입력한 기본 정보들을 서버로 보내 저장한다
                registerUserInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* 사용자 정보를 서버에 저장하는 메서드 */
    void registerUserInfo()
    {
        server_token = app_pref.getString("token", "");
        Log.e(TAG, "server_token = " + server_token);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        encode("입력된 사용자 정보 저장");
        String session = app_pref.getString("sessionId", "");
        Call<String> call = apiInterface.registerUserInfo(session, encode_str, server_token, nickname_edittext.getText().toString(), age_edittext.getText().toString(), gender, user_area);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "response = " + result);
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

    /* 사용자 정보를 서버에 저장한 후 날아오는 JSON 값들을 파싱하는 메서드 */
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
        // 로그로 확인한 후 액티비티 이동
        Intent intent = new Intent(GetUserInformationActivity.this, ChoiceKeywordActivity.class);
        intent.putExtra("age_group", age_group);
        intent.putExtra("user_gender", user_gender);
        intent.putExtra("interest", interest);
        startActivity(intent);
    }

    /* editText 바깥을 터치했을 때 키보드를 내리는 메서드 */
    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void init()
    {
        nickname_edittext = findViewById(R.id.nickname_edittext);
        age_edittext = findViewById(R.id.age_edittext);
        area_picker = findViewById(R.id.area_picker);
        radioGroup = findViewById(R.id.gender_radiogroup);
        male_radiobutton = findViewById(R.id.male_radiobutton);
        female_radiobutton = findViewById(R.id.female_radiobutton);
    }

    // 로그인 후 개인정보 수정 눌렀을 때 저장돼 있는 지역명에 따라 NumberPicker 값을 set하는 메서드
    private void setUserPickerValue(NumberPicker numberPicker, String area)
    {
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(first_area.length - 1);
        numberPicker.setWrapSelectorWheel(false);
        for (int i = 0; i < first_area.length; i++)
        {
            switch (area)
            {
            case "서울" :
                numberPicker.setValue(1);
                break;

            case "부산" :
                numberPicker.setValue(2);
                break;

            case "대구" :
                numberPicker.setValue(3);
                break;

            case "인천" :
                numberPicker.setValue(4);
                break;

            case "광주" :
                numberPicker.setValue(5);
                break;

            case "대전" :
                numberPicker.setValue(6);
                break;

            case "울산" :
                numberPicker.setValue(7);
                break;

            case "세종" :
                numberPicker.setValue(8);
                break;

            case "경기도 가평군" :
                numberPicker.setValue(10);
                break;

            case "경기도 고양시" :
                numberPicker.setValue(11);
                break;

            case "경기도 과천시" :
                numberPicker.setValue(12);
                break;

            case "경기도 광명시" :
                numberPicker.setValue(13);
                break;

            case "경기도 광주시" :
                numberPicker.setValue(14);
                break;

            case "경기도 구리시" :
                numberPicker.setValue(15);
                break;

            case "경기도 군포시" :
                numberPicker.setValue(16);
                break;

            case "경기도 남양주시" :
                numberPicker.setValue(17);
                break;

            case "경기도 동두천시" :
                numberPicker.setValue(18);
                break;

            case "경기도 부천시" :
                numberPicker.setValue(19);
                break;

            case "경기도 성남시" :
                numberPicker.setValue(20);
                break;

            case "경기도 수원시" :
                numberPicker.setValue(21);
                break;

            case "경기도 시흥시" :
                numberPicker.setValue(22);
                break;

            case "경기도 안산시" :
                numberPicker.setValue(23);
                break;

            case "경기도 안성시" :
                numberPicker.setValue(24);
                break;

            case "경기도 안양시" :
                numberPicker.setValue(25);
                break;

            case "경기도 양주시" :
                numberPicker.setValue(26);
                break;

            case "경기도 양평군" :
                numberPicker.setValue(27);
                break;

            case "경기도 여주시" :
                numberPicker.setValue(28);
                break;

            case "경기도 연천군" :
                numberPicker.setValue(29);
                break;

            case "경기도 오산시" :
                numberPicker.setValue(30);
                break;

            case "경기도 용인시" :
                numberPicker.setValue(31);
                break;

            case "경기도 의왕시" :
                numberPicker.setValue(32);
                break;

            case "경기도 의정부시" :
                numberPicker.setValue(33);
                break;

            case "경기도 이천시" :
                numberPicker.setValue(34);
                break;

            case "경기도 파주시" :
                numberPicker.setValue(35);
                break;

            case "경기도 평택시" :
                numberPicker.setValue(36);
                break;

            case "경기도 포천시" :
                numberPicker.setValue(37);
                break;

            case "경기도 하남시" :
                numberPicker.setValue(38);
                break;

            case "경기도 화성시" :
                numberPicker.setValue(39);
                break;

            default:
                break;
            }
        }
    }

    /* NumberPicker에서 지역을 선택하면 서버에 보내는 형태로 지역명을 변경해 변수에 저장하는 메서드 */
    void getUserArea(NumberPicker picker)
    {
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                switch (newVal)
                {
                    case 0:    // 시 · 도 선택
                        Log.e(TAG, "0번 값");
                        user_area = "값 없음";
                        break;

                    case 1:    // 서울특별시
                        Log.e(TAG, "1번 값");
                        user_area = "서울특별시";
                        break;

                    case 2:    // 부산광역시
                        Log.e(TAG, "2번 값");
                        user_area = "부산광역시";
                        break;

                    case 3:    // 대구광역시
                        Log.e(TAG, "3번 값");
                        user_area = "대구광역시";
                        break;

                    case 4:    // 인천광역시
                        Log.e(TAG, "4번 값");
                        user_area = "인천광역시";
                        break;

                    case 5:    // 광주광역시
                        Log.e(TAG, "5번 값");
                        user_area = "광주광역시";
                        break;

                    case 6:    // 대전광역시
                        Log.e(TAG, "6번 값");
                        user_area = "대전광역시";
                        break;

                    case 7:    // 울산광역시
                        Log.e(TAG, "7번 값");
                        user_area = "울산광역시";
                        break;

                    case 8:    // 세종시
                        /* 고운동만 나온다 */
                        Log.e(TAG, "8번 값");
                        user_area = "세종시";
                        break;

                    case 9:    // 경기도 가평군
                        Log.e(TAG, "9번 값");
                        user_area = "경기도 가평군";
                        break;

                    case 10:    // 경기도 고양시
                        Log.e(TAG, "10번 값");
                        user_area = "경기도 고양시";
                        break;

                    case 11:    // 경기도 과천시
                        Log.e(TAG, "11번 값");
                        user_area = "경기도 과천시";
                        break;

                    case 12:    // 경기도 광명시
                        Log.e(TAG, "12번 값");
                        user_area = "경기도 광명시";
                        break;

                    case 13:    // 경기도 광주시
                        Log.e(TAG, "13번 값");
                        user_area = "경기도 광주시";
                        break;

                    case 14:    // 경기도 구리시
                        Log.e(TAG, "14번 값");
                        user_area = "경기도 구리시";
                        break;

                    case 15:    // 경기도 군포시
                        Log.e(TAG, "15번 값");
                        user_area = "경기도 군포시";
                        break;

                    case 16:    // 경기도 남양주시
                        Log.e(TAG, "16번 값");
                        user_area = "경기도 남양주시";
                        break;

                    case 17:    // 경기도 동두천시
                        Log.e(TAG, "17번 값");
                        user_area = "경기도 동두천시";
                        break;

                    case 18:    // 경기도 부천시
                        Log.e(TAG, "18번 값");
                        user_area = "경기도 부천시";
                        break;

                    case 19:    // 경기도 성남시
                        Log.e(TAG, "19번 값");
                        user_area = "경기도 성남시";
                        break;

                    case 20:    // 경기도 수원시
                        Log.e(TAG, "20번 값");
                        user_area = "경기도 수원시";
                        break;

                    case 21:    // 경기도 시흥시
                        Log.e(TAG, "21번 값");
                        user_area = "경기도 시흥시";
                        break;

                    case 22:    // 경기도 안산시
                        Log.e(TAG, "22번 값");
                        user_area = "경기도 안산시";
                        break;

                    case 23:    // 경기도 안성시
                        Log.e(TAG, "23번 값");
                        user_area = "경기도 안성시";
                        break;

                    case 24:    // 경기도 안양시
                        Log.e(TAG, "24번 값");
                        user_area = "경기도 안양시";
                        break;

                    case 25:    // 경기도 양주시
                        Log.e(TAG, "25번 값");
                        user_area = "경기도 양주시";
                        break;

                    case 26:    // 경기도 양평군
                        Log.e(TAG, "26번 값");
                        user_area = "경기도 양평군";
                        break;

                    case 27:    // 경기도 여주시
                        Log.e(TAG, "27번 값");
                        user_area = "경기도 여주시";
                        break;

                    case 28:    // 경기도 연천군
                        Log.e(TAG, "28번 값");
                        user_area = "경기도 연천군";
                        break;

                    case 29:    // 경기도 오산시
                        Log.e(TAG, "29번 값");
                        user_area = "경기도 오산시";
                        break;

                    case 30:    // 경기도 용인시
                        Log.e(TAG, "30번 값");
                        user_area = "경기도 용인시";
                        break;

                    case 31:    // 경기도 의왕시
                        Log.e(TAG, "31번 값");
                        user_area = "경기도 의왕시";
                        break;

                    case 32:    // 경기도 의정부시
                        Log.e(TAG, "32번 값");
                        user_area = "경기도 의정부시";
                        break;

                    case 33:    // 경기도 이천시
                        Log.e(TAG, "33번 값");
                        user_area = "경기도 이천시";
                        break;

                    case 34:    // 경기도 파주시
                        Log.e(TAG, "34번 값");
                        user_area = "경기도 파주시";
                        break;

                    case 35:    // 경기도 평택시
                        Log.e(TAG, "35번 값");
                        user_area = "경기도 평택시";
                        break;

                    case 36:    // 경기도 포천시
                        Log.e(TAG, "36번 값");
                        user_area = "경기도 포천시";
                        break;

                    case 37:    // 경기도 하남시
                        Log.e(TAG, "37번 값");
                        user_area = "경기도 하남시";
                        break;

                    case 38:    // 경기도 화성시
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
    public void onBackPressed()
    {
        int nicknames = nickname_edittext.getText().toString().length();
        int ages = age_edittext.getText().toString().length();
        if (nicknames > 0 || ages > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(GetUserInformationActivity.this);
            builder.setMessage("지금 나가시면 입력했던 정보들은 저장되지 않아요\n그래도 나가시겠어요?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
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
        else
        {
            finish();
        }
    }
}