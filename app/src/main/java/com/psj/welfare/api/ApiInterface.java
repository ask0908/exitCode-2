package com.psj.welfare.api;

import androidx.annotation.Nullable;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/* 레트로핏에 사용되는 메서드들을 모아놓은 인터페이스
 * 언제 어떤 걸 호출하고 어떤 걸 요청하는가? */
public interface ApiInterface
{
    /* ↓ 리뷰 관련 메서드 */
    // ===================================================================================================

    /**
     * 서버에 저장된 혜택 리뷰 목록을 조회하는 기능
     * 인자로 리뷰를 보고자 하는 정책의 이름, 안드/iOS 중 어떤 타입인지를 명시해서 서버로 넘겨준다
     * DetailBenefitActivity에서 사용
     *
     * @param type    - "list" 고정
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
     * "image_url":"https:\/\/www.hyemo.com\/images\/reviews\/캡처.PNG",
     * "create_date":"2020-12-13 17:36:03"
     * }
     * ],
     * "TotalCount":3
     */
    @GET("https://www.hyemo.com/review")
    Call<String> getReview(
            @Header("LoginToken") String token,
            @Header("SessionId") String sessionId,
            @Query("type") String type,
            @Query("welf_id") String welf_id
    );

    /* 레트로핏을 통해 서버로 이미지를 보낼 때 무조건 첫 어노테이션은 @Multipart여야 한다 */

    /**
     * 이미지, 리뷰 텍스트를 서버로 보내 저장하는 메서드
     * ReviewActivity에서 사용. 현재 카메라로 촬영 후 앨범에 저장한 이미지를 등록할 수 없는 에러 있음(이유 : 용량이 너무 커서)
     * 이미지가 없어도 리뷰를 업로드할 수 있어야 한다
     *
     * @param login_token      - 유저의 로그인 토큰
     * @param welf_id          - 혜택 id 정보
     * @param content          - 리뷰 내용 정보
     * @param imageReqBody     - 이미지 파일 정보
     * @param imageFile        - 이미지 파일
     * @param difficulty_level - 쉬워요, 어려워요
     * @param satisfaction     - 도움이 됐어요, 도움이 안 됐어요
     * @param star_count       - 1~5 사이 정수
     * @return
     */
    @Multipart
    @POST("https://www.hyemo.com/review")
    Call<String> uploadReview(
            @Part("login_token") String login_token,
            @Part("welf_id") int welf_id,
            @Part("content") String content,
            @Part("file") RequestBody imageReqBody,
            @Part MultipartBody.Part imageFile,
            @Part("difficulty_level") String difficulty_level,
            @Part("satisfaction") String satisfaction,
            @Part("star_count") String star_count
    );

    /**
     * 리뷰 수정하는 메서드
     * ReviewUpdateActivity에서 사용
     *
     * @param login_token  - 유저의 로그인 토큰
     * @param review_id    - 리뷰 id 정보
     * @param content      - 수정한 리뷰 내용
     * @param imageReqBody - 이미지 파일 정보
     * @param imageFile    - 이미지 파일
     * @return
     */
    @Multipart
    @POST("https://www.hyemo.com/review")
    Call<String> updateReview(
            @Part("login_token") String login_token,
            @Part("review_id") int review_id,
            @Part("content") String content,
            @Part("file") RequestBody imageReqBody,
            @Part MultipartBody.Part imageFile,
            @Part("difficulty_level") String difficulty_level,
            @Part("satisfaction") String satisfaction,
            @Part("star_count") String star_count
    );

    /**
     * 리뷰 삭제하는 메서드, @DELETE가 먹히지 않아서 @POST로 변경
     * ReviewUpdateActivity에서 사용
     *
     * @param login_token - 유저의 로그인 토큰
     * @param review_id   - 리뷰 id 정보
     * @param type        - delete
     * @return - {
     * "Status":"200",
     * "Message":"리뷰 삭제가 완료되었습니다."
     * }
     */
//   @DELETE("review/{login_token}/{review_id}")
//   @DELETE("review/login_token={login_token}?review_id={review_id}")
    @FormUrlEncoded
    @POST("https://www.hyemo.com/review")
    Call<String> deleteReview(
            @Field("login_token") String login_token,
            @Field("review_id") int review_id,
            @Field("type") String type
    );


    /* ↓ 지도 관련 메서드 */
    // ===================================================================================================

    /**
     * MainFragment 하단의 내 주변 혜택 찾기 버튼 클릭 시, 지역별 혜택 개수들을 가져오는 메서드
     * MapActivity, MapDetailActivity에서 사용
     *
     * @param token       - 로그인 시 서버에서 받는 토큰값
     * @param sessionId   - 쉐어드에 저장된 세션 id
     * @param local       - GPS 값을 통해 알아낸 유저의 현재 위치
     * @param page_number - 1번째 지도 화면, 2번째 지도 화면 구분을 위한 숫자
     * @return
     */
    @GET("https://www.hyemo.com/map")
    Call<String> getNumberOfBenefit(
            @Header("LoginToken") String token,
            @Header("SessionId") String sessionId,
            @Query("local") String local,
            @Query("page_number") String page_number
    );


    /* ↓ 로그인 메서드 */
    // ===================================================================================================
    @FormUrlEncoded
    @POST("https://www.hyemo.com/login")
    Call<String> sendUserTypeAndPlatform(
            @Field("email") String email,
            @Field("fcm_token") String fcm_token,
            @Field("osType") String osType,
            @Field("platform") String platform
    );

    //로그인 메서드 장고 버전
    @POST("https://www.hyemo.com/django/login")
    Call<String> Login(
            @Body String Login
    );




//    /**
//     * LoginActivity에서 카카오 로그인 시 서버로 OS 이름, 플랫폼, fcm 토큰값, 유저 이메일 정보를 보내 저장하는 메서드
//     * 장고 사용으로 인해 어노테이션, 매개변수 변경됨
//     * @param login_information - email, fcm_token, osType, platform 값들을 JSON으로 묶어 toString()한 것
//     * @return
//     */
//    @POST("http://www.hyemo.com:8080/login/login_request")
//    Call<String> sendUserTypeAndPlatform(
//            @Body String login_information
//    );

    /* ↓ 푸시 알림 관련 메서드 */
    // ===================================================================================================

    @GET("https://www.hyemo.com/django/push/users")
    Call<String> getMyPush(
            @Header("logintoken") String token
    );

    /**
     * 로그인 시 서버에서 받는 토큰을 넘겨 푸시 알림 데이터들을 받아오는 메서드
     * AllPushFragment에서 사용
     *
     * @param token   - 로그인 시 서버에서 생성되는 토큰
     * @param session - 쉐어드에 저장된 세션 id
     * @param type    - "pushList" 고정
     * @return - JSON 형태의 혜택 제목, 푸시 제목, 푸시 body, 푸시를 받은 날짜가 문자열 꼴로 나온다
     */
    @GET("https://www.hyemo.com/push")
    Call<String> getPushData(
            @Header("LoginToken") String token,
            @Header("SessionId") String session,
            @Query("type") String type
    );

//    /* Observable<Call> 테스트 */
//    @GET("https://www.hyemo.com/push")
//    Flowable<Call<String>> gotPushData(
//            @Header("LoginToken") String token,
//            @Header("SessionId") String session,
//            @Query("type") String type
//    );

    /**
     * 사용자가 알림을 수신받을 경우 수신 상태값 변경하는 메서드
     * MyFirebaseMessagingService에서 사용
     *
     * @param login_token - 로그인 시 서버에서 생성되는 토큰
     * @param type        - "customizedRecv" 고정
     * @return
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/push")
    Call<String> changePushStatus(
            @Field("LoginToken") String login_token,
            @Field("type") String type
    );

    /**
     * 유저가 알림을 확인했을 때 수신 상태값을 바꾸는 메서드
     *
     * @param session - 세션 id
     * @param token   - 로그인 시 서버에서 받는 토큰
     * @param pushId  - 서버에서 알림 보내면 받는 것 같은데 이걸 넣어서 보내야 하는 듯
     * @param type    - "pushRecv" 고정
     * @return - {"Status":"200","Message":"알림 상태 정보 수정이 완료되었습니다."}
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/push")
    Call<String> checkUserWatchedPush(
            @Header("SessionId") String session,
            @Header("LoginToken") String token,
            @Field("pushId") String pushId,
            @Field("type") String type
    );

    /**
     * 사용자가 알림 클릭해서 수신받을 경우 그 알림의 수신 상태값을 바꾸는 메서드
     * @param token - 로그인 후 서버에서 받은 토큰 (헤더에 저장)
     * @param push_id - 수신 상태값 바꾸려는 푸시 알림의 PK 값, body에 JSON 형태로 넣어야 한다 = {"push_id":2}
     * @return -
     * {
     *     "status_code": 200,
     *     "message": "알림 수신 상태가 변경되었습니다."
     * }
     */
    @POST("https://www.hyemo.com/django/push/users")
    Call<String> changePushStatusWhenClicked(
            @Header("logintoken") String token,
            @Body String push_id
    );

    /**
     * 알림 삭제, 사용자가 알림을 삭제할 경우 is_remove 컬럼값을 true로 바꾸는 메서드
     * PushGatherFragment에서 사용
     *
     * @param session - 세션 아이디(헤더로 보내야 함)
     * @param token   - 로그인 시 서버에서 받는 토큰(헤더로 보내야 함)
     * @param pushId  - 알림 아이디
     * @param type    - "delete" 고정
     * @return - {"Status":"200","Message":"알림 삭제가 완료되었습니다."}
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/push")
    Call<String> removePush(
            @Header("SessionId") String session,
            @Header("LoginToken") String token,
            @Field("pushId") String pushId,
            @Field("type") String type
    );
    // ===================================================================================================

    /* ↓ 사용자 정보(나이, 성별, 지역, 닉네임) 관련 메서드 */
    // ===================================================================================================

    /**
     * 내 정보를 클릭했을 때 서버에서 사용자 정보를 조회해서 가져오는 메서드
     * MyPageFragment에서 사용
     *
     * @param login_token - 로그인 시 서버에서 생성되는 토큰
     * @return - {"Status":"200","Message":"","is_push":"false"}
     */
    @GET("https://www.hyemo.com/user")
    Call<String> getUserInfo(
            @Header("SessionId") String sessionId,
            @Header("Action") String action,
            @Query("login_token") String login_token
    );

    /**
     * 유저가 푸시알림설정 스위치를 on으로 두면 true, off로 두면 false를 서버로 보내 기존 값을 수정해 저장하는 메서드
     * MyPageFragment에서 사용
     *
     * @param login_token - 로그인 시 서버에서 생성되는 토큰
     * @param type        - "push" 고정
     * @param is_push     - 스위치의 T/F 값
     * @return - 알림 설정 값이 같으면 : {"Status":"200","Message":"유저 정보가 동일합니다."}
     * 알림 설정 값이 다르면 : {"Status":"200","Message":"유저 정보 수정이 완료되었습니다.","is_push":"false"}
     */
    @FormUrlEncoded
    @PUT("https://www.hyemo.com/user")
    Call<String> putPushSetting(
            @Header("SessionId") String sessionId,
            @Header("Action") String action,
            @Field("login_token") String login_token,
            @Field("type") String type,
            @Field("is_push") String is_push
    );

    /**
     * 사용자 정보(나이, 성별, 지역, 닉네임)를 입력받으면 서버에 저장하는 메서드
     * GetUserInformationActivity에서 사용됨, 테스트해야 함
     *
     * @param login_token   - 로그인 시 서버에서 생성되는 토큰 (필수)
     * @param user_nickname - 유저가 입력한 닉네임 (필수)
     * @param age           - 유저 나이
     * @param gender        - 유저 성별(남자, 여자)
     * @param city          - 유저의 거주 지역
     * @return - {
     * "Status":"200",
     * "Message":[
     * {
     * "age_group":"10대",
     * "gender":"여자",
     * "interest":"10대,가출,검정고시,통신비,자퇴,퇴학,중학생,고등학생,소년소녀가정,조손가정,한부모가족,가정위탁,성범죄"
     * }
     * ]
     * }
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/user")
    Call<String> registerUserInfo(
//         @Header("SessionId") String sessionId,
//         @Header("Action") String action,
            @Field("login_token") String login_token,
            @Field("nickName") String user_nickname,
            @Field("age") String age,
            @Field("gender") String gender,
            @Field("city") String city
    );

    /**
     * 사용자 정보 조회, 유저 닉네임 / 생년월일 / 성별 / 지역을 가져오는 메서드
     *
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param type  - "user" 고정
     * @return - {"Status":"200","Message":[{"nickName":"oo","age":"19950312","gender":"여자","city":"서울특별시"}]}
     */
    @GET("https://www.hyemo.com/user")
    Call<String> checkUserInformation(
            @Query("login_token") String token,
            @Query("type") String type
    );

    /**
     * 관심사 리스트 조회, 유저가 관심사 선택 화면에 들어갈 때 보여줄 관심사 리스트를 가져온다
     *
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param type  - "interestList" 고정
     * @return -
     * 정보가 없거나 나이가 '~대'가 아닐 경우 : {"Status":"200","Message":[{"interest":"10대,가출,검정고시,통신비,...,무공훈장"}]}
     * 정보가 있을 경우 : {"Status":"200","Message":[{"gender":"여자","age":"20대","city":"서울특별시","interest":"장학생,학자금,...가정폭력,지체"}]}
     */
    @GET("https://www.hyemo.com/user")
    Call<String> getAllKeyword(
            @Query("login_token") String token,
            @Query("type") String type
    );
    // ===================================================================================================

    /* ↓ 혜택 데이터 조회 관련 메서드 */
    // ===================================================================================================

    /**
     * 아래 인자를 서버로 넘겨서, 해당하는 혜택 정보를 조회하는 메서드 (혜택 정보 조회)
     * DetailBenefitActivity에서 사용
     *
     * @param token       - 로그인 시 서버에서 받는 토큰
     * @param session     - 쉐어드에 저장된 세션 id
     * @param type        - "detail" 고정(혜택 정보 조회)
     * @param local       - 혜택이 제공되는 지역의 이름
     * @param welf_name   - 혜택 이름
     * @return -"Status":"200",
     * "Message":[
     * {
     * "id":7,
     * "welf_name":"저소득층 에너지효율 개선",
     * "welf_target":"기초생활수급가구;; 차상위계층;; 복지사각지대(기초지자체 추천);; 사회복지시설 등\n ※ 주거급여를 지원받는 자가가구는 제외",
     * "welf_contents":"에너지 효율을 높일 수 있도록 노후 주택 에너지 사용 환경 개선\n · 시공지원 : 단열공사;; 창호공사;; 바닥공사 등을 통한 에너지 효율 개선\n · 물품지원 : 고효율 가스·기름 보일러 교체;; 냉방기기 지원",
     * "welf_apply":"읍면동 주민센터에 신청",
     * "welf_contact":"한국에너지재단(☎1670-7653)",
     * "welf_period":"문의처로문의",
     * "welf_end":"사업종료시까지",
     * "welf_local":"전국",
     * "isBookmark":"false"
     * }
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> getWelfareInformation(
            @Header("LoginToken") String token,
            @Header("SessionId") String session,
            @Query("type") String type,
            @Query("local") String local,
            @Query("welf_name") String welf_name
    );

    /**
     * 키워드와 일치하는 복지혜택들의 데이터를 서버에서 가져오는 메서드 (혜택 상위 카테고리 검색)
     * SearchResultActivity에서 사용
     *
     * @param type    - search(키워드 기반 검색 요청을 의미하는 검색 타입)
     * @param keyword - 유저가 입력한 검색 키워드
     * @return - {
     * "Status": "200",
     * "Message": [
     * {
     * "welf_name": "출산양육지원금",
     * "welf_local": "충북",
     * "parent_category": "육아·임신",
     * "welf_category": "현금 지원",
     * "tag": "출산;; 다자녀"
     * },..중략..],
     * "TotalCount":13
     * }
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> searchWelfare(
            @Header("LoginToken") String token,
            @Header("SessionId") String session,
            @Query("type") String type,
            @Query("keyword") String keyword,
            @Query("city") String city,
            @Query("userAgent") String userAgent
    );

    /**
     * MainFragment에서 사용자가 선택한 카테고리에 속하는 혜택 데이터들을 가져오는 메서드 (혜택 상위 카테고리 검색)
     * ResultBenefitActivity, MapDetailActivity에서 사용
     *
     * @param type             - category_search(카테고리 기반 검색 요청을 의미하는 검색 타입)
     * @param category_keyword - 유저가 선택한 카테고리, 중복 선택 가능하며 구분자는 "|"로 구분함
     * @return - "중장년·노인|저소득층"으로 검색이 성공한 경우)
     * "Status": "200",
     * "Message": [
     * {
     * "welf_name": "저소득층 기저귀·조제분유 지원",
     * "welf_category": "현물 지원",
     * "tag": "저소득층;;장애인;;다자녀;;분유;;육아",
     * "welf_local": "전국"
     * },...(중략)...],
     * "TotalCount": 12
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> searchWelfareCategory(
            @Header("LoginToken") String token,
            @Header("SessionId") String SessionId,
            @Query("type") String type,
            @Query("keyword") String category_keyword,
            @Query("userAgent") String userAgent
    );

    /**
     * 혜택 하위 카테고리 검색. 카테고리에 해당하는 혜택 정보를 검색해 결과를 받아오는 메서드
     * SearchResultActivity에서 사용
     *
     * @param type          - "child_category_search" 고정 (요청 타입), 필수 요청값
     * @param welf_category - 선택한 하위 카테고리 정보(현물 지원, 현금 지원 등), 필수 요청값
     * @param keyword       - 키워드 검색 정보
     * @param city
     * @return -
     * "Status":"200",
     * "Message":[
     * {
     * "welf_name":"전문기술인재장학금",
     * "parent_category":"청년",
     * "welf_category":"현금 지원",
     * "tag":"장학생;;학비;;전문대",
     * "welf_local":"전국"
     * },...(중략)...
     * ],
     * "TotalCount":10
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> searchSubCategoryWelfare(
            @Query("type") String type,
            @Query("welf_category") String welf_category,
            @Query("keyword") String keyword,
            @Query("welf_local") String city
//         @Query("userAgent") String userAgent
    );

    /**
     * 상위 카테고리 선택하고 조회 버튼 누른 후 이동하는 화면에서 상단 리사이클러뷰의 아이템을 누르면, 그 카테고리에 해당하는 정책들을 보여주는 메서드
     * ResultBenefitActivity에서 사용
     *
     * @param type            - "child_category_search" 고정
     * @param select_category - SearchFragment에서 선택한 카테고리들, 여러 개일 경우 "|"를 구분자로 묶는다
     * @param welf_category   - 하위 카테고리 이름(일자리 지원, 현금 지원 등)
     * @param userAgent       - android|SM-543N|30 꼴의 사용자 정보
     * @return -
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> searchUpLevelCategory(
            @Query("type") String type,
            @Query("select_category") String select_category,
            @Query("welf_category") String welf_category,
            @Query("userAgent") String userAgent
    );

    /**
     * 상위 '지역' 선택하고 조회 버튼 누른 후 이동하는 화면에서 상단 리사이클러뷰의 아이템을 누르면, 그 카테고리에 해당하는 정책들을 보여주는 메서드
     * ResultBenefitActivity에서 사용
     *
     * @param type          - "child_category_search" 고정
     * @param welf_local    - 맨처음에 선택한 지역(서울, 충북 등)
     * @param welf_category - 하위 카테고리 이름(일자리 지원, 현금 지원 등)
     *                      //    * @param userAgent - android|SM-543N|30 꼴의 사용자 정보
     * @return -
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> searchUpLevelCategory_region(
            @Query("type") String type,
            @Query("welf_local") String welf_local,
            @Query("welf_category") String welf_category
    );

    /**
     * 사용자 관심사에 따라 관련된 혜택을 보여주는 기능
     * MainFragment에서 사용 중
     *
     * @param login_token - 로그인 시 서버에서 받는 토큰값
     * @param type        - customized 고정
     * @param userAgent   - android|SM-543N|30 꼴의 사용자 로그
     * @return -
     * "Status":"200",
     * "Message":[
     * {
     * "welf_name":"장애아동수당지원",
     * "welf_local":"전북",
     * "welf_category":"현금 지원",
     * "tag":"장애인;; 아동"
     * },...
     * ],
     * "TotalCount":10
     */
    @GET("https://www.hyemo.com/welf")
    Call<String> userOrderedWelfare(
            @Query("login_token") String login_token,
            @Query("type") String type,
            @Query("userAgent") String userAgent
    );
    // ===================================================================================================

    /* ↓ 유튜브 관련 메서드 */
    // ===================================================================================================

    /**
     * 유튜브 영상 정보를 받아오는 메서드
     *
     * @return - {
     * "Status":"200",
     * "Message":[
     * {
     * "thumbnail":"https://i.ytimg.com/vi/V16hJ4ACn_A/default.jpg",
     * "videoId":"V16hJ4ACn_A",
     * "title":"잘 알려지지 않은 3가지 혜택! 꼭 받으세요~"
     * },...(중략)...
     * "TotalCount":6
     */
    @GET("https://www.hyemo.com/youtube")
    Call<String> getYoutubeInformation(
//         @Header("SessionId") String sessionId,
//         @Header("Action") String action
    );
    // ===================================================================================================

    /* ↓ 헤더에 세션 id, 로그인 토큰 넣어서 서버로 사용자 로그 전송하는 메서드 */
    // ===================================================================================================

    /**
     * 서버에서 받은 세션 id, 로그인 토큰을 헤더에 넣어 사용자 행동 로그를 서버로 전송하는 메서드
     *
     * @param token             - 로그인 시 서버에서 받는 토큰
     * @param sessionId         - 서버에서 받은 세션 id, JSON 형식으로 오기 때문에 파싱 필요
     * @param type              - 유저가 어떤 화면에서 어떤 행동을 했는지 판별하기 위한 변수, 각 화면 별로 입력해야 하는 값이 다름
     *                          main : 처음 앱에 접속한 경우 사용 / home : 메인 화면 / login : 로그인 화면 / myPage : 마이페이지 화면 / search : 검색 화면 / youtube_review : 유튜버 리뷰 화면
     * @param action            - 유저 행동 정보(앱 접속, 뒤로가기 실행 등)
     * @param keyword           - 사용자가 검색한 정보(전남, 청년 등)
     * @param deviceInformation - 사용자 기기 정보(android|SM-432M|30)
     * @return
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/log")
    Call<String> userLog(
            @Header("LoginToken") String token,
            @Header("SessionId") String sessionId,
            @Field("type") String type,
            @Field("Action") String action,
            @Field("keyword") String keyword,
            @Field("userAgent") String deviceInformation
    );
    // ===================================================================================================

    /* ↓ 닉네임 중복 처리 메서드 */
    // ===================================================================================================

    /**
     * 닉네임 중복 처리 메서드
     *
     * @param nickname - 사용자가 입력한 닉네임
     * @param type     - "nickNameCheck" 고정
     * @return 닉네임이 중복되는 경우 : {"Status":"200","Message":"닉네임이 중복되었습니다.","Result":"true"}
     * 닉네임이 중복되지 않는 경우 : {"Status":"200","Message":"닉네임이 중복되지 않았습니다.","Result":"false"}
     */
    @FormUrlEncoded
    @POST("https://www.hyemo.com/user")
    Call<String> duplicateNickname(
            @Field("nickName") String nickname,
            @Field("type") String type
    );

    /* ↓ 람다로 바뀐 후 새로 추가된 메서드
    * TestFragment(바꾸고 있는 메인 화면)에서 사용하는 로그인/비로그인 시 혜택 보여주는 메서드 */
    // ===================================================================================================

    /**
     * 비로그인이고 관심사를 선택하지 않은 유저에게 보여줄 전체 혜택과 유튜브 영상을 SELECT하는 메서드
     * @param type - "total_main" 고정
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "youtube": [
     *                 {
     *                     "id": 208,
     *                     "title": "카카오뱅크 청년 전월세 보증금 대출 받기! 금리는? | 월세탈출기",
     *                     "thumbnail": "https://i.ytimg.com/vi/vylRbcluNuo/hqdefault.jpg"
     *                 },
     *                 {
     *                     "id": 207,
     *                     "title": "근무시간, 대기시간, 휴게시간 구분하는 방법",
     *                     "thumbnail": "https://i.ytimg.com/vi/IIab7DdSauY/hqdefault.jpg"
     *                 },
     *                 {
     *                     "id": 206,
     *                     "title": "이제 이 것 안하면 과태료 100만원 물어야 합니다!!!",
     *                     "thumbnail": "https://i.ytimg.com/vi/r22cxGM_tlQ/hqdefault.jpg"
     *                 }
     *             ],
     *             "welf_data": [
     *                 {
     *                     "welf_id": 843,
     *                     "welf_name": "통합사례관리 지원",
     *                     "welf_tag": "환자 - 저소득층 - 한부모/조손가정",
     *                     "welf_field": "주거"
     *                 },
     *                 {
     *                     "welf_id": 659,
     *                     "welf_name": "지역아동센터 지원",
     *                     "welf_tag": "아동/청소년 -장애인 -저소득층 -한부모/조손가정 -다문화",
     *                     "welf_field": "주거"
     *                 }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/welf")
    Call<String> showWelfareAndYoutubeNotLogin(
            @Query("type") String type
    );

    /**
     * 관심사 선택했지만 로그인하지 않은 유저에게 보여줄 데이터
     * 로그인 토큰이 없으면 ""을 넣어보자
     * @param age - 나이(10대 미만~60대 이상)
     * @param gender - 남성 / 여성
     * @param local - 지역
     * @param type - "main" 고정
     * @return
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/welf")
    Call<String> showDataForNotLoginAndChoseInterest(
            @Query("age") String age,
            @Query("gender") String gender,
            @Query("local") String local,
            @Query("type") String type
    );

    /**
     * 관심사를 선택한 유저에게 보여줄 전체 혜택 리스트와 유튜브 영상을 SELECT하는 메서드
     * 비로그인일 시 나이, 성별, 지역이 필수 인자고 로그인했을 시 로그인 토큰이 필수다
     * @param type - "main" 고정
     * @param token - 로그인 시 서버에서 받는 토큰
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "youtube": [
     *                 {
     *                     "id": 208,
     *                     "title": "카카오뱅크 청년 전월세 보증금 대출 받기! 금리는? | 월세탈출기",
     *                     "thumbnail": "https://i.ytimg.com/vi/vylRbcluNuo/hqdefault.jpg"
     *                 },
     *                 {
     *                     "id": 207,
     *                     "title": "근무시간, 대기시간, 휴게시간 구분하는 방법",
     *                     "thumbnail": "https://i.ytimg.com/vi/IIab7DdSauY/hqdefault.jpg"
     *                 },
     *                 {
     *                     "id": 206,
     *                     "title": "이제 이 것 안하면 과태료 100만원 물어야 합니다!!!",
     *                     "thumbnail": "https://i.ytimg.com/vi/r22cxGM_tlQ/hqdefault.jpg"
     *                 }
     *             ],
     *             "welf_data": [ <- 이 JSONArray 안의 내용을 메인 화면의 3개 아이템만 있는 리사이클러뷰에 넣어야 한다
     *                 {
     *                     "welf_id": 843,
     *                     "welf_name": "통합사례관리 지원",
     *                     "welf_tag": "환자 - 저소득층 - 한부모/조손가정",
     *                     "welf_field": "주거"
     *                 },
     *                 {
     *                     "welf_id": 633,
     *                     "welf_name": "백천사회복지관 특화 서비스제공(경산시)",
     *                     "welf_tag": "저소득층 - 아동/청소년 - 노년 - 한부모/조손가정 - 다자녀 - 다문화",
     *                     "welf_field": "주거"
     *                 }...
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/welf")
    Call<String> showWelfareAndYoutubeLogin(
//            @Query("age") String age,
//            @Query("gender") String gender,
//            @Query("local") String local,
            @Header("logintoken") String token,
            @Query("type") String type
    );

    /* ↓ 람다로 바뀐 후 새로 추가된 검색 메서드
    * TestSearchFragment에서 사용한다 */
    // ===================================================================================================

    /**
     * 검색어와 일치하는 내용의 혜택을 조회하는 메서드
     * @param keyword - 검색어 정보(유저가 입력한 검색어) ※ 필수값
     * @param page - 페이지(1, 2, 3, ...) ※ 필수값
     * @param token - 서버에서 받은 토큰
     * @param sessionId - 쉐어드에 저장된 세션 id
     * @param category - 검색어 필터링 값(값이 여럿일 경우 '-'로 구분한다 : 건강-교육-근로)
     * @param local - 유저 거주 지역(서울-경기-강원 등)
     * @param age - 유저 나이대(20, 30)
     * @param provideType -
     * @param type - "search" 고정 ※ 필수값
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "welf_id": 938,
     *             "welf_name": "학기 중 토ㆍ일ㆍ공휴일 아동급식 지원",
     *             "welf_tag": "학생 - 아동/청소년 ",
     *             "welf_count": 0,
     *             "welf_local": "충남",
     *             "welf_thema": "기타"
     *         },
     *         ...이런 식으로 한 번에 10개가 온다
     *     ],
     *     "TotalCount": 22,
     *     "TotalPage": 3
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/search")
    Call<String> renewalKeywordSearch(
            @Query("keyword") String keyword,
            @Query("page") String page,
            @Query("logintoken") String token,
            @Query("sessionid") String sessionId,
            @Query("category") String category,
            @Query("local") String local,
            @Query("age") String age,
            @Query("provide_type") String provideType,
            @Query("type") String type
    );

    /**
     * 추천 태그로 검색하는 메서드
     * @param keyword - "추천 태그" 고정
     * @param page - 요청하는 페이지 값(페이징 시 사용)
     * @param type - "tag" 고정
     * @return
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/search")
    Call<String> searchRecommendTag(
            @Query("keyword") String keyword,
            @Query("page") String page,
            @Query("logintoken") String token,
            @Query("sessionid") String sessionId,
            @Query("category") String category,
            @Query("local") String local,
            @Query("age") String age,
            @Query("provide_type") String provideType,
            @Query("type") String type
    );

    /**
     * 비회원이 각 테마별 전체 혜택들을 볼 수 있는 메서드
     * 메인 화면에서 "+ 더보기" 텍스트를 누르면 이동하는 화면에서 호출한다
     * @param page - 요청하는 페이지 값
     * @param assist_method - 더 보고자 하는 지원 형태에 대한 혜택(start, all, 현금 지원, 서비스 지원 등)
     *              - start : 각 테마별 10개씩 리턴하고(assist_method_10) 전체 혜택 리스트 중 10개 리턴(all_10)
     *              - all : 전체 혜택 리스트
     * @param gender - 미리보기에서 선택한 성별
     * @param age - 미리보기에서 선택한 나이
     * @param local - 미리보기에서 선택한 지역
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "welf_id": 533,
     *             "welf_name": "긴급복지 지원",
     *             "welf_tag": "임신/출산 - 환자 - 저소득층",
     *             "welf_count": 0
     *         },...이런 식으로 10개가 한 덩어리로 옴
     *     ],
     *     "total_page": 4,
     *     "total_num": 34
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/welf-more")
    Call<String> moreViewWelfareNotLogin(
            @Header("sessionid") String session,
            @Query("page") String page,
            @Query("assist_method") String assist_method,
            @Query("gender") String gender,
            @Query("age") String age,
            @Query("local") String local
    );

    /**
     * 회원이 각 테마별 전체 혜택들을 볼 수 있는 기능
     * 메인 화면에서 "+ 더보기" 텍스트를 누르면 이동하는 화면에서 호출한다
     * @param token - 서버에서 받은 토큰
     * @param session - 쉐어드에 저장된 세션 id
     * @param page - 요청하는 페이지 값
     * @param assist_method - 더 보고자 하는 혜택 테마 이름
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "welf_id": 551,
     *             "welf_name": "성폭력 피해자 지원",
     *             "welf_tag": "여성 -환자",
     *             "welf_count": 0
     *         },...이런 식으로 10개가 올 것. 예시에선 7개만 옴
     *     ],
     *     "total_page": 1,
     *     "total_num": 7
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/welf-more")
    Call<String> moreViewWelfareLogin(
            @Header("logintoken") String token,
            @Header("sessionid") String session,
            @Query("page") String page,
            @Query("assist_method") String assist_method
    );

    /* 마이페이지에서 사용하는 메서드 */
    // ===================================================================================================
    /**
     * 닉네임 변경 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param nickname - 변경하려는 닉네임
     * @param type - check : 중복 검사 / save : 중복되지 않은(유효한) 닉네임을 DB에 저장시켜라 / show_name : 사용자의 닉네임 요청
     * @return -
     * 1. 사용자의 닉네임을 요청
     * {
     *     "statusCode": 200,
     *     "message": "팀노바_대균"
     * }
     *
     * 2. 닉네임 중복 검사
     * {
     *     "statusCode": 200,
     *     "message": "사용 가능한 닉네임 입니다."
     * }
     *
     * 3. 유효한 닉네임을 최종적으로 DB에 저장
     * {
     *     "statusCode": 200,
     *     "message": "닉네임 변경 완료"
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/change-name")
    Call<String> editNickname(
            @Header("logintoken") String token,
            @Nullable @Query("new_name") String nickname,
            @Query("type") String type
    );

    /**
     * 내가 작성한 리뷰를 확인하는 기능
     * WrittenReviewCheckActivity에서 사용
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param page - 요청하는 페이지 값(1p당 10개 청크)
     * @return -
     * - 작성한 리뷰가 있는 경우 -
     *
     * {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "welf_name": "저소득층 자산형성 지원(희망, 내일키움통장 사업)",
     *             "writer": "user124",
     *             "content": "test",
     *             "star_count": 3,
     *             "difficulty_level": "쉬워요",
     *             "satisfaction": "도움 돼요",
     *             "create_date": "2021-04-30 22:29:04",
     *             "welf_id": 570
     *         },...x5
     *     ],
     *     "total": 5,
     *     "totalPage": 1
     * }
     *
     * - 작성한 리뷰가 없는 경우 -
     *
     * {
     *     "statusCode": 200,
     *     "message": [],
     *     "total": 0,
     *     "totalPage": 0
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/my_review")
    Call<String> checkMyReview(
            @Header("logintoken") String token,
            @Query("page") String page
    );

    /**
     * 리뷰 수정 람다 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param edit_review - 서버로 넘기는 JSON 데이터를 String으로 변경한 것
     * @return -
     * {
     *      "statusCode": 200,
     *      "message":"리뷰 수정이 완료되었습니다."
     * }
     */
    @PUT("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/review")
    Call<String> editReview(
            @Header("logintoken") String token,
            @Body String edit_review
    );

    /**
     * 리뷰 삭제 람다 메서드
     * delete를 사용할 경우 body를 사용할 수 없기 때문에 @HTTP를 써서 body를 넘겨야 한다
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param remove_review - 서버로 넘기는 JSON 데이터를 String으로 변경한 것
     * @return -
     * {
     *     "statusCode": 200,
     *     "message": "리뷰 삭제가 완료되었습니다."
     * }
     */
    //@DELETE는 @Body를 쓸수 없다 그래서 @HTTP를 사용(@HTTP는 @Body를 쓸 수 있다)
    @HTTP(method = "DELETE", path = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/review", hasBody = true)
    Call<String> deleteReview(
            @Header("logintoken") String token,
            @Body String remove_review
    );

    /* 북마크 관련 메서드 */
    // ===================================================================================================
    /**
     * 북마크 목록을 확인하는 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param type - "show" 고정(북마크 데이터를 가져오는 거니까)
     * @param page - 요청하는 페이지 값
     * @return - {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "id": 529,
     *             "welf_name": "수의사 연수교육 지원",
     *             "tag": "농축수산인"
     *         },
     *         {
     *             "id": 975,
     *             "welf_name": "성인문해교육 지원",
     *             "tag": ""
     *         }, ... 이런 식으로 10개가 온다
     *     ],
     *     "total": 10,
     *     "total_page": 1
     * }
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/mypage-bookmark")
    Call<String> getBookmark(
            @Header("logintoken") String token,
            @Query("type") String type,
            @Query("page") String page
    );

    /**
     * 북마크 데이터를 삭제하는 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param type - "delete" 고정(북마크 데이터를 삭제하는 거니까)
     * @param page - 요청하는 페이지 값
     * @param id - 삭제하려는 북마크 데이터의 id값 (id 간 구분자는 "-")
     * @return -
     * 200: 삭제되었습니다.
     * 200: 북마크 조회 완료
     * 200: 이미 삭제된 북마크 입니다.
     * 500: 존재하지 않는 북마크 입니다.
     * 400: 계정 정보가 존재하지 않습니다.
     * 404: data is empty
     * 500: Failed to connect to MySQL.
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/mypage-bookmark")
    Call<String> deleteBookmark(
            @Header("logintoken") String token,
            @Query("type") String type,
            @Nullable @Query("page") String page,   // 1p에 있는 북마크를 삭제할 때 굳이 페이지를 넣을 필요 없음
            @Query("id") String id
    );

    /* 관심사 선택, 수정 메서드 */
    // ===================================================================================================

    /**
     * 유저의 관심사를 추가하는 기능
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param age - 나이대(10대 미만-10대-20대)
     * @param local - 지역(서울-경기-인천)
     * @param family - 가구 형태(다문화-다자녀-소년소녀 가장)
     * @param category - 카테고리(군인/보훈대상자-농축수산인-장애인)
     * @return -
     * {
     *     "statusCode": 200,
     *     "message": "관심사 등록이 완료됐습니다."
     * }
     */
    //관심사 추가 메서드
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/put-interest")
    Call<String> AddMyInterest(
            @Header("logintoken") String token,
            @Query("age") String age,
            @Query("local") String local,
            @Query("family") String family,
            @Query("category") String category
    );

    /**
     * 마이페이지에서 관심사를 확인하고 수정할 수 있는 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param age - 관심 나이대 (여러 개일 경우 '-'로 열거)
     * @param local - 관심 지역 (여러 개일 경우 '-'로 열거)
     * @param family - 관심 가구 형태 (여러 개일 경우 '-'로 열거)
     * @param category - 관심 카테고리 (여러 개일 경우 '-'로 열거)
     * @param type - show : 유저의 관심사 리턴 / modify : 관심사 수정, 조회할 때는 type과 헤더에 token만 넣어주면 된다!!!
     * @return -
     * // 관심사 확인
     * {
     *     "statusCode": 200,
     *     "message": [
     *         {
     *             "age": "20대|30대",
     *             "local": "서울|경기|인천",
     *             "category": "군인/보훈대상자|농축산인|여성",
     *             "family": "다문화|다자녀|소년소녀가장"
     *         }
     *     ]
     * }
     *
     * // 관심사 수정
     * {
     *     "statusCode": 200,
     *     "message": "관심사 수정이 완료됐습니다."
     * }
     */
    //관심사 조회 or 수정 메서드
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/modify-interest")
    Call<String> checkAndModifyInterest(
            @Header("logintoken") String token,
            @Query("age") String age,
            @Query("local") String local,
            @Query("family") String family,
            @Query("category") String category,
            @Query("type") String type
    );

    /* 회원탈퇴 메서드 */
    // ===================================================================================================

    /**
     * 회원탈퇴 메서드
     * @param token - 로그인 시 서버에서 받는 토큰
     * @param type - "leave" 고정
     * @param reason - 탈퇴 사유
     * @return
     */
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/leave-member")
    Call<String> leaveFromApp(
            @Header("logintoken") String token,
            @Query("type") String type,
            @Query("leave_reason") String reason
    );

    //회원 탈퇴
    @POST("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/leave-member")
    Call<String> WithdrawalApp(
            @Header("logintoken") String LoginToken,
            @Body String reason
    );

    // 리뷰 작성 테스트
    @POST("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/review")
    Call<String> reviewWrite(
            @Header("logintoken") String LoginToken,
            @Body String review
    );

}