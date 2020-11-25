package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/* 리뷰 텍스트뷰를 누르면 나오는 화면
 * 별점을 확인하고 핸드폰에서 이미지를 첨부해 리뷰를 쓸 수 있다 */
public class ReviewActivity extends AppCompatActivity
{
    private static final String TAG = "ReviewActivity";

    private RatingBar review_rate_edit;
    private EditText review_content_edit;
    private ImageView review_photo;

    // editText에 작성한 내용을 담을 변수
    String review_content;

    private Bitmap bitmap;
    Uri filePath;

    // 이미지 절대 경로
    String absolute;

    // 갤러리에서 가져온 이미지를 서버로 보내 저장하기 전 담는 변수
    String encodeImageString;

    // Volley로 이미지 + 텍스트를 서버로 저장할 때 사용할 API 주소가 담긴 변수

    ProgressDialog progressDialog;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        setSupportActionBar(findViewById(R.id.review_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        // 이미지를 추가하려면 앨범에 접근해야 하기 때문에 이를 위한 권한 처리기 생성
        PermissionListener permissionListener = new PermissionListener()
        {
            @Override
            public void onPermissionGranted()
            {
                //
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions)
            {
                //
            }
        };

        // 권한 처리기 설정 후 실행
        TedPermission.with(this)
                .setRationaleMessage("앨범 접근 권한 설정")
                .setDeniedMessage("이미지를 추가하려면 앨범 접근 권한을 설정해야 합니다")
                .setPermissionListener(permissionListener)
                .setPermissions(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
                .check();

        // 카메라 이미지를 누르면 갤러리로 이동해서 파일을 가져온다
        review_photo.setOnClickListener(v ->
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
        });

    }

    // 앨범에서 이미지 가져온 이후 필요한 처리는 여기서 수행한다
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();
//            absolute = Environment.getExternalStorageDirectory().getAbsolutePath();
//            absolute = getRealPathFromURI(filePath);
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
                        .into(review_photo);
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

    private String getRealPathFromURI(Uri contentUri)
    {
        int column_index = 0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst())
        {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
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

    /* 이미지 없이 텍스트 리뷰들만 서버로 보내는 레트로핏 메서드 */
//    void sendReview()
//    {
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
////        review_content = review_content_edit.getText().toString();
//        String star_count = String.valueOf(review_rate_edit.getRating());
//        /* 이미지 검증 후 인자로 추가해야 함(나중에) */
//        Call<String> call = apiInterface.sendReview("test", review_content, "writer 테스트", "testgmail@gmail.com", "0",
//                "0", star_count);
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    Log.e(TAG, "response = " + response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e("sendReview()", "에러 : " + t.getMessage());
//            }
//        });
//    }

    private void init()
    {
        review_rate_edit = findViewById(R.id.review_rate_edit);
        review_rate_edit = findViewById(R.id.review_rate_edit);
        review_content_edit = findViewById(R.id.review_content_edit);
        review_photo = findViewById(R.id.review_photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.review_menu, menu);
        return true;
    }

//    void uploadReview()
//    {
//        StringRequest request = new StringRequest(Request.Method.POST, URL_UPLOAD, new com.android.volley.Response.Listener<String>()
//        {
//            @Override
//            public void onResponse(String response)
//            {
//                Log.e("uploadReview()", response);
//                try
//                {
//                    JSONObject jsonObject = new JSONObject(response);
//                    String success = jsonObject.getString("success");
//                    // 서버에 저장되면 PHP 파일에서 "1"을 리턴한다
//                    if (success.equals("1"))
//                    {
//                        Log.e(TAG, "success = " + success);
//                        Toast.makeText(ReviewActivity.this, "소중한 리뷰 감사합니다", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                catch (JSONException e)
//                {
//                    e.printStackTrace();
//                    Log.e("uploadReview()", "에러 : " + e.toString());
//                }
//            }
//        }, new com.android.volley.Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError e)
//            {
//                Log.e("uploadReview()", "에러 : " + e.toString());
//            }
//        })
//        {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError
//            {
//                review_content = review_content_edit.getText().toString();
//                Map<String, String> params = new HashMap<>();
//                params.put("welf_name", "test");
//                params.put("content", review_content);
//                params.put("writer", "아이디는최대10자만");
//                params.put("email", "gmail@gmail.com");
//                params.put("like_count", "0");
//                params.put("bad_count", "0");
//                params.put("star_count", "3.0");
//                params.put("osType", "android");
//                params.put("fileName", encodeImageString);
//                return params;
//            }
//        };
//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(request);
//
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.review_done:
                // 서버에 저장하는 메서드 호출. 서버로 보낼 때 Uri는 String으로 바꿔서 보낸다?
//                String photo_uri = String.valueOf(uri);
//                Log.e(TAG, "photo_uri = " + photo_uri);
//                sendReview();
//                uploadReview();
                /* 이미지 전송 메서드 호출 */
                uploadReview(absolute);
                Toast.makeText(this, "소중한 리뷰가 등록되었어요", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case android.R.id.home:
                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void uploadReview(String filePath)
    {
        Retrofit retrofit = ApiClient.getApiClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Log.e("aaa", "file = " + file);
        Log.e("aaa", "file.length() = " + file.length());

        // 이미지 파일을 만들고(thumbnailFile)
        RequestBody imageReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "test.png", imageReqBody); // 2번 인자 file.getName()에서 String으로 수정

        RequestBody imageDescription = RequestBody.create(MediaType.parse("text/plain"), "image-type");

        // 이미지를 업로드하는 레트로핏 객체를 생성
        Call<String> call = apiInterface.uploadReview("test", "test", "writerTest", "gmail@gmail.com", "0", "0",
                "3.5", imageDescription, imagePart);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                Log.e(TAG, "응답메소드 호출");
                if (response.isSuccessful())
                {
                    Log.e(TAG, "response = " + response);
                    Toast.makeText(getApplicationContext(), "전송됨", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
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

    // 절대경로 얻는 메서드
    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException
    {
        Log.i(TAG, "getpath메소드 실행");
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