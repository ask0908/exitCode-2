package com.psj.welfare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.psj.welfare.data.HorizontalYoutubeItem;

import java.util.ArrayList;

public class YoutubeListAdapter extends RecyclerView.Adapter<YoutubeListAdapter.youtubeholder> {

    private ClickListener clickListener = null;
    private ArrayList<HorizontalYoutubeItem> youtubelist;
    private Context context;

    public void setYoutubeClickListener(ClickListener clickListener)
    {
        this.clickListener = clickListener;
    }


    public YoutubeListAdapter(ArrayList<HorizontalYoutubeItem> youtubelist, Context context, ClickListener clickListener) {
        this.clickListener = clickListener;
        this.youtubelist = youtubelist;
        this.context = context;
    }

    @NonNull
    @Override
    public youtubeholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflater는 xml로 정의된 view (또는 menu 등)를 실제 객체화 시키는 용도, 뷰를 연결 시킴
        View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtubelist_item, parent, false);
        return new YoutubeListAdapter.youtubeholder(View);
    }

    @Override
    public void onBindViewHolder(@NonNull youtubeholder holder, int position) {

        String youtube_thumbnail = youtubelist.get(position).getYoutube_thumbnail(); //유튜브 썸네일
        String youtube_title = youtubelist.get(position).getYoutube_title(); //유튜브 타이틀
        String youtube_name = youtubelist.get(position).getYoutube_name() + " · " + youtubelist.get(position).getYoutube_upload_date(); //유튜버 + 업로드날짜

        Glide.with(holder.itemView)
                .load(youtube_thumbnail)
                .into(holder.youtube_list_thumbnail);
        holder.youtube_list_title.setText(youtube_title);
        holder.youtube_list_name.setText(youtube_name);

        //레이아웃의 사이즈를 동적으로 맞춤
        setsize(holder);
    }

    @Override
    public int getItemCount() {
        return youtubelist != null ? youtubelist.size() : 0;
    }

    public class youtubeholder extends RecyclerView.ViewHolder{

        private ConstraintLayout youtube_layout; //유튜브 리스트 레이아웃
        private ImageView youtube_list_thumbnail; //썸네일
        private TextView youtube_list_title; //유튜브 타이틀
        private TextView youtube_list_name; //유튜버 + 업로드 날짜

        public youtubeholder(@NonNull View itemView) {
            super(itemView);

            youtube_layout = itemView.findViewById(R.id.youtube_layout);
            youtube_list_thumbnail = itemView.findViewById(R.id.youtube_list_thumbnail);
            youtube_list_title = itemView.findViewById(R.id.youtube_list_title);
            youtube_list_name = itemView.findViewById(R.id.youtube_list_name);

            youtube_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.youtubeClick(v, getAdapterPosition());
                }
            });
        }

    }

    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(youtubeholder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);

        //썸네일
        holder.youtube_list_thumbnail.getLayoutParams().width = (int) (size.x * 0.38);
        holder.youtube_list_thumbnail.getLayoutParams().height = (int) (size.y * 0.112);

        //유튜브 리스트 레이아웃
        holder.youtube_layout.getLayoutParams().width = (int) (size.x * 0.92);
        holder.youtube_layout.getLayoutParams().height = (int) (size.y * 0.115);

        //유튜브 제목 텍스트
        holder.youtube_list_title.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/26);
        //유튜버 + 업로드 날짜
        holder.youtube_list_name.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/28);
    }

    //액티비티에서 클릭 리스너 하기 위한 인터페이스(임의로 만듦)
    public interface ClickListener {
        void youtubeClick(View v, int position);
    }
}
