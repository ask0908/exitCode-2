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
import com.psj.welfare.data.OtherYoutubeItem;
import com.psj.welfare.data.YoutubeItem;
import com.psj.welfare.util.LogUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    List<YoutubeItem> youtube_list;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(YoutubeActivity.this);
        setContentView(R.layout.activity_youtube);

        get_youtube_hashmap = new HashMap<>();
        youtube_toolbar = findViewById(R.id.youtube_toolbar);
        setSupportActionBar(youtube_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("유튜버 혜택 소개");

        init();

        if (getIntent().hasExtra("youtube_hashmap"))
        {
            Intent intent = getIntent();
            get_youtube_hashmap = (HashMap<String, String>) intent.getSerializableExtra("youtube_hashmap");
        }

        if (getIntent().hasExtra("youtube_name"))
        {
            Intent intent = getIntent();
            youtube_name = intent.getStringExtra("youtube_name");
            first_title.setText(youtube_name);
        }

        key_name = (String) get_youtube_hashmap.get(youtube_name);
        video_name = (String) getkey(get_youtube_hashmap, key_name);

//        getYoutubeInformation();

        other_video_recyclerview = findViewById(R.id.other_video_recyclerview);
        other_video_recyclerview.setHasFixedSize(true);
        other_video_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        Log.e(TAG, "key_name : " + key_name);

        player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
        {
            @Override
            public void onReady(YouTubePlayer youTubePlayer)
            {
                youTubePlayer.cueVideo(key_name, 0);
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

//    private void jsonParsing(String result)
//    {
//        youtube_list = new ArrayList<>();
//        url_id_list = new ArrayList<>();
//        title_list = new ArrayList<>();
//        other_list = new ArrayList<>();
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
//                item.setUrl_id(url_id);
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
//
//        adapter = new OtherYoutubeAdapter(YoutubeActivity.this, other_list, itemClickListener);
//        adapter.setOnItemClickListener(((view, pos) -> {
//            String youtube_url_id = other_list.get(pos).getUrl_id();
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