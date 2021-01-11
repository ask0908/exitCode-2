package com.psj.welfare.Test;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.YoutubeItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.YoutubeAdapter;
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

public class YoutubeTestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getName();

    Toolbar youtube_test_toolbar;

    RecyclerView youtube_recycler;
    YoutubeAdapter adapter;
    List<YoutubeItem> youtube_list;

    String url_id, thumbnail, title, youtube_count;
    List<String> url_id_list;

    YoutubeItem youtube;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_test);

        youtube_test_toolbar = findViewById(R.id.youtube_test_toolbar);
        setSupportActionBar(youtube_test_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("유튜버 리뷰");

        youtube_recycler = findViewById(R.id.youtube_recycler);
        youtube_recycler.setHasFixedSize(true);
        youtube_recycler.setLayoutManager(new LinearLayoutManager(this));

        getYoutubeInformation();
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
                youtube = new YoutubeItem();
//                youtube.setThumbnail(thumbnail);
                youtube.setUrl_id(url_id);
//                youtube.setTitle(title);
                youtube_list.add(youtube);
                url_id_list.add(url_id);
            }
            youtube_count = jsonObject.getString("TotalCount");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        // 로그로 확인
        for (int i = 0 ; i < url_id_list.size(); i++)
        {
            Log.e(TAG, "url_id_list = " + url_id_list.get(i));  // 값 출력 확인
        }
        for (int i = 0; i < youtube_list.size(); i++)
        {
            Log.e(TAG, "youtube_list = " + youtube_list.get(i));
        }
        adapter = new YoutubeAdapter(YoutubeTestActivity.this, youtube_list);
        youtube_recycler.setAdapter(adapter);
    }
}