package com.psj.welfare.Test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.List;

public class TestViewPagerAdapter extends RecyclerView.Adapter<ViewHolderPage>
{
    private Context context;
    private List<TestViewPagerData> list;

    public TestViewPagerAdapter(Context context, List<TestViewPagerData> list)
    {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolderPage onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.view_pager2_item, parent, false);
        return new ViewHolderPage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderPage holder, int position)
    {
        if (holder instanceof ViewHolderPage)
        {
            ViewHolderPage page = (ViewHolderPage) holder;
            page.onBind(list.get(position));
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }
}
