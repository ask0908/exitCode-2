package com.psj.welfare.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.data.HorizontalYoutubeItem;

import java.util.List;

/* 새 메서드로 유튜브 데이터 가져와서 가로 리사이클러뷰에 보여주기 위한 어댑터 */
public class MainHorizontalYoutubeAdapter extends RecyclerView.Adapter<MainHorizontalYoutubeAdapter.MainHorizontalYoutubeViewHolder>
{
    private final String TAG;

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
        TAG = context.getClass().getSimpleName();
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
//            Log.e(TAG,"test : test");
        }
//        Log.e(TAG,"list : " + String.valueOf(list.size()));
//        Log.e(TAG,"position : " + String.valueOf(position));

        //레이아웃의 사이즈를 동적으로 맞춤
        setsize(holder,position);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class MainHorizontalYoutubeViewHolder extends RecyclerView.ViewHolder
    {
        CardView horizontal_youtube_layout;
        ImageView test_youtube_image;
        TextView test_youtube_title;
        ItemClickListener itemClickListener;

        CardView test_allview_youtube_layout; //유튜브 더보기 버튼
        CardView test_youtube_layout; //유튜브 썸네일 레이아웃
        LinearLayout youtube_layout; //유튜브 썸네일 아이템 전체 레이아웃

        public MainHorizontalYoutubeViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            horizontal_youtube_layout = view.findViewById(R.id.test_youtube_layout);
            test_youtube_image = view.findViewById(R.id.test_youtube_image);
            test_youtube_title = view.findViewById(R.id.test_youtube_title);

            test_allview_youtube_layout = view.findViewById(R.id.test_allview_youtube_layout);
            test_youtube_layout = view.findViewById(R.id.test_youtube_layout);
            youtube_layout = view.findViewById(R.id.youtube_layout);

            this.itemClickListener = itemClickListener;
            //유튜브 아이템 클릭
            horizontal_youtube_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition(); //선택한 유튜브의 포지션 값 저장
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });

            //유튜브 더보기 아이템 클릭
            test_allview_youtube_layout.setOnClickListener(v -> {
                int pos = getAdapterPosition(); //선택한 유튜브의 포지션 값 저장
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.moreviewItemClick(v, pos);
                }
            });

        }
    }

    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(MainHorizontalYoutubeViewHolder holder, int pos) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);
        //디스플레이 값을 기준으로 레이아웃등 크기를 동적으로 정함
        holder.test_youtube_layout.getLayoutParams().width = (int) (size.x * 0.39); //유튜브 썸네일 레이아웃
        holder.test_youtube_layout.getLayoutParams().height = (int) (size.y * 0.16); //유튜브 썸네일 레이아웃
        holder.youtube_layout.setPadding(size.x / 60, size.x / 120, size.x / 85,0);

        holder.test_allview_youtube_layout.getLayoutParams().width = (int) (size.x * 0.18); //유튜브 더보기
        holder.test_allview_youtube_layout.getLayoutParams().height = (int) (size.y * 0.16); //유튜브 더보기

        //유튜브 썸네일 이미지 UI최적화 수정중
//        holder.test_youtube_image.getLayoutParams().width = (int  ) (size.x * 0.38); //유튜브 썸네일 이미지
//        holder.test_youtube_image.getLayoutParams().height = (int) (size.y * 0.13); //유튜브 썸네일 이미지
    }

    public interface ItemClickListener
    {
        void onItemClick(View v, int pos);
        void moreviewItemClick(View v, int pos);
    }

}
