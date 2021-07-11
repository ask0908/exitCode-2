package com.psj.welfare.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
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

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.adapter.MoreViewAdapter;
import com.psj.welfare.adapter.SeeMoreBottomAdapter;
import com.psj.welfare.data.MoreViewItem;
import com.psj.welfare.data.SeeMoreItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.util.NetworkStatus;
import com.psj.welfare.viewmodel.MoreViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    // 하단 리사이클러뷰에 넣을 값을 담을 변수, total_page는 서버에서 받은 전체 페이지 수다
    String welf_id, welf_name, welf_tag, welf_count;
    int total_page, total_num;
    // 상단 리사이클러뷰에 넣을 값을 담을 변수
    String top_assist_method;
    // all_10 안의 값들을 담을 변수
    String ten_id, ten_name, ten_tag, ten_assist_method, ten_count;
    // 상단 리사이클러뷰 안의 지원형태를 눌렀을 경우 데이터를 가져와 담을 변수
    String support_id, support_name, support_tag, support_assist_method, support_count;

    String status;
    String sqlite_token, sessionId;

    DBOpenHelper helper;

    private Parcelable recyclerViewState;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_test_more_view);
        // 인터넷 연결 상태를 체크해서 3G, 와이파이 중 하나라도 연결돼 있다면 더보기 화면에서 필요한 로직들을 진행시킴
        if (NetworkStatus.checkNetworkStatus(this) == 1 || NetworkStatus.checkNetworkStatus(this) == 2)
        {
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
            // 하단 리사이클러뷰에 보이는 아이템들의 크기를 고정으로 설정
            more_view_bottom_recyclerview.setHasFixedSize(true);
            more_view_bottom_recyclerview.setLayoutManager(new LinearLayoutManager(this));

            // 화면 최상단의 '맞춤 혜택 총 n개' 텍스트뷰 글자 크기, 하단 리사이클러뷰 아이템 간격 및 크기 조절
            ScreenSize screen = new ScreenSize();
            Point size = screen.getScreenSize(TestMoreViewActivity.this);

            // 하단 리사이클러뷰 패딩 설정
            more_view_bottom_recyclerview.setPadding((int) (size.x * 0.058), 0, (int) (size.x * 0.015), 0);

            // "맞춤 혜택 총 n개" 글자 크기 설정
            more_view_result_count.setTextSize((int) (size.x * 0.017));

            // 상, 하단 리사이클러뷰에 사용할 리스트 초기화
            up_list = new ArrayList<>();
            list = new ArrayList<>();
            noDuplicateList = new ArrayList<>();

            // 상단 리사이클러뷰의 첫 번째 아이템은 전체여야 한다. 전체를 누르면 모든 결과를 보여주고 페이징 처리한다(인자로 all 넘기기)
            up_list.add(0, new MoreViewItem("전체"));

            sharedPreferences = getSharedPreferences("app_pref", 0);
            sessionId = sharedPreferences.getString("sessionId", "");
            isLogin = sharedPreferences.getBoolean("logout", false);

            /* 로그인 상태에 따른 더보기 리스트 예외처리 */
            String gender = sharedPreferences.getString("gender", "");
            String age = sharedPreferences.getString("age_group", "");
            String area = sharedPreferences.getString("user_area", "");
            if (gender != null && age != null && area != null)
            {
                if (!gender.equals("") && !age.equals("") && !area.equals(""))
                {
                    // 로그인 x, 관심사 o인 경우 여기로 이동된다
                    Logger.d("비로그인으로 들어옴\n페이지 : " + page + ", assist_method : " + getString(R.string.assist_method_all));
                    getDataFromFormOfSupport(String.valueOf(page), getString(R.string.assist_method_all));
                }
                else
                {
                    // 로그인한 경우
                    Logger.d("로그인으로 들어왔지만 나이, 성별, 지역이 없음");
                    moreViewWelfareLogin(page, getString(R.string.assist_method_start));
                }
            }
            // 로그인 했을 때는 else 안으로 빠진다
            else
            {
                Logger.d("로그인해서 들어왔고 나이, 성별, 지역값 있음\n나이 : " + age + ", 성별 : " + gender + ", 지역 : " + area);
                moreViewWelfareLogin(page, getString(R.string.assist_method_start));
            }

            // 리사이클러뷰 페이징 처리
            moreViewPaging();
        }
        else
        {
            // 인터넷 연결이 되지 않은 상태일 경우
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("인터넷에 연결되어 있지 않습니다\nWi-Fi 또는 데이터를 활성화 해주세요")
                    .setCancelable(false)
                    .setPositiveButton("예", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }

    }   // onCreate() end

    /* 관심사 o, 로그인 x일 때 상단 리사이클러뷰의 지원 형태를 눌렀을 경우 데이터를 가져오는 메서드
     * 이 메서드에선 페이징 처리를 해줘야 한다 */
    private void getDataFromFormOfSupportNotLogin(String page, String assist_method, String gender, String age, String local)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> getDataFromFormObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "태그 눌러 데이터 가져온 결과 : " + str);
                    parseDataFromForm(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "태그 눌러 데이터 가져온 결과가 null입니다");
                }
            }
        };

        moreViewModel.moreViewWelfareNotLogin(sessionId, page, assist_method, gender, age, local)
                .observe(this, getDataFromFormObserver);
    }

    /* 상단 리사이클러뷰의 지원 형태를 눌렀을 경우 데이터를 가져오는 메서드, 토큰을 서버로 넘겨야 하기 때문에 로그인 시에만 호출한다
     * 이 메서드에선 페이징 처리를 해줘야 한다 */
    private void getDataFromFormOfSupport(String page, String assist_method)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> getDataFromFormObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "로그인 후 태그 눌러 데이터 가져온 결과 : " + str);
                    parseDataFromForm(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "태그 눌러 데이터 가져온 결과가 null입니다");
                }
            }
        };

        moreViewModel.moreViewWelfareLogin(sqlite_token, sessionId, page, assist_method)
                .observe(this, getDataFromFormObserver);
    }

    // 로그인 후 더보기를 누르고 이 화면에 들어와서, 상단 리사이클러뷰의 태그를 눌렀을 경우 데이터를 가져오는 메서드
    @SuppressLint("CheckResult")
    private void parseDataFromForm(String str)
    {
        Log.e(TAG, "parseDataFromForm() 안에 들어온 값 : " + str);
        try
        {
            JSONObject jsonObject = new JSONObject(str);
            total_page = jsonObject.getInt("total_page");
            total_num = jsonObject.getInt("total_num");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                support_id = inner_json.getString("welf_id");
                support_name = inner_json.getString("welf_name");
//                support_assist_method = inner_json.getString("assist_method");
                support_tag = inner_json.getString("welf_tag");
                support_count = inner_json.getString("welf_count");

                SeeMoreItem item = new SeeMoreItem();
                item.setWelf_id(support_id);
                item.setWelf_name(support_name);
//                item.setAssist_method(support_assist_method);
                item.setWelf_tag(support_tag);
                item.setWelf_count(support_count);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

//        for (int i = 0; i < list.size(); i++)
//        {
//            Log.e(TAG, "list - getAssist_method : " + list.get(i).getAssist_method());
//            Log.e(TAG, "list - getWelf_count : " + list.get(i).getWelf_count());
//            Log.e(TAG, "list - getWelf_id : " + list.get(i).getWelf_id());
//            Log.e(TAG, "list - getWelf_name : " + list.get(i).getWelf_name());
//            Log.e(TAG, "list - getWelf_tag : " + list.get(i).getWelf_tag());
//        }

        // 하단 리사이클러뷰에 보여주는 데이터 개수만큼 텍스트뷰에 총 몇개인지 보여준다
        Flowable.just(list.size())
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> more_view_result_count.setText("맞춤 혜택 총 " + list.size() + "개"));

        // 하단 리사이클러뷰 초기화
        adapter = new SeeMoreBottomAdapter(TestMoreViewActivity.this, list, itemClickListener);
        adapter.setOnItemClickListener(pos ->
        {
            String id = list.get(pos).getWelf_id();
            String name = list.get(pos).getWelf_name();
            String assist_method = list.get(pos).getAssist_method();
            String tag = list.get(pos).getWelf_tag();
            String count = list.get(pos).getWelf_count();
            Logger.d("하단 리사이클러뷰의 아이템 이름 : " + name + ", 조회수 : " + count + ", id : " + id + ", 태그 : " + tag + ", 지원 형태 : " + assist_method);
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", id);
            startActivity(intent);
        });
        more_view_bottom_recyclerview.setAdapter(adapter);

    } // parseDataFromForm() end

    /* 로그인 후 더보기를 눌러 이 화면에 들어왔을 경우 호출되는 메서드 */
    private void moreViewWelfareLogin(int page, String assist_method)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
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
                    // 처음 더보기 화면에 들어왔을 때 받은 JSON 값들을 파싱해서 하단 리사이클러뷰에 뿌림
                    firstEntranceParsing(str);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "로그인 상태에서 str이 null입니다");
                }
            }
        };

        // assist_method : 상단 리사이클러뷰에서 선택한 지원형태 이름
        moreViewModel.moreViewWelfareLogin(sqlite_token, sessionId, String.valueOf(page), assist_method)
                .observe(this, moreViewObserver);
    }

    /* 로그인 시 더보기를 눌렀을 경우 받아온 JSON 값을 파싱하는 메서드
     * 이 메서드는 페이징이 필요없다 */
    @SuppressLint("CheckResult")
    private void firstEntranceParsing(String result)
    {
        Log.e(TAG, "처음 더보기 화면 들어오고 firstEntranceParsing() 호출##");
        Log.e(TAG, "firstEntranceParsing()으로 들어온 값 : " + result);
        Gson gson = new Gson();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            status = jsonObject.getString("statusCode");
            /* 이 JSONArray를 JSONObject로 파싱할 때는 주의해야 한다. message라는 JSONArray를 가져와 JSONArray 객체에 담지만
             * 그 안의 assist_method_10과 all_10이 "message":[{"assist_method_10":[{"welf_id":580}]}] 형태로 들어있기 때문에
             * for문으로 jsonArray의 길이만큼 반복하며 값을 가져오는 게 아니라 곧바로 getJSONObject(0)으로 JSONArray가 value인 assist_method_10 JSONObject를 가져와야 한다 */
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            JSONObject inner_json = jsonArray.getJSONObject(0);
            JSONArray all_10_array = inner_json.getJSONArray("all_10");
            JSONArray assist_method_array = inner_json.getJSONArray("assist_method_10");

            // 상단 리사이클러뷰에 보여줄 지원형태들, assist_method_10 안의 데이터 중 assist_method만 보여준다
            for (int i = 0; i < assist_method_array.length(); i++)
            {
                MoreViewItem item = gson.fromJson(assist_method_array.getJSONObject(i).toString(), MoreViewItem.class);
                up_list.add(item);
            }

            /* 처음 더보기 화면에 들어왔을 때 - start, 1을 인자로 넘겨서 가져온 데이터 중 all_10 안의 데이터만 하단 리사이클러뷰에 보여준다 */
            // 하단 리사이클러뷰에 보여줄 혜택들, all_10 안의 혜택들만 보여준다
            for (int i = 0; i < all_10_array.length(); i++)
            {
                SeeMoreItem bottom_item = gson.fromJson(all_10_array.getJSONObject(i).toString(), SeeMoreItem.class);
                list.add(bottom_item);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 상단 리사이클러뷰에 보여줄 지원형태 값들의 중복 처리 메서드
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
//            for (int i = 0; i < list.size(); i++)
//            {
//                Log.e(TAG, "list - getAssist_method : " + list.get(i).getAssist_method());
//                Log.e(TAG, "list - getWelf_tag : " + list.get(i).getWelf_tag());
//                Log.e(TAG, "list - getWelf_name : " + list.get(i).getWelf_name());
//                Log.e(TAG, "list - getWelf_id : " + list.get(i).getWelf_id());
//                Log.e(TAG, "list - getWelf_count : " + list.get(i).getWelf_count());
//            }
            /* 서버 응답 코드가 200(성공)인 경우 */
            Flowable.just(list.size())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> more_view_result_count.setText("맞춤 혜택 총 " + list.size() + "개"));

            // 처음 더보기 화면에 들어왔을 때 상단 리사이클러뷰에 값을 넣어서 보여줘야 하니까 이건 필요한 처리다
            // 상단 리사이클러뷰, 어댑터 초기화
            up_adapter = new MoreViewAdapter(this, noRepeat, up_clickListener);
            up_adapter.setOnItemClickListener(pos ->
            {

                page = 1;
                // 상단 리사이클러뷰에서 선택한 지원형태의 이름을 변수에 담는다
                String name = noRepeat.get(pos).getAssist_method();
                Log.e(TAG, "상단 리사이클러뷰에서 선택한 카테고리명 : " + name);  // 로그로 상단 리사이클러뷰 클릭 이벤트 작동 확인
                // 쉐어드에 클릭한 지원형태명 저장(페이징 시 사용)
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("assist_method", name);
                editor.apply();

                // 상단 리사이클러뷰 아이템을 클릭할 때마다 다른 값들을 보여줘야 하기 때문에 하단 리사이클러뷰 초기화에 쓰이는 list는 clear해준다
                list.clear();

                // 관심사 o, 로그인 x일 때 더보기 리스트로 들어온 경우
                if (sharedPreferences.getString("first_visit", "").equals("1"))
                {
                    String gender = sharedPreferences.getString("gender", "");
                    String age = sharedPreferences.getString("age_group", "");
                    String local = sharedPreferences.getString("user_area", "");
                    // first_visit이 1이다 = 아직 로그인을 안했다는 뜻
                    if (!name.equals("전체"))
                    {
                        Log.e(TAG, "관심사 o, 로그인 x인 경우##");
                        // 아래 메서드를 호출하면 하단 리사이클러뷰 초기화에 쓰이는 list에 새로 값들이 들어간다
                        getDataFromFormOfSupportNotLogin(String.valueOf(page), sharedPreferences.getString("assist_method", ""), gender, age, local);
                    }

                    // 전체를 클릭한 경우에만 이곳으로 빠진다
                    else
                    {
                        Log.e(TAG, "관심사 o, 로그인 x인 경우 전체 클릭%%");
                        getDataFromFormOfSupportNotLogin(String.valueOf(page), "all", gender, age, local);
                        adapter.notifyDataSetChanged();
                    }
                }
                // 로그인 후 더보기 리스트로 들어온 경우
                else
                {
                    // 선택한 이름이 전체가 아닐 경우 = 다른 지원형태명을 클릭한 경우
                    if (!name.equals("전체"))
                    {
                        // 아래 메서드를 호출하면 하단 리사이클러뷰 초기화에 쓰이는 list에 새로 값들이 들어간다
                        getDataFromFormOfSupport(String.valueOf(page), sharedPreferences.getString("assist_method", ""));
                    }

                    // 전체를 클릭한 경우에만 이곳으로 빠진다
                    else
                    {
                        getDataFromFormOfSupport(String.valueOf(page), "all");
                        adapter.notifyDataSetChanged();
                    }
                }
            }); // 상단 리사이클러뷰 클릭 리스너 end
            more_view_top_recyclerview.setAdapter(up_adapter);

            // 하단 리사이클러뷰 초기화 및 클릭 리스너 정의
            // 아이템 클릭 시 상세보기 화면으로 이동한다
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

            recyclerViewState = more_view_bottom_recyclerview.getLayoutManager().onSaveInstanceState();
            more_view_bottom_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            /* 여기에 페이징을 넣으면 더보기 리스트에 들어가서도 다음 페이지 데이터를 가져온다 */
//            moreViewPaging(String.valueOf(integer_page), sharedPreferences.getString("assist_method", ""));

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
                if (inner_item.getAssist_method().equals(item.getAssist_method()) || inner_item.equals(item)
                        || inner_item.getAssist_method().contains(item.getAssist_method()))
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

    }

    /* 비로그인 시 더보기 눌렀을 경우 호출돼 데이터를 가져오는 메서드 */
    private void moreViewWelfareNotLogin(String page, String assist_method, String gender, String age_group, String area)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        /* ↓ context를 사용하는 AVM이 아니라 일반 VM을 쓰면 이렇게 해야 함 */
//        moreViewModel.moreViewWelfareNoLogin().observe(this, new Observer<String>()
//        {
//            @Override
//            public void onChanged(String s)
//            {
//
//            }
//        });

        moreViewModel = new ViewModelProvider(this).get(MoreViewModel.class);
        final Observer<String> moreViewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "비로그인 상태에서 더보기 눌러 데이터 가져온 결과 : " + str);
                    firstEntranceParsing(str);
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
        moreViewModel.moreViewWelfareNotLogin(sessionId, page, assist_method, gender, age_group, area)
                .observe(this, moreViewObserver);
    }

    // 리사이클러뷰 스크롤이 마지막에 도달하면 이벤트 발생
    private void moreViewPaging()
    {
        String token_from_server = sharedPreferences.getString("token", "");
        if (token_from_server.equals(""))
        {
            // 토큰이 없다 = 비로그인
            String gender = sharedPreferences.getString("gender", "");
            String age = sharedPreferences.getString("age_group", "");
            String area = sharedPreferences.getString("user_area", "");
            more_view_bottom_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
                {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                    if (layoutManager != null)
                    {
                        if (layoutManager.getItemCount() == 0)
                        {
                            //
                        }
                        else
                        {
                            int totalItemCount = layoutManager.getItemCount();
                            int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

                            if (lastVisible >= totalItemCount - 1 && page < total_page)
                            {
                                page++;
                                if (sharedPreferences.getString("assist_method", "").equals("전체"))
                                {
                                    getDataFromFormOfSupportNotLogin(String.valueOf(page), "all", gender, age, area);
                                    Log.e(TAG, "비로그인 상태에서 페이징 시도 / 성별 : " + gender + ", 나이 : " + age + ", 지역 : " + area);
                                    Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : all");
                                }
                                else
                                {
                                    getDataFromFormOfSupportNotLogin(String.valueOf(page), sharedPreferences.getString("assist_method", ""),
                                            gender, age, area);
                                    Log.e(TAG, "비로그인 상태에서 페이징 시도 / 성별 : " + gender + ", 나이 : " + age + ", 지역 : " + area);
                                    Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : " +
                                            sharedPreferences.getString("assist_method", ""));
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
        else
        {
            more_view_bottom_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
                {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                    if (layoutManager != null)
                    {
                        if (layoutManager.getItemCount() == 0)
                        {
                            //
                        }
                        else
                        {
                            int totalItemCount = layoutManager.getItemCount();
                            int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();

                            if (lastVisible >= totalItemCount - 1 && page < total_page)
                            {
                                page++;
                                if (sharedPreferences.getString("assist_method", "").equals("전체"))
                                {
                                    getDataFromFormOfSupport(String.valueOf(page), "all");
                                    Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : all");
                                }
                                else
                                {
                                    getDataFromFormOfSupport(String.valueOf(page), sharedPreferences.getString("assist_method", ""));
                                    Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : " +
                                            sharedPreferences.getString("assist_method", ""));
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
//        more_view_bottom_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
//        {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
//            {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
//                if (layoutManager != null)
//                {
//                    if (layoutManager.getItemCount() == 0)
//                    {
//                        //
//                    }
//                    else
//                    {
//                        int totalItemCount = layoutManager.getItemCount();
//                        int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
//
//                        if (lastVisible >= totalItemCount - 1 && page < total_page)
//                        {
//                            page++;
//                            if (sharedPreferences.getString("assist_method", "").equals("전체"))
//                            {
//                                getDataFromFormOfSupport(String.valueOf(page), "all");
//                                Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : all");
//                            }
//                            else
//                            {
//                                getDataFromFormOfSupport(String.valueOf(page), sharedPreferences.getString("assist_method", ""));
//                                Log.e(TAG, "현재 서버에 요청하는 페이지 : " + page + ", 지원형태명 : " +
//                                        sharedPreferences.getString("assist_method", ""));
//                            }
//                        }
//                        else
//                        {
//                            Log.e(TAG, "서버에서 불러올 데이터가 없음");
//                        }
//                    }
//                }
//            }
//        });
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