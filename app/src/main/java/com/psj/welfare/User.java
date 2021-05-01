package com.psj.welfare;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Category")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "age") //나이
    public String age;

    @ColumnInfo(name = "gender") //성별
    public String gender;

    @ColumnInfo(name = "home") //지역
    public String home;


    public User(String age, String gender, String home) {
        this.age = age; //나이
        this.gender = gender; //성별
        this.home = home; //지역
    }


}
