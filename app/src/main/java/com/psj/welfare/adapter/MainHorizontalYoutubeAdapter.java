package com.psj.welfare.adapter;

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

/* 새 메서드로 유튜브 데이터 가져와서 가로 리사이클러뷰에 보여주기 위한 어댑터 */
public class MainHorizontalYoutubeAdapter extends RecyclerView.Adapter<MainHorizontalYoutubeAdapter.MainHorizontalYoutubeViewHolder>
{
    private Context context;
    private List<HorizontalYoutubeItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MainHorizontalYoutubeAdapter(Context context, List<HorizontalYoutubeItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MainHorizontalYoutubeAdapter.MainHorizontalYoutubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_youtube_item, parent, false);
        return new MainHorizontalYoutubeViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainHorizontalYoutubeAdapter.MainHorizontalYoutubeViewHolder holder, int position)
    {
        HorizontalYoutubeItem item = list.get(position);
        Glide.with(context)
                .load(item.getYoutube_thumbnail())
                .into(holder.test_youtube_image);
        holder.test_youtube_image.setClipToOutline(true);
        holder.test_youtube_title.setText(item.getYoutube_name());

        if((list.size()-1) == position){
            holder.test_allview_youtube_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class MainHorizontalYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        CardView horizontal_youtube_layout; //유튜브 레이아웃
        ImageView test_youtube_image; //유튜브 이미지
        TextView test_youtube_title; //유튜브 제목
        ItemClickListener itemClickListener; //유튜브 클릭 리스너

        CardView test_allview_youtube_layout; //유튜브 더보기 버튼

        public MainHorizontalYoutubeViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            horizontal_youtube_layout = view.findViewById(R.id.test_youtube_layout);
            test_youtube_image = view.findViewById(R.id.test_youtube_image);
            test_youtube_title = view.findViewById(R.id.test_youtube_title);

            test_allview_youtube_layout = view.findViewById(R.id.test_allview_youtube_layout);

            this.itemClickListener = itemClickListener; //클릭리스터 인터페이스(프래그먼트에서 사용하기 위함)
            int pos = getAdapterPosition(); //선택한 유튜브의 포지션 값 저장
            //유튜브 아이템 클릭
            horizontal_youtube_layout.setOnClickListener(v -> {
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });

            //유튜브 더보기 아이템 클릭
            test_allview_youtube_layout.setOnClickListener(v -> {
//                Intent intent = new Intent(context, YoutubeActivity.class);
//                context.startActivity(intent);
            });

        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View v, int pos);
    }

}
