# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/andrea/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class KEY to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-keepattributes SourceFile, LineNumberTable

-keep class com.android.vending.billing.**

-keep class android.support.v7.widget.** { *; }
-keep interface android.support.v7.widget.**

-keep class android.support.v13.app.** { *; }
-keep interface android.support.v13.app.**

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.**

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-dontwarn android.support.**