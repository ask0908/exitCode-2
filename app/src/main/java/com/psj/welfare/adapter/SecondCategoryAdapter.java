package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SecondCategoryItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class SecondCategoryAdapter extends RecyclerView.Adapter<SecondCategoryAdapter.ViewHolder>
{
    public Context categoryContext;
    private ArrayList<SecondCategoryItem> categoryData;
    private View.OnClickListener onClickListener;

    public SecondCategoryAdapter(Context CategoryContext, ArrayList<SecondCategoryItem> CategoryDataSet, View.OnClickListener OnClickListener)
    {
        this.categoryContext = CategoryContext;
        this.categoryData = CategoryDataSet;
        this.onClickListener = OnClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView secondCategory_Title;
        private LinearLayout secondCategory_line;
        public View rootView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            secondCategory_Title = itemView.findViewById(R.id.secondCategory_Title);
            secondCategory_line = itemView.findViewById(R.id.secondCategory_line);
            rootView = itemView;

            itemView.setClickable(true);
            itemView.setEnabled(true);
            itemView.setOnClickListener(onClickListener);
        }
    }

    @NonNull
    @Override
    public SecondCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_secondcategory, parent, false);
        ViewHolder viewholder = new ViewHolder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(@NonNull SecondCategoryAdapter.ViewHolder viewHolder, int position)
    {
        viewHolder.secondCategory_Title.setText(categoryData.get(position).getCategoryTitle());
        viewHolder.secondCategory_line.setBackgroundColor(categoryData.get(position).getcategoryBg());

        // Tag - Label 을 달아준다
        viewHolder.rootView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return categoryData.size();
    }

    public SecondCategoryItem getCategory(int position)
    {
        return categoryData != null ? categoryData.get(position) : null;
    }

}
