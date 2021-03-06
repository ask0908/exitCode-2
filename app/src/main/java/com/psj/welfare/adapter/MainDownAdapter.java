package com.psj.welfare.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.data.MainThreeDataItem;

import java.util.List;

public class MainDownAdapter extends RecyclerView.Adapter<MainDownAdapter.MainDownViewHolder>
{
    private static final String TAG = MainDownAdapter.class.getSimpleName();

    private Context context;
    private List<MainThreeDataItem> list;
    private ItemClickListener itemClickListener = null;

    public MainDownAdapter(Context context, List<MainThreeDataItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MainDownAdapter.MainDownViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.see_more_down_item, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (parent.getWidth() * 0.85);
        params.height = (int) (parent.getHeight() * 0.25);
        view.setLayoutParams(params);

        return new MainDownViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainDownAdapter.MainDownViewHolder holder, int position)
    {
        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize((Activity) context);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.bottom_result_layout.getLayoutParams();
        params.setMargins((int) (size.x * 0.03), (int) (size.y * 0.015), (int) (size.x * 0.03), (int) (size.y * 0.015));
        holder.bottom_result_layout.setLayoutParams(params);

        if (!list.isEmpty())
        {
            MainThreeDataItem item = list.get(position);
            holder.bottom_result_name.setText(item.getWelf_name());
            holder.bottom_result_views.setText("View" + item.getWelf_count());
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
        }
        else
        {
            Log.e(TAG, "메인에서 쓰는 리스트에 값이 없습니다");
        }
    }

    @Override
    public int getItemCount()
    {
        return 3;
    }

    public class MainDownViewHolder extends RecyclerView.ViewHolder
    {
        CardView bottom_result_layout;
        TextView bottom_result_name, bottom_result_tag, bottom_result_views;
        ItemClickListener itemClickListener;

        public MainDownViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            bottom_result_layout = view.findViewById(R.id.bottom_result_layout);
            bottom_result_name = view.findViewById(R.id.bottom_result_name);
            bottom_result_tag = view.findViewById(R.id.bottom_result_tag);
            bottom_result_views = view.findViewById(R.id.bottom_result_views);

            this.itemClickListener = itemClickListener;
            bottom_result_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition();

                //itemClickListener 이 계속 null 나옴 -> 왜 null아니여야 하는가??
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onMainThreeClick(v,pos);
                    notifyDataSetChanged();
                }

            });
        }
    }

    public interface ItemClickListener
    {
        void onMainThreeClick(View v, int pos);
    }

}
