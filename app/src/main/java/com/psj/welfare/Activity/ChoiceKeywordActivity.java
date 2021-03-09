package com.psj.welfare.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.Adapter.ChoiceKeywordAdapter;
import com.psj.welfare.Data.ChoiceKeywordItem;
import com.psj.welfare.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* GetUserInformationActivity에서 사용자가 기본 정보(나이, 성별, 지역, 닉네임)를 입력하고 확인을 누르면 이 곳으로 이동해서 관심있는 키워드를 선택하게 한다
* 확인 버튼을 맨 밑에서 우상단으로 올린다 */
public class ChoiceKeywordActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // 체크된 체크박스를 담아서 확인 버튼을 눌렀을 때 사용자가 하나라도 골랐는지 확인할 때 사용할 리스트
    String age_group, user_gender, interest;
    String[] split_list;

    private RecyclerView choice_keyword_recyclerview;
    private ChoiceKeywordAdapter adapter;
    private ChoiceKeywordAdapter.ItemClickListener itemClickListener;

    // 리사이클러뷰에 체크박스 보여줄 때 사용할 리스트
    List<ChoiceKeywordItem> list;
    // 체크박스 클릭 시 어떤 체크박스를 골랐는지 정보를 담을 리스트
    List<String> str_list, noDuplicatedList;
    String str_server, message;

    SharedPreferences sharedPreferences;

    private Menu mOptionMenu;

    String encode_str;

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_keyword);

        list = new ArrayList<>();
        str_list = new ArrayList<>();
        noDuplicatedList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);

        choice_keyword_recyclerview = findViewById(R.id.choice_keyword_recyclerview);
        choice_keyword_recyclerview.setHasFixedSize(true);
        choice_keyword_recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

        // 쉐어드에 저장된 데이터 널 체크
        // 연령대
        if (sharedPreferences.getString("age_group", "") != null)
        {
            age_group = sharedPreferences.getString("age_group", "");
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("저장된 연령대 정보가 없습니다.\n먼저 [개인정보 수정] 버튼을 눌러 개인정보를 입력해 주세요")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }

        getAllKeyword();

        // 성별
        if (sharedPreferences.getString("user_gender", "") != null)
        {
            user_gender = sharedPreferences.getString("user_gender", "");
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("저장된 성별 정보가 없습니다.\n먼저 [개인정보 수정] 버튼을 눌러 개인정보를 입력해 주세요")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }

        // 관심사
        if (sharedPreferences.getString("interest", "") != null)
        {
            interest = sharedPreferences.getString("interest", "");
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("저장된 관심사 정보가 없습니다.\n먼저 [개인정보 수정] 버튼을 눌러 개인정보를 입력해 주세요")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }

        // 관심사 널 체크 후 값이 있다면 구분자를 기준으로 split하고 리스트에 추가한다
        if (interest != null)
        {
            split_list = interest.split(",");
            for (String str : split_list)
            {
                ChoiceKeywordItem item = new ChoiceKeywordItem();
                item.setInterest(str);
                list.add(item);
            }
            // split된 문자열들이 저장된 리스트를 리사이클러뷰 어댑터에 넣어서 유저에게 보여준다
            adapter = new ChoiceKeywordAdapter(this, list, itemClickListener);
            adapter.setOnItemClickListener((view, pos) ->
            {
                String name = list.get(pos).getInterest();
                str_list.add(name);
            });
            choice_keyword_recyclerview.setAdapter(adapter);
        }
        else
        {
            // 이곳으로 오는 경우는 쉐어드의 토큰이 DB에 저장된 토큰과 일치하지 않거나, 서버로 값을 보낼 때 이상하게 보내져서 결과값이 오지 않는 경우다
            // 토큰문제인 경우가 더 많아서 로그인을 다시 시도하면 어떻게 되는지 확인해보자
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("오류가 발생했습니다\n다시 로그인을 시도해 주세요")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            Intent intent = new Intent(ChoiceKeywordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finishAffinity();
                        }
                    }).show();
        }

        Toolbar choice_toolbar = findViewById(R.id.choice_toolbar);
        setSupportActionBar(choice_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mOptionMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.keyword_menu, menu);

        /* 메뉴 버튼 색 바꾸기 */
        MenuItem liveitem = mOptionMenu.findItem(R.id.keyword_ok);
        SpannableString spannableString = new SpannableString(liveitem.getTitle().toString());
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryDark)), 0, spannableString.length(), 0);
        liveitem.setTitle(spannableString);
        return true;
    }

    public void checkKeyword()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "str_list = " + str_list);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token = sharedPreferences.getString("token", "");
        if (token != null)
        {
            Call<String> call = apiInterface.checkKeyword(token, "interest");
            call.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(Call<String> call, Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String result = response.body();
                        Log.e(TAG, "사용자 관심사 조회 api 성공 : " + result);
                        jsonParsing(result);
                    }
                    else
                    {
                        Log.e(TAG, "사용자 관심사 조회 실패 : " + response.body());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t)
                {
                    Log.e(TAG, "사용자 관심사 조회 에러 : " + t.getMessage());
                }
            });
        }

    }

    // 좌상단 뒤로가기 버튼을 누르면 사용자 개인정보(나이, 성별, 지역, 닉네임)을 입력받는 화면으로 돌아간다
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                finish();
                return true;

            case R.id.keyword_ok :
                Log.e(TAG, "str_list = " + str_list);
                if (str_list.size() == 0)
                {
                    // 아무것도 선택하지 않으면 여기로 오는지 확인해야 함
                    Toast.makeText(this, "최소 1개를 선택하셔야 합니다", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                {
                    // 1개라도 선택했다면
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    isConnected = isNetworkConnected(this);
                    if (!isConnected)
                    {
                        // false면 다이얼로그 띄움
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
                                .setCancelable(false)
                                .setPositiveButton("예", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                        startActivity(intent);
                                    }
                                }).show();
                    }
                    else
                    {
                        // true면 로직 이어서 수행
                        editor.putString("user_category", str_list.toString());
                        editor.apply();
                        registerUserInterest();
                    }
                    return true;
                }
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                isConnected = isNetworkConnected(this);
//                if (!isConnected)
//                {
//                    // false면 다이얼로그 띄움
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                            .setCancelable(false)
//                            .setPositiveButton("예", new DialogInterface.OnClickListener()
//                            {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which)
//                                {
//                                    dialog.dismiss();
//                                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                    startActivity(intent);
//                                }
//                            }).show();
//                }
//                else
//                {
//                    // true면 로직 이어서 수행
//                    editor.putString("user_category", str_list.toString());
//                    editor.apply();
//                    registerUserInterest();
//                }
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* 21.03.05) 관심사 리스트 조회 */
    private void getAllKeyword()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        if (!sharedPreferences.getString("token", "").equals(""))
        {
            String token = sharedPreferences.getString("token", "");
            Call<String> call = apiInterface.getAllKeyword(token, "interestList");
            call.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(Call<String> call, Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String result = response.body();
                        Log.e(TAG, "관심사 리스트 조회 성공 : " + result);
                    }
                    else
                    {
                        Log.e(TAG, "관심사 리스트 조회 실패 : " + response.body());
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t)
                {
                    Log.e(TAG, "관심사 리스트 조회 에러 : " + t.getMessage());
                }
            });
        }
    }

    /* 유저가 선택한 키워드들을 서버에 등록하는 기능 */
    void registerUserInterest()
    {
        String token = sharedPreferences.getString("token", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "str_list = " + str_list);

        StringBuilder sb = new StringBuilder();
        for (String s : str_list)
        {
            sb.append(s);
            sb.append("|");
        }
        Log.e(TAG, "문자열에 담은 결과 확인 = " + sb.toString());

        // 맨 마지막의 |만 잘라서 str_server에 저장한다
        if (sb.toString().length() > 0 && sb.toString().charAt(sb.toString().length() - 1) == '|')
        {
            str_server = sb.toString().substring(0, sb.toString().length() - 1);
        }
        Log.e(TAG, "str_server = " + str_server);
        encode("키워드 선택 화면 진입");
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.registerUserInterest(session, encode_str, token, "interest", str_server);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 : " + response.body());
                    jsonParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    /* 서버에 유저가 선택한 키워드들 등록한 후 서버에서 받은 JSON 값 파싱하는 메서드 */
    private void jsonParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("Message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, message);
        Toast.makeText(this, "키워드 정보 수정이 완료됐어요", Toast.LENGTH_SHORT).show();
        finish();
    }

    /* 비행기 모드 확인하는 메서드 */
    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);   // 핸드폰
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);       // 와이파이
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);     // 태블릿
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected(); // wimax 상태 체크
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            // 모바일 네트워크 체크
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            // wifi 네트워크 체크
            {
                return true;
            }
        }
        return false;
    }

}