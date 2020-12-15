package com.psj.welfare.activity;

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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewUpdateActivity extends AppCompatActivity
{
    private String TAG = this.getClass().getName();

    private RatingBar review_rate_update_edit;
    private EditText review_content_update_edit;
    private ImageView update_review_photo;

    private Bitmap bitmap;
    Uri filePath;

    // 이미지 절대 경로
    String absolute;

    // 갤러리에서 가져온 이미지를 서버로 보내 저장하기 전 담는 변수
    String encodeImageString;

    File file;

    private static final int PICK_PHOTO = 1;

    SharedPreferences sharedPreferences;
    String token, content, image_url;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_update);

        setSupportActionBar(findViewById(R.id.review_update_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        sharedPreferences = getSharedPreferences("app_pref", 0);
        token = sharedPreferences.getString("token", "");

        // getIntent() null 체크
        if (getIntent().hasExtra("content") && getIntent().hasExtra("image_url"))
        {
            Intent intent = getIntent();
            content = intent.getStringExtra("content");
            image_url = intent.getStringExtra("image_url");
            String before_id = intent.getStringExtra("id");
            if (before_id != null)
            {
                id = Integer.parseInt(before_id);
            }
            Log.e(TAG, "DetailBenefit에서 가져온 리뷰 내용 : " + content + ", image_url : " + image_url);
            review_content_update_edit.setText(content);
            Glide.with(ReviewUpdateActivity.this)
                    .load(image_url)
                    .into(update_review_photo);
        }

        // 카메라 이미지를 누르면 갤러리로 이동해서 파일을 가져온다
        update_review_photo.setOnClickListener(v ->
        {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO);
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
                Glide.with(this)
                        .load(bitmap)
                        .into(update_review_photo);
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
        review_content_update_edit = findViewById(R.id.review_content_update_edit);
        update_review_photo = findViewById(R.id.update_review_photo);
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
                Toast.makeText(this, "리뷰 수정 완료", Toast.LENGTH_SHORT).show();
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
//                builder.setTitle("리뷰 삭제")
//                        .setMessage("리뷰를 삭제하시겠습니까?")
//                        .setPositiveButton("예", new DialogInterface.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which)
//                            {
//                                // 리뷰 삭제 메서드 호출
//                                deleteReview();
//                                Toast.makeText(ReviewUpdateActivity.this, "리뷰 삭제 완료", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            }
//                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        Toast.makeText(ReviewUpdateActivity.this, "리뷰 삭제 취소", Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    }
//                }).show();

            case android.R.id.home:
                return true;

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

        RequestBody imageReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "update.png", imageReqBody); // 2번 인자 file.getName()에서 String으로 수정

        RequestBody imageDescription = RequestBody.create(MediaType.parse("text/plain"), "image-type");

        Call<String> call = apiInterface.updateReview(token, id, review_content_update_edit.getText().toString(), imageDescription, imagePart);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "수정 결과 = " + result);
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