package com.psj.welfare.Data;

/* 새로 만든 모델 클래스, ResultBenefitActivity에서 사용 */
public class CategorySearchResultItem
{
    private String welf_name;
    private String welf_category;
    private String parent_category;
    private String tag;
    private String welf_local;

    public CategorySearchResultItem()
    {
    }

    public CategorySearchResultItem(String welf_category)
    {
        this.welf_category = welf_category;
    }

    public String getParent_category()
    {
        return parent_category;
    }

    public void setParent_category(String parent_category)
    {
        this.parent_category = parent_category;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public String getWelf_category()
    {
        return welf_category;
    }

    public void setWelf_category(String welf_category)
    {
        this.welf_category = welf_category;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getWelf_local()
    {
        return welf_local;
    }

    public void setWelf_local(String welf_local)
    {
        this.welf_local = welf_local;
    }
}
