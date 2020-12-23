package com.psj.welfare.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.PushGatherItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.adapter.PushGatherAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 받았던 알림들을 확인할 수 있는 프래그먼트 */
public class PushGatherFragment extends Fragment
{
    private final String TAG = "InterestBenefitFragment";

    Toolbar push_toolbar;
    RecyclerView push_layout_recycler;
    PushGatherAdapter adapter;
    PushGatherAdapter.ItemClickListener itemClickListener;

    List<PushGatherItem> list;

    SharedPreferences app_pref;

    String welf_name, push_title, push_body, push_date, token;

    public PushGatherFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_push_gather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        push_toolbar = view.findViewById(R.id.push_toolbar);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(push_toolbar);
        }
        push_toolbar.setTitle("알림");
        app_pref = getActivity().getSharedPreferences("app_pref", 0);
        token = app_pref.getString("token", "");
        Log.e(TAG, "onViewCreated() 쉐어드에 저장된 서버 토큰값 = " + token);

        getPushData();

        push_layout_recycler = view.findViewById(R.id.push_layout_recycler);
        push_layout_recycler.setHasFixedSize(true);
        push_layout_recycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        push_layout_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    /* 서버에 저장된 푸시 데이터들을 가져오는 메서드 */
    void getPushData()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getPushData(token);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "쉐어드에 저장된 서버 토큰으로 뽑은 결과 = " + response.body());
                    messageParsing(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e("getPushData()", "에러 = " + t.getMessage());
            }
        });
    }

    /* 서버에서 받은 값들을 파싱해서 리사이클러뷰에 뿌리는 메서드 */
    void messageParsing(String response)
    {
        List<PushGatherItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject push_object = jsonArray.getJSONObject(i);
                welf_name = push_object.getString("welf_name");
                push_title = push_object.getString("title");
                push_body = push_object.getString("content");
                push_date = push_object.getString("update_date");
                PushGatherItem item = new PushGatherItem();
                item.setWelf_name(welf_name);
                item.setPush_gather_title(push_title);
                item.setPush_gather_desc(push_body);
                item.setPush_gather_date(push_date);
                list.add(item);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener(new PushGatherAdapter.ItemClickListener()
        {
            @Override
            public void onItemClick(View view, int position)
            {
                String welf_name = list.get(position).getWelf_name();
                // 혜택 이름을 뽑고 푸시를 클릭하면 해당 액티비티로 이동하도록 한다
                Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
                intent.putExtra("RBF_title", welf_name);
                startActivity(intent);
            }
        });
        push_layout_recycler.setAdapter(adapter);
    }

}