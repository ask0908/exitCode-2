package com.psj.welfare;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao //(Room이 바라보게 하는) 레파지토리가 되게 하는 어노테이션
public interface CategoryDao {

    //데이터 모두 조회
    @Query("SELECT * FROM Firstcategory")
    List<CategoryData> findAll();

//    //특정 데이터 조회
//    @Query("SELECT * FROM Firstcategory WHERE temid = 'temid'")
//    CategoryData begingData();

    //데이터 모두 삭제
    @Query("DELETE FROM Firstcategory")
    void deleteall();

    //데이터 업데이트
    @Query("UPDATE Firstcategory SET age = :age, gender = :gender, home = :home WHERE temid = 'temid'")
    void update(String age, String gender,String home);

    //데이터 갯수 조회
//    @Query("SELECT COUNT(*) Firstcategory")
//    int count();

    @Update
    void update(CategoryData data);

    //데이터 삽입
    @Insert(onConflict = REPLACE)
    void insert(CategoryData data);

    @Insert
    void insertAll(CategoryData... datas);

    @Delete
    void delete(CategoryData data);
}
