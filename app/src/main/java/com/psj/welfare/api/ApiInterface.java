package com.psj.welfare.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/* 레트로핏에 사용되는 메서드들을 모아놓은 인터페이스
* 언제 어떤 걸 호출하고 어떤 걸 요청하는가? */
public interface ApiInterface
{
	/**
	 * LoginActivity에서 구글 로그인 시 로그인할 때 어떤 로그인 api를 쓰는지 등의 정보를 서버로 보내 저장하는 메서드
	 * @param userPlatform - GOOGLE 문자열을 서버로 보낸다
	 * @param userEmail - 구글 로그인할 때 사용하는 이메일
	 * @param userName - 이메일 계정의 이름
	 * @param userToken - 구글 로그인 시 발급되는 토큰
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_register.php")
	Call<String> googleUser(
			@Field("userPlatform") String userPlatform,
			@Field("userEmail") String userEmail,
			@Field("userName") String userName,
			@Field("userToken") String userToken
	);

	/**
	 * 구글 로그인 후 로그아웃 시 다시 로그인을 시도하는 메서드
	 * 서버로 유저 이메일, 구글 계정 토큰을 보내 일치하면 로그인시킨다
	 * @param userEmail - 유저 이메일
	 * @param userToken - 구글 계정 토큰
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_login.php")
	Call<String> ReUser(
			@Field("userEmail") String userEmail,
			@Field("userToken") String userToken
	);

	/**
	 * 서버에 저장된 토큰, 이메일과 클라이언트가 가진 토큰, 이메일이 일치하는지 확인하는 메서드
	 * @param userEmail - 유저 이메일
	 * @param userToken - 유저가 가진 토큰
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_token_check.php")
	Call<String> tokenCheck(@Field("userEmail") String userEmail, @Field("localToken") String userToken);

	/**
	 * 선택한 혜택의 상세정보들을 서버에서 가져오는 메서드
	 * @param detail : 정책 제목
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_detail.php")
	Call<String> detailData(
			@Field("be_name") String detail,
			@Field("email") String email
	);

	/**
	 * 복지 혜택 결과창에서 사용자가 선택한 관심사, 혜택 제목을 서버에서 가져오는 메서드
	 * @param mainFavor - 사용자가 선택한 정책명
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_category_result.php")
	Call<String> mainFavor(
			@Field("reqBody") String mainFavor
	);

	/**
	 * 사용자가 입력한 키워드와 일치하는 정책을 서버에서 찾아 가져오는 메서드
	 * @param search - 정책 이름
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_search.php")
	Call<String> search(
			@Field("reqBody") String search
	);

	/**
	 * 1번째로 선택하는 관심사
	 * @param category
	 * @param level
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category1(
			@Field("category1_name") String category,
			@Field("level") String level
	);

	/**
	 * 2번째로 선택하는 관심사. 1번 관심사에서 선택한 것에 따라 보여지는 게 다르다
	 * @param category_first
	 * @param category_second
	 * @param level
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category2(
			@Field("category1_name") String category_first,
			@Field("category2_name") String category_second,
			@Field("level") String level
	);

	/**
	 * 3번째로 선택하는 관심사. 2번 관심사에서 선택한 것에 따라 보여지는 게 다르다
	 * @param category_first
	 * @param category_second
	 * @param category_third
	 * @param level
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category3(
			@Field("category1_name") String category_first,
			@Field("category2_name") String category_second,
			@Field("category3_name") String category_third,
			@Field("level") String level
	);

	/**
	 * 유저 이메일과 토큰을 서버에 저장하는 메서드
	 * @param userEmail
	 * @param fcm_token
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_fcm_token_save.php")
	Call<String> fcmToken(
			@Field("userEmail") String userEmail,
			@Field("fcm_token") String fcm_token,
			@Field("osType") String osType
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

	/**
	 * 질문지 클릭 시 서버로 키워드 2개와 유저 이메일 주소를 보내는 메서드
	 * @param keyword_1 - 1번째 키워드
	 * @param keyword_2 - 2번째 키워드
	 * @param email - 유저 이메일
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/php/common/benefit_info_register.php")
	Call<String> pushQuestion(
			@Field("keyword_1") String keyword_1,
			@Field("keyword_2") String keyword_2,
			@Field("email") String email
	);

	/**
	 * 푸시를 눌렀을 때 서버로 유저 이메일을 보내는 메서드
	 * 푸시를 누른 시간, 이메일, 상태값을 보내기로 했으나 먼저 이메일만 보낸다
	 * @param email - 유저 이메일
	 * @param welf_name - 정책명
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_bookmark.php")
	Call<String> getBookmark(
			@Field("email") String email,
			@Field("welf_name") String welf_name
	);

	/**
	 * 리뷰 데이터를 가져오는 메서드
	 * 인자로 리뷰를 보고자 하는 정책의 이름, 안드/iOS 중 어떤 타입인지를 명시해서 서버로 넘겨준다
	 * @param welf_name - 정책명
	 * @param osType - 안드 or iOS 중 어떤 것인가?
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/php/common/review_list.php")
	Call<String> getReview(
			@Field("welf_name") String welf_name,
			@Field("osType") String osType
	);

	/**
	 * 리뷰 작성 화면에서 작성한 제목, 내용 등의 데이터를 서버로 보내 저장하는 메서드
	 * 이미지는 아직 넣지 않는다
	 * @param welf_name - 리뷰를 작성하는 정책 이름
	 * @param content - 유저가 작성한 리뷰 내용
	 * @param writer - 유저 이름
	 * @param email - 유저 이메일
	 * @param like_count - 좋아요 수
	 * @param bad_count - 싫어요 수
	 * @param star_count - 별점 수
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/php/common/review_register.php")
	Call<String> sendReview(
			@Field("welf_name") String welf_name,
			@Field("content") String content,
			@Field("writer") String writer,
			@Field("email") String email,
			@Field("like_count") String like_count,
			@Field("bad_count") String bad_count,
			@Field("star_count") String star_count
	);

	/**
	 * 이미지, 텍스트 전송
	 */
	@Multipart
	@POST("/backend/php/common/review_register.php")
	Call<String> sendReviewImage(
			@Field("welf_name") String welf_name,
			@Field("content") String content,
			@Field("writer") String writer,
			@Field("email") String email,
			@Field("like_count") String like_count,
			@Field("bad_count") String bad_count,
			@Field("star_count") String star_count,
			@Part("fileName") RequestBody fileName,
			@Part MultipartBody.Part file
	);

	/**
	 * 이미지, 리뷰 텍스트를 서버로 보내 저장하는 메서드
	 * @param welf_name - 리뷰가 쓰인 혜택 이름
	 * @param content - 리뷰 내용
	 * @param writer - 리뷰 작성자
	 * @param email - 리뷰 작성자의 이메일
	 * @param like_count - 좋아요 수
	 * @param bad_count - 싫어요 수
	 * @param star_count - 별점
	 * @param imageReqBody - 이미지 요청 시 넘겨야 하는 RequestBody
	 * @param imageFile - 리뷰에 첨부한 이미지 파일
	 * @return
	 */
	// part는 filed와 달리 데이터를 직렬화하여 전송한다
	@Multipart
	@POST("/backend/php/common/review_register.php")
	Call<String> uploadReview(
			@Part("welf_name") String welf_name,
			@Part("content") String content,
			@Part("writer") String writer,
			@Part("email") String email,
			@Part("like_count") String like_count,
			@Part("bad_count")  String bad_count,
			@Part("star_count") String star_count,
			@Part("imageFile") RequestBody imageReqBody,
			@Part MultipartBody.Part imageFile
	);

	/**
	 * MainFragment 하단의 내 주변 혜택 찾기 버튼 클릭 시, 지역별 혜택 개수들을 가져오는 메서드
	 * @param local - 지역명(지금은 부산으로 대체)
	 * @param page_number - 1번째 지도 화면, 2번째 지도 화면 구분을 위한 숫자
	 * @return
	 */
	@GET("http://www.urbene-fit.com/map")
	Call<String> getNumberOfBenefit(
			@Query("local") String local,
			@Query("page_number") String page_number
	);

}
