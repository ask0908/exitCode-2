package com.psj.welfare.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.DBOpenHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends AndroidViewModel
{
    private final String TAG = SearchViewModel.class.getSimpleName();
    private Context context;
    private ApiInterface apiInterface;
    private MutableLiveData<String> searchLiveData;
    SharedPreferences sharedPreferences;
    String sqlite_token;

    DBOpenHelper helper;

    public SearchViewModel(@NonNull Application application)
    {
        super(application);
    }

    public MutableLiveData<String> renewalSearchKeyword(String keyword, String page, String category, String local, String age, String provideType)
    {
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        sharedPreferences = getApplication().getApplicationContext().getSharedPreferences("app_pref", 0);
        helper = new DBOpenHelper(getApplication().getApplicationContext());
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        String session = sharedPreferences.getString("sessionId", "");

        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.renewalKeywordSearch(keyword, page, sqlite_token, session, category, local, age, provideType, "search")
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            data.setValue(response.body());
                        }
                        else
                        {
                            Log.e(TAG, "실패 : " + response.body());
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
