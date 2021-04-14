package com.psj.welfare.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.HorizontalYoutubeItem;

import java.util.List;

public class TestYoutubeAdapter extends RecyclerView.Adapter<TestYoutubeAdapter.TestYoutubeViewHolder>
{
    private Context context;
    private List<HorizontalYoutubeItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public TestYoutubeAdapter(Context context, List<HorizontalYoutubeItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TestYoutubeAdapter.TestYoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_youtube_item, parent, false);
        return new TestYoutubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestYoutubeAdapter.TestYoutubeViewHolder holder, int position)
    {

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class TestYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        public TestYoutubeViewHolder(@NonNull View view)
        {
            super(view);
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View v, int pos);
    }

}
