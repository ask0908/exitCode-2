# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keepattributes LineNumberTable,SourceFile
-keepattributes SourceFile,LineNumberTable
#-adaptclassstrings
# 카카오 SDK를 제외하고 코드 축소, 난독화, 최적화 진행
# https://developers.kakao.com/docs/latest/ko/getting-started/sdk-android#select-module
#-keep class com.kakao.sdk.**.model.* { <fields>; }
#-keep class * extends com.google.gson.TypeAdapter

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}
-keep class android.net.*.* { *; }
# 로그를 일일이 찾아서 삭제하지 않아도 apk 파일 생성 시 로그를 없애주는 부분
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}