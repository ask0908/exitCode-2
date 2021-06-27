package com.psj.welfare.test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    ArrayList<MoreViewItem> noRepeat;

    // 중복없는 값들을 담아서 상단 리사이클러뷰에 보여줄 리스트
    ArrayList<MoreViewItem> noDuplicateList;

    SharedPreferences sharedPreferences;
    boolean isLogin;
    MoreViewModel moreViewModel;

    // 서버 값 파싱할 때 값 담는 변수
    String welf_id, welf_name, welf_tag, welf_count, bottom_assist_method, total_page;
    // 상단 리사이클러뷰에 넣을 값을 담을 변수
    String top_assist_method;
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
        noDuplicateList = new ArrayList<>();

        // 상단 리사이클러뷰의 첫 번째 아이템은 전체여야 한다. 전체를 누르면 모든 결과를 보여줘야 한다(인자로 all 넘기기)
        up_list.add(0, new MoreViewItem("전체"));

        sharedPreferences = getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("choice");
        editor.apply();
        sessionId = sharedPreferences.getString("sessionId", "");
        Log.e(TAG, "세션 id : " + sessionId);

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
            Log.e(TAG, "126번 줄의 moreViewWelfareLogin() 호출 및 페이징까지 동작");
            moreViewWelfareLogin(String.valueOf(integer_page), "start");

            /* 처음 이 화면에 들어왔을 때는 로그인이든 비로그인이든 페이징 처리 없이 데이터를 보여준다 */
//            moreViewPaging(String.valueOf(integer_page), "all");
            Log.e(TAG, "129번 줄의 페이징 메서드 동작");
        }

        // 상단 리사이클러뷰에 값 세팅(전체, 교육, 근로 등)
//        setCategory();

        /* 상단 리사이클러뷰 아이템 세팅(전체, 사업, 교육, 기타 등) 및 클릭 리스너 정의 */
//        up_adapter = new MoreViewAdapter(this, up_list, up_clickListener);
//        up_adapter.setOnItemClickListener(pos ->
//        {
//            final String name = up_list.get(pos).getWelf_thema();
//            Log.e(TAG, "상단 리사이클러뷰에서 선택한 카테고리명 : " + name);
//            // name을 누르면 상단 리사이클러뷰에서 선택한 아이템의 이름이 나오게 된다(전체, 사업, 교육 등)
//            // 일단 여기서 api를 호출해 이름을 theme 인자로 넘기고, page와 같이 넘겨서 해당 카테고리의 값들을 받아온다
//            list.clear();
//            if (!name.equals("전체"))
//            {
//                Log.e(TAG, "상단 리사이클러뷰 클릭 이벤트 - 테마 : " + name + ", page : " + integer_page);
//                moreViewWelfareLogin(String.valueOf(integer_page), name);
//                Log.e(TAG, "150번 줄의 moreViewWelfareLogin() 호출됨");
//                moreViewPaging(String.valueOf(integer_page), name);
//                Log.e(TAG, "151번 줄의 페이징 메서드 동작");
//                adapter.notifyDataSetChanged();
//            }
//            else
//            {
//                moreViewWelfareLogin(String.valueOf(integer_page), "all");
//                Log.e(TAG, "158번 줄의 moreViewWelfareLogin() 호출됨");
//                moreViewPaging(String.valueOf(integer_page), "all");
//                Log.e(TAG, "160번 줄의 페이징 메서드 동작");
//                adapter.notifyDataSetChanged();
//            }
//        });
//        more_view_top_recyclerview.setAdapter(up_adapter);

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
                                // TODO : 이 아래 줄이 한번 페이징할 때마다 계속 호출되는데, 이전에 눌렀던 상단 리사이클러뷰 아이템들이 같이 로그에 찍혀서
                                //  그것들의 검색 결과까지 같이 보여준다
                                Log.e(TAG, "current_page : " + current_page + ", theme : " + theme);
                                moreViewWelfareLogin(current_page, "start");
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
    private void moreViewWelfareLogin(String page, String assist_method)
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
                    Log.e(TAG, "로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
                    firstEntranceParsing(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "로그인 상태에서 str이 null입니다");
                }
            }
        };

        // theme : 상단 리사이클러뷰에서 선택한 카테고리 이름을 넣는다
        moreViewModel.moreViewWelfareLogin(sqlite_token, sessionId, page, assist_method)
                .observe(this, moreViewObserver);
    }

    /* 비로그인 시 더보기 눌렀을 경우 호출돼 데이터를 가져오는 메서드 */
    private void moreViewWelfareNotLogin(String page, String assist_method)
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
        moreViewModel.moreViewWelfareNotLogin(page, assist_method, "남성", "20", "서울")
                .observe(this, moreViewObserver);
    }

    /* 로그인 시 더보기를 눌렀을 경우 받아온 JSON 값을 파싱하는 메서드
     * 이 메서드는 페이징이 필요없고 각 지원형태 별로 가져오는 10개의 데이터들을 갖고 있다가 맨 먼저 보여줘야 한다 */
    private void firstEntranceParsing(String result)
    {
        Log.e(TAG, "서버 리턴값 파싱하는 메서드 호출##");
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            status = jsonObject.getString("statusCode");
            /* 이 JSONArray를 JSONObject로 파싱할 때는 주의해야 한다. message라는 JSONArray를 가져와 JSONArray 객체에 담지만
             * 그 안의 assist_method_10과 all_10이 "message":[{"assist_method_10":[{"welf_id":580}]}] 형태로 들어있기 때문에
             * for문으로 jsonArray의 길이만큼 반복하며 값을 가져오는 게 아니라 곧바로 getJSONObject(0)으로 JSONArray가 value인 assist_method_10 JSONObject를 가져와야 한다 */
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            JSONObject inner_json = jsonArray.getJSONObject(0);
            JSONArray assist_method_array = inner_json.getJSONArray("assist_method_10");
            for (int i = 0; i < assist_method_array.length(); i++)
            {
                JSONObject assist_obj = assist_method_array.getJSONObject(i);
                welf_id = assist_obj.getString("welf_id");
                welf_name = assist_obj.getString("welf_name");
                welf_tag = assist_obj.getString("welf_tag");
                top_assist_method = assist_obj.getString("assist_method");

                MoreViewItem item = new MoreViewItem();
                item.setWelf_id(welf_id);
                item.setWelf_name(welf_name);
                item.setWelf_tag(welf_tag);
                item.setAssist_method(top_assist_method);
                up_list.add(item);

                SeeMoreItem bottom_item = new SeeMoreItem();
                bottom_item.setWelf_id(welf_id);
                bottom_item.setWelf_name(welf_name);
                bottom_item.setWelf_tag(welf_tag);
                list.add(bottom_item);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        checkDuplicate();

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
            // 현재 페이지 null 체크
            if (total_page != null)
            {
                total_pages = Integer.parseInt(total_page);
            }

            // 상단 리사이클러뷰, 어댑터 초기화
            up_adapter = new MoreViewAdapter(this, noRepeat, up_clickListener);
            up_adapter.setOnItemClickListener(pos ->
            {
                final String name = noRepeat.get(pos).getAssist_method();
                Log.e(TAG, "상단 리사이클러뷰에서 선택한 카테고리명 : " + name);
                // name을 누르면 상단 리사이클러뷰에서 선택한 아이템의 이름이 나오게 된다(전체, 사업, 교육 등)
                // 일단 여기서 api를 호출해 이름을 theme 인자로 넘기고, page와 같이 넘겨서 해당 카테고리의 값들을 받아온다
                list.clear();
                if (!name.equals("전체"))
                {
                    moreViewWelfareLogin(String.valueOf(integer_page), name);
                    moreViewPaging(String.valueOf(integer_page), name);
                    up_adapter.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                }
                else
                {
                    moreViewWelfareLogin(String.valueOf(integer_page), "all");
                    moreViewPaging(String.valueOf(integer_page), "all");
                    adapter.notifyDataSetChanged();
                }
            });
            more_view_top_recyclerview.setAdapter(up_adapter);

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
//                Intent intent = new Intent(this, DetailTabLayoutActivity.class);
//                intent.putExtra("welf_id", id);
//                startActivity(intent);
            });
            more_view_bottom_recyclerview.setAdapter(adapter);
        }

    }

    /* 객체 내의 중복된 값들을 제거하고 ArrayList에 중복 제거된 값들을 담는 메서드 */
    private void checkDuplicate()
    {
        /* 서버에서 값을 받으면 up_list 안에 중복된 값들이 여럿 있는데, 이것들을 중복 검사하는 로직입니다
         * 참고 : https://stackoverflow.com/questions/29965140/remove-duplicates-arraylist-custom-object */
        // 1. 먼저 중복이 제거된 값들을 담을 ArrayList(이하 list)를 만듭니다. 이 list는 나중에 상단 리사이클러뷰의 어댑터 초기화 시 어댑터 생성자의 매개변수로 넘길 겁니다
        noRepeat = new ArrayList<>();
        // 2. for-each 문으로 up_list에 MoreViewItem 객체를 넣을 겁니다
        for (MoreViewItem item : up_list)
        {
            // 3. 중복된 객체의 존재 여부를 판별할 boolean 변수를 false로 선언합니다
            boolean isFound = false;
            // 4. 다시 내부에 for-each 문을 만들고, 이번엔 1에서 만든 중복 없는 값들을 담을 리스트를 콜론의 우항에 넣어 줍니다
            // 중복 없는 값이 들어가야 할 리스트에 중복되지 않는 값들을 넣는 처리를 이 안에서 수행할 겁니다
            for (MoreViewItem inner_item : noRepeat)
            {
                // 5. if 안에 중복을 없애기 위한 조건을 넣어줍니다
                // 내부 for-each에서 가져온 assist_method가 up_list에 들어있는 assist_method와 같거나(||), up_list 안에 들어있는 객체와 같은 값일 경우에
                // 이 if문 안으로 들어오게 됩니다
                if (inner_item.getAssist_method().equals(item.getAssist_method()) || inner_item.equals(item))
                {
                    // 3에서 만든 boolean 변수에 true를 대입한 후 break;로 if문을 탈출합니다
                    // 이 처리로 인해 중복된 값이 발견되면 for문을 벗어나 밑의 if문으로 이동하게 됩니다
                    isFound = true;
                    break;
                }
            }

            // 6. isFound가 true일 경우(왜냐면 3에서 false로 초기화했기 때문에, true여야 중복되지 않았다는 신호가 됩니다)에만 1에서 새로 만든 리스트에 값을 넣습니다
            // 이 처리까지 끝나고 로그로 리스트 안의 값을 확인해보면 중복없는 값들만 들어있는 걸 볼 수 있습니다
            if (!isFound)
            {
                noRepeat.add(item);
            }
        }

        for (int i = 0; i < noRepeat.size(); i++)
        {
            Log.e(TAG, "noRepeat 안의 지원 유형들 : " + noRepeat.get(i).getAssist_method());
        }

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