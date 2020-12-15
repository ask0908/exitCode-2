package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.SearchItem;
import com.psj.welfare.R;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>
{
    private Context context;
    private List<SearchItem> lists;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SearchAdapter(Context context, List<SearchItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new SearchViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchViewHolder holder, int position)
    {
        SearchItem item = lists.get(position);
        holder.search_title.setText(item.getWelf_name());
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder
    {
        TextView search_title;
        ItemClickListener itemClickListener;

        public SearchViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            search_title = view.findViewById(R.id.search_title);
            this.itemClickListener = itemClickListener;
            search_title.setOnClickListener(v -> {
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


//    public Context searchContext;
//    private ArrayList<SearchItem> searchData;
//    private View.OnClickListener onClickListener;
//
//    public SearchAdapter(Context SearchContext, ArrayList<SearchItem> SearchDataSet, View.OnClickListener OnClickListener)
//    {
//        this.searchContext = SearchContext;
//        this.searchData = SearchDataSet;
//        this.onClickListener = OnClickListener;
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder
//    {
//
//        public TextView search_title;
//        public View rootView;
//
//        public ViewHolder(@NonNull View view)
//        {
//            super(view);
//
//            search_title = view.findViewById(R.id.search_title);
//            rootView = view;
//
//            view.setClickable(true);
//            view.setEnabled(true);
//            view.setOnClickListener(onClickListener);
//        }
//    }
//
//    @NonNull
//    @Override
//    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
//    {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
//        ViewHolder viewholder = new ViewHolder(view);
//        return viewholder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder viewHolder, int position)
//    {
//        viewHolder.search_title.setText(searchData.get(position).getSearchTitle());
//        // Tag - Label 을 달아준다
//        viewHolder.rootView.setTag(position);
//    }
//
//    @Override
//    public int getItemCount()
//    {
//        return searchData.size();
//    }

}
