package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.psj.welfare.R;

import java.text.SimpleDateFormat;

// 푸시알림설정 스위치를 off로 설정했을 때 나타나는 다이얼로그
public class CustomPushDenyDialog
{
    private Context context;
    private SharedPreferences sharedPreferences;

    public CustomPushDenyDialog(Context context)
    {
        this.context = context;
    }

    public void showDenyDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.push_deny_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(true); // 다이얼로그 바깥 영역을 눌러도 사라지게 한다
        dialog.show();

        SimpleDateFormat format = new SimpleDateFormat( "yyyy년 MM월 dd일");
        String format_time = format.format (System.currentTimeMillis());

        final TextView push_denied_date_textview = dialog.findViewById(R.id.push_denied_date_textview);
        push_denied_date_textview.setText(format_time);

        sharedPreferences = context.getSharedPreferences("app_pref", 0);
        boolean checked = sharedPreferences.getBoolean("fcm_canceled", false);
        if (checked)
        {
            dialog.dismiss();
        }

        final Button push_deny_btn = dialog.findViewById(R.id.push_deny_btn);
        push_deny_btn.setOnClickListener(v -> dialog.dismiss());
    }

}
