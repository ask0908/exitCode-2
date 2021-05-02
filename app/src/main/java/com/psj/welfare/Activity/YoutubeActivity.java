package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.psj.welfare.R;
import com.psj.welfare.adapter.OtherYoutubeAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.HorizontalYoutubeItem;
import com.psj.welfare.data.OtherYoutubeItem;
import com.psj.welfare.util.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class YoutubeActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    Toolbar youtube_toolbar;
    YouTubePlayerView player_1;
    String url_id, thumbnail, title, youtube_count;
    List<HorizontalYoutubeItem> youtube_list;
    List<String> url_id_list, title_list;
    TextView first_title;

    RecyclerView other_video_recyclerview;
    OtherYoutubeAdapter adapter;
    OtherYoutubeAdapter.YoutubeItemClickListener itemClickListener;
    List<OtherYoutubeItem> other_list;

    HashMap<String, String> get_youtube_hashmap;
    String youtube_name;

    String key_name;
    String video_name;

    SharedPreferences sharedPreferences;
    String encode_str, encode_action;

    Intent intent;
    HorizontalYoutubeItem item;
    HashMap<String, String> youtube_hashmap;

    // 새 서버에서 가져온 유튜브 데이터를 저장할 변수
    String youtube_id, youtube_title, youtube_thumbnail, youtube_videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(YoutubeActivity.this);
        setContentView(R.layout.activity_youtube);

        youtube_list = new ArrayList<>();
        get_youtube_hashmap = new HashMap<>();
        youtube_hashmap = new HashMap<>();

        /* 재생 중인 영상을 제외한 다른 영상들의 리스트를 보여줄 리사이클러뷰 */
        other_video_recyclerview = findViewById(R.id.other_video_recyclerview);
        other_video_recyclerview.setHasFixedSize(true);
        other_video_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        showWelfareAndYoutubeNotLogin();

        youtube_toolbar = findViewById(R.id.youtube_toolbar);
        setSupportActionBar(youtube_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("유튜버 혜택 소개");

        init();

        if (getIntent().hasExtra("youtube_hashmap"))
        {
            Intent intent = getIntent();
            get_youtube_hashmap = (HashMap<String, String>) intent.getSerializableExtra("youtube_hashmap");
            Log.e(TAG, "해시맵 안의 키값 확인 : " + get_youtube_hashmap.keySet());
            Log.e(TAG, "해시맵 안의 밸류값 확인 : " + get_youtube_hashmap.values());
        }

        /* 인텐트에서 데이터가 담긴 객체를 꺼내 YoutubeActivity의 객체에 매핑
        * 여기서 가져온 영상은 바로 재생시킬 수 있도록 하고, 밑의 리사이클러뷰에는 재생중인 영상을 제외한 다른 영상들을 보여준다 */
        if (getIntent().hasExtra("youtube_information"))
        {
            intent = getIntent();
            item = (HorizontalYoutubeItem) intent.getSerializableExtra("youtube_information");
            Log.e(TAG, "item에서 꺼낸 videoId : " + item.getYoutube_videoId());
            Log.e(TAG, "item에서 꺼낸 영상 이름 : " + item.getYoutube_name());
            Log.e(TAG, "item에서 꺼낸 썸네일 : " + item.getYoutube_thumbnail());
            first_title.setText(item.getYoutube_name());

            /* youtube_information 인텐트를 받아왔으면, 하단 리사이클러뷰에 재생중인 영상을 제외한 나머지 영상들을 보여주기 위해 ArrayList에 객체의 값들을 넣는다
             * ArrayList는 넣고자 하는 값의 유형(객체)만 들어갈 수 있도록 선언해야 한다 */
            youtube_list = new ArrayList<>();
            // youtube_list에는 현재 재생중인 영상을 제외한 나머지 영상이 들어가야 한다

            for (int i = 0; i < youtube_list.size(); i++)
            {
                Log.e(TAG, "youtube_list 안의 영상 이름값 : " + youtube_list.get(i).getYoutube_name());
                Log.e(TAG, "youtube_list 안의 videoId 값 : " + youtube_list.get(i).getYoutube_videoId());
                Log.e(TAG, "youtube_list 안의 썸네일 값 : " + youtube_list.get(i).getYoutube_thumbnail());
            }

            /* 재구성한 유튜브 어댑터로 어댑터를 초기화한다 */

        }

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

        player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
        {
            @Override
            public void onReady(YouTubePlayer youTubePlayer)
            {
                youTubePlayer.cueVideo(item.getYoutube_videoId(), 0);
                youTubePlayer.pause();
            }
        });
    }

    public static Object getkey(HashMap<String, String> hashmap, Object value)
    {
        for (Object obj : hashmap.keySet())
        {
            if (hashmap.get(obj).equals(value))
            {
                return obj;
            }
        }
        return null;
    }

    void showWelfareAndYoutubeNotLogin()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.showWelfareAndYoutubeNotLogin("total_main");
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
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "비로그인일 시 데이터 가져오기 에러 : " + t.getMessage());
            }
        });
    }

    private void responseParse(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);

                /* 유튜브 데이터 */
                JSONArray arr = inner_json.getJSONArray("youtube");
                for (int j = 0; j < arr.length(); j++)
                {
                    JSONObject final_json = arr.getJSONObject(j);
                    youtube_id = final_json.getString("id");
                    youtube_title = final_json.getString("title");
                    youtube_thumbnail = final_json.getString("thumbnail");
                    youtube_videoId = final_json.getString("videoId");

                    HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                    item.setYoutube_id(youtube_id);
                    item.setYoutube_name(youtube_title);
                    item.setYoutube_thumbnail(youtube_thumbnail);
                    item.setYoutube_videoId(youtube_videoId);
                    youtube_list.add(item);
                }
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    void getYoutubeInformation()
    {
        encode("유튜브 재생 화면 진입");
        sharedPreferences = getSharedPreferences("app_pref", 0);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getYoutubeInformation();
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
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
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

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

    private void jsonParsing(String result)
    {
        url_id_list = new ArrayList<>();
        title_list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                thumbnail = inner_obj.getString("thumbnail");
                url_id = inner_obj.getString("videoId");
                title = inner_obj.getString("title");
                url_id_list.add(url_id);
                title_list.add(title);

                OtherYoutubeItem item = new OtherYoutubeItem();
                item.setThumbnail(thumbnail);
                item.setVideo_id(url_id);
                item.setTitle(title);
                boolean isDuplicated = false;
                for (int j = 0; j < other_list.size(); j++)
                {
                    if (other_list.get(j).getTitle().equals(video_name))
                    {
                        isDuplicated = true;
                        break;
                    }
                }
                if (!isDuplicated)
                {
                    if (!item.getTitle().contains(video_name))
                    {
                        other_list.add(item);
                    }
                }
            }
            youtube_count = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < other_list.size(); i++)
        {
            Log.e(TAG, "other_list : " + other_list.get(i).getVideo_id());
        }

        adapter = new OtherYoutubeAdapter(YoutubeActivity.this, other_list, itemClickListener);
        adapter.setOnItemClickListener(((view, pos) -> {
            String youtube_url_id = other_list.get(pos).getVideo_id();
            String thumbnail = other_list.get(pos).getThumbnail();
            String title = other_list.get(pos).getTitle();
            player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
            {
                @Override
                public void onReady(@NotNull YouTubePlayer youTubePlayer)
                {
                    youTubePlayer.loadVideo(youtube_url_id, 0);
                    youTubePlayer.pause();
                }
            });
            Intent intent = getIntent();
            intent.putExtra("youtube_name", title);
            finish();
            startActivity(intent);
        }));
        other_video_recyclerview.setAdapter(adapter);
    }

    private void init()
    {
        player_1 = findViewById(R.id.player_1);
        getLifecycle().addObserver(player_1);

        first_title = findViewById(R.id.first_video_title);
    }

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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        userLog("유튜브 화면에서 뒤로가기 눌러 메인으로 이동");
    }

    void userLog(String user_action)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String token;
        if (sharedPreferences.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = sharedPreferences.getString("token", "");
        }
        String sessionId = sharedPreferences.getString("sessionId", "");
        String action = encodeAction(user_action);
        Call<String> call = apiInterface.userLog(token, sessionId, "youtube_review", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버로 한글을 보낼 때 그냥 보내면 안되고 인코딩해서 보내야 한다. 이 때 한글을 인코딩하는 메서드 */
    private String encodeAction(String str)
    {
        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}