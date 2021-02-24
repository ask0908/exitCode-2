package com.psj.welfare.Activity;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.psj.welfare.R;
import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 리뷰 수정하는 화면 */
public class ReviewUpdateActivity extends AppCompatActivity
{
    private String TAG = this.getClass().getName();

    // 별점 매길 때 사용하는 ratingBar
    private RatingBar review_rate_update_edit;
    // 리뷰 작성하는 editText
    private EditText review_content_update_edit;
    // 리뷰에 붙일 이미지를 넣는 이미지뷰
//    private ImageView update_review_photo;

    // 갤러리에서 가져온 이미지를 담을 비트맵 객체
    private Bitmap bitmap;
    // 갤러리에서 가져온 이미지의 경로를 담을 Uri
    Uri filePath;

    // 이미지 절대 경로
    String absolute;

    // 갤러리에서 가져온 이미지를 서버로 보내 저장하기 전 담는 변수
    String encodeImageString;

    // 갤러리에서 가져온 이미지 파일을 담아서 레트로핏으로 서버로 보낼 때 쓸 파일 객체
    File file;

    // 갤러리에서 사진 가져올 때 onActivityResult()에서 결과 확인용으로 쓸 상수
    private static final int PICK_PHOTO = 1;

    // 단말에 저장된 토큰값을 가져올 때 쓸 쉐어드
    SharedPreferences sharedPreferences;
    // 단말에 저장된 토큰값, 리뷰 작성 내용, 갤러리에서 가져온 이미지 경로를 담을 변수
    String token, content, image_url, star_count = "";
    // 작성된 리뷰의 테이블 내 id를 담을 변수, 리뷰 화면에서 작성된 리뷰(아이템)를 누르면 그 아이템에 매핑된 테이블 내 id를 가져와서
    // 리뷰를 작성한 사람만 수정할 수 있게 한다
    int id;

    // 신청이 쉬웠나요 라디오그룹
    RadioGroup update_difficulty_radiogroup;
    // 혜택 만족도 라디오 그룹
    RadioGroup update_satisfaction_radiogroup;
    String difficulty, satisfaction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_update);

        // 화면 최상단의 툴바 쓰기 위한 처리
        setSupportActionBar(findViewById(R.id.review_update_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        sharedPreferences = getSharedPreferences("app_pref", 0);
        token = sharedPreferences.getString("token", "");

        // getIntent() null 체크 후 안에 있는 데이터 꺼냄
        if (getIntent().hasExtra("content") && getIntent().hasExtra("star_count"))
        {
            Intent intent = getIntent();
            content = intent.getStringExtra("content");
            star_count = intent.getStringExtra("star_count");
            Log.e(TAG, "DetailBenefit에서 가져온 리뷰 내용 : " + content + ", image_url : " + image_url);
            // 별 카운트에 값이 있다면 그걸 상단의 별점바에 세팅한다
            if (star_count != null)
            {
                float count = Float.parseFloat(star_count);
                review_rate_update_edit.setRating(count);
            }
            // id는 서버에서 String 형태로 날아오고, 수정 후 서버로 보낼 땐 int로 보내야 하기 때문에 int로 캐스팅한다
            String before_id = intent.getStringExtra("id");
            if (before_id != null)
            {
                id = Integer.parseInt(before_id);
            }
            /* 리뷰 수정 누르면 입력했던 내용 가져오도록 하는 중이었다 */
            // 자신이 작성한 리뷰 내용을 가져와 editText에 set
            review_content_update_edit.setText(content);
            // 리뷰 작성 시 사용했던 이미지를 가져와 set한다. 이 때 사진을 수정하지 않았을 경우 에러가 뜨기 때문에 기존에 첨부한 이미지의 경로를 가져와서
            // 이 파일과 매핑시켜야 하는데?
//            Glide.with(ReviewUpdateActivity.this)
//                    .load(image_url)
//                    .into(update_review_photo);
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
                        // 신청이 쉬워요 클릭
                        difficulty = "쉬워요";
                        break;

                    case R.id.update_hard_radiobutton :
                        // 신청이 어려워요 클릭
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
                        // 도움이 됐어요 클릭
                        satisfaction = "도움이 됐어요";
                        break;

                    case R.id.update_bad_radiobutton :
                        // 도움이 안 됐어요 클릭
                        satisfaction = "도움이 안 됐어요";
                        break;

                    default:
                        break;
                }
            }
        });

    }

    // 앨범에서 이미지 가져온 이후 필요한 처리는 여기서 수행한다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();
            try
            {
                absolute = getPath(this, filePath);
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
            Log.e(TAG, "absolute = " + absolute);
            file = new File(absolute);
            Log.e(TAG, "file.length() = " + file.length());
            if (!file.exists())
            {
                file.mkdirs();
            }
            Log.e(TAG, "file = " + file);
            try
            {
                InputStream inputStream = getContentResolver().openInputStream(filePath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                // 갤러리에서 가져온 이미지를 비트맵 객체에 넣고 Glide로 이미지뷰에 set한다
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                Glide.with(this)
//                        .load(bitmap)
//                        .into(update_review_photo);
                encodeBitmapImage(bitmap);
                /* 촬영한 이미지를 이미지뷰에 set할 때 90도 회전되서 set되므로 setRotation()으로 90도 회전시켜서 이미지뷰에 들어가게 한다 */
//                review_photo.setRotation(90);
                // 유저가 이미지뷰에 붙여넣은 이미지의 확장자를 가져온다
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // 비트맵 이미지를 jpeg 확장자로 저장하는 메서드
    private void encodeBitmapImage(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
        encodeImageString = Base64.encodeToString(bytesOfImage, Base64.DEFAULT);
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
                /* 리뷰 수정 메서드 호출 */
                updateReview();
                finish();
                break;

            case R.id.review_delete :
                /* AlertDialog 안에서 리뷰 삭제 메서드 호출 */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("리뷰 삭제")
                        .setMessage("리뷰를 삭제하시겠습니까?\n삭제된 리뷰는 복구할 수 없습니다")
                        .setPositiveButton("예", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Log.e(TAG, "token = " + token + ", id = " + id);
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

    /* 글을 작성했을 때의 로그인 토큰값을 넘겨야 한다!!! */
    // 리뷰 삭제 메서드
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
                    String result = response.body();
                    Log.e(TAG, "삭제 결과 = " + result);
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

    // 리뷰 수정 메서드
    void updateReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

//        RequestBody imageReqBody = RequestBody.create(MediaType.parse("image/*"), file);
//        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "update.png", imageReqBody); // 2번 인자 file.getName()에서 String으로 수정
//
//        RequestBody imageDescription = RequestBody.create(MediaType.parse("text/plain"), "image-type");

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
                    String result = response.body();
                    Log.e(TAG, "수정 결과 = " + result);
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

    // 절대경로 얻는 메서드
    @SuppressLint("NewApi")
    public String getPath(Context context, Uri uri) throws URISyntaxException
    {
        Log.e(TAG, "getPath() 실행");
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri))
        {
            if (isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }
            else if (isDownloadsDocument(uri))
            {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            }
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type))
                {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme()))
        {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try
            {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst())
                {
                    return cursor.getString(column_index);
                }
            }
            catch (Exception e)
            {
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }


    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

}