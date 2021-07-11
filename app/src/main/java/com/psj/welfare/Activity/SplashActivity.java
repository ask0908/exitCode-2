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
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.orhanobut.logger.Logger;
import com.psj.welfare.AppDatabase;
import com.psj.welfare.CategoryDao;
import com.psj.welfare.CategoryData;
import com.psj.welfare.R;
import com.psj.welfare.TutorialWelcome;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.LogUtil;
import com.psj.welfare.util.UnCatchTaskService;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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

    String token, sessionId;

    public Intent intent;

    boolean isPushClicked = false;

    boolean being_preview = false; //미리보기 했는지

    String age = ""; // room데이터가 있는지 확인하기 위해 임시로 데이터를 담는 변수
    // 쉐어드에 저장된 미리보기의 성별, 나이, 지역 정보
    String user_age, user_gender, user_area;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startService(new Intent(this, UnCatchTaskService.class));

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("age_group");
        editor.remove("user_gender");
        editor.remove("user_area");
        editor.apply();

        String force_stopped = sharedPreferences.getString("force_stopped", "");
        user_age = sharedPreferences.getString("age_group", "");
        user_gender = sharedPreferences.getString("user_gender", "");
        user_area = sharedPreferences.getString("user_area", "");
        Logger.d("강제종료값 확인 : " + force_stopped + "\n나이 : " + user_age + "\n성별 : " + user_gender + "\n지역 : " + user_area);
        if (force_stopped != null)
        {
            if (force_stopped.equals("강제종료됨"))
            {
                // 강제종료한 적이 있고 나이, 성별, 지역값이 없으면 스플래시 이후 계속 관심사 선택 화면으로 보낸다
                if (user_age.equals("") || user_gender.equals("") || user_area.equals(""))
                {
                    Logger.d("interestHandler 실행됨!!");
                    Handler handler = new Handler();
                    handler.postDelayed(new interestHandler(), 1200);
                }
                else
                {
                    // 강제종료한 적이 있고 나이, 성별, 지역값이 있으면 바로 메인으로 보낸다
                    Logger.d("강제종료한 기록 있고 성별, 나이, 지역값 모두 있음");
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
//                Log.e(TAG, "스플래시 화면에서 받은 fcm token : " + token);
                            editor.putString("fcm_token", token);
                            editor.apply();
                        }
                    });

                    beingRoomData();

                    onNewIntent(intent);
                }

            }
            else
            {
                Log.e(TAG, "강제종료한 기록 없음");
                // 강제종료한 적이 없으면 여기로 빠진다. 그냥 스플래시 화면에서 진행하는 로직을 여기로 가져오자
                // 첫 로그인 여부를 구별하기 위한 값을 쉐어드에 저장
                editor.putString("first_visit", "1");
                editor.apply();

                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바(상태표시줄) 글자색 검정색으로 바꾸기
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바(상태표시줄) 배경 흰색으로 설정

                //상태표시줄 색상변경
//        setStatusBarGradiant(SplashActivity.this);

                intent = getIntent();

                //미리보기 했는지 여부
                SharedPreferences shared = getSharedPreferences("welf_preview", 0);
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
//                Log.e(TAG, "스플래시 화면에서 받은 fcm token : " + token);
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
//                Log.e(TAG, "task : " + task);
//                Log.e(TAG, "task.getResult() : " + task.getResult());
//                Log.e(TAG, "task.getResult().getToken() : " + task.getResult().getToken());
                    }
                });

                //room데이터가 있는지 확인
                beingRoomData();

                onNewIntent(intent);
            }
        }

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

    // 미리보기에서 입력한 정보가 없는 채 앱을 켰을 때는 관심사 선택 화면으로 보내서 선택하게 한다
    // 이 때 관심사 선택 화면에서는 뒤로가기 이미지를 누를 수 없고 백버튼을 눌러도 나갈 수 없게 한다 (관심사 2번 화면에선 관심사 1번 화면으로 이동할 수 있게는 한다)
    private class interestHandler implements Runnable
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
//                    intent.putExtra("push", 100); // 이 부분 때문에 앱을 켜면 알림 화면부터 나왔습니다. 이 부분은 푸시 누를 때만 호출하면 될 거 같아여
                    startActivity(intent);
                    finish();
                }
                else
                { //room데이터 값이 있다면
                    Intent intent = new Intent(SplashActivity.this, MainTabLayoutActivity.class);
//                    intent.putExtra("push", 100);
                    startActivity(intent);
                    finish();
                }
//                Intent intent = new Intent(SplashActivity.this, ChooseFirstInterestActivity.class);
//                intent.putExtra("force_stopped", 404);
//                startActivity(intent);
//                SplashActivity.this.finish();
            }
        }
    }

    // 사용자가 앱에 들어왔을 때 세션 id값을 받아오는 메서드
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

    // 서버에서 받은 세션 id를 파싱해서 쉐어드에 저장하는 메서드
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

    //상태표시줄 색상변경
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