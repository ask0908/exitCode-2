package com.psj.welfare.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.adapter.MoreViewAdapter;
import com.psj.welfare.adapter.SeeMoreBottomAdapter;
import com.psj.welfare.data.MoreViewItem;
import com.psj.welfare.data.SeeMoreItem;
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
    SeeMoreBottomAdapter adapter;
    SeeMoreBottomAdapter.OnItemClickListener itemClickListener;
    List<SeeMoreItem> list;

    SharedPreferences sharedPreferences;
    boolean isLogin;
    MoreViewModel moreViewModel;

    // 서버 값 파싱할 때 값 담는 변수
    String welf_id, welf_name, welf_tag, welf_count, welf_field, welf_theme, total_page, total_count;
    String status;
    int total_pages;
    String sqlite_token, sessionId;

    // 페이징 시 메서드 인자로 넘기는 받아야 할 페이지 숫자를 담을 변수
    // parseInt()로 int로 바꾼 다음 ++해서 다시 String으로 바꿔 이 변수에 담아야 한다
    String current_page;
    int integer_page = 1;

    DBOpenHelper helper;

    private Parcelable recyclerViewState;

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

        // 상, 하단 리사이클러뷰에 사용할 리스트 초기화
        up_list = new ArrayList<>();
        list = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("choice");
        editor.apply();
        sessionId = sharedPreferences.getString("sessionId", "");

        isLogin = sharedPreferences.getBoolean("logout", false);

        if (isLogin)
        {
            // true = 로그아웃 상태 -> 비로그인 상태에서 더보기 눌렀을 경우 호출하는 메서드 실행
//            Log.e(TAG, "로그아웃함");
//            moreViewWelfareNotLogin();
        }
        else
        {
            // false = 로그인 상태 -> 로그인 상태에서 더보기 눌렀을 경우 호출하는 메서드 실행
//            Log.e(TAG, "로그인함");
            Log.e(TAG, "126번 줄의 moreViewWelfareLogin() 호출 및 페이징까지 동작");
            moreViewWelfareLogin(String.valueOf(integer_page), "all");

            // 리사이클러뷰 페이징 처리
            moreViewPaging(String.valueOf(integer_page), "all");
            Log.e(TAG, "129번 줄의 페이징 메서드 동작");
        }

        // 상단 리사이클러뷰에 값 세팅(전체, 교육, 근로 등)
        setCategory();

        /* 상단 리사이클러뷰 아이템 세팅(전체, 사업, 교육, 기타 등) 및 클릭 리스너 정의 */
        up_adapter = new MoreViewAdapter(this, up_list, up_clickListener);
        up_adapter.setOnItemClickListener(pos ->
        {
            final String name = up_list.get(pos).getWelf_thema();
            Log.e(TAG, "상단 리사이클러뷰에서 선택한 카테고리명 : " + name);
            // name을 누르면 상단 리사이클러뷰에서 선택한 아이템의 이름이 나오게 된다(전체, 사업, 교육 등)
            // 일단 여기서 api를 호출해 이름을 theme 인자로 넘기고, page와 같이 넘겨서 해당 카테고리의 값들을 받아온다
            list.clear();
            if (!name.equals("전체"))
            {
                Log.e(TAG, "상단 리사이클러뷰 클릭 이벤트 - 테마 : " + name + ", page : " + integer_page);
                moreViewWelfareLogin(String.valueOf(integer_page), name);
                Log.e(TAG, "150번 줄의 moreViewWelfareLogin() 호출됨");
                moreViewPaging(String.valueOf(integer_page), name);
                Log.e(TAG, "151번 줄의 페이징 메서드 동작");
                adapter.notifyDataSetChanged();
            }
            else
            {
                moreViewWelfareLogin(String.valueOf(integer_page), "all");
                Log.e(TAG, "158번 줄의 moreViewWelfareLogin() 호출됨");
                moreViewPaging(String.valueOf(integer_page), "all");
                Log.e(TAG, "160번 줄의 페이징 메서드 동작");
                adapter.notifyDataSetChanged();
            }
        });
        more_view_top_recyclerview.setAdapter(up_adapter);

    }

    // 리사이클러뷰 페이징 처리
    // 상단 리사이클러뷰에서 선택한 아이템에 따라 각각 다른 theme를 서버로 넘겨 값을 가져오고, 그 값들을 1페이지부터 페이징한다
    private void moreViewPaging(String page, String theme)
    {
        Log.e(TAG, "페이징 메서드 호출@@");
        more_view_bottom_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm != null)
                {
                    if (llm.getItemCount() != 0)
                    {
                        int totalItemCount = llm.getItemCount();
                        int lastVisible = llm.findLastCompletelyVisibleItemPosition();  // 마지막 아이템의 pos 값을 리턴하는 메서드

//                        String log_str = String.valueOf(integer_page);
                        if (!page.equals(String.valueOf(total_page)))   // 매개변수로 받은 page가 서버에서 받은 page와 같다면
                        {
                            if (lastVisible >= totalItemCount - 1)
                            {
                                int pages = Integer.parseInt(page);
                                pages++;
                                current_page = String.valueOf(pages);
                                // TODO : 이 아래 줄이 한번 페이징할 때마다 계속 호출되는데, 이전에 눌렀던 상단 리사이클러뷰 아이템들이 같이 로그에 찍혀서 그것들의 검색 결과까지 같이 보여준다
                                Log.e(TAG, "current_page : " + current_page + ", theme : " + theme);
                                moreViewWelfareLogin(current_page, theme);
                            }
                        }
                        else
                        {
                            Log.e(TAG, "서버에서 불러올 데이터가 없음");
                        }
                    }
                }
            }
        });
    }

    /* 로그인 후 더보기 눌렀을 경우 호출돼 데이터를 가져오는 메서드 */
    private void moreViewWelfareLogin(String page, String theme)
    {
        Log.e(TAG, "테마에 맞는 데이터 가져오기 메서드 호출!!");
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> moreViewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
//                    Log.e(TAG, "로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
                    messageParsing(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "로그인 상태에서 str이 null입니다");
                }
            }
        };

        // theme : 상단 리사이클러뷰에서 선택한 카테고리 이름을 넣는다
        moreViewModel.moreViewWelfareLogin(sqlite_token, sessionId, page, theme)
                .observe(this, moreViewObserver);
    }

    /* 비로그인 시 더보기 눌렀을 경우 호출돼 데이터를 가져오는 메서드 */
    private void moreViewWelfareNotLogin()
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> moreViewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "비로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
                    dialog.dismiss();
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

    /* 로그인 시 더보기를 눌렀을 경우 받아온 JSON 값을 파싱하는 메서드 */
    private void messageParsing(String result)
    {
        Log.e(TAG, "서버 리턴값 파싱하는 메서드 호출##");
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total_count = jsonObject.getString("total_num");
            total_page = jsonObject.getString("total_page");
            status = jsonObject.getString("statusCode");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                welf_id = inner_json.getString("welf_id");
                welf_name = inner_json.getString("welf_name");
                welf_tag = inner_json.getString("welf_tag");
                welf_theme = inner_json.getString("welf_theme");
                welf_count = inner_json.getString("welf_count");

                SeeMoreItem items = new SeeMoreItem();
                items.setWelf_id(welf_id);
                items.setWelf_name(welf_name);
                items.setWelf_tag(welf_tag);
                items.setWelf_count(welf_count);
                list.add(items);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 서버 응답 코드에 따른 예외처리
        if (status.equals("400") || status.equals("404") || status.equals("500"))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("인터넷 연결이 불안정해요\n잠시 후 다시 시도해 주세요")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
        }
        else
        {
            /* 서버 응답 코드가 200(성공)인 경우 */

            // 총 혜택 개수 null 체크
            if (total_count != null)
            {
                more_view_result_count.setText("맞춤 혜택 총 " + total_count + "개");
            }

            // 현재 페이지 null 체크
            if (total_page != null)
            {
                total_pages = Integer.parseInt(total_page);
            }

            // 다음 페이지 데이터 가져올 시 스크롤이 자동으로 맨 위로 올라가는 현상 방지
            recyclerViewState = more_view_bottom_recyclerview.getLayoutManager().onSaveInstanceState();

            // 하단 리사이클러뷰 초기화 및 클릭 리스너 정의
            // 아이템 클릭 시 상세보기 화면으로 이동
            adapter = new SeeMoreBottomAdapter(TestMoreViewActivity.this, list, itemClickListener);
            adapter.setOnItemClickListener(pos ->
            {
                String id = list.get(pos).getWelf_id();
                String name = list.get(pos).getWelf_name();
                String tag = list.get(pos).getWelf_tag();
                String count = list.get(pos).getWelf_count();
                Log.e(TAG, "하단 리사이클러뷰의 아이템 이름 : " + name + ", 조회수 : " + count + ", id : " + id + ", 태그 : " + tag);
                Intent intent = new Intent(this, DetailTabLayoutActivity.class);
                intent.putExtra("welf_id", id);
                startActivity(intent);
            });
            more_view_bottom_recyclerview.setAdapter(adapter);
            more_view_bottom_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }

    }

    // 상단 리사이클러뷰에 값 세팅(전체, 교육, 근로 등)
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