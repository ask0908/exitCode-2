package com.psj.welfare.data;

/* ChooseFirstInterestActivity에서 Gson으로 JSON 파싱 시 사용하는 모델 클래스 */
public class MyInterest
{
    private String age;             // 나이(10대 미만, 10대, 20대...60대 이상)
    private String local;           // 지역(서울, 경기)
    private String category;        // 카테고리(군인/보훈대상자, 농축산인, 여성)
    private String family;          // 가구 형태(다문화, 다자녀, 소년소녀가장)

    public MyInterest(String age, String local, String category, String family)
    {
        this.age = age;
        this.local = local;
        this.category = category;
        this.family = family;
    }

    public String getAge()
    {
        return age;
    }

    public void setAge(String age)
    {
        this.age = age;
    }

    public String getLocal()
    {
        return local;
    }

    public void setLocal(String local)
    {
        this.local = local;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getFamily()
    {
        return family;
    }

    public void setFamily(String family)
    {
        this.family = family;
    }
}
