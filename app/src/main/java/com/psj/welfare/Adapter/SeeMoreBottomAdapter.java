package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.SeeMoreItem;

import java.util.List;

public class SeeMoreBottomAdapter extends RecyclerView.Adapter<SeeMoreBottomAdapter.SeeMoreViewHolder>
{
    private Context context;
    private List<SeeMoreItem> list;
    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SeeMoreBottomAdapter(Context context, List<SeeMoreItem> list, OnItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SeeMoreBottomAdapter.SeeMoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.see_more_down_item, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (parent.getWidth() * 0.85);
        params.height = (int) (parent.getHeight() * 0.15);
        view.setLayoutParams(params);

        return new SeeMoreViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SeeMoreBottomAdapter.SeeMoreViewHolder holder, int position)
    {
        /* 어댑터에서 뷰 크기, 글자 크기 조절 */
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 어댑터에서 사용하려면 아래처럼 써야 한다
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size); // or getSize(size)
        holder.bottom_result_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        holder.bottom_result_tag.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 28);
        holder.bottom_result_views.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 28);

        SeeMoreItem item = list.get(position);
        // 혜택명
        holder.bottom_result_name.setText(item.getWelf_name());
        // 임신/출산, 다문화 등 태그
        // 구분자를 다른 특수문자로 바꿔서 아이템에 set
        if (item.getWelf_tag().contains("-"))
        {
            String before = item.getWelf_tag().replace(" ", "");
            String str = "#" + before;
            String s = str.replace("-", " #");

            holder.bottom_result_tag.setText(s);
        }
        else if (!item.getWelf_tag().equals(""))
        {
            String before = item.getWelf_tag().replace(" ","");
            String else_str = "#" + before;
            holder.bottom_result_tag.setText(else_str);
        }
        // 조회수
        holder.bottom_result_views.setText("View " + item.getWelf_count());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class SeeMoreViewHolder extends RecyclerView.ViewHolder
    {
        CardView bottom_result_layout;
        TextView bottom_result_name, bottom_result_tag, bottom_result_views;
        OnItemClickListener itemClickListener;

        public SeeMoreViewHolder(@NonNull View view, OnItemClickListener itemClickListener)
        {
            super(view);

            bottom_result_layout = view.findViewById(R.id.bottom_result_layout);
            bottom_result_name = view.findViewById(R.id.bottom_result_name);
            bottom_result_tag = view.findViewById(R.id.bottom_result_tag);
            bottom_result_views = view.findViewById(R.id.bottom_result_views);

            this.itemClickListener = itemClickListener;
            bottom_result_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(int pos);
    }

}
