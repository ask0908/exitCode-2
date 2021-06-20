 package com.psj.welfare.test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.psj.welfare.AppDatabase;
import com.psj.welfare.BannerDetail;
import com.psj.welfare.CategoryDao;
import com.psj.welfare.CategoryData;
import com.psj.welfare.DetailTabLayoutActivity;
import com.psj.welfare.MainBannerAdapter;
import com.psj.welfare.MainBannerData;
import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;
import com.psj.welfare.activity.YoutubeActivity;
import com.psj.welfare.activity.YoutubeMoreActivity;
import com.psj.welfare.adapter.MainDownAdapter;
import com.psj.welfare.adapter.MainHorizontalYoutubeAdapter;
import com.psj.welfare.data.HorizontalYoutubeItem;
import com.psj.welfare.data.MainThreeDataItem;
import com.psj.welfare.util.DBOpenHelper;
import com.psj.welfare.viewmodel.MainViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/* 구현 완료 시 MainFragment로 옮긴다 */
public class TestFragment extends Fragment
{
    public static final String TAG = TestFragment.class.getSimpleName();

    private ViewPager2 MainBannerViewpager2; //메인 배너
    private MainBannerAdapter bannerAdapter; //뷰페이저에 넣을 아답터
    private ArrayList<MainBannerData> bannerList; //메인 배너 담을 리스트
    private MainBannerAdapter.BannerListener bannerListener; //배너 클릭 리스너

    private ConstraintLayout youtube_title_layout; //유튜버 혜택 리뷰 타이틀
    private ConstraintLayout MainTop; //상단 타이틀
    private ConstraintLayout welfdata_layout; //맞춤 혜택 데이터를 담고 있는 레이아웃
    private ConstraintLayout scrollview_innerlayout; //메인에서 사용하는 스크롤 뷰 안에 있는 레이아웃
    private ConstraintLayout Welfdata_title_layout; //로그인시 닉네임 보여주는 레이아웃(ex 20대, 서울, 여성 맞춤 혜택)
    private CardView notlogin_card; //비로그인 + 미리보기 안했을 경우 나타나는 카드뷰
    private TextView title_text; //메인 텍스트 사이즈
    private TextView youtube_title_text; //"유튜버들의 혜택 리뷰"

    //로그인 여부를 확인하기 위해 사용하는 쉐어드
    private SharedPreferences app_pref;
    private String age = null; //미리보기 나이
    private String gender = null; //미리보기 성별
    private String local = null; //미리보기 지역
    private String benefit = null; //미리보기 관심사 선택시 데이터

    // 최상위 핸들러 정의
    private Handler sliderHandler = new Handler();


    // 20대, 강원, 여성
    TextView Welfdata_first_title;
    // "맞춤 혜택"
    TextView Welfdata_second_title;
    // 더보기(화면 이동)
    TextView more_see_textview;

    // 편집하기 버튼
    Button notlogin_button;

    // 전체, 건강, 교육, 교통 등 관심 주제를 가로로 보여줄 상단 리사이클러뷰
//    RecyclerView up_recycler;
//    MainCategoryAdapter upAdapter;
//    MainCategoryAdapter.ItemClickListener up_clickListener;
//    List<MainThreeDataItem> keyword_list;
    MainThreeDataItem all_item;

    // 상단 리사이클러뷰에서 선택한 값에 따라 내용물이 바뀌는 "하단 리사이클러뷰", 3개만 보여준다
    RecyclerView MainWelfdata;
    MainDownAdapter downAdapter;
    MainDownAdapter.ItemClickListener downClickListener;
    List<MainThreeDataItem> down_list;

    List<MainThreeDataItem> other_list;

    // 유튜브 영상 가로로 보여줄 리사이클러뷰, 첫 아이템은 모두 보여주고 바로 옆의 아이템은 조금 잘려서 보이도록 한다
    RecyclerView youtube_video_recyclerview;
    MainHorizontalYoutubeAdapter youtubeAdapter;
    MainHorizontalYoutubeAdapter.ItemClickListener youtubeClickListener;
    List<HorizontalYoutubeItem> youtube_list;

    DBOpenHelper helper;
    String sqlite_token;

//    HashMap<String, String> youtube_hashmap;

    ValueHandler handler = new ValueHandler(); //타이틀을 사용하기 위한 핸들러

    int count_int;

    boolean loggedOut = false;

    String thumbnail, title, videoId;
    String welf_name, welf_local, welf_category, tag, count;

    private FirebaseAnalytics analytics;

    /* MVVM 적용 */
    MainViewModel mainViewModel;

    // 새 서버에서 가져온 유튜브 데이터를 저장할 변수
    String youtube_id, youtube_title, youtube_thumbnail, youtube_videoId;
    // 새 서버에서 가져온 혜택 데이터를 저장할 변수. 메인 화면의 3개 아이템을 보여주는 리사이클러뷰에 이 변수에 담긴 값들을 set한다
    String welfare_id, welfare_name, welfare_tag, welfare_viewcount;
    // 새 서버에서 가져온 배너 데이터를 저장할 변수
    String banner_image, banner_title;
    public TestFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        youtube_title_text = view.findViewById(R.id.youtube_title_text);
        title_text = view.findViewById(R.id.title_text);
        youtube_title_layout = view.findViewById(R.id.youtube_title_layout);
        MainTop = view.findViewById(R.id.MainTop);
        welfdata_layout = view.findViewById(R.id.welfdata_layout);
        MainBannerViewpager2 = view.findViewById(R.id.MainBannerViewpager2);
        notlogin_card = view.findViewById(R.id.notlogin_card);
        Welfdata_title_layout = view.findViewById(R.id.Welfdata_title_layout);
        scrollview_innerlayout = view.findViewById(R.id.scrollview_innerlayout);
        Welfdata_first_title = view.findViewById(R.id.Welfdata_first_title);
        Welfdata_second_title = view.findViewById(R.id.Welfdata_second_title);
        more_see_textview = view.findViewById(R.id.more_see_textview);
        notlogin_button = view.findViewById(R.id.notlogin_button);
//        up_recycler = view.findViewById(R.id.interest_subject_recyclerview);
        MainWelfdata = view.findViewById(R.id.MainWelfdata);
        youtube_video_recyclerview = view.findViewById(R.id.youtube_video_recyclerview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

//        keyword_list = new ArrayList<>();
        down_list = new ArrayList<>();
//        youtube_hashmap = new HashMap<>();
        youtube_list = new ArrayList<>();
        other_list = new ArrayList<>();

        MainWelfdata.setLayoutManager(new LinearLayoutManager(getActivity()));
        downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
        MainWelfdata.setAdapter(downAdapter);

        helper = new DBOpenHelper(getActivity());
        helper.openDatabase();
        helper.create();

        //메인 배너 뷰페이저 셋팅
        bannerList = new ArrayList<>(); //메인 배너 담을 리스트
        bannerAdapter = new MainBannerAdapter(bannerList,getActivity(),bannerListener,MainBannerViewpager2);
        MainBannerViewpager2.setAdapter(bannerAdapter);


        //싱글톤 패턴
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
            {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        showWelfareAndYoutubeNotLogin();

        // view 100+ -> view 0으로 변경
        // 편집하기 각각 왼쪽, 오른쪽 간격 수정

        if (getActivity() != null)
        {
            analytics = FirebaseAnalytics.getInstance(getActivity());
        }

        //selected_interest_textview(제목) 값 넣기
        settitle();

        //xml크기를 동적으로 변환
        setsize();

        //배너 뷰페이저
        setviewpager();


        //배너 아이템 클릭
        bannerAdapter.setbannerClickListener(new MainBannerAdapter.BannerListener() {
            @Override
            public void bannerClick(View v, int pos) {
                Intent intent = new Intent(getActivity(), BannerDetail.class);
                intent.putExtra("banner_title", bannerList.get(pos).getTitle());
                startActivity(intent);
            }
        });

        //혜택 아이템 클릭
        downAdapter.setOnItemClickListener(new MainDownAdapter.ItemClickListener(){
            @Override
            public void onMainThreeClick(View v, int pos) {
                    Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
                    intent.putExtra("welf_id", down_list.get(pos).getWelf_id());
                    intent.putExtra("being_id",true);
                    startActivity(intent);
            }
        });

        //혜택 더보기 클릭
        more_see_textview.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TestMoreViewActivity.class);
            startActivity(intent);
        });

        //자동 스크롤
        MainBannerViewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 2500);
            }
        });
    }


    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            MainBannerViewpager2.setCurrentItem(MainBannerViewpager2.getCurrentItem() + 1);
        }
    };

    //배너 뷰페이저
    private void setviewpager() {
        // ViewPager에서 양쪽 페이지를 미리보는 기능을 만들려면 clipToPadding, clipChilderen 값을 false로 지정해주고
        //setOffscreenPageLimit() 값을 설정해줍니다.
        // 그리고 setPageTransformer() 메서드 에서 값을 설정해줘야 합니다.
        MainBannerViewpager2.setClipToPadding(false);
        MainBannerViewpager2.setClipChildren(false);
        MainBannerViewpager2.setOffscreenPageLimit(3);
        MainBannerViewpager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(10));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });
        //page의 offset을 설정해야하는데
        //현재 보이는 screen width에서 page간의 margin값을 빼주고
        //pager width 값을 빼면 offsetPx를 구할 수 있습니다.
//        MainBannerViewpager2.setPageTransformer(compositePageTransformer);
//        MainBannerViewpager2.setPageTransformer(new MarginPageTransformer(40));

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());
        //디스플레이 값을 기준으로 페이지 마진 구하기
        MainBannerViewpager2.setPageTransformer(new MarginPageTransformer((int) (size.x * 0.06)));


//        MainBannerViewpager2.setCurrentItem(10,false);



//        MainBannerViewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
////                Log.e("pos",String.valueOf(position));
//            }
//        });
    }

    //xml크기를 동적으로 변환
    private void setsize() {
        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다.
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize(getActivity());

        //상단 타이틀
        MainTop.getLayoutParams().height = (int) (size.y * 0.14);
        //뷰페이저 크기
        MainBannerViewpager2.getLayoutParams().height = (int) (size.y * 0.25);
        //닉네임 첫번째줄 텍스트
        Welfdata_first_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.025));
        //닉네임 두번째줄 텍스트
        Welfdata_second_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.025));
        //관심사 선택 카드뷰
        notlogin_card.getLayoutParams().height = (int) (size.y * 0.22);
        //맞춤 혜택 보여주기 레이아웃
        welfdata_layout.getLayoutParams().height = (int) (size.y * 0.47);
        //유튜버 혜택 리뷰 타이틀 레이아웃
        youtube_title_layout.getLayoutParams().height = (int) (size.y * 0.0415);
        //유튜브 혜택 타이틀 텍스트
        youtube_title_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) (size.y * 0.025));
        //유튜브 리사이클러뷰
        youtube_video_recyclerview.getLayoutParams().height = (int) (size.y * 0.3);
        //"나에게 맞는 혜택 찾기" 버튼 텍스트 크기
        notlogin_button.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.y * 0.021));
        //메인 타이틀 "혜택모아" 텍스트
        title_text.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) (size.y * 0.035));
    }

    //selected_interest_textview(제목) 값 넣기
    //room데이터 이용은 메인 쓰레드에서 하면 안된다
    void settitle(){
        new Thread(() -> {
            try{
                //Room을 쓰기위해 데이터베이스 객체 만들기
                AppDatabase database = Room.databaseBuilder(getActivity().getApplicationContext(), AppDatabase.class, "Firstcategory")
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
//                Log.e("age",alldata.get(0).age);
                benefit = age + ", " + local + ", " + gender;
            }catch(Exception e){
                e.printStackTrace();
            }

            //로그인 했는지 여부 확인하기위한 쉐어드
            app_pref = getActivity().getSharedPreferences(getString(R.string.shared_name), 0);
            boolean being_logout = app_pref.getBoolean("logout",true); //로그인 했는지 여부 확인하기
            String user_nickname = app_pref.getString("user_nickname",""); //닉네임 받아오기

            Message message = handler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("age", age);
            bundle.putString("benefit", benefit);
            bundle.putBoolean("being_logout",being_logout);
            bundle.putString("user_nickname", user_nickname);
            message.setData(bundle);
            //sendMessage가 되면 이 handler가 해당되는 핸들러객체가(ValueHandler) 자동으로 호출된다.
            handler.sendMessage(message);

        }).start();
    }

    //핸들러구현한 객체(핸들러역할), 스레드에서 저장한 타이틀 값을 사용하기 위한 핸들러
    class ValueHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String age = bundle.getString("age");
            String benefit = bundle.getString("benefit");
            String user_nickname = bundle.getString("user_nickname");
            boolean being_logout = bundle.getBoolean("being_logout");


            if(!being_logout){ //로그인 했다면
                Welfdata_first_title.setText(user_nickname + "님");
                notlogin_card.setVisibility(View.GONE);
                welfdata_layout.setVisibility(View.VISIBLE);
            } else if(age != null){ //미리보기 관심사를 선택했다면
                Welfdata_first_title.setText(benefit);
                notlogin_card.setVisibility(View.GONE);
                welfdata_layout.setVisibility(View.VISIBLE);
            } else { //비로그인 + 관심사 선택 X
                notlogin_card.setVisibility(View.VISIBLE);
                welfdata_layout.setVisibility(View.GONE);
            }
        }
    }

    /* MVVM 디자인 패턴으로 바꾼 후 추가한 메서드
     * MVVM에 맞게 Observer와 뷰모델을 사용해 서버에서 결과값을 가져온 다음 파싱해 뷰에 붙인다 */
    private void showWelfareAndYoutubeNotLogin()
    {

        //서버로부터 데이터를 받아오는데 걸리는 시간동안 보여줄 프로그래스 바
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMax(100);
        dialog.setMessage("잠시만 기다려 주세요...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        final Observer<String> mainObserver = new Observer<String>()
        {
            @Override
            public void onChanged(String str)
            {
                if (str != null)
                {
                    responseParse(str);
//                    Log.e(TAG,"str : " + str);
                }
                else
                {
                    Log.e(TAG, "str(결과값)이 null입니다");
                }

                dialog.dismiss(); //서버 연결후에 프로그래스바 숨기기
            }
        };
        mainViewModel.getAllData().observe(getActivity(), mainObserver);
    }

    private void responseParse(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("message");
//            Log.e(TAG,"jsonArray : " + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject inner_json = jsonArray.getJSONObject(i);

//                Log.e(TAG, "data : " + inner_json);
                /* 유튜브 데이터 */
                JSONArray arr = inner_json.getJSONArray("youtube");
                for (int j = 0; j < arr.length(); j++)
                {

                    JSONObject final_json = arr.getJSONObject(j);
                    youtube_id = final_json.getString("id");
                    youtube_title = final_json.getString("title");
                    youtube_thumbnail = final_json.getString("thumbnail");
                    youtube_videoId = final_json.getString("videoId");

                    HorizontalYoutubeItem item = new HorizontalYoutubeItem();
                    item.setYoutube_id(youtube_id);
                    item.setYoutube_name(youtube_title);
                    item.setYoutube_thumbnail(youtube_thumbnail);
                    item.setYoutube_videoId(youtube_videoId);
                    youtube_list.add(item);

                }


                /* 메인에 보여줄 3개 혜택 데이터 */
                JSONArray welf_datas = inner_json.getJSONArray("welf_data");
                for (int j = 0; j < welf_datas.length(); j++)
                {
                    JSONObject data = welf_datas.getJSONObject(j);
                    welfare_id = data.getString("welf_id");
                    welfare_name = data.getString("welf_name");
                    welfare_tag = data.getString("welf_tag");
                    welfare_viewcount = data.getString("welf_count");

                    /* MainCategoryItem : 상단 리사이클러뷰의 리스트에 넣을 모델 클래스
                     * MainCategoryBottomItem : 하단 리사이클러뷰의 리스트에 넣을 모델 클래스 */

                    // 상단 리사이클러뷰에 넣을 테마(welf_field)를 저장할 객체 생성
                    MainThreeDataItem item = new MainThreeDataItem();
                    item.setWelf_id(welfare_id);
                    item.setWelf_name(welfare_name);
                    item.setWelf_count(welfare_viewcount);
                    item.setWelf_tag(welfare_tag);

//                    // 하단 리사이클러뷰에 넣을 값들
//                    MainCategoryBottomItem bottomItem = new MainCategoryBottomItem();
//                    bottomItem.setWelf_id(welfare_id);
//                    bottomItem.setWelf_name(welfare_name);
//                    bottomItem.setWelf_count(welfare_viewcount);
//                    bottomItem.setWelf_tag(welfare_tag);

//                    keyword_list.add(item);
                    down_list.add(item);
                    downAdapter.notifyDataSetChanged();

                }



                /* 배너에 보여줄 데이터 */
                JSONArray banner = inner_json.getJSONArray("banner");
                for (int k = 0; k < banner.length(); k++)
                {
//                    Log.e(TAG,"banner : " + banner);
                    JSONObject banner_json = banner.getJSONObject(k);
                    banner_image = banner_json.getString("img_url"); //배너 이미지
                    banner_title = banner_json.getString("banner_text"); //배너 타이틀

                    MainBannerData bannerData = new MainBannerData();
                    bannerData.setImageurl(banner_image);
                    bannerData.setTitle(banner_title);

                    bannerList.add(bannerData);
                    bannerAdapter.notifyDataSetChanged();

//                    Log.e(TAG,"banner_image : " + banner_image);
//                    Log.e(TAG,"banner_title : " + banner_title);
                }

            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }














//        /* 유튜브 해시맵에 영상 이름과 videoId 값을 넣어서 인텐트로 넘길 준비를 한다 */
//        for (int i = 0; i < youtube_list.size(); i++)
//        {
//            youtube_hashmap.put(youtube_list.get(i).getYoutube_name(), youtube_list.get(i).getYoutube_videoId());
//        }











//        /* 상단 리사이클러뷰에 들어갈 테마(교육, 공부 등)의 중복처리 로직 시작 */
//        // ArrayList 안의 데이터를 중복처리하기 위해 먼저 StringBuilder에 넣는다
//        StringBuilder stringBuilder = new StringBuilder();
//        StringBuilder welfareNameBuilder = new StringBuilder();
//        StringBuilder welfareFieldBuilder = new StringBuilder();
//        StringBuilder welfareTagBuilder = new StringBuilder();
//
//        /* 상단 리사이클러뷰에 쓰는 리스트의 크기만큼 반복해서 'OO 지원' 사이사이에 ';;'을 붙인다 */
//
//        for (int i = 0; i < keyword_list.size(); i++)
//        {
//            stringBuilder.append(keyword_list.get(i).getWelf_field()).append(";;");
//            welfareTagBuilder.append(keyword_list.get(i).getWelf_tag()).append(";;");
//        }
//
////        Log.e(TAG,"getWelf_field" + stringBuilder.toString());
////        Log.e(TAG,"getWelf_tag" + welfareTagBuilder.toString());
//
//        /* 하단 리사이클러뷰에 쓰는 리스트 크기만큼 반복해 혜택명 사이에 ';;'을 붙인다 */
//
//        for (int i = 0; i < down_list.size(); i++)
//        {
//            welfareNameBuilder.append(down_list.get(i).getWelf_name()).append(";;");
//            welfareFieldBuilder.append(down_list.get(i).getWelf_field()).append(";;");
//        }
//
////        Log.e(TAG,"stringBuilder" + stringBuilder.toString());
//
//        // ";;"가 섞인 문자열을 구분자로 각각 split
//        String[] nameArr = welfareNameBuilder.toString().split(";;");
//        String[] fieldArr = welfareFieldBuilder.toString().split(";;");
//        String[] tagArr = welfareTagBuilder.toString().split(";;");
//
//        // 중복되는 것들을 없애기 위해 HashSet 사용
//        nameArr = new LinkedHashSet<>(Arrays.asList(nameArr)).toArray(new String[0]);
//        fieldArr = new HashSet<>(Arrays.asList(fieldArr)).toArray(new String[0]);
//
//        keyword_list.clear();
//
//        for (int i = 0; i < fieldArr.length; i++)
//        {
//            all_item = new MainThreeDataItem();
//            MainThreeDataItem item = new MainThreeDataItem();
//            item.setWelf_field(fieldArr[i]);
//            item.setWelf_name(nameArr[i]);
//            item.setWelf_tag(tagArr[i]);
//            all_item.setWelf_field(fieldArr[i]);
//            all_item.setWelf_name(nameArr[i]);
//            all_item.setWelf_tag(tagArr[i]);
//            keyword_list.add(item);
//            other_list.add(all_item);
//
////            Log.e(TAG,"nameArr : " + nameArr[i]);
////            Log.e(TAG,"tagArr : " + tagArr[i]);
////            Log.e(TAG,"-------------------------");
//        }
//
//        // 아래 처리를 하지 않으면 이 액티비티로 들어올 때마다 전체 카테고리 개수가 1개씩 증가한다
//        // keyword_list 크기가 0일 경우 아래에서 에러가 발생한다
//        if (!keyword_list.get(0).getWelf_field().equals("전체") && !keyword_list.contains("전체"))
//        {
//            keyword_list.add(0, new MainThreeDataItem("전체"));
//        }






//        downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
//        downAdapter.setOnItemClickListener((view, pos) ->
//        {
//            Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
//            intent.putExtra("welf_id", welfare_id);
//            intent.putExtra("being_id",true);
//            Log.e(TAG,"pos : " + pos);
//            Log.e(TAG,"id : " + down_list.get(pos).getWelf_id());
//            Log.e(TAG,"name : " + down_list.get(pos).getWelf_name());
//
//            startActivity(intent);
//        });



//        downAdapter = new MainDownAdapter(getActivity(), other_list, downClickListener);
//        downAdapter.setOnItemClickListener((view, pos) ->
//        {
//            Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
//            intent.putExtra("welf_id", welfare_id);
//            intent.putExtra("being_id",true);
//            startActivity(intent);
//        });
//        MainWelfdata.setAdapter(downAdapter);











//        // 상단 리사이클러뷰에 붙일 어댑터 초기화
//        upAdapter = new MainCategoryAdapter(getActivity(), keyword_list, up_clickListener);
//        upAdapter.setOnItemClickListener(((view, pos) ->
//        {
//            /* 상단 리사이클러뷰에서 전체를 클릭한 경우
//             * 메서드를 한번 더 호출하지 않고 랜덤하게 3개의 데이터를 뽑아서 하단 리사이클러뷰에 데이터를 set해야 한다 */
//            if (keyword_list.get(pos).getWelf_field().equals("전체"))
//            {
//                // 전체 필터를 눌렀을 때는 모든 데이터가 들어있는 리스트를 어댑터 초기화 시 넘겨줌으로써 더보기를 눌러도 모든 데이털르 볼 수 있도록 한다
//                downAdapter = new MainDownAdapter(getActivity(), down_list, downClickListener);
//                downAdapter.setOnItemClickListener((view1, pos1) ->
//                {
//                    Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
//                    intent.putExtra("welf_id",welfare_id);
//                    intent.putExtra("being_id",true);
//                    startActivity(intent);
//                });
//                MainWelfdata.setAdapter(downAdapter);
//            }
//            else
//            {
//                /* 상단 리사이클러뷰에서 전체 이외의 필터를 클릭한 경우
//                 * 여기서 선택한 'OO 지원'에 맞는 혜택들로 하단 리사이클러뷰 어댑터 초기화 시 사용되는 리스트를 채워야 한다 */
//                other_list.clear();
//
//                /* 선택한 OO 지원에 속하는 혜택들만을 리사이클러뷰에 보여주는 처리 시작 */
//                // 현금 지원을 선택했으면 전체 데이터가 담긴 리스트에서 welf_category가 현금 지원인 혜택들을 리스트에 담아서 하단 리사이클러뷰 어댑터에 넣어야 한다
//                for (int i = 0; i < down_list.size(); i++)
//                {
//                    MainThreeDataItem item = new MainThreeDataItem();
//                    if (down_list.get(i).getWelf_field().contains(keyword_list.get(pos).getWelf_field()))
//                    {
//                        item.setWelf_field(keyword_list.get(pos).getWelf_field());
//                        item.setWelf_name(down_list.get(i).getWelf_name());
//                        item.setWelf_tag(down_list.get(i).getWelf_tag());
//                        other_list.add(item);
//                    }
//                }
//
//                // 하단 리사이클러뷰에 어댑터 set
//                downAdapter = new MainDownAdapter(getActivity(), other_list, downClickListener);
//                downAdapter.setOnItemClickListener((view1, pos1) ->
//                {
//                    Intent intent = new Intent(getActivity(), DetailTabLayoutActivity.class);
//                    intent.putExtra("welf_id",welfare_id);
//                    intent.putExtra("being_id",true);
//                    startActivity(intent);
//                });
//                MainWelfdata.setAdapter(downAdapter);
//            }
//        }));
//        up_recycler.setAdapter(upAdapter);



        youtubeAdapter = new MainHorizontalYoutubeAdapter(getActivity(), youtube_list, youtubeClickListener);
        youtubeAdapter.setOnItemClickListener(new MainHorizontalYoutubeAdapter.ItemClickListener()
        {
            //유튜브 썸네일 클릭 이벤트
            @Override
            public void onItemClick(View v, int pos)
            {
                /* 수정 중이라 주석 처리 */
                HorizontalYoutubeItem item = new HorizontalYoutubeItem();

                item.setYoutube_id(youtube_list.get(pos).getYoutube_id());
                item.setYoutube_name(youtube_list.get(pos).getYoutube_name());
                item.setYoutube_thumbnail(youtube_list.get(pos).getYoutube_thumbnail());
                item.setYoutube_videoId(youtube_list.get(pos).getYoutube_videoId());
                item.setYoutube_id(youtube_list.get(pos).getYoutube_id());
//                item.setYoutube_id(youtube_id);
//                item.setYoutube_name(youtube_title);
//                item.setYoutube_thumbnail(youtube_thumbnail);
//                item.setYoutube_videoId(youtube_videoId);
                Intent intent = new Intent(getActivity(), YoutubeActivity.class);
//                String youtube_name = youtube_list.get(pos).getYoutube_name();
//                Log.e(TAG, "선택한 유튜브 영상 : " + youtube_list.get(pos).getYoutube_name());
                intent.putExtra("youtube_information", item);
//                intent.putExtra("youtube_hashmap", youtube_hashmap);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                startActivity(intent);

            }

            //유튜브 더보기 버튼 클릭 이벤트
            @Override
            public void moreviewItemClick(View v, int pos)
            {
                Intent intent = new Intent(getActivity(), YoutubeMoreActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "유튜브 더보기 화면으로 이동");
                analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
                startActivity(intent);
            }
        });
        youtube_video_recyclerview.setAdapter(youtubeAdapter);

    }

}