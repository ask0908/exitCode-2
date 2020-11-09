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

public class FirstCategoryAdapter extends RecyclerView.Adapter<FirstCategoryAdapter.ViewHolder> {

	// item class(MainItem)를 정의해 놓았음
	public Context categoryContext;
	private ArrayList<FirstCategoryItem> categoryData;
	private View.OnClickListener onClickListener;

	// 생성자
	public FirstCategoryAdapter(Context CategoryContext, ArrayList<FirstCategoryItem> CategoryDataSet, View.OnClickListener OnClickListener) {
		this.categoryContext = CategoryContext;
		this.categoryData = CategoryDataSet;
		this.onClickListener = OnClickListener;
	}


	public class ViewHolder extends RecyclerView.ViewHolder {

		private TextView firstCategory_Title;
		public LinearLayout firstCategory_line;
		public View rootView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			firstCategory_Title = itemView.findViewById(R.id.firstCategory_Title);
			firstCategory_line = itemView.findViewById(R.id.firstCategory_line);
			rootView = itemView;

			itemView.setClickable(true);
			itemView.setEnabled(true);
			itemView.setOnClickListener(onClickListener);

		}
	}


	@NonNull
	@Override
	public FirstCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_firstcategory, parent, false);
		// set the view's size, margins, paddings and layout parameters

		ViewHolder viewholder = new ViewHolder(view);
		return viewholder;
	}

	@Override
	public void onBindViewHolder(@NonNull FirstCategoryAdapter.ViewHolder viewHolder, int position) {

		viewHolder.firstCategory_Title.setText(categoryData.get(position).getCategoryTitle());
		viewHolder.firstCategory_line.setBackgroundColor(categoryData.get(position).getCategoryBg());


		// Tag - Label 을 달아준다
		viewHolder.rootView.setTag(position);
	}

	@Override
	public int getItemCount() {
		return categoryData.size();
	}

	public FirstCategoryItem getCategory(int position) {
		return categoryData != null ? categoryData.get(position) : null;
	}


}
