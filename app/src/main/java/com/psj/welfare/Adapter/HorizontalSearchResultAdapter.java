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

import com.psj.welfare.data.SearchItem;
import com.psj.welfare.R;

import java.util.List;

public class HorizontalSearchResultAdapter extends RecyclerView.Adapter<HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder>
{
    private Context context;
    private List<SearchItem> list;
    private ItemClickListener itemClickListener;

    private int selected_position = -1;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public HorizontalSearchResultAdapter(Context context, List<SearchItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.unselected_category_item, parent, false);
        return new HorizontalSearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder holder, int position)
    {
        SearchItem item = list.get(position);
        holder.category_btn.setText(item.getWelf_category());

        if (selected_position == position)
        {
            holder.sub_category_layout.setBackgroundResource(R.drawable.textlines_after);
            holder.category_btn.setTextColor(Color.parseColor("#EE2F43"));
        }
        else
        {
            holder.sub_category_layout.setBackgroundResource(R.drawable.textlines);
            holder.category_btn.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class HorizontalSearchResultViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout sub_category_layout;
        TextView category_btn;
        ItemClickListener itemClickListener;

        public HorizontalSearchResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            sub_category_layout = view.findViewById(R.id.search_category_layout);
            category_btn = view.findViewById(R.id.search_category_btn);

            this.itemClickListener = itemClickListener;
            category_btn.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);

                    selected_position = pos;
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
