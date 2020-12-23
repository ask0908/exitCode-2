package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.RecommendItem;
import com.psj.welfare.R;

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
        holder.recommend_welf_name.setText(item.getWelf_name());
        // 서버에서 받은 데이터에 들어있는 구분자를 해시태그로 바꾼다
        String tag = item.getTag().replace(";; ", ";;");
        String final_tag = tag.replace(";;", " #");
        holder.recommend_tag.setText("#" + final_tag);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder
    {
        TextView recommend_welf_name, recommend_tag;
        ItemClickListener itemClickListener;

        public RecommendViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            recommend_welf_name = view.findViewById(R.id.recommend_welf_name);
            recommend_tag = view.findViewById(R.id.recommend_tag);

            this.itemClickListener = itemClickListener;
            recommend_welf_name.setOnClickListener(v ->
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
        void onItemClick(View view, int pos);
    }

}
