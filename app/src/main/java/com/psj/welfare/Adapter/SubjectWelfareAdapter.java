package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.SubjectWelfareItem;

import java.util.ArrayList;

public class SubjectWelfareAdapter extends RecyclerView.Adapter<SubjectWelfareAdapter.SubjectWelfareViewHolder>
{
    private Context context;
    private ArrayList<SubjectWelfareItem> list;
    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SubjectWelfareAdapter(Context context, ArrayList<SubjectWelfareItem> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SubjectWelfareAdapter.SubjectWelfareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.welfare_category_item, parent, false);
        return new SubjectWelfareViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectWelfareAdapter.SubjectWelfareViewHolder holder, int position)
    {
        final SubjectWelfareItem item = list.get(position);
        holder.subject_welfare_textview.setText(item.getSubject());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class SubjectWelfareViewHolder extends RecyclerView.ViewHolder
    {
        TextView subject_welfare_textview;
        onItemClickListener itemClickListener;

        public SubjectWelfareViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);
            subject_welfare_textview = view.findViewById(R.id.search_category_button);

            this.itemClickListener = itemClickListener;
            subject_welfare_textview.setOnClickListener(v -> {
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
