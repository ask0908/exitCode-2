package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.data.CategorySearchResultItem;
import com.psj.welfare.R;

import java.util.Collections;
import java.util.List;

/* 관심사 선택 후 이동하는 ResultBenefitActivity의 가로 리사이클러뷰(category_recycler)에 카테고리명들을 붙이는 어댑터 */
public class SelectedCategoryAdapter extends RecyclerView.Adapter<SelectedCategoryAdapter.SelectedCategoryViewHolder>
{
    private Context context;
    private List<CategorySearchResultItem> list;
    private ItemClickListener itemClickListener;

    // 아이템 색 바꿀 때 쓰는 변수
    private int selected_position = -1;

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
        View view = LayoutInflater.from(context).inflate(R.layout.unselected_category_item, parent, false);
        return new SelectedCategoryViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCategoryAdapter.SelectedCategoryViewHolder holder, int position)
    {
        CategorySearchResultItem item = list.get(position);
        // 현금 지원 뒤에 ;;가 붙어서 이것에 대한 예외처리
        List<String> item_list = Collections.singletonList(list.get(position).getWelf_category());
        for (int i = 0; i < item_list.size(); i++)
        {
            Log.e("zzz", "item_list = " + item_list);
        }
        StringBuilder listToString = new StringBuilder();
        for (String str : item_list)
        {
            listToString.append(str);
        }
//        Log.e("zzz", "listToString : " + listToString);
        String after_str = listToString.toString().split(";;")[0];
        holder.category_btn.setText(after_str);

        /* 필터 색 바꾸기 */
        if (selected_position == position)
        {
            holder.sub_category_layout.setBackgroundResource(R.drawable.textlines_after);
            holder.category_btn.setTextColor(Color.parseColor("#EE2F43"));
        }
        else
        {
            holder.sub_category_layout.setBackgroundResource(R.drawable.textlines);
            holder.category_btn.setTextColor(Color.parseColor("#000000"));
        }
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

            sub_category_layout = view.findViewById(R.id.search_category_layout);
            category_btn = view.findViewById(R.id.search_category_btn);

            this.itemClickListener = itemClickListener;
            category_btn.setOnClickListener(v -> {
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
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
