package com.psj.welfare.adapter;

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
import com.psj.welfare.data.OtherYoutubeItem;
import com.psj.welfare.R;

import java.util.List;

public class OtherYoutubeAdapter extends RecyclerView.Adapter<OtherYoutubeAdapter.OtherYoutubeViewHolder>
{
    private Context context;
    private List<OtherYoutubeItem> list;
    private YoutubeItemClickListener itemClickListener;

    public void setOnItemClickListener(YoutubeItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public OtherYoutubeAdapter(Context context, List<OtherYoutubeItem> list, YoutubeItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public OtherYoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.other_youtube_item, parent, false);
        return new OtherYoutubeViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherYoutubeViewHolder holder, int position)
    {
        OtherYoutubeItem item = list.get(position);
        holder.other_youtube_title.setText(item.getTitle());
        Glide.with(context)
                .load(item.getThumbnail())
                .into(holder.other_youtube_image);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class OtherYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout other_youtube_layout;
        ImageView other_youtube_image;
        TextView other_youtube_title;
        YoutubeItemClickListener itemClickListener;

        public OtherYoutubeViewHolder(@NonNull View view, YoutubeItemClickListener itemClickListener)
        {
            super(view);

            other_youtube_layout = view.findViewById(R.id.other_youtube_layout);
            other_youtube_image = view.findViewById(R.id.other_youtube_thumbnail);
            other_youtube_title = view.findViewById(R.id.other_youtube_title);

            this.itemClickListener = itemClickListener;
            other_youtube_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onYoutubeClick(v, pos);
                }
            });
        }
    }

    public interface YoutubeItemClickListener
    {
        void onYoutubeClick(View view, int pos);
    }

}
