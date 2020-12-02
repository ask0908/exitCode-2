package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.PushGatherItem;
import com.psj.welfare.R;

import java.util.List;

public class PushGatherAdapter extends RecyclerView.Adapter<PushGatherAdapter.PushGatherViewHolder>
{
    private Context context;
    private List<PushGatherItem> lists;
    private ItemClickListener itemClickListener;

    public PushGatherAdapter(Context context, List<PushGatherItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PushGatherAdapter.PushGatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.push_gather_item, parent, false);
        return new PushGatherViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PushGatherAdapter.PushGatherViewHolder holder, int position)
    {
        PushGatherItem item = lists.get(position);
        holder.push_gather_title.setText(item.getPush_gather_title());
        holder.push_gather_desc.setText(item.getPush_gather_desc());
        holder.push_gather_date.setText(item.getPush_gather_date());
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class PushGatherViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout push_gather_item_layout;
        TextView push_gather_title, push_gather_desc, push_gather_date;
        ItemClickListener itemClickListener;

        public PushGatherViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            push_gather_item_layout = view.findViewById(R.id.push_gather_item_layout);
            push_gather_title = view.findViewById(R.id.push_gather_title);
            push_gather_desc = view.findViewById(R.id.push_gather_desc);
            push_gather_date = view.findViewById(R.id.push_gather_date);

            this.itemClickListener = itemClickListener;
            push_gather_item_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
