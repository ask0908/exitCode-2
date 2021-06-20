package com.psj.welfare;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DetailReviewAllAdapter extends RecyclerView.Adapter<DetailReviewAllAdapter.AllReviewViewHolder> {

    private ArrayList<DetailReviewData> DetailReviewList; //데이터 받을 어레이리스트 변수
    private Context context; //리사이클러뷰를 실행시킨 뷰를 가져오기 위한 변수

    public DetailReviewAllAdapter(ArrayList<DetailReviewData> detailReviewList, Context context) {
        DetailReviewList = detailReviewList;
        this.context = context;
    }

    @NonNull
    @Override
    public AllReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflater는 xml로 정의된 view (또는 menu 등)를 실제 객체화 시키는 용도, 뷰를 연결 시킴
        View ReviewLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_two_review, parent, false);
        return new DetailReviewAllAdapter.AllReviewViewHolder(ReviewLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull AllReviewViewHolder holder, int position) {


        DetailReviewData reviewData = DetailReviewList.get(position);

        holder.two_review_nickname.setText(reviewData.getNickName()); //닉네임 데이터 넣기
        holder.two_review_content.setText(reviewData.getContent()); //내용 데이터 넣기
        holder.two_review_star.setStar(reviewData.getStar_count()); //별점

        String parsing_date = reviewData.getCreate_date();
        String date = parsing_date.substring(0, 10);
        String DataReplace = date.replace("-", "/"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
        holder.two_review_date.setText(DataReplace); //날짜 데이터 넣기

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        setsize(holder);

        if(position == DetailReviewList.size()-1){
            //마지막 아이템이면파트를 나누는 라인을 안보여줌
            holder.review_delimiter.setVisibility(View.GONE);
        } else {
            //마지막 아이템이 아니면 파트를 나누는 라인을 보여줌
            holder.review_delimiter.setVisibility(View.VISIBLE);
        }
    }

    public class AllReviewViewHolder extends RecyclerView.ViewHolder {

        com.hedgehog.ratingbar.RatingBar two_review_star; //별점
        LinearLayout review_item; //아이템 전체 레이아웃
        TextView two_review_nickname; //닉네임
        TextView two_review_date; //날짜
        TextView two_review_content; //내용
        View review_delimiter; //리뷰 파트 나누는 라인

        public AllReviewViewHolder(@NonNull View view) {
            super(view);
            review_item = view.findViewById(R.id.review_item); //아이템 전체 레이아웃
            two_review_nickname = view.findViewById(R.id.two_review_nickname); //닉네임
            two_review_star = view.findViewById(R.id.two_review_star); //별점
            two_review_date = view.findViewById(R.id.two_review_date); //날짜
            two_review_content = view.findViewById(R.id.two_review_content); //내용
            review_delimiter = view.findViewById(R.id.review_delimiter); //리뷰 파트 나누는 라인
        }
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void setsize(DetailReviewAllAdapter.AllReviewViewHolder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        holder.two_review_nickname.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/23); //닉네임
        holder.two_review_content.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/25); //내용
        holder.two_review_content.setPadding(0,0,0,(int) (size.y * 0.01));

        holder.review_item.setPadding((int) (size.x * 0.015), (int) (size.y * 0.03), (int) (size.x * 0.015), (int) (size.y * 0.03)); //레이아웃 패딩값 적용
        holder.two_review_date.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/30);

    }

    @Override
    public int getItemCount() {
        return DetailReviewList != null ? DetailReviewList.size() : 0;
    }
}
