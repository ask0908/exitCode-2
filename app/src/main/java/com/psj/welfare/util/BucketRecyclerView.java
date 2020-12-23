package com.psj.welfare.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BucketRecyclerView extends RecyclerView
{
    private List<View> mNonEmptyViews = Collections.emptyList();
    private List<View> mEmptyViews = Collections.emptyList();

    private AdapterDataObserver observer = new AdapterDataObserver()
    {
        @Override
        public void onChanged()
        {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount)
        {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload)
        {
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount)
        {
            toggleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount)
        {
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount)
        {
            toggleViews();
        }
    };

    private void toggleViews()
    {
        if (getAdapter() != null && !mEmptyViews.isEmpty() && !mNonEmptyViews.isEmpty())
        {
            // getItemCount()로 가져온 아이템 개수가 0이면
            if (getAdapter().getItemCount() == 0)
            {
                Log.e("toggleViews()", "getItemCount() = " + getAdapter().getItemCount());
                // 비어있는 모든 뷰를 숨긴다
                Util.showViews(mEmptyViews);
                setVisibility(GONE);
                Util.hideViews(mNonEmptyViews);
            }
            else
            {
                Log.e("toggleViews()", "getItemCount() = " + getAdapter().getItemCount());
                Util.showViews(mNonEmptyViews);
                setVisibility(VISIBLE);
                Util.hideViews(mEmptyViews);
            }
        }
    }

    public BucketRecyclerView(@NonNull Context context)
    {
        super(context);
    }

    public BucketRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public BucketRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAdapter(Adapter adapter)
    {
        super.setAdapter(adapter);
        if (adapter != null)
        {
            adapter.registerAdapterDataObserver(observer);
        }
        observer.onChanged();
    }

    public void hideIfEmpty(View ...views)
    {
        mNonEmptyViews = Arrays.asList(views);
    }

    public void showIfEmpty(View ...emptyViews)
    {
        mEmptyViews = Arrays.asList(emptyViews);
    }

}
