package com.psj.welfare;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.activity.LoginActivity;

import org.eazegraph.lib.models.BarModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailReviewFragment extends Fragment {

    CardView star_cardview, review_cardview; //별점 카드뷰 ,리뷰 카드뷰
    TextView review_average_textview, review_total_textview; //별점 평균, 별점 갯수
    ImageView level_imageview, satisfaction_imageview; //타이틀 앞에 붙어있는 이미지
    TextView level_text, satisfaction_text; //신청 난이도 타이틀, 혜택 만족도 타이틀
    TextView easy_text, difficulty_text, useful_text, unuseful_text; //쉬워요,어려워요,도움 돼요,도움 안돼요
    TextView easy_percent, difficulty_percent, useful_percent, unuseful_percent; //쉬워요 퍼센트,어려워요 퍼센트,도움 돼요 퍼센트,도움 안돼요 퍼센트
    TextView review_text, nothing_review, look_allreview; //사용자 리뷰 타이틀, 리뷰 없음, 리뷰 모두 보기
    Button btn_review_write; //리뷰 작성 버튼
    ConstraintLayout review_layout, look_allreview_layout, nothing_review_layout, review_constraint; //리뷰 타이틀 레이아웃, 리뷰 모두보기 레이아웃, 리뷰 없음 레이아웃, 리뷰 전체를 감싸는 레이아웃

    com.hedgehog.ratingbar.RatingBar star_average; //별점 평균
    ProgressBar level_progressbar, satisfaction_progressbar; //난이도 평균 프로그래스바, 만족도 평균 프로그래스바
    org.eazegraph.lib.charts.BarChart review_chart; //평점 차트

    //리사이클러뷰 사용하기 위한 변수 선언
    private RecyclerView review_recycler; //리사이클러뷰 선언
    private RecyclerView.Adapter DetailReviewAdapter; //아답터 연결
    private RecyclerView.LayoutManager layoutManager; //레이아웃 매니저
    private ArrayList<DetailReviewData> DetailReviewList; //리뷰 2개 보여주기 데이터

    boolean being_logout; //로그인 했는지 여부

    String message, TotalCount, isBookmark, ReviewState; //액티비티에서 받아온 파싱 전 데이터
    String welf_id, welf_name; //혜택 id값, 혜택명
    String  nickName, content, difficulty_level, satisfaction, create_date; //welf_data 데이터 파싱한 데이터
    String star, one_point, two_point, three_point, four_point, five_point, easyPercent, helpPercent; //ReviewState 데이터 파싱한 데이터
    int review_id,login_id,star_count;

    public DetailReviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_review, container, false);

        star_cardview = view.findViewById(R.id.star_cardview); //별점 카드뷰
        review_cardview = view.findViewById(R.id.review_cardview); //리뷰 카드뷰

        review_average_textview = view.findViewById(R.id.review_average_textview); //별점 평균
        review_total_textview = view.findViewById(R.id.review_total_textview); //별점 갯수
        level_imageview = view.findViewById(R.id.level_imageview); //별점 갯수
        satisfaction_imageview = view.findViewById(R.id.satisfaction_imageview); //별점 갯수
        star_average = view.findViewById(R.id.star_average); //별점 평균

        level_text = view.findViewById(R.id.level_text); //신청 난이도 타이틀
        satisfaction_text = view.findViewById(R.id.satisfaction_text); //혜택 만족도 타이틀

        level_progressbar = view.findViewById(R.id.level_progressbar); //난이도 평균 프로그래스바
        satisfaction_progressbar = view.findViewById(R.id.satisfaction_progressbar); //만족도 평균 프로그래스바
        review_chart = view.findViewById(R.id.review_chart); ////평점 차트

        easy_text = view.findViewById(R.id.easy_text); //쉬워요
        difficulty_text = view.findViewById(R.id.difficulty_text); //어려워요
        useful_text = view.findViewById(R.id.useful_text); //도움 돼요
        unuseful_text = view.findViewById(R.id.unuseful_text); //도움 안돼요

        easy_percent = view.findViewById(R.id.easy_percent); //쉬워요 퍼센트
        difficulty_percent = view.findViewById(R.id.difficulty_percent); //어려워요 퍼센트
        useful_percent = view.findViewById(R.id.useful_percent); //도움 돼요 퍼센트
        unuseful_percent = view.findViewById(R.id.unuseful_percent); //도움 안돼요 퍼센트

        review_text = view.findViewById(R.id.review_text); //사용자 리뷰 타이틀
        nothing_review = view.findViewById(R.id.nothing_review); //리뷰 없음
        look_allreview = view.findViewById(R.id.look_allreview); //리뷰 모두 보기
        btn_review_write = view.findViewById(R.id.btn_review_write); //리뷰 작성 버튼

        review_layout = view.findViewById(R.id.review_layout); //리뷰 타이틀 레이아웃
        look_allreview_layout = view.findViewById(R.id.look_allreview_layout); //리뷰 모두보기 레이아웃
        nothing_review_layout = view.findViewById(R.id.nothing_review_layout); //리뷰 없음 레이아웃
        review_constraint = view.findViewById(R.id.review_constraint); //리뷰 전체를 감싸는 레이아웃


        //리사이클러뷰 사용하기 위한 변수 연결
        review_recycler = view.findViewById(R.id.review_recycler); //리사이클러뷰 연결
        review_recycler.setHasFixedSize(true); //setHasFixedSize(true)는 리사이클러뷰 안 아이템들의 크기를 가변적으로 하지 않고 고정으로 한다는 것
        layoutManager = new LinearLayoutManager(getActivity()); //리사이클러뷰의 레이아웃을 Linear 방식으로 한다는 것
        review_recycler.setLayoutManager(layoutManager); //리사이클러뷰의 레이아웃을 정함
        //recyclerView.setLayoutManager(new LinearLayoutManager(this)); 위 두줄을 이렇게 한줄로 쓸 수도 있다
        DetailReviewList = new ArrayList<>(); // 강의 데이터를 담을 어레이 리스트 (어댑터 쪽으로)

        DetailReviewAdapter = new DetailReviewAdapter(DetailReviewList, getActivity()); //아답터 연결(어레이리스트와 뷰 연결)
        review_recycler.setAdapter(DetailReviewAdapter); //리사이클러뷰에 아답터 연결

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //상세페이지 리뷰 데이터 받아오기
        SetData();

        //로그인 했는지 여부
        being_loging();

        //리뷰 작성 하기
        btn_review_write.setOnClickListener(v -> {
            if (!being_logout) { //로그인 했다면
                Intent intent = new Intent(getActivity(), DetailReviewWrite.class);
                intent.putExtra("welf_id", welf_id); //혜택 id값 보내기
                intent.putExtra("welf_name", welf_name); //혜택명 보내기
                intent.putExtra("being_id", true); //id값을 보내줬는지
                startActivity(intent);
            } else { //로그인 안했다면
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("리뷰를 작성하시려면\n먼저 로그인이 필요해요.\n로그인 하시겠어요?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }

        });

        //리뷰 모두 보기 버튼 클릭
        look_allreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailReviewAllLook.class);
                intent.putExtra("welf_id", welf_id); //혜택 id값 보내기
                intent.putExtra("welf_name", welf_name); //혜택명 보내기
                intent.putExtra("being_id", true); //id값을 보내줬는지
                startActivity(intent);
            }
        });

    }

    //상세페이지 내용 데이터 받아오기
    private void SetData() {
        //액티비티에서 보내온 데이터 보여주기
        message = getArguments().getString("message"); //혜택 상세 데이터
        TotalCount = getArguments().getString("TotalCount"); //리뷰 갯수
        isBookmark = getArguments().getString("isBookmark"); //북마크 여부
        welf_id = getArguments().getString("welf_id"); //혜택id
        welf_name = getArguments().getString("welf_name"); //혜택명

        Log.e("welf_id", welf_id);
        ReviewState = null;
        if (!TotalCount.equals("0")) { //리뷰 갯수가 0이 아니면
            ReviewState = getArguments().getString("ReviewState"); //리뷰 데이터
        }

        //리뷰 갯수 입력
        review_text.setText("사용자 리뷰 " + TotalCount + "개");
        //리뷰 총 갯수
        review_total_textview.setText("(" + TotalCount + ")");

        Log.e("TotalCount", TotalCount);
        int IntTotalCount = Integer.parseInt(TotalCount);
        if (IntTotalCount == 0) { //리뷰가 없다면
            star_cardview.setVisibility(View.GONE);
            review_recycler.setVisibility(View.GONE);
            look_allreview_layout.setVisibility(View.GONE); //모든 리뷰 보여주기
            nothing_review_layout.setVisibility(View.VISIBLE);
        } else if (IntTotalCount < 3 && IntTotalCount > 0) { //리뷰가 1개 또는 2개 일 때
            star_cardview.setVisibility(View.VISIBLE);
            review_recycler.setVisibility(View.VISIBLE);
            look_allreview_layout.setVisibility(View.GONE); //모든 리뷰 보여주기
            nothing_review_layout.setVisibility(View.GONE);
            //받아온 데이터 파싱 하기
            parsing();
        } else {
            star_cardview.setVisibility(View.VISIBLE);
            review_recycler.setVisibility(View.VISIBLE);
            look_allreview_layout.setVisibility(View.VISIBLE); //모든 리뷰 보여주기
            nothing_review_layout.setVisibility(View.GONE);
            //받아온 데이터 파싱 하기
            parsing();
        }


    }

    //받아온 데이터 파싱하기
    private void parsing() {
        try {
            //welf_data 데이터 파싱하기
            JSONArray jsonArray_message = new JSONArray(message); //혜택 상세 데이터
            JSONObject jsonObject_message = jsonArray_message.getJSONObject(0);

            Log.e("message", message.toString());
            JSONArray jsonArray_welf_data = jsonObject_message.getJSONArray("review_data");
            for (int i = 0; i < jsonArray_welf_data.length(); i++) {
                JSONObject jsonObject = jsonArray_welf_data.getJSONObject(i);

                review_id = jsonObject.getInt("review_id"); //리뷰 id
                login_id = jsonObject.getInt("login_id"); //유저 id
                nickName = jsonObject.getString("nickName"); //닉네임
                content = jsonObject.getString("content"); //내용
                star_count = jsonObject.getInt("star_count"); //별점
                difficulty_level = jsonObject.getString("difficulty_level"); //난이도 평가
                satisfaction = jsonObject.getString("satisfaction"); //만족도 평가
                create_date = jsonObject.getString("create_date"); //리뷰 작성 날짜

                DetailReviewData reviewData = new DetailReviewData();
                reviewData.setReview_id(review_id);
                reviewData.setLogin_id(login_id);
                reviewData.setNickName(nickName);
                reviewData.setContent(content);
                float star = (float)star_count;
                reviewData.setStar_count(star);
                reviewData.setDifficulty_level(difficulty_level);
                reviewData.setSatisfaction(satisfaction);
                reviewData.setCreate_date(create_date);

                DetailReviewList.add(reviewData);
                DetailReviewAdapter.notifyDataSetChanged();
            }

            Log.e("ReviewState", ReviewState.toString());
            //ReviewState 데이터 파싱하기
            JSONArray jsonArray_ReviewState = new JSONArray(ReviewState);
            JSONObject jsonObject_state = jsonArray_ReviewState.getJSONObject(0);

            Log.e("jsonObject_state", jsonObject_state.toString());


            star = jsonObject_state.getString("star"); //별점
            one_point = jsonObject_state.getString("one_point"); //1점 평가 유저수
            two_point = jsonObject_state.getString("twe_point"); //2점 평가 유저수
            three_point = jsonObject_state.getString("three_point"); //3점 평가 유저수
            four_point = jsonObject_state.getString("four_point"); //4점 평가 유저수
            five_point = jsonObject_state.getString("five_point"); //5점 평가 유저수
            easyPercent = jsonObject_state.getString("easyPercent"); //난이도 평가
            helpPercent = jsonObject_state.getString("helpPercent"); //만족도 평가

            review_average_textview.setText(star); //별점 평균
            star_average.setStar(Float.parseFloat(star)); //별점 평균 그림

            int easyPercent_int = Integer.parseInt(easyPercent); //쉬워요 평균
            int helpPercent_int = Integer.parseInt(helpPercent); //도움돼요 평균
            String difficultPercent = String.valueOf(100 - easyPercent_int); //어려워요 평균
            String unhelpPercent = String.valueOf(100 - helpPercent_int); //어려워요 평균


            Log.e("easyPercent_int", easyPercent);
            Log.e("helpPercent_int", helpPercent);

            level_progressbar.setProgress(easyPercent_int); //난이도 평균
            satisfaction_progressbar.setProgress(helpPercent_int); //만족도 평균

            easy_percent.setText("(" + easyPercent + "%)"); //쉬워요 퍼센터
            difficulty_percent.setText("(" + difficultPercent + "%)"); //어려워요 퍼센트
            useful_percent.setText("(" + helpPercent + "%)"); //도움 돼요 퍼센터
            unuseful_percent.setText("(" + unhelpPercent + "%)"); //도움 안돼요 퍼센트


            //평점 차트 보여주기
            review_chart.clearChart();
            review_chart.addBar(new BarModel("5점", Float.parseFloat(five_point), 0xFFFF7088));
            review_chart.addBar(new BarModel("4점", Float.parseFloat(four_point), 0xFFFF7088));
            review_chart.addBar(new BarModel("3점", Float.parseFloat(three_point), 0xFFFF7088));
            review_chart.addBar(new BarModel("2점", Float.parseFloat(two_point), 0xFFFF7088));
            review_chart.addBar(new BarModel("1점", Float.parseFloat(one_point), 0xFFFF7088));
            review_chart.startAnimation();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //로그인 했는지 여부 확인
    private void being_loging() {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getActivity().getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", false); //로그인 했는지 여부 확인하기

//        if (!being_logout) { //로그인 했다면
//            btn_review_write.setVisibility(View.VISIBLE);
//        } else { //로그인 안했다면
//            btn_review_write.setVisibility(View.GONE);
//        }
    }


    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    private void SetSize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());
        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함

        review_average_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 15); //별점 평균
        review_total_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 29); //별점 갯수

        level_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 23); //신청 난이도 타이틀
        satisfaction_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 23); //혜택 만족도 타이틀

        easy_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //쉬워요
        difficulty_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //어려워요
        useful_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //도움 돼요
        unuseful_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //도움 안돼요

        easy_percent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 30); //쉬워요 퍼센트
        difficulty_percent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 30); //어려워요 퍼센트
        useful_percent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 30); //도움 돼요 퍼센트
        unuseful_percent.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 30); //도움 안돼요 퍼센트

        review_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 24); //사용자 리뷰 타이틀
        nothing_review.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //리뷰 없음
        look_allreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //리뷰 모두 보기

        btn_review_write.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 26); //리뷰 작성 버튼 텍스트 크기
        btn_review_write.getLayoutParams().height = size.y / 20; //리뷰 작성 버튼 크기 변경


        ViewGroup.LayoutParams params = star_cardview.getLayoutParams();
        params.width = size.x / 7 * 6;
        params.height = size.y / 7 * 3;
        star_cardview.setLayoutParams(params); //별점 카드뷰

        ViewGroup.LayoutParams params2 = review_cardview.getLayoutParams();
        params2.width = size.x / 7 * 6;
        review_cardview.setLayoutParams(params2); //리뷰 카드뷰

        ViewGroup.LayoutParams params3 = review_layout.getLayoutParams();
        params3.height = size.y / 12;
        review_layout.setLayoutParams(params3); //리뷰 타이틀 레이아웃

        ViewGroup.LayoutParams params4 = nothing_review_layout.getLayoutParams();
        params4.height = size.y / 6;
        nothing_review_layout.setLayoutParams(params4); //리뷰 없음 레이아웃

        ViewGroup.LayoutParams params5 = look_allreview_layout.getLayoutParams();
        params5.height = size.y / 12;
        look_allreview_layout.setLayoutParams(params5); //리뷰 모두보기 레이아웃


        ViewGroup.LayoutParams params_imageview = level_imageview.getLayoutParams();
        params_imageview.width = size.x / 36;
        params_imageview.height = size.y / 57;
        level_imageview.setLayoutParams(params_imageview); //타이틀 앞에 붙어있는 이미지
        ViewGroup.LayoutParams params_imageview2 = satisfaction_imageview.getLayoutParams();
        params_imageview2.width = size.x / 36;
        params_imageview2.height = size.y / 57;
        satisfaction_imageview.setLayoutParams(params_imageview2); //타이틀 앞에 붙어있는 이미지

        review_recycler.setPadding(size.x / 30, size.x / 30, size.x / 30, size.x / 30); //레이아웃 패딩값 적용
    }
}