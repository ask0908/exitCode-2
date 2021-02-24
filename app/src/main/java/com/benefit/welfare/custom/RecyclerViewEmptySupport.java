package com.benefit.welfare.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/* 리사이클러뷰에 리뷰 데이터가 없으면 작성된 리뷰가 없다고 띄울 때 사용하기 위한 클래스, xml에서 리사이클러뷰 대신 이 클래스의 경로를 입력해 사용한다
* DetailBenefitActivity에서 사용 */
public class RecyclerViewEmptySupport extends RecyclerView
{
    private View emptyView;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver()
    {
        @Override
        public void onChanged()
        {
            // 현재 리사이클러뷰에 지정된 어댑터를 가져온다
            Adapter<?> adapter = getAdapter();
            // 어댑터 널 체크
            if (adapter != null && emptyView != null)
            {
                // getItemCount()로 가져온 데이터가 없으면 리사이클러뷰를 숨기고 텍스트뷰를 보이게 한다
                if (adapter.getItemCount() == 0)
                {
                    emptyView.setVisibility(View.VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                }
                // 데이터가 있으면 텍스트뷰를 숨기고 리사이클러뷰를 보여준다
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

        // 어댑터가 있으면 어댑터 변경사항을 확인하기 위해 AdapterDataObserver 객체를 붙인다
        if (adapter != null)
        {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        // 어댑터가 없는 경우
        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView)
    {
        this.emptyView = emptyView;
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
