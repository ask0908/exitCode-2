package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.PushQuestionItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.PushQuestionAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 3번째 프래그먼트에서 유저가 기본정보를 입력한 후 다음 버튼을 누르면, 유저 정보에 따라 질문지들을 리사이클러뷰로 보여주는 액티비티
* 지금은 기능구현이 먼저기 때문에 더미 데이터로 테스트. 아이템을 누르면 서버로 정보를 보내서 푸시 알림을 받도록 한다 */
public class PushQuestionActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    String age, gender, area;
    RecyclerView push_question_recyclerview;
    PushQuestionAdapter adapter;
    PushQuestionAdapter.ItemClickListener itemClickListener;
    List<PushQuestionItem> list;
    // 서버로 보낼 키워드, 유저 이메일
    String keyword_1, keyword_2, email;

    // 서버에서 받은 질문지, 키워드 2개
    String question, keyword1, keyword2;

    // 복수 선택 시 선택한 질문지의 키워드를 담을 리스트, 질문지 문장을 담을 리스트
    private List<PushQuestionItem> first_keyword_list, second_keyword_list, questions_list;

    Button keyword_send_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_question);

        keyword_send_btn = findViewById(R.id.keyword_send_btn);
        questions_list = new ArrayList<>();
        first_keyword_list = new ArrayList<>();
        second_keyword_list = new ArrayList<>();

        Logger.addLogAdapter(new AndroidLogAdapter());

        Intent intent = getIntent();
        age = intent.getStringExtra("age");
        gender = intent.getStringExtra("gender");
        area = intent.getStringExtra("area");
        Log.e(TAG, "나이 : " + age + "살, 성별 : " + gender + ", 사는 지역 : " + area);
        sendKeyword();

        push_question_recyclerview = findViewById(R.id.push_question_recycler);
        push_question_recyclerview.setHasFixedSize(true);
        push_question_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        // 확인 버튼을 누르면 키워드들을 서버로 전송한다
        keyword_send_btn.setOnClickListener(v -> {
            // 레트로핏 메서드 호출
        });

    }

    /* 유저의 나이, 성별, 거주지를 입력 후 확인을 누르면 질문지들과 키워드들을 가져오는 메서드 */
    void sendKeyword()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.sendKeyword("ddd", age, gender, area, "kakao", "android@test.com");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "response = " + response.body());
                    String detail = response.body();
                    jsonParsing(detail);
                }
                else
                {
                    Log.e(TAG, "실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e("sendKeyword() 에러", t.getMessage());
            }
        });
    }

    /* 서버에서 넘어온 질문지, 키워드 값들을 파싱하는 메서드 */
    private void jsonParsing(String detail)
    {
        List<PushQuestionItem> list = new ArrayList<>();
        List<PushQuestionItem> keyword1_list = new ArrayList<>();
        List<PushQuestionItem> keyword2_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(detail);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                Log.e(TAG, "jsonArray = " + jsonArray);
                JSONObject jsonobject_detail = jsonArray.getJSONObject(i);
                question = jsonobject_detail.getString("question");
                keyword1 = jsonobject_detail.getString("keyword_1");
                keyword2 = jsonobject_detail.getString("keyword_2");
                PushQuestionItem item = new PushQuestionItem();
                item.setQuestion(question);
                PushQuestionItem keyword1_item = new PushQuestionItem();
                keyword1_item.setKeyword_1(keyword1);
                PushQuestionItem keyword2_item = new PushQuestionItem();
                keyword2_item.setKeyword_2(keyword2);

                list.add(item);
                keyword1_list.add(keyword1_item);
                keyword2_list.add(keyword2_item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Logger.e("list = " + list);
        adapter = new PushQuestionAdapter(PushQuestionActivity.this, list, itemClickListener);
        push_question_recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener((view, pos) -> {
            if (pos == 0)
            {
                keyword_1 = "아기";
                keyword_2 = "어린이";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 1)
            {
                keyword_1 = "장애인";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 2)
            {
                keyword_1 = "학생";
                keyword_2 = "청년";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 3)
            {
                keyword_1 = "저소득층";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 4)
            {
                keyword_1 = "중장년";
                keyword_2 = "노인";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 5)
            {
                keyword_1 = "문화";
                keyword_2 = "생활";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 6)
            {
                keyword_1 = "취업";
                keyword_2 = "창업";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 7)
            {
                keyword_1 = "학생";
                keyword_2 = "청년";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 8)
            {
                keyword_1 = "육아";
                keyword_2 = "임신";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 9)
            {
                keyword_1 = "아기";
                keyword_2 = "어린이";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
            else if (pos == 10)
            {
                keyword_1 = "저소득층";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
            }
        });
    }

    /* 서버로 키워드 2개와 이메일을 보내는 함수 */
    void pushQuestion()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String email = "ne0001912@gmail.com";
        Call<String> call = apiInterface.pushQuestion(keyword_1, keyword_2, email);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    // 서버로 키워드 2개와 이메일 보내는 게 성공했을 경우
                    Toast.makeText(PushQuestionActivity.this, "관심사가 성공적으로 등록되었어요!", Toast.LENGTH_SHORT).show();
                    Logger.e("response : " + response.body());
                }
                else
                {
                    // 서버로 전송 실패했을 경우
                    Toast.makeText(PushQuestionActivity.this, "관심사 등록 중 문제가 발생했습니다.\n다시 한 번 시도해 주세요", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "response : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.e("에러 : " + t.getMessage());
            }
        });
    }

}