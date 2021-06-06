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

    DBOpenHelper helper;
    String sqlite_token;

    ArrayList<String> checked_welf_list;
    StringBuilder stringBuilder;
    private List<BookmarkItem> currentSelectedItems = new ArrayList<>();

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

        // 전체선택, 선택해제
        all_or_not_textview.setOnClickListener(v ->
        {
            isAllSelectClicked = true;
            String text = all_or_not_textview.getText().toString();
            // 전체선택 클릭
            if (text.equals("전체선택"))
            {
                all_or_not_textview.setText("선택해제");
                adapter.selectAll();
                all_bookmark_edit_count.setText("선택 " + list.size() + "개");
            }
            // 선택해제 클릭
            else if (text.equals("선택해제"))
            {
                all_or_not_textview.setText("전체선택");
                adapter.deselectAll();
                all_bookmark_edit_count.setText("선택 0개");
            }
        });

        // BookmarkCheckActivity에서 받은 리스트 값 풀어서 리사이클러뷰에 set
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("list");
        if (bundle != null)
        {
            list = (ArrayList<BookmarkItem>) bundle.getSerializable("list");
            if (list != null && list.size() != 0)
            {
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
                        if (isAllSelectClicked)
                        {
                            // 전체선택이 클릭됐다 -> 체크박스 개수를 담을 리스트의 size를 0으로 만든다
                            currentSelectedItems.clear();
                            checked_welf_list.clear();
                            // 체크박스 클릭 -> 선택된 체크박스의 id값을 ArrayList에 추가
                            currentSelectedItems.add(item); /* 아이템이 추가돼도 리스트의 크기가 1로 고정돼 있다 */
                            for (int i = 0; i < currentSelectedItems.size(); i++)
                            {
                                Log.e(TAG, "currentSelectedItems의 size : " + currentSelectedItems.size());
                            }
                            // 체크박스의 id값을 액티비티로 가져온 다음
                            String id = item.getId();
                            // 액티비티에 선언한 ArrayList에 있는지 확인 후 add한다(중복 처리)
                            if (!checked_welf_list.contains(id))
                            {
                                checked_welf_list.add(id);
                            }
                            // 우상단 텍스트뷰는 체크박스가 1개라도 선택됐으면 '선택해제'로 변경한다
                            all_or_not_textview.setText("선택해제");
                            // 체크해서 리스트 숫자가 변경될 때마다 텍스트뷰의 문장을 바꿔 보여준다
                            all_bookmark_edit_count.setText("선택 " + currentSelectedItems.size() + "개");
                        }
                        else
                        {
                            // 체크박스 클릭 -> 선택된 체크박스의 id값을 ArrayList에 추가
                            currentSelectedItems.add(item);
                            // 체크박스의 id값을 액티비티로 가져온 다음
                            String id = item.getId();
                            // 액티비티에 선언한 ArrayList에 있는지 확인 후 add한다(중복 처리)
                            if (!checked_welf_list.contains(id))
                            {
                                checked_welf_list.add(id);
                            }
                            // 우상단 텍스트뷰는 체크박스가 1개라도 선택됐으면 '선택해제'로 변경한다
                            all_or_not_textview.setText("선택해제");
                            // 체크해서 리스트 숫자가 변경될 때마다 텍스트뷰의 문장을 바꿔 보여준다
                            all_bookmark_edit_count.setText("선택 " + currentSelectedItems.size() + "개");
                        }
//                        Observable.just(adapter.getCheckedCount())
//                                .subscribeOn(Schedulers.io())
//                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + currentSelectedItems.size() + "개"));
                    }

                    @Override
                    public void onItemUncheck(BookmarkItem item)
                    {
                        currentSelectedItems.remove(item);
                        String id = item.getId();
                        for (int i = 0; i < checked_welf_list.size(); i++)
                        {
                            if (checked_welf_list.get(i).equals(item.getId()))
                            {
                                checked_welf_list.remove(id);
                            }
                        }
                        Observable.just(adapter.getCheckedCount())
                                .subscribeOn(Schedulers.io())
                                .subscribe(data -> all_bookmark_edit_count.setText("선택 " + adapter.getCheckedCount() + "개"));
//                        all_bookmark_edit_count.setText("선택 " + list.size() + "개");
                    }
                });
                bookmark_edit_recyclerview.setAdapter(adapter);
            }
        }

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
            String sendStr = stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1);
            deleteBookmark("delete", String.valueOf(integer_page), sendStr);
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