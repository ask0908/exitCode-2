package com.psj.welfare.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.RecommendItem;

import java.util.List;

public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>
{
    private Context context;
    private List<RecommendItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public RecommendAdapter(Context context, List<RecommendItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecommendAdapter.RecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.recommend_item, parent, false);
        return new RecommendViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendAdapter.RecommendViewHolder holder, int position)
    {
        RecommendItem item = list.get(position);
        if (holder.recommend_welf_name.getText().toString().length() < 15)
        {
            holder.recommend_welf_name.setGravity(Gravity.CENTER);
            holder.recommend_welf_name.setText(item.getWelf_name());
        }
        else
        {
            holder.recommend_welf_name.setText(item.getWelf_name());
        }
        holder.recommend_local.setText("#" + list.get(position).getWelf_local());
        holder.recommend_page_text.setText((position + 1) + "/" + list.size());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout recommend_layout;
        Button recommend_detail_btn;
        TextView recommend_welf_name, recommend_local, recommend_page_text;
        ItemClickListener itemClickListener;

        public RecommendViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            recommend_layout = view.findViewById(R.id.recommend_layout);
            recommend_welf_name = view.findViewById(R.id.recommend_welf_name);
            recommend_detail_btn = view.findViewById(R.id.recommend_detail_btn);
            recommend_local = view.findViewById(R.id.recommend_local);
            recommend_page_text = view.findViewById(R.id.recommend_page_text);

            this.itemClickListener = itemClickListener;
            recommend_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });

            recommend_detail_btn.setOnClickListener(v -> {
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
        void onItemClick(View view, int pos);
    }

}
