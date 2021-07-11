package com.psj.welfare;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.orhanobut.logger.Logger;
import com.psj.welfare.activity.MainTabLayoutActivity;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.util.UnCatchTaskService;

/* 미리보기 첫 화면에서 "알아보기" 버튼 눌러 이동하는 나이, 성별, 지역 선택 화면 */
public class TutorialCategory extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private ImageButton BtnMinus, BtnPlus; //나이 선택 버튼(-, +)
    private Button BtnGenderMan, BtnGenderWoman; //남성 선택 버튼, 여성 선택 버튼
    private Button BtnCategory; //다음 페이지 가기 버튼
    private NumberPicker PickerHome; //지역 선택 picker

    private Point size; //디스플레이 전체 크기 담기 위한 변수

    private TextView TextAge; //나이대
    private String [] PickerString = {"서울","강원","광주","경기","경남","경북","대구","대전","부산","세종","울산","인천","전남","전북","제주","충남","충북"}; //NumberPicker에 넣을 값
    private String [] AgeArray = {"10대 미만","10대","20대","30대","40대","50대","60대 이상"}; //나이대 선택을 위한 값
    private int AgeArrayValue = 2 ; //나이대 배열 인덱스 값
    private String gender = null; //성별
    private String home = null; //지역
//    private String age = "20대"; //나이대를 담는 변수(기본값 20대)

    private SharedPreferences sharedPreferences;

    DBOpenHelper helper;
    String sqlite_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_category);
        Logger.d("TutorialCategory에서 UnCatchTaskService가 실행중인가? : " + isMyServiceRunning(UnCatchTaskService.class));

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        helper = new DBOpenHelper(this);
        helper.openDatabase();
        helper.create();

        // Room DB에서 토큰 가져와 변수에 대입
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        BtnMinus = findViewById(R.id.BtnMinus); //나이대 - 버튼
        BtnPlus = findViewById(R.id.BtnPlus); //나이대 - 버튼
        TextAge = findViewById(R.id.TextAge); //나이대 텍스트뷰

        BtnGenderMan = findViewById(R.id.BtnGenderMan); //남성 선택 버튼
        BtnGenderWoman = findViewById(R.id.BtnGenderWoman); //여성 선택 버튼

        PickerHome = findViewById(R.id.PickerHome); //지역 선택 picker
        PickerHome.setDisplayedValues(PickerString); //numberpicker를 string값으로 한다
        PickerHome.setMaxValue(PickerString.length - 1); //최대값
        PickerHome.setMinValue(0); //최소값
        PickerHome.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); //피커 선택 했을 때 소프트키 나오는 기능 막기

        BtnCategory = findViewById(R.id.BtnCategory); //다음 버튼

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //남성 선택 버튼
        BtnGenderMan.setOnClickListener(v -> {
            BtnGenderMan.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            BtnGenderMan.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //핑크색 글자
            BtnGenderWoman.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            BtnGenderWoman.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            gender = "남성";
        });

        //여성 선택 버튼
        BtnGenderWoman.setOnClickListener(v -> {
            BtnGenderWoman.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            BtnGenderWoman.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //핑크색 글자
            BtnGenderMan.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            BtnGenderMan.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            gender = "여성";
        });


        //나이대 -버튼
        BtnMinus.setOnClickListener( v->{
            if(AgeArrayValue != 0){ //나이대가 10대 미만이 아닐 경우
                AgeArrayValue -= 1; //나이대 배열 인덱스 값 -1
                TextAge.setText(AgeArray[AgeArrayValue]); //나이대 값 텍스트뷰에 입력
            }
        });

        //나이대 +버튼
        BtnPlus.setOnClickListener( v->{
            if(AgeArrayValue != AgeArray.length-1){ //나이대가 70대 이상이 아닐 경우
                AgeArrayValue += 1; //나이대 배열 인덱스 값 +1
                TextAge.setText(AgeArray[AgeArrayValue]); //나이대 값 텍스트뷰에 입력
            }
        });

        //다음 페이지 가기 버튼
        BtnCategory.setOnClickListener(v -> {


            if(gender == null){ //값을 모두 선택 하지 않았다면
                Toast.makeText(this, "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
            } else { //값을 모두 선택 했다면

                editor.putString("gender", gender);
                editor.putString("age_group", TextAge.getText().toString());
                editor.putString("user_area", PickerString[PickerHome.getValue()]);
                editor.apply();

                // 받은 값들을 서버로 보낸다

                new Thread(() -> { //Room은 메인 스레드에서 실행시키면 오류가 난다
                    //Room을 쓰기위해 데이터베이스 객체 만들기
                    AppDatabase database = Room.databaseBuilder(TutorialCategory.this, AppDatabase.class, "Firstcategory")
                            .fallbackToDestructiveMigration()
                            .build();

                    //DB에 쿼리를 던지기 위해 선언
                    CategoryDao categoryDao = database.getcategoryDao();
                    //임시 id값, 나이, 성별, 지역값을 넣어준다
                    CategoryData categoryData = new CategoryData("temid",TextAge.getText().toString(), gender, PickerString[PickerHome.getValue()]);

                    categoryDao.deleteall();
                    categoryDao.insert(categoryData);

                }).start();

                //다음 페이지로 이동
                Intent intent = new Intent(TutorialCategory.this, TutorialResult.class);
                startActivity(intent);
            }
        });

    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize(){
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        size = new Point();
        display.getRealSize(size); // or getSize(size)

        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        BtnCategory.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/15); //다음 버튼 크기 텍스트값
        BtnGenderMan.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/20); //남성 버튼 크기 텍스트값
        BtnGenderWoman.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/20); //여성 버튼 크기 텍스트값

    }

    /* 서비스가 실행중인지 확인해서 T/F를 리턴하는 메서드 */
    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder TutorialDialog = new AlertDialog.Builder(TutorialCategory.this);
        View dialogview = getLayoutInflater().inflate(R.layout.custom_tutorial_dialog,null); //다이얼로그의 xml뷰 담기
        Button BtbCancle = dialogview.findViewById(R.id.BtbCancle); //취소 버튼
        Button BtnBack = dialogview.findViewById(R.id.BtnBack); //뒤로 가기 버튼
        ConstraintLayout review_dialog_layout = dialogview.findViewById(R.id.review_dialog_layout);

        review_dialog_layout.getLayoutParams().width = (int) (size.x*0.9);
        review_dialog_layout.getLayoutParams().height = (int) (size.y*0.26);

        TutorialDialog.setView(dialogview); //alertdialog에 view 넣기
        final AlertDialog alertDialog = TutorialDialog.create(); //다이얼로그 객체로 만들기
        alertDialog.show(); //다이얼로그 보여주기

        //취소 버튼
        BtbCancle.setOnClickListener(v->{
            alertDialog.dismiss(); //다이얼로그 사라지기
        });

        //뒤로 가기 버튼
        BtnBack.setOnClickListener(v->{
            Intent intent = new Intent(TutorialCategory.this, MainTabLayoutActivity.class); //CategoryWelcome페이지로 가기

            //미리보기 했는지
            SharedPreferences shared = getSharedPreferences("welf_preview", 0);
            SharedPreferences.Editor editor = shared.edit();
            editor.putBoolean("being_preview", true); //미리보기 건너뛰기를 했거나 미리보기 화면에 들어갔다면
            editor.apply();

            startActivity(intent);
            Toast.makeText(TutorialCategory.this,"미리보기를 취소 했습니다",Toast.LENGTH_SHORT).show();
            alertDialog.dismiss(); //다이얼로그 사라지기
            finish();
        });
    }
}