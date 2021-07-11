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

public class InnerRecyclerViewAdapter extends RecyclerView.Adapter<InnerRecyclerViewAdapter.ViewHolder>
{
    public final String TAG = InnerRecyclerViewAdapter.class.getSimpleName();

    public ArrayList<String> nameList;  // 모든 값이 필터 구분 없이 들어있는 리스트
    public static ArrayList<String> mLocalList = new ArrayList<>();
    public static ArrayList<String> mAgeList = new ArrayList<>();
    public static ArrayList<String> mProvideTypeList = new ArrayList<>();

    public String category_title; //'지역'or '나이대'or '지원형태' 를 담는 변수(카테고리 타이틀을 담는 변수)
    public ArrayList<Boolean> filter_local; //체크박스에 체크한 지역
    public ArrayList<Boolean> filter_age; //체크박스에 체크한 나이
    public ArrayList<Boolean> filter_provideType; //체크박스에 체크한 지역


    // 액티비티에서 어댑터 안의 리스트에 들어간 아이템을 가져올 때 쓰는 메서드
    public static String getAllValues()
    {
        StringBuilder localBuilder = new StringBuilder();
        StringBuilder provideTypeBuilder = new StringBuilder();
        StringBuilder ageBuilder = new StringBuilder();

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
        String lastLocal;
        String lastAge;
        String lastProvideType;

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

        //한번 호출 하면 값 초기화 해줘야 한다
        mLocalList.clear();
        mAgeList.clear();
        mProvideTypeList.clear();

        return lastLocal + "zz" + lastAge + "zz" + lastProvideType;
    }



    public static void resetfilter(){
        mLocalList.clear();
        mAgeList.clear();
        mProvideTypeList.clear();
    }


    public InnerRecyclerViewAdapter(ArrayList<String> nameList, String category_title, ArrayList<Boolean> filter_local, ArrayList<Boolean> filter_age, ArrayList<Boolean> filter_provideType)
    {
        this.nameList = nameList;
        this.category_title = category_title;

        this.filter_local = filter_local;
        this.filter_age = filter_age;
        this.filter_provideType = filter_provideType;
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

        switch(category_title){
            case  "지역":
                holder.checkBox.setChecked(filter_local.get(position));
//                if(filter_local.get(position)){
//                    mCategoryList.add(nameList.get(position));
////                    Log.e(TAG,position + " : " + nameList.get(position));
//                }
                if(holder.checkBox.isChecked()){
                    mLocalList.add(nameList.get(position));
                }
                break;

            case  "나이대":
                holder.checkBox.setChecked(filter_age.get(position));
//                if(filter_age.get(position)){
//                    mAgeList.add(nameList.get(position));
//                }
                if(holder.checkBox.isChecked()){
                    mAgeList.add(nameList.get(position));
                }
                break;

            case  "지원 형태":
                holder.checkBox.setChecked(filter_provideType.get(position));
//                if(filter_provideType.get(position)){
//                    mProvideTypeList.add(nameList.get(position));
//                }
                if(holder.checkBox.isChecked()){
                    mProvideTypeList.add(nameList.get(position));
                }
                break;
        }

        if(holder.checkBox.isChecked()){

        }





















        holder.checkBox.setText(nameList.get(position));
        holder.checkBox.setOnClickListener(v -> {
            // 체크박스 체크 값을 가져와서
            boolean getChecked = holder.checkBox.isChecked();
            // 체크된 것만 리스트에 담는다 -> 체크한 후 체크해제하면 리스트에서 지워야 한다
            if (getChecked)
            {
//                Log.e(TAG, "선택된 아이템의 pos : " + nameList.get(position));
                // 카테고리
//                if (nameList.get(position).equals("교육") || nameList.get(position).equals("건강") || nameList.get(position).equals("근로") ||
//                        nameList.get(position).equals("금융") || nameList.get(position).equals("기타") || nameList.get(position).equals("문화") ||
//                        nameList.get(position).equals("사업") || nameList.get(position).equals("주거") || nameList.get(position).equals("환경"))
//                {
//                    if (!mCategoryList.contains("교육") || !mCategoryList.contains("건강") || !mCategoryList.contains("근로") || !mCategoryList.contains("금융") ||
//                            !mCategoryList.contains("기타") || !mCategoryList.contains("문화") || !mCategoryList.contains("사업") || !mCategoryList.contains("주거") ||
//                            !mCategoryList.contains("환경"))
//                    {
//                        mCategoryList.add(nameList.get(position));
//                    }
//                }

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
            }
            if (!getChecked)
            {
//                mCategoryList.remove(nameList.get(position));
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
