package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ThirdCategoryItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class ThirdCategoryAdapter extends RecyclerView.Adapter<ThirdCategoryAdapter.ViewHolder> {

	// item class(MainItem)를 정의해 놓았음
	public Context categoryContext;
	private ArrayList<ThirdCategoryItem> categoryData;
	private View.OnClickListener onClickListener;

	// 생성자
	public ThirdCategoryAdapter(Context CategoryContext, ArrayList<ThirdCategoryItem> CategoryDataSet, View.OnClickListener OnClickListener) {
		this.categoryContext = CategoryContext;
		this.categoryData = CategoryDataSet;
		this.onClickListener = OnClickListener;
	}


	public class ViewHolder extends RecyclerView.ViewHolder {

		private TextView thirdCategory_Title;
		private LinearLayout thirdCategory_line;
		public View rootView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			thirdCategory_Title = itemView.findViewById(R.id.thirdCategory_Title);
			thirdCategory_line = itemView.findViewById(R.id.thirdCategory_line);

			rootView = itemView;

			itemView.setClickable(true);
			itemView.setEnabled(true);
			itemView.setOnClickListener(onClickListener);

		}
	}


	@NonNull
	@Override
	public ThirdCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thirdcategory, parent, false);
		// set the view's size, margins, paddings and layout parameters

		ViewHolder viewholder = new ViewHolder(view);
		return viewholder;
	}

	@Override
	public void onBindViewHolder(@NonNull ThirdCategoryAdapter.ViewHolder viewHolder, int position) {

		viewHolder.thirdCategory_Title.setText(categoryData.get(position).getCategoryTitle());
		viewHolder.thirdCategory_line.setBackgroundColor(categoryData.get(position).getcategoryBg());

		// Tag - Label 을 달아준다
		viewHolder.rootView.setTag(position);
	}

	@Override
	public int getItemCount() {
		return categoryData.size();
	}

	public ThirdCategoryItem getCategory(int position) {
		return categoryData != null ? categoryData.get(position) : null;
	}

}
