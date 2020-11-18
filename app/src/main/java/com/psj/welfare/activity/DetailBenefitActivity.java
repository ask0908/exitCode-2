package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.DetailBenefitRecyclerAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 상세 액티비티는 사용자가 자세한 혜택의 내용을 확인할 수 있어야 한다
 * 자세한 내용일지라도 사용자의 피로도를 줄여줄 수 있는 방법이 적용돼야 한다
 * */
public class DetailBenefitActivity extends AppCompatActivity
{
    public static final String TAG = "DetailBenefitActivity"; // 로그 찍을 때 사용하는 TAG

    private String detail_data; // 상세 페이지 타이틀

    // 대상, 내용, 기간 텍스트 View
    private TextView detail_contents, detail_title, detail_contact, detail_target;
    // 타이틀과 연관된 이미지 View
    private ImageView detail_logo;

    LinearLayout apply_view, content_view;
    View apply_bottom_view, content_bottom_view;

    // 서버에서 온 값을 파싱할 때 사용할 변수
    String name, target, contents, period, contact;

    // 연관된 혜택 밑의 가로로 버튼들을 넣을 리사이클러뷰
    private RecyclerView detail_benefit_recyclerview;
    private DetailBenefitRecyclerAdapter adapter;
    private DetailBenefitRecyclerAdapter.ItemClickListener itemClickListener;
    ArrayList<DetailBenefitItem> lists = new ArrayList<>();

    // 즐겨찾기 버튼
    LikeButton favorite_btn;

    // 즐겨찾기 저장 시 사용할 이메일, 혜택명, 북마크 여부
    String email, welf_name, isBookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbenefit);

        Log.e(TAG, "onCreate 실행");

        /* findViewById() 모아놓은 메서드 */
        init();

        // 이 화면으로 돌아왔을 때 서버에 저장된 즐겨찾기 버튼 상태값이 true라면 빨간색 하트가 나오게 하고
		// false라면 회색 하트가 나오게 처리해 즐겨찾기 상태를 알 수 있게 한다

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
        detailData(detail_data, email);

    } // onCreate() end

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
					Log.e(TAG, "onResponse 성공 : " + response.body());
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

    private void init()
    {
        detail_logo = findViewById(R.id.detail_logo);

        detail_title = findViewById(R.id.detail_title);
        detail_target = findViewById(R.id.detail_target);
        detail_contents = findViewById(R.id.detail_contents);
        detail_contact = findViewById(R.id.detail_contact);

        apply_view = findViewById(R.id.apply_view);
        content_view = findViewById(R.id.content_view);
        apply_bottom_view = findViewById(R.id.apply_bottom_view);
        content_bottom_view = findViewById(R.id.content_bottom_view);

        detail_benefit_recyclerview = findViewById(R.id.detail_benefit_btn_recyclerview);
        favorite_btn = findViewById(R.id.favorite_btn);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(TAG, "onResume 실행");

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

        // 내용을 누르면 신청방법 밑의 노란 바가 사라지고, 내용 밑에 노란 바가 생겨서 어떤 걸 클릭했는지 알려주게 한다
        content_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                content_bottom_view.setVisibility(View.VISIBLE);
                apply_bottom_view.setVisibility(View.GONE);
            }
        });

        // 신청방법을 누르면 내용 밑의 노란 바가 사라지고, 신청방법 밑에 노란 바가 생겨서 어떤 걸 클릭했는지 알려주게 한다
        apply_view.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                apply_bottom_view.setVisibility(View.VISIBLE);
                content_bottom_view.setVisibility(View.GONE);
            }
        });

    }

    // 서버에서 가져온 String 형태의 JSON 값들을 파싱한 뒤, 특수문자를 콤마로 바꿔서 뷰에 set하는 메서드
    /* GSON 써서도 해보자 */
    private void jsonParsing(String detail) {

        try {
            JSONObject jsonObject_total = new JSONObject(detail);
            String retBody_data;

            retBody_data = jsonObject_total.getString("retBody");

            Log.i(TAG, "retBody 내용 : " + retBody_data);

            JSONObject jsonObject_detail = new JSONObject(retBody_data);

            String name;
            String target;
            String contents;
            String period;
            String contact;
            String isBookmark;

            name = jsonObject_detail.getString("welf_name");
            target = jsonObject_detail.getString("welf_target");
            contents = jsonObject_detail.getString("welf_contents");
            period = jsonObject_detail.getString("welf_period");
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

        } catch (JSONException e) {
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
        Pattern line_pattern = Pattern.compile("^;");
        Pattern comma_pattern = Pattern.compile(";;");

        String target_line = target.replace("^;", "\n");
        String target_comma = target_line.replace(";;", ",");
        Log.e(TAG, "특수기호 변환 후 : " + target_comma);

        String contents_line = contents.replace("^;", "\n");
        String contents_comma = contents_line.replace(";;", ",");
        Log.e(TAG, "특수기호 변환 후 : " + contents_comma);

        String contact_line = contact.replace("^;", "\n");
        String contact_comma = contact_line.replace(";;", ",");
        Log.e(TAG, "특수기호 변환 후 : " + contact_comma);

        detail_target.setText(target_comma);
        detail_contents.setText(contents_comma);
        detail_contact.setText(contact_comma);
    }

}
