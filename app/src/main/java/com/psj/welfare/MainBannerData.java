package com.psj.welfare;

public class MainBannerData {
    private String imageurl;
    private String title;
    private int number;

    public MainBannerData() {   }

    public MainBannerData(String imageurl, String title, int number) {
        this.imageurl = imageurl;
        this.title = title;
        this.number = number;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
