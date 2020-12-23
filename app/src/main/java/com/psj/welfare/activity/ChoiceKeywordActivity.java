package com.psj.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ChoiceKeywordItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.ChoiceKeywordAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* GetUserInformationActivity에서 사용자가 기본 정보(나이, 성별, 지역, 닉네임)를 입력하고 확인을 누르면 이 곳으로 이동해서 관심있는 키워드를 선택하게 한다 */
public class ChoiceKeywordActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    // 확인 버튼
    private Button choice_complete_btn;
    // 체크박스에 체크하면 그 체크박스의 값들을 담을 변수 (api 문서 갱신되면 바꿔야 할 수 있음)
    String checked_value;
    // 체크된 체크박스를 담아서 확인 버튼을 눌렀을 때 사용자가 하나라도 골랐는지 확인할 때 사용할 리스트
    String age_group, user_gender, interest;
    String[] split_list;

    private RecyclerView choice_keyword_recyclerview;
    private ChoiceKeywordAdapter adapter;
    private ChoiceKeywordAdapter.ItemClickListener itemClickListener;

    List<ChoiceKeywordItem> list;
    // 체크박스 클릭 시 어떤 체크박스를 골랐는지 정보를 담을 리스트
    List<String> str_list;
    String str_server, message;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_keyword);

        list = new ArrayList<>();
        str_list = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        choice_complete_btn = findViewById(R.id.choice_complete_btn);
        choice_keyword_recyclerview = findViewById(R.id.choice_keyword_recyclerview);
        choice_keyword_recyclerview.setHasFixedSize(true);
        choice_keyword_recyclerview.setLayoutManager(new GridLayoutManager(this, 2));

        if (getIntent().hasExtra("age_group") || getIntent().hasExtra("user_gender") || getIntent().hasExtra("interest"))
        {
            Intent intent = getIntent();
            age_group = intent.getStringExtra("age_group");
            user_gender = intent.getStringExtra("user_gender");
            interest = intent.getStringExtra("interest");
        }
        Log.e(TAG, "넘겨받은 age_group = " + age_group + ", user_gender = " + user_gender + ", interest = " + interest);
        split_list = interest.split(",");
        for (String str : split_list)
        {
            Log.e(TAG, "split = " + str);
            ChoiceKeywordItem item = new ChoiceKeywordItem();
            item.setInterest(str);
            list.add(item);
        }
        Log.e(TAG, "list = " + list);
        adapter = new ChoiceKeywordAdapter(this, list, itemClickListener);
        adapter.setOnItemClickListener(new ChoiceKeywordAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int pos)
            {
                String name = list.get(pos).getInterest();
                str_list.add(name);
            }
        });
        choice_keyword_recyclerview.setAdapter(adapter);

        Toolbar choice_toolbar = findViewById(R.id.choice_toolbar);
        setSupportActionBar(choice_toolbar);
        getSupportActionBar().setTitle("관심 카테고리 설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 확인 버튼을 누르면 유저가 선택한 카테고리들을 서버에 저장하고 액티비티를 이동한다
        choice_complete_btn.setOnClickListener(v -> {
            Log.e(TAG, "str_list = " + str_list);
            editor.putString("user_category", str_list.toString());
            registerUserInterest();
        });

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
        Call<String> call = apiInterface.registerUserInterest(token, "interest", str_server);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
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
        Intent intent = new Intent(ChoiceKeywordActivity.this, MainTabLayoutActivity.class);
        startActivity(intent);
        finish();
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
        }
        return super.onOptionsItemSelected(item);
    }
}