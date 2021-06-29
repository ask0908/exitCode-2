package com.psj.welfare.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 인터넷 연결 상태를 확인하는 static 메서드가 있는 클래스
 * 참고 : https://youngest-programming.tistory.com/32
 */
public class NetworkStatus
{
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    public static int checkNetworkStatus(Context context)
    {
        // 해당 context의 서비스를 사용하기 위해서 context 객체를 받는다
        // getSystemService()의 인자로 checkNetworkStatus()의 매개변수로 받은 context 객체를 넘길 수 있지만 static 메서드 안이기 때문에 클래스(Context) 참조를 통해 접근해야 한다
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null)
        {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE)
            {
                // 3G or LTE로 연결
                return TYPE_MOBILE;
            }
            else if (type == ConnectivityManager.TYPE_WIFI)
            {
                // 와이파이 연결
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;  //연결이 되지않은 상태
    }

}
