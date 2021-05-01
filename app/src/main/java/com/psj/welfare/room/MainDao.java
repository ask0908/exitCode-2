package com.psj.welfare.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.psj.welfare.data.Token;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MainDao
{
    /* 서버에서 토큰을 받으면 INSERT로 DB에 넣고, 재로그인해서 새로 토큰을 받으면 UPDATE로 DB 안의 토큰값을 교체한다
    * MainFragment에서 토큰을 조회할 때는 SELECT로 테이블에 저장된 토큰을 가져와 그걸로 맞춤 혜택을 가져올 수 있게 한다 */
    @Insert(onConflict = REPLACE)
    void insert(Token token);

    @Query("UPDATE room_token SET token = :sToken WHERE id = :sID")
    void update(int sID, String sToken);

    @Query("SELECT * FROM room_token")
    List<Token> getTokens();
}
