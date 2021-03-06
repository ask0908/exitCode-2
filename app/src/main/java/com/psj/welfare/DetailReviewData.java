package com.psj.welfare;

import com.google.gson.annotations.SerializedName;

public class DetailReviewData {

    //Gson으로 데이터를 받을 때 서버에서 JSON으로 받아온 데이터와 변수명이 다르면 SerializedName 해주면 된다
    @SerializedName("id")
    private int review_id; //리뷰 아이디값
    private int login_id; //로그인 아이디값
    private boolean is_me; //내가 쓴 리뷰인지
    @SerializedName("writer")
    private String nickName; //닉네임
    private String content; // 내용
    private float star_count; //별점
    private String difficulty_level; // 난이도 평가
    private String satisfaction; //만족도 평가
    private String create_date; //리뷰 작성 날짜


    public DetailReviewData() {
    }


    public boolean getIs_me() {
        return is_me;
    }

    public void setIs_me(boolean is_me) {
        this.is_me = is_me;
    }

    public int getReview_id() {
        return review_id;
    }

    public void setReview_id(int review_id) {
        this.review_id = review_id;
    }

    public int getLogin_id() {
        return login_id;
    }

    public void setLogin_id(int login_id) {
        this.login_id = login_id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getStar_count() {
        return star_count;
    }

    public void setStar_count(float star_count) {
        this.star_count = star_count;
    }

    public String getDifficulty_level() {
        return difficulty_level;
    }

    public void setDifficulty_level(String difficulty_level) {
        this.difficulty_level = difficulty_level;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(String satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }
}
