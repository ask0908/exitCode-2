package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    public MapResultAdapter(Context context, List<MapResultItem> lists)
    {
        this.context = context;
        this.lists = lists;
    }

    @NonNull
    @Override
    public MapResultAdapter.MapResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.map_result_item, parent, false);
        return new MapResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapResultAdapter.MapResultViewHolder holder, int position)
    {
        MapResultItem item = lists.get(position);
        holder.map_result_benefit_name.setText(item.getBenefit_name());
        holder.map_result_benefit_btn.setText(item.getBenefit_btn_text());
        holder.map_result_benefit_btn.setOnClickListener(v -> {
            // 여기서도 버튼 클릭 리스너가 먹히나?
            int pos = position;
            Log.e(TAG, "pos = " + pos);
        });
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class MapResultViewHolder extends RecyclerView.ViewHolder
    {
        TextView map_result_benefit_name;
        Button map_result_benefit_btn;

        public MapResultViewHolder(@NonNull View view)
        {
            super(view);

            map_result_benefit_name = view.findViewById(R.id.map_result_benefit_name);
            map_result_benefit_btn = view.findViewById(R.id.map_result_benefit_btn);
        }
    }
}
