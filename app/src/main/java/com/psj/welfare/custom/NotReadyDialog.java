package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.psj.welfare.R;

public class NotReadyDialog
{
    private Context context;

    public NotReadyDialog(Context context)
    {
        this.context = context;
    }

    public void showNotReadyDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.not_ready_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(true);
        dialog.show();

        final TextView not_ok = dialog.findViewById(R.id.not_ok);
        not_ok.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                dialog.dismiss();
            }
        });
    }

}
