package com.psj.welfare.data;

public class SeeMoreItem
{
    private String welf_id;     // 혜택의 idx
    private String welf_name;   // 혜택명
    private String welf_tag;    // 임신/출산, 다자녀, 다문화 등
    private String welf_field;  // 상단 리사이클러뷰에 들어가는 값들(사업, 교육, 기타, 문화 등)
    private String welf_count;  // 조회수

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

    public String getWelf_field()
    {
        return welf_field;
    }

    public void setWelf_field(String welf_field)
    {
        this.welf_field = welf_field;
    }

    public String getWelf_count()
    {
        return welf_count;
    }

    public void setWelf_count(String welf_count)
    {
        this.welf_count = welf_count;
    }
}
