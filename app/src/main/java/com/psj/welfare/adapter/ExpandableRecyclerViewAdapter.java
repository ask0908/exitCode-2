package com.psj.welfare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;

public class ExpandableRecyclerViewAdapter extends RecyclerView.Adapter<ExpandableRecyclerViewAdapter.ViewHolder>
{
    private final String TAG = ExpandableRecyclerViewAdapter.class.getSimpleName();

    public static ArrayList<String> nameList;
    public static ArrayList<String> ageList;
    public static ArrayList<String> localList;
    public static ArrayList<String> provideTypeList;
    ArrayList<Integer> counter = new ArrayList<>();
    ArrayList<ArrayList> itemNameList;  // 필터 항목들이 들어있는 리스트
    Context context;

    public ExpandableRecyclerViewAdapter(Context context,
                                         ArrayList<String> nameList,
                                         ArrayList<String> ageList,
                                         ArrayList<String> localList,
                                         ArrayList<String> provideTypeList,
                                         ArrayList<ArrayList> itemNameList)
    {
        this.nameList = nameList;
        this.ageList = ageList;
        this.localList = localList;
        this.context = context;
        this.provideTypeList = provideTypeList;
        this.itemNameList = itemNameList;

        for (int i = 0; i < nameList.size(); i++)
        {
            counter.add(0);
        }
    }

//    public ExpandableRecyclerViewAdapter(Context context, ArrayList<String> nameList, ArrayList<ArrayList> itemNameList)
//    {
//        this.nameList = nameList;
//        this.itemNameList = itemNameList;
//        this.context = context;
//
//        for (int i = 0; i < nameList.size(); i++)
//        {
//            counter.add(0);
//        }
//    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        ImageButton dropBtn;
        RecyclerView cardRecyclerView;
        ConstraintLayout select_layout;
        CardView cardView;

        public ViewHolder(View view)
        {
            super(view);
            name = view.findViewById(R.id.categoryTitle);
            dropBtn = view.findViewById(R.id.categoryExpandBtn);
            cardRecyclerView = view.findViewById(R.id.innerRecyclerView);
            select_layout = view.findViewById(R.id.select_layout);
            cardView = view.findViewById(R.id.cardView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_collapseview, parent, false);

        ExpandableRecyclerViewAdapter.ViewHolder vh = new ExpandableRecyclerViewAdapter.ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position)
    {
        holder.name.setText(nameList.get(position));
        InnerRecyclerViewAdapter itemInnerRecyclerView = new InnerRecyclerViewAdapter(itemNameList.get(position));
        Log.e(TAG, "itemNameList.get(position) : " + itemNameList.get(position));
        Log.e(TAG, "nameList.get(position) : " + nameList.get(position));
        Log.e(TAG, "provideTypeList.get(position) : " + provideTypeList.get(position));
        Log.e(TAG, "localList.get(position) : " + localList.get(position));
        Log.e(TAG, "ageList.get(position) : " + ageList.get(position));
//        holder.cardRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        holder.cardRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        // 확장 리사이클러뷰를 접고 펼치는 로직
        holder.dropBtn.setOnClickListener(v -> {
            if (counter.get(position) % 2 == 0)
            {
                holder.cardRecyclerView.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.cardRecyclerView.setVisibility(View.GONE);
            }
            counter.set(position, counter.get(position) + 1);
        });

        // 확장 리사이클러뷰를 접고 펼치는 로직
        holder.select_layout.setOnClickListener(view ->
        {
            if (counter.get(position) % 2 == 0)
            {
                holder.cardRecyclerView.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.cardRecyclerView.setVisibility(View.GONE);
            }
            counter.set(position, counter.get(position) + 1);
        });
        holder.cardRecyclerView.setAdapter(itemInnerRecyclerView);
    }

    @Override
    public int getItemCount()
    {
        return nameList.size();
    }


}
