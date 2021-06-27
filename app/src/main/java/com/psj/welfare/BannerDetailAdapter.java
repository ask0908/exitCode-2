package com.psj.welfare;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BannerDetailAdapter extends RecyclerView.Adapter<BannerDetailAdapter.BannerDetailholder> {

    private BannerDetailAdapter.BannerDetailClick bannerClick = null; //혜택 클릭 리스너
    private ArrayList<BannerDetailData> bannerlist; //혜택 데이터 리스트
    private Context context;


    //클릭을 액티비티에서 하기 위한 생성자
    public void setBannerClickListener(BannerDetailAdapter.BannerDetailClick bannerClick)
    {
        this.bannerClick = bannerClick;
    }

    public BannerDetailAdapter(ArrayList<BannerDetailData> bannerlist, Context context) {
        this.bannerlist = bannerlist;
        this.context = context;
    }

    @NonNull
    @Override
    public BannerDetailholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.bannerdetail_item, parent, false);
        return new BannerDetailAdapter.BannerDetailholder(View);
    }

    @Override
    public int getItemCount() {
        return bannerlist != null ? bannerlist.size() : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull BannerDetailholder holder, int position) {

        //레이아웃의 사이즈를 동적으로 맞춤
        setsize(holder);

        String BannerId = bannerlist.get(position).getBannerId(); //혜택 id
        String BannerTitle = bannerlist.get(position).getBannerTitle(); //혜택 타이틀
        String BannerTag = bannerlist.get(position).getBannerTag(); //혜택 태그
        String BannerContents = bannerlist.get(position).getBannerContents(); //혜택 콘텐츠

//        Log.e("TAG","BannerTag" + BannerTag);
        String BannerTag_removeblank = BannerTag.replaceAll("\\p{Z}",""); //공백 제거
//        Log.e("TAG","BannerTag_removeblank" + BannerTag_removeblank);
        String[] BannerTaglist = BannerTag_removeblank.split("-");

        String bannertag_final = ""; //배너 태그

        if(!BannerTag.equals("")){
            for (int i = 0; i < BannerTaglist.length; i++){
                bannertag_final += "#" + BannerTaglist[i] + " ";
            }
        }

        if(bannertag_final.equals("")){ //배너가 "" 이면 안보여주기
            holder.banner_tag.setVisibility(View.GONE);
        } else {
            holder.banner_tag.setVisibility(View.VISIBLE);
        }

        holder.banner_title.setText(BannerTitle);
        holder.banner_tag.setText(bannertag_final);
        holder.banner_contents.setText(BannerContents);

    }


    public class BannerDetailholder extends RecyclerView.ViewHolder {

        private LinearLayout bannerdetail_layout; //배너 아이템 레이아웃
        private LinearLayout banner_btn_layout; //혜택 버튼 레이아웃
        private TextView banner_btn_text; //혜택 버튼 텍스트
        private ImageView banner_btn_image; //혜택 버튼 이미지지

        private TextView banner_title; //혜택 타이틀
        private TextView banner_tag; //혜택 태그
        private TextView banner_contents; //혜택 내용

        public BannerDetailholder(@NonNull View itemView) {
            super(itemView);

            bannerdetail_layout = itemView.findViewById(R.id.bannerdetail_layout); //혜택 레이아웃
            banner_btn_layout = itemView.findViewById(R.id.banner_btn_layout); //혜택 버튼 레이아웃
            banner_btn_text = itemView.findViewById(R.id.banner_btn_text); //혜택 버튼 텍스트
            banner_btn_image = itemView.findViewById(R.id.banner_btn_image); //혜택 버튼 이미지
            banner_title = itemView.findViewById(R.id.banner_title); //혜택 타이틀
            banner_tag = itemView.findViewById(R.id.banner_tag); //혜택 태그
            banner_contents = itemView.findViewById(R.id.banner_contents); //혜택 내용


            banner_btn_layout.setOnClickListener(v -> { //혜택 버튼
                bannerClick.bannerdetailClick(v, getAdapterPosition());
            });
        }
    }

    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(BannerDetailholder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);

        //혜택 타이틀
        holder.banner_title.setPadding(0,(int) (size.y * 0.018),0,0);
        holder.banner_title.setTextSize((int) (size.x * 0.017));

        //혜택 태그
        holder.banner_tag.setPadding(0,(int) (size.y * 0.006),0,0);
        holder.banner_tag.setTextSize((int) (size.x * 0.0139));

        //혜택 내용
        holder.banner_contents.setPadding(0,(int) (size.y * 0.027),0,(int) (size.y * 0.045));
        holder.banner_contents.setTextSize((int) (size.x * 0.0135));

        //혜택 버튼 레이아웃
        holder.banner_btn_layout.getLayoutParams().height = (int) (size.y * 0.06);
        holder.banner_btn_layout.getLayoutParams().width = (int) (size.x * 0.88);
        //혜택 버튼 텍스트
        holder.banner_btn_text.setTextSize((int) (size.x * 0.017));
        //혜택 버튼 이미지
        holder.banner_btn_image.getLayoutParams().height = (int) (size.x * 0.08);
        holder.banner_btn_image.getLayoutParams().width = (int) (size.x * 0.075);

        //혜택 레이아웃
        holder.bannerdetail_layout.setPadding((int) (size.x * 0.06),(int) (size.x * 0.05),(int) (size.x * 0.05),(int) (size.x * 0.08));
    }

    public interface BannerDetailClick{
        void bannerdetailClick(View v, int position);
    }
}
