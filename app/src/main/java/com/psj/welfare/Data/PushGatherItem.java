package com.psj.welfare.Data;

public class PushGatherItem
{
    private String welf_name;          // 복지혜택 이름
    private String push_gather_title;  // 받았던 푸시의 제목
    private String push_gather_desc;   // 받았던 푸시의 내용
    private String push_gather_date;   // 푸시를 받은 날(며칠 전 등)

    public PushGatherItem()
    {
    }

    public PushGatherItem(String push_gather_title, String push_gather_desc, String push_gather_date)
    {
        this.push_gather_title = push_gather_title;
        this.push_gather_desc = push_gather_desc;
        this.push_gather_date = push_gather_date;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public String getPush_gather_title()
    {
        return push_gather_title;
    }

    public void setPush_gather_title(String push_gather_title)
    {
        this.push_gather_title = push_gather_title;
    }

    public String getPush_gather_desc()
    {
        return push_gather_desc;
    }

    public void setPush_gather_desc(String push_gather_desc)
    {
        this.push_gather_desc = push_gather_desc;
    }

    public String getPush_gather_date()
    {
        return push_gather_date;
    }

    public void setPush_gather_date(String push_gather_date)
    {
        this.push_gather_date = push_gather_date;
    }
}
