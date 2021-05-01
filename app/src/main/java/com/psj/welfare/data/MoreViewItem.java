package com.psj.welfare.data;

/* TestMoreViewActivity(더보기 리스트 화면)의 리사이클러뷰에 쓸 아이템 */
public class MoreViewItem
{
    private String welf_id;
    private String welf_name;
    private String welf_tag;
    private String welf_count;

    private String welf_thema;

    public MoreViewItem(String welf_thema)
    {
        this.welf_thema = welf_thema;
    }

    public MoreViewItem()
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

    public String getWelf_thema()
    {
        return welf_thema;
    }

    public void setWelf_thema(String welf_thema)
    {
        this.welf_thema = welf_thema;
    }
}
