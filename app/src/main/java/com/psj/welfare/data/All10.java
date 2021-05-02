package com.psj.welfare.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class All10
{
    @SerializedName("welf_id")
    @Expose
    private String welfId;

    @SerializedName("welf_name")
    @Expose
    private String welfName;

    @SerializedName("welf_tag")
    @Expose
    private String welfTag;

    @SerializedName("welf_count")
    @Expose
    private String welfCount;

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

    public String getWelfCount()
    {
        return welfCount;
    }

    public void setWelfCount(String welfCount)
    {
        this.welfCount = welfCount;
    }
}
