package com.psj.welfare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomReviewDialog;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 리뷰 작성, 수정하는 화면 */
public class DetailReviewWrite extends AppCompatActivity
{

    private final String TAG = this.getClass().getSimpleName();

    ConstraintLayout empty_layout; //키보드 누르면 밀림현상 없애기 위해 임시적으로 만든 빈 레이아웃
    ConstraintLayout DetailTabTop; //제목, 뒤로가기 버튼이 들어가는 상단 레이아웃
    ImageButton back_btn; //뒤로 가기 버튼
    TextView BenefitTitle, score_textview, please_tab_textview, level_textview, satisfaction_textview, your_opinion_textview; //혜택명, 별점 주기 타이틀, 탭해서 별점주기, 과정 평가, 만족도 평가, 의견 남겨주세요
    RatingBar review_star; //별점
    RadioGroup difficulty_radiogroup, satisfaction_radiogroup; //신청 과정 평가 라디오 그룹, 만족도 평가 라디오 그룹
    RadioButton easy_radiobutton, hard_radiobutton, good_radiobutton, bad_radiobutton; //쉬워요 버튼, 어려워요 버튼, 도움 됐어요 버튼, 도움 안됐어요 버튼
    EditText review_content_edit; //의견 입력란
    Button btnRegister; //등록 버튼
    LinearLayout text_length_layout; //의견란 글자수 담을 레이아웃
    TextView text_length, text_length_textview; //의견란 현재 글자수, 의견란 최대 글자수

    private Point size; //디스플레이 전체 크기 담기 위한 변수

    boolean being_id; //혜택 id값이 존재 하는지
    String welf_id, welf_name, level_value, satisfaction_value; //혜택 id값, 혜택명, 난이도, 만족도

    boolean being_logout; //로그인 했는지 여부 확인하기
    String SessionId; //세션 값
    String token; //토큰 값

    String result_code;
    DBOpenHelper helper;
    String sqlite_token;

    int checkStatus = 0;  //어떤 액티비티에서 넘어왔는지 확인 (100 = '내가 작성한 리뷰 페이지' or '모든 리뷰 보기 페이지', 200 = '상세페이지')
    int written_review_id;

    String content_from_intent;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_review_write);
        setStatusBarGradiant(DetailReviewWrite.this); //상태 표시줄 색 바꾸기
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); //키보드가 올라가도 레이아웃이 변하지 않도록
        //android:windowSoftInputMode="adjustNothing" 매니페스트에서 이렇게 코드 입력한것과 동일한 기능

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

        // 인텐트로 받아온 리뷰가 있다면 그것의 길이를 setText()
        if (content_from_intent != null && !content_from_intent.equals(""))
        {
            text_length.setText(String.valueOf(content_from_intent.length()));
        }

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //이전 화면으로 가기
        back_btn.setOnClickListener(v ->
        {
            finish(); //현재 액티비티 종료
            //액티비티 스택을 추적하고싶을경우. 이런 경우엔 새액티비티를 시작할때마다 intent에 FLAG_ACTIVITY_REORDER_TO_FRONT 나 FLAG_ACTIVITY_PREVIOUS_IS_TOP 같은 플래그를 줄 수 있습니다.
        });


        //별점 갯수 변화 할 때 반응 하는 리스너
        review_star.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                //만약 별점을 1개 이하로 줄려고 하면 1개 값이 입력되도록 한다
                if (review_star.getRating() < 1.0f)
                {
                    review_star.setRating(1);
                }
            }
        });

        //NestedScrollView 안에서 edittext한테 스크롤 가능하게 하기
        review_content_edit.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                final int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_MOVE:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }
        });

        /* 리뷰 입력 시 특수문자, 이모티콘이 들어오지 못하게 한다 */
        review_content_edit.setFilters(new InputFilter[]{new InputFilter()
        {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
            {
                // 이모티콘, 특수문자 입력 방지하는 정규식
                // 둘 중 하나라도 입력되면 공백을 리턴한다
                /**
                 * ^ : 패턴의 시작을 알리는 문자
                 * [] : 문자의 집합 or 범위 나타냄, 두 문자 사이는 "-"로 범위를 나타낸다. 이 안에 있는 문자 중 하나라도 해당되면 정규식과 매치된다
                 * [] 내부 : 한글, 영어, 숫자만 입력할 수 있게 하고 천지인 키보드의 .(middle dot)도 쓸 수 있도록 한다
                 * $ : 문자열(패턴)의 종료를 알리는 문자
                 * -> 입력되는 문자열의 시작부터 끝까지 한글, 영어, 숫자를 제외한 문자가 들어오면 공백을 리턴해서 아무것도 입력되지 않게 한다
                 */
                Pattern pattern = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ \\s\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                // editText에서 사용하고 싶은 문자가 있으면 '[ ]' 대 괄호 안에 넣으면 된다(띄어쓰기를 사용하고 싶으면 띄어쓰기를 넣으면 된다), '\\s' 는 공백을 나타낸다
                // compile()안에 editText에 입력하고자 하는 문자or숫자 등등의 입력 방식을 써주면 된다
                // '-' 는 ~에서 ~까지라는 뜻 ex) a-z 소문자 a에서 소문자 z까지
                if (source.equals("") || pattern.matcher(source).matches())
                {
                    return source;
                }
                return "";
            }
        }, new InputFilter.LengthFilter(350)});  // 리뷰 입력은 350자까지만 된다


        //현재 글자수 세기 위함
        review_content_edit.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                //텍스트가 변할 때
                String input = review_content_edit.getText().toString();
                text_length.setText(String.valueOf(input.length()));
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        //쉬움 버튼 누름
        easy_radiobutton.setOnClickListener(v ->
        {
            easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            level_value = "쉬워요";
        });

        //어려움 버튼 누름
        hard_radiobutton.setOnClickListener(v ->
        {
            hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            level_value = "어려워요";
        });

        //도움 됐어요 버튼 누름
        good_radiobutton.setOnClickListener(v ->
        {
            good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            satisfaction_value = "도움 돼요";
        });

        //도움 안됐어요 버튼 누름
        bad_radiobutton.setOnClickListener(v ->
        {
            bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
            bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
            good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
            good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
            satisfaction_value = "도움 안 돼요";
        });

        //리뷰 등록 버튼 누름
        btnRegister.setOnClickListener(v ->
        {
            // 이 화면에 들어올 때 review_edit과 100 or 200을 키밸류로 받았으면 등록 버튼을 누를 시 리뷰 수정 메서드를 호출한다
            if (checkStatus == 100 || checkStatus == 200)
            {
                editReview();
            }

            // 그게 아니면 리뷰 작성 메서드를 호출한다
            else
            {
                if (level_value == null)
                { //난이도 평가를 안했다면
                    Toast.makeText(this, "신청 과정을 평가해 주세요", Toast.LENGTH_SHORT).show();
                }
                else if (satisfaction_value == null)
                { //만족도 평가를 안했다면
                    Toast.makeText(this, "만족도를 평가해 주세요", Toast.LENGTH_SHORT).show();
                }
                else if (review_content_edit.getText().toString().length() == 0)
                { //의견란이 공백이라면
                    Toast.makeText(this, "의견을 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //의견 서버에 업로드 하기
                    UploadOpinion();
                }
            }
        });
    }


    // 리뷰 수정 메서드
    private void editReview()
    {
        int star_count_int = (int) review_star.getRating(); //rating 값을 int 값으로
//        String star_count = String.valueOf(review_star.getRating());
        String send_id = String.valueOf(written_review_id);
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
//        Log.e(TAG, "star_count_int : " + star_count_int + ", level_value : " + level_value + ", 만족도 : " + satisfaction_value + ", 리뷰의 idx : " + send_id +
//                ", 입력한 내용 : " + review_content_edit.getText().toString() + ", 토큰 : " + sqlite_token);

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", send_id);
            jsonObject.put("content", review_content_edit.getText().toString());
            jsonObject.put("star_count", String.valueOf(star_count_int));
            jsonObject.put("difficulty_level", level_value);
            jsonObject.put("satisfaction", satisfaction_value);
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
            if(checkStatus == 100){
                finish();
            } else if (checkStatus == 200){
                goto_review();
            }
//
//
//                Log.e(TAG,"----------------");
//                Log.e(TAG,"welf_id : " + welf_id);
//                Intent intent = new Intent(DetailReviewWrite.this, DetailTabLayoutActivity.class);
////                STACK 정리, 기존의 상세보기 페이지가 stack에 맨위에 있으면 기존 액티비티는 종료하고 새로운 액티비티를 띄운다
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra("being_id", true);
//                intent.putExtra("review_write", true);
//                intent.putExtra("welf_id", welf_id);
//                finish();
//                startActivity(intent);
//            }
        }

    }


    //리뷰 등록 하기
    private void UploadOpinion()
    {
        int star_count_int = (int) review_star.getRating();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("welf_id", welf_id);
            jsonObject.put("content", review_content_edit.getText().toString());
            jsonObject.put("star_count", String.valueOf(star_count_int));
            jsonObject.put("difficulty_level", level_value);
            jsonObject.put("satisfaction", satisfaction_value);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.reviewWrite(token, jsonObject.toString());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "리뷰 작성 결과 : " + result);
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
//                        Log.e("test", jsonObject.toString());
                        Intent intent = new Intent(DetailReviewWrite.this, DetailTabLayoutActivity.class);
                        //STACK 정리, 기존의 상세보기 페이지가 stack에 맨위에 있으면 기존 액티비티는 종료하고 새로운 액티비티를 띄운다
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("being_id", true);
                        intent.putExtra("review_write", true);
                        intent.putExtra("welf_id", welf_id);
                        finish();
                        startActivity(intent);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.e(TAG, "리뷰 작성 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 작성 에러 : " + t.getMessage());
            }
        });

    }


    //인텐트로 받아온 welf_id값
    private void being_intent()
    {
        Intent intent = getIntent();
        if (getIntent().hasExtra("review_edit"))
        {
            checkStatus = intent.getIntExtra("review_edit", -1);
            written_review_id = intent.getIntExtra("review_id", -1); //혜택id



            float Star_count = intent.getFloatExtra("Star_count", -1);
            String satisfaction = intent.getStringExtra("satisfaction");
            String difficulty_level = intent.getStringExtra("difficulty_level");
            content_from_intent = intent.getStringExtra("content");
            Log.e(TAG, "being_intent에서 받은 content_from_intent : " + content_from_intent);

            welf_id = intent.getStringExtra("welf_id");
            welf_name = intent.getStringExtra("welf_name");

            //만족도
            if (satisfaction.equals("도움 돼요"))
            {
                good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
                good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
                bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
                bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
                satisfaction_value = "도움 돼요";
            }
            else if (satisfaction.equals("도움 안 돼요"))
            {
                bad_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
                bad_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
                good_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
                good_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
                satisfaction_value = "도움 안 돼요";
            }

            //난이도
            if (difficulty_level.equals("쉬워요"))
            {
                easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
                easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
                hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
                hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
                level_value = "쉬워요";
            }
            else if (difficulty_level.equals("어려워요"))
            {
                hard_radiobutton.setBackgroundResource(R.drawable.radius_smallround_pink_border); //핑크색 테두리 버튼
                hard_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color_text_pink)); //핑크색 글자
                easy_radiobutton.setBackgroundResource(R.drawable.radius_smallround_lightgray_border); //그레이 테두리 버튼
                easy_radiobutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //그레이색 글자
                level_value = "어려워요";
            }


            review_star.setRating(Star_count); //별점
            review_content_edit.setText(content_from_intent); //난이도

            BenefitTitle.setText(welf_name);
        }
        else
        {
            being_id = intent.getBooleanExtra("being_id", false); //혜택 데이터가 있는지
            welf_id = intent.getStringExtra("welf_id"); //혜택id
            welf_name = intent.getStringExtra("welf_name"); //혜택명
            BenefitTitle.setText(welf_name);
        }
        /* if (being_id) 부분 때문에 혜택이름과 id를 받아오지 못해서 삭제했습니다 */
    }



    //로그인 했는지 여부 확인
    private void being_loging()
    {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", false); //로그인 했는지 여부 확인하기

        if (!being_logout)
        { //로그인 했다면
            SessionId = app_pref.getString("sessionId", ""); //세션값 받아오기
            token = app_pref.getString("token", ""); //토큰값 받아오기
        }
    }


    //뒤로가기 눌렀을 경우 리뷰 상세보기 화면으로 가기
    private void goto_review(){
        Intent intent = new Intent(DetailReviewWrite.this, DetailTabLayoutActivity.class);
        //STACK 정리, 기존의 상세보기 페이지가 stack에 맨위에 있으면 기존 액티비티는 종료하고 새로운 액티비티를 띄운다
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("welf_name",welf_name);
        intent.putExtra("welf_id",welf_id);
        intent.putExtra("being_id",true);
        intent.putExtra("review_write",true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        CustomReviewDialog dialog = new CustomReviewDialog(this);
        dialog.showDialog();
    }

    //자바 변수와 xml 변수 연결
    private void init()
    {
        empty_layout = findViewById(R.id.empty_layout); //키보드 누르면 밀림현상 없애기 위해 임시적으로 만든 빈 레이아웃
        DetailTabTop = findViewById(R.id.DetailTabTop); //제목, 뒤로가기 버튼이 들어가는 상단 레이아웃
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

        //별점 기본값 설정
        review_star.setRating(3);
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void SetSize()
    {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        size = new Point();
        display.getRealSize(size); // or getSize(size)

        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        empty_layout.getLayoutParams().height = (int) (size.y * 0.14); //키보드 누르면 밀림현상 없애기 위해 임시적으로 만든 빈 레이아웃
        DetailTabTop.getLayoutParams().height = (int) (size.y * 0.205); //제목, 뒤로가기 버튼이 들어가는 상단 레이아웃

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
//        review_content_edit.getLayoutParams().height = (int) (size.y * 0.2); //의견란
        review_content_edit.getLayoutParams().width = (int) (size.x * 0.835); //의견란
        review_content_edit.setPadding(size.y / 70, size.y / 70, size.y / 70, size.y / 70); //의견란 패딩값 적용
        review_content_edit.setMaxHeight((int) (size.y * 0.14));


        text_length_layout.getLayoutParams().height = size.y / 24; //의견란 글자수 레이아웃 크기 변경
        text_length_layout.getLayoutParams().width = size.x / 6 * 5; //의견란 글자수 레이아웃 크기 변경
        text_length_layout.setPadding(0, size.y / 150, 0, size.y / 90); //의견란 글자수 레이아웃 패딩값 적용

        text_length.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 28); //의견란 현재 글자수
        text_length_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 28); //의견란 최대 글자수

        btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 20); //등록 버튼 텍스트 크기
        btnRegister.getLayoutParams().height = size.y / 16; //등록 버튼 크기 변경
        btnRegister.getLayoutParams().width = (int) (size.x * 0.835); //등록 버튼 크기 변경
        btnRegister.setPadding(0, size.y / 90, 0, size.y / 80); //등록 버튼패딩값 적용

    }

    //상단 상태표시줄 화면 ui에 맞게 그라데이션 넣기
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.actionbar_gradient_end);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }
}