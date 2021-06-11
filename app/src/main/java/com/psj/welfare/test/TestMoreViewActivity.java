package com.psj.welfare.test;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MoreBottomAdapter;
import com.psj.welfare.adapter.MoreViewAdapter;
import com.psj.welfare.data.MainThreeDataItem;
import com.psj.welfare.data.MoreViewItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.viewmodel.MoreViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TestMoreViewActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // 더보기를 눌렀을 때 전체, 건강, 교육 카테고리 별 총 몇 개의 혜택개수가 나왔는지 보여줄 텍스트뷰
    TextView more_view_result_count;

    // TestFragment와 같이 필터들을 선택할 상단 리사이클러뷰
    RecyclerView more_view_top_recyclerview;
    MoreViewAdapter up_adapter;
    MoreViewAdapter.ItemClickListener up_clickListener;
    List<MoreViewItem> up_list;

    // 상단 리사이클러뷰에서 선택한 카테고리에 따라 값이 바뀌는 하단 리사이클러뷰
    RecyclerView more_view_bottom_recyclerview;
    MoreBottomAdapter bottom_adapter;
    MoreBottomAdapter.ItemClickListener bottom_clickListener;
    List<MainThreeDataItem> bottom_list;

    SharedPreferences sharedPreferences;
    boolean isLogin;
    MoreViewModel moreViewModel;

    // 비로그인/로그인 상관없이 공통으로 쓰는 변수
    String welf_id, welf_name, welf_tag, welf_count, welf_theme, total_page, total_count;
    int total_pages;
    String sqlite_token, sessionId;

    DBOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_test_more_view);

        helper = new DBOpenHelper(TestMoreViewActivity.this);
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

        more_view_result_count = findViewById(R.id.more_view_result_count);
        more_view_top_recyclerview = findViewById(R.id.more_view_top_recyclerview);
        more_view_bottom_recyclerview = findViewById(R.id.more_view_bottom_recyclerview);

        more_view_bottom_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        up_list = new ArrayList<>();
        bottom_list = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);
        sessionId = sharedPreferences.getString("sessionId", "");

        Log.e(TAG, "액티비티에서 가져온 토큰 : " + sqlite_token + ", 세션 : " + sessionId);
        isLogin = sharedPreferences.getBoolean("logout", false);
        Log.e(TAG, "더보기 눌렀을 때 로그아웃 상태 : " + isLogin);

        moreViewWelfareNotLogin();
//        if (isLogin)
//        {
//            // true = 로그아웃 상태 -> 비로그인 상태에서 더보기 눌렀을 경우 호출하는 메서드 실행
//            moreViewWelfareNotLogin();
//        }
//        else
//        {
//            // false = 로그인 상태 -> 로그인 상태에서 더보기 눌렀을 경우 호출하는 메서드 실행
//            moreViewWelfareLogin();
//        }
    }

    private void moreViewWelfareLogin()
    {
        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> moreViewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
                }
                else
                {
                    Log.e(TAG, "로그인 상태에서 str이 null입니다");
                }
            }
        };

        // theme : 상단 리사이클러뷰에서 선택한 카테고리 이름을 넣는다
        moreViewModel.moreViewWelfareLogin(sqlite_token, sessionId, "1", "교육")
                .observe(this, moreViewObserver);
    }

    private void moreViewWelfareNotLogin()
    {
//        final ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setMax(100);
//        dialog.setMessage("잠시만 기다려 주세요...");
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        dialog.show();

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> moreViewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "비로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
//                    messageParsing(str);
                    gsonParsing(str);
//                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "비로그인 상태에서 str이 null입니다");
                }
            }
        };

        // theme : 상단 리사이클러뷰에서 선택한 카테고리 이름을 넣는다
        // gender, age, local : 미리보기 부분의 파일이 없어 하드코딩으로 대신
        moreViewModel.moreViewWelfareNotLogin("1", "start", "남성", "20", "서울")
                .observe(this, moreViewObserver);
    }

    private void gsonParsing(String result)
    {
        final Gson gson = new GsonBuilder().create();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                Log.e(TAG, "inner_json : " + inner_json);

                JSONObject jsonObject1 = new JSONObject(String.valueOf(inner_json));
                JSONArray jsonArray1 = jsonObject1.getJSONArray("theme_10");
                for (int j = 0; j < jsonArray1.length(); j++)
                {
                    JSONObject inner_json2 = jsonArray1.getJSONObject(j);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

    /* 비로그인/로그인 상관없이 공통으로 쓰는 JSON 파싱 메서드 */
    private void messageParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            welf_theme = jsonObject.getString("welf_theme");
            total_page = jsonObject.getString("total_page");
            total_count = jsonObject.getString("total_num");

            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                welf_id = inner_json.getString("welf_id");
                welf_name = inner_json.getString("welf_name");
                welf_tag = inner_json.getString("welf_tag");
                welf_count = inner_json.getString("welf_count");

                MainThreeDataItem item = new MainThreeDataItem();
                item.setWelf_id(welf_id);
                item.setWelf_name(welf_name);
                item.setWelf_tag(welf_tag);
                item.setWelf_count(welf_count);
                bottom_list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        setCategory();
        more_view_result_count.setText("맞춤 혜택 총 " + total_count + "개");

        total_pages = Integer.parseInt(total_page);

        up_adapter = new MoreViewAdapter(this, up_list, up_clickListener);
        up_adapter.setOnItemClickListener(pos ->
        {
            String name = up_list.get(pos).getWelf_thema();
            Log.e(TAG, "상단 리사이클러뷰에서 선택한 카테고리명 : " + name);
        });
        more_view_top_recyclerview.setAdapter(up_adapter);

        bottom_adapter = new MoreBottomAdapter(this, bottom_list, bottom_clickListener);
        bottom_adapter.setOnBottomClickListener(pos ->
        {
            String name = bottom_list.get(pos).getWelf_name();
            String count = bottom_list.get(pos).getWelf_count();
            String id = bottom_list.get(pos).getWelf_id();
            String tag = bottom_list.get(pos).getWelf_tag();
            Log.e(TAG, "하단 리사이클러뷰의 아이템 이름 : " + name + ", 조회수 : " + count + ", id : " + id + ", 태그 : " + tag);
        });
        more_view_bottom_recyclerview.setAdapter(bottom_adapter);

    }

    public void setCategory()
    {
        MoreViewItem item = new MoreViewItem("전체");
        MoreViewItem item2 = new MoreViewItem("사업");
        MoreViewItem item3 = new MoreViewItem("교육");
        MoreViewItem item4 = new MoreViewItem("기타");
        MoreViewItem item5 = new MoreViewItem("문화");
        MoreViewItem item6 = new MoreViewItem("주거");
        MoreViewItem item7 = new MoreViewItem("교통");
        MoreViewItem item8 = new MoreViewItem("건강");
        MoreViewItem item9 = new MoreViewItem("근로");
        MoreViewItem item10 = new MoreViewItem("금융");
        MoreViewItem item11 = new MoreViewItem("환경");
        up_list.add(item);
        up_list.add(item2);
        up_list.add(item3);
        up_list.add(item4);
        up_list.add(item5);
        up_list.add(item6);
        up_list.add(item7);
        up_list.add(item8);
        up_list.add(item9);
        up_list.add(item10);
        up_list.add(item11);
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