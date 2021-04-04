package com.psj.welfare.util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/* 레트로핏 메서드 요청, 응답값을 확인할 때 사용하는 클래스 */
public class UserAgentInterceptor implements Interceptor
{
    private final String userAgent;

    public UserAgentInterceptor(String userAgent)
    {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException
    {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", userAgent)
                .build();
        return chain.proceed(requestWithUserAgent);
    }
}
