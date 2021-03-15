package com.psj.welfare.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.adapter.ChoiceKeywordAdapter;
import com.psj.welfare.adapter.ExpandableListAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.CustomWheelDialog;
import com.psj.welfare.data.ChoiceKeywordItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestChoiceKeywordActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // 체크된 체크박스를 담아서 확인 버튼을 눌렀을 때 사용자가 하나라도 골랐는지 확인할 때 사용할 리스트
    String age_group, user_gender, interest;
    String[] split_list;

    /* expandable 리사이클러뷰 테스트용 */
    private RecyclerView recyclerView;

    private RecyclerView choice_keyword_recyclerview;
    private ChoiceKeywordAdapter adapter;
    private ChoiceKeywordAdapter.ItemClickListener itemClickListener;

    // 리사이클러뷰에 체크박스 보여줄 때 사용할 리스트
    List<ChoiceKeywordItem> list;
    // 체크박스 클릭 시 어떤 체크박스를 골랐는지 정보를 담을 리스트
    List<String> str_list, noDuplicatedList;
    // 유저가 선택한 키워드를 서버로 보낼 때 사용하는 변수
    String str_server, message;

    SharedPreferences sharedPreferences;

    private Menu mOptionMenu;

    String encode_str;

    // 인터넷 상태 확인 후 AlertDialog를 띄울 때 사용할 변수
    boolean isConnected = false;

    // 10대~70대 남자, 여자 관심사들을 담는 변수
    // 해시맵에 담기 전 값을 보관한다
    JSONObject user_10_man;
    JSONObject user_10_woman;
    JSONObject user_20_man;
    JSONObject user_20_woman;
    JSONObject user_30_man;
    JSONObject user_30_woman;
    JSONObject user_40_man;
    JSONObject user_40_woman;
    JSONObject user_50_man;
    JSONObject user_50_woman;
    JSONObject user_60_man;
    JSONObject user_60_woman;
    JSONObject user_70_man;
    JSONObject user_70_woman;
    JSONObject nothing;

    // 위의 JSONObject에 담긴 JSON 값에서 key, value를 분류해 각각 담아 리사이클러뷰에 활용할 때 사용하는 변수
    Map<String, String> hashMap;

    // 나이, 성별 받는 휠이 2개 있는 다이얼로그로 이동하도록 하는 이미지뷰
    ImageView filter_imageview;

    // 다이얼로그에서 나이, 성별 선택하면 "20대,남자" 형식의 값을 가질 변수
    // 해시맵에서 값을 가져올 때 키로 사용한다
    String userInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_choice_keyword);

        filter_imageview = findViewById(R.id.filter_imageview);
        /* expandable 리사이클러뷰 테스트용 리사이클러뷰 */
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<ExpandableListAdapter.Item> data = new ArrayList<>();

        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Fruits"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Apple"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Orange"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Banana"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Cars"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Audi"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Aston Martin"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "BMW"));
        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Cadillac"));

        ExpandableListAdapter.Item places = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "Places");
        places.invisibleChildren = new ArrayList<>();
        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Kerala"));
        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Tamil Nadu"));
        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Karnataka"));
        places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, "Maharashtra"));

        data.add(places);

        recyclerView.setAdapter(new ExpandableListAdapter(data));
        /* expandable 리사이클러뷰 테스트용 리사이클러뷰 끝 */

        list = new ArrayList<>();
        str_list = new ArrayList<>();
        noDuplicatedList = new ArrayList<>();
        hashMap = new HashMap<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);

        filter_imageview.setOnClickListener(v ->
        {
            // 휠이 2개 있는 커스텀 다이얼로그를 띄운다
            CustomWheelDialog dialog = new CustomWheelDialog(TestChoiceKeywordActivity.this, new CustomWheelDialog.onDialogListener()
            {
                @Override
                public void receiveData(String age, String gender)
                {
                    Log.e(TAG, "다이얼로그에서 액티비티로 온 나이값 : " + age + ", 성별값 : " + gender);
                    userInformation = age + "," + gender;
                    Log.e(TAG, "나이, 성별을 합쳐서 해시맵 키로 사용할 변수 완성 형태 : " + userInformation);
                    setInterestIntoRecyclerview(userInformation);
                }
            });
            dialog.showDialog();
        });

        choice_keyword_recyclerview = findViewById(R.id.choice_keyword_recyclerview);
        choice_keyword_recyclerview.setHasFixedSize(true);
        choice_keyword_recyclerview.setLayoutManager(new GridLayoutManager(this, 4));

        getAllKeyword();
        getAllInterest();

        Toolbar choice_toolbar = findViewById(R.id.choice_toolbar);
        setSupportActionBar(choice_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /* 다이얼로그에서 선택한 나이, 성별에 맞는 관심사를 리사이클러뷰에 넣는 메서드 */
    private void setInterestIntoRecyclerview(String userInformation)
    {
        // 새로 받은 값들로 리스트를 채워야 하기 때문에 리스트를 꼭 비워준다. 이 처리가 없으면 기존의 데이터들이 사라지지 않고 밑에 새 데이터들이 쌓인다
        list.clear();

        // "20대,남자" 형태의 문자열이 인자로 들어오면 해시맵에서 이 문자열을 key로 삼아서 해당하는 관심사(value) 데이터들을 가져온다
        // ','가 섞여있기 때문에 split은 필수다
        split_list = hashMap.get(userInformation).split(",");
        for (String str : split_list)
        {
            ChoiceKeywordItem item = new ChoiceKeywordItem();
            item.setInterest(str);
            list.add(item);
        }

        // 하단 리사이클러뷰 어댑터 초기화
        // 위에서 새 값을 넣은 리스트로 어댑터를 초기화한다
        adapter = new ChoiceKeywordAdapter(this, list, itemClickListener);
        adapter.setOnItemClickListener((view, pos) ->
        {
            String name = list.get(pos).getInterest();
            str_list.add(name);
        });
        choice_keyword_recyclerview.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mOptionMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_keyword_menu, menu);

        /* 메뉴 버튼 색 바꾸기 */
        MenuItem liveitem = mOptionMenu.findItem(R.id.keyword_ok);
        SpannableString spannableString = new SpannableString(liveitem.getTitle().toString());
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryDark)), 0, spannableString.length(), 0);
        liveitem.setTitle(spannableString);
        return true;
    }

    // 좌상단 뒤로가기 버튼을 누르면 사용자 개인정보(나이, 성별, 지역, 닉네임)을 입력받는 화면으로 돌아간다
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;

            case R.id.keyword_ok:
                // skip을 누른 경우 저장 처리 없이 화면이 꺼지도록 해야 한다
                Toast.makeText(this, "Skip 버튼 클릭", Toast.LENGTH_SHORT).show();
                /* 저장하기 버튼이 추가되어 아래 코드는 저장하기 버튼 클릭 리스너로 옮긴다 */
//                Log.e(TAG, "str_list = " + str_list);
//                if (str_list.size() == 0)
//                {
//                    // 아무것도 선택하지 않으면 여기로 오는지 확인해야 함
//                    Toast.makeText(this, "최소 1개를 선택하셔야 합니다", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//                else
//                {
//                    // 1개라도 선택했다면
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    isConnected = isNetworkConnected(this);
//                    if (!isConnected)
//                    {
//                        // false면 다이얼로그 띄움
//                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                        builder.setMessage("네트워크가 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
//                                .setCancelable(false)
//                                .setPositiveButton("예", new DialogInterface.OnClickListener()
//                                {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which)
//                                    {
//                                        dialog.dismiss();
//                                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                                        startActivity(intent);
//                                    }
//                                }).show();
//                    }
//                    else
//                    {
//                        // true면 로직 이어서 수행
//                        editor.putString("user_category", str_list.toString());
//                        editor.apply();
//                        registerUserInterest();
//                    }
//                    return true;
//                }
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

    /* 나이, 성별 상관없이 모든 관심사들을 가져오는 메서드 */
    private void getAllInterest()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getAllInterest("all");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    interestParsing(result);
//                    test(result);
                    Log.e(TAG, "모든 관심사들 가져오기 성공 : " + result);
                }
                else
                {
                    Log.e(TAG, "모든 관심사들 가져오기 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "모든 관심사 가져오기 에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 모든 관심사를 받은 후 파싱해서 HashMap에 저장하는 메서드 */
    private void interestParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            // 여기서 쓰이는 JSON은 for문으로 돌리지 말고 index로 값을 뽑아와야 한다
            user_10_man = jsonArray.getJSONObject(0);
            user_10_woman = jsonArray.getJSONObject(1);
            user_20_man = jsonArray.getJSONObject(2);
            user_20_woman = jsonArray.getJSONObject(3);
            user_30_man = jsonArray.getJSONObject(4);
            user_30_woman = jsonArray.getJSONObject(5);
            user_40_man = jsonArray.getJSONObject(6);
            user_40_woman = jsonArray.getJSONObject(7);
            user_50_man = jsonArray.getJSONObject(8);
            user_50_woman = jsonArray.getJSONObject(9);
            user_60_man = jsonArray.getJSONObject(10);
            user_60_woman = jsonArray.getJSONObject(11);
            user_70_man = jsonArray.getJSONObject(12);
            user_70_woman = jsonArray.getJSONObject(13);
            nothing = jsonArray.getJSONObject(14);
            /* 값 다 가져오는 건 확인했고 이제 Hashmap에 넣는다
             * 왜냐면 다이얼로그 wheel을 통해 나이, 성별을 받아서 합친 다음 String 변수(20대,남자) 형태로 만드는데 이것을 key로 활용해서
             * hashmap에서 key에 해당하는 value를 뽑아온 다음, 뽑은 값을 list에 담아 리사이클러뷰에 보여주기 위함이다 */
            hashMap.put("10대,남자", user_10_man.getString("10대,남자"));
            hashMap.put("10대,여자", user_10_woman.getString("10대,여자"));
            hashMap.put("20대,남자", user_20_man.getString("20대,남자"));
            hashMap.put("20대,여자", user_20_woman.getString("20대,여자"));
            hashMap.put("30대,남자", user_30_man.getString("30대,남자"));
            hashMap.put("30대,여자", user_30_woman.getString("30대,여자"));
            hashMap.put("40대,남자", user_40_man.getString("40대,남자"));
            hashMap.put("40대,여자", user_40_woman.getString("40대,여자"));
            hashMap.put("50대,남자", user_50_man.getString("50대,남자"));
            hashMap.put("50대,여자", user_50_woman.getString("50대,여자"));
            hashMap.put("60대,남자", user_60_man.getString("60대,남자"));
            hashMap.put("60대,여자", user_60_woman.getString("60대,여자"));
            hashMap.put("70대,남자", user_70_man.getString("70대,남자"));
            hashMap.put("70대,여자", user_70_woman.getString("70대,여자"));
            hashMap.put("기본정보없음", nothing.getString(","));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* 유저가 성별, 나이를 입력하기 전에 보여줄 관심사들을 리사이클러뷰에 set */
        split_list = hashMap.get("기본정보없음").split(",");
        for (String str : split_list)
        {
            ChoiceKeywordItem item = new ChoiceKeywordItem();
            item.setInterest(str);
            list.add(item);
        }

        adapter = new ChoiceKeywordAdapter(this, list, itemClickListener);
        adapter.setOnItemClickListener((view, pos) ->
        {
            String name = list.get(pos).getInterest();
            str_list.add(name);
        });
        choice_keyword_recyclerview.setAdapter(adapter);
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