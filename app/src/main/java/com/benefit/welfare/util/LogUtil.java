package com.benefit.welfare.util;

import android.os.Build;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class LogUtil
{
    // ip 주소, 안드로이드 버전, 액티비티/프래그먼트에 들어온 시간 및 나간 시간, 컨텐츠를 끝까지 소비했는지 여부, 어디에서 어디로 갔는지(페이지명으로 기록)
    // 구글 애널리틱스 사용도 고려

    /* ip 주소를 확인하는 메서드. 핸드폰에 연결된 와이파이의 주소를 출력하지만 와이파이 연결이 안 돼 있으면 192.0.0.4가 나온다 */
    public static String getIp()
    {
        String ip = "";
        // 디바이스에 있는 모든 네트워크를 반복
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
            {
                NetworkInterface intf = en.nextElement();

                //네트워크 중에서 IP가 할당된 넘들에 대해서 뺑뺑이를 한 번 더 돕니다.
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); )
                {

                    InetAddress inetAddress = enumIpAddr.nextElement();

                    //네트워크에는 항상 Localhost 즉, 루프백(LoopBack)주소가 있으며, 우리가 원하는 것이 아닙니다.
                    //IP는 IPv6와 IPv4가 있습니다.
                    //IPv6의 형태 : fe80::64b9::c8dd:7003
                    //IPv4의 형태 : 123.234.123.123
                    //어떻게 나오는지는 찍어보세요.
                    if (inetAddress.isLoopbackAddress())
                    {
                        Log.e("IPAddress", intf.getDisplayName() + "(loopback) | " + inetAddress.getHostAddress());
                    }
                    else
                    {
                        Log.e("IPAddress", intf.getDisplayName() + " | " + inetAddress.getHostAddress());
                    }

                    //루프백이 아니고, IPv4가 맞다면 리턴
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                    {
                        ip = inetAddress.getHostAddress();
                        return ip;
                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return ip;
    }

    /* 안드로이드 버전을 확인해 리턴하는 메서드 */
    public static String getVersion()
    {
        int sdk_ver = Build.VERSION.SDK_INT;
        return String.valueOf(sdk_ver);
    }

    public static String getDeviceName()
    {
        return Build.MODEL;
    }

    /* android|SM-543N|30 형태로 로그를 출력하는 메서드 */
    public static String getUserLog()
    {
        return "android|" + getDeviceName() + "|" + getVersion();
    }

}
