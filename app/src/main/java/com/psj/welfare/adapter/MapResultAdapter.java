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
import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.R;

import java.util.ArrayList;
import java.util.List;

public class MapResultAdapter extends RecyclerView.Adapter<MapResultAdapter.MapResultViewHolder>
{
    private final String TAG = "MapResultAdapter";
    private Context context;
    private List<MapResultItem> lists;
    private ItemClickListener itemClickListener;

    // 중복 처리에 썼던 리스트
    List<String> str_list = new ArrayList<>();

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MapResultAdapter(Context context, List<MapResultItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MapResultAdapter.MapResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.map_result_item, parent, false);
        return new MapResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MapResultAdapter.MapResultViewHolder holder, int position)
    {
        MapResultItem item = lists.get(position);
        holder.map_result_benefit_name.setText(item.getWelf_name());
        holder.map_result_local.setText(item.getParent_category());
        switch (lists.get(position).getWelf_category())
        {
            case "일자리 지원" :
                Glide.with(context)
                        .load(R.drawable.job)
                        .into(holder.map_result_benefit_image);
                break;

            case "카드 지원" :
                Glide.with(context)
                        .load(R.drawable.card_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "인력 지원" :
                Glide.with(context)
                        .load(R.drawable.person_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "현금 지원" :
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "현물 지원" :
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "대출 지원" :
                Glide.with(context)
                        .load(R.drawable.loan_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "임대 지원" :
                Glide.with(context)
                        .load(R.drawable.rent_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "보험 지원" :
                Glide.with(context)
                        .load(R.drawable.insurance_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "법률 지원" :
                Glide.with(context)
                        .load(R.drawable.law_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "상담 지원" :
                Glide.with(context)
                        .load(R.drawable.counseling_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "물품 지원" :
                Glide.with(context)
                        .load(R.drawable.goods_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "재활 지원" :
                Glide.with(context)
                        .load(R.drawable.recover_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "창업 지원" :
                Glide.with(context)
                        .load(R.drawable.foundation_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "진료 지원" :
                Glide.with(context)
                        .load(R.drawable.medical_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "활동 지원" :
                Glide.with(context)
                        .load(R.drawable.activity_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "서비스 지원" :
                Glide.with(context)
                        .load(R.drawable.service_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "치료 지원" :
                Glide.with(context)
                        .load(R.drawable.care_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "감면 지원" :
                Glide.with(context)
                        .load(R.drawable.tax_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "멘토링 지원" :
                Glide.with(context)
                        .load(R.drawable.mentor_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "정보 지원" :
                Glide.with(context)
                        .load(R.drawable.information_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "숙식 지원" :
                Glide.with(context)
                        .load(R.drawable.room_board_support)
                        .into(holder.map_result_benefit_image);
                break;

            case "문화체험 지원" :
                Glide.with(context)
                        .load(R.drawable.culture_support)
                        .into(holder.map_result_benefit_image);
                break;

            default:
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public static class MapResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        CardView map_result_layout;
        TextView map_result_benefit_name, map_result_local;
        ImageView map_result_benefit_image;
        ItemClickListener itemClickListener;

        public MapResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            map_result_layout = view.findViewById(R.id.map_result_layout);
            map_result_benefit_name = view.findViewById(R.id.map_result_benefit_name);
            map_result_local = view.findViewById(R.id.map_result_local);
            map_result_benefit_image = view.findViewById(R.id.map_result_benefit_image);

            this.itemClickListener = itemClickListener;
            map_result_layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
            {
                itemClickListener.onItemClick(v, pos);
            }
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
