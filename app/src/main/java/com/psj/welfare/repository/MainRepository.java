package com.psj.welfare.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainRepository
{
    private static final String TAG = MainRepository.class.getSimpleName();
    private ApiInterface apiInterface;

    public MainRepository()
    {
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    }

    public MutableLiveData<String> getAllDatas()
    {
        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.showWelfareAndYoutubeNotLogin("total_main")
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        data.setValue(response.body());
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
