package com.psj.welfare.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.DetailBenefitRecyclerAdapter;
import com.psj.welfare.adapter.ReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.AESUtils;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 혜택 상세보기 화면, 리뷰 화면도 존재함 */
public class DetailBenefitActivity extends AppCompatActivity
{
    private ConstraintLayout layout;
    private ScrollView review_scrollview;
    public final String TAG = this.getClass().getName();

    private String detail_data; // 상세 페이지 타이틀

    // 대상, 내용, 기간 텍스트 View
    private TextView detail_contents, detail_title, detail_contact, detail_target;
    // 타이틀과 연관된 이미지 View
    private ImageView detail_logo;

    // 내용, 신청방법, 리뷰
    LinearLayout apply_view, content_view, review_view;
    // 내용, 신청방법, 리뷰 밑의 노란 가로 바
    View apply_bottom_view, content_bottom_view, review_bottom_view;

    // 서버에서 온 값을 파싱할 때 사용할 변수
    String name, target, contents, period, contact, benefit_no, welf_apply;

    // 리뷰 데이터를 조회해서 서버에서 온 값을 파싱할 때 사용할 변수 (email은 85번 줄에 이미 있어서 안 씀)
    String id, content, writer, create_date, email2, like_count, star_count, image_url;

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
    ReviewAdapter.ItemClickListener review_clickListener;

    // 리뷰 수정, 삭제 시 작성자와 유저 닉네임이 일치하는지 확인할 때 사용할 쉐어드
    SharedPreferences sharedPreferences;

    // 클릭 시 리뷰 위로 스크롤을 올리기 위한 boolean 변수
    boolean isClicked = false;

    // 막대그래프
    private BarChart chart;

    // 혜택 내용을 타인에게 공유할 때 사용할 이미지뷰
    ImageView review_share_imageview;

    // 혜택 이미지를 담은 url (암호화 해야 함)
    String benefit_image_url = "http://3.34.64.143/images/reviews/조제분유.jpg";
    String encrypted_email, decrypted_email;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbenefit);

        Logger.addLogAdapter(new AndroidLogAdapter());
        sharedPreferences = getSharedPreferences("app_pref", 0);

        /* findViewById() 모아놓은 메서드 */
        init();
        layout.setVisibility(View.GONE);

        /* BarChart로 가로 막대그래프 만들기 */
        chart.clearChart();

        // 지금은 데이터가 없어서 하드코딩
        chart.addBar(new BarModel("5점", 50, 0xFF56B7F1));
        chart.addBar(new BarModel("4점", 40, 0xFF56B7F1));
        chart.addBar(new BarModel("3점", 10, 0xFF56B7F1));
        chart.addBar(new BarModel("2점", 10, 0xFF56B7F1));
        chart.addBar(new BarModel("1점", 5, 0xFF56B7F1));

        chart.startAnimation();

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
                // 토스트로 관심사에 등록됐다는 내용을 보낸다
                String message = detail_title.getText().toString();
                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사로 설정했습니다", Toast.LENGTH_SHORT).show();

                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에 저장한다. 지금은 이메일을 하드코딩해 사용하지만 암호화를 해서 저장해야 한다
                email = getString(R.string.email);
                try
                {
                    encrypted_email = AESUtils.encrypt(email);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    decrypted_email = AESUtils.decrypt(encrypted_email);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                Logger.e("encrypted_email = " + encrypted_email);
                Logger.e("decrypted_email = " + decrypted_email);
                welf_name = detail_title.getText().toString();
                isBookmark = "true";

                // 메서드 호출
                getBookmark(decrypted_email, welf_name);
            }

            @Override
            public void unLiked(LikeButton likeButton)
            {
                // 회색 하트로 변하면 false를 0으로 치고 서버로 보낸다
                boolean isClicked = favorite_btn.isLiked();
                Logger.e("즐찾 버튼 클릭 상태 : " + isClicked);

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

            Logger.e("상세 페이지에서 보여줄 타이틀 : " + detail_data);
        }

        // 리사이클러뷰 처리
        detail_benefit_recyclerview.setHasFixedSize(true);
        detail_benefit_recyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        adapter = new DetailBenefitRecyclerAdapter(this, lists, itemClickListener);
        detail_benefit_recyclerview.setAdapter(adapter);

        // 리사이클러뷰 어댑터 안의 클릭 리스너를 액티비티 위에 만든다
        itemClickListener = (view, position) -> Logger.e("pos = " + position);

        String email = "ne0001912@gmail.com";
        detailData(detail_data, email);

        // 리뷰 작성 화면으로 이동
        more_big_scene_button.setOnClickListener(v -> {
            Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
            startActivityForResult(intent, 1);
        });

        // 공유 모양 이미지뷰 클릭 리스너
        review_share_imageview.setOnClickListener(v -> {
            /* 카카오 링크 적용 */
            FeedTemplate params = FeedTemplate
                    // 제목, 이미지 url, 이미지 클릭 시 이동하는 위치?를 입력한다
                    // 이미지 url을 사용자 정의 파라미터로 전달하면 최대 2MB 이미지를 메시지에 담아 보낼 수 있다
                    .newBuilder(ContentObject.newBuilder("똑똑~ 혜택 정보가 도착했어요~♥", benefit_image_url,
                            LinkObject.newBuilder().setMobileWebUrl("'https://developers.kakao.com").build())
                            .setDescrption("지금 바로 " + detail_title.getText().toString() + " 혜택의 정보를 확인해 보세요!")  // 제목 밑의 본문
                            .build())
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                            .setWebUrl("'https://developers.kakao.com")
                            .setMobileWebUrl("'https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder()
                            .setWebUrl("https://developers.kakao.com")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20).setSharedCount(30).build())   // 좋아요 수, 댓글(리뷰) 수, 공유된 회수 더미
                    .build();

            Map<String, String> serverCallbackArgs = new HashMap<>();
            serverCallbackArgs.put("user_id", "${current_user_id}");
            serverCallbackArgs.put("product_id", "${shared_product_id}");

            KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    Logger.e(errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {
                    // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용해야 한다.
                    Logger.e(result.toString());
                }
            });
        });

    }

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
                    Logger.e("getBookmark()의 success 부분 = " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.e("getBookmark() 에러 : " + t.getMessage());
            }
        });
    }

    /* 리뉴얼된 혜택 상세정보들을 가져오는 메서드드 */
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
                    Logger.e("detailData() - onResponse() = " + response.body());
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

        review_rating = findViewById(R.id.review_rating);
        more_big_scene_button = findViewById(R.id.more_big_scene_button);

        chart = findViewById(R.id.review_chart);
        review_share_imageview = findViewById(R.id.review_share_imageview);
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
            if (isBookmark.equals(getString(R.string.is_true)))
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

    // list 문자열, review_id(혜택 id 정보)를 서버로 넘겨 리뷰들을 가져오는 메서드
    void getReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        /* review_id에는 리뷰의 idx를 가져와서 넣어야 한다 */
        Call<String> call = apiInterface.getReview("list", "1019");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String detail = response.body();
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
            JSONArray jsonArray = jsonObject_total.getJSONArray("Message");
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
                value.setId(id);    // id -> writer 변경
                value.setContent(content);
                value.setCreate_date(create_date);
                value.setImage_url(image_url);
                value.setWriter(writer);
                value.setStar_count(Float.parseFloat(star_count));  // String으로 오기 때문에 float로 캐스팅해야 함
                list.add(value);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        review_adapter = new ReviewAdapter(DetailBenefitActivity.this, list, review_clickListener);
        review_adapter.setOnItemClickListener(new ReviewAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                // 쉐어드에서 가져온 user_nickname 값과 writer 값을 가져와서 비교한 다음 일치하면 수정, 삭제를 할 수 있게 한다
                String user_nickname = sharedPreferences.getString("user_nickname", "");
                String writer_nickname = list.get(position).getWriter();
                String review_content = list.get(position).getContent();
                String posting_id = list.get(position).getId();
                Log.e(TAG, "작성자 = " + writer_nickname);
                Log.e(TAG, "리뷰 내용 = " + review_content);
                Log.e(TAG, "글 id = " + posting_id);
                if (user_nickname.equals(writer_nickname))
                {
                    // 인텐트로 값들을 갖고 이동해 삭제할 수 있게 한다
                    String image_url = list.get(position).getImage_url();
                    Intent intent = new Intent(DetailBenefitActivity.this, ReviewUpdateActivity.class);
                    intent.putExtra("image_url", image_url);
                    intent.putExtra("content", review_content);
                    intent.putExtra("id", posting_id);
                    startActivity(intent);
                }
            }
        });
        review_recycler.setAdapter(review_adapter);
    }

}
