package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.BookmarkItem;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>
{
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private List<BookmarkItem> list;
    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public BookmarkAdapter(Context context, List<BookmarkItem> list, OnItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public BookmarkAdapter.BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarkViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.BookmarkViewHolder holder, int position)
    {
        final BookmarkItem item = list.get(position);
        holder.bookmark_welf_name.setText(item.getWelf_name());
        if (item.getTag().contains("-"))
        {
            String before = item.getTag().replace(" ", "");
            String str = "#" + before;
            String s = str.replace("-", " #");
            String s1 = s.replace(" -", " #");
            String s2 = s1.replace("- ", " #");
            String s3 = s2.replace(" - ", " #");
            holder.bookmark_tag.setText(s3);
        }
        else if (!item.getTag().equals(""))
        {
            String before = item.getTag().replace(" ", "");
            String else_str = "#" + before;
            holder.bookmark_tag.setText(else_str);
        }
        else if (item.getTag().equals("None"))
        {
            holder.bookmark_tag.setText("");
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class BookmarkViewHolder extends RecyclerView.ViewHolder
    {
        TextView bookmark_welf_name, bookmark_tag, bookmark_detail;
        OnItemClickListener itemClickListener;

        public BookmarkViewHolder(@NonNull View view, OnItemClickListener itemClickListener)
        {
            super(view);
            bookmark_welf_name = view.findViewById(R.id.bookmark_welf_name);
            bookmark_tag = view.findViewById(R.id.bookmark_tag);
            bookmark_detail = view.findViewById(R.id.bookmark_detail);

            this.itemClickListener = itemClickListener;
            bookmark_detail.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    public interface OnItemClickListener
    {
        void onItemClick(int pos);
    }

}
