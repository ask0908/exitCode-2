package com.psj.welfare;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.util.Objects;

public class CustomTutorialDialog extends Dialog {

    private Button BtbCancle; //취소 버튼
    private Button BtnBack; //뒤로가기 버튼


    public CustomTutorialDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_tutorial_dialog);

        //다이얼로그의 배경을 투명으로 만든다
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        BtbCancle = findViewById(R.id.BtbCancle); //취소 버튼
        BtnBack = findViewById(R.id.BtnBack); //뒤로가기 버튼
    }
}
