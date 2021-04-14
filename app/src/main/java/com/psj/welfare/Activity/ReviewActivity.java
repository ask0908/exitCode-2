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

public class ReviewActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();

    private RatingBar review_rate_edit;
    private EditText review_content_edit;

    RadioGroup difficulty_radiogroup;
    RadioButton btn_easy, btn_difficult;

    RadioGroup satisfaction_radiogroup;
    RadioButton btn_satisfied, btn_unsatisfied;

    String difficulty_level, satisfaction;

    String review_id = "";
    String content, star_count = "";
    int id;

    Button btnRegister;

    TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ReviewActivity.this);
        setContentView(R.layout.activity_review);

        satisfaction = "도움이 됐어요";
        difficulty_level = "쉬워요";

        init();

        if (getIntent().hasExtra("id"))
        {
            Intent intent = getIntent();
            review_id = intent.getStringExtra("id");
        }
        if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
        {
            tv_title.setText("리뷰 수정");
            btnRegister.setText("수정하기");
            Intent intent = getIntent();
            content = intent.getStringExtra("content");
            star_count = intent.getStringExtra("star_count");
            difficulty_level = intent.getStringExtra("difficulty_level");
            satisfaction = intent.getStringExtra("satisfaction");
            if (star_count != null)
            {
                float count = Float.parseFloat(star_count);
                review_rate_edit.setRating(count);
            }
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
                btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
            }
            else
            {
                btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
                btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
            }

            if (satisfaction.equals("도움이 됐어요"))
            {
                btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
            }
            else
            {
                btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
                btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
            }
        }

        review_content_edit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(165)
        });

        difficulty_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.easy_radiobutton:
                        difficulty_level = "쉬워요";
                        btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        btn_difficult.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_difficult.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
                        break;

                    case R.id.hard_radiobutton:
                        difficulty_level = "어려워요";
                        btn_easy.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_easy.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
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
                        btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
                        break;

                    case R.id.bad_radiobutton:
                        btn_satisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_grey_border));
                        btn_satisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.gray));
                        btn_unsatisfied.setBackground(ContextCompat.getDrawable(ReviewActivity.this, R.drawable.radius_pink_border));
                        btn_unsatisfied.setTextColor(ContextCompat.getColor(ReviewActivity.this, R.color.colorPink));
                        break;

                    default:
                        break;
                }
            }
        });

        btnRegister.setOnClickListener(view ->
        {
            if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
            {
                if (review_content_edit.getText().toString().equals(""))
                {
                    Toast.makeText(ReviewActivity.this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    updateReview();
                    finish();
                    setResult(RESULT_OK);
                }
            }
            else
            {
                if (review_content_edit.getText().toString().equals(""))
                {
                    Toast.makeText(ReviewActivity.this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    uploadReview();
                    finish();
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
                if (difficulty_level == null || satisfaction == null)
                {
                    Toast.makeText(this, "느낀점을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                {
                    if (review_content_edit.getText().toString().equals(""))
                    {
                        Toast.makeText(this, "아직 리뷰가 작성되지 않았습니다", Toast.LENGTH_SHORT).show();
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
            }
        });

    }

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