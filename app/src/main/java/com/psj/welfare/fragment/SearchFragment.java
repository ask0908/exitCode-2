package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.adapter.SearchAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 혜택 이름 검색하는 프래그먼트 */
public class SearchFragment extends Fragment
{
    /* FourthFragment TAG */
    public static final String TAG = "FourthFragment"; // 로그 찍을 때 사용하는 TAG

    private RecyclerView search_frame_recycler;
    private SearchAdapter search_frame_adapter;

    private EditText searching;
    private LinearLayout search_main;
    FrameLayout search_result;
    TextView search_frame_title;

    private ArrayList<SearchItem> searchList;

    int position_Search = 0;
    String search;

    public SearchFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
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
        search_frame_adapter = new SearchAdapter(getActivity(), searchList, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 키워드 검색으로 나온 결과들 중 하나를 선택했을 때 해당 정책의 상세 정보를 보러 가기위한 로직
                Object obj = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) obj;
                    Log.e(TAG, "검색 키워드를 클릭 했어요 -> " + position);
                    Intent frag_search_intent = new Intent(getActivity(), DetailBenefitActivity.class);
                    frag_search_intent.putExtra("RBF_title", searchList.get(position).getSearchTitle());
                    startActivity(frag_search_intent);

                }
            }
        });
        search_frame_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        search_frame_recycler.setAdapter(search_frame_adapter);

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        searching.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {

                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
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
    public void performSearch(String search)
    {
        Log.e(TAG, "performSearch start!");

        // 모바일 키보드 숨기기 로직
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searching.getWindowToken(), 0);

        search_result.setVisibility(View.VISIBLE);

        Log.e(TAG, "키워드 입력 : " + search);

        // 입력한 키워드가 null이 아니면 서버로 키워드를 보내 연관 혜택들을 가져온다
        if (search != null)
        {
            ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
            Call<String> call = apiInterface.search(search);
            call.enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
                {
                    if (response.isSuccessful() && response.body() != null)
                    {
                        Log.e(TAG, "onResponse 성공 : " + response.body());
                        String searchData = response.body();
                        jsonParsing(searchData);

                    }
                    else
                    {
                        Log.e(TAG, "onResponse 실패");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
                {
                    Log.e(TAG, "onFailure : " + t.toString());

                }
            }); // call enqueue end
        }

    }

    private void jsonParsing(String search)
    {
        try
        {
            // 서버에서 온 값을 JSONObject 객체에 담는다
            JSONObject search_total = new JSONObject(search);
            String search_data, search_title;
            // JSONObject 객체 안에서 retCount라는 String을 찾아서 String 변수에 삽입
            search_title = search_total.getString("retCount");

            if (search_title.equals("0"))
            {
                // 키워드 검색 결과 없을 때 로직
                search_frame_title.setText("검색 결과가 존재하지 않아요!\n\n다른 키워드로 검색해보세요");
            }
            else
            {
                Log.e(TAG, "키워드 검색 내용 크기 : " + search_total.length());
                Log.e(TAG, "키워드 검색 내용 세팅 시작!!");
                for (int i = 0; i < search_total.length() - 1; i++)
                {
                    String search_num = String.valueOf(i);
                    Log.e(TAG, "json 반복문 : " + search_num);
                    search_data = search_total.getString(search_num);
                    searchList.add(position_Search, new SearchItem(search_data));
                    position_Search++;
                }
                search_frame_adapter.notifyDataSetChanged();
                Log.e(TAG, "키워드 검색 내용 세팅 끝!!");

                // 키워드 검색 결과 수 출력 로직
                search_frame_title.setText(search_title + "개 검색 결과가 나오네요!");
            }


        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    } // jsonParsing end

} // FourthFragment class end