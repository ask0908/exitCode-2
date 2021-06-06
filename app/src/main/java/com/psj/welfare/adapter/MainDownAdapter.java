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
import com.psj.welfare.data.MainThreeDataItem;

import java.util.List;

public class MainDownAdapter extends RecyclerView.Adapter<MainDownAdapter.MainDownViewHolder>
{
    private static final String TAG = MainDownAdapter.class.getSimpleName();

    private Context context;
    private List<MainThreeDataItem> list;
    private ItemClickListener itemClickListener;

    public MainDownAdapter(Context context, List<MainThreeDataItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MainDownAdapter.MainDownViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_down_item, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (parent.getWidth() * 0.85);
        params.height = (int) (parent.getHeight() * 0.25);
        view.setLayoutParams(params);

        return new MainDownViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainDownAdapter.MainDownViewHolder holder, int position)
    {
        if (!list.isEmpty())
        {
            MainThreeDataItem item = list.get(position);
            holder.bottom_result_name.setText(item.getWelf_name());
            if (item.getWelf_tag().contains("-"))
            {
                String before = item.getWelf_tag().replace(" ", "");
                String str = "#" + before;
                String s = str.replace("-", " #");
                String s1 = s.replace(" -", " #");
                String s2 = s1.replace("- ", " #");
                String s3 = s2.replace(" - ", " #");
                holder.bottom_result_subject.setText(s3);
//                Log.e("before1 : ",before);
//                Log.e("tag1 : ",item.getWelf_tag());
            }
            else if (!item.getWelf_tag().equals(""))
            {
                String before = item.getWelf_tag().replace(" ","");
                String else_str = "#" + before;
                holder.bottom_result_subject.setText(else_str);
//                Log.e("before2 : ",before);
//                Log.e("tag2 : ",item.getWelf_tag());
            }
        }
        else
        {
            Log.e(TAG, "메인에서 쓰는 리스트에 값이 없습니다");
        }
    }

    @Override
    public int getItemCount()
    {
        return 3;
    }

    public class MainDownViewHolder extends RecyclerView.ViewHolder
    {
        CardView bottom_result_layout;
        TextView bottom_result_name, bottom_result_subject, bottom_result_views;
        ItemClickListener itemClickListener;

        public MainDownViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            bottom_result_layout = view.findViewById(R.id.bottom_result_layout);
            bottom_result_name = view.findViewById(R.id.bottom_result_name);
            bottom_result_subject = view.findViewById(R.id.bottom_result_subject);
            bottom_result_views = view.findViewById(R.id.bottom_result_views);

            this.itemClickListener = itemClickListener;
            bottom_result_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onMainThreeClick(v, pos);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onMainThreeClick(View v, int pos);
    }

}
