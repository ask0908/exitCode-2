package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.AreaWelfare;

import java.util.List;

public class AreaWelfareAdapter extends RecyclerView.Adapter<AreaWelfareAdapter.AreaWelfareViewHolder>
{
    private Context context;
    private List<AreaWelfare> list;
    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public AreaWelfareAdapter(Context context, List<AreaWelfare> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AreaWelfareAdapter.AreaWelfareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.welfare_category_item, parent, false);
        return new AreaWelfareViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaWelfareAdapter.AreaWelfareViewHolder holder, int position)
    {
        final AreaWelfare item = list.get(position);
        holder.search_category_button.setText(item.getLocal_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class AreaWelfareViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout search_category_item_layout;
        TextView search_category_button;
        onItemClickListener itemClickListener;

        public AreaWelfareViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);
            search_category_item_layout = view.findViewById(R.id.search_category_item_layout);
            search_category_button = view.findViewById(R.id.search_category_button);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getRealSize(size);

            /* 아이템 레이아웃의 가로 세로 길이를 화면 길이에 맞게 동적으로 조절한다 */
            ViewGroup.LayoutParams params_star = search_category_item_layout.getLayoutParams();
//            params_star.width = (size.x / 7) * 6;
//            params_star.height = (size.y / 14);
//            search_category_item_layout.setLayoutParams(params_star);

            this.itemClickListener = itemClickListener;
            search_category_button.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    public interface onItemClickListener
    {
        void onItemClick(int pos);
    }

}
