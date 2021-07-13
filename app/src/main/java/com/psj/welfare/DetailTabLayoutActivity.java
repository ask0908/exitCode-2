package com.psj.welfare;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.SocialObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.psj.welfare.activity.LoginActivity;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class DetailTabLayoutActivity extends AppCompatActivity
{

    public final String TAG = this.getClass().getSimpleName();
    String benefit_image_url = "http://3.34.64.143/images/reviews/조제분유.jpg";//공유하기에 보낼 임시 이미지

    TextView BenefitTitle; //혜택명
    TextView detail_contents_button, detail_application_button, detail_review_button; //내용버튼, 신청버튼, 리뷰버튼
    Fragment ContentsFragment, ApplicationFragment, ReviewFragment; //내용,신청,리뷰 프래그먼트
    FragmentManager fragmentManager = getSupportFragmentManager(); //프래그먼트 매니저, 액티비티와 프래그먼트의 중간에서 서로를 이어주는 역할
    FragmentTransaction fragmentTransaction; //프래그먼트 트랜잭션을 이용한 프래그먼트 삽입, 교체, 제거 기능 수행
    ImageButton back_btn, bookmark_btn; //이전 화면으로 가기 버튼, 북마크
    ImageView share_btn;//공유하기

    ProgressBar Detail_progressbar; //혜택 데이터 가져오기 전까지 보여줄 프로그래스 바
    JSONArray message;
    String TotalCount;
    String isBookmark;
    JSONArray ReviewState;

    Bundle detail_bundle = new Bundle(); //액티비티에서 프래그먼트로 보낼 혜택 상세 데이터
    String welf_name; //혜택 명
    String welf_id; //혜택 아이디 값
    boolean being_logout; //로그인 했는지 여부 확인하기
    String SessionId; //세션 값
    String token; //토큰 값
    boolean being_id; //혜택 아이디가 있는지
    boolean review_write; //리뷰 작성 했는지

    //공유하기 버튼 중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능 )
    private static final long MIN_CLICK_INTERVAL = 600;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(DetailTabLayoutActivity.this);
        setContentView(R.layout.activity_detail_tab_layout);

        //자바 변수와 xml 변수 연결
        init();

        //인텐트로 받아온 welf_id값
        being_intent();

        //로그인 했는지 여부 확인
        being_loging();

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //서버로부터 혜택 상세 데이터 가져오기
        LoadBenefitDetail();

        //북마크 하기
        bookmark_btn.setOnClickListener(v -> {

            if (!being_logout) { //로그인 했다면
                SetBookmark(); //북마크 하기
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailTabLayoutActivity.this);
                builder.setMessage("북마크를 하시려면\n먼저 로그인이 필요해요.\n로그인 하시겠어요?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(DetailTabLayoutActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
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

        //이전 화면으로 가기
        back_btn.setOnClickListener(v -> {
            finish(); //현재 액티비티 종료
            //액티비티 스택을 추적하고싶을경우. 이런 경우엔 새액티비티를 시작할때마다 intent에 FLAG_ACTIVITY_REORDER_TO_FRONT 나 FLAG_ACTIVITY_PREVIOUS_IS_TOP 같은 플래그를 줄 수 있습니다.
        });

        // 공유하기
        share_btn.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                ShareBenefit();
            }
        });

        //내용 버튼 눌렀을 때
        detail_contents_button.setOnClickListener(v -> {
            //내용 버튼 글자, 테두리색 핑크
            detail_contents_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //내용 핑크색 글자
            detail_contents_button.setBackgroundResource(R.drawable.radius_smallround_pink_border); //내용 핑크색 테두리 버튼
            //다른 버튼 글자 회색, 테두리 없음
            detail_application_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //신청 회색 글자
            detail_application_button.setBackgroundResource(R.color.fui_transparent); //신청 테두리 없음
            detail_review_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //리뷰 회색 글자
            detail_review_button.setBackgroundResource(R.color.fui_transparent); //리뷰 테두리 없음

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.detail_frame, ContentsFragment); //내용 프래그먼트 연결
            fragmentTransaction.commit(); //프래그먼트 적용
        });

        //신청 버튼 눌렀을 때
        detail_application_button.setOnClickListener(v -> {
            //신청 버튼 글자, 테두리색 핑크
            detail_application_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //신청 핑크색 글자
            detail_application_button.setBackgroundResource(R.drawable.radius_smallround_pink_border); //신청 핑크색 테두리 버튼
            //다른 버튼 글자 회색, 테두리 없음
            detail_contents_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //내용 회색 글자
            detail_contents_button.setBackgroundResource(R.color.fui_transparent); //내용 테두리 없음
            detail_review_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //리뷰 회색 글자
            detail_review_button.setBackgroundResource(R.color.fui_transparent); //리뷰 테두리 없음

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.detail_frame, ApplicationFragment); //신청 프래그먼트 연결
            fragmentTransaction.commit(); //프래그먼트 적용
        });

        //리뷰 버튼 눌렀을 때
        detail_review_button.setOnClickListener(v -> {
            //내용 버튼 글자, 테두리색 핑크
            detail_review_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //리뷰 핑크색 글자
            detail_review_button.setBackgroundResource(R.drawable.radius_smallround_pink_border); //리뷰 핑크색 테두리 버튼
            //다른 버튼 글자 회색, 테두리 없음
            detail_application_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //신청 회색 글자
            detail_application_button.setBackgroundResource(R.color.fui_transparent); //신청 테두리 없음
            detail_contents_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //내용 회색 글자
            detail_contents_button.setBackgroundResource(R.color.fui_transparent); //내용 테두리 없음

            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.detail_frame, ReviewFragment); //리뷰 프래그먼트 연결
            fragmentTransaction.commit(); //프래그먼트 적용
        });
    }


    private void moreViewWelfareNotLogin() {

        //서버로부터 데이터를 받아오는데 걸리는 시간동연 보여줄 프로그래스 바
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        dialog.dismiss();
    }

    //상단 상태표시줄 화면 ui에 맞게 그라데이션 넣기
    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.actionbar_gradient_end);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    //혜택 공유하기
    private void ShareBenefit(){
        /* 카카오 링크 적용 */
        FeedTemplate params = FeedTemplate
                // 제목, 이미지 url, 이미지 클릭 시 이동하는 위치?를 입력한다
                // 이미지 url을 사용자 정의 파라미터로 전달하면 최대 2MB 이미지를 메시지에 담아 보낼 수 있다
                .newBuilder(ContentObject.newBuilder("당신이 놓치고 있는 혜택", benefit_image_url,
                        LinkObject.newBuilder().setMobileWebUrl("'https://developers.kakao.com").build())
                        .setDescrption(welf_name)  // 제목 밑의 본문
                        .build())
                .setSocial(SocialObject.newBuilder().build()).addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                        .setWebUrl("https://www.hyemo.com/")
                        .setMobileWebUrl("https://developers.kakao.com")
                        .setAndroidExecutionParams("key1=value1")
                        .build()))
                .build();

        Map<String, String> serverCallbackArgs = new HashMap<>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>()
        {
            @Override
            public void onFailure(ErrorResult errorResult)
            {
                Log.e(TAG, errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result)
            {
                // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용해야 한다.
                Log.e(TAG, result.toString());
            }
        });
    }

    //자바 변수와 xml 변수 연결
    private void init() {
        BenefitTitle = findViewById(R.id.BenefitTitle); //혜택명
        detail_contents_button = findViewById(R.id.detail_contents_button); //내용버튼
        detail_application_button = findViewById(R.id.detail_application_button); //신청버튼
        detail_review_button = findViewById(R.id.detail_review_button); //리뷰버튼

        Detail_progressbar = findViewById(R.id.Detail_progressbar); //프로그래스바
        back_btn = findViewById(R.id.back_btn); //이전 화면으로 가기
        bookmark_btn = findViewById(R.id.bookmark_btn); //북마크
        share_btn = findViewById(R.id.share_btn); //공유하기

        ContentsFragment = new DetailContentsFragment();
        ApplicationFragment = new DetailApplicationFragment();
        ReviewFragment = new DetailReviewFragment();
    }

    //로그인 했는지 여부 확인
    private void being_loging() {
        //로그인 했는지 여부 확인하기위한 쉐어드
        SharedPreferences app_pref = getSharedPreferences(getString(R.string.shared_name), 0);
        being_logout = app_pref.getBoolean("logout", false); //로그인 했는지 여부 확인하기
        SessionId = "";
        token = "";

        if (!being_logout) { //로그인 했다면
            SessionId = app_pref.getString("sessionId", ""); //세션값 받아오기
            token = app_pref.getString("token", ""); //토큰값 받아오기
        }
    }

    //인텐트로 받아온 welf_id값
    private void being_intent() {
        welf_id = ""; //혜택 아이디 값
        Intent intent = getIntent();
        being_id = intent.getBooleanExtra("being_id", false); //혜택 데이터가 있는지, 없는 경우 서버에서 데이터 안받아 오도록
        /* if (being_id) 부분 때문에 검색 -> 상세보기 화면으로 갈 수 없어서 삭제했습니다 */
        welf_id = intent.getStringExtra("welf_id");
//        Log.e(TAG, "welf_id : " + welf_id);
        review_write = intent.getBooleanExtra("review_write", false); //리뷰 작성 했는지
    }

    //북마크 하기
    private void SetBookmark() {


        //서버에서 값을 받는걸 기다리기에는 적용이 너무 느림
        if (isBookmark.equals("true")) { //북마크를 했었다면 북마크 취소하기
            isBookmark = "false"; //북마크 값을 false
            bookmark_btn.setBackgroundResource(R.drawable.bookmark_no);
            Toast.makeText(DetailTabLayoutActivity.this,"북마크가 취소 됐습니다",Toast.LENGTH_SHORT).show();
        } else if (isBookmark.equals("false")) { //북마크를 안했었다면 북마크 추가하기
            isBookmark = "true"; //북마크 값을 true
            bookmark_btn.setBackgroundResource(R.drawable.bookmark_ok);
            Toast.makeText(DetailTabLayoutActivity.this,"북마크에 추가 됐습니다",Toast.LENGTH_SHORT).show();
        }
//        Log.e("isBookmark",isBookmark);


        String URL = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/"; //연결하고자 하는 서버의 url, 반드시 /로 끝나야 함
        ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient(URL).create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
        Call<String> call = apiInterfaceTest.BookMark(token, SessionId, "bookmark", welf_id); //인터페이스에서 사용할 메소드 선언
        call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback
//                Log.e("결과",response.body().toString());

            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }


    //상세페이지 내용 데이터 서버로부터 받아오기
    void LoadBenefitDetail() {
//        Log.e(TAG, "LoadBenefitDetail() 호출");
        //서버로부터 데이터를 받아오는데 걸리는 시간동안 보여줄 프로그래스 바
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false); //"false"면 다이얼로그 나올 때 dismiss 띄우기 전까지 안사라짐
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();


        String URL = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/"; //연결하고자 하는 서버의 url, 반드시 /로 끝나야 함
        ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient(URL).create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
//        Log.e(TAG, "token : " + token + ", 세션 : " + SessionId + ", welf_id : " + welf_id);
        Call<String> call = apiInterfaceTest.BenefitDetail(token, SessionId, "detail", welf_id); //인터페이스에서 사용할 메소드 선언
        call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
            @Override
            public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback
                if (response.isSuccessful() && response.body() != null)
                {
//                    Log.e(TAG, "상세보기에서 데이터 가져오기 성공 : " + response.body());
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());

                        message = jsonObject.getJSONArray("message"); //혜택 상세 데이터
                        TotalCount = jsonObject.getString("TotalCount"); //리뷰 갯수
                        isBookmark = jsonObject.getString("isBookmark"); //북마크 여부

                        ReviewState = null;
                        //리뷰가 0이 아닐 때 리뷰 데이터를 받아온다
                        if (!TotalCount.equals("0")) {
                            ReviewState = jsonObject.getJSONArray("ReviewSate"); //리뷰데이터
//                        Log.e("ReviewState",ReviewState.toString());
                            detail_bundle.putString("ReviewState", String.valueOf(ReviewState)); //bundle안에 리뷰 데이터 담기
                        }

                        JSONArray jsonArray_message = jsonObject.getJSONArray("message");
                        JSONObject jsonObject_message = jsonArray_message.getJSONObject(0);

                        JSONArray jsonArray_welf_data = jsonObject_message.getJSONArray("welf_data");
                        JSONObject jsonObject_welf_name = jsonArray_welf_data.getJSONObject(0);
                        welf_name = jsonObject_welf_name.getString("welf_name");
                        BenefitTitle.setText(welf_name);

                        detail_bundle.putString("message", String.valueOf(message)); //bundle안에 혜택 상세 데이터 담기
                        detail_bundle.putString("TotalCount", TotalCount); //bundle안에 리뷰 갯수 담기
                        detail_bundle.putString("isBookmark", isBookmark); //bundle안에 북마크 여부 담기
                        detail_bundle.putString("welf_id",welf_id); //bundle안에 혜택id 담기
                        detail_bundle.putString("welf_name",welf_name); //bundle안에 혜택명 담기

                        ContentsFragment.setArguments(detail_bundle); //프래그먼트에 bundle데이터 담기
                        ApplicationFragment.setArguments(detail_bundle); //프래그먼트에 bundle데이터 담기
                        ReviewFragment.setArguments(detail_bundle); //프래그먼트에 bundle데이터 담기



//                    Detail_progressbar.setVisibility(View.GONE); //프로그래스바 그만 보여주기


                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.detail_frame, ContentsFragment); //처음 액티비티로 들어올 때 내용 프래그먼트를 보여준다

                        if (review_write){ //리뷰 작성 안했다면 내용 보여주고, 했다면 리뷰 바로 보여주기
                            detail_review_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_text_color)); //리뷰 핑크색 글자
                            detail_review_button.setBackgroundResource(R.drawable.radius_smallround_pink_border); //리뷰 핑크색 테두리 버튼
                            //다른 버튼 글자 회색, 테두리 없음
                            detail_application_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //신청 회색 글자
                            detail_application_button.setBackgroundResource(R.color.fui_transparent); //신청 테두리 없음
                            detail_contents_button.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGray_B)); //내용 회색 글자
                            detail_contents_button.setBackgroundResource(R.color.fui_transparent); //내용 테두리 없음

                            fragmentTransaction.replace(R.id.detail_frame, ReviewFragment); //리뷰 프래그먼트 연결
                        }

                        fragmentTransaction.commit(); //프래그먼트 적용


//                    Log.e("message",message.toString());
//                    Log.e("response.body",response.body());
                    } catch (JSONException e) {
                        e.printStackTrace();
//                    Log.e("e",e.toString());
                    }

//                    Log.e(TAG, "isBookmark : " + isBookmark);
//                    Log.e(TAG, "message : " + message);
//                    Log.e(TAG, "TotalCount : " + TotalCount);

                    if (isBookmark != null)
                    {
                        if(isBookmark.equals("true")) { //북마크 했다면
                            bookmark_btn.setBackgroundResource(R.drawable.bookmark_ok);
                        } else if (isBookmark.equals("false")) { //북마크 안했다면
                            bookmark_btn.setBackgroundResource(R.drawable.bookmark_no);
                        }
                    }
//                if(isBookmark.equals("true")) { //북마크 했다면
//                    bookmark_btn.setBackgroundResource(R.drawable.bookmark_ok);
//                } else if (isBookmark.equals("false")) { //북마크 안했다면
//                    bookmark_btn.setBackgroundResource(R.drawable.bookmark_no);
//                }


                    dialog.dismiss();
                }
                else
                {
                    Log.e(TAG, "상세보기에서 데이터 가져오기 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "상세보기에서 데이터 가져오기 에러 : " + t.getMessage());
            }
        });

    }


    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)

        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        BenefitTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 18); //혜택명
        detail_contents_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 23); //내용버튼 텍스트 크기
        detail_application_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 23); //신청버튼 텍스트 크기
        detail_review_button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.x / 23); //리뷰버튼 텍스트 크기
    }

}