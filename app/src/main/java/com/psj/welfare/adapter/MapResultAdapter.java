package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.R;

import java.util.ArrayList;
import java.util.Collections;
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
        // getWelf_category()로 받아온 결과에 특수문자가 섞여있으면 이 특문 이후의 문자열을 지우고 그 값을 set하고 싶다
//        holder.map_result_local.setText(item.getWelf_category());
        List<String> item_list = Collections.singletonList(lists.get(position).getWelf_category());
        for (int i = 0; i < item_list.size(); i++)
        {
            Log.e("ddd", "item_list = " + item_list);
        }
        // 리스트의 요소 뒤에 붙어있는 ;; 같은 구분자들을 전부 공백으로 바꾼다
        StringBuilder listToString = new StringBuilder();
        for (String str : item_list)
        {
            listToString.append(str);
        }
        String after_str = listToString.toString().split(";;")[0];
        holder.map_result_local.setText(after_str);
        Log.e("ddd", ";;와 그 뒤의 문자열 지운 결과 : " + after_str);
        switch (after_str)
        {
            case "일자리 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.job);
                break;

            case "카드 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.card_support);
                break;

            case "인력 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.person_support);
                break;

            case "현금 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.goods_support);
                break;

            case "현물 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.goods_support);
                break;

            case "대출 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.loan_support);
                break;

            case "대출 지원 " :
                holder.map_result_benefit_image.setImageResource(R.drawable.loan_support);
                break;

            case "임대 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.rent_support);
                break;

            case "보험 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.insurance_support);
                break;

            case "법률 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.law_support);
                break;

            case "상담 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.counseling_support);
                break;

            case "물품 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.goods_support);
                break;

            case "재활 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.recover_support);
                break;

            case "창업 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.foundation_support);
                break;

            case "진료 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.medical_support);
                break;

            case "활동 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.activity_support);
                break;

            case "서비스 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.service_support);
                break;

            case "치료 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.care_support);
                break;

            case "감면 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.tax_support);
                break;

            case "멘토링 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.mentor_support);
                break;

            case "정보 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.information_support);
                break;

            case "숙식 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.room_board_support);
                break;

            case "문화체험 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.culture_support);
                break;

            case "취업 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.job_hunt_support);
                break;

            case "교육 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.learning_support);
                break;

            case "교육지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.learning_support);
                break;

            case "공간 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.counseling_support);
                break;

            case "사업화 지원" :
                holder.map_result_benefit_image.setImageResource(R.drawable.office_support);
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
