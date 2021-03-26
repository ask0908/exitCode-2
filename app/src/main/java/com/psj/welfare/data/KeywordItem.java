package com.psj.welfare.data;

import java.util.Objects;

/* 관심사 선택 리사이클러뷰 */
public class KeywordItem
{
    // 관심사 이름
    private String name;

    /* 관심사 이름으로 동적으로 추가되는 아이템을 만들어야 하기 때문에, 생성자를 만들어 동적 추가를 가능할 수 있도록 한다 */
    public KeywordItem()
    {
    }

    public KeywordItem(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof KeywordItem)
        {
            KeywordItem item = (KeywordItem) obj;
            if (this.name.equals(item.name))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
