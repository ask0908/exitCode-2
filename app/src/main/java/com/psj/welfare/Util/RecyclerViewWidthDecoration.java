package com.psj.welfare.util;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/* 리사이클러뷰 아이템 간의 가로 간격을 조절할 때 쓰는 클래스
* 숫자가 클수록 간격이 넓어지고 작을수록 간격이 좁아진다 */
public class RecyclerViewWidthDecoration extends RecyclerView.ItemDecoration
{
    private final int divWidth;

    public RecyclerViewWidthDecoration(int divWidth)
    {
        this.divWidth = divWidth;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.right = divWidth;
    }
}
