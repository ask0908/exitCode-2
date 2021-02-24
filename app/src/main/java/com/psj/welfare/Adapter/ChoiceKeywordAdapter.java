package com.psj.welfare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ChoiceKeywordItem;
import com.psj.welfare.R;

import java.util.List;

/* 아이템 안의 체크박스 - 체크하고 스크롤하다 보면 체크가 해제됨, 다른 곳이 체크되는 현상 */
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
        // final로 선언해야 체크박스의 체크 상태값(T/F)이 바뀌지 않는다
        final ChoiceKeywordItem item = list.get(position);
        holder.search_category_textview.setText(item.getInterest());

        // 먼저 체크박스의 리스너를 null로 초기화한다
        holder.search_category_checkbox.setOnCheckedChangeListener(null);

        // 모델 클래스의 게터로 체크 상태값을 가져온다
        holder.search_category_checkbox.setChecked(item.getSelected());

        // 체크박스의 상태값을 알기 위해 리스너 부착
        holder.search_category_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // 여기의 item은 final 키워드를 붙인 모델 클래스의 객체와 동일하다
                item.setSelected(isChecked);
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