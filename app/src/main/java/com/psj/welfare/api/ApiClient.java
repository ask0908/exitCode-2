package com.psj.welfare.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class ApiClient
{
    // 클라이언트가 통신할 서버 URL
//    public static final String BASE_URL = "https://www.urbene-fit.com/";
    public static final String BASE_URL = "https://www.hyemo.com/";
    public static final String SECOND_URL = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/";

    // 사용자에 대한 인증정보를 매 요청마다 서버로 함께 전달해 주어야 하는 경우가 발생하거나,
    // 개발 중 요청과 응답에 대한 로깅을 해야되는 경우가 발생합니다
    // 요청시마다 넣어주는 방법도 있지만, 비효율적이라 Retrofit은 OkHttpClient를 매개변수로 받는 client()를 제공해주고 있습니다
    // OkHttpClient 객체를 생성하여, Header정보에 Token정보를 설정해줄 수 있습니다
//     OkHttpClient client = new OkHttpClient();

    private static Retrofit retrofit;

    /* 람다로 바뀐 후 새 주소를 기반으로 만들어진 메서드를 쓰기 위해 호출해야 하는 메서드 */
    public static Retrofit getRetrofit()
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(SECOND_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getApiClient()
    {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // rest api call 상태를 확인하는 메서드를 인터셉터로 끼워넣는다. 필요없다면 아래의 .client(client) 부분을 지우면 된다
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor())
//                .build();

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(client)
                    .build();
        }
        return retrofit;
    }

    /* rest api call의 상태를 확인하는 메서드 */
    public static HttpLoggingInterceptor httpLoggingInterceptor()
    {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger()
        {
            @Override
            public void log(String message)
            {
                Log.e("HttpLoggingInterceptor : ", message + "");
            }
        });

        return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

}
