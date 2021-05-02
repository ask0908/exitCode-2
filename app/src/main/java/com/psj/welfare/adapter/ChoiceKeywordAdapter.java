package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.data.ChoiceKeywordItem;
import com.psj.welfare.R;

import java.util.List;

/* 아이템 안의 체크박스 - 체크하고 스크롤하다 보면 체크가 해제됨, 다른 곳이 체크되는 현상 */
public class ChoiceKeywordAdapter extends RecyclerView.Adapter<ChoiceKeywordAdapter.ChoiceKeywordViewHolder>
{
    private Context context;
    private List<ChoiceKeywordItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public ChoiceKeywordAdapter(Context context, List<ChoiceKeywordItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ChoiceKeywordAdapter.ChoiceKeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.category_layout, parent, false);
        return new ChoiceKeywordViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChoiceKeywordAdapter.ChoiceKeywordViewHolder holder, int position)
    {
        final ChoiceKeywordItem item = list.get(position);
        holder.search_category_checkbox.setText(item.getInterest());

        holder.search_category_checkbox.setOnCheckedChangeListener(null);

        holder.search_category_checkbox.setChecked(item.getSelected());

        holder.search_category_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                item.setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    static class ChoiceKeywordViewHolder extends RecyclerView.ViewHolder
    {
        CheckBox search_category_checkbox;
        ItemClickListener itemClickListener;

        public ChoiceKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);
            this.setIsRecyclable(false);

            search_category_checkbox = view.findViewById(R.id.search_category_checkbox);

            this.itemClickListener = itemClickListener;
            search_category_checkbox.setOnClickListener(v -> {
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
