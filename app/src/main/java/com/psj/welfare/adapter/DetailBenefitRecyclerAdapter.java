package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
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

public class DetailBenefitRecyclerAdapter extends RecyclerView.Adapter<DetailBenefitRecyclerAdapter.DetailBenefitViewHolder>
{
    private final String TAG = "DetailBenefit 어댑터";
    private Context context;
    private List<DetailBenefitItem> item;
    private ItemClickListener itemClickListener;

    public DetailBenefitRecyclerAdapter(Context context, List<DetailBenefitItem> item, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.item = item;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DetailBenefitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.detail_benefit_item, parent, false);
        return new DetailBenefitViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailBenefitViewHolder holder, int position)
    {
        //
    }

    @Override
    public int getItemCount()
    {
        return 3;
    }

    public class DetailBenefitViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout benefit_layout;
        TextView content_title, information_text;
        ImageView more_information;
        ItemClickListener itemClickListener;

        public DetailBenefitViewHolder(View view, ItemClickListener itemClickListener)
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
            Log.e(TAG, "클릭됨");
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
