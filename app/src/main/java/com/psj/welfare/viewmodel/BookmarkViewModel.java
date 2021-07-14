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

public class BookmarkViewModel extends AndroidViewModel
{
    private final String TAG = BookmarkViewModel.class.getSimpleName();
    private ApiInterface apiInterface;


//    DBOpenHelper helper;
//    String sqlite_token;


    public BookmarkViewModel(@NonNull Application application)
    {
        super(application);
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
//        helper = new DBOpenHelper(application.getApplicationContext());
//        helper.openDatabase();
//        helper.create();
//
//        Cursor cursor = helper.selectColumns();
//        if (cursor != null)
//        {
//            while (cursor.moveToNext())
//            {
//                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
////                Log.e("TAG",sqlite_token);
//            }
//        }

    }

    public MutableLiveData<String> selectBookmark(String token,String page)
    {
        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.getBookmark(token,"show", page)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
//                            Log.e(TAG, "북마크 데이터 가져오기 : " + response.body());
                            data.setValue(response.body());
                        }
                        else
                        {
                            Log.e(TAG, "뷰모델에서 북마크 데이터 가져오기 실패 : " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "뷰모델에서 북마크 데이터 가져오기 에러 : " + t.getMessage());
                    }
                });
        return data;
    }

}
