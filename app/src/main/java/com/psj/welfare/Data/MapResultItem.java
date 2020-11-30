package com.psj.welfare.Data;

public class MapResultItem
{
    private String benefit_name;
    private String benefit_btn_text;

    public MapResultItem()
    {
    }

    public MapResultItem(String benefit_name, String benefit_btn_text)
    {
        this.benefit_name = benefit_name;
        this.benefit_btn_text = benefit_btn_text;
    }

    public String getBenefit_name()
    {
        return benefit_name;
    }

    public void setBenefit_name(String benefit_name)
    {
        this.benefit_name = benefit_name;
    }

    public String getBenefit_btn_text()
    {
        return benefit_btn_text;
    }

    public void setBenefit_btn_text(String benefit_btn_text)
    {
        this.benefit_btn_text = benefit_btn_text;
    }
}
