package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.psj.welfare.R;
import com.psj.welfare.adapter.OtherYoutubeAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.OtherYoutubeItem;
import com.psj.welfare.data.YoutubeItem;

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

public class YoutubeTestActivity extends YouTubeBaseActivity
{
    private final String TAG = this.getClass().getSimpleName();

    private YouTubePlayer mYouTubePlayer;
    YouTubePlayerView youtubeView;
    YouTubePlayer.OnInitializedListener listener;
    String youtube_name;
    HashMap<String, String> get_youtube_hashmap;

    List<YoutubeItem> youtube_list;
    List<String> url_id_list, title_list;

    String url_id, thumbnail, title, youtube_count;

    String key_name;

    String video_name;

    TextView first_title;

    RecyclerView other_video_recyclerview;
    OtherYoutubeAdapter adapter;
    OtherYoutubeAdapter.YoutubeItemClickListener itemClickListener;
    List<OtherYoutubeItem> other_list;

    SharedPreferences sharedPreferences;

    String encode_str, change_url_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(YoutubeTestActivity.this);
        setContentView(R.layout.activity_youtube_test);

        first_title = findViewById(R.id.first_video_title);
        other_video_recyclerview = findViewById(R.id.other_video_recyclerview);
        other_video_recyclerview.setHasFixedSize(true);
        other_video_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        get_youtube_hashmap = (HashMap<String, String>) intent.getSerializableExtra("youtube_hashmap");
        youtube_name = intent.getStringExtra("youtube_name");
        first_title.setText(youtube_name);

        getYoutubeInformation();

        key_name = get_youtube_hashmap.get(youtube_name);
        video_name = (String) getkey(get_youtube_hashmap, key_name);

        Log.e(TAG, "key_name : " + key_name);

        youtubeView = findViewById(R.id.youtubeView);
        listener = new YouTubePlayer.OnInitializedListener()
        {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b)
            {
                youTubePlayer.cueVideo(key_name, 0);
                youTubePlayer.pause();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
            {
                //
            }

        };
        youtubeView.initialize(key_name, listener);
    }

    private void youTubePlayerSetup()
    {
        listener = new YouTubePlayer.OnInitializedListener()
        {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b)
            {
                mYouTubePlayer = youTubePlayer;
                mYouTubePlayer.cueVideo(change_url_id);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult)
            {

            }
        };
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
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
        youtube_list = new ArrayList<>();
        url_id_list = new ArrayList<>();
        title_list = new ArrayList<>();
        other_list = new ArrayList<>();
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

        adapter = new OtherYoutubeAdapter(YoutubeTestActivity.this, other_list, itemClickListener);
        adapter.setOnItemClickListener(((view, pos) -> {
            String youtube_url_id = other_list.get(pos).getVideo_id();
            String thumbnail = other_list.get(pos).getThumbnail();
            String title = other_list.get(pos).getTitle();
            Log.e(TAG, "youtube_url_id = " + youtube_url_id + ", 썸네일 = " + thumbnail + " 제목 = " + title);
            change_url_id = youtube_url_id;
            youtubeView.initialize(key_name, listener);
            /* 하단 리사이클러뷰의 영상을 누르면 그 영상을 재생시키기 위해 이 액티비티를 재시작해야 한다
             * 이걸 구현하려면 new Intent()를 쓰는 게 아닌 getIntent()를 쓰고 finish()로 이 액티비티를 지운 다음, intent를 시작하면
             * 선택한 영상의 제목을 갖고 액티비티를 재시작하는 효과를 낸다. 액티비티 스택이 쌓이지 않기 때문에 뒤로가기를 눌러도 MainFragment로 이동된다 */
//            Intent intent = getIntent();
//            intent.putExtra("youtube_name", title);
//            finish();
//            startActivity(intent);
        }));
        other_video_recyclerview.setAdapter(adapter);
    }

}