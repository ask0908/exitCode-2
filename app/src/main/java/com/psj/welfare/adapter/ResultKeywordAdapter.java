package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;

import java.util.List;

public class ResultKeywordAdapter extends RecyclerView.Adapter<ResultKeywordAdapter.ResultKeywordViewHolder>
{
    private Context context;
    private List<ResultKeywordItem> lists;
    private ItemClickListener itemClickListener;

    public void setOnResultKeywordClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public ResultKeywordAdapter(Context context, List<ResultKeywordItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ResultKeywordAdapter.ResultKeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.result_keyword_item, parent, false);
        return new ResultKeywordViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultKeywordAdapter.ResultKeywordViewHolder holder, int position)
    {
        ResultKeywordItem item = lists.get(position);
        holder.keyword_category.setText(item.getKeyword_category());
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class ResultKeywordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView keyword_category;
        ItemClickListener itemClickListener;

        public ResultKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            keyword_category = view.findViewById(R.id.keyword_category);
            this.itemClickListener = itemClickListener;
            keyword_category.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                    {
                        itemClickListener.onItemClick(v, pos);
                    }
                }
            });
        }

        @Override
        public void onClick(View v)
        {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
