package com.benefit.welfare.Data;

public class MapResultItem
{
    private String parent_category; // 기타, 육아·임신 등 카테고리
    private String welf_name;       // 혜택명
    private String welf_category;   // 현금 지원, 일자리 지원 등 하위 카테고리
    private String keyword_tag;     // ';; ' 구분자 섞여서 날아오는 태그들
    private String welf_local;      // 혜택이 실시되는 지역

    public MapResultItem()
    {
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

    public String getKeyword_tag()
    {
        return keyword_tag;
    }

    public void setKeyword_tag(String keyword_tag)
    {
        this.keyword_tag = keyword_tag;
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
