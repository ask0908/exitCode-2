package com.psj.welfare.Custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.psj.welfare.R;

public class CustomWheelDialog
{
    private Context context;

    // 나이
    final String[] age_array = {"10대", "20대", "30대", "40대", "50대", "60대", "70대"};
    // 성별
    final String[] gender_array = {"남자", "여자"};

    // 액티비티로 값을 보내기 위해 만든 인터페이스
    // 안의 추상 메서드 인자로 나이, 성별을 보낸다
    private onDialogListener dialogListener;

    // 액티비티로 보낼 값들
    String dialog_age, dialog_gender;

    public CustomWheelDialog(Context context, onDialogListener dialogListener)
    {
        this.context = context;
        this.dialogListener = dialogListener;
    }

    public void showDialog()
    {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_wheel_dialog);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
        dialog.show();

        final NumberPicker age_wheel = dialog.findViewById(R.id.age_wheel);
        final Button user_information_btn = dialog.findViewById(R.id.user_information_btn);
        final Button man_btn = dialog.findViewById(R.id.man_btn);
        final Button woman_btn = dialog.findViewById(R.id.woman_btn);

        man_btn.setOnClickListener(v -> {
            // 남자 버튼을 클릭하면 여자 버튼의 클릭 상태를 false로 돌려야 한다
            man_btn.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_primary_dark));
            man_btn.setTextColor(ContextCompat.getColor(context, R.color.colorMainWhite));
            woman_btn.setPressed(false);
            woman_btn.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_main_before));
            woman_btn.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
            dialog_gender = "남자";
        });

        woman_btn.setOnClickListener(v -> {
            woman_btn.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_primary_dark));
            woman_btn.setTextColor(ContextCompat.getColor(context, R.color.colorMainWhite));
            man_btn.setPressed(false);
            man_btn.setBackground(ContextCompat.getDrawable(context, R.drawable.btn_main_before));
            man_btn.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
            dialog_gender = "여자";
        });

        age_wheel.setMaxValue(age_array.length - 1);
        age_wheel.setMinValue(0);
        age_wheel.setDisplayedValues(age_array);
        age_wheel.setWrapSelectorWheel(true);

        user_information_btn.setOnClickListener(v -> {
            if (dialog_gender == null)
            {
                Toast.makeText(context, "성별을 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
            else
            {
                dialog.dismiss();
                switch (age_wheel.getValue())
                {
                    case 0 :
                        // 나이 휠에서 0을 고르면 10대다
                        dialog_age = "10대";
                        break;

                    case 1 :
                        dialog_age = "20대";
                        break;

                    case 2 :
                        dialog_age = "30대";
                        break;

                    case 3 :
                        dialog_age = "40대";
                        break;

                    case 4 :
                        dialog_age = "50대";
                        break;

                    case 5 :
                        dialog_age = "60대";
                        break;

                    case 6 :
                        dialog_age = "70대";
                        break;

                    default:
                        break;

                }
                // 입력된 값을 액티비티로 전송
                dialogListener.receiveData(dialog_age, dialog_gender);
            }
//            dialog.dismiss();
//            switch (age_wheel.getValue())
//            {
//                case 0 :
//                    // 나이에서 0을 고르면 10대다
//                    dialog_age = "10대";
//                    break;
//
//                case 1 :
//                    dialog_age = "20대";
//                    break;
//
//                case 2 :
//                    dialog_age = "30대";
//                    break;
//
//                case 3 :
//                    dialog_age = "40대";
//                    break;
//
//                case 4 :
//                    dialog_age = "50대";
//                    break;
//
//                case 5 :
//                    dialog_age = "60대";
//                    break;
//
//                case 6 :
//                    dialog_age = "70대";
//                    break;
//
//                default:
//                    break;
//
//            }
//            // 입력된 값을 액티비티로 전송
//            dialogListener.receiveData(dialog_age, dialog_gender);
        });

    }

    public interface onDialogListener
    {
        void receiveData(String age, String gender);
    }

}
