package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.psj.welfare.R;
import com.psj.welfare.activity.ChooseFirstInterestActivity;

public class CustomInterestDialog
{
    private Context context;

    public CustomInterestDialog(Context context)
    {
        this.context = context;
    }

    public void showDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_interest_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.show();

        // 뒤로가기 눌렀을 경우
        final Button interest_yes = dialog.findViewById(R.id.interest_back);
        interest_yes.setOnClickListener(v ->
        {
            dialog.dismiss();
            ((ChooseFirstInterestActivity) context).finish();
        });

        // 취소하기 눌렀을 경우
        final Button interest_no = dialog.findViewById(R.id.interest_cancel);
        interest_no.setOnClickListener(v ->
        {
            dialog.dismiss();
        });
    }

}
