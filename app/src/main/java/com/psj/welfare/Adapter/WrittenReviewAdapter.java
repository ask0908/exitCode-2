package com.psj.welfare.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.DetailReviewWrite;
import com.psj.welfare.R;
import com.psj.welfare.custom.MyReviewListener;
import com.psj.welfare.data.WrittenReviewItem;
import com.psj.welfare.util.DBOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WrittenReviewAdapter extends RecyclerView.Adapter<WrittenReviewAdapter.WrittenReviewViewHolder>
{
    private final String TAG = WrittenReviewAdapter.class.getSimpleName();

    private Context context;
    private List<WrittenReviewItem> list;
    private OnItemClickListener itemClickListener;
    private MyReviewListener myReviewListener;
//    private OnDeleteListener deleteListener;

    String result_date, welf_id, welf_name;
    DBOpenHelper helper;
    String sqlite_token;

    public void setOnReviewListener(MyReviewListener myReviewListener)
    {
        this.myReviewListener = myReviewListener;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public WrittenReviewAdapter(Context context, List<WrittenReviewItem> list, OnItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
        helper = new DBOpenHelper(context);
        helper.openDatabase();
        helper.create();

        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
    }

    @NonNull
    @Override
    public WrittenReviewAdapter.WrittenReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.written_review_item, parent, false);
        return new WrittenReviewViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WrittenReviewAdapter.WrittenReviewViewHolder holder, int position)
    {
        WrittenReviewItem item = list.get(position);
        holder.written_review_writer.setText(item.getWriter());
        holder.written_review_star.setStar(item.getStar_count());
        holder.written_review_welf_name.setText(item.getWelf_name());
        holder.written_review_content.setText(item.getContent());
        welf_id = String.valueOf(item.getWelf_id());
        welf_name = item.getWelf_name();

        // 21/06/06 형식으로 날짜 보여주는 처리부
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-mm-dd");
        try
        {
            Date tempDate = dateFormat.parse(item.getCreate_date());
            SimpleDateFormat newFormat = new SimpleDateFormat("yy/mm/dd");
            result_date = newFormat.format(tempDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        holder.written_review_date.setText(result_date);

        // 이름 오른쪽의 메뉴 버튼
        holder.written_review_option.setOnClickListener(v ->
        {
            PopupMenu menu = new PopupMenu(context, holder.written_review_option);
            menu.inflate(R.menu.written_review_menu);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem item)
                {
                    switch (item.getItemId())
                    {
                        case R.id.written_menu_edit:    // 수정 클릭
                            Intent intent = new Intent(context, DetailReviewWrite.class);
                            intent.putExtra("welfId", list.get(position).getWelf_id());
                            intent.putExtra("welf_name", list.get(position).getWelf_name());
                            intent.putExtra("review_id", list.get(position).getReview_id());

                            intent.putExtra("Star_count", list.get(position).getStar_count());
                            intent.putExtra("satisfaction", list.get(position).getSatisfaction());
                            intent.putExtra("difficulty_level", list.get(position).getDifficulty_level());
                            intent.putExtra("content", list.get(position).getContent());

//                            Log.e(TAG,"Star_count : "+ list.get(position).getStar_count());
//                            Log.e(TAG,"satisfaction : "+ list.get(position).getSatisfaction());
//                            Log.e(TAG,"difficulty_level : "+ list.get(position).getDifficulty_level());
//                            Log.e(TAG,"content : "+ list.get(position).getContent());

                           intent.putExtra("review_edit", 100);
//                            Log.e(TAG, "수정할 혜택의 welf_id : " + list.get(position).getWelf_id() + ", welf_name : " + list.get(position).getWelf_name());
                            context.startActivity(intent);
                            notifyDataSetChanged();
                            break;

                        case R.id.written_menu_delete:  // 삭제 클릭
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("삭제하신 리뷰는 복구할 수 없어요.\n정말 리뷰를 삭제하시겠어요?")
                                    .setPositiveButton("예", (dialog, which) ->
                                    {
                                        // 리뷰 삭제 메서드 호출
                                        myReviewListener.deleteReview(true, list.get(position).getReview_id());
                                    })
                                    .setNegativeButton("아니오", ((dialog, which) ->
                                    {
                                        Toast.makeText(context, "리뷰 삭제를 취소했어요", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }))
                                    .show();
                            break;

                        default:
                            break;
                    }
                    return false;
                }
            });
            menu.show();
        });

        //레이아웃의 사이즈를 동적으로 맞춤
        setsize(holder);
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class WrittenReviewViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout written_review_item_layout, written_review_detail_layout;
        com.hedgehog.ratingbar.RatingBar written_review_star;
        TextView written_review_welf_name, written_review_option, written_review_writer, written_review_date, written_review_content;
        OnItemClickListener itemClickListener;
        ConstraintLayout review_content_layout; //리뷰 내용 레이아웃
        LinearLayout review_title_layout,review_star_layout; //리뷰 타이틀 레이아웃, 리뷰 별점 레이아웃
        public WrittenReviewViewHolder(@NonNull View view, OnItemClickListener itemClickListener)
        {
            super(view);

            review_title_layout = view.findViewById(R.id.review_title_layout);
            review_star_layout = view.findViewById(R.id.review_star_layout);
            review_content_layout = view.findViewById(R.id.review_content_layout);
            written_review_item_layout = view.findViewById(R.id.written_review_item_layout);
            written_review_detail_layout = view.findViewById(R.id.written_review_detail_layout);
            written_review_welf_name = view.findViewById(R.id.written_review_welf_name);
            written_review_star = view.findViewById(R.id.written_review_star);
            written_review_option = view.findViewById(R.id.written_review_option);
            written_review_writer = view.findViewById(R.id.written_review_writer);
            written_review_date = view.findViewById(R.id.written_review_date);
            written_review_content = view.findViewById(R.id.written_review_content);

            this.itemClickListener = itemClickListener;
            written_review_detail_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    //레이아웃의 사이즈를 동적으로 맞춤
    private void setsize(WrittenReviewViewHolder holder) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
//        ScreenSize screen = new ScreenSize();
//        //context의 스크린 사이즈를 구함
//        Point size = screen.getScreenSize((Activity) context);

        //닉네임
        holder.written_review_writer.setPadding(0,(int)(size.y * 0.03),0,0);
        //별점 레이아웃
        holder.review_star_layout.setPadding(0,(int)(size.y * 0.003),0,0);
        //내용 레이아웃
        holder.review_content_layout.setPadding(0,(int)(size.y * 0.006),0,0);

        //아래와 같이 마진을 주기 위해서는 마진을 주려는 객체가 최상의 view이면 안되고 해당 객체를 감싸는 LinearLayout이 필요하다
        //마진값 주기 위한 처리
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.written_review_item_layout.getLayoutParams();
        layoutParams.topMargin = (int)(size.y * 0.03);
        layoutParams.bottomMargin = (int)(size.y * 0.03);
        holder.written_review_item_layout.setLayoutParams(layoutParams); //아이템 전체 레이아웃 마진값 설정

//        holder.written_review_detail_layout.setPadding(0,0,0,(int)(size.y * 0.));
        //아이템 전체 레이아웃
//        holder.written_review_item_layout.setPadding((int)(size.x * 0.04),(int)(size.y * 0.03),(int)(size.x * 0.04),(int)(size.y * 0.03));

        holder.written_review_welf_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 23);
        holder.written_review_writer.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 26);
        holder.written_review_content.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 26);
    }

    public interface OnItemClickListener
    {
        void onItemClick(int pos);
    }

}
