package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
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
    private ImageView review_photo;

    private Bitmap bitmap;
    Uri filePath;

    // 이미지 절대 경로
    String absolute;

    // 갤러리에서 가져온 이미지를 서버로 보내 저장하기 전 담는 변수
    String encodeImageString;

    File file;

    private static final int PICK_PHOTO = 1;

    // 신청이 쉬웠나요 라디오그룹
    RadioGroup difficulty_radiogroup;
    // 혜택 만족도 라디오 그룹
    RadioGroup satisfaction_radiogroup;

    // 라디오 그룹에서 선택한 값 담을 변수
    String difficulty, satisfaction = "";

    // 선택해서 가져온 리뷰 아이디
    String review_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        if (getIntent().hasExtra("id"))
        {
            Intent intent = getIntent();
            review_id = intent.getStringExtra("id");
        }
        Log.e(TAG, "받아온 id = " + review_id);

        setSupportActionBar(findViewById(R.id.review_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        /* 리뷰 작성 시 165자 제한 */
        review_content_edit.setFilters(new InputFilter[] {
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
                    case R.id.easy_radiobutton :
                        // 신청이 쉬워요 클릭
                        difficulty = "쉬워요";
                        break;

                    case R.id.hard_radiobutton :
                        // 신청이 어려워요 클릭
                        difficulty = "어려워요";
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
                    case R.id.good_radiobutton :
                        // 도움이 됐어요 클릭
                        satisfaction = "도움이 됐어요";
                        break;

                    case R.id.bad_radiobutton :
                        // 도움이 안 됐어요 클릭
                        satisfaction = "도움이 안 됐어요";
                        break;

                    default:
                        break;
                }
            }
        });

        // 이미지를 추가하려면 앨범에 접근해야 하기 때문에 이를 위한 권한 처리기 생성
//        PermissionListener permissionListener = new PermissionListener()
//        {
//            @Override
//            public void onPermissionGranted()
//            {
//                //
//            }
//
//            @Override
//            public void onPermissionDenied(List<String> deniedPermissions)
//            {
//                //
//            }
//        };
//
//        // 권한 처리기 설정 후 실행
//        TedPermission.with(this)
//                .setRationaleMessage("앨범 접근 권한 설정")
//                .setDeniedMessage("이미지를 추가하려면 앨범 접근 권한을 설정해야 합니다")
//                .setPermissionListener(permissionListener)
//                .setPermissions(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
//                .check();

        // 카메라 이미지를 누르면 갤러리로 이동해서 파일을 가져온다
//        review_photo.setOnClickListener(v ->
//        {
//            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, PICK_PHOTO);
//        });

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
                Glide.with(this)
                        .load(bitmap)
                        .into(review_photo);
                encodeBitmapImage(bitmap);
                /* 촬영한 이미지를 이미지뷰에 set할 때 90도 회전되서 set되므로 setRotation()으로 90도 회전시켜서 이미지뷰에 들어가게 한다 */
//                review_photo.setRotation(90);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    // 파일 확장자 알아내는 메서드(나중에 고도화할 때 사용)
    public static String getMimeType(Context context, Uri uri)
    {
        String extension;

        // Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT))
        {
            // If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        }
        else
        {
            // If scheme is a File
            // This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension;
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
        review_rate_edit = findViewById(R.id.review_rate_edit);
        review_content_edit = findViewById(R.id.review_content_edit);
        review_photo = findViewById(R.id.review_photo);

        difficulty_radiogroup = findViewById(R.id.difficulty_radiogroup);
        satisfaction_radiogroup = findViewById(R.id.satisfaction_radiogroup);
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
                if (difficulty.equals("") || satisfaction.equals(""))
                {
                    Toast.makeText(this, "느낀점을 선택해 주세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                {
                    uploadReview();
                    finish();
                    break;
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
//        Log.e("aaa", "file = " + file);
//        Log.e("aaa", "file.length() = " + file.length());

        // 이미지 파일을 만들고(thumbnailFile)
        /* 리뷰 이미지 삭제로 코드 주석 처리 */
//        RequestBody imageReqBody = RequestBody.create(MediaType.parse("image/*"), file);
//        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "test.png", imageReqBody); // 2번 인자 file.getName()에서 String으로 수정
//        RequestBody imageDescription = RequestBody.create(MediaType.parse("text/plain"), "image-type");

        /* 리뷰 쓰는 조건 = 사용자가 정보를 등록해야 함(나이, 성별, 닉네임) */
        // 이미지를 업로드하는 레트로핏 객체를 생성
//        Call<String> call = apiInterface.uploadReview(token, 1019, review_content_edit.getText().toString(), imageDescription, imagePart);

        int rate = (int) review_rate_edit.getRating();
        int id = 0;
        id = Integer.parseInt(review_id);
        String star_count = String.valueOf(rate);
        Call<String> call = apiInterface.uploadReview(token, id, review_content_edit.getText().toString(), null, null,
                difficulty, satisfaction, star_count);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "리뷰 작성 성공 = " + response);
                    Toast.makeText(getApplicationContext(), "리뷰가 등록되었습니다", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e("tag", t.toString());
                Log.e(TAG, "fail");
                Toast.makeText(getApplicationContext(), "에러 : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 절대경로 얻는 메서드(static 제거)
    @SuppressLint("NewApi")
    public String getPath(Context context, Uri uri) throws URISyntaxException
    {
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