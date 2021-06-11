package com.psj.welfare.custom;

// 탈퇴하기 커스텀 다이얼로그에서 라디오 버튼 선택 시 값을 액티비티로 보내기 위해 만든 콜백 인터페이스
public interface MyWithdrawListener
{
    /* 위부터 순서대로 1~4번 라디오 버튼 선택 시 다이얼로그를 없애고 액티비티로 선택한 라디오버튼의 문자열을 보내는 메서드들이다 */
    void sendFirstValue(String value);
    void sendSecondValue(String second_value);
    void sendThirdValue(String third_value);
    void sendFourthValue(String fourth_value);
}
