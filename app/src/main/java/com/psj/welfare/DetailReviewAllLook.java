package com.psj.welfare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailReviewAllLook extends AppCompatActivity {

    private final String TAG = DetailReviewAllLook.class.getSimpleName();

    private ConstraintLayout filter_button_layout; //리뷰 필터 버튼쪽 레이아웃
    private ConstraintLayout title_layout,filter_layout; //타이틀 레이아웃, 리뷰 필터 레이아웃
    private ImageButton back_btn,filter_icon; //뒤로가기 버튼, 필터링 버튼
    private TextView benefit_title,review_count,filter_text; //혜택명, 리뷰 갯 수, 필터 텍스트
    private Point size; //디스플레이 크기를 담을 변수

    private ProgressDialog dialog; //서버에서 데이터 받아올 동안 보여줄 프로그래스 바

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView allreview_recycler; //리사이클러뷰 선언
    private DetailReviewAllAdapter allreview_adapter; //아답터 연결
    private RecyclerView.LayoutManager allreview_layoutManager; //레이아웃 매니저
    private ArrayList<DetailReviewData> allreviewList; //리뷰 2개 보여주기 데이터

    private boolean being_id; //혜택 id값이 존재 하는지
    private String welf_id; //혜택 아이디 값
    private String welf_name; //혜택 명


    private String token = null; //토큰 값
//    private String status = null; //리뷰 삭제후 반환값
    private String message; //리뷰 삭제후 받을 메세지
    // API 호출 후 서버 응답코드
    private int status_code;

    private String filter = "newest";

    //쉐어드 싱글톤
    private SharedSingleton sharedSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_review_all_look);

        //쉐어드 싱글톤 사용
        sharedSingleton = SharedSingleton.getInstance(this);
        token = sharedSingleton.getToken(); //토큰 값

        //자바 변수와 xml 변수 연결
        init();

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();


        //인텐트로 받아온 welf_id값
        being_intent();

        //서버로부터 리뷰 데이터 가져오기
        LoadReview();

        //뒤로가기 버튼
        back_btn.setOnClickListener(v->{
            // 뒤로가기 버튼 누르면 강제로 리뷰 상세보기 화면으로 넘기기
            // 데이터 갱신하기 위함, DetailTabLayoutActivity 화면에서 생명주기로 데이터 다시받아오면 데이터가 겹쳐짐
            goto_review();
//            finish();
        });

        //리뷰 필터 아이콘
        filter_button_layout.setOnClickListener(v -> {
            setReview_filter(); //리뷰 필터
        });
        filter_icon.setOnClickListener(v -> {
            setReview_filter(); //리뷰 필터
        });

        allreview_adapter.setOnItemClickListener(new DetailReviewAllAdapter.ReviewAllClickListener() {
            @Override
            public void repairClick(View v, int pos) { //리뷰 수정 버튼
//                Toast.makeText(DetailReviewAllLook.this,"수정",Toast.LENGTH_SHORT).show();
//                Log.e(TAG,"수정");
                Intent intent = new Intent(DetailReviewAllLook.this, DetailReviewWrite.class);

                intent.putExtra("welf_id", welf_id);
                intent.putExtra("welf_name", welf_name);
                intent.putExtra("review_id", allreviewList.get(pos).getReview_id());

                intent.putExtra("Star_count", allreviewList.get(pos).getStar_count());
                intent.putExtra("satisfaction", allreviewList.get(pos).getSatisfaction());
                intent.putExtra("difficulty_level", allreviewList.get(pos).getDifficulty_level());
                intent.putExtra("content", allreviewList.get(pos).getContent());

//                Log.e(TAG," welf_id : "+ welf_id);
//                Log.e(TAG," welf_name : "+ welf_name);
//                Log.e(TAG," review_id : "+ allreviewList.get(pos).getReview_id());
//
//                Log.e(TAG," Star_count : "+ allreviewList.get(pos).getStar_count());
//                Log.e(TAG," satisfaction : "+ allreviewList.get(pos).getSatisfaction());
//                Log.e(TAG," difficulty_level : "+ allreviewList.get(pos).getDifficulty_level());
//                Log.e(TAG," content : "+ allreviewList.get(pos).getContent());



                intent.putExtra("review_edit", 100);
                startActivity(intent);
            }

            @Override
            public void DeleteClick(View v, int pos) { //리뷰 삭제 버튼
//                Toast.makeText(DetailReviewAllLook.this,"삭제",Toast.LENGTH_SHORT).show();
//                Log.e(TAG,"삭제");
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DetailReviewAllLook.this);
                builder.setMessage("삭제하신 리뷰는 복구할 수 없어요.\n정말 리뷰를 삭제하시겠어요?")
                        .setPositiveButton("예", (dialog, which) ->
                        {
                            // 리뷰 삭제 메서드 호출
//                            Log.e(TAG,"getReview_id : " + allreviewList.get(pos).getReview_id());
                            removeReview(allreviewList.get(pos).getReview_id(),"true");

                        })
                        .setNegativeButton("아니오", ((dialog, which) ->
                        {
                            Toast.makeText(DetailReviewAllLook.this, "리뷰 삭제를 취소했어요", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }))
                        .show();
            }
        });
    }

    //리뷰 삭제하기
    private void removeReview(int review_id, String is_remove)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", review_id);
            jsonObject.put("is_remove", is_remove);

//            Log.e(TAG, "삭제 api로 보낼 JSON 만들어진 것 테스트 : " + jsonObject.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        Call<String> call = apiInterface.deleteReview(token, jsonObject.toString());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        status_code = jsonObject.getInt("status_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(status_code == 200){
                        removeResponseParse(response.body());
                    } else {
                        Toast.makeText(DetailReviewAllLook.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                    }


                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 삭제 에러 : " + t.getMessage());
            }
        });
    }

    //리뷰 삭제후 반환값 받기
    private void removeResponseParse(String result)
    {
        Toast.makeText(DetailReviewAllLook.this, "리뷰가 성공적으로 삭제됐어요", Toast.LENGTH_SHORT).show();
        LoadReview();
    }

    //인텐트로 받아온 welf_id값
    private void being_intent() {
        welf_id = ""; //혜택 아이디 값
        Intent intent = getIntent();
        being_id = intent.getBooleanExtra("being_id", false); //혜택 데이터가 있는지
        if (being_id) {
            welf_id = intent.getStringExtra("welf_id"); //혜택id
            welf_name = intent.getStringExtra("welf_name"); //혜택명
            benefit_title.setText(welf_name);
        }
    }


    //서버로부터 리뷰 데이터 가져오기(조회)
    private void LoadReview() {
        //서버로부터 데이터를 받아오는데 걸리는 시간동안 보여줄 프로그래스 바
        dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false); //"false"면 다이얼로그 나올 때 dismiss 띄우기 전까지 안사라짐
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        allreviewList.clear();
        //ApiInterfaceTest apiInterfaceTest = ApiClient.getApiClient().create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient().create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        Call<String> call = apiInterfaceTest.ReviewAllLook(token,filter,welf_id); //인터페이스에서 사용할 메소드 선언
        call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback

                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
//                    Log.e(TAG,"리뷰 데이터 받기 : " + result);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        status_code = jsonObject.getInt("status_code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(status_code == 200){
                        GsonResponseParse(result);
                    } else {
                        Toast.makeText(DetailReviewAllLook.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                    }

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


    //Gson으로 파싱
    private void GsonResponseParse(String result){
//        Log.e(TAG,"result : " + result);

        Gson gson = new Gson();
        try {
            JSONObject jsonObject = new JSONObject(result);

            review_count.setText("사용자 리뷰 " + jsonObject.getString("total_num") + "개");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++){
                //DetailReviewDataGson.class 타입으로 변경 (타입을 String, int, jsonArray등 일반 데이터 타입으로 받을 수도 있다
                DetailReviewData data = gson.fromJson(jsonArray.getJSONObject(i).toString(),DetailReviewData.class);
//                Log.e(TAG,"data.getNickName : " + data.getNickName());
//                Log.e(TAG,"data.getStar_count : " + data.getStar_count());
//                Log.e(TAG,"data.getIs_me : " + data.getIs_me());

                allreviewList.add(data);
                allreview_adapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    //리뷰 필터
    private void setReview_filter(){
        AlertDialog.Builder TutorialDialog = new AlertDialog.Builder(DetailReviewAllLook.this);
        View dialogview = getLayoutInflater().inflate(R.layout.custom_reviewfilter_dialog,null); //다이얼로그의 xml뷰 담기

        ConstraintLayout reviewfilter_dialog = dialogview.findViewById(R.id.reviewfilter_dialog); //다이얼로그 전체 크기
        TextView newest = dialogview.findViewById(R.id.newest); //최신 순
        TextView high_star = dialogview.findViewById(R.id.high_star); //별점 높은 순
        TextView low_star = dialogview.findViewById(R.id.low_star); //별점 낮은 순

        TutorialDialog.setView(dialogview); //alertdialog에 view 넣기
        final AlertDialog alertDialog = TutorialDialog.create(); //다이얼로그 객체로 만들기
        alertDialog.show(); //다이얼로그 보여주기

        reviewfilter_dialog.getLayoutParams().height = (int) (size.y*0.3); //다이얼로그 전체 레이아웃 동적으로 크기
        newest.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.04));
        high_star.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.04));
        low_star.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(size.x * 0.04));

        newest.setOnClickListener(v->{ //리뷰 최신순
            filter = "newest";
            allreviewList.clear();
            //서버로부터 리뷰 데이터 가져오기
            LoadReview();
            alertDialog.dismiss();
            filter_text.setText("최신순");
        });

        high_star.setOnClickListener(v->{ //리뷰 최신순
            filter = "high_star";
            allreviewList.clear();
            //서버로부터 리뷰 데이터 가져오기
            LoadReview();
            alertDialog.dismiss();
            filter_text.setText("별점 높은 순");
        });

        low_star.setOnClickListener(v->{ //리뷰 최신순
            filter = "low_star";
            allreviewList.clear();
            //서버로부터 리뷰 데이터 가져오기
            LoadReview();
            alertDialog.dismiss();
            filter_text.setText("별점 낮은 순");
        });

    }


    //자바 변수와 xml 변수 연결
    private void init() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);         // 상태바(상태표시줄) 글자색 검정색으로 바꾸기
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorMainWhite));    // 상태바(상태표시줄) 배경 흰색으로 설정

        filter_button_layout = findViewById(R.id.filter_button_layout); //리뷰 필터 버튼쪽 레이아웃
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
        size = screen.getScreenSize(DetailReviewAllLook.this);
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        title_layout.getLayoutParams().height = size.y/14; //타이틀 레이아웃
        filter_layout.getLayoutParams().height = size.y/14; //리뷰 필터 레이아웃

        back_btn.getLayoutParams().width = size.x /16; //뒤로 가기 버튼
        back_btn.getLayoutParams().height = size.x /16; //뒤로 가기 버튼
        filter_icon.getLayoutParams().width = size.x /20; //필터링 버튼
        filter_icon.getLayoutParams().height = size.x /20; //필터링 버튼

        benefit_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
        review_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //리뷰 수
        filter_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //필터 텍스트
        filter_text.setPadding(0,0,(int)(size.x*0.05),0);

        allreview_recycler.setPadding(size.x / 30, (int)(size.x*0.01), size.x / 30, size.x / 30); //레이아웃 패딩값 적용
    }

    //뒤로가기 눌렀을 경우 리뷰 상세보기 화면으로 가기
    private void goto_review(){
        Intent intent = new Intent(DetailReviewAllLook.this, DetailTabLayoutActivity.class);
        //STACK 정리, 기존의 상세보기 페이지가 stack에 맨위에 있으면 기존 액티비티는 종료하고 새로운 액티비티를 띄운다
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("welf_name",welf_name);
        intent.putExtra("welf_id",welf_id);
        intent.putExtra("being_id",true);
        intent.putExtra("review_write",true);
        startActivity(intent);
        finish();
    }

    //뒤로가기 누르면 상세보기 페이지로 넘어감
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        // 뒤로가기 버튼 누르면 강제로 리뷰 상세보기 화면으로 넘기기
        // 데이터 갱신하기 위함, DetailTabLayoutActivity 화면에서 생명주기로 데이터 다시받아오면 데이터가 겹쳐짐
        goto_review();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        //서버로부터 리뷰 데이터 가져오기
        LoadReview();
    }
}