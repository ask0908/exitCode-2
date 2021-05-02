package com.psj.welfare.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Theme10
{
    @SerializedName("welf_id")
    @Expose
    public String welfId;

    @SerializedName("welf_name")
    @Expose
    public String welfName;

    @SerializedName("welf_tag")
    @Expose
    public String welfTag;

    @SerializedName("welf_field")
    @Expose
    public String welfField;

    @SerializedName("welf_count")
    @Expose
    public String welfCount;

    public String getWelfId()
    {
        return welfId;
    }

    public void setWelfId(String welfId)
    {
        this.welfId = welfId;
    }

    public String getWelfName()
    {
        return welfName;
    }

    public void setWelfName(String welfName)
    {
        this.welfName = welfName;
    }

    public String getWelfTag()
    {
        return welfTag;
    }

    public void setWelfTag(String welfTag)
    {
        this.welfTag = welfTag;
    }

    public String getWelfField()
    {
        return welfField;
    }

    public void setWelfField(String welfField)
    {
        this.welfField = welfField;
    }

    public String getWelfCount()
    {
        return welfCount;
    }

    public void setWelfCount(String welfCount)
    {
        this.welfCount = welfCount;
    }
}
