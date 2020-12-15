package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ResultBenefitItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class RBFTitleAdapter extends RecyclerView.Adapter<RBFTitleAdapter.MyViewHolder>
{
    public Context RBFTitleContext;
    private ArrayList<ResultBenefitItem> RBF_Data;
    private static View.OnClickListener onClickListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView RBF_title;
        public View rootView;

        public MyViewHolder(View view)
        {
            super(view);
            RBF_title = view.findViewById(R.id.RBF_title);
            rootView = view;

            view.setClickable(true);
            view.setEnabled(true);
            view.setOnClickListener(onClickListener);
        }
    }

    public RBFTitleAdapter(Context RBFTitleContext, ArrayList<ResultBenefitItem> RBFTitle_ListSet, View.OnClickListener onClick)
    {
        this.RBFTitleContext = RBFTitleContext;
        this.RBF_Data = RBFTitle_ListSet;
        this.onClickListener = onClick;
    }

    @Override
    public RBFTitleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rbftitle, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RBFTitleAdapter.MyViewHolder holder, int position)
    {
        holder.RBF_title.setText(RBF_Data.get(position).getRBF_Title());
        // tag - label
        holder.rootView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return RBF_Data.size();
    }

    public ResultBenefitItem getRBFTitle(int position)
    {
        return RBF_Data != null ? RBF_Data.get(position) : null;
    }
}
