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

import com.psj.welfare.Data.MapSearchItem;
import com.psj.welfare.R;

import java.util.List;

public class MapSearchAdapter extends RecyclerView.Adapter<MapSearchAdapter.MapSearchViewHolder>
{
    private Context context;
    private List<MapSearchItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MapSearchAdapter(Context context, List<MapSearchItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MapSearchAdapter.MapSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.map_search_item, parent, false);
        return new MapSearchViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MapSearchAdapter.MapSearchViewHolder holder, int position)
    {
        MapSearchItem item = list.get(position);
        holder.search_item_textview.setText(item.getMap_search_result_name());
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class MapSearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        LinearLayout map_search_layout;
        TextView search_item_textview;
        ImageView search_close_imageview;
        ItemClickListener itemClickListener;

        public MapSearchViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            map_search_layout = view.findViewById(R.id.map_search_layout);
            search_item_textview = view.findViewById(R.id.search_item_textview);
            search_close_imageview = view.findViewById(R.id.search_close_imageview);

            this.itemClickListener = itemClickListener;
            map_search_layout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                    {
                        itemClickListener.onItemClick(v, pos);
                    }
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
