package com.benefit.welfare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.benefit.welfare.Data.OtherYoutubeItem;
import com.benefit.welfare.Data.YoutubeItem;
import com.benefit.welfare.R;
import com.benefit.welfare.adapter.OtherYoutubeAdapter;
import com.benefit.welfare.api.ApiClient;
import com.benefit.welfare.api.ApiInterface;
import com.benefit.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 유튜브 영상 재생하는 액티비티. 밑에 재생중인 영상을 제외한 다른 영상들 리스트를 보여준다 */
public class YoutubeActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    Toolbar youtube_toolbar;
    YouTubePlayerView player_1;
    String url_id, thumbnail, title, youtube_count;
    List<YoutubeItem> youtube_list;
    List<String> url_id_list, title_list;
    // 메인에서 선택한 유튜브 영상명을 붙일 텍스트뷰
    TextView first_title;

    /* 다른 영상들을 보여주는 하단 리사이클러뷰 */
    RecyclerView other_video_recyclerview;
    OtherYoutubeAdapter adapter;
    OtherYoutubeAdapter.YoutubeItemClickListener itemClickListener;
    List<OtherYoutubeItem> other_list;

    // 선택한 유튜브 영상을 보여줄 때 사용할 해시맵
    HashMap<String, String> get_youtube_hashmap;
    String youtube_name;

    // 재생할 영상 아이디
    String key_name;

    // 리스트에서 중복되는 영상 제거할 때 사용할 변수
    String video_name;

    SharedPreferences sharedPreferences;
    String encode_str, encode_action;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);

        // MainFragment에서 가져온 해시맵을 담을 해시맵
        get_youtube_hashmap = new HashMap<>();
        youtube_toolbar = findViewById(R.id.youtube_toolbar);
        // 툴바 처리
        setSupportActionBar(youtube_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("유튜버 혜택 소개");

        init();

        // 인텐트 널체크 후 안의 해시맵 데이터 꺼내기
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

        // 가져온 해시맵 안의 데이터 확인
        if (get_youtube_hashmap != null)
        {
            for (Map.Entry<String, String> element : get_youtube_hashmap.entrySet())
            {
                Log.e("받아온 유튜브 해시맵", String.format("키 -> %s, 값 -> %s", element.getKey(), element.getValue()));
            }
            Log.e(TAG, "받아온 유튜브 영상 이름 = " + youtube_name);
        }

        // first_title 텍스트뷰에 set된 텍스트와 일치하는 키값을 찾고, 그 키값에 매핑된 value값을 String 변수에 담는다
        key_name = (String) get_youtube_hashmap.get(youtube_name);
        video_name = (String) getkey(get_youtube_hashmap, key_name);

        // 서버에서 유튜브 데이터 가져오는 메서드
        getYoutubeInformation();

        other_video_recyclerview = findViewById(R.id.other_video_recyclerview);
        other_video_recyclerview.setHasFixedSize(true);
        other_video_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        // 유튜브 플레이어엔 임시로 영상을 추가한다. video_id 안에 유튜브 영상 링크의 "watch?v="의 오른쪽 부분을 가져와 넣으면 된다
        /* loadVideo() vs cueVideo() : loadVideo()는 비디오를 로드하고 자동으로 재생하는 반면 cueVideo()는 비디오, 미리보기 이미지만 로드하고 자동 재생하지 않는다
        * loadVideo()를 쓸 경우 여러 유튜브 뷰를 한 액티비티에 뒀을 때 맨 위의 영상만 썸네일이 나온다
        * cueVideo()를 쓸 경우 썸네일과 재생 버튼이 나온다. 사용자 입장에서 썸네일이 나오지 않으면 이상하게 볼 것 같아서 cueVideo()를 사용했다 */
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

    // 재생중인 유튜브 영상과 같은 이름의 영상을 찾는 메서드
    // JSON 값을 파싱할 때 중복되는 이름의 영상을 제외하고 밑의 영상 리스트에 넣는다
    public static Object getkey(HashMap<String, String> hashmap, Object value)
    {
        for (Object obj : hashmap.keySet())
        {
            if (hashmap.get(obj).equals(value))
            {
                // 여기서 재생중인 영상과 이름이 같은 영상 데이터가 리턴된다
                return obj;
            }
        }
        return null;
    }

    void getYoutubeInformation()
    {
        encode("유튜브 재생 화면 진입");
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
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
                    Log.e(TAG, "유튜브 화면 결과 = " + result);
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

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
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

    // 밑에 있는 리사이클러뷰에 다른 영상들의 썸네일과 이름을 보여줘야 한다
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
                item.setUrl_id(url_id);
                item.setTitle(title);
                // for문으로 리스트를 돌면서 지금 재생중인 영상 제목과 일치하는 영상을 찾는다
                boolean isDuplicated = false;
                for (int j = 0; j < other_list.size(); j++)
                {
                    // video_name의 값은 서버에서 받아온 이름 중 재생중인 영상명과 일치하는 영상이다
                    // 발견 시 true로 설정하고 for문 탈출
                    if (other_list.get(j).getTitle().equals(video_name))
                    {
                        isDuplicated = true;
                        break;
                    }
                }
                // false고 video_name과 다른 이름이 영상일 경우에만 하단 리사이클러뷰에서 사용하는 리스트에 데이터들을 넣어 화면에 뿌린다
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
        Log.e(TAG, "count = " + youtube_count);
        for (int i = 0; i < other_list.size(); i++)
        {
            Log.e("하단 리사이클러뷰에 들어갈 영상 제목들", "other_list = " + other_list.get(i).getTitle());
        }
        Log.e("jsonParsing()", "key_name = " + key_name);

        // 하단 리사이클러뷰에 들어갈 어댑터
        adapter = new OtherYoutubeAdapter(YoutubeActivity.this, other_list, itemClickListener);
        adapter.setOnItemClickListener(((view, pos) -> {
            String youtube_url_id = other_list.get(pos).getUrl_id();
            String thumbnail = other_list.get(pos).getThumbnail();
            String title = other_list.get(pos).getTitle();
            Log.e(TAG, "url_id = " + youtube_url_id + ", 썸네일 = " + thumbnail + " 제목 = " + title);
            /* 하단 리사이클러뷰의 영상을 누르면 그 영상을 재생시키기 위해 이 액티비티를 재시작해야 한다
            * 이걸 구현하려면 new Intent()를 쓰는 게 아닌 getIntent()를 쓰고 finish()로 이 액티비티를 지운 다음, intent를 시작하면
            * 선택한 영상의 제목을 갖고 액티비티를 재시작하는 효과를 낸다. 액티비티 스택이 쌓이지 않기 때문에 뒤로가기를 눌러도 MainFragment로 이동된다 */
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
        // 뒤로가기를 눌렀을 시 어디서 어디로 이동했다고 서버로 보낸다
        userLog("유튜브 화면에서 뒤로가기 눌러 메인으로 이동");
    }

    /* 사용자 행동 정보 저장 메서드 */
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
                    Log.e(TAG, "유튜브 화면에서 백버튼 누름 : " + result);
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

}