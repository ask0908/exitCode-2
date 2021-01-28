package com.psj.welfare.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.psj.welfare.R;
import com.psj.welfare.activity.ResultBenefitActivity;
import com.psj.welfare.activity.SearchResultActivity;
import com.psj.welfare.custom.CustomResultBenefitDialog;

import java.util.ArrayList;

/* 혜택 이름 검색하는 프래그먼트
* 하단의 카테고리들을 선택하고 버튼을 눌러 이동한 경우, 혜택 하위 카테고리 검색을 적용해 결과를 보여준다
* 카테고리 다중선택이 없어져서 이제 해시태그 키워드를 누르면, 그 카테고리에 관련된 혜택들만 보여준다 */
public class SearchFragment extends Fragment
{
    public static final String TAG = "SearchFragment"; // 로그 찍을 때 사용하는 TAG

    private EditText searching;

    Toolbar search_toolbar;

    TextView recommend_search_textview, recent_search_history_textview;

    TextView main_job_title, main_student_title, main_living_title, main_pregnancy_title, main_child_title, main_cultural_title,
            main_company_title, main_homeless_title, main_old_title, main_disorder_title, main_multicultural_title, main_law_title;

    LinearLayout main_job, main_student, main_living, main_pregnancy, main_child, main_cultural, main_company, main_homeless, main_old, main_disorder,
            main_multicultural, main_law;

    // 유저에게 제공할 혜택들을 담을 ArrayList
    ArrayList<String> m_favorList;

    Button main_done;

    public SearchFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        m_favorList = new ArrayList<>();

        init(view);

        recommend_search_textview = view.findViewById(R.id.recommend_search_textview);
        recent_search_history_textview = view.findViewById(R.id.recent_search_history_textview);
        searching = view.findViewById(R.id.searching);

        search_toolbar = view.findViewById(R.id.search_toolbar);
        if ((AppCompatActivity)getActivity() != null)
        {
            ((AppCompatActivity)getActivity()).setSupportActionBar(search_toolbar);
        }
        search_toolbar.setTitle("검색");

        // 안드로이드 EditText 키보드에서 검색 버튼 추가 코드
        searching.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    Log.e(TAG, "검색 키워드 : " + searching.getText());
//                    search_main.setVisibility(View.GONE);
                    // 검색 버튼 클릭 되었을 때 처리하는 기능
                    performSearch(searching.getText().toString());
                    return true;
                }

                return false;
            }

        });

        /* 아기·어린이 혜택 버튼 */
        main_child.setOnClickListener(OnSingleClickListener ->
        {
            Log.e(TAG, "아기·어린이 레이아웃 클릭!");

            if (OnSingleClickListener.isSelected())
            {
                Log.e(TAG, "OnSingleClickListener.isSelected() = " + OnSingleClickListener.isSelected());
                m_favorList.remove("아기·어린이");
                main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                main_child.setSelected(!main_child.isSelected());
                main_child.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                Log.e(TAG, "OnSingleClickListener.isSelected() = " + OnSingleClickListener.isSelected());
                m_favorList.add("아기·어린이");
                main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                main_child.setSelected(true);
                main_child.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 학생·청년 혜택 버튼 */
        main_student.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("청년");
                main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_student.setSelected(!main_student.isSelected());
                main_student.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                // 이 때 아이콘이 하얗게 변한다
                m_favorList.add("청년");
                main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_student.setSelected(true);
                main_student.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 중장년·노인 혜택 버튼 */
        main_old.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("중장년·노인");
                main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_old.setSelected(!main_old.isSelected());
                main_old.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("중장년·노인");
                main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_old.setSelected(true);
                main_old.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 육아·임신 혜택 버튼 */
        main_pregnancy.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("육아·임신");
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_pregnancy.setSelected(!main_pregnancy.isSelected());
                main_pregnancy.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("육아·임신");
                main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_pregnancy.setSelected(true);
                main_pregnancy.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 장애인 혜택 버튼 */
        main_disorder.setOnClickListener(OnSingleClickListener ->
        {

            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("장애인");
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_disorder.setSelected(!main_disorder.isSelected());
                main_disorder.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("장애인");
                main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_disorder.setSelected(true);
                main_disorder.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 문화·생활 혜택 버튼 */
        main_cultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("문화·생활");
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_cultural.setSelected(!main_cultural.isSelected());
                main_cultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("문화·생활");
                main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_cultural.setSelected(true);
                main_cultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 다문화 혜택 버튼 */
        main_multicultural.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("다문화");
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_multicultural.setSelected(!main_multicultural.isSelected());
                main_multicultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("다문화");
                main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_multicultural.setSelected(true);
                main_multicultural.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 기업·자영업자 혜택 버튼 */
        main_company.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("기업·자영업자");
                main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_company.setSelected(!main_company.isSelected());
                main_company.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("기업·자영업자");
                main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_company.setSelected(true);
                main_company.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 법률 혜택 버튼 */
        main_law.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("법률");
                main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_law.setSelected(!main_law.isSelected());
                main_law.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("법률");
                main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_law.setSelected(true);
                main_law.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 주거 혜택 버튼 */
        main_living.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("주거");
                main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_living.setSelected(!main_living.isSelected());
                main_living.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("주거");
                main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_living.setSelected(true);
                main_living.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 취업·창업 혜택 버튼 */
        main_job.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("취업·창업");
                main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_job.setSelected(!main_job.isSelected());
                main_job.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("취업·창업");
                main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_job.setSelected(true);
                main_job.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        /* 저소득층 혜택 버튼 */
        main_homeless.setOnClickListener(OnSingleClickListener ->
        {
            if (OnSingleClickListener.isSelected())
            {
                m_favorList.remove("저소득층");
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 상태 비활성화");
                main_homeless.setSelected(!main_homeless.isSelected());
                main_homeless.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));
            }
            else
            {
                m_favorList.add("저소득층");
                main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
                Log.e(TAG, "버튼 클릭 활성화!!");
                main_homeless.setSelected(true);
                main_homeless.setBackground(getResources().getDrawable(R.drawable.search_fragment_after_layout));

                // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
                if (m_favorList.size() == 0)
                {
                    CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                    dialog.callDialog();
                }
                else
                {
                    /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                    if (!m_favorList.contains("전체"))
                    {
                        m_favorList.add(0, "전체");
                    }
                    for (int i = 0; i < m_favorList.size(); i++)
                    {
                        Log.e(TAG, "m_favorList : " + m_favorList);
                    }
                    // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                    Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                    m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                    startActivity(m_intent);
                    // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                    m_favorList.clear();
                }
            }
        });

        // 조회하기 버튼
        main_done.setOnClickListener(OnSingleClickListener ->
        {
            // 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
            if (m_favorList.size() == 0)
            {
                CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
                dialog.callDialog();
            }
            else
            {
                /* if문 안의 처리를 하지 않으면 백버튼을 누르고 혜택 조회하러 가기 버튼을 누를 때마다 전체 버튼이 계속 생성되는 버그가 발생함 */
                if (!m_favorList.contains("전체"))
                {
                    m_favorList.add(0, "전체");
                }
                for (int i = 0; i < m_favorList.size(); i++)
                {
                    Log.e(TAG, "m_favorList : " + m_favorList);
                }
                // 선택한 모든 카테고리 이름을 인텐트에 넣어서 보낸다
                Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
                m_intent.putStringArrayListExtra("favor_btn", m_favorList);
                startActivity(m_intent);
                // 백버튼으로 이 곳에 돌아온 다음 같은 걸 체크해 보내면 쌓여서 가는 현상이 있어 clear()로 비운다
                m_favorList.clear();
            }
        });

        main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));

    }

    // 모바일 키보드에서 검색 버튼 눌렀을 때
    public void performSearch(String search)
    {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searching.getWindowToken(), 0);
        Log.e(TAG, "performSearch() 안으로 들어온 검색 키워드 : " + search);
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra("search", search);
        startActivity(intent);
        // 아래가 원래 작동하던 코드
//        if (search != null)
//        {
//            searchWelfare(search);
//        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_child.setSelected(false);

        main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_student.setSelected(false);

        main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_old.setSelected(false);

        main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_pregnancy.setSelected(false);

        main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_disorder.setSelected(false);

        main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_cultural.setSelected(false);

        main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_multicultural.setSelected(false);

        main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_company.setSelected(false);

        main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_law.setSelected(false);

        main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_living.setSelected(false);

        main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_job.setSelected(false);

        main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
        main_homeless.setSelected(false);
    }

    private void init(View view)
    {
        main_done = view.findViewById(R.id.main_done);

        main_child = view.findViewById(R.id.main_child);
        main_student = view.findViewById(R.id.main_student);
        main_old = view.findViewById(R.id.main_old);
        main_pregnancy = view.findViewById(R.id.main_pregnancy);
        main_disorder = view.findViewById(R.id.main_disorder);
        main_cultural = view.findViewById(R.id.main_cultural);
        main_multicultural = view.findViewById(R.id.main_multicultural);
        main_company = view.findViewById(R.id.main_company);
        main_law = view.findViewById(R.id.main_law);
        main_living = view.findViewById(R.id.main_living);
        main_job = view.findViewById(R.id.main_job);
        main_homeless = view.findViewById(R.id.main_homeless);

        main_child_title = view.findViewById(R.id.main_child_title);
        main_student_title = view.findViewById(R.id.main_student_title);
        main_old_title = view.findViewById(R.id.main_old_title);
        main_pregnancy_title = view.findViewById(R.id.main_pregnancy_title);
        main_disorder_title = view.findViewById(R.id.main_disorder_title);
        main_cultural_title = view.findViewById(R.id.main_cultural_title);
        main_multicultural_title = view.findViewById(R.id.main_multicultural_title);
        main_company_title = view.findViewById(R.id.main_company_title);
        main_law_title = view.findViewById(R.id.main_law_title);
        main_living_title = view.findViewById(R.id.main_living_title);
        main_job_title = view.findViewById(R.id.main_job_title);
        main_homeless_title = view.findViewById(R.id.main_homeless_title);
    }

}