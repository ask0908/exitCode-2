package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.SearchResultItem;

import java.util.List;

public class RenewalSearchResultAdapter extends RecyclerView.Adapter<RenewalSearchResultAdapter.RenewalSearchResultViewHolder>
{
    private static final String TAG = RenewalSearchResultAdapter.class.getSimpleName();

    private Context context;
    private List<SearchResultItem> list;
    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public RenewalSearchResultAdapter(Context context, List<SearchResultItem> list, onItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RenewalSearchResultAdapter.RenewalSearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_recycler_item, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (parent.getWidth());
        params.height = (int) (parent.getHeight() * 0.3);
        view.setLayoutParams(params);

        return new RenewalSearchResultViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RenewalSearchResultAdapter.RenewalSearchResultViewHolder holder, int position)
    {
        if (!list.isEmpty())
        {
            SearchResultItem item = list.get(position);
            holder.search_result_name.setText(item.getWelf_name());
            holder.search_result_views.setText("View " + item.getWelf_count());
            if (item.getWelf_tag().contains("-"))
            {
                String before = item.getWelf_tag().replace(" ", "");
                String str = "#" + before;
                String s = str.replace("-", " #");
                String s1 = s.replace(" -", " #");
                String s2 = s1.replace("- ", " #");
                String s3 = s2.replace(" - ", " #");
                holder.search_result_subject.setText(s3);
            }
        }
        else
        {
            Log.e(TAG, "검색 결과 리사이클러뷰의 리스트에 값이 없습니다");
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class RenewalSearchResultViewHolder extends RecyclerView.ViewHolder
    {
        CardView search_result_layout;
        TextView search_result_name, search_result_subject, search_result_views;
        onItemClickListener itemClickListener;

        public RenewalSearchResultViewHolder(@NonNull View view, onItemClickListener itemClickListener)
        {
            super(view);

            search_result_layout = view.findViewById(R.id.search_result_layout);
            search_result_name = view.findViewById(R.id.search_result_name);
            search_result_subject = view.findViewById(R.id.search_result_subject);
            search_result_views = view.findViewById(R.id.search_result_views);

            this.itemClickListener = itemClickListener;
            search_result_layout.setOnClickListener(v -> {
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
