package com.benefit.welfare.Custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.benefit.welfare.R;

import java.text.SimpleDateFormat;

// 푸시알림설정 스위치를 on으로 설정했을 때 나타나는 다이얼로그
public class CustomPushPermitDialog
{
    private Context context;

    public CustomPushPermitDialog(Context context)
    {
        this.context = context;
    }

    public void showPushDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.push_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(true); // 다이얼로그 바깥 영역을 눌러도 사라지게 한다
        dialog.show();

        SimpleDateFormat format = new SimpleDateFormat( "yyyy년 MM월 dd일");
        String format_time = format.format (System.currentTimeMillis());

        final TextView push_permitted_date_textview = dialog.findViewById(R.id.push_permitted_date_textview);
        push_permitted_date_textview.setText(format_time);

        final Button push_permit_btn = dialog.findViewById(R.id.push_permit_btn);
        // 확인 버튼을 누르면 다이얼로그를 없애기만 한다
        push_permit_btn.setOnClickListener(v -> dialog.dismiss());
    }

}
