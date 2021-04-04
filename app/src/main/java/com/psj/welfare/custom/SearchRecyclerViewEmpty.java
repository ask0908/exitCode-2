package com.psj.welfare.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class SearchRecyclerViewEmpty extends RecyclerView
{
    private View emptyImageView;
    private View emptyView;
    private View emptyView2;

    public SearchRecyclerViewEmpty(@NonNull Context context)
    {
        super(context);
    }

    public SearchRecyclerViewEmpty(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SearchRecyclerViewEmpty(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void setEmptyView(View emptyImageView, View view, View view2)
    {
        this.emptyImageView = emptyImageView;
        this.emptyView = view;
        this.emptyView2 = view2;
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver()
    {
        @Override
        public void onChanged()
        {
            Adapter<?> adapter = getAdapter();
            if (adapter != null && emptyView != null && emptyImageView != null && emptyView2 != null)
            {
                if (adapter.getItemCount() == 0)
                {
                    emptyImageView.setVisibility(VISIBLE);
                    emptyView.setVisibility(VISIBLE);
                    emptyView2.setVisibility(VISIBLE);
                    SearchRecyclerViewEmpty.this.setVisibility(View.GONE);
                }
                else
                {
                    emptyImageView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    emptyView2.setVisibility(View.GONE);
                    SearchRecyclerViewEmpty.this.setVisibility(VISIBLE);
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

}
