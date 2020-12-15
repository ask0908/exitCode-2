package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.psj.welfare.Data.FirstCategoryItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class FirstCategoryAdapter extends RecyclerView.Adapter<FirstCategoryAdapter.FirstCategoryViewHolder>
{
    public Context categoryContext;
    private ArrayList<FirstCategoryItem> categoryData;
    private View.OnClickListener onClickListener;

    public FirstCategoryAdapter(Context CategoryContext, ArrayList<FirstCategoryItem> CategoryDataSet, View.OnClickListener OnClickListener)
    {
        this.categoryContext = CategoryContext;
        this.categoryData = CategoryDataSet;
        this.onClickListener = OnClickListener;
    }

    public class FirstCategoryViewHolder extends RecyclerView.ViewHolder
    {
        private TextView firstCategory_Title;
        public LinearLayout firstCategory_line;
        public View rootView;

        public FirstCategoryViewHolder(@NonNull View view)
        {
            super(view);
            firstCategory_Title = view.findViewById(R.id.firstCategory_Title);
            firstCategory_line = view.findViewById(R.id.firstCategory_line);
            rootView = view;

            view.setClickable(true);
            view.setEnabled(true);
            view.setOnClickListener(onClickListener);
        }
    }

    @NonNull
    @Override
    public FirstCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
    	View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_firstcategory, parent, false);
        return new FirstCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FirstCategoryViewHolder firstCategoryViewHolder, int position)
    {
        firstCategoryViewHolder.firstCategory_Title.setText(categoryData.get(position).getCategoryTitle());
        firstCategoryViewHolder.firstCategory_line.setBackgroundColor(categoryData.get(position).getCategoryBg());
        // Tag - Label 을 달아준다
        firstCategoryViewHolder.rootView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return categoryData.size();
    }

}
