package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.adapter.SearchAdapter;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSearch extends Fragment {

	public static final String TAG = "FragmentSearch";

	private RecyclerView search_frame_recycler;
	private SearchAdapter search_frame_adapter;
	private TextView search_frame_title;

	int position_Search = 0;
	String search;

	private ArrayList<SearchItem> searchList;

	public FragmentSearch() {
		Log.e(TAG, "생성자 실행!");
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		Log.e(TAG, "onAttach 실행!");
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate 실행!");

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		Log.e(TAG, "onCreateView 실행!");

		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragmentsearch, container, false);
		search_frame_title = rootView.findViewById(R.id.search_frame_title);

		searchList = new ArrayList<>();
		search_frame_recycler = (RecyclerView) rootView.findViewById(R.id.search_frame_recycler);
		search_frame_recycler.setHasFixedSize(true);
		search_frame_adapter = new SearchAdapter(getActivity(), searchList, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 키워드 검색으로 나온 결과들 중 하나를 선택했을 때 해당 정책의 상세 정보를 보러 가기위한 로직
				Object obj = v.getTag();
				if (v.getTag() != null) {
					int position = (int) obj;
					Log.i(TAG, "검색 키워드를 클릭 했어요 -> " + position);
					Intent frag_search_intent = new Intent(getActivity(), DetailBenefitActivity.class);
					frag_search_intent.putExtra("RBF_title", searchList.get(position).getSearchTitle());
					startActivity(frag_search_intent);

				}
			}
		});
		search_frame_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		search_frame_recycler.setAdapter(search_frame_adapter);

		Log.e(TAG, "프래그먼트 onCreateView return");
		return rootView;

	} // onCreateView end

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.e(TAG, "onActivityCreated 실행!");

		if (search != null) {
			// 레트로핏 서버 URL 설정해놓은 객체 생성 후 GET, POST 같은 서버에 데이터를 보내기 위해서 생성합니다
			ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

			// 인터페이스 ApiService에 선언한 search()를 호출합니다
			Call<String> call = apiInterface.search(search);
			call.enqueue(new Callback<String>() {
				@Override
				public void onResponse(Call<String> call, Response<String> response) {
					if (response.isSuccessful()) {
						Log.i(TAG, "onResponse 성공 : " + response.body().toString());
						String searchData = response.body().toString();
						jsonParsing(searchData);

					} else {
						Log.i(TAG, "onResponse 실패");
					}
				}

				@Override
				public void onFailure(Call<String> call, Throwable t) {
					Log.i(TAG, "onFailure : " + t.toString());

				}
			}); // call enqueue end
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, "onStart 실행!");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e(TAG, "onResume 실행!");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.e(TAG, "onPause 실행!");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "onStop 실행!");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.e(TAG, "onDestroyView 실행!");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy 실행!");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.e(TAG, "onDetach 실행!");
	}

	public void searchData(String search) {
		this.search = search;
	}

	private void jsonParsing(String search) {

		try {

			JSONObject search_total = new JSONObject(search);
			String search_data, search_title;
			search_title = search_total.getString("retCount");

			if (search_title.equals("0")) {
				// 키워드 검색 결과 없을 때 로직
				search_frame_title.setText("검색 결과가 존재하지 않아요!\n\n다른 키워드로 검색해보세요");
			} else {

				Log.i(TAG, "키워드 검색 내용 크기 : " + search_total.length());
				Log.i(TAG, "키워드 검색 내용 세팅 시작!!");
				for (int i = 0; i < search_total.length() - 1; i++) {
					String search_num = String.valueOf(i);
					Log.i(TAG, "json 반복문 : " + search_num);
					search_data = search_total.getString(search_num);
					searchList.add(position_Search, new SearchItem(search_data));
					position_Search++;
				}
				search_frame_adapter.notifyDataSetChanged();
				Log.i(TAG, "키워드 검색 내용 세팅 끝!!");

				// 키워드 검색 결과 수 출력 로직
				search_frame_title.setText(search_title + "개 검색 결과가 나오네요!");
			}


		} catch (JSONException e) {
			e.printStackTrace();
		}
	} // jsonParsing end

} // FragmentSearch class end
