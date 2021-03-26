package com.psj.welfare.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/* 리뷰 작성 화면 */
public class ReviewActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();

    private RatingBar review_rate_edit;
    private EditText review_content_edit;

    // 신청이 쉬웠나요 라디오그룹
    RadioGroup difficulty_radiogroup;
    RadioButton btn_easy, btn_difficult;
    // 혜택 만족도 라디오 그룹
    RadioGroup satisfaction_radiogroup;
    RadioButton btn_satisfied, btn_unsatisfied;

    // 라디오 그룹에서 선택한 값 담을 변수
    String difficulty_level, satisfaction;

    // 선택해서 가져온 리뷰 아이디
    String review_id = "";
    /* 0322) ReviewUpdateActivity로 이동하던 로직을 수정해서 추가한 변수 */
    // 아이템의 수정을 눌러서 들어왔을 경우, 리뷰 내용과 별점을 각각 담을 변수
    String content, star_count = "";
    int id;

    //@@ 리뷰 등록 버튼
    Button btnRegister;

    //@@ 페이지 타이틀(리뷰 작성/리뷰 수정)
    TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ReviewActivity.this);
        setContentView(R.layout.activity_review);

        satisfaction = "도움이 됐어요";
        difficulty_level = "쉬워요";

        Log.e(TAG, "인텐트로 받아온 welf_local : " + getIntent().getStringExtra("welf_local"));
        Log.e(TAG, "인텐트로 받아온 welf_name : " + getIntent().getStringExtra("welf_name"));
        init();

        if (getIntent().hasExtra("id"))
        {
            Intent intent = getIntent();
            review_id = intent.getStringExtra("id");
            Log.e(TAG, "'id' 인텐트에서 가져온 리뷰 게시글의 인덱스 값 : " + review_id);
        }
        if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
        {
            // 아이템의 '수정'을 눌러서 들어온 경우, 해당 리뷰 내용과 별점을 가져온다
            tv_title.setText("리뷰 수정");
            btnRegister.setText("수정하기");
            Intent intent = getIntent();
            content = intent.getStringExtra("content");
            star_count = intent.getStringExtra("star_count");
            difficulty_level = intent.getStringExtra("difficulty_level");
            satisfaction = intent.getStringExtra("satisfaction");
            // 별 카운트에 값이 있다면 그걸 상단의 별점바에 세팅한다
            if (star_count != null)
            {
                float count = Float.parseFloat(star_count);
                review_rate_edit.setRating(count);
            }
            // id는 서버에서 String 형태로 날아오고, 수정 후 서버로 보낼 땐 int로 보내야 하기 때문에 int로 캐스팅한다
            String before_id = intent.getStringExtra("id");
            if (before_id != null)
            {
                id = Integer.parseInt(before_id);
            }
            review_content_edit.setText(content);
            if (difficulty_level.equals("쉬워요"))
            {
                btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
            }
            else
            {
                btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
            }

            if (satisfaction.equals("도움이 됐어요"))
            {
                btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
            }
            else
            {
                btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
            }
        }
        Log.e(TAG, "받아온 id = " + review_id); //@@ 이거 review id임.

        /* 리뷰 작성 시 165자 제한 */
        review_content_edit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(165)
        });

        /* 별점 밑 혜택 신청하면서 느낀 점을 라디오버튼으로 선택하게 한다 */
        difficulty_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.easy_radiobutton:
                        Log.e(TAG, "쉬워요 클릭됨");
                        difficulty_level = "쉬워요";
                        btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                        break;

                    case R.id.hard_radiobutton:
                        Log.e(TAG, "어려워요 클림됨");
                        difficulty_level = "어려워요";
                        btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                        btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        break;

                    default:
                        break;
                }
            }
        });

        satisfaction_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.good_radiobutton:
                        satisfaction = "도움이 됐어요";
                        Log.e(TAG, "도움됐어요 클릭됨");
                        btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                        break;

                    case R.id.bad_radiobutton:
                        satisfaction = "도움이 안 됐어요";
                        Log.e(TAG, "도움 안됐어요 클릭됨");
                        btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.grey));
                        btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        break;

                    default:
                        break;
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
                {
                    Log.e(TAG, "수정하기 버튼 클릭");
                    if(review_content_edit.getText().toString().equals("")){
                        Toast.makeText(ReviewActivity.this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
                    }else{
                        updateReview();
                        finish();
                        setResult(RESULT_OK);
                    }

                }
                else
                {
                    Log.e(TAG, "등록하기 버튼 클릭");
                    if(review_content_edit.getText().toString().equals("")){
                        Toast.makeText(ReviewActivity.this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
                    }else{
                        uploadReview();
                        finish();
                    }

                }
            }
        });
    }

    private void init()
    {
        review_rate_edit = findViewById(R.id.review_rate_edit);
        review_content_edit = findViewById(R.id.review_content_edit);

        difficulty_radiogroup = findViewById(R.id.difficulty_radiogroup);
        satisfaction_radiogroup = findViewById(R.id.satisfaction_radiogroup);
        btn_easy = findViewById(R.id.easy_radiobutton);
        btn_difficult = findViewById(R.id.hard_radiobutton);
        btn_satisfied = findViewById(R.id.good_radiobutton);
        btn_unsatisfied = findViewById(R.id.bad_radiobutton);

        btnRegister = findViewById(R.id.btnRegister);

        tv_title = findViewById(R.id.tv_title);

        review_rate_edit.setRating(3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.review_done:
                /* 이미지 전송 메서드 호출 */
                if (difficulty_level == null || satisfaction == null)
                {
                    Toast.makeText(this, "느낀점을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                {
                    // 작성한 리뷰가 없으면 토스트를 띄워서 작성 유도
                    if (review_content_edit.getText().toString().equals(""))
                    {
                        Toast.makeText(this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "아무것도 안썼는데?");
                        break;
                    }
                    else
                    {
                        uploadReview();
                        finish();
                        break;
                    }
                }

            case android.R.id.home:
                int lengths = review_content_edit.getText().toString().length();
                if (lengths > 0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
                    builder.setMessage("지금 나가시면 입력하신 내용들은 저장되지 않아요\n그래도 나가시겠어요?")
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
                    return true;
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        int lengths = review_content_edit.getText().toString().length();
        if (lengths > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
            builder.setMessage("지금 나가시면 입력하신 내용들은 저장되지 않아요\n그래도 나가시겠어요?")
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

    /* 리뷰 이미지, 텍스트를 같이 서버에 업로드하는 메서드
     * 이미지가 없어도 텍스트만 올라갈 수 있어야 한다 */
    void uploadReview()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        Retrofit retrofit = ApiClient.getApiClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        int rate = (int) review_rate_edit.getRating();
        int id = Integer.parseInt(review_id);
        String star_count = String.valueOf(rate);
        Call<String> call = apiInterface.uploadReview(token, id, review_content_edit.getText().toString(), null, null,
                difficulty_level, satisfaction, star_count);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "리뷰 작성 성공 : " + response);
                    Toast.makeText(getApplicationContext(), "리뷰가 등록되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                }
                else
                {
                    Log.e(TAG, "리뷰 작성 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 작성 에러 : " + t.toString());
                Toast.makeText(getApplicationContext(), "에러 : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

//    // 리뷰 수정 메서드
//    void updateReview()
//    {
//        Log.e(TAG, "리뷰 수정 111");
//        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
//        Log.e(TAG, "리뷰 수정 222");
//        String token = sharedPreferences.getString("token", "");
//        Log.e(TAG, "리뷰 수정 333");
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        Log.e(TAG, "리뷰 수정 444");
//        Call<String> call = apiInterface.updateReview(token, id, content, null, null,
//                difficulty_level, satisfaction, star_count);
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                Log.e(TAG, "리뷰 수정 555");
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    Log.e(TAG, "리뷰 수정 666");
//                    String result = response.body();
//                    Log.e(TAG, "리뷰 수정 777");
//                    setResult(RESULT_OK);  //@@ 이게 맞나?
//                    Log.e(TAG, "수정 결과 = " + result);
//                    Log.e(TAG, "리뷰 수정 888");
//                    Toast.makeText(ReviewActivity.this, "리뷰가 수정되었습니다", Toast.LENGTH_SHORT).show();
//                }
//                else
//                {
//                    Log.e(TAG, "실패 : " + response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e(TAG, "에러 : " + t.getMessage());
//            }
//        });
//    }

    // 리뷰 수정 메서드
    void updateReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        int rate = (int) review_rate_edit.getRating();
        SharedPreferences sharedPreferences = getSharedPreferences("app_pref", 0);
        String star_count = String.valueOf(rate);
        String token = sharedPreferences.getString("token", "");
        Call<String> call = apiInterface.updateReview(token, id, review_content_edit.getText().toString(), null, null,
                difficulty_level, satisfaction, star_count);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "리뷰 수정 결과 = " + result);
                    Toast.makeText(ReviewActivity.this, "리뷰가 수정되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                }
                else
                {
                    Log.e(TAG, "리뷰 수정 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 수정 에러 : " + t.getMessage());
            }
        });
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}