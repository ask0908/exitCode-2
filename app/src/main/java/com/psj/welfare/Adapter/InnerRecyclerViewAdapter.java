package com.psj.welfare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;

import java.util.ArrayList;
import java.util.List;

public class InnerRecyclerViewAdapter extends RecyclerView.Adapter<InnerRecyclerViewAdapter.ViewHolder>
{
    private final String TAG = InnerRecyclerViewAdapter.class.getSimpleName();

    public ArrayList<String> nameList;  // 모든 값이 필터 구분 없이 들어있는 리스트
    public static ArrayList<String> mCategoryList = new ArrayList<>();
    public static ArrayList<String> mLocalList = new ArrayList<>();
    public static ArrayList<String> mProvideTypeList = new ArrayList<>();
    public static ArrayList<String> mAgeList = new ArrayList<>();
    public List<String> list;

    // 액티비티에서 어댑터 안의 리스트에 들어간 아이템을 가져올 때 쓰는 메서드
    public static String getAllValues()
    {
        StringBuilder categoryBuilder = new StringBuilder();
        StringBuilder localBuilder = new StringBuilder();
        StringBuilder provideTypeBuilder = new StringBuilder();
        StringBuilder ageBuilder = new StringBuilder();
        for (int i = 0; i < mCategoryList.size(); i++)
        {
            categoryBuilder.append(mCategoryList.get(i));
            categoryBuilder.append("-");
        }
        for (int i = 0; i < mLocalList.size(); i++)
        {
            localBuilder.append(mLocalList.get(i));
            localBuilder.append("-");
        }
        for (int i = 0; i < mProvideTypeList.size(); i++)
        {
            provideTypeBuilder.append(mProvideTypeList.get(i));
            provideTypeBuilder.append("-");
        }
        for (int i = 0; i < mAgeList.size(); i++)
        {
            ageBuilder.append(mAgeList.get(i));
            ageBuilder.append("-");
        }
        // 마지막의 '-' 제거
        String lastCategory;
        String lastLocal;
        String lastAge;
        String lastProvideType;

        // 아래 처리를 하지 않으면 필터를 선택하지 않았을 시 배열 관련 에러가 뜨면서 앱이 죽는다
        if (!categoryBuilder.toString().equals(""))
        {
            lastCategory = categoryBuilder.toString().substring(0, categoryBuilder.toString().length() - 1);
        }
        else
        {
            lastCategory = null;
        }

        if (!localBuilder.toString().equals(""))
        {
            lastLocal = localBuilder.toString().substring(0, localBuilder.toString().length() - 1);
        }
        else
        {
            lastLocal = null;
        }

        if (!ageBuilder.toString().equals(""))
        {
            lastAge = ageBuilder.toString().substring(0, ageBuilder.toString().length() - 1);
        }
        else
        {
            lastAge = null;
        }

        if (!provideTypeBuilder.toString().equals(""))
        {
            lastProvideType = provideTypeBuilder.toString().substring(0, provideTypeBuilder.toString().length() - 1);
        }
        else
        {
            lastProvideType = null;
        }
        return lastCategory + "zz" + lastLocal + "zz" + lastAge + "zz" + lastProvideType;
    }

    public InnerRecyclerViewAdapter(ArrayList<String> nameList, List<String> list)
    {
        this.nameList = nameList;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_expand_item_view, parent, false);
        return new ViewHolder(v);
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
//                Log.e(TAG, "선택된 아이템의 pos : " + nameList.get(position));
                // 카테고리
                if (nameList.get(position).equals("교육") || nameList.get(position).equals("건강") || nameList.get(position).equals("근로") ||
                        nameList.get(position).equals("금융") || nameList.get(position).equals("기타") || nameList.get(position).equals("문화") ||
                        nameList.get(position).equals("사업") || nameList.get(position).equals("주거") || nameList.get(position).equals("환경"))
                {
                    if (!mCategoryList.contains("교육") || !mCategoryList.contains("건강") || !mCategoryList.contains("근로") || !mCategoryList.contains("금융") ||
                            !mCategoryList.contains("기타") || !mCategoryList.contains("문화") || !mCategoryList.contains("사업") || !mCategoryList.contains("주거") ||
                            !mCategoryList.contains("환경"))
                    {
                        mCategoryList.add(nameList.get(position));
                    }
                }

                // 지역
                if (nameList.get(position).equals("서울") || nameList.get(position).equals("경기") || nameList.get(position).equals("인천") ||
                        nameList.get(position).equals("강원") || nameList.get(position).equals("충남") || nameList.get(position).equals("충북") ||
                        nameList.get(position).equals("경북") || nameList.get(position).equals("경남") || nameList.get(position).equals("전북") ||
                        nameList.get(position).equals("전남") || nameList.get(position).equals("제주"))
                {
                    if (!mLocalList.contains("서울") || !mLocalList.contains("경기") || !mLocalList.contains("인천") || !mLocalList.contains("강원") ||
                            !mLocalList.contains("충남") || !mLocalList.contains("충북") || !mLocalList.contains("경북") || !mLocalList.contains("경남\"") ||
                            !mLocalList.contains("전북") || !mLocalList.contains("전남") || !mLocalList.contains("제주"))
                    {
                        mLocalList.add(nameList.get(position));
                    }
                }

                // 지원 형태
                if (nameList.get(position).equals("현금 지원") || nameList.get(position).equals("물품 지원") || nameList.get(position).equals("서비스 지원") ||
                        nameList.get(position).equals("세금 면제") || nameList.get(position).equals("감면") || nameList.get(position).equals("정보 지원") ||
                        nameList.get(position).equals("교육 지원") || nameList.get(position).equals("인력 지원") || nameList.get(position).equals("시설 지원") ||
                        nameList.get(position).equals("카드 지원") || nameList.get(position).equals("주거 지원") || nameList.get(position).equals("일자리 지원") ||
                        nameList.get(position).equals("융자 지원") || nameList.get(position).equals("면제 지원"))
                {
                    if (!mProvideTypeList.contains("현금 지원") || !mProvideTypeList.contains("물품 지원") || !mProvideTypeList.contains("서비스 지원") ||
                            !mProvideTypeList.contains("세금 면제") || !mProvideTypeList.contains("감면") || !mProvideTypeList.contains("정보 지원") ||
                            !mProvideTypeList.contains("교육 지원") || !mProvideTypeList.contains("인력 지원") || !mProvideTypeList.contains("시설 지원") ||
                            !mProvideTypeList.contains("카드 지원") || !mProvideTypeList.contains("주거 지원") || !mProvideTypeList.contains("일자리 지원") ||
                            !mProvideTypeList.contains("융자 지원") || !mProvideTypeList.contains("면제 지원"))
                    {
                        mProvideTypeList.add(nameList.get(position));
                    }
                }

                // 나이대
                if (nameList.get(position).equals("10대") || nameList.get(position).equals("20대") || nameList.get(position).equals("30대") ||
                        nameList.get(position).equals("40대") || nameList.get(position).equals("50대") || nameList.get(position).equals("60대 이상"))
                {
                    if (!mAgeList.contains("10대") || !mAgeList.contains("20대") || !mAgeList.contains("30대") || !mAgeList.contains("40대") ||
                            !mAgeList.contains("50대") || !mAgeList.contains("60대 이상"))
                    {
                        mAgeList.add(nameList.get(position));
                    }
                }
            }
            if (!getChecked)
            {
                mCategoryList.remove(nameList.get(position));
                mLocalList.remove(nameList.get(position));
                mProvideTypeList.remove(nameList.get(position));
                mAgeList.remove(nameList.get(position));
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
