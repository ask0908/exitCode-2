package com.psj.welfare.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

/* 주변센터를 리사이클러뷰로 보여줄 때 쓰는 어댑터. 아직 필요없어서 안 씀 */
public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.FacilitiesViewHolder>
{
    private Context context;

    public FacilitiesAdapter(Context context)
    {
        this.context = context;
    }

    @NonNull
    @Override
    public FacilitiesAdapter.FacilitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.facilities_item, parent, false);
        return new FacilitiesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilitiesAdapter.FacilitiesViewHolder holder, int position)
    {
        //
    }

    @Override
    public int getItemCount()
    {
        return 5;
    }

    public static class FacilitiesViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout facilities_parent_layout;
        TextView facilities_type, facilities_distance, facilities_name, facilities_time;

        public FacilitiesViewHolder(@NonNull View view)
        {
            super(view);

            facilities_parent_layout = view.findViewById(R.id.facilities_parent_layout);
            facilities_type = view.findViewById(R.id.facilities_type);
            facilities_distance = view.findViewById(R.id.facilities_distance);
            facilities_name = view.findViewById(R.id.facilities_name);
            facilities_time = view.findViewById(R.id.facilities_time);
        }
    }
}
