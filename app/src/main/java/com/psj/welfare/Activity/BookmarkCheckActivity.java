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
        recyclerviewScrollListener(); //리사이클러뷰가 마지막에 도달하면 이벤트 발생

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        setsize();

        bookmark_back_image.setOnClickListener(v -> finish());

        //서버에서 북마크한 데이터 가져오기
        getBookmark(String.valueOf(integer_page),false);

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


    //리사이클러뷰가 마지막에 도달하면 이벤트 발생
    private void recyclerviewScrollListener() {
//        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                LinearLayoutManager llm = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
//                int totalItemCount = llm.getItemCount(); //리사이클러뷰가 가진 총 아이템 갯수
//                //indLastCompletelyVisibleItemPosition 메서드이다. 얘는 마지막 아이템이 완전히 보일때 해당 Position을 알려준다.
//                int lastVisible = llm.findLastCompletelyVisibleItemPosition(); //Position을 반환하기 때문에 0부터 시작이 된다
//                //ex) 1개의 아이템을 가지고 있다. Position은 0으로 나올 것이고, totalItemCount 는 1로 나오게 된다
//
//                if (llm != null)
//                {
//                    if (llm.getItemCount() == 0)
//                    {
//                        //
//                    }
//                    else
//                    {
//                        String log_str = String.valueOf(integer_page);
//                        if (!log_str.equals(String.valueOf(total_paging_count)))
//                        {
//                            if (lastVisible >= totalItemCount - 1)
//                            {
//                                integer_page++;
//                                progressBar.setVisibility(View.VISIBLE); //하단 프로그래스바
//                                current_page = String.valueOf(integer_page);
//                                getBookmark(current_page);
//                            }
//                        }
//                        else
//                        {
//                            Log.e(TAG, "서버에서 불러올 데이터가 없음");
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    }
//                }
//            }
//        };
//        bookmark_recyclerview.addOnScrollListener(onScrollListener);



        //페이징으로 추가 데이터 받아오기
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
                                progressBar.setVisibility(View.VISIBLE); //하단 프로그래스바
                                current_page = String.valueOf(integer_page);
                                //서버에서 북마크한 데이터 가져오기
                                getBookmark(current_page,true);
                            }
                        }
                        else
                        {
                            Log.e(TAG, "서버에서 불러올 데이터가 없음");
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

    }


    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void setsize(){
        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        bookmark_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 18); //"북마크" 텍스트
        bookmark_top_textview.setPadding((int)(size.x*0.1), 0,(int)(size.x*0.07),0);

        all_bookmark_count.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        bookmark_edit_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        bookmark_check_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 20);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            list.clear();
            getBookmark(String.valueOf(integer_page),false);
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

    //서버에서 북마크한 데이터 가져오기
    private void getBookmark(String page, boolean paging)
    {
        //paging은 페이징으로 데이터를 불러오는건지 아닌건지를 판단할 때 쓰임

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        if(!paging){ //페이징으로 서버를 연결하는것이 아니라면
            dialog.show();
        }

        viewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);
        final Observer<String> bookmarkObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                if (s != null)
                {
                    if(!paging){ //페이징으로 서버를 연결한다면
                        bookmarkParsing(s);
                        dialog.dismiss();
                    } else { //페이징이 아닌 처음 액티비티로 들어왔을 때 서버를 연결항다면
                        bookmarkParsing(s);
                    }
//                    Log.e(TAG, "액티비티에서 북마크 데이터 가져온 결과 : " + s);
                    bookmark_check_empty_textview.setVisibility(View.GONE); //"아직 북마크한 혜택이 없습니다"
                }
                else
                {
                    Log.e(TAG, "가져온 북마크 데이터가 없습니다");
                    bookmark_check_empty_textview.setVisibility(View.VISIBLE); //"아직 북마크한 혜택이 없습니다"
                }
            }
        };

        viewModel.selectBookmark(page).observe(this, bookmarkObserver);
    }

    // 가져온 북마크 데이터를 파싱하는 메서드
    @SuppressLint("CheckResult")
    private void bookmarkParsing(String result)
    {
//        Log.e(TAG,"result : " + result);
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total = jsonObject.getString("total");
            total_paging_count = jsonObject.getString("total_page");
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
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

//        // 페이징해서 새 데이터 가져올 때 리사이클러뷰 스크롤이 자동으로 맨 위로 올라가는 현상이 있는데 그걸 막기 위한 코드
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



        bookmark_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState); //리사이클러뷰 현재 위치값 저장한 데이터를 스크롤 해서 새로운 데이터를 받아왔을 때 기억하고 셋팅함
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        list.clear();
        integer_page = 1;
        getBookmark(String.valueOf(integer_page),false);
        adapter.notifyDataSetChanged();
    }

}