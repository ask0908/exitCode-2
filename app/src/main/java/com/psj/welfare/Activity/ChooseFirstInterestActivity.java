package com.psj.welfare.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.custom.CustomInterestDialog;

import java.util.ArrayList;

/* 첫 번째 관심사 선택 화면 */
public class ChooseFirstInterestActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    ImageView first_interest_back_image;
    TextView first_select_interest_textview, first_interest_top_textview, age_text, area_text;
    // 나이대
    Button under_teenage_button, teenage_button, twenty_button, thirty_button, forty_button, fifty_button, over_sixty_button;

    // 지역
    Button interest_seoul_btn, interest_gangwon_btn, interest_gyonggi_btn, interest_gyongnam_btn, interest_gyongbuk_btn, interest_gwangju_btn,
            interest_daegu_btn, interest_daejeon_btn, interest_busan_btn, interest_sejong_btn, interest_ulsan_btn, interest_incheon_btn,
            interest_jeonnam_btn, interest_jeonbuk_btn, interest_jeju_btn, interest_chungnam_btn, interest_chungbuk_btn;
    // 다음 버튼
    Button go_second_interest_btn;

    // 선택된 나이, 지역들을 담을 리스트
    ArrayList<String> age;
    ArrayList<String> area;

    // 나이대, 지역의 버튼 수만큼 int 배열을 만들어서 클릭 횟수를 저장한다
    // 이 클릭 횟수를 통해 버튼 테두리를 바꾸거나 리스트에 값을 넣고 뺀다
    int[] arr = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // 선택 완료 버튼을 눌렀을 경우 첫 번째 선택 화면을 종료해야 하기 때문에 선언한 변수
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(this);
        setContentView(R.layout.activity_choose_first_interest);

        activity = ChooseFirstInterestActivity.this;

        // findViewById() 모아놓은 메서드
        init();

        age = new ArrayList<>();
        area = new ArrayList<>();


        ScreenSize screen = new ScreenSize();
        Point size = screen.getScreenSize(this);

        first_select_interest_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 17);
        first_interest_top_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 22);
        age_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        area_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) size.x / 24);
        go_second_interest_btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (size.x*0.055)); //버튼 텍스트 크기

        buttonsClickListener();

        /* 버튼 클릭 시 테두리와 글자 색이 바뀌고 해당 값이 변수에 저장돼야 한다
         * 나이대, 지역에서 여러 값을 선택하면 값들 사이에 "-"를 붙인다 -> 다음 버튼 누르면 리스트에 저장되게 하자 */
        go_second_interest_btn.setOnClickListener(v ->
        {
            if (age.size() == 0 || area.size() == 0)
            {
                // 나이, 지역 중 아무것도 선택하지 않았으면 선택하도록 유도
                Toast.makeText(activity, "나이와 지역 모두 1개라도 선택해 주셔야 해요", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Intent intent = new Intent(this, ChooseSecondInterestActivity.class);
                intent.putStringArrayListExtra("age", age);
                intent.putStringArrayListExtra("area", area);
                startActivity(intent);
            }
        });

        // 뒤로가기 이미지 클릭 시
        first_interest_back_image.setOnClickListener(v ->
        {
            if (age.size() > 0 || area.size() > 0)
            {
                CustomInterestDialog dialog = new CustomInterestDialog(this);
                dialog.showDialog();
            }
            else
            {
                finish();
            }
        });

    }

    // 나이, 지역 버튼 클릭 리스너들 모아놓은 메서드
    private void buttonsClickListener()
    {
        /* 나이대 */
        under_teenage_button.setOnClickListener(v ->
        {
            String value = "10대 미만";
            arr[0]++;
            if (arr[0] % 2 == 0)
            {
                // 짝수로 나눠서 나머지가 0이라면 유저가 선택하지 않았다는 뜻이다
                under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                // 나머지가 0이 아니라면 유저가 선택했다는 뜻이다
                under_teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                under_teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        teenage_button.setOnClickListener(v ->
        {
            String value = "10대";
            arr[1]++;
            if (arr[1] % 2 == 0)
            {
                teenage_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                teenage_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                teenage_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        twenty_button.setOnClickListener(v ->
        {
            String value = "20대";
            arr[2]++;
            if (arr[2] % 2 == 0)
            {
                twenty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                twenty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                twenty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        thirty_button.setOnClickListener(v ->
        {
            String value = "30대";
            arr[3]++;
            if (arr[3] % 2 == 0)
            {
                thirty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                thirty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                thirty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        forty_button.setOnClickListener(v ->
        {
            String value = "40대";
            arr[4]++;
            if (arr[4] % 2 == 0)
            {
                forty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                forty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                forty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        fifty_button.setOnClickListener(v ->
        {
            String value = "50대";
            arr[5]++;
            if (arr[5] % 2 == 0)
            {
                fifty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                fifty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                fifty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        over_sixty_button.setOnClickListener(v ->
        {
            String value = "60대 이상";
            arr[6]++;
            if (arr[6] % 2 == 0)
            {
                over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                age.remove(value);
                Log.e(TAG, "if 안 age list : " + age);
            }
            else
            {
                over_sixty_button.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                over_sixty_button.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                age.add(value);
                Log.e(TAG, "else 안 age list : " + age);
            }
        });

        /* 지역 */
        interest_seoul_btn.setOnClickListener(v ->
        {
            String value = "서울";
            arr[7]++;
            if (arr[7] % 2 == 0)
            {
                interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_seoul_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_seoul_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gangwon_btn.setOnClickListener(v ->
        {
            String value = "강원";
            arr[8]++;
            if (arr[8] % 2 == 0)
            {
                interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gangwon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gangwon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyonggi_btn.setOnClickListener(v ->
        {
            String value = "강원";
            arr[9]++;
            if (arr[9] % 2 == 0)
            {
                interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyonggi_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyonggi_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyongnam_btn.setOnClickListener(v ->
        {
            String value = "경남";
            arr[10]++;
            if (arr[10] % 2 == 0)
            {
                interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyongnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyongnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gyongbuk_btn.setOnClickListener(v ->
        {
            String value = "경북";
            arr[11]++;
            if (arr[11] % 2 == 0)
            {
                interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gyongbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gyongbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_gwangju_btn.setOnClickListener(v ->
        {
            String value = "광주";
            arr[12]++;
            if (arr[12] % 2 == 0)
            {
                interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_gwangju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_gwangju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_daegu_btn.setOnClickListener(v ->
        {
            String value = "대구";
            arr[13]++;
            if (arr[13] % 2 == 0)
            {
                interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_daegu_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_daegu_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_daejeon_btn.setOnClickListener(v ->
        {
            String value = "대전";
            arr[14]++;
            if (arr[14] % 2 == 0)
            {
                interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_daejeon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_daejeon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_busan_btn.setOnClickListener(v ->
        {
            String value = "부산";
            arr[15]++;
            if (arr[15] % 2 == 0)
            {
                interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_busan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_busan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_sejong_btn.setOnClickListener(v ->
        {
            String value = "세종";
            arr[16]++;
            if (arr[16] % 2 == 0)
            {
                interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_sejong_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_sejong_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_ulsan_btn.setOnClickListener(v ->
        {
            String value = "울산";
            arr[17]++;
            if (arr[17] % 2 == 0)
            {
                interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_ulsan_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_ulsan_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_incheon_btn.setOnClickListener(v ->
        {
            String value = "인천";
            arr[18]++;
            if (arr[18] % 2 == 0)
            {
                interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_incheon_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_incheon_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeonnam_btn.setOnClickListener(v ->
        {
            String value = "전남";
            arr[19]++;
            if (arr[19] % 2 == 0)
            {
                interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeonnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeonnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeonbuk_btn.setOnClickListener(v ->
        {
            String value = "전북";
            arr[20]++;
            if (arr[20] % 2 == 0)
            {
                interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeonbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeonbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_jeju_btn.setOnClickListener(v ->
        {
            String value = "제주";
            arr[21]++;
            if (arr[21] % 2 == 0)
            {
                interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_jeju_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_jeju_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_chungnam_btn.setOnClickListener(v ->
        {
            String value = "충남";
            arr[22]++;
            if (arr[22] % 2 == 0)
            {
                interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_chungnam_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_chungnam_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });

        interest_chungbuk_btn.setOnClickListener(v ->
        {
            String value = "충북";
            arr[23]++;
            if (arr[23] % 2 == 0)
            {
                interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.colorGray_B));
                interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_unselected));
                area.remove(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
            else
            {
                interest_chungbuk_btn.setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                interest_chungbuk_btn.setBackground(ContextCompat.getDrawable(this, R.drawable.interest_selected));
                area.add(value);
                Log.e(TAG, "if 안 area list : " + area);
            }
        });
    }

    // findViewById() 모아놓은 메서드
    private void init()
    {
        first_interest_back_image = findViewById(R.id.first_interest_back_image);
        first_select_interest_textview = findViewById(R.id.first_select_interest_textview);
        first_interest_top_textview = findViewById(R.id.first_interest_top_textview);
        age_text = findViewById(R.id.age_text);
        area_text = findViewById(R.id.area_text);

        under_teenage_button = findViewById(R.id.under_teenage_button);
        teenage_button = findViewById(R.id.teenage_button);
        twenty_button = findViewById(R.id.twenty_button);
        thirty_button = findViewById(R.id.thirty_button);
        forty_button = findViewById(R.id.forty_button);
        fifty_button = findViewById(R.id.fifty_button);
        over_sixty_button = findViewById(R.id.over_sixty_button);

        interest_seoul_btn = findViewById(R.id.interest_seoul_btn);
        interest_gangwon_btn = findViewById(R.id.interest_gangwon_btn);
        interest_gyonggi_btn = findViewById(R.id.interest_gyonggi_btn);
        interest_gyongnam_btn = findViewById(R.id.interest_gyongnam_btn);
        interest_gyongbuk_btn = findViewById(R.id.interest_gyongbuk_btn);
        interest_gwangju_btn = findViewById(R.id.interest_gwangju_btn);
        interest_daegu_btn = findViewById(R.id.interest_daegu_btn);
        interest_daejeon_btn = findViewById(R.id.interest_daejeon_btn);
        interest_busan_btn = findViewById(R.id.interest_busan_btn);
        interest_sejong_btn = findViewById(R.id.interest_sejong_btn);
        interest_ulsan_btn = findViewById(R.id.interest_ulsan_btn);
        interest_incheon_btn = findViewById(R.id.interest_incheon_btn);
        interest_jeonnam_btn = findViewById(R.id.interest_jeonnam_btn);
        interest_jeonbuk_btn = findViewById(R.id.interest_jeonbuk_btn);
        interest_jeju_btn = findViewById(R.id.interest_jeju_btn);
        interest_chungnam_btn = findViewById(R.id.interest_chungnam_btn);
        interest_chungbuk_btn = findViewById(R.id.interest_chungbuk_btn);

        go_second_interest_btn = findViewById(R.id.go_second_interest_btn);
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.renewal_gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

    @Override
    public void onBackPressed()
    {
        if (age.size() > 0 || area.size() > 0)
        {
            CustomInterestDialog dialog = new CustomInterestDialog(this);
            dialog.showDialog();
        }
        else
        {
            finish();
        }
    }
}