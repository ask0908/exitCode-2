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

import java.util.ArrayList;
import java.util.List;

/* SearchResultActivity의 가로 리사이클러뷰에 쓰이는 어댑터
* 하위 카테고리들을 보여주는데 중복된 카테고리들을 쳐내고 하나만 보이게 해야 함 */
public class HorizontalSearchResultAdapter extends RecyclerView.Adapter<HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder>
{
    private Context context;
    private List<SearchItem> list;
    private ItemClickListener itemClickListener;

    // 중복 처리에 사용하는 리스트
    List<String> str_list = new ArrayList<>();

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public HorizontalSearchResultAdapter(Context context, List<SearchItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_category_item, parent, false);
        return new HorizontalSearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalSearchResultAdapter.HorizontalSearchResultViewHolder holder, int position)
    {
        SearchItem item = list.get(position);
        holder.category_btn.setText(item.getParent_category());
        str_list.add(item.getParent_category());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class HorizontalSearchResultViewHolder extends RecyclerView.ViewHolder
    {
        TextView category_btn;
        ItemClickListener itemClickListener;

        public HorizontalSearchResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

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
        void onItemClick(View view, int pos);
    }

}
