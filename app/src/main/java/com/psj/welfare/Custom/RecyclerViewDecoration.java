package com.psj.welfare.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/* 리사이클러뷰 아이템 간의 가로 간격을 조절할 때 쓰는 클래스 */
public class RecyclerViewDecoration extends RecyclerView.ItemDecoration
{
    private final int divWidth;

    public RecyclerViewDecoration(int divWidth)
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
