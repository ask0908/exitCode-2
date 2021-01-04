package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.R;

import java.util.List;

/* ResultBenefitActivity의 가로 리사이클러뷰(category_recycler)에 쓰는 어댑터 */
public class SelectedCategoryAdapter extends RecyclerView.Adapter<SelectedCategoryAdapter.SelectedCategoryViewHolder>
{
    private Context context;
    private List<CategorySearchResultItem> list;
    private ItemClickListener itemClickListener;

    public static int selected_item = 0;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SelectedCategoryAdapter(Context context, List<CategorySearchResultItem> list, ItemClickListener itemClickListener)
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
    public SelectedCategoryAdapter.SelectedCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_category_item, parent, false);
        return new SelectedCategoryViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCategoryAdapter.SelectedCategoryViewHolder holder, int position)
    {
        CategorySearchResultItem item = list.get(position);
        holder.category_btn.setText(item.getWelf_category());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class SelectedCategoryViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout sub_category_layout;
        TextView category_btn;
        ItemClickListener itemClickListener;

        public SelectedCategoryViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            sub_category_layout = view.findViewById(R.id.sub_category_layout);
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
