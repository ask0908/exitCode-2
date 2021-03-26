package com.psj.welfare.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.RecommendItem;

import java.util.List;

/* MainFragment에서 맞춤 혜택들을 가로 리사이클러뷰로 보여줄 때 사용하는 어댑터
* "현재 아이템의 인덱스 / 총 맞춤 혜택 개수" 형태로 몇 번째 인덱스의 맞춤 혜택을 보고 있는지 유저에게 보여준다 */
public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.RecommendViewHolder>
{
    private Context context;
    private List<RecommendItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public RecommendAdapter(Context context, List<RecommendItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecommendAdapter.RecommendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.recommend_item, parent, false);
        return new RecommendViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendAdapter.RecommendViewHolder holder, int position)
    {
        RecommendItem item = list.get(position);
        // 혜택명
        if (holder.recommend_welf_name.getText().toString().length() < 15)
        {
            holder.recommend_welf_name.setGravity(Gravity.CENTER);
            holder.recommend_welf_name.setText(item.getWelf_name());
        }
        else
        {
            holder.recommend_welf_name.setText(item.getWelf_name());
        }
        // 혜택 실시지역
        holder.recommend_local.setText("#" + list.get(position).getWelf_local());
        // 현재 아이템의 위치 / 총 아이템 개수
        holder.recommend_page_text.setText((position + 1) + "/" + list.size());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder
    {
        CardView recommend_layout;
        Button recommend_detail_btn;
        TextView recommend_welf_name, recommend_local, recommend_page_text;
        ItemClickListener itemClickListener;

        public RecommendViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            recommend_layout = view.findViewById(R.id.recommend_layout);
            recommend_welf_name = view.findViewById(R.id.recommend_welf_name);
            recommend_detail_btn = view.findViewById(R.id.recommend_detail_btn);
            recommend_local = view.findViewById(R.id.recommend_local);
            recommend_page_text = view.findViewById(R.id.recommend_page_text);

            this.itemClickListener = itemClickListener;
            recommend_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });

            /* 0322) 메인 화면에서 버튼을 눌러도 상세보기 화면으로 이동하지 않아서 버튼에도 클릭 리스너 추가함 */
            recommend_detail_btn.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
