package com.psj.welfare.data;

public class SearchResultItem
{
    private String welf_id;
    private String welf_name;
    private String welf_tag;
    private String welf_count;
    private String welf_local;
    private String welf_thema;

    private boolean isSelected;

    public SearchResultItem()
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

    public String getWelf_local()
    {
        return welf_local;
    }

    public void setWelf_local(String welf_local)
    {
        this.welf_local = welf_local;
    }

    public String getWelf_thema()
    {
        return welf_thema;
    }

    public void setWelf_thema(String welf_thema)
    {
        this.welf_thema = welf_thema;
    }

    public boolean getSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
