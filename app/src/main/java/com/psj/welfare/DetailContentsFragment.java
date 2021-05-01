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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailContentsFragment extends Fragment {

    ImageView title_imageview,region_imageview,SupportType_imageview,contents_imageview,target_imageview; //타이틀 앞에 붙어있는 이미지
    TextView title_text,region_text,SupportType_text,contents_text,target_text; //혜탱명 텍스트('혜택명' 이란 텍스트 크기를 주기 위해 선언),지역 텍스트, 지원형태 텍스트, 혜택내용 텍스트, 혜택대상 텍스트
    TextView benefit_title,benefit_region,benefit_SupportType,benefit_contents,benefit_target; //혜탱명,지역,지원형태,혜택내용,혜택대상
    Button BtnGoWebsite,Btntarget_tag; //홈페이지 가기 버튼, 대상 상세보기
    ConstraintLayout contents_constraint,target_constraint; //내용 컨스트레인트 레이아웃, 혜택 대상 컨스트레인트 레이아웃
    LinearLayout title_layout;

    String message,TotalCount,isBookmark,ReviewState; //액티비티에서 받아온 파싱 전 데이터
    String welf_name,welf_local,welf_category,target,target_tag,welf_content,welf_url; //welf_data데이터를 파싱하고 담을 데이터
    public DetailContentsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_contents, container, false);

        //타이틀 앞에 붙어있는 이미지
        title_imageview = view.findViewById(R.id.title_imageview);
        region_imageview = view.findViewById(R.id.region_imageview);
        SupportType_imageview = view.findViewById(R.id.SupportType_imageview);
        contents_imageview = view.findViewById(R.id.contents_imageview);
        target_imageview = view.findViewById(R.id.target_imageview);

        title_text = view.findViewById(R.id.title_text); //혜택명 텍스트
        region_text = view.findViewById(R.id.region_text); //지역 텍스트
        SupportType_text = view.findViewById(R.id.SupportType_text); //지원형태 텍스트
        contents_text = view.findViewById(R.id.contents_text); //혜택내용 텍스트
        target_text = view.findViewById(R.id.target_text); //혜택대상 텍스트

        benefit_title = view.findViewById(R.id.benefit_title); //혜택명
        benefit_region = view.findViewById(R.id.benefit_region); //지역
        benefit_SupportType = view.findViewById(R.id.benefit_SupportType); //지원형태
        benefit_contents = view.findViewById(R.id.benefit_contents); //혜택내용
        benefit_target = view.findViewById(R.id.benefit_target); //혜택대상

        BtnGoWebsite = view.findViewById(R.id.BtnGoWebsite); //홈페이지 가기 버튼
        Btntarget_tag = view.findViewById(R.id.Btntarget_tag); //대상 상세보기 버튼

        contents_constraint = view.findViewById(R.id.contents_constraint); //혜택 내용 레이아웃
        target_constraint = view.findViewById(R.id.target_constraint); //혜택 대상 레이아웃

        title_layout = view.findViewById(R.id.title_layout); //혜택 대상 레이아웃
        return view;
    }

    //onCreateView에서 return해준 view를 받아서 사용
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //상세페이지 내용 데이터 받아오기
        SetData();

        //대상 상세 조건이 없다면 버튼 안보여주기
        if (target_tag.isEmpty()){
            Btntarget_tag.setVisibility(View.GONE);
        }

        //대상 상세보기 화면 누르면 다이얼로그 띄워줌
        Btntarget_tag.setOnClickListener(v -> {
            //혜택대상 상세보기 다이얼로그
            OpenTargetDialog();
        });

        //웹사이트 연결
        BtnGoWebsite.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(welf_url));
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

            welf_name = jsonObject.getString("welf_name");
            welf_local = jsonObject.getString("welf_local");
            welf_category = jsonObject.getString("welf_category");
            target = jsonObject.getString("target");
            target_tag = jsonObject.getString("target_tag");
            welf_content = jsonObject.getString("welf_content");
            welf_url = jsonObject.getString("welf_url");

            benefit_title.setText(welf_name); //혜택명

            //지역 split해서 "#" 정리
            String welf_locals = "";
            String[] welf_local_split = welf_local.split("-");
            for (int i = 0; i < welf_local_split.length; i++){
                String welf_local = welf_local_split[i].trim();
                if (i != welf_local_split.length-1) {
                    welf_locals += welf_local + "  ";
                } else {
                    welf_locals += welf_local;
                }
            }
            benefit_region.setText(welf_locals); //지역


            //지원형태 split해서 "#" 정리
            String welf_categorys = "";
            String[] welf_category_split = welf_category.split("-");
            for (int i = 0; i < welf_category_split.length; i++){
                String welf_category = welf_category_split[i].trim();
                if (i != welf_category_split.length-1) {
                    welf_categorys += "#" + welf_category + " ";
                } else {
                    welf_categorys += "#" + welf_category;
                }
            }
            benefit_SupportType.setText(welf_categorys); //지원형태


            //대상 split해서 "#" 정리
            String targets = "";
            String[] target_split = target.split("-");
            for (int i = 0; i < target_split.length; i++){
                String target = target_split[i].trim();
                if (i != target_split.length-1) {
                    targets += target + "  ";
                } else {
                    targets += target;
                }
            }
            benefit_target.setText(targets); //대상


            //내용 split해서 "-" 정리
            String contents = "";
            String[] content_split = welf_content.split("-");
            for (int i = 1; i < content_split.length; i++){
                String coontent = content_split[i].trim();
                if (i != content_split.length-1) {
                    contents += coontent + "\n\n";
                } else {
                    contents += coontent;
                }
            }
            benefit_contents.setText(contents); //내용

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //혜택대상 상세보기 다이얼로그
    private void OpenTargetDialog() {
        AlertDialog.Builder TargetDialog = new AlertDialog.Builder(getActivity());
        View dialogview = getLayoutInflater().inflate(R.layout.custom_benefitdetail_targettag_dialog,null); //다이얼로그의 xml뷰 담기

        ConstraintLayout targettag_layout = dialogview.findViewById(R.id.targettag_layout); //다이얼로그 레이아웃
        ImageView targettag_imageview = dialogview.findViewById(R.id.targettag_imageview); //타이틀 옆에 네모 이미지
        TextView targettag_text = dialogview.findViewById(R.id.targettag_text); //타이틀
        TextView delete_dialog_thirdtext = dialogview.findViewById(R.id.delete_dialog_thirdtext); //상세 조건

//        targettag_layout.setBackgroundResource(R.drawable.radius_25);
        //이미지 및 텍스트 크기 조절
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        targettag_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/22); //혜택명 텍스트
        delete_dialog_thirdtext.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/26); //혜택명 텍스트


        //상세조건 split해서 "#" 정리
        String target_tags = "";
        String[] target_tag_split = target_tag.split("#");
        for (int i = 1; i < target_tag_split.length; i++){
            String target_tag = target_tag_split[i].trim();
            if (i != target_tag_split.length-1) {
                target_tags += "#" + target_tag + "\n\n";
            } else {
                target_tags += "#" + target_tag;
            }
        }

        delete_dialog_thirdtext.setText(target_tags); //대상 상세조건

        TargetDialog.setView(dialogview); //alertdialog에 view 넣기
        final AlertDialog alertDialog = TargetDialog.create(); //다이얼로그 객체로 만들기
        alertDialog.show(); //다이얼로그 보여주기
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize(){

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        title_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //혜택명 텍스트
        region_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //지역 텍스트
        SupportType_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //지원형태 텍스트
        contents_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //혜택내용 텍스트
        target_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/20); //혜택대상 텍스트

        benefit_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //혜택명
        benefit_region.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //지역
        benefit_SupportType.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //지원형태
        benefit_contents.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //혜택내용
        benefit_target.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/24); //혜택대상

        BtnGoWebsite.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/15); //자세히보러가기 버튼 텍스트 크기
        BtnGoWebsite.getLayoutParams().height = size.y/15; //자세히보러가기 버튼 크기 변경
        Btntarget_tag.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x/26); //target_tag버튼 텍스트 크기
        Btntarget_tag.getLayoutParams().height = size.y/22; //target_tag버튼 크기 변경

        contents_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용
        target_constraint.setPadding(size.x/30,size.x/30,size.x/30,size.x/30); //레이아웃 패딩값 적용

        benefit_title.setPadding(0,size.y/140,0,0); //타이틀 패딩값 적용
        benefit_region.setPadding(0,size.y/140,0,0); //지역 패딩값 적용
        benefit_SupportType.setPadding(0,size.y/140,0,0); //지원형태 패딩값 적용
        benefit_contents.setPadding(0,size.y/75,0,size.x/80); //혜택내용 패딩값 적용
        benefit_target.setPadding(0,size.y/75,0,size.x/80); //혜택대상 패딩값 적용


        ViewGroup.LayoutParams params = title_imageview.getLayoutParams();
        params.width = size.x/32;
        params.height = size.y/56;
        title_imageview.setLayoutParams(params);
        ViewGroup.LayoutParams params2 = region_imageview.getLayoutParams();
        params2.width = size.x/32;
        params2.height = size.y/56;
        region_imageview.setLayoutParams(params2);
        ViewGroup.LayoutParams params3 = SupportType_imageview.getLayoutParams();
        params3.width = size.x/32;
        params3.height = size.y/56;
        SupportType_imageview.setLayoutParams(params3);
        ViewGroup.LayoutParams params4 = contents_imageview.getLayoutParams();
        params4.width = size.x/32;
        params4.height = size.y/56;
        contents_imageview.setLayoutParams(params4);
        ViewGroup.LayoutParams params5 = target_imageview.getLayoutParams();
        params5.width = size.x/32;
        params5.height = size.y/56;
        target_imageview.setLayoutParams(params5);
    }
}