package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    RecyclerView push_question_recyclerview;
    PushQuestionAdapter adapter;
    PushQuestionAdapter.ItemClickListener itemClickListener;
    List<PushQuestionItem> list;
    // 서버로 보낼 키워드, 유저 이메일
    String keyword_1, keyword_2, email;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_question);

        Logger.addLogAdapter(new AndroidLogAdapter());

        Intent intent = getIntent();
        String age = intent.getStringExtra("age");
        String gender = intent.getStringExtra("gender");
        String area = intent.getStringExtra("area");
        Log.e(TAG, "나이 : " + age + "살, 성별 : " + gender + ", 사는 지역 : " + area);

        push_question_recyclerview = findViewById(R.id.push_question_recycler);
        push_question_recyclerview.setHasFixedSize(true);
        push_question_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        PushQuestionItem item = new PushQuestionItem("아이 낳는 것도 키우는 것도 걱정이시죠?");                     // 아기, 어린이
        PushQuestionItem item2 = new PushQuestionItem("몸이 불편해서 곤란하신가요?");                              // 장애인, 지원
        PushQuestionItem item3 = new PushQuestionItem("대학교 등록금이 걱정이신가요?");                            // 학생, 청년
        PushQuestionItem item4 = new PushQuestionItem("저소득층을 위한 교육비 지원 정책이 궁금하신가요?");           // 저소득층, 지원
        PushQuestionItem item5 = new PushQuestionItem("어르신들을 위한 건강검진 놓치고 있지 않으신가요?");           // 중장년, 노인
        PushQuestionItem item6 = new PushQuestionItem("문화활동으로 평소 쌓인 스트레스를 해소하는 건 어떠신가요?");   // 문화, 생활
        PushQuestionItem item7 = new PushQuestionItem("취업 관련 교육비를 지원해주는 정책을 찾고 계신가요?");         // 취업, 창업
        PushQuestionItem item8 = new PushQuestionItem("청소년 대상의 예방접종 정책을 찾고 계신가요?");               // 학생, 청년
        PushQuestionItem item9 = new PushQuestionItem("임신·출산 관련 문제로 고민중이신가요?");                     // 육아, 임신
        PushQuestionItem item10 = new PushQuestionItem("하교 후, 혼자 있는 아이가 걱정되시나요?");                  // 아기, 어린이
        PushQuestionItem item11 = new PushQuestionItem("경제적 어려움을 겪고 계신가요?");                          // 저소득층, 지원
        // 리사이클러뷰에 하드코딩된 질문지 세팅
        list.add(item);
        list.add(item2);
        list.add(item3);
        list.add(item4);
        list.add(item5);
        list.add(item6);
        list.add(item7);
        list.add(item8);
        list.add(item9);
        list.add(item10);
        list.add(item11);
        adapter = new PushQuestionAdapter(PushQuestionActivity.this, list, itemClickListener);
        // 아이템(질문지) 클릭 시 질문 별 키워드 추출
        adapter.setOnItemClickListener((view, pos) -> {
            if (pos == 0)
            {
                keyword_1 = "아기";
                keyword_2 = "어린이";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 1)
            {
                keyword_1 = "장애인";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 2)
            {
                keyword_1 = "학생";
                keyword_2 = "청년";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 3)
            {
                keyword_1 = "저소득층";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 4)
            {
                keyword_1 = "중장년";
                keyword_2 = "노인";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 5)
            {
                keyword_1 = "문화";
                keyword_2 = "생활";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 6)
            {
                keyword_1 = "취업";
                keyword_2 = "창업";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 7)
            {
                keyword_1 = "학생";
                keyword_2 = "청년";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 8)
            {
                keyword_1 = "육아";
                keyword_2 = "임신";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 9)
            {
                keyword_1 = "아기";
                keyword_2 = "어린이";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
            else if (pos == 10)
            {
                keyword_1 = "저소득층";
                keyword_2 = "지원";
                Log.e(TAG, "키워드1 = " + keyword_1 + ", 키워드2 = " + keyword_2);
                pushQuestion();
            }
        });
        push_question_recyclerview.setAdapter(adapter);
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
                    Logger.e(response.body());
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