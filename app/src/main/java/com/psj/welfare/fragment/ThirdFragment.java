package com.psj.welfare.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.psj.welfare.R;
import com.psj.welfare.activity.PushQuestionActivity;

import java.util.ArrayList;

public class ThirdFragment extends Fragment
{
    private ConstraintLayout third_fragment_layout;
    private final String TAG = "ThirdFragment";
    private Spinner first_choice_spinner, second_choice_spinner;
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

    public ThirdFragment()
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
        return inflater.inflate(R.layout.fragment_third, container, false);
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

        // 첫 번째 스피너에 들어갈 값 초기화
        first_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.first_spinner_item, android.R.layout.simple_spinner_item);
        first_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        first_choice_spinner.setAdapter(first_adapter);
        first_choice_spinner.setPrompt("시 · 도 선택");
        first_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            // 첫 번째 스피너에서 뭘 선택했냐에 따라 두 번째 스피너의 내용이 바뀌게 한다
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                first_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                Log.e(TAG, "str_position = " + first_spinner_value);
                // 첫 번째 스피너에서 고른 값에 따라 다른 값들이 나오도록 처리한다
                switch (first_spinner_value)
                {
                    case "서울특별시" :
                        // 서울시에 포함된 구의 이름들로 두 번째 스피너를 채운다
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.seoul_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "부산광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.busan_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "대구광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.daegu_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "인천광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.incheon_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "광주광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gwangju_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "대전광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.daejeon_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "울산광역시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.ulsan_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "세종시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sejong_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 고양시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_goyang_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 성남시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_seongnam_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 수원시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_suwon_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 안산시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_ansan_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 안양시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_anyang_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    case "경기도 용인시" :
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.gyongi_yongin_item, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        second_choice_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                        {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                            {
                                second_spinner_value = String.valueOf(parent.getItemAtPosition(position));
                                Log.e(TAG, "2번째 선택한 값 = " + second_spinner_value);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent)
                            {
                                //
                            }
                        });
                        break;

                    default:
                        second_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.nothing, android.R.layout.simple_spinner_item);
                        second_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        second_choice_spinner.setAdapter(second_adapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Log.e(TAG, "아이템 클릭 안됨");
            }
        });


        // 남자 버튼 클릭 시
        // 여자 버튼을 누른 채로 남자 버튼을 누르면, 여자 버튼은 원상태로 돌아오고 남자 버튼이 선택된 상태가 돼야 한다
        man_btn.setOnClickListener(OnSingleClickListener -> {
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
        woman_btn.setOnClickListener(OnSingleClickListener -> {
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
        next_btn.setOnClickListener(v -> {
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
            if (first_spinner_value.equals("시 · 도 선택"))
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
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void init(View view)
    {
        third_fragment_layout = view.findViewById(R.id.third_fragment_layout);
        first_choice_spinner = view.findViewById(R.id.first_choice_spinner);
        second_choice_spinner = view.findViewById(R.id.second_choice_spinner);
        age_edittext = view.findViewById(R.id.age_edittext);
        man_btn = view.findViewById(R.id.man_btn);
        woman_btn = view.findViewById(R.id.woman_btn);
        next_btn = view.findViewById(R.id.next_btn);
    }

}