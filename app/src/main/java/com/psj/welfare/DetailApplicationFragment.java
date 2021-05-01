package com.psj.welfare;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DetailApplicationFragment extends Fragment {

    ImageView apply_step_imageview,apply_paper_imageview,number_imageview,period_imageview; //타이틀 앞에 붙어있는 이미지
    TextView apply_step_text,apply_paper_text,number_text,period_text; //신청 방법 타이틀,신청 서류 타이틀,문의처 타이틀,신청 기간 타이틀
    TextView benefit_apply_step,benefit_apply_paper,benefit_center,benefit_number,benefit_apply_start,benefit_apply_end; //신청 방법,신청 서류,문의처 센터,문의처 번호,신청 기간 시작,신청 기간 끝
    Button BtnGoApply,number_call; //신청하러 가기 버튼, 전화 하기 버튼
    ConstraintLayout method_constraint,apply_paper_constraint,number_constraint,period_constraint; //방법 레이아웃, 신청 서류 레이아웃, 문의처 레이아웃, 기간 레이아웃

    String message,TotalCount,isBookmark,ReviewState; //액티비티에서 받아온 파싱 전 데이터
    String apply_step,apply_paper,center,center_number,apply_start,apply_end,apply_url; //welf_data데이터를 파싱하고 담을 데이터
    public DetailApplicationFragment() { }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_application, container, false);

        //타이틀 앞에 붙어있는 이미지
        apply_step_imageview = view.findViewById(R.id.apply_step_imageview);
        apply_paper_imageview = view.findViewById(R.id.apply_paper_imageview);
        number_imageview = view.findViewById(R.id.number_imageview);
        period_imageview = view.findViewById(R.id.period_imageview);

        apply_step_text = view.findViewById(R.id.apply_step_text); //신청 방법 타이틀
        apply_paper_text = view.findViewById(R.id.apply_paper_text); //신청 서류 타이틀
        number_text = view.findViewById(R.id.number_text); //문의처 타이틀
        period_text = view.findViewById(R.id.period_text); //신청 기간 타이틀

        benefit_apply_step = view.findViewById(R.id.benefit_apply_step); //신청 방법
        benefit_apply_paper = view.findViewById(R.id.benefit_apply_paper); //신청 서류
        benefit_center = view.findViewById(R.id.benefit_center); //문의처 센터
        benefit_number = view.findViewById(R.id.benefit_number); //문의처 번호
        benefit_apply_start = view.findViewById(R.id.benefit_apply_start); //신청 기간 시작
        benefit_apply_end = view.findViewById(R.id.benefit_apply_end); //신청 기간 끝

        BtnGoApply = view.findViewById(R.id.BtnGoApply); //신청하러 가기 버튼
        number_call = view.findViewById(R.id.number_call); //전화 하기 버튼

        method_constraint = view.findViewById(R.id.method_constraint); //방법 레이아웃
        apply_paper_constraint = view.findViewById(R.id.apply_paper_constraint); //신청 서류 레이아웃
        number_constraint = view.findViewById(R.id.number_constraint); //문의처 레이아웃
        period_constraint = view.findViewById(R.id.period_constraint); //기간 레이아웃
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //상세페이지 내용 데이터 받아오기
        SetData();

        //문의처로 전화하기
        number_call.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+center_number));
            startActivity(intent);
        });

        //웹사이트 연결
        BtnGoApply.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apply_url));
            startActivity(intent);
        });
    }

    //상세페이지 내용 데이터 받아오기
    private void SetData() {
        //액티비티에서 보내온 데이터 보여주기
        message = getArguments().getString("message"); //혜택 상세 데이터
        TotalCount = getArguments().getString("TotalCount"); //리뷰 갯수
        isBookmark = getArguments().getString("isBookmark"); //북마크 여부
        ReviewState = null;
        if (!TotalCount.equals("0")) { //리뷰 갯수가 0이 아니면
            ReviewState = getArguments().getString("ReviewState"); //리뷰 데이터
        }


        try {
            //welf_data 데이터 파싱하기
            JSONArray jsonArray_message = new JSONArray(message); //혜택 상세 데이터
            JSONObject jsonObject_message = jsonArray_message.getJSONObject(0);

            JSONArray jsonArray_welf_data = jsonObject_message.getJSONArray("welf_data");
            JSONObject jsonObject = jsonArray_welf_data.getJSONObject(0);


            apply_step = jsonObject.getString("apply_step"); //신청 방법
            apply_paper = jsonObject.getString("apply_paper"); //신청 서류
            center = jsonObject.getString("center"); //문의처
            center_number = jsonObject.getString("center_number"); //문의처 연락처
            apply_start = jsonObject.getString("apply_start"); //신청 기간 시작
            apply_end = jsonObject.getString("apply_end"); //신청 기간 마감
            apply_url = jsonObject.getString("apply_url"); //신청 링크


            //신청 방법 split해서 "#" 정리
            String apply_steps = "";
            String[] apply_step_split = apply_step.split("-");
            for (int i = 1; i < apply_step_split.length; i++){
                String apply_step = apply_step_split[i].trim();
                if (i != apply_step_split.length-1) {
                    apply_steps += apply_step + "\n\n";
                } else {
                    apply_steps += apply_step;
                }
            }
            benefit_apply_step.setText(apply_steps); //신청 방법

            //신청 서류 split해서 "#" 정리
            String apply_papers = "";
            String[] apply_paper_split = apply_paper.split("-");
            for (int i = 1; i < apply_paper_split.length; i++){
                String apply_paper = apply_paper_split[i].trim();
                if (i != apply_paper_split.length-1) {
                    apply_papers += apply_paper + "\n\n";
                } else {
                    apply_papers += apply_paper;
                }
            }
            benefit_apply_paper.setText(apply_papers); //신청 서류


            benefit_center.setText(center); //문의처
            //연락처가 있다면 보여주고 없다면 안보여주기
            if (center_number.isEmpty() || center_number.equals("null") || center_number.equals("")) {
                benefit_number.setVisibility(View.GONE); //문의처 연락처 안보여주기
                number_call.setVisibility(View.GONE); //전화하기 버튼 안보여주기
            } else {
                benefit_number.setText(center_number); //문의처 연락처 데이터 입력
            }

            benefit_apply_start.setText("시작일 : " + apply_start); //신청 기간 시작
            benefit_apply_end.setText("종료일 : " + apply_end); //신청 기간 마감

            //신청 사이트가 없다면 신청 하러 가기 버튼 안보이게
            if (apply_url.equals("- 없음")){
                BtnGoApply.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize(){

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        apply_step_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //신청 방법 타이틀
        apply_paper_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //신청 서류 타이틀
        number_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //문의처 타이틀
        period_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //신청 기간 타이틀

        benefit_apply_step.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //신청 방법
        benefit_apply_paper.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //신청 서류
        benefit_center.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //문의처 센터
        benefit_number.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //문의처 번호
        benefit_apply_start.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //신청 기간 시작
        benefit_apply_end.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //신청 기간 끝

        BtnGoApply.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/15); //신청하러 가기 버튼 텍스트 크기
        BtnGoApply.getLayoutParams().height = size.y/14; //신청하러 가기 버튼 크기 변경
        number_call.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/26); //전화 하기 텍스트 크기
        number_call.getLayoutParams().height = size.y/22; //전화 하기 크기 변경

        method_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용
        apply_paper_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용
        number_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용
        period_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용

        benefit_apply_step.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용
        benefit_apply_paper.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용
        benefit_center.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용
        benefit_number.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용
        benefit_apply_start.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용
        benefit_apply_end.setPadding(0,size.y/130,0,0); //타이틀 패딩값 적용



        ViewGroup.LayoutParams params = apply_step_imageview.getLayoutParams();
        params.width = size.x/32;
        params.height = size.y/56;
        apply_step_imageview.setLayoutParams(params);
        ViewGroup.LayoutParams params2 = apply_paper_imageview.getLayoutParams();
        params2.width = size.x/32;
        params2.height = size.y/56;
        apply_paper_imageview.setLayoutParams(params2);
        ViewGroup.LayoutParams params3 = number_imageview.getLayoutParams();
        params3.width = size.x/32;
        params3.height = size.y/56;
        number_imageview.setLayoutParams(params);
        ViewGroup.LayoutParams params4 = period_imageview.getLayoutParams();
        params4.width = size.x/32;
        params4.height = size.y/56;
        period_imageview.setLayoutParams(params4);
    }
}
