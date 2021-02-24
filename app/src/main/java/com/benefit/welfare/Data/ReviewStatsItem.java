package com.benefit.welfare.Data;

import com.google.gson.annotations.SerializedName;

/* 서버에서 리뷰 통계 가져올 때 쓰는 모델 클래스 */
public class ReviewStatsItem
{
    @SerializedName("total_user")
    private String total_user;

    @SerializedName("star_sum")
    private String star_sum;

    @SerializedName("one_point")
    private String one_point;

    @SerializedName("two_point")
    private String two_point;

    @SerializedName("three_point")
    private String three_point;

    @SerializedName("four_point")
    private String four_point;

    @SerializedName("five_point")
    private String five_point;

    @SerializedName("esay")
    private String easy;

    @SerializedName("hard")
    private String hard;

    @SerializedName("help")
    private String help;

    @SerializedName("helf_not")
    private String help_not;

    public String getTotal_user()
    {
        return total_user;
    }

    public void setTotal_user(String total_user)
    {
        this.total_user = total_user;
    }

    public String getStar_sum()
    {
        return star_sum;
    }

    public void setStar_sum(String star_sum)
    {
        this.star_sum = star_sum;
    }

    public String getOne_point()
    {
        return one_point;
    }

    public void setOne_point(String one_point)
    {
        this.one_point = one_point;
    }

    public String getTwo_point()
    {
        return two_point;
    }

    public void setTwo_point(String two_point)
    {
        this.two_point = two_point;
    }

    public String getThree_point()
    {
        return three_point;
    }

    public void setThree_point(String three_point)
    {
        this.three_point = three_point;
    }

    public String getFour_point()
    {
        return four_point;
    }

    public void setFour_point(String four_point)
    {
        this.four_point = four_point;
    }

    public String getFive_point()
    {
        return five_point;
    }

    public void setFive_point(String five_point)
    {
        this.five_point = five_point;
    }

    public String getEasy()
    {
        return easy;
    }

    public void setEasy(String easy)
    {
        this.easy = easy;
    }

    public String getHard()
    {
        return hard;
    }

    public void setHard(String hard)
    {
        this.hard = hard;
    }

    public String getHelp()
    {
        return help;
    }

    public void setHelp(String help)
    {
        this.help = help;
    }

    public String getHelp_not()
    {
        return help_not;
    }

    public void setHelp_not(String help_not)
    {
        this.help_not = help_not;
    }
}
