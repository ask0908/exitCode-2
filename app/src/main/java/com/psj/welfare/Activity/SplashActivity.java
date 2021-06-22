package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.psj.welfare.AppDatabase;
import com.psj.welfare.CategoryDao;
import com.psj.welfare.CategoryData;
import com.psj.welfare.R;
import com.psj.welfare.TutorialWelcome;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Keep
public class SplashActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    String token, sessionId;

    public Intent intent;

    boolean isPushClicked = false;

    boolean being_preview = false; //미리보기 했는지

    String age = ""; // room데이터가 있는지 확인하기 위해 임시로 데이터를 담는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        intent = getIntent();

        setStatusBarGradiant(SplashActivity.this);

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //미리보기 했는지 여부
        SharedPreferences shared = getSharedPreferences("welf_preview",MODE_PRIVATE);
        being_preview = shared.getBoolean("being_preview",false);
//        Log.e("being_preview",String.valueOf(being_preview));

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
                token = task.getResult().getToken();
                Log.e(TAG, "스플래시 화면에서 받은 fcm token : " + token);
                editor.putString("fcm_token", token);
                editor.apply();
            }
        });

        /* 아래 코드를 쓰면 토큰이 항상 refresh되지만 이 토큰으로 fcm을 받을 수 있을지? */
        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>()
        {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task)
            {
                Log.e(TAG, "task : " + task);
                Log.e(TAG, "task.getResult() : " + task.getResult());
                Log.e(TAG, "task.getResult().getToken() : " + task.getResult().getToken());
            }
        });

        //room데이터가 있는지 확인
        beingRoomData();

        onNewIntent(intent);
    }

    void beingRoomData()
    {
        Thread thread = new Thread()
        {
            public void run()
            {
                try
                {
                    //Room을 쓰기위해 데이터베이스 객체 만들기
                    AppDatabase database = Room.databaseBuilder(SplashActivity.this, AppDatabase.class, "Firstcategory")
                            .fallbackToDestructiveMigration()
                            .build();

                    //DB에 쿼리를 던지기 위해 선언
                    CategoryDao categoryDao = database.getcategoryDao();

                    List<CategoryData> alldata = categoryDao.findAll();
                    for (CategoryData data : alldata)
                    {
                        age = data.age;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

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
                String aaa = intent.getExtras().getString("push_clicked");
                if (aaa == null)
                {
                    isPushClicked = true;
                    Handler handler = new Handler();
                    handler.postDelayed(new SplashHandler(), 1000);
                }
            }
            else
            {
                //일반적으로 앱을 실행시켰을 때
                Handler handler = new Handler();
                handler.postDelayed(new NormalHandler(), 1000);
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
            if (!age.equals(""))
            { //room데이터 값이 있다면
                startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
//                startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
            }
            else if (being_preview)
            { //미리보기를 취소했거나 화면을 한번이라도 들어갔다면
                startActivity(new Intent(SplashActivity.this, MainTabLayoutActivity.class));
            }
            else
            { //room데이터 값이 없다면
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

                if (age.equals(null))
                { //room데이터 값이 없다면
                    Intent intent = new Intent(SplashActivity.this, TutorialWelcome.class);
                    intent.putExtra("push", 100);
                    startActivity(intent);
                }
                else
                { //room데이터 값이 있다면
                    Intent intent = new Intent(SplashActivity.this, MainTabLayoutActivity.class);
                    intent.putExtra("push", 100);
                    startActivity(intent);
                }

                SplashActivity.this.finish();
            }
        }
    }

    void userLog()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
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

    private void sessionParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            sessionId = jsonObject.getString("SessionId");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sessionId", sessionId);
        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        // 이 메서드를 재정의하고 비워두면 스플래시 화면에서 백버튼을 눌러도 뒤로가기가 되지 않는다
    }

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

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
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