package com.psj.welfare.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.hedgehog.ratingbar.RatingBar;
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.R;
import com.psj.welfare.activity.ReviewUpdateActivity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/* DetailBenefitActivity에서 리뷰 목록을 보여줄 때 사용하는 어댑터 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>
{
    private Context context;
    private List<ReviewItem> list;
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public ReviewAdapter(Context context, List<ReviewItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position)
    {
        ReviewItem item = list.get(position);
        if (!item.getImage_url().equals("없음"))
        {
            Glide.with(context)
                    .load(item.getImage_url())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(40)))
                    .into(holder.review_image);
        }
        else
        {
            holder.review_image.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
        }
        holder.review_writer.setText(item.getWriter());
        holder.review_content.setText(item.getContent());
        holder.review_date.setText(item.getCreate_date());
        float count = item.getStar_count();
        Log.e("ReviewAdapter", "getId() : " + item.getId());  // 닉네임이 출력된다
        Log.e("ReviewAdapter", "getWriter() : " + item.getWriter());
        Log.e("ReviewAdapter", "getImage_url() : " + item.getImage_url());
        Log.e("ReviewAdapter", "getContent() : " + item.getContent());
        Log.e("ReviewAdapter", "별점 : " + item.getStar_count());
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_pref", 0);
        // item.getWriter()랑 쉐어드에 저장된 닉네임이랑 비교해서 같으면 보여주고 다르면 안 보여주면 될 듯
        if (!item.getWriter().equals(sharedPreferences.getString("user_nickname", "")))
        {
            // 다르니까 보여주지 않는다
            holder.update_textview.setVisibility(View.GONE);
            holder.delete_textview.setVisibility(View.GONE);
        }
        else
        {
            // 같은 경우에만 보여줘서 클릭할 수 있게 한다
            holder.update_textview.setVisibility(View.VISIBLE);
            holder.delete_textview.setVisibility(View.VISIBLE);
        }
        holder.update_textview.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewUpdateActivity.class);
            context.startActivity(intent);
        });
        holder.delete_textview.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewUpdateActivity.class);
            context.startActivity(intent);
        });

        // 리뷰 작성자와 내 닉네임이 같을 때만 수정, 삭제 버튼을 보여줘야 한다
        String user_nickname = sharedPreferences.getString("user_nickname", "");
        if (list.get(position).getWriter().equals(user_nickname))
        {
            holder.update_textview.setVisibility(View.VISIBLE);
            holder.delete_textview.setVisibility(View.VISIBLE);
        }
        else
        {
            // 다른 경우엔 내가 작성한 리뷰가 아니니까 수정, 삭제 문구를 가린다
            holder.update_textview.setVisibility(View.GONE);
            holder.delete_textview.setVisibility(View.GONE);
        }

        // 별점 표시
        if (item.getStar_count() != 0.0)
        {
            Log.e("ReviewAdapter", "rate = " + item.getStar_count());
            holder.review_rate.setStar(count);
        }
        else
        {
            Log.e("ReviewAdapter", "rate = 0.0");
        }

        // 시간 정보 가져오는 객체 생성 후 저장
        // 참고: https://krksap.tistory.com/1158
        // 참고: https://heowc.dev/2018/03/18/java8-time-package/
        // 참고: https://howtodoinjava.com/java/date-time/zoneddatetime-parse/
        // 참고: https://galid1.tistory.com/653
        ZonedDateTime seoulDateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentDateTime = LocalDateTime.parse(seoulDateTime.format(formatter), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // String -> LocalDateTime 로 타입 변경
            LocalDateTime localDateTime = LocalDateTime.parse(item.getCreate_date(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

//            Log.e("ReviewAdapter", " LocalDateTime 로 변경한 현재 시간 값: " + currentDateTime);
//            Log.e("ReviewAdapter", " LocalDateTime 로 변경한 글 등록 시간 값: " + localDateTime);

            // 글 등록시간, 현재 시간 비교
            Duration duration = null;
            duration = Duration.between(localDateTime, currentDateTime);
            long diffTime = 0;
            diffTime = duration.toMillis() / 1000;
            if (diffTime < SEC)
            {
                // Log.d(TAG, diffTime + " 초전");
                holder.review_date.setText(diffTime + "초 전");
            }
            else if ((diffTime /= SEC) < MIN)
            {
                // Log.d(TAG, diffTime + " 분전");
                holder.review_date.setText(diffTime + "분 전");
            }
            else if ((diffTime /= MIN) < HOUR)
            {
                // Log.d(TAG, diffTime + " 시간전");
                holder.review_date.setText(diffTime + "시간 전");
            }
            else if ((diffTime /= HOUR) < DAY)
            {
                //  Log.d(TAG, diffTime + " 일전");
                holder.review_date.setText(diffTime + " 일 전");
            }
            else if ((diffTime /= DAY) < MONTH)
            {
                // Log.d(TAG, diffTime + " 달전");
                holder.review_date.setText(diffTime + " 달 전");
            }
            else
            {
                // Log.d(TAG, diffTime + " 년전");
                holder.review_date.setText(diffTime + " 년 전");
            }
        }

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder
    {
        private ConstraintLayout review_item_layout;
        private LinearLayout like_layout;
        private CardView cardView;
        private ImageView review_image;
//        private RoundedImageView review_image;
        private TextView review_writer, review_date, review_content, update_textview, delete_textview;
        private RatingBar review_rate;
        ItemClickListener itemClickListener;

        public ReviewViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            cardView = view.findViewById(R.id.review_image_card);
            review_item_layout = view.findViewById(R.id.review_item_layout);
            review_image = view.findViewById(R.id.review_image);
            review_writer = view.findViewById(R.id.review_id);
            review_date = view.findViewById(R.id.review_date);
            review_content = view.findViewById(R.id.review_content);
            review_rate = view.findViewById(R.id.review_rate);
            update_textview = view.findViewById(R.id.update_textview);
            delete_textview = view.findViewById(R.id.delete_textview);

            // 좋아요 버튼
           like_layout = view.findViewById(R.id.like_layout);

            this.itemClickListener = itemClickListener;
            review_item_layout.setOnClickListener(v -> {
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
}
