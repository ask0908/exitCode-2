package com.psj.welfare.test;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.psj.welfare.data.MainCategoryBottomItem;
import com.psj.welfare.data.MainThreeDataItem;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    MainCategoryAdapter upAdapter;
    MainCategoryAdapter.ItemClickListener up_clickListener;
    List<MainThreeDataItem> keyword_list;
    MainThreeDataItem all_item;

    // 상단 리사이클러뷰에서 선택한 값에 따라 내용물이 바뀌는 "하단 리사이클러뷰"
    // 3개만 보여준다
    RecyclerView down_recycler;
    MainDownAdapter downAdapter;
    MainDownAdapter.ItemClickListener downClickListener;
    List<MainThreeDataItem> down_list;

    List<MainThreeDataItem> other_list;

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
    String youtube_id, youtube_title, youtube_thumbnail, youtube_videoId;
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
        other_list = new ArrayList<>();

        helper = new DBOpenHelper(getActivity());
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
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

        showWelfareAndYoutubeNotLogin();

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
        keyword_list = new ArrayList<>();
        down_list = new ArrayList<>();

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

                /* 메인에 보여줄 3개 혜택 데이터 */
                JSONArray welf_datas = inner_json.getJSONArray("welf_data");
                for (int j = 0; j < welf_datas.length(); j++)
                {
                    JSONObject data = welf_datas.getJSONObject(j);
                    welfare_id = data.getString("welf_id");
                    welfare_name = data.getString("welf_name");
                    welfare_tag = data.getString("welf_tag");
                    welfare_field = data.getString("welf_field");

                    /* MainCategoryItem : 상단 리사이클러뷰의 리스트에 넣을 모델 클래스
                     * MainCategoryBottomItem : 하단 리사이클러뷰의 리스트에 넣을 모델 클래스 */

                    // 상단 리사이클러뷰에 넣을 테마(welf_field)를 저장할 객체 생성
                    MainThreeDataItem item = new MainThreeDataItem();
                    item.setWelf_name(welfare_name);
                    item.setWelf_field(welfare_field);
                    item.setWelf_tag(welfare_tag);

                    // 하단 리사이클러뷰에 넣을 값들
                    MainCategoryBottomItem bottomItem = new MainCategoryBottomItem();
                    bottomItem.setWelf_name(welfare_name);
                    bottomItem.setWelf_field(welfare_field);
                    bottomItem.setWelf_tag(welfare_tag);

                    keyword_list.add(item);
                    down_list.add(item);
                }
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* 유튜브 해시맵에 영상 이름과 videoId 값을 넣어서 인텐트로 넘길 준비를 한다 */
        for (int i = 0; i < youtube_list.size(); i++)
        {
            youtube_hashmap.put(youtube_list.get(i).getYoutube_name(), youtube_list.get(i).getYoutube_videoId());
        }

        /* 여기에 하단 리사이클러뷰 선언을 해야 처음 메인에 들어왔을 때도 혜택들이 보인다 */
        downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
        downAdapter.setOnItemClickListener((view1, pos1) ->
        {
            String name = down_list.get(pos1).getWelf_name();
            String field = down_list.get(pos1).getWelf_field();
        });
        down_recycler.setAdapter(downAdapter);

        /* 상단 리사이클러뷰에 들어갈 테마(교육, 공부 등)의 중복처리 로직 시작 */
        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder welfareNameBuilder = new StringBuilder();
        StringBuilder welfareFieldBuilder = new StringBuilder();
        StringBuilder welfareTagBuilder = new StringBuilder();

        /* 상단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 'OO 지원' 사이사이에 ';;'을 붙인다 */
        for (int i = 0; i < keyword_list.size(); i++)
        {
            stringBuilder.append(keyword_list.get(i).getWelf_field()).append(";;");
            welfareTagBuilder.append(keyword_list.get(i).getWelf_tag()).append(";;");
        }

        /* 하단 리사이클러뷰에 쓰는 리스트 크기만큼 반복해 혜택명 사이에 ';;'을 붙인다 */
        for (int i = 0; i < down_list.size(); i++)
        {
            welfareNameBuilder.append(down_list.get(i).getWelf_name()).append(";;");
            welfareFieldBuilder.append(down_list.get(i).getWelf_field()).append(";;");
        }

        // ";;"가 섞인 문자열을 구분자로 각각 split
        String[] arr = stringBuilder.toString().split(";;");
        String[] nameArr = welfareNameBuilder.toString().split(";;");
        String[] fieldArr = welfareFieldBuilder.toString().split(";;");
        String[] tagArr = welfareTagBuilder.toString().split(";;");

        // 중복되는 것들을 없애기 위해 HashSet 사용
        nameArr = new LinkedHashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);
        fieldArr = new HashSet<>(Arrays.asList(fieldArr)).toArray(new String[0]);

        keyword_list.clear();

        for (int i = 0; i < fieldArr.length; i++)
        {
            all_item = new MainThreeDataItem();
            MainThreeDataItem item = new MainThreeDataItem();
            item.setWelf_field(fieldArr[i]);
            item.setWelf_name(nameArr[i]);
            item.setWelf_tag(tagArr[i]);
            all_item.setWelf_field(fieldArr[i]);
            all_item.setWelf_name(nameArr[i]);
            all_item.setWelf_tag(tagArr[i]);
            keyword_list.add(item);
            other_list.add(all_item);
        }

        for (int i = 0; i < keyword_list.size(); i++)
        {
            Log.e(TAG, "태그 확인 : " + keyword_list.get(i).getWelf_tag());
        }

        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
        // keyword_list 크기가 0일 경우 아래에서 에러가 발생한다
        if (!keyword_list.get(0).getWelf_field().equals("전체") && !keyword_list.contains("전체"))
        {
            keyword_list.add(0, new MainThreeDataItem("전체"));
        }

        // 상단 리사이클러뷰에 붙일 어댑터 초기화
        upAdapter = new MainCategoryAdapter(getActivity(), keyword_list, up_clickListener);
        upAdapter.setOnItemClickListener(((view, pos) ->
        {
            /* 상단 리사이클러뷰에서 전체를 클릭한 경우
             * 메서드를 한번 더 호출하지 않고 랜덤하게 3개의 데이터를 뽑아서 하단 리사이클러뷰에 데이터를 set해야 한다 */
            if (keyword_list.get(pos).getWelf_field().equals("전체"))
            {
                // 전체 필터를 눌렀을 때는 모든 데이터가 들어있는 리스트를 어댑터 초기화 시 넘겨줌으로써 더보기를 눌러도 모든 데이털르 볼 수 있도록 한다
                downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
                downAdapter.setOnItemClickListener((view1, pos1) ->
                {
                    String name = down_list.get(pos1).getWelf_name();
                    String field = down_list.get(pos1).getWelf_field();
                });
                down_recycler.setAdapter(downAdapter);
            }
            else
            {
                /* 상단 리사이클러뷰에서 전체 이외의 필터를 클릭한 경우
                 * 여기서 선택한 'OO 지원'에 맞는 혜택들로 하단 리사이클러뷰 어댑터 초기화 시 사용되는 리스트를 채워야 한다 */
                other_list.clear();

                /* 선택한 OO 지원에 속하는 혜택들만을 리사이클러뷰에 보여주는 처리 시작 */
                // 현금 지원을 선택했으면 전체 데이터가 담긴 리스트에서 welf_category가 현금 지원인 혜택들을 리스트에 담아서 하단 리사이클러뷰 어댑터에 넣어야 한다
                for (int i = 0; i < down_list.size(); i++)
                {
                    MainThreeDataItem item = new MainThreeDataItem();
                    if (down_list.get(i).getWelf_field().contains(keyword_list.get(pos).getWelf_field()))
                    {
                        item.setWelf_field(keyword_list.get(pos).getWelf_field());
                        item.setWelf_name(down_list.get(i).getWelf_name());
                        item.setWelf_tag(down_list.get(i).getWelf_tag());
                        other_list.add(item);
                    }
                }

                // 하단 리사이클러뷰에 어댑터 set
                downAdapter = new MainDownAdapter(getActivity(), other_list, downClickListener);
                downAdapter.setOnItemClickListener((view1, pos1) ->
                {
                    String name = other_list.get(pos1).getWelf_name();
                    String field = other_list.get(pos1).getWelf_field();
                });
                down_recycler.setAdapter(downAdapter);
            }
        }));

        up_recycler.setAdapter(upAdapter);

        youtubeAdapter = new MainHorizontalYoutubeAdapter(getActivity(), youtube_list, youtubeClickListener);
        youtubeAdapter.setOnItemClickListener(new MainHorizontalYoutubeAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View v, int pos)
            {
                /* 수정 중이라 주석 처리 */
//                HorizontalYoutubeItem item = new HorizontalYoutubeItem();
//                item.setYoutube_id(youtube_id);
//                item.setYoutube_name(youtube_title);
//                item.setYoutube_thumbnail(youtube_thumbnail);
//                item.setYoutube_videoId(youtube_videoId);
//                Intent intent = new Intent(getActivity(), YoutubeActivity.class);
//                String youtube_name = youtube_list.get(pos).getYoutube_name();
//                Log.e(TAG, "선택한 유튜브 영상 : " + youtube_list.get(pos).getYoutube_name());
//                intent.putExtra("youtube_information", item);
//                intent.putExtra("youtube_hashmap", youtube_hashmap);
//                Bundle bundle = new Bundle();
//                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
//                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//                startActivity(intent);
            }
        });
        youtube_video_recyclerview.setAdapter(youtubeAdapter);

    }

}