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
	 * LoginActivity에서 구글 로그인 시 로그인할 때 어떤 플랫폼으로 로그인하는지 등의 정보를 서버로 보내 저장하는 메서드
	 * 카톡 회원탈퇴 처리 후 테스트 결과 정상 작동 확인
	 * @param userPlatform - GOOGLE 문자열을 서버로 보낸다
	 * @param userEmail - 구글 로그인할 때 사용하는 이메일
	 * @param userName - 유저 이름
	 * @param userToken - 구글 로그인 시 발급되는 토큰
	 * @return - 구글 로그인 성공 여부
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
	 * 서버로 유저 이메일, 구글 계정 토큰을 보내 일치하면 로그인시킴. 현재 쓰이지 않음
	 * @param userEmail - 유저 이메일
	 * @param userToken - 구글 계정 토큰
	 * @return - 구글 로그인을 통한 재로그인 성공 여부
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_login.php")
	Call<String> ReUser(
			@Field("userEmail") String userEmail,
			@Field("userToken") String userToken
	);

	/**
	 * 서버에 저장된 토큰, 이메일과 클라이언트가 가진 토큰, 이메일이 일치하는지 확인하는 메서드
	 * 앱 자체 회원가입 기능이 없어 현재 사용되지 않음
	 * @param userEmail - 유저 이메일
	 * @param userToken - 유저가 가진 토큰
	 * @return
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_token_check.php")
	Call<String> tokenCheck(
			@Field("userEmail") String userEmail,
			@Field("localToken") String userToken
	);

	/**
	 * 선택한 혜택의 상세정보들을 서버에서 가져오는 메서드
	 * DetailBenefitActivity에서 사용
	 * @param detail : 정책 제목
	 * @return - 정책 내용들을 서버에서 리턴값으로 받는다. 받은 값들은 클라에서 파싱해 사용한다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_detail.php")
	Call<String> detailData(
			@Field("be_name") String detail,
			@Field("email") String email
	);

	/**
	 * 복지 혜택 결과창(ResultBenefitActivity)에서 사용자가 선택한 관심사, 혜택 제목을 서버에서 가져오는 메서드
	 * @param mainFavor - 사용자가 선택한 정책명
	 * @return - 사용자가 선택한 관심사에 속한 혜택 제목들. JSON 형태의 문자열 형태로 오기 때문에 파싱해서 사용한다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_category_result.php")
	Call<String> mainFavor(
			@Field("reqBody") String mainFavor
	);

	/**
	 * 사용자가 입력한 키워드와 일치하는 정책을 서버에서 찾아 가져오는 메서드
	 * 현재 작동하지 않음
	 * @param search - 정책 이름
	 * @return - 검색한 키워드에 속하는 정책 제목들을 서버에서 리턴값으로 받는다.
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_search.php")
	Call<String> search(
			@Field("reqBody") String search
	);

	/**
	 * 1번째로 선택하는 관심사
	 * FirstCategory에서 사용
	 * @param category - 유저가 선택한 관심사
	 * @param level - 1~3단계 중 현재 단계(1)
	 * @return - 선택한 관심사에 속하는 2단계 제목들
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_level_select.php")
	Call<String> category1(
			@Field("category1_name") String category,
			@Field("level") String level
	);

	/**
	 * 2번째로 선택하는 관심사. 1번 관심사에서 선택한 것에 따라 보여지는 게 다르다
	 * SecondCategory에서 사용, 현재 서버에서 넘어오는 값이 없음
	 * @param category_first - 1번째로 선택한 관심사
	 * @param category_second - 2번째로 선택한 관심사
	 * @param level - 1~3단계 중 현재 단계(2)
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
	 * ThirdCategory에서 사용, 현재 서버에서 넘어오는 값이 없음
	 * @param category_first - 1번째로 선택한 관심사
	 * @param category_second - 2번째로 선택한 관심사
	 * @param category_third - 3번째(마지막)로 선택한 관심사
	 * @param level - 1~3단계 중 현재 단계(3)
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
	 * LoginActivity에서 사용
	 * @param userEmail - 유저의 이메일
	 * @param fcm_token - fcm 토큰
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
	 * Compatibility_FirstActivity에서 사용, 현재 문제와 선택지가 나오지 않음
	 * @param problemIndex : 문제 번호, 1번째 테스트에서 가져올 거니까 1을 넣어야 한다
	 * @return JSON 형태의 문자열 꼴로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getFirstProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 서버에서 2번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * Compatibility_SecondActivity에서 사용
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
	 * Compatibility_ThirdActivity에서 사용
	 * @param problemIndex : 문제 번호, 3번째 테스트에서 가져올 거니까 3를 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getThirdProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 서버에서 4번 문제와 이미지, 버튼에 넣을 텍스트들을 가져오는 메서드
	 * Compatibility_FourthActivity에서 사용, 현재 문제와 선택지가 나오지 않음
	 * @param problemIndex : 문제 번호, 4번째 테스트에서 가져올 거니까 4를 넣어야 한다
	 * @return JSON 형태의 문자열로 리턴값을 받는다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_contents.php")
	Call<String> getFourthProblem(
			@Field("problemIndex") String problemIndex
	);

	/**
	 * 테스트 결과 화면에 뿌릴 텍스트들을 서버에서 가져오는 메서드. 유저가 진행한 결과 출력된 나라 이름을 서버로 넘겨, 그 나라에 해당하는 이미지 / 문자열들을
	 * 가져와 뷰에 부려준다
	 * Compatibility_ResultActivity에서 사용
	 * @param resultCountry - 유저의 mbti 테스트 결과 나온 나라 이름
	 * @return JSON 형태의 문자열로 리턴값을 받고 파싱해서 사용한다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_snack_result.php")
	Call<String> getFirstResult(
			@Field("resultCountry") String resultCountry
	);

	/**
	 * 질문지 클릭 시 서버로 키워드 2개와 유저 이메일 주소를 보내는 메서드
	 * PushQuestionActivity에서 사용, 현재 서버로 전송되지 않음
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
	 * 유저 이메일, 북마크한 정책명을 서버로 보내고, 서버한테는 저장됐는지 안됐는지 확인할 수 있는 텍스트를 받는다
	 * DetailBenefitActivity에서 사용
	 * @param email - 유저 이메일
	 * @param welf_name - 정책명
	 * @return - 저장됐는지 여부를 알리는 텍스트를 리턴받는다. 클라에선 이 값을 받아 성공 / 실패 여부를 확인한다
	 */
	@FormUrlEncoded
	@POST("/backend/android/and_bookmark.php")
	Call<String> getBookmark(
			@Field("email") String email,
			@Field("welf_name") String welf_name
	);

	/**
	 * 서버에 저장된 리뷰 데이터를 가져오는 메서드
	 * 인자로 리뷰를 보고자 하는 정책의 이름, 안드/iOS 중 어떤 타입인지를 명시해서 서버로 넘겨준다
	 * DetailBenefitActivity에서 사용
	 * @param welf_name - 정책명
	 * @param osType - 안드 or iOS 중 어떤 것인가?
	 * @return - JSON 형태의 문자열. 클라에선 파싱한 뒤 for문 안에서 문자열 안의 Key 개수만큼 아이템을 만들어 리사이클러뷰에 set한다
	 */
	@FormUrlEncoded
	@POST("/backend/php/common/review_list.php")
	Call<String> getReview(
			@Field("welf_name") String welf_name,
			@Field("osType") String osType
	);

	/**
	 * 리뷰 작성 화면에서 작성한 제목, 내용 등의 데이터를 서버로 보내 저장하는 메서드
	 * 이미지는 아직 넣지 않는다. 현재 사용되지 않는 메서드
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
	 * 서버로 리뷰 내용, 유저가 첨부한 이미지 등을 보내 저장하는 메서드
	 * 현재 사용되지 않음
	 * @param welf_name - 리뷰를 작성하는 정책의 이름 (리뷰 작성 대상)
	 * @param content - 유저가 작성한 리뷰 내용
	 * @param writer - 글쓴이(=유저)
	 * @param email - 유저 이메일
	 * @param like_count - 좋아요 수
	 * @param bad_count - 싫어요 수
	 * @param star_count - 별점
	 * @param fileName - 서버로 업로드되는 파일 이름
	 * @param file - 서버로 업로드되는 이미지 파일
	 * @return
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
	 * ReviewActivity에서 사용. 현재 카메라로 촬영 후 앨범에 저장한 이미지를 등록할 수 없는 에러 있음(이유 : 용량이 너무 커서)
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
	 * MapActivity, MapDetailActivity에서 사용
	 * @param local - GPS 값을 통해 알아낸 유저의 현재 위치
	 * @param page_number - 1번째 지도 화면, 2번째 지도 화면 구분을 위한 숫자
	 * @return - 1로 요청했으면 전국 지역별 정책 개수, 2로 요청했으면 선택한 지역의 정책 제목들을 받는다. 받은 데이터는 파싱해서 뷰에 뿌려준다
	 */
	@GET("http://www.urbene-fit.com/map")
	Call<String> getNumberOfBenefit(
			@Query("local") String local,
			@Query("page_number") String page_number
	);

}
