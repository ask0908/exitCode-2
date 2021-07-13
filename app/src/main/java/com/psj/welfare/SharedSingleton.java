package com.psj.welfare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedSingleton {

    private static SharedSingleton sharedSingleton = new SharedSingleton();
    private static SharedPreferences shared ;
    private static SharedPreferences.Editor editor;

    //sharedpreference 이름
    private final static String SheredName = "app_pref";

    //다른 사람이 인스턴스화하는 것을 방지하기 위해 private로 만든다
    private SharedSingleton(){}

    //일반적으로 singletone 방법
//    public static LoginSingleton getInstance(){
//        if(loginSingleton == null){
//            loginSingleton = new LoginSingleton();
//        }
//        return loginSingleton;
//    }

    //shared를 singleton으로 사용하는 방법, shared객체를 한번만 선언해서 static 메모리 한번만 할당 받음(같은 객체를 계속 사용)
    public static SharedSingleton getInstance(Context context){
        if(shared == null){
            shared = context.getSharedPreferences(SheredName, Activity.MODE_PRIVATE);
            editor = shared.edit();
        }
        return sharedSingleton;
    }

    //해당 쉐어드 값 지우기
    public void setRemoveShared(String remove){
        editor.remove(remove);
        editor.apply();
    }

    //로그인 했는지 여부값 가져오기
    public boolean getBooleanLogin(){
        return shared.getBoolean("user_login",false);
    }

    //로그인 여부 shared에 입력하기
    public void setBooleanLogin(boolean user_login){
        editor.putBoolean("user_login", user_login);
        editor.apply();
    }

    //SessionId값 가져오기
    public String getSessionId(){
        return shared.getString("sessionId",null);
    }

    //SessionId값 shared에 입력하기
    public void setSessionId(String sessionId){
        editor.putString("sessionId", sessionId);
        editor.apply();
    }

    //Token값 가져오기
    public String getToken(){
        return shared.getString("token",null);
    }

    //Token값 shared에 입력하기
    public void setToken(String token){
        editor.putString("token", token);
        editor.apply();
    }

    //nickname값 가져오기
    public String getNickname(){
        return shared.getString("user_nickname",null);
    }

    //nickname값 shared에 입력하기
    public void setNickname(String nickname){
        editor.putString("user_nickname", nickname);
        editor.apply();
    }

    //관심사 선택 했는지 값 가져오기
    public boolean getBooleanInterst(){
        return shared.getBoolean("interest_select",false);
    }

    //관심사 선택 했는지 값 shared에 입력하기
    public void setBooleanInterst(boolean interest_select){
        editor.putBoolean("interest_select", interest_select);
        editor.apply();
    }

    //FCMtoken값 가져오기
    public String getFCMtoken(){
        return shared.getString("fcm_token",null);
    }

    //FCMtoken값 shared에 입력하기
    public void setFCMtoken(String FCMtoken){
        editor.putString("fcm_token", FCMtoken);
        editor.apply();
    }

    //미리보기 선택 했는지 값 가져오기 (미리보기 안하고 건너뛰기 해도 값은 true로 받아야 한다)
    public boolean getBooleanPreview(){
        return shared.getBoolean("is_preview",false);
    }

    //미리보기 선택 했는지 값 shared에 입력하기 (미리보기 안하고 건너뛰기 해도 값은 true로 받아야 한다)
    public void setBooleanPreview(boolean BooleanPreview){
        editor.putBoolean("is_preview", BooleanPreview);
        editor.apply();
    }
}
