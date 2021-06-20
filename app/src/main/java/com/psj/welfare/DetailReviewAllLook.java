package com.psj.welfare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class DetailReviewAllLook extends AppCompatActivity {

    private final String TAG = DetailReviewAllLook.class.getSimpleName();

    ConstraintLayout title_layout,filter_layout; //타이틀 레이아웃, 리뷰 필터 레이아웃
    ImageButton back_btn,filter_icon; //뒤로가기 버튼, 필터링 버튼
    TextView benefit_title,review_count,filter_text; //혜택명, 리뷰 갯 수, 필터 텍스트

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView allreview_recycler; //리사이클러뷰 선언
    private DetailReviewAllAdapter allreview_adapter; //아답터 연결
    private RecyclerView.LayoutManager allreview_layoutManager; //레이아웃 매니저
    private ArrayList<DetailReviewData> allreviewList; //리뷰 2개 보여주기 데이터

    boolean being_id; //혜택 id값이 존재 하는지
    String welf_id; //혜택 아이디 값
    String welf_name; //혜택 명

    boolean being_logout; //로그인 했는지 여부 확인하기
    String SessionId = null; //세션 값
    String token = null; //토큰 값


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_review_all_look);

        //자바 변수와 xml 변수 연결
        init();

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //로그인 했는지 여부 확인
        being_loging();

        //인텐트로 받아온 welf_id값
        being_intent();

        //서버로부터 리뷰 데이터 가져오기
        LoadReview();

        //뒤로가기 버튼
        back_btn.setOnClickListener(v->{
            finish();
        });

        //리뷰 필터 아이콘
        filter_icon.setOnClickListener(v -> {

        });
    }


    //로그인 했는지 여부 확인
    private void being_loging() {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", false); //로그인 했는지 여부 확인하기
        SessionId = null;
        token = null;
        if (!being_logout) { //로그인 했다면
            SessionId = app_pref.getString("sessionId", ""); //세션값 받아오기
            token = app_pref.getString("token", ""); //토큰값 받아오기
        }
    }


    //인텐트로 받아온 welf_id값
    private void being_intent() {
        welf_id = ""; //혜택 아이디 값
        Intent intent = getIntent();
        being_id = intent.getBooleanExtra("being_id", false); //혜택 데이터가 있는지
        if (being_id) {
            welf_id = intent.getStringExtra("welf_id"); //혜택id
            welf_name = intent.getStringExtra("welf_name"); //혜택명
        }
    }


    //서버로부터 리뷰 데이터 가져오기
    private void LoadReview() {

        //ApiInterfaceTest apiInterfaceTest = ApiClient.getApiClient().create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient().create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        Call<String> call = apiInterfaceTest.ReviewAllLook(token,welf_id); //인터페이스에서 사용할 메소드 선언
        call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback

                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    responseParse(result);
                }
                else
                {
                    Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
            }
        });



    }

    //서버로부터 받아온 유튜브데이터 파싱
    private void responseParse(String result) {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                String id = inner_json.getString("id");
                String writer = inner_json.getString("writer");
                String content = inner_json.getString("content");
                int star_count = Integer.parseInt(inner_json.getString("star_count"));
                String difficulty_level = inner_json.getString("difficulty_level");
                String satisfaction = inner_json.getString("satisfaction");
                String create_date = inner_json.getString("create_date");

//                Log.e(TAG,"id : " + id);
//                Log.e(TAG,"writer : " + writer);
//                Log.e(TAG,"content : " + content);

                DetailReviewData ReviewData = new DetailReviewData();
//                ReviewData.setReview_id(review_id);
//                ReviewData.setLogin_id(login_id);
                ReviewData.setNickName(writer);
                ReviewData.setContent(content);
                float star = (float)star_count;
                ReviewData.setStar_count(star);
                ReviewData.setDifficulty_level(difficulty_level);
                ReviewData.setSatisfaction(satisfaction);
                ReviewData.setCreate_date(create_date);

                allreviewList.add(ReviewData);
                allreview_adapter.notifyDataSetChanged();

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    //자바 변수와 xml 변수 연결
    private void init() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바(상태표시줄) 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바(상태표시줄) 배경 흰색으로 설정

        title_layout = findViewById(R.id.title_layout); //타이틀 레이아웃
        filter_layout = findViewById(R.id.filter_layout); //리뷰 필터 레이아웃
        back_btn = findViewById(R.id.back_btn); //뒤로가기 버튼
        filter_icon = findViewById(R.id.filter_icon); //필터링 버튼
        benefit_title = findViewById(R.id.benefit_title); //혜택명
        review_count = findViewById(R.id.review_count); //리뷰 갯 수
        filter_text = findViewById(R.id.filter_text); //필터 텍스트

        //리사이클러뷰 사용하기 위한 변수 연결
        allreview_recycler = findViewById(R.id.all_review_recyclerview); //리사이클러뷰 연결
        allreview_recycler.setHasFixedSize(true); //setHasFixedSize(true)는 리사이클러뷰 안 아이템들의 크기를 가변적으로 하지 않고 고정으로 한다는 것
        allreview_layoutManager = new LinearLayoutManager(this); //리사이클러뷰의 레이아웃을 Linear 방식으로 한다는 것
        allreview_recycler.setLayoutManager(allreview_layoutManager); //리사이클러뷰의 레이아웃을 정함
        //recyclerView.setLayoutManager(new LinearLayoutManager(this)); 위 두줄을 이렇게 한줄로 쓸 수도 있다
        allreviewList = new ArrayList<>(); // 강의 데이터를 담을 어레이 리스트 (어댑터 쪽으로)

        allreview_adapter = new DetailReviewAllAdapter(allreviewList, this); //아답터 연결(어레이리스트와 뷰 연결)
        allreview_recycler.setAdapter(allreview_adapter); //리사이클러뷰에 아답터 연결
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void SetSize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(DetailReviewAllLook.this);
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        title_layout.getLayoutParams().height = size.y/14; //타이틀 레이아웃
        filter_layout.getLayoutParams().height = size.y/16; //리뷰 필터 레이아웃

        back_btn.getLayoutParams().width = size.x /16; //뒤로 가기 버튼
        back_btn.getLayoutParams().height = size.x /16; //뒤로 가기 버튼
        filter_icon.getLayoutParams().width = size.x /20; //필터링 버튼
        filter_icon.getLayoutParams().height = size.x /20; //필터링 버튼

        benefit_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
        review_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //리뷰 수
        filter_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //필터 텍스트

        allreview_recycler.setPadding(size.x / 30, size.x / 30, size.x / 30, size.x / 30); //레이아웃 패딩값 적용
    }
}