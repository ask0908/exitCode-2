package com.psj.welfare.Test;

public class RetrofitConnection
{
    String URL = "YOUR_SERVER_URL";


    private String authToken;

    public RetrofitConnection(String authToken) {
        this.authToken = authToken;
    }


//    OkHttpClient client = new OkHttpClient.Builder()
//            .addInterceptor(new Interceptor() {
//                @Override
//                public Response intercept(Chain chain) throws IOException
//                {
//                    Request newRequest  = chain.request().newBuilder()
//                            .addHeader("key", authToken)
//                            .build();
//                    return chain.proceed(newRequest);
//                }
//            }).build();
//
//    Retrofit retrofit = new Retrofit.Builder()
//            .client(client)
//            .baseUrl(URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//    RetrofitInterface server = retrofit.create(RetrofitInterface.class);
}
