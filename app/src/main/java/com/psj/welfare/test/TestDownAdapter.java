package com.psj.welfare.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.List;

public class TestDownAdapter extends RecyclerView.Adapter<TestDownAdapter.TestDownViewHolder>
{
    private Context context;
    private List<TestUpModel> list;
    private ItemClickListener itemClickListener;

    public TestDownAdapter(Context context, List<TestUpModel> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TestDownAdapter.TestDownViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_down_item, parent, false);
        return new TestDownViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TestDownAdapter.TestDownViewHolder holder, int position)
    {
        final TestUpModel model = list.get(position);
        holder.bottom_result_name.setText(model.getWelf_name());
        holder.bottom_result_subject.setText(model.getParent_category());
    }

    @Override
    public int getItemCount()
    {
        return 3;
    }

    public class TestDownViewHolder extends RecyclerView.ViewHolder
    {
        CardView bottom_result_layout;
        TextView bottom_result_name, bottom_result_subject, bottom_result_views;
        ItemClickListener itemClickListener;

        public TestDownViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);
            bottom_result_layout = view.findViewById(R.id.bottom_result_layout);
            bottom_result_name = view.findViewById(R.id.bottom_result_name);
            bottom_result_subject = view.findViewById(R.id.bottom_result_subject);
            bottom_result_views = view.findViewById(R.id.bottom_result_views);

            this.itemClickListener = itemClickListener;
            bottom_result_layout.setOnClickListener(v -> {
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
        void onItemClick(View v, int pos);
    }

}
