package com.psj.welfare.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.data.MainThreeDataItem;

import java.util.List;

public class MoreBottomAdapter extends RecyclerView.Adapter<MoreBottomAdapter.MoreBottomViewHolder>
{
    private Context context;
    private List<MainThreeDataItem> list;
    private ItemClickListener itemClickListener;

    public void setOnBottomClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public MoreBottomAdapter(Context context, List<MainThreeDataItem> list, ItemClickListener itemClickListener)
    {
        this.context = context;
        this.list = list;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MoreBottomAdapter.MoreBottomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.test_down_item, parent, false);

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (parent.getWidth() * 0.85);
        params.height = (int) (parent.getHeight() * 0.25);
        view.setLayoutParams(params);

        return new MoreBottomViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MoreBottomAdapter.MoreBottomViewHolder holder, int position)
    {
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
//        BenefitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
//
//        score_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //별점 타이틀
//        score_textview.setPadding(0, size.y / 100, 0, size.y / 130); //타이틀 패딩값 적용
//
//        please_tab_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //탭해서 별점주기
//
//        level_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //과정 평가
//        level_textview.setPadding(0, size.y / 75, 0, size.y / 130); //과정 평가 패딩값 적용
//        satisfaction_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //만족도 평가
//        satisfaction_textview.setPadding(0, size.y / 75, 0, 0); //만족도 패딩값 적용
//        your_opinion_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //의견 남겨주세요
//        your_opinion_textview.setPadding(0, size.y / 75, 0, size.y / 100); //의견란 패딩값 적용

        if (!list.isEmpty())
        {
            /* 어댑터에서 뷰 크기, 글자 크기 조절 */
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
            Display display = wm.getDefaultDisplay();  // 어댑터에서 사용하려면 이렇게 바꿔야 한다
            Point size = new Point();
            display.getRealSize(size); // or getSize(size)

            holder.category_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
            holder.category_subject.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 28);

            ViewGroup.LayoutParams card_params = holder.category_result_layout.getLayoutParams();
            card_params.height = size.y / 8;
            holder.category_result_layout.setLayoutParams(card_params);

            MainThreeDataItem item = list.get(position);
            holder.category_name.setText(item.getWelf_name());
            if (item.getWelf_tag().contains("-"))
            {
                String before = item.getWelf_tag().replace(" ", "");
                String str = "#" + before;
                String s = str.replace("-", " #");
                String s1 = s.replace(" -", " #");
                String s2 = s1.replace("- ", " #");
                String s3 = s2.replace(" - ", " #");
                holder.category_subject.setText(s3);
            }
            holder.category_count.setText("View " + item.getWelf_count());
        }
    }

//    private void SetSize()
//    {
//        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
//        BenefitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
//
//        score_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //별점 타이틀
//        score_textview.setPadding(0, size.y / 100, 0, size.y / 130); //타이틀 패딩값 적용
//
//        please_tab_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //탭해서 별점주기
//
//        level_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //과정 평가
//        level_textview.setPadding(0, size.y / 75, 0, size.y / 130); //과정 평가 패딩값 적용
//        satisfaction_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //만족도 평가
//        satisfaction_textview.setPadding(0, size.y / 75, 0, 0); //만족도 패딩값 적용
//        your_opinion_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 21); //의견 남겨주세요
//        your_opinion_textview.setPadding(0, size.y / 75, 0, size.y / 100); //의견란 패딩값 적용
//
////        ViewGroup.LayoutParams params_star = review_star.getLayoutParams();
////        params_star.width = size.x/7*6; params_star.height = size.y/2;
////        review_star.setLayoutParams(params_star);
//
//        ViewGroup.LayoutParams params_radiogroup = difficulty_radiogroup.getLayoutParams(); //난이도 라디오 버튼 그룹
//        params_radiogroup.height = size.y / 12;
//        difficulty_radiogroup.setLayoutParams(params_radiogroup);
//
//        ViewGroup.LayoutParams params_radiogroup2 = satisfaction_radiogroup.getLayoutParams(); //만족도 라디오 버튼 그룹
//        params_radiogroup2.height = size.y / 12;
//        satisfaction_radiogroup.setLayoutParams(params_radiogroup2);
//
//        easy_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //쉬워요 버튼 텍스트 크기
//        easy_radiobutton.getLayoutParams().height = size.y / 18; //쉬워요 버튼 크기 변경
//        easy_radiobutton.getLayoutParams().width = size.x / 6; //쉬워요 버튼 크기 변경
//
//        hard_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //어려워요 버튼 텍스트 크기
//        hard_radiobutton.getLayoutParams().height = size.y / 18; //어려워요 버튼 크기 변경
//        hard_radiobutton.getLayoutParams().width = size.x / 6; //어려워요 버튼 크기 변경
//
//        good_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 돼요 버튼 텍스트 크기
//        good_radiobutton.getLayoutParams().height = size.y / 18; //도움 돼요 버튼 크기 변경
//        good_radiobutton.getLayoutParams().width = size.x / 6; //도움 돼요 버튼 크기 변경
//
//        bad_radiobutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //도움 안돼요 버튼 텍스트 크기
//        bad_radiobutton.getLayoutParams().height = size.y / 18; //도움 안돼요 버튼 크기 변경
//        bad_radiobutton.getLayoutParams().width = size.x / 6; //도움 안돼요 버튼 크기 변경
//
//        review_content_edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24);
//        review_content_edit.getLayoutParams().height = size.x / 3 * 2; //의견란
//        review_content_edit.getLayoutParams().width = size.x / 6 * 5; //의견란
//        review_content_edit.setPadding(size.y / 70, size.y / 70, size.y / 70, size.y / 70); //의견란 패딩값 적용
//
//        text_length_layout.getLayoutParams().height = size.y / 24; //의견란 글자수 레이아웃 크기 변경
//        text_length_layout.getLayoutParams().width = size.x / 6 * 5; //의견란 글자수 레이아웃 크기 변경
//        text_length_layout.setPadding(0, size.y / 150, 0, size.y / 90); //의견란 글자수 레이아웃 패딩값 적용
//
//        text_length.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 현재 글자수
//        text_length_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //의견란 최대 글자수
//
//        btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 20); //등록 버튼 텍스트 크기
//        btnRegister.getLayoutParams().height = size.y / 16; //등록 버튼 크기 변경
//        btnRegister.getLayoutParams().width = size.x / 6 * 5; //등록 버튼 크기 변경
//        btnRegister.setPadding(0, size.y / 90, 0, size.y / 80); //등록 버튼패딩값 적용
//
//    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class MoreBottomViewHolder extends RecyclerView.ViewHolder
    {
        CardView category_result_layout;
        TextView category_name, category_subject, category_count;
        ItemClickListener itemClickListener;

        public MoreBottomViewHolder(@NonNull View view, ItemClickListener itemClickListener)
        {
            super(view);

            category_result_layout = view.findViewById(R.id.bottom_result_layout);
            category_name = view.findViewById(R.id.bottom_result_name);
            category_subject = view.findViewById(R.id.bottom_result_subject);
            category_count = view.findViewById(R.id.bottom_result_views);

            this.itemClickListener = itemClickListener;
            category_result_layout.setOnClickListener(v ->
            {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemClickListener != null)
                {
                    itemClickListener.onBottomClick(pos);
                }
            });
        }
    }

    public interface ItemClickListener
    {
        void onBottomClick(int pos);
    }

}
