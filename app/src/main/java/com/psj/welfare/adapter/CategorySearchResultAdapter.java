package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.R;

import java.util.List;

/* ResultBenefitActivity의 세로 리사이클러뷰에 쓰는 어댑터 */
public class CategorySearchResultAdapter extends RecyclerView.Adapter<CategorySearchResultAdapter.CategorySearchResultViewHolder>
{
    private Context context;
    private List<CategorySearchResultItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public CategorySearchResultAdapter(Context context, List<CategorySearchResultItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public CategorySearchResultAdapter.CategorySearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.category_search_result_item, parent, false);
        return new CategorySearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorySearchResultAdapter.CategorySearchResultViewHolder holder, int position)
    {
        CategorySearchResultItem item = list.get(position);
        holder.result_textview.setText(item.getWelf_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class CategorySearchResultViewHolder extends RecyclerView.ViewHolder
    {
        TextView result_textview;
        ItemClickListener itemClickListener;

        public CategorySearchResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
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

    public interface ItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
