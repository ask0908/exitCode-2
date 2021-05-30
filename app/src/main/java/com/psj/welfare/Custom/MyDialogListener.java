package com.psj.welfare.custom;

public interface MyDialogListener
{
    // 중복 체크 후 T/F 값을 프래그먼트로 보내는 메서드
    void onDuplicatedCheck(boolean isDuplicated);
    // 닉네임 변경 클릭 시 변경된 닉네임을 프래그먼트로 보내는 메서드
    void onPositiveClicked(String edited_str);
}
