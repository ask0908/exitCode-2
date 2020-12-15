package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.psj.welfare.R;
import com.psj.welfare.activity.MainTabLayoutActivity;

public class CustomAlertDialog
{
    private Context context;

    public CustomAlertDialog(Context context)
    {
        this.context = context;
    }

    public void showAlertDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.compatibility_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.show();

        // 예를 누르면 다이얼로그를 없애고 메인화면으로 이동한다
        final TextView alert_ok = dialog.findViewById(R.id.dialog_ok);
        alert_ok.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, MainTabLayoutActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        });

        // 아니오를 누르면 다이얼로그를 없애기만 한다
        final TextView alert_cancel = dialog.findViewById(R.id.dialog_cancel);
        alert_cancel.setOnClickListener(v -> dialog.dismiss());
    }

}
