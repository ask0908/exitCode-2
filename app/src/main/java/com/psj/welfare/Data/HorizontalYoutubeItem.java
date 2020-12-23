package com.psj.welfare.Data;

/* MainFragment에서 가로로 유튜브 영상들을 보여주는 리사이클러뷰에 사용할 모델 클래스 */
public class HorizontalYoutubeItem
{
    private String category;
    private String youtube_name;
    private String youtube_desc;
    private String youtube_thumbnail;

    /* 하드코딩에 쓰기 위해 추가한 생성자 */
    public HorizontalYoutubeItem(String category, String youtube_name, String youtube_desc)
    {
        this.category = category;
        this.youtube_name = youtube_name;
        this.youtube_desc = youtube_desc;
    }

    public HorizontalYoutubeItem()
    {
    }

    public String getYoutube_thumbnail()
    {
        return youtube_thumbnail;
    }

    public void setYoutube_thumbnail(String youtube_thumbnail)
    {
        this.youtube_thumbnail = youtube_thumbnail;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getYoutube_name()
    {
        return youtube_name;
    }

    public void setYoutube_name(String youtube_name)
    {
        this.youtube_name = youtube_name;
    }

    public String getYoutube_desc()
    {
        return youtube_desc;
    }

    public void setYoutube_desc(String youtube_desc)
    {
        this.youtube_desc = youtube_desc;
    }
}
