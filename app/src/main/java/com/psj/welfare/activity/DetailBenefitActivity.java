package com.psj.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.google.gson.JsonObject;
import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.DetailBenefitRecyclerAdapter;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 상세 액티비티는 사용자가 자세한 혜택의 내용을 확인할 수 있어야한다
 * 자세한 내용일지라도 사용자의 피로도를 줄여 줄 수 있는 방법이 적용되어야 한다
 * */
public class DetailBenefitActivity extends AppCompatActivity {

	public static final String TAG = "DetailBenefitActivity"; // 로그 찍을 때 사용하는 TAG

	private String detail_data; // 상세 페이지 타이틀

	// 대상, 내용, 기간 텍스트 View
	private TextView detail_contents, detail_title, detail_contact, detail_target;
	// 타이틀과 연관된 이미지 View
	private ImageView detail_logo;

	LinearLayout apply_view, content_view;
	View apply_bottom_view, content_bottom_view;

	// 연관된 혜택 밑의 가로로 버튼들을 넣을 리사이클러뷰
	private RecyclerView detail_benefit_recyclerview;
	private DetailBenefitRecyclerAdapter adapter;
	private DetailBenefitRecyclerAdapter.ItemClickListener itemClickListener;
	ArrayList<DetailBenefitItem> lists = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailbenefit);

		Log.e(TAG, "onCreate 실행");

		detail_logo = findViewById(R.id.detail_logo);

		detail_title = findViewById(R.id.detail_title);
		detail_target = findViewById(R.id.detail_target);
		detail_contents = findViewById(R.id.detail_contents);
		detail_contact = findViewById(R.id.detail_contact);

		apply_view = findViewById(R.id.apply_view);
		content_view = findViewById(R.id.content_view);
		apply_bottom_view = findViewById(R.id.apply_bottom_view);
		content_bottom_view = findViewById(R.id.content_bottom_view);

		detail_benefit_recyclerview = (RecyclerView) findViewById(R.id.detail_benefit_btn_recyclerview);

		if (getIntent().hasExtra("RBF_title")) {

			Intent RBF_intent = getIntent();
			detail_data = RBF_intent.getStringExtra("RBF_title");

			Log.e(TAG, "상세 페이지에서 보여줄 타이틀 : " + detail_data);

		}

		// 리사이클러뷰 처리
		detail_benefit_recyclerview.setHasFixedSize(true);
		detail_benefit_recyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
		adapter = new DetailBenefitRecyclerAdapter(this, lists, itemClickListener);
		detail_benefit_recyclerview.setAdapter(adapter);

		// 리사이클러뷰 어댑터 안의 클릭 리스너
		itemClickListener = new DetailBenefitRecyclerAdapter.ItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Log.e(TAG, "pos = " + position);
			}
		};

		// 레트로핏 서버 URL 설정해놓은 객체 생성 후 GET, POST 같은 서버에 데이터를 보내기 위해서 생성합니다
		ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
		Log.e(TAG, "상세 내용 불러올 정책 제목 : " + detail_data.toString());
		// 인터페이스 ApiService에 선언한 detailData()를 호출합니다
		Call<JsonObject> call = apiInterface.detailData(detail_data);
		call.enqueue(new Callback<JsonObject>() {
			@Override
			public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
				if (response.isSuccessful()) {
					Log.i(TAG, "onResponse 성공 : " + response.body().toString());
					String detail = response.body().toString();
					jsonParsing(detail);

				} else {
					Log.i(TAG, "onResponse 실패");

				}
			}

			@Override
			public void onFailure(Call<JsonObject> call, Throwable t) {
				Log.i(TAG, "onFailure : " + t.toString());

			}
		}); // call enqueue end


	} // onCreate end

	@Override
	protected void onStart() {
		super.onStart();

		Log.e(TAG, "onStart 실행");


	} // onStart end

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume 실행");

		detail_logo.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				Intent d_intent = new Intent(DetailBenefitActivity.this, MainTabLayoutActivity.class);
				startActivity(d_intent);
				finish();
			}
		});

		content_view.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				content_bottom_view.setVisibility(View.VISIBLE);
				apply_bottom_view.setVisibility(View.GONE);
			}
		});

		apply_view.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				apply_bottom_view.setVisibility(View.VISIBLE);
				content_bottom_view.setVisibility(View.GONE);
			}
		});

	} // onResume end

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

			name = jsonObject_detail.getString("welf_name");
			target = jsonObject_detail.getString("welf_target");
			contents = jsonObject_detail.getString("welf_contents");
			period = jsonObject_detail.getString("welf_period");
			contact = jsonObject_detail.getString("welf_contact");

			detail_title.setText(name);

			symbolChange(target, contents, contact);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	} // jsonParsing end

	// 상세 페이지 긴 글을 축소 / 확장 기능
	public void setReadMoreText(ReadMoreTextView target, ReadMoreTextView contents) {

		target.setTrimCollapsedText("더보기");
		target.setTrimExpandedText("\t...간략히");
		target.setTrimLength(20);
		target.setColorClickableText(ContextCompat.getColor(this, R.color.colorMainBlue));

		contents.setTrimCollapsedText("더보기");
		contents.setTrimExpandedText("\t...간략히");
		contents.setTrimLength(20);
		contents.setColorClickableText(ContextCompat.getColor(this, R.color.colorMainBlue));

	}

	// 특수기호 변환 기능
	public void symbolChange(String target, String contents, String contact) {
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


} // DetailBenefitActivity class end
