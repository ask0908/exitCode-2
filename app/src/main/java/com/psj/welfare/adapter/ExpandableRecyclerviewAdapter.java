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
        /* 여기서 그리드 레이아웃 매니저를 적용하고 4를 넣어야 가로 한 줄에 아이템이 4개씩 들어간다 */
        holder.cardRecyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        // 아이템을 클릭하면 해당 아이템 밑으로 그리드 레이아웃 리사이클러뷰가 보이도록 클릭 리스너를 추가한다
        holder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // position을 4로 나눴을 때 나머지가 0인 경우에만 아이템이 보여지게 한다
                if (counter.get(position) % 4 == 0)
                {
                    holder.cardRecyclerView.setVisibility(View.VISIBLE);
                    holder.dropBtn.setImageResource(R.drawable.circle_minus);
                }
                else
                {
                    // 아이템이 보여지지 않는 경우, -로 그림을 바꾸고 아래에 나와있는 뷰들을 감춘다
                    holder.cardRecyclerView.setVisibility(View.GONE);
                    holder.dropBtn.setImageResource(R.drawable.circle_plus);
                }
                // +1을 하는 이유 : 헤더를 제외하고 축소된 상태의 뷰들을 가져오기 위함
                // +1을 하지 않아도 별 차이는 없지만 일단 넣음
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
