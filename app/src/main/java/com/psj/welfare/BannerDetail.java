package com.psj.welfare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BannerDetail extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAnalytics analytics; //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수

    private ImageView bannerdetail_image; //배너 이미지
    private ImageButton back_btn; //뒤로가기 버튼
    private ConstraintLayout back_btn_layout; //뒤로가기 버튼 레이아웃
    private TextView banner_title_first; //배너 타이틀 첫벌째 줄
    private TextView banner_title_second; //배너 타이틀 두번째 줄

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView banner_recyclerview; //리사이클러뷰 선언
    private BannerDetailAdapter banner_adapter; //아답터 연결
    private RecyclerView.LayoutManager banner_layoutManager; //레이아웃 매니저
    private ArrayList<BannerDetailData> bannerList; //강의 데이터

    //로그인 관련 변수
    boolean being_logout = false; //로그아웃 했는지 (true면 로그아웃, false면 로그인)
    String SessionId = null; //세션 값
    String token = null; //토큰 값

    private String banner_name; //메인에서 받아온 배너 타이틀
    private String banner_name_api; //api용 배너 타이틀

    private String banner_title; //배너 타이틀
    private String banner_image; //배너 이미지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_detail);


        //인텐트로 받아온 banner_title값
        being_intent();

        //로그인 했는지 여부
        being_loging();

        //초기화 작업
        init();

        //xml크기를 동적으로 변환
        setsize();

        //배너너 데이터 서버에서 받오기
        bannerdata();

        //배너 "자세히보기" 버튼 클릭
        banner_adapter.setBannerClickListener(new BannerDetailAdapter.BannerDetailClick() {
            @Override
            public void bannerdetailClick(View v, int position) {
                Intent intent = new Intent(BannerDetail.this,DetailTabLayoutActivity.class);
                intent.putExtra("welf_id", bannerList.get(position).getBannerId());
                intent.putExtra("being_id",true);
                startActivity(intent);
            }
        });

        //뒤로가기
        back_btn_layout.setOnClickListener(v -> {
            finish();
        });
        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    //    배너너 데이터 서버에서 받오기
    private void bannerdata() {

        //서버 연결전에 프로그래스바 보여주기
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (!being_logout) //로그인 했을 경우
        {
            ApiInterfaceTest apiInterface = ApiClient.getRetrofit().create(ApiInterfaceTest.class);
            Call<String> call = apiInterface.BannerDetail_beingid(token, SessionId, banner_name_api);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body();

//                        Log.e(TAG, "result1 : " + result);
                        responseParse(result);
                    } else {
                        Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
                    }

                    dialog.dismiss(); //서버 연결후에 프로그래스바 숨기기
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
                }
            });


        } else //로그인 안했을 경우
        {
            ApiInterfaceTest apiInterface = ApiClient.getRetrofit().create(ApiInterfaceTest.class);
            Call<String> call = apiInterface.BannerDetail(banner_name_api);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body();

//                        Log.e(TAG, "result2 : " + result);
                        responseParse(result);
                    } else {
                        Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
                    }

                    dialog.dismiss(); //서버 연결후에 프로그래스바 숨기기
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
                }
            });

        }
    }


    //서버로부터 받아온 배너데이터 파싱
    private void responseParse(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
//            Log.e(TAG,"jsonArray.length() : " + jsonArray.length());
//            Log.e(TAG,"message : " + jsonObject.getString("message"));

            JSONObject inner_json = jsonArray.getJSONObject(0);
            JSONArray jsontitle = inner_json.getJSONArray("banner"); //배너 타이틀, 이미지 데이터
            JSONObject title_final = jsontitle.getJSONObject(0);
            banner_title = title_final.getString("banner_name"); //배너 타이틀
            banner_image = title_final.getString("img_url"); //배너 이미지

            //타이틀을 \n기준으로 자른다
            String[] title = banner_title.split("\n");

            banner_title_first.setText(title[0]); //"TOP5"
            banner_title_second.setText(title[1]); //타이틀 내용

            Glide.with(this)
                    .load(banner_image)
                    .into(bannerdetail_image);

            JSONArray jsonitem = inner_json.getJSONArray("welf_data"); //배너 아이템 데이터
            for (int i = 0; i < jsonitem.length(); i++) {
                JSONObject item_final = jsonitem.getJSONObject(i);
//                Log.e(TAG,"item_final : " + item_final);
                String welf_id = item_final.getString("welf_id");
                String welf_name = item_final.getString("welf_name");
                String welf_tag = item_final.getString("welf_tag");
                String welf_text = item_final.getString("welf_text");


                BannerDetailData banneritem = new BannerDetailData();
                banneritem.setBannerId(welf_id);
                banneritem.setBannerTitle(welf_name);
                banneritem.setBannerTag(welf_tag);
                banneritem.setBannerContents(welf_text);

                bannerList.add(banneritem);
                banner_adapter.notifyDataSetChanged();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //로그인 했는지 여부 확인
    private void being_loging() {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", true); //로그인 했는지 여부 확인하기

        if (!being_logout) { //로그인 했다면
            SessionId = app_pref.getString("sessionId", ""); //세션값 받아오기
            token = app_pref.getString("token", ""); //토큰값 받아오기
        }
    }

    //초기화 작업
    private void init() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바(상태표시줄) 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바(상태표시줄) 배경 흰색으로 설정

        bannerdetail_image = findViewById(R.id.bannerdetail_image); //배너 이미지
        back_btn = findViewById(R.id.back_btn); //뒤로가기 버튼
        back_btn_layout = findViewById(R.id.back_btn_layout); //뒤로가기 버튼 레이아웃
        banner_title_first = findViewById(R.id.banner_title_first); //배너 타이틀 첫벌째 줄
        banner_title_second = findViewById(R.id.banner_title_second); //배너 타이틀 두번째 줄

        banner_recyclerview = findViewById(R.id.banner_recyclerview); //배너 리사이클러뷰
        //배너 데이터 보여주기 위한 리사이클러뷰
        banner_recyclerview.setHasFixedSize(true); //setHasFixedSize(true)는 리사이클러뷰 안 아이템들의 크기를 가변적으로 하지 않고 고정으로 한다는 것
        banner_layoutManager = new LinearLayoutManager(this); //리사이클러뷰의 레이아웃을 Linear 방식으로 한다는 것
        banner_recyclerview.setLayoutManager(banner_layoutManager); //리사이클러뷰의 레이아웃을 정함
        bannerList = new ArrayList<>(); // 강의 데이터를 담을 어레이 리스트 (어댑터 쪽으로)

        banner_adapter = new BannerDetailAdapter(bannerList, this); //아답터 연결(어레이리스트와 뷰 연결)
        banner_recyclerview.setAdapter(banner_adapter); //리사이클러뷰에 아답터 연결
    }


    //인텐트로 받아온 banner_title값
    private void being_intent() {
        if (getIntent().hasExtra("banner_title")) {
            Intent intent = getIntent();
            banner_name = intent.getStringExtra("banner_title"); //혜택 데이터가 있는지, 없는 경우 서버에서 데이터 안받아 오도록
            banner_name_api = banner_name.replace("\n", "-"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
//            Log.e(TAG, "banner_name_api : " + banner_name_api);
        }
    }


    //xml크기를 동적으로 변환
    private void setsize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(BannerDetail.this);

        //배너 이미지
        bannerdetail_image.getLayoutParams().width = (int) (size.x);
        bannerdetail_image.getLayoutParams().height = (int) (size.y * 0.37);
        //뒤로 가기 버튼
        back_btn.getLayoutParams().width = (int) (size.x * 0.1);
        back_btn.getLayoutParams().height = (int) (size.x * 0.1);

        //배너 타이틀
        banner_title_first.setTextSize((int) (size.x * 0.02));
        banner_title_first.setPadding(0,0,0,(int) (size.x * 0.01));
        banner_title_second.setTextSize((int) (size.x * 0.02));
    }
}