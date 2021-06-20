package com.psj.welfare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.data.HorizontalYoutubeItem;

import java.util.ArrayList;

public class YoutubeMoreAdapter extends RecyclerView.Adapter<YoutubeMoreAdapter.youtubemoreholder> {

    private YoutubeMoreAdapter.YoutubeMoreClick moreClick = null;
    private ArrayList<HorizontalYoutubeItem> youtubemorelist;
    private Context context;

    //클릭을 액티비티에서 하기 위한 생성자
    public void setYoutubeClickListener(YoutubeMoreAdapter.YoutubeMoreClick moreClick)
    {
        this.moreClick = moreClick;
    }

    public YoutubeMoreAdapter(ArrayList<HorizontalYoutubeItem> youtubemorelist, Context context, YoutubeMoreClick moreClick) {
        this.moreClick = moreClick;
        this.youtubemorelist = youtubemorelist;
        this.context = context;
    }

    @NonNull
    @Override
    public youtubemoreholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtubemore_item, parent, false);
        return new youtubemoreholder(View);
    }

    @Override
    public void onBindViewHolder(@NonNull youtubemoreholder holder, int position) {

        String youtube_thumbnail = youtubemorelist.get(position).getYoutube_thumbnail(); //유튜브 썸네일
        String youtube_title = youtubemorelist.get(position).getYoutube_title(); //유튜브 타이틀
        String youtube_name = youtubemorelist.get(position).getYoutube_name() + " · " + youtubemorelist.get(position).getYoutube_upload_date(); //유튜버 + 업로드날짜

        Glide.with(holder.itemView)
                .load(youtube_thumbnail)
                .into(holder.youtube_more_thumbnail);
        holder.youtube_more_title.setText(youtube_title);
        holder.youtube_more_name.setText(youtube_name);

        //레이아웃의 사이즈를 동적으로 맞춤
        setsize(holder);
    }

    @Override
    public int getItemCount() {
        return youtubemorelist != null ? youtubemorelist.size() : 0;
    }

    public class youtubemoreholder extends RecyclerView.ViewHolder {

        private ConstraintLayout youtube_more_layout; //아이템 전체 레이아웃
        private LinearLayout youtube_more_text_layout; //텍스트 레이아웃
        private ImageView youtube_more_thumbnail; //썸네일 이미지
        private TextView youtube_more_title; //유튜브 제목
        private TextView youtube_more_name; //유튜버 + 업로드 날짜짜

       public youtubemoreholder(@NonNull View itemView) {
            super(itemView);
           youtube_more_layout = itemView.findViewById(R.id.youtube_more_layout);
           youtube_more_text_layout = itemView.findViewById(R.id.youtube_more_text_layout);
           youtube_more_thumbnail = itemView.findViewById(R.id.youtube_more_thumbnail);
           youtube_more_title = itemView.findViewById(R.id.youtube_more_title);
           youtube_more_name = itemView.findViewById(R.id.youtube_more_name);

           youtube_more_layout.setOnClickListener(v -> {
               moreClick.youtubemoreClick(v,getAdapterPosition());
           });

        }
    }


    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(youtubemoreholder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);

//        holder.youtube_list_thumbnail.getLayoutParams().width = (int) (size.x * 0.38);
//        holder.youtube_list_thumbnail.getLayoutParams().height = (int) (size.y * 0.108);
//
//        holder.youtube_more_layout.getLayoutParams().width = (int) (size.x * 0.92);


//        holder.youtube_more_layout.getLayoutParams().width = (int) (size.x * 0.92); //아이템 전체 레이아웃
//        holder.youtube_more_layout.getLayoutParams().height = (int) (size.y * 0.43); //아이템 전체 레이아웃

        holder.youtube_more_text_layout.setPadding((int) (size.x * 0.02),(int) (size.y * 0.015),(int) (size.x * 0.02),(int) (size.y * 0.015));

//        holder.youtube_more_thumbnail.getLayoutParams().width = (int) (size.x * 0.92); //썸네일 이미지
        holder.youtube_more_thumbnail.getLayoutParams().height = (int) (size.y * 0.27); //썸네일 이미지

        holder.youtube_more_title.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.045));
        holder.youtube_more_name.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.x * 0.04));
        holder.youtube_more_name.setPadding(0,(int) (size.y * 0.005),0,0);
    }

    public interface YoutubeMoreClick{
        void youtubemoreClick(View v, int position);
    }
}
