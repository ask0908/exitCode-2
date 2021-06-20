package com.psj.welfare;

public class BannerDetailData {

    private String BannerId; //배너 아이디
    private String BannerTitle; //배너 타이틀
    private String BannerTag; //배너 태그
    private String BannerContents; //배너 내용

    public BannerDetailData() {
    }

    public BannerDetailData(String bannerId, String bannerTitle, String bannerTag, String bannerContents) {
        BannerId = bannerId;
        BannerTitle = bannerTitle;
        BannerTag = bannerTag;
        BannerContents = bannerContents;
    }

    public String getBannerId() {
        return BannerId;
    }
    public void setBannerId(String bannerId) {
        BannerId = bannerId;
    }
    public String getBannerTitle() {
        return BannerTitle;
    }
    public void setBannerTitle(String bannerTitle) {
        BannerTitle = bannerTitle;
    }
    public String getBannerTag() {
        return BannerTag;
    }
    public void setBannerTag(String bannerTag) {
        BannerTag = bannerTag;
    }
    public String getBannerContents() {
        return BannerContents;
    }
    public void setBannerContents(String bannerContents) {
        BannerContents = bannerContents;
    }
}
