package com.psj.welfare;

public class DetailReviewData {

    private String review_id; //리뷰 아이디값
    private String login_id; //로그인 아이디값
    private String nickName; //닉네임
    private String content; // 내용
    private float star_count; //별점
    private String difficulty_level; // 난이도 평가
    private String satisfaction; //만족도 평가
    private String create_date; //리뷰 작성 날짜

    public DetailReviewData() {
    }

    public DetailReviewData(String review_id, String login_id, String nickName, String content, float star_count, String difficulty_level, String satisfaction, String create_date) {
        this.review_id = review_id;
        this.login_id = login_id;
        this.nickName = nickName;
        this.content = content;
        this.star_count = star_count;
        this.difficulty_level = difficulty_level;
        this.satisfaction = satisfaction;
        this.create_date = create_date;
    }

    public String getReview_id() {
        return review_id;
    }

    public void setReview_id(String review_id) {
        this.review_id = review_id;
    }

    public String getLogin_id() {
        return login_id;
    }

    public void setLogin_id(String login_id) {
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
