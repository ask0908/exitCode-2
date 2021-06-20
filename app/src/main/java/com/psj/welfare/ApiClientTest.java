package com.psj.welfare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class ApiClientTest
{
    // 클라이언트가 통신할 서버 URL

    // 사용자에 대한 인증정보를 매 요청마다 서버로 함께 전달해 주어야 하는 경우가 발생하거나,
    // 개발 중 요청과 응답에 대한 로깅을 해야되는 경우가 발생합니다
    // 요청시마다 넣어주는 방법도 있지만, 비효율적이라 Retrofit은 OkHttpClient를 매개변수로 받는 client()를 제공해주고 있습니다
    // OkHttpClient 객체를 생성하여, Header정보에 Token정보를 설정해줄 수 있습니다
//     OkHttpClient client = new OkHttpClient();

    private static Retrofit retrofit;

    public static Retrofit ApiClient(String url)
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit ApiClient()
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
