package com.psj.welfare.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.activity.DetailBenefitActivity;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.adapter.PushGatherAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.PushGatherItem;
import com.psj.welfare.util.DBOpenHelper;
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

    String pushId, welf_name, welf_local, push_title, push_body, push_date, session;

    /* 어댑터에서 데이터를 담은 리스트를 액티비티에서 사용하기 위한 변수 */
    private List<PushGatherItem> activity_list;

    // 알림 삭제 결과 확인용 변수
    String status, message;

    String sqlite_token;

    DBOpenHelper helper;

    PushViewModel pushViewModel;

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

//        // size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
//        ScreenSize screen = new ScreenSize();
//        // context의 스크린 사이즈를 구함
//        Point size = screen.getScreenSize(getActivity());
//
//        push_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.035));
//        push_all_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));
//        push_latest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));
//        push_recommend_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.x * 0.045));

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
        helper = new DBOpenHelper(getActivity());
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while(cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);

        boolean isLogin = sharedPreferences.getBoolean("logout", false);
        Log.e(TAG, "로그아웃 상태(false : 로그인, true : 로그아웃) : " + isLogin);
        if (isLogin)
        {
            // 로그아웃 상태인 경우 - xml 화면에 변경 없이 그대로 보여지게 한다
        }
        else
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

    /* 서버에서 푸시 데이터들을 가져오는 메서드 */
    private void getPushData()
    {
        pushViewModel = new ViewModelProvider(getActivity()).get(PushViewModel.class);
        final Observer<String> pushObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    Log.e(TAG, "서버에서 받은 푸시 목록 : " + str);
                    messageParsing(str);
                }
                else
                {
                    Log.e(TAG, "str이 null입니다");
                }
            }
        };
        pushViewModel.getPushDatas().observe(getActivity(), pushObserver);
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
                SharedPreferences.Editor editor = sharedPreferences.edit();
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

        /* 푸시 알림 리스트를 만들 어댑터 초기화 */
        adapter = new PushGatherAdapter(getActivity(), list, itemClickListener);
        adapter.setOnItemClickListener((view, position) ->
        {
            String welf_name = list.get(position).getWelf_name();
            welf_local = list.get(position).getWelf_local();
            // 혜택 이름을 뽑고 푸시를 클릭하면 해당 액티비티로 이동하도록 한다
            // 알림 클릭 시 해당 알림의 id와 일치하는 알림 데이터의 수신 상태값을 바꾼다
            checkUserWatchedPush();
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
            push_bell_textview.setVisibility(View.VISIBLE);
            push_bell_textview.setText("도착한 혜택 알림이 없어요");
            push_bell_image.setVisibility(View.VISIBLE);
            push_recyclerview.setVisibility(View.VISIBLE);
        }
        else
        {
            push_bell_image.setVisibility(View.GONE);
            push_bell_textview.setVisibility(View.GONE);
            push_recyclerview.setVisibility(View.VISIBLE);

            /* 왼쪽으로 아이템을 밀어서 푸시 알림을 삭제할 수 있게 하는 콜백, 빠르게 하면 안되고 어느 정도 뜸 들여서 해야 삭제가 정상적으로 이뤄진다 */
            ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
            {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
                {
                    return false;
                }

                // 스와이프 이벤트 발생 시 호출되는 콜백
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
                {
                    List<PushGatherItem> list = adapter.getList();
                    final int position = viewHolder.getAdapterPosition();
                    // 왼쪽으로 스와이프했을 경우
                    if (direction == ItemTouchHelper.LEFT)
                    {
                        for (int i = 0; i < activity_list.size(); i++)
                        {
                            // 스와이프한 아이템의 id값이 리스트의 id값과 일치하는지 확인 후 제거
                            if (activity_list.get(i).getPushId().equals(list.get(position).getPushId()))
                            {
                                String erase_target_id = activity_list.get(i).getPushId();
                                removePush(erase_target_id);
                                activity_list.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyDataSetChanged();
                                push_recyclerview.setAdapter(adapter);  // RecyclerViewEmptySupport로 구현한 리사이클러뷰에 사용 시 에러 발생
                            }
                        }
                    }
                }
            };

            // 위에서 만든 콜백을 ItemTouchHelper에 붙인 다음 리사이클러뷰에 붙여야 밀어서 삭제하기가 가능하다
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(push_recyclerview);
        }
        push_recyclerview.setAdapter(adapter);
    }

    /* 알림 클릭 시 수신 상태값을 바꾸는 메서드 */
    void checkUserWatchedPush()
    {
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        if (!sharedPreferences.getString("sessionId", "").equals(""))
        {
            session = sharedPreferences.getString("sessionId", "");
        }
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<String> call = apiInterface.checkUserWatchedPush(session, sqlite_token, pushId, "pushRecv");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    Log.e(TAG, "알림 클릭 후 수신 알림값 변경 확인 : " + response.body());
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
        sharedPreferences = getActivity().getSharedPreferences("app_pref", 0);
        String session = sharedPreferences.getString("sessionId", "");
        Call<String> call = apiInterface.removePush(session, sqlite_token, pushId, "delete");
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    toastParse(result);
                    Log.e(TAG, "알림 삭제 결과 : " + result);
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

    /* 알림 삭제 결과에 따라 토스트를 띄우는 메서드 */
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
        else
        {
            Toast.makeText(getActivity(), "에러가 발생했습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
        }
    }

}