package com.psj.welfare.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class ApiClient
{
	// 클라이언트가 통신할 서버 URL
//	public static final String BASE_URL = "https://3.34.64.143/";
	public static final String BASE_URL = "https://www.urbene-fit.com/";

	// 사용자에 대한 인증정보를 매 요청마다 서버로 함께 전달해 주어야 하는 경우가 발생하거나,
	// 개발 중 요청과 응답에 대한 로깅을 해야되는 경우가 발생합니다
	// 요청시마다 넣어주는 방법도 있지만, 비효율적이라 Retrofit은 OkHttpClient를 매개변수로 받는
	// Client() 메서드를 제공해주고 있습니다
	// OkHttpClient 객체를 생성하여, Header정보에 Token정보를 설정해줄 수 있습니다
	// OkHttpClient client = new OkHttpClient();

	// 클라이언트 <-> 서버 통신에 필요한 라이브러리 객체 생성
	private static Retrofit retrofit;

	// 레트로핏 통신 기능 (서버 URL, 반환 받을 때 변환 형태)
	public static Retrofit getApiClient()
	{
		Gson gson = new GsonBuilder()
				.setLenient()
				.create();

		if (retrofit == null)
		{
			OkHttpClient okHttpClient = new OkHttpClient.Builder()
					.build();

			retrofit = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.client(okHttpClient)
					.addConverterFactory(ScalarsConverterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(gson))
					.build();
		}
		return retrofit;
	}
}
