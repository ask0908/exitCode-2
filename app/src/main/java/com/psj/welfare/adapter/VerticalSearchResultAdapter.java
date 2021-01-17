package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;

import java.util.List;

/* SearchResultActivity의 세로 리사이클러뷰에 쓰이는 어댑터 */
public class VerticalSearchResultAdapter extends RecyclerView.Adapter<VerticalSearchResultAdapter.VerticalSearchResultViewHolder>
{
    private Context context;
    private List<SearchItem> list;
    private VerticalItemClickListener itemClickListener;

    public void setOnItemClickListener(VerticalItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public VerticalSearchResultAdapter(Context context, List<SearchItem> list, VerticalItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public VerticalSearchResultAdapter.VerticalSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_item, parent, false);
        return new VerticalSearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VerticalSearchResultAdapter.VerticalSearchResultViewHolder holder, int position)
    {
        SearchItem item = list.get(position);
        holder.search_result_textview.setText(item.getWelf_name());
        holder.search_welf_local_textview.setText("#" + item.getWelf_local());

        Log.e("키워드 검색 후", "카테고리 = " + list.get(position).getWelf_category());

        switch (list.get(position).getWelf_category())
        {
            case "일자리 지원" :
                Glide.with(context)
                        .load(R.drawable.job)
                        .into(holder.search_result_imageview);
                break;

            case "카드 지원" :
                Glide.with(context)
                        .load(R.drawable.card_support)
                        .into(holder.search_result_imageview);
                break;

            case "인력 지원" :
                Glide.with(context)
                        .load(R.drawable.person_support)
                        .into(holder.search_result_imageview);
                break;

            case "현금 지원" :
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.search_result_imageview);
                break;

            case "현물 지원" :
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.search_result_imageview);
                break;

            case "대출 지원" :
                Glide.with(context)
                        .load(R.drawable.loan)
                        .into(holder.search_result_imageview);
                break;

            case "임대 지원" :
                Glide.with(context)
                        .load(R.drawable.rent_support)
                        .into(holder.search_result_imageview);
                break;

            case "보험 지원" :
                Glide.with(context)
                        .load(R.drawable.insurance_support)
                        .into(holder.search_result_imageview);
                break;

            default:
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class VerticalSearchResultViewHolder extends RecyclerView.ViewHolder
    {
        ImageView search_result_imageview;
        TextView search_result_textview, search_welf_local_textview;
        VerticalItemClickListener itemClickListener;

        public VerticalSearchResultViewHolder(@NonNull View view, VerticalItemClickListener itemClickListener)
        {
            super(view);

            search_result_imageview = view.findViewById(R.id.search_result_imageview);
            search_welf_local_textview = view.findViewById(R.id.search_welf_local_textview);
            search_result_textview = view.findViewById(R.id.search_result_title);
            this.itemClickListener = itemClickListener;
            search_result_textview.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });
        }
    }

    public interface VerticalItemClickListener
    {
        void onItemClick(View view, int pos);
    }

}
