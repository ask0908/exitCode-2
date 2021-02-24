package com.benefit.welfare.Data;

public class PushQuestionItem
{
    private String question;
    private String keyword_1;
    private String keyword_2;
    private boolean isSelected = false;

    public PushQuestionItem()
    {
    }

    public PushQuestionItem(String question)
    {
        this.question = question;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String question)
    {
        this.question = question;
    }

    public String getKeyword_1()
    {
        return keyword_1;
    }

    public void setKeyword_1(String keyword_1)
    {
        this.keyword_1 = keyword_1;
    }

    public String getKeyword_2()
    {
        return keyword_2;
    }

    public void setKeyword_2(String keyword_2)
    {
        this.keyword_2 = keyword_2;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
