package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.psj.welfare.R;
import com.psj.welfare.api.ApiClient;
import com.psj.welfare.api.ApiInterface;
import com.psj.welfare.databinding.EditNicknameDialogBinding;
import com.psj.welfare.util.DBOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomEditNicknameDialog
{
    private final String TAG = this.getClass().getSimpleName();
    private String sqlite_token, message;
    boolean isDuplicated = true;
    DBOpenHelper helper;
    private MyDialogListener dialogListener;

    private Context context;
    private EditNicknameDialogBinding binding;

    public void setDialogListener(MyDialogListener dialogListener)
    {
        this.dialogListener = dialogListener;
    }

    public CustomEditNicknameDialog(Context context)
    {
        this.context = context;
        helper = new DBOpenHelper(context);
        helper.openDatabase();
        helper.create();
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

            binding.nicknameErase.setOnClickListener(v -> binding.editNicknameEdittext.setText(""));

            /* 중복확인 버튼 */
            binding.duplicateCheckBtn.setOnClickListener(v ->
            {
                // 중복확인 버튼을 누르면 아이디가 중복되는지 체크
                int length = binding.editNicknameEdittext.getText().toString().length();
                if (TextUtils.isEmpty(binding.editNicknameEdittext.getText().toString()) || length == 0 || length > 11)
                {
                    SpannableString spannableString = new SpannableString("닉네임은 2~10자 문자/숫자만 가능해요");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    binding.goodOrBadTextview.setText(spannableString);
                    binding.goodOrBadTextview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.goodOrBadTextview.setVisibility(View.GONE);
                    duplicationCheck(binding.editNicknameEdittext.getText().toString(), "check");
                    dialogListener.onDuplicatedCheck(!isDuplicated);
                    SpannableString spannableString = new SpannableString("사용할 수 있는 닉네임이에요");
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#99FF33")), 0, spannableString.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    binding.goodOrBadTextview.setText(spannableString);
                    binding.goodOrBadTextview.setVisibility(View.VISIBLE);
                }
            });

            binding.editNicknameCancel.setOnClickListener(v -> dialog.dismiss());

            // 닉네임 변경 버튼
            binding.editNicknameOk.setOnClickListener(v ->
            {
                String input = binding.editNicknameEdittext.getText().toString();
                Pattern unicodeOutliers = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
                // 이모티콘, 특수문자 들어왔는지 확인
                if (unicodeOutliers.matcher(input).matches())
                {
                    Log.e(TAG, "이모티콘이 입력됐어요");
                    cannotUseThis();
                }
                // 닉네임 변경 버튼을 누르면 다이얼로그에서 프래그먼트로 값을 넘긴다
                if (TextUtils.isEmpty(input))
                {
                    cannotUseThis();
                }
                else
                {
                    Log.e(TAG, "isDuplicated : " + isDuplicated);
                    if (!isDuplicated)
                    {
                        dialogListener.onPositiveClicked(input);
                        duplicationCheck(input, "save");
                        dialog.dismiss();
                    }
                    else
                    {
                        Toast.makeText(context, "중복확인을 먼저 해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
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
        binding.goodOrBadTextview.setVisibility(View.VISIBLE);
    }

    // 닉네임 중복 검사
    void duplicationCheck(String nickname, String type)
    {
        Cursor cursor = helper.selectColumns();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                sqlite_token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        Call<String> call = apiInterface.editNickname(sqlite_token, nickname, type);
        Log.e(TAG, "token : " + sqlite_token + ", 변경할 닉네임 : " + nickname + ", type : " + type);
        call.enqueue(new Callback<String>()
        {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response)
            {
                if (response.isSuccessful() && response.body() != null)
                {
                    String result = response.body();
                    Log.e(TAG, "닉네임 중복 확인 결과 : " + result);
                    duplicateParsing(result);
                }
                else
                {
                    Log.e(TAG, "닉네임 중복 확인 실패 : " + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t)
            {
                Log.e(TAG, "닉네임 중복 확인 에러 : " + t.getMessage());
            }
        });
    }

    private void duplicateParsing(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            message = jsonObject.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        isDuplicated = !message.equals("사용 가능한 닉네임 입니다.");
        if (message.equals("중복된 닉네임 입니다."))
        {
            binding.goodOrBadTextview.setText("");
            SpannableString spannableString = new SpannableString("다른 사용자가 이미 사용중이에요");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CC0033")), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.goodOrBadTextview.setText(spannableString);
            binding.goodOrBadTextview.setVisibility(View.VISIBLE);
        }
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
