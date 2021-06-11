package com.psj.welfare.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/* MainFragment에서 가로로 유튜브 영상들을 보여주는 리사이클러뷰에 사용할 모델 클래스 */
public class HorizontalYoutubeItem implements Serializable
{
    private String category;
    @SerializedName("title")
    private String youtube_title; //유튜버 타이틀
    private String youtube_id; //유뷰트 id(DB에서 id 칼럼 값)
    private String youtube_thumbnail; //유튜브 썸네일 이미지
    private String youtube_videoId; //유튜브 비디오 id
    private String youtube_upload_date; //유튜브 업로드 날짜
    private String youtube_name; //유튜버 이름


    public HorizontalYoutubeItem()
    {
    }

    public String getYoutube_thumbnail()
    {
        return youtube_thumbnail;
    }

    public void setYoutube_thumbnail(String youtube_thumbnail)
    {
        this.youtube_thumbnail = youtube_thumbnail;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getYoutube_title() {
        return youtube_title;
    }

    public void setYoutube_title(String youtube_title) {
        this.youtube_title = youtube_title;
    }

    public String getYoutube_upload_date() {
        return youtube_upload_date;
    }

    public void setYoutube_upload_date(String youtube_upload_date) {
        this.youtube_upload_date = youtube_upload_date;
    }

    public String getYoutube_name() {
        return youtube_name;
    }

    public void setYoutube_name(String youtube_name) {
        this.youtube_name = youtube_name;
    }

    public String getYoutube_id()
    {
        return youtube_id;
    }

    public void setYoutube_id(String youtube_id)
    {
        this.youtube_id = youtube_id;
    }

    public String getYoutube_videoId()
    {
        return youtube_videoId;
    }

    public void setYoutube_videoId(String youtube_videoId)
    {
        this.youtube_videoId = youtube_videoId;
    }
}
