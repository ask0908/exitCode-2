package com.benefit.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.benefit.welfare.R;

/* MainFragment에서 관심사를 선택하지 않고 '조회하러 가기' 버튼을 눌렀을 때 띄울 커스텀 다이얼로그 */
public class CustomResultBenefitDialog
{
    private Context context;

    public CustomResultBenefitDialog(Context context)
    {
        this.context = context;
    }

    public void callDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.result_benefit_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.show();

        final TextView dialog_ok = (TextView) dialog.findViewById(R.id.dialog_ok);
        dialog_ok.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

}
