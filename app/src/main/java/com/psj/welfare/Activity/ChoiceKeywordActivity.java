package com.psj.welfare.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psj.welfare.R;
import com.psj.welfare.adapter.KeywordAdapter;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.data.KeywordItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChoiceKeywordActivity extends AppCompatActivity
{
    private final String TAG = this.getClass().getSimpleName();

    Button buttons[] = new Button[113];
    Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, button11, button12, button13, button14,
            button15, button16, button17, button18, button19, button20, button21, button22, button23, button24, button25, button26, button27, button28,
            button29, button30, button31, button32, button33, button34, button35, button36, button37, button38, button39, button40, button41, button42,
            button43, button44, button45, button46, button47, button48, button49, button50, button51, button52, button53, button54, button55, button56,
            button57, button58, button59, button60, button61, button62, button63, button64, button65, button66, button67, button68, button69, button70,
            button71, button72, button73, button74, button75, button76, button77, button78, button79, button80, button81, button82, button83, button84,
            button85, button86, button87, button88, button89, button90, button91, button92, button93, button94, button95, button96, button97, button98,
            button99, button100, button101, button102, button103, button104, button105, button106, button107, button108, button109, button110, button111,
            button112, button113;

    private ScrollView buttons_scrollview;
    private LinearLayout zero_head_up_layout, first_head_up_layout, second_head_up_layout, third_head_up_layout, fourth_head_up_layout;

    private TextView next_keyword_textview;
    private TextView before_keyword_textview;
    private TextView current_page;
    int scrollY;
    private SharedPreferences sharedPreferences;

    String userInformation;

    List<String> keyword_list;

    int[] array_count = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    String[] keywords = {"10대", "1주택자", "가정위탁", "가정폭력", "가출", "검정고시", "고등학생", "고시원", "고용지원", "교통비", "국내 여행", "권고사직",
            "근로", "근로자", "금융 교육", "기초생활수급자", "기초연금", "노인", "뇌병변", "다문화", "다자녀", "대출", "대학생", "독거노인", "돌봄", "맞벌이",
            "무공훈장", "무상교육", "무주택자", "문화 생활", "미혼모", "방과후", "범죄피해", "법률상담", "보육료", "보험", "보호처분", "보훈", "부부", "분유",
            "사회 초년생", "산업재해", "생계", "서민금융", "성범죄", "성인", "성폭력", "소년소녀가정", "소상공인", "신용 등급", "신혼부부", "암", "어린이집",
            "언어", "여성", "연금", "영유아", "요금", "우울증", "월세", "유공자", "육아", "육아휴직", "의료비", "이주민", "임신", "자녀", "자살", "자영업자",
            "자퇴", "자폐", "장려금", "장애인", "장학금", "장학생", "저소득층", "저축통장", "적금", "전문대", "전세대출", "제대군인", "조손가정", "중소기업",
            "중장년", "중학생", "지적", "지체", "직업훈련", "직장인", "진로", "쪽방", "창업", "채무", "청년", "체육활동", "출산", "취업", "치료", "치매",
            "컨설팅", "탈북", "통신비", "퇴학", "특수교육", "폐업", "학비", "학자금", "한부모", "한부모가족", "해외", "현금", "형사처분", "휴업"};

    String str_server;
    String message;
    List<String> str_list;

    private Button send_user_interest_btn;

    private Menu mOptionMenu;

    String encode_str;

    private RecyclerView selected_keyword_recycler;
    private KeywordAdapter adapter;
    private KeywordAdapter.onItemClickListener itemClickListener;
    private List<KeywordItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(ChoiceKeywordActivity.this);
        setContentView(R.layout.activity_choice_keyword);

        Toolbar choice_toolbar = findViewById(R.id.choice_toolbar);
        setSupportActionBar(choice_toolbar);
        getSupportActionBar().setTitle("관심사 선택");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        next_keyword_textview = findViewById(R.id.next_keyword_textview);
        before_keyword_textview = findViewById(R.id.before_keyword_textview);

        keyword_list = new ArrayList<>();
        items = new ArrayList<>();
        str_list = new ArrayList<>();

        sharedPreferences = getSharedPreferences("app_pref", 0);

        init();
        init2();

        selected_keyword_recycler = findViewById(R.id.selected_keyword_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        selected_keyword_recycler.setLayoutManager(linearLayoutManager);

        next_keyword_textview.setOnClickListener(v ->
        {
            if (buttons_scrollview.getScrollY() == 0)
            {
                before_keyword_textview.setText("< 이전");
                before_keyword_textview.setVisibility(View.VISIBLE);
                buttons_scrollview.smoothScrollTo(0, 1201);
                current_page.setText("2");
            }
            else if (buttons_scrollview.getScrollY() == 1201)
            {
                buttons_scrollview.smoothScrollTo(0, 2624);
                current_page.setText("3");
            }
            else if (buttons_scrollview.getScrollY() == 2624)
            {
                buttons_scrollview.smoothScrollTo(0, 3493);
                current_page.setText("4");
                next_keyword_textview.setVisibility(View.INVISIBLE);
            }
        });

        before_keyword_textview.setOnClickListener(view -> {
            if (buttons_scrollview.getScrollY() == 3493)
            {
                next_keyword_textview.setVisibility(View.VISIBLE);
                buttons_scrollview.smoothScrollTo(0, 2624);
                current_page.setText("3");
            }
            else if (buttons_scrollview.getScrollY() == 2624)
            {
                buttons_scrollview.smoothScrollTo(0, 1201);
                current_page.setText("2");
            }
            else if (buttons_scrollview.getScrollY() == 1201)
            {
                buttons_scrollview.smoothScrollTo(0, 0);
                current_page.setText("1");
                before_keyword_textview.setVisibility(View.INVISIBLE);
            }
        });

        adapter = new KeywordAdapter(ChoiceKeywordActivity.this, items, itemClickListener);
        adapter.setOnItemClickListener((view, position) ->
        {
            for (int i = 0; i < keywords.length; i++)
            {
                if (items.get(position).getName().equals(keywords[i]))
                {
                    array_count[i] = array_count[i] + 1;

                    String bottom_name = items.get(position).getName();
                    buttons[i].setBackground(ContextCompat.getDrawable(this, R.drawable.btn_keyword_unselected));
                    buttons[i].setTextColor(ContextCompat.getColor(this, R.color.layout_background_start_gradation));
                    keyword_list.remove(bottom_name);
                }
            }
        });
        selected_keyword_recycler.setAdapter(adapter);

        send_user_interest_btn.setOnClickListener(v -> {
            if (items.size() == 0)
            {
                Toast.makeText(this, "관심사는 최소 1개 이상 선택해주세요!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                for (int i = 0; i < items.size(); i++)
                {
                    str_list.add(items.get(i).getName());
                }
                if (str_list.size() > 0)
                {
                    registerUserInterest();
                }
            }
        });
    }

    void init()
    {
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button10 = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);
        button12 = findViewById(R.id.button12);
        button13 = findViewById(R.id.button13);
        button14 = findViewById(R.id.button14);
        button15 = findViewById(R.id.button15);
        button16 = findViewById(R.id.button16);
        button17 = findViewById(R.id.button17);
        button18 = findViewById(R.id.button18);
        button19 = findViewById(R.id.button19);
        button20 = findViewById(R.id.button20);
        button21 = findViewById(R.id.button21);
        button22 = findViewById(R.id.button22);
        button23 = findViewById(R.id.button23);
        button24 = findViewById(R.id.button24);
        button25 = findViewById(R.id.button25);
        button26 = findViewById(R.id.button26);
        button27 = findViewById(R.id.button27);
        button28 = findViewById(R.id.button28);
        button29 = findViewById(R.id.button29);
        button30 = findViewById(R.id.button30);
        button31 = findViewById(R.id.button31);
        button32 = findViewById(R.id.button32);
        button33 = findViewById(R.id.button33);
        button34 = findViewById(R.id.button34);
        button35 = findViewById(R.id.button35);
        button36 = findViewById(R.id.button36);
        button37 = findViewById(R.id.button37);
        button38 = findViewById(R.id.button38);
        button39 = findViewById(R.id.button39);
        button40 = findViewById(R.id.button40);
        button41 = findViewById(R.id.button41);
        button42 = findViewById(R.id.button42);
        button43 = findViewById(R.id.button43);
        button44 = findViewById(R.id.button44);
        button45 = findViewById(R.id.button45);
        button46 = findViewById(R.id.button46);
        button47 = findViewById(R.id.button47);
        button48 = findViewById(R.id.button48);
        button49 = findViewById(R.id.button49);
        button50 = findViewById(R.id.button50);
        button51 = findViewById(R.id.button51);
        button52 = findViewById(R.id.button52);
        button53 = findViewById(R.id.button53);
        button54 = findViewById(R.id.button54);
        button55 = findViewById(R.id.button55);
        button56 = findViewById(R.id.button56);
        button57 = findViewById(R.id.button57);
        button58 = findViewById(R.id.button58);
        button59 = findViewById(R.id.button59);
        button60 = findViewById(R.id.button60);
        button61 = findViewById(R.id.button61);
        button62 = findViewById(R.id.button62);
        button63 = findViewById(R.id.button63);
        button64 = findViewById(R.id.button64);
        button65 = findViewById(R.id.button65);
        button66 = findViewById(R.id.button66);
        button67 = findViewById(R.id.button67);
        button68 = findViewById(R.id.button68);
        button69 = findViewById(R.id.button69);
        button70 = findViewById(R.id.button70);
        button71 = findViewById(R.id.button71);
        button72 = findViewById(R.id.button72);
        button73 = findViewById(R.id.button73);
        button74 = findViewById(R.id.button74);
        button75 = findViewById(R.id.button75);
        button76 = findViewById(R.id.button76);
        button77 = findViewById(R.id.button77);
        button78 = findViewById(R.id.button78);
        button79 = findViewById(R.id.button79);
        button80 = findViewById(R.id.button80);
        button81 = findViewById(R.id.button81);
        button82 = findViewById(R.id.button82);
        button83 = findViewById(R.id.button83);
        button84 = findViewById(R.id.button84);
        button85 = findViewById(R.id.button85);
        button86 = findViewById(R.id.button86);
        button87 = findViewById(R.id.button87);
        button88 = findViewById(R.id.button88);
        button89 = findViewById(R.id.button89);
        button90 = findViewById(R.id.button90);
        button91 = findViewById(R.id.button91);
        button92 = findViewById(R.id.button92);
        button93 = findViewById(R.id.button93);
        button94 = findViewById(R.id.button94);
        button95 = findViewById(R.id.button95);
        button96 = findViewById(R.id.button96);
        button97 = findViewById(R.id.button97);
        button98 = findViewById(R.id.button98);
        button99 = findViewById(R.id.button99);
        button100 = findViewById(R.id.button100);
        button101 = findViewById(R.id.button101);
        button102 = findViewById(R.id.button102);
        button103 = findViewById(R.id.button103);
        button104 = findViewById(R.id.button104);
        button105 = findViewById(R.id.button105);
        button106 = findViewById(R.id.button106);
        button107 = findViewById(R.id.button107);
        button108 = findViewById(R.id.button108);
        button109 = findViewById(R.id.button109);
        button110 = findViewById(R.id.button110);
        button111 = findViewById(R.id.button111);
        button112 = findViewById(R.id.button112);
        button113 = findViewById(R.id.button113);

        buttons[0] = button1;
        buttons[1] = button2;
        buttons[2] = button3;
        buttons[3] = button4;
        buttons[4] = button5;
        buttons[5] = button6;
        buttons[6] = button7;
        buttons[7] = button8;
        buttons[8] = button9;
        buttons[9] = button10;
        buttons[10] = button11;
        buttons[11] = button12;
        buttons[12] = button13;
        buttons[13] = button14;
        buttons[14] = button15;
        buttons[15] = button16;
        buttons[16] = button17;
        buttons[17] = button18;
        buttons[18] = button19;
        buttons[19] = button20;
        buttons[20] = button21;
        buttons[21] = button22;
        buttons[22] = button23;
        buttons[23] = button24;
        buttons[24] = button25;
        buttons[25] = button26;
        buttons[26] = button27;
        buttons[27] = button28;
        buttons[28] = button29;
        buttons[29] = button30;
        buttons[30] = button31;
        buttons[31] = button32;
        buttons[32] = button33;
        buttons[33] = button34;
        buttons[34] = button35;
        buttons[35] = button36;
        buttons[36] = button37;
        buttons[37] = button38;
        buttons[38] = button39;
        buttons[39] = button40;
        buttons[40] = button41;
        buttons[41] = button42;
        buttons[42] = button43;
        buttons[43] = button44;
        buttons[44] = button45;
        buttons[45] = button46;
        buttons[46] = button47;
        buttons[47] = button48;
        buttons[48] = button49;
        buttons[49] = button50;
        buttons[50] = button51;
        buttons[51] = button52;
        buttons[52] = button53;
        buttons[53] = button54;
        buttons[54] = button55;
        buttons[55] = button56;
        buttons[56] = button57;
        buttons[57] = button58;
        buttons[58] = button59;
        buttons[59] = button60;
        buttons[60] = button61;
        buttons[61] = button62;
        buttons[62] = button63;
        buttons[63] = button64;
        buttons[64] = button65;
        buttons[65] = button66;
        buttons[66] = button67;
        buttons[67] = button68;
        buttons[68] = button69;
        buttons[69] = button70;
        buttons[70] = button71;
        buttons[71] = button72;
        buttons[72] = button73;
        buttons[73] = button74;
        buttons[74] = button75;
        buttons[75] = button76;
        buttons[76] = button77;
        buttons[77] = button78;
        buttons[78] = button79;
        buttons[79] = button80;
        buttons[80] = button81;
        buttons[81] = button82;
        buttons[82] = button83;
        buttons[83] = button84;
        buttons[84] = button85;
        buttons[85] = button86;
        buttons[86] = button87;
        buttons[87] = button88;
        buttons[88] = button89;
        buttons[89] = button90;
        buttons[90] = button91;
        buttons[91] = button92;
        buttons[92] = button93;
        buttons[93] = button94;
        buttons[94] = button95;
        buttons[95] = button96;
        buttons[96] = button97;
        buttons[97] = button98;
        buttons[98] = button99;
        buttons[99] = button100;
        buttons[100] = button101;
        buttons[101] = button102;
        buttons[102] = button103;
        buttons[103] = button104;
        buttons[104] = button105;
        buttons[105] = button106;
        buttons[106] = button107;
        buttons[107] = button108;
        buttons[108] = button109;
        buttons[109] = button110;
        buttons[110] = button111;
        buttons[111] = button112;
        buttons[112] = button113;
    }

    void init2()
    {
        zero_head_up_layout = findViewById(R.id.zero_head_up_layout);
        first_head_up_layout = findViewById(R.id.first_head_up_layout);
        second_head_up_layout = findViewById(R.id.second_head_up_layout);
        third_head_up_layout = findViewById(R.id.third_head_up_layout);
        fourth_head_up_layout = findViewById(R.id.fourth_head_up_layout);

        buttons_scrollview = findViewById(R.id.buttons_scrollview);
        current_page = findViewById(R.id.current_page);

        send_user_interest_btn = findViewById(R.id.send_user_interest_btn);
    }

    void registerUserInterest()
    {
        String token = sharedPreferences.getString("token", "");
        String session = sharedPreferences.getString("sessionId", "");
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Log.e(TAG, "str_list = " + str_list);

        StringBuilder sb = new StringBuilder();
        for (String s : str_list)
        {
            sb.append(s);
            sb.append("|");
        }

        if (sb.toString().length() > 0 && sb.toString().charAt(sb.toString().length() - 1) == '|')
        {
            str_server = sb.toString().substring(0, sb.toString().length() - 1);
        }
        encode("관심사 선택 확인에서 관심사 선택 : " + str_server);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("interest", str_server);
        editor.apply();
        Call<String> call = apiInterface.registerUserInterest(session, encode_str, token, "interest", str_server);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "성공 : " + response.body());
                    jsonParsing(result);
                }
                else
                {
                    Log.e(TAG, "실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "에러 : " + t.getMessage());
            }
        });
    }

    private void encode(String str)
    {
        try
        {
            encode_str = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    private void jsonParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("Message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        Log.e(TAG, message);
        Toast.makeText(this, "관심사 설정이 완료됐어요", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ChoiceKeywordActivity.this, MainTabLayoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mOptionMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_keyword_menu, menu);

        MenuItem liveitem = mOptionMenu.findItem(R.id.keyword_ok);
        SpannableString spannableString = new SpannableString(liveitem.getTitle().toString());
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryDark)), 0, spannableString.length(), 0);
        liveitem.setTitle(spannableString);
        return true;
    }

    // 좌상단 뒤로가기 버튼을 누르면 사용자 개인정보(나이, 성별, 지역, 닉네임)을 입력받는 화면으로 돌아간다
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceKeywordActivity.this);
                builder.setMessage("지금 나가시면 선택하셨던 관심사들은 저장되지 않아요. 그래도 나가시겠어요?")
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("예", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
                return true;

            case R.id.keyword_ok:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(ChoiceKeywordActivity.this);
                builder2.setMessage("지금 나가시면 선택하셨던 관심사들은 저장되지 않아요. 그래도 나가시겠어요?")
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("예", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        for (int i = 0; i < 113; i++)
        {
            final int finalI = i;
            buttons[finalI].setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    String name = buttons[finalI].getText().toString();
                    array_count[finalI] = array_count[finalI] + 1;
                    if (array_count[finalI] % 2 == 0)
                    {
                        buttons[finalI].setBackground(ContextCompat.getDrawable(ChoiceKeywordActivity.this, R.drawable.btn_keyword_unselected));
                        buttons[finalI].setTextColor(ContextCompat.getColor(ChoiceKeywordActivity.this, R.color.layout_background_start_gradation));
                        for (int i = 0; i < items.size(); i++)
                        {
                            if (items.get(i).getName().equals(keywords[finalI]))
                            {
                                items.remove(i);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    else
                    {
                        buttons[finalI].setBackground(ContextCompat.getDrawable(ChoiceKeywordActivity.this, R.drawable.btn_keyword_selected));
                        buttons[finalI].setTextColor(ContextCompat.getColor(ChoiceKeywordActivity.this, R.color.colorMainWhite));

                        KeywordItem item = new KeywordItem();
                        item.setName(name);
                        items.add(item);

                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public void setStatusBarGradiant(Activity activity)
    {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.gradation_background);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(background);
    }

}