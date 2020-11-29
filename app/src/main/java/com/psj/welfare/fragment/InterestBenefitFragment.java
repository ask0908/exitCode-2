package com.psj.welfare.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.psj.welfare.R;
import com.psj.welfare.activity.PushQuestionActivity;

import java.util.ArrayList;

public class InterestBenefitFragment extends Fragment
{
    private ConstraintLayout third_fragment_layout;
    private final String TAG = "ThirdFragment";
    private NumberPicker first_choice_spinner, second_choice_spinner;
    // ArrayAdapter에 들어갈 ArrayList
    private ArrayList<String> first_list = new ArrayList<>();
    private ArrayList<String> second_list = new ArrayList<>();
    private EditText age_edittext;
    private Button man_btn, woman_btn, next_btn;
    // 스피너에서 아이템을 선택했는지 여부를 확인하는 데 사용할 String 변수. 여기에 시 · 도 선택, 시 · 군 · 구 선택이 들어있다면 다음 액티비티로 넘어가지 못하게 한다
    String first_spinner_value, second_spinner_value;
    private ArrayAdapter<CharSequence> first_adapter, second_adapter;

    // 성별, 나이, 지역을 담을 변수. 질문지 액티비티로 이동 시 같이 가져간다
    String gender, age, area;

    public InterestBenefitFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_interest_benefit, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        // 화면의 빈 공간을 누르면 키보드가 사라지게 한다
        third_fragment_layout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                hideKeyboard();
                return false;
            }
        });

        /* 스피너를 NumberPicker로 바꿔서 iOS 스피너처럼 보이도록 만든다 */
        final String[] first_area = {"지역 선택", "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시", "세종시", "경기도 가평군",
        "경기도 고양시", "경기도 과천시", "경기도 광명시", "경기도 광주시", "경기도 구리시", "경기도 군포시", "경기도 남양주시", "경기도 동두천시", "경기도 부천시", "경기도 성남시",
        "경기도 수원시", "경기도 시흥시", "경기도 안산시", "경기도 안성시", "경기도 안양시", "경기도 양주시", "경기도 양평군", "경기도 여주시", "경기도 연천군", "경기도 오산시",
        "경기도 용인시", "경기도 의왕시", "경기도 의정부시", "경기도 이천시", "경기도 파주시", "경기도 평택시", "경기도 포천시", "경기도 하남시", "경기도 화성시"};
        final String[] seoul_area = {"강남구", "강동구", "강북구", "강서구", "관악구", "광진구", "구로구", "금천구", "노원구", "도봉구", "동대문구", "동작구", "마포구", "서대문구",
        "서초구", "성동구", "성북구", "송파구", "양천구", "영등포구", "용산구", "은평구", "종로구", "중구", "중랑구", "중랑구"};
        final String[] busan_area = {"강서구", "금정구", "기장군", "남구", "동구", "동래구", "부산진구", "북구", "사상구", "사상구", "사하구", "서구", "수영구", "연제구", "영도구",
        "중구", "해운대구"};
        final String[] daegu_area = {"남구", "달서구", "달성군", "동구", "북구", "서구", "수성구", "중구"};
        final String[] incheon_area = {"강화군", "계양구", "남동구", "동구", "미추홀구", "부평구", "서구", "연수구", "옹진군", "중구"};
        final String[] gwanju_area = {"광산구", "남구", "동구", "북구", "서구"};
        final String[] daejeon_area = {"대덕구", "동구", "서구", "유성구", "중구"};
        final String[] ulsan_area = {"남구", "동구", "북구", "서구", "울주군"};
        final String[] sejong_area = {"고운동", "금남면", "대평동", "도담동", "보람동", "부강면", "새롬동", "소담동", "소정면", "아름동", "연기면", "연동면", "연서면", "장군면",
        "전동면", "전의면", "조치원읍", "종촌동", "한솔동"};
        final String[] gyongi_goyang_area = {"덕양구", "일산동구", "일산서구"};
        final String[] gyongi_seongnam_area = {"분당구", "수정구", "중원구"};
        final String[] gyongi_suwon_area = {"권선구", "영통구", "장안구", "팔달구"};
        final String[] gyongi_ansan_area = {"단원구", "상록구"};
        final String[] gyongi_anyang_area = {"동안구", "만안구"};
        final String[] gyongi_yongin_area = {"기흥구", "수지구", "처인구"};
        final String[] nothing = {"위에서 시 · 도를 선택하지 않으셨군요", "시 · 도를 먼저 선택해 주세요!"};
        final String[] area_exception = {"선택이 완료되셨나요?", "완료되셨다면 확인 버튼을 눌러주세요"};

        first_choice_spinner.setMinValue(0);
        first_choice_spinner.setMaxValue(first_area.length - 1);
        first_choice_spinner.setDisplayedValues(first_area);
        first_choice_spinner.setWrapSelectorWheel(false);
        second_choice_spinner.setMinValue(0);
        second_choice_spinner.setWrapSelectorWheel(false);

        // To change format of number in NumberPicker
        first_choice_spinner.setFormatter(new NumberPicker.Formatter()
        {
            @Override
            public String format(int value)
            {
                switch (value)
                {
                    case 0 :
                        first_spinner_value = "시 · 도 선택";
                        return "시 · 도 선택";

                    case 1 :
                        first_spinner_value = "서울특별시";
                        return "서울특별시";

                    case 2 :
                        first_spinner_value = "부산광역시";
                        return "부산광역시";

                    default:
                        break;
                }
                Log.e("1번 setFormatter()", "value : " + value);
                return null;
            }
        });
        second_choice_spinner.setFormatter(new NumberPicker.Formatter()
        {
            @Override
            public String format(int value)
            {
                Log.e("setFormatter()", "value : " + value);
                return String.format("%02d", value);
            }
        });

        // 1번째 스피너의 값에 따라 2번째 스피너에 보여지는 값이 달라지게 한다
        first_choice_spinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                Log.e(TAG, "이전 값 : " + oldVal + ", 새로운 값 : " + newVal);
                switch (newVal)
                {
                    case 0 :    // 시 · 도 선택
                        Log.e(TAG, "0번 값");
                        first_spinner_value = "값 없음";
                        second_spinner_value = "값 없음";
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setDisplayedValues(nothing);
                        second_choice_spinner.setMaxValue(nothing.length - 1);
                        break;

                    case 1 :    // 서울특별시
                        Log.e(TAG, "1번 값");
                        first_spinner_value = "서울특별시";
                        second_choice_spinner.setDisplayedValues(seoul_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(seoul_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다

                        if (second_choice_spinner.getValue() == 0)
                        {
                            Log.e(TAG, "0번 인덱스의 getValue() : " + second_choice_spinner.getValue());
                            second_spinner_value = "강남구";
                        }
                        else if (second_choice_spinner.getValue() == 1)
                        {
                            Log.e(TAG, "1번 인덱스의 getValue() : " + second_choice_spinner.getValue());
                            second_spinner_value = "강동구";
                        }
//                        switch (second_choice_spinner.getValue())
//                        {
//                            case 0 :
//                                Log.e(TAG, "getValue() : " + second_choice_spinner.getValue());
//                                second_spinner_value = "강남구";
//                                break;
//
//                            case 1 :
//                                Log.e(TAG, "getValue() : " + second_choice_spinner.getValue());
//                                second_spinner_value = "강동구";
//                                break;
//
//                            case 2 :
//                                Log.e(TAG, "getValue() : " + second_choice_spinner.getValue());
//                                second_spinner_value = "강북구";
//                                break;
//
//                            case 3 :
//                                second_spinner_value = "강서구";
//                                break;
//
//                            case 4 :
//                                second_spinner_value = "관악구";
//                                break;
//
//                            case 5 :
//                                second_spinner_value = "광진구";
//                                break;
//
//                            case 6 :
//                                second_spinner_value = "구로구";
//                                break;
//
//                            case 7 :
//                                second_spinner_value = "금천구";
//                                break;
//
//                            case 8 :
//                                second_spinner_value = "노원구";
//                                break;
//
//                            case 9 :
//                                second_spinner_value = "도봉구";
//                                break;
//
//                            case 10 :
//                                second_spinner_value = "동대문구";
//                                break;
//
//                            case 11 :
//                                second_spinner_value = "동작구";
//                                break;
//
//                            case 12 :
//                                second_spinner_value = "마포구";
//                                break;
//
//                            case 13 :
//                                second_spinner_value = "서대문구";
//                                break;
//
//                            case 14 :
//                                second_spinner_value = "서초구";
//                                break;
//
//                            case 15 :
//                                second_spinner_value = "성동구";
//                                break;
//
//                            case 16 :
//                                second_spinner_value = "성북구";
//                                break;
//
//                            case 17 :
//                                second_spinner_value = "송파구";
//                                break;
//
//                            case 18 :
//                                second_spinner_value = "양천구";
//                                break;
//
//                            case 19 :
//                                second_spinner_value = "영등포구";
//                                break;
//
//                            case 20 :
//                                second_spinner_value = "용산구";
//                                break;
//
//                            case 21 :
//                                second_spinner_value = "은평구";
//                                break;
//
//                            case 22 :
//                                second_spinner_value = "종로구";
//                                break;
//
//                            case 23 :
//                                second_spinner_value = "중구";
//                                break;
//
//                            case 24 :
//                                second_spinner_value = "중랑구";
//                                break;
//
//                            default:
//                                break;
//                        }
                        break;

                    case 2 :    // 부산광역시
                        /* 동구만 나온다 */
                        Log.e(TAG, "2번 값");
                        first_spinner_value = "부산광역시";
                        second_choice_spinner.setDisplayedValues(busan_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(busan_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "강서구";
                                break;

                            case 1 :
                                second_spinner_value = "금정구";
                                break;

                            case 2 :
                                second_spinner_value = "기장군";
                                break;

                            case 3 :
                                second_spinner_value = "남구";
                                break;

                            case 4 :
                                second_spinner_value = "동구";
                                break;

                            case 5 :
                                second_spinner_value = "동래구";
                                break;

                            case 6 :
                                second_spinner_value = "부산진구";
                                break;

                            case 7 :
                                second_spinner_value = "북구";
                                break;

                            case 8 :
                                second_spinner_value = "사상구";
                                break;

                            case 9 :
                                second_spinner_value = "사하구";
                                break;

                            case 10 :
                                second_spinner_value = "서구";
                                break;

                            case 11 :
                                second_spinner_value = "수영구";
                                break;

                            case 12 :
                                second_spinner_value = "연제구";
                                break;

                            case 13 :
                                second_spinner_value = "영도구";
                                break;

                            case 14 :
                                second_spinner_value = "중구";
                                break;

                            case 15 :
                                second_spinner_value = "해운대구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 3 :    // 대구광역시
                        /* 남구만 나온다 */
                        Log.e(TAG, "3번 값");
                        first_spinner_value = "대구광역시";
                        second_choice_spinner.setDisplayedValues(daegu_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(daegu_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "남구";
                                break;

                            case 1 :
                                second_spinner_value = "달서구";
                                break;

                            case 2 :
                                second_spinner_value = "달성군";
                                break;

                            case 3 :
                                second_spinner_value = "동구";
                                break;

                            case 4 :
                                second_spinner_value = "북구";
                                break;

                            case 5 :
                                second_spinner_value = "서구";
                                break;

                            case 6 :
                                second_spinner_value = "수성구";
                                break;

                            case 7 :
                                second_spinner_value = "중구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 4 :    // 인천광역시
                        /* 강화군만 나온다 */
                        Log.e(TAG, "4번 값");
                        first_spinner_value = "인천광역시";
                        second_choice_spinner.setDisplayedValues(incheon_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(incheon_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "강화군";
                                break;

                            case 1 :
                                second_spinner_value = "계양구";
                                break;

                            case 2 :
                                second_spinner_value = "남동구";
                                break;

                            case 3 :
                                second_spinner_value = "동구";
                                break;

                            case 4 :
                                second_spinner_value = "미추홀구";
                                break;

                            case 5 :
                                second_spinner_value = "부평구";
                                break;

                            case 6 :
                                second_spinner_value = "서구";
                                break;

                            case 7 :
                                second_spinner_value = "연수구";
                                break;

                            case 8 :
                                second_spinner_value = "옹진군";
                                break;

                            case 9 :
                                second_spinner_value = "중구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 5 :    // 광주광역시
                        /* 광산구만 나온다 */
                        Log.e(TAG, "5번 값");
                        first_spinner_value = "광주광역시";
                        second_choice_spinner.setDisplayedValues(gwanju_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gwanju_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "광산구";
                                break;

                            case 1 :
                                second_spinner_value = "남구";
                                break;

                            case 2 :
                                second_spinner_value = "동구";
                                break;

                            case 3 :
                                second_spinner_value = "북구";
                                break;

                            case 4 :
                                second_spinner_value = "서구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 6 :    // 대전광역시
                        /* 서구만 나온다 */
                        Log.e(TAG, "6번 값");
                        first_spinner_value = "대전광역시";
                        second_choice_spinner.setDisplayedValues(daejeon_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(daejeon_area.length - 2);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "대덕구";
                                break;

                            case 1 :
                                second_spinner_value = "동구";
                                break;

                            case 2 :
                                second_spinner_value = "서구";
                                break;

                            case 3 :
                                second_spinner_value = "유성구";
                                break;

                            case 4 :
                                second_spinner_value = "중구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 7 :    // 울산광역시
                        /* 2번 스피너에 상관없이 1번 스피너의 1번째 값이 전달된다 */
                        Log.e(TAG, "7번 값");
                        first_spinner_value = "울산광역시";
                        second_choice_spinner.setDisplayedValues(ulsan_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(ulsan_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "남구";
                                break;

                            case 1 :
                                second_spinner_value = "동구";
                                break;

                            case 2 :
                                second_spinner_value = "북구";
                                break;

                            case 3 :
                                second_spinner_value = "서구";
                                break;

                            case 4 :
                                second_spinner_value = "울주군";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 8 :    // 세종시
                        /* 고운동만 나온다 */
                        Log.e(TAG, "8번 값");
                        first_spinner_value = "세종시";
                        second_choice_spinner.setDisplayedValues(sejong_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(sejong_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "고운동";
                                break;

                            case 1 :
                                second_spinner_value = "금남면";
                                break;

                            case 2 :
                                second_spinner_value = "대평동";
                                break;

                            case 3 :
                                second_spinner_value = "도담동";
                                break;

                            case 4 :
                                second_spinner_value = "보람동";
                                break;

                            case 5 :
                                second_spinner_value = "부강면";
                                break;

                            case 6 :
                                second_spinner_value = "새롬동";
                                break;

                            case 7 :
                                second_spinner_value = "소담동";
                                break;

                            case 8 :
                                second_spinner_value = "소정면";
                                break;

                            case 9 :
                                second_spinner_value = "아름동";
                                break;

                            case 10 :
                                second_spinner_value = "연기면";
                                break;

                            case 11 :
                                second_spinner_value = "연동면";
                                break;

                            case 12 :
                                second_spinner_value = "연서면";
                                break;

                            case 13 :
                                second_spinner_value = "장군면";
                                break;

                            case 14 :
                                second_spinner_value = "전동면";
                                break;

                            case 15 :
                                second_spinner_value = "전의면";
                                break;

                            case 16 :
                                second_spinner_value = "조치원읍";
                                break;

                            case 17 :
                                second_spinner_value = "종촌동";
                                break;

                            case 18 :
                                second_spinner_value = "한솔동";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 9 :    // 경기도 가평군
                        Log.e(TAG, "9번 값");
                        first_spinner_value = "경기도 가평군";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 10 :    // 경기도 고양시
                        Log.e(TAG, "10번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_goyang_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_goyang_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "덕양구";
                                break;

                            case 1 :
                                second_spinner_value = "일산동구";
                                break;

                            case 2 :
                                second_spinner_value = "일산서구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 11 :    // 경기도 과천시
                        Log.e(TAG, "11번 값");
                        first_spinner_value = "경기도 과천시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 12 :    // 경기도 광명시
                        Log.e(TAG, "12번 값");
                        first_spinner_value = "경기도 광명시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 13 :    // 경기도 광주시
                        Log.e(TAG, "13번 값");
                        first_spinner_value = "경기도 광주시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 14 :    // 경기도 구리시
                        Log.e(TAG, "14번 값");
                        first_spinner_value = "경기도 구리시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 15 :    // 경기도 군포시
                        Log.e(TAG, "15번 값");
                        first_spinner_value = "경기도 군포시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 16 :    // 경기도 남양주시
                        Log.e(TAG, "16번 값");
                        first_spinner_value = "경기도 남양주시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 17 :    // 경기도 동두천시
                        Log.e(TAG, "17번 값");
                        first_spinner_value = "경기도 동두천시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 18 :    // 경기도 부천시
                        Log.e(TAG, "18번 값");
                        first_spinner_value = "경기도 부천시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 19 :    // 경기도 성남시
                        Log.e(TAG, "19번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_seongnam_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_seongnam_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "분당구";
                                break;

                            case 1 :
                                second_spinner_value = "수정구";
                                break;

                            case 2 :
                                second_spinner_value = "중원구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 20 :    // 경기도 수원시
                        Log.e(TAG, "20번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_suwon_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_suwon_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "권선구";
                                break;

                            case 1 :
                                second_spinner_value = "영통구";
                                break;

                            case 2 :
                                second_spinner_value = "장안구";
                                break;

                            case 3 :
                                second_spinner_value = "팔달구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 21 :    // 경기도 시흥시
                        Log.e(TAG, "21번 값");
                        first_spinner_value = "경기도 시흥시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 22 :    // 경기도 안산시
                        Log.e(TAG, "22번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_ansan_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_ansan_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "단원구";
                                break;

                            case 1 :
                                second_spinner_value = "상록구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 23 :    // 경기도 안성시
                        Log.e(TAG, "23번 값");
                        first_spinner_value = "경기도 안성시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 24 :    // 경기도 안양시
                        Log.e(TAG, "24번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_anyang_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_anyang_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "동안구";
                                break;

                            case 1 :
                                second_spinner_value = "만안구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 25 :    // 경기도 양주시
                        Log.e(TAG, "25번 값");
                        first_spinner_value = "경기도 양주시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 26 :    // 경기도 양평군
                        Log.e(TAG, "26번 값");
                        first_spinner_value = "경기도 양평군";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 27 :    // 경기도 여주시
                        Log.e(TAG, "27번 값");
                        first_spinner_value = "경기도 여주시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 28 :    // 경기도 연천군
                        Log.e(TAG, "28번 값");
                        first_spinner_value = "경기도 연천군";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 29 :    // 경기도 오산시
                        Log.e(TAG, "29번 값");
                        first_spinner_value = "경기도 오산시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 30 :    // 경기도 용인시
                        Log.e(TAG, "30번 값");
                        second_choice_spinner.setDisplayedValues(gyongi_yongin_area);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(gyongi_yongin_area.length - 1);
                        // getValue()는 int를 리턴하기 때문에 리턴값별로 어떤 구를 보낼건지 써줘야 한다
                        switch (second_choice_spinner.getValue())
                        {
                            case 0 :
                                second_spinner_value = "기흥구";
                                break;

                            case 1 :
                                second_spinner_value = "수지구";
                                break;

                            case 2 :
                                second_spinner_value = "처인구";
                                break;

                            default:
                                break;
                        }
                        break;

                    case 31 :    // 경기도 의왕시
                        Log.e(TAG, "31번 값");
                        first_spinner_value = "경기도 의왕시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 32 :    // 경기도 의정부시
                        Log.e(TAG, "32번 값");
                        first_spinner_value = "경기도 의정부시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 33 :    // 경기도 이천시
                        Log.e(TAG, "33번 값");
                        first_spinner_value = "경기도 이천시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 34 :    // 경기도 파주시
                        Log.e(TAG, "34번 값");
                        first_spinner_value = "경기도 파주시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 35 :    // 경기도 평택시
                        Log.e(TAG, "35번 값");
                        first_spinner_value = "경기도 평택시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 36 :    // 경기도 포천시
                        Log.e(TAG, "36번 값");
                        first_spinner_value = "경기도 포천시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 37 :    // 경기도 하남시
                        Log.e(TAG, "37번 값");
                        first_spinner_value = "경기도 하남시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    case 38 :    // 경기도 화성시
                        Log.e(TAG, "38번 값");
                        first_spinner_value = "경기도 화성시";
                        second_spinner_value = "";
                        second_choice_spinner.setDisplayedValues(area_exception);
                        second_choice_spinner.setMinValue(0);
                        second_choice_spinner.setMaxValue(area_exception.length - 1);
                        break;

                    default:
                        break;
                }
            }
        });

        /* 첫 번째 스피너에 들어갈 값 초기화 */
//        first_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.first_spinner_item, android.R.layout.simple_spinner_item);
//        first_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        first_choice_spinner.setAdapter(first_adapter);
//        first_choice_spinner.setPrompt("시 · 도 선택");
//        first_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//        {
//            // 첫 번째 스피너에서 뭘 선택했냐에 따라 두 번째 스피너의 내용이 바뀌게 한다
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//            {
//                first_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                Log.e(TAG, "str_position = " + first_spinner_value);
//                // 첫 번째 스피너에서 고른 값에 따라 다른 값들이 나오도록 처리한다
//                switch (first_spinner_value)
//                {
//                    case "서울특별시" :
//                        // 서울시에 포함된 구의 이름들로 두 번째 스피너를 채운다
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.seoul_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "부산광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.busan_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "대구광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.daegu_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "인천광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.incheon_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "광주광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gwangju_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "대전광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.daejeon_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "울산광역시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.ulsan_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "세종시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sejong_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 고양시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_goyang_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 성남시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_seongnam_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 수원시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_suwon_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 안산시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_ansan_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 안양시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_anyang_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    case "경기도 용인시" :
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_yongin_item, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
//                        {
//                            @Override
//                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//                            {
//                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
//                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
//                            }
//
//                            @Override
//                            public void onNothingSelected(AdapterView<?> parent)
//                            {
//                                //
//                            }
//                        });
//                        break;
//
//                    default:
//                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.nothing, android.R.layout.simple_spinner_item);
//                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        second_choice_spinner.setAdapter(second_adapter);
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent)
//            {
//                Log.e(TAG, "아이템 클릭 안됨");
//            }
//        });


        /**/
        // 남자 버튼 클릭 시
        // 여자 버튼을 누른 채로 남자 버튼을 누르면, 여자 버튼은 원상태로 돌아오고 남자 버튼이 선택된 상태가 돼야 한다
        man_btn.setOnClickListener(OnSingleClickListener ->
        {
            woman_btn.setSelected(false);
            woman_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "남자 버튼 클릭 해제됨");
                man_btn.setSelected(false);
                man_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            }
            else
            {
                Log.e(TAG, "남자 버튼 클릭됨");
                man_btn.setSelected(true);
                man_btn.setTextColor(getResources().getColor(R.color.colorMainWhite));
                gender = "남자";
            }
        });

        // 여자 버튼 클릭 이벤트
        // 남자 버튼을 누른 상태로 여자 버튼을 누르면, 남자 버튼은 원상태로 돌아오고 여자 버튼이 선택된 상태가 돼야 한다
        woman_btn.setOnClickListener(OnSingleClickListener ->
        {
            man_btn.setSelected(false);
            man_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "여자 버튼 클릭 해제됨");
                woman_btn.setSelected(false);
                woman_btn.setTextColor(getResources().getColor(R.color.colorBlack));
            }
            else
            {
                Log.e(TAG, "여자 버튼 클릭됨");
                woman_btn.setSelected(true);
                woman_btn.setTextColor(getResources().getColor(R.color.colorMainWhite));
                gender = "여자";
            }
        });

        // 다음 버튼을 누르면 입력된 사용자 기본 정보를 갖고 액티비티 이동
        next_btn.setOnClickListener(v ->
        {
            // 값들 다 입력했는지 확인
            if (age_edittext.getText().toString().equals(""))
            {
                Toast.makeText(getActivity(), "나이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!gender.equals("남자") && !gender.equals("여자"))
            {
                Toast.makeText(getActivity(), "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (first_spinner_value.equals("값 없음") || second_spinner_value.equals("값 없음"))
            {
                Toast.makeText(getActivity(), "지역을 선택해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            // 인텐트 설정 및 보낼 데이터 (나이, 성별, 지역)
            Intent intent = new Intent(getActivity(), PushQuestionActivity.class);
            age = age_edittext.getText().toString();
            area = first_spinner_value + " " + second_spinner_value;
            intent.putExtra("age", age);
            intent.putExtra("gender", gender);
            intent.putExtra("area", area);
            Log.e(TAG, "나이 : " + age + "살, 성별 : " + gender + ", 사는 지역 : " + area);
            startActivity(intent);
        });

    }

    /* editText에 포커스된 상태에서 화면 누르면 키보드 사라지게 하는 메서드 */
    private void hideKeyboard()
    {
        if (getActivity() != null && getActivity().getCurrentFocus() != null)
        {
            // 프래그먼트기 때문에 getActivity() 사용
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void init(View view)
    {
        third_fragment_layout = view.findViewById(R.id.third_fragment_layout);
        first_choice_spinner = (NumberPicker) view.findViewById(R.id.first_choice_spinner);
        second_choice_spinner = view.findViewById(R.id.second_choice_spinner);
        age_edittext = view.findViewById(R.id.age_edittext);
        man_btn = view.findViewById(R.id.man_btn);
        woman_btn = view.findViewById(R.id.woman_btn);
        next_btn = view.findViewById(R.id.next_btn);
    }

}