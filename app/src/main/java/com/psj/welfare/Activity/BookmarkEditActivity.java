package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.util.ArrayList;
import java.util.List;

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

    ArrayList<BookmarkItem> list;
    // 전체선택 or 선택해제 클릭 여부 판단하는 변수
    boolean isAllSelectClicked = false;
    int integer_page = 1;
    String sendStr;

    DBOpenHelper helper;
    String sqlite_token;

    ArrayList<String> checked_welf_list;
    StringBuilder stringBuilder;
    private List<BookmarkItem> currentSelectedItems = new ArrayList<>();    // 체크된 체크박스들을 담을 리스트

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

        bookmark_edit_top_layout = findViewById(R.id.bookmark_edit_top_layout);
        bookmark_edit_back_image = findViewById(R.id.bookmark_edit_back_image);
        all_bookmark_edit_count = findViewById(R.id.all_bookmark_edit_count);   // 1개 선택했을 때부터 "선택 1개" 식으로 텍스트가 보이도록 한다
        all_or_not_textview = findViewById(R.id.all_or_not_textview);
        bookmark_edit_top_textview = findViewById(R.id.bookmark_edit_top_textview);
        bookmark_edit_recyclerview = findViewById(R.id.bookmark_edit_recyclerview);
        bookmark_edit_delete_cancel = findViewById(R.id.bookmark_edit_delete_cancel);
        bookmark_edit_delete_btn = findViewById(R.id.bookmark_edit_delete_btn);

        checked_welf_list = new ArrayList<>();

        stringBuilder = new StringBuilder();

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        bookmark_edit_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18);
        all_bookmark_edit_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        all_or_not_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18);

        // 취소 버튼을 눌렀을 때 1개라도 선택돼 있다면 경고 다이얼로그 띄우기
        bookmark_edit_delete_cancel.setOnClickListener(v -> finish());
        bookmark_edit_back_image.setOnClickListener(v -> finish());

        bookmark_edit_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        bookmark_edit_recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        // BookmarkCheckActivity에서 받은 리스트 값 풀어서 리사이클러뷰에 set
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("list");
        if (bundle != null)
        {
            list = (ArrayList<BookmarkItem>) bundle.getSerializable("list");
            if (list != null && list.size() != 0)
            {
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
                    public void onItemCheck(BookmarkItem item)
                    {
                        /* 체크박스가 체크됐을 경우의 처리를 여기 구현한다 */
                        // 체크된 체크박스를 따로 담을 리스트에 add하기 전 중복 검사
                        if (!currentSelectedItems.contains(item))
                        {
                            currentSelectedItems.add(item);
                            checked_welf_list.add(item.getId());
                        }

                        // currentSelectedItems 크기가 0보다 커지는 순간 우상단의 "전체선택"을 "선택해제"로 변경한다
                        all_or_not_textview.setText("선택해제");

                        Log.e(TAG, "currentSelectedItems.size() : " + currentSelectedItems.size());
                        Log.e(TAG, "checked_welf_list.size() : " + checked_welf_list.size());
                        Log.e(TAG, "adapter.getCheckedCount() : " + adapter.getCheckedCount());

                        // 체크된 체크박스들의 개수를 텍스트뷰에 실시간으로 업데이트해 보여준다. 보여지는 개수는 어댑터에 정의한 메서드를 액티비티에서 호출해서 가져올 수 있다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + currentSelectedItems.size() + "개"));   // checked_welf_list.size()로도 해보기
                    }

                    @Override
                    public void onItemUncheck(BookmarkItem item)
                    {
                        /* 체크박스가 체크 해제됐을 경우의 처리 (전체선택을 눌렀을 경우 x) */
                        // 리스트에서 체크해제한 아이템을 제거한다. 그리고 어댑터 안의 checked_count에도 이것이 반영됐는지 확인
                        currentSelectedItems.remove(item);
                        checked_welf_list.remove(item.getId());

                        // 낱개의 체크박스를 체크 해제한 결과를 텍스트뷰에 반영한다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + currentSelectedItems.size() + "개"));
                    }
                });
                bookmark_edit_recyclerview.setAdapter(adapter);
            }
        }

        // 전체선택, 선택해제
        all_or_not_textview.setOnClickListener(v ->
        {
            String text = all_or_not_textview.getText().toString();
            // 전체선택 클릭
            if (text.equals("전체선택"))
            {
                isAllSelectClicked = true;
                all_or_not_textview.setText("선택해제");
                adapter.selectAll();
                Log.e(TAG, "전체선택 눌렀을 때 list의 size : " + list.size());
                all_bookmark_edit_count.setText("선택 " + list.size() + "개");

                for (int i = 0; i < list.size(); i++)
                {
                    checked_welf_list.add(list.get(i).getId());
                }

                /* 전체선택을 누르면 체크된 체크박스들을 담는 리스트에 이전 액티비티에서 받아온 리스트 안의 체크박스들을 담는다
                * 그래서 onItemUncheck() 콜백 메서드가 호출될 때 하나씩 없애서, 전체 선택 후 하나씩 체크해제해 개수가 줄어드는 걸 구현한다 */
                for (int i = 0; i < list.size(); i++)
                {
                    BookmarkItem item = new BookmarkItem();
                    item.setId(list.get(i).getId());
                    Log.e(TAG, "item - getId() : " + item.getId());
                    currentSelectedItems.add(item);
                }
                Log.e(TAG, "전체선택 눌렀을 때 currentSelectedItems의 크기 : " + currentSelectedItems.size());
                for (int i = 0; i < currentSelectedItems.size(); i++)
                {
                    Log.e(TAG, "currentSelectedItems - getId() : " + currentSelectedItems.get(i).getId());
                }

                /* 전체선택 누르고 낱개 체크박스를 눌러 선택해제하면 전체 개수에서 -1씩 숫자를 빼야 한다 */
                bookmark_edit_recyclerview.setAdapter(null);
                adapter = new BookmarkEditAdapter(this, list, itemClickListener, new BookmarkEditAdapter.OnItemCheckListener()
                {
                    @Override
                    public void onItemCheck(BookmarkItem item)
                    {
                        Log.e(TAG, "선택해제 보일 때 onItemCheck() 호출됨");
                        // 체크된 체크박스를 따로 담을 리스트에 add하기 전 중복 검사
                        if (!currentSelectedItems.contains(item))
                        {
                            currentSelectedItems.add(item);
                        }
                        Log.e(TAG, "전체선택 누른 후 낱개 체크박스 아이템 체크 시 currentSelectedItems의 크기 : " + currentSelectedItems.size());
                        Log.e(TAG, "전체선택 누른 후 onItemCheck()에서 어댑터의 getCheckedCount() : " + adapter.getCheckedCount());

                        // currentSelectedItems 크기가 0보다 커지는 순간 우상단의 "전체선택"을 "선택해제"로 변경한다
//                        all_or_not_textview.setText("선택해제");

                        // 체크된 체크박스들의 개수를 텍스트뷰에 실시간으로 업데이트해 보여준다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));
                    }

                    /* 전체선택 상태에서 빨간 상태의 체크박스를 눌러 선택해제한 경우 호출됨 */
                    @Override
                    public void onItemUncheck(BookmarkItem item)
                    {
                        // 전체선택을 눌러 모든 체크박스가 다 체크된 상태에서 onItemUncheck()가 호출된 경우(=선택해제한 경우), 그 아이템의 id를 가져와서
                        // 선택한 체크박스들의 id가 담긴 리스트에 넣는다
                        String id = item.getId();
                        checked_welf_list.remove(id);

                        // 체크해제된 개수를 어댑터에서 받아 Rxjava를 써서 텍스트뷰에 set -> 실시간으로 몇 개가 선택되었는지 보여준다
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));

                        // 전체선택 후 하나씩 계속 없애다가 선택한 체크박스들의 id가 담긴 리스트의 크기가 0이 되면(=리스트에 아무것도 없다면)
                        // 이 화면에 처음 들어왔을 때의 상태로 어댑터, 텍스트뷰의 글자를 되돌린다
                        if (checked_welf_list.size() == 0)
                        {
                            isAllSelectClicked = false;
                            all_or_not_textview.setText("전체선택");
                            adapter.deselectAll();
                        }
                    }
                });
                bookmark_edit_recyclerview.setAdapter(adapter);
                adapter.selectAll();

            }
            // 선택해제 클릭
            else if (text.equals("선택해제"))
            {
                isAllSelectClicked = false;
                all_or_not_textview.setText("전체선택");
                adapter.deselectAll();
                currentSelectedItems.clear();   // 전부 선택해제했으니 선택된 체크박스를 담는 리스트도 clear()해야 한다
                all_bookmark_edit_count.setText("선택 0개");
                Log.e(TAG, "선택해제 눌렀을 때 currentSelectedItems의 size : " + currentSelectedItems.size());
            }
        });

        // 북마크 삭제
        bookmark_edit_delete_btn.setOnClickListener(v ->
        {
            for (int i = 0; i < checked_welf_list.size(); i++)
            {
                // ArrayList 크기만큼 반복하면서 구분자(-)를 붙여 id 값들을 가져와야 한다
                // 마지막 id값 오른쪽에는 구분자를 붙이지 않는다
                stringBuilder.append(checked_welf_list.get(i)).append("-");
            }
            // 마지막에 붙은 "-"을 제거한 뒤 북마크 제거 메서드의 인자로 넘겨 북마크 삭제 구현
            sendStr = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
            Log.e(TAG, "서버로 보낼 sendStr : " + sendStr);
            // TODO : 모든 에러 다 잡으면 그 때 주석 해제
//            deleteBookmark("delete", String.valueOf(integer_page), sendStr);
        });

    }   // onCreate() end

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
                    setResult(RESULT_OK);
                    finish();
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