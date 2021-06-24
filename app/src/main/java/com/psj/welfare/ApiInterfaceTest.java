package com.psj.welfare;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterfaceTest {

    //미리보기 결과값
    @GET("v2/welf")
    Call<String> BenefitTutorial(
            @Query("age") String age,
            @Query("gender") String gender,
            @Query("local") String local,
            @Query("type") String type
    );

    //혜택 상세보기
    @GET("v2/welf")
    Call<String> BenefitDetail(
            @Header("logintoken") String LoginToken,
            @Header("sessionid") String SessionId,
            @Query("type") String type,
            @Query("welf_id") String welf_id
    );

    //북마크 추가, 제거
    @GET("v2/user")
    Call<String> BookMark(
            @Header("logintoken") String LoginToken,
            @Header("sessionid") String SessionId,
            @Query("type") String type,
            @Query("welf_id") String welf_id
    );

    //리뷰 작성
    @POST("v2/review")
    Call<String> ReviewWrite(
            @Header("logintoken") String LoginToken,
            @Body String review
    );

    //리뷰 모두 보기
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/review")
    Call<String> ReviewAllLook(
            @Header("logintoken") String LoginToken,
            @Query("filter") String filter,
            @Query("welf_id") String welf_id
    );


    //유튜브 영상 선택한 경우 데이터 받기(로그인x)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/youtube-more")
    Call<String> YoutubeSelect(
            @Query("type") String type,
            @Query("page") String page,
            @Query("id") String id
    );

    //유튜브 영상 선택한 경우 데이터 받기(로그인o)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/youtube-more")
    Call<String> YoutubeSelect_beingid(
            @Header("logintoken") String LoginToken,
            @Header("sessionid") String sessionid,
            @Query("type") String type,
            @Query("page") String page,
            @Query("id") String id
    );


    //유튜브 영상 선택하지 않은 경우 데이터 받기(로그인x)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/youtube-more")
    Call<String> YoutubeNonSelect(
            @Query("type") String type,
            @Query("page") String page
    );

    //유튜브 영상 선택하지 않은 경우 데이터 받기(로그인o)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/youtube-more")
    Call<String> YoutubeNonSelect_beingid(
            @Header("logintoken") String LoginToken,
            @Header("sessionid") String sessionid,
            @Query("type") String type,
            @Query("page") String page
    );

    //배너 데이터 받기(로그인o)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/banner-detail")
    Call<String> BannerDetail_beingid(
            @Header("logintoken") String LoginToken,
            @Header("sessionid") String sessionid,
            @Query("banner_name") String banner_name
    );

    //배너 데이터 받기(로그인x)
    @GET("https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/v2/banner-detail")
    Call<String> BannerDetail(
            @Query("banner_name") String banner_name
    );


}
