package com.psj.welfare.Data;

// 상단 리사이클러뷰에 쓰는 모델 클래스
public class ResultKeywordItem
{
    private String parent_category; // 기타, 육아·임신 등 카테고리
    private String welf_name;       // 혜택명
    private String welf_category;   // 현금 지원, 일자리 지원 등 하위 카테고리
    private String keyword_tag;     // ';; ' 구분자 섞여서 날아오는 태그들

    public ResultKeywordItem()
    {
    }

    public ResultKeywordItem(String welf_category)
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

    public String getKeyword_tag()
    {
        return keyword_tag;
    }

    public void setKeyword_tag(String keyword_tag)
    {
        this.keyword_tag = keyword_tag;
    }
}
