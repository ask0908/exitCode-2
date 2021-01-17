package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.Data.RecommendItem;
import com.psj.welfare.R;

import java.util.List;

/* MainFragment에서 추천 혜택들을 가로 리사이클러뷰로 보여줄 때 사용하는 어댑터 */
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
        String final_tag = "";
        holder.recommend_welf_name.setText(item.getWelf_name());
        // 서버에서 받은 데이터에 들어있는 구분자를 해시태그로 바꾼다
        holder.recommend_local.setText("#" + list.get(position).getWelf_local());
        switch (list.get(position).getWelf_category())
        {
            case "일자리 지원" :
                Glide.with(context)
                        .load(R.drawable.counseling)
                        .into(holder.recommend_welf_image);
                break;

            case "카드 지원" :
                Glide.with(context)
                        .load(R.drawable.loan)
                        .into(holder.recommend_welf_image);
                break;

            case "현금 지원" :
                Glide.with(context)
                        .load(R.drawable.cash)
                        .into(holder.recommend_welf_image);
                break;

            case "현물 지원" :
                Glide.with(context)
                        .load(R.drawable.loan)
                        .into(holder.recommend_welf_image);
                break;

            case "대출 지원" :
                Glide.with(context)
                        .load(R.drawable.loan)
                        .into(holder.recommend_welf_image);
                break;

            case "임대 지원" :
                Glide.with(context)
                        .load(R.drawable.house)
                        .into(holder.recommend_welf_image);
                break;

            case "보험 지원" :
                Glide.with(context)
                        .load(R.drawable.cash)
                        .into(holder.recommend_welf_image);
                break;

            default:
                break;
        }
//        String tag = item.getTag().replace(";; ", ";;");
//        String final_tag = tag.replace(";;", " #");
//        holder.recommend_tag.setText("#" + final_tag);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class RecommendViewHolder extends RecyclerView.ViewHolder
    {
        CardView recommend_layout;
        ImageView recommend_welf_image;
        TextView recommend_welf_name, recommend_local;
        ItemClickListener itemClickListener;

        public RecommendViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            recommend_layout = view.findViewById(R.id.recommend_layout);
            recommend_welf_name = view.findViewById(R.id.recommend_welf_name);
            recommend_welf_image = view.findViewById(R.id.recommend_welf_image);
            recommend_local = view.findViewById(R.id.recommend_local);

            this.itemClickListener = itemClickListener;
            recommend_layout.setOnClickListener(v ->
            {
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
