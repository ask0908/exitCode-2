package com.psj.welfare.adapter;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.data.BookmarkItem;

import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>
{
    private final String TAG = "BookmarkAdapter";

    private Context context;
    private List<BookmarkItem> list;
    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public BookmarkAdapter(Context context, List<BookmarkItem> list, OnItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public BookmarkAdapter.BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarkViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkAdapter.BookmarkViewHolder holder, int position)
    {
        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        setsize(holder);

        final BookmarkItem item = list.get(position);
        holder.bookmark_welf_name.setText(item.getWelf_name());

//        Log.e(TAG,item.getTag());

        if (item.getTag().contains("-"))
        {
            String before = item.getTag().replace(" ", "");
            String str = "#" + before;
            String s = str.replace("-", " #");
            String s1 = s.replace(" -", " #");
            String s2 = s1.replace("- ", " #");
            String s3 = s2.replace(" - ", " #");
            holder.bookmark_tag.setText(s3);
        }
        else if (!item.getTag().equals(""))
        {
            String before = item.getTag().replace(" ", "");
            String else_str = "#" + before;
            holder.bookmark_tag.setText(else_str);
        }
        else if (item.getTag().equals("None"))
        {
            holder.bookmark_tag.setText("");
        }
        else if(item.getTag().equals("")){
            holder.bookmark_tag.setText("");
        }
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class BookmarkViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout bookmark_name_layout; //북마크 이름 레이아웃
        TextView bookmark_welf_name, bookmark_tag, bookmark_detail; //북마크 이름, 태그, "자세히보기"
        OnItemClickListener itemClickListener;

        public BookmarkViewHolder(@NonNull View view, OnItemClickListener itemClickListener)
        {
            super(view);
            bookmark_name_layout = view.findViewById(R.id.bookmark_name_layout);
            bookmark_welf_name = view.findViewById(R.id.bookmark_welf_name);
            bookmark_tag = view.findViewById(R.id.bookmark_tag);
            bookmark_detail = view.findViewById(R.id.bookmark_detail);

            this.itemClickListener = itemClickListener;
            bookmark_detail.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void setsize(BookmarkViewHolder holder) {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        //아래와 같이 마진을 주기 위해서는 마진을 주려는 객체가 최상의 view이면 안되고 해당 객체를 감싸는 LinearLayout이 필요하다
        //마진값 주기 위한 처리
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.bookmark_name_layout.getLayoutParams();
        layoutParams.topMargin = (int)(size.y * 0.025);
        layoutParams.leftMargin = (int)(size.x * 0.025);
        layoutParams.bottomMargin = (int)(size.y * 0.0115);
        holder.bookmark_name_layout.setLayoutParams(layoutParams); //아이템 전체 레이아웃 마진값 설정

        holder.bookmark_welf_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size.x*0.0395)); //혜택 이름
        holder.bookmark_tag.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size.x*0.038)); //혜택 태그
        holder.bookmark_tag.setPadding((int)(size.x*0.025),0,(int)(size.x*0.025),0);
        holder.bookmark_detail.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size.x*0.036)); //"자세히보기" 텍스트
    }

    public interface OnItemClickListener
    {
        void onItemClick(int pos);
    }

}
