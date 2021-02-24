package com.benefit.welfare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.benefit.welfare.Data.EndCategoryItem;
import com.benefit.welfare.R;

import java.util.ArrayList;

public class EndCategoryAdapter extends RecyclerView.Adapter<EndCategoryAdapter.ViewHolder>
{
    public Context categoryContext;
    private ArrayList<EndCategoryItem> categoryData;
    private View.OnClickListener onClickListener;

    public EndCategoryAdapter(Context CategoryContext, ArrayList<EndCategoryItem> CategoryDataSet, View.OnClickListener onClickListener)
    {
        this.categoryContext = CategoryContext;
        this.categoryData = CategoryDataSet;
        this.onClickListener = onClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private LinearLayout EndCategory_line;
        private TextView EndCategory_Title;
        public View rootView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            EndCategory_line = itemView.findViewById(R.id.EndCategory_line);
            EndCategory_Title = itemView.findViewById(R.id.EndCategory_Title);
            rootView = itemView;

            itemView.setClickable(true);
            itemView.setEnabled(true);
            itemView.setOnClickListener(onClickListener);
        }
    }

    @NonNull
    @Override
    public EndCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
    	View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_endcategory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EndCategoryAdapter.ViewHolder viewHolder, int position)
    {
        viewHolder.EndCategory_Title.setText(categoryData.get(position).getCategoryTitle());
        viewHolder.EndCategory_line.setBackgroundColor(categoryData.get(position).getCategoryBg());

        // Tag - Label 을 달아준다
        viewHolder.rootView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return categoryData.size();
    }

}
