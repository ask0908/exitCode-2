package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ResultBenefitItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class RBFAdapter extends RecyclerView.Adapter<RBFAdapter.MyViewHolder>
{
    public Context RBFContext;
    private ArrayList<ResultBenefitItem> RBF_Data;
    private static View.OnClickListener onClickListener;

    public static class MyViewHolder extends RecyclerView.ViewHolder
    {
        public TextView RBF_btn;
        public View rootView;

        public MyViewHolder(View view)
        {
            super(view);
            RBF_btn = view.findViewById(R.id.RBF_btn);
            rootView = view;

            view.setClickable(true);
            view.setEnabled(true);
            view.setOnClickListener(onClickListener);
        }
    }

    public RBFAdapter(Context RBFContext, ArrayList<ResultBenefitItem> RBF_ListSet, View.OnClickListener onClick)
    {
        this.RBFContext = RBFContext;
        this.RBF_Data = RBF_ListSet;
        this.onClickListener = onClick;
    }

    @Override
    public RBFAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rbfbtn, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        holder.RBF_btn.setText(RBF_Data.get(position).getRBF_btn());
        holder.RBF_btn.setBackground(ContextCompat.getDrawable(RBFContext, RBF_Data.get(position).getRBF_btnColor()));
        // tag - label
        holder.rootView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return RBF_Data.size();
    }

    public ResultBenefitItem getRBF(int position)
    {
        return RBF_Data != null ? RBF_Data.get(position) : null;
    }
}
