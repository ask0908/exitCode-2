package com.psj.welfare.data;

import java.io.Serializable;

// 리스트를 다른 액티비티로 넘기고 받기 위해 직렬화 처리
public class BookmarkItem implements Serializable
{
    String id;          // user_bookmark에서 데이터의 인덱스 값
    String welf_name;   // 혜택명
    String welf_id;     // 혜택 자체 id
    String tag;         // 태그
    boolean isSelected; // 체크박스 선택 여부를 확인하기 위해 만든 변수

    private transient boolean tempChecked;
    private transient int tempAttendances;
    private int attendance;

    public BookmarkItem()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getWelf_name()
    {
        return welf_name;
    }

    public void setWelf_name(String welf_name)
    {
        this.welf_name = welf_name;
    }

    public String getWelf_id()
    {
        return welf_id;
    }

    public void setWelf_id(String welf_id)
    {
        this.welf_id = welf_id;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public boolean getSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }

}
