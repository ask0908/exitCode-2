package com.psj.welfare.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.BookmarkItem;

import java.util.List;

public class BookmarkEditAdapter extends RecyclerView.Adapter<BookmarkEditAdapter.BookmarkEditViewHolder>
{
    private final String TAG = this.getClass().getSimpleName();

    private Context context;
    private List<BookmarkItem> list;
    private ItemClickListener itemClickListener;
    private OnItemCheckListener itemCheckListener;  /* 체크된 체크박스들을 리스트에 담기 위해 어댑터의 전역변수로 인터페이스 참조변수 선언 */
    private boolean isSelectedAll = false;

    private int checked_count = 0;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public BookmarkEditAdapter(Context context, List<BookmarkItem> list, ItemClickListener itemClickListener, OnItemCheckListener itemCheckListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
        this.itemCheckListener = itemCheckListener;
    }

    @NonNull
    @Override
    public BookmarkEditAdapter.BookmarkEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.bookmark_edit_item, parent, false);
        return new BookmarkEditViewHolder(view, itemClickListener, itemCheckListener);
    }

    // 전체선택 해제
    public void deselectAll()
    {
        isSelectedAll = false;
        notifyDataSetChanged();
    }

    // 전체선택
    public void selectAll()
    {
        isSelectedAll = true;
        notifyDataSetChanged();
    }

    // 체크된 체크박스 개수를 리턴하는 메서드
    public int getCheckedCount()
    {
        return checked_count;
    }

    @Override
    public void onBindViewHolder(@NonNull final BookmarkEditAdapter.BookmarkEditViewHolder holder, final int position)
    {
        final BookmarkItem item = list.get(position);
        Log.e(TAG, "북마크 삭제 화면 - item.getTag() : " + item.getTag());
        holder.bookmark_edit_welf_name.setText(item.getWelf_name());
        if (item.getTag().contains("-"))
        {
            String before = item.getTag().replace(" ", "");
            String str = "#" + before;
            String s = str.replace("-", " #");
            String s1 = s.replace(" -", " #");
            String s2 = s1.replace("- ", " #");
            String s3 = s2.replace(" - ", " #");
            holder.bookmark_edit_tag.setText(s3);
        }
        else if (!item.getTag().equals(""))
        {
            String before = item.getTag().replace(" ", "");
            String else_str = "#" + before;
            holder.bookmark_edit_tag.setText(else_str);
        }
        else if (item.getTag().equals("None"))
        {
            holder.bookmark_edit_tag.setText("");
        }

        if (isSelectedAll)
        {
            holder.bookmark_edit_checkbox.setChecked(true);
        }
        else
        {
            holder.bookmark_edit_checkbox.setChecked(false);
        }

        holder.bookmark_edit_checkbox.setOnCheckedChangeListener(null);
        holder.bookmark_edit_checkbox.setChecked(item.getSelected());
        holder.bookmark_edit_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    checked_count++;
                    holder.bookmark_edit_checkbox.setBackground(ContextCompat.getDrawable(context, R.drawable.bookmark_check));
                    holder.bookmark_edit_item_parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.bookmark_recyclerview_item));
                }
                else
                {
                    checked_count--;
                    holder.bookmark_edit_checkbox.setBackground(ContextCompat.getDrawable(context, R.drawable.bookmark_uncheck));
                    holder.bookmark_edit_item_parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMainWhite));
                }
            }
        });

        // 전체선택 or 전체선택 해제
        if (isSelectedAll)
        {
            holder.bookmark_edit_checkbox.setChecked(true);
//            holder.bookmark_edit_checkbox.setBackground(ContextCompat.getDrawable(context, R.drawable.bookmark_check));
//            holder.bookmark_edit_item_parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.bookmark_recyclerview_item));
        }
        else
        {
            holder.bookmark_edit_checkbox.setChecked(false);
//            holder.bookmark_edit_checkbox.setBackground(ContextCompat.getDrawable(context, R.drawable.bookmark_uncheck));
//            holder.bookmark_edit_item_parent_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMainWhite));
        }

        /* 체크박스에 클릭 리스너를 추가해서 체크박스가 체크/체크해제될 때마다 리스트에 값을 넣을 겁니다 */
        holder.bookmark_edit_checkbox.setOnClickListener(v -> {
            /* 여기서 클릭 리스너의 내부 로직을 만들진 않습니다. 액티비티에서 개수를 보여줘야 하기 때문에 내부 로직은 액티비티에서 구현합니다
            * 먼저 체크박스가 체크됐는지 확인합니다
            * -> 그 다음 체크됐으면 onItemCheck(), onItemUncheck()의 인자로 체크된 또는 체크해제된 체크박스를 인자로 넘깁니다 */
            if (holder.bookmark_edit_checkbox.isChecked())
            {
                itemCheckListener.onItemCheck(item);
                Log.e(TAG, "체크된 아이템의 이름 : " + item.getWelf_name());
                Log.e(TAG, "체크된 아이템의 id : " + item.getId());
//                checked_count++;
            }
            else
            {
                itemCheckListener.onItemUncheck(item);
                Log.e(TAG, "체크해제된 아이템 이름 : " + item.getWelf_name());
//                checked_count--;
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public static class BookmarkEditViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout bookmark_edit_item_parent_layout;
        TextView bookmark_edit_welf_name, bookmark_edit_tag;
        CheckBox bookmark_edit_checkbox;
        ItemClickListener itemClickListener;
        OnItemCheckListener itemCheckListener;

        public BookmarkEditViewHolder(@NonNull View view, ItemClickListener itemClickListener, OnItemCheckListener itemCheckListener)
        {
            super(view);
            bookmark_edit_item_parent_layout = view.findViewById(R.id.bookmark_edit_item_parent_layout);
            bookmark_edit_welf_name = view.findViewById(R.id.bookmark_edit_welf_name);
            bookmark_edit_tag = view.findViewById(R.id.bookmark_edit_tag);
            bookmark_edit_checkbox = view.findViewById(R.id.bookmark_edit_checkbox);

            this.itemClickListener = itemClickListener;
            bookmark_edit_item_parent_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });

            this.itemCheckListener = itemCheckListener;
        }

    }

    public interface ItemClickListener
    {
        void onItemClick(int pos);
    }

    /* 선택된 체크박스의 개수를 세서 액티비티의 텍스트뷰에 setText()하기 위해 만든 인터페이스입니다. 전역변수로도 선언했으니 확인해 주세요
    * onItemCheck() : 체크된 아이템을 리스트에 담기 위해 만든 메서드입니다
    * onItemUncheck() : 체크해제된 아이템을 리스트에서 빼기 위해 만든 메서드입니다
    * 이것만 확인하고 125번 줄로 이동해 주세요 */
    public interface OnItemCheckListener
    {
        void onItemCheck(BookmarkItem item);
        void onItemUncheck(BookmarkItem item);
    }

}
