package com.psj.welfare.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.TutorialWelcome;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;
import com.psj.welfare.util.UnCatchTaskService;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* @Keep : 이 어노테이션이 적인 클래스 파일은 암호화하지 말라는 의미
* 왜냐면 이 클래스가 실행되어 onCreate()가 호출되면 FCM 토큰을 받아오는데 이 토큰이 암호화되버려서, FCM이 오지 않거나 앱이 죽는 현상이 발생했다
* 그래서 추가한 어노테이션 */
@Keep
public class SplashActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    //fcm토큰
    private String FCMtoken;
    //토큰, 세션 아이디
    private String token, sessionId;

    public Intent intent;

    boolean isPushClicked = false;

    String age = ""; // room데이터가 있는지 확인하기 위해 임시로 데이터를 담는 변수
    // 쉐어드에 저장된 미리보기의 성별, 나이, 지역 정보
    String user_age, user_gender, user_area;

    //로그인관련 쉐어드 singleton
    private SharedSingleton sharedSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startService(new Intent(this, UnCatchTaskService.class));

        //로그인 singleton 패턴으로 사용
        sharedSingleton = SharedSingleton.getInstance(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바(상태표시줄) 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바(상태표시줄) 배경 흰색으로 설정


        intent = getIntent();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task)
            {
                if (!task.isSuccessful())
                {
                    Log.e("FCM LOG", "getInstanceId failed", task.getException());
                    return;
                }
//                Log.e(TAG,"fcm token : " + task.getResult().getToken());
                FCMtoken = task.getResult().getToken();
                sharedSingleton.setFCMtoken(FCMtoken);
            }
        });

        //위쪽 코드 deprecated 여서 대신 사용할 코드(미완성)
//        /* 아래 코드를 쓰면 토큰이 항상 refresh되지만 이 토큰으로 fcm을 받을 수 있을지? */
//        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>()
//        {
//            @Override
//            public void onComplete(@NonNull Task<InstallationTokenResult> task)
//            {
//            }
//        });

        //room데이터가 있는지 확인
//        beingRoomData();

        onNewIntent(intent);
    }


    //Room 사용법 참고용-----------------------------------------------------------
//    void beingRoomData()
//    {
//        Thread thread = new Thread()
//        {
//            public void run()
//            {
//                try
//                {
//                    //Room을 쓰기위해 데이터베이스 객체 만들기
//                    AppDatabase database = Room.databaseBuilder(SplashActivity.this, AppDatabase.class, "Firstcategory")
//                            .fallbackToDestructiveMigration()
//                            .build();
//
//                    //DB에 쿼리를 던지기 위해 선언
//                    CategoryDao categoryDao = database.getcategoryDao();
//
//                    List<CategoryData> alldata = categoryDao.findAll();
//                    for (CategoryData data : alldata)
//                    {
//                        age = data.age;
//                    }
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        };
//        thread.start();
//    }



    @Override
    protected void onNewIntent(Intent intent)
    {
        boolean isConnected = isNetworkConnected(SplashActivity.this); //인터넷이 월활하지 않을 때
        if (!isConnected) //인터넷이 연결이 안됐다면
        {
            AlertDialog.Builder TutorialDialog = new AlertDialog.Builder(SplashActivity.this);
            View dialogview = getLayoutInflater().inflate(R.layout.custom_inconnected_internet_dialog,null); //다이얼로그의 xml뷰 담기
            Button BtnOk = dialogview.findViewById(R.id.BtnOk); //취소 버튼

            TutorialDialog.setView(dialogview); //alertdialog에 view 넣기
            final AlertDialog alertDialog = TutorialDialog.create(); //다이얼로그 객체로 만들기
            alertDialog.show(); //다이얼로그 보여주기

            //취소 버튼
            BtnOk.setOnClickListener(v->{
                alertDialog.dismiss(); //다이얼로그 사라지기
                finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
            });
        } else { //인터넷이 연결 됐을 때만 작동하기

            //토큰값 가져오기
            userLog();

            //카카오 로그인시 필요한 해시키
            getHashKey();

            if (intent != null && intent.getExtras() != null) //푸시 알림 눌러 실행시켰을 때
            {
                String push_clicked = intent.getExtras().getString("push_clicked");
                Logger.d("푸시가 클릭됐는가 : " + push_clicked);
                if (push_clicked != null)
                {
                    isPushClicked = true;
                    Handler handler = new Handler();
                    handler.postDelayed(new SplashHandler(), 1200);
                }
            }
            else
            {
                //일반적으로 앱을 실행시켰을 때
                Handler handler = new Handler();
                handler.postDelayed(new NormalHandler(), 1200);
            }
        }
        super.onNewIntent(intent);
    }



    //일반적으로 앱을 실행시켰을 때
    private class NormalHandler implements Runnable
    {
        @Override
        public void run()
        {
            //미리보기를 했다면 (건너뛰기 포함)
            if(sharedSingleton.getBooleanPreview()){
                if(sharedSingleton.getBooleanLogin() && !sharedSingleton.getBooleanInterst()){ //로그인은 하고 관심사를 선택 하지 않았다면
                    startActivity(new Intent(SplashActivity.this, ChooseFirstInterestActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
                }
            } else { //미리보기를 안했다면
                startActivity(new Intent(SplashActivity.this, TutorialWelcome.class));
            }

            SplashActivity.this.finish();
        }
    }


    //푸시알림으로 앱을 실행시켰을 때
    private class SplashHandler implements Runnable
    {
        @Override
        public void run()
        {
            boolean isConnected = isNetworkConnected(SplashActivity.this);
            if (!isConnected)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("인터넷 연결이 원활하지 않습니다")
                        .setCancelable(false)
                        .setPositiveButton("종료", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finishAffinity();
                            }
                        }).show();
            }
            else
            {
                //미리보기를 했다면 (건너뛰기 포함)
                if(sharedSingleton.getBooleanPreview()){
                    //로그인은 하고 관심사를 선택 하지 않았다면
                    if(sharedSingleton.getBooleanLogin() && !sharedSingleton.getBooleanInterst()){
                        startActivity(new Intent(SplashActivity.this, ChooseFirstInterestActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
                    }
                } else { //미리보기를 안했다면
                    startActivity(new Intent(SplashActivity.this, TutorialWelcome.class));
                }
                
                SplashActivity.this.finish();
            }
        }
    }



    // 사용자가 앱에 들어왔을 때 세션 id값을 받아오는 메서드
    void userLog()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        sharedPreferences = getSharedPreferences("app_pref", 0);
//        String token = sharedPreferences.getString("token", "");
        token = sharedSingleton.getToken();
        Call<String> call = apiInterface.userLog(token, null, "main", "앱 접속", null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    sessionParsing(result);
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

    // 서버에서 받은 세션 id를 파싱해서 쉐어드에 저장하는 메서드
    private void sessionParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            sessionId = jsonObject.getString("SessionId");
//            Log.e(TAG,"sessionId : " + sessionId);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        sharedSingleton.setSessionId(sessionId);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("sessionId", sessionId);
//        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        // 이 메서드를 재정의하고 비워두면 스플래시 화면에서 백버튼을 눌러도 뒤로가기가 되지 않는다
    }

    // 네트워크 연결 상태 확인 메서드
    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected();
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        return false;
    }

    //카카오 로그인시 필요한 해시키
    private void getHashKey()
    {
        PackageInfo packageInfo = null;
        try
        {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        if (packageInfo == null)
        {
            Log.e("KeyHash", "KeyHash:null");
        }

        for (Signature signature : packageInfo.signatures)
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
//                Log.e("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
            catch (NoSuchAlgorithmException e)
            {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}