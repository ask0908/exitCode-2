package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.adapter.BookmarkAdapter;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.BookmarkItem;
import com.psj.welfare.viewmodel.BookmarkViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/* 북마크 혜택 */
public class BookmarkCheckActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ConstraintLayout bookmark_top_layout;
    ImageView bookmark_back_image;
    TextView bookmark_top_textview, all_bookmark_count, bookmark_edit_textview, bookmark_check_empty_textview;
    RecyclerViewEmptySupport bookmark_recyclerview;
    BookmarkAdapter adapter;
    BookmarkAdapter.OnItemClickListener itemClickListener;

    ProgressBar progressBar;
    BookmarkViewModel viewModel;
    String id, welf_id, welf_name, tag;
    // 총 검색 결과 개수, 서버에 있는 전체 페이지 수
    String total, total_paging_count;
    ArrayList<BookmarkItem> list;
    String current_page;
    int integer_page = 1;

    private Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_bookmark_check);

        init();
        list = new ArrayList<>();
        progressBar = findViewById(R.id.bookmark_progressbar);
        bookmark_recyclerview = findViewById(R.id.bookmark_recyclerview);
        bookmark_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        bookmark_recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        /* 북마크 페이징 처리 */
        bookmark_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
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
                        if (!log_str.equals(String.valueOf(total_paging_count)))
                        {
                            if (lastVisible >= totalItemCount - 1)
                            {
                                integer_page++;
                                progressBar.setVisibility(View.VISIBLE);
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

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        bookmark_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18);
        all_bookmark_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        bookmark_edit_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);

        bookmark_back_image.setOnClickListener(v -> finish());

        getBookmark(String.valueOf(integer_page));

        // 자세히 보기
        bookmark_edit_textview.setOnClickListener(v -> {
            if (list.size() > 0)
            {
                Intent intent = new Intent(this, BookmarkEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("list", list);
                intent.putExtra("list", bundle);
                startActivityForResult(intent, 1);
            }
            else
            {
                Toast.makeText(this, "저장된 북마크가 없습니다", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            list.clear();
            getBookmark(String.valueOf(integer_page));
        }
    }

    private void init()
    {
        bookmark_top_layout = findViewById(R.id.bookmark_top_layout);

        bookmark_back_image = findViewById(R.id.bookmark_back_image);
        bookmark_top_textview = findViewById(R.id.bookmark_top_textview);
        all_bookmark_count = findViewById(R.id.all_bookmark_count);
        bookmark_edit_textview = findViewById(R.id.bookmark_edit_textview);
        bookmark_check_empty_textview = findViewById(R.id.bookmark_check_empty_textview);
    }

    // 내 북마크 가져오기
    private void getBookmark(String page)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        viewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);
        final Observer<String> bookmarkObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                if (s != null)
                {
                    Log.e(TAG, "액티비티에서 북마크 데이터 가져온 결과 : " + s);
                    bookmarkParsing(s);
                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "가져온 북마크 데이터가 없습니다");
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
            total_paging_count = jsonObject.getString("total_page");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                id = inner_json.getString("id");
                welf_id = inner_json.getString("welf_id");
                welf_name = inner_json.getString("welf_name");
                tag = inner_json.getString("tag");

                BookmarkItem item = new BookmarkItem();
                item.setId(id);
                item.setWelf_id(welf_id);
                item.setWelf_name(welf_name);
                item.setTag(tag);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 페이징해서 새 데이터 가져올 때 리사이클러뷰 스크롤이 자동으로 맨 위로 올라가는 현상이 있는데 그걸 막기 위한 코드
        recyclerViewState = bookmark_recyclerview.getLayoutManager().onSaveInstanceState();

        // 혜택 개수 set
        Flowable.just(total)
                .subscribeOn(Schedulers.io())
                .subscribe(data -> all_bookmark_count.setText("전체 " + total + "개"));

        adapter = new BookmarkAdapter(this, list, itemClickListener);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        if (adapter.getItemCount() > 0)
        {
            bookmark_recyclerview.setVisibility(View.VISIBLE);
            bookmark_check_empty_textview.setVisibility(View.GONE);
            all_bookmark_count.setText("전체 " + total + "개");
        }
        else if (adapter.getItemCount() == 0)
        {
            bookmark_recyclerview.setVisibility(View.GONE);
            bookmark_check_empty_textview.setVisibility(View.VISIBLE);
            bookmark_recyclerview.setEmptyView(bookmark_check_empty_textview);
        }
        else
        {
            all_bookmark_count.setText("");
        }

        progressBar.setVisibility(View.GONE);

        adapter.setOnItemClickListener(pos -> {
            String welf_id = list.get(pos).getWelf_id();
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", welf_id);
            intent.putExtra("being_id",true);
            startActivity(intent);
        });
        bookmark_recyclerview.setAdapter(adapter);

    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

//    @Override
//    protected void onRestart()
//    {
//        super.onRestart();
//        list.clear();
//        getBookmark(String.valueOf(integer_page));
//        adapter.notifyDataSetChanged();
//    }
}