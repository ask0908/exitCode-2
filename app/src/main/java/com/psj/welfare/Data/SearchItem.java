package com.psj.welfare.Data;

/* SearchFragment에서 사용된 SearchAdapter에서 리사이클러뷰 표현 시 쓰이는 모델 클래스 */
public class SearchItem
{
    String searchTitle;

    /* 검색 API 리뉴얼 이후 추가한 변수 */
    String welf_name;           // 혜택 이름
    String welf_local;          // 혜택이 실시되는 지역명
    String parent_category;     // 상위 카테고리
    String welf_category;       // 혜택이 속한 카테고리(하위 카테고리)
    String tag;                 // 혜택과 연결된 태그

    public SearchItem()
    {
    }

    public SearchItem(String welf_name, String welf_local, String parent_category, String welf_category, String tag)
    {
        this.welf_name = welf_name;
        this.welf_local = welf_local;
        this.parent_category = parent_category;
        this.welf_category = welf_category;
        this.tag = tag;
    }

    public SearchItem(String searchTitle)
    {
        this.searchTitle = searchTitle;
    }

    public void setSearchTitle(String searchTitle)
    {
        this.searchTitle = searchTitle;
    }

    public String getSearchTitle()
    {
        return searchTitle;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public String getWelf_local()
    {
        return welf_local;
    }

    public void setWelf_local(String welf_local)
    {
        this.welf_local = welf_local;
    }

    public String getParent_category()
    {
        return parent_category;
    }

    public void setParent_category(String parent_category)
    {
        this.parent_category = parent_category;
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

}
