package com.psj.welfare.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.R;
import com.psj.welfare.data.HorizontalYoutubeItem;

import java.util.List;

public class TestYoutubeAdapter extends RecyclerView.Adapter<TestYoutubeAdapter.TestYoutubeViewHolder>
{
    private Context context;
    private List<HorizontalYoutubeItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public TestYoutubeAdapter(Context context, List<HorizontalYoutubeItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TestYoutubeAdapter.TestYoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_youtube_item, parent, false);
        return new TestYoutubeViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TestYoutubeAdapter.TestYoutubeViewHolder holder, int position)
    {
        HorizontalYoutubeItem item = list.get(position);
        Glide.with(context)
                .load(item.getYoutube_thumbnail())
                .into(holder.test_youtube_image);
        holder.test_youtube_image.setClipToOutline(true);
        holder.test_youtube_title.setText(item.getYoutube_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class TestYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        CardView horizontal_youtube_layout;
        ImageView test_youtube_image;
        TextView test_youtube_title;
        ItemClickListener itemClickListener;

        public TestYoutubeViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            horizontal_youtube_layout = view.findViewById(R.id.test_youtube_layout);
            test_youtube_image = view.findViewById(R.id.test_youtube_image);
            test_youtube_title = view.findViewById(R.id.test_youtube_title);

            this.itemClickListener = itemClickListener;
            horizontal_youtube_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View v, int pos);
    }

}
