package com.psj.welfare.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.activity.RegionChooseActivity;
import com.psj.welfare.activity.ThemeChooseActivity;
import com.psj.welfare.activity.YoutubeActivity;
import com.psj.welfare.adapter.HorizontalYoutubeAdapter;
import com.psj.welfare.adapter.RecommendAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.data.HorizontalYoutubeItem;
import com.psj.welfare.data.RecommendItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment
{
    public static final String TAG = "MainFragment";

    ConstraintLayout main_top, go_benefit_layout, main_top_layout, middle_layout;
    TextView main_fragment_textview, youtube_text;
    ImageView theme_image, region_image;

    Button kakao_unlink_btn;
    Button kakao_logout_btn;

    TextView benefit_count_textview;

    RecyclerView youtube_video_recyclerview;
    HorizontalYoutubeAdapter adapter;
    HorizontalYoutubeAdapter.ItemClickListener itemClickListener;
    List<HorizontalYoutubeItem> lists;

    String thumbnail, title, videoId;

    SharedPreferences sharedPreferences;

    String welf_name, welf_local, welf_category, tag, count;

    RecyclerView recom_recycler;
    RecommendAdapter recommendAdapter;
    RecommendAdapter.ItemClickListener recom_itemClickListener;
    List<RecommendItem> list;
    TextView btn_left_textview;
    Button find_welfare_btn;

    public String status = "";

    HashMap<String, String> youtube_hashmap;

    int count_int;

    boolean loggedOut = false;

    private final int LOCATION = 1001;

    private FirebaseAnalytics analytics;

    CardView buttonTheme, buttonRegion;

    String sqlite_token;

    DBOpenHelper helper;

    public MainFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        main_top_layout = view.findViewById(R.id.main_top_layout);
        middle_layout = view.findViewById(R.id.middle_layout);
        main_top = view.findViewById(R.id.main_top);
        go_benefit_layout = view.findViewById(R.id.go_benefit_layout);

        theme_image = view.findViewById(R.id.theme_image);
        region_image = view.findViewById(R.id.region_image);

        youtube_text = view.findViewById(R.id.youtube_text);
        main_fragment_textview = view.findViewById(R.id.main_fragment_textview);
        benefit_count_textview = view.findViewById(R.id.benefit_count_textview);
        recom_recycler = view.findViewById(R.id.recom_recycler);
        btn_left_textview = view.findViewById(R.id.btn_left_textview);
        find_welfare_btn = view.findViewById(R.id.find_welfare_btn);
        youtube_video_recyclerview = view.findViewById(R.id.youtube_video_recyclerview);
        buttonTheme = view.findViewById(R.id.buttonTheme);
        buttonRegion = view.findViewById(R.id.buttonRegion);
        kakao_logout_btn = view.findViewById(R.id.kakao_logout_btn);
        kakao_unlink_btn = view.findViewById(R.id.kakao_unlink_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

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

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        userOrderedWelfare();

        boolean isLogouted = sharedPreferences.getBoolean("logout", false);
        if (sharedPreferences.getString("interest", "") != null && isLogouted)
        {
            // 관심사가 있고 비로그인인 경우 비로그인 UI를 보여준다
            recom_recycler.setVisibility(View.GONE);
            btn_left_textview.setVisibility(View.VISIBLE);
            find_welfare_btn.setVisibility(View.VISIBLE);

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) middle_layout.getLayoutParams();
            lp.matchConstraintPercentHeight = (float) 0.65;
            middle_layout.setLayoutParams(lp);
            go_benefit_layout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.radius_25));
            main_top.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bottom_left_right_radius));
            ConstraintLayout.LayoutParams other_params = (ConstraintLayout.LayoutParams) main_fragment_textview.getLayoutParams();
            other_params.matchConstraintPercentHeight = (float) 0.16;
            main_fragment_textview.setLayoutParams(other_params);
            ConstraintLayout.LayoutParams region_params = (ConstraintLayout.LayoutParams) buttonRegion.getLayoutParams();
            region_params.verticalBias = (float) 0.55;
            region_params.matchConstraintPercentHeight = (float) 0.35;
            buttonRegion.setLayoutParams(region_params);
            ConstraintLayout.LayoutParams theme_params = (ConstraintLayout.LayoutParams) buttonTheme.getLayoutParams();
            theme_params.verticalBias = (float) 0.55;
            theme_params.matchConstraintPercentHeight = (float) 0.35;
            buttonTheme.setLayoutParams(theme_params);
            ConstraintLayout.LayoutParams find_welfare_btn_params = (ConstraintLayout.LayoutParams) find_welfare_btn.getLayoutParams();
            find_welfare_btn_params.verticalBias = (float) 0.4;
            find_welfare_btn.setLayoutParams(find_welfare_btn_params);

            ConstraintLayout.LayoutParams theme_image_params = (ConstraintLayout.LayoutParams) theme_image.getLayoutParams();
            theme_image_params.verticalBias = (float) 0.65;
            theme_image.setLayoutParams(theme_image_params);

            ConstraintLayout.LayoutParams region_image_params = (ConstraintLayout.LayoutParams) region_image.getLayoutParams();
            region_image_params.verticalBias = (float) 0.65;
            region_image.setLayoutParams(region_image_params);
        }
        else if (sharedPreferences.getString("interest", "") != null && !isLogouted)
        {
            // 관심사가 있고 로그인한 경우 로그인 UI를 보여준다
            recom_recycler.setVisibility(View.VISIBLE);
            btn_left_textview.setVisibility(View.GONE);
            find_welfare_btn.setVisibility(View.GONE);

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) middle_layout.getLayoutParams();
            lp.matchConstraintPercentHeight = (float) 0.52;
            middle_layout.setLayoutParams(lp);
            ConstraintLayout.LayoutParams other_params = (ConstraintLayout.LayoutParams) main_fragment_textview.getLayoutParams();
            other_params.matchConstraintPercentHeight = (float) 0.16;
            main_fragment_textview.setLayoutParams(other_params);
            ConstraintLayout.LayoutParams count_params = (ConstraintLayout.LayoutParams) benefit_count_textview.getLayoutParams();
            count_params.matchConstraintPercentHeight = (float) 0.15;
            benefit_count_textview.setLayoutParams(count_params);
            benefit_count_textview.setText("추천 혜택");
            go_benefit_layout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bottom_left_right_radius));
            main_top.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.radius_25));

            ConstraintLayout.LayoutParams region_params = (ConstraintLayout.LayoutParams) buttonRegion.getLayoutParams();
            region_params.verticalBias = (float) 0.35;
            region_params.matchConstraintPercentHeight = (float) 0.275;
            buttonRegion.setLayoutParams(region_params);
            ConstraintLayout.LayoutParams theme_params = (ConstraintLayout.LayoutParams) buttonTheme.getLayoutParams();
            theme_params.matchConstraintPercentHeight = (float) 0.275;
            theme_params.verticalBias = (float) 0.35;
            buttonTheme.setLayoutParams(theme_params);
        }
        else
        {
            // 그 외의 경우에는 모두 비로그인 UI를 보여준다
            recom_recycler.setVisibility(View.GONE);
            btn_left_textview.setVisibility(View.VISIBLE);
            find_welfare_btn.setVisibility(View.VISIBLE);

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) middle_layout.getLayoutParams();
            lp.matchConstraintPercentHeight = (float) 0.65;
            middle_layout.setLayoutParams(lp);
            go_benefit_layout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.radius_25));
            main_top.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bottom_left_right_radius));
            ConstraintLayout.LayoutParams other_params = (ConstraintLayout.LayoutParams) main_fragment_textview.getLayoutParams();
            other_params.matchConstraintPercentHeight = (float) 0.16;
            main_fragment_textview.setLayoutParams(other_params);
            ConstraintLayout.LayoutParams region_params = (ConstraintLayout.LayoutParams) buttonRegion.getLayoutParams();
            region_params.matchConstraintPercentHeight = (float) 0.25;
            buttonRegion.setLayoutParams(region_params);
            ConstraintLayout.LayoutParams theme_params = (ConstraintLayout.LayoutParams) buttonTheme.getLayoutParams();
            theme_params.matchConstraintPercentHeight = (float) 0.25;
            buttonTheme.setLayoutParams(theme_params);
            ConstraintLayout.LayoutParams textview_params = (ConstraintLayout.LayoutParams) youtube_text.getLayoutParams();
            textview_params.verticalBias = (float) 0.3;
            youtube_text.setLayoutParams(textview_params);
            ConstraintLayout.LayoutParams find_welfare_btn_params = (ConstraintLayout.LayoutParams) find_welfare_btn.getLayoutParams();
            find_welfare_btn_params.verticalBias = (float) 0.4;
            find_welfare_btn.setLayoutParams(find_welfare_btn_params);
        }


        youtube_hashmap = new HashMap<>();

        find_welfare_btn.setOnClickListener(v ->
        {
            if (sharedPreferences.getString("interest", "") == null)
            {
//                Intent intent = new Intent(getActivity(), ChoiceKeywordActivity.class);
//                startActivity(intent);
            }
            else
            {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                if (sharedPreferences.getBoolean("logout", false))
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "로그인 화면 이동");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    loggedOut = true;
                    intent.putExtra("loggedOut", loggedOut);
                    startActivity(intent);
                }
                else
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "로그인 화면 이동");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    loggedOut = false;
                    intent.putExtra("loggedOut", loggedOut);
                    startActivity(intent);
                }
            }
        });

        youtube_video_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));

        recom_recycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));

        if (sharedPreferences.getBoolean("logout", false))
        {
            recom_recycler.setVisibility(View.GONE);
            btn_left_textview.setVisibility(View.VISIBLE);
            find_welfare_btn.setVisibility(View.VISIBLE);
        }

        kakao_logout_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Toast.makeText(getActivity(), "로그아웃 클릭", Toast.LENGTH_SHORT).show();
                if (Session.getCurrentSession().getTokenInfo().getAccessToken() != null)
                {
                    String aaa = Session.getCurrentSession().getTokenInfo().getAccessToken();
                    UserManagement.getInstance().requestLogout(new LogoutResponseCallback()
                    {
                        @Override
                        public void onCompleteLogout()
                        {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                }
                else
                {
                    //
                }
            }
        });

        kakao_unlink_btn.setOnClickListener(v ->
        {
            final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
            new AlertDialog.Builder(getActivity()).setMessage(appendMessage).setPositiveButton(getString(R.string.com_kakao_ok_button), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback()
                    {
                        @Override
                        public void onFailure(ErrorResult errorResult)
                        {
                            //
                        }

                        @Override
                        public void onSessionClosed(ErrorResult errorResult)
                        {
                            //
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }

                        @Override
                        public void onNotSignedUp()
                        {
                            //
                        }

                        @Override
                        public void onSuccess(Long userId)
                        {
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();
                        }
                    });
                    dialog.dismiss();
                    getActivity().finish();
                }
            }).setNegativeButton(getString(R.string.com_kakao_cancel_button), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            }).show();
        });

        buttonTheme.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), ThemeChooseActivity.class);
            startActivity(intent);
        });

        buttonRegion.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), RegionChooseActivity.class);
            startActivity(intent);
        });

    }

    void userOrderedWelfare()
    {
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.userOrderedWelfare(sqlite_token, "customized", LogUtil.getUserLog());  // token -> sqlite_token으로 변경
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    recommendParsing(result);
                }
                else
                {
                    Log.e(TAG, "맞춤 혜택 가져오기 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "맞춤 혜택 가져오기 에러 : " + t.getMessage());
            }
        });
    }

    private void recommendParsing(String result)
    {
        list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            count = jsonObject.getString("TotalCount");
            status = jsonObject.getString("Status");
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_name = inner_obj.getString("welf_name");
                welf_local = inner_obj.getString("welf_local");
                welf_category = inner_obj.getString("welf_category");
                tag = inner_obj.getString("tag");

                RecommendItem item = new RecommendItem();
                item.setWelf_name(welf_name);
                item.setWelf_local(welf_local);
                item.setWelf_category(welf_category);
                item.setTag(tag);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (count != null)
        {
            count_int = Integer.parseInt(count);
        }
        recommendAdapter = new RecommendAdapter(getActivity(), list, recom_itemClickListener);
        recommendAdapter.setOnItemClickListener((view, pos) ->
        {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "맞춤 혜택 눌러서 상세보기 화면으로 이동");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            String name = list.get(pos).getWelf_name();
            Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("welf_local", list.get(pos).getWelf_local());
            startActivity(intent);
        });
        recom_recycler.setAdapter(recommendAdapter);
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
        lists = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                thumbnail = inner_obj.getString("thumbnail");
                title = inner_obj.getString("title");
                videoId = inner_obj.getString("videoId");
                // 리사이클러뷰에 JSONObject의 개수만큼 데이터를 뿌리기 위해 객체, 리스트, setter 활용
                HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                item.setYoutube_name(title);
                item.setYoutube_thumbnail(thumbnail);
                item.setYoutube_id(videoId);
                lists.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        for (int i = 0; i < lists.size(); i++)
        {
            youtube_hashmap.put(lists.get(i).getYoutube_name(), lists.get(i).getYoutube_id());
        }
        adapter = new HorizontalYoutubeAdapter(getActivity(), lists, itemClickListener);
        adapter.setOnItemClickListener((view, position) ->
        {
            Intent intent = new Intent(getActivity(), YoutubeActivity.class);
//            Intent intent = new Intent(getActivity(), YoutubeTestActivity.class);
            String youtube_name = lists.get(position).getYoutube_name();
            intent.putExtra("youtube_name", youtube_name);
            intent.putExtra("youtube_hashmap", youtube_hashmap);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
            startActivity(intent);
        });
        youtube_video_recyclerview.setAdapter(adapter);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        getYoutubeInformation();
    }

}