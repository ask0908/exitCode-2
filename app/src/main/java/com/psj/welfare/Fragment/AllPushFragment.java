package com.psj.welfare.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.adapter.PushGatherAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.PushGatherItem;
import com.psj.welfare.viewmodel.PushViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPushFragment extends Fragment
{
    private final String TAG = "AllPushFragment";

    private RecyclerView push_recyclerview;
    private ImageView push_bell_image;
    private TextView push_bell_textview;
    private Button push_login_button;

    PushGatherAdapter adapter;
    PushGatherAdapter.ItemClickListener itemClickListener;

    // 로그인 여부 확인 위한 쉐어드
    SharedPreferences sharedPreferences;

    String pushId, welf_name, welf_local, push_title, push_body, push_date;

    /* 어댑터에서 데이터를 담은 리스트를 액티비티에서 사용하기 위한 변수 */
    private List<PushGatherItem> activity_list;

    // 알림 삭제 결과 확인용 변수
    String status, message;

    //토큰, 세션 아이디
    private String token, sessionId;
    private boolean isLogin; //로그인 했는지 여부

    //로그인관련 쉐어드 singleton
    private SharedSingleton sharedSingleton;

    PushViewModel viewModel;

    public AllPushFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_all_push, container, false);

        push_recyclerview = view.findViewById(R.id.push_recyclerview);
        push_bell_image = view.findViewById(R.id.push_bell_image);
        push_bell_textview = view.findViewById(R.id.push_bell_textview);
        push_login_button = view.findViewById(R.id.push_login_button);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        push_recyclerview = view.findViewById(R.id.push_recyclerview);
        push_recyclerview.setHasFixedSize(true);
        push_recyclerview.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        push_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        activity_list = new ArrayList<>();

        sharedSingleton = SharedSingleton.getInstance(getActivity());
        token = sharedSingleton.getToken(); //토큰 값
        sessionId = sharedSingleton.getSessionId(); //세션 값
        isLogin = sharedSingleton.getBooleanLogin(); //로그인 했는지 여부

        if (isLogin)
        {
            // 로그인 상태인 경우 - 알림이 있으면 알림들을 보여주고, 없으면 벨 이미지와 문자열이 바뀐 텍스트뷰를 보여준다
            push_bell_image.setVisibility(View.GONE);
            push_bell_textview.setVisibility(View.GONE);
            push_login_button.setVisibility(View.GONE);
            push_recyclerview.setVisibility(View.VISIBLE);
            getPushData();
        }


        // 비로그인 시 푸시 화면 넘어오면 보이는 로그인 버튼
        push_login_button.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });
    }

    /* 헤더에 토큰을 넣어서 계정별 푸시 알림을 가져오는 메서드 */
    private void getMyPush()
    {
        viewModel = new ViewModelProvider(getActivity()).get(PushViewModel.class);
        final Observer<String> pushObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null && !str.equals(""))
                {
                    Logger.t("TestPushGatherFragment에서 받은 푸시 값 : ").json(str);
                    parsePushData(str);
                }
            }
        };

        viewModel.getMyPush().observe(getActivity(), pushObserver);
    }

    private void parsePushData(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /* 서버에서 푸시 데이터들을 가져오는 메서드 */
    private void getPushData()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.getPushData(token, sessionId, "pushList");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Logger.t("푸시 받아온 결과 : ").json(result);
                    parseGetPush(result);
                }
                else
                {
                    Logger.t("푸시 받아오기 실패 : ").json(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Logger.d("푸시 받아오기 에러 : " + t.getMessage());
            }
        });
    }

    private void parseGetPush(String result)
    {
        List<PushGatherItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            // 알림이 없으면 토스트 알림을 띄우기 위해 상태값 쉐어드에 저장
            if (!jsonObject.getString("Status").equals(""))
            {
                String status = jsonObject.getString("Status");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("push_status", status);
                editor.apply();
            }
            status = jsonObject.getString("Status");
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

        /* 푸시 알림 리스트를 만들 어댑터 초기화 */
        adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener((view, position) ->
        {
            String welf_name = list.get(position).getWelf_name();
            welf_local = list.get(position).getWelf_local();
            // 혜택 이름을 뽑고 푸시를 클릭하면 해당 액티비티로 이동하도록 한다
            // 알림 클릭 시 해당 알림의 id와 일치하는 알림 데이터의 수신 상태값을 바꾼다
            checkUserWatchedPush();
            // TODO : 위의 구 메서드 주석 처리하고 아래의 새 메서드 테스트해보기
            changePushStatusWhenClicked();
            Intent intent = new Intent(getActivity(), DetailBenefitActivity.class);
            intent.putExtra("name", welf_name);
            intent.putExtra("welf_local", welf_local);
            startActivity(intent);
        });
        activity_list = adapter.getList();
        /* getItemCount()의 결과값이 0이라면 아이템이 없는 것이니 리사이클러뷰를 감추고 텍스트뷰를 보여준다
         * 0이 아니라면 알림이 있는 것이니 텍스트뷰를 감추고 리사이클러뷰를 보여준다
         * invisible은 뷰를 안 보이게 하는 대신 위치를 차지하지만 gone은 뷰를 안 보이게 하고 위치도 지워줘서 invisible보다 gone을 쓰는 게 더 좋다 */
        if (adapter.getItemCount() == 0)
        {
            // 어댑터에 값이 없으면 알림이 없는 것이니 리사이클러뷰를 숨기고 텍스트뷰를 보여준다
            push_bell_textview.setText("도착한 혜택 알림이 없어요");
            push_bell_textview.setVisibility(View.VISIBLE);
            push_bell_image.setVisibility(View.VISIBLE);
            push_recyclerview.setVisibility(View.GONE);
        }
        else
        {
            push_bell_image.setVisibility(View.GONE);
            push_bell_textview.setVisibility(View.GONE);
            push_recyclerview.setVisibility(View.VISIBLE);
        }
        push_recyclerview.setAdapter(adapter);
    }

    /* 알림 클릭 시 수신 상태값을 바꾸는 메서드 (신) */
    private void changePushStatusWhenClicked()
    {
        Logger.d("클릭 후 수신 상태값 바꾸는 메서드에서 확인한 토큰 : " + token);
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.changePushStatusWhenClicked(token, pushId);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Logger.t("알림 클릭 후 수신 알림값 변경 확인 : ").json(response.body());
                }
                else
                {
                    Logger.t("수신 알림값 변경 실패 : ").json(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Logger.d("수신 알림값 변경 에러 : " + t.getMessage());
            }
        });
    }

    /* 알림 클릭 시 수신 상태값을 바꾸는 메서드 (구) */
    void checkUserWatchedPush()
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.checkUserWatchedPush(sessionId, token, pushId, "pushRecv");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Logger.t("알림 클릭 후 수신 알림값 변경 확인 : ").json(response.body());
                }
                else
                {
                    Logger.t("수신 알림값 변경 실패 : ").json(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Logger.d("수신 알림값 변경 에러 : " + t.getMessage());
            }
        });
    }

}