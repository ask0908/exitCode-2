package com.psj.welfare.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;
import java.util.List;

public class InnerRecyclerViewAdapter extends RecyclerView.Adapter<InnerRecyclerViewAdapter.ViewHolder>
{
    private final String TAG = InnerRecyclerViewAdapter.class.getSimpleName();

    public ArrayList<String> nameList;  // 모든 값이 필터 구분 없이 들어있는 리스트
    public static ArrayList<String> categoryList = new ArrayList<>();       // 카테고리 란에서 선택한 체크박스들을 담을 리스트
    public static ArrayList<String> localList = new ArrayList<>();          // 지역 란에서 선택한 체크박스들을 담을 리스트
    public static ArrayList<String> provideTypeList = new ArrayList<>();    // 지원 형태 란에서 선택한 체크박스들을 담을 리스트
    public static ArrayList<String> ageList = new ArrayList<>();            // 나이대 란에서 선택한 체크박스들을 담을 리스트

    public InnerRecyclerViewAdapter(ArrayList<String> nameList)
    {
        this.nameList = nameList;
    }

    public InnerRecyclerViewAdapter(ArrayList<String> nameList, ArrayList<String> categoryList, ArrayList<String> localList, ArrayList<String> provideTypeList,
                                    ArrayList<String> ageList)
    {
        this.nameList = nameList;
        InnerRecyclerViewAdapter.categoryList = categoryList;
        InnerRecyclerViewAdapter.localList = localList;
        InnerRecyclerViewAdapter.provideTypeList = provideTypeList;
        InnerRecyclerViewAdapter.ageList = ageList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_expand_item_view, parent, false);

        InnerRecyclerViewAdapter.ViewHolder vh = new InnerRecyclerViewAdapter.ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.checkBox.setTag(position);
        holder.checkBox.setText(nameList.get(position));
        holder.checkBox.setOnClickListener(v -> {
            // 체크박스 체크 값을 가져와서
            boolean getChecked = holder.checkBox.isChecked();
            // 체크된 것만 리스트에 담는다 -> 체크한 후 체크해제하면 리스트에서 지워야 한다
            if (getChecked)
            {
                if (!ageList.contains(nameList.get(position)))
                {
                    ageList.add(nameList.get(position));
                    Log.e(TAG, "내가 선택한 것 : " + nameList.get(position));
                    Log.e(TAG, "static ageList : " + ageList);
                    Log.e(TAG, "ageList size() : " + ageList.size());
                }
                // 리스트에 담기 전에 리스트에 해당 값이 들어있는지 확인한 후 없다면 넣는다
                if (!categoryList.contains(nameList.get(position)))
                {
                    categoryList.add(nameList.get(position));
                    Log.e(TAG, "nameList.get(position) : " + nameList.get(position));
                    Log.e(TAG, "static categoryList : " + categoryList);
                    Log.e(TAG, "categoryList size() : " + categoryList.size());
                }
            }
            if (!getChecked)
            {
                categoryList.remove(nameList.get(position));
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return nameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        CheckBox checkBox;
        TextView name;

        public ViewHolder(View view)
        {
            super(view);
            checkBox = view.findViewById(R.id.itemCheckbox);
//            name = view.findViewById(R.id.itemTextView);
        }
    }

}
