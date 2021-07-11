package com.psj.welfare.util;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.orhanobut.logger.Logger;

/* 앱 강제종료 여부를 확인하기 위해 만든 서비스, 매니페스트 밑부분에 등록돼 있다
프로세스를 강제종료시키는 시점은 알 수 없지만 Task를 종료하는 시점은 감지할 수 있다

Task : 사용자가 특정 작업을 수행할 때 상호작용하는 액티비티들의 모음, 액티비티가 열리는 순서대로 스택(백스택)으로 정렬된다.
  액티비티는 다른 앱의 액티비티를 시작할 수도 있다. 앱에서 이메일을 보내려는 경우 '이메일 보내기' 작업을 수행할 인텐트를 정의하고, 이메일 주소 및 메시지 같은 일부 데이터를
  포함할 수 있다.
  그 다음 이런 종류의 인텐트를 처리하기 위해 자신을 선언하는 다른 앱의 액티비티가 열린다. 이 경우 인텐트는 이메일을 보내는 것이므로, 이메일 앱의 '작성' 액티비티가 시작된다
  액티비티가 서로 다른 앱에서 제공될 수 있지만 안드로이드는 두 액티비티를 같은 Task에 유지해서 원활한 사용자 경험을 유지한다

Process : 앱 구성 요소가 시작되고 앱에 실행중인 다른 구성요소가 없는 경우 안드로이드 시스템은 단일 실행 쓰레드로 앱에 대한 새로운 리눅스 프로세스를 시작한다
  기본적으로 동일한 응용 프로그램의 모든 구성 요소는 같은 프로세스 및 쓰레드(메인 쓰레드)에서 실행된다. 그리고 기본적으로 모든 앱은 자체 프로세스에서 실행되며
  앱의 모든 구성요소는 해당 프로세스에서 실행된다

태스크 vs 프로세스 참고 : https://medium.com/@vikas.bajpayee/process-vs-task-53bbee024d33
코드 참고 : https://mine-it-record.tistory.com/228 */
public class UnCatchTaskService extends Service
{
    private final String TAG = this.getClass().getSimpleName();
    SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        super.onTaskRemoved(rootIntent);

        /* 미리보기의 나이, 성별, 지역을 선택하는 화면에서 앱이 강제종료됐을 경우, true 값을 쉐어드에 저장해서
        * SplashActivity가 실행됐을 때 이 쉐어드 값을 검사해 true라면 메인으로 가기, 알아보기 버튼이 있는 화면을 시작하게 한다 */
        Logger.d("onTaskRemoved() 호출");

        sharedPreferences = getApplicationContext().getSharedPreferences("app_pref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("force_stopped", "강제종료됨");
        editor.apply();

        // Task가 종료되는 시점에서 서비스도 같이 종료시키기 위해 호출
        stopSelf();
    }
}
