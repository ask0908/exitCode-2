package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.adapter.BookmarkEditAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.BookmarkItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.viewmodel.BookmarkViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 북마크 편집 */
public class BookmarkEditActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
    ConstraintLayout bookmark_edit_top_layout;
    TextView all_bookmark_edit_count, bookmark_edit_top_textview, all_or_not_textview;
    ImageView bookmark_edit_back_image;
    RecyclerView bookmark_edit_recyclerview;
    BookmarkEditAdapter adapter;
    BookmarkEditAdapter.ItemClickListener itemClickListener;
    Button bookmark_edit_delete_cancel, bookmark_edit_delete_btn;
    ProgressBar bookmark_edit_progressbar; //서버에서 데이터 받아올 동안 보여주는 프로그래스바


    String total_page; //전체 페이지
    String total; //북마크 총 개수
    BookmarkViewModel viewModel; //서버에서 데이터 받아올 뷰 모델
    String id, welf_id, welf_name, tag; //북마크 id(북마크 테이블에서 고유 번호), welf_id(혜택 테이블에서 고유 번호), 혜택 이름, 혜택 태그
    ArrayList<BookmarkItem> list = new ArrayList<>(); //북마크 데이터를 담을 변수

    // 전체선택 or 선택해제 클릭 여부 판단하는 변수
    boolean isAllSelectClicked = false;
    String current_page;
    int integer_page = 1;

    private Parcelable recyclerViewState; // 페이징해서 새 데이터 가져올 때 리사이클러뷰 스크롤이 자동으로 맨 위로 올라가는 현상이 있는데 그걸 막기 위한 코드
    String delete_bookmark; //삭제할 북마크
    StringBuilder delete_stringBuilder; //삭제할 북마크를 잠시 담을 stringBuilder

    DBOpenHelper helper;
    String sqlite_token;

//    ArrayList<String> checked_welf_list;

//    private List<BookmarkItem> currentSelectedItems = new ArrayList<>();    // 체크된 체크박스들을 담을 리스트

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_bookmark_edit);

        helper = new DBOpenHelper(this);
        helper.openDatabase();
        helper.create();

        //객체 연결(초기화)
        init();

//        checked_welf_list = new ArrayList<>();
//        stringBuilder = new StringBuilder();

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        setsize();

        //서버에서 데이터 받아오기
        getBookmark("1");

        // 취소 버튼을 눌렀을 때 1개라도 선택돼 있다면 경고 다이얼로그 띄우기
        bookmark_edit_delete_cancel.setOnClickListener(v -> finish());
        bookmark_edit_back_image.setOnClickListener(v -> finish());

        bookmark_edit_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        bookmark_edit_recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        //북마크 선택or선택 취소
        bookmark_selected();

        // 전체선택, 선택해제
        all_selected();

        // 북마크 삭제
        delete_bookmark();

        //리사이클러뷰가 마지막에 도달하면 이벤트 발생 (서버에서 받아올 데이터가 더 있으면 추가적으로 데이터를 받아옴)
        recyclerviewScrollListener();

    }   // onCreate() end

    //객체 연결(초기화)
    private void init(){
        bookmark_edit_top_layout = findViewById(R.id.bookmark_edit_top_layout);
        bookmark_edit_back_image = findViewById(R.id.bookmark_edit_back_image);
        all_bookmark_edit_count = findViewById(R.id.all_bookmark_edit_count);   // 1개 선택했을 때부터 "선택 1개" 식으로 텍스트가 보이도록 한다
        all_or_not_textview = findViewById(R.id.all_or_not_textview);
        bookmark_edit_top_textview = findViewById(R.id.bookmark_edit_top_textview);
        bookmark_edit_recyclerview = findViewById(R.id.bookmark_edit_recyclerview);
        bookmark_edit_delete_cancel = findViewById(R.id.bookmark_edit_delete_cancel);
        bookmark_edit_delete_btn = findViewById(R.id.bookmark_edit_delete_btn);
        bookmark_edit_progressbar = findViewById(R.id.bookmark_edit_progressbar);
    }

    //북마크 선택or선택 취소
    @SuppressLint("CheckResult")
    private void bookmark_selected(){

        // BookmarkCheckActivity에서 받은 리스트 값 풀어서 리사이클러뷰에 set
//        Intent intent = getIntent();
//        Bundle bundle = intent.getBundleExtra("list");
//        if (bundle != null)
//        {
            //BookmarkCheckActivity에서 intent로 받아온 데이터로 하면 안됨
//            list = (ArrayList<BookmarkItem>) bundle.getSerializable("list");
//            for (int i = 0; i < list.size(); i++){
//                list.get(i).getSelected();
//                Log.e(TAG,"list1 : " + list.get(i).getId());
//            }



//            //list값이 있어야 한다
//            if (list != null && list.size() != 0)
//            {
                // 체크박스 리사이클러뷰 어댑터 초기화
                adapter = new BookmarkEditAdapter(this, list, itemClickListener, new BookmarkEditAdapter.OnItemCheckListener()
                {
                    /* ↓ 아래 부분은 BookmarkEditAdapter를 먼저 봐야 이해될 겁니다. 아직 안 봤다면 BookmarkEditAdapter의 맨 밑으로 이동해 주세요
                     * 어댑터를 보고 이곳으로 왔다면 어댑터에 인터페이스를 선언하고 클릭 시 인터페이스 안의 2개 메서드를 호출한다는 것과 대강의 로직까진 알고 있을 겁니다
                     * 이제 어댑터에서 말한대로, 액티비티에서 이 2개 메서드가 호출됐을 때 각각 어떻게 작동할지를 정해줘야 합니다
                     * onItemCheck() : 리스트에 체크된 체크박스를 add()합니다
                     * onItemUncheck() : 체크해제된 체크박스를 리스트에서 remove()합니다
                     * 위 메서드의 결과로 리스트 안의 데이터 개수는 계속 변동되는데, 그 때마다 이 리스트의 크기를 setText()해서 보여주면 유저는 실시간으로 선택된 체크박스의 개수만큼
                     * 텍스트뷰 안의 숫자가 바뀌는 걸 볼 수 있습니다. 이렇게 하면 선택된 체크박스의 개수를 구해 텍스트뷰에 보여주는 로직이 완성됩니다 */
                    @Override
                    public void onItemCheck(BookmarkItem item, int pos)
                    {

                        list.get(pos).setSelected(true); //해당 아이템을 체크
                        all_or_not_textview.setText("선택해제");

                        // 체크된 체크박스들의 개수를 텍스트뷰에 실시간으로 업데이트해 보여준다. 보여지는 개수는 어댑터에 정의한 메서드를 액티비티에서 호출해서 가져올 수 있다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));   // checked_welf_list.size()로도 해보기


//                        Log.e(TAG,"adapter.getCheckedCount()" + adapter.getCheckedCount());

                        /* 체크박스가 체크됐을 경우의 처리를 여기 구현한다 */
                        // 체크된 체크박스를 따로 담을 리스트에 add하기 전 중복 검사
//                        if (!currentSelectedItems.contains(item))
//                        {
//                            currentSelectedItems.add(item);
////                            checked_welf_list.add(item.getId());
//                        }

                        // currentSelectedItems 크기가 0보다 커지는 순간 우상단의 "전체선택"을 "선택해제"로 변경한다


//                        Log.e(TAG, "currentSelectedItems.size() : " + currentSelectedItems.size());
//                        Log.e(TAG, "checked_welf_list.size() : " + checked_welf_list.size());
//                        Log.e(TAG, "adapter.getCheckedCount() : " + adapter.getCheckedCount());
                    }

                    @Override
                    public void onItemUncheck(BookmarkItem item, int pos)
                    {

                        //만약 모든 북마크가 선택 해제 되었다면 (마지막 북마크 체크 해제전)
                        if(adapter.getCheckedCount() == 1){
                            all_or_not_textview.setText("전체선택");
                        }

                        list.get(pos).setSelected(false); //해당 아이템을 체크 풀기

                        // 낱개의 체크박스를 체크 해제한 결과를 텍스트뷰에 반영한다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));





//                        Log.e(TAG,"adapter.getCheckedCount() : " + adapter.getCheckedCount());

//                        Log.e(TAG,"pos2" + pos);
                        /* 체크박스가 체크 해제됐을 경우의 처리 (전체선택을 눌렀을 경우 x) */
                        // 리스트에서 체크해제한 아이템을 제거한다. 그리고 어댑터 안의 checked_count에도 이것이 반영됐는지 확인
//                        currentSelectedItems.remove(item);
//                        checked_welf_list.remove(item.getId());


                    }
                });
                bookmark_edit_recyclerview.setAdapter(adapter);
//            }
//        }
    }



    // 전체선택, 선택해제
    @SuppressLint("CheckResult")
    private void all_selected(){
        all_or_not_textview.setOnClickListener(v ->
        {
            String text = all_or_not_textview.getText().toString();
            // 전체선택 클릭
            if (text.equals("전체선택"))
            {

                //모든 북마크 아이템 체크상태를 true로
                for (int i = 0; i < list.size(); i++){
                    list.get(i).setSelected(true);
                }

                all_or_not_textview.setText("선택해제");
                adapter.selectAll(); //adapter로 가서 checked_count값 갱신
                all_bookmark_edit_count.setText("선택 " + list.size() + "개");


                Observable.just(adapter.getCheckedCount())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));



//                /* 전체선택을 누르면 체크된 체크박스들을 담는 리스트에 이전 액티비티에서 받아온 리스트 안의 체크박스들을 담는다
//                * 그래서 onItemUncheck() 콜백 메서드가 호출될 때 하나씩 없애서, 전체 선택 후 하나씩 체크해제해 개수가 줄어드는 걸 구현한다 */
////                for (int i = 0; i < list.size(); i++)
////                {
////                    BookmarkItem item = new BookmarkItem();
////                    item.setId(list.get(i).getId());
////                    Log.e(TAG, "item - getId() : " + item.getId());
////                    currentSelectedItems.add(item);
////                }
////                Log.e(TAG, "전체선택 눌렀을 때 currentSelectedItems의 크기 : " + currentSelectedItems.size());
////                for (int i = 0; i < currentSelectedItems.size(); i++)
////                {
////                    Log.e(TAG, "currentSelectedItems - getId() : " + currentSelectedItems.get(i).getId());
////                }
//
//                /* 전체선택 누르고 낱개 체크박스를 눌러 선택해제하면 전체 개수에서 -1씩 숫자를 빼야 한다 */
//                bookmark_edit_recyclerview.setAdapter(null);
//                adapter = new BookmarkEditAdapter(this, list, itemClickListener, new BookmarkEditAdapter.OnItemCheckListener()
//                {
//                    @Override
//                    public void onItemCheck(BookmarkItem item, int pos)
//                    {
////                        Log.e(TAG, "선택해제 보일 때 onItemCheck() 호출됨");
//                        // 체크된 체크박스를 따로 담을 리스트에 add하기 전 중복 검사
////                        if (!currentSelectedItems.contains(item))
////                        {
////                            currentSelectedItems.add(item);
////                        }
////                        Log.e(TAG, "전체선택 누른 후 낱개 체크박스 아이템 체크 시 currentSelectedItems의 크기 : " + currentSelectedItems.size());
////                        Log.e(TAG, "전체선택 누른 후 onItemCheck()에서 어댑터의 getCheckedCount() : " + adapter.getCheckedCount());
//
//                        // currentSelectedItems 크기가 0보다 커지는 순간 우상단의 "전체선택"을 "선택해제"로 변경한다
////                        all_or_not_textview.setText("선택해제");
//
//                        // 체크된 체크박스들의 개수를 텍스트뷰에 실시간으로 업데이트해 보여준다
//                        Observable.just(adapter.getCheckedCount())
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));
//                    }
//
//                    /* 전체선택 상태에서 빨간 상태의 체크박스를 눌러 선택해제한 경우 호출됨 */
//                    @Override
//                    public void onItemUncheck(BookmarkItem item, int pos)
//                    {
//                        // 전체선택을 눌러 모든 체크박스가 다 체크된 상태에서 onItemUncheck()가 호출된 경우(=선택해제한 경우), 그 아이템의 id를 가져와서
//                        // 선택한 체크박스들의 id가 담긴 리스트에 넣는다
//                        String id = item.getId();
////                        checked_welf_list.remove(id);
//
//                        // 체크해제된 개수를 어댑터에서 받아 Rxjava를 써서 텍스트뷰에 set -> 실시간으로 몇 개가 선택되었는지 보여준다
//                        Observable.just(adapter.getCheckedCount())
//                                .subscribeOn(Schedulers.io())
//                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));
//
//                        // 전체선택 후 하나씩 계속 없애다가 선택한 체크박스들의 id가 담긴 리스트의 크기가 0이 되면(=리스트에 아무것도 없다면)
//                        // 이 화면에 처음 들어왔을 때의 상태로 어댑터, 텍스트뷰의 글자를 되돌린다
////                        if (checked_welf_list.size() == 0)
////                        {
////                            isAllSelectClicked = false;
////                            all_or_not_textview.setText("전체선택");
////                            adapter.deselectAll();
////                        }
//                    }
//                });

//                bookmark_edit_recyclerview.setAdapter(adapter);
//                adapter.selectAll();
//                Log.e(TAG,"count2 : " + adapter.getCheckedCount());
            }
            // 선택해제 클릭
            else if (text.equals("선택해제"))
            {
//                isAllSelectClicked = false;

                //모든 북마크 아이템 체크상태를 false로
                for (int i = 0; i < list.size(); i++){
                    list.get(i).setSelected(false);
                }

                all_or_not_textview.setText("전체선택");
                adapter.deselectAll(); //adapter로 가서 checked_count값 갱신
                all_bookmark_edit_count.setText("선택 0개");

                Observable.just(adapter.getCheckedCount())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));


//                Log.e(TAG,"count2 : " + adapter.getCheckedCount());
//                currentSelectedItems.clear();   // 전부 선택해제했으니 선택된 체크박스를 담는 리스트도 clear()해야 한다
//                Log.e(TAG, "선택해제 눌렀을 때 currentSelectedItems의 size : " + currentSelectedItems.size());
            }
        });
    }

    // 북마크 삭제
    private void delete_bookmark(){
        bookmark_edit_delete_btn.setOnClickListener(v ->
        {

            delete_stringBuilder = new StringBuilder(); //삭제할 북마크를 잠시 담을 stringBuilder
            int getSelected_count = 0; //삭제할 북마크 개수
            for (int i = 0; i < list.size(); i++){
                if(list.get(i).getSelected() && getSelected_count == 0){
                    //스트링끼리 더하게 되면 메모리도 잡아먹고 시스템적으로 비효율적
                    //그래서StringBuilder을 사용
                    delete_stringBuilder.append(list.get(i).getId());
                    getSelected_count ++;
                } else if(list.get(i).getSelected() && getSelected_count != 0){
                    delete_stringBuilder.append("-");
                    delete_stringBuilder.append(list.get(i).getId());
                    getSelected_count ++;
                }
            }
            delete_bookmark = delete_stringBuilder.toString(); //삭제할 북마크


//            Log.e(TAG,"count2 : " + adapter.getCheckedCount());
//            Log.e(TAG,"list.size() : " + list.size());
//            for (int i = 0; i < list.size(); i++){
//                Log.e(TAG,"selected : " + list.get(i).getSelected());
//                Log.e(TAG,"i : " + i);
//            }



//            for (int i = 0; i < checked_welf_list.size(); i++)
//            {
//                // ArrayList 크기만큼 반복하면서 구분자(-)를 붙여 id 값들을 가져와야 한다
//                // 마지막 id값 오른쪽에는 구분자를 붙이지 않는다
//                stringBuilder.append(checked_welf_list.get(i)).append("-");
//            }
//
//
////             마지막에 붙은 "-"을 제거한 뒤 북마크 제거 메서드의 인자로 넘겨 북마크 삭제 구현
//            delete_bookmark = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
//            Log.e(TAG, "서버로 보낼 delete_bookmark : " + delete_bookmark);

////             TODO : 모든 에러 다 잡으면 그 때 주석 해제
            deleteBookmark("delete", String.valueOf(integer_page), delete_bookmark);
        });
    }

    // 북마크 삭제
    private void deleteBookmark(String type, String page, String id)
    {
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.deleteBookmark(sqlite_token, type, page, id);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "북마크 삭제 결과 : " + result);
                    Toast.makeText(BookmarkEditActivity.this, "선택하신 북마크의 삭제가 완료됐어요", Toast.LENGTH_SHORT).show();
                    finish();
//                    setResult(RESULT_OK);
                }
                else
                {
                    Log.e(TAG, "북마크 삭제 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "북마크 삭제 에러 : " + t.getMessage());
            }
        });
    }


    //리사이클러뷰가 마지막에 도달하면 이벤트 발생
    private void recyclerviewScrollListener() {
        bookmark_edit_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm != null)
                {
                    if (llm.getItemCount() == 0)
                    {
                        //
                    }
                    else
                    {
                        int totalItemCount = llm.getItemCount();
                        int lastVisible = llm.findLastCompletelyVisibleItemPosition();

                        String log_str = String.valueOf(integer_page);
                        if (!log_str.equals(String.valueOf(total_page)))
                        {
                            if (lastVisible >= totalItemCount - 1)
                            {
                                integer_page++;
                                current_page = String.valueOf(integer_page);
                                getBookmark(current_page);
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


    // 내 북마크 가져오기
    private void getBookmark(String page)
    {
        bookmark_edit_progressbar.setVisibility(View.VISIBLE);
        viewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);
        final Observer<String> bookmarkObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                if (s != null)
                {
//                    Log.e(TAG, "액티비티에서 북마크 데이터 가져온 결과 : " + s);
                    bookmarkParsing(s); //받아온 데이터 파싱하기
                    bookmark_edit_progressbar.setVisibility(View.GONE);
                }
                else
                {
                    Log.e(TAG, "가져온 북마크 데이터가 없습니다");
                    bookmark_edit_progressbar.setVisibility(View.GONE);
                }
            }
        };

        viewModel.selectBookmark(page).observe(this, bookmarkObserver);
    }

    // 가져온 북마크 데이터를 파싱하는 메서드
    @SuppressLint("CheckResult")
    private void bookmarkParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total = jsonObject.getString("total");
            total_page = jsonObject.getString("total_page");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                id = inner_json.getString("bookmark_id");
                welf_id = inner_json.getString("welf_id");
                welf_name = inner_json.getString("welf_name");
                tag = inner_json.getString("tag");

                BookmarkItem item = new BookmarkItem();
                item.setId(id);
                item.setWelf_id(welf_id);
                item.setWelf_name(welf_name);
                item.setTag(tag);
                list.add(item);
                adapter.notifyDataSetChanged();
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 페이징해서 새 데이터 가져올 때 리사이클러뷰 스크롤이 자동으로 맨 위로 올라가는 현상이 있는데 그걸 막기 위한 코드
        recyclerViewState = bookmark_edit_recyclerview.getLayoutManager().onSaveInstanceState();

//        // 혜택 개수 set
//        Flowable.just(total)
//                .subscribeOn(Schedulers.io())
//                .subscribe(data -> all_bookmark_count.setText("전체 " + total + "개"));

//        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
//        bookmark_edit_progressbar.setVisibility(View.GONE);

//        adapter.setOnItemClickListener(pos -> {
//            String welf_id = list.get(pos).getWelf_id();
//            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
//            intent.putExtra("welf_id", welf_id);
//            intent.putExtra("being_id",true);
//            startActivity(intent);
//        });
//        bookmark_edit_recyclerview.setAdapter(adapter);
        bookmark_edit_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState); //리사이클러뷰 현재 위치값 저장한 데이터를 스크롤 해서 새로운 데이터를 받아왔을 때 기억하고 셋팅함
    }









    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void setsize(){
        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        bookmark_edit_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18); //"북마크 편집" 텍스트
        bookmark_edit_top_textview.setPadding((int)(size.x*0.05), 0,0,0);

        all_bookmark_edit_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        all_or_not_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18); //"전체선택" 텍스트
        all_or_not_textview.setPadding(0, 0,(int)(size.x*0.05),0);

        bookmark_edit_recyclerview.setPadding((int)(size.x*0.05),0,(int)(size.x*0.05),0); //리사이클러뷰

        bookmark_edit_delete_cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24); //취소 버튼
        bookmark_edit_delete_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24); //선택삭제 버튼
    }


    // 상태바에 그라데이션
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}