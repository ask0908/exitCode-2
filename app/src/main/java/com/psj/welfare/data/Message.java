package com.psj.welfare.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Message
{
    @SerializedName("theme_10")
    @Expose
    private List<Theme10> theme10 = null;

    @SerializedName("all_10")
    @Expose
    private List<All10> all10 = null;

    public List<Theme10> getTheme10()
    {
        return theme10;
    }

    public void setTheme10(List<Theme10> theme10)
    {
        this.theme10 = theme10;
    }

    public List<All10> getAll10()
    {
        return all10;
    }

    public void setAll10(List<All10> all10)
    {
        this.all10 = all10;
    }

}
