package com.psj.welfare.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.psj.welfare.DetailReviewWrite;
import com.psj.welfare.R;

public class CustomReviewDialog
{
    private Context context;

    public CustomReviewDialog(Context context)
    {
        this.context = context;
    }

    public void showDialog()
    {
        Display display = ((DetailReviewWrite) context).getWindowManager().getDefaultDisplay();  // in Activity
        Point size = new Point();
        display.getRealSize(size); // or getSize(size)

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        ((WrittenReviewCheckActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int displayWidth = displayMetrics.widthPixels;
//        int displayHeight = displayMetrics.heightPixels;

        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_tutorial_dialog);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.show();

        final Button BtbCancle = dialog.findViewById(R.id.BtbCancle); //계속 작성 버튼
        final Button BtnBack = dialog.findViewById(R.id.BtnBack); //작성 취소 버튼
        final TextView delete_dialog_firsttext = dialog.findViewById(R.id.delete_dialog_firsttext);
        final TextView delete_dialog_secondtext = dialog.findViewById(R.id.delete_dialog_secondtext);
        final ConstraintLayout review_dialog_layout = dialog.findViewById(R.id.review_dialog_layout);

        delete_dialog_firsttext.setText("이 화면에서 벗어날 경우\n작성하시던 내용은 저장되지 않아요");
        delete_dialog_secondtext.setText("리뷰 작성을 취소하시겠어요?");
        BtbCancle.setText("계속작성");
        BtnBack.setText("작성취소");

        review_dialog_layout.getLayoutParams().width = (int) (size.x * 0.9);
        review_dialog_layout.getLayoutParams().height = (int) (size.y * 0.26);

        BtbCancle.setOnClickListener(v ->
        {
            dialog.dismiss();
        });

        BtnBack.setOnClickListener(v ->
        {
            dialog.dismiss();
            Toast.makeText(context, "리뷰 수정이 취소 되었습니다", Toast.LENGTH_SHORT).show();
            ((DetailReviewWrite) context).finish();
        });
    }

}
