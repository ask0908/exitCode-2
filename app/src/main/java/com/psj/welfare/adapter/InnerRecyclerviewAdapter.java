package com.psj.welfare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;

public class InnerRecyclerviewAdapter extends RecyclerView.Adapter<InnerRecyclerviewAdapter.InnerRecyclerViewHolder>
{
    public ArrayList<String> nameList;

    public InnerRecyclerviewAdapter(ArrayList<String> nameList)
    {
        this.nameList = nameList;
    }

    @NonNull
    @Override
    public InnerRecyclerviewAdapter.InnerRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_expand_item_view, parent, false);
        return new InnerRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerRecyclerviewAdapter.InnerRecyclerViewHolder holder, int position)
    {
        holder.name.setText(nameList.get(position));
    }

    @Override
    public int getItemCount()
    {
        return nameList.size();
    }

    public class InnerRecyclerViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;

        public InnerRecyclerViewHolder(@NonNull View view)
        {
            super(view);
            name = view.findViewById(R.id.itemTextView);
        }
    }
}
