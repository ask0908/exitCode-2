package com.psj.welfare.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ResultKeywordItem;
import com.psj.welfare.R;

import java.util.Collections;
import java.util.List;

/* MapDetailActivity에서 상단 리사이클러뷰에 카테고리들을 세팅하는 리사이클러뷰에서 사용하는 어댑터 */
public class ResultKeywordAdapter extends RecyclerView.Adapter<ResultKeywordAdapter.ResultKeywordViewHolder>
{
    private final String TAG = "ResultKeywordAdapter";

    private Context context;
    private List<ResultKeywordItem> lists;
    private ItemClickListener itemClickListener;

    // 아이템 색 바꿀 때 쓰는 변수
    private int selected_position = -1;

    public void setOnResultKeywordClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public ResultKeywordAdapter(Context context, List<ResultKeywordItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ResultKeywordAdapter.ResultKeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.result_keyword_item, parent, false);
        return new ResultKeywordViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultKeywordAdapter.ResultKeywordViewHolder holder, int position)
    {
        ResultKeywordItem item = lists.get(position);
        holder.keyword_category.setText(item.getWelf_category());
        holder.keyword_layout.setTag(position);

        /* 구분자를 기준으로 앞의 요소를 제외한 뒤의 요소를 전부 없애는 로직 */
        // 서버에서 받아온 모든 데이터가 아래 리스트에 담겨진다. 아래에서 뷰에 변화를 줄 때 위치를 참고하기 위해 이 리스트에 모든 데이터를 담아두고
        // position 값을 통해 수정한다
        List<String> item_list = Collections.singletonList(lists.get(position).getWelf_category());

        // 서버에서 받아온 요소들을 하나씩 아이템에 출력시킬 때 사용할 StringBuilder
        // StringBuilder로 문자열들을 '문자열+구분자+문자열' 형태로 묶은 다음, 구분자를 기준으로 파싱해서 아이템에 하나씩 나오도록 한다
        StringBuilder listToString = new StringBuilder();
        for (String str : item_list)
        {
            listToString.append(str);
        }
        String after_str = listToString.toString().split(";; ")[0];

        for (int i = 0; i < lists.size(); i++)
        {
            if (!lists.get(i).getWelf_category().contains(after_str))
            {
                holder.keyword_category.setText(after_str);
            }
        }

        /* 필터 색 바꾸기 */
        if (selected_position == position)
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.textlines_after);
            holder.keyword_category.setTextColor(Color.parseColor("#EE2F43"));
        }
        else
        {
            holder.keyword_layout.setBackgroundResource(R.drawable.textlines);
            holder.keyword_category.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class ResultKeywordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout keyword_layout;
        TextView keyword_category;
        ItemClickListener itemClickListener;

        public ResultKeywordViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            keyword_layout = view.findViewById(R.id.keyword_layout);
            keyword_category = view.findViewById(R.id.keyword_category);

            this.itemClickListener = itemClickListener;
            keyword_category.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);

                    /* 필터 색 바꾸기 */
                    selected_position = pos;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onClick(View v)
        {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

}
