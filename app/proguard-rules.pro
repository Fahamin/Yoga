
-dontwarn org.apache.commons.lang.builder.ToStringBuilder
-dontwarn org.slf4j.impl.StaticLoggerBinder
-ignorewarnings
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-keepattributes Signature
-keepclassmembers class com.livetv.configurator.nexus.kodiapps.model.* {
*;
}


-keepattributes *Annotation*
# in order to provide the most meaningful crash reports, add the following line:
-keepattributes SourceFile,LineNumberTable
# If you are using custom exceptions, add this line so that custom exception types are skipped during obfuscation:
-keep public class * extends java.lang.Exception
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**


# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-dontwarn com.firebase.**
-dontnote com.firebase.client.core.GaePlatform

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

-keep class com.livetv.configurator.nexus.kodiapps.* { *; }
# Picasso
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.internal.platform.*
# Please add these rules to your existing keep rules in order to
# This is generated automatically by the Android Gradle plugin.
-dontwarn android.os.ServiceManager
-dontwarn com.bun.miitmdid.core.MdidSdkHelper
-dontwarn com.bun.miitmdid.interfaces.IIdentifierListener
-dontwarn com.bun.miitmdid.interfaces.IdSupplier
-dontwarn com.google.firebase.iid.FirebaseInstanceId
-dontwarn com.google.firebase.iid.InstanceIdResult
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient$Info
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient
-dontwarn com.tencent.android.tpush.otherpush.OtherPushClient
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
-keep class androidx.lifecycle.LiveData { *; }
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

-keep public class com.google.android.gms.ads.**{
   public *;
}

# For old ads classes
-keep public class com.google.ads.**{
   public *;
}

# For mediation
-keepattributes *Annotation*

# Other required classes for Google Play Services
# Read more at http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
}
-dontwarn androidx.room.paging.**
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

# Rendescript
-keepclasseswithmembernames class * {
   native <methods>;
}

-keep class androidx.renderscript.** { *; }
-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}
-dontwarn com.squareup.okhttp.**
-keep class com.pushwoosh.** { *; }
-keep class com.arellomobile.** { *; }
-dontwarn com.pushwoosh.**
-dontwarn com.arellomobile.**
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
-keepattributes *Annotation*,EnclosingMethod,Signature,SourceFile,LineNumberTable
-keepnames class com.fasterxml.jackson.** { *; }
 -dontwarn com.fasterxml.jackson.databind.**
 -keep class org.codehaus.** { *; }
# My POJO class directory
-keep public class com.vpn.maxvpn.protonvpn.model.** {
  public void set*(***);
  public *** get*();
  public protected private *;
}
# Retrofit2
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# GSON Annotations
-keepclassmembers,allowobfuscation class * {

}
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class packagename.*
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod
-dontwarn android.os.ServiceManager*
-dontwarn com.bun.miitmdid.core.MdidSdkHelper*
-dontwarn com.bun.miitmdid.interfaces.IIdentifierListener*
-dontwarn com.bun.miitmdid.interfaces.IdSupplier*
-dontwarn com.google.firebase.iid.FirebaseInstanceId*
-dontwarn com.google.firebase.iid.InstanceIdResult*
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient$Info*
-dontwarn com.huawei.hms.ads.identifier.AdvertisingIdClient*
-dontwarn com.tencent.android.tpush.otherpush.OtherPushClient*
# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
# Understand the @Keep support annotation.
-keep class androidx.annotation.Keep

-keep @androidx.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}


-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile