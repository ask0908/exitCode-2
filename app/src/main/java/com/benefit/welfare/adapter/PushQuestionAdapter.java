package com.benefit.welfare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.benefit.welfare.Data.PushQuestionItem;
import com.benefit.welfare.R;

import java.util.ArrayList;
import java.util.List;

/* PushQuestionActivity에서 사용, 관심사 관련 질문지 아이템들을 리사이클러뷰에 붙일 때 사용되는 어댑터 */
public class PushQuestionAdapter extends RecyclerView.Adapter<PushQuestionAdapter.PushQuestionViewHolder>
{
    private Context context;
    private List<PushQuestionItem> lists;
    private ItemClickListener itemClickListener;

    static List<String> keyword_lists;

    // 액티비티에서 리사이클러뷰 아이템에 클릭 리스너 붙이기 위한 setter 설정
    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public PushQuestionAdapter(List<PushQuestionItem> lists)
    {
        this.lists = lists;
    }

    public PushQuestionAdapter(Context context, List<PushQuestionItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PushQuestionAdapter.PushQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.push_question_item, parent, false);
        return new PushQuestionViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PushQuestionAdapter.PushQuestionViewHolder holder, int position)
    {
        PushQuestionItem item = lists.get(position);
        holder.question_text.setText(item.getQuestion());
        // 아이템 복수 선택 처리부
        keyword_lists = new ArrayList<>();
        holder.question_text.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            if (item.isSelected())
            {
                holder.question_text.setBackgroundResource(R.drawable.btn_done);
//                if (position == 0)
//                {
//                    Log.e("onBindViewHolder()", "키워드 아기, 육아 전송");
//                }
            }
            else
            {
                holder.question_text.setBackgroundResource(R.drawable.btn_round);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public static class PushQuestionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout question_layout;
        TextView question_text;
        ItemClickListener itemClickListener;

        public PushQuestionViewHolder(View view, ItemClickListener itemClickListener)
        {
            super(view);

            question_layout = view.findViewById(R.id.question_layout);
            question_text = view.findViewById(R.id.push_question_text);

            this.itemClickListener = itemClickListener;
            question_layout.setOnClickListener(this);
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
