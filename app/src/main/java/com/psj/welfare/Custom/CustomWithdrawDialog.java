package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.psj.welfare.R;

public class CustomWithdrawDialog
{
    private Context context;
    private MyWithdrawListener listener;

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
        dialog.getWindow().setAttributes(params);
        dialog.show();

        final RadioGroup radioGroup = dialog.findViewById(R.id.withdraw_radiogroup);
        final RadioButton first_reason = dialog.findViewById(R.id.first_reason);
        final RadioButton second_reason = dialog.findViewById(R.id.second_reason);
        final RadioButton third_reason = dialog.findViewById(R.id.third_reason);
        final RadioButton fourth_reason = dialog.findViewById(R.id.fourth_reason);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int id)
            {
                switch (id)
                {
                    // TODO : 라디오 버튼 클릭 시 다이얼로그를 없애고 선택한 값을 액티비티로 보내야 한다. custom > MyDialogListener 참고
                    // 다이얼로그 클릭 시 ripple이 너무 크고 다이얼로그 크기도 작다
                    case R.id.first_reason :
                        listener.sendFirstValue(first_reason.getText().toString());
                        dialog.dismiss();
                        break;

                    case R.id.second_reason :
                        listener.sendSecondValue(second_reason.getText().toString());
                        dialog.dismiss();
                        break;

                    case R.id.third_reason :
                        listener.sendThirdValue(third_reason.getText().toString());
                        dialog.dismiss();
                        break;

                    case R.id.fourth_reason :
                        listener.sendFourthValue(fourth_reason.getText().toString());
                        dialog.dismiss();
                        break;

                    default:
                        break;
                }
            }
        });

    }

}
