package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.MapResultItem;
import com.psj.welfare.R;

import java.util.List;

public class MapResultAdapter extends RecyclerView.Adapter<MapResultAdapter.MapResultViewHolder>
{
    private final String TAG = "MapResultAdapter";
    private Context context;
    private List<MapResultItem> lists;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MapResultAdapter(Context context, List<MapResultItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MapResultAdapter.MapResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.map_result_item, parent, false);
        return new MapResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MapResultAdapter.MapResultViewHolder holder, int position)
    {
        MapResultItem item = lists.get(position);
        holder.map_result_benefit_name.setText(item.getBenefit_name());
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public static class MapResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView map_result_benefit_name;
        ImageView map_result_benefit_btn;
        ItemClickListener itemClickListener;

        public MapResultViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            map_result_benefit_name = view.findViewById(R.id.map_result_benefit_name);
            map_result_benefit_btn = view.findViewById(R.id.map_result_benefit_btn);

            this.itemClickListener = itemClickListener;
            map_result_benefit_btn.setOnClickListener(this);
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
