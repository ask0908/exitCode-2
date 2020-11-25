package com.psj.welfare.Data;

import com.google.gson.annotations.SerializedName;

public class ReviewDTO
{
    @SerializedName("id")
    public String id;

    @SerializedName("content")
    public String content;

    @SerializedName("writer")
    public String writer;

    @SerializedName("email")
    public String email;

    @SerializedName("create_date")
    public String create_date;

    @SerializedName("like_count")
    public String like_count;

    @SerializedName("bad_count")
    public String bad_count;

    @SerializedName("star_count")
    public float star_count;

    @SerializedName("image_url")
    public String image_url;

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

    public String getWriter()
    {
        return writer;
    }

    public void setWriter(String writer)
    {
        this.writer = writer;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getCreate_date()
    {
        return create_date;
    }

    public void setCreate_date(String create_date)
    {
        this.create_date = create_date;
    }

    public String getLike_count()
    {
        return like_count;
    }

    public void setLike_count(String like_count)
    {
        this.like_count = like_count;
    }

    public String getBad_count()
    {
        return bad_count;
    }

    public void setBad_count(String bad_count)
    {
        this.bad_count = bad_count;
    }

    public float getStar_count()
    {
        return star_count;
    }

    public void setStar_count(float star_count)
    {
        this.star_count = star_count;
    }

    public String getImage_url()
    {
        return image_url;
    }

    public void setImage_url(String image_url)
    {
        this.image_url = image_url;
    }

    @Override
    public String toString()
    {
        return "ReviewDTO{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", writer='" + writer + '\'' +
                ", email='" + email + '\'' +
                ", create_date='" + create_date + '\'' +
                ", like_count='" + like_count + '\'' +
                ", bad_count='" + bad_count + '\'' +
                ", star_count='" + star_count + '\'' +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}
