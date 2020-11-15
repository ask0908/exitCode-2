package com.psj.welfare.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/* 레트로핏에 사용되는 메서드들을 모아놓은 인터페이스 */
public interface ApiInterface
{

	@FormUrlEncoded
	@POST("/backend/android/and_detail.php")
	Call<JsonObject> detailData(
			@Field("be_name") String detail
	);

	@FormUrlEncoded
	@POST("/backend/android/and_category_result.php")
	Call<String> mainFavor(
			@Field("reqBody") String mainFavor
	);

	@FormUrlEncoded
	@POST("/backend/android/and_search.php")
	Call<String> search(
			@Field("reqBody") String search
	);

	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category1(
			@Field("category1_name") String category,
			@Field("level") String level
	);

	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category2(
			@Field("category1_name") String category_first,
			@Field("category2_name") String category_second,
			@Field("level") String level
	);

	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category3(
			@Field("category1_name") String category_first,
			@Field("category2_name") String category_second,
			@Field("category3_name") String category_third,
			@Field("level") String level
	);

	@FormUrlEncoded
	@POST("/backend/android/and_fcm_token_save.php")
	Call<String> fcmToken(
			@Field("userEmail") String userEmail,
			@Field("fcm_token") String fcm_token
	);

	/**
	 * 서버에서 1번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * @param problemIndex : 문제 번호, 1번째 테스트에서 가져올 거니까 1을 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getFirstProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 서버에서 2번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * @param problemIndex : 문제 번호, 2번째 테스트에서 가져올 거니까 2를 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getSecondProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 서버에서 3번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * @param problemIndex : 문제 번호, 3번째 테스트에서 가져올 거니까 3를 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getThirdProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 서버에서 3번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * @param problemIndex : 문제 번호, 3번째 테스트에서 가져올 거니까 3를 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getFourthProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 테스트 결과 화면에 뿌릴 텍스트들을 서버에서 가져오는 메서드
	 * @param resultCountry : 정확히 뭔지는 모르겠다. 1번 문제니까 1번 문제의 결과 화면을 나타내는 숫자같다. String이다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_result.php")
	Call<String> getFirstResult(
			@Field("resultCountry") String resultCountry
	);

}
