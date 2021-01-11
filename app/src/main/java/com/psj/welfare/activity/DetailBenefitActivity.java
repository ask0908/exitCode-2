package com.psj.welfare.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.FacilitiesAdapter;
import com.psj.welfare.adapter.ReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.AESUtils;
import com.psj.welfare.util.LogUtil;

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
    public final String TAG = this.getClass().getSimpleName();

    private ConstraintLayout all_review_scene;
    private LinearLayout content_all_layout;
    private ScrollView detail_benefit_scrollview;

    // 혜택명
    private TextView welfare_desc_title;
    private String detail_data;

    // 내용, 신청방법, 리뷰
    LinearLayout content_view, review_view;
    // 내용, 신청방법, 리뷰 밑의 노란 가로 바
    View content_bottom_view, review_bottom_view;

    // 서버에서 온 값을 파싱할 때 사용할 변수
    String welf_id, welf_target, welf_contents, welf_apply, welf_contact, welf_period, welf_end, welf_local, welf_bookmark;

    // 리뷰 데이터를 조회해서 서버에서 온 값을 파싱할 때 사용할 변수 (email은 85번 줄에 이미 있어서 안 씀)
    String id, content, writer, create_date, email2, like_count, star_count, image_url;

    // 즐겨찾기 버튼
    LikeButton favorite_btn;

    // 즐겨찾기 저장 시 사용할 이메일, 혜택명, 북마크 여부
    String email, welf_name, isBookmark;

    // 리뷰 액티비티 안의 뷰들
    private ImageView review_status_image;
    private RatingBar review_rating;
    private RecyclerView review_recycler;
    private Button more_big_scene_button;

    // 리뷰 보여주는 리사이클러뷰에 붙일 어댑터
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

    // 혜택 정보를 조회할 때 지역명을 인자로 넘겨야 하는데, 지역명을 담기 위한 변수
    String user_area;

    // MapDetailActivity에서 날아온 지역명
    String map_detail_area;

    // 지원대상 텍스트 있는 레이아웃(헤더), 그 밑에 딸린 레이아웃
    LinearLayout mLinearLayoutHeader, mLinearLayout;

    // 상세내용 텍스트 있는 레이아웃(헤더), 그 밑에 딸린 레이아웃
    LinearLayout welfare_detail_content_header, detail_expandable_layout;

    // 주변시설 텍스트 있는 레이아웃
    ConstraintLayout facilities_layout;
    RecyclerView facilities_recyclerview;
    FacilitiesAdapter facilitiesAdapter;

    // 주변시설 텍스트와 하단 바가 있는 레이아웃
    LinearLayout facilities_view;
    View facilities_bottom_view, institution_bottom_view;

    // 연락처 레이아웃
    LinearLayout welfare_detail_call, welfare_detail_call_expandable;

    TextView median_income, detail_support_target_textview, facilities_textview, content_textview, review_textview, institution_textview;

    View divider_view, detail_divider_view, top_divider_view;

    ImageView first_imageview, second_imageview;

    // 리뷰 작성 버튼
    Button review_write_button;

    BarChart review_chart;

    // 미구현 레이아웃
    LinearLayout institution_view;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbenefit);

        Logger.addLogAdapter(new AndroidLogAdapter());
        sharedPreferences = getSharedPreferences("app_pref", 0);
        // 쉐어드에서 유저가 거주하는 지역명을 받아온다
        user_area = sharedPreferences.getString("user_area", "");
        // 서울, 인천 뒤에 붙는 특별시/광역시 글자를 제거한다
        if (user_area != null)
        {
            switch (user_area)
            {
                case "서울특별시" :
                    user_area = "서울";
                    break;

                case "인천광역시" :
                    user_area = "인천";
                    break;

                case "대전광역시" :
                    user_area = "대전";
                    break;

                case "울산광역시" :
                    user_area = "울산";
                    break;

                case "부산광역시" :
                    user_area = "부산";
                    break;

                case "광주광역시" :
                    user_area = "광주";
                    break;

                case "대구광역시" :
                    user_area = "대구";
                    break;

                default:
                    break;
            }
        }

        /* findViewById() 모아놓은 메서드 */
        init();
        all_review_scene.setVisibility(View.GONE);

        /* BarChart로 가로 막대그래프 만들기
        * https://github.com/blackfizz/EazeGraph */
        review_chart.clearChart();

        // 지금은 데이터가 없어서 하드코딩
        review_chart.addBar(new BarModel("5점", 50, 0xFFFF5549));
        review_chart.addBar(new BarModel("4점", 40, 0xFFFF5549));
        review_chart.addBar(new BarModel("3점", 10, 0xFFFF5549));
        review_chart.addBar(new BarModel("2점", 10, 0xFFFF5549));
        review_chart.addBar(new BarModel("1점", 5, 0xFFFF5549));

        review_chart.startAnimation();

        // 리뷰 보여주는 리사이클러뷰 처리
        review_recycler = findViewById(R.id.review_recycler);
        review_recycler.setLayoutManager(new LinearLayoutManager(this));
        review_recycler.setHasFixedSize(true);
//        review_recycler.addItemDecoration(new DividerItemDecoration(DetailBenefitActivity.this, DividerItemDecoration.VERTICAL));

        // 주변시설 리사이클러뷰
        facilities_recyclerview = findViewById(R.id.facilities_recyclerview);
        facilities_recyclerview.setHasFixedSize(true);
        facilities_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        facilitiesAdapter = new FacilitiesAdapter(this);
        facilities_recyclerview.setAdapter(facilitiesAdapter);

        // 중위소득 기준표 텍스트 클릭 시 토스트 출력(성공 시 액티비티 이동)
        median_income.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable spannable = (Spannable) median_income.getText();
        ClickableSpan span = new ClickableSpan()
        {
            @Override
            public void onClick(@NonNull View widget)
            {
                Toast.makeText(DetailBenefitActivity.this, "중위소득 기준표 클릭", Toast.LENGTH_SHORT).show();
            }
        };
        spannable.setSpan(span, 17, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 자세한 지원 대상 정보 확인 텍스트뷰
        detail_support_target_textview.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable support_target_span = (Spannable) detail_support_target_textview.getText();
        ClickableSpan support_target_clickable_span = new ClickableSpan()
        {
            @Override
            public void onClick(@NonNull View widget)
            {
                Toast.makeText(DetailBenefitActivity.this, "상세 지원 정보 클릭", Toast.LENGTH_SHORT).show();
            }
        };
        support_target_span.setSpan(support_target_clickable_span, 18, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 즐겨찾기 버튼 클릭 리스너
        favorite_btn.setOnLikeListener(new OnLikeListener()
        {
            @Override
            public void liked(LikeButton likeButton)
            {
                // 토스트로 관심사에 등록됐다는 내용을 보낸다
                String message = welfare_desc_title.getText().toString();
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
                welf_name = welfare_desc_title.getText().toString();
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
                String message = welfare_desc_title.getText().toString();
                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사에서 설정 해제했습니다", Toast.LENGTH_SHORT).show();

                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에서 삭제한다
                email = "ne0001912@gmail.com";
                welf_name = welfare_desc_title.getText().toString();
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
        else if (getIntent().hasExtra("name"))
        {
            Intent intent = getIntent();
            detail_data = intent.getStringExtra("name");
        }

        if (getIntent().hasExtra("area"))
        {
            Intent intent = getIntent();
            map_detail_area = intent.getStringExtra("area");
            Log.e(TAG, "map_detail_area : " + map_detail_area);
        }

        Log.e(TAG, "getWelfareInformation()에 넣을 혜택명 : " + detail_data);
        /* 혜택의 상세정보를 가져오는 메서드, detail_data에 null이 들어간다 */
        getWelfareInformation();

        // 리뷰 작성 화면으로 이동
//        more_big_scene_button.setOnClickListener(v -> {
//            Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
//            startActivityForResult(intent, 1);
//        });

        // 공유 모양 이미지뷰 클릭 리스너
        review_share_imageview.setOnClickListener(v -> {
            /* 카카오 링크 적용 */
            FeedTemplate params = FeedTemplate
                    // 제목, 이미지 url, 이미지 클릭 시 이동하는 위치?를 입력한다
                    // 이미지 url을 사용자 정의 파라미터로 전달하면 최대 2MB 이미지를 메시지에 담아 보낼 수 있다
                    .newBuilder(ContentObject.newBuilder("똑똑~ 혜택 정보가 도착했어요~♥", benefit_image_url,
                            LinkObject.newBuilder().setMobileWebUrl("'https://developers.kakao.com").build())
                            .setDescrption("지금 바로 " + welfare_desc_title.getText().toString() + " 혜택의 정보를 확인해 보세요!")  // 제목 밑의 본문
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
                    String result = response.body();
                    Log.e("즐겨찾기 추가", "성공 : " + result);
                }
                else
                {
                    Log.e("즐겨찾기 추가", "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.e("getBookmark() 에러 : " + t.getMessage());
            }
        });
    }

    /* 리뉴얼된 혜택 상세정보들을 가져오는 메서드 (UI 변경으로 jsonParsing() 안의 값들 세팅되는 뷰 바꿔야 함) */
    void getWelfareInformation()
    {
        String token = sharedPreferences.getString("token" ,"");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getWelfareInformation("detail", map_detail_area, detail_data, token, LogUtil.getUserLog());
        Log.e("fff", "type = detail, map_detail_area = " + map_detail_area + ", map_detail_area = " + detail_data + ", token = " + token + ", " + LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    jsonParsing(result);
                    Log.e(TAG, "성공 = " + response.body());
                }
                else
                {
                    Log.e(TAG, "실패 = " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // findViewById() 모아놓은 메서드
    private void init()
    {
        detail_benefit_scrollview = findViewById(R.id.detail_benefit_scrollview);
        welfare_desc_title = findViewById(R.id.welfare_desc_title);

        content_view = findViewById(R.id.content_view);
        review_view = findViewById(R.id.review_view);
        content_bottom_view = findViewById(R.id.content_bottom_view);
        review_bottom_view = findViewById(R.id.review_bottom_view);

        favorite_btn = findViewById(R.id.favorite_btn);

        review_share_imageview = findViewById(R.id.review_share_imageview);

        content_all_layout = findViewById(R.id.content_all_layout);
        top_divider_view = findViewById(R.id.top_divider_view);

        mLinearLayout = findViewById(R.id.expandable_layout);
        mLinearLayoutHeader = findViewById(R.id.header_layout);
        detail_expandable_layout = findViewById(R.id.detail_expandable_layout);
        welfare_detail_content_header = findViewById(R.id.welfare_detail_content);
        median_income = findViewById(R.id.median_income_base_table_textview);
        detail_support_target_textview = findViewById(R.id.detail_support_target_textview);
        welfare_detail_call = findViewById(R.id.welfare_detail_call);
        welfare_detail_call_expandable = findViewById(R.id.welfare_detail_call_expandable);
        facilities_layout = findViewById(R.id.facilities_layout);
        content_view = findViewById(R.id.content_view);

        facilities_view = findViewById(R.id.facilities_view);
        facilities_textview = findViewById(R.id.facilities_textview);

        content_textview = findViewById(R.id.content_textview);
        content_bottom_view = findViewById(R.id.content_bottom_view);
        divider_view = findViewById(R.id.divider_view);
        facilities_bottom_view = findViewById(R.id.facilities_bottom_view);
        detail_divider_view = findViewById(R.id.detail_divider_view);
        review_textview = findViewById(R.id.review_textview);

        institution_view = findViewById(R.id.institution_view);

        first_imageview = findViewById(R.id.first_imageview);
        second_imageview = findViewById(R.id.second_imageview);

        institution_view = findViewById(R.id.institution_view);
        all_review_scene = findViewById(R.id.all_review_scene);
        institution_bottom_view = findViewById(R.id.institution_bottom_view);
        institution_textview = findViewById(R.id.institution_textview);

        review_chart = findViewById(R.id.review_chart);
        review_write_button = findViewById(R.id.review_write_button);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // 리뷰 작성 버튼
        review_write_button.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // 내용 클릭
        content_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.VISIBLE);
                facilities_bottom_view.setVisibility(View.GONE);
                facilities_layout.setVisibility(View.GONE);
                all_review_scene.setVisibility(View.GONE);
                review_textview.setTextColor(getColor(R.color.colorGray_B));
                review_bottom_view.setVisibility(View.GONE);
                top_divider_view.setVisibility(View.VISIBLE);
                content_all_layout.setVisibility(View.VISIBLE);
                content_textview.setTextColor(getColor(R.color.colorBlack));
                facilities_textview.setTextColor(getColor(R.color.colorGray_B));

                mLinearLayoutHeader.setVisibility(View.VISIBLE);
                mLinearLayout.setVisibility(View.VISIBLE);
                divider_view.setVisibility(View.VISIBLE);
                welfare_detail_content_header.setVisibility(View.VISIBLE);
                detail_expandable_layout.setVisibility(View.VISIBLE);
                detail_divider_view.setVisibility(View.VISIBLE);
                welfare_detail_call.setVisibility(View.VISIBLE);
                welfare_detail_call_expandable.setVisibility(View.VISIBLE);
            }
        });

        // 주변시설 클릭
        facilities_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.GONE);
                facilities_bottom_view.setVisibility(View.VISIBLE);
                review_textview.setTextColor(getColor(R.color.colorGray_B));
                top_divider_view.setVisibility(View.VISIBLE);
                facilities_layout.setVisibility(View.VISIBLE);
                content_all_layout.setVisibility(View.GONE);
                review_bottom_view.setVisibility(View.GONE);
                content_textview.setTextColor(getColor(R.color.colorGray_B));
                facilities_textview.setTextColor(getColor(R.color.colorBlack));

                // 주변시설을 누르면 주변시설 관련 레이아웃, 뷰를 제외한 다른 뷰들은 GONE 처리해서 보이지 않게 한다
                mLinearLayoutHeader.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.GONE);
                divider_view.setVisibility(View.GONE);
                all_review_scene.setVisibility(View.GONE);
                welfare_detail_content_header.setVisibility(View.GONE);
                detail_expandable_layout.setVisibility(View.GONE);
                detail_divider_view.setVisibility(View.GONE);
                welfare_detail_call.setVisibility(View.GONE);
                welfare_detail_call_expandable.setVisibility(View.GONE);
            }
        });

        // 리뷰 클릭
        review_textview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.GONE);
                facilities_bottom_view.setVisibility(View.GONE);
                facilities_layout.setVisibility(View.GONE);
                content_all_layout.setVisibility(View.GONE);
                top_divider_view.setVisibility(View.GONE);
                all_review_scene.setVisibility(View.VISIBLE);
                content_textview.setTextColor(getColor(R.color.colorGray_B));
                facilities_textview.setTextColor(getColor(R.color.colorGray_B));
                review_textview.setTextColor(getColor(R.color.colorBlack));
                review_bottom_view.setVisibility(View.VISIBLE);
                review_bottom_view.setBackgroundColor(getColor(R.color.colorBlack));
                isClicked = true;
                getReview();
                detail_benefit_scrollview.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!isClicked)
                        {
                            // 스크롤이 올라가는 지점은 별점 부분이 맨 위로 올 때까지 올라간다
                            ObjectAnimator.ofInt(detail_benefit_scrollview, "scrollY", all_review_scene.getTop()).setDuration(500).start();
                        }
                        else
                        {
                            ObjectAnimator.ofInt(detail_benefit_scrollview, "scrollY", all_review_scene.getTop()).setDuration(500).start();
                        }
                    }
                });
            }
        });

        // 신청기관 클릭(미구현)
        institution_textview.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Toast.makeText(DetailBenefitActivity.this, "준비중인 서비스입니다", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 서버에서 가져온 String 형태의 JSON 값들을 파싱한 뒤, 특수문자를 콤마로 바꿔서 뷰에 set하는 메서드
    /* GSON 써서도 해보자 */
    private void jsonParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_obj = jsonArray.getJSONObject(i);
                welf_id = inner_obj.getString("id");
                welf_name = inner_obj.getString("welf_name");
                welf_contents = inner_obj.getString("welf_contents");
                welf_apply = inner_obj.getString("welf_apply");
                welf_target = inner_obj.getString("welf_target");
                welf_contact = inner_obj.getString("welf_contact");
                welf_period = inner_obj.getString("welf_period");
                welf_end = inner_obj.getString("welf_end");
                welf_local = inner_obj.getString("welf_local");
                welf_bookmark = inner_obj.getString("isBookmark");
            }
            if (welf_bookmark.equals(getString(R.string.is_true)))
            {
                favorite_btn.setLiked(true);
            }
            else
            {
                favorite_btn.setLiked(false);
            }

            symbolChange(welf_target, welf_contents, welf_contact);
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

    // 혜택의 대상, 내용, 연락처에 붙어있는 특수문자를 콤마(,)로 바꾸는 메서드
    private void symbolChange(String welf_target, String welf_content, String welf_contact)
    {
        if (welf_target != null)
        {
            String target_line = welf_target.replace("^;", "\n");
            String target_comma = target_line.replace(";;", ",");
//            detail_target.setText(target_comma);
        }
        if (welf_content != null)
        {
            String contents_line = welf_content.replace("^;", "\n");
            String contents_comma = contents_line.replace(";;", ",");
            Log.e(TAG, ";;를 ,로 변환한 결과 : " + contents_comma);
//            detail_contents.setText(contents_comma);
        }
        if (welf_contact != null)
        {
            String contact_line = welf_contact.replace("^;", "\n");
            String contact_comma = contact_line.replace(";;", ",");
            Log.e(TAG, ";;를 ,로 변환한 결과 : " + contact_line);
//            detail_contact.setText(contact_comma);
        }
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
