package com.psj.welfare.api;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService
{
    private final String TAG = this.getClass().getSimpleName();

    public MyFirebaseInstanceIDService()
    {
    }

    @Override
    public void onNewToken(@NonNull String s)
    {
        super.onNewToken(s);
    }
}
