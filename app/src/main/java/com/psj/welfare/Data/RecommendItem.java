package com.psj.welfare.data;

/* MainFragment에서 가로로 맞춤 혜택들을 리사이클러뷰로 보여줄 때 사용하는 모델 클래스 */
public class RecommendItem
{
    private String welf_name;
    private String welf_local;
    private String welf_category;
    private String tag;

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
