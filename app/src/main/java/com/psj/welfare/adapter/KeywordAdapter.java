package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.KeywordItem;

import java.util.List;

public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>
{
    private Context context;
    private List<KeywordItem> list;
    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public KeywordAdapter(Context context, List<KeywordItem> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public KeywordAdapter.KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.keyword_item, parent, false);
        return new KeywordViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordAdapter.KeywordViewHolder holder, int position)
    {
        final KeywordItem item = list.get(position);

        // 선택한 관심사 이름이 아이템에 만들어지도록 setText()를 활용함
        holder.selected_keyword_name.setText(item.getName());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class KeywordViewHolder extends RecyclerView.ViewHolder
    {
        TextView selected_keyword_name;
        onItemClickListener itemClickListener;

        public KeywordViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);

            selected_keyword_name = view.findViewById(R.id.selected_keyword_name);
            this.itemClickListener = itemClickListener;
            selected_keyword_name.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onKeywordClick(v, pos);
                    removeItem(pos);
                }
            });
        }
    }

    public void removeItem(int position)
    {
        list.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public interface onItemClickListener
    {
        void onKeywordClick(View v, int position);
    }

}
