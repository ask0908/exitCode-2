package com.psj.welfare.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/* 리사이클러뷰에 리뷰 데이터가 없으면 작성된 리뷰가 없다고 띄울 때 사용하기 위한 클래스, xml에서 리사이클러뷰 대신 이 클래스의 경로를 입력해 사용한다
* DetailBenefitActivity, TestSearchResultActivity에서 사용 */
public class RecyclerViewEmptySupport extends RecyclerView
{
    private View emptyImageView;
    private View emptyView;
    private View emptyView2;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver()
    {
        @Override
        public void onChanged()
        {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null)
            {
                if (adapter.getItemCount() == 0)
                {
                    emptyView.setVisibility(View.VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                }
                else
                {
                    emptyView.setVisibility(View.GONE);
                    RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void setAdapter(Adapter adapter)
    {
        super.setAdapter(adapter);

        if (adapter != null)
        {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView)
    {
        this.emptyView = emptyView;
    }

    public void setEmptyView(View emptyImageView, View view, View view2)
    {
        this.emptyImageView = emptyImageView;
        this.emptyView = view;
        this.emptyView2 = view2;
    }

    public RecyclerViewEmptySupport(@NonNull Context context)
    {
        super(context);
    }

    public RecyclerViewEmptySupport(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
}
