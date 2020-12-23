package com.psj.welfare.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
//	@FormUrlEncoded
//	@POST("/backend/android/and_category_result.php")
//	Call<String> mainFavor(
//			@Field("reqBody") String mainFavor
//	);

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
	 * LoginActivity에서 사용했지만 지금은 쓰지 않아 주석 처리
	 * @param userEmail - 유저의 이메일
	 * @param fcm_token - fcm 토큰
	 * @return
	 */
//	@FormUrlEncoded
//	@POST("/backend/android/and_fcm_token_save.php")
//	Call<String> fcmToken(
//			@Field("userEmail") String userEmail,
//			@Field("fcm_token") String fcm_token,
//			@Field("osType") String osType
//	);

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

	/* 리뷰 관련 메서드 */
	/**
	 * 서버에 저장된 혜택 리뷰 목록을 조회하는 기능
	 * 인자로 리뷰를 보고자 하는 정책의 이름, 안드/iOS 중 어떤 타입인지를 명시해서 서버로 넘겨준다
	 * DetailBenefitActivity에서 사용
	 * @param type - list
	 * @param welf_id - 혜택 id 정보
	 * @return - {
	 * "Status":"200",
	 * "Message":[
	 * {
	 * "id":3,
	 * "content":"testtest1",
	 * "writer":"17번학생",
	 * "email":null,
	 * "like_count":"0",
	 * "star_count":"0",
	 * "image_url":"https:\/\/www.urbene-fit.com\/images\/reviews\/캡처.PNG",
	 * "create_date":"2020-12-13 17:36:03"
	 * }
	 * ],
	 * "TotalCount":3
	 */
	@GET("review")
	Call<String> getReview(
			@Query("type") String type,
			@Query("welf_id") String welf_id
	);

	/* 레트로핏을 통해 서버로 이미지를 보낼 때 무조건 첫 어노테이션은 @Multipart여야 한다 */
	/**
	 * 이미지, 리뷰 텍스트를 서버로 보내 저장하는 메서드
	 * ReviewActivity에서 사용. 현재 카메라로 촬영 후 앨범에 저장한 이미지를 등록할 수 없는 에러 있음(이유 : 용량이 너무 커서)
	 * 이미지가 없어도 리뷰를 업로드할 수 있어야 한다
	 * @param login_token - 유저의 로그인 토큰
	 * @param welf_id - 혜택 id 정보
	 * @param content - 리뷰 내용 정보
	 * @param imageReqBody - 이미지 파일 정보
	 * @param imageFile - 이미지 파일
	 * @return
	 */
	@Multipart
	@POST("review")
	Call<String> uploadReview(
			@Part("login_token") String login_token,
			@Part("welf_id") int welf_id,
			@Part("content") String content,
			@Part("file") RequestBody imageReqBody,
			@Part MultipartBody.Part imageFile
	);

	/**
	 * 리뷰 수정하는 메서드
	 * ReviewUpdateActivity에서 사용
	 * @param login_token - 유저의 로그인 토큰
	 * @param review_id - 리뷰 id 정보
	 * @param content - 수정한 리뷰 내용
	 * @param imageReqBody - 이미지 파일 정보
	 * @param imageFile - 이미지 파일
	 * @return
	 */
	@Multipart
	@POST("https://www.urbene-fit.com/review")
	Call<String> updateReview(
			@Part("login_token") String login_token,
			@Part("review_id") int review_id,
			@Part("content") String content,
			@Part("file") RequestBody imageReqBody,
			@Part MultipartBody.Part imageFile
	);

	/**
	 * 리뷰 삭제하는 메서드, @DELETE가 먹히지 않아서 @POST로 변경
	 * ReviewUpdateActivity에서 사용
	 * @param login_token - 유저의 로그인 토큰
	 * @param review_id - 리뷰 id 정보
	 * @param type - delete
	 * @return - {
	 * "Status":"200",
	 * "Message":"리뷰 삭제가 완료되었습니다."
	 * }
	 */
//	@DELETE("review/{login_token}/{review_id}")
//	@DELETE("review/login_token={login_token}?review_id={review_id}")
	@FormUrlEncoded
	@POST("review")
	Call<String> deleteReview(
			@Field("login_token") String login_token,
			@Field("review_id") int review_id,
			@Field("type") String type
	);

	/**
	 * MainFragment 하단의 내 주변 혜택 찾기 버튼 클릭 시, 지역별 혜택 개수들을 가져오는 메서드
	 * MapActivity, MapDetailActivity에서 사용
	 * @param local - GPS 값을 통해 알아낸 유저의 현재 위치
	 * @param page_number - 1번째 지도 화면, 2번째 지도 화면 구분을 위한 숫자
	 * @return - 1로 요청했으면 전국 지역별 정책 개수, 2로 요청했으면 선택한 지역의 정책 제목들을 받는다. 받은 데이터는 파싱해서 뷰에 뿌려준다
	 */
	@GET("map")
	Call<String> getNumberOfBenefit(
			@Query("local") String local,
			@Query("page_number") String page_number
	);

	/**
	 * LoginActivity에서 카카오 로그인 시 서버로 OS 이름, 플랫폼, fcm 토큰값, 유저 이메일 정보를 보내 저장하는 메서드
	 * LoginActivity에서 사용
	 * @param osType - android
	 * @param platform - kakao
	 * @param fcm_token - 로그로 확인한 FCM 토큰값
	 * @param email - 유저의 이메일. 카톡 로그인 시 받을 수 있게 처리해야 함
	 * @return - 처리 성공 시 200 + 로그인에 성공하셨습니다 + 토큰값을 JSON 꼴로 리턴받는다. 비밀번호 불일치 시 400, 필수 요청값이 비어있으면 404, DB 연결 오류면 500을 리턴한다
	 */
	@FormUrlEncoded
	@POST("https://www.urbene-fit.com/login")
	Call<String> sendUserTypeAndPlatform(
			@Field("osType") String osType,
			@Field("platform") String platform,
			@Field("fcm_token") String fcm_token,
			@Field("email") String email
	);

	/**
	 * 유저 정보를 보내면 서버에서 질문지와 키워드 정보를 받는 메서드
	 * @param nickname - 유저 닉네임
	 * @param age - 유저 나이
	 * @param gender - 유저 성별
	 * @param city - 유저 거주 도시
	 * @param platform - kakao
	 * @param email - 유저 이메일
	 * @return
	 */
	@FormUrlEncoded
	@POST("https://www.urbene-fit.com/user")
	Call<String> sendKeyword(
			@Field("nickName") String nickname,
			@Field("age") String age,
			@Field("gender") String gender,
			@Field("city") String city,
			@Field("platform") String platform,
			@Field("email") String email
	);

	/**
	 * 로그인 시 서버에서 받는 토큰을 넘겨 푸시 알림 데이터들을 받아오는 메서드
	 * @param login_token - 로그인 시 서버에서 생성되는 토큰
	 * @return - JSON 형태의 혜택 제목, 푸시 제목, 푸시 body, 푸시를 받은 날짜가 문자열 꼴로 나온다
	 */
	@GET("https://www.urbene-fit.com/push")
	Call<String> getPushData(
			@Query("login_token") String login_token
	);

	/**
	 * 내 정보를 클릭했을 때 서버에서 사용자 정보를 조회해서 가져오는 메서드
	 * @param login_token - 로그인 시 서버에서 생성되는 토큰
	 * @return - {"Status":"200","Message":"","is_push":"false"}
	 */
	@GET("https://www.urbene-fit.com/user")
	Call<String> getUserInfo(
			@Query("login_token") String login_token
	);

	/**
	 * 유저가 푸시알림설정 스위치를 on으로 두면 true, off로 두면 false를 서버로 보내 기존 값을 수정해 저장하는 메서드
	 * @param login_token - 로그인 시 서버에서 생성되는 토큰
	 * @param is_push - 스위치의 T/F 값
	 * @return - 알림 설정 값이 같으면 : {"Status":"200","Message":"유저 정보가 동일합니다."}
	 * 			 알림 설정 값이 다르면 : {"Status":"200","Message":"유저 정보 수정이 완료되었습니다.","is_push":"false"}
	 */
	@FormUrlEncoded
	@PUT("https://www.urbene-fit.com/user")
	Call<String> putPushSetting(
			@Field("login_token") String login_token,
			@Field("is_push") String is_push
	);

	/**
	 * 키워드와 일치하는 복지혜택들의 데이터를 서버에서 가져오는 메서드 (혜택 상위 카테고리 검색)
	 * SearchResultActivity에서 사용
	 * @param type - search(키워드 기반 검색 요청을 의미하는 검색 타입)
	 * @param keyword - 유저가 입력한 검색 키워드
	 * @return - {
	 *     "Status": "200",
	 *     "Message": [
	 *         {
	 *             "welf_name": "출산양육지원금",
	 *             "welf_local": "충북",
	 *             "parent_category": "육아·임신",
	 *             "welf_category": "현금 지원",
	 *             "tag": "출산;; 다자녀"
	 *         },..중략..],
	 *         "TotalCount":13
	 *         }
	 */
	@GET("https://www.urbene-fit.com/welf")
	Call<String> searchWelfare(
			@Query("type") String type,
			@Query("keyword") String keyword
	);

	/**
	 * MainFragment에서 사용자가 선택한 카테고리에 속하는 혜택 데이터들을 가져오는 메서드
	 * ResultBenefitActivity에서 사용
	 * @param type - category_search(카테고리 기반 검색 요청을 의미하는 검색 타입)
	 * @param category_keyword - 유저가 선택한 카테고리, 중복 선택 가능하며 구분자는 "|"로 구분함
	 * @return - "중장년·노인|저소득층"으로 검색이 성공한 경우)
	 * 	   "Status": "200",
	 *     "Message": [
	 *         {
	 *             "welf_name": "저소득층 기저귀·조제분유 지원",
	 *             "welf_category": "현물 지원",
	 *             "tag": "저소득층;;장애인;;다자녀;;분유;;육아",
	 *             "welf_local": "전국"
	 *         },...(중략)...],
	 *     "TotalCount": 12
	 */
	@GET("https://www.urbene-fit.com/welf")
	Call<String> searchWelfareCategory(
			@Query("type") String type,
			@Query("keyword") String category_keyword
	);

	/**
	 * 혜택 하위 카테고리 검색. 카테고리에 해당하는 혜택 정보를 검색해 결과를 받아오는 메서드
	 * SearchResultActivity에서 사용
	 * @param type - child_category_search 문자열을 입력한다 (요청 타입), 필수 요청값
	 * @param select_category - 선택한 상위 카테고리 정보, 여러 개를 선택했을 경우 구분자로 '|'를 중간에 넣어서 보낸다
	 * @param welf_category - 선택한 하위 카테고리 정보(현물 지원, 현금 지원 등), 필수 요청값
	 * @param keyword - 키워드 검색 정보
	 * @return -
	 * "Status":"200",
	 * "Message":[
	 * 			{
	 * 				"welf_name":"전문기술인재장학금",
	 * 				"parent_category":"청년",
	 * 				"welf_category":"현금 지원",
	 * 				"tag":"장학생;;학비;;전문대",
	 * 				"welf_local":"전국"
	 * 			},...(중략)...
	 * 			],
	 * "TotalCount":10
	 */
	@GET("https://www.urbene-fit.com/welf")
	Call<String> searchSubCategoryWelfare(
			@Query("type") String type,
			@Query("select_category") String select_category,
			@Query("welf_category") String welf_category,
			@Query("keyword") String keyword
	);

	/**
	 * 사용자 정보(나이, 성별, 지역, 닉네임)를 입력받으면 서버에 저장하는 메서드
	 * GetUserInformationActivity에서 사용됨, 테스트해야 함
	 * @param login_token - 로그인 시 서버에서 생성되는 토큰
	 * @param user_nickname - 유저가 입력한 닉네임
	 * @param age - 유저 나이
	 * @param gender - 유저 성별
	 * @param city - 유저의 거주 지역
	 * @return - {
	 * 				"Status":"200",
	 * 				"Message":[
	 * 						{
	 * 						"age_group":"10대",
	 * 						"gender":"여자",
	 * 						"interest":"10대,가출,검정고시,통신비,자퇴,퇴학,중학생,고등학생,소년소녀가정,조손가정,한부모가족,가정위탁,성범죄"
	 * 						}
	 * 						]
	 * 				}
	 */
	@FormUrlEncoded
	@POST("https://www.urbene-fit.com/user")
	Call<String> registerUserInfo(
			@Field("login_token") String login_token,
			@Field("nickName") String user_nickname,
			@Field("age") String age,
			@Field("gender") String gender,
			@Field("city") String city
	);

	/**
	 * 사용자가 관심있는 관심사를 등록하는 기능
	 * ChoiceKeywordActivity에서 사용됨
	 * @param login_token - 로그인 시 서버에서 생성되는 토큰
	 * @param type - 요청 타입(interest)
	 * @param interest - 사용자의 관심사 정보(10대,가출,검정고시,통신비,자퇴,...)
	 * @return - {
	 * 			"Status":"200",
	 * 			"Message":"사용자 관심사 등록이 완료되었습니다."
	 * 			}
	 */
	@FormUrlEncoded
	@PUT("https://www.urbene-fit.com/user")
	Call<String> registerUserInterest(
			@Field("login_token") String login_token,
			@Field("type") String type,
			@Field("interest") String interest
	);

	/**
	 * 유튜브 영상 정보를 받아오는 메서드
	 * @return - {
	 * 			"Status":"200",
	 * 			"Message":[
	 * 			{
	 * 			"thumbnail":"https://i.ytimg.com/vi/V16hJ4ACn_A/default.jpg",
	 * 			"videoId":"V16hJ4ACn_A",
	 * 			"title":"잘 알려지지 않은 3가지 혜택! 꼭 받으세요~"
	 * 			},...(중략)...
	 * 			"TotalCount":6
	 */
	@GET("https://www.urbene-fit.com/youtube")
	Call<String> getYoutubeInformation();

	/**
	 * 사용자 관심사에 따라 관련된 혜택을 보여주는 기능
	 * 어디서 사용할지는 미정
	 * @param login_token - 로그인 시 서버에서 받는 토큰값
	 * @param type - customized 고정
	 * @return -
	 * "Status":"200",
	 * "Message":[
	 * 			{
	 * 				"welf_name":"장애아동수당지원",
	 * 				"welf_local":"전북",
	 * 				"welf_category":"현금 지원",
	 * 				"tag":"장애인;; 아동"
	 * 			},...
	 * 			],
	 * 				"TotalCount":10
	 */
	@GET("https://www.urbene-fit.com/welf")
	Call<String> userOrderedWelfare(
			@Query("login_token") String login_token,
			@Query("type") String type
	);

}
