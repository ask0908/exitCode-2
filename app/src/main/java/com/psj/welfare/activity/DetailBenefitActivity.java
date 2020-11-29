package com.psj.welfare.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.DetailBenefitRecyclerAdapter;
import com.psj.welfare.adapter.ReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 상세 액티비티는 사용자가 자세한 혜택의 내용을 확인할 수 있어야 한다
 * 자세한 내용일지라도 사용자의 피로도를 줄여줄 수 있는 방법이 적용돼야 한다
 * */
public class DetailBenefitActivity extends AppCompatActivity
{
    private ConstraintLayout layout;
    private LinearLayout footer_layout;
    private ScrollView review_scrollview;
    public final String TAG = this.getClass().getName(); // 로그 찍을 때 사용하는 TAG

    private String detail_data; // 상세 페이지 타이틀

    // 대상, 내용, 기간 텍스트 View
    private TextView detail_contents, detail_title, detail_contact, detail_target;
    // 타이틀과 연관된 이미지 View
    private ImageView detail_logo;

    // 지원대상, 지원내용, 리뷰
    LinearLayout apply_view, content_view, review_view;
    View apply_bottom_view, content_bottom_view, review_bottom_view;

    // 서버에서 온 값을 파싱할 때 사용할 변수
    String name, target, contents, period, contact, benefit_no, welf_apply;

    // 리뷰 데이터를 조회해서 서버에서 온 값을 파싱할 때 사용할 변수 (email은 85번 줄에 이미 있어서 안 씀)
    String id, content, writer, create_date, email2, like_count, bad_count, star_count, image_url;

    // 연관된 혜택 밑의 가로로 버튼들을 넣을 리사이클러뷰
    private RecyclerView detail_benefit_recyclerview;
    private DetailBenefitRecyclerAdapter adapter;
    private DetailBenefitRecyclerAdapter.ItemClickListener itemClickListener;
    ArrayList<DetailBenefitItem> lists = new ArrayList<>();

    // 즐겨찾기 버튼
    LikeButton favorite_btn;

    // 즐겨찾기 저장 시 사용할 이메일, 혜택명, 북마크 여부
    String email, welf_name, isBookmark;

    // 리뷰 작성하는 레이아웃을 VISIBLE로 바꾸면 GONE으로 바꿔야 하는 레이아웃
    LinearLayout content_apply_layout;

    // 리뷰 액티비티 안의 뷰들
    private ImageView review_status_image;
    private RatingBar review_rating;
    private RecyclerView review_recycler;
    private Button more_big_scene_button;

    ReviewAdapter review_adapter;
//    List<ReviewItem> list = new ArrayList<>();

    // 리뷰 내용을 담을 변수, 정책 이름을 담을 변수
    String review_content;

    // 클릭 시 리뷰 위로 스크롤을 올리기 위한 boolean 변수
    boolean isClicked = false;

    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbenefit);

        /* findViewById() 모아놓은 메서드 */
        init();
        layout.setVisibility(View.GONE);

        /* BarChart */
        chart.clearChart();

        chart.addBar(new BarModel("5점", 50, 0xFF56B7F1));
        chart.addBar(new BarModel("4점", 40, 0xFF56B7F1));
        chart.addBar(new BarModel("3점", 10, 0xFF56B7F1));
        chart.addBar(new BarModel("2점", 10, 0xFF56B7F1));
        chart.addBar(new BarModel("1점", 5, 0xFF56B7F1));

        chart.startAnimation();

        /* 가로 MPAndroidChart 처리 */
//        chart.setMaxVisibleValueCount(60);
//        chart.setPinchZoom(false);
//        chart.setDrawGridBackground(false);
//
//        MyXAxisValueFormatter formatter = new MyXAxisValueFormatter();
//
//        BarDataSet set1;
//        set1 = new BarDataSet(formatter.getDataSet(), "The year 2017");
//
//        set1.setColors(Color.parseColor("#F78B5D"),
//                Color.parseColor("#FCB232"),
//                Color.parseColor("#FDD930"),
//                Color.parseColor("#ADD137"),
//                Color.parseColor("#A0C25A")
//        );
//
//        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
//        dataSets.add(set1);
//
//        BarData data = new BarData(dataSets);
//
//        // hide Y-axis
//        YAxis left = chart.getAxisLeft();
//        left.setDrawLabels(false);
//
//        // custom X-axis labels
//        String[] values = new String[] { "1 star", "2 stars", "3 stars", "4 stars", "5 stars"};
//        XAxis xAxis = chart.getXAxis();
//        chart.getXAxis().setDrawGridLines(false);   // 구분선(Grid Lines) 제거
//        chart.setDrawGridBackground(false);
//        xAxis.setValueFormatter(new MyXAxisValueFormatter(values));
//
//        chart.setData(data);
//
//        // custom description
//        Description description = new Description();
//        description.setText("");
//        chart.setDescription(description);
//
//        // hide legend
//        chart.getLegend().setEnabled(false);
//
//        chart.animateY(1000);
//        chart.invalidate();

        // 리사이클러뷰 처리
        review_recycler = findViewById(R.id.review_recycler);
        review_recycler.setLayoutManager(new LinearLayoutManager(this));
        review_recycler.setHasFixedSize(true);
        review_recycler.addItemDecoration(new DividerItemDecoration(DetailBenefitActivity.this, DividerItemDecoration.VERTICAL));   // 아이템 간 구분선

        // 즐겨찾기 버튼 클릭 리스너
        favorite_btn.setOnLikeListener(new OnLikeListener()
        {
            @Override
            public void liked(LikeButton likeButton)
            {
                // 빨간색 하트로 변하면 true를 1로 치고 서버로 보낸다
                boolean isClicked = favorite_btn.isLiked();
                Log.e(TAG, "즐찾 버튼 클릭 상태 : " + isClicked);

                // 토스트로 관심사에 등록됐다는 내용을 보낸다
                String message = detail_title.getText().toString();
                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사로 설정했습니다", Toast.LENGTH_SHORT).show();

                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에 저장한다
                email = "ne0001912@gmail.com";
                welf_name = detail_title.getText().toString();
                isBookmark = "true";

                // 메서드 호출
                getBookmark(email, welf_name);
            }

            @Override
            public void unLiked(LikeButton likeButton)
            {
                // 회색 하트로 변하면 false를 0으로 치고 서버로 보낸다
                boolean isClicked = favorite_btn.isLiked();
                Log.e(TAG, "즐찾 버튼 클릭 상태 : " + isClicked);

                // 토스트로 관심사에서 등록 해제됐다는 내용을 보낸다
                String message = detail_title.getText().toString();
                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사에서 설정 해제했습니다", Toast.LENGTH_SHORT).show();

                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에서 삭제한다
                email = "ne0001912@gmail.com";
                welf_name = detail_title.getText().toString();
                isBookmark = "false";

                // 메서드 호출
                getBookmark(email, welf_name);
            }
        });

        // 인텐트에 혜택 이름이 포함돼 있는지 확인 후 있는 경우에만 가져온다
        if (getIntent().hasExtra("RBF_title"))
        {
            Intent RBF_intent = getIntent();
            detail_data = RBF_intent.getStringExtra("RBF_title");

            Log.e(TAG, "상세 페이지에서 보여줄 타이틀 : " + detail_data);
        }

        // 리사이클러뷰 처리
        detail_benefit_recyclerview.setHasFixedSize(true);
        detail_benefit_recyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapter = new DetailBenefitRecyclerAdapter(this, lists, itemClickListener);
        detail_benefit_recyclerview.setAdapter(adapter);

        // 리사이클러뷰 어댑터 안의 클릭 리스너를 액티비티 위에 만든다
        itemClickListener = new DetailBenefitRecyclerAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                Log.e(TAG, "pos = " + position);
            }
        };

        String email = "ne0001912@gmail.com";
        detailData("test", email);

        // 리뷰 작성 화면으로 이동
        more_big_scene_button.setOnClickListener(v -> {
            Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
            startActivityForResult(intent, 1);
        });

    } // onCreate() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            // 조건에 맞춰 리뷰를 작성하고 작성 버튼을 누르면 이 곳으로 돌아와서, 서버에서 리뷰 데이터를 가져오는 메서드를 호출해 갱신 효과를 낸다
            getReview();
        }
    }

    // 즐겨찾기 추가
    void getBookmark(String email, String welf_name)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getBookmark(email, welf_name);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "response : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "getBookmark() 에러 : " + t.getMessage());
            }
        });
    }

    // 선택한 혜택의 상세정보들을 서버에서 가져오는 메서드
    void detailData(String detail_data, String email)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "상세 내용 불러올 정책 제목 : " + detail_data);
        Call<String> call = apiInterface.detailData(detail_data, email);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    // 성공했을 경우 서버에서 정책 제목들을 가져온다
//					Log.e(TAG, "onResponse 성공 : " + response.body());
                    String detail = response.body();
                    jsonParsing(detail);
                }
                else
                {
                    Log.e(TAG, "onResponse 실패");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "onFailure : " + t.getMessage());
            }
        });
    }

    // findViewById() 모아놓은 메서드
    private void init()
    {
        layout = findViewById(R.id.all_review_scene);
        footer_layout = findViewById(R.id.footer_layout);
        review_scrollview = findViewById(R.id.review_scrollview);

        detail_logo = findViewById(R.id.detail_logo);

        detail_title = findViewById(R.id.detail_title);
        detail_target = findViewById(R.id.detail_target);
        detail_contents = findViewById(R.id.detail_contents);
        detail_contact = findViewById(R.id.detail_contact);

        apply_view = findViewById(R.id.apply_view);
        content_view = findViewById(R.id.content_view);
        review_view = findViewById(R.id.review_view);
        apply_bottom_view = findViewById(R.id.apply_bottom_view);
        content_bottom_view = findViewById(R.id.content_bottom_view);
        review_bottom_view = findViewById(R.id.review_bottom_view);

        detail_benefit_recyclerview = findViewById(R.id.detail_benefit_btn_recyclerview);
        favorite_btn = findViewById(R.id.favorite_btn);

        content_apply_layout = findViewById(R.id.content_apply_layout);

//        review_status_image = findViewById(R.id.review_status_image);
        review_rating = findViewById(R.id.review_rating);
        more_big_scene_button = findViewById(R.id.more_big_scene_button);

        chart = findViewById(R.id.review_chart);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // 좌상단 로고를 클릭하면 프래그먼트들이 있는 화면으로 돌아간다
        detail_logo.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent d_intent = new Intent(DetailBenefitActivity.this, MainTabLayoutActivity.class);
                startActivity(d_intent);
                finish();
            }
        });

        // 내용을 누르면 신청방법, 리뷰 밑의 노란 바가 사라지고, 내용 밑에 노란 바가 생겨서 어떤 걸 클릭했는지 알려주게 한다
        content_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_apply_layout.setVisibility(View.VISIBLE);
                content_bottom_view.setVisibility(View.VISIBLE);
                apply_bottom_view.setVisibility(View.GONE);
                review_bottom_view.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                footer_layout.setVisibility(View.VISIBLE);
            }
        });

        // 신청방법을 누르면 내용, 리뷰 밑의 노란 바가 사라지고, 신청방법 밑에 노란 바가 생겨서 어떤 걸 클릭했는지 알려주게 한다
        apply_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_apply_layout.setVisibility(View.VISIBLE);
                apply_bottom_view.setVisibility(View.VISIBLE);
                content_bottom_view.setVisibility(View.GONE);
                review_bottom_view.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                footer_layout.setVisibility(View.VISIBLE);
            }
        });

        // 리뷰를 누르면 내용, 신청방법 밑의 노란 바가 사라지고, 신청방법 밑에 노란 바가 생겨서 어떤 걸 클릭했는지 알려주게 한다
        review_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                isClicked = true;
                getReview();
                content_apply_layout.setVisibility(View.GONE);
                review_bottom_view.setVisibility(View.VISIBLE);
                content_bottom_view.setVisibility(View.GONE);
                apply_bottom_view.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                footer_layout.setVisibility(View.GONE);
                // 리뷰를 누르면 리뷰 위로 스크롤이 자동으로 올라가게 한다
                review_scrollview.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!isClicked)
                        {
                            // 스크롤이 올라가는 지점은 이 혜택의 점수는요 텍스트뷰가 맨 위로 올 때까지 올라간다
                            ObjectAnimator.ofInt(review_scrollview, "scrollY", layout.getTop()).setDuration(500).start();
                        }
                        else
                        {
                            ObjectAnimator.ofInt(review_scrollview, "scrollY", layout.getTop()).setDuration(500).start();
                        }
                    }
                });
            }
        });

    }

    // 서버에서 가져온 String 형태의 JSON 값들을 파싱한 뒤, 특수문자를 콤마로 바꿔서 뷰에 set하는 메서드
    /* GSON 써서도 해보자 */
    private void jsonParsing(String detail)
    {
        try
        {
            JSONObject jsonObject_total = new JSONObject(detail);
            String retBody_data;

            retBody_data = jsonObject_total.getString("retBody");

            Log.e(TAG, "retBody 내용 : " + retBody_data);

            JSONObject jsonObject_detail = new JSONObject(retBody_data);

            benefit_no = jsonObject_detail.getString("benefit_no");
            name = jsonObject_detail.getString("welf_name");
            target = jsonObject_detail.getString("welf_target");
            contents = jsonObject_detail.getString("welf_contents");
            period = jsonObject_detail.getString("welf_period");
            welf_apply = jsonObject_detail.getString("welf_apply");
            contact = jsonObject_detail.getString("welf_contact");
            isBookmark = jsonObject_detail.getString("isBookmark");

            // 서버에서 true 값을 가져왔으면 빨간 하트로 표현하고, false 값을 가져왔으면 회색 하트 그대로 둔다
            if (isBookmark.equals("true"))
            {
                // 좋아요 버튼 활성화시킴
                favorite_btn.setLiked(true);
            }
            else
            {
                favorite_btn.setLiked(false);
            }

            detail_title.setText(name);
            detail_target.setText(target);
            detail_contents.setText(contents);
            detail_contact.setText(contact);

            symbolChange(target, contents, contact);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    // 더보기 기능
    public void setReadMoreText(ReadMoreTextView target, ReadMoreTextView contents)
    {

        target.setTrimCollapsedText("더보기");
        target.setTrimExpandedText("\t...간략히");
        target.setTrimLength(20);
        target.setColorClickableText(ContextCompat.getColor(this, R.color.colorMainBlue));

        contents.setTrimCollapsedText("더보기");
        contents.setTrimExpandedText("\t...간략히");
        contents.setTrimLength(20);
        contents.setColorClickableText(ContextCompat.getColor(this, R.color.colorMainBlue));

    }

    // 혜택의 대상, 내용, 연락처에 붙어있는 특수문자를 콤마(,)로 바꾸는 기능
    public void symbolChange(String target, String contents, String contact)
    {
        String target_line = target.replace("^;", "\n");
        String target_comma = target_line.replace(";;", ",");
//        Log.e(TAG, "특수기호 변환 후 : " + target_comma);

        String contents_line = contents.replace("^;", "\n");
        String contents_comma = contents_line.replace(";;", ",");
//        Log.e(TAG, "특수기호 변환 후 : " + contents_comma);

        String contact_line = contact.replace("^;", "\n");
        String contact_comma = contact_line.replace(";;", ",");
//        Log.e(TAG, "특수기호 변환 후 : " + contact_comma);

        detail_target.setText(target_comma);
        detail_contents.setText(contents_comma);
        detail_contact.setText(contact_comma);
    }

    // 리뷰를 보고자 하는 정책의 이름, 운영체제 이름을 서버로 넘겨 리뷰들을 가져오는 메서드
    void getReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getReview("test", "android");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String detail = response.body();
                    Log.e(TAG, "response = " + response.body());
                    jsonParse(detail);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e("getReview()", "에러 : " + t.getMessage());
            }
        });
    }

    // getReview()에서 JSON 값을 파싱하기 위해 따로 만든 메서드
    private void jsonParse(String detail)
    {
        List<ReviewItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject_total = new JSONObject(detail);
            JSONArray jsonArray = jsonObject_total.getJSONArray("retBody");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                id = jsonObject.getString("id");
                content = jsonObject.getString("content");
                writer = jsonObject.getString("writer");
                email2 = jsonObject.getString("email");
                create_date = jsonObject.getString("create_date");
                like_count = jsonObject.getString("like_count");
                star_count = jsonObject.getString("star_count");
                image_url = jsonObject.getString("image_url");
                /* 아래 처리를 하지 않으면 아이템 개수는 정상적으로 서버에서 가져온 만큼 생성되지만, 표시될 땐 마지막으로 받은 데이터만 표시된다!!! */
                ReviewItem value = new ReviewItem();
                value.setId(writer);    // id -> writer 변경
                value.setContent(content);
                value.setCreate_date(create_date);
                value.setImage_url(image_url);
                value.setStar_count(Float.parseFloat(star_count));  // String으로 오기 때문에 float로 캐스팅해야 함
//                value.setStar_count(Float.parseFloat(star_count));
//                item.id = id;
//                item.content = content;
//                item.create_date = create_date;
//                item.image_url = image_url;
                list.add(value);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        review_adapter = new ReviewAdapter(DetailBenefitActivity.this, list);
        review_recycler.setAdapter(review_adapter);
    }

}
