package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;

public class ExpandableRecyclerviewAdapter extends RecyclerView.Adapter<ExpandableRecyclerviewAdapter.ExpandableViewHolder>
{
    ArrayList<String> nameList;
    ArrayList<Integer> counter = new ArrayList<>();
    ArrayList<ArrayList<String>> itemNameList;
    Context context;

    public ExpandableRecyclerviewAdapter(Context context, ArrayList<String> nameList, ArrayList<ArrayList<String>> itemNameList)
    {
        this.nameList = nameList;
        this.itemNameList = itemNameList;
        this.context = context;

        for (int i = 0; i < nameList.size(); i++)
        {
            counter.add(0);
        }
    }

    @NonNull
    @Override
    public ExpandableRecyclerviewAdapter.ExpandableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_collapseview, parent, false);
        return new ExpandableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpandableRecyclerviewAdapter.ExpandableViewHolder holder, int position)
    {
        holder.name.setText(nameList.get(position));
        InnerRecyclerviewAdapter itemInnerRecyclerView = new InnerRecyclerviewAdapter(itemNameList.get(position));
        holder.cardRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (counter.get(position) % 4 == 0)
                {
                    holder.cardRecyclerView.setVisibility(View.VISIBLE);
                    holder.dropBtn.setImageResource(R.drawable.circle_minus);
                }
                else
                {
                    holder.cardRecyclerView.setVisibility(View.GONE);
                    holder.dropBtn.setImageResource(R.drawable.circle_plus);
                }
                counter.set(position, counter.get(position) + 1);
            }
        });
        holder.cardRecyclerView.setAdapter(itemInnerRecyclerView);
    }

    @Override
    public int getItemCount()
    {
        return nameList.size();
    }

    public class ExpandableViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        ImageView dropBtn;
        RecyclerView cardRecyclerView;
        CardView cardView;

        public ExpandableViewHolder(@NonNull View view)
        {
            super(view);
            name = itemView.findViewById(R.id.categoryTitle);
            dropBtn = itemView.findViewById(R.id.categoryExpandBtn);
            cardRecyclerView = itemView.findViewById(R.id.innerRecyclerView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
