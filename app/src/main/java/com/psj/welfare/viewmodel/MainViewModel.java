package com.psj.welfare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.repository.MainRepository;

/* 뷰모델의 유일한 책임 : UI 데이터를 관리하는 것. 뷰 계층 구조에 액세스하거나 액티비티/프래그먼트에 대한 참조 변수를 가져선 안 된다
* 참고 : https://medium.com/teachmind/necessity-of-viewmodel-and-difference-between-mutablelivedata-and-mediatorlivedata-f1c30df27232 */
public class MainViewModel extends AndroidViewModel
{
    public final String TAG = MainViewModel.class.getSimpleName();
    private MainRepository mainRepository;
    private MutableLiveData<String> mainLiveData;

    public MainViewModel(@NonNull Application application)
    {
        super(application);

        mainRepository = new MainRepository();
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

}
