package com.psj.welfare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailReviewWrite extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    ImageButton back_btn; //뒤로 가기 버튼
    TextView BenefitTitle, score_textview, please_tab_textview, level_textview, satisfaction_textview, your_opinion_textview; //혜택명, 별점 주기 타이틀, 탭해서 별점주기, 과정 평가, 만족도 평가, 의견 남겨주세요
    RatingBar review_star; //별점
    RadioGroup difficulty_radiogroup, satisfaction_radiogroup; //신청 과정 평가 라디오 그룹, 만족도 평가 라디오 그룹
    RadioButton easy_radiobutton, hard_radiobutton, good_radiobutton, bad_radiobutton; //쉬워요 버튼, 어려워요 버튼, 도움 됐어요 버튼, 도움 안됐어요 버튼
    EditText review_content_edit; //의견 입력란
    Button btnRegister; //등록 버튼
    LinearLayout text_length_layout; //의견란 글자수 담을 레이아웃
    TextView text_length, text_length_textview; //의견란 현재 글자수, 의견란 최대 글자수

    boolean being_id; //혜택 id값이 존재 하는지
    String welf_id, welf_name, level_value, satisfaction_value; //혜택 id값, 혜택명, 난이도, 만족도

    boolean being_logout; //로그인 했는지 여부 확인하기
    String SessionId; //세션 값
    String token; //토큰 값

    String result_code;
    DBOpenHelper helper;
    String sqlite_token;

    int checkStatus = 0;
    int written_review_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_review_write);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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

        //로그인 했는지 여부 확인
        being_loging();

        //자바 변수와 xml 변수 연결
        init();

        //인텐트로 받아온 welf_id값
        being_intent();

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //이전 화면으로 가기
        back_btn.setOnClickListener(v -> {
            finish(); //현재 액티비티 종료
            //액티비티 스택을 추적하고싶을경우. 이런 경우엔 새액티비티를 시작할때마다 intent에 FLAG_ACTIVITY_REORDER_TO_FRONT 나 FLAG_ACTIVITY_PREVIOUS_IS_TOP 같은 플래그를 줄 수 있습니다.
        });

        //별점 기본값 설정
        review_star.setRating(3);

        //별점 갯수 변화 할 때 반응 하는 리스너
        review_star.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //만약 별점을 1개 이하로 줄려고 하면 1개 값이 입력되도록 한다
                if (review_star.getRating() < 1.0f) {
                    review_star.setRating(1);
                }
            }
        });


        //입력 글자 수 제한
        review_content_edit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(350)
        });

        //현재 글자수 세기 위함
        review_content_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //텍스트가 변할 때
                String input = review_content_edit.getText().toString();
                text_length.setText(String.valueOf(input.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //쉬움 버튼 누름
        easy_radiobutton.setOnClickListener(v -> {
            easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            level_value = "쉬워요";
        });

        //어려움 버튼 누름
        hard_radiobutton.setOnClickListener(v -> {
            hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            level_value = "어려워요";
        });

        //도움 됐어요 버튼 누름
        good_radiobutton.setOnClickListener(v -> {
            good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            satisfaction_value = "도움 돼요";
        });

        //도움 안됐어요 버튼 누름
        bad_radiobutton.setOnClickListener(v -> {
            bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            satisfaction_value = "도움 안 돼요";
        });

        //리뷰 등록 버튼 누름
        btnRegister.setOnClickListener(v -> {
            if (checkStatus == 100)
            {
                editReview();
            }
            else
            {
                if (level_value.equals("")) { //난이도 평가를 안했다면
                    Toast.makeText(this, "신청 과정을 평가해 주세요", Toast.LENGTH_SHORT).show();
                } else if (satisfaction_value.equals("")) { //만족도 평가를 안했다면
                    Toast.makeText(this, "만족도를 평가해 주세요", Toast.LENGTH_SHORT).show();
                } else if (review_content_edit.getText().toString().length() == 0) { //의견란이 공백이라면
                    Toast.makeText(this, "의견을 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    //의견 서버에 업로드 하기
                    UploadOpinion();
                }
            }
        });
    }   // onCreate() end

    // 리뷰 수정 메서드
    private void editReview()
    {
        String star_count = String.valueOf(review_star.getRating());
        String send_id = String.valueOf(written_review_id);
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Log.e(TAG, "star_count : " + star_count + ", level_value : " + level_value + ", 만족도 : " + satisfaction_value + ", 리뷰의 idx : " + send_id +
                ", 입력한 내용 : " + review_content_edit.getText().toString() + ", 토큰 : " + sqlite_token);
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", send_id);
            jsonObject.put("content", review_content_edit.getText().toString());
            jsonObject.put("star_count", star_count);
            jsonObject.put("difficulty_level", level_value);
            jsonObject.put("satisfaction", satisfaction_value);

            Log.e(TAG, "수정 api로 보낼 JSON 만들어진 것 테스트 : " + jsonObject.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Call<String> call = apiInterface.editReview(token, jsonObject.toString());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    try
                    {
                        JSONObject inner_json = new JSONObject(response.body());
                        Log.e(TAG, "리뷰 수정 성공 : " + inner_json.toString());
                        editReviewResultParsing(inner_json.toString());
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
//                    String result = response.body();
//                    editReviewResultParsing(result);
//                    Log.e(TAG, "리뷰 수정 성공 : " + result);
                }
                else
                {
                    Log.e(TAG, "리뷰 수정 에러 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 수정 에러 : " + t.getMessage());
            }
        });
    }

    // 리뷰 수정 후 서버에서 넘어온 결과값 파싱
    private void editReviewResultParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            result_code = jsonObject.getString("statusCode");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 리뷰 수정 성공했으면 토스트 띄우고 화면 종료
        if (result_code.equals("200"))
        {
            Toast.makeText(this, "리뷰 수정이 완료됐습니다", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    //로그인 했는지 여부 확인
    private void being_loging() {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", false); //로그인 했는지 여부 확인하기

        if (!being_logout) { //로그인 했다면
            SessionId = app_pref.getString("sessionId", ""); //세션값 받아오기
            token = app_pref.getString("token", ""); //토큰값 받아오기
        }
    }

    //의견 서버에 업로드 하기
    private void UploadOpinion() {
        int star_count_int = (int)review_star.getRating();
        Log.e("star_count_int",String.valueOf(star_count_int));
        //리뷰 json값으로 만들기
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("welf_id", welf_id);
            jsonObject.put("content", review_content_edit.getText().toString());
            jsonObject.put("star_count", String.valueOf(star_count_int));
            jsonObject.put("difficulty_level", level_value);
            jsonObject.put("satisfaction", satisfaction_value);

            Log.e("test",jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String URL = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/"; //연결하고자 하는 서버의 url, 반드시 /로 끝나야 함
        ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient(URL).create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        Call<String> call = apiInterfaceTest.ReviewWrite(token,jsonObject.toString()); //인터페이스에서 사용할 메소드 선언
        call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback

                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    Log.e("test",jsonObject.toString());
                    Intent intent = new Intent(DetailReviewWrite.this,DetailTabLayoutActivity.class);
                    //STACK 정리, 기존의 상세보기 페이지가 stack에 맨위에 있으면 기존 액티비티는 종료하고 새로운 액티비티를 띄운다
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("being_id",true);
                    intent.putExtra("review_write",true);
                    intent.putExtra("welf_id",welf_id);
                    finish();
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    //자바 변수와 xml 변수 연결
    private void init() {
        back_btn = findViewById(R.id.back_btn); //뒤로 가기 버튼
        BenefitTitle = findViewById(R.id.BenefitTitle); //혜택명
        score_textview = findViewById(R.id.score_textview); //별점 주기 타이틀
        please_tab_textview = findViewById(R.id.please_tab_textview); //탭해서 별점주기
        level_textview = findViewById(R.id.level_textview); //과정 평가
        satisfaction_textview = findViewById(R.id.satisfaction_textview); //만족도 평가
        your_opinion_textview = findViewById(R.id.your_opinion_textview); //의견 남겨주세요

        review_star = findViewById(R.id.review_star); //별점
        difficulty_radiogroup = findViewById(R.id.difficulty_radiogroup); //신청 과정 평가 라디오 그룹
        satisfaction_radiogroup = findViewById(R.id.satisfaction_radiogroup); //만족도 평가 라디오 그룹
        easy_radiobutton = findViewById(R.id.easy_radiobutton); //쉬워요 버튼
        hard_radiobutton = findViewById(R.id.hard_radiobutton); //어려워요 버튼
        good_radiobutton = findViewById(R.id.good_radiobutton); //도움 됐어요 버튼
        bad_radiobutton = findViewById(R.id.bad_radiobutton); //도움 안됐어요

        review_content_edit = findViewById(R.id.review_content_edit); //의견 입력란
        btnRegister = findViewById(R.id.btnRegister); //등록 버튼

        text_length_layout = findViewById(R.id.text_length_layout); //의견란 글자수 담을 레이아웃
        text_length = findViewById(R.id.text_length); //의견란 현재 글자수
        text_length_textview = findViewById(R.id.text_length_textview); //의견란 최대 글자수
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void SetSize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)

        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        BenefitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명

        score_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //별점 타이틀
        score_textview.setPadding(0, size.y / 100, 0, size.y / 130); //타이틀 패딩값 적용

        please_tab_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //탭해서 별점주기

        level_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //과정 평가
        level_textview.setPadding(0, size.y / 75, 0, size.y / 130); //과정 평가 패딩값 적용
        satisfaction_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //만족도 평가
        satisfaction_textview.setPadding(0, size.y / 75, 0, 0); //만족도 패딩값 적용
        your_opinion_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //의견 남겨주세요
        your_opinion_textview.setPadding(0, size.y / 75, 0, size.y / 100); //의견란 패딩값 적용

//        ViewGroup.LayoutParams params_star = review_star.getLayoutParams();
//        params_star.width = size.x/7*6; params_star.height = size.y/2;
//        review_star.setLayoutParams(params_star);

        ViewGroup.LayoutParams params_radiogroup = difficulty_radiogroup.getLayoutParams(); //난이도 라디오 버튼 그룹
        params_radiogroup.height = size.y / 12;
        difficulty_radiogroup.setLayoutParams(params_radiogroup);

        ViewGroup.LayoutParams params_radiogroup2 = satisfaction_radiogroup.getLayoutParams(); //만족도 라디오 버튼 그룹
        params_radiogroup2.height = size.y / 12;
        satisfaction_radiogroup.setLayoutParams(params_radiogroup2);

        easy_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //쉬워요 버튼 텍스트 크기
        easy_radiobutton.getLayoutParams().height = size.y / 18; //쉬워요 버튼 크기 변경
        easy_radiobutton.getLayoutParams().width = size.x / 6; //쉬워요 버튼 크기 변경

        hard_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //어려워요 버튼 텍스트 크기
        hard_radiobutton.getLayoutParams().height = size.y / 18; //어려워요 버튼 크기 변경
        hard_radiobutton.getLayoutParams().width = size.x / 6; //어려워요 버튼 크기 변경

        good_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 돼요 버튼 텍스트 크기
        good_radiobutton.getLayoutParams().height = size.y / 18; //도움 돼요 버튼 크기 변경
        good_radiobutton.getLayoutParams().width = size.x / 6; //도움 돼요 버튼 크기 변경

        bad_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 안돼요 버튼 텍스트 크기
        bad_radiobutton.getLayoutParams().height = size.y / 18; //도움 안돼요 버튼 크기 변경
        bad_radiobutton.getLayoutParams().width = size.x / 6; //도움 안돼요 버튼 크기 변경

        review_content_edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24);
        review_content_edit.getLayoutParams().height = size.x / 3 * 2; //의견란
        review_content_edit.getLayoutParams().width = size.x / 6 * 5; //의견란
        review_content_edit.setPadding(size.y / 70, size.y / 70, size.y / 70, size.y / 70); //의견란 패딩값 적용

        text_length_layout.getLayoutParams().height = size.y / 24; //의견란 글자수 레이아웃 크기 변경
        text_length_layout.getLayoutParams().width = size.x / 6 * 5; //의견란 글자수 레이아웃 크기 변경
        text_length_layout.setPadding(0, size.y / 150, 0, size.y / 90); //의견란 글자수 레이아웃 패딩값 적용

        text_length.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 현재 글자수
        text_length_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 최대 글자수

        btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 20); //등록 버튼 텍스트 크기
        btnRegister.getLayoutParams().height = size.y / 16; //등록 버튼 크기 변경
        btnRegister.getLayoutParams().width = size.x / 6 * 5; //등록 버튼 크기 변경
        btnRegister.setPadding(0, size.y / 90, 0, size.y / 80); //등록 버튼패딩값 적용

    }

    //인텐트로 받아온 welf_id값
    private void being_intent() {
        Intent intent = getIntent();
        if (getIntent().hasExtra("review_edit"))
        {
            checkStatus = intent.getIntExtra("review_edit", -1);
            written_review_id = intent.getIntExtra("review_id", -1); //혜택id
            String welf_name = intent.getStringExtra("welf_name");
            BenefitTitle.setText(welf_name);
            Log.e(TAG, "checkStatus : " + checkStatus + ", 내가 작성한 리뷰 id : " + written_review_id + ", 내가 작성한 리뷰의 혜택명 : " + welf_name);
        }
        else
        {
            being_id = intent.getBooleanExtra("being_id", false); //혜택 데이터가 있는지
            welf_id = intent.getStringExtra("welf_id"); //혜택id
            welf_name = intent.getStringExtra("welf_name"); //혜택명
            BenefitTitle.setText(welf_name);
            Log.e(TAG, "checkStatus = " + checkStatus + ", welf_id : " + welf_id);
        }
        /* if (being_id) 부분 때문에 혜택이름과 id를 받아오지 못해서 삭제했습니다 */
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        AlertDialog.Builder reviewDialog = new AlertDialog.Builder(DetailReviewWrite.this);
        View dialogview = getLayoutInflater().inflate(R.layout.custom_tutorial_dialog,null); //다이얼로그의 xml뷰 담기
        Button BtbCancle = dialogview.findViewById(R.id.BtbCancle); //취소 버튼
        Button BtnBack = dialogview.findViewById(R.id.BtnBack); //뒤로 가기 버튼
        TextView delete_dialog_firsttext = dialogview.findViewById(R.id.delete_dialog_firsttext);

        delete_dialog_firsttext.setText("이 화면에서 벗어날 경우\n리뷰 작성이 취소됩니다");

        reviewDialog.setView(dialogview); //alertdialog에 view 넣기
        final AlertDialog alertDialog = reviewDialog.create(); //다이얼로그 객체로 만들기
        alertDialog.show(); //다이얼로그 보여주기

        //취소 버튼
        BtbCancle.setOnClickListener(v->{
            alertDialog.dismiss(); //다이얼로그 사라지기
        });

        //뒤로 가기 버튼
        BtnBack.setOnClickListener(v->{
            super.onBackPressed();
            Toast.makeText(DetailReviewWrite.this,"리뷰 작성이 취소 되었습니다",Toast.LENGTH_SHORT).show();
            alertDialog.dismiss(); //다이얼로그 사라지기
            finish();
        });
    }
}