package com.psj.welfare.Data;

/* SelectedCategoryAdapter에서 사용됨, ResultBenefitActivity에서 선택한 버튼들의 정보를 가져온다 */
public class SelectedButtonItem
{
    String button;
    String button_title;
    String button_color;
    String title_num;

    public String getButton()
    {
        return button;
    }

    public void setButton(String button)
    {
        this.button = button;
    }

    public String getButton_title()
    {
        return button_title;
    }

    public void setButton_title(String button_title)
    {
        this.button_title = button_title;
    }

    public String getButton_color()
    {
        return button_color;
    }

    public void setButton_color(String button_color)
    {
        this.button_color = button_color;
    }

    public String getTitle_num()
    {
        return title_num;
    }

    public void setTitle_num(String title_num)
    {
        this.title_num = title_num;
    }
}
