package com.psj.welfare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.Data.HorizontalYoutubeItem;
import com.psj.welfare.R;

import java.util.List;

/* MainFragment의 가로 리사이클러뷰에 사용하는 어댑터 */
public class HorizontalYoutubeAdapter extends RecyclerView.Adapter<HorizontalYoutubeAdapter.HorizontalYoutubeViewHolder>
{
    private Context context;
    private List<HorizontalYoutubeItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public HorizontalYoutubeAdapter(Context context, List<HorizontalYoutubeItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HorizontalYoutubeAdapter.HorizontalYoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_youtube_item, parent,false);
        return new HorizontalYoutubeViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalYoutubeAdapter.HorizontalYoutubeViewHolder holder, int position)
    {
        HorizontalYoutubeItem item = list.get(position);
        Glide.with(context)
                .load(item.getYoutube_thumbnail())
                .into(holder.youtube_image);
        holder.youtube_title.setText(item.getYoutube_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class HorizontalYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout horizontal_youtube_layout;
        ImageView youtube_image;
        TextView youtube_title;
        ItemClickListener itemClickListener;

        public HorizontalYoutubeViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            horizontal_youtube_layout = view.findViewById(R.id.horizontal_youtube_layout);
            youtube_image = view.findViewById(R.id.youtube_image);
            youtube_title = view.findViewById(R.id.youtube_title);

            this.itemClickListener = itemClickListener;
            horizontal_youtube_layout.setOnClickListener(v -> {
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

}
