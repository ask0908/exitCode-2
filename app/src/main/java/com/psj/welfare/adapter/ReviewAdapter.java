package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hedgehog.ratingbar.RatingBar;
import com.psj.welfare.Data.ReviewItem;
import com.psj.welfare.R;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>
{
    private Context context;
    private List<ReviewItem> list;

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public ReviewAdapter(Context context, List<ReviewItem> list)
    {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ReviewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item_test, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ReviewViewHolder holder, int position)
    {
        ReviewItem item = list.get(position);
        holder.review_id.setText(item.getId());
        Glide.with(context)
                .load(item.getImage_url())
                .into(holder.review_image);
        holder.review_content.setText(item.getContent());
        holder.review_date.setText(item.getCreate_date());
        float count = item.getStar_count();
        Log.e("adapter", "getId() : " + item.getId());
        Log.e("adapter", "별점 : " + item.getStar_count());
        if (item.getStar_count() != 0.0)
        {
            Log.e("별점", "rate = " + item.getStar_count());
            holder.review_rate.setStar(3.0f);
        }
        else
        {
            Log.e("별점", "rate = 0.0");
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

            Log.e("adapter", " LocalDateTime 로 변경한 현재 시간 값: " + currentDateTime);
            Log.e("adapter", " LocalDateTime 로 변경한 글 등록 시간 값: " + localDateTime);

            // 글 등록시간, 현재 시간 비교
            Duration duration = null;
            duration = Duration.between(localDateTime, currentDateTime);
            long diffTime = 0;
            diffTime = duration.toMillis() / 1000;
            if (diffTime < SEC)
            {
                // Log.d(TAG, diffTime + " 초전");
                holder.review_date.setText(diffTime + "초전");
            }
            else if ((diffTime /= SEC) < MIN)
            {
                // Log.d(TAG, diffTime + " 분전");
                holder.review_date.setText(diffTime + "분전");
            }
            else if ((diffTime /= MIN) < HOUR)
            {
                // Log.d(TAG, diffTime + " 시간전");
                holder.review_date.setText(diffTime + "시간전");
            }
            else if ((diffTime /= HOUR) < DAY)
            {
                //  Log.d(TAG, diffTime + " 일전");
                holder.review_date.setText(diffTime + " 일전");
            }
            else if ((diffTime /= DAY) < MONTH)
            {
                // Log.d(TAG, diffTime + " 달전");
                holder.review_date.setText(diffTime + " 달전");
            }
            else
            {
                // Log.d(TAG, diffTime + " 년전");
                holder.review_date.setText(diffTime + " 년전");
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
        private ImageView review_image;
        private TextView review_id, review_date, review_content;
        private RatingBar review_rate;

        public ReviewViewHolder(@NonNull View view)
        {
            super(view);

            review_image = view.findViewById(R.id.review_image);
            review_id = view.findViewById(R.id.review_id);
            review_date = view.findViewById(R.id.review_date);
            review_content = view.findViewById(R.id.review_content);
            review_rate = view.findViewById(R.id.review_rate);
        }
    }
}
