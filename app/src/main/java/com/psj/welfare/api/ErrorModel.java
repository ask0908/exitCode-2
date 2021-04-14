package com.psj.welfare.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorModel
{
    @SerializedName("error")
    @Expose
    public String error;

    @Override
    public String toString()
    {
        return "ErrorModel{" +
                "error='" + error + '\'' +
                '}';
    }

}
