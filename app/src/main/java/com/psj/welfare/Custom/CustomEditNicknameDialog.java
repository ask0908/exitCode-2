package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.orhanobut.logger.Logger;
import com.psj.welfare.R;
import com.psj.welfare.SharedSingleton;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.databinding.EditNicknameDialogBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomEditNicknameDialog
{
    private final String TAG = this.getClass().getSimpleName();
    private String message;
    private boolean isDuplicated = false; //중복 검사를 했는지 (true면 중복 검사를 했다)
//    DBOpenHelper helper;
    private MyDialogListener dialogListener;

    private Context context;
    private EditNicknameDialogBinding binding;

    //쉐어드 싱글톤
    private SharedSingleton sharedSingleton;
    //토큰
    private String token;

    // API 호출 후 서버 응답코드
    private int status_code;

    public void setDialogListener(MyDialogListener dialogListener)
    {
        this.dialogListener = dialogListener;
    }

    public CustomEditNicknameDialog(Context context)
    {
        this.context = context;
        sharedSingleton = SharedSingleton.getInstance(context);
        token = sharedSingleton.getToken();
//        helper = new DBOpenHelper(context);
//        helper.openDatabase();
//        helper.create();
    }

    public void showDialog()
    {
        final Dialog dialog = new Dialog(context);
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            /* 커스텀 다이얼로그에서 데이터 바인딩을 사용하려면 아래와 같이 한다
             * dialog.setContentView()의 인자로 binding.getRoot()를 넘겨야 커스텀 다이얼로그 레이아웃을 가져와 유저에게 보여줄 수 있다 */
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.edit_nickname_dialog, null, false);
            dialog.setContentView(binding.getRoot());
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
            dialog.setCancelable(false);

            // 최대 입력 가능 글자수 = 10
            binding.editNicknameEdittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            dialog.show();


            //입력값 한글,소문자,대문자만 받기
            binding.editNicknameEdittext.setFilters(new InputFilter[]{new InputFilter()
            {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
                {
                    // 이모티콘, 특수문자 입력 방지하는 정규식
                    // 둘 중 하나라도 입력되면 공백을 리턴한다
                    /**
                     * ^ : 패턴의 시작을 알리는 문자
                     * [] : 문자의 집합 or 범위 나타냄, 두 문자 사이는 "-"로 범위를 나타낸다. 이 안에 있는 문자 중 하나라도 해당되면 정규식과 매치된다
                     * [] 내부 : 한글, 영어, 숫자만 입력할 수 있게 하고 천지인 키보드의 .(middle dot)도 쓸 수 있도록 한다
                     * $ : 문자열(패턴)의 종료를 알리는 문자
                     * -> 입력되는 문자열의 시작부터 끝까지 한글, 영어, 숫자를 제외한 문자가 들어오면 공백을 리턴해서 아무것도 입력되지 않게 한다
                     */
                    Pattern pattern = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                    if (source.equals("") || pattern.matcher(source).matches())
                    {
                        return source;
                    }
                    return "";
                }
            }, new InputFilter.LengthFilter(10)});  // 닉네임 입력은 10자까지만 된다


            // edittext에 있는 닉네임 지우기 아이콘 클릭
            binding.nicknameErase.setOnClickListener(v -> {
                binding.editNicknameEdittext.setText("");
                //텍스트 꾸미기 -> SpannableString
                SpannableString spannableString = new SpannableString("문자/숫자만 가능 (2~10자리)");
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#757576")), 0, spannableString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                binding.goodOrBadTextview.setText(spannableString);

                isDuplicated = false; //중복 검사를 다시 해야 하기 때문에 false로!
            });


            /* 중복확인 버튼 */
            binding.duplicateCheckBtn.setOnClickListener(v ->
            {
                // 중복확인 버튼을 누르면 아이디가 중복되는지 체크
                int length = binding.editNicknameEdittext.getText().toString().length();
                if (TextUtils.isEmpty(binding.editNicknameEdittext.getText().toString()) || length < 2 || length > 11)
                {
                    //텍스트 꾸미기 -> SpannableString
                    SpannableString spannableString = new SpannableString("닉네임은 2~10자 문자/숫자만 가능해요");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    binding.goodOrBadTextview.setText(spannableString);
                }
                else
                {
                    //중복 확인 메서드
                    duplicationCheck(binding.editNicknameEdittext.getText().toString(), "check");
                }
            });

            // 취소 버튼
            binding.editNicknameCancel.setOnClickListener(v -> dialog.dismiss());

            // 닉네임 변경 버튼
            binding.editNicknameOk.setOnClickListener(v ->
            {
                String input = binding.editNicknameEdittext.getText().toString();
                Pattern unicodeOutliers = Pattern.compile("^[\\w]$");
                // 이모티콘, 특수문자 들어왔는지 확인
                if (unicodeOutliers.matcher(input).matches())
                {
                    cannotUseThis();
                }

                // 닉네임 변경 버튼을 누르면 다이얼로그에서 프래그먼트로 값을 넘긴다
               if (TextUtils.isEmpty(input))
                {
                    cannotUseThis();
                }
                else
                {
//                    Log.e(TAG, "isDuplicated : " + isDuplicated);
                    if (isDuplicated)
                    {
                        dialogListener.onPositiveClicked(input);
                        duplicationCheck(input, "save");
                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(context, "닉네임 중복확인을 해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            binding.editNicknameEdittext.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 중복확인 버튼을 누르면 아이디가 중복되는지 체크
                    int length = binding.editNicknameEdittext.getText().toString().length();
                    if (TextUtils.isEmpty(binding.editNicknameEdittext.getText().toString()) || length < 2 || length > 11)
                    {
                        //텍스트 꾸미기 -> SpannableString
                        SpannableString spannableString = new SpannableString("닉네임은 2~10자 문자/숫자만 가능해요");
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        binding.goodOrBadTextview.setText(spannableString);
                    }
                    else
                    {
                        //텍스트 꾸미기 -> SpannableString
                        SpannableString spannableString = new SpannableString("문자/숫자만 가능 (2~10자리)");
                        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#757576")), 0, spannableString.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        binding.goodOrBadTextview.setText(spannableString);
                    }

                    isDuplicated = false; //텍스트가 바뀌면 중복확인을 다시 해야한다
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

        }
    }

    // editText 밑의 문구를 에러 문구로 바꾸는 메서드
    private void cannotUseThis()
    {
        SpannableString spannableString = new SpannableString("닉네임은 2~10자 문자/숫자만 가능해요");
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.goodOrBadTextview.setText(spannableString);
    }


    // 닉네임 중복 검사
    void duplicationCheck(String nickname, String type)
    {
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.editNickname(token, nickname, type);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
//                Log.e(TAG,"response.body() : " + response.body());

                //서버에서 null값이 올 수도 있다
                if (response.isSuccessful() && response.body() != null)
                {

                    String result = response.body();
                    Logger.t("닉네임 중복 확인 결과 : ").json(result);
                    duplicateParsing(result);
                }
                else
                {
                    Logger.d("닉네임 중복 확인 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Logger.d("닉네임 중복 확인 에러 : " + t.getMessage());
            }
        });
    }

    private void duplicateParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("message");
            status_code = jsonObject.getInt("status_code");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


        //사용 가능한 닉네임 일 때
        if(status_code == 200){
            SpannableString spannableString = new SpannableString("사용할 수 있는 닉네임이에요");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#99FF33")), 0, spannableString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.goodOrBadTextview.setText(spannableString);

            isDuplicated = true; //중복 확인을 했다
        }
        //닉네임이 중복 됐을 때
        else if(status_code == 200){
            SpannableString spannableString = new SpannableString("현재 사용하고 있는 닉네임이에요");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.goodOrBadTextview.setText(spannableString);
        } else {
            Toast.makeText(context,"오류가 발생했습니다.",Toast.LENGTH_SHORT).show();
        }
    }

}
