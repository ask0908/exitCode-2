package com.psj.welfare.api;

import androidx.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService
{
    private final String TAG = "MyFirebaseInstanceIDService";
    private static final String SUBSCRIBE_TO = "urbenefit";

    public MyFirebaseInstanceIDService()
    {
    }

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
        String token = FirebaseInstanceId.getInstance().getToken();
    }
}
