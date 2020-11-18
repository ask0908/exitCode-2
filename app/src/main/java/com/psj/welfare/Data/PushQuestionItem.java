package com.psj.welfare.Data;

import com.google.gson.annotations.Expose;

public class PushQuestionItem
{
    @Expose
    private String question;

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
}
