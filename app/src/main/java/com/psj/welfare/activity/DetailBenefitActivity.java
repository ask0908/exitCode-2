package com.psj.welfare.activity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.google.android.material.tabs.TabLayout;
import com.hedgehog.ratingbar.RatingBar;
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
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.Data.ReviewStatsItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.FacilitiesAdapter;
import com.psj.welfare.adapter.ReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.util.LogUtil;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private String push_welf_name;

    // 내용, 신청방법, 리뷰
    LinearLayout content_view, review_view;
    // 내용, 신청방법, 리뷰 밑의 노란 가로 바
    View content_bottom_view, review_bottom_view;

    // 서버에서 온 값을 파싱할 때 사용할 변수
    String welf_id, welf_target, welf_contents, welf_apply, welf_contact, welf_period, welf_end, welf_local, welf_bookmark, push_welf_local, welfare_target,
    welfare_target_tag, welf_image, welf_wording = "";

    // 리뷰 데이터를 조회해서 서버에서 온 값을 파싱할 때 사용할 변수 (email은 85번 줄에 이미 있어서 안 씀)
    String id, content, writer, create_date, like_count, star_count, image_url, review_count = "";
    // 리뷰 개수 보여줄 텍스트뷰
    TextView total_review_count;

    // 즐겨찾기 버튼
    LikeButton favorite_btn;

    // 즐겨찾기 저장 시 사용할 이메일, 혜택명, 북마크 여부
    String email, welf_name, isBookmark;

    // 리뷰 목록 보여주는 리사이클러뷰
    private RecyclerView review_recycler;

    // 리뷰 보여주는 리사이클러뷰에 붙일 어댑터
    ReviewAdapter review_adapter;
    ReviewAdapter.ItemClickListener review_clickListener;
    ReviewAdapter.DeleteClickListener deleteClickListener;

    // 리뷰 수정, 삭제 시 작성자와 유저 닉네임이 일치하는지 확인할 때 사용할 쉐어드
    SharedPreferences sharedPreferences;

    // 클릭 시 리뷰 위로 스크롤을 올리기 위한 boolean 변수
    boolean isClicked = false;

    // 혜택 내용을 타인에게 공유할 때 사용할 이미지뷰
    ImageView review_share_imageview;

    // 혜택 이미지를 담은 url (암호화 해야 함)
    String benefit_image_url = "http://3.34.64.143/images/reviews/조제분유.jpg";

    // 혜택 정보를 조회할 때 지역명을 인자로 넘겨야 하는데, 지역명을 담기 위한 변수
    String user_area;

    // MapDetailActivity에서 날아온 지역명
    String map_detail_area;

    // 지원대상 텍스트 있는 레이아웃(헤더), 그 밑에 딸린 레이아웃
//    LinearLayout mLinearLayoutHeader, mLinearLayout;

    // 상세내용 텍스트 있는 레이아웃(헤더), 그 밑에 딸린 레이아웃
//    LinearLayout welfare_detail_content_header, detail_expandable_layout;

    // 주변시설 텍스트 있는 레이아웃
    ConstraintLayout facilities_layout;
    RecyclerView facilities_recyclerview;
    FacilitiesAdapter facilitiesAdapter;

    // 주변시설 텍스트와 하단 바가 있는 레이아웃
    LinearLayout facilities_view;
    View facilities_bottom_view, institution_bottom_view;

    // 연락처 레이아웃
//    LinearLayout welfare_detail_call, welfare_detail_call_expandable;

    TextView median_income, detail_support_target_textview, facilities_textview, content_textview, review_textview, institution_textview;

    View divider_view, detail_divider_view, top_divider_view;

    ImageView first_imageview, second_imageview;

    // 리뷰 작성 버튼
    Button review_write_button;

    // 리뷰 통계 자료를 가져올 때 파싱한 데이터를 담을 변수
    String total_user, star_sum, one_point, two_point, three_point, four_point, five_point, easy, hard, help, help_not;
    // 리뷰 통계 자료 붙일 뷰
    TextView review_average_textview;
    RatingBar review_rate_average;
    // 1점~5점 별 선택한 인원수를 보여줄 차트
    BarChart review_chart;
    // 신청이 쉬웠다, 어려웠다 프로그레스 바
    ProgressBar easy_progressbar, hard_progressbar;
    // 도움이 됐다, 안 됐다 프로그레스 바
    ProgressBar help_progressbar, help_not_progressbar;

    // 클릭하면 다이얼로그 나오는 상세조건 텍스트뷰
    TextView first_target;
    // 혜택 내용을 보여줄 텍스트뷰
    TextView first_benefit;

    String first_welf_target;
    String first_welf_content;
    // 대상을 넣을 리스트
    List<String> target_list = new ArrayList<>();
    // 내용을 넣을 리스트
    List<String> content_list = new ArrayList<>();
    LinearLayout second_content_layout, third_content_layout;
    TextView first_target_textview;

    // 서버에서 받은 이미지 있으면 넣거나 없으면 기본 이미지 넣을 이미지뷰
    ImageView welf_imageview;
    // 서버에서 받은 텍스트 있으면 넣거나 없으면 기본 텍스트 넣을 텍스트뷰
    TextView welf_word_textview;

    // 미구현 레이아웃
    LinearLayout institution_view;

    // 내용, 리뷰, 주변센터 프래그먼트 들어갈 뷰페이저
    ViewPager detail_viewpager;
    TabLayout detail_tab_layout;

    // 로그에 한글 보낼 때 한글을 인코딩해서 저장할 때 쓰는 변수
    String encode_str;

    // 리뷰 작성 화면 이동 시 유저와 작성자가 같은지 확인할 때 쓰는 변수
    String send_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbenefit);

        Logger.addLogAdapter(new AndroidLogAdapter());
        sharedPreferences = getSharedPreferences("app_pref", 0);
        // 쉐어드에서 유저가 거주하는 지역명을 받아온다
        user_area = sharedPreferences.getString("user_area", "");
        // 서울, 인천 뒤에 붙는 특별시/광역시 글자를 제거해 서버에 요청할 때 쓸 수 있도록 가공한다
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

        // 더미데이터 있는 뷰라서 안 보이게 처리함
        facilities_view.setVisibility(View.GONE);

        all_review_scene.setVisibility(View.GONE);
        content_bottom_view.setBackgroundColor(getColor(R.color.colorBlack));
        review_textview.setTextColor(getColor(R.color.colorGray_B));

        /* 뷰페이저 설정 (나중에 구현) */
//        DetailViewpagerAdapter detailViewpagerAdapter = new DetailViewpagerAdapter(getSupportFragmentManager());
//        detail_viewpager.setAdapter(detailViewpagerAdapter);
//        detail_tab_layout.setupWithViewPager(detail_viewpager);

        // 리뷰 보여주는 리사이클러뷰 처리
        review_recycler = findViewById(R.id.review_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        review_recycler.setLayoutManager(new LinearLayoutManager(this));
        review_recycler.setHasFixedSize(true);

        // 주변시설 리사이클러뷰
        facilities_recyclerview = findViewById(R.id.facilities_recyclerview);
        facilities_recyclerview.setHasFixedSize(true);
        facilities_recyclerview.setLayoutManager(new LinearLayoutManager(this));
        facilitiesAdapter = new FacilitiesAdapter(this);
        facilities_recyclerview.setAdapter(facilitiesAdapter);

        /* 중위소득 기준표 텍스트 클릭 시 토스트 출력(성공 시 액티비티 이동) */
//        median_income.setMovementMethod(LinkMovementMethod.getInstance());
//        Spannable spannable = (Spannable) median_income.getText();
//        ClickableSpan span = new ClickableSpan()
//        {
//            @Override
//            public void onClick(@NonNull View widget)
//            {
//                Toast.makeText(DetailBenefitActivity.this, "중위소득 기준표 클릭", Toast.LENGTH_SHORT).show();
//            }
//        };
//        spannable.setSpan(span, 17, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        /* 자세한 지원 대상 정보 확인 텍스트뷰 */
//        detail_support_target_textview.setMovementMethod(LinkMovementMethod.getInstance());
//        Spannable support_target_span = (Spannable) detail_support_target_textview.getText();
//        ClickableSpan support_target_clickable_span = new ClickableSpan()
//        {
//            @Override
//            public void onClick(@NonNull View widget)
//            {
//                Toast.makeText(DetailBenefitActivity.this, "상세 지원 정보 클릭", Toast.LENGTH_SHORT).show();
//            }
//        };
//        support_target_span.setSpan(support_target_clickable_span, 18, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        /* 즐겨찾기 버튼 클릭 리스너, 오픈베타 이후 구현 */
//        favorite_btn.setOnLikeListener(new OnLikeListener()
//        {
//            @Override
//            public void liked(LikeButton likeButton)
//            {
//                // 토스트로 관심사에 등록됐다는 내용을 보낸다
//                String message = welfare_desc_title.getText().toString();
//                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사로 설정했습니다", Toast.LENGTH_SHORT).show();
//
//                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에 저장한다. 지금은 이메일을 하드코딩해 사용하지만 암호화를 해서 저장해야 한다
//                email = getString(R.string.email);
//                try
//                {
//                    encrypted_email = AESUtils.encrypt(email);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//
//                try
//                {
//                    decrypted_email = AESUtils.decrypt(encrypted_email);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//                Logger.e("encrypted_email = " + encrypted_email);
//                Logger.e("decrypted_email = " + decrypted_email);
//                welf_name = welfare_desc_title.getText().toString();
//                isBookmark = "true";
//
//                // 메서드 호출
//                getBookmark(decrypted_email, welf_name);
//            }
//
//            @Override
//            public void unLiked(LikeButton likeButton)
//            {
//                // 회색 하트로 변하면 false를 0으로 치고 서버로 보낸다
//                boolean isClicked = favorite_btn.isLiked();
//                Logger.e("즐찾 버튼 클릭 상태 : " + isClicked);
//
//                // 토스트로 관심사에서 등록 해제됐다는 내용을 보낸다
//                String message = welfare_desc_title.getText().toString();
//                Toast.makeText(DetailBenefitActivity.this, message + " 정책을 내 관심사에서 설정 해제했습니다", Toast.LENGTH_SHORT).show();
//
//                // 서버로 이메일, 혜택 이름(welf_name)를 보내서 즐겨찾기에서 삭제한다
//                email = "ne0001912@gmail.com";
//                welf_name = welfare_desc_title.getText().toString();
//                isBookmark = "false";
//
//                // 메서드 호출
//                getBookmark(email, welf_name);
//            }
//        });

        /* 인텐트에 혜택 이름이 포함돼 있는지 확인 후 있는 경우에만 가져온다 */
        if (getIntent().hasExtra("RBF_title"))
        {
            Intent RBF_intent = getIntent();
            detail_data = RBF_intent.getStringExtra("RBF_title");

            Logger.e("상세 페이지에서 보여줄 타이틀 : " + detail_data);
            welfare_desc_title.setText(detail_data);
        }

        if (getIntent().hasExtra("name"))
        {
            Intent intent = getIntent();
            push_welf_name = intent.getStringExtra("name");
            welfare_desc_title.setText(push_welf_name);
        }
        Log.e(TAG, "push_welf_name = " + push_welf_name);

        if (getIntent().hasExtra("welf_local"))
        {
            Intent intent = getIntent();
            push_welf_local = intent.getStringExtra("welf_local");
            Log.e(TAG, "인텐트로 가져온 지역명 = " + push_welf_local);
        }
        Log.e(TAG, "push_welf_local = " + push_welf_local);

        // "장애인 교통비;; 영상전화 사용료 지원" 혜택이 중간에 특수문자가 껴 있어서 이걸 제거하기 위한 처리
        if (welfare_desc_title.getText().toString().contains(";; "))
        {
            String temp = welfare_desc_title.getText().toString().replace(";; ", ", ");
            welfare_desc_title.setText(temp);
        }

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
                            .setWebUrl("https://www.urbene-fit.com")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder()
                            .setWebUrl("https://www.urbene-fit.com")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .setAndroidExecutionParams("key2=value2")
                            .setIosExecutionParams("key2=value2")
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

        // 상세조건 누를 때마다 각 대상에 맞는 상세정보가 다이얼로그로 나오게 한다
        first_target.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailBenefitActivity.this);
                /* target_tag 넣기 */
                builder.setMessage(first_welf_target)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        /* 혜택의 상세정보를 가져오는 메서드, detail_data에 null이 들어간다
         * ContentFragment로 이동시킨다 */
        getWelfareInformation();

        // 리뷰 작성 버튼
        review_write_button.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                // 비로그인한 유저가 눌렀을 경우 로그인하라는 메시지를 띄워 로그인을 유도한다
                // 로그인 거절 시 다시 리뷰화면으로 돌아간다
                if (sharedPreferences.getBoolean("logout", false))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailBenefitActivity.this);
                    builder.setMessage("리뷰를 작성하시려면 먼저 로그인이 필요해요.\n로그인 하시겠어요?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    Intent intent = new Intent(DetailBenefitActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            }).show();
                }
                else if (writer == null)
                {
                    // 리뷰 목록에 보이는 닉네임과 리뷰 작성을 누르는 유저의 닉네임이 같을 경우 이동하지 못하게 한다
                    // writer 변수에 저장돼있는 닉네임(리뷰 목록에 보이는 닉네임)과 쉐어드에 저장된 닉네임(리뷰 작성을 누르는 유저의 닉네임)을 비교해서 같을 경우
                    // 토스트로 리뷰 작성 불가를 알리고 다르다면(=작성한 적이 없다면) 리뷰 작성 화면으로 이동시킨다
                    // writer가 null이면 작성된 리뷰가 없다는 뜻이므로 리뷰 작성 화면으로 이동시킨다
                    Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
                    intent.putExtra("id", send_id);
                    Log.e(TAG, "1. ReviewActivity로 보낼 혜택 id = " + send_id);
                    startActivityForResult(intent, 1);
                }
                else if (!writer.equals(""))
                {
                    if (writer.equals(sharedPreferences.getString("user_nickname", "")))
                    {
                        // 선택한 리뷰의 작성자 닉네임 = 작성 버튼 누른 사람의 닉네임이 같으면 토스트로 작성 불가 알림
                        Toast.makeText(DetailBenefitActivity.this, "한번 리뷰를 작성하시면 다른 리뷰를 작성하실 수 없습니다", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        // 다른 경우 리뷰 작성화면으로 이동할 수 있도록 한다
                        Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
                        intent.putExtra("id", send_id);
                        Log.e(TAG, "2. ReviewActivity로 보낼 혜택 id = " + send_id);
                        startActivityForResult(intent, 1);
                    }
                }
            }
        });

        getReview();

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

    /* 리뉴얼된 혜택 상세정보들을 가져오는 메서드 (UI 변경으로 jsonParsing() 안의 값들 세팅되는 뷰 바꿔야 함)
    * ContentFragment로 이동시킨다 */
    void getWelfareInformation()
    {
        String token = sharedPreferences.getString("token" ,"");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.getWelfareInformation(token, session, "detail", push_welf_local, push_welf_name, token, LogUtil.getUserLog());
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
        welf_imageview = findViewById(R.id.welf_imageview);
        welf_word_textview = findViewById(R.id.welf_word_textview);
        favorite_btn = findViewById(R.id.favorite_btn);

        // 내용 글자, 글자 밑 가로 막대 있는 레이아웃
        content_view = findViewById(R.id.content_view);
        content_textview = findViewById(R.id.content_textview);
        content_bottom_view = findViewById(R.id.content_bottom_view);

        // 리뷰 글자, 글자 밑 가로 막대 있는 레이아웃
        review_view = findViewById(R.id.review_view);
        review_textview = findViewById(R.id.review_textview);
        review_bottom_view = findViewById(R.id.review_bottom_view);

        // 주변시설 글자, 글자 밑 가로 막대 있는 레이아웃
        facilities_view = findViewById(R.id.facilities_view);
        facilities_textview = findViewById(R.id.facilities_textview);
        facilities_bottom_view = findViewById(R.id.facilities_bottom_view);

        welfare_desc_title = findViewById(R.id.welfare_desc_title);
        detail_benefit_scrollview = findViewById(R.id.detail_benefit_scrollview);

        review_share_imageview = findViewById(R.id.review_share_imageview);

        content_all_layout = findViewById(R.id.content_all_layout);
        top_divider_view = findViewById(R.id.top_divider_view);

        // 주변시설 눌렀을 때 주변시설 보여줄 레이아웃
        facilities_layout = findViewById(R.id.facilities_layout);

        institution_view = findViewById(R.id.institution_view);
        all_review_scene = findViewById(R.id.all_review_scene);
        institution_bottom_view = findViewById(R.id.institution_bottom_view);
        institution_textview = findViewById(R.id.institution_textview);

        total_review_count = findViewById(R.id.total_review_count);
        review_write_button = findViewById(R.id.review_write_button);

        review_average_textview = findViewById(R.id.review_average_textview);
        review_rate_average = findViewById(R.id.review_rate_average);
        review_chart = findViewById(R.id.review_chart);
        easy_progressbar = findViewById(R.id.easy_progressbar);
        hard_progressbar = findViewById(R.id.hard_progressbar);
        help_progressbar = findViewById(R.id.help_progressbar);
        help_not_progressbar = findViewById(R.id.help_not_progressbar);

        first_target = findViewById(R.id.first_target);

        first_benefit = findViewById(R.id.first_benefit);

        first_target_textview = findViewById(R.id.first_target_textview);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // 리뷰를 작성하면 이 메서드가 호출된다. 이 때 서버에서 리뷰 데이터를 가져오는 메서드를 호출해 갱신 효과를 낸다
        getReview();

//        // 리뷰 작성 버튼
//        review_write_button.setOnClickListener(new OnSingleClickListener()
//        {
//            @Override
//            public void onSingleClick(View v)
//            {
//                // 리뷰 목록에 보이는 닉네임과 리뷰 작성을 누르는 유저의 닉네임이 같을 경우 이동하지 못하게 한다
//                // writer 변수에 저장돼있는 닉네임(리뷰 목록에 보이는 닉네임)과 쉐어드에 저장된 닉네임(리뷰 작성을 누르는 유저의 닉네임)을 비교해서 같을 경우
//                // 토스트로 리뷰 작성 불가를 알리고 다르다면(=작성한 적이 없다면) 리뷰 작성 화면으로 이동시킨다
//                // writer가 null이면 작성된 리뷰가 없다는 뜻이므로 리뷰 작성 화면으로 이동시킨다
//                if (writer == null)
//                {
//                    Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
//                    intent.putExtra("id", send_id);
//                    Log.e(TAG, "1. ReviewActivity로 보낼 혜택 id = " + send_id);
//                    startActivityForResult(intent, 1);
//                }
//                else if (!writer.equals(""))
//                {
//                    if (writer.equals(sharedPreferences.getString("user_nickname", "")))
//                    {
//                        // 선택한 리뷰의 작성자 닉네임 = 작성 버튼 누른 사람의 닉네임이 같으면 토스트로 작성 불가 알림
//                        Toast.makeText(DetailBenefitActivity.this, "한번 리뷰를 작성하시면 다른 리뷰를 작성하실 수 없습니다", Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        // 다른 경우 리뷰 작성화면으로 이동할 수 있도록 한다
//                        Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
//                        intent.putExtra("id", send_id);
//                        Log.e(TAG, "2. ReviewActivity로 보낼 혜택 id = " + send_id);
//                        startActivityForResult(intent, 1);
//                    }
//                }
//            }
//        });

        // 내용 클릭
        content_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.VISIBLE);
                content_bottom_view.setBackgroundColor(getColor(R.color.colorBlack));
                facilities_bottom_view.setVisibility(View.GONE);
                facilities_layout.setVisibility(View.GONE);
                all_review_scene.setVisibility(View.GONE);
                review_textview.setTextColor(getColor(R.color.colorGray_B));
                review_bottom_view.setVisibility(View.GONE);
                top_divider_view.setVisibility(View.VISIBLE);
                content_all_layout.setVisibility(View.VISIBLE);
                content_textview.setTextColor(getColor(R.color.colorPrimaryDark));
                facilities_textview.setTextColor(getColor(R.color.colorGray_B));
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
                review_textview.setTextColor(getColor(R.color.colorPrimaryDark));
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

        // 주변시설 클릭
        facilities_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.GONE);
                facilities_bottom_view.setVisibility(View.VISIBLE);
                facilities_bottom_view.setBackgroundColor(getColor(R.color.colorBlack));
                review_textview.setTextColor(getColor(R.color.colorGray_B));
                top_divider_view.setVisibility(View.VISIBLE);
                facilities_layout.setVisibility(View.VISIBLE);
                content_all_layout.setVisibility(View.GONE);
                review_bottom_view.setVisibility(View.GONE);
                content_textview.setTextColor(getColor(R.color.colorGray_B));
                facilities_textview.setTextColor(getColor(R.color.colorPrimaryDark));
                all_review_scene.setVisibility(View.GONE);
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
                welfare_target = inner_obj.getString("target");
                welfare_target_tag = inner_obj.getString("target_tag");
                welf_image = inner_obj.getString("welf_image");
                welf_wording = inner_obj.getString("welf_wording");
            }

            Log.e(TAG, "welf_id = " + welf_id);
            send_id = welf_id;

            // 이미지, 문구 처리
            if (!welf_image.equals("") && !welf_wording.equals(""))
            {
                if (welf_image.equals("기본 이미지") && welf_wording.equals("기본 문구"))
                {
                    welf_imageview.setImageResource(R.drawable.detail_main_img);
                    welf_word_textview.setText("쉽고\n빠르게\n혜택을\n받아보세요");
                }
                else
                {
                    /* 나중에 서버에 이미지, 문구 추가되면 그걸 가져와서 각 뷰에 넣는다 */
                }
            }

            /* 좋아요 버튼은 오픈베타 이후 구현 */
//            if (welf_bookmark.equals(getString(R.string.is_true)))
//            {
//                favorite_btn.setLiked(true);
//            }
//            else
//            {
//                favorite_btn.setLiked(false);
//            }

            if (!welfare_target.equals(""))
            {
                // '/'를 ','로 바꿔서 set한다
                String first_target = welfare_target.replace("/", ", ");
                first_target_textview.setText(first_target);
            }

            /* target_tag는 상세조건을 누르면 나오는 다이얼로그에서 보여준다 */
            if (!welfare_target_tag.equals(""))
            {
//                first_welf_target = welfare_target_tag;
                String[] before_str = welfare_target_tag.split("#");
                List<String> tag_list = new ArrayList<>();
                for (String str : before_str)
                {
                    tag_list.add(str);
                }
                Log.e("ff", "태그의 # 제거 후 결과 : " + tag_list);
                tag_list.remove(0);
                Log.e("ff", "0번 지운 후 결과 : " + tag_list);
                for (int i = 0; i < tag_list.size(); i++)
                {
                    String letter = tag_list.get(i);
                    String replace_letter = letter.replaceAll("/", "");
                    tag_list.set(i, replace_letter);
                }
                Log.e("ff", "'/' 문자열 지운 결과 : " + tag_list);
                for (int i = 0; i < tag_list.size(); i++)
                {
                    String letter = tag_list.get(i);
                    String replace_letter = letter.replaceAll(";;", ",");
                    tag_list.set(i, replace_letter);
                }
                Log.e(TAG, ";;를 ,로 바꾼 결과 : " + tag_list);
                /* StringBuilder로 ArrayList 안의 문자열들 사이에 ,를 섞어서 이어붙인다 */
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tag_list.size(); i++)
                {
                    sb.append("#" + tag_list.get(i) + ", ");
                }
                String sb_result = sb.toString();
                Log.e("ff", "콤마 붙임 : " + sb_result);
                // 마지막의 ',' 문자를 지운다
                String erase_str = "";
                if (!sb_result.equals(""))
                {
                    erase_str = sb_result.substring(0, sb_result.lastIndexOf(","));
                    Log.e("ff", "마지막의 콤마 삭제 : " + erase_str);
                }
                // 콤마를 개행문자로 바꿔서 다이얼로그를 누르면 최종적으로 보일 문자열을 만든다
                erase_str = erase_str.replaceAll(", ", "\n");
                Log.e(TAG, "콤마->개행문자 변경 결과 : " + erase_str);
                if (!erase_str.equals("") && erase_str.lastIndexOf("\n") != -1)
                {
                    first_welf_target = erase_str.substring(0, erase_str.lastIndexOf("\n"));
                }
                Log.e(TAG, "최종적으로 다이얼로그에 보여야 하는 문구 : " + first_welf_target);
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

    // 혜택의 대상, 내용, 연락처에 붙어있는 특수문자를 콤마(,)로 바꿔 뷰에 set하는 메서드
    private void symbolChange(String welf_target, String welf_content, String welf_contact)
    {
        if (welf_target != null)
        {
            String target_line = welf_target.replace("^;", "\n");
            String target_comma = target_line.replace(";;", ",");
            /* 받은 데이터에서 ,를 구분자로 스플릿해서 String[]에 담는다 */
            String[] str = target_comma.split(", ");
            for (int i = 0; i < str.length; i++)
            {
                target_list.add(str[i]);
            }
        }
        if (welf_content != null)
        {
            String contents_line = welf_content.replace("^;", "\n");
            String contents_comma = contents_line.replace(";;", ",");
            /* 지원내용에 ;; 특수문자를 변환한 내용을 넣는다 */
            first_benefit.setText(contents_comma);
            String[] str = contents_comma.split(", ");
            for (int i = 0; i < str.length; i++)
            {
                content_list.add(str[i]);
            }
            for (int i = 0; i < content_list.size(); i++)
            {
                first_welf_content = content_list.get(i) + "\n";
            }
        }
        if (welf_contact != null)
        {
            String contact_line = welf_contact.replace("^;", "\n");
            String contact_comma = contact_line.replace(";;", ",");
            /* 문의처에 ;; 특수문자를 변환한 내용을 넣는다 */
//            welf_contact_detail.setText(contact_comma);
        }
    }

    // list 문자열, review_id(혜택 id 정보)를 서버로 넘겨 리뷰들을 가져오는 메서드
    void getReview()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String session = sharedPreferences.getString("sessionId", "");
        String token = sharedPreferences.getString("token", "");
        encode("리뷰 데이터 호출");
        /* review_id에는 리뷰의 idx를 가져와서 넣어야 한다 */
        Call<String> call = apiInterface.getReview(token, session, "list", welf_id);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String detail = response.body();
                    Log.e(TAG, "리뷰 조회 결과 : " + detail);
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

    /* 서버에서 받은 세션 id를 인코딩하는 메서드 */
    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    // getReview()에서 JSON 값을 파싱하기 위해 따로 만든 메서드
    private void jsonParse(String detail)
    {
        List<ReviewItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject_total = new JSONObject(detail);
            review_count = jsonObject_total.getString("TotalCount");
            JSONArray jsonArray = jsonObject_total.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                id = jsonObject.getString("id");
                content = jsonObject.getString("content");
                writer = jsonObject.getString("writer");
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
            /* 리뷰 통계 부분에 쓸 데이터 파싱 */
            JSONArray stats = jsonObject_total.getJSONArray("Review_stats");
            for (int i = 0; i < stats.length(); i++)
            {
                JSONObject inner = stats.getJSONObject(i);
                total_user = inner.getString("total_user");
                star_sum = inner.getString("star_sum");
                one_point = inner.getString("one_point");
                two_point = inner.getString("two_point");
                three_point = inner.getString("three_point");
                four_point = inner.getString("four_point");
                five_point = inner.getString("five_point");
                easy = inner.getString("esay");
                hard = inner.getString("hard");
                help = inner.getString("help");
                help_not = inner.getString("helf_not");

                // 통계 가져오기 위한 모델 클래스 객체 설정
                ReviewStatsItem item = new ReviewStatsItem();
                item.setTotal_user(total_user);
                item.setStar_sum(star_sum);
                item.setOne_point(one_point);
                item.setTwo_point(two_point);
                item.setThree_point(three_point);
                item.setFour_point(four_point);
                item.setFive_point(five_point);
                item.setEasy(easy);
                item.setHard(hard);
                item.setHelp(help);
                item.setHelp_not(help_not);
            }
        }   // try end
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        /* 뷰에 데이터 set */
        // 리뷰 평점 구하기
        if (one_point != null && two_point != null && three_point != null && four_point != null && five_point != null)
        {
            int one_person = Integer.parseInt(one_point);
            int two_person = Integer.parseInt(two_point);
            int three_person = Integer.parseInt(three_point);
            int four_person = Integer.parseInt(four_point);
            int five_person = Integer.parseInt(five_point);
            int one = 1;
            int two = 2;
            int three = 3;
            int four = 4;
            int five = 5;
            int writer_count = Integer.parseInt(review_count);
            // 각 인원수에 1~5점씩을 곱한 다음 리뷰 작성 인원수로 나눈다
            int add_average = (one_person * one) + (two_person * two) + (three_person * three) + (four_person * four) + (five_person * five);
            if (add_average != 0 && !review_count.equals(""))
            {
                int review_average = add_average / writer_count;
                // 평균이 구해지면 그 숫자를 텍스트뷰와 별점에 넣는다
                review_average_textview.setText("" + review_average);
                review_rate_average.setStar(review_average);
            }
        }
        // 리뷰 개수 붙이기
        if (!review_count.equals(""))
        {
            total_review_count.setText("리뷰 " + review_count + "개");
        }
        // 가로 막대그래프에 데이터 set
        /* BarChart로 가로 막대그래프 만들기 - 아래 링크에서 가져옴
         * https://github.com/blackfizz/EazeGraph */
        if (five_point != null || four_point != null || three_point != null || two_point != null || one_point != null)
        {
            // onCreate()에서 clearChart()로 데이터를 지울 경우 1점~5점이 놓인 순서가 꼬이거나 그래프가 개떡같이 출력되는 현상이 발생한다
            // 그래서 onCreate()가 아닌 JSON 값을 파싱하고 set하기 전 clearChart()로 데이터를 지운 후, 파싱한 데이터를 붙여서 위의 현상을 수정했다
            review_chart.clearChart();
            review_chart.addBar(new BarModel("5점", Float.parseFloat(five_point), 0xFFFF5549));
            review_chart.addBar(new BarModel("4점", Float.parseFloat(four_point), 0xFFFF5549));
            review_chart.addBar(new BarModel("3점", Float.parseFloat(three_point), 0xFFFF5549));
            review_chart.addBar(new BarModel("2점", Float.parseFloat(two_point), 0xFFFF5549));
            review_chart.addBar(new BarModel("1점", Float.parseFloat(one_point), 0xFFFF5549));
            review_chart.startAnimation();
        }
        else
        {
            review_chart.clearChart();
            review_chart.addBar(new BarModel("5점", 0f, 0xFFFF5549));
            review_chart.addBar(new BarModel("4점", 0f, 0xFFFF5549));
            review_chart.addBar(new BarModel("3점", 0f, 0xFFFF5549));
            review_chart.addBar(new BarModel("2점", 0f, 0xFFFF5549));
            review_chart.addBar(new BarModel("1점", 0f, 0xFFFF5549));
            review_chart.startAnimation();
        }

        // 쉬워요, 어려워요 프로그레스 바
        if (easy != null || hard != null)
        {
            easy_progressbar.setProgress(Integer.parseInt(easy));
            hard_progressbar.setProgress(Integer.parseInt(hard));
        }
        else
        {
            easy_progressbar.setProgress(0);
            hard_progressbar.setProgress(0);
        }
        // 도움됐어요, 안됐어요 프로그레스 바
        if (help != null || help_not != null)
        {
            help_progressbar.setProgress(Integer.parseInt(help));
            help_not_progressbar.setProgress(Integer.parseInt(help_not));
        }
        else
        {
            help_progressbar.setProgress(0);
            help_not_progressbar.setProgress(0);
        }

        review_adapter = new ReviewAdapter(DetailBenefitActivity.this, list, review_clickListener, deleteClickListener);
        review_adapter.setOnDeleteClickListener(new ReviewAdapter.DeleteClickListener()
        {
            @Override
            public void onDeleteClick(View view, int position)
            {
                String user_nickname = sharedPreferences.getString("user_nickname", "");
                String writer_nickname = list.get(position).getWriter();
                String posting_id = list.get(position).getId();
                int id = Integer.parseInt(posting_id);
                if (user_nickname.equals(writer_nickname))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailBenefitActivity.this);
                    builder.setTitle("리뷰 삭제")
                            .setMessage("리뷰를 삭제하시겠습니까?\n삭제된 리뷰는 복구할 수 없습니다")
                            .setPositiveButton("예", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    deleteReview(id);
                                    Toast.makeText(DetailBenefitActivity.this, "리뷰 삭제 완료", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Toast.makeText(DetailBenefitActivity.this, "리뷰 삭제 취소", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });
        review_adapter.setOnItemClickListener(new ReviewAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                // 쉐어드에서 가져온 user_nickname 값과 writer 값을 가져와서 비교한 다음 일치하면 수정, 삭제를 할 수 있게 한다
                String user_nickname = sharedPreferences.getString("user_nickname", "");
                String writer_nickname = list.get(position).getWriter();
                String review_content = list.get(position).getContent();
                float star = list.get(position).getStar_count();
                String star_count = String.valueOf(star);
                String posting_id = list.get(position).getId();
                Log.e(TAG, "작성자 = " + writer_nickname);
                Log.e(TAG, "리뷰 내용 = " + review_content);
                Log.e(TAG, "글 id = " + posting_id);
                if (user_nickname.equals(writer_nickname))
                {
                    // 인텐트로 값들을 갖고 이동해 삭제할 수 있게 한다
//                    String image_url = list.get(position).getImage_url();
                    Intent intent = new Intent(DetailBenefitActivity.this, ReviewUpdateActivity.class);
//                    intent.putExtra("image_url", image_url);
                    intent.putExtra("content", review_content);
                    intent.putExtra("id", posting_id);
                    intent.putExtra("star_count", star_count);
                    startActivity(intent);
                }
            }
        });
        review_recycler.setAdapter(review_adapter);
    }

    // 리뷰 삭제 메서드
    void deleteReview(int id)
    {
        String token = sharedPreferences.getString("token", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.deleteReview(token, id, "delete");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "삭제 결과 = " + result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

}
