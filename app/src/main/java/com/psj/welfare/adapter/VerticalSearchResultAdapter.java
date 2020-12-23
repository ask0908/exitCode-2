package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;

import java.util.List;

/* SearchResultActivity의 세로 리사이클러뷰에 쓰이는 어댑터 */
public class VerticalSearchResultAdapter extends RecyclerView.Adapter<VerticalSearchResultAdapter.VerticalSearchResultViewHolder>
{
    private Context context;
    private List<SearchItem> list;
    private VerticalItemClickListener itemClickListener;

    public void setOnItemClickListener(VerticalItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public VerticalSearchResultAdapter(Context context, List<SearchItem> list, VerticalItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public VerticalSearchResultAdapter.VerticalSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.category_search_result_item, parent, false);
        return new VerticalSearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VerticalSearchResultAdapter.VerticalSearchResultViewHolder holder, int position)
    {
        SearchItem item = list.get(position);
        holder.result_textview.setText(item.getWelf_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class VerticalSearchResultViewHolder extends RecyclerView.ViewHolder
    {
        TextView result_textview;
        VerticalItemClickListener itemClickListener;

        public VerticalSearchResultViewHolder(@NonNull View view, VerticalItemClickListener itemClickListener)
        {
            super(view);

            result_textview = view.findViewById(R.id.category_search_result_title);
            this.itemClickListener = itemClickListener;
            result_textview.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });
        }
    }

    public interface VerticalItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
