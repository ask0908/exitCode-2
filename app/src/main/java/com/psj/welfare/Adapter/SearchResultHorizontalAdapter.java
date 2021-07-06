package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;

/* 선택한 체크박스 필터들을 가로로 보여줄 때 사용할 리사이클러뷰 어댑터 */
public class SearchResultHorizontalAdapter extends RecyclerView.Adapter<SearchResultHorizontalAdapter.SearchResultHorizontalViewHolder>
{
    private final String TAG = SearchResultHorizontalAdapter.class.getSimpleName();

    private Context context;
    private ItemClickListener itemClickListener;

    // 카테고리 필터별 체크박스 이름들을 저장할 리스트
    ArrayList<String> sList; //액티비티에서 받아온 필터 데이터

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public SearchResultHorizontalAdapter(Context context,ArrayList<String> allList,ItemClickListener itemClickListener){
        this.context = context;
        sList = allList;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public SearchResultHorizontalAdapter.SearchResultHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.search_result_horizontal_filter, parent, false);
        return new SearchResultHorizontalViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultHorizontalAdapter.SearchResultHorizontalViewHolder holder, int position)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        //필터 크기 동적으로 맞추기
        holder.search_filter_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) size.x / 80);
        // 중복 처리
        if (!holder.search_filter_name.getText().equals(sList.get(position)))
        {
            holder.search_filter_name.setText(sList.get(position));
        }

    }

    @Override
    public int getItemCount()
    {
        return sList.size();
    }

    public class SearchResultHorizontalViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout search_filter_layout;
        TextView search_filter_name;
        ItemClickListener itemClickListener;

        public SearchResultHorizontalViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);
            search_filter_layout = view.findViewById(R.id.search_filter_layout);
            search_filter_name = view.findViewById(R.id.search_filter_name);

            this.itemClickListener = itemClickListener;
            // 필터 클릭 시 가로 리사이클러뷰에서 아이템을 삭제하고 남은 아이템에 속하는 혜택들만 보여줘야 한다
            search_filter_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onItemClick(int pos);
    }

    // 어댑터에서 처리가 끝난 리스트를 액티비티로 넘겨줄 때 사용하는 메서드
    public ArrayList<String> getList()
    {
        return sList;
    }

}
