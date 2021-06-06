package com.psj.welfare.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.DetailReviewWrite;
import com.psj.welfare.R;
import com.psj.welfare.data.WrittenReviewItem;

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

    String result_date, welf_id, welf_name;

    public void setOnItemClickListener(OnItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public WrittenReviewAdapter(Context context, List<WrittenReviewItem> list, OnItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
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
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        WrittenReviewItem item = list.get(position);
        holder.written_review_writer.setText(item.getWriter());
        holder.written_review_star.setStar(item.getStar_count());
        holder.written_review_welf_name.setText(item.getWelf_name());
        holder.written_review_content.setText(item.getContent());
        welf_id = String.valueOf(item.getWelf_id());
        welf_name = item.getWelf_name();

        holder.written_review_welf_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        holder.written_review_writer.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        holder.written_review_content.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        holder.written_review_content.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);

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
                        case R.id.written_menu_edit:
                            Intent intent = new Intent(context, DetailReviewWrite.class);
                            intent.putExtra("welf_id", list.get(position).getWelf_id());
                            intent.putExtra("welf_name", list.get(position).getWelf_name());
                            Log.e(TAG, "선택한 혜택의 welf_id : " + list.get(position).getWelf_id() + ", welf_name : " + list.get(position).getWelf_name());
                            context.startActivity(intent);
                            break;

                        case R.id.written_menu_delete:
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("삭제하신 리뷰는 되돌릴 수 없어요.\n정말 리뷰를 삭제하시겠어요?")
                                    .setPositiveButton("예", (dialog, which) ->
                                    {
                                        Toast.makeText(context, "리뷰를 삭제했습니다", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .setNegativeButton("아니오", ((dialog, which) ->
                                    {
                                        Toast.makeText(context, "리뷰 삭제 취소", Toast.LENGTH_SHORT).show();
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

        public WrittenReviewViewHolder(@NonNull View view, OnItemClickListener itemClickListener)
        {
            super(view);

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

    public interface OnItemClickListener
    {
        void onItemClick(int pos);
    }

}
