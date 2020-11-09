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

public class RBFAdapter extends RecyclerView.Adapter<RBFAdapter.MyViewHolder> {

	public Context RBFContext;
	private ArrayList<ResultBenefitItem> RBF_Data;
	private static View.OnClickListener onClickListener;

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		// each data item is just a string in this case
		public TextView RBF_btn;
		public View rootView;

		public MyViewHolder(View v) {
			super(v);
			RBF_btn = v.findViewById(R.id.RBF_btn);
			rootView = v;

			v.setClickable(true);
			v.setEnabled(true);
			v.setOnClickListener(onClickListener);
		}
	}

	// Provide a suitable constructor (depends on the kind of dataset)
	public RBFAdapter(Context RBFContext, ArrayList<ResultBenefitItem> RBF_ListSet, View.OnClickListener onClick) {
		this.RBFContext = RBFContext;
		this.RBF_Data = RBF_ListSet;
		this.onClickListener = onClick;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public RBFAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
													  int viewType) {
		// create a new view
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_rbfbtn, parent, false);

		MyViewHolder vh = new MyViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		// - get element from your dataset at this position
		// - replace the contents of the view with that element

		holder.RBF_btn.setText(RBF_Data.get(position).getRBF_btn());
		holder.RBF_btn.setBackground(ContextCompat.getDrawable(RBFContext, RBF_Data.get(position).getRBF_btnColor()));
		// tag - label
		holder.rootView.setTag(position);
	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount() {
		return RBF_Data.size();
	}

	public ResultBenefitItem getRBF(int position) {
		return RBF_Data != null ? RBF_Data.get(position) : null;
	}
}
