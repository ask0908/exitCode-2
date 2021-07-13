package com.psj.welfare.custom;

import android.os.SystemClock;
import android.view.View;

/* 공유하기 중복 클릭 방지용으로 만든 추상 클래스 */
public abstract class OnSingleClickListener implements View.OnClickListener
{
    // 처음 클릭 후 0.6초 동안은 다시 클릭할 수 없다
    private static final long MIN_CLICK_INTERVAL = 600;

    // 처음 클릭 후 다시 클릭한 시간
    private long mLastClickTime;

    // 구현하는 액티비티/프래그먼트에서 호출될 추상 메서드
    // 이 메서드의 행동 양식은 이곳에서 정했기 때문에 액티비티/프래그먼트에선 다시 정할 필요가 없나?
    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View v)
    {
        // 언제 클릭했는가?
        long currentClickTime = SystemClock.uptimeMillis();
        // 첫 클릭 이후 다음 클릭 시간은 언제인가?
        long elapsedTime = currentClickTime - mLastClickTime;
        // 마지막 클릭 시간에 현재 클릭 시간을 저장해서 다음 클릭 시 비교할 때 사용한다
        mLastClickTime = currentClickTime;

        // 중복 클릭인 경우
        if (elapsedTime <= MIN_CLICK_INTERVAL)
        {
            return;
        }

        // if를 탈출했다면 중복 클릭이 아니기 때문에 추상 메서드 호출
        onSingleClick(v);
    }
}