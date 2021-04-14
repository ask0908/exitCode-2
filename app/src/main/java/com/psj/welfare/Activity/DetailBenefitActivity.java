package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.analytics.FirebaseAnalytics;
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
import com.psj.welfare.R;
import com.psj.welfare.adapter.ReviewAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;
import com.psj.welfare.custom.RecyclerViewEmptySupport;
import com.psj.welfare.data.ReviewItem;
import com.psj.welfare.data.ReviewStatsItem;
import com.psj.welfare.util.LogUtil;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DetailBenefitActivity extends AppCompatActivity
{
    public final String TAG = this.getClass().getSimpleName();

    LinearLayout review_cardview, review_data_layout, target_of_benefit, description_of_benefit;
    ConstraintLayout content_all_layout, all_review_scene;

    Button detail_contents_button, detail_review_button;

    private TextView name_of_benefit;

    SharedPreferences sharedPreferences;

    boolean isClicked = false;

    ImageView review_share_imageview;

    String benefit_image_url = "http://3.34.64.143/images/reviews/조제분유.jpg";

    private String push_welf_name;

    TextView first_target;
    TextView detail_description;

    String welf_name;

    String welf_id, welf_target, welf_contents, welf_apply, welf_contact, welf_period, welf_end, welf_local, welf_bookmark, push_welf_local, welfare_target,
            welfare_target_tag, welf_image, welf_wording = "";

    String id, content, writer, create_date, like_count, star_count, image_url, review_count, difficulty_level, satisfaction = "";
    TextView total_review_count;

    private RecyclerViewEmptySupport review_recycler;
    TextView list_empty;

    ReviewAdapter review_adapter;
    ReviewAdapter.ItemClickListener review_clickListener;
    ReviewAdapter.DeleteClickListener deleteClickListener;

    Button review_write_button;

    String total_user, star_sum, one_point, two_point, three_point, four_point, five_point, easy, hard, help, help_not;
    TextView percent_easy, percent_difficult, percent_helpful, percent_nothelpful;
    TextView review_average_textview;
    RatingBar review_rate_average;
    BarChart review_chart;
    ProgressBar easy_progressbar;
    ProgressBar help_progressbar;

    String detail_target;

    private FirebaseAnalytics analytics;

    String encode_str;

    boolean isConnected = false;

    String first_welf_target;
    String erase_str;
    TextView first_target_textview;

    LinearLayout target_layout, condition_layout;

    List<ReviewItem> list;

    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(DetailBenefitActivity.this);
        setContentView(R.layout.activity_detailbenefit);

        sharedPreferences = getSharedPreferences("app_pref", 0);

        analytics = FirebaseAnalytics.getInstance(this);
        detailPageLog("혜택 상세보기 화면 진입");

        init();
        list = new ArrayList<>();

        review_recycler = findViewById(R.id.review_recycler);
        review_recycler.setLayoutManager(new LinearLayoutManager(this));
        list_empty = findViewById(R.id.list_empty);
        review_recycler.setEmptyView(list_empty);
        review_recycler.setHasFixedSize(true);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (getIntent().hasExtra("name"))
        {
            Intent intent = getIntent();
            push_welf_name = intent.getStringExtra("name");
            if (push_welf_name.contains(";; "))
            {
                push_welf_name = push_welf_name.replace(";; ", ", ");
                name_of_benefit.setText(push_welf_name);
            }
            else
            {
                name_of_benefit.setText(push_welf_name);
            }
            editor.putString("detail_benefit_name", push_welf_name);
            editor.apply();
        }

        if (getIntent().hasExtra("welf_local"))
        {
            Intent intent = getIntent();
            push_welf_local = intent.getStringExtra("welf_local");
            editor.putString("detail_benefit_area", push_welf_local);
            editor.apply();
        }

        getWelfareInformation();

        review_share_imageview.setOnClickListener(v ->
        {
            /* 카카오 링크 적용 */
            FeedTemplate params = FeedTemplate
                    // 제목, 이미지 url, 이미지 클릭 시 이동하는 위치?를 입력한다
                    // 이미지 url을 사용자 정의 파라미터로 전달하면 최대 2MB 이미지를 메시지에 담아 보낼 수 있다
                    .newBuilder(ContentObject.newBuilder("당신이 놓치고 있는 혜택", benefit_image_url,
                            LinkObject.newBuilder().setMobileWebUrl("'https://developers.kakao.com").build())
                            .setDescrption(name_of_benefit.getText().toString())  // 제목 밑의 본문
                            .build())
                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                            .setWebUrl("https://www.hyemo.com/")
                            .setMobileWebUrl("https://developers.kakao.com")
                            .setAndroidExecutionParams("key1=value1")
                            .setIosExecutionParams("key1=value1")
                            .build()))
//                    .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("웹에서 보기", LinkObject.newBuilder()
//                            .setWebUrl("https://www.hyemo.com/")
//                            .setMobileWebUrl("https://developers.kakao.com")
//                            .setAndroidExecutionParams("key2=value2")
//                            .setIosExecutionParams("key2=value2")
//                            .build()))
//                    .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20).setSharedCount(30).build())   // 좋아요 수, 댓글(리뷰) 수, 공유된 회수 더미
                    .build();

            Map<String, String> serverCallbackArgs = new HashMap<>();
            serverCallbackArgs.put("user_id", "${current_user_id}");
            serverCallbackArgs.put("product_id", "${shared_product_id}");

            KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>()
            {
                @Override
                public void onFailure(ErrorResult errorResult)
                {
                    Log.e(TAG, errorResult.toString());
                }

                @Override
                public void onSuccess(KakaoLinkResponse result)
                {
                    // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용해야 한다.
                    Log.e(TAG, result.toString());
                }
            });
        });

        review_write_button.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {

                if (sharedPreferences.getBoolean("logout", false) || sharedPreferences.getString("user_nickname", "").equals(""))
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
                else
                {
                    boolean alreadyWriten = false;
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (list.get(i).getWriter().equals(sharedPreferences.getString("user_nickname", "")))
                        {
                            alreadyWriten = true;
                            break;
                        }
                    }
                    if (alreadyWriten)
                    {
                        Toast.makeText(DetailBenefitActivity.this, "한번 리뷰를 작성하시면 다른 리뷰를 작성하실 수 없습니다", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
                        intent.putExtra("id", welf_id);
                        startActivityForResult(intent, 1);
                    }
                }
            }
        });

        first_target.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailBenefitActivity.this);
                builder.setMessage(erase_str)
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

    }

    private void init()
    {
        content_all_layout = findViewById(R.id.content_all_layout);
        review_cardview = findViewById(R.id.review_cardview);
        review_data_layout = findViewById(R.id.review_data_layout);
        all_review_scene = findViewById(R.id.all_review_scene);

        target_of_benefit = findViewById(R.id.target_of_benefit);
        description_of_benefit = findViewById(R.id.description_of_benefit);

        name_of_benefit = findViewById(R.id.name_of_benefit);

        detail_contents_button = findViewById(R.id.detail_contents_button);
        detail_review_button = findViewById(R.id.detail_review_button);

        review_write_button = findViewById(R.id.review_write_button);

        review_share_imageview = findViewById(R.id.review_share_imageview);

        total_review_count = findViewById(R.id.total_review_count);
        review_write_button = findViewById(R.id.review_write_button);

        review_average_textview = findViewById(R.id.review_average_textview);
        review_rate_average = findViewById(R.id.review_rate_average);
        review_chart = findViewById(R.id.review_chart);
        easy_progressbar = findViewById(R.id.easy_progressbar);
        help_progressbar = findViewById(R.id.help_progressbar);
        percent_difficult = findViewById(R.id.percent_difficult);
        percent_easy = findViewById(R.id.percent_easy);
        percent_helpful = findViewById(R.id.percent_helpful);
        percent_nothelpful = findViewById(R.id.percent_nothelpful);

        first_target = findViewById(R.id.first_target);

        detail_description = findViewById(R.id.detail_description);

        first_target_textview = findViewById(R.id.first_target_textview);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            getReview(welf_id);
        }
        else if (requestCode == 2 && resultCode == RESULT_OK)
        {
            getReview(welf_id);
        }
    }

    void getWelfareInformation()
    {
        Log.e(TAG, "getWelfareInformation() 호출");

        String token = sharedPreferences.getString("token", "");
        Log.e(TAG, "token : " + token);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Log.e(TAG, "session : " + session);
        Log.e(TAG, "push_welf_local : " + push_welf_local);
        Log.e(TAG, "push_welf_name : " + push_welf_name);
        Call<String> call = apiInterface.getWelfareInformation(token, session, "detail", push_welf_local, push_welf_name, token, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "result : " + result);
                    jsonParsing(result);
                }
                else
                {
                    Toast.makeText(DetailBenefitActivity.this, "에러가 발생했습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "errorBody() : " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Toast.makeText(DetailBenefitActivity.this, "에러가 발생했습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getReview(welf_id);

        detail_review_button.setOnClickListener(v ->
        {
            detail_review_button.setBackground(ContextCompat.getDrawable(DetailBenefitActivity.this, R.drawable.radius_pink_border));
            detail_review_button.setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.colorPink));
            detail_contents_button.setBackground(ContextCompat.getDrawable(DetailBenefitActivity.this, R.drawable.radius_gray_border));
            detail_contents_button.setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.colorGray_B));
            all_review_scene.setVisibility(View.VISIBLE);

            target_of_benefit.setVisibility(View.GONE);
            description_of_benefit.setVisibility(View.GONE);
            getReview(welf_id);
        });

        detail_contents_button.setOnClickListener(v ->
        {
            detail_review_button.setBackground(ContextCompat.getDrawable(DetailBenefitActivity.this, R.drawable.radius_gray_border));
            detail_review_button.setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.colorGray_B));
            detail_contents_button.setBackground(ContextCompat.getDrawable(DetailBenefitActivity.this, R.drawable.radius_pink_border));
            detail_contents_button.setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.colorPink));
            all_review_scene.setVisibility(View.GONE);
            target_of_benefit.setVisibility(View.VISIBLE);
            description_of_benefit.setVisibility(View.VISIBLE);
        });
    }

    private void jsonParsing(String result)
    {
        Log.e(TAG, "jsonParsing() 안의 result : " + result);
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            JSONObject inner_obj = jsonArray.getJSONObject(0);
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

            getReview(welf_id);

            if (!welfare_target.equals(""))
            {
                detail_target = welfare_target.replace("/", ", ");
                first_target_textview.setText(detail_target);
            }

            if (!welfare_target_tag.equals(""))
            {
                if (welfare_target_tag.contains("#"))
                {
                    String[] before_str = welfare_target_tag.split("#");
                    List<String> tag_list = new ArrayList<>();
                    for (String str : before_str)
                    {
                        tag_list.add(str);
                    }
                    tag_list.remove(0);
                    for (int i = 0; i < tag_list.size(); i++)
                    {
                        String letter = tag_list.get(i);
                        String replace_letter = letter.replaceAll("/", "");
                        tag_list.set(i, replace_letter);
                    }
                    for (int i = 0; i < tag_list.size(); i++)
                    {
                        String letter = tag_list.get(i);
                        String replace_letter = letter.replaceAll(";;", ",");
                        tag_list.set(i, replace_letter);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < tag_list.size(); i++)
                    {
                        sb.append("#" + tag_list.get(i) + ", ");
                    }
                    String sb_result = sb.toString();
                    if (!sb_result.equals(""))
                    {
                        erase_str = sb_result.substring(0, sb_result.lastIndexOf(","));
                    }
                    erase_str = erase_str.replaceAll(", ", "\n");
                    if (!erase_str.equals("") && erase_str.lastIndexOf("\n") != -1)
                    {
                        first_welf_target = erase_str.substring(0, erase_str.lastIndexOf("\n"));
                    }
                }
            }

            symbolChange(welf_target, welf_contents, welf_contact);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        // 대상과 상세조건을 매칭시키기 위해 대상, 태그 split
        if (welfare_target != null && welfare_target_tag != null)
        {
            String[] targets_array = welfare_target.split("/");
            String[] conditions_array = welfare_target_tag.split("/");

            for (int i = 0; i < conditions_array.length; i++)
            {
                conditions_array[i] = conditions_array[i].replace("#", "\n#");
            }

            List<String> target_list = new ArrayList<>();
            List<String> condition_list = new ArrayList<>();

            Collections.addAll(target_list, targets_array);
            Collections.addAll(condition_list, conditions_array);

            for (int i = 0; i < condition_list.size(); i++)
            {
                String letter = condition_list.get(i);
                String replace_letter = letter.replaceAll(";;", ",");
                condition_list.set(i, replace_letter);
            }

            target_layout = findViewById(R.id.target_layout);
            condition_layout = findViewById(R.id.condition_layout);

            target_layout.setOrientation(LinearLayout.VERTICAL);
            condition_layout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams left_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 9f);
            left_param.setMargins(0, 20, 0, 30);
            LinearLayout.LayoutParams right_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            right_param.setMargins(0, 0, 0, 10);

            final TextView[] myTextView = new TextView[target_list.size()];
            final TextView[] rightTextView = new TextView[condition_list.size()];

            for (int i = 0; i < target_list.size(); i++)
            {
                myTextView[i] = new TextView(this);
                myTextView[i].setLayoutParams(left_param);
                final Typeface face = ResourcesCompat.getFont(this, R.font.nanum_barun_gothic_bold);
                myTextView[i].setTypeface(face);
                myTextView[i].setText(target_list.get(i));
                myTextView[i].setTextSize(15);
                myTextView[i].setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.gray));
                myTextView[i].setPadding(0, 0, 20, 10);
                target_layout.addView(myTextView[i]);
            }

            for (int i = 0; i < condition_list.size(); i++)
            {
                rightTextView[i] = new TextView(this);
                rightTextView[i].setLayoutParams(right_param);
                final Typeface face = ResourcesCompat.getFont(this, R.font.jalnan);
                rightTextView[i].setTypeface(face);
                rightTextView[i].setText("상세조건");
                rightTextView[i].setTextSize(12);
                rightTextView[i].setTextColor(ContextCompat.getColor(DetailBenefitActivity.this, R.color.colorMainWhite));
                rightTextView[i].setBackground(ContextCompat.getDrawable(DetailBenefitActivity.this, R.drawable.radius_grey_filled));
                int tag_num = i;
                rightTextView[i].setOnClickListener(v ->
                {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "상세보기 화면에서 대상의 상세조건 확인");
                    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(condition_list.get(tag_num) + "\n").show();
                });
                condition_layout.addView(rightTextView[i]);
            }
        }
    }

    private void symbolChange(String welf_target, String welf_content, String welf_contact)
    {
        if (welf_target != null)
        {
            String target_line = welf_target.replace("^;", "\n");
            String target_comma = target_line.replace(";;", ",");
            String[] str = target_comma.split(", ");
        }
        if (welf_content != null)
        {
            String contents_line = welf_content.replace("^;", "\n");
            String contents_comma = contents_line.replace(";;", ",");
            detail_description.setText(contents_comma);
        }
        if (welf_contact != null)
        {
            String contact_line = welf_contact.replace("^;", "\n");
            String contact_comma = contact_line.replace(";;", ",");
        }
    }

    void getReview(String welf_id)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String session = sharedPreferences.getString("sessionId", "");
        String token = sharedPreferences.getString("token", "");
        encode("리뷰 데이터 호출");
        Call<String> call = apiInterface.getReview(token, session, "list", welf_id);
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

    private void jsonParse(String detail)
    {
        try
        {
            list.clear();
            JSONObject jsonObject_total = new JSONObject(detail);
            review_count = jsonObject_total.getString("TotalCount");
            if (review_count.equals("0"))
            {
                review_data_layout.setVisibility(View.GONE);
                total_review_count.setText("");
            }
            else
            {
                review_data_layout.setVisibility(View.VISIBLE);
            }
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
                difficulty_level = jsonObject.getString("difficulty_level");
                satisfaction = jsonObject.getString("satisfaction");

                ReviewItem value = new ReviewItem();
                value.setId(id);
                value.setContent(content);
                value.setCreate_date(create_date);
                value.setImage_url(image_url);
                value.setWriter(writer);
                value.setStar_count(Float.parseFloat(star_count));
                value.setDifficulty(difficulty_level);
                value.setSatisfaction(satisfaction);
                list.add(value);
            }

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
                int add_average = (one_person * one) + (two_person * two) + (three_person * three) + (four_person * four) + (five_person * five);
                if (add_average != 0 && !review_count.equals("") && writer_count != 0)
                {
                    float review_average = (float) add_average / (float) writer_count;
                    String str_review_average = String.format("%.1f", review_average);
                    review_average_textview.setText(str_review_average);
                    review_rate_average.setStar(review_average);
                }
            }
            if (!review_count.equals(""))
            {
                total_review_count.setText("리뷰 " + review_count + "개");
            }
            if (five_point != null || four_point != null || three_point != null || two_point != null || one_point != null)
            {
                review_chart.clearChart();
                review_chart.addBar(new BarModel("5점", Float.parseFloat(five_point), 0xFFFF7088));
                review_chart.addBar(new BarModel("4점", Float.parseFloat(four_point), 0xFFFF7088));
                review_chart.addBar(new BarModel("3점", Float.parseFloat(three_point), 0xFFFF7088));
                review_chart.addBar(new BarModel("2점", Float.parseFloat(two_point), 0xFFFF7088));
                review_chart.addBar(new BarModel("1점", Float.parseFloat(one_point), 0xFFFF7088));
                review_chart.startAnimation();
            }
            else
            {
                review_chart.clearChart();
                review_chart.addBar(new BarModel("5점", 0f, 0xFFFF7088));
                review_chart.addBar(new BarModel("4점", 0f, 0xFFFF7088));
                review_chart.addBar(new BarModel("3점", 0f, 0xFFFF7088));
                review_chart.addBar(new BarModel("2점", 0f, 0xFFFF7088));
                review_chart.addBar(new BarModel("1점", 0f, 0xFFFF7088));
                review_chart.startAnimation();
            }

            if (easy != null || hard != null)
            {
                float other_easy = Float.parseFloat(easy);
                float other_hard = Float.parseFloat(hard);
                float other_review_count = Float.parseFloat(review_count);
                float result_easy = ((other_easy / other_review_count) * 100);
                float result_hard = ((other_hard / other_review_count) * 100);
                easy_progressbar.setProgress((int) result_easy);
                easy_progressbar.getIndeterminateDrawable().setColorFilter(
                        getResources().getColor(R.color.colorPink),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                percent_easy.setText("(" + (int) result_easy + "%)");
                int difficult = 100 - (int) result_easy;
                percent_difficult.setText("(" + difficult + "%)");
            }
            else
            {
                easy_progressbar.setProgress(0);
            }
            if (help != null || help_not != null)
            {
                float other_help = Float.parseFloat(help);
                float other_help_not = Float.parseFloat(help_not);
                float other_review_count = Float.parseFloat(review_count);
                float result_help = ((other_help / other_review_count) * 100);
                float result_help_not = ((other_help_not / other_review_count) * 100);
                help_progressbar.setProgress((int) result_help);
                percent_helpful.setText("(" + (int) result_help + "%)");
                int nothelpful = 100 - (int) result_help;
                percent_nothelpful.setText("(" + nothelpful + "%)");
            }
            else
            {
                help_progressbar.setProgress(0);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
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
                                    Toast.makeText(DetailBenefitActivity.this, "리뷰가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
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
                String user_nickname = sharedPreferences.getString("user_nickname", "");
                String writer_nickname = list.get(position).getWriter();
                String review_content = list.get(position).getContent();
                String review_difficulty = list.get(position).getDifficulty();
                String review_satisfaction = list.get(position).getSatisfaction();
                float star = list.get(position).getStar_count();
                String star_count = String.valueOf(star);
                String posting_id = list.get(position).getId();
                if (user_nickname.equals(writer_nickname))
                {
                    Intent intent = new Intent(DetailBenefitActivity.this, ReviewActivity.class);
                    intent.putExtra("content", review_content);
                    intent.putExtra("id", posting_id);
                    intent.putExtra("star_count", star_count);
                    intent.putExtra("difficulty_level", review_difficulty);
                    intent.putExtra("satisfaction", review_satisfaction);
                    intent.putExtra("welf_local", push_welf_local);
                    intent.putExtra("welf_name", push_welf_name);
                    startActivityForResult(intent, 2);
                }
            }
        });
        review_recycler.setAdapter(review_adapter);
    }

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
                    getReview(welf_id);
                }
                else
                {
                    //
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    void detailPageLog(String detail_action)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        sharedPreferences = getSharedPreferences("app_pref", 0);
        String token;
        if (sharedPreferences.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = sharedPreferences.getString("token", "");
        }
        String session = sharedPreferences.getString("sessionId", "");
        String action = userAction(detail_action);
        Call<String> call = apiInterface.userLog(token, session, "detail", action, null, LogUtil.getUserLog());
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    //
                }
                else
                {
                    //
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    private String userAction(String user_action)
    {
        try
        {
            user_action = URLEncoder.encode(user_action, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return user_action;
    }

    public boolean isNetworkConnected(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        boolean bwimax = false;
        if (wimax != null)
        {
            bwimax = wimax.isConnected();
        }
        if (mobile != null)
        {
            if (mobile.isConnected() || wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        else
        {
            if (wifi.isConnected() || bwimax)
            {
                return true;
            }
        }
        return false;
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}