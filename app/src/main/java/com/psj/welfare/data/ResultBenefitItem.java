package com.psj.welfare.data;

public class ResultBenefitItem
{
    String RBF_btn;
    String RBF_Title;
    int RBF_btnColor;
    String RBF_titleNum;

    // 검색 api 리뉴얼로 인해 추가한 변수
	String welf_name;
	String welf_category;
	String tag;
	String welf_local;

    public ResultBenefitItem()
    {
    }

    public ResultBenefitItem(String RBF_btn, int RBF_btnColor)
    {
        this.RBF_btn = RBF_btn;
        this.RBF_btnColor = RBF_btnColor;
    }

    public ResultBenefitItem(String RBF_Title)
    {
        this.RBF_Title = RBF_Title;
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

	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getWelf_local()
	{
		return welf_local;
	}

	public void setWelf_local(String welf_local)
	{
		this.welf_local = welf_local;
	}

	// -------------------------------

	public String getRBF_titleNum()
    {
        return RBF_titleNum;
    }

    public void setRBF_titleNum(String RBF_titleNum)
    {
        this.RBF_titleNum = RBF_titleNum;
    }

    public void setRBF_btn(String RBF_btn)
    {
        this.RBF_btn = RBF_btn;
    }

    public void setRBF_Title(String RBF_Title)
    {
        this.RBF_Title = RBF_Title;
    }

    public String getRBF_btn()
    {
        return RBF_btn;
    }

    public String getRBF_Title()
    {
        return RBF_Title;
    }

    public void setRBF_btnColor(int RBF_btnColor)
    {
        this.RBF_btnColor = RBF_btnColor;
    }

    public int getRBF_btnColor()
    {
        return RBF_btnColor;
    }
}
