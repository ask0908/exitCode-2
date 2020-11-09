package com.psj.welfare.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ThirdCategoryItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.ThirdCategoryAdapter;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 세번째 카테고리 선택 액티비티는 사용자의 맞춤 혜택을 보여주기 위한 세번째 단계이다
 * 두번째 선택으로 연관된 세번째 선택지가 주어진다
 * 사용자에게 보여주는 혜택의 범위를 줄일 수 있어야 한다
 * */
public class ThirdCategory extends AppCompatActivity {

	public static final String TAG = "ThirdCategory";

	private RecyclerView ThirdCategory_recyclerview;
	private RecyclerView.Adapter ThirdCategory_Adapter;

	int position_ThirdCategory = 0;
	int init_color;

	private ArrayList<ThirdCategoryItem> ThirdCategory_List;
	JSONObject jsonObject;
	JSONArray select_list;

	String first_select;
	String second_select;
	String third_select;

	Button category_done;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thirdcategory);
		Log.i(TAG, "onCreate 실행!");

		category_done = findViewById(R.id.category_done);

		ThirdCategory_List = new ArrayList<ThirdCategoryItem>();
		init_color = Color.parseColor("#e6e6e6");

		if (getIntent().hasExtra("retBody")) {
			Intent End_intent = getIntent();

			first_select = End_intent.getStringExtra("first_select");
			second_select = End_intent.getStringExtra("second_select");
			String retBody = End_intent.getStringExtra("retBody");
			Log.i(TAG, "리스트 형태 결과 카테고리 정보 -> " + retBody.toString());
			try {
				jsonObject = new JSONObject(retBody);
				select_list = jsonObject.getJSONArray("third_layer");
				Log.e(TAG, "세번째 카테고리 첫번째 정보 : " + select_list.getString(0));
				Log.e(TAG, "리스트 형태 세번째 카테고리 길이 : " + select_list.length());

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "전달 받은 인텐트 값 없어요!");
		}

		// 사용자가 선택한 첫번째 카테고리 정보를 통해서 서버에서 가져온 두번째 카테고리 정보 세팅 하는 곳
		for (int i = 0; i < select_list.length(); i++) {
			try {
				ThirdCategory_List.add(position_ThirdCategory, new ThirdCategoryItem(select_list.getString(i), init_color));
				position_ThirdCategory++;
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} // 반복문 끝

		// 카테고리 세번째 리사이클러뷰 시작
		ThirdCategory_recyclerview = findViewById(R.id.recycler_categoryThird);
		ThirdCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this));
		ThirdCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		ThirdCategory_Adapter = new ThirdCategoryAdapter(getApplicationContext(), ThirdCategory_List, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Object object = v.getTag();
				if (v.getTag() != null) {
					int position = (int) object;
					Log.e(TAG, "클릭한 포지션 값 : " + position);
					String c_title = ((ThirdCategoryAdapter) ThirdCategory_Adapter).getCategory(position).getCategoryTitle();
					int c_bg = ((ThirdCategoryAdapter) ThirdCategory_Adapter).getCategory(position).getcategoryBg();

					int select_color = Color.parseColor("#54A4FF");
					int ready_color = Color.parseColor("#e6e6e6");

					// 선택한 버튼 이외에 버튼은 기본 색상을 가지는 반복문
					for (int i = 0; i < ThirdCategory_List.size(); i++) {
						try {
							ThirdCategory_List.set(i, new ThirdCategoryItem(select_list.getString(i), ready_color));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					// 선택한 버튼은 색상이 파랑으로 바뀌는 로직
					ThirdCategory_List.set(position, new ThirdCategoryItem(c_title, select_color));
					ThirdCategory_Adapter.notifyDataSetChanged();

					// 선택한 버튼과 선택하지 않은 버튼의 색상 수정이 끝난 후
					// 사용자가 선택한 타이틀을 서버에 보내기 위해서 저장하는 변수
					third_select = ThirdCategory_List.get(position).getCategoryTitle();
					Log.e(TAG, "서버에 보내기 위해서 저장하는 타이틀 : " + third_select);
				}
			}
		});
		ThirdCategory_recyclerview.setAdapter(ThirdCategory_Adapter);
		ThirdCategory_recyclerview.setHasFixedSize(true);
		// 복지 카테고리 첫번째 리사이클러뷰 끝

	} // onCreate end

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart 실행!");

	} // onStart end

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume 실행!");

		category_done.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				// 레트로핏 서버 URL 설정해놓은 객체 생성 후 GET, POST 같은 서버에 데이터를 보내기 위해서 생성합니다
				ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

				// 인터페이스 ApiService에 선언한 category3()를 호출합니다
				Call<String> call = apiInterface.category3(first_select, second_select, third_select, "3");
				call.enqueue(new Callback<String>() {
					@Override
					public void onResponse(Call<String> call, Response<String> response) {
						if (response.isSuccessful()) {
							Log.i(TAG, "onResponse 성공 : " + response.body().toString());
							String category = response.body().toString();
							jsonParsing(category);
						} else {
							Log.i(TAG, "onResponse 실패" + response.body().toString());

						}
					}

					@Override
					public void onFailure(Call<String> call, Throwable t) {
						Log.i(TAG, "onFailure : " + t.toString());

					}
				}); // call enqueue end
			}
		});

	} // onResume end

	private void jsonParsing(String category) {

		try {
			JSONObject jsonObject = new JSONObject(category);

			Log.i(TAG, "JSON 길이 : " + jsonObject.length());
			String retBody = jsonObject.getString("retBody");

			Log.i(TAG, "세번째 카테고리 retBody : " + retBody);

			Intent third_intent = new Intent(ThirdCategory.this, EndCategory.class);
			third_intent.putExtra("retBody", retBody);
			startActivity(third_intent);
			finish();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	} // jsonParsing end

} // ThirdCategory class end
