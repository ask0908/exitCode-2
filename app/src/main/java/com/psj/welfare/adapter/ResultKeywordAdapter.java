package com.psj.welfare.adapter;

import android.content.Context;
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
        ResultKeywordItem item = lists.get(position);
        holder.keyword_category.setText(item.getParent_category());
        holder.keyword_layout.setTag(position);
//        holder.keyword_category.setOnClickListener(OnSingleClickListener -> {
//            if (OnSingleClickListener.isSelected())
//            {
//                holder.keyword_category.setBackground(ContextCompat.getDrawable(context, R.drawable.keyword_round_textview_purple));
//                holder.keyword_category.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
//                holder.keyword_category.setSelected(!holder.keyword_category.isSelected());
//            }
//            else
//            {
//                holder.keyword_category.setBackground(ContextCompat.getDrawable(context, R.drawable.keyword_round_textview));
//                holder.keyword_category.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
//                holder.keyword_category.setSelected(true);
//            }
//        });
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

    public ResultKeywordItem getRBF(int position)
    {
        return lists != null ? lists.get(position) : null;
    }

}
