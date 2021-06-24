package com.psj.welfare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainBannerAdapter extends RecyclerView.Adapter<MainBannerAdapter.BannerHolder> {

    private MainBannerAdapter.BannerListener bannerListener = null; //배너 클릭 리스너
    private ArrayList<MainBannerData> BannerArrayList; //배너데이터
    private ArrayList<MainBannerData> DefaultList; //최초로 서버에서 받은 데이터
    private Context context; //컨텍스트
    private ViewPager2 viewPager2;
    private int ListCount;

    //클릭을 액티비티에서 하기 위한 생성자
    public void setbannerClickListener(MainBannerAdapter.BannerListener bannerListener) {
        this.bannerListener = bannerListener;
    }


    //무한 스크롤을 하기 위해 viewPager2를 인자로 받음
    public MainBannerAdapter(ArrayList<MainBannerData> bannerArrayList, Context context, BannerListener bannerListener, ViewPager2 viewPager2, ArrayList<MainBannerData> DefaultList) {
        this.BannerArrayList = bannerArrayList;
        this.context = context;
        this.bannerListener = bannerListener;
        this.viewPager2 = viewPager2;
        this.DefaultList = DefaultList; //최초로 서버에서 받은 데이터 (이 데이터가 없으면 무한 스크롤 기능 때문에 BannerArrayList 데이터가 계속 늘어난다)
    }

    @NonNull
    @Override
    public BannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflater는 xml로 정의된 view (또는 menu 등)를 실제 객체화 시키는 용도, 뷰를 연결 시킴
        View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item, parent, false);
        return new BannerHolder(bannerView);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerHolder holder, int position) {
        int bannercount = DefaultList.size(); //서버로부터 받아온 베너 데이터 갯수
//        Log.e("TAG", "BannerArrayList : " + BannerArrayList.size());
//        Log.e("TAG","position : " + position);

        if (bannercount != 0) {

            MainBannerData bannerData = BannerArrayList.get(position % bannercount); //배너데이터
            String bannerImage = bannerData.getImageurl(); //배너이미지
            String bannertitle = bannerData.getTitle(); //배너타이틀

            Glide.with(holder.itemView)
                    .load(bannerImage)
                    .into(holder.banner_image);

            holder.banner_title.setText(bannertitle);
            holder.banner_total.setText(String.valueOf(bannercount));
            holder.banner_num.setText(String.valueOf(position % bannercount + 1));

            //레이아웃의 사이즈를 동적으로 맞춤
            setsize(holder);

////        이미지를 무한 반복(처음 이미지면 마지막 이미지가 왼쪽에 로드됨) 반대도 마찬가지, 여기서는 일부러 안함
//        if(position == BannerArrayList.size()-2){
//            viewPager2.post(runnable);
//        }

        }
    }

    @Override
    public int getItemCount() {
        //무한 스크롤 처럼 보이도록 트릭을 사용(데이터를 여러개 넣고 그중 가운데 값부터 시작)
        return (BannerArrayList != null ? BannerArrayList.size() + 6000 : 0);
    }

    public class BannerHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout banner_layout; //배너 레이아웃
        private ImageView banner_image; //배너 이미지
        private TextView banner_title; //배너 타이틀
        private TextView banner_num; //현재 배너 포지션
        private TextView banner_total; //전체 배너 갯수

        private CardView banner_cardview; //배너 카드뷰 크기

        public BannerHolder(@NonNull View itemView) {
            super(itemView);

            banner_layout = itemView.findViewById(R.id.banner_layout); //배너 레이아웃
            banner_image = itemView.findViewById(R.id.banner_image); //배너 이미지
            banner_title = itemView.findViewById(R.id.banner_title); //배너 타이틀
            banner_num = itemView.findViewById(R.id.banner_num); //현재 배너 포지션
            banner_total = itemView.findViewById(R.id.banner_total); //전체 배너 갯수
            banner_cardview = itemView.findViewById(R.id.banner_cardview); //배너 카드뷰 크기

            banner_layout.setOnClickListener(v -> {
                bannerListener.bannerClick(v, getAdapterPosition());
            });
        }

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            BannerArrayList.addAll(DefaultList);
            notifyDataSetChanged();
        }
    };


    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(MainBannerAdapter.BannerHolder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);

//        holder.youtube_list_thumbnail.getLayoutParams().width = (int) (size.x * 0.38);
//        holder.youtube_list_thumbnail.getLayoutParams().height = (int) (size.y * 0.108);
//
//        holder.youtube_more_layout.getLayoutParams().width = (int) (size.x * 0.92);

//        holder.banner_cardview.getLayoutParams().width = (int) (size.x * 0.7); //아이템 전체 레이아웃
//        holder.banner_cardview.getLayoutParams().height = (int) (size.y * 0.24); //아이템 전체 레이아웃

    }

    //액티비티에서 클릭 리스너 하기 위한 인터페이스(임의로 만듦)
    public interface BannerListener {
        void bannerClick(View v, int position);
    }
}
