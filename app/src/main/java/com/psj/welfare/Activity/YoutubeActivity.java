package com.psj.welfare.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.psj.welfare.ApiInterfaceTest;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.YoutubeListAdapter;
import com.psj.welfare.adapter.OtherYoutubeAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.data.HorizontalYoutubeItem;
import com.psj.welfare.data.OtherYoutubeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YoutubeActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAnalytics analytics; //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수

    boolean being_logout = false; //로그아웃 했는지 (true면 로그아웃, false면 로그인)
    String SessionId = null; //세션 값
    String token = null; //토큰 값

    ImageButton back_btn; //이전 화면으로 가기
    int page = 1; //페이징처리를 위한 변수
    String id; //현재 재생중인 영상의 id값(videoid 와 다르다)
    String youtube_upload_date; //유튜버 업로드 날짜
    String youtube_name; //유튜버 이름

    private int total_page; //유튜브 데이터 총 페이지(서버에서 보내주는 데이터의 마지막 페이지 값)

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView recyclerView; //리사이클러뷰 선언
    private YoutubeListAdapter youtube_adapter; //아답터 연결
    private RecyclerView.LayoutManager youtube_layoutManager; //레이아웃 매니저
    private ArrayList<HorizontalYoutubeItem> youtubeadapter_List; //강의 데이터
    private YoutubeListAdapter.ClickListener youtubeclick; //유튜브 목록 클릭 리스너

    private Button youtube_more_btn; //유튜브 더보기 버튼
    private TextView youtube_more_text; //유튜브 "다른 영상 보기" 텍스트
    private ConstraintLayout youtube_more_layout; //영상 더보기 레이아웃
    private ConstraintLayout youtubeTop_layout; //상단 툴바 레이아웃

//    Toolbar youtube_toolbar;
    YouTubePlayerView player_1;
    String url_id, thumbnail, title, youtube_count;
    List<HorizontalYoutubeItem> youtube_list;
    List<String> url_id_list, title_list;
    TextView first_title;
    TextView first_video_youtuber; //유튜버 + 업로드 날짜







    RecyclerView other_video_recyclerview;
    OtherYoutubeAdapter adapter;
    OtherYoutubeAdapter.YoutubeItemClickListener itemClickListener;
    List<OtherYoutubeItem> other_list;

//    HashMap<String, String> get_youtube_hashmap;
//    String youtube_name;

    String key_name;
    String video_name;

    SharedPreferences sharedPreferences;
    String encode_str, encode_action;

    Intent intent;
    HorizontalYoutubeItem item;
//    HashMap<String, String> youtube_hashmap;

    // 새 서버에서 가져온 유튜브 데이터를 저장할 변수
    String youtube_id, youtube_title, youtube_thumbnail, youtube_videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(YoutubeActivity.this);
        setContentView(R.layout.activity_youtube);

        back_btn = findViewById(R.id.back_btn); //뒤로가기 버튼

        youtube_list = new ArrayList<>();
//        get_youtube_hashmap = new HashMap<>();
//        youtube_hashmap = new HashMap<>();

        /* 재생 중인 영상을 제외한 다른 영상들의 리스트를 보여줄 리사이클러뷰 */
        other_video_recyclerview = findViewById(R.id.other_video_recyclerview);
        other_video_recyclerview.setHasFixedSize(true);
        other_video_recyclerview.setLayoutManager(new LinearLayoutManager(this));


        //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수 선언
        if (YoutubeActivity.class != null)
        {
            analytics = FirebaseAnalytics.getInstance(this);
        }

        //로그인 했는지 여부
        being_loging();

        //초기화 작업
        init();

//        showWelfareAndYoutubeNotLogin();

//        youtube_toolbar = findViewById(R.id.youtube_toolbar);
//        setSupportActionBar(youtube_toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("유튜버 혜택 소개");






        /* 인텐트에서 데이터가 담긴 객체를 꺼내 YoutubeActivity의 객체에 매핑
        * 여기서 가져온 영상은 바로 재생시킬 수 있도록 하고, 밑의 리사이클러뷰에는 재생중인 영상을 제외한 다른 영상들을 보여준다 */
        if (getIntent().hasExtra("youtube_information"))
        {
            intent = getIntent();
            item = (HorizontalYoutubeItem) intent.getSerializableExtra("youtube_information");
            first_title.setText(item.getYoutube_name()); // 유튜브 타이틀
            id = item.getYoutube_id(); // 유튜브 id값

            /* youtube_information 인텐트를 받아왔으면, 하단 리사이클러뷰에 재생중인 영상을 제외한 나머지 영상들을 보여주기 위해 ArrayList에 객체의 값들을 넣는다
             * ArrayList는 넣고자 하는 값의 유형(객체)만 들어갈 수 있도록 선언해야 한다 */


//            youtube_list = new ArrayList<>();
            // youtube_list에는 현재 재생중인 영상을 제외한 나머지 영상이 들어가야 한다




//            for (int i = 0; i < youtube_list.size(); i++)
//            {
//                Log.e(TAG, "youtube_list 안의 영상 이름값 : " + youtube_list.get(i).getYoutube_name());
//                Log.e(TAG, "youtube_list 안의 videoId 값 : " + youtube_list.get(i).getYoutube_videoId());
//                Log.e(TAG, "youtube_list 안의 썸네일 값 : " + youtube_list.get(i).getYoutube_thumbnail());
//            }

            /* 재구성한 유튜브 어댑터로 어댑터를 초기화한다 */

        }


        //        if (getIntent().hasExtra("youtube_hashmap"))
//        {
//            Intent intent = getIntent();
//            get_youtube_hashmap = (HashMap<String, String>) intent.getSerializableExtra("youtube_hashmap");
//            Log.e(TAG, "해시맵 안의 키값 확인 : " + get_youtube_hashmap.keySet());
//            Log.e(TAG, "해시맵 안의 밸류값 확인 : " + get_youtube_hashmap.values());
//        }



//        other_list = new ArrayList(get_youtube_hashmap.values());

        /* 이전 유튜브 API를 삭제했기 때문에 인텐트로 받아온 해시맵을 ArrayList에 넣어서 리사이클러뷰에 넣어야 한다 */
//        adapter = new OtherYoutubeAdapter(YoutubeActivity.this, other_list, itemClickListener);
//        adapter.setOnItemClickListener(((view, pos) -> {
//            String youtube_url_id = other_list.get(pos).getVideo_id();
//            String title = other_list.get(pos).getTitle();
//            player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//            {
//                @Override
//                public void onReady(@NotNull YouTubePlayer youTubePlayer)
//                {
//                    youTubePlayer.loadVideo(youtube_url_id, 0);
//                    youTubePlayer.pause();
//                }
//            });
//            Intent intent = getIntent();
//            intent.putExtra("youtube_name", title);
//            finish();
//            startActivity(intent);
//        }));
//        other_video_recyclerview.setAdapter(adapter);





//        //리사이클러뷰가 마지막에 도달하면 이벤트 발생
//        youtubeScrollListener();

        //유튜브 데이터 서버에서 받아오기
        youtubedata(1);

        player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
        {
            @Override
            public void onReady(YouTubePlayer youTubePlayer)
            {
                youTubePlayer.cueVideo(item.getYoutube_videoId(), 0);
                youTubePlayer.pause();
            }
        });

        //이전 화면으로 가기
        back_btn.setOnClickListener(v -> {
            finish(); //현재 액티비티 종료
            //액티비티 스택을 추적하고싶을경우. 이런 경우엔 새액티비티를 시작할때마다 intent에 FLAG_ACTIVITY_REORDER_TO_FRONT 나 FLAG_ACTIVITY_PREVIOUS_IS_TOP 같은 플래그를 줄 수 있습니다.
        });


        //다른 영상 보러가기기
       youtube_adapter.setYoutubeClickListener(new YoutubeListAdapter.ClickListener() {
            @Override
            public void youtubeClick(View v, int position) {
//                Log.e(TAG,"position : " + position);
                Intent intent = new Intent(YoutubeActivity.this,YoutubeActivity.class);
                HorizontalYoutubeItem item = new HorizontalYoutubeItem();

                item.setYoutube_id(youtubeadapter_List.get(position).getYoutube_id());
                item.setYoutube_name(youtubeadapter_List.get(position).getYoutube_name());
                item.setYoutube_thumbnail(youtubeadapter_List.get(position).getYoutube_thumbnail());
                item.setYoutube_videoId(youtubeadapter_List.get(position).getYoutube_videoId());
                item.setYoutube_id(youtubeadapter_List.get(position).getYoutube_id());
                intent.putExtra("youtube_information", item);

                //파이어베이스에서 그로스해킹용으로 쓰기 위한 데이터 변수
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                startActivity(intent);
            }
        });



        youtube_more_btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, YoutubeMoreActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 더보기 화면으로 이동");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            startActivity(intent);
        });
    }



    //리사이클러뷰가 마지막에 도달하면 이벤트 발생
    private void youtubeScrollListener() {
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
                    youtubedata(page); //페이지값을 서버에 보내준다
                }
            }
        };
        //리사이클러뷰가 마지막에 도달했을 때 이벤트 발생시킴
        recyclerView.addOnScrollListener(onScrollListener);
    }



    //유튜브 데이터 서버에서 받아오기
    public void youtubedata(int page){


        //서버 연결전에 프로그래스바 보여주기
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();


        if(!being_logout) //로그인 했을 경우
        {
            ApiInterfaceTest apiInterface = ApiClient.getRetrofit().create(ApiInterfaceTest.class);
            Call<String> call = apiInterface.YoutubeSelect_beingid(token,SessionId,"show_video",String.valueOf(page),id);
            call.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(Call<String> call, Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String result = response.body();

//                        Log.e(TAG,"result1 : " + result);
                        responseParse(result);
                    }
                    else
                    {
                        Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
                    }

                    dialog.dismiss(); //서버 연결후에 프로그래스바 숨기기
                }

                @Override
                public void onFailure(Call<String> call, Throwable t)
                {
                    Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
                }
            });


        }
        else //로그인 안했을 경우
        {
            ApiInterfaceTest apiInterface = ApiClient.getRetrofit().create(ApiInterfaceTest.class);
            Call<String> call = apiInterface.YoutubeSelect("show_video",String.valueOf(page),id);
            call.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(Call<String> call, Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        String result = response.body();

                        responseParse(result);
                    }
                    else
                    {
                        Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
                    }

                    dialog.dismiss(); //서버 연결후에 프로그래스바 숨기기
                }

                @Override
                public void onFailure(Call<String> call, Throwable t)
                {
                    Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
                }
            });

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

    //서버로부터 받아온 유튜브데이터 파싱
    private void responseParse(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            total_page = Integer.parseInt(jsonObject.getString("total_page")); //서버에서 보내주는 마지막 페이지 값

//            Log.e(TAG,"jsonArray.length() : " + jsonArray.length());
//            Log.e(TAG,"message : " + jsonObject.getString("message"));

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                if(page == 1){
                    /* 유튜브 데이터 */
                    JSONArray jsonArraysee = inner_json.getJSONArray("see_video"); //현재 선택한 유튜브 영상
                    JSONObject see_object = jsonArraysee.getJSONObject(i);
                    String id = see_object.getString("id"); ////현재 선택한 유튜브 id값
                    String title = see_object.getString("title"); ////현재 선택한 유튜브 제목
                    String name = see_object.getString("name"); ////현재 선택한 유튜버 이름
                    String thumbnail = see_object.getString("thumbnail"); ////현재 선택한 유튜브 썸네일 이미지
                    String videoId = see_object.getString("videoId"); ////현재 선택한 유튜브 비디오id
                    String upload_date = see_object.getString("upload_date"); ////현재 선택한 유튜브 업로드 날짜

                    first_video_youtuber.setText(name + " · "+ upload_date);
                }

                JSONArray jsonArraymore = inner_json.getJSONArray("more_video"); //현재 선택한 유튜브 영상을 제외한 다른 유튜브(페이징 처리)
                for (int j = 0; j < jsonArraymore.length(); j++)
                {
                    JSONObject final_json = jsonArraymore.getJSONObject(j);
                    youtube_id = final_json.getString("id");
                    youtube_title = final_json.getString("title");
                    youtube_thumbnail = final_json.getString("thumbnail");
                    youtube_videoId = final_json.getString("videoId");
                    youtube_name = final_json.getString("name");
                    youtube_upload_date = final_json.getString("upload_date");

                    HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                    item.setYoutube_id(youtube_id);
                    item.setYoutube_title(youtube_title);
                    item.setYoutube_thumbnail(youtube_thumbnail);
                    item.setYoutube_videoId(youtube_videoId);
                    item.setYoutube_name(youtube_name);
                    item.setYoutube_upload_date(youtube_upload_date);
                    youtubeadapter_List.add(item);
                    youtube_adapter.notifyDataSetChanged();
//                    youtube_list.add(item);

                }
            }


        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }




















//    public static Object getkey(HashMap<String, String> hashmap, Object value)
//    {
//        for (Object obj : hashmap.keySet())
//        {
//            if (hashmap.get(obj).equals(value))
//            {
//                return obj;
//            }
//        }
//        return null;
//    }
//
//    void showWelfareAndYoutubeNotLogin()
//    {
//        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
//        Call<String> call = apiInterface.showWelfareAndYoutubeNotLogin("total_main");
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    String result = response.body();
//
//                    Log.e(TAG,"result : " + result);
//                    responseParse(result);
//                }
//                else
//                {
//                    Log.e(TAG, "비로그인일 시 데이터 가져오기 실패 : " + response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
//            }
//        });
//    }
//
//
//    void getYoutubeInformation()
//    {
//        encode("유튜브 재생 화면 진입");
//        sharedPreferences = getSharedPreferences("app_pref", 0);
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        Call<String> call = apiInterface.getYoutubeInformation();
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    String result = response.body();
//                    jsonParsing(result);
//                }
//                else
//                {
//                    Log.e(TAG, "실패 : " + response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e(TAG, "에러 : " + t.getMessage());
//            }
//        });
//    }
//
//    private void encode(String str)
//    {
//        try
//        {
//            encode_str = URLEncoder.encode(str, "UTF-8");
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    private void jsonParsing(String result)
//    {
//        url_id_list = new ArrayList<>();
//        title_list = new ArrayList<>();
//        try
//        {
//            JSONObject jsonObject = new JSONObject(result);
//            JSONArray jsonArray = jsonObject.getJSONArray("Message");
//            for (int i = 0; i < jsonArray.length(); i++)
//            {
//                JSONObject inner_obj = jsonArray.getJSONObject(i);
//                thumbnail = inner_obj.getString("thumbnail");
//                url_id = inner_obj.getString("videoId");
//                title = inner_obj.getString("title");
//                url_id_list.add(url_id);
//                title_list.add(title);
//
//                OtherYoutubeItem item = new OtherYoutubeItem();
//                item.setThumbnail(thumbnail);
//                item.setVideo_id(url_id);
//                item.setTitle(title);
//                boolean isDuplicated = false;
//                for (int j = 0; j < other_list.size(); j++)
//                {
//                    if (other_list.get(j).getTitle().equals(video_name))
//                    {
//                        isDuplicated = true;
//                        break;
//                    }
//                }
//                if (!isDuplicated)
//                {
//                    if (!item.getTitle().contains(video_name))
//                    {
//                        other_list.add(item);
//                    }
//                }
//            }
//            youtube_count = jsonObject.getString("TotalCount");
//        }
//        catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < other_list.size(); i++)
//        {
//            Log.e(TAG, "other_list : " + other_list.get(i).getVideo_id());
//        }
//
//        adapter = new OtherYoutubeAdapter(YoutubeActivity.this, other_list, itemClickListener);
//        adapter.setOnItemClickListener(((view, pos) -> {
//            String youtube_url_id = other_list.get(pos).getVideo_id();
//            String thumbnail = other_list.get(pos).getThumbnail();
//            String title = other_list.get(pos).getTitle();
//            player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//            {
//                @Override
//                public void onReady(@NotNull YouTubePlayer youTubePlayer)
//                {
//                    youTubePlayer.loadVideo(youtube_url_id, 0);
//                    youTubePlayer.pause();
//                }
//            });
//            Intent intent = getIntent();
//            intent.putExtra("youtube_name", title);
//            finish();
//            startActivity(intent);
//        }));
//        other_video_recyclerview.setAdapter(adapter);
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item)
//    {
//        switch (item.getItemId())
//        {
//            case android.R.id.home :
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
////    @Override
////    public void onBackPressed()
////    {
////        super.onBackPressed();
////        userLog("유튜브 화면에서 뒤로가기 눌러 메인으로 이동");
////    }
//
//
//    void userLog(String user_action)
//    {
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        String token;
//        if (sharedPreferences.getString("token", "").equals(""))
//        {
//            token = null;
//        }
//        else
//        {
//            token = sharedPreferences.getString("token", "");
//        }
//        String sessionId = sharedPreferences.getString("sessionId", "");
//        String action = encodeAction(user_action);
//        Call<String> call = apiInterface.userLog(token, sessionId, "youtube_review", action, null, LogUtil.getUserLog());
//        call.enqueue(new Callback<String>()
//        {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response)
//            {
//                if (response.isSuccessful() && response.body() != null)
//                {
//                    String result = response.body();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t)
//            {
//                Log.e(TAG, "에러 : " + t.getMessage());
//            }
//        });
//    }
//
//
//    /* 서버로 한글을 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 한다. 이 때 한글을 인코딩하는 메서드 */
//    private String encodeAction(String str)
//    {
//        try
//        {
//            str = URLEncoder.encode(str, "UTF-8");
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            e.printStackTrace();
//        }
//        return str;
//    }











    //초기화 작업
    private void init()
    {
        player_1 = findViewById(R.id.player_1);
        getLifecycle().addObserver(player_1);

        first_title = findViewById(R.id.first_video_title);
        first_video_youtuber = findViewById(R.id.first_video_youtuber); //유튜버 + 업로드날짜
        youtube_more_btn = findViewById(R.id.youtube_more_btn); //유튜브 더보기 버튼
        youtube_more_text = findViewById(R.id.youtube_more_text); //유튜브 "다른 영상 보기" 텍스트
        youtube_more_layout = findViewById(R.id.youtube_more_layout); //영상 더보기 레이아웃
        youtubeTop_layout = findViewById(R.id.youtubeTop_layout); //상단 툴바 레이아웃

        //유튜브 다른 영상 목록 보여주기 위한 리사이클러뷰
        recyclerView = findViewById(R.id.other_video_recyclerview); //리사이클러뷰 연결
        recyclerView.setHasFixedSize(true); //setHasFixedSize(true)는 리사이클러뷰 안 아이템들의 크기를 가변적으로 하지 않고 고정으로 한다는 것
        youtube_layoutManager = new LinearLayoutManager(this); //리사이클러뷰의 레이아웃을 Linear 방식으로 한다는 것
        recyclerView.setLayoutManager(youtube_layoutManager); //리사이클러뷰의 레이아웃을 정함
        youtubeadapter_List = new ArrayList<>(); // 강의 데이터를 담을 어레이 리스트 (어댑터 쪽으로)

        youtube_adapter = new YoutubeListAdapter(youtubeadapter_List, this, youtubeclick); //아답터 연결(어레이리스트와 뷰 연결)
        recyclerView.setAdapter(youtube_adapter); //리사이클러뷰에 아답터 연결

        //xml크기를 동적으로 변환
        setsize();
    }


    //xml크기를 동적으로 변환
    private void setsize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(YoutubeActivity.this);
        //scrollview 크기
//        youtube_more_btn.getLayoutParams().width = (int) (size.x * 0.2);
//        youtube_more_btn.getLayoutParams().height = (int) (size.y * 0.1);
        youtube_more_text.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.043));
        youtube_more_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.043));


        //상단 툴바 레이아웃
        youtubeTop_layout.getLayoutParams().height = (int) (size.y * 0.073);
        //유튜브 영상
        player_1.getLayoutParams().height = (int) (size.y * 0.29);
        //유튜버 + 업로드 날짜
        first_video_youtuber.setTextSize((int) (size.y * 0.038));
        //다른 영상 보기 레이아웃
        youtube_more_layout.getLayoutParams().height = (int) (size.y * 0.08);
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