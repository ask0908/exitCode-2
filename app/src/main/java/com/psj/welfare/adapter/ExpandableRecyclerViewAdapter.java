package com.psj.welfare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
    public static ArrayList<String> categoryList;
    public static ArrayList<String> provideTypeList;
    ArrayList<Integer> counter = new ArrayList<>();
    ArrayList<ArrayList> itemNameList;  // 필터 항목들이 들어있는 리스트
    Context context;

    public ArrayList<Boolean> filter_local; //체크박스에 체크한 지역
    public ArrayList<Boolean> filter_age; //체크박스에 체크한 나이
    public ArrayList<Boolean> filter_provideType; //체크박스에 체크한 지역

    public ExpandableRecyclerViewAdapter(Context context,
                                         ArrayList<String> nameList,    // 필터들이 들어있는 리스트
                                         ArrayList<String> categoryList,
                                         ArrayList<String> ageList,
                                         ArrayList<String> localList,
                                         ArrayList<String> provideTypeList,
                                         ArrayList<ArrayList> itemNameList,
                                         ArrayList<Boolean> filter_local,
                                         ArrayList<Boolean> filter_age,
                                         ArrayList<Boolean> filter_provideType)
    {
        this.context = context;
        ExpandableRecyclerViewAdapter.nameList = nameList;
        ExpandableRecyclerViewAdapter.categoryList = categoryList;
        ExpandableRecyclerViewAdapter.ageList = ageList;
        ExpandableRecyclerViewAdapter.localList = localList;
        ExpandableRecyclerViewAdapter.provideTypeList = provideTypeList;
        this.itemNameList = itemNameList;   // 선택여부에 상관없이 모든 카테고리 필터들의 값이 들어있는 리스트

        this.filter_local = filter_local;
        this.filter_age = filter_age;
        this.filter_provideType = filter_provideType;

        for (int i = 0; i < nameList.size(); i++)
        {
            counter.add(0);
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
        // 필터들이 들어있는 리스트
        InnerRecyclerViewAdapter itemInnerRecyclerView = new InnerRecyclerViewAdapter(itemNameList.get(position), nameList.get(position), filter_local, filter_age, filter_provideType);
        holder.cardRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        // 확장 리사이클러뷰를 접고 펼치는 로직
        holder.dropBtn.setOnClickListener(v -> {
            if (counter.get(position) % 2 == 0)
            {
                holder.cardRecyclerView.setVisibility(View.VISIBLE);
                holder.dropBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_up));
            }
            else
            {
                holder.cardRecyclerView.setVisibility(View.GONE);
                holder.dropBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down));
            }
            counter.set(position, counter.get(position) + 1);
        });

        holder.select_layout.setOnClickListener(view ->
        {
            if (counter.get(position) % 2 == 0)
            {
                holder.cardRecyclerView.setVisibility(View.VISIBLE);
                holder.dropBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_up));
            }
            else
            {
                holder.cardRecyclerView.setVisibility(View.GONE);
                holder.dropBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down));
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

}
