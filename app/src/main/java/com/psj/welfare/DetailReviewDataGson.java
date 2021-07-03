package com.psj.welfare;

import com.google.gson.annotations.SerializedName;

public class DetailReviewDataGson {

    //JSON으로 받아온 데이터 타이틀과 다르면 SerializedName 해주면 된다
    @SerializedName("id")
    private int review_id;
    @SerializedName("writer")
    private String nickName;
    private boolean is_me;
    private String content;
    private float star_count;
    private String difficulty_level;
    private String satisfaction;
    private String create_date;

    public DetailReviewDataGson() {    }

    public DetailReviewDataGson(int review_id, String nickName, boolean is_me, String content, float star_count, String difficulty_level, String satisfaction, String create_date) {
        this.review_id = review_id;
        this.nickName = nickName;
        this.is_me = is_me;
        this.content = content;
        this.star_count = star_count;
        this.difficulty_level = difficulty_level;
        this.satisfaction = satisfaction;
        this.create_date = create_date;
    }

    public int getReview_id() {
        return review_id;
    }

    public void setReview_id(int review_id) {
        this.review_id = review_id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean getIs_me() {
        return is_me;
    }

    public void setIs_me(boolean is_me) {
        this.is_me = is_me;
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
