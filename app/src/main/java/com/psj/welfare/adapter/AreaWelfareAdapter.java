package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.AreaWelfare;

import java.util.List;

public class AreaWelfareAdapter extends RecyclerView.Adapter<AreaWelfareAdapter.AreaWelfareViewHolder>
{
    private Context context;
    private List<AreaWelfare> list;
    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public AreaWelfareAdapter(Context context, List<AreaWelfare> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AreaWelfareAdapter.AreaWelfareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.welfare_category_item, parent, false);
        return new AreaWelfareViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaWelfareAdapter.AreaWelfareViewHolder holder, int position)
    {
        final AreaWelfare item = new AreaWelfare();
        holder.search_category_button.setText("# 건강");
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class AreaWelfareViewHolder extends RecyclerView.ViewHolder
    {
        TextView search_category_button;
        onItemClickListener itemClickListener;

        public AreaWelfareViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);

            search_category_button = view.findViewById(R.id.search_category_button);
            this.itemClickListener = itemClickListener;
            search_category_button.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    public interface onItemClickListener
    {
        void onItemClick(int pos);
    }

}
