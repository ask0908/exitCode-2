package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.psj.welfare.R;
import com.psj.welfare.activity.GetUserInformationActivity;
import com.psj.welfare.activity.MainTabLayoutActivity;

public class FillAlertDialog
{
    private Context context;

    public FillAlertDialog(Context context)
    {
        this.context = context;
    }

    public void showAlert()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fill_alert_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(true);
        dialog.show();

        final TextView ok = dialog.findViewById(R.id.fill_alert_ok);
        ok.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                dialog.dismiss();
                GetUserInformationActivity activity = (GetUserInformationActivity) GetUserInformationActivity.activity;
                activity.finish();
                Intent intent = new Intent(context, MainTabLayoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });

        final TextView no = dialog.findViewById(R.id.fill_alert_no);
        no.setOnClickListener(new OnSingleClickListener()
        {
            @Override
            public void onSingleClick(View v)
            {
                dialog.dismiss();
            }
        });
    }

}
