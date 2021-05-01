package com.psj.welfare;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Firstcategory")
public class CategoryData {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "temid") //임시 id값
    public String temid;

    @ColumnInfo(name = "age") //나이
    public String age;

    @ColumnInfo(name = "gender") //성별
    public String gender;

    @ColumnInfo(name = "home") //지역
    public String home;


    public CategoryData(String temid, String age, String gender, String home) {
        this.temid = temid; //임시 id값
        this.age = age; //나이
        this.gender = gender; //성별
        this.home = home; //지역
    }
}
