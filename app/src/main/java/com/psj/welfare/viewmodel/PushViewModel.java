package com.psj.welfare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.psj.welfare.repository.PushRepository;

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
        this.pushLiveData = pushRepository.getPushDatas();
    }

    public MutableLiveData<String> getPushDatas()
    {
        return pushLiveData;
    }

}
