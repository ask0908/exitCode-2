package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.AgeGroupItem;

import java.util.ArrayList;

public class AgeGroupAdapter extends RecyclerView.Adapter<AgeGroupAdapter.AgeGroupViewHolder>
{
    private Context context;
    private ArrayList<AgeGroupItem> list;
    public onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public AgeGroupAdapter(Context context, ArrayList<AgeGroupItem> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AgeGroupAdapter.AgeGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.welfare_category_item, parent, false);
        return new AgeGroupViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AgeGroupAdapter.AgeGroupViewHolder holder, int position)
    {
        final AgeGroupItem item = list.get(position);
        holder.search_age_button.setText(item.getAge());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class AgeGroupViewHolder extends RecyclerView.ViewHolder
    {
        TextView search_age_button;
        onItemClickListener itemClickListener;

        public AgeGroupViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);
            search_age_button = view.findViewById(R.id.search_category_button);

            this.itemClickListener = itemClickListener;
            search_age_button.setOnClickListener(v -> {
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
