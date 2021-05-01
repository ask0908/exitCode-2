package com.psj.welfare.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;
import java.util.List;

public class TestUpAdapter extends RecyclerView.Adapter<TestUpAdapter.TestUpViewHolder>
{
    private Context context;
    private List<TestUpModel> list = new ArrayList<>();
    private ItemClickListener itemClickListener;

    private int selected_position = 0;

    public TestUpAdapter(Context context, List<TestUpModel> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TestUpAdapter.TestUpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_up_item, parent, false);
        return new TestUpViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TestUpAdapter.TestUpViewHolder holder, int position)
    {
        final TestUpModel model = list.get(position);
        holder.interest_text.setText(model.getWelf_category());

        if (selected_position == position)
        {
            holder.interest_text.setTextColor(ContextCompat.getColor(context, R.color.layout_background_start_gradation));
            holder.interest_bottom_view.setBackgroundColor(ContextCompat.getColor(context, R.color.layout_background_start_gradation));
        }
        else
        {
            holder.interest_text.setTextColor(ContextCompat.getColor(context, R.color.middle_gray));
            holder.interest_bottom_view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount()
    {
        //어레이 리스트가 null이 아니면 어레이리스트 size를 가져오고 null이면 0을 가져와라
        return (list != null ? list.size() : 0);
    }

    public class TestUpViewHolder extends RecyclerView.ViewHolder
    {
        TextView interest_text;
        View interest_bottom_view;
        ItemClickListener itemClickListener;

        public TestUpViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            interest_text = view.findViewById(R.id.interest_text);
            interest_bottom_view = view.findViewById(R.id.interest_bottom_view);

            this.itemClickListener = itemClickListener;
            interest_text.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);

                    selected_position = pos;
                    notifyDataSetChanged();
                }

            });
        }

    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
