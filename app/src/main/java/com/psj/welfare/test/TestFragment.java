package com.psj.welfare.test;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MainDownAdapter;
import com.psj.welfare.adapter.MainHorizontalYoutubeAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.HorizontalYoutubeItem;
import com.psj.welfare.data.MainThreeDataItem;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 테스트 완료 후 MainFragment에 xml과 같이 복붙해야 한다 */
public class TestFragment extends Fragment
{
    public static final String TAG = "TestFragment";

    // 20대, 강원, 여성
    TextView selected_interest_textview;
    // 더보기(화면 이동)
    TextView more_see_textview;

    // 편집하기 버튼
    Button edit_interest_btn;

    // 전체, 건강, 교육, 교통 등 관심 주제를 가로로 보여줄 상단 리사이클러뷰
    RecyclerView up_recycler;
    TestUpAdapter upAdapter;
    TestUpAdapter.ItemClickListener up_clickListener;
    List<TestUpModel> up_list;
    // 상단 리사이클러뷰에서 선택한 관심 주제에 따라 내용물을 바꿀 하단 리사이클러뷰, 3개만 보여준다
    RecyclerView down_recycler;
    MainDownAdapter downAdapter;
    MainDownAdapter.ItemClickListener downClickListener;
    List<MainThreeDataItem> down_list;

    // 유튜브 영상 가로로 보여줄 리사이클러뷰, 첫 아이템은 모두 보여주고 바로 옆의 아이템은 조금 잘려서 보이도록 한다
    RecyclerView youtube_video_recyclerview;
    MainHorizontalYoutubeAdapter youtubeAdapter;
    MainHorizontalYoutubeAdapter.ItemClickListener youtubeClickListener;
    List<HorizontalYoutubeItem> youtube_list;

    DBOpenHelper helper;
    String sqlite_token;

    HashMap<String, String> youtube_hashmap;

    int count_int;

    boolean loggedOut = false;

    String thumbnail, title, videoId;
    String welf_name, welf_local, welf_category, tag, count;

    private FirebaseAnalytics analytics;

    // 새 서버에서 가져온 유튜브 데이터를 저장할 변수
    String youtube_id, youtube_title, youtube_thumbnail;
    // 새 서버에서 가져온 혜택 데이터를 저장할 변수. 메인 화면의 3개 아이템을 보여주는 리사이클러뷰에 이 변수에 담긴 값들을 set한다
    String welfare_id, welfare_name, welfare_tag, welfare_field;

    public TestFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_test, container, false);

        selected_interest_textview = view.findViewById(R.id.selected_interest_textview);
        more_see_textview = view.findViewById(R.id.more_see_textview);
        edit_interest_btn = view.findViewById(R.id.edit_interest_btn);
        up_recycler = view.findViewById(R.id.interest_subject_recyclerview);
        down_recycler = view.findViewById(R.id.interest_result_recyclerview);
        youtube_video_recyclerview = view.findViewById(R.id.youtube_video_recyclerview);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        down_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        youtube_hashmap = new HashMap<>();
        youtube_list = new ArrayList<>();
        up_list = new ArrayList<>();
        down_list = new ArrayList<>();

        helper = new DBOpenHelper(getActivity());
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while(cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        // view 100+ -> view 0으로 변경
        // 편집하기 각각 왼쪽, 오른쪽 간격 수정

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        // 상단 리사이클러뷰에서 어떻게 보이는지 확인하기 위해 하드코딩으로 테스트
        TestUpModel model = new TestUpModel("전체");
        TestUpModel model2 = new TestUpModel("건강");
        TestUpModel model3 = new TestUpModel("교육");
        TestUpModel model4 = new TestUpModel("교통");
        TestUpModel model5 = new TestUpModel("주거");
        up_list.add(model);
        up_list.add(model2);
        up_list.add(model3);
        up_list.add(model4);
        up_list.add(model5);
        upAdapter = new TestUpAdapter(getActivity(), up_list, up_clickListener);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        up_recycler.setLayoutManager(llm);
        up_recycler.addItemDecoration(new RecyclerViewDecoration(40));
        up_recycler.setAdapter(upAdapter);

        /* 하단 리사이클러뷰에서 어떻게 보이는지 확인하기 위해 하드코딩으로 테스트 */
//        TestUpModel model6 = new TestUpModel("코로나19 생활지원비", "");
//        TestUpModel model7 = new TestUpModel("2020 관광통역안내사 전시해설", "#문화");
//        TestUpModel model8 = new TestUpModel("고혈압 당뇨병 등록관리 서비스 제공", "#청년 #중장년 #노년");
//        down_list.add(model6);
//        down_list.add(model7);
//        down_list.add(model8);
//        downAdapter = new TestDownAdapter(getActivity(), down_list, down_clickListener);
//        downAdapter.setOnItemClickListener(new TestDownAdapter.ItemClickListener()
//        {
//            @Override
//            public void onItemClick(View v, int pos)
//            {
//                Toast.makeText(getActivity(), "클릭한 아이템 : " + down_list.get(pos).getWelf_name(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        down_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
//        down_recycler.setAdapter(downAdapter);

        showWelfareAndYoutubeNotLogin();

        more_see_textview.setOnClickListener(v -> Toast.makeText(getActivity(), "더보기 클릭", Toast.LENGTH_SHORT).show());

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

                    HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                    item.setYoutube_id(youtube_id);
                    item.setYoutube_name(youtube_title);
                    item.setYoutube_thumbnail(youtube_thumbnail);
                    youtube_list.add(item);
                }

                /* 메인에 보여줄 3개 혜택 데이터 */
                JSONArray welf_datas = inner_json.getJSONArray("welf_data");
                for (int j = 0; j < welf_datas.length(); j++)
                {
                    JSONObject data = welf_datas.getJSONObject(j);
                    welfare_id = data.getString("welf_id");
                    welfare_name = data.getString("welf_name");
                    welfare_tag = data.getString("welf_tag");
                    welfare_field = data.getString("welf_field");

                    MainThreeDataItem item = new MainThreeDataItem();
                    item.setWelf_id(welfare_id);
                    item.setWelf_name(welfare_name);
                    item.setWelf_tag(welfare_tag);
                    item.setWelf_field(welfare_field);
                    down_list.add(item);
                }
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        youtubeAdapter = new MainHorizontalYoutubeAdapter(getActivity(), youtube_list, youtubeClickListener);
        youtubeAdapter.setOnItemClickListener(new MainHorizontalYoutubeAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View v, int pos)
            {
//                Intent intent = new Intent(getActivity(), YoutubeActivity.class);
//                String youtube_name = youtube_list.get(pos).getYoutube_name();
                Log.e(TAG, "선택한 유튜브 영상 : " + youtube_list.get(pos).getYoutube_name());
//                intent.putExtra("youtube_name", youtube_name);
//                intent.putExtra("youtube_hashmap", youtube_hashmap);
//                Bundle bundle = new Bundle();
//                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
//                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                startActivity(intent);
            }
        });
        youtube_video_recyclerview.setAdapter(youtubeAdapter);

        downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
        down_recycler.setAdapter(downAdapter);

    }

    /* 새 서버의 메서드 사용 위해 주석 처리 */
//    void getYoutubeInformation()
//    {
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
//                    Log.e(TAG, "유튜브 영상 가져오기 결과 : " + result);
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
//    private void jsonParsing(String result)
//    {
//        youtube_hashmap = new HashMap<>();
//        try
//        {
//            JSONObject jsonObject = new JSONObject(result);
//            JSONArray jsonArray = jsonObject.getJSONArray("Message");
//            for (int i = 0; i < jsonArray.length(); i++)
//            {
//                JSONObject inner_obj = jsonArray.getJSONObject(i);
//                thumbnail = inner_obj.getString("thumbnail");
//                title = inner_obj.getString("title");
//                videoId = inner_obj.getString("videoId");
//                // 리사이클러뷰에 JSONObject의 개수만큼 데이터를 뿌리기 위해 객체, 리스트, setter 활용
//                HorizontalYoutubeItem item = new HorizontalYoutubeItem();
//                item.setYoutube_name(title);
//                item.setYoutube_thumbnail(thumbnail);
//                item.setYoutube_id(videoId);
//                youtube_list.add(item);
//            }
//        }
//        catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < youtube_list.size(); i++)
//        {
//            youtube_hashmap.put(youtube_list.get(i).getYoutube_name(), youtube_list.get(i).getYoutube_id());
//        }
//        youtubeAdapter = new TestYoutubeAdapter(getActivity(), youtube_list, youtubeClickListener);
//        youtubeAdapter.setOnItemClickListener((view, position) ->
//        {
//            Intent intent = new Intent(getActivity(), YoutubeActivity.class);
////            Intent intent = new Intent(getActivity(), YoutubeTestActivity.class);
//            String youtube_name = youtube_list.get(position).getYoutube_name();
//            intent.putExtra("youtube_name", youtube_name);
//            intent.putExtra("youtube_hashmap", youtube_hashmap);
//            Bundle bundle = new Bundle();
//            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
//            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//            startActivity(intent);
//        });
//        youtube_video_recyclerview.addItemDecoration(new RecyclerViewDecoration(40));
//        youtube_video_recyclerview.setAdapter(youtubeAdapter);
//
//    }

}