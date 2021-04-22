package com.psj.welfare.data;

/* 새로 바뀐 메인 화면의 하단 리사이클러뷰에 쓸 모델 클래스 */
public class MainCategoryBottomItem
{
    private String welf_id;
    private String welf_name;
    private String welf_tag;
    private String welf_count;
    private String welf_field;

    public MainCategoryBottomItem()
    {
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

    public String getWelf_count()
    {
        return welf_count;
    }

    public void setWelf_count(String welf_count)
    {
        this.welf_count = welf_count;
    }

    public String getWelf_field()
    {
        return welf_field;
    }

    public void setWelf_field(String welf_field)
    {
        this.welf_field = welf_field;
    }
}
