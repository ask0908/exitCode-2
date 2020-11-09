package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.psj.welfare.R;

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
