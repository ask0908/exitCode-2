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

public class MyPageViewModel extends AndroidViewModel
{
    private final String TAG = MyPageViewModel.class.getSimpleName();
    private ApiInterface apiInterface;

    public MyPageViewModel(@NonNull Application application)
    {
        super(application);

        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    }

    public MutableLiveData<String> getMyReview(String token, String page)
    {
        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.checkMyReview(token, page)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            String result = response.body();
                            Log.e(TAG, "뷰모델에서 내가 작성한 리뷰 가져오기 : " + result);
                            data.setValue(result);
                        }
                        else
                        {
                            Log.e(TAG, "뷰모델에서 내가 작성한 리뷰 가져오기 실패 : " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "뷰모델에서 내가 작성한 리뷰 가져오기 에러 : " + t.getMessage());
                    }
                });
        return data;
    }

}