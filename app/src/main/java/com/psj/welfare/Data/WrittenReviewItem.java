package com.psj.welfare.data;

public class WrittenReviewItem
{
    private String welf_name;
    private int welf_id;
    private String writer;
    private String content;
    private String create_date;
    private float star_count;
    private int review_id;
    private String difficulty_level; //난이도
    private String satisfaction; //만족도

    public String getDifficulty_level() {
        return difficulty_level;
    }

    public void setDifficulty_level(String difficulty_level) {
        this.difficulty_level = difficulty_level;
    }

    public String getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(String satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public int getWelf_id()
    {
        return welf_id;
    }

    public void setWelf_id(int welf_id)
    {
        this.welf_id = welf_id;
    }

    public String getWriter()
    {
        return writer;
    }

    public void setWriter(String writer)
    {
        this.writer = writer;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getCreate_date()
    {
        return create_date;
    }

    public void setCreate_date(String create_date)
    {
        this.create_date = create_date;
    }

    public float getStar_count()
    {
        return star_count;
    }

    public void setStar_count(float star_count)
    {
        this.star_count = star_count;
    }

    public int getReview_id()
    {
        return review_id;
    }

    public void setReview_id(int review_id)
    {
        this.review_id = review_id;
    }
}
