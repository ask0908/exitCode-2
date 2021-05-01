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
    @GET("v2/review")
    Call<String> ReviewAllLook(
            @Header("logintoken") String LoginToken,
            @Query("welf_id") String welf_id
    );


}
