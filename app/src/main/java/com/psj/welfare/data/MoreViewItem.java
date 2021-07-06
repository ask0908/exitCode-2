package com.psj.welfare.data;

import com.google.gson.annotations.SerializedName;

/* TestMoreViewActivity(더보기 리스트 화면)의 리사이클러뷰에 쓸 아이템 */
public class MoreViewItem
{
    @SerializedName("welf_id")
    private String welf_id;
    @SerializedName("welf_name")
    private String welf_name;
    @SerializedName("welf_tag")
    private String welf_tag;
    @SerializedName("assist_method")
    private String assist_method;
    @SerializedName("welf_count")
    private String welf_count;

    public MoreViewItem()
    {
    }

    public MoreViewItem(String assist_method)
    {
        this.assist_method = assist_method;
    }

    public String getWelf_id()
    {
        return welf_id;
    }

    public void setWelf_id(String welf_id)
    {
        this.welf_id = welf_id;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public String getWelf_tag()
    {
        return welf_tag;
    }

    public void setWelf_tag(String welf_tag)
    {
        this.welf_tag = welf_tag;
    }

    public String getAssist_method()
    {
        return assist_method;
    }

    public void setAssist_method(String assist_method)
    {
        this.assist_method = assist_method;
    }

    public String getWelf_count()
    {
        return welf_count;
    }

    public void setWelf_count(String welf_count)
    {
        this.welf_count = welf_count;
    }

}
