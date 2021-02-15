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

import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;

import java.util.List;

/* MapDetailActivity에서 상단 리사이클러뷰에 카테고리들을 세팅하는 리사이클러뷰에서 사용하는 어댑터 */
public class ResultKeywordAdapter extends RecyclerView.Adapter<ResultKeywordAdapter.ResultKeywordViewHolder>
{
    private Context context;
    private List<ResultKeywordItem> lists;
    private ItemClickListener itemClickListener;

    // 아이템 색 바꿀 때 쓰는 변수
    private int selected_position = -1;

    public void setOnResultKeywordClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public ResultKeywordAdapter(Context context, List<ResultKeywordItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ResultKeywordAdapter.ResultKeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.result_keyword_item, parent, false);
        return new ResultKeywordViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultKeywordAdapter.ResultKeywordViewHolder holder, int position)
    {
        holder.setIsRecyclable(false);
        ResultKeywordItem item = lists.get(position);
        holder.keyword_category.setText(item.getParent_category());
        holder.keyword_layout.setTag(position);

        /* 필터 색 바꾸기 */
        if (selected_position == position)
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.select_after);
            holder.keyword_category.setTextColor(Color.parseColor("#FFFFFF"));
        }
        else
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.rbf_btn_before);
            holder.keyword_category.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class ResultKeywordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout keyword_layout;
        TextView keyword_category;
        ItemClickListener itemClickListener;

        public ResultKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            keyword_layout = view.findViewById(R.id.keyword_layout);
            keyword_category = view.findViewById(R.id.keyword_category);

            this.itemClickListener = itemClickListener;
            keyword_category.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);

                    /* 필터 색 바꾸기 */
                    selected_position = pos;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onClick(View v)
        {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
