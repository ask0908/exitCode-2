package com.psj.welfare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.PushGatherItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PushGatherAdapter extends RecyclerView.Adapter<PushGatherAdapter.PushGatherViewHolder>
{
    private Context context;
    private List<PushGatherItem> lists;
    private ItemClickListener itemClickListener;

    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

    public PushGatherAdapter(Context context, List<PushGatherItem> lists, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.lists = lists;
        this.itemClickListener = itemClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PushGatherAdapter.PushGatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.push_gather_item, parent, false);
        return new PushGatherViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PushGatherAdapter.PushGatherViewHolder holder, int position)
    {
        PushGatherItem item = lists.get(position);
        holder.push_gather_title.setText(item.getPush_gather_title());
        String welf_name;
        if (item.getWelf_name().contains(";; "))
        {
            welf_name = item.getWelf_name().replace(";; ", ", ");
            holder.push_gather_desc.setText(welf_name);
        }
        else
        {
            holder.push_gather_desc.setText(item.getWelf_name());
        }
        holder.push_gather_date.setText(item.getPush_gather_date());

        // 오늘 날짜의 연월일시분초를 담을 변수
        ZonedDateTime seoulDateTime;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            // 서울 기준으로 연월일시분초를 구한다
            seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            // 알림이 얼마 전에 온 건지 구하기 위해 2021-01-01 12:59:59 형태로 바꿀 것이다
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 실제로 위와 같은 형태로 바꾼다
            LocalDateTime currentDateTime = LocalDateTime.parse(seoulDateTime.format(formatter), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            /* 서버에서 dateTime 컬럼의 날짜값을 받으면 T가 섞여 있는데 이것 때문에 며칠 전에 왔는지 계산하는 부분에서 에러가 발생한다
            * 그래서 서버에서 받은 날짜값 중 T를 없애야 하는데 아래는 그 로직이다 */
            // T를 없앤 날짜값을 담을 String 변수를 선언, null 값이라도 넣지 않으면 빨간 줄이 생긴다
            String date = null;
            // 서버에서 받은 날짜 사이에 T가 섞여있으면
            if (item.getPush_gather_date().contains("T"))
            {
                // T를 스페이스 바 한칸으로 교체
                // 2021-07-13T14:04:42 -> 2021-07-13 14:04:42로 바뀜
                date = item.getPush_gather_date().replace("T", " ");
            }
            // 푸시 알림의 날짜 사이의 차이를 구함
            LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 오늘 날짜와 푸시 알림이 온 날짜 사이의 차이를 구한다
            Duration duration = Duration.between(localDateTime, currentDateTime);
            // 얻은 값을 1000으로 나눠 n초 전, n분 전 등을 알 수 있도록 처리
            long diffTime = duration.toMillis() / 1000;
            // 상수값이 뭐냐에 따라 다른 텍스트를 보여준다
            if (diffTime < SEC)
            {
                holder.push_gather_date.setText(diffTime + "초 전");
            }
            else if ((diffTime /= SEC) < MIN)
            {
                holder.push_gather_date.setText(diffTime + "분 전");
            }
            else if ((diffTime /= MIN) < HOUR)
            {
                holder.push_gather_date.setText(diffTime + "시간 전");
            }
            else if ((diffTime /= HOUR) < DAY)
            {
                holder.push_gather_date.setText(diffTime + " 일 전");
            }
            else if ((diffTime /= DAY) < MONTH)
            {
                holder.push_gather_date.setText(diffTime + " 달 전");
            }
            else
            {
                holder.push_gather_date.setText(diffTime + " 년 전");
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return lists.size();
    }

    public class PushGatherViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout push_gather_item_layout;
        TextView push_gather_title, push_gather_desc, push_gather_date;
        ItemClickListener itemClickListener;

        public PushGatherViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            push_gather_item_layout = view.findViewById(R.id.push_gather_item_layout);
            push_gather_title = view.findViewById(R.id.push_gather_title);
            push_gather_desc = view.findViewById(R.id.push_gather_desc);
            push_gather_date = view.findViewById(R.id.push_gather_date);

            this.itemClickListener = itemClickListener;
            push_gather_item_layout.setOnClickListener(v ->
            {
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

    public List<PushGatherItem> getList()
    {
        return lists;
    }

}
