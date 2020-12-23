package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ChoiceKeywordItem;
import com.psj.welfare.R;

import java.util.List;

public class ChoiceKeywordAdapter extends RecyclerView.Adapter<ChoiceKeywordAdapter.ChoiceKeywordViewHolder>
{
    private Context context;
    private List<ChoiceKeywordItem> list;
    private ItemClickListener itemClickListener;

    /* 회의 끝나고 한번 실행해서 결과 어떤지 확인하기 */
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
    public void onBindViewHolder(@NonNull ChoiceKeywordAdapter.ChoiceKeywordViewHolder holder, int position)
    {
        ChoiceKeywordItem item = list.get(position);
        holder.search_category_textview.setText(item.getInterest());
        holder.search_category_checkbox.setOnCheckedChangeListener(null);
        holder.search_category_checkbox.setSelected(item.getSelected());
        holder.search_category_checkbox.setTag(list.get(position));
        holder.search_category_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    list.get(position).setSelected(true);
                    Toast.makeText(context, "checked = " + list.get(position).getSelected(), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    list.get(position).setSelected(false);
                    Toast.makeText(context, "checked = " + list.get(position).getSelected(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class ChoiceKeywordViewHolder extends RecyclerView.ViewHolder
    {
        CheckBox search_category_checkbox;
        TextView search_category_textview;
        ItemClickListener itemClickListener;

        public ChoiceKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            search_category_checkbox = view.findViewById(R.id.search_category_checkbox);
            search_category_textview = view.findViewById(R.id.search_category_textview);

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
