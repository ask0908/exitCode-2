package com.psj.welfare.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/* MainFragment에서 가로로 유튜브 영상들을 보여주는 리사이클러뷰에 사용할 모델 클래스 */
public class HorizontalYoutubeItem implements Serializable
{
    private String category;
    @SerializedName("title")
    private String youtube_name;
    private String youtube_id;
    private String youtube_thumbnail;
    private String youtube_videoId;

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

    public String getYoutube_name()
    {
        return youtube_name;
    }

    public void setYoutube_name(String youtube_name)
    {
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
