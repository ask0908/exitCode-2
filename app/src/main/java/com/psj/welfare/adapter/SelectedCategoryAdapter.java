package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SelectedButtonItem;
import com.psj.welfare.R;

import java.util.List;

public class SelectedCategoryAdapter extends RecyclerView.Adapter<SelectedCategoryAdapter.SelectedCategoryViewHolder>
{
    private Context context;
    private List<SelectedButtonItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SelectedCategoryAdapter(Context context, List<SelectedButtonItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SelectedCategoryAdapter.SelectedCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_category_item, parent, false);
        return new SelectedCategoryViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCategoryAdapter.SelectedCategoryViewHolder holder, int position)
    {
        SelectedButtonItem item = list.get(position);
        holder.category_btn.setText(item.getButton_title());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class SelectedCategoryViewHolder extends RecyclerView.ViewHolder
    {
        TextView category_btn;
        ItemClickListener itemClickListener;

        public SelectedCategoryViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            category_btn = view.findViewById(R.id.category_btn);
            this.itemClickListener = itemClickListener;
            category_btn.setOnClickListener(v -> {
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
