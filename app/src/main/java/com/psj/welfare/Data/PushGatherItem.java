package com.psj.welfare.Data;

public class PushGatherItem
{
    private String welf_name;           // 복지혜택 이름
    private String welf_local;          // 혜택 실시 지역
    private String push_gather_title;   // 받았던 푸시의 제목
    private String push_gather_desc;    // 받았던 푸시의 내용
    private String push_gather_date;    // 푸시를 받은 날(며칠 전 등)
    private String welf_category;       // OO 지원
    private String tag;                 // 학자금;;대출;;장애인
    private String welf_period;         // 문의처로문의
    private String welf_end;            // 사업종료시까지

    public PushGatherItem()
    {
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

    public String getWelf_period()
    {
        return welf_period;
    }

    public void setWelf_period(String welf_period)
    {
        this.welf_period = welf_period;
    }

    public String getWelf_end()
    {
        return welf_end;
    }

    public void setWelf_end(String welf_end)
    {
        this.welf_end = welf_end;
    }
}
