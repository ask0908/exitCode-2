package com.psj.welfare.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.ApiInterfaceTest;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.YoutubeMoreAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.data.HorizontalYoutubeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YoutubeMoreActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAnalytics analytics; //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수

    private ImageButton back_btn; //뒤로가기 버튼
    private TextView YoutubeMore_count; //혜택이 총 몇개 있는지

    private boolean Islogin; //로그아웃 했는지
    private String SessionId; //세션 값
    private String token; //토큰 값

    String youtube_upload_date; //유튜버 업로드 날짜
    String youtube_name; //유튜버 이름
    String youtube_id; //유튜브 아이디
    String youtube_title; //유튜브 타이틀
    String youtube_thumbnail; //유튜브 썸네일
    String youtube_videoId; //유튜브 비디오id


    int page = 1; //페이징 하기 위한 변수
    private int total_page; //유튜브 데이터 총 페이지(서버에서 보내주는 데이터의 마지막 페이지 값)
    private String total_num; //서버에서 보내주는 혜택 총 갯수

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView recyclerView; //리사이클러뷰 선언
    private YoutubeMoreAdapter youtubemore_adapter; //아답터 연결
    private RecyclerView.LayoutManager youtube_layoutManager; //레이아웃 매니저
    private ArrayList<HorizontalYoutubeItem> youtubemore_List; //강의 데이터
    private YoutubeMoreAdapter.YoutubeMoreClick youtubeclick; //유튜브 목록 클릭 리스너

    //쉐어드 싱글톤
    private SharedSingleton sharedSingleton;

    // API 호출 후 서버 응답코드
    private int status_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(YoutubeMoreActivity.this);
        setContentView(R.layout.activity_youtube_more);

        //쉐어드 싱글톤 사용
        sharedSingleton = SharedSingleton.getInstance(this);

        //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수 선언
        if (YoutubeActivity.class != null) {
            analytics = FirebaseAnalytics.getInstance(this);
        }

        //로그인 했는지 여부
        being_loging();

        //변수 초기화
        init();

        //유튜브 데이터 서버에서 받아오기
        youtubemoredata(1);

        //리사이클러뷰가 마지막에 도달하면 이벤트 발생
        youtubeMoreScrollListener();

        //뒤로가기 버튼
        back_btn.setOnClickListener(v -> {
            finish(); //현재 액티비티 종료
            //액티비티 스택을 추적하고싶을경우. 이런 경우엔 새액티비티를 시작할때마다 intent에 FLAG_ACTIVITY_REORDER_TO_FRONT 나 FLAG_ACTIVITY_PREVIOUS_IS_TOP 같은 플래그를 줄 수 있습니다.
        });

        //영상 보러가기
        youtubemore_adapter.setYoutubeClickListener(new YoutubeMoreAdapter.YoutubeMoreClick(){
            @Override
            public void youtubemoreClick(View v, int position) {
                Intent intent = new Intent(YoutubeMoreActivity.this,YoutubeActivity.class);
                HorizontalYoutubeItem item = new HorizontalYoutubeItem();

                item.setYoutube_id(youtubemore_List.get(position).getYoutube_id());
                item.setYoutube_name(youtubemore_List.get(position).getYoutube_name());
                item.setYoutube_thumbnail(youtubemore_List.get(position).getYoutube_thumbnail());
                item.setYoutube_videoId(youtubemore_List.get(position).getYoutube_videoId());
                item.setYoutube_id(youtubemore_List.get(position).getYoutube_id());
                intent.putExtra("youtube_information", item);

                //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                startActivity(intent);
            }
        });
    }

    //리사이클러뷰가 마지막에 도달하면 이벤트 발생
    private void youtubeMoreScrollListener() {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int totalItemcount = layoutManager.getItemCount(); //리사이클러뷰가 가진 총 아이템 갯수
                //indLastCompletelyVisibleItemPosition 메서드이다. 얘는 마지막 아이템이 완전히 보일때 해당 Position을 알려준다.
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition(); //Position을 반환하기 때문에 0부터 시작이 된다
                //ex) 1개의 아이템을 가지고 있다. Position은 0으로 나올 것이고, totalItemCount 는 1로 나오게 된다

                if (lastVisible >= totalItemcount - 1 && page < total_page) {
                    page++;
                    youtubemoredata(page); //페이지값을 서버에 보내준다

                }
            }
        };
        //리사이클러뷰가 마지막에 도달했을 때 이벤트 발생시킴
        recyclerView.addOnScrollListener(onScrollListener);
    }

    //유튜브 데이터 서버에서 받아오기
    public void youtubemoredata(int page) {

        //서버 연결전에 프로그래스바 보여주기
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false); //"false"면 다이얼로그 나올 때 dismiss 띄우기 전까지 안사라짐
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        if (Islogin) //로그인 했을 경우
        {
            ApiInterfaceTest apiInterface = ApiClient.getRetrofit().create(ApiInterfaceTest.class);
            Call<String> call = apiInterface.YoutubeNonSelect_beingid(token, SessionId, "show_list", String.valueOf(page));
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body();
//                        Log.e(TAG, "result1 : " + result);

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            status_code = jsonObject.getInt("status_code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //서버에서 정상적으로 값을 받았다면
                        if(status_code == 200){
                            responseParse(result);
                        } else {
                            Toast.makeText(YoutubeMoreActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                        }



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
            Call<String> call = apiInterface.YoutubeNonSelect("show_list", String.valueOf(page));
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String result = response.body();
//                        Log.e(TAG, "result2 : " + result);

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result);
                            status_code = jsonObject.getInt("status_code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //서버에서 정상적으로 값을 받았다면
                        if(status_code == 200){
                            responseParse(result);
                        } else {
                            Toast.makeText(YoutubeMoreActivity.this,"오류가 발생했습니다",Toast.LENGTH_SHORT).show();
                        }

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


    //서버로부터 받아온 유튜브데이터 파싱
    private void responseParse(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            total_page = Integer.parseInt(jsonObject.getString("total_page")); //서버에서 보내주는 마지막 페이지 값
            total_num = jsonObject.getString("total_num"); //서버에서 보내주는 혜택 총 갯수
            YoutubeMore_count.setText("총 " + total_num + "개의 혜택 영상");
//            Log.e(TAG,"jsonArray.length() : " + jsonArray.length());
//            Log.e(TAG,"message : " + jsonObject.getString("message"));

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject inner_json = jsonArray.getJSONObject(i);

                youtube_id = inner_json.getString("id");
                youtube_title = inner_json.getString("title");
                youtube_thumbnail = inner_json.getString("thumbnail");
                youtube_videoId = inner_json.getString("videoId");
                youtube_name = inner_json.getString("name");
                youtube_upload_date = inner_json.getString("upload_date");

                HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                item.setYoutube_id(youtube_id);
                item.setYoutube_title(youtube_title);
                item.setYoutube_thumbnail(youtube_thumbnail);
                item.setYoutube_videoId(youtube_videoId);
                item.setYoutube_name(youtube_name);
                item.setYoutube_upload_date(youtube_upload_date);
                youtubemore_List.add(item);
                youtubemore_adapter.notifyDataSetChanged();

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //변수 초기화
    private void init() {
        back_btn = findViewById(R.id.back_btn); //뒤로가기 버튼
        YoutubeMore_count = findViewById(R.id.YoutubeMore_count); //혜택이 총 몇개 있는지
        recyclerView = findViewById(R.id.YoutubeMore_recyclerview); //리사이클러뷰 연결

        //유튜브 다른 영상 목록 보여주기 위한 리사이클러뷰
        recyclerView.setHasFixedSize(true); //setHasFixedSize(true)는 리사이클러뷰 안 아이템들의 크기를 가변적으로 하지 않고 고정으로 한다는 것
        youtube_layoutManager = new LinearLayoutManager(this); //리사이클러뷰의 레이아웃을 Linear 방식으로 한다는 것
        recyclerView.setLayoutManager(youtube_layoutManager); //리사이클러뷰의 레이아웃을 정함
        youtubemore_List = new ArrayList<>(); // 강의 데이터를 담을 어레이 리스트 (어댑터 쪽으로)

        youtubemore_adapter = new YoutubeMoreAdapter(youtubemore_List, this, youtubeclick); //아답터 연결(어레이리스트와 뷰 연결)
        recyclerView.setAdapter(youtubemore_adapter); //리사이클러뷰에 아답터 연결

        //xml크기를 동적으로 변환
        setsize();
    }


    //로그인 했는지 여부 확인
    private void being_loging() {
        Islogin = sharedSingleton.getBooleanLogin();
        SessionId = sharedSingleton.getSessionId();
        token = sharedSingleton.getToken();
    }



    //xml크기를 동적으로 변환
    private void setsize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(YoutubeMoreActivity.this);

        //유튜브 리사이클러뷰
        recyclerView.setPadding((int) (size.x * 0.03),0,(int) (size.x * 0.03),0);

        //scrollview 크기
//        youtube_more_btn.getLayoutParams().width = (int) (size.x * 0.2);
//        youtube_more_btn.getLayoutParams().height = (int) (size.y * 0.1);
//        youtube_more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.043));
//        youtube_more_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.043));
    }

    //상태표시줄 색상변경
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }
}