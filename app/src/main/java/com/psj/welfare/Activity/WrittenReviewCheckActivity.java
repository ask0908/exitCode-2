package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.adapter.WrittenReviewAdapter;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.WrittenReviewItem;
import com.psj.welfare.viewmodel.MyPageViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class WrittenReviewCheckActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
//    ActivityWrittenReviewCheckBinding binding;
//    private com.psj.welfare.databinding.WrittenReview binding;

    TextView written_review_count, written_review_empty_textview, written_review_textview;
    RecyclerViewEmptySupport written_review_recyclerview;
    WrittenReviewAdapter adapter;
    WrittenReviewAdapter.OnItemClickListener itemClickListener;
    List<WrittenReviewItem> list;

    ProgressBar written_review_progressbar;

    MyPageViewModel viewModel;

    String page = "1";

    // 리사이클러뷰에서 보여줄 내용
    String welf_name, writer, content, create_date;
    int welf_id;
    String star_count;
    // 페이징에 필요한 내용
    String total, totalPage;

    private Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_written_review_check);

        written_review_recyclerview = findViewById(R.id.written_review_recyclerview);
        written_review_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        written_review_recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        written_review_textview = findViewById(R.id.written_review_textview);
        written_review_count = findViewById(R.id.written_review_count);
        written_review_empty_textview = findViewById(R.id.written_review_empty_textview);
        written_review_progressbar = findViewById(R.id.written_review_progressbar);

        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        written_review_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        written_review_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);

        getMyReview(page);
    }

    // 내가 작성한 리뷰 가져오기
    private void getMyReview(String page)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        viewModel = new ViewModelProvider(this).get(MyPageViewModel.class);
        final Observer<String> myReviewObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String s)
            {
                if (s != null)
                {
                    Log.e(TAG, "액티비티에서 내가 작성한 리뷰 가져온 결과 : " + s);
                    parsingResult(s);
                    dialog.dismiss();
                }
            }
        };

        viewModel.getMyReview(page).observe(this, myReviewObserver);
    }

    @SuppressLint("CheckResult")
    private void parsingResult(String result)
    {
        list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total = jsonObject.getString("total");
            totalPage = jsonObject.getString("totalPage");
            JSONArray jsonArray = jsonObject.getJSONArray("message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                welf_name = inner_json.getString("welf_name");
                writer = inner_json.getString("writer");
                content = inner_json.getString("content");
                star_count = inner_json.getString("star_count");
                create_date = inner_json.getString("create_date");
                welf_id = inner_json.getInt("welf_id");

                WrittenReviewItem item = new WrittenReviewItem();
                item.setWelf_name(welf_name);
                item.setWriter(writer);
                item.setContent(content);
                item.setStar_count(Float.parseFloat(star_count));
                item.setCreate_date(create_date);
                item.setWelf_id(welf_id);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        recyclerViewState = written_review_recyclerview.getLayoutManager().onSaveInstanceState();

        // 내가 작성한 리뷰 개수
        Flowable.just(total)
                .subscribeOn(Schedulers.io())
                .subscribe(data -> written_review_count.setText("전체 " + total + "개"));

        adapter = new WrittenReviewAdapter(this, list, itemClickListener);

        // 페이징 시 위로 자동 스크롤되는 현상 방지
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());

        // 작성한 리뷰가 없으면 텍스트뷰를 보여주고 1개라도 있다면 그 리뷰 데이터를 보여준다
        if (adapter.getItemCount() > 0)
        {
            written_review_recyclerview.setVisibility(View.VISIBLE);
            written_review_empty_textview.setVisibility(View.GONE);
            written_review_count.setText("전체 " + total + "개");
        }
        else if (adapter.getItemCount() == 0)
        {
            written_review_recyclerview.setVisibility(View.GONE);
            written_review_empty_textview.setVisibility(View.VISIBLE);
            written_review_recyclerview.setEmptyView(written_review_empty_textview);
        }

        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String writer = list.get(pos).getWriter();
            String date = list.get(pos).getCreate_date();
            int welfId = list.get(pos).getWelf_id();
            float star_counts = list.get(pos).getStar_count();
            Log.e(TAG, "이름 : " + name + ", 작성자 : " + writer + ", 작성일 : " + date + ", welf_id : " + welfId + ", 평점 : " + star_counts);
        });

        written_review_recyclerview.setAdapter(adapter);
        written_review_progressbar.setVisibility(View.GONE);

    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}