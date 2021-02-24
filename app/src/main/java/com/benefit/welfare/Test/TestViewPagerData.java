package com.benefit.welfare.Test;

public class TestViewPagerData
{
    String welf_desc;   // 혜택명
    String welf_number; // 혜택 개수

    public TestViewPagerData(String welf_desc)
    {
        this.welf_desc = welf_desc;
    }

    public String getWelf_desc()
    {
        return welf_desc;
    }

    public void setWelf_desc(String welf_desc)
    {
        this.welf_desc = welf_desc;
    }

    public String getWelf_number()
    {
        return welf_number;
    }

    public void setWelf_number(String welf_number)
    {
        this.welf_number = welf_number;
    }
}
