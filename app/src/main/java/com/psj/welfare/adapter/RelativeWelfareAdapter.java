package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.DetailBenefitItem;
import com.psj.welfare.R;

import java.util.List;

public class RelativeWelfareAdapter extends RecyclerView.Adapter<RelativeWelfareAdapter.RelativeViewHolder>
{
    private Context context;
    private List<DetailBenefitItem> list;
    private ItemClickListener itemClickListener;

    public RelativeWelfareAdapter(Context context, List<DetailBenefitItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RelativeWelfareAdapter.RelativeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.relative_welfare_item, parent, false);
        return new RelativeViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RelativeWelfareAdapter.RelativeViewHolder holder, int position)
    {
        //
    }

    @Override
    public int getItemCount()
    {
        return 5;
    }

    public class RelativeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout benefit_layout;
        TextView content_title, information_text;
        ImageView more_information;
        ItemClickListener itemClickListener;

        public RelativeViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            benefit_layout = view.findViewById(R.id.benefit_layout);
            content_title = view.findViewById(R.id.content_title);
            information_text = view.findViewById(R.id.information_text);
            more_information = view.findViewById(R.id.more_information);

            this.itemClickListener = itemClickListener;
            benefit_layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
            {
                itemClickListener.onItemClick(v, pos);
            }
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
