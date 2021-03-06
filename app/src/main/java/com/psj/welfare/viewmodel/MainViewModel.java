package com.psj.welfare.viewmodel;

import android.app.Application;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.repository.MainRepository;
import com.psj.welfare.util.DBOpenHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 뷰모델의 유일한 책임 : UI 데이터를 관리하는 것. 뷰 계층 구조에 액세스하거나 액티비티/프래그먼트에 대한 참조 변수를 가져선 안 된다
* 참고 : https://medium.com/teachmind/necessity-of-viewmodel-and-difference-between-mutablelivedata-and-mediatorlivedata-f1c30df27232 */
public class MainViewModel extends AndroidViewModel
{
    public final String TAG = MainViewModel.class.getSimpleName();
    private MainRepository mainRepository;
    private MutableLiveData<String> mainLiveData;

    private ApiInterface apiInterface;
    DBOpenHelper helper;
    String sqlite_token;

    public MainViewModel(@NonNull Application application)
    {
        super(application);

        mainRepository = new MainRepository();
        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        helper = new DBOpenHelper(application.getApplicationContext());
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
        this.mainLiveData = mainRepository.getAllDatas();
    }

    /* getAllData()를 호출하면 새로 업데이트된 LiveData를 리턴한다
    * 따라서 UI는 기존 LiveData를 등록 해제(unregister)한 후, 새 LiveData 객체에 등록(register)해야 한다
    * 이는 UI가 재생성되는 경우에도 해당한다. final을 사용할 수 있지만 이 경우 로직이 더 꼬여질 수 있단 생각이 들어 사용하지 않았다
    * 참고 : https://tourspace.tistory.com/25 */
    public MutableLiveData<String> getAllData()
    {
        return mainLiveData;
    }

    /* 관심사 o, 로그인 x 유저에게 보여줄 전체 혜택 리스트와 유튜브 데이터를 보여주는 기능 */
    public MutableLiveData<String> showDataForNotLoginAndChoseInterest(String age,
                                                                 String gender,
                                                                 String local,
                                                                 String type)
    {
        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.showDataForNotLoginAndChoseInterest(age, gender, local, type)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            String result = response.body();
                            Log.e(TAG, "서버에서 가져온 데이터 : " + result);
                            data.setValue(result);
                        }
                        else
                        {
                            Log.e(TAG, "관심사 선택한 유저에게 보여줄 데이터 가져오기 실패 : " + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "관심사 선택한 유저에게 보여줄 데이터 가져오기 에러 : " + t.getMessage());
                    }
                });
        return data;
    }

    public MutableLiveData<String> showWelfareAndYoutubeLogin(String logintoken, String type)
    {
        final MutableLiveData<String> data = new MutableLiveData<>();
        apiInterface.showWelfareAndYoutubeLogin(logintoken,type)
                .enqueue(new Callback<String>()
                {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response)
                    {
                        if (response.isSuccessful() && response.body() != null)
                        {
                            String result = response.body();
                            data.setValue(result);
                        }
                        else
                        {
                            Log.e(TAG, "뷰모델에서 로그인 시 데이터 가져오기 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t)
                    {
                        Log.e(TAG, "뷰모델에서 로그인 시 데이터 가져오기 에러 : " + t.getMessage());
                    }
                });
        return data;
    }

}
