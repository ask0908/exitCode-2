package com.psj.welfare.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.Data.YoutubeDTO;
import com.psj.welfare.R;
import com.psj.welfare.adapter.RelativeWelfareAdapter;
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

/* 유튜버 리뷰 화면에는 선택한 유튜버의 영상 몇 개를 받아와서 스크롤뷰 안에 보여준다
* 리사이클러뷰를 넣어서 아이템에 유튜브 플레이어를 넣고 이걸 보여주자
* 이 화면에서 영상들을 어떻게 보여줄까? */
public class YoutubeActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    Toolbar youtube_toolbar;
    YouTubePlayerView player_1;
    YouTubePlayerView player_2, player_3, player_4, player_5, player_6;
    String url_id, thumbnail, title, youtube_count;
    List<YoutubeDTO> youtube_list;
    List<String> url_id_list, title_list;
    TextView first_title, second_title, third_title, fourth_title, fifth_title;

    /* 영상과 관련된 혜택들을 보여줄 리사이클러뷰(재구현 필요) */
    RecyclerView similar_recycler;
    private RelativeWelfareAdapter adapter;
    private RelativeWelfareAdapter.ItemClickListener itemClickListener;
    List<DetailBenefitItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        youtube_toolbar = findViewById(R.id.youtube_toolbar);
        setSupportActionBar(youtube_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("유튜버 혜택 소개");

        init();

        getYoutubeInformation();

        similar_recycler = findViewById(R.id.similar_recycler);
        similar_recycler.setHasFixedSize(true);
        similar_recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RelativeWelfareAdapter(YoutubeActivity.this, list, itemClickListener);
        similar_recycler.setAdapter(adapter);

        // 유튜브 플레이어엔 임시로 영상을 추가한다. video_id 안에 유튜브 영상 링크의 "watch?v="의 오른쪽 부분을 가져와 넣으면 된다
        /* loadVideo() vs cueVideo() : loadVideo()는 비디오를 로드하고 자동으로 재생하는 반면 cueVideo()는 비디오, 미리보기 이미지만 로드하고 자동 재생하지 않는다
        * loadVideo()를 쓸 경우 여러 유튜브 뷰를 한 액티비티에 뒀을 때 맨 위의 영상만 썸네일이 나온다
        * cueVideo()를 쓸 경우 썸네일과 재생 버튼이 나온다. 사용자 입장에서 썸네일이 나오지 않으면 이상하게 볼 것 같아서 cueVideo()를 사용했다 */
        player_1.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
        {
            @Override
            public void onReady(YouTubePlayer youTubePlayer)
            {
                String video_id = url_id_list.get(1);
                youTubePlayer.cueVideo(video_id, 0);
                youTubePlayer.pause();
            }
        });

//        player_2.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                String video_id = url_id_list.get(1);
//                youTubePlayer.cueVideo(video_id, 0);
//                youTubePlayer.pause();
//            }
//        });
//
//        player_3.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                String video_id = url_id_list.get(2);
//                youTubePlayer.cueVideo(video_id, 0);
//                youTubePlayer.pause();
//            }
//        });
//
//        player_4.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                String video_id = url_id_list.get(3);
//                youTubePlayer.cueVideo(video_id, 0);
//                youTubePlayer.pause();
//            }
//        });
//
//        player_5.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                String video_id = url_id_list.get(4);
//                youTubePlayer.cueVideo(video_id, 0);
//                youTubePlayer.pause();
//            }
//        });
//
//        player_6.addYouTubePlayerListener(new AbstractYouTubePlayerListener()
//        {
//            @Override
//            public void onReady(YouTubePlayer youTubePlayer)
//            {
//                String video_id = url_id_list.get(5);
//                youTubePlayer.cueVideo(video_id, 0);
//                youTubePlayer.pause();
//            }
//        });
    }

    void getYoutubeInformation()
    {
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

    private void jsonParsing(String result)
    {
        youtube_list = new ArrayList<>();
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
//                youtube = new YoutubeDTO();
//                youtube.setThumbnail(thumbnail);
//                youtube.setUrl_id(url_id);
//                youtube.setTitle(title);
//                youtube_list.add(youtube);
                url_id_list.add(url_id);
                title_list.add(title);
            }
            youtube_count = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        first_title.setText(title_list.get(1));
//        second_title.setText(title_list.get(1));
//        third_title.setText(title_list.get(2));
//        fourth_title.setText(title_list.get(3));
//        fifth_title.setText(title_list.get(4));
        Log.e(TAG, "count = " + youtube_count);
    }

    private void init()
    {
        player_1 = findViewById(R.id.player_1);
        getLifecycle().addObserver(player_1);

//        player_2 = findViewById(R.id.player_2);
//        getLifecycle().addObserver(player_2);
//
//        player_3 = findViewById(R.id.player_3);
//        getLifecycle().addObserver(player_3);
//
//        player_4 = findViewById(R.id.player_4);
//        getLifecycle().addObserver(player_4);
//
//        player_5 = findViewById(R.id.player_5);
//        getLifecycle().addObserver(player_5);
//
//        player_6 = findViewById(R.id.player_6);
//        getLifecycle().addObserver(player_6);

        first_title = findViewById(R.id.first_video_title);
//        second_title = findViewById(R.id.second_video_title);
//        third_title = findViewById(R.id.third_video_title);
//        fourth_title = findViewById(R.id.fourth_video_title);
//        fifth_title = findViewById(R.id.fifth_video_title);
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
}