package com.psj.welfare.util;

import android.view.View;

import java.util.List;

public class Util
{
    /* 리사이클러뷰에 아이템이 있을 경우 보여주는 메서드 */
    public static void showViews(List<View> views)
    {
        for (View view : views)
        {
            view.setVisibility(View.VISIBLE);
        }
    }

    /* 리사이클러뷰에 아이템이 없으면 인자로 들어온 뷰를 사라지게 하는 메서드 */
    public static void hideViews(List<View> views)
    {
        for (View view : views)
        {
            view.setVisibility(View.GONE);
        }
    }

}
