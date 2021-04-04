package com.psj.welfare.adapter;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.hedgehog.ratingbar.RatingBar;
import com.psj.welfare.R;
import com.psj.welfare.data.ReviewItem;

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
    private ItemClickListener itemClickListener;
    private DeleteClickListener deleteClickListener;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public void setOnDeleteClickListener(DeleteClickListener deleteClickListener)
    {
        this.deleteClickListener = deleteClickListener;
    }

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public ReviewAdapter(Context context, List<ReviewItem> list, ItemClickListener itemClickListener, DeleteClickListener deleteClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view, itemClickListener, deleteClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position)
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
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_pref", 0);
        if (!item.getWriter().equals(sharedPreferences.getString("user_nickname", "")))
        {
            holder.update_textview.setVisibility(View.GONE);
            holder.delete_textview.setVisibility(View.GONE);
            holder.like_layout.setVisibility(View.GONE);
        }
        else
        {
            holder.update_textview.setVisibility(View.VISIBLE);
            holder.delete_textview.setVisibility(View.VISIBLE);
            holder.like_layout.setVisibility(View.INVISIBLE);
        }

        String user_nickname = sharedPreferences.getString("user_nickname", "");
        if (list.get(position).getWriter().equals(user_nickname))
        {
            holder.update_textview.setVisibility(View.VISIBLE);
            holder.delete_textview.setVisibility(View.VISIBLE);
            holder.like_layout.setVisibility(View.INVISIBLE);

        }
        else
        {
            holder.update_textview.setVisibility(View.GONE);
            holder.delete_textview.setVisibility(View.GONE);
            holder.like_layout.setVisibility(View.GONE);
        }

        if (item.getStar_count() != 0.0)
        {
            holder.review_rate.setStar(count);
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
            LocalDateTime localDateTime = LocalDateTime.parse(item.getCreate_date(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Duration duration = null;
            duration = Duration.between(localDateTime, currentDateTime);
            long diffTime = 0;
            diffTime = duration.toMillis() / 1000;
            if (diffTime < SEC)
            {
                holder.review_date.setText(diffTime + "초 전");
            }
            else if ((diffTime /= SEC) < MIN)
            {
                holder.review_date.setText(diffTime + "분 전");
            }
            else if ((diffTime /= MIN) < HOUR)
            {
                holder.review_date.setText(diffTime + "시간 전");
            }
            else if ((diffTime /= HOUR) < DAY)
            {
                holder.review_date.setText(diffTime + " 일 전");
            }
            else if ((diffTime /= DAY) < MONTH)
            {
                holder.review_date.setText(diffTime + " 달 전");
            }
            else
            {
                holder.review_date.setText(diffTime + " 년 전");
            }
        }

        if (position == getItemCount() - 1)
        {
            holder.recycler_divider.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder
    {
        private LinearLayout like_layout;
        private CardView cardView;
        private ImageView review_image;
        private TextView review_writer, review_date, review_content, update_textview, delete_textview;
        private RatingBar review_rate;
        ItemClickListener itemClickListener;
        DeleteClickListener deleteClickListener;

        private View recycler_divider;

        public ReviewViewHolder(@NonNull View view, ItemClickListener itemClickListener, DeleteClickListener deleteClickListener)
        {
            super(view);

            cardView = view.findViewById(R.id.review_image_card);
            review_image = view.findViewById(R.id.review_image);
            review_writer = view.findViewById(R.id.review_id);
            review_date = view.findViewById(R.id.review_date);
            review_content = view.findViewById(R.id.review_content);
            review_rate = view.findViewById(R.id.review_rate);
            update_textview = view.findViewById(R.id.update_textview);
            delete_textview = view.findViewById(R.id.delete_textview);
            recycler_divider = view.findViewById(R.id.recycler_divider);

            // 좋아요 버튼
            like_layout = view.findViewById(R.id.like_layout);

            this.itemClickListener = itemClickListener;
            update_textview.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(v, pos);
                }
            });

            this.deleteClickListener = deleteClickListener;
            delete_textview.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteClickListener != null)
                {
                    deleteClickListener.onDeleteClick(v, pos);
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

    public interface DeleteClickListener
    {
        void onDeleteClick(View view, int position);
    }

}
