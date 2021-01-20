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

import com.psj.welfare.Data.RecommendItem;
import com.psj.welfare.R;

import java.util.Collections;
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
        holder.recommend_welf_name.setText(item.getWelf_name());
        // 서버에서 받은 데이터에 들어있는 구분자를 해시태그로 바꾼다
        holder.recommend_local.setText("#" + list.get(position).getWelf_local());
        Log.e("ddd", "welf_category : " + list.get(position).getWelf_category());
        // 첫 번째 요소만 빼고 뒤의 요소를 전부 쳐낸다
        List<String> item_list = Collections.singletonList(list.get(position).getWelf_category());
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
        Log.e("ddd", ";;와 그 뒤의 문자열 지운 결과 : " + after_str);
        switch (after_str)
        {
            case "일자리 지원" :
//                Glide.with(context)
//                        .load(R.drawable.job)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.job);
                break;

            case "카드 지원" :
//                Glide.with(context)
//                        .load(R.drawable.card_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.card_support);
                break;

            case "인력 지원" :
//                Glide.with(context)
//                        .load(R.drawable.person_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.person_support);
                break;

            case "현금 지원" :
//                Glide.with(context)
//                        .load(R.drawable.goods_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.goods_support);
                break;

            case "현물 지원" :
//                Glide.with(context)
//                        .load(R.drawable.goods_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.goods_support);
                break;

            case "대출 지원" :
//                Glide.with(context)
//                        .load(R.drawable.loan_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.loan_support);
                break;

            case "임대 지원" :
//                Glide.with(context)
//                        .load(R.drawable.rent_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.rent_support);
                break;

            case "보험 지원" :
//                Glide.with(context)
//                        .load(R.drawable.insurance_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.insurance_support);
                break;

            case "법률 지원" :
//                Glide.with(context)
//                        .load(R.drawable.law_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.law_support);
                break;

            case "상담 지원" :
//                Glide.with(context)
//                        .load(R.drawable.counseling_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.counseling_support);
                break;

            case "물품 지원" :
//                Glide.with(context)
//                        .load(R.drawable.goods_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.goods_support);
                break;

            case "재활 지원" :
//                Glide.with(context)
//                        .load(R.drawable.recover_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.recover_support);
                break;

            case "창업 지원" :
//                Glide.with(context)
//                        .load(R.drawable.foundation_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.foundation_support);
                break;

            case "진료 지원" :
//                Glide.with(context)
//                        .load(R.drawable.medical_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.medical_support);
                break;

            case "활동 지원" :
//                Glide.with(context)
//                        .load(R.drawable.activity_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.activity_support);
                break;

            case "서비스 지원" :
//                Glide.with(context)
//                        .load(R.drawable.service_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.service_support);
                break;

            case "치료 지원" :
//                Glide.with(context)
//                        .load(R.drawable.care_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.care_support);
                break;

            case "감면 지원" :
//                Glide.with(context)
//                        .load(R.drawable.tax_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.tax_support);
                break;

            case "멘토링 지원" :
//                Glide.with(context)
//                        .load(R.drawable.mentor_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.mentor_support);
                break;

            case "정보 지원" :
//                Glide.with(context)
//                        .load(R.drawable.information_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.information_support);
                break;

            case "숙식 지원" :
//                Glide.with(context)
//                        .load(R.drawable.room_board_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.room_board_support);
                break;

            case "문화체험 지원" :
//                Glide.with(context)
//                        .load(R.drawable.culture_support)
//                        .into(holder.recommend_welf_image);
                holder.recommend_welf_image.setImageResource(R.drawable.culture_support);
                break;

            case "취업 지원" :
                holder.recommend_welf_image.setImageResource(R.drawable.job_hunt_support);
                break;

            case "교육 지원" :
                holder.recommend_welf_image.setImageResource(R.drawable.learning_support);
                break;

            case "교육지원" :
                holder.recommend_welf_image.setImageResource(R.drawable.learning_support);
                break;

            case "공간 지원" :
                holder.recommend_welf_image.setImageResource(R.drawable.counseling_support);
                break;

            case "사업화 지원" :
                holder.recommend_welf_image.setImageResource(R.drawable.office_support);
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
