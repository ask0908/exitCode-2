package com.psj.welfare.data;

import com.google.gson.annotations.SerializedName;

public class ReviewItem
{
    @SerializedName("id")
    private String id;

    @SerializedName("content")
    private String content;

    @SerializedName("create_date")
    private String create_date;

    @SerializedName("image_url")
    private String image_url;

    @SerializedName("writer")
    private String writer;

    @SerializedName("star_count")
    private float star_count;

    public ReviewItem()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getCreate_date()
    {
        return create_date;
    }

    public void setCreate_date(String create_date)
    {
        this.create_date = create_date;
    }

    public String getImage_url()
    {
        return image_url;
    }

    public void setImage_url(String image_url)
    {
        this.image_url = image_url;
    }

    public float getStar_count()
    {
        return star_count;
    }

    public void setStar_count(float star_count)
    {
        this.star_count = star_count;
    }

    public String getWriter()
    {
        return writer;
    }

    public void setWriter(String writer)
    {
        this.writer = writer;
    }
}
