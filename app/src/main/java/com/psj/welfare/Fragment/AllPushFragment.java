package com.psj.welfare.fragment;

import android.content.Intent;
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
import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.R;
import com.psj.welfare.SharedSingleton;
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

    String push_id, welf_name, welf_id, push_title, push_body, update_date;

    // 알림 삭제 결과 확인용 변수
    private int status_code;
    String message;

    // 토큰, 세션 아이디
    private String token, session_id;
    private boolean isLogin; //로그인 했는지 여부

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

        sharedSingleton = SharedSingleton.getInstance(getActivity());
        token = sharedSingleton.getToken();
        session_id = sharedSingleton.getSessionId();
        isLogin = sharedSingleton.getBooleanLogin();

        if (isLogin)
        {
            // 로그인 상태
            push_bell_image.setVisibility(View.GONE);
            push_bell_textview.setVisibility(View.GONE);
            push_login_button.setVisibility(View.GONE);
            push_recyclerview.setVisibility(View.VISIBLE);
            // 로그인 상태일 때 푸시 알림 목록을 가져와서 보여준다
            getMyPush();
        }
        else
        {
            // 비로그인 상태
            push_bell_textview.setText("최신 혜택과 추천 혜택을 받고 싶다면\n로그인을 해 주세요");
            push_bell_image.setVisibility(View.VISIBLE);
            push_bell_textview.setVisibility(View.VISIBLE);
            push_login_button.setVisibility(View.VISIBLE);
            push_recyclerview.setVisibility(View.GONE);
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
                    Logger.t("AllPushFragment에서 받은 푸시 값 : ").json(str);
                    parseGotPush(str);
                }
            }
        };

        viewModel.getMyPush().observe(getActivity(), pushObserver);
    }

    /* 푸시 알림 가져온 결과를 파싱해서 프래그먼트에 뿌려주는 메서드 */
    private void parseGotPush(String result)
    {
        // 리사이클러뷰 어댑터 초기화 시 넣을 리스트를 초기화함
        List<PushGatherItem> list = new ArrayList<>();
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            status_code = jsonObject.getInt("status_code");
            message = jsonObject.getString("message");

            // status_code가 500(알림 정보가 존재하지 않습니다), 404(data is empty), 400(계정이 존재하지 않습니다) 일 때
            if (status_code == 500 || status_code == 404 || status_code == 400)
            {
                push_bell_textview.setText("도착한 혜택 알림이 없어요");
                push_bell_textview.setVisibility(View.VISIBLE);
                push_bell_image.setVisibility(View.VISIBLE);
                push_recyclerview.setVisibility(View.GONE);
            }
            else
            {
                // status_code가 200일 때 JSONArray(message) 안의 값들 파싱
                JSONArray jsonArray = jsonObject.getJSONArray("message");
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject push_object = jsonArray.getJSONObject(i);
                    push_id = push_object.getString("push_id");
                    welf_id = push_object.getString("welf_id");
                    welf_name = push_object.getString("welf_name");
                    push_title = push_object.getString("title");
                    push_body = push_object.getString("content");
                    update_date = push_object.getString("update_date");

                    PushGatherItem item = new PushGatherItem();
                    item.setPushId(push_id);
                    item.setWelf_id(welf_id);
                    item.setWelf_name(welf_name);
                    item.setPush_gather_title(push_title);
                    item.setPush_gather_desc(push_body);
                    item.setPush_gather_date(update_date);
                    list.add(item);
                }

                /* 푸시 알림들을 보여줄 리사이클러뷰 어댑터 초기화 */
                adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
                adapter.setOnItemClickListener((view, position) ->
                {
                    // 알림 클릭 시 해당 알림의 id와 일치하는 알림 데이터의 수신 상태값을 바꾼다
                    String welf_id = list.get(position).getWelf_id();
                    changePushStatusWhenClicked();
                    // 알림 클릭 시 혜택의 id를 얻어 혜택 상세보기 액티비티로 이동하도록 한다
                    Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
                    intent.putExtra("welf_id", welf_id);
                    startActivity(intent);
                });
//                activity_list = adapter.getList();
                push_bell_image.setVisibility(View.GONE);
                push_bell_textview.setVisibility(View.GONE);
                push_recyclerview.setVisibility(View.VISIBLE);
                push_recyclerview.setAdapter(adapter);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }

    /* 알림 클릭 시 수신 상태값을 바꾸는 메서드 */
    private void changePushStatusWhenClicked()
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        // 서버로 push_id 값을 넘길 때 JSON으로 바꿔서 넘겨야 하기 때문에 JSON 변환 처리
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("push_id", push_id);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Call<String> call = apiInterface.changePushStatusWhenClicked(token, jsonObject.toString());
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