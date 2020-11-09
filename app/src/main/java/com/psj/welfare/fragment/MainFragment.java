package com.psj.welfare.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.psj.welfare.R;
import com.psj.welfare.activity.ResultBenefitActivity;
import com.psj.welfare.custom.CustomResultBenefitDialog;
import com.psj.welfare.custom.OnSingleClickListener;

import java.util.ArrayList;

/**
 * MainActivity를 대체하는 프래그먼트
 * 앱 실행 시 이 프래그먼트의 화면이 먼저 보이게 한다
 */
public class MainFragment extends Fragment {
	public static final String TAG = "MainFragment"; // 로그 찍을 때 사용하는 TAG

	// 스크롤뷰
	private ScrollView main_ScrollView;

	// 좌상단 메인 로고. 클릭 시 더보기 버튼이 있는 페이지로 화면 전환 효과 없이 이동하게 한다 (main logo)
	ImageView main_logo, main_job_img, main_student_img, main_living_img, main_pregnancy_img, main_child_img, main_cultural_img, main_company_img, main_homeless_img, main_old_img, main_disorder_img, main_multicultural_img, main_law_img, main_etc_img;

	TextView main_move_text, main_job_title, main_student_title, main_living_title, main_pregnancy_title, main_child_title, main_cultural_title, main_company_title, main_homeless_title, main_old_title, main_disorder_title, main_multicultural_title, main_law_title, main_etc_title;

	LinearLayout main_job, main_student, main_living, main_pregnancy, main_child, main_cultural, main_company, main_homeless, main_old, main_disorder, main_multicultural, main_law, main_etc;

	// 맨 밑의 조회하기 버튼
	Button main_done;

	// 더보기 버튼을 눌렀는지 확인할 때 사용할 boolean 변수. true일 경우에만 로고 클릭 이벤트를 발동시킨다
	boolean isClicked = false;

	// 더보기 버튼 있는 레이아웃
	LinearLayout more_layout;
	// 장애인, 다문화, 법률, 기타 버튼 있는 레이아웃
	LinearLayout multi_law_layout, etc_layout;

	ArrayList<String> m_favorList = new ArrayList<>();  // 유저에게 제공할 혜택들을 담을 ArrayList

	// 스크롤 수정
	LinearLayout main_content;

	public MainFragment() {
		// 프래그먼트 사용 시 있어야 하는 디폴트 생성자
	}

	/* 프래그먼트와 연결된 뷰 계층 생성 위해 호출됨, onViewCreated()보다 먼저 호출된다
	 * 최초에 onAttach()가 호출된 이후 onCreate() 다음에 3번째로 호출된다 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView() 호출");
		return (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
	}

	/* onCreateView() 다음에 호출됨. 뷰 계층 구조가 완전히 생성됐음을 알려 하위 클래스가 스스로 초기화되게 한다 */
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.e(TAG, "onViewCreated() 호출");

		init(view);

		/* 더보기 버튼을 누르면 더보기 버튼이 사라지고 다른 4가지 버튼들이 나오게 한다 */
		more_layout.setOnClickListener(OnSingleClickListener -> {
			isClicked = true;
			Log.e(TAG, "isClicked 상태 : " + isClicked);
			Log.e(TAG, "더보기 버튼 클릭됨");

			// 더보기 버튼이 있는 레이아웃은 아예 안 보이게 한다
			more_layout.setVisibility(View.GONE);

			// 다른 4가지 레이아웃들은 보이도록 한다
			multi_law_layout.setVisibility(View.VISIBLE);
			etc_layout.setVisibility(View.VISIBLE);
			main_disorder.setVisibility(View.VISIBLE);
			main_law.setVisibility(View.VISIBLE);
		});

		// 메인 로고 클릭 리스너
		main_logo.setOnClickListener(OnSingleClickListener -> {
			if (isClicked) {
				isClicked = false;
				Log.e(TAG, "isClicked 상태 : " + isClicked);
				// 화면 전환 효과 없이 더보기 버튼이 있는 페이지로 이동하게 한다 = VISIBLE 상태가 되었던 버튼들을 다시 GONE 상태로 되돌린다
				more_layout.setVisibility(View.VISIBLE);
				multi_law_layout.setVisibility(View.GONE);
				etc_layout.setVisibility(View.GONE);
				main_disorder.setVisibility(View.GONE);
				main_law.setVisibility(View.GONE);
			}
		});

		// 조회하기 버튼
		main_done.setOnClickListener(OnSingleClickListener -> {
			Log.e("main_done 버튼 클릭", "m_favorList 크기 = " + m_favorList.size());
			// 관심사 선택이 1개라도 안돼 있으면 커스텀 다이얼로그를 띄운다
			if (m_favorList.size() == 0)
			{
				CustomResultBenefitDialog dialog = new CustomResultBenefitDialog(getActivity());
				dialog.callDialog();
			}
			else
			{
				m_favorList.add(0, "전체");
				for (int i = 0; i < m_favorList.size(); i++) {
					Log.e(TAG, "m_favorList : " + m_favorList);
				}
				Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
				m_intent.putStringArrayListExtra("favor_btn", m_favorList);
				startActivity(m_intent);
			}
//			m_favorList.add(0, "전체");
//			for (int i = 0; i < m_favorList.size(); i++) {
//				Log.e(TAG, "m_favorList : " + m_favorList);
//			}
//			Intent m_intent = new Intent(getActivity(), ResultBenefitActivity.class);
//			m_intent.putStringArrayListExtra("favor_btn", m_favorList);
//			startActivity(m_intent);
		});

		/* 아기·어린이 혜택 버튼 */
		main_child.setOnClickListener(OnSingleClickListener -> {
			Log.e(TAG, "아기·어린이 레이아웃 클릭!");

			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("아기·어린이");
				main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child));
				main_child_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_child.setSelected(!main_child.isSelected());
			} else {
				m_favorList.add("아기·어린이");
				main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child_after));
				main_child_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_child.setSelected(true);
			}
		});

		/* 학생·청년 혜택 버튼 */
		main_student.setOnClickListener(OnSingleClickListener -> {
			Log.e(TAG, "학생·청년 레이아웃 클릭!");

			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("학생·청년");
				main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student));
				main_student_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_student.setSelected(!main_student.isSelected());
			} else {
				m_favorList.add("학생·청년");
				main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student_after));
				main_student_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_student.setSelected(true);
			}
		});

		/* 중장년·노인 혜택 버튼 */
		main_old.setOnClickListener(OnSingleClickListener -> {
			Log.e(TAG, "학생·청년 레이아웃 클릭!");

			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("중장년·노인");
				main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old));
				main_old_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_old.setSelected(!main_old.isSelected());
			} else {
				m_favorList.add("중장년·노인");
				main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old_after));
				main_old_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_old.setSelected(true);
			}
		});

		/* 육아·임신 혜택 버튼 */
		main_pregnancy.setOnClickListener(OnSingleClickListener -> {
			Log.e(TAG, "학생·청년 레이아웃 클릭!");

			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("육아·임신");
				main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy));
				main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_pregnancy.setSelected(!main_pregnancy.isSelected());
			} else {
				m_favorList.add("육아·임신");
				main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy_after));
				main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_pregnancy.setSelected(true);
			}
		});

		/* 장애인 혜택 버튼 */
		main_disorder.setOnClickListener(OnSingleClickListener -> {

			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("장애인");
				main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder));
				main_disorder_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_disorder.setSelected(!main_disorder.isSelected());
			} else {
				m_favorList.add("장애인");
				main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder_after));
				main_disorder_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_disorder.setSelected(true);
			}
		});

		/* 문화·생활 혜택 버튼 */
		main_cultural.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("문화·생활");
				main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural));
				main_cultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_cultural.setSelected(!main_cultural.isSelected());
			} else {
				m_favorList.add("문화·생활");
				main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural_after));
				main_cultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_cultural.setSelected(true);
			}
		});

		/* 다문화 혜택 버튼 */
		main_multicultural.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("다문화");
				main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural));
				main_multicultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_multicultural.setSelected(!main_multicultural.isSelected());
			} else {
				m_favorList.add("다문화");
				main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural_after));
				main_multicultural_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_multicultural.setSelected(true);
			}
		});

		/* 기업·자영업자 혜택 버튼 */
		main_company.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("기업·자영업자");
				main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company));
				main_company_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_company.setSelected(!main_company.isSelected());
			} else {
				m_favorList.add("기업·자영업자");
				main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company_after));
				main_company_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_company.setSelected(true);
			}
		});

		/* 법률 혜택 버튼 */
		main_law.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("법률");
				main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law));
				main_law_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_law.setSelected(!main_law.isSelected());
			} else {
				m_favorList.add("법률");
				main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law_after));
				main_law_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_law.setSelected(true);
			}
		});

		/* 주거 혜택 버튼 */
		main_living.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("주거");
				main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living));
				main_living_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_living.setSelected(!main_living.isSelected());
			} else {
				m_favorList.add("주거");
				main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living_after));
				main_living_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_living.setSelected(true);
			}
		});

		/* 취업·창업 혜택 버튼 */
		main_job.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("취업·창업");
				main_job_title.setTextColor(getResources().getColor(R.color.colorBlack));
				main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_job.setSelected(!main_job.isSelected());
			} else {
				m_favorList.add("취업·창업");
				main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job_after));
				main_job_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_job.setSelected(true);
			}
		});

		/* 저소득층 혜택 버튼 */
		main_homeless.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("저소득층");
				main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless));
				main_homeless_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_homeless.setSelected(!main_homeless.isSelected());
			} else {
				m_favorList.add("저소득층");
				main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless_after));
				main_homeless_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_homeless.setSelected(true);
			}
		});

		/* 기타 혜택 버튼 */
		main_etc.setOnClickListener(OnSingleClickListener -> {
			if (OnSingleClickListener.isSelected()) {
				m_favorList.remove("기타");
				main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc));
				main_etc_title.setTextColor(getResources().getColor(R.color.colorBlack));
				Log.i(TAG, "버튼 클릭 상태 비활성화");
				main_etc.setSelected(!main_etc.isSelected());
			} else {
				m_favorList.add("기타");
				main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc_after));
				main_etc_title.setTextColor(getResources().getColor(R.color.colorMainWhite));
				Log.i(TAG, "버튼 클릭 활성화!!");
				main_etc.setSelected(true);
			}
		});

		// 혜택 조회하러 가기 클릭 했을 때
		main_move_text.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void onSingleClick(View v) {
				Log.i(TAG, "혜택 조회하러 가기 클릭!");

				// 스크롤 0.5초만에 Y축 main_content 머리로 이동
				main_ScrollView.post(new Runnable() {
					@Override
					public void run() {
						if (!isClicked) {
							// 더보기 버튼 클릭 전 스크롤 이동
							ObjectAnimator.ofInt(main_ScrollView, "scrollY", main_content.getTop()).setDuration(500).start();
						} else {
							// 더보기 버튼 클릭 후 스크롤 이동
							ObjectAnimator.ofInt(main_ScrollView, "scrollY", main_content.getTop()).setDuration(500).start();
						}

					} // run end
				}); // post end
			}
		});

	}

	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);
		Log.e(TAG, "onAttach() 실행");
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate() 실행");
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.e(TAG, "onActivityCreated() 실행");
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.e(TAG, "onStart() 실행");
		// 조회하기 버튼만 누르고 이동했다가 다시 돌아온 후, 다시 조회하기 버튼을 누르면 전체가 여러 개 찍히는 현상이 있다
		// 이 현상을 없애기 위해서 ArrayList.clear()로 리스트를 싹 비운다
		m_favorList.clear();

		// ResultBenefitActivity에서 뒤로가기 누를 시 버튼이 선택된 상태의 프래그먼트로 돌아오기 때문에, 버튼이 선택되지 않은 처음의 상태로 되돌리기 위해서 필요한 로직
		main_child_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.child));
		main_child_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_child.setSelected(false);

		main_student_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.student));
		main_student_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_student.setSelected(false);

		main_old_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.old));
		main_old_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_old.setSelected(false);

		main_pregnancy_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.pregnancy));
		main_pregnancy_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_pregnancy.setSelected(false);

		main_disorder_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.disorder));
		main_disorder_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_disorder.setSelected(false);

		main_cultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.cultural));
		main_cultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_cultural.setSelected(false);

		main_multicultural_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.multicultural));
		main_multicultural_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_multicultural.setSelected(false);

		main_company_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company));
		main_company_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_company.setSelected(false);

		main_law_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.law));
		main_law_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_law.setSelected(false);

		main_living_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.living));
		main_living_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_living.setSelected(false);

		main_job_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.job));
		main_job_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_job.setSelected(false);

		main_homeless_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.homeless));
		main_homeless_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_homeless.setSelected(false);

		main_etc_img.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.etc));
		main_etc_title.setTextColor(getResources().getColor(R.color.colorBlack));
		main_etc.setSelected(false);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.e(TAG, "onResume() 실행");
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.e(TAG, "onPause() 실행");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.e(TAG, "onStop() 실행");
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		Log.e(TAG, "onDestroyView() 실행");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.e(TAG, "onDestroy() 실행");
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		Log.e(TAG, "onDetach() 실행");
	}

	private void init(View view) {
		main_content = view.findViewById(R.id.main_content);

		main_logo = view.findViewById(R.id.main_logo);
		more_layout = view.findViewById(R.id.more_layout);

		/* 더보기 버튼을 누르면 나타나야 하는 레이아웃 */
		multi_law_layout = view.findViewById(R.id.multi_law_layout);
		etc_layout = view.findViewById(R.id.etc_layout);

		main_ScrollView = view.findViewById(R.id.main_ScrollView);
		main_move_text = view.findViewById(R.id.main_move_text);

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
		main_etc = view.findViewById(R.id.main_etc);
		main_done = view.findViewById(R.id.main_done);

		main_child_img = view.findViewById(R.id.main_child_img);
		main_student_img = view.findViewById(R.id.main_student_img);
		main_old_img = view.findViewById(R.id.main_old_img);
		main_pregnancy_img = view.findViewById(R.id.main_pregnancy_img);
		main_disorder_img = view.findViewById(R.id.main_disorder_img);
		main_cultural_img = view.findViewById(R.id.main_cultural_img);
		main_multicultural_img = view.findViewById(R.id.main_multicultural_img);
		main_company_img = view.findViewById(R.id.main_company_img);
		main_law_img = view.findViewById(R.id.main_law_img);
		main_living_img = view.findViewById(R.id.main_living_img);
		main_job_img = view.findViewById(R.id.main_job_img);
		main_homeless_img = view.findViewById(R.id.main_homeless_img);
		main_etc_img = view.findViewById(R.id.main_etc_img);

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
		main_etc_title = view.findViewById(R.id.main_etc_title);

	} // init end

} // MainFragment class end