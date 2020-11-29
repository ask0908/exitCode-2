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

    public ResultKeywordAdapter(Context context, List<ResultKeywordItem> lists)
    {
        this.context = context;
        this.lists = lists;
    }

    @NonNull
    @Override
    public ResultKeywordAdapter.ResultKeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.result_keyword_item, parent, false);
        return new ResultKeywordViewHolder(view);
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

    public class ResultKeywordViewHolder extends RecyclerView.ViewHolder
    {
        TextView keyword_category;

        public ResultKeywordViewHolder(@NonNull View view)
        {
            super(view);

            keyword_category = view.findViewById(R.id.keyword_category);
        }
    }
}
