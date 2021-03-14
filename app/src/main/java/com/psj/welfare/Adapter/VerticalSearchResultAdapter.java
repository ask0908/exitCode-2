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

import com.psj.welfare.data.SearchItem;
import com.psj.welfare.R;

import java.util.Collections;
import java.util.List;

/* SearchResultActivity의 세로 리사이클러뷰에 쓰이는 어댑터 */
public class VerticalSearchResultAdapter extends RecyclerView.Adapter<VerticalSearchResultAdapter.VerticalSearchResultViewHolder>
{
    private final String TAG = "VerticalSearchResultAdapter";

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
        String welf_name = item.getWelf_name();
        if (welf_name.contains(";; "))
        {
            welf_name = welf_name.replace(";; ", ", ");
        }
        holder.search_result_textview.setText(welf_name);
        holder.search_welf_local_textview.setText("#" + item.getWelf_local());

//        Log.e("키워드 검색 후", "카테고리 = " + list.get(position).getWelf_category());
        /* 첫 번째 요소만 빼고 뒤의 요소를 전부 없애는 로직 */
        List<String> item_list = Collections.singletonList(list.get(position).getWelf_category());
        for (int i = 0; i < item_list.size(); i++)
        {
            Log.e(TAG, "item_list = " + item_list);
        }
        // 리스트의 요소 뒤에 붙어있는 ;; 같은 구분자들을 전부 공백으로 바꾼다
        StringBuilder listToString = new StringBuilder();
        for (String str : item_list)
        {
            listToString.append(str);
        }
        String after_str = listToString.toString().split(";;")[0];
        Log.e(TAG, "첫 번째 OO 지원 : " + after_str);

        // split한 결과의 첫 번째 요소를 변수에 담아 이 변수값을 통해 어떤 이미지를 띄울지 정한다
        switch (after_str)
        {
            case "일자리 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.job);
                break;

            case "카드 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.card_support);
                break;

            case "인력 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.person_support);
                break;

            case "현금 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "현물 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "대출 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.loan_support);
                break;

            case "임대 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.rent_support);
                break;

            case "보험 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.insurance_support);
                break;

            case "법률 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.law_support);
                break;

            case "상담 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.counseling_support);
                break;

            case "물품 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.goods_support);
                break;

            case "재활 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.recover_support);
                break;

            case "창업 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.foundation_support);
                break;

            case "진료 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.medical_support);
                break;

            case "활동 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.activity_support);
                break;

            case "서비스 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.service_support);
                break;

            case "치료 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.care_support);
                break;

            case "감면 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.tax_support);
                break;

            case "멘토링 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.mentor_support);
                break;

            case "정보 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.information_support);
                break;

            case "숙식 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.room_board_support);
                break;

            case "문화체험 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.culture_support);
                break;

            case "취업 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.job_hunt_support);
                break;

            case "교육 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.learning_support);
                break;

            case "교육지원" :
                holder.search_result_imageview.setImageResource(R.drawable.learning_support);
                break;

            case "공간 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.counseling_support);
                break;

            case "사업화 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.office_support);
                break;

            case "컨설팅 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.consulting);
                break;

            case "홍보 지원" :
                holder.search_result_imageview.setImageResource(R.drawable.campaign);
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
        CardView search_result_item;
        ImageView search_result_imageview;
        TextView search_result_textview, search_welf_local_textview;
        VerticalItemClickListener itemClickListener;

        public VerticalSearchResultViewHolder(@NonNull View view, VerticalItemClickListener itemClickListener)
        {
            super(view);

            search_result_item = view.findViewById(R.id.search_result_item);
            search_result_imageview = view.findViewById(R.id.search_result_imageview);
            search_welf_local_textview = view.findViewById(R.id.search_welf_local_textview);
            search_result_textview = view.findViewById(R.id.search_result_title);
            this.itemClickListener = itemClickListener;
            search_result_item.setOnClickListener(v -> {
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
