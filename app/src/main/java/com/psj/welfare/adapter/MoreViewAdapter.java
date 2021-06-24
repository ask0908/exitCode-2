package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.MoreViewItem;

import java.util.List;

/* 혜택 더보기 리스트 화면에서 상단 리사이클러뷰에 카테고리들을 보여줄 때 사용할 어댑터 */
public class MoreViewAdapter extends RecyclerView.Adapter<MoreViewAdapter.MoreViewHolder>
{
    private final String TAG = MoreViewAdapter.class.getSimpleName();

    private Context context;
    private List<MoreViewItem> list;
    private ItemClickListener itemClickListener;

    private int selected_position = 0;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MoreViewAdapter(Context context, List<MoreViewItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MoreViewAdapter.MoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_up_item, parent, false);
        return new MoreViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MoreViewAdapter.MoreViewHolder holder, int position)
    {
        MoreViewItem item = list.get(position);
        holder.assist_method_text.setText(item.getAssist_method());

        if (selected_position == position)
        {
            holder.assist_method_text.setTextColor(ContextCompat.getColor(context, R.color.layout_background_start_gradation));
            holder.more_result_view.setBackgroundColor(ContextCompat.getColor(context, R.color.layout_background_start_gradation));
            holder.more_result_view.setVisibility(View.VISIBLE);

        }
        else
        {
            holder.assist_method_text.setTextColor(ContextCompat.getColor(context, R.color.middle_gray));
            holder.more_result_view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class MoreViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout interest_layout;
        TextView assist_method_text;
        View more_result_view;
        ItemClickListener itemClickListener;

        public MoreViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            interest_layout = view.findViewById(R.id.interest_layout);
            assist_method_text = view.findViewById(R.id.assist_method_text);
            more_result_view = view.findViewById(R.id.interest_bottom_view);

            this.itemClickListener = itemClickListener;
            interest_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);

                    selected_position = pos;
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(int pos);
    }

}
