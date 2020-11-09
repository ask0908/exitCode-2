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

import com.psj.welfare.Data.EndCategoryItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.EndCategoryAdapter;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * 마지막 카테고리 선택 액티비티는 사용자의 맞춤 혜택을 보여주기 위한 마지막 단계이다
 * 세번째 선택으로 연관된 마지막 선택지가 주어진다
 * 사용자에게 보여주는 혜택의 범위를 줄일 수 있어야 한다
 * 마지막 선택이기 때문에 선택 후에는 혜택의 상세 내용을 보여주어야 한다
 * */
public class EndCategory extends AppCompatActivity {

	public static final String TAG = "EndCategory";

	private RecyclerView EndCategory_recyclerview;
	private RecyclerView.Adapter EndCategory_Adapter;

	int position_EndCategory = 0;
	int init_color;

	private ArrayList<EndCategoryItem> EndCategory_List;
	private JSONArray jsonArray;
	JSONObject jsonObject;
	JSONArray select_list;
	String end_select;

	Button endCategory_done;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_endcategory);
		Log.i(TAG, "onCreate 실행!");

		endCategory_done = findViewById(R.id.endCategory_done);

		EndCategory_List = new ArrayList<EndCategoryItem>();
		init_color = Color.parseColor("#e6e6e6");

		if (getIntent().hasExtra("retBody")) {
			Intent End_intent = getIntent();

			String retBody = End_intent.getStringExtra("retBody");
			Log.i(TAG, "리스트 형태 결과 카테고리 정보 -> " + retBody.toString());
			try {
				jsonObject = new JSONObject(retBody);
				select_list = jsonObject.getJSONArray("welfare_list");
				Log.e(TAG, "결과 카테고리 첫번째 정보 : " + select_list.getString(0));
				Log.e(TAG, "리스트 형태 결과 카테고리 길이 : " + select_list.length());

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Log.i(TAG, "전달 받은 인텐트 값 없어요!");
		}

		// 사용자가 선택한 두번째 or 세번째 카테고리 정보를 통해서 서버에서 가져온 결과 카테고리 정보 세팅 하는 곳
		for (int i = 0; i < select_list.length(); i++) {
			try {
				EndCategory_List.add(position_EndCategory, new EndCategoryItem(select_list.getString(i), init_color));
				position_EndCategory++;
			} catch (Exception e) {
				e.printStackTrace();
			}

		} // 반복문 끝

		// 카테고리 결과 리사이클러뷰 시작
		EndCategory_recyclerview = findViewById(R.id.recycler_categoryEnd);
		EndCategory_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		EndCategory_Adapter = new EndCategoryAdapter(getApplicationContext(), EndCategory_List, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Object object = v.getTag();
				if (v.getTag() != null) {
					int position = (int) object;
					Log.e(TAG, "클릭한 포지션 값 : " + position);
					String c_title = ((EndCategoryAdapter) EndCategory_Adapter).getCategory(position).getCategoryTitle();
					int c_bg = ((EndCategoryAdapter) EndCategory_Adapter).getCategory(position).getCategoryBg();

					int select_color = Color.parseColor("#54A4FF");
					int ready_color = Color.parseColor("#e6e6e6");

					// 선택한 버튼 이외에 버튼은 기본 색상을 가지는 반복문
					for (int i = 0; i < EndCategory_List.size(); i++) {
						try {
							EndCategory_List.set(i, new EndCategoryItem(select_list.getString(i), ready_color));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					// 선택한 버튼은 색상이 파랑으로 바뀌는 로직
					EndCategory_List.set(position, new EndCategoryItem(c_title, select_color));
					EndCategory_Adapter.notifyDataSetChanged();

					// 선택한 버튼과 선택하지 않은 버튼의 색상 수정이 끝난 후
					// 사용자가 선택한 타이틀을 서버에 보내기 위해서 저장하는 변수
					end_select = EndCategory_List.get(position).getCategoryTitle();
					Log.e(TAG, "서버에 보내기 위해서 저장하는 타이틀 : " + end_select);
				} // 리사이클러뷰의 설정한 태그값 체크 끝
			}
		});
		EndCategory_recyclerview.setAdapter(EndCategory_Adapter);
		EndCategory_recyclerview.setHasFixedSize(true);
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

		endCategory_done.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				Intent end_intent = new Intent(EndCategory.this, DetailBenefitActivity.class);
				end_intent.putExtra("RBF_title", end_select);
				startActivity(end_intent);
				finish();
			}
		});
	} // onResume end

} // EndCategory class end
