package com.psj.welfare.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherYoutubeItem
{
    @Expose
    @SerializedName("videoId")
    private String url_id;

    @Expose
    @SerializedName("thumbnail")
    private String thumbnail;

    @Expose
    @SerializedName("title")
    private String title;

    public String getUrl_id()
    {
        return url_id;
    }

    public void setUrl_id(String url_id)
    {
        this.url_id = url_id;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
