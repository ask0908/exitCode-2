package com.psj.welfare;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao //(Room이 바라보게 하는) 레파지토리가 되게 하는 어노테이션
public interface UserDao {

    //데이터 모두 조회
    @Query("SELECT * FROM category")
    List<User> findAll();

    //특정 id 데이터만 조회
//    @Query("SELECT * FROM category WHERE temid = :temid")
//    List<User> findid(String temid);

//    @Query("SELECT * FROM category WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM category WHERE age LIKE :first AND " +
//            "gender LIKE :last LIMIT 1")
//    User findByName(String first, String last);

    //데이터 모두 삭제
    @Query("DELETE FROM category")
    void deleteall();


    @Update
    void update(User user);

    //데이터 삽입
    @Insert(onConflict = REPLACE)
    void insert(User user);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
