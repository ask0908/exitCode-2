package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.data.ResultKeywordItem;
import com.psj.welfare.R;

import java.util.Collections;
import java.util.List;

public class ResultKeywordAdapter extends RecyclerView.Adapter<ResultKeywordAdapter.ResultKeywordViewHolder>
{
    private final String TAG = "ResultKeywordAdapter";

    private Context context;
    private List<ResultKeywordItem> lists;
    private ItemClickListener itemClickListener;

    private int selected_position = -1;

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
        holder.keyword_category.setText(item.getWelf_category());
        holder.keyword_layout.setTag(position);

        List<String> item_list = Collections.singletonList(lists.get(position).getWelf_category());

        StringBuilder listToString = new StringBuilder();
        for (String str : item_list)
        {
            listToString.append(str);
        }

        String after_str = listToString.toString().split(";; ")[0];

        for (int i = 0; i < lists.size(); i++)
        {
            if (!lists.get(i).getWelf_category().contains(after_str))
            {
                holder.keyword_category.setText(after_str);
            }
        }

        if (selected_position == position)
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.textlines_after);
            holder.keyword_category.setTextColor(Color.parseColor("#EE2F43"));
        }
        else
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.textlines);
            holder.keyword_category.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class ResultKeywordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout keyword_layout;
        TextView keyword_category;
        ItemClickListener itemClickListener;

        public ResultKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            keyword_layout = view.findViewById(R.id.keyword_layout);
            keyword_category = view.findViewById(R.id.keyword_category);

            this.itemClickListener = itemClickListener;
            keyword_category.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);

                    selected_position = pos;
                    notifyDataSetChanged();
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