package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.PushGatherItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PushGatherAdapter extends RecyclerView.Adapter<PushGatherAdapter.PushGatherViewHolder>
{
    private Context context;
    private List<PushGatherItem> lists;
    private ItemClickListener itemClickListener;

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public PushGatherAdapter(Context context, List<PushGatherItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PushGatherAdapter.PushGatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.push_gather_item, parent, false);
        return new PushGatherViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PushGatherAdapter.PushGatherViewHolder holder, int position)
    {
        PushGatherItem item = lists.get(position);
        holder.push_gather_title.setText(item.getPush_gather_title());
        holder.push_gather_desc.setText(item.getWelf_name());
        holder.push_gather_date.setText(item.getPush_gather_date());

        ZonedDateTime seoulDateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentDateTime = LocalDateTime.parse(seoulDateTime.format(formatter), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime localDateTime = LocalDateTime.parse(item.getPush_gather_date(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Duration duration = null;
            duration = Duration.between(localDateTime, currentDateTime);
            long diffTime = 0;
            diffTime = duration.toMillis() / 1000;
            if (diffTime < SEC)
            {
                holder.push_gather_date.setText(diffTime + "초 전");
            }
            else if ((diffTime /= SEC) < MIN)
            {
                holder.push_gather_date.setText(diffTime + "분 전");
            }
            else if ((diffTime /= MIN) < HOUR)
            {
                holder.push_gather_date.setText(diffTime + "시간 전");
            }
            else if ((diffTime /= HOUR) < DAY)
            {
                holder.push_gather_date.setText(diffTime + " 일 전");
            }
            else if ((diffTime /= DAY) < MONTH)
            {
                holder.push_gather_date.setText(diffTime + " 달 전");
            }
            else
            {
                holder.push_gather_date.setText(diffTime + " 년 전");
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class PushGatherViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout push_gather_item_layout;
        TextView push_gather_title, push_gather_desc, push_gather_date;
        ItemClickListener itemClickListener;

        public PushGatherViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            push_gather_item_layout = view.findViewById(R.id.push_gather_item_layout);
            push_gather_title = view.findViewById(R.id.push_gather_title);
            push_gather_desc = view.findViewById(R.id.push_gather_desc);
            push_gather_date = view.findViewById(R.id.push_gather_date);

            this.itemClickListener = itemClickListener;
            push_gather_item_layout.setOnClickListener(v ->
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
        void onItemClick(View view, int position);
    }

    public List<PushGatherItem> getList()
    {
        return lists;
    }

}
