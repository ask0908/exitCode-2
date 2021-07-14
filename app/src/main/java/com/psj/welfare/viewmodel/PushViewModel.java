package com.psj.welfare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.repository.PushRepository;

/* AllPushFragment에서 호출하는 푸시 알림 목록 가져오기 메서드를 가진 뷰모델
*  */
public class PushViewModel extends AndroidViewModel
{
    private final String TAG = PushViewModel.class.getSimpleName();
    private PushRepository pushRepository;
    private MutableLiveData<String> pushLiveData;

    public PushViewModel(@NonNull Application application)
    {
        super(application);

        // getActivity()로 프래그먼트의 context를 가져와야 하지만 방법을 찾지 못해서 아래 방법으로 context 가져옴
        pushRepository = new PushRepository(getApplication().getApplicationContext());
        this.pushLiveData = pushRepository.getMyPush();
    }

    public MutableLiveData<String> getMyPush()
    {
        return pushLiveData;
    }

}
