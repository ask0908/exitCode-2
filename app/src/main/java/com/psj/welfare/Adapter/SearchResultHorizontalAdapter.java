package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
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
import java.util.Arrays;
import java.util.List;

/* 선택한 체크박스 필터들을 가로로 보여줄 때 사용할 리사이클러뷰 어댑터 */
public class SearchResultHorizontalAdapter extends RecyclerView.Adapter<SearchResultHorizontalAdapter.SearchResultHorizontalViewHolder>
{
    private final String TAG = SearchResultHorizontalAdapter.class.getSimpleName();

    private Context context;
    private ItemClickListener itemClickListener;

    // 카테고리 필터별 체크박스 이름들을 저장할 리스트
    ArrayList<String> categoryList;
    ArrayList<String> localList;
    ArrayList<String> provideTypeList;
    ArrayList<String> ageList;
    ArrayList<String> mList;
    ArrayList<String> sList;

    public void setOnItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    /* 생성자를 통해서 카테고리 필터별 체크박스 이름들을 모두 받는다 */
    public SearchResultHorizontalAdapter(Context context,
                                         ArrayList<String> allList,
                                         ArrayList<String> categoryList,
                                         ArrayList<String> localList,
                                         ArrayList<String> provideTypeList,
                                         ArrayList<String> ageList,
                                         ItemClickListener itemClickListener)
    {
        this.context = context;
        this.categoryList = categoryList;
        this.localList = localList;
        this.provideTypeList = provideTypeList;
        this.ageList = ageList;
        this.itemClickListener = itemClickListener;

        Log.e(TAG, "어댑터에서 받은 allList 값 : " + allList);  // 값 들어오는 것 확인
        sList = allList;
        sList.addAll(allList);
        Log.e(TAG, "sList 값 확인 : " + sList);

        String[] str_arr = new String[4];
        if (sList.size() != 0)
        {
            if (!sList.get(0).equals("null") || !sList.get(1).equals("null") || !sList.get(2).equals("null") || !sList.get(3).equals("null"))
            {
                str_arr[0] = sList.get(0);
                str_arr[1] = sList.get(1);
                str_arr[2] = sList.get(2);
                str_arr[3] = sList.get(3);
            }
        }
        Log.e(TAG, "str_arr 안에 값 넣어본 결과 : " + Arrays.toString(str_arr));

        valueSplit(allList);

//        Log.e(TAG, "categoryList 값 확인 : " + categoryList);
//        Log.e(TAG, "localList 값 확인 : " + localList);
//        Log.e(TAG, "provideTypeList 값 확인 : " + provideTypeList);
//        Log.e(TAG, "ageList 값 확인 : " + ageList);
    }

    // 어댑터 생성자의 인자로 넘겨 받은 리스트 안의 값들을 꺼내 split하는 메서드
    // '-'를 기준으로 split한 다음 낱개로 가로 필터 리사이클러뷰에 보여주기 위한 처리
    private void valueSplit(List<String> list)
    {
        /**
         * 1. 어댑터에서 전체 리스트를 받으면 for로 안의 값들을 모두 꺼낸다
         * 2. 1에서 리스트의 값을 다 꺼내면 카테고리 별로 구분짓고 '-'를 기준으로 split
         * -> 4개의 String[]이 만들어질 것이다. (null 처리 필수) 이 때 String[].length > 0일 경우 안의 값들을 ArrayList로 만든다
         * -> 모든 값들을 ArrayList 안에 때려박는다(클릭 시 포지션 값 추출하기 위한 것도 있음)
         * 3. onBindViewHolder()에서 setText로 2번에서 만들어진 리스트의 값을 꺼내 set
         */
        Log.e(TAG, "list 값을 담은 sList 안의 원본 데이터 : " + sList);
        StringBuilder stringBuilder = new StringBuilder();
        // 인자로 받은 리스트 안의 값을 for를 사용해 모두 꺼낸다
        for (int i = 0; i < list.size(); i++)
        {
            // 어디에 담지?? split할 거니까 StringBuilder에 담아보자
            stringBuilder.append(list.get(i)).append("-");
        }
        if (!stringBuilder.toString().equals(""))
        {
            Log.e(TAG, "StringBuilder로 리스트 안의 값 이어붙이기 됐는지 확인 : " + stringBuilder);  // 값 들어온 것 확인
            // stringBuilder 안의 값들을 '-zz'를 기준으로 split
            String[] arr = stringBuilder.toString().split("-");
            // arr[0] 식으로 인덱스를 넣으면 액티비티에서처럼 필터 카테고리 별로 구분지어지는 처리
            mList = new ArrayList<>(Arrays.asList(arr));
//            sList = new ArrayList<>(Arrays.asList(arr));
        }
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
        if (!mList.get(position).equals("null"))
        {
            // 중복 처리
            if (!holder.search_filter_name.getText().equals(mList.get(position)))
            {
                holder.search_filter_name.setText(mList.get(position));
                holder.search_filter_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float) size.x / 80);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return mList.size();
    }

    public class SearchResultHorizontalViewHolder extends RecyclerView.ViewHolder
    {
        LinearLayout search_filter_layout;
        TextView search_filter_name;
        ItemClickListener itemClickListener;

        ArrayList<String> mCategoryList;
        ArrayList<String> mLocalList;
        ArrayList<String> mProvideTypeList;
        ArrayList<String> mAgeList;

        public SearchResultHorizontalViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            search_filter_layout = view.findViewById(R.id.search_filter_layout);
            search_filter_name = view.findViewById(R.id.search_filter_name);

            mCategoryList = new ArrayList<>();
            mLocalList = new ArrayList<>();
            mProvideTypeList = new ArrayList<>();
            mAgeList = new ArrayList<>();

            mCategoryList.addAll(categoryList);
            mLocalList.addAll(localList);
            mProvideTypeList.addAll(provideTypeList);
            mAgeList.addAll(ageList);

//            Log.e(TAG, "mCategoryList : " + mCategoryList);
//            Log.e(TAG, "mLocalList : " + mLocalList);
//            Log.e(TAG, "mProvideTypeList : " + mProvideTypeList);
//            Log.e(TAG, "mAgeList : " + mAgeList);

            this.itemClickListener = itemClickListener;
            // 필터 클릭 시 가로 리사이클러뷰에서 아이템을 삭제하고 남은 아이템에 속하는 혜택들만 보여줘야 한다
            search_filter_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onItemClick(pos);
                    String name = mList.get(pos);
                    Log.e(TAG, "어댑터에서 확인한 이름 : " + name);
                    // 가로 리사이클러뷰에서 선택한 이름의 아이템을 삭제
                    mList.remove(name);
                    Log.e(TAG, "클릭한 필터명 삭제한 후 어댑터 뷰홀더의 mList : " + mList);   // 리사이클러뷰에서 삭제할 때마다 리스트에 삭제한 아이템들이 반영되는 것 확인
//                    Log.e(TAG, "어댑터 뷰홀더에서 sList : " + sList);
//                    Log.e(TAG, "sList 1번 : " + sList.get(0));
//                    Log.e(TAG, "sList 2번 : " + sList.get(1));
//                    Log.e(TAG, "sList 3번 : " + sList.get(2));
//                    Log.e(TAG, "sList 4번 : " + sList.get(3));

                    /* for문으로 mList 안의 값들을 각 필터별 리스트 안의 값들과 비교한다. 같은 값이 있는 경우 오른쪽에 '-'을 붙여서 String[]에 추가한다 */
//                    String[] str_arr = new String[4];
//                    ArrayList<String> list = new ArrayList<>();
//                    for (int i = 0; i < categoryList.size(); i++)
//                    {
//                        if (categoryList.get(i).equals(name))
//                        {
//                            // 카테고리는 1번째 위치에 와야 한다
//                            list.add(name + "-");
//                        }
//                    }
//                    Log.e(TAG, "list : " + list);
//                    Log.e(TAG, "str_arr : " + Arrays.toString(str_arr));
                    notifyDataSetChanged(); // 가로 리사이클러뷰에서 필터 이름 클릭해 삭제하면 리사이클러뷰에서도 지워야 하기 때문에 호출
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
        return mList;
    }

}
