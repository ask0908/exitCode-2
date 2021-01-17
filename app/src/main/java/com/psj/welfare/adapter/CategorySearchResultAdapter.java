package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.R;

import java.util.List;

/* ResultBenefitActivity의 세로 리사이클러뷰에 쓰는 어댑터 */
public class CategorySearchResultAdapter extends RecyclerView.Adapter<CategorySearchResultAdapter.CategorySearchResultViewHolder>
{
    private Context context;
    private List<CategorySearchResultItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public CategorySearchResultAdapter(Context context, List<CategorySearchResultItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public CategorySearchResultAdapter.CategorySearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.category_search_result_item, parent, false);
        return new CategorySearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorySearchResultAdapter.CategorySearchResultViewHolder holder, int position)
    {
        CategorySearchResultItem item = list.get(position);
        holder.welf_local_textview.setText("#" + item.getWelf_local());
        holder.result_textview.setText(item.getWelf_name());

        switch (list.get(position).getWelf_category())
        {
            case "일자리 지원" :
                Glide.with(context)
                        .load(R.drawable.job)
                        .into(holder.result_imageview);
                break;

            case "카드 지원" :
                Glide.with(context)
                        .load(R.drawable.card_support)
                        .into(holder.result_imageview);
                break;

            case "인력 지원" :
                Glide.with(context)
                        .load(R.drawable.person_support)
                        .into(holder.result_imageview);
                break;

            case "현금 지원" :
                holder.result_imageview.setImageResource(R.drawable.goods_support);
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.result_imageview);
                break;

            case "현물 지원" :
                holder.result_imageview.setImageResource(R.drawable.goods_support);
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.result_imageview);
                break;

            case "대출 지원" :
                Glide.with(context)
                        .load(R.drawable.loan)
                        .into(holder.result_imageview);
                break;

            case "임대 지원" :
                Glide.with(context)
                        .load(R.drawable.rent_support)
                        .into(holder.result_imageview);
                break;

            case "보험 지원" :
                Glide.with(context)
                        .load(R.drawable.insurance_support)
                        .into(holder.result_imageview);
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

    public static class CategorySearchResultViewHolder extends RecyclerView.ViewHolder
    {
        ImageView result_imageview;
        TextView result_textview, welf_local_textview;
        ItemClickListener itemClickListener;

        public CategorySearchResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            welf_local_textview = view.findViewById(R.id.welf_local_textview);
            result_imageview = view.findViewById(R.id.result_imageview);
            result_textview = view.findViewById(R.id.category_search_result_title);
            this.itemClickListener = itemClickListener;
            result_textview.setOnClickListener(v -> {
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
