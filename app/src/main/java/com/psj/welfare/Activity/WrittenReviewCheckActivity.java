package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.adapter.WrittenReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.MyReviewListener;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.WrittenReviewItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.viewmodel.MyPageViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WrittenReviewCheckActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();
//    ActivityWrittenReviewCheckBinding binding;
//    private com.psj.welfare.databinding.WrittenReview binding;

    private ImageView written_review_back_image; //뒤로가기 버튼
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
    private String satisfaction, difficulty_level; //만족도, 난이도
    int welf_id, review_id;
    String star_count;
    // 페이징에 필요한 내용
    String total, totalPage;

    DBOpenHelper helper;
    String sqlite_token;
    String message, status;

    private Parcelable recyclerViewState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_written_review_check);

        helper = new DBOpenHelper(this);
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

        written_review_back_image = findViewById(R.id.written_review_back_image);

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

        //뒤로 가기 버튼 누르기
        written_review_back_image.setOnClickListener(v -> {
            finish();
        });
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
//                    Log.e(TAG, "액티비티에서 내가 작성한 리뷰 가져온 결과 : " + s);
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
//            Log.e(TAG,"jsonArray : " + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);
                welf_name = inner_json.getString("welf_name");
                writer = inner_json.getString("writer");
                content = inner_json.getString("content");
                star_count = inner_json.getString("star_count");
                create_date = inner_json.getString("create_date");
                welf_id = inner_json.getInt("welf_id");
                review_id = inner_json.getInt("review_id");
                satisfaction = inner_json.getString("satisfaction");
                difficulty_level = inner_json.getString("difficulty_level");

//                Log.e(TAG,"welf_name : " + welf_name);
//                Log.e(TAG,"star_count : " + star_count);

                WrittenReviewItem item = new WrittenReviewItem();
                item.setWelf_name(welf_name);
                item.setWriter(writer);
                item.setContent(content);
                item.setStar_count(Float.parseFloat(star_count));
                item.setCreate_date(create_date);
                item.setWelf_id(welf_id);
                item.setReview_id(review_id);
                item.setSatisfaction(satisfaction);
                item.setDifficulty_level(difficulty_level);
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

        adapter.setOnReviewListener(new MyReviewListener()
        {
            @Override
            public void deleteReview(boolean isRemoved, int id)
            {
                if (isRemoved)
                {
                    removeReview(id);
                }
            }
        });

        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String writer = list.get(pos).getWriter();
            String date = list.get(pos).getCreate_date();
            int welfId = list.get(pos).getWelf_id();
            int reviewId = list.get(pos).getReview_id();
            float star_counts = list.get(pos).getStar_count();
//            Log.e(TAG, "이름 : " + name + ", 작성자 : " + writer + ", 작성일 : " + date + ", welf_id : " + welfId + ", 평점 : " + star_counts
//                    + ", 리뷰의 idx : " + reviewId);
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", String.valueOf(welfId));
            intent.putExtra("review_id", String.valueOf(reviewId));
            intent.putExtra("welf_name", name);
            startActivity(intent);
        });

        written_review_recyclerview.setAdapter(adapter);
        written_review_progressbar.setVisibility(View.GONE);

    }

    private void removeReview(int review_id)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", review_id);
            jsonObject.put("is_remove", "true");

            Log.e(TAG, "삭제 api로 보낼 JSON 만들어진 것 테스트 : " + jsonObject.toString());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        Call<String> call = apiInterface.deleteReview(sqlite_token, jsonObject.toString());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    removeResponseParse(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "리뷰 삭제 에러 : " + t.getMessage());
            }
        });
    }

    private void removeResponseParse(String result)
    {
        try
        {
            JSONObject result_object = new JSONObject(result);
            status = result_object.getString("statusCode");
            message = result_object.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (status.equals("200"))
        {
            Toast.makeText(this, "리뷰가 성공적으로 삭제됐어요", Toast.LENGTH_SHORT).show();
            getMyReview(page);
            adapter.notifyDataSetChanged();
        }
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
        getMyReview(page);
    }
}