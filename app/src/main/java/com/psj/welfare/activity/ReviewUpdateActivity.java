package com.psj.welfare.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewUpdateActivity extends AppCompatActivity
{
    private String TAG = this.getClass().getName();

    private RatingBar review_rate_update_edit;
    private EditText review_content_update_edit;

    private Bitmap bitmap;
    Uri filePath;

    String absolute;

    String encodeImageString;

    File file;

    private static final int PICK_PHOTO = 1;

    SharedPreferences sharedPreferences;
    String token, content, image_url, star_count = "";
    int id;

    RadioGroup update_difficulty_radiogroup;
    RadioGroup update_satisfaction_radiogroup;
    String difficulty, satisfaction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ReviewUpdateActivity.this);
        setContentView(R.layout.activity_review_update);

        setSupportActionBar(findViewById(R.id.review_update_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        sharedPreferences = getSharedPreferences("app_pref", 0);
        token = sharedPreferences.getString("token", "");

        if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
        {
            Intent intent = getIntent();
            content = intent.getStringExtra("content");
            star_count = intent.getStringExtra("star_count");
            if (star_count != null)
            {
                float count = Float.parseFloat(star_count);
                review_rate_update_edit.setRating(count);
            }
            String before_id = intent.getStringExtra("id");
            if (before_id != null)
            {
                id = Integer.parseInt(before_id);
            }
            review_content_update_edit.setText(content);
        }

        // 카메라 이미지를 누르면 갤러리로 이동해서 파일을 가져온다
//        update_review_photo.setOnClickListener(v ->
//        {
//            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, PICK_PHOTO);
//        });

        update_difficulty_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.update_easy_radiobutton :
                        difficulty = "쉬워요";
                        break;

                    case R.id.update_hard_radiobutton :
                        difficulty = "어려워요";
                        break;
                }
            }
        });

        update_satisfaction_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.update_good_radiobutton :
                        satisfaction = "도움이 됐어요";
                        break;

                    case R.id.update_bad_radiobutton :
                        satisfaction = "도움이 안 됐어요";
                        break;

                    default:
                        break;
                }
            }
        });

    }

    private void init()
    {
        review_rate_update_edit = findViewById(R.id.review_rate_update_edit);
        update_difficulty_radiogroup = findViewById(R.id.update_difficulty_radiogroup);
        update_satisfaction_radiogroup = findViewById(R.id.update_satisfaction_radiogroup);
        review_content_update_edit = findViewById(R.id.review_content_update_edit);
//        update_review_photo = findViewById(R.id.update_review_photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.review_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.review_update:
                updateReview();
                finish();
                break;

            case R.id.review_delete :
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("리뷰 삭제")
                        .setMessage("리뷰를 삭제하시겠습니까?\n삭제된 리뷰는 복구할 수 없습니다")
                        .setPositiveButton("예", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                deleteReview();
                                Toast.makeText(ReviewUpdateActivity.this, "리뷰 삭제 완료", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Toast.makeText(ReviewUpdateActivity.this, "리뷰 삭제 취소", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).create().show();

            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void deleteReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.deleteReview(token, id, "delete");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    void updateReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        int rate = (int) review_rate_update_edit.getRating();
        String star_count = String.valueOf(rate);
        Call<String> call = apiInterface.updateReview(token, id, review_content_update_edit.getText().toString(), null, null,
                difficulty, satisfaction, star_count);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Toast.makeText(ReviewUpdateActivity.this, "리뷰 수정 완료", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
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