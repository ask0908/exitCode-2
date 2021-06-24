package com.psj.welfare.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.psj.welfare.R;
import com.psj.welfare.ScreenSize;

/* 회원탈퇴 다이얼로그. "선택해 주세요"를 누르면 나오는 라디오 버튼 다이얼로그다 */
public class CustomWithdrawDialog
{
    private Context context;
    private MyWithdrawListener listener;
    private SharedPreferences sharedPreferences;
//    private ConstraintLayout withdraw_dialog; //다이얼로그 전체 레이아웃

    public void setOnWithdrawListener(MyWithdrawListener listener)
    {
        this.listener = listener;
    }

    public CustomWithdrawDialog(Context context)
    {
        this.context = context;
    }

    public void showDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.withdraw_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params); //다이얼로그 크기를 정함
        dialog.show();

        ConstraintLayout withdraw_dialog = dialog.findViewById(R.id.withdraw_dialog); //다이얼로그 전체 레이아웃
        RadioGroup radioGroup = dialog.findViewById(R.id.withdraw_radiogroup);    // 라디오 버튼 사용 위한 라디오 그룹
        RadioButton first_reason = dialog.findViewById(R.id.first_reason);        // 나한테 맞는 혜택이 없어서
        RadioButton second_reason = dialog.findViewById(R.id.second_reason);      // 보는 게 어려워서
        RadioButton third_reason = dialog.findViewById(R.id.third_reason);        // 사용하기 불편해서
        RadioButton fourth_reason = dialog.findViewById(R.id.fourth_reason);      // 기타

        //size에 저장되는 가로/세로 길이의 단위는 픽셀(Pixel)입니다. (activity 화면 기준)
        ScreenSize screen = new ScreenSize();
        //context의 스크린 사이즈를 구함
        Point size = screen.getScreenSize((Activity) context);
        withdraw_dialog.getLayoutParams().height = (int) (size.y*0.37); //다이얼로그 전체 레이아웃 동적으로 크기

        // 라디오 버튼 글자 크기 조절
        first_reason.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        second_reason.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        third_reason.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        fourth_reason.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

        sharedPreferences = context.getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 선택한 탈퇴 사유는 다이얼로그를 다시 열면 선택된 상태로 놔둔다
        String reason = sharedPreferences.getString("reason", "");
        if (reason != null)
        {
            if (reason.equals(first_reason.getText().toString()))
            {
                first_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                second_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                third_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                fourth_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
            }
            else if (reason.equals(second_reason.getText().toString()))
            {
                second_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                first_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                third_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                fourth_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
            }
            else if (reason.equals(third_reason.getText().toString()))
            {
                third_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                first_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                second_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                fourth_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
            }
            else if (reason.equals(fourth_reason.getText().toString()))
            {
                fourth_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                first_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                second_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
                third_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_radiobutton_img));
            }
        }

        // 라디오 그룹 안에서 선택한 라디오 버튼이 무엇이냐에 따라 다른 값을 액티비티로 전달하기 위해 만든 콜백 리스너
        // 라디오 그룹에 콜백을 추가해야 라디오 그룹 안의 라디오 버튼 id에 따라 다른 콜백 메서드 호출이 가능
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int id)
            {
                switch (id)
                {
                    /* sendFirstValue(String value) : 1번 라디오 버튼 선택 시 거기에 적힌 문자열을 액티비티로 보내는 커스텀 콜백
                     * 1~4번 버튼 모두 이름만 다르고 같은 기능을 하는 콜백을 달았다 */
                    case R.id.first_reason:
                        listener.sendFirstValue(first_reason.getText().toString());
                        first_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                        editor.putString("reason", first_reason.getText().toString());
                        editor.apply();
                        dialog.dismiss();
                        break;

                    case R.id.second_reason:
                        listener.sendSecondValue(second_reason.getText().toString());
                        second_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                        editor.putString("reason", second_reason.getText().toString());
                        editor.apply();
                        dialog.dismiss();
                        break;

                    case R.id.third_reason:
                        listener.sendThirdValue(third_reason.getText().toString());
                        third_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                        editor.putString("reason", third_reason.getText().toString());
                        editor.apply();
                        dialog.dismiss();
                        break;

                    case R.id.fourth_reason:
                        listener.sendFourthValue(fourth_reason.getText().toString());
                        fourth_reason.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.withdraw_checked_radiobutton));
                        editor.putString("reason", fourth_reason.getText().toString());
                        editor.apply();
                        dialog.dismiss();
                        break;

                    default:
                        break;
                }
            }
        });

    }


}
