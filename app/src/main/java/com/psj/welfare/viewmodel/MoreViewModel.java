package com.psj.welfare.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 더보기 화면에서 사용하는 뷰모델 */
public class MoreViewModel extends AndroidViewModel
{
    private final String TAG = MoreViewModel.class.getSimpleName();
    private ApiInterface apiInterface;

    public MoreViewModel(@NonNull Application application)
    {
        super(application);
    }

    /* 로그인 상태일 때 메인에서 더보기를 누르면 호출되는 메서드 */
    public MutableLiveData<String> moreViewWelfareLogin(String token, String session, String page, String assist_method)
    {
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        final MutableLiveData<String> data = new MutableLiveData<>();
        Log.e(TAG, "token : " + token);
        Log.e(TAG, "session : " + session);
        Log.e(TAG, "page : " + page);
        Log.e(TAG, "assist_method : " + assist_method);
        apiInterface.moreViewWelfareLogin(token, session, page, assist_method)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.e(TAG, "로그인 상태에서 상단 필터 클릭해 가져온 데이터 : " + response.body());
                            data.setValue(response.body());
                        }
                        else
                        {
                            Log.e(TAG, "로그인 상태에서 더보기 눌러 데이터 가져오기 실패 : " + response.body()); // TODO : 여기서 null 나옴
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "에러 : " + t.getMessage());
                    }
                });
        return data;
    }

    /* 관심사 o, 로그인 x일 때 메인에서 더보기를 누르면 호출되는 메서드 */
    public MutableLiveData<String> moreViewWelfareNotLogin(String session, String page, String assist_method, String gender, String age, String local)
    {
        Log.e(TAG, "비로그인 시 데이터 가져오는 메서드 호출");
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        final MutableLiveData<String> data = new MutableLiveData<>();
        Log.e(TAG, "session : " + session);
        Log.e(TAG, "page : " + page);
        Log.e(TAG, "assist_method : " + assist_method);
        Log.e(TAG, "gender : " + gender);
        Log.e(TAG, "age : " + age);
        Log.e(TAG, "local : " + local);
        apiInterface.moreViewWelfareNotLogin(session, page, assist_method, gender, age, local)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            Log.e(TAG, "더보기 뷰모델에서 관심사 x, 로그인 o일 때 가져온 데이터 : " + response.body());
                            data.setValue(response.body());
                        }
                        else
                        {
                            Log.e(TAG, "비로그인 상태에서 더보기 눌러 데이터 가져오기 실패 : " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "에러 : " + t.getMessage());
                    }
                });
        return data;
    }

}
