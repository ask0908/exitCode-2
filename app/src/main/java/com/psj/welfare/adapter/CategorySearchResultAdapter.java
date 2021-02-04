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

import com.psj.welfare.Data.CategorySearchResultItem;
import com.psj.welfare.R;

import java.util.Collections;
import java.util.List;

/* ResultBenefitActivity의 세로 리사이클러뷰(관심사 선택)에 쓰는 어댑터 */
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
                holder.result_imageview.setImageResource(R.drawable.job);
                break;

            case "카드 지원" :
                holder.result_imageview.setImageResource(R.drawable.card_support);
                break;

            case "인력 지원" :
                holder.result_imageview.setImageResource(R.drawable.person_support);
                break;

            case "현금 지원" :
                holder.result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "현물 지원" :
                holder.result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "대출 지원" :
                holder.result_imageview.setImageResource(R.drawable.loan_support);
                break;

            case "대출 지원 " :
                holder.result_imageview.setImageResource(R.drawable.loan_support);
                break;

            case "임대 지원" :
                holder.result_imageview.setImageResource(R.drawable.rent_support);
                break;

            case "보험 지원" :
                holder.result_imageview.setImageResource(R.drawable.insurance_support);
                break;

            case "법률 지원" :
                holder.result_imageview.setImageResource(R.drawable.law_support);
                break;

            case "상담 지원" :
                holder.result_imageview.setImageResource(R.drawable.counseling_support);
                break;

            case "물품 지원" :
                holder.result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "재활 지원" :
                holder.result_imageview.setImageResource(R.drawable.recover_support);
                break;

            case "창업 지원" :
                holder.result_imageview.setImageResource(R.drawable.foundation_support);
                break;

            case "진료 지원" :
                holder.result_imageview.setImageResource(R.drawable.medical_support);
                break;

            case "활동 지원" :
                holder.result_imageview.setImageResource(R.drawable.activity_support);
                break;

            case "서비스 지원" :
                holder.result_imageview.setImageResource(R.drawable.service_support);
                break;

            case "치료 지원" :
                holder.result_imageview.setImageResource(R.drawable.care_support);
                break;

            case "감면 지원" :
                holder.result_imageview.setImageResource(R.drawable.tax_support);
                break;

            case "멘토링 지원" :
                holder.result_imageview.setImageResource(R.drawable.mentor_support);
                break;

            case "정보 지원" :
                holder.result_imageview.setImageResource(R.drawable.information_support);
                break;

            case "숙식 지원" :
                holder.result_imageview.setImageResource(R.drawable.room_board_support);
                break;

            case "문화체험 지원" :
                holder.result_imageview.setImageResource(R.drawable.culture_support);
                break;

            case "취업 지원" :
                holder.result_imageview.setImageResource(R.drawable.job_hunt_support);
                break;

            case "교육 지원" :
                holder.result_imageview.setImageResource(R.drawable.learning_support);
                break;

            case "교육지원" :
                holder.result_imageview.setImageResource(R.drawable.learning_support);
                break;

            case "공간 지원" :
                holder.result_imageview.setImageResource(R.drawable.counseling_support);
                break;

            case "사업화 지원" :
                holder.result_imageview.setImageResource(R.drawable.office_support);
                break;

            case "컨설팅 지원" :
                holder.result_imageview.setImageResource(R.drawable.consulting);
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
        CardView category_search_item;
        ImageView result_imageview;
        TextView result_textview, welf_local_textview;
        ItemClickListener itemClickListener;

        public CategorySearchResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            category_search_item = view.findViewById(R.id.category_search_item);
            welf_local_textview = view.findViewById(R.id.welf_local_textview);
            result_imageview = view.findViewById(R.id.result_imageview);
            result_textview = view.findViewById(R.id.category_search_result_title);
            this.itemClickListener = itemClickListener;
            category_search_item.setOnClickListener(v -> {
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
