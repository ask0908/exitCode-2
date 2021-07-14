package com.psj.welfare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.psj.welfare.activity.MainTabLayoutActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class TutorialResult extends AppCompatActivity {

    private String age, gender, local; //카테고리 선택값을 담을 변수
    private ArrayList<String> welf_name, welf_tag = new ArrayList<>(); //서버에서 받은 혜택을 담을 변수

    private Button BtnGoMain; //메인으로 가기 버튼
    private TextView BenefitTitle1, BenefitTitle2, BenefitTitle3; //혜택명
    private TextView BenefitTag1, BenefitTag2, BenefitTag3; //혜택태그
    private TextView BenefitText; //혜택 설명 텍스트

    //쉐어드 싱글톤
    private SharedSingleton sharedSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_result);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // 상태바 글자색 검정색으로 바꾸기

        //쉐어드 싱글톤 사용
        sharedSingleton = SharedSingleton.getInstance(this);

        BtnGoMain = findViewById(R.id.BtnGoMain); //메인으로 가기 버튼
        BenefitTitle1 = findViewById(R.id.BenefitTitle1); //혜택명1
        BenefitTitle2 = findViewById(R.id.BenefitTitle2); //혜택명2
        BenefitTitle3 = findViewById(R.id.BenefitTitle3); //혜택명3
        BenefitTag1 = findViewById(R.id.BenefitTag1); //혜택태그1
        BenefitTag2 = findViewById(R.id.BenefitTag2); //혜택태그2
        BenefitTag3 = findViewById(R.id.BenefitTag3); //혜택태그3
        BenefitText = findViewById(R.id.BenefitText); //혜택 설명 텍스트

        //버튼 및 텍스트의 사이즈를 동적으로 맞춤
        SetSize();

        //혜택 데이터 가져오기
        LodingBenefit();

        //메인으로 가기 버튼
        BtnGoMain.setOnClickListener(v -> {
            Intent intent = new Intent(TutorialResult.this, MainTabLayoutActivity.class);

            //미리보기 했다는 정보 입력
            sharedSingleton.setBooleanPreview(true);

            startActivity(intent);
            finish();
        });


    }


    //혜택 데이터 가져오기
    //room데이터 이용은 메인 쓰레드에서 하면 안된다
    public void LodingBenefit() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setCancelable(false); //"false"면 다이얼로그 나올 때 dismiss 띄우기 전까지 안사라짐
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        new Thread(() -> { //Room은 메인 스레드에서 실행시키면 오류가 난다
            //Room을 쓰기위해 데이터베이스 객체 만들기
            AppDatabase database = Room.databaseBuilder(TutorialResult.this, AppDatabase.class, "Firstcategory")
                    .fallbackToDestructiveMigration()
                    .build();

            //DB에 쿼리를 던지기 위해 선언
            CategoryDao categoryDao = database.getcategoryDao();

            List<CategoryData> alldata = categoryDao.findAll();
            for (CategoryData data : alldata) {
                age = data.age;
                gender = data.gender;
                local = data.home;
            }

            String benefit = age + " " + local + " " + gender + "이\n놓치고 있는 혜택이예요";
            BenefitText.setText(benefit);


            String URL = "https://8daummzu2k.execute-api.ap-northeast-2.amazonaws.com/"; //연결하고자 하는 서버의 url, 반드시 /로 끝나야 함
            ApiInterfaceTest apiInterfaceTest = ApiClientTest.ApiClient(URL).create(ApiInterfaceTest.class); //레트로핏 인스턴스로 인터페이스 객체 구현
            Call<String> call = apiInterfaceTest.BenefitTutorial(age, gender, local, "tutorial"); //인터페이스에서 사용할 메소드 선언
            call.enqueue(new Callback<String>() { //enqueue로 비동기 통신 실행, 통신 완료 후 이벤트 처리 위한 callback 리스너 등록
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) { //onResponse 통신 성공시 callback

                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray jsonArray = jsonObject.getJSONArray("message");
                        Log.e("message",String.valueOf(jsonArray.toString()));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);

                            String result = "";
                            if (!object.getString("welf_tag").equals("")) { //태그의 값이 있다면
                                String RemoveBlank = object.getString("welf_tag").replaceAll("\\s+", "");
//                                Log.e("RemoveBlank", RemoveBlank);
                                String DataReplace1 = RemoveBlank.replace("-", " #"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
                                String DataReplace2 = DataReplace1.replace(" -", " #"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
                                String DataReplace3 = DataReplace2.replace("- ", " #"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
                                String DataReplace4 = DataReplace3.replace(" - ", " #"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈
                                String DataReplace5 = DataReplace4.replace("–", " #"); //제이슨으로 받은 태그에서 -구분자를 #으로 바꿈


//                                Log.e("DataReplace", DataReplace5);
                                result = "#" + DataReplace5; //맨 앞에 string #추가
//                                Log.e("result", result);
                            }

                            //파싱한 데이터 값 넣어주기
                            if (i == 0) {
                                BenefitTitle1.setText(object.getString("welf_name"));
                                BenefitTag1.setText(result);
                            } else if (i == 1) {
                                BenefitTitle2.setText(object.getString("welf_name"));
                                BenefitTag2.setText(result);
                            } else {
                                BenefitTitle3.setText(object.getString("welf_name"));
                                BenefitTag3.setText(result);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

        }).start();
    }

    //버튼 및 텍스트의 사이즈를 동적으로 맞춤
    void SetSize(){
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        /* getActivity().getWindowManager().getDefaultDisplay() */ // in Fragment
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)

        //디스플레이 값을 기준으로 버튼 텍스트 크기를 정함
        BtnGoMain.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/20); //메인으로 가기 버튼튼
        BenefitTitle1.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/25); //첫번째 혜택 혜택명
        BenefitTitle2.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/25); //두번째 혜택 혜택명
        BenefitTitle3.setTextSize(TypedValue.COMPLEX_UNIT_PX,size.x/25); //세번째 혜택 혜택명
    }


}