package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

	// item class(MainItem)를 정의해 놓았음
	public Context searchContext;
	private ArrayList<SearchItem> searchData;
	private View.OnClickListener onClickListener;

	// 생성자
	public SearchAdapter(Context SearchContext, ArrayList<SearchItem> SearchDataSet, View.OnClickListener OnClickListener) {
		this.searchContext = SearchContext;
		this.searchData = SearchDataSet;
		this.onClickListener = OnClickListener;
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		public TextView search_title;
		public View rootView;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			search_title = itemView.findViewById(R.id.search_title);
			rootView = itemView;

			itemView.setClickable(true);
			itemView.setEnabled(true);
			itemView.setOnClickListener(onClickListener);
		}
	}

	@NonNull
	@Override
	public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// create a new view
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
		// set the view's size, margins, paddings and layout parameters

		ViewHolder viewholder = new ViewHolder(view);
		return viewholder;
	}

	@Override
	public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder viewHolder, int position) {

		viewHolder.search_title.setText(searchData.get(position).getSearchTitle());

		// Tag - Label 을 달아준다
		viewHolder.rootView.setTag(position);
	}

	@Override
	public int getItemCount() {
		return searchData.size();
	}

	public SearchItem getSearch(int position) {
		return searchData != null ? searchData.get(position) : null;
	}


}
