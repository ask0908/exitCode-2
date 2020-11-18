package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class FourthFragment extends Fragment {

	/* FourthFragment TAG */
	public static final String TAG = "FourthFragment"; // 로그 찍을 때 사용하는 TAG

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private RecyclerView search_frame_recycler;
	private SearchAdapter search_frame_adapter;

	private EditText searching;
	private LinearLayout search_main;
	FrameLayout search_result;
	TextView search_frame_title;

	private ArrayList<SearchItem> searchList;

	int position_Search = 0;
	String search;

	public FourthFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment FourthFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static FourthFragment newInstance(String param1, String param2) {
		FourthFragment fragment = new FourthFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView start!");
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_fourth, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.e(TAG, "onActivityCreated 실행!");


	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.e(TAG, "onViewCreated start!");

		searching = view.findViewById(R.id.searching);
		search_main = view.findViewById(R.id.search_main);
		search_result = view.findViewById(R.id.search_result);
		search_frame_recycler = view.findViewById(R.id.search_frame_recycler);
		search_frame_title = view.findViewById(R.id.search_frame_title);

		searchList = new ArrayList<>();
		search_frame_recycler = (RecyclerView) view.findViewById(R.id.search_frame_recycler);
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

		// 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
		searching.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					Log.e(TAG, "키보드에서 검색 버튼 클릭 했어요!");
					Log.e(TAG, "키워드 : " + searching.getText());

					search_main.setVisibility(View.GONE);
					// 검색 버튼 클릭 되었을 때 처리하는 기능
					performSearch(searching.getText().toString());

					return true;

				}

				return false;

			}

		});
	} // onViewCreated end

	// 모바일 키보드에서 검색 버튼 눌렀을 때 다음 처리는 어떻게 할 것인지
	public void performSearch(String search) {
		Log.e(TAG, "performSearch start!");

		// 모바일 키보드 숨기기 로직
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searching.getWindowToken(), 0);

		search_result.setVisibility(View.VISIBLE);

		Log.e(TAG, "키워드 입력 : " + search);

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

} // FourthFragment class end