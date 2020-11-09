package com.psj.welfare.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.Data.ResultBenefitItem;
import com.psj.welfare.R;
import com.psj.welfare.adapter.RBFAdapter;
import com.psj.welfare.adapter.RBFTitleAdapter;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.custom.OnSingleClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * 메인 페이지에서 관심사 선택 후 보여주는 혜택 결과 페이지는 양이 많은 결과 데이터를 사용자에게
 * 직관성있게 보여주어야 한다
 * 그리고 사용자가 맞춤 혜택을 찾을 수 있는 흐름도 보여주어야 한다
 * */
public class ResultBenefitActivity extends AppCompatActivity
{

    public static final String TAG = "ResultBenefitActivity"; // 로그 찍을 때 사용하는 TAG

    // 리사이클러뷰 객체 선언
    private RecyclerView RbfBtn_recycler, RbfTitle_recycler;
    private RecyclerView.Adapter RbfBtn_Adapter, RbfTitle_Adapter;

    TextView RB_title; // 혜택 결과 개수 타이틀
    ImageView RBF_back; // 뒤로가기 버튼 이미지
    Button select_go;
    int position_RB = 1; // 관심사 버튼 넘버
    int position_RBT = 0; // 관심사 타이틀 넘버

    private ArrayList<ResultBenefitItem> RBF_ListSet; // 관심사 버튼 리스트
    private ArrayList<ResultBenefitItem> RBFTitle_ListSet; // 관심사 타이틀 리스트
    ArrayList<String> favor_data; // 관심사 버튼 문자열

    // 서버에서 응답 받은 JSON 구조를 파싱하기 위한 변수들
    JSONArray child, student, law, old, pregnancy, disorder, cultural, multicultural, company, living, job, homeless, etc;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultbenefit);

        RB_title = findViewById(R.id.RB_title);
        RBF_back = findViewById(R.id.RBF_back);
        select_go = findViewById(R.id.select_go);
        RbfBtn_recycler = findViewById(R.id.RbfBtn_recycler);
        RbfTitle_recycler = findViewById(R.id.RbfTitle_recycler);

        RBF_ListSet = new ArrayList<>();
        RBFTitle_ListSet = new ArrayList<>();

        // 메인에서 전달받은 인텐트를 검사하는 곳
        // 선택한 관심사를 현재 페이지에서 버튼으로 표현하기 위한 데이터
        if (getIntent().hasExtra("favor_btn"))
        {
            Intent RB_intent = getIntent();
            favor_data = RB_intent.getStringArrayListExtra("favor_btn");
            Log.i(TAG, "리스트 형태 관심사 정보 -> " + favor_data.toString());
            // 메인에서 전달받은 리스트 첫번째 정보는 '전체'인데 이것을 포커싱 상태로 변환하는 로직이다
            RBF_ListSet.add(0, new ResultBenefitItem(favor_data.get(0), R.drawable.rbf_btn_after));

            // 메인에서 전달받은 리스트 첫번째를 제외한 나머지 정보는 포커싱 상태를 해제하기 위한 로직이다
            for (int i = 1; i < favor_data.size(); i++)
            {
                Log.i(TAG, "리스트 형태 관심사 버튼 반복문 -> " + favor_data.get(i));
                RBF_ListSet.add(position_RB, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                position_RB++;
            }

        }
        else
        {
            Log.e(TAG, "전달 받은 인텐트 값 없어요!");
        }

        // 리사이클러뷰로 구현한 버튼을 클릭하면 로그가 출력되게 하고 싶다
        RbfBtn_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RbfBtn_Adapter = new RBFAdapter(getApplicationContext(), RBF_ListSet, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object obj = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) obj;
                    Log.e(TAG, "관심사 버튼을 클릭 했어요 -> " + position);
                    int btnColor = ((RBFAdapter) RbfBtn_Adapter).getRBF(position).getRBF_btnColor();
                    Log.e(TAG, "내가 선택한 버튼 색상 : " + btnColor);
                    Log.e(TAG, "비교할 버튼 새상 : " + R.drawable.btn_done);

                    if (btnColor != R.drawable.rbf_btn_after)
                    {
                        Log.e(TAG, "선택하지 않은 버튼 입니다!");
                        for (int i = 0; i < favor_data.size(); i++)
                        {
                            RBF_ListSet.set(i, new ResultBenefitItem(favor_data.get(i), R.drawable.rbf_btn_before));
                        }
                        RBF_ListSet.set(position, new ResultBenefitItem(favor_data.get(position), R.drawable.rbf_btn_after));

                        RbfBtn_Adapter.notifyItemRangeChanged(0, favor_data.size());

                        // 전체 버튼 클릭 기능 시작
                        if (favor_data.get(position).equals("전체"))
                        {
                            Log.e(TAG, "전체 혜택 세팅 전 포지션 값 -> " + position_RBT);
                            // 어댑터에 연결되어 있던 리스트 값을 전부 삭제한다
                            // 왜냐하면, IndexOutOfBoundException 에러가 발생하는데
                            // 전에 세팅되어 있던 어댑터에 데이터가 남아있는 상태로 다시 세팅을 했을 때
                            // 사이즈가 달라 생기는 문제라서 이렇게 해결해 보았다
                            RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                            position_RBT = 0;
                            RBFTitle_ListSet.clear();
                            try
                            {
                                for (int i = 0; i < favor_data.size(); i++)
                                {
                                    if (favor_data.get(i).equals("아기·어린이"))
                                    {
                                        Log.e(TAG, "아기·어린이 혜택 결과 길이 -> " + child.length());
                                        for (int j = 0; j < child.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(child.getString(j)));
                                            position_RBT++;

                                        } // 아기·어린이 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("학생·청년"))
                                    {
                                        Log.e(TAG, "학생·청년 혜택 결과 길이 -> " + student.length());
                                        for (int j = 0; j < student.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(student.getString(j)));
                                            position_RBT++;

                                        } // 학생·청년 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("법률"))
                                    {
                                        Log.e(TAG, "학생·청년 혜택 결과 길이 -> " + law.length());
                                        for (int j = 0; j < law.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(law.getString(j)));
                                            position_RBT++;

                                        } // 법률 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("중장년·노인"))
                                    {
                                        Log.e(TAG, "중장년·노인 혜택 결과 길이 -> " + old.length());
                                        for (int j = 0; j < old.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(old.getString(j)));
                                            position_RBT++;

                                        } // 중장년·노인 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("육아·임신"))
                                    {
                                        Log.e(TAG, "육아·임신 혜택 결과 길이 -> " + pregnancy.length());
                                        for (int j = 0; j < pregnancy.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(pregnancy.getString(j)));
                                            position_RBT++;

                                        } // 육아·임신 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("장애인"))
                                    {
                                        Log.e(TAG, "장애인 혜택 결과 길이 -> " + disorder.length());
                                        for (int j = 0; j < disorder.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(disorder.getString(j)));
                                            position_RBT++;

                                        } // 장애인 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("문화·생활"))
                                    {
                                        Log.e(TAG, "문화·생활 혜택 결과 길이 -> " + cultural.length());
                                        for (int j = 0; j < cultural.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(cultural.getString(j)));
                                            position_RBT++;

                                        } // 문화·생활 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("다문화"))
                                    {
                                        Log.e(TAG, "다문화 혜택 결과 길이 -> " + multicultural.length());
                                        for (int j = 0; j < multicultural.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(multicultural.getString(j)));
                                            position_RBT++;

                                        } // 다문화 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("기업·자영업자"))
                                    {
                                        Log.e(TAG, "기업·자영업자 혜택 결과 길이 -> " + company.length());
                                        for (int j = 0; j < company.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(company.getString(j)));
                                            position_RBT++;

                                        } // 기업·자영업자 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("취업·창업"))
                                    {
                                        Log.e(TAG, "취업·창업 혜택 결과 길이 -> " + job.length());
                                        for (int j = 0; j < job.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(job.getString(j)));
                                            position_RBT++;

                                        } // 취업·창업 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("주거"))
                                    {
                                        Log.e(TAG, "주거 혜택 결과 길이 -> " + living.length());
                                        for (int j = 0; j < living.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(living.getString(j)));
                                            position_RBT++;

                                        } // 주거 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("기초생활수급자"))
                                    {
                                        Log.e(TAG, "기초생활수급자 혜택 결과 길이 -> " + homeless.length());
                                        for (int j = 0; j < homeless.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(homeless.getString(j)));
                                            position_RBT++;

                                        } // 기초생활수급자 세팅 끝
                                    }
                                    else if (favor_data.get(i).equals("기타"))
                                    {
                                        Log.e(TAG, "기타 혜택 결과 길이 -> " + etc.length());
                                        for (int j = 0; j < etc.length(); j++)
                                        {
                                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(etc.getString(j)));
                                            position_RBT++;

                                        } // 기타 세팅 끝
                                    }
                                } // 관심사 반복문 끝
                                Log.e(TAG, "전체 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);

                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        } // 전체 버튼 클릭 기능 끝

                        // 아기·어린이 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("아기·어린이"))
                            {
                                Log.e(TAG, "아기·어린이 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "아기·어린이 혜택 결과 길이 -> " + child.length());
                                for (int j = 0; j < child.length(); j++)
                                {

                                    Log.e(TAG, "아기·어린이 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(child.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "아기·어린이 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 아기·어린이 버튼 클릭 기능 끝

                        // 학생·청년 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("학생·청년"))
                            {
                                Log.e(TAG, "학생·청년 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "학생·청년 혜택 결과 길이 -> " + student.length());
                                for (int j = 0; j < student.length(); j++)
                                {

                                    Log.e(TAG, "학생·청년 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(student.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "학생·청년 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);


                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 학생·청년 버튼 클릭 기능 끝

                        // 중장년·노인 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("중장년·노인"))
                            {
                                Log.e(TAG, "중장년·노인 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "중장년·노인 혜택 결과 길이 -> " + old.length());
                                for (int j = 0; j < old.length(); j++)
                                {

                                    Log.e(TAG, "중장년·노인 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(old.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "중장년·노인 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 중장년·노인 버튼 클릭 기능 끝

                        // 육아·임신 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("육아·임신"))
                            {
                                Log.e(TAG, "육아·임신 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "육아·임신 혜택 결과 길이 -> " + pregnancy.length());
                                for (int j = 0; j < pregnancy.length(); j++)
                                {

                                    Log.e(TAG, "육아·임신 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(pregnancy.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "육아·임신 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 육아·임신 버튼 클릭 기능 끝

                        // 장애인 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("장애인"))
                            {
                                Log.e(TAG, "장애인 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "장애인 혜택 결과 길이 -> " + disorder.length());
                                for (int j = 0; j < disorder.length(); j++)
                                {

                                    Log.e(TAG, "장애인 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(disorder.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "장애인 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 장애인 버튼 클릭 기능 끝

                        // 문화·생활 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("문화·생활"))
                            {
                                Log.e(TAG, "문화·생활 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "문화·생활 혜택 결과 길이 -> " + cultural.length());
                                for (int j = 0; j < cultural.length(); j++)
                                {

                                    Log.e(TAG, "문화·생활 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(cultural.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "문화·생활 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 문화·생활 버튼 클릭 기능 끝

                        // 다문화 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("다문화"))
                            {
                                Log.e(TAG, "다문화 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "다문화 혜택 결과 길이 -> " + multicultural.length());
                                for (int j = 0; j < multicultural.length(); j++)
                                {

                                    Log.e(TAG, "다문화 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(multicultural.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "다문화 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 다문화 버튼 클릭 기능 끝

                        // 기업·자영업자 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("기업·자영업자"))
                            {
                                Log.e(TAG, "기업·자영업자 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "기업·자영업자 혜택 결과 길이 -> " + company.length());
                                for (int j = 0; j < company.length(); j++)
                                {

                                    Log.e(TAG, "기업·자영업자 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(company.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "기업·자영업자 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 기업·자영업자 버튼 클릭 기능 끝

                        // 법률 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("법률"))
                            {
                                Log.e(TAG, "법률 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "법률 혜택 결과 길이 -> " + law.length());
                                for (int j = 0; j < law.length(); j++)
                                {

                                    Log.e(TAG, "법률 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(law.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "법률 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 법률 버튼 클릭 기능 끝

                        // 주거 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("주거"))
                            {
                                Log.e(TAG, "주거 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "주거 혜택 결과 길이 -> " + living.length());
                                for (int j = 0; j < living.length(); j++)
                                {

                                    Log.e(TAG, "주거 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(living.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "주거 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 주거 버튼 클릭 기능 끝

                        // 취업·창업 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("취업·창업"))
                            {
                                Log.e(TAG, "취업·창업 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "취업·창업 혜택 결과 길이 -> " + job.length());
                                for (int j = 0; j < job.length(); j++)
                                {

                                    Log.e(TAG, "취업·창업 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(job.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "취업·창업 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 취업·창업 버튼 클릭 기능 끝

                        // 저소득층 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("저소득층"))
                            {
                                Log.e(TAG, "저소득층 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "저소득층 혜택 결과 길이 -> " + homeless.length());
                                for (int j = 0; j < homeless.length(); j++)
                                {

                                    Log.e(TAG, "저소득층 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(homeless.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "저소득층 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 저소득층 버튼 클릭 기능 끝

                        // 기타 버튼 클릭 기능 시작
                        try
                        {
                            if (favor_data.get(position).equals("기타"))
                            {
                                Log.e(TAG, "기타 혜택 세팅 전 포지션 값 -> " + position_RBT);
                                RbfTitle_Adapter.notifyItemRangeRemoved(0, position_RBT);
                                position_RBT = 0;
                                RBFTitle_ListSet.clear();
                                Log.e(TAG, "기타 혜택 결과 길이 -> " + etc.length());
                                for (int j = 0; j < etc.length(); j++)
                                {

                                    Log.e(TAG, "기타 혜택 결과 리스트 세팅 시작");
                                    RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(etc.getString(j)));
                                    position_RBT++;
                                    RbfTitle_Adapter.notifyItemInserted(position_RBT);
                                }
                                Log.e(TAG, "기타 혜택 세팅 후 리사이클러뷰 포지션 크기 -> " + position_RBT);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } // 기타 버튼 클릭 기능 끝

                    }
                    else
                    {
                        Log.e(TAG, "선택한 버튼 입니다!");
                    } // 버튼 활성화 or 비활성화 체크 끝

                } // 리사이클러뷰 포지션 값 체크 끝
            } // 리사이클러뷰 클릭 기능 끝
        }); // 어댑터 세팅 기능 끝
        RbfBtn_recycler.setAdapter(RbfBtn_Adapter);
        RbfBtn_recycler.setHasFixedSize(true);

        // 리사이클러뷰로 구현한 버튼을 클릭하면 로그가 출력되게 하고 싶다
        RbfTitle_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RbfTitle_Adapter = new RBFTitleAdapter(getApplicationContext(), RBFTitle_ListSet, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Object obj = v.getTag();
                if (v.getTag() != null)
                {
                    int position = (int) obj;
                    Log.e(TAG, "관심사 버튼을 클릭 했어요 -> " + position);
                    String title = ((RBFTitleAdapter) RbfTitle_Adapter).getRBFTitle(position).getRBF_Title();

                    Intent RBF_intent = new Intent(ResultBenefitActivity.this, DetailBenefitActivity.class);
                    RBF_intent.putExtra("RBF_title", title);
                    startActivity(RBF_intent);
                    finish();

                }

            }
        });
        RbfTitle_recycler.setAdapter(RbfTitle_Adapter);
        RbfTitle_recycler.setHasFixedSize(true);

        String favor = "";
        for (int i = 0; i < favor_data.size(); i++)
        {

            favor += favor_data.get(i) + ",";

        }

        Log.e(TAG, "리스트값 스트링으로 변환 -> " + favor);

        // 레트로핏 서버 URL 설정해놓은 객체 생성 후 GET, POST 같은 서버에 데이터를 보내기 위해서 생성합니다
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        // 인터페이스 ApiService에 선언한 mainFavor()를 호출합니다
        Call<String> call = apiInterface.mainFavor(favor);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(Call<String> call, Response<String> response)
            {
                if (response.isSuccessful())
                {
                    Log.e(TAG, "onResponse 성공 : " + response.body().toString());
                    /*
                     * 사용자가 선택한 관심사와 혜택 제목이 서버에서 응답이 온다
                     * 응답은 Json 구조로 응답이 올 것이고 나는 응답 받은 Json 데이터를 파싱할 것이다
                     * */
                    String favorData = response.body().toString();
                    jsonParsing(favorData);


                }
                else
                {
                    Log.e(TAG, "onResponse 실패");

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t)
            {
                Log.e(TAG, "onFailure : " + t.toString());


            }
        }); // call enqueue end


    } // onCreate end

    @Override
    protected void onResume()
    {
        super.onResume();

        RBF_back.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent RBF_intent = new Intent(ResultBenefitActivity.this, MainTabLayoutActivity.class);
                startActivity(RBF_intent);
                finish();
            }
        });

        // 1차 카테고리 선택을 하기 위한 페이지 이동
        select_go.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                Intent RBF_intent = new Intent(ResultBenefitActivity.this, FirstCategory.class);
                startActivity(RBF_intent);
                finish();
            }
        });

    } // onResume end

    private void jsonParsing(String favorData)
    {

        /*
         * 파라미터로 받은 Json 구조를 파싱할 것이다
         * ex) 아기·어린이 혜택이라면 해당 혜택과 관련된 결과들을 ArrayList 에 저장할 것이다
         * ArrayList 에 저장한 값은 리사이클러뷰로 위에서 구현된 혜택 버튼의 문자열과 비교하여 일치한다면
         * 해당 리스트를 출력해 줄 것이다
         * 리스트의 값이 2개 이상일 경우를 위해서 ArrayList 0번째 인자에는 혜택 제목(아기·어린이 혜택)을 저장할 것이다
         * */
        try
        {
            JSONObject jsonObject = new JSONObject(favorData);

            Log.e(TAG, "JSON 길이 : " + jsonObject.length());

            // 사용자가 선택한 관심사와 서버에서 받은 관심사를 비교하여 일치하면
            // 혜택 결과 리스트에 추가
            // 처음 페이지에 접근했을 때 전체 결과를 세팅하는 로직
            position_RBT = 0;
            try
            {
                for (int i = 0; i < favor_data.size(); i++)
                {
                    if (favor_data.get(i).equals("아기·어린이"))
                    {
                        child = jsonObject.getJSONArray("아기·어린이");
                        Log.e(TAG, "아기·어린이 혜택 결과 길이 -> " + child.length());
                        for (int j = 0; j < child.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(child.getString(j)));
                            position_RBT++;

                        } // 아기·어린이 세팅 끝
                    }
                    else if (favor_data.get(i).equals("학생·청년"))
                    {
                        student = jsonObject.getJSONArray("학생·청년");
                        Log.e(TAG, "학생·청년 혜택 결과 길이 -> " + student.length());
                        for (int j = 0; j < student.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(student.getString(j)));
                            position_RBT++;

                        } // 학생·청년 세팅 끝
                    }
                    else if (favor_data.get(i).equals("법률"))
                    {
                        law = jsonObject.getJSONArray("법률");
                        Log.e(TAG, "학생·청년 혜택 결과 길이 -> " + law.length());
                        for (int j = 0; j < law.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(law.getString(j)));
                            position_RBT++;

                        } // 법률 세팅 끝
                    }
                    else if (favor_data.get(i).equals("중장년·노인"))
                    {
                        old = jsonObject.getJSONArray("중장년·노인");
                        Log.e(TAG, "중장년·노인 혜택 결과 길이 -> " + old.length());
                        for (int j = 0; j < old.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(old.getString(j)));
                            position_RBT++;

                        } // 중장년·노인 세팅 끝
                    }
                    else if (favor_data.get(i).equals("육아·임신"))
                    {
                        pregnancy = jsonObject.getJSONArray("육아·임신");
                        Log.e(TAG, "육아·임신 혜택 결과 길이 -> " + pregnancy.length());
                        for (int j = 0; j < pregnancy.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(pregnancy.getString(j)));
                            position_RBT++;

                        } // 육아·임신 세팅 끝
                    }
                    else if (favor_data.get(i).equals("장애인"))
                    {
                        disorder = jsonObject.getJSONArray("장애인");
                        Log.e(TAG, "장애인 혜택 결과 길이 -> " + disorder.length());
                        for (int j = 0; j < disorder.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(disorder.getString(j)));
                            position_RBT++;

                        } // 장애인 세팅 끝
                    }
                    else if (favor_data.get(i).equals("문화·생활"))
                    {
                        cultural = jsonObject.getJSONArray("문화·생활");
                        Log.e(TAG, "문화·생활 혜택 결과 길이 -> " + cultural.length());
                        for (int j = 0; j < cultural.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(cultural.getString(j)));
                            position_RBT++;

                        } // 문화·생활 세팅 끝
                    }
                    else if (favor_data.get(i).equals("다문화"))
                    {
                        multicultural = jsonObject.getJSONArray("다문화");
                        Log.e(TAG, "다문화 혜택 결과 길이 -> " + multicultural.length());
                        for (int j = 0; j < multicultural.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(multicultural.getString(j)));
                            position_RBT++;

                        } // 다문화 세팅 끝
                    }
                    else if (favor_data.get(i).equals("기업·자영업자"))
                    {
                        company = jsonObject.getJSONArray("기업·자영업자");
                        Log.e(TAG, "기업·자영업자 혜택 결과 길이 -> " + company.length());
                        for (int j = 0; j < company.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(company.getString(j)));
                            position_RBT++;

                        } // 기업·자영업자 세팅 끝
                    }
                    else if (favor_data.get(i).equals("취업·창업"))
                    {
                        job = jsonObject.getJSONArray("취업·창업");
                        Log.e(TAG, "취업·창업 혜택 결과 길이 -> " + job.length());
                        for (int j = 0; j < job.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(job.getString(j)));
                            position_RBT++;

                        } // 취업·창업 세팅 끝
                    }
                    else if (favor_data.get(i).equals("주거"))
                    {
                        living = jsonObject.getJSONArray("주거");
                        Log.e(TAG, "주거 혜택 결과 길이 -> " + living.length());
                        for (int j = 0; j < living.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(living.getString(j)));
                            position_RBT++;

                        } // 주거 세팅 끝
                    }
                    else if (favor_data.get(i).equals("저소득층"))
                    {
                        homeless = jsonObject.getJSONArray("저소득층");
                        Log.e(TAG, "저소득층 혜택 결과 길이 -> " + homeless.length());
                        for (int j = 0; j < homeless.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(homeless.getString(j)));
                            position_RBT++;

                        } // 저소득층 세팅 끝
                    }
                    else if (favor_data.get(i).equals("기타"))
                    {
                        etc = jsonObject.getJSONArray("기타");
                        Log.e(TAG, "기타 혜택 결과 길이 -> " + etc.length());
                        for (int j = 0; j < etc.length(); j++)
                        {
                            RBFTitle_ListSet.add(position_RBT, new ResultBenefitItem(etc.getString(j)));
                            position_RBT++;

                        } // 기타 세팅 끝
                    }
                } // 관심사 반복문 끝
                Log.e(TAG, "리사이클러뷰 포지션 크기 -> " + position_RBT);

                // 메인 문구 색상 변경 시작
                RB_title.setText("복지 혜택 결과가 '" + position_RBT + "' 개가 검색되었습니다.");
                String m_word = RB_title.getText().toString();
                SpannableString spannableString = new SpannableString(m_word);

                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#6f52e8")), 10, 14, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                RB_title.setText(spannableString);
                // 메인 문구 색상 변경 끝

                RbfTitle_Adapter.notifyDataSetChanged();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    } // jsonParsing end

} // ResultBenefitActivity class end
