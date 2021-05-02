package com.psj.welfare.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.util.DBOpenHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushRepository
{
    private static final String TAG = PushRepository.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    SharedPreferences sharedPreferences;
    String sqlite_token;

    DBOpenHelper helper;

    public PushRepository(Context context)
    {
        this.mContext = context;
        // 람다 사용 이전에 만들어진 메서드라 getApiClient()를 사용해야 한다. getRetrofit()은 쓰면 안된다
        apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
    }

    public MutableLiveData<String> getPushDatas()
    {
        sharedPreferences = mContext.getSharedPreferences("app_pref", 0);
        helper = new DBOpenHelper(mContext);
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
        apiInterface.getPushData(sqlite_token, session, "pushList")
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            String result = response.body();
                            Log.e(TAG, "레포지토리에서 받은 푸시값 : " + result);
                            data.setValue(result);
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
