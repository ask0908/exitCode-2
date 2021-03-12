package com.psj.welfare.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.API.ApiClient;
import com.psj.welfare.API.ApiInterface;
import com.psj.welfare.Activity.DetailBenefitActivity;
import com.psj.welfare.Adapter.PushGatherAdapter;
import com.psj.welfare.Data.PushGatherItem;
import com.psj.welfare.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 받았던 알림들을 확인할 수 있는 프래그먼트 */
public class PushGatherFragment extends Fragment
{
    private final String TAG = "PushGatherFragment";

    Toolbar push_toolbar;
    RecyclerView push_layout_recycler;
    PushGatherAdapter adapter;
    PushGatherAdapter.ItemClickListener itemClickListener;

    // 토큰값을 확인할 때 사용할 쉐어드
    SharedPreferences app_pref;

    String pushId, welf_name, welf_local, push_title, push_body, push_date, token, session;
    // 푸시 알림 변수 바껴서 다시 생성, welf_name, welf_local은 일치
    String welf_category, push_tag, welf_period, welf_end;

    /* 어댑터에서 데이터를 담은 리스트를 액티비티에서 사용하기 위한 변수 */
    List<PushGatherItem> activity_list;

    // 알림 삭제 결과 확인용 변수
    String status, message;

    public PushGatherFragment()
    {
    }

    public static PushGatherFragment newInstance()
    {
        PushGatherFragment fragment = new PushGatherFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.e(TAG, "onActivityCreated() 호출");
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
        Log.e(TAG, "onViewCreated() 호출");

        push_toolbar = view.findViewById(R.id.push_toolbar);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(push_toolbar);
        }
        push_toolbar.setTitle("알림");

        activity_list = new ArrayList<>();

        /* 서버에 저장된 푸시 데이터들을 가져오는 메서드 */
        getPushData();

        push_layout_recycler = view.findViewById(R.id.push_layout_recycler);
        push_layout_recycler.setHasFixedSize(true);
        push_layout_recycler.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        push_layout_recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                final int position = viewHolder.getAdapterPosition();
                switch (direction)
                {
                    case ItemTouchHelper.LEFT :
                        activity_list.remove(position);
                        adapter.notifyItemRemoved(position);
                        // 왼쪽으로 밀어서 푸시 알림 삭제
                        removePush(pushId);
                        break;
                }
            }
        };

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(push_layout_recycler);
    }

    /* 푸시 알림 받으면 수신 상태값을 변경하는 메서드 */
    void changePushStatus()
    {
        app_pref = Objects.requireNonNull(getActivity()).getSharedPreferences("app_pref", 0);
        if (app_pref.getString("token", "").equals(""))
        {
            token = null;
        }
        else
        {
            token = app_pref.getString("token", "");
        }
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.changePushStatus(token, "customizedRecv");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e("changePushStatus()", "수신 상태값 변경 성공 : " + result);
                }
                else
                {
                    Log.e(TAG, "수신 상태값 변경 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 서버에서 푸시 데이터들을 가져오는 메서드 */
    void getPushData()
    {
        app_pref = getActivity().getSharedPreferences("app_pref", 0);
        String token = app_pref.getString("token", "");
        String session = app_pref.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.getPushData(token, session, "pushList");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "서버에서 받은 푸시 알림 데이터 = " + response.body());
                    messageParsing(response.body());
                    changePushStatus();
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
            // 알림이 없으면 토스트 알림을 띄우기 위해 상태값 쉐어드에 저장
            if (!jsonObject.getString("Status").equals(""))
            {
                String status = jsonObject.getString("Status");
                SharedPreferences.Editor editor = app_pref.edit();
                editor.putString("push_status", status);
                editor.apply();
            }
            JSONArray jsonArray = jsonObject.getJSONArray("Message");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject push_object = jsonArray.getJSONObject(i);
                pushId = push_object.getString("pushId");
                welf_name = push_object.getString("welf_name");
                welf_local = push_object.getString("welf_local");
                push_title = push_object.getString("title");
                push_body = push_object.getString("content");
                push_date = push_object.getString("update_date");

                PushGatherItem item = new PushGatherItem();
                item.setPushId(pushId);
                item.setWelf_name(welf_name);
                item.setPush_gather_title(push_title);
                item.setPush_gather_desc(push_body);
                item.setPush_gather_date(push_date);
                item.setWelf_local(welf_local);
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
                welf_local = list.get(position).getWelf_local();
                // 혜택 이름을 뽑고 푸시를 클릭하면 해당 액티비티로 이동하도록 한다
                Log.e(TAG, "알림 화면에서 아이템 클릭 - id : " + pushId);
                Log.e(TAG, "알림 화면에서 아이템 클릭 - 혜택명 : " + welf_name);
                Log.e(TAG, "알림 화면에서 아이템 클릭 - 지역 : " + welf_local);
                Log.e(TAG, "알림 화면에서 아이템 클릭 - 이름 : " + push_title);
                Log.e(TAG, "알림 화면에서 아이템 클릭 - 내용 : " + push_body);
                Log.e(TAG, "알림 화면에서 아이템 클릭 - 날짜 : " + push_date);
                // 알림 클릭 시 해당 알림의 id와 일치하는 알림 데이터의 수신 상태값을 바꾼다
                checkUserWatchedPush();
                Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
                intent.putExtra("name", welf_name);
                intent.putExtra("welf_local", welf_local);
                startActivity(intent);
            }
        });
        activity_list = adapter.getList();
        for (int i = 0; i < activity_list.size(); i++)
        {
            Log.e(TAG, "activity_list = " + activity_list.get(i));
        }
        push_layout_recycler.setAdapter(adapter);
    }

    /* 알림 클릭 시 수신 상태값을 바꾸는 메서드 */
    void checkUserWatchedPush()
    {
        app_pref = getActivity().getSharedPreferences("app_pref", 0);
        if (!app_pref.getString("sessionId", "").equals(""))
        {
            session = app_pref.getString("sessionId", "");
        }
        if (!app_pref.getString("token", "").equals(""))
        {
            token = app_pref.getString("token", "");
        }
        Log.e(TAG, "pushId 확인 : " + pushId);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.checkUserWatchedPush(session, token, pushId, "pushRecv");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "수신 알림값 변경 결과 : " + result);
                }
                else
                {
                    Log.e(TAG, "수신 알림값 변경 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    /* 알림 삭제 메서드 */
    private void removePush(String pushId)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        app_pref = getActivity().getSharedPreferences("app_pref", 0);
        String token = app_pref.getString("token", "");
        String session = app_pref.getString("sessionId", "");
        Call<String> call = apiInterface.removePush(session, token, pushId, "delete");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "알림 삭제 성공 : " + result);
                    toastParse(result);
                }
                else
                {
                    Log.e(TAG, "알림 삭제 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "알림 삭제 에러 : " + t.getMessage());
            }
        });
    }

    private void toastParse(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            status = jsonObject.getString("Status");
            message = jsonObject.getString("Message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status.equals("200"))
        {
            Toast.makeText(getActivity(), "알림 삭제가 완료되었습니다", Toast.LENGTH_SHORT).show();
        }
    }

    /* API 사용 후 결과를 확인할 때 사용하는 메서드, 여기서 쓰진 않고 retrofit 객체화 시 client 객체를 설정해 사용한다 */
    private HttpLoggingInterceptor httpLoggingInterceptor()
    {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger()
        {
            @Override
            public void log(String message)
            {
                Log.e("인터셉터 내용 : ", message);
            }
        });

        return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }
}