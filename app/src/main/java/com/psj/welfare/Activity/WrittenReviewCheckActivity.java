package com.psj.welfare.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.psj.welfare.util.NetworkStatus;
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

/**
 * 작성한 리뷰 화면
 */
public class WrittenReviewCheckActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    // 뒤로가기 버튼
    ImageView written_review_back_image;
    TextView written_review_count, written_review_empty_textview, written_review_textview;

    // 작성한 리뷰들을 보여주는 리사이클러뷰, 리뷰가 없으면 작성된 리뷰가 없다는 텍스트뷰 출력
    RecyclerViewEmptySupport written_review_recyclerview;
    WrittenReviewAdapter adapter;
    WrittenReviewAdapter.OnItemClickListener itemClickListener;
    List<WrittenReviewItem> list;

    // 작성한 리뷰 정보를 가져올 때 사용하는 뷰모델, 마이페이지에서만 들어갈 수 있어서 MyPageViewModel에 기능을 만듦
    MyPageViewModel viewModel;

    // 서버에서 페이징 데이터 받아오기 위해 넘길 페이지 숫자
    int page = 1;

    // 서버에서 받아와 리사이클러뷰에서 보여줄 작성한 리뷰 내용
    String welf_name, writer, content, create_date;
    // 만족도, 난이도
    String satisfaction, difficulty_level;
    // 혜택의 idx와 리뷰의 idx, 둘은 다른 것
    int welf_id, review_id;
    // 별점
    String star_count;
    // 페이징에 필요한 내용
    String total, total_page;

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
        // 인터넷 연결 상태를 체크해서 3G, 와이파이 중 하나라도 연결돼 있다면 더보기 화면에서 필요한 로직들을 진행시킴
        checkNetworkStatus();
    }

    private void checkNetworkStatus()
    {
        if (NetworkStatus.checkNetworkStatus(this) == 1 || NetworkStatus.checkNetworkStatus(this) == 2)
        {
            helper = new DBOpenHelper(this);
            helper.openDatabase();
            helper.create();

            // 리사이클러뷰 초기화 시 사용할 리스트 초기화
            list = new ArrayList<>();

            // Room DB에서 토큰 가져와 변수에 대입
            Cursor cursor = helper.selectColumns();
            if (cursor != null)
            {
                while (cursor.moveToNext())
                {
                    sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
                }
            }

            written_review_back_image = findViewById(R.id.written_review_back_image);
            written_review_textview = findViewById(R.id.written_review_textview);
            written_review_count = findViewById(R.id.written_review_count);
            written_review_empty_textview = findViewById(R.id.written_review_empty_textview);

            written_review_recyclerview = findViewById(R.id.written_review_recyclerview);
            written_review_recyclerview.setLayoutManager(new LinearLayoutManager(this));
            // 리사이클러뷰 아이템 밑에 가로 구분선이 나오도록 설정
            written_review_recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

            /* 텍스트뷰 글자 크기 설정 */
            ScreenSize screen = new ScreenSize();
            Point size = screen.getScreenSize(this);

            written_review_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
            written_review_empty_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);

            // 작성한 리뷰 가져오기
            getMyReview(String.valueOf(page));

            // 뒤로 가기 버튼
            written_review_back_image.setOnClickListener(v -> finish());

            // 리사이클러뷰 페이징 처리
            writtenReviewPaging();
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
                        }
                    }).show();
        }
    }

    /* 리사이클러뷰 페이징 처리 */
    private void writtenReviewPaging()
    {
        written_review_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener()
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
                        // 어댑터의 getItemCount() 리턴값이 0 초과인 경우(=데이터가 1개라도 있는 경우), 마지막 스크롤을 감지한 경우 페이징
                        int totalItemCount = llm.getItemCount();
                        int lastVisible = llm.findLastCompletelyVisibleItemPosition();

                        // 마지막 아이템 위치를 알아내서 페이지 숫자에 +1해 다른 리뷰 페이징 데이터 가져옴
                        if (lastVisible >= totalItemCount - 1 && page < Integer.parseInt(total_page))
                        {
                            page++;
                            getMyReview(String.valueOf(page));
                        }
                        else
                        {
                            Log.e(TAG, "서버에서 받아올 데이터가 없음");
                        }
                    }
                }
            }
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
                    parsingResult(s);
                    dialog.dismiss();
                }
            }
        };

        viewModel.getMyReview(page).observe(this, myReviewObserver);
    }

    /* getMyReview()로 가져온 리뷰 JSON을 파싱해서 리사이클러뷰에 set */
    @SuppressLint("CheckResult")
    private void parsingResult(String result)
    {
        //리뷰 초기화
        list.clear();

        Log.e(TAG, "작성한 리뷰 : " + result);
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            total = jsonObject.getString("total");
            total_page = jsonObject.getString("totalPage");
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
                review_id = inner_json.getInt("review_id");
                satisfaction = inner_json.getString("satisfaction");
                difficulty_level = inner_json.getString("difficulty_level");

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

        // 작성한 리뷰 리사이클러뷰 초기화
        adapter = new WrittenReviewAdapter(this, list, itemClickListener);

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

        // 리뷰 삭제
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

        // 리사이클러뷰의 리뷰 아이템 클릭
        adapter.setOnItemClickListener(pos ->
        {
            String name = list.get(pos).getWelf_name();
            String writer = list.get(pos).getWriter();
            String date = list.get(pos).getCreate_date();
            int welfId = list.get(pos).getWelf_id();
            int reviewId = list.get(pos).getReview_id();
            float star_counts = list.get(pos).getStar_count();
            Intent intent = new Intent(this, DetailTabLayoutActivity.class);
            intent.putExtra("welf_id", String.valueOf(welfId));
            intent.putExtra("review_id", String.valueOf(reviewId));
            intent.putExtra("welf_name", name);
            startActivity(intent);
        });

        written_review_recyclerview.setAdapter(adapter);

        // 페이징 시 위로 자동 스크롤되는 현상 방지
        written_review_recyclerview.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    /* 리뷰 삭제 메서드 */
    private void removeReview(int review_id)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", review_id);
            jsonObject.put("is_remove", "true");
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

    /* 리뷰 삭제 결과 파싱 */
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
            getMyReview(String.valueOf(page));
            adapter.notifyDataSetChanged();
        }
    }

    /* 상태바 색상 변경 */
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
        checkNetworkStatus();
//        Log.e(TAG,"TEST");
    }
}