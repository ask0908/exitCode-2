package com.psj.welfare.Test;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

public class ViewHolderPage extends RecyclerView.ViewHolder
{
    private TextView textView;
    private FrameLayout layout;

    TestViewPagerData data;

    public ViewHolderPage(@NonNull View view)
    {
        super(view);
        textView = view.findViewById(R.id.welfare_desc);
        layout = view.findViewById(R.id.test_item_layout);
    }

    /* 여기서 뷰페이저2의 텍스트뷰에 보일 텍스트를 정하면 그대로 보여진다 */
    public void onBind(TestViewPagerData data)
    {
        this.data = data;

        textView.setText(data.getWelf_desc());
    }

}
